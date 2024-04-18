package com.tsfrm.loadtestproductcatalog.domain;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;

@Data
@AllArgsConstructor
public class VdiNutrition {
    private String nutritionName;
    private String nutritionUnit;
    private String nutritionValue;
    private List<VdiNutrition> nutritions;
}
