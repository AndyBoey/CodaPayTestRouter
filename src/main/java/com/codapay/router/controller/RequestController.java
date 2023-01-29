package com.codapay.router.controller;

import com.codapay.router.Application;
import com.codapay.router.dto.ApplicationRequestDTO;
import com.codapay.router.engine.RoutingConfig;
import com.codapay.router.engine.RoutingLookup;
import com.codapay.router.payload.ApiPayload;
import com.codapay.router.service.RequestService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

@RestController
public class RequestController {
    @Autowired
    private RequestService requestService;

    @PostMapping("/request")
    public ApiPayload route(@RequestBody ApiPayload requestPayload) {
        ApplicationRequestDTO requestDTO = new ApplicationRequestDTO();
        requestDTO.setGame(requestPayload.getGame());
        requestDTO.setGamerID(requestPayload.getGamerID());
        requestDTO.setPoints(requestPayload.getPoints());

        ApplicationRequestDTO responseDTO = requestService.request(requestDTO);
        if(responseDTO == null) {

            throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE, "Unable to request from backend");
        }

        ApiPayload responsePayload = new ApiPayload();
        responsePayload.setGame(responseDTO.getGame());
        responsePayload.setGamerID(responseDTO.getGamerID());
        responsePayload.setPoints(responseDTO.getPoints());

        return responsePayload;
    }
}
