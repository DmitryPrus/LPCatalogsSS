package com.tsfrm.loadtestproductcatalog.repository;

public class JdbcConfig {

    // test 4
//    public String dbUrl = System.getenv("DB_URL") != null ? System.getenv("DB_URL") : "jdbc:mysql://test4db.365rm.us/sosdb";
//    public String dbUser = System.getenv("DB_USER") != null ? System.getenv("DB_USER") : "v1vdiapi2SOSDB";
//    public String dbPassword = System.getenv("DB_PASSWORD") != null ? System.getenv("DB_PASSWORD") : "AQ5t6mZXGBFW";

    // staging

    public String dbUrl = System.getenv("DB_URL") != null ? System.getenv("DB_URL") : "jdbc:mysql://stagingdb.365rm.us/sosdb";
    public String dbUser = System.getenv("DB_USER") != null ? System.getenv("DB_USER") : "v1vdiapi2SOSDB";
    public String dbPassword = System.getenv("DB_PASSWORD") != null ? System.getenv("DB_PASSWORD") : "S03LRm2PZhkU";
}
