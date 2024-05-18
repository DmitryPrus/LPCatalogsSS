package com.tsfrm.loadtestproductcatalog.domain.jsonEntity;

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
public class VdiProductJsonEntity extends BaseJsonEntity {
    private String id; // means nothing
    private String org;
    private String name;
    private String shortname;
    private String upc;
    private String scancode;
    private String category1;
    private String category2;
    private String category3;
    private String status;
    private String userkey; // means productId for outbound final test JSON
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


    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        VdiProductJsonEntity that = (VdiProductJsonEntity) o;
        return Objects.equals(userkey, that.userkey) && Objects.equals(org, that.org) && Objects.equals(location, that.location);
    }

    @Override
    public int hashCode() {
        return Objects.hash(userkey, org, location);
    }
}
