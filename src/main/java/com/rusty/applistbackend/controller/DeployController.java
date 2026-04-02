package com.rusty.applistbackend.controller;

import com.rusty.applistbackend.service.DeployService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class DeployController {

    private final DeployService deployService;

    @PostMapping("/deploy/{appId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<String> deploy(@PathVariable String appId) {
        deployService.deploy(appId);
        return ResponseEntity.ok("배포 시작됨");
    }
}
