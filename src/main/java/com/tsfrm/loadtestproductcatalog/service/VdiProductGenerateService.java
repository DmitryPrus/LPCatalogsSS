package com.tsfrm.loadtestproductcatalog.service;

import com.tsfrm.loadtestproductcatalog.domain.*;
import com.tsfrm.loadtestproductcatalog.domain.entity.OrgEntity;
import com.tsfrm.loadtestproductcatalog.repository.JdbcConfig;
import com.tsfrm.loadtestproductcatalog.repository.JsonStorageRepository;
import com.tsfrm.loadtestproductcatalog.repository.OrgRepository;
import com.tsfrm.loadtestproductcatalog.repository.ProductRepository;
import com.tsfrm.loadtestproductcatalog.service.exception.ValidationException;
import lombok.Getter;
import lombok.NonNull;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.OffsetDateTime;
import java.util.*;
import java.util.stream.Collectors;

public class VdiProductGenerateService {

    Random random;
    ProductRepository productRepository;
    OrgRepository orgRepository;
    @Getter
    List<OrgEntity> allOrgs;
    @Getter
    List<String> allProductIds;
    private JsonStorageRepository jsonStorageRepository;
    private static final Logger log = LogManager.getLogger(VdiProductGenerateService.class);

    public VdiProductGenerateService(JsonStorageRepository jsonStorageRepository) {
        this.jsonStorageRepository = jsonStorageRepository;

        log.info("Initialization started. Don't run your test");
        random = new Random();
        productRepository = new ProductRepository(new JdbcConfig(), jsonStorageRepository);
        orgRepository = new OrgRepository(new JdbcConfig(), jsonStorageRepository);
        updateAllOrgsAndProducts();
        log.info("Initialization completed. Extracted " + allOrgs.size() + " orgs for VDI2");
    }


    public List<VdiProductsTransaction> generateMessages(TestFormData request) {
        if (request.getOperators() > allOrgs.size())
            throw new ValidationException(String.format("Request validation failed. Too many operators. Requested: %d; available: %d", request.getOperators(), allOrgs.size()));

        Collections.shuffle(allOrgs);
        var orgs = new ArrayList<OrgEntity>();
        for (int i = 0; i < request.getOperators(); i++) {
            orgs.add(allOrgs.get(i));
        }

        isValid(request, orgs);
        log.info("Request is valid. Generation started");

        var messages = new ArrayList<VdiProductsTransaction>();

        // OPERATORS
        for (OrgEntity o : orgs) {
            var vpt = new VdiProductsTransaction();
            var marketProductList = new ArrayList<VdiMarketProduct>();
            var locations = o.getLocations();
            Collections.shuffle(locations);
            //LOCATIONS per operator
            for (int i = 0; i < request.getLocations(); i++) {
                var location = locations.get(i);
                var productsToRemove = removeProducts(request.getProductsToDelete(), location.getProductIds());
                var productsToCreate = generateProduct(request.getNewProducts());
                var productsToUpdate = updateProducts(request.getProductsToUpdate(), location.getProductIds());
                productsToUpdate.addAll(productsToCreate);
                marketProductList.add(new VdiMarketProduct(location.getLocationId(), generateCatalogType(), productsToRemove, productsToUpdate));
            }

            vpt.setVdiHeader(generateHeader(o.getUserKey()));
            vpt.setProducts(marketProductList);
            messages.add(vpt);
        }

        //TODO replace this method!
        //CompletableFuture.runAsync(this::updateAllOrgsAndProducts);
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
        Collections.shuffle(productIds);
        var resultList = new ArrayList<VdiProductsRemove>();
        for (int i = 0; i < numberToRemove; i++) {
            var productId = productIds.get(i);
            resultList.add(new VdiProductsRemove(productId));
        }
        return resultList;
    }


    public List<VdiProduct> updateProducts(int numberToUpdate, List<String> productIds) {
        var listToUpdate = new ArrayList<VdiProduct>();

        for (int i = 0; i < numberToUpdate; i++) {
            var productId = productIds.get(i);
            var pe = productRepository.findById(productId);
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
                    .productId(productId)
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
                    .nutritions(null)
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
            while (allProductIds.contains(productId)) {
                productId = UUID.randomUUID().toString();
            }

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
                    .nutritions(null)
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


    // для всех update/create сделаем обновление tax
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


    // fee always the same
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
        String recommendation = "Reduce the value of the exceeding parameter or fill database by needed value and bindings.";
        orgEntities.forEach(o -> {
            if (o.getLocations().size() < request.getLocations())
                throw new ValidationException(String.format("Too many locations. Organization  %s contains %d locations. But required %d . %s", o.getOrg(), o.getLocations().size(), request.getLocations(), recommendation));

            o.getLocations().forEach(l -> {
                if (l.getProductIds().size() < request.getProductsToUpdate())
                    throw new ValidationException(String.format("Too many products to update. Location  %s contains %d products. But required %d . %s", l.getLocationId(), l.getProductIds().size(), request.getLocations(), recommendation));

                if (l.getProductIds().size() < request.getProductsToDelete())
                    throw new ValidationException(String.format("Too many products to delete. Location  %s contains %d products. But required %d . %s", l.getLocationId(), l.getProductIds().size(), request.getLocations(), recommendation));
            });

        });
    }

    private void updateAllOrgsAndProducts(){
        allOrgs = orgRepository.getVdi2OrgEntitiesFullList();
        allProductIds = allOrgs.stream()
                .flatMap(org -> org.getLocations().stream())
                .flatMap(location -> location.getProductIds().stream())
                .collect(Collectors.toList());
    }

}
