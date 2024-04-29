package com.tsfrm.loadtestproductcatalog.service;

import com.tsfrm.loadtestproductcatalog.domain.*;
import com.tsfrm.loadtestproductcatalog.domain.entity.LocationEntity;
import com.tsfrm.loadtestproductcatalog.domain.entity.OrgEntity;
import com.tsfrm.loadtestproductcatalog.domain.entity.VdiProductEntity;
import com.tsfrm.loadtestproductcatalog.repository.JdbcConfig;
import com.tsfrm.loadtestproductcatalog.repository.JsonStorageRepository;
import com.tsfrm.loadtestproductcatalog.repository.OrgRepository;
import com.tsfrm.loadtestproductcatalog.repository.ProductRepository;
import com.tsfrm.loadtestproductcatalog.service.exception.ValidationException;
import lombok.NonNull;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.OffsetDateTime;
import java.util.*;
import java.util.stream.Collectors;

public class VdiProductGenerateService {

    private Random random;
    private ProductRepository productRepository;
    private OrgRepository orgRepository;
    private JsonStorageRepository jsonStorageRepository;
    private JsonEntityConverter converter;
    private static final Logger log = LogManager.getLogger(VdiProductGenerateService.class);

    public VdiProductGenerateService(JsonStorageRepository jsonStorageRepository) {
        this.jsonStorageRepository = jsonStorageRepository;
        this.converter = new JsonEntityConverter();
        random = new Random();
        productRepository = new ProductRepository(new JdbcConfig(), jsonStorageRepository);
        orgRepository = new OrgRepository(new JdbcConfig(), jsonStorageRepository);
    }


    public List<VdiProductsTransaction> generateMessages(TestFormData request) {
        var allOrgs = new ArrayList<>(jsonStorageRepository.getOrgs().stream().toList());
        isValid(request, allOrgs);
        Collections.shuffle(allOrgs);

        var availableOrgsAndLocationsMap = new HashMap<String, ArrayList<LocationEntity>>();

        // logic which allow choose only those orgs/locations which contain available requested quantity
        // of products/locations for update/delete
        if (forUpdate(request)) {
            for (OrgEntity org : allOrgs) {
                if (org.getLocations().size() < request.getLocations()) continue;
                for (LocationEntity loc : org.getLocations()) {
                    if (loc.getProductIds().size() < (request.getProductsToUpdate() + request.getProductsToDelete()))
                        continue;
                    availableOrgsAndLocationsMap.putIfAbsent(org.getOrg(), new ArrayList<>());
                    availableOrgsAndLocationsMap.get(org.getOrg()).add(loc);
                }
            }
        } else { // add all existing orgs and entities as available in case when there are no data for update and delete
            for (OrgEntity org : allOrgs) {
                for (LocationEntity loc : org.getLocations()) {
                    availableOrgsAndLocationsMap.putIfAbsent(org.getOrg(), new ArrayList<>());
                    availableOrgsAndLocationsMap.get(org.getOrg()).add(loc);
                }
            }
        }

        availableOrgsAndLocationsMap.entrySet()
                .removeIf(entry -> entry.getValue().size() < request.getLocations());

        if (availableOrgsAndLocationsMap.size() < request.getOperators())
            throw new ValidationException("There are no data for your requested parameters. Reduce number of operators/locations or products to update/delete");


        var messages = new ArrayList<VdiProductsTransaction>();
        int orgsHandled = 0;

        for (OrgEntity o : allOrgs) {
            if (orgsHandled >= request.getOperators()) break;
            if (!availableOrgsAndLocationsMap.containsKey(o.getOrg())) continue;
            var locations = new ArrayList<>(o.getLocations());
            var vpt = new VdiProductsTransaction();
            var marketProductList = new ArrayList<VdiMarketProduct>();
            Collections.shuffle(locations);
            int locationsHandled = 0;
            var availableLocationIdsSet = availableOrgsAndLocationsMap.get(o.getOrg()).stream().map(LocationEntity::getLocationId).collect(Collectors.toSet());

            for (LocationEntity location : locations) {
                if (locationsHandled >= request.getLocations()) break;
                if (!availableLocationIdsSet.contains(location.getLocationId())) continue;
                var productIds = new ArrayList<>(location.getProductIds());
                Collections.shuffle(productIds);
                var productsToRemove = removeProducts(request.getProductsToDelete(), productIds);
                var productsToCreate = generateProduct(request.getNewProducts());
                var productsToUpdate = updateProducts(request.getProductsToUpdate(), productIds);
                productsToUpdate.addAll(productsToCreate);
                marketProductList.add(new VdiMarketProduct(location.getLocationUserKey(), generateCatalogType(), productsToRemove, productsToUpdate));

                //delete from productLocationMap (needed for transactions)
                jsonStorageRepository.getOrgLocProductMap()
                        .get(o.getOrg())
                        .get(location.getLocationId())
                        .removeIf(productEntity ->
                                productsToRemove.stream()
                                        .anyMatch(deleteProd -> deleteProd.getProductId().equals(productEntity.getId()))
                        );

                // update products in productLocationMap
                var allProductIds = jsonStorageRepository.getOrgLocProductMap()
                        .get(o.getOrg())
                        .get(location.getLocationId())
                        .stream()
                        .map(VdiProductEntity::getId)
                        .collect(Collectors.toSet());

                productsToUpdate.stream()
                        .filter(vp -> !allProductIds.contains(vp.getProductId()))
                        .map(vp -> converter.vdiProductToEntity(vp, o.getOrg()))
                        .forEach(jsonStorageRepository.getOrgLocProductMap()
                                .get(o.getOrg())
                                .get(location.getLocationId())::add);

                locationsHandled++;
            }
            vpt.setVdiHeader(generateHeader(o.getUserKey()));
            vpt.setProducts(marketProductList);
            messages.add(vpt);
            orgsHandled++;
        }
        return messages;
    }


    public VdiHeader generateHeader(@NonNull String userKey) {
        var vdiVersion = Const.HEADER_VDI_VERSION;
        var vdiType = Const.HEADER_VDI_TYPE;
        var providerName = Const.HEADER_PROVIDER_NAME;
        var partnerUid = Const.HEADER_PARTNER_APPLICATION_ID;
        var applicationId = Const.HEADER_PARTNER_APPLICATION_ID;
        var operatorId = userKey;
        var requestId = UUID.randomUUID().toString();
        var correlationId = UUID.randomUUID().toString();
        var transactionId = UUID.randomUUID().toString();
        var transactionDtm = OffsetDateTime.now();

        return new VdiHeader(vdiVersion, vdiType, providerName, partnerUid, applicationId, operatorId, requestId,
                correlationId, transactionId, transactionDtm);
    }


    public VdiCatalogType generateCatalogType() {
        return random.nextBoolean() ? VdiCatalogType.FULL : VdiCatalogType.PARTIAL;
    }


    public List<VdiProductsRemove> removeProducts(int numberToRemove, List<String> productIds) {
        var resultList = new ArrayList<VdiProductsRemove>();
        for (int i = 0; i < numberToRemove; i++) {
            var productId = productIds.get(i);
            resultList.add(new VdiProductsRemove(productId));
        }
        return resultList;
    }


    public List<VdiProduct> updateProducts(int numberToUpdate, List<String> productIds) {
        var listToUpdate = new ArrayList<VdiProduct>();
        var products = productRepository.findAllByIds(productIds);
        Collections.shuffle(products);

        for (int i = 0; i < numberToUpdate; i++) {
            var pe = products.get(i);
            if (pe == null) continue;

            Integer minStock = null;
            Integer maxStock = null;

            try {
                minStock = Integer.parseInt(pe.getMinstock());
            } catch (Exception ignored) {
            }
            try {
                maxStock = Integer.parseInt(pe.getMaxstock());
            } catch (Exception ignored) {
            }

            var productResult = VdiProduct.builder()
                    .productId(pe.getId())
                    .productCode(pe.getScancode())
                    .productName(pe.getName())
                    .shortProductName(pe.getShortname())
                    .categoryCode(pe.getCategory1())
                    .categoryName(pe.getCategory1())
                    .cost(generateNumberValue(0.01, 10))
                    .price(generateNumberValue(0.01, 10))
                    .lastChangeDtm(OffsetDateTime.now())
                    .minQuantity(minStock)
                    .maxQuantity(maxStock)
                    .menuName(Const.PRODUCTS_MENU_NAME)
                    .subMenuName(Const.PRODUCTS_SUBMENY_NAME)
                    .productImage(null)
                    .productSKUs(List.of(generateProductSKU()))
                    .productAttributes(List.of(generateAttributes()))
                    .barCodes(List.of(generateBarCode()))
                    .taxes(List.of(generateTaxes()))
                    .fees(List.of(generateVdiFee()))
                    .nutritions(new ArrayList<>())
                    .build();

            listToUpdate.add(productResult);
        }
        return listToUpdate;
    }


    // method creates new product list
    public List<VdiProduct> generateProduct(int numberToCreate) {
        var resultList = new ArrayList<VdiProduct>(numberToCreate);
        for (int i = 0; i < numberToCreate; i++) {
            var rawProduct = UtilProductGeneration.PRODUCT_RAW_LIST.get(random.nextInt(UtilProductGeneration.PRODUCT_RAW_LIST.size()));
            var productId = UUID.randomUUID().toString().replaceAll("-", "");

            var cost = generateNumberValue(1, 10);
            var resultProudct = VdiProduct.builder()
                    .productId(productId)
                    .productCode(rawProduct.getProductCode())
                    .productName(rawProduct.getProductName())
                    .shortProductName(rawProduct.getShortProductName())
                    .categoryCode(rawProduct.getCategoryCode())
                    .categoryName(rawProduct.getCategoryName())
                    .cost(cost)
                    .price(cost.multiply(new BigDecimal("2.10")).setScale(2, RoundingMode.HALF_UP))
                    .lastChangeDtm(OffsetDateTime.now())
                    .minQuantity(random.nextInt(5) + 1)
                    .maxQuantity(random.nextInt(20) + 1)
                    .menuName(Const.PRODUCTS_MENU_NAME)
                    .subMenuName(Const.PRODUCTS_SUBMENY_NAME)
                    .productImage(null)
                    .productSKUs(List.of(generateProductSKU()))
                    .productAttributes(List.of(generateAttributes()))
                    .barCodes(List.of(generateBarCode()))
                    .taxes(List.of(generateTaxes()))
                    .fees(List.of(generateVdiFee()))
                    .nutritions(new ArrayList<>())
                    .build();
            resultList.add(resultProudct);
        }
        return resultList;
    }


    private VdiProductSKU generateProductSKU() {
        return new VdiProductSKU("SKU1");
    }

    private VdiBarCode generateBarCode() {
        return new VdiBarCode("BarCode1");
    }

    private VdiProductAttribute generateAttributes() {
        return new VdiProductAttribute("Attribute1");
    }

    private VdiNutrition generateVdiNutrition() {
        var internalNutrition = new VdiNutrition("Saturated Fat", "g", "3", null);
        return new VdiNutrition("Total Fat", "g", "8", List.of(internalNutrition));
    }


    private VdiTax generateTaxes() {
        return VdiTax.builder()
                .taxId(Const.TAX_IDS.get(random.nextInt(Const.TAX_IDS.size())))
                .taxName(Const.TAX_NAMES.get(random.nextInt(Const.TAX_NAMES.size())))
                .taxRate(generateNumberValue(0.01, 0.2))
                .taxValue(generateNumberValue(0.01, 0.2))
                .taxCount(Const.TAX_COUNT)
                .taxTotal(BigDecimal.ONE)
                .includedInPrice(false)
                .build();
    }


    private VdiFee generateVdiFee() {
        return VdiFee.builder()
                .feeId(Const.FEE_ID)
                .feeName(Const.FEE_NAME)
                .feeCount(Const.FEE_COUNT)
                .feeValue(Const.FEE_VALUE)
                .feeTotal(Const.FEE_VALUE.multiply(new BigDecimal(Const.FEE_COUNT)))
                .build();
    }

    public static BigDecimal generateNumberValue(double minValue, double maxValue) {
        var random = new Random();
        double randomValue = minValue + random.nextDouble() * (maxValue - minValue);
        return BigDecimal.valueOf(randomValue).setScale(2, RoundingMode.HALF_UP);
    }

    private void isValid(TestFormData request, List<OrgEntity> orgEntities) {
        if (request.getOperators() <= 0) throw new ValidationException("Operators must contain value >0");
        if (request.getLocations() <= 0) throw new ValidationException("Locations must contain value >0");
        if (request.getNewProducts() > 5000) throw new ValidationException("Too many products to create");
        if (request.getProductsToUpdate()<=0 && request.getProductsToDelete() <=0  && request.getNewProducts()<=0)
            throw new ValidationException("There are no data to modify");

        if (request.getOperators() > orgEntities.size())
            throw new ValidationException(String.format("Too many operators. Available %s, requested %s", orgEntities.size(), request.getOperators()));


        if (request.getNewProducts() > 0) {
            int totalProducts = orgEntities.stream()
                    .flatMap(org -> org.getLocations().stream())
                    .mapToInt(loc -> loc.getProductIds().size())
                    .sum();
            log.info("Products in storage: " + totalProducts);
            if (totalProducts > 300000)
                throw new ValidationException("New products creation restriction. Too many existing products [" + totalProducts + "]. Use 'newProducts' as 0 , and try again");
        }
    }

    private boolean forUpdate(TestFormData testFormData) {
        return testFormData.getProductsToUpdate() + testFormData.getProductsToDelete() > 0;
    }

}
