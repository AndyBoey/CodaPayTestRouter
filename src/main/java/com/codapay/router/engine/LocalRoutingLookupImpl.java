package com.codapay.router.engine;

import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class LocalRoutingLookupImpl implements RoutingLookup {
    private List<RoutingConfig> routingList = new ArrayList();
    private Integer currRouting = 0;
    private Map<String, RoutingConfig> routingMap = new HashMap();
    private Map<String, RoutingStat> routingStatsMap = new HashMap();

    @Override
    public boolean registerRoute(RoutingConfig routingConfig) {

        RoutingConfig currRoutingConfig = routingMap.get(routingConfig.getInstanceId());
        // TODO: test if the server is actually there

        // register into lookup
        synchronized (this) {
            if(currRoutingConfig != null) {
                System.out.println("Route with instance Id " + routingConfig.getInstanceId() + " exists, will update URL");
                currRoutingConfig.setAddress(routingConfig.getAddress());
            }
            else {
                routingMap.put(routingConfig.getInstanceId(), routingConfig);
                routingStatsMap.put(routingConfig.getInstanceId(), new RoutingStat());
                routingList.add(routingConfig);
            }

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
                this.routingStatsMap.remove(instanceId);
            }
        }

        return true;
    }

    @Override
    public void accumulateRouteStat(String instanceId, Integer incrementRequest, boolean successRequest) {
        synchronized (this) {
            RoutingStat routingStat = this.routingStatsMap.get(instanceId);
            if(routingStat != null) {
                routingStat.setTotalRequestCount(routingStat.getTotalRequestCount() + incrementRequest);
                if(successRequest)
                    routingStat.setTotalSuccessRequest(routingStat.getTotalSuccessRequest() + incrementRequest);
                else
                    routingStat.setTotalErrorRequest(routingStat.getTotalErrorRequest() + incrementRequest);

                // recalculate error rate
                BigDecimal bdError = new BigDecimal(routingStat.getTotalErrorRequest());
                BigDecimal bdTotal = new BigDecimal(routingStat.getTotalRequestCount());
                BigDecimal bdErrorRate = bdError.divide(bdTotal, 2, RoundingMode.HALF_UP).multiply(new BigDecimal(100));
                routingStat.setErrorRate(bdErrorRate.intValue());
            }
        }
    }

    @Override
    public boolean clearRoutes() {
        synchronized (this) {
            this.routingMap.clear();
            this.routingList.clear();
            this.routingStatsMap.clear();
        }

        return true;
    }

    @Override
    public String getStatus() {
        StringBuffer buff = new StringBuffer();
        for(int i = 0; i < routingList.size(); i++) {
            buff.append(routingList.get(i).toString());
            buff.append("\n");
        }

        buff.append("\n");
        buff.append("current routing: " + this.currRouting);
        buff.append("\n");
        buff.append("\n");

        for(int i = 0; i < routingList.size(); i++) {
            RoutingStat currRoutingStat = this.routingStatsMap.get(routingList.get(i).getInstanceId());
            buff.append(routingList.get(i).getInstanceId())
                    .append(", total req: ").append(currRoutingStat.getTotalRequestCount())
                    .append(", total success: ").append(currRoutingStat.getTotalSuccessRequest())
                    .append(", total error: ").append(currRoutingStat.getTotalErrorRequest())
                    .append(", error rate: ").append(currRoutingStat.getErrorRate()).append("%");
            buff.append("\n");
        }

        return buff.toString();
    }


}
