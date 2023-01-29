package com.codapay.router.engine;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class RoutingStat {
    private Integer totalRequestCount = 0;
    private Integer totalSuccessRequest = 0;
    private Integer totalErrorRequest = 0;
    private Integer errorRate = 0;
}
