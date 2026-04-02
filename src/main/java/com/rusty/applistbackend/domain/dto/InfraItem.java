package com.rusty.applistbackend.domain.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

@Getter
@Setter
public class InfraItem {
    private String id = UUID.randomUUID().toString().replace("-", "").substring(0, 12);
    private String host;
    private String domain;
    @JsonProperty("port(f/w)")
    private String port;
    private String ssl;
    private String application;
    private DeployConfig deployConfig = new DeployConfig();
}
