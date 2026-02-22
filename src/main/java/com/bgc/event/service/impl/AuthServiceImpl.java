package com.bgc.event.service.impl;

/**
 * <pre>
 * - Project    : BGC EVENT
 * - Package    : com.bgc.event.service.impl
 * - File       : AuthServiceImpl.java
 * - Date       : 2026. 02. 21.
 * - User       : NTAGANIRA H.
 * - Desc       : Implementation of authentication and authorization service
 * </pre>
 */

import com.bgc.event.dto.LoginRequest;
import com.bgc.event.dto.LoginResponse;
import com.bgc.event.entity.User;
import com.bgc.event.exception.AuthenticationException;
import com.bgc.event.repository.UserRepository;
import com.bgc.event.security.JwtTokenProvider;
import com.bgc.event.service.AuthService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {
    
    private final UserRepository userRepository;
    private final JwtTokenProvider jwtTokenProvider;
    private final BCryptPasswordEncoder passwordEncoder;
    
    // In-memory token blacklist (in production, use Redis)
    private final Set<String> tokenBlacklist = ConcurrentHashMap.newKeySet();
    
    @Override
    @Transactional(readOnly = true)
    public LoginResponse authenticate(LoginRequest loginRequest) throws AuthenticationException {
        log.info("Authenticating user with email: {}", loginRequest.getEmail());
        
        User user = userRepository.findByEmail(loginRequest.getEmail())
                .orElseThrow(() -> {
                    log.warn("Authentication failed: User not found with email: {}", loginRequest.getEmail());
                    return new AuthenticationException("Invalid email or password");
                });
        
        if (!passwordEncoder.matches(loginRequest.getPassword(), user.getPassword())) {
            log.warn("Authentication failed: Invalid password for user: {}", loginRequest.getEmail());
            throw new AuthenticationException("Invalid email or password");
        }
        
        if (!user.isEnabled()) {
            log.warn("Authentication failed: User account is disabled: {}", loginRequest.getEmail());
            throw new AuthenticationException("Account is disabled");
        }
        
        String token = jwtTokenProvider.generateToken(user);
        String refreshToken = jwtTokenProvider.generateRefreshToken(user);
        
        // Update last login timestamp
        user.setLastLoginAt(LocalDateTime.now());
        userRepository.save(user);
        
        log.info("User authenticated successfully: {}", loginRequest.getEmail());
        
        return LoginResponse.builder()
                .token(token)
                .refreshToken(refreshToken)
                .tokenType("Bearer")
                .expiresIn(jwtTokenProvider.getExpirationInSeconds())
                .userId(user.getId())
                .email(user.getEmail())
                .firstName(user.getFirstName())
                .lastName(user.getLastName())
                .roles(user.getRoles())
                .build();
    }
    
    @Override
    @Cacheable(value = "users", key = "#token")
    public User validateToken(String token) throws AuthenticationException {
        log.debug("Validating token");
        
        if (tokenBlacklist.contains(token)) {
            log.warn("Token validation failed: Token is blacklisted");
            throw new AuthenticationException("Token has been invalidated");
        }
        
        if (!jwtTokenProvider.validateToken(token)) {
            log.warn("Token validation failed: Invalid token");
            throw new AuthenticationException("Invalid or expired token");
        }
        
        String email = jwtTokenProvider.getEmailFromToken(token);
        
        return userRepository.findByEmail(email)
                .orElseThrow(() -> {
                    log.warn("Token validation failed: User not found with email: {}", email);
                    return new AuthenticationException("User not found");
                });
    }
    
    @Override
    @Transactional(readOnly = true)
    public boolean hasRole(Long userId, String requiredRole) {
        log.debug("Checking role {} for user ID: {}", requiredRole, userId);
        
        return userRepository.findById(userId)
                .map(user -> user.getRoles().stream()
                        .anyMatch(role -> role.getName().equals(requiredRole)))
                .orElse(false);
    }
    
    @Override
    @CacheEvict(value = "users", key = "#token")
    public void logout(String token) {
        log.info("Logging out user with token: {}", token);
        
        // Add token to blacklist
        tokenBlacklist.add(token);
        
        // In production with Redis, set TTL same as token expiration
        log.info("User logged out successfully");
    }
    
    @Override
    public String refreshToken(String refreshToken) throws AuthenticationException {
        log.debug("Refreshing token");
        
        if (!jwtTokenProvider.validateRefreshToken(refreshToken)) {
            log.warn("Token refresh failed: Invalid refresh token");
            throw new AuthenticationException("Invalid refresh token");
        }
        
        String email = jwtTokenProvider.getEmailFromRefreshToken(refreshToken);
        
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> {
                    log.warn("Token refresh failed: User not found with email: {}", email);
                    return new AuthenticationException("User not found");
                });
        
        log.info("Token refreshed successfully for user: {}", email);
        
        return jwtTokenProvider.generateToken(user);
    }
    
    @Override
    @Transactional
    public void changePassword(Long userId, String oldPassword, String newPassword) throws AuthenticationException {
        log.info("Changing password for user ID: {}", userId);
        
        User user = userRepository.findById(userId)
                .orElseThrow(() -> {
                    log.warn("Password change failed: User not found with ID: {}", userId);
                    return new AuthenticationException("User not found");
                });
        
        if (!passwordEncoder.matches(oldPassword, user.getPassword())) {
            log.warn("Password change failed: Incorrect old password for user ID: {}", userId);
            throw new AuthenticationException("Current password is incorrect");
        }
        
        user.setPassword(passwordEncoder.encode(newPassword));
        user.setPasswordChangedAt(LocalDateTime.now());
        userRepository.save(user);
        
        // Invalidate all existing tokens for this user
        // In production, you'd want to add all user's tokens to blacklist
        
        log.info("Password changed successfully for user ID: {}", userId);
    }
}