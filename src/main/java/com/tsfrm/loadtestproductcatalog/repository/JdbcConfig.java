package com.tsfrm.loadtestproductcatalog.repository;

public class JdbcConfig {
    public String dbUrl = System.getenv("DB_URL") != null ? System.getenv("DB_URL") : "jdbc:mysql://localhost:3306/sosdb";
    public String dbUser = System.getenv("DB_USER") != null ? System.getenv("DB_USER") : "CERTEFI";
    public String dbPassword = System.getenv("DB_PASSWORD") != null ? System.getenv("DB_PASSWORD") : "certefiPass1$";
}
