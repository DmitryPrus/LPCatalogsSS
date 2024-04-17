package com.tsfrm.loadtestproductcatalog.repository;

import com.tsfrm.loadtestproductcatalog.domain.entity.VdiProductEntity;

import java.sql.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

public class ProductRepository extends BaseRepository<VdiProductEntity> {

    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    private JsonStorageRepository jsonStorageRepository;

    public ProductRepository(JdbcConfig config, JsonStorageRepository jsonStorageRepository) {
        super(config);
        this.jsonStorageRepository = jsonStorageRepository;
    }


    @Override
    public VdiProductEntity findById(String id) {
        return jsonStorageRepository.getLocationProductMap().entrySet().stream()
                .flatMap(entry -> entry.getValue().stream())
                .filter(vp -> vp.getId().equals(id))
                .findFirst()
                .orElse(null);
    }

    public List<VdiProductEntity> findAllByProductIds(List<String> productIds) {
        return  null;
    }

    public List<String> getAllProductIdList() {
        return null;
    }


    public boolean isIdExists(String productId) {
        return false;
    }
}
