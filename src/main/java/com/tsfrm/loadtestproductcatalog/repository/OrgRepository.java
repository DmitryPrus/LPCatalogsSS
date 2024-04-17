package com.tsfrm.loadtestproductcatalog.repository;

import com.tsfrm.loadtestproductcatalog.domain.entity.LocationEntity;
import com.tsfrm.loadtestproductcatalog.domain.entity.OrgEntity;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.sql.*;
import java.util.*;

public class OrgRepository extends BaseRepository<OrgEntity> {

    private static final Logger log = LogManager.getLogger(OrgRepository.class);

    private JsonStorageRepository jsonStorageRepository;

    public OrgRepository(JdbcConfig config, JsonStorageRepository jsonStorageRepository) {
        super(config);
        this.jsonStorageRepository = jsonStorageRepository;
    }

    public List<OrgEntity> getVdi2OrgEntitiesFullList() {
        return jsonStorageRepository.getOrgs().stream().toList();
    }

    /**
     * not used
     */
    @Override
    public OrgEntity findById(String id) {
        return null;
    }
}
