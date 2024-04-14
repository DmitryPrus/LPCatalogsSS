package com.tsfrm.loadtestproductcatalog.domain;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class VdiMarketProduct {
    private String marketId; //location id
    private VdiCatalogType catalogType;
    private List<VdiProductsRemove> productsRemove;
    private List<VdiProduct> productsUpdate;
}
