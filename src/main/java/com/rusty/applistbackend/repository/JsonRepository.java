package com.rusty.applistbackend.repository;

import com.rusty.applistbackend.domain.AppData;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.PostConstruct;
import org.springframework.stereotype.Repository;

import java.io.File;
import java.io.IOException;

@Repository
public class JsonRepository {

    // 프로젝트 루트(applist-backend) 바로 아래에 infra-data.json 파일로 저장됩니다.
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
                currentData = new AppData(); // 예외 발생 시 빈 상태로 덮어쓰기 (리스크 대응)
                save();
            }
        } else {
            currentData = new AppData();
            save();
        }
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