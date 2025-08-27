package com.splitbuddy.splitbuddy.utility;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.security.Key;
import java.util.Date;

@Component
public class JwtUtil {

    // Inject values from application.yml
    @Value("${spring.jwt.secret}")
    private String SECRET_KEY;

    @Value("${spring.jwt.expiration}")
    private long EXPIRATION_TIME;

    // this will be used to generate and validate JWT tokens

    private Key getSigningKey() {
        return Keys.hmacShaKeyFor(SECRET_KEY.getBytes());
    }

    // Generate JWT token with userId as subject
    public String generateToken(String userId) {
        return Jwts.builder()
                .subject(userId)
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + EXPIRATION_TIME))
                .signWith(getSigningKey())
                .compact();
    }

    // Validate JWT token using userId
    public boolean validateToken(String token, String userId) {
        try {
            Claims claims = extractAllClaims(token);
            String tokenSubject = claims.getSubject();
            Date expiration = claims.getExpiration();
            return (tokenSubject.equals(userId) && expiration.after(new Date()));
        } catch (Exception e) {
            return false;
        }
    }

    // Extract claims
    private Claims extractAllClaims(String token) {
        return Jwts.parser()
                .verifyWith((javax.crypto.SecretKey) getSigningKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    // Extract subject (user id)
    public String extractSubject(String token) {
        return extractAllClaims(token).getSubject();
    }
}
