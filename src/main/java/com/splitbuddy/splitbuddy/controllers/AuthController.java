package com.splitbuddy.splitbuddy.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import jakarta.validation.Valid;

import com.splitbuddy.splitbuddy.dto.request.LoginRequest;
import com.splitbuddy.splitbuddy.dto.request.RegistrationRequest;
import com.splitbuddy.splitbuddy.dto.response.AuthResponse;
import com.splitbuddy.splitbuddy.dto.response.UserInfoResponse;
import com.splitbuddy.splitbuddy.services.AuthService;

/**
 * Authentication Controller
 * 
 * Handles user authentication and registration.
 * 
 * Frontend Types: See expo/splitbuddy/src/types/api-contracts.ts
 * - LoginRequest, RegistrationRequest, AuthResponse, UserInfoResponse
 * 
 * API Documentation: See backend/API_DOCUMENTATION.md#authentication
 * OpenAPI Spec: See backend/openapi.yaml#/paths/auth
 * 
 * @see expo/splitbuddy/src/utils/api.ts for frontend API functions
 */
@RestController
@RequestMapping("/api/auth")
@CrossOrigin(origins = "*")
public class AuthController {

    @Autowired
    private AuthService authService;

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginRequest loginRequest) {
        AuthResponse response = authService.login(loginRequest);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/register")
    public ResponseEntity<AuthResponse> register(@Valid @RequestBody RegistrationRequest registerRequest) {
        AuthResponse response = authService.register(registerRequest);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/me")
    public ResponseEntity<UserInfoResponse> getUserInfo() {
        UserInfoResponse response = authService.getUserInfo();
        return ResponseEntity.ok(response);
    }

    @GetMapping("/test")
    public ResponseEntity<String> testEndpoint() {
        return ResponseEntity.ok("Auth controller is working! CORS should be working now.");
    }
}
