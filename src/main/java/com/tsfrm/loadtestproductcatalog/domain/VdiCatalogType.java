package com.tsfrm.loadtestproductcatalog.domain;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public enum VdiCatalogType {
    @JsonProperty("Full")
    FULL,
    @JsonProperty("Partial")
    PARTIAL
}
