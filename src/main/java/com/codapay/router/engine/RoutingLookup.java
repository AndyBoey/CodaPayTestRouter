package com.codapay.router.engine;

public interface RoutingLookup {
    public boolean registerRoute(RoutingConfig routingConfig);
    public RoutingConfig getNextRouting();
    public RoutingConfig getRoutingByInstanceId(String instanceId);
    public boolean removeRoute(String instanceId);
    public boolean clearRoutes();
    public String getStatus();
}
