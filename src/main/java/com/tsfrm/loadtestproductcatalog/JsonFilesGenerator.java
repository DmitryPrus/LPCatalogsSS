package com.tsfrm.loadtestproductcatalog;

import com.tsfrm.loadtestproductcatalog.domain.entity.LocationEntity;
import com.tsfrm.loadtestproductcatalog.domain.entity.OrgEntity;
import com.tsfrm.loadtestproductcatalog.domain.entity.VdiProductEntity;
import com.tsfrm.loadtestproductcatalog.repository.JdbcConfig;
import com.tsfrm.loadtestproductcatalog.repository.JsonStorageRepository;
import com.tsfrm.loadtestproductcatalog.service.RunTestService;

import java.sql.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

public class JsonFilesGenerator {

    JdbcConfig config = new JdbcConfig();
    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    JsonStorageRepository jsonRepository = new JsonStorageRepository();
    public int locationsMinimum = System.getenv("LOCATIONS_PER_OPERATOR_MINIMUM") != null ? Integer.parseInt(System.getenv("LOCATIONS_PER_OPERATOR_MINIMUM")) : 1;


    public static void main(String[] args) throws SQLException, ClassNotFoundException {
//        logic to write JSON file from database
        JsonFilesGenerator jfg = new JsonFilesGenerator();
        jfg.readDatabaseDataAndStoreToJson();
//        String url = System.getenv("DESTINATION_URL") != null ? System.getenv("DESTINATION_URL") : "http://localhost:8082/mmsproducts/1/localtest";
//        Integer threadsQuantity = System.getenv("OUTBOUND_THREADS_QUANTITY") != null ? Integer.parseInt(System.getenv("OUTBOUND_THREADS_QUANTITY")) : 2;
//        RunTestService rt = new RunTestService(url, "", threadsQuantity);
        System.out.println();
    }

    private void readDatabaseDataAndStoreToJson() throws SQLException {
        var orgList = getAllOrgEntitiesWithoutLocations();
        System.out.println("OrgLis created with quantity " + orgList.size());
        System.out.println("Fill orglist by locations");
        fillByLocations(orgList);
        System.out.println("Fill by products");
        fillOrgsByLocationsAndProducts(orgList);

        orgList.removeIf(org -> {
            var locations = org.getLocations();
            return locations == null ||
                    locations.size() < locationsMinimum;
        });

        var locOrgLocProdMap = jsonRepository.getOrgLocProductMap();
        for (OrgEntity o : orgList) {
            locOrgLocProdMap.putIfAbsent(o.getOrg(), new HashMap<>());
            for (LocationEntity l : o.getLocations()) {
                locOrgLocProdMap.get(o.getOrg()).putIfAbsent(l.getLocationId(), new HashSet<>());
                locOrgLocProdMap.get(o.getOrg()).get(l.getLocationId()).addAll(findAllByProductIds(l.getProductIds()));
            }
        }

        System.out.println("Cleared all organizations having no binded locations");
        jsonRepository.setOrgs(new HashSet<>(orgList));
        jsonRepository.setOrgLocProductMap(locOrgLocProdMap);
        jsonRepository.writeProcessing();
        System.out.println("Json fillment processing finished");
    }


