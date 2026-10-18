package org.suraksha.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.suraksha.dto.ApiResponse;
import org.suraksha.dto.LessonProgressResponseDto;
import org.suraksha.dto.UserProgressSummaryDto;
import org.suraksha.service.ProgressService;

@RestController
@RequestMapping("/api/progress")
public class ProgressController {

    private final ProgressService progressService;

    public ProgressController(ProgressService progressService) {
        this.progressService = progressService;
    }

    /**
     * Marks a specific lesson as completed by the authenticated student.
     * PROTECTED ACCESS: Requires valid JWT Bearer token authentication.
     */
    @PostMapping("/lessons/{id}")
    public ResponseEntity<ApiResponse<LessonProgressResponseDto>> markLessonCompleted(
            @PathVariable Long id,
            Authentication authentication
    ) {
        String userEmail = authentication.getName();
        LessonProgressResponseDto progress = progressService.markLessonCompleted(id, userEmail);
        return ResponseEntity.ok(ApiResponse.success("Lesson marked as completed successfully", progress));
    }

    /**
     * Retrieves overall learning progress, lesson completions, quiz attempts, and drill records
     * for the currently authenticated student.
     * PROTECTED ACCESS: Requires valid JWT Bearer token authentication. Enforces strict user isolation.
     */
    @GetMapping
    public ResponseEntity<ApiResponse<UserProgressSummaryDto>> getUserProgress(Authentication authentication) {
        String userEmail = authentication.getName();
        UserProgressSummaryDto progressSummary = progressService.getUserProgress(userEmail);
        return ResponseEntity.ok(ApiResponse.success("User progress retrieved successfully", progressSummary));
    }
}
