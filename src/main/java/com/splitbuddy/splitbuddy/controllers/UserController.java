package com.splitbuddy.splitbuddy.controllers;

import com.splitbuddy.splitbuddy.models.User;
import com.splitbuddy.splitbuddy.repositories.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/users")
public class UserController {

    @Autowired
    private UserRepository userRepository;

    @PostMapping
    public ResponseEntity<User> createUser(@RequestBody User user) {
        return ResponseEntity.ok(userRepository.save(user));
    }

    @GetMapping
    public ResponseEntity<List<User>> getAllUsers() {
        return ResponseEntity.ok(userRepository.findAll());
    }

    // search user by email
    @GetMapping("/search")
    public ResponseEntity<User> searchUsersByEmail(@RequestParam String email) {
        Optional<User> user = userRepository.findByEmail(email);
        return ResponseEntity.ok(user.orElse(null));
    }
}
