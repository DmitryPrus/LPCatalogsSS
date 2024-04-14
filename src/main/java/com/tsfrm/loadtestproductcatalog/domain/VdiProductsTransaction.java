package com.tsfrm.loadtestproductcatalog.domain;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class VdiProductsTransaction {
    private VdiHeader vdiHeader;
    @JsonProperty("mmsPRODUCTS")
    private List<VdiMarketProduct> products;
}
