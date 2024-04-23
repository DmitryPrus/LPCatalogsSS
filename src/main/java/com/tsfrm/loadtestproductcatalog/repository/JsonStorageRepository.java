package com.tsfrm.loadtestproductcatalog.repository;

import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.model.S3Object;
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
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.*;

public class JsonStorageRepository {

    private static final Logger log = LogManager.getLogger(JsonStorageRepository.class);
    private final String PRODUCTS_STORAGE = System.getenv("PRODUCTS_STORAGE_PATH") != null ? System.getenv("PRODUCTS_STORAGE_PATH") : "src/main/resources/storage/product-storage.json";
    private final String LOCATIONS_STORAGE = System.getenv("LOCATIONS_STORAGE_PATH") != null ? System.getenv("LOCATIONS_STORAGE_PATH") : "src/main/resources/storage/location-storage.json";
    private final String ORGS_STORAGE = System.getenv("ORGS_STORAGE_PATH") != null ? System.getenv("ORGS_STORAGE_PATH") : "src/main/resources/storage/org-storage.json";

    private final String AWS_ACCESS_KE = System.getenv("AWS_ACCESS_KE") != null ? System.getenv("AWS_ACCESS_KE") : "AKIAS2A3QYOTSMXRMWWE";
    private final String AWS_SECRET_KE = System.getenv("AWS_SECRET_KE") != null ? System.getenv("AWS_SECRET_KE") : "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";

    private final String BUCKET_NAME = System.getenv("BUCKET_NAME") != null ? System.getenv("BUCKET_NAME") : "orgsstorage";
    private final String BUCKET_ORGS_KEY = System.getenv("BUCKET_ORGS_KEY") != null ? System.getenv("BUCKET_ORGS_KEY") : "org-storage.json";
    private final String BUCKET_LOCATIONS_KEY = System.getenv("BUCKET_LOCATIONS_KEY") != null ? System.getenv("BUCKET_LOCATIONS_KEY") : "location-storage.json";
    private final String BUCKET_PRODUCTS_KEY = System.getenv("BUCKET_PRODUCTS_KEY") != null ? System.getenv("BUCKET_PRODUCTS_KEY") : "product-storage.json";

    private AmazonS3 s3Client;
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
        s3Client = AmazonS3ClientBuilder
                .standard()
                .withCredentials(new AWSStaticCredentialsProvider(new BasicAWSCredentials(AWS_ACCESS_KE, AWS_SECRET_KE)))
                .withRegion(Regions.EU_NORTH_1).build();
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

        //s3 bucket
        writeJsonFileToS3(productJsonList, BUCKET_NAME, BUCKET_PRODUCTS_KEY);
        writeJsonFileToS3(locationJsonList, BUCKET_NAME, BUCKET_LOCATIONS_KEY);
        writeJsonFileToS3(orgJsonList, BUCKET_NAME, BUCKET_ORGS_KEY);

        // local storage
//        writeJsonFile(productJsonList, PRODUCTS_STORAGE);
//        writeJsonFile(locationJsonList, LOCATIONS_STORAGE);
//        writeJsonFile(orgJsonList, ORGS_STORAGE);
    }


    public void readProcessing() {

        //local storage
//        var productsJson = readProductsJson(PRODUCTS_STORAGE);
//        var locationsJson = readLocationsJson(LOCATIONS_STORAGE);
//        var orgsJson = readOrgsJson(ORGS_STORAGE);

        //s3 storage
        var productsJson = readProductsJsonFromS3(BUCKET_NAME, BUCKET_PRODUCTS_KEY);
        var locationsJson = readLocationsJsonFromS3(BUCKET_NAME, BUCKET_LOCATIONS_KEY);
        var orgsJson = readOrgsJsonFromS3(BUCKET_NAME, BUCKET_ORGS_KEY);

        productsJson.removeIf(Objects::isNull);
        var generalMap = new HashMap<String, Map<String, HashSet<VdiProductEntity>>>();

        productsJson.removeIf(Objects::isNull);
        for (OrgJsonEntity o : orgsJson) {
            generalMap.putIfAbsent(o.getOrg(), new HashMap<>());
            for (LocationJsonEntity l : locationsJson) {
                if (l.getOrgId().equals(o.getOrg())) {
                    generalMap.get(o.getOrg()).putIfAbsent(l.getLocationId(), new HashSet<>());
                }
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
                var locUserKey = locationsJson.stream().filter(loc -> loc.getLocationId().equals(locId)).map(LocationJsonEntity::getLocationUserKey).findFirst().orElse(null);
                var locEntity = new LocationEntity();
                locEntity.setLocationId(locId);
                locEntity.setLocationUserKey(locUserKey);
                locEntity.setProductIds(productIdList);
                orgEntity.getLocations().add(locEntity);
            }
            orgsToUpdate.add(orgEntity);
        }
        this.orgs = orgsToUpdate;
        log.info("Data uploaded to memory. Orgs quantity: " + orgs.size());
    }

    private void writeJsonFile(Set<? extends BaseJsonEntity> data, String filePath) {
        var objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
        objectMapper.configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false);

        try {
            var outputFile = new File(filePath);
            objectMapper.writeValue(outputFile, data);
            log.info("Data successfully added: " + outputFile.getAbsolutePath());
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

    public void writeJsonFileToS3(Set<? extends BaseJsonEntity> data, String bucketName, String key) {
        var objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
        objectMapper.configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false);

        try {
            var jsonString = objectMapper.writeValueAsString(data);
            byte[] contentBytes = jsonString.getBytes(StandardCharsets.UTF_8);
            var inputStream = new ByteArrayInputStream(contentBytes);
            var metadata = new ObjectMetadata();
            metadata.setContentLength(contentBytes.length);
            s3Client.putObject(bucketName, key, inputStream, metadata);
            log.info("Data successfully added to S3 bucket: " + bucketName + "/" + key);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private List<VdiProductJsonEntity> readProductsJsonFromS3(String bucketName, String key) {
        var objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
        objectMapper.configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false);

        try {
            var s3Object = s3Client.getObject(bucketName, key);
            var reader = new BufferedReader(new InputStreamReader(s3Object.getObjectContent()));
            var content = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                content.append(line);
            }
            return objectMapper.readValue(content.toString(), new TypeReference<>() {
            });
        } catch (IOException e) {
            e.printStackTrace();
        }
        return new ArrayList<>();
    }

    private List<LocationJsonEntity> readLocationsJsonFromS3(String bucketName, String key) {
        var objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
        objectMapper.configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false);

        try {
            var s3Object = s3Client.getObject(bucketName, key);
            var reader = new BufferedReader(new InputStreamReader(s3Object.getObjectContent()));
            var content = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                content.append(line);
            }
            return objectMapper.readValue(content.toString(), new TypeReference<>() {
            });
        } catch (IOException e) {
            e.printStackTrace();
        }
        return new ArrayList<>();
    }

    private List<OrgJsonEntity> readOrgsJsonFromS3(String bucketName, String key) {
        var objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
        objectMapper.configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false);

        try {
            var s3Object = s3Client.getObject(bucketName, key);
            var reader = new BufferedReader(new InputStreamReader(s3Object.getObjectContent()));
            var content = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                content.append(line);
            }
            return objectMapper.readValue(content.toString(), new TypeReference<>() {
            });
        } catch (IOException e) {
            e.printStackTrace();
        }
        return new ArrayList<>();
    }
}
