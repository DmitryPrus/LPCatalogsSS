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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class JsonStorageRepository {

    private final String PRODUCTS_STORAGE = "src/main/resources/storage/product-storage.json";
    private final String LOCATIONS_STORAGE = "src/main/resources/storage/location-storage.json";
    private final String ORGS_STORAGE = "src/main/resources/storage/org-storage.json";

    @Setter
    @Getter
    private List<OrgEntity> orgs;
    private JsonEntityConverter converter;
    private List<VdiProductEntity> products;


    public JsonStorageRepository(){
        this.converter = new JsonEntityConverter();
        this.products = new ArrayList<>();
        this.orgs = new ArrayList<>();
    }


    public void writeProcessing() {
        var productJsonList = new ArrayList<VdiProductJsonEntity>();
        var locationJsonList = new ArrayList<LocationJsonEntity>();
        var orgJsonList = new ArrayList<OrgJsonEntity>();

        for (OrgEntity org : orgs) {
            for (LocationEntity loc : org.getLocations()) {
                var productsForLoc = products.stream()
                        .filter(prod -> loc.getProductIds().contains(prod.getId()))
                        .toList();
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

        var locProductMap = new HashMap<String, List<VdiProductEntity>>();
        var orgLocMap = new HashMap<String, List<LocationEntity>>();

        productsJson.forEach(pj -> {
            locProductMap.putIfAbsent(pj.getLocation(), new ArrayList<>());
            locProductMap.get(pj.getLocation()).add(converter.jsonToProduct(pj));
        });
        locationsJson.forEach(loc -> {
            orgLocMap.putIfAbsent(loc.getOrgId(), new ArrayList<>());
            orgLocMap.get(loc.getOrgId()).add(converter.jsonToLocation(loc, locProductMap.get(loc.getLocationId())));
        });
        this.products = productsJson.stream().map(converter::jsonToProduct).toList();

        orgsJson.forEach(o -> {
            this.orgs.add(converter.jsonToOrg(o, orgLocMap.get(o.getOrg())));
        });

    }

    private void writeJsonFile(List<? extends BaseJsonEntity> data, String filePath) {
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
