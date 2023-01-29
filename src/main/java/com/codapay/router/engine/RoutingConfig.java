package com.codapay.router.engine;

import lombok.Getter;
import lombok.Setter;

import java.net.URI;
import java.net.URL;

@Getter
@Setter
public class RoutingConfig {
    private String address;
    private String instanceId;

    public String toString() {
        return "InstanceId: " + instanceId + ", Addr: " + address;
    }
}
