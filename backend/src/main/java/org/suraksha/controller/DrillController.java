package org.suraksha.controller;

import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.suraksha.dto.ApiResponse;
import org.suraksha.dto.DrillResultDto;
import org.suraksha.dto.DrillScenarioDto;
import org.suraksha.dto.DrillSubmissionRequestDto;
import org.suraksha.service.DrillService;

@RestController
@RequestMapping("/api/drills")
public class DrillController {

    private final DrillService drillService;

    public DrillController(DrillService drillService) {
        this.drillService = drillService;
    }

    /**
     * Retrieves virtual mock drill scenario details, decision steps, and choice trees.
     * PUBLIC ACCESS: Allows learners to run simulations.
     */
    @GetMapping("/{slug}")
    public ResponseEntity<ApiResponse<DrillScenarioDto>> getDrillBySlug(@PathVariable String slug) {
        DrillScenarioDto scenario = drillService.getDrillByIdOrSlug(slug);
        return ResponseEntity.ok(ApiResponse.success("Drill scenario retrieved successfully", scenario));
    }

    /**
     * Submits completed decision path for a virtual mock drill and records a DrillAttempt.
     * PROTECTED ACCESS: Requires valid JWT Bearer token authentication.
     */
    @PostMapping("/{id}/submit")
    public ResponseEntity<ApiResponse<DrillResultDto>> submitDrill(
            @PathVariable String id,
            @Valid @RequestBody DrillSubmissionRequestDto request,
            Authentication authentication
    ) {
        String userEmail = authentication.getName();
        DrillResultDto result = drillService.submitDrill(id, request, userEmail);
        return ResponseEntity.ok(ApiResponse.success("Drill decisions evaluated successfully", result));
    }
}
