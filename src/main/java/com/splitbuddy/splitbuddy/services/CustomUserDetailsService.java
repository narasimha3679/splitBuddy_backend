package com.splitbuddy.splitbuddy.services;

import com.splitbuddy.splitbuddy.models.User;
import com.splitbuddy.splitbuddy.repositories.UserRepository;

import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.*;
import org.springframework.stereotype.Service;

@Service
public class CustomUserDetailsService implements UserDetailsService {

    @Autowired
    private UserRepository userRepository;

    @Override
    public UserDetails loadUserByUsername(String userIdString) throws UsernameNotFoundException {
        // Parse userId string back to UUID (since JWT stores it as string)
        UUID userId;
        try {
            userId = UUID.fromString(userIdString);
        } catch (IllegalArgumentException e) {
            throw new UsernameNotFoundException("Invalid UUID format: " + userIdString);
        }

        // Find user by UUID
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UsernameNotFoundException("User not found with ID: " + userIdString));

        return org.springframework.security.core.userdetails.User.builder()
                .username(user.getId().toString()) // Use UUID as string in UserDetails
                .password(user.getPasswordHash())
                .authorities("USER") // or map from user.getRoles() if you have roles
                .build();
    }
}
