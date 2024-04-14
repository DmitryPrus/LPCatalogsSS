package com.tsfrm.loadtestproductcatalog.repository;

import com.tsfrm.loadtestproductcatalog.domain.entity.LocationEntity;
import com.tsfrm.loadtestproductcatalog.domain.entity.OrgEntity;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

import java.sql.*;
import java.util.*;

public class OrgRepository extends BaseRepository<OrgEntity> {

    private static final Logger log = LogManager.getLogger(OrgRepository.class);

    public OrgRepository(JdbcConfig config) {
        super(config);
    }

    public List<OrgEntity> getVdi2OrgEntitiesFullList() {
        try {
            var orgList = getAllOrgEntitiesWithoutLocations();
            log.info("OrgLis created with quantity " + orgList.size());
            log.info("Fill orglist by locations and productIds started");
            fillOrgsByLocationsAndProducts(orgList);

            orgList.removeIf(org -> {
                var locations = org.getLocations();
                return locations == null || locations.isEmpty();
            });
            log.info("Cleared all organizations having no binded locations");

            return orgList;
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Retrives organizations and fill orgId and userKey only.
     * Regarding to information from Dan VDI 2 use only those orgs which has null in 'location' column of vdiuserkey table
     * https://365retailmarkets.atlassian.net/browse/SOS-47770
     */
    private List<OrgEntity> getAllOrgEntitiesWithoutLocations() throws SQLException {
        var resultList = new ArrayList<OrgEntity>();
        var query = "SELECT ORG, ANY_VALUE(USERKEY) AS USERKEY FROM VDIUSERKEY K " +
                "INNER JOIN vdiproviderinfo VPI ON K.VDIPROVIDER = VPI.ID " +
                "WHERE K.ORG IS NOT NULL and K.USERKEY is not null " +
                "GROUP BY ORG " +
                "LIMIT 200;";

        try (
                var connection = DriverManager.getConnection(config.dbUrl, config.dbUser, config.dbPassword);
                var preparedStatement = connection.prepareStatement(query)
        ) {
            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                while (resultSet.next()) {
                    var org = new OrgEntity();
                    org.setOrg(resultSet.getString("org"));
                    org.setUserKey(resultSet.getString("userkey"));
                    resultList.add(org);
                }
            }
        }
        return resultList;
    }

    private void fillOrgsByLocationsAndProducts(List<OrgEntity> orgs) throws SQLException {
        var orgIds = orgs.stream()
                .map(OrgEntity::getOrg).toList();

        if (orgIds.isEmpty()) throw new RuntimeException("There are no organizations for VDI2 in database");

        var queryProduct = "select PRODUCT, LOCATION, ORG from productlocation where productlocation.ORG in (";
        for (int i = 0; i < orgIds.size(); i++) {
            queryProduct += "?";
            if (i < orgIds.size() - 1) {
                queryProduct += ",";
            }
        }
        queryProduct += ")";

        //org id <locationId, Set of productIds>
        var orgLocationsMap = new HashMap<String, Map<String, Set<String>>>();

        try (
                var connection = DriverManager.getConnection(config.dbUrl, config.dbUser, config.dbPassword);
                var preparedStatement = connection.prepareStatement(queryProduct)
        ) {
            for (int i = 0; i < orgIds.size(); i++) {
                preparedStatement.setString(i + 1, orgIds.get(i));
            }

            log.info("SQL Query: " + queryProduct);

            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                while (resultSet.next()) {
                    var orgId = resultSet.getString("ORG");
                    var locationID = resultSet.getString("LOCATION");
                    var productID = resultSet.getString("PRODUCT");

                    orgLocationsMap.putIfAbsent(orgId, new HashMap<>());
                    orgLocationsMap.get(orgId).putIfAbsent(locationID, new HashSet<>());
                    orgLocationsMap.get(orgId).get(locationID).add(productID);
                }
            }
        }

        log.info("Query finished. Fill of Organizations by locations and products started.");

        for (OrgEntity o : orgs) {
            var locationMap = orgLocationsMap.get(o.getOrg());
            if (locationMap == null) continue;
            o.setLocations(new ArrayList<>());
            for (var loc : locationMap.entrySet()) {
                var locId = loc.getKey();
                var productIds = new ArrayList<>(loc.getValue());
                if (!productIds.isEmpty()) {
                    o.getLocations().add(new LocationEntity(locId, productIds));
                }
            }
        }
    }

    /**
     * not used
     */
    @Override
    public OrgEntity findById(String id) throws SQLException {
        OrgEntity org = null;
        var query = "SELECT * FROM vdiuserkey WHERE vdiuserkey.ORG = ?";

        try (
                var connection = DriverManager.getConnection(config.dbUrl, config.dbUser, config.dbPassword);
                var preparedStatement = connection.prepareStatement(query)
        ) {
            preparedStatement.setString(1, id);
            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                if (resultSet.next()) {
                    org = new OrgEntity();
                    org.setOrg(resultSet.getString("org"));
                    org.setUserKey(resultSet.getString("userkey"));
                }
            }
        }
        return org;
    }
}
