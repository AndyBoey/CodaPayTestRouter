package com.codapay.router.controller;

import com.codapay.router.engine.RoutingConfig;
import com.codapay.router.engine.RoutingLookup;
import com.codapay.router.payload.RegisterPayload;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("route")
public class RoutingController {
    @Autowired
    private RoutingLookup routingLookup;

    @PostMapping("/register")
    public String route(@RequestBody RegisterPayload registerRequest) {

        RoutingConfig routingConfig = new RoutingConfig();
        routingConfig.setAddress(registerRequest.getIpAddr());
        routingConfig.setInstanceId(registerRequest.getInstanceId());

        return Boolean.toString(routingLookup.registerRoute(routingConfig));
    }
    @DeleteMapping("/{instanceId}")
    public void delete(@PathVariable String instanceId) {
        this.routingLookup.removeRoute(instanceId);
    }

    @PostMapping("/clear")
    public void clear() {
        this.routingLookup.clearRoutes();
    }

    @GetMapping(value="/{instanceId}")
    public String instanceStatus(@PathVariable String instanceId) {
        RoutingConfig routingConfig = this.routingLookup.getRoutingByInstanceId(instanceId);

        if(routingConfig == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Routing with instance ID " + instanceId + " not found");
        }

        return routingConfig.toString();
    }

    @GetMapping(value="/status", produces = MediaType.TEXT_PLAIN_VALUE)
    public String status() {
        return routingLookup.getStatus();
    }
}
