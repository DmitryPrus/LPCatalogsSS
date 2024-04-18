package com.tsfrm.loadtestproductcatalog.controller;

import com.tsfrm.loadtestproductcatalog.domain.TestFormData;
import com.tsfrm.loadtestproductcatalog.service.RunTestService;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class RunTestController {

    RunTestService runTestService;
    String url;
    int threadsQuantity;

    {
        url = System.getenv("DESTINATION_URL") != null ? System.getenv("DESTINATION_URL") : "http://localhost:8082/mmsproducts/1/localtest";
        threadsQuantity = System.getenv("OUTBOUND_THREADS_QUANTITY") != null ? Integer.parseInt(System.getenv("OUTBOUND_THREADS_QUANTITY")) : 20;
        runTestService = new RunTestService(url, threadsQuantity);
    }


    @PostMapping("/runtest")
    public String runTest(@RequestBody TestFormData formData) {
        return runTestService.runTestSpringController(formData);
    }
}
