package com.tsfrm.loadtestproductcatalog.controller;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.tsfrm.loadtestproductcatalog.domain.TestFormData;
import com.tsfrm.loadtestproductcatalog.service.RunTestService;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class LambdaController implements RequestHandler<TestFormData, String> {

    RunTestService runTestService;
    String url;
    String authUrlToken;
    int threadsQuantity;
    private static final Logger log = LogManager.getLogger(LambdaController.class);

    {
        String message = String.format("""
                        DESTINATION_URL : %s
                        AUTH_TOKEN : %s
                        OUTBOUND_THREADS_QUANTITY : %s
                        LOCATIONS_PER_OPERATOR_MINIMUM: %s
                        PRODUCTS_STORAGE_PATH: %s
                        LOCATIONS_STORAGE_PATH: %s
                        ORGS_STORAGE_PATH: %s
                        BUCKET_NAME: %s
                        BUCKET_ORGS_KEY: %s
                        BUCKET_LOCATIONS_KEY: %s
                        BUCKET_PRODUCTS_KEY: %s
                        """,
                System.getenv("DESTINATION_URL"),
                System.getenv("AUTH_TOKEN"),
                System.getenv("OUTBOUND_THREADS_QUANTITY"),
                System.getenv("LOCATIONS_PER_OPERATOR_MINIMUM"),
                System.getenv("PRODUCTS_STORAGE_PATH"),
                System.getenv("LOCATIONS_STORAGE_PATH"),
                System.getenv("ORGS_STORAGE_PATH"),
                System.getenv("BUCKET_NAME"),
                System.getenv("BUCKET_ORGS_KEY"),
                System.getenv("BUCKET_LOCATIONS_KEY"),
                System.getenv("BUCKET_PRODUCTS_KEY")
        );

        log.info("Run with data: \n" + message);
        url = System.getenv("DESTINATION_URL") != null ? System.getenv("DESTINATION_URL") : "http://localhost:8082/mmsproducts/1/localtest";
        threadsQuantity = System.getenv("OUTBOUND_THREADS_QUANTITY") != null ? Integer.parseInt(System.getenv("OUTBOUND_THREADS_QUANTITY")) : 20;
        authUrlToken = System.getenv("AUTH_TOKEN");
        runTestService = new RunTestService(url, authUrlToken, threadsQuantity);

    }

    @Override
    public String handleRequest(TestFormData request, Context context) {
        return runTestService.runTest(request);
    }
}
