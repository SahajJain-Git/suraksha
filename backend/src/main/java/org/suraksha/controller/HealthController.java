package org.suraksha.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.suraksha.dto.ApiResponse;
import org.suraksha.dto.HealthResponseDto;
import org.suraksha.service.HealthService;

/**
 * Health check REST Controller.
 * Exposes /api/health to inspect application liveness and database connectivity.
 */
@RestController
@RequestMapping("/api/health")
public class HealthController {

    private final HealthService healthService;

    public HealthController(HealthService healthService) {
        this.healthService = healthService;
    }

    @GetMapping
    public ResponseEntity<ApiResponse<HealthResponseDto>> getHealthStatus() {
        HealthResponseDto health = healthService.checkHealth();
        return ResponseEntity.ok(ApiResponse.success("Health status retrieved successfully", health));
    }
}
