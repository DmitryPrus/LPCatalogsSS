package com.tsfrm.loadtestproductcatalog.controller;

import com.amazonaws.util.StringUtils;
import com.tsfrm.loadtestproductcatalog.domain.TestFormData;
import com.tsfrm.loadtestproductcatalog.service.RunTestService;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
public class RunTestController {

    RunTestService runTestService;
    String url;
    int threadsQuantity;

    {
        url = System.getenv("DESTINATION_URL") != null ? System.getenv("DESTINATION_URL") : "http://localhost:8082/mmsproducts/1/localtest";
        //String url = http://localhost:8082/mmsproducts/1/localtest;
        //String url = https://gateway.test4.365rm.us/vdi/VDIdataExchange/mmsproducts/1/e10c2ae5-1f8a-4224-add1-8e78d4a228dce;
        threadsQuantity = System.getenv("OUTBOUND_THREADS_QUANTITY") != null ? Integer.parseInt(System.getenv("OUTBOUND_THREADS_QUANTITY")) : 3;
        //runTestService = new RunTestService(url, authUrlToken, threadsQuantity);
    }


    @PostMapping("/runtest")
    public String runTest(@RequestBody List<TestFormData> formData) {
        for (TestFormData f : formData){
            var validationMessage = requestInvalidMessage(f);
            if (!StringUtils.isNullOrEmpty(validationMessage)) return "Validation error. " + validationMessage + " for request: "+f;
        }

        runTestService = new RunTestService(url, formData.get(0).getAuthToken(), threadsQuantity);
        return  runTestService.runTestSpringController(formData);
    }

    private String requestInvalidMessage(TestFormData req) {
        if (req == null) return "No request provided";
//        if (req.getOperators() <= 0) return "'Operators' must be > 0. Provided: " + req.getOperators();
//        if (!StringUtils.isNullOrEmpty(req.getOperatorId()) && req.getOperators()!=1) return "'Operators' quantity required = 1 for provided chosen operator "+ req.getOperatorId();
        if (StringUtils.isNullOrEmpty(req.getOperatorId())) return "No operator provided";
        if (req.getLocations() <= 0) return "'Locations' must be > 0. Provided: " + req.getLocations();
        if (req.getNewProducts() < 0 || req.getNewProducts() > 5000)
            return "New products quantity possible range is 0..5000; Provided: " + req.getNewProducts();
        if (req.getProductsToUpdate() < 0) return "Invalid 'productsToUpdate': " + req.getProductsToUpdate();
        if (req.getProductsToDelete() < 0) return "Invalid 'productsToDelete': " + req.getProductsToDelete();
        if (req.getProductsToUpdate() <= 0 && req.getProductsToDelete() <= 0 && req.getNewProducts() <= 0)
            return "There are no data to modify";

        if (StringUtils.isNullOrEmpty(req.getAuthToken())) return "Required token";
        return null;
    }
}
