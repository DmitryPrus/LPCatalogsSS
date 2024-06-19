package com.tsfrm.loadtestproductcatalog.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.tsfrm.loadtestproductcatalog.domain.VdiProductsTransaction;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;


public class RequestProcessor implements Runnable {
    private VdiProductsTransaction message;
    private String url;
    private String authUrlToken;
    private int number;

    private final String requestIdPrefix = "load-product-catalogs-test-";


    private static final Logger log = LogManager.getLogger(RequestProcessor.class);

    public RequestProcessor(VdiProductsTransaction message, String url, String authUrlToken, int number) {
        this.message = message;
        this.url = url;
        this.authUrlToken = authUrlToken;
        this.number = number;
    }

    @Override
    public void run() {
        try {
            log.info("Thread " + number + " started handle of message.");
            var objectMapper = new ObjectMapper();
            objectMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
            objectMapper.registerModule(new JavaTimeModule());

            var json = objectMapper.writeValueAsString(message);
            var httpClient = HttpClient.newHttpClient();
            var request = HttpRequest.newBuilder()
                    .uri(URI.create(url + "/" + requestIdPrefix + number))
                    .header("Content-Type", "application/json")
                    .header("Authorization", "Bearer " + authUrlToken)
                    .POST(HttpRequest.BodyPublishers.ofString(json))
                    .build();

            log.info("Thread " + number + " is sending a message to URL: " + url);
            var response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

            int statusCode = response.statusCode();
            if (statusCode >= 400) {
                log.error("Error sending message. Status :" + statusCode + " " + response.body());
            } else {
                log.info("Thread " + number + " finished with status: " + statusCode);
            }
        } catch (IOException | InterruptedException e) {
            throw new RuntimeException(e);
        }
    }
}
