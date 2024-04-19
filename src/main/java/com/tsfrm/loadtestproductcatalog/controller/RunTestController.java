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
    String authUrlToken;
    int threadsQuantity;

    {
        String urlurl = "http://localhost:8082/mmsproducts/1/localtest";
        authUrlToken = System.getenv("AUTH_TOKEN") != null ? System.getenv("AUTH_TOKEN") : "eyJhbGciOiJSUzI1NiIsInR5cCI6IkpXVCIsImtpZCI6ImVxYUhSVm55blF2ZHQyUGk5WWh2QSJ9.eyJpc3MiOiJodHRwczovL3Rlc3Q0LTM2NS51cy5hdXRoMC5jb20vIiwic3ViIjoiODVaeVRLWFYxQVZsRm1Ldmp5bDhiVnVpajdVakVGbkFAY2xpZW50cyIsImF1ZCI6Imh0dHBzOi8vdmRpLWdhdGV3YXkudGVzdDQuMzY1cm0udXMiLCJpYXQiOjE3MTM1MDg3MDMsImV4cCI6MTcxMzU5NTEwMywic2NvcGUiOiJvZmZsaW5lX2FjY2VzcyBvcGVuaWQiLCJndHkiOiJjbGllbnQtY3JlZGVudGlhbHMiLCJhenAiOiI4NVp5VEtYVjFBVmxGbUt2anlsOGJWdWlqN1VqRUZuQSJ9.GPTGbOZ-d0_2bO6_R7KybfnIbA9TLeTfxgXZw1HpNXZJ0dya76Rn4Psan6MOHFST99GtC_Rxxr0y8wmmE30NUXEeYSTcd6EfZ1RTdb5qCsFeFUy1_hT-iCkT0XKrLMG-7uHJQDksvxqpwbW6imhuo74NAtwCJG_NNlGDWQ4CuuRbyyutEkxgQ4Cxy-VYkxvEEZGF5CoFVntMRUHr3A263q7pFPAxh0dSF20zARtbjIYC6bqUs_2QV48dEHtboBE3qeN9O8jVv3NXcOJeBAM7_cVAySavRW7gQIhwRdPMIgT-foiU2MB1FrqemliK0vlU_s21zQQCKj3hl-hFPCwyQw";
//        url = System.getenv("DESTINATION_URL") != null ? System.getenv("DESTINATION_URL") : "https://gateway.test4.365rm.us/vdi/VDIdataExchange/mmsproducts/1/e10c2ae5-1f8a-4224-add1-8e78d4a228dce";
        url = urlurl;
        threadsQuantity = System.getenv("OUTBOUND_THREADS_QUANTITY") != null ? Integer.parseInt(System.getenv("OUTBOUND_THREADS_QUANTITY")) : 20;
        runTestService = new RunTestService(url, authUrlToken, threadsQuantity);
    }


    @PostMapping("/runtest")
    public String runTest(@RequestBody TestFormData formData) {
        return runTestService.runTestSpringController(formData);
    }
}
