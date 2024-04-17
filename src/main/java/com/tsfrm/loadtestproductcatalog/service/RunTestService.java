package com.tsfrm.loadtestproductcatalog.service;


import com.tsfrm.loadtestproductcatalog.domain.*;
import com.tsfrm.loadtestproductcatalog.repository.JsonStorageRepository;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.concurrent.Executors;

public class RunTestService {

    VdiProductGenerateService vdiProductGenerateService;
    public String httpUrl;
    public Integer threadsQuantity;

    private static final Logger log = LogManager.getLogger(RunTestService.class);

    public RunTestService(String httpUrl, Integer threadsQuantity) {
        var jsonStorageRepository = new JsonStorageRepository();
        this.vdiProductGenerateService = new VdiProductGenerateService(jsonStorageRepository);
        this.httpUrl = httpUrl;
        this.threadsQuantity = threadsQuantity;
    }

    public String runTest(TestFormData testFormData) {
        try {
            log.info("Process started with data: " + testFormData);
            var messages = vdiProductGenerateService.generateMessages(testFormData);
            log.info("Created " + messages.size() + " messages");

            var executorService = Executors.newFixedThreadPool(threadsQuantity);

            for (int i = 0; i < messages.size(); i++) {
                var requestProcessor = new RequestProcessor(messages.get(i), httpUrl, i + 1);
                executorService.execute(requestProcessor);
            }
            executorService.shutdown();
            while (!executorService.isTerminated()) {}
            log.info("All messages have been processed successfully");
            return "All messages have been processed successfully";
        } catch (Exception e) {
            return e.getMessage();
        }
    }


}
