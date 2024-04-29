package com.tsfrm.loadtestproductcatalog.repository;

import com.tsfrm.loadtestproductcatalog.domain.VdiProduct;
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

    public List<VdiProductEntity> findAllByIds(List<String> ids) {
        var productIdsMap = new HashMap<String, VdiProductEntity>(20000);
        jsonStorageRepository.getOrgLocProductMap().values().stream()
                .flatMap(locProductMap -> locProductMap.values().stream())
                .flatMap(Set::stream)
                .forEach(prodEntity -> productIdsMap.put(prodEntity.getId(), prodEntity));

        var resultList = new ArrayList<VdiProductEntity>();
        ids.forEach(id -> {
            var item = productIdsMap.get(id);
            if (item!=null) resultList.add(item);
        });
        return resultList;
    }
}
