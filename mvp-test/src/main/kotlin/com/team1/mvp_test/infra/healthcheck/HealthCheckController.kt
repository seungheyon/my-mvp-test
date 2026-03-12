package com.team1.mvp_test.infra.healthcheck

import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RestController

@RestController
class HealthCheckController {

    @GetMapping("/health")
    fun healthCheck(): ResponseEntity<String> {
        return ResponseEntity
            .status(HttpStatus.OK)
            .body("UP")
    }
}