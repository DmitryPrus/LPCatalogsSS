package com.tsfrm.loadtestproductcatalog;

import com.amazonaws.util.StringUtils;
import com.tsfrm.loadtestproductcatalog.domain.TestFormData;
import com.tsfrm.loadtestproductcatalog.service.RunTestService;

public class LambdaMemoryMainTest {

    RunTestService runTestService;

    public static void main(String[] args) {
        var destinationUrl = "http://localhost:8082/mmsproducts/1/localtest";

        int operators = 1;
        int locations = 2;
        int newProducts = 0;
        int productsToDelete = 20000;
        int productsToUpdate = 20000;
        var authToken = "token";

        var testFromData = new TestFormData(operators, locations, newProducts, productsToDelete, productsToUpdate, authToken);
        LambdaMemoryMainTest lt = new LambdaMemoryMainTest();
        System.out.println(lt.handleRequest(destinationUrl, testFromData));
    }


    public String handleRequest(String destinationUrl, TestFormData testFormData) {
        var validationMessage = requestInvalidMessage(testFormData);
        if (!StringUtils.isNullOrEmpty(validationMessage)) return "Validation error. " + validationMessage;
        runTestService = new RunTestService(destinationUrl, testFormData.getAuthToken(), 3);
        return runTestService.runTest(testFormData);
    }

    private String requestInvalidMessage(TestFormData req) {
        if (req == null) return "No request provided";
        if (req.getOperators() <= 0) return "'Operators' must be > 0. Provided: " + req.getOperators();
        if (req.getLocations() <= 0) return "'Locations' must be > 0. Provided: " + req.getLocations();
        if (req.getNewProducts() < 0 || req.getNewProducts() > 5000)
            return "New products quantity possible range is 0..5000; Provided: " + req.getNewProducts();
        if (req.getProductsToUpdate() < 0) return "Invalid 'productsToUpdate': " + req.getProductsToUpdate();
        if (req.getProductsToDelete() < 0) return "Invalid 'productsToDelete': " + req.getProductsToDelete();
        if (StringUtils.isNullOrEmpty(req.getAuthToken())) return "Required token";
        if (req.getProductsToUpdate() <= 0 && req.getProductsToDelete() <= 0 && req.getNewProducts() <= 0)
            return "There are no data to modify";
        return null;
    }

}