    /**
     * Retrives organizations and fill orgId and userKey only.
     * Regarding to information from Dan VDI 2 use only those orgs which has null in 'location' column of vdiuserkey table
     * https://365retailmarkets.atlassian.net/browse/SOS-47770
     */
    private List<OrgEntity> getAllOrgEntitiesWithoutLocations() throws SQLException {
        var resultList = new ArrayList<OrgEntity>();
        var query = String.format("""
                        SELECT o.ID as org, vuk.USERKEY
                        FROM org o
                                 INNER JOIN vdiuserkey vuk ON o.ID = vuk.ORG
                                 INNER JOIN vdiproviderinfo vp ON vuk.VDIPROVIDER = vp.ID
                                 INNER JOIN sfecfg c ON c.NAME = o.ID
                        WHERE c.TYPE = 'VDIENABLE'
                          AND c.VALUE = 'Y'
                          AND c.CFGTYPE = 'ORG'
                          AND vp.NAME = '%s'
                        limit 200;
                        """,
                "April_01"
        );

        try (
                var connection = DriverManager.getConnection(config.dbUrl, config.dbUser, config.dbPassword);
                var preparedStatement = connection.prepareStatement(query)
        ) {
            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                while (resultSet.next()) {
                    var org = new OrgEntity();
                    org.setOrg(resultSet.getString("org"));
                    org.setUserKey(resultSet.getString("USERKEY"));
                    resultList.add(org);
                }
            }
        }
        return resultList;
    }

    private void fillByLocations(List<OrgEntity> orgs) throws SQLException {
        var orgIds = orgs.stream().map(OrgEntity::getOrg).toList();
        if (orgIds.isEmpty()) throw new RuntimeException("There are no organizations for VDI2 in database");

        var vdiEnableLocations = """
                SELECT l.id  , vl.USERKEY
                                          FROM vdiuserkey vl
                                                   INNER JOIN vdiproviderinfo vp ON vl.vdiprovider = vp.id
                                                   INNER JOIN location l ON vl.location = l.id
                                                   INNER JOIN sfecfg c ON c.name = l.id
                                          WHERE c.type = 'VDIENABLE'
                                            AND c.value = 'Y'
                                            AND c.cfgtype = 'LOC'
                                            AND vp.name = 'April_01'
                                            AND l.org = ?;
                """;

        System.out.println("SQL Query: " + vdiEnableLocations);
        try (
                var connection = DriverManager.getConnection(config.dbUrl, config.dbUser, config.dbPassword);
                var preparedStatement = connection.prepareStatement(vdiEnableLocations)
        ) {
            for (OrgEntity org : orgs) {
                preparedStatement.setString(1, org.getOrg());
                org.setLocations(new ArrayList<>());
                try (ResultSet resultSet = preparedStatement.executeQuery()) {
                    while (resultSet.next()) {
                        var locationID = resultSet.getString("id");
                        var locationUserKey = resultSet.getString("USERKEY");
                        var locAtionEntity = new LocationEntity();
                        locAtionEntity.setLocationId(locationID);
                        locAtionEntity.setLocationUserKey(locationUserKey);
                        locAtionEntity.setProductIds(new ArrayList<>());
                        org.getLocations().add(locAtionEntity);
                    }
                }
            }
        }
    }

    private void fillOrgsByLocationsAndProducts(List<OrgEntity> orgs) throws SQLException {
        var orgIds = orgs.stream().map(OrgEntity::getOrg).toList();
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

            System.out.println("SQL Query: " + queryProduct);

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

        System.out.println("Query finished. Fill of Organizations by locations and products started.");

        for (OrgEntity o : orgs) {
            var locationMap = orgLocationsMap.get(o.getOrg());
            if (locationMap == null) continue;
            for (var loc : locationMap.entrySet()) {
                var locId = loc.getKey();
                if (locId == null) continue;
                var productIds = new ArrayList<>(loc.getValue());
                if (!productIds.isEmpty()) {
                    o.getLocations().forEach(oLoc -> {
                        if (locId.equals(oLoc.getLocationId())) {
                            oLoc.setProductIds(productIds);
                        }
                    });
                }
            }
        }
    }

    public List<VdiProductEntity> findAllByProductIds(List<String> productIds) {
        if (productIds == null || productIds.isEmpty()) return new ArrayList<>();
        List<VdiProductEntity> resultList = new ArrayList<>();

        String query = "SELECT * FROM product WHERE product.id in (";
        for (int i = 0; i < productIds.size(); i++) {
            query += "?";
            if (i < productIds.size() - 1) {
                query += ",";
            }
        }
        query += ") AND product.SCANCODE is not null AND product.userkey is not null";

        try (
                Connection connection = DriverManager.getConnection(config.dbUrl, config.dbUser, config.dbPassword);
                PreparedStatement preparedStatement = connection.prepareStatement(query)
        ) {
            for (int i = 0; i < productIds.size(); i++) {
                preparedStatement.setString(i + 1, productIds.get(i));
            }
            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                while (resultSet.next()) {
                    var product = new VdiProductEntity();
                    product.setId(resultSet.getString("id"));
                    product.setOrg(resultSet.getString("org"));
                    product.setName(resultSet.getString("name"));
                    product.setShortname(resultSet.getString("shortname"));
                    product.setUpc(resultSet.getString("upc"));
                    product.setScancode(resultSet.getString("scancode"));
                    product.setCategory1(resultSet.getString("category1"));
                    product.setCategory2(resultSet.getString("category2"));
                    product.setCategory3(resultSet.getString("category3"));
                    product.setStatus(resultSet.getString("status"));
                    product.setUserkey(resultSet.getString("userkey"));
                    product.setCost(resultSet.getBigDecimal("cost"));
                    product.setPrice(resultSet.getBigDecimal("price"));
                    product.setTax(resultSet.getBigDecimal("tax"));
                    product.setTax2(resultSet.getBigDecimal("tax2"));
                    product.setTax3(resultSet.getBigDecimal("tax3"));
                    product.setTax4(resultSet.getBigDecimal("tax4"));
                    product.setPoints(resultSet.getInt("points"));
                    product.setMinstock(resultSet.getString("minstock"));
                    product.setMaxstock(resultSet.getString("maxstock"));

                    var dateCreated = resultSet.getString("datecreated");
                    var lastupdated = resultSet.getString("lastupdated");
                    LocalDateTime createdToSet = null;
                    LocalDateTime updatedToSet = null;

                    try {
                        createdToSet = LocalDateTime.parse(dateCreated, formatter);
                    } catch (Exception e) {
                        createdToSet = LocalDateTime.now();
                    }

                    try {
                        updatedToSet = LocalDateTime.parse(lastupdated, formatter);
                    } catch (Exception e) {
                        updatedToSet = LocalDateTime.now();
                    }

                    product.setDatecreated(createdToSet);
                    product.setLastupdated(updatedToSet);
                    resultList.add(product);
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return resultList;
    }

}
