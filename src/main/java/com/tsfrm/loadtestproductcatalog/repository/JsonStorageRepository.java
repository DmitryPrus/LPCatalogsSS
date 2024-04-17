package com.tsfrm.loadtestproductcatalog.repository;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.tsfrm.loadtestproductcatalog.domain.entity.LocationEntity;
import com.tsfrm.loadtestproductcatalog.domain.entity.OrgEntity;
import com.tsfrm.loadtestproductcatalog.domain.entity.VdiProductEntity;
import com.tsfrm.loadtestproductcatalog.domain.jsonEntity.BaseJsonEntity;
import com.tsfrm.loadtestproductcatalog.domain.jsonEntity.LocationJsonEntity;
import com.tsfrm.loadtestproductcatalog.domain.jsonEntity.OrgJsonEntity;
import com.tsfrm.loadtestproductcatalog.domain.jsonEntity.VdiProductJsonEntity;
import com.tsfrm.loadtestproductcatalog.service.JsonEntityConverter;
import lombok.Getter;
import lombok.Setter;

import java.io.File;
import java.io.IOException;
import java.util.*;

public class JsonStorageRepository {

    private final String PRODUCTS_STORAGE = "src/main/resources/storage/product-storage.json";
    private final String LOCATIONS_STORAGE = "src/main/resources/storage/location-storage.json";
    private final String ORGS_STORAGE = "src/main/resources/storage/org-storage.json";

    private JsonEntityConverter converter;

    @Setter
    @Getter
    private Set<OrgEntity> orgs;
    @Setter
    @Getter
    private Map<String, HashSet<VdiProductEntity>> locationProductMap;


    public JsonStorageRepository() {
        this.converter = new JsonEntityConverter();
        this.orgs = new HashSet<>();
        this.locationProductMap = new HashMap<>();
        readProcessing();
    }


    public void writeProcessing() {
        var productJsonList = new HashSet<VdiProductJsonEntity>();
        var locationJsonList = new HashSet<LocationJsonEntity>();
        var orgJsonList = new HashSet<OrgJsonEntity>();

        for (OrgEntity org : orgs) {
            for (LocationEntity loc : org.getLocations()) {
                var productsForLoc = locationProductMap.get(loc.getLocationId());
                productsForLoc.forEach(p -> {
                    var productJson = converter.productToJson(p, loc.getLocationId());
                    productJsonList.add(productJson);
                });
                locationJsonList.add(converter.locationToJson(loc, org.getOrg()));
            }
            orgJsonList.add(converter.orgToJson(org));
        }

        writeJsonFile(productJsonList, PRODUCTS_STORAGE);
        writeJsonFile(locationJsonList, LOCATIONS_STORAGE);
        writeJsonFile(orgJsonList, ORGS_STORAGE);
    }


    public void readProcessing() {
        var productsJson = readProductsJson(PRODUCTS_STORAGE);
        var locationsJson = readLocationsJson(LOCATIONS_STORAGE);
        var orgsJson = readOrgsJson(ORGS_STORAGE);

        var locProductMap = new HashMap<String, HashSet<VdiProductEntity>>();
        var orgLocMap = new HashMap<String, HashSet<LocationEntity>>();

        productsJson.removeIf(Objects::isNull);
        productsJson.forEach(pj -> {
            locProductMap.putIfAbsent(pj.getLocation(), new HashSet<>());
            locProductMap.get(pj.getLocation()).add(converter.jsonToProduct(pj));
        });

        locationsJson.forEach(loc -> {
            var locProducts = locProductMap.get(loc.getLocationId());
            orgLocMap.putIfAbsent(loc.getOrgId(), new HashSet<>());
            orgLocMap.get(loc.getOrgId()).add(converter.jsonToLocation(loc, locProducts));
        });

        this.locationProductMap = locProductMap;
        orgsJson.forEach(o -> this.orgs.add(converter.jsonToOrg(o, orgLocMap.get(o.getOrg()))));
    }

    private void writeJsonFile(Set<? extends BaseJsonEntity> data, String filePath) {
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
        objectMapper.configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false);

        try {
            File outputFile = new File(filePath);
            objectMapper.writeValue(outputFile, data);

            System.out.println("Data successfully added: " + outputFile.getAbsolutePath());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private List<VdiProductJsonEntity> readProductsJson(String filePath) {
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
        objectMapper.configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false);

        try {
            File inputFile = new File(filePath);
            return objectMapper.readValue(inputFile, new TypeReference<>() {
            });
        } catch (Exception e) {
            e.printStackTrace();
        }

        return new ArrayList<>();
    }

    private List<LocationJsonEntity> readLocationsJson(String filePath) {
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
        objectMapper.configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false);

        try {
            File inputFile = new File(filePath);
            return objectMapper.readValue(inputFile, new TypeReference<>() {
            });
        } catch (Exception e) {
            e.printStackTrace();
        }

        return new ArrayList<>();
    }

    private List<OrgJsonEntity> readOrgsJson(String filePath) {
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
        objectMapper.configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false);

        try {
            File inputFile = new File(filePath);
            return objectMapper.readValue(inputFile, new TypeReference<>() {
            });
        } catch (Exception e) {
            e.printStackTrace();
        }

        return new ArrayList<>();
    }
}
