package com.tsfrm.loadtestproductcatalog.repository;

import com.tsfrm.loadtestproductcatalog.domain.entity.VdiProductEntity;

import java.util.*;

public class ProductRepository extends BaseRepository<VdiProductEntity> {

    private JsonStorageRepository jsonStorageRepository;

    public ProductRepository(JdbcConfig config, JsonStorageRepository jsonStorageRepository) {
        super(config);
        this.jsonStorageRepository = jsonStorageRepository;
    }


    @Override
    public VdiProductEntity findById(String id) {
        return jsonStorageRepository.getOrgLocProductMap().values().stream()
                .flatMap(locProductMap -> locProductMap.values().stream())
                .flatMap(Set::stream)
                .filter(productEntity -> productEntity.getId().equals(id))
                .findFirst()
                .orElse(null);
    }

    public List<VdiProductEntity> findAllByProductIds(List<String> productIds) {
        return null;
    }

    public List<String> getAllProductIdList() {
        return null;
    }


    public boolean isIdExists(String productId) {
        return false;
    }
}
