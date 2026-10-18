package org.suraksha.controller;

import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.suraksha.dto.*;
import org.suraksha.service.AuthService;

/**
 * Authentication REST Controller exposing endpoints for user registration,
 * credential login, and authenticated user profile retrieval.
 */
@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    /**
     * Registers a new student account.
     * Accessible publicly.
     */
    @PostMapping("/register")
    public ResponseEntity<ApiResponse<AuthResponseDto>> register(@Valid @RequestBody RegisterRequestDto request) {
        AuthResponseDto response = authService.register(request);
        return new ResponseEntity<>(
                ApiResponse.success("Account registered successfully", response),
                HttpStatus.CREATED
        );
    }

    /**
     * Authenticates existing user credentials and returns a signed JWT.
     * Accessible publicly.
     */
    @PostMapping("/login")
    public ResponseEntity<ApiResponse<AuthResponseDto>> login(@Valid @RequestBody LoginRequestDto request) {
        AuthResponseDto response = authService.login(request);
        return ResponseEntity.ok(ApiResponse.success("Login successful", response));
    }

    /**
     * Retrieves the profile of the currently authenticated user.
     * Requires a valid Bearer token in the Authorization header.
     */
    @GetMapping("/me")
    public ResponseEntity<ApiResponse<UserProfileDto>> getCurrentUser(Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ApiResponse.error("Authentication required"));
        }

        UserProfileDto profile = authService.getCurrentUserProfile(authentication.getName());
        return ResponseEntity.ok(ApiResponse.success("Profile retrieved successfully", profile));
    }
}
