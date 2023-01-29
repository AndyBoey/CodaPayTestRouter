package com.codapay.router.controller;

import com.codapay.router.engine.RoutingConfig;
import com.codapay.router.engine.RoutingLookup;
import com.codapay.router.payload.ApiPayload;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.server.ResponseStatusException;

@RestController
public class RequestController {
    @Autowired
    private RoutingLookup lookup;

    @PostMapping("/request")
    public ApiPayload route(@RequestBody ApiPayload request) {
        RoutingConfig routingConfig = lookup.getNextRouting();

        if(routingConfig == null) {
            // no configuration yet!!
            System.out.println("No application servers registered yet!!");
            throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE, "No application servers available");
        }
        System.out.println("Forwarding to instanceId " + routingConfig.getInstanceId() + ", address: " + routingConfig.getAddress());

        RestTemplate restTemplate = new RestTemplate();
        ApiPayload applicationResponse = restTemplate.postForObject(routingConfig.getAddress(), request, ApiPayload.class);

        return applicationResponse;
    }
}
