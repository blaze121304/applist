package com.rusty.applistbackend.controller;

import com.rusty.applistbackend.domain.AppData;
import com.rusty.applistbackend.domain.Todo;
import com.rusty.applistbackend.repository.JsonRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
@RequiredArgsConstructor
public class AppController {

    private final JsonRepository jsonRepository;

    // 1. 메인 대시보드
    @GetMapping("/")
    public String index(Model model) {
        // 이 부분 수정: infraRows -> infraTierGroups
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

    // 3. 인프라 정보 저장 (이 부분도 변수명이 바뀌었으므로 확인해주세요)
    @PostMapping("/infra/edit")
    public String editInfraSubmit(@ModelAttribute AppData appData) {
        jsonRepository.getData().setInfraTierGroups(appData.getInfraTierGroups());
        jsonRepository.save();
        return "redirect:/";
    }

    // --- 여기서부터가 날아갔던 Todo 관련 로직입니다! ---

    // 4. Todo 추가
    @PostMapping("/todo/add")
    public String addTodo(@RequestParam String content) {
        if (content != null && !content.trim().isEmpty()) {
            jsonRepository.getData().getTodos().add(new Todo(content));
            jsonRepository.save();
        }
        return "redirect:/";
    }

    // 5. Todo 완료 상태 토글 (V 버튼)
    @PostMapping("/todo/toggle")
    public String toggleTodo(@RequestParam String id) {
        jsonRepository.getData().getTodos().stream()
                .filter(todo -> todo.getId().equals(id))
                .findFirst()
                .ifPresent(todo -> todo.setCompleted(!todo.isCompleted()));
        jsonRepository.save();
        return "redirect:/";
    }

    // 6. Todo 삭제 (X 버튼)
    @PostMapping("/todo/delete")
    public String deleteTodo(@RequestParam String id) {
        jsonRepository.getData().getTodos().removeIf(todo -> todo.getId().equals(id));
        jsonRepository.save();
        return "redirect:/";
    }
}