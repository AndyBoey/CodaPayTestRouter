package com.codapay.router.payload;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ApiPayload {
    private String game;
    private String gamerID;
    private Integer points;
}
