package com.splitbuddy.splitbuddy.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.splitbuddy.splitbuddy.dto.request.LoginRequest;
import com.splitbuddy.splitbuddy.dto.request.RegistrationRequest;
import com.splitbuddy.splitbuddy.dto.response.AuthResponse;
import com.splitbuddy.splitbuddy.services.AuthService;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    @Autowired
    private AuthService authService;

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest loginRequest) {
        // Call the authService to handle login
        AuthResponse response = authService.login(loginRequest);

        if (response.getToken() != null) {
            return ResponseEntity.ok(response);
        } else if (response.getMessage().equals("User not found")) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
        } else {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
        }

    }

    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody RegistrationRequest registerRequest) {
        // Call the authService to handle registration
        return ResponseEntity.ok(authService.register(registerRequest));
    }

    // get user info
    @GetMapping("/me")
    public ResponseEntity<?> getUserInfo() {
        return ResponseEntity.ok(authService.getUserInfo());
    }

}
