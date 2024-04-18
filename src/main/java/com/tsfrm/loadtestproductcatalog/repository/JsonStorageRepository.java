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

    public final String PRODUCTS_STORAGE = System.getenv("PRODUCTS_STORAGE_PATH") != null ? System.getenv("PRODUCTS_STORAGE_PATH") : "src/main/resources/storage/product-storage.json";
    public final String LOCATIONS_STORAGE = System.getenv("LOCATIONS_STORAGE_PATH") != null ? System.getenv("LOCATIONS_STORAGE_PATH") : "src/main/resources/storage/location-storage.json";
    public final String ORGS_STORAGE = System.getenv("ORGS_STORAGE_PATH") != null ? System.getenv("ORGS_STORAGE_PATH") : "src/main/resources/storage/org-storage.json";

    private JsonEntityConverter converter;

    @Setter
    @Getter
    private Set<OrgEntity> orgs;
    @Setter
    @Getter
    private Map<String, Map<String, HashSet<VdiProductEntity>>> orgLocProductMap;


    public JsonStorageRepository() {
        this.converter = new JsonEntityConverter();
        this.orgs = new HashSet<>();
        this.orgLocProductMap = new HashMap<>();
        readProcessing();
    }


    public void writeProcessing() {
        var productJsonList = new HashSet<VdiProductJsonEntity>();
        var locationJsonList = new HashSet<LocationJsonEntity>();
        var orgJsonList = new HashSet<OrgJsonEntity>();

        for (OrgEntity org : orgs) {
            for (LocationEntity loc : org.getLocations()) {
                var productsForLoc = orgLocProductMap.get(org.getOrg()).get(loc.getLocationId());
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

        productsJson.removeIf(Objects::isNull);
        var generalMap = new HashMap<String, Map<String, HashSet<VdiProductEntity>>>();

        productsJson.removeIf(Objects::isNull);
        for (OrgJsonEntity o : orgsJson) {
            generalMap.putIfAbsent(o.getOrg(), new HashMap<>());
            for (LocationJsonEntity l : locationsJson) {
                generalMap.get(o.getOrg()).putIfAbsent(l.getLocationId(), new HashSet<>());
            }
        }
        for (VdiProductJsonEntity p : productsJson) {
            var locId = p.getLocation();
            var orgId = p.getOrg();
            if (locId == null || orgId == null) continue;
            var orgMap = generalMap.get(orgId);
            if (orgMap == null) continue;
            var locMap = orgMap.get(locId);
            if (locMap != null) locMap.add(converter.jsonToProduct(p));
        }
        this.orgLocProductMap = generalMap;

        //fill orgs
        var orgsToUpdate = new HashSet<OrgEntity>();
        for (Map.Entry<String, Map<String, HashSet<VdiProductEntity>>> orgEntry : generalMap.entrySet()) {
            var org = orgEntry.getKey();
            var locMap = orgEntry.getValue();
            var orgEntity = new OrgEntity();
            for (OrgJsonEntity orgJson : orgsJson) {
                if (orgJson.getOrg().equals(org)) {
                    orgEntity.setOrg(org);
                    orgEntity.setUserKey(orgJson.getUserKey());
                    orgEntity.setLocations(new ArrayList<>());
                }
            }

            for (Map.Entry<String, HashSet<VdiProductEntity>> locEntry : locMap.entrySet()) {
                var locId = locEntry.getKey();
                var productIdList = locEntry.getValue().stream().map(VdiProductEntity::getId).toList();
                var locEntity = new LocationEntity();
                locEntity.setLocationId(locId);
                locEntity.setProductIds(productIdList);
                orgEntity.getLocations().add(locEntity);
            }
            orgsToUpdate.add(orgEntity);
        }
        this.orgs = orgsToUpdate;
    }

    private void writeJsonFile(Set<? extends BaseJsonEntity> data, String filePath) {
        var objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
        objectMapper.configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false);

        try {
            var outputFile = new File(filePath);
            objectMapper.writeValue(outputFile, data);

            System.out.println("Data successfully added: " + outputFile.getAbsolutePath());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private List<VdiProductJsonEntity> readProductsJson(String filePath) {
        var objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
        objectMapper.configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false);

        try {
            var inputFile = new File(filePath);
            return objectMapper.readValue(inputFile, new TypeReference<>() {
            });
        } catch (Exception e) {
            e.printStackTrace();
        }

        return new ArrayList<>();
    }

    private List<LocationJsonEntity> readLocationsJson(String filePath) {
        var objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
        objectMapper.configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false);

        try {
            var inputFile = new File(filePath);
            return objectMapper.readValue(inputFile, new TypeReference<>() {
            });
        } catch (Exception e) {
            e.printStackTrace();
        }

        return new ArrayList<>();
    }

    private List<OrgJsonEntity> readOrgsJson(String filePath) {
        var objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
        objectMapper.configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false);

        try {
            var inputFile = new File(filePath);
            return objectMapper.readValue(inputFile, new TypeReference<>() {
            });
        } catch (Exception e) {
            e.printStackTrace();
        }

        return new ArrayList<>();
    }
}
