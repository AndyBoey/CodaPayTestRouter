package com.codapay.router.service;

import com.codapay.router.dto.ApplicationRequestDTO;
import com.codapay.router.engine.RoutingConfig;
import com.codapay.router.engine.RoutingLookup;
import com.codapay.router.payload.ApiPayload;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.server.ResponseStatusException;

import java.net.ConnectException;

@Service
public class RequestServiceImpl implements RequestService {
    @Autowired
    private RoutingLookup lookup;

    @Override
    public ApplicationRequestDTO request(ApplicationRequestDTO dto) {
        RoutingConfig routingConfig = null;
        ApplicationRequestDTO responseDTO = null;

        boolean breakLoop = false;

        while (!breakLoop) {
            routingConfig = lookup.getNextRouting();

            if (routingConfig == null) {
                // no configuration yet!!
                System.out.println("No application servers registered yet!!");
                return null;
            }

            System.out.println("Forwarding to instanceId " + routingConfig.getInstanceId() + ", address: " + routingConfig.getAddress());

            try {
                responseDTO = this.forward(dto, routingConfig);
            }
            catch(Exception ex) {
                System.out.println("Removing route " + routingConfig.getInstanceId() + " due to connectivity error");
                lookup.removeRoute(routingConfig.getInstanceId());
                breakLoop = false;
            }

            return responseDTO;
        }

        return null;
    }

    protected ApplicationRequestDTO forward(ApplicationRequestDTO dto, RoutingConfig routingConfig) {
        RestTemplate restTemplate = new RestTemplate();
        ApiPayload requestPayload = new ApiPayload();
        requestPayload.setGame(dto.getGame());
        requestPayload.setGamerID(dto.getGamerID());
        requestPayload.setPoints(dto.getPoints());

        ApplicationRequestDTO responseDto = null;
        ApiPayload responsePayload = restTemplate.postForObject(routingConfig.getAddress(), requestPayload, ApiPayload.class);
        if(responsePayload != null) {
            responseDto = new ApplicationRequestDTO();
            responseDto.setGame(responsePayload.getGame());
            responseDto.setGamerID(responsePayload.getGamerID());
            responseDto.setPoints(responsePayload.getPoints());
        }

        return responseDto;
    }
}
