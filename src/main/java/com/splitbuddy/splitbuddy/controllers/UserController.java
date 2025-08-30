package com.splitbuddy.splitbuddy.controllers;

import com.splitbuddy.splitbuddy.dto.response.UserInfoResponse;
import com.splitbuddy.splitbuddy.models.User;
import com.splitbuddy.splitbuddy.repositories.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/users")
public class UserController {

    @Autowired
    private UserRepository userRepository;

    @PostMapping
    public ResponseEntity<UserInfoResponse> createUser(@RequestBody User user) {
        User savedUser = userRepository.save(user);
        UserInfoResponse response = new UserInfoResponse(
                savedUser.getId().toString(),
                savedUser.getName(),
                savedUser.getEmail());
        return ResponseEntity.ok(response);
    }

    @GetMapping
    public ResponseEntity<List<UserInfoResponse>> getAllUsers() {
        List<User> users = userRepository.findAll();
        List<UserInfoResponse> responses = users.stream()
                .map(user -> new UserInfoResponse(
                        user.getId().toString(),
                        user.getName(),
                        user.getEmail()))
                .collect(Collectors.toList());
        return ResponseEntity.ok(responses);
    }

    @GetMapping("/search")
    public ResponseEntity<UserInfoResponse> searchUsersByEmail(@RequestParam String email) {
        Optional<User> user = userRepository.findByEmail(email);
        return user.map(u -> new UserInfoResponse(
                u.getId().toString(),
                u.getName(),
                u.getEmail()))
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }
}
