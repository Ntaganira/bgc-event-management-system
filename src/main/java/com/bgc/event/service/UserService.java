package com.bgc.event.service;

/**
 * <pre>
 * - Project    : BGC EVENT
 * - Package    : com.bgc.event.service
 * - File       : UserService.java
 * - Date       : 2026. 02. 21.
 * - User       : NTAGANIRA H.
 * - Desc       : Service interface for user management operations
 * </pre>
 */

import com.bgc.event.dto.UserDto;
import com.bgc.event.dto.UserProfileDto;
import com.bgc.event.dto.UserCreateRequest;
import com.bgc.event.dto.UserUpdateRequest;
import com.bgc.event.entity.User;
import com.bgc.event.exception.UserException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

public interface UserService {
    
    /**
     * Create a new user
     */
    UserDto createUser(UserCreateRequest request, Long createdBy) throws UserException;
    
    /**
     * Update existing user
     */
    UserDto updateUser(Long userId, UserUpdateRequest request, Long updatedBy) throws UserException;
    
    /**
     * Get user by ID
     */
    UserDto getUserById(Long userId) throws UserException;
    
    /**
     * Get user by email
     */
    UserDto getUserByEmail(String email) throws UserException;
    
    /**
     * Get all users with pagination
     */
    Page<UserDto> getAllUsers(Pageable pageable);
    
    /**
     * Search users
     */
    Page<UserDto> searchUsers(String searchTerm, Pageable pageable);
    
    /**
     * Delete user (soft delete)
     */
    void deleteUser(Long userId, Long deletedBy) throws UserException;
    
    /**
     * Enable/disable user
     */
    void setUserEnabled(Long userId, boolean enabled, Long updatedBy) throws UserException;
    
    /**
     * Assign roles to user
     */
    void assignRoles(Long userId, List<Long> roleIds, Long assignedBy) throws UserException;
    
    /**
     * Get user profile
     */
    UserProfileDto getUserProfile(Long userId) throws UserException;
    
    /**
     * Update user profile (self-service)
     */
    UserProfileDto updateUserProfile(Long userId, UserProfileDto profile) throws UserException;
    
    /**
     * Change password
     */
    void changePassword(Long userId, String oldPassword, String newPassword) throws UserException;
    
    /**
     * Reset password (admin only)
     */
    void resetPassword(Long userId, String newPassword, Long resetBy) throws UserException;
    
    /**
     * Get dashboard stats
     */
    long countActiveUsers();
    
    /**
     * Get user growth rate
     */
    double getUserGrowthRate();
    
    /**
     * Get recent inactive users
     */
    List<User> getRecentInactiveUsers(int days);
    
    /**
     * Check if user has role
     */
    boolean hasRole(Long userId, String role);
    
    /**
     * Get user activity summary
     */
    Map<String, Object> getUserActivitySummary(Long userId);
    
    /**
     * Get users by role
     */
    List<UserDto> getUsersByRole(String role);
    
    /**
     * Lock user account
     */
    void lockUser(Long userId, LocalDateTime lockUntil, String reason, Long lockedBy) throws UserException;
    
    /**
     * Unlock user account
     */
    void unlockUser(Long userId, Long unlockedBy) throws UserException;
    
    /**
     * Get online users
     */
    List<UserDto> getOnlineUsers();
}