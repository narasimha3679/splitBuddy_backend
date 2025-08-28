package com.splitbuddy.splitbuddy.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
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

@RestController
@RequestMapping("/api/auth")
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
}
