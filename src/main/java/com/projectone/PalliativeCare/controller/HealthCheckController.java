package com.projectone.PalliativeCare.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class HealthCheckController {

    @GetMapping("/healthz") // Using the path Render suggested
    public ResponseEntity<String> healthCheck() {
        // This endpoint simply returns "UP" with a 200 OK status.
        // It's very fast because it doesn't wait for the database or other services.
        return ResponseEntity.ok("UP");
    }
}