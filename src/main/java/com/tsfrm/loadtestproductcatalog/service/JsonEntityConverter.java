package com.tsfrm.loadtestproductcatalog.service;

import com.tsfrm.loadtestproductcatalog.domain.entity.LocationEntity;
import com.tsfrm.loadtestproductcatalog.domain.entity.OrgEntity;
import com.tsfrm.loadtestproductcatalog.domain.entity.VdiProductEntity;
import com.tsfrm.loadtestproductcatalog.domain.jsonEntity.LocationJsonEntity;
import com.tsfrm.loadtestproductcatalog.domain.jsonEntity.OrgJsonEntity;
import com.tsfrm.loadtestproductcatalog.domain.jsonEntity.VdiProductJsonEntity;

import java.util.ArrayList;
import java.util.List;

public class JsonEntityConverter {

    public VdiProductJsonEntity productToJson(VdiProductEntity vp, String locationId) {
        var je = new VdiProductJsonEntity();
        je.setId(vp.getId());
        je.setOrg(vp.getOrg());
        je.setName(vp.getName());
        je.setShortname(vp.getShortname());
        je.setUpc(vp.getUpc());
        je.setScancode(vp.getScancode());
        je.setCategory1(vp.getCategory1());
        je.setCategory2(vp.getCategory2());
        je.setCategory3(vp.getCategory3());
        je.setStatus(vp.getStatus());
        je.setUserkey(vp.getUserkey());
        je.setCost(vp.getCost());
        je.setPrice(vp.getPrice());
        je.setTax(vp.getTax());
        je.setTax2(vp.getTax2());
        je.setTax3(vp.getTax3());
        je.setTax4(vp.getTax4());
        je.setPoints(vp.getPoints());
        je.setMinstock(vp.getMinstock());
        je.setMaxstock(vp.getMaxstock());
        je.setDatecreated(vp.getDatecreated());
        je.setLastupdated(vp.getLastupdated());
        je.setLocation(locationId);

        return je;
    }

    public VdiProductEntity jsonToProduct(VdiProductJsonEntity vp) {
        var pe = new VdiProductEntity();
        pe.setId(vp.getId());
        pe.setOrg(vp.getOrg());
        pe.setName(vp.getName());
        pe.setShortname(vp.getShortname());
        pe.setUpc(vp.getUpc());
        pe.setScancode(vp.getScancode());
        pe.setCategory1(vp.getCategory1());
        pe.setCategory2(vp.getCategory2());
        pe.setCategory3(vp.getCategory3());
        pe.setStatus(vp.getStatus());
        pe.setUserkey(vp.getUserkey());
        pe.setCost(vp.getCost());
        pe.setPrice(vp.getPrice());
        pe.setTax(vp.getTax());
        pe.setTax2(vp.getTax2());
        pe.setTax3(vp.getTax3());
        pe.setTax4(vp.getTax4());
        pe.setPoints(vp.getPoints());
        pe.setMinstock(vp.getMinstock());
        pe.setMaxstock(vp.getMaxstock());
        pe.setDatecreated(vp.getDatecreated());
        pe.setLastupdated(vp.getLastupdated());

        return pe;
    }

    public LocationJsonEntity locationToJson(LocationEntity location, String ortId) {
        return new LocationJsonEntity(location.getLocationId(), ortId);
    }

    public LocationEntity jsonToLocation(LocationJsonEntity location, List<VdiProductEntity> products) {
        var ids = products.stream().map(VdiProductEntity::getId).toList();
        return new LocationEntity(location.getLocationId(), ids);
    }

    public OrgJsonEntity orgToJson(OrgEntity org) {
        return new OrgJsonEntity(org.getOrg(), org.getUserKey());
    }

    public OrgEntity jsonToOrg(OrgJsonEntity org, List<LocationEntity> locations) {
        return new OrgEntity(org.getOrg(), org.getUserKey(), locations);
    }

}
