package org.suraksha.controller;

import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.suraksha.dto.ApiResponse;
import org.suraksha.dto.QuizDetailDto;
import org.suraksha.dto.QuizResultDto;
import org.suraksha.dto.QuizSubmissionRequestDto;
import org.suraksha.service.QuizService;

@RestController
@RequestMapping("/api/quizzes")
public class QuizController {

    private final QuizService quizService;

    public QuizController(QuizService quizService) {
        this.quizService = quizService;
    }

    /**
     * Retrieves quiz questions and answer options for student assessment.
     * PUBLIC ACCESS: Allows learners to preview and take quizzes.
     * CRITICAL SECURITY: Correct answer flags and explanations are stripped.
     */
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<QuizDetailDto>> getQuizById(@PathVariable String id) {
        QuizDetailDto quiz = quizService.getQuizByIdOrSlug(id);
        return ResponseEntity.ok(ApiResponse.success("Quiz retrieved successfully", quiz));
    }

    /**
     * Submits student quiz answers for server-side grading and records a QuizAttempt.
     * PROTECTED ACCESS: Requires valid JWT Bearer token authentication.
     */
    @PostMapping("/{id}/submit")
    public ResponseEntity<ApiResponse<QuizResultDto>> submitQuiz(
            @PathVariable String id,
            @Valid @RequestBody QuizSubmissionRequestDto request,
            Authentication authentication
    ) {
        String userEmail = authentication.getName();
        QuizResultDto result = quizService.submitQuiz(id, request, userEmail);
        return ResponseEntity.ok(ApiResponse.success("Quiz submitted and evaluated successfully", result));
    }
}
