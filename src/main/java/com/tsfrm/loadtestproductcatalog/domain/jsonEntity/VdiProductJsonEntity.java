package com.tsfrm.loadtestproductcatalog.domain.jsonEntity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class VdiProductJsonEntity extends BaseJsonEntity {
    private String id;
    private String org;
    private String name;
    private String shortname;
    private String upc;
    private String scancode;
    private String category1;
    private String category2;
    private String category3;
    private String status;
    private String userkey;
    private BigDecimal cost;
    private BigDecimal price;
    private BigDecimal tax;
    private BigDecimal tax2;
    private BigDecimal tax3;
    private BigDecimal tax4;
    private int points;
    private String minstock;
    private String maxstock;
    private LocalDateTime datecreated;
    private LocalDateTime lastupdated;
    private String location;
}
