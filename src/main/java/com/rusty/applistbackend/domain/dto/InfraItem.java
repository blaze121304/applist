package com.rusty.applistbackend.domain.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class InfraItem {
    private String host;
    private String domain;
    @JsonProperty("port(f/w)")
    private String port;
    private String ssl;
    private String application;
}