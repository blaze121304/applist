package com.rusty.applistbackend.domain.dto;

import lombok.Getter;
import lombok.Setter;
import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
public class InfraTierGroup {
    private String tier;
    private List<InfraItem> items = new ArrayList<>();
}
