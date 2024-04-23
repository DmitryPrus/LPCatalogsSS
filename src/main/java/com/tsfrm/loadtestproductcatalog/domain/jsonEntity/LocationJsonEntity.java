package com.tsfrm.loadtestproductcatalog.domain.jsonEntity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Objects;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LocationJsonEntity extends BaseJsonEntity {
    private String locationId;
    private String locationUserKey;
    private String orgId;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        LocationJsonEntity that = (LocationJsonEntity) o;
        return Objects.equals(locationId, that.locationId) && Objects.equals(locationUserKey, that.locationUserKey) && Objects.equals(orgId, that.orgId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(locationId, locationUserKey, orgId);
    }
}
