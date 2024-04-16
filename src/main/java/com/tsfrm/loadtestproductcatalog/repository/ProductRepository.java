package com.tsfrm.loadtestproductcatalog.repository;

import com.tsfrm.loadtestproductcatalog.domain.entity.VdiProductEntity;

import java.sql.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

public class ProductRepository extends BaseRepository<VdiProductEntity> {

    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    public ProductRepository(JdbcConfig config) {
        super(config);
    }


    @Override
    public VdiProductEntity findById(String id) {
        VdiProductEntity product = null;
        String query = "SELECT * FROM product WHERE product.id = ?";
        try (
                Connection connection = DriverManager.getConnection(config.dbUrl, config.dbUser, config.dbPassword);
                PreparedStatement preparedStatement = connection.prepareStatement(query)
        ) {
            preparedStatement.setString(1, id);
            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                if (resultSet.next()) {
                    product = new VdiProductEntity();
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
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return product;
    }

    public List<VdiProductEntity> findAllByProductIds(List<String> productIds) {
        List<VdiProductEntity> resultList = new ArrayList<>();

        String query = "SELECT * FROM product WHERE product.id in (";
        for (int i = 0; i < productIds.size(); i++) {
            query += "?";
            if (i < productIds.size() - 1) {
                query += ",";
            }
        }
        query += ")";

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

    public List<String> getAllProductIdList() {
        List<String> productIdList = null;
        try (
                Connection connection = DriverManager.getConnection(config.dbUrl, config.dbUser, config.dbPassword);
                Statement statement = connection.createStatement();
                ResultSet resultSet = statement.executeQuery("SELECT id FROM product")
        ) {
            productIdList = new ArrayList<>(100000);

            while (resultSet.next()) {
                productIdList.add(resultSet.getString("id"));
            }

        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return productIdList;
    }


    public boolean isIdExists(String productId) {
        String query = "SELECT id FROM product WHERE product.id = ?";
        try (
                Connection connection = DriverManager.getConnection(config.dbUrl, config.dbUser, config.dbPassword);
                PreparedStatement preparedStatement = connection.prepareStatement(query)
        ) {
            preparedStatement.setString(1, productId);
            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                return resultSet.next();
            }

        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }
}
