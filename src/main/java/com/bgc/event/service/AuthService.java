package com.bgc.event.service;

/**
 * <pre>
 * - Project    : BGC EVENT
 * - Package    : com.bgc.event.service
 * - File       : AuthService.java
 * - Date       : 2026. 02. 21.
 * - User       : NTAGANIRA H.
 * - Desc       : Service interface for authentication and authorization operations
 * </pre>
 */

import com.bgc.event.dto.LoginRequest;
import com.bgc.event.dto.LoginResponse;
import com.bgc.event.entity.User;
import com.bgc.event.exception.AuthenticationException;

public interface AuthService {
    
    /**
     * Authenticates user and generates JWT token
     * @param loginRequest containing email and password
     * @return LoginResponse with JWT token and user details
     * @throws AuthenticationException if credentials are invalid
     */
    LoginResponse authenticate(LoginRequest loginRequest) throws AuthenticationException;
    
    /**
     * Validates JWT token and returns user details
     * @param token JWT token
     * @return User entity if token is valid
     * @throws AuthenticationException if token is invalid or expired
     */
    User validateToken(String token) throws AuthenticationException;
    
    /**
     * Checks if user has required role
     * @param userId ID of the user
     * @param requiredRole Role to check against
     * @return true if user has the role
     */
    boolean hasRole(Long userId, String requiredRole);
    
    /**
     * Logs out user by invalidating token
     * @param token JWT token to invalidate
     */
    void logout(String token);
    
    /**
     * Refreshes expired JWT token
     * @param refreshToken Refresh token
     * @return New JWT token
     * @throws AuthenticationException if refresh token is invalid
     */
    String refreshToken(String refreshToken) throws AuthenticationException;
    
    /**
     * Changes user password
     * @param userId ID of the user
     * @param oldPassword Current password
     * @param newPassword New password
     * @throws AuthenticationException if old password is incorrect
     */
    void changePassword(Long userId, String oldPassword, String newPassword) throws AuthenticationException;
}