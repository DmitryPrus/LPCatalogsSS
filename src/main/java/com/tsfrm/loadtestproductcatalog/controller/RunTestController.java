//package com.tsfrm.loadtestproductcatalog.controller;
//
//import com.tsfrm.loadtestproductcatalog.domain.TestFormData;
//import com.tsfrm.loadtestproductcatalog.service.RunTestService;
//import org.springframework.web.bind.annotation.PostMapping;
//import org.springframework.web.bind.annotation.RequestBody;
//import org.springframework.web.bind.annotation.RestController;
//
//@RestController
//public class RunTestController {
//
//    RunTestService runTestService;
//    String url;
//    String authUrlToken;
//    int threadsQuantity;
//
//    {
//        authUrlToken = System.getenv("AUTH_TOKEN") != null ? System.getenv("AUTH_TOKEN") : "eyJhbGciOiJSUzI1NiIsInR5cCI6IkpXVCIsImtpZCI6ImVxYUhSVm55blF2ZHQyUGk5WWh2QSJ9.eyJpc3MiOiJodHRwczovL3Rlc3Q0LTM2NS51cy5hdXRoMC5jb20vIiwic3ViIjoiODVaeVRLWFYxQVZsRm1Ldmp5bDhiVnVpajdVakVGbkFAY2xpZW50cyIsImF1ZCI6Imh0dHBzOi8vdmRpLWdhdGV3YXkudGVzdDQuMzY1cm0udXMiLCJpYXQiOjE3MTM4NTkxMzUsImV4cCI6MTcxMzk0NTUzNSwic2NvcGUiOiJvZmZsaW5lX2FjY2VzcyBvcGVuaWQiLCJndHkiOiJjbGllbnQtY3JlZGVudGlhbHMiLCJhenAiOiI4NVp5VEtYVjFBVmxGbUt2anlsOGJWdWlqN1VqRUZuQSJ9.VyZAOfP-KFhqYxodVKaltB1SJbyAFwZQ1NzEOlPVtWSKELBjripJ27dDC8xWAl7eiKP2wtqajssXMaDV_aC5XLtAQsZbyT4dZFn91q6WNOuOYlJKz01KNos8rnvcY2rz3OsgfKTc9MJpcl7DrvBhgUDKAVH1W38EvzW9wWjKeMFvoWakHEMyxKC1Z9m98KcUg4JK4Bt6mFcYAA-iN6PlkGCUuaKag6YtIZGN_4nobp_vODOrujgOJHBF5ogoj1Oj6gWsP6X3XUIiokfXDBBqnL3_wUvP8yyKh85wA9vPIIXaytCdQwM9W08YEcfhT-NuykOmeLAwKF41Uoru6NCstw";
//        url = System.getenv("DESTINATION_URL") != null ? System.getenv("DESTINATION_URL") : "https://gateway.test4.365rm.us/vdi/VDIdataExchange/mmsproducts/1/e10c2ae5-1f8a-4224-add1-8e78d4a228dce";
//        //String url = http://localhost:8082/mmsproducts/1/localtest;
//        //String url = https://gateway.test4.365rm.us/vdi/VDIdataExchange/mmsproducts/1/e10c2ae5-1f8a-4224-add1-8e78d4a228dce;
//        threadsQuantity = System.getenv("OUTBOUND_THREADS_QUANTITY") != null ? Integer.parseInt(System.getenv("OUTBOUND_THREADS_QUANTITY")) : 3;
//        runTestService = new RunTestService(url, authUrlToken, threadsQuantity);
//    }
//
//
//    @PostMapping("/runtest")
//    public String runTest(@RequestBody TestFormData formData) {
//        return runTestService.runTestSpringController(formData);
//    }
//}
