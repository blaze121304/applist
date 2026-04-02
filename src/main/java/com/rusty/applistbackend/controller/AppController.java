package com.rusty.applistbackend.controller;

import com.rusty.applistbackend.domain.dto.AppData;
import com.rusty.applistbackend.domain.dto.Todo;
import com.rusty.applistbackend.repository.JsonRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@Controller
@RequiredArgsConstructor
public class AppController {

    private final JsonRepository jsonRepository;

    @Value("${info.app.version:unknown}")
    private String version;

    // 1. 메인 대시보드
    @GetMapping("/")
    public String index(Model model) {
        model.addAttribute("tierGroups", jsonRepository.getData().getInfraTierGroups());
        model.addAttribute("todos", jsonRepository.getData().getTodos());
        return "index";
    }

    // 2. 인프라 정보 편집 화면
    @GetMapping("/infra/edit")
    public String editInfraForm(Model model) {
        model.addAttribute("appData", jsonRepository.getData());
        return "infra-edit";
    }

    // 3. 인프라 정보 저장
    @PostMapping("/infra/edit")
    public String editInfraSubmit(@ModelAttribute AppData appData) {
        // 새 아이템에 ID 자동 부여
        appData.getInfraTierGroups().forEach(group ->
            group.getItems().forEach(item -> {
                if (item.getId() == null || item.getId().isBlank()) {
                    item.setId(UUID.randomUUID().toString().replace("-", "").substring(0, 12));
                }
            })
        );
        jsonRepository.getData().setInfraTierGroups(appData.getInfraTierGroups());
        jsonRepository.save();
        return "redirect:/";
    }

    // 4. Todo 추가
    @PostMapping("/todo/add")
    public String addTodo(@RequestParam String content) {
        if (content != null && !content.trim().isEmpty()) {
            jsonRepository.getData().getTodos().add(new Todo(content));
            jsonRepository.save();
        }
        return "redirect:/";
    }

    // 5. Todo 완료 상태 토글
    @PostMapping("/todo/toggle")
    public String toggleTodo(@RequestParam String id) {
        jsonRepository.getData().getTodos().stream()
                .filter(todo -> todo.getId().equals(id))
                .findFirst()
                .ifPresent(todo -> todo.setCompleted(!todo.isCompleted()));
        jsonRepository.save();
        return "redirect:/";
    }

    // 6. Todo 삭제
    @PostMapping("/todo/delete")
    public String deleteTodo(@RequestParam String id) {
        jsonRepository.getData().getTodos().removeIf(todo -> todo.getId().equals(id));
        jsonRepository.save();
        return "redirect:/";
    }

    // 7. 로그인 페이지
    @GetMapping("/login")
    public String loginPage() {
        return "login";
    }

    @GetMapping("/api/version")
    @ResponseBody
    public String version() {
        return version;
    }
}
