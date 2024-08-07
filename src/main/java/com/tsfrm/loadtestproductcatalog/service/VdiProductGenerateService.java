package com.tsfrm.loadtestproductcatalog.service;

import com.amazonaws.util.StringUtils;
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


    /**
     * Method required only existing operatorId
     */
    public List<VdiProductsTransaction> generateMessages(TestFormData request) {
        var orgEntity = validateRequestAndRetrieveOrgEntity(request, jsonStorageRepository.getOrgs().stream().toList());

        var messages = new ArrayList<VdiProductsTransaction>();
        var vpt = new VdiProductsTransaction();
        var marketProductList = new ArrayList<VdiMarketProduct>();
        var locations = new ArrayList<>(orgEntity.getLocations());

        locations.removeIf(
                location -> forUpdate(request) &&
                        ((request.getProductsToUpdate() + request.getProductsToDelete()) > location.getProductIds().size())
        );
        Collections.shuffle(locations);

        int locationsHandled = 0;

        for (LocationEntity location : locations) {
            if (locationsHandled >= request.getLocations()) break;
            var productIds = new ArrayList<>(location.getProductIds());
            Collections.shuffle(productIds);
            var catalogType = generateCatalogType(request);
            var productsToRemove = removeProducts(request.getProductsToDelete(), productIds);
            productIds.removeAll(productsToRemove.stream().map(VdiProductsRemove::getProductId).toList());
            var productsToCreate = generateProduct(request.getNewProducts());
            var productsToUpdate = updateProducts(request.getProductsToUpdate(), productIds);
            productsToUpdate.addAll(productsToCreate);
            marketProductList.add(new VdiMarketProduct(location.getLocationUserKey(), catalogType, productsToRemove, productsToUpdate));

            //delete from productLocationMap (needed for transactions)
            jsonStorageRepository.getOrgLocProductMap()
                    .get(orgEntity.getOrg())
                    .get(location.getLocationId())
                    .removeIf(productEntity ->
                            productsToRemove.stream()
                                    .anyMatch(deleteProd -> deleteProd.getProductId().equals(productEntity.getUserkey()))
                    );

            // Full mode make replacement for all related products
            // we delete all records about products and write them as newly created below
            if (catalogType == VdiCatalogType.FULL) {
                jsonStorageRepository.getOrgLocProductMap()
                        .get(orgEntity.getOrg())
                        .get(location.getLocationId())
                        .clear();
            }

            // update products in productLocationMap
            var allProductIds = jsonStorageRepository.getOrgLocProductMap()
                    .get(orgEntity.getOrg())
                    .get(location.getLocationId())
                    .stream()
                    .map(VdiProductEntity::getUserkey)
                    .collect(Collectors.toSet());

            productsToUpdate.stream()
                    .filter(vp -> !allProductIds.contains(vp.getProductId()))
                    .map(vp -> converter.vdiProductToEntity(vp, orgEntity.getOrg()))
                    .forEach(jsonStorageRepository.getOrgLocProductMap()
                            .get(orgEntity.getOrg())
                            .get(location.getLocationId())::add);

            locationsHandled++;
        }
        vpt.setVdiHeader(generateHeader(orgEntity.getUserKey()));
        vpt.setProducts(marketProductList);
        messages.add(vpt);

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


    /**
     * Removal does not work for VDI 2 , and if we set into field 'productsToDelete' > 0
     * we use Full mode to really remove it
     */
    public VdiCatalogType generateCatalogType(TestFormData request) {
        return request.getProductsToDelete() > 0 ? VdiCatalogType.FULL
                : random.nextBoolean() ? VdiCatalogType.FULL : VdiCatalogType.PARTIAL;
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

            var shortName = (pe.getShortname() != null) ? pe.getShortname() : pe.getName();
            var categoryCode = (pe.getCategory1() != null) ? pe.getCategory1() : "no-code";
            var categoryName = (pe.getCategory1() != null) ? pe.getCategory1() : "no-name";
            var productResult = VdiProduct.builder()
                    .productId(pe.getUserkey()) //USERKEY IS productId for JSON
                    .productCode(pe.getScancode())
                    .productName(pe.getName())
                    .shortProductName(shortName)
                    .categoryCode(categoryCode)
                    .categoryName(categoryName)
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
                    .barCodes(List.of(new VdiBarCode(pe.getScancode())))
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
            var productId = "LPCatalogID" + UUID.randomUUID().toString().replaceAll("-", "").substring(0, 20);

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
        var barcode = UUID.randomUUID().toString().replaceAll("-", "").substring(0, 11);
        return new VdiBarCode("LPBar" + barcode);
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

    private OrgEntity validateRequestAndRetrieveOrgEntity(TestFormData request, List<OrgEntity> orgEntities) {
        if (request.getNewProducts() > 5000)
            throw new ValidationException("Too many products to create for request: " + request);
        if (request.getProductsToUpdate() <= 0 && request.getProductsToDelete() <= 0 && request.getNewProducts() <= 0)
            throw new ValidationException("There are no data to modify. For request: " + request);

        if (request.getNewProducts() > 0) {
            int totalProducts = orgEntities.stream()
                    .flatMap(org -> org.getLocations().stream())
                    .mapToInt(loc -> loc.getProductIds().size())
                    .sum();
            log.info("Products in storage: " + totalProducts);
            if (totalProducts > 500000)
                throw new ValidationException("New products creation restriction. Too many existing products [" + totalProducts + "]. Use 'newProducts' as 0 , and try again");
        }

        var chosenOrg = orgEntities.stream()
                .filter(o -> o.getOrg().equalsIgnoreCase(request.getOperatorId()))
                .findFirst()
                .orElseThrow(() -> new ValidationException("Operator does not exist: " + request.getOperatorName() + " with id " + request.getOperatorId()));

        if (chosenOrg.getLocations().size() < request.getLocations())
            throw new ValidationException("Too many locations requested. Available: " + chosenOrg.getLocations().size() + ", required: " + request.getLocations() + ". For request: " + request);
        if (forUpdate(request)) {
            int quantityForUpdate = request.getProductsToUpdate() + request.getProductsToDelete();
            int locationsCounter = 0;
            for (var l : chosenOrg.getLocations()) {
                if (l.getProductIds().size() >= quantityForUpdate) locationsCounter++;
            }
            if (locationsCounter < request.getLocations()) {
                var message = String.format("Too many products requested for update. %n There are %d locations contain more than %d products But required %d locations to be updated. %n Reduce quantity of locations or quantity of products for update/delete for request: %n", locationsCounter, quantityForUpdate, request.getLocations());
                throw new ValidationException(message + request);
            }
        }
        return chosenOrg;
    }

    private boolean forUpdate(TestFormData testFormData) {
        return testFormData.getProductsToUpdate() + testFormData.getProductsToDelete() > 0;
    }

}
