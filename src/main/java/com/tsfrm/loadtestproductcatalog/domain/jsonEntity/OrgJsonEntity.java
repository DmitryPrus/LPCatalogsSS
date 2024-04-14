package com.tsfrm.loadtestproductcatalog.domain.jsonEntity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrgJsonEntity extends BaseJsonEntity {
    private String org;
    private String userKey;
}
