package com.tsfrm.loadtestproductcatalog.domain.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Objects;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class VdiProductEntity {
    private String id;  // means nothing
    private String org;
    private String name;
    private String shortname;
    private String upc;
    private String scancode;
    private String category1;
    private String category2;
    private String category3;
    private String status;
    private String userkey;  // is productId for outbound json
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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        VdiProductEntity that = (VdiProductEntity) o;
        return Objects.equals(userkey, that.userkey);
    }

    @Override
    public int hashCode() {return Objects.hash(userkey);}
}
