package com.codapay.router.service;

import com.codapay.router.dto.ApplicationRequestDTO;
import com.codapay.router.engine.RoutingConfig;
import com.codapay.router.engine.RoutingLookup;
import com.codapay.router.payload.ApiPayload;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpServerErrorException;
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
            else {

                System.out.println("Forwarding to instanceId " + routingConfig.getInstanceId() + ", address: " + routingConfig.getAddress());

                try {
                    responseDTO = this.forward(dto, routingConfig);
                    breakLoop = true;
                } catch (Exception ex) {
                    if (ex instanceof HttpServerErrorException) {
                        HttpServerErrorException httpEx = (HttpServerErrorException) ex;
                        System.out.println(httpEx.getStatusCode());
                        /**
                         * better to return null and respond as error back to client here
                         * because a HttpServer error 5xx could mean that the request is processed but failed in different stages
                         * let the client decide what to do next
                         */
                        breakLoop = true;
                    } else if (ex instanceof ConnectException) {
                        System.out.println("Removing route " + routingConfig.getInstanceId() + " due to connectivity error");
                        lookup.removeRoute(routingConfig.getInstanceId());
                        breakLoop = false; // loop again to try sending to another instance
                    }
                }
            }
        }

        if(responseDTO == null) {
            lookup.accumulateRouteStat(routingConfig.getInstanceId(), 1, false);
        }
        else {
            lookup.accumulateRouteStat(routingConfig.getInstanceId(), 1, true);
        }

        return responseDTO;
    }

    protected ApplicationRequestDTO forward(ApplicationRequestDTO dto, RoutingConfig routingConfig) {
        RestTemplate restTemplate = new RestTemplate();
        ApiPayload requestPayload = new ApiPayload();
        requestPayload.setGame(dto.getGame());
        requestPayload.setGamerID(dto.getGamerID());
        requestPayload.setPoints(dto.getPoints());

        ApplicationRequestDTO responseDto = null;

        HttpEntity<ApiPayload> requestEntity = new HttpEntity(requestPayload);
        ResponseEntity<ApiPayload> responseEntity = restTemplate.exchange(routingConfig.getAddress(), HttpMethod.POST, requestEntity, ApiPayload.class);
        System.out.println(responseEntity.getStatusCode());

        responseDto = new ApplicationRequestDTO();
        responseDto.setGame(responseEntity.getBody().getGame());
        responseDto.setGamerID(responseEntity.getBody().getGamerID());
        responseDto.setPoints(responseEntity.getBody().getPoints());

        return responseDto;
    }
}
