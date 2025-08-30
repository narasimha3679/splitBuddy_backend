package com.splitbuddy.splitbuddy.services;

import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import com.splitbuddy.splitbuddy.dto.request.LoginRequest;
import com.splitbuddy.splitbuddy.dto.request.RegistrationRequest;
import com.splitbuddy.splitbuddy.dto.response.AuthResponse;
import com.splitbuddy.splitbuddy.dto.response.UserInfoResponse;
import com.splitbuddy.splitbuddy.exceptions.DuplicateResourceException;
import com.splitbuddy.splitbuddy.exceptions.InvalidCredentialsException;
import com.splitbuddy.splitbuddy.exceptions.UserNotFoundException;
import com.splitbuddy.splitbuddy.models.User;
import com.splitbuddy.splitbuddy.repositories.UserRepository;
import com.splitbuddy.splitbuddy.utility.JwtUtil;

@Service
public class AuthService {

    @Autowired
    private UserRepository userRepository;

    private BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    @Autowired
    private JwtUtil jwtUtil;

    // Method to register a new user
    public AuthResponse register(RegistrationRequest request) {
        // check if user already exists
        if (userRepository.findByEmail(request.getEmail()).isPresent()) {
            throw new DuplicateResourceException("Email already in use");
        }

        User newUser = new User();
        newUser.setName(request.getName());
        newUser.setEmail(request.getEmail());
        newUser.setPasswordHash(passwordEncoder.encode(request.getPassword()));
        userRepository.save(newUser);

        String token = jwtUtil.generateToken(newUser.getId().toString());

        return new AuthResponse(token, "Registration successful");
    }

    // Login
    public AuthResponse login(LoginRequest request) {
        Optional<User> userOpt = userRepository.findByEmail(request.getEmail());
        if (userOpt.isEmpty()) {
            throw new UserNotFoundException("User not found with email: " + request.getEmail());
        }

        User user = userOpt.get();
        if (passwordEncoder.matches(request.getPassword(), user.getPasswordHash())) {
            String token = jwtUtil.generateToken(user.getId().toString());
            return new AuthResponse(token, "Login successful");
        } else {
            throw new InvalidCredentialsException("Invalid email or password");
        }
    }

    public UserInfoResponse getUserInfo() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String userIdString = authentication.getName();
        Long userId = Long.valueOf(userIdString);

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException("User not found with ID: " + userIdString));

        UserInfoResponse response = new UserInfoResponse();
        response.setId(user.getId().toString());
        response.setName(user.getName());
        response.setEmail(user.getEmail());

        return response;
    }
}
