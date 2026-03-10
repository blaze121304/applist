package com.rusty.applistbackend.domain.dto;

import lombok.Getter;
import lombok.Setter;
import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
public class AppData {
    // 이제 Tier 그룹들의 리스트를 가집니다.
    private List<InfraTierGroup> infraTierGroups = new ArrayList<>();
    private List<Todo> todos = new ArrayList<>();
}