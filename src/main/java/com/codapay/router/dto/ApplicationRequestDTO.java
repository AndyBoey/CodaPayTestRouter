package com.codapay.router.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ApplicationRequestDTO {
    private String game;
    private String gamerID;
    private Integer points;
}
