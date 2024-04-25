package com.tsfrm.loadtestproductcatalog.controller;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyRequestEvent;
import com.amazonaws.util.StringUtils;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.tsfrm.loadtestproductcatalog.domain.TestFormData;
import com.tsfrm.loadtestproductcatalog.service.RunTestService;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class LambdaController implements RequestHandler<APIGatewayProxyRequestEvent, String> {

    RunTestService runTestService;
    String destinationUrl;
    int threadsQuantity;
    private static final Logger log = LogManager.getLogger(LambdaController.class);

    {
        String message = String.format("""
                        DESTINATION_URL : %s
                        HEADER_PROVIDER_NAME: %s
                        OUTBOUND_THREADS_QUANTITY : %s
                        LOCATIONS_PER_OPERATOR_MINIMUM: %s
                        BUCKET_NAME: %s
                        BUCKET_ORGS_KEY: %s
                        BUCKET_LOCATIONS_KEY: %s
                        BUCKET_PRODUCTS_KEY: %s
                        USER_KEY: %s
                        USER_PASS: %s
                        """,
                System.getenv("DESTINATION_URL"),
                System.getenv("HEADER_PROVIDER_NAME"),
                System.getenv("OUTBOUND_THREADS_QUANTITY"),
                System.getenv("LOCATIONS_PER_OPERATOR_MINIMUM"),
                System.getenv("BUCKET_NAME"),
                System.getenv("BUCKET_ORGS_KEY"),
                System.getenv("BUCKET_LOCATIONS_KEY"),
                System.getenv("BUCKET_PRODUCTS_KEY"),
                System.getenv("USER_KEY"),
                System.getenv("USER_PASS")
        );

        log.info("Run with data: \n" + message);
        destinationUrl = System.getenv("DESTINATION_URL") != null ? System.getenv("DESTINATION_URL") : "http://localhost:8082/mmsproducts/1/localtest";
        threadsQuantity = System.getenv("OUTBOUND_THREADS_QUANTITY") != null ? Integer.parseInt(System.getenv("OUTBOUND_THREADS_QUANTITY")) : 3;
    }

    @Override
    public String handleRequest(APIGatewayProxyRequestEvent event, Context context) {
        try {
            var mapper = new ObjectMapper();
            var testFormData = mapper.readValue(event.getBody(), TestFormData.class);
            var validationMessage = requestInvalidMessage(testFormData);
            if (!StringUtils.isNullOrEmpty(validationMessage)) return "Validation error. "+ validationMessage;

            runTestService = new RunTestService(destinationUrl, testFormData.getToken(), threadsQuantity);
            return runTestService.runTest(testFormData);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }

    private String requestInvalidMessage(TestFormData req) {
        if (req == null) return "No request provided";
        if (req.getOperators() <= 0) return "'Operators' must be > 0. Provided: " + req.getOperators();
        if (req.getLocations() <= 0) return "'Locations' must be > 0. Provided: " + req.getLocations();
        if (req.getNewProducts() < 0 || req.getNewProducts() > 5000) return "New products quantity range is 0..5000; Provided: " + req.getNewProducts();
        if (req.getProductsToUpdate() < 0) return "Invalid 'productsToUpdate': " + req.getProductsToUpdate();
        if (req.getProductsToDelete() <0) return "Invalid 'productsToDelete': " + req.getProductsToDelete();
        if (StringUtils.isNullOrEmpty(req.getToken())) return "Required token";
        return null;
    }
}
