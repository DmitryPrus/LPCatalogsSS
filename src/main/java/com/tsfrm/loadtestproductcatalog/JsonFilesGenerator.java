package com.tsfrm.loadtestproductcatalog;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.tsfrm.loadtestproductcatalog.domain.entity.LocationEntity;
import com.tsfrm.loadtestproductcatalog.domain.entity.OrgEntity;
import com.tsfrm.loadtestproductcatalog.domain.jsonEntity.BaseJsonEntity;
import com.tsfrm.loadtestproductcatalog.domain.jsonEntity.LocationJsonEntity;
import com.tsfrm.loadtestproductcatalog.domain.jsonEntity.OrgJsonEntity;
import com.tsfrm.loadtestproductcatalog.domain.jsonEntity.VdiProductJsonEntity;
import com.tsfrm.loadtestproductcatalog.repository.JdbcConfig;
import com.tsfrm.loadtestproductcatalog.repository.ProductRepository;
import com.tsfrm.loadtestproductcatalog.service.JsonEntityConverter;
import com.tsfrm.loadtestproductcatalog.service.VdiProductGenerateService;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class JsonFilesGenerator {

    VdiProductGenerateService vdiProductGenerateService = new VdiProductGenerateService();
    JsonEntityConverter converter = new JsonEntityConverter();
    List<OrgEntity> orgEntities = vdiProductGenerateService.getAllOrgs();
    ProductRepository productRepository = new ProductRepository(new JdbcConfig());

    public static void main(String[] args) {
//        var productsJson = readJsonFile("src/main/resources/storage/product-storage.json");
//        var productJsonToWrite = converter.productToJson(listP,"locationid1");
//        writeJsonFile(productJsonToWrite, "src/main/resources/storage/product-storage.json");

        JsonFilesGenerator jfg = new JsonFilesGenerator();
        jfg.writeProcessing(jfg.orgEntities);
    }

    private void writeProcessing (List<OrgEntity>orgEntities){
        var productJsonList = new ArrayList<VdiProductJsonEntity>();
        var locationJsonList = new ArrayList<LocationJsonEntity>();
        var orgJsonList = new ArrayList<OrgJsonEntity>();

        for (OrgEntity org : orgEntities){
            for (LocationEntity loc : org.getLocations()){
                for (String prodId : loc.getProductIds()){
                    var product = productRepository.findById(prodId);
                    if (product==null) continue;
                    var productJson = converter.productToJson(product, loc.getLocationId());
                    productJsonList.add(productJson);
                }
                locationJsonList.add(converter.locationToJson(loc, org.getOrg()));
            }
            orgJsonList.add(converter.orgToJson(org));
        }

        writeJsonFile(productJsonList, "src/main/resources/storage/product-storage.json");
        writeJsonFile(locationJsonList, "src/main/resources/storage/location-storage.json");
        writeJsonFile(orgJsonList, "src/main/resources/storage/org-storage.json");
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

    private List<? extends BaseJsonEntity> readJsonFile(String filePath) {
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
        objectMapper.configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false);

        try {
            File inputFile = new File(filePath);
            return objectMapper.readValue(inputFile, new TypeReference<List<? extends BaseJsonEntity>>() {});
        } catch (Exception e) {
            e.printStackTrace();
        }

        return new ArrayList<>();
    }
}
