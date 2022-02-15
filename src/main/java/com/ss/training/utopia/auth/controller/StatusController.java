package com.ss.training.utopia.auth.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class StatusController {

    @GetMapping("/api/v1/login/ready")
    public ResponseEntity<String> healthCheck() {
        return ResponseEntity.ok("Healthy");
    }

}
