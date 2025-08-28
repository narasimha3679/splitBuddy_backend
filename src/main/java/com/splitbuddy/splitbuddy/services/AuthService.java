package com.splitbuddy.splitbuddy.services;

import java.util.Optional;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import com.splitbuddy.splitbuddy.dto.request.LoginRequest;
import com.splitbuddy.splitbuddy.dto.request.RegistrationRequest;
import com.splitbuddy.splitbuddy.dto.response.AuthResponse;
import com.splitbuddy.splitbuddy.dto.response.UserInfoResponse;
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
        // if not, create new user and return success message
        if (userRepository.findByEmail(request.getEmail()).isPresent()) {
            return new AuthResponse(null, "Email already in use");
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

            // return status 404
            return new AuthResponse(null, "User not found");
        }

        User user = userOpt.get();
        if (passwordEncoder.matches(request.getPassword(), user.getPasswordHash())) {
            String token = jwtUtil.generateToken(user.getId().toString());
            return new AuthResponse(token, "Login successful");
        } else {
            return new AuthResponse(null, "Invalid email or password");
        }
    }

    public UserInfoResponse getUserInfo() {
        // Get the current authenticated user
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String userIdString = authentication.getName(); // This is the UUID string

        // Convert to UUID and fetch user
        UUID userId = UUID.fromString(userIdString);
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        // Return clean user info
        return new UserInfoResponse(
                user.getId().toString(),
                user.getName(), // Assuming you have a name field
                user.getEmail()// Assuming you have an avatar field
        );
    }

}
