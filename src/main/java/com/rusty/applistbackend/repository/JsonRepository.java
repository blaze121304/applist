package com.rusty.applistbackend.repository;

import com.rusty.applistbackend.domain.dto.AppData;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.PostConstruct;
import org.springframework.stereotype.Repository;

import java.io.File;
import java.io.IOException;
import java.util.UUID;

@Repository
public class JsonRepository {

    private static final String FILE_PATH = "infra-data.json";
    private final ObjectMapper objectMapper;
    private AppData currentData;

    public JsonRepository(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    @PostConstruct
    public void init() {
        File file = new File(FILE_PATH);
        if (file.exists()) {
            try {
                currentData = objectMapper.readValue(file, AppData.class);
            } catch (IOException e) {
                System.err.println("JSON 읽기 실패, 데이터를 초기화합니다: " + e.getMessage());
                currentData = new AppData();
            }
        } else {
            currentData = new AppData();
        }
        // 기존 아이템에 ID가 없으면 자동 부여 후 저장
        assignMissingIds();
        save();
    }

    private void assignMissingIds() {
        currentData.getInfraTierGroups().forEach(group ->
            group.getItems().forEach(item -> {
                if (item.getId() == null || item.getId().isBlank()) {
                    item.setId(UUID.randomUUID().toString().replace("-", "").substring(0, 12));
                }
            })
        );
    }

    public AppData getData() {
        return currentData;
    }

    public void save() {
        try {
            objectMapper.writerWithDefaultPrettyPrinter().writeValue(new File(FILE_PATH), currentData);
        } catch (IOException e) {
            System.err.println("JSON 저장 실패: " + e.getMessage());
        }
    }
}
