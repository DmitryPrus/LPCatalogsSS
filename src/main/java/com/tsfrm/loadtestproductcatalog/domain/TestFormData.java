package com.tsfrm.loadtestproductcatalog.domain;


import jakarta.annotation.Nullable;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.NonNull;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class TestFormData {
    //private int operators;  // How many bunch of jsons to generate
    @NonNull
    private String operatorId; // id of choosen operator. 'operators' will be set as 1
    @Nullable
    private String operatorName;
    private int locations; // how many vdiMarketProduct  to generate
    private int newProducts;
    private int productsToDelete;
    private int productsToUpdate;
    private String authToken;
}
