package com.splitbuddy.splitbuddy.services;

import com.splitbuddy.splitbuddy.models.User;
import com.splitbuddy.splitbuddy.repositories.UserRepository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.*;
import org.springframework.stereotype.Service;

@Service
public class CustomUserDetailsService implements UserDetailsService {

    @Autowired
    private UserRepository userRepository;

    @Override
    public UserDetails loadUserByUsername(String userIdString) throws UsernameNotFoundException {
        // Parse userId string back to Long (since JWT stores it as string)
        Long userId;
        try {
            userId = Long.valueOf(userIdString);
        } catch (NumberFormatException e) {
            throw new UsernameNotFoundException("Invalid Long format: " + userIdString);
        }

        // Find user by Long ID
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UsernameNotFoundException("User not found with ID: " + userIdString));

        return org.springframework.security.core.userdetails.User.builder()
                .username(user.getId().toString()) // Use Long as string in UserDetails
                .password(user.getPasswordHash())
                .authorities("USER") // or map from user.getRoles() if you have roles
                .build();
    }
}
