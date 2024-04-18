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
    int threadsQuantity;
    private static final Logger log = LogManager.getLogger(LambdaController.class);

    {
        String message = String.format("""
                        DB_URL : %s
                        DB_USER : %s
                        DB_PASSWORD : *****
                        DESTINATION_URL : %s
                        OUTBOUND_THREADS_QUANTITY : %s
                        LOCATIONS_PER_OPERATOR_MINIMUM: %s
                        PRODUCTS_STORAGE_PATH: %s
                        LOCATIONS_STORAGE_PATH: %s
                        ORGS_STORAGE_PATH: %s
                        """,
                System.getenv("DB_URL"),
                System.getenv("DB_USER"),
                System.getenv("DESTINATION_URL"),
                System.getenv("OUTBOUND_THREADS_QUANTITY"),
                System.getenv("LOCATIONS_PER_OPERATOR_MINIMUM"),
                System.getenv("PRODUCTS_STORAGE_PATH"),
                System.getenv("LOCATIONS_STORAGE_PATH"),
                System.getenv("ORGS_STORAGE_PATH")
        );

        log.info("Run with data: \n" + message);
        url = System.getenv("DESTINATION_URL") != null ? System.getenv("DESTINATION_URL") : "http://localhost:8082/mmsproducts/1/localtest";
        threadsQuantity = System.getenv("OUTBOUND_THREADS_QUANTITY") != null ? Integer.parseInt(System.getenv("OUTBOUND_THREADS_QUANTITY")) : 20;
        runTestService = new RunTestService(url, threadsQuantity);

    }

    @Override
    public String handleRequest(TestFormData request, Context context) {
        return runTestService.runTest(request);
    }
}
