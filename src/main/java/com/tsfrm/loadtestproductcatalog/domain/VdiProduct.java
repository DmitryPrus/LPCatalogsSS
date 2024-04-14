package com.tsfrm.loadtestproductcatalog.domain;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class VdiProduct {
    private String productId;
    private String productCode;
    private String productName;
    private String shortProductName;
    private String categoryCode;
    private String categoryName;
    private BigDecimal cost;
    private BigDecimal price;
    private OffsetDateTime lastChangeDtm;
    private Integer minQuantity;
    private Integer maxQuantity;
    private String menuName;
    private String subMenuName;
    private String productImage;
    private List<VdiProductSKU> productSKUs;
    private List<VdiProductAttribute> productAttributes;
    private List<VdiBarCode> barCodes;
    private List<VdiTax> taxes;
    private List<VdiFee> fees;
    private List<VdiNutrition> nutritions;

    //constructor needed for generation data particular
    public VdiProduct(
            String productCode,
            String productName,
            String shortProductName,
            String categoryCode,
            String categoryName
    ) {
        this.productCode = productCode;
        this.productName = productName;
        this.shortProductName = shortProductName;
        this.categoryCode = categoryCode;
        this.categoryName = categoryName;
    }
}
