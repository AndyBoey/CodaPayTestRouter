package com.codapay.router.engine;

import lombok.Getter;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class LocalRoutingLookupImpl implements RoutingLookup {
    private List<RoutingConfig> routingList = new ArrayList();
    private Integer currRouting = 0;
    private Map<String, RoutingConfig> routingMap = new HashMap();

    @Override
    public boolean registerRoute(RoutingConfig routingConfig) {
        if(routingMap.get(routingConfig.getInstanceId()) != null) {
            System.out.println("Route with instance Id " + routingConfig.getInstanceId() + " has already been registered!");
            return false;
        }

        // TODO: test if the server is actually there


        // register into lookup
        synchronized (this) {
            routingMap.put(routingConfig.getInstanceId(), routingConfig);
            routingList.add(routingConfig);

            System.out.println("registered " + routingConfig.toString());
        }

        return true;
    }

    @Override
    public RoutingConfig getNextRouting() {
        RoutingConfig currRoutingConfig;
        synchronized (this) {
            if(routingList.size() == 0)
                currRoutingConfig = null;
            else {
                if (currRouting >= routingList.size()) {
                    currRouting = 0;
                }

                currRoutingConfig = routingList.get(currRouting);
                currRouting++;
            }
        }

        return currRoutingConfig;
    }

    @Override
    public RoutingConfig getRoutingByInstanceId(String instanceId) {
        return this.routingMap.get(instanceId);
    }

    @Override
    public boolean removeRoute(String instanceId) {
        synchronized (this) {
            RoutingConfig currConfig = this.routingMap.get(instanceId);
            if(currConfig != null) {
                this.routingMap.remove(instanceId);
                this.routingList.remove(currConfig);
            }
        }

        return true;
    }

    @Override
    public boolean clearRoutes() {
        synchronized (this) {
            this.routingMap.clear();
            this.routingList.clear();
        }

        return true;
    }

    @Override
    public String getStatus() {
        StringBuffer buff = new StringBuffer();
        for(int i = 0; i < routingList.size(); i++) {
            buff.append(routingList.get(i).toString());
            buff.append("\n");
            buff.append("current routing: " + this.currRouting);
        }

        return buff.toString();
    }


}
