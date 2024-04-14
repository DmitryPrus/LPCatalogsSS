package com.tsfrm.loadtestproductcatalog.repository;

import java.sql.SQLException;

public abstract class BaseRepository<T> {
    protected JdbcConfig config;

    public BaseRepository(JdbcConfig config) {
        this.config = config;
    }

    public abstract T findById(String id) throws SQLException;

}
