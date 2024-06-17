package com.tsfrm.loadtestproductcatalog.service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

public class Const {
    //SELECT USERKEY FROM VDIUSERKEY K INNER JOIN VDI2PROVIDER P ON K.VDIPROVIDER = P.ID WHERE K.ORG IS NOT NULL
    // header data

    // location must be under operatorId
    public static final String HEADER_PARTNER_APPLICATION_ID = "365";
    public static final String HEADER_VDI_TYPE = "mmsproducts";
    public static final String HEADER_PROVIDER_NAME = System.getenv("HEADER_PROVIDER_NAME") != null ? System.getenv("HEADER_PROVIDER_NAME") : "CatalogProvider"; //VERY IMPORTANT TO MODIFY
    public static final String HEADER_VDI_VERSION = "Load-product-catalog-test";


    public static final List<String> TAX_IDS = new ArrayList<>(List.of("taxId1", "taxId2", "taxId3", "taxId4"));
    public static final List<String> TAX_NAMES = new ArrayList<>(List.of("tax1", "tax2", "tax3", "tax4"));
    public static final int TAX_COUNT = 1;

    public static final BigDecimal FEE_VALUE = BigDecimal.valueOf(0.05);
    public static final int FEE_COUNT = 1;
    public static final String FEE_ID = "FeeId";
    public static final String FEE_NAME = "ADDITIONAL TAX";

    public static final String PRODUCTS_MENU_NAME = "menuName";
    public static final String PRODUCTS_SUBMENY_NAME = "menuName";
}
