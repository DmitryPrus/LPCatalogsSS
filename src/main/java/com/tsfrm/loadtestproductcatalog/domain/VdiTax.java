package com.tsfrm.loadtestproductcatalog.domain;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class VdiTax {
    private String taxId;
    private String taxName;
    private BigDecimal taxRate;
    private BigDecimal taxValue;
    private Integer taxCount;
    private BigDecimal taxTotal;
    private boolean includedInPrice;
}
