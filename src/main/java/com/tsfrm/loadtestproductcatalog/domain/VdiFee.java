package com.tsfrm.loadtestproductcatalog.domain;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

import java.math.BigDecimal;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class VdiFee {
    private String feeId;
    private String feeName;
    private Integer feeCount;
    private BigDecimal feeValue;
    private BigDecimal feeTotal;
}
