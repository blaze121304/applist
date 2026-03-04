package com.rusty.applistbackend.domain;

import lombok.Getter;
import lombok.Setter;
import java.util.UUID;

@Getter
@Setter
public class Todo {
    private String id;
    private String content;
    private boolean completed;

    public Todo() {
        this.id = UUID.randomUUID().toString(); // 생성 시 자동 ID 부여
    }

    public Todo(String content) {
        this.id = UUID.randomUUID().toString();
        this.content = content;
        this.completed = false;
    }
}