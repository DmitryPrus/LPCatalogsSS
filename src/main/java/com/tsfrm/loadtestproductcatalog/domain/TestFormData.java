package com.tsfrm.loadtestproductcatalog.domain;


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class TestFormData {
    private int operators;  // How many bunch of jsons to generate
    private int locations; // how many vdiMarketProduct  to generate
    private int newProducts;
    private int productsToDelete;
    private int productsToUpdate;
}