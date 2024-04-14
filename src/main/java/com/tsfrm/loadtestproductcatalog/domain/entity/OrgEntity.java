package com.tsfrm.loadtestproductcatalog.domain.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrgEntity {
    private String org;
    private String userKey;
    private List<LocationEntity> locations;
}
