package com.tsfrm.loadtestproductcatalog.controller;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyRequestEvent;
import com.amazonaws.util.StringUtils;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.type.TypeFactory;
import com.tsfrm.loadtestproductcatalog.domain.TestFormData;
import com.tsfrm.loadtestproductcatalog.service.RunTestService;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.List;

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
            var testFormData = mapper.readValue(event.getBody(), new TypeReference<List<TestFormData>>() {
            });

            for (var f : testFormData){
                var validationMessage = requestInvalidMessage(f);
                if (!StringUtils.isNullOrEmpty(validationMessage)) return "Validation error. " + validationMessage;
            }

            runTestService = new RunTestService(destinationUrl, testFormData.get(0).getAuthToken(), threadsQuantity);
            return  runTestService.runTestSpringController(testFormData);
        } catch (JsonProcessingException e) {
            return "Error. " + e.getMessage();
        }
    }

    private String requestInvalidMessage(TestFormData req) {
        if (req == null) return "No request provided";
        if (req.getOperators() <= 0) return "'Operators' must be > 0. Provided: " + req.getOperators();
        if (!StringUtils.isNullOrEmpty(req.getExactoperator()) && req.getOperators()!=1)
            return "'Operators' quantity required = 1 for provided chosen operator "+ req.getExactoperator();
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
