package com.bgc.event.service.impl;

/**
 * <pre>
 * - Project    : BGC EVENT
 * - Package    : com.bgc.event.service.impl
 * - File       : UserServiceImpl.java
 * - Date       : 2026. 02. 21.
 * - User       : NTAGANIRA H.
 * - Desc       : Implementation of User Service
 * </pre>
 */

import com.bgc.event.dto.UserCreateRequest;
import com.bgc.event.dto.UserDto;
import com.bgc.event.dto.UserProfileDto;
import com.bgc.event.dto.UserUpdateRequest;
import com.bgc.event.entity.Role;
import com.bgc.event.entity.User;
import com.bgc.event.exception.UserException;
import com.bgc.event.repository.RoleRepository;
import com.bgc.event.repository.UserRepository;
import com.bgc.event.service.AuditService;
import com.bgc.event.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class UserServiceImpl implements UserService {
    
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final BCryptPasswordEncoder passwordEncoder;
    private final AuditService auditService;
    
    @Override
    public UserDto createUser(UserCreateRequest request, Long createdBy) throws UserException {
        log.info("Creating new user with email: {}", request.getEmail());
        
        // Check if user exists
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new UserException("User with email " + request.getEmail() + " already exists");
        }
        
        if (userRepository.existsByUsername(request.getUsername())) {
            throw new UserException("Username " + request.getUsername() + " is already taken");
        }
        
        // Create user
        User user = User.builder()
                .username(request.getUsername())
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .firstName(request.getFirstName())
                .lastName(request.getLastName())
                .phoneNumber(request.getPhoneNumber())
                .enabled(true)
                .build();
        
        // Assign default role (PUBLIC)
        Role publicRole = roleRepository.findByName("PUBLIC")
                .orElseThrow(() -> new UserException("Default role not found"));
        user.setRoles(new HashSet<>(Collections.singletonList(publicRole)));
        
        // Assign additional roles if any
        if (request.getRoleIds() != null && !request.getRoleIds().isEmpty()) {
            Set<Role> roles = new HashSet<>(roleRepository.findAllById(request.getRoleIds()));
            user.getRoles().addAll(roles);
        }
        
        User savedUser = userRepository.save(user);
        
        auditService.logAction("CREATE_USER", createdBy, "USER", savedUser.getId(),
                savedUser.getEmail(), null, savedUser, null, "SUCCESS", null, null);
        
        log.info("User created successfully with ID: {}", savedUser.getId());
        return mapToDto(savedUser);
    }
    
    @Override
    @CacheEvict(value = "users", key = "#userId")
    public UserDto updateUser(Long userId, UserUpdateRequest request, Long updatedBy) throws UserException {
        log.info("Updating user ID: {}", userId);
        
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserException("User not found with ID: " + userId));
        
        // Store old values for audit
        User oldUser = cloneUser(user);
        
        // Update fields
        if (request.getFirstName() != null) user.setFirstName(request.getFirstName());
        if (request.getLastName() != null) user.setLastName(request.getLastName());
        if (request.getPhoneNumber() != null) user.setPhoneNumber(request.getPhoneNumber());
        if (request.getEnabled() != null) user.setEnabled(request.getEnabled());
        
        // Update roles if provided
        if (request.getRoleIds() != null) {
            Set<Role> roles = new HashSet<>(roleRepository.findAllById(request.getRoleIds()));
            user.setRoles(roles);
        }
        
        User updatedUser = userRepository.save(user);
        
        auditService.logAction("UPDATE_USER", updatedBy, "USER", userId,
                user.getEmail(), oldUser, updatedUser, null, "SUCCESS", null, null);
        
        log.info("User ID: {} updated successfully", userId);
        return mapToDto(updatedUser);
    }
    
    @Override
    @Cacheable(value = "users", key = "#userId")
    @Transactional(readOnly = true)
    public UserDto getUserById(Long userId) throws UserException {
        log.debug("Fetching user by ID: {}", userId);
        
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserException("User not found with ID: " + userId));
        
        return mapToDto(user);
    }
    
    @Override
    @Transactional(readOnly = true)
    public UserDto getUserByEmail(String email) throws UserException {
        log.debug("Fetching user by email: {}", email);
        
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UserException("User not found with email: " + email));
        
        return mapToDto(user);
    }
    
    @Override
    @Transactional(readOnly = true)
    public Page<UserDto> getAllUsers(Pageable pageable) {
        log.debug("Fetching all users, page: {}", pageable);
        
        Page<User> users = userRepository.findByDeletedFalse(pageable);
        return users.map(this::mapToDto);
    }
    
    @Override
    @Transactional(readOnly = true)
    public Page<UserDto> searchUsers(String searchTerm, Pageable pageable) {
        log.debug("Searching users with term: {}", searchTerm);
        
        Page<User> users = userRepository.searchUsers(searchTerm, pageable);
        return users.map(this::mapToDto);
    }
    
    @Override
    @CacheEvict(value = "users", key = "#userId")
    public void deleteUser(Long userId, Long deletedBy) throws UserException {
        log.info("Deleting user ID: {}", userId);
        
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserException("User not found with ID: " + userId));
        
        // Soft delete
        user.setDeleted(true);
        user.setDeletedAt(LocalDateTime.now());
        user.setEnabled(false);
        userRepository.save(user);
        
        auditService.logAction("DELETE_USER", deletedBy, "USER", userId,
                user.getEmail(), null, null, null, "SUCCESS", null, null);
        
        log.info("User ID: {} deleted successfully", userId);
    }
    
    @Override
    public void setUserEnabled(Long userId, boolean enabled, Long updatedBy) throws UserException {
        log.info("Setting user ID: {} enabled: {}", userId, enabled);
        
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserException("User not found with ID: " + userId));
        
        user.setEnabled(enabled);
        userRepository.save(user);
        
        auditService.logAction(enabled ? "ENABLE_USER" : "DISABLE_USER", updatedBy,
                "USER", userId, user.getEmail(), null, null, null, "SUCCESS", null, null);
    }
    
    @Override
    public void assignRoles(Long userId, List<Long> roleIds, Long assignedBy) throws UserException {
        log.info("Assigning roles to user ID: {}", userId);
        
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserException("User not found with ID: " + userId));
        
        Set<Role> roles = new HashSet<>(roleRepository.findAllById(roleIds));
        user.setRoles(roles);
        userRepository.save(user);
        
        auditService.logAction("ASSIGN_ROLES", assignedBy, "USER", userId,
                user.getEmail(), null, null, null, "SUCCESS", null, null);
    }
    
    @Override
    @Transactional(readOnly = true)
    public UserProfileDto getUserProfile(Long userId) throws UserException {
        log.debug("Fetching profile for user ID: {}", userId);
        
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserException("User not found with ID: " + userId));
        
        return mapToProfileDto(user);
    }
    
    @Override
    public UserProfileDto updateUserProfile(Long userId, UserProfileDto profile) throws UserException {
        log.info("Updating profile for user ID: {}", userId);
        
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserException("User not found with ID: " + userId));
        
        // Update allowed profile fields
        if (profile.getFirstName() != null) user.setFirstName(profile.getFirstName());
        if (profile.getLastName() != null) user.setLastName(profile.getLastName());
        if (profile.getPhoneNumber() != null) user.setPhoneNumber(profile.getPhoneNumber());
        
        User updatedUser = userRepository.save(user);
        
        auditService.logAction("UPDATE_PROFILE", userId, "USER", userId,
                user.getEmail(), null, null, null, "SUCCESS", null, null);
        
        return mapToProfileDto(updatedUser);
    }
    
    @Override
    public void changePassword(Long userId, String oldPassword, String newPassword) throws UserException {
        log.info("Changing password for user ID: {}", userId);
        
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserException("User not found with ID: " + userId));
        
        if (!passwordEncoder.matches(oldPassword, user.getPassword())) {
            throw new UserException("Current password is incorrect");
        }
        
        user.setPassword(passwordEncoder.encode(newPassword));
        user.setPasswordChangedAt(LocalDateTime.now());
        userRepository.save(user);
        
        auditService.logAction("CHANGE_PASSWORD", userId, "USER", userId,
                user.getEmail(), null, null, null, "SUCCESS", null, null);
    }
    
    @Override
    public void resetPassword(Long userId, String newPassword, Long resetBy) throws UserException {
        log.info("Resetting password for user ID: {} by admin: {}", userId, resetBy);
        
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserException("User not found with ID: " + userId));
        
        user.setPassword(passwordEncoder.encode(newPassword));
        user.setPasswordChangedAt(LocalDateTime.now());
        userRepository.save(user);
        
        auditService.logAction("RESET_PASSWORD", resetBy, "USER", userId,
                user.getEmail(), null, null, null, "SUCCESS", null, null);
    }
    
    @Override
    @Transactional(readOnly = true)
    public long countActiveUsers() {
        return userRepository.count();
    }
    
    @Override
    @Transactional(readOnly = true)
    public double getUserGrowthRate() {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime lastMonth = now.minusMonths(1);
        LocalDateTime previousMonth = lastMonth.minusMonths(1);
        
        long thisMonthCount = userRepository.countUsersRegisteredBetween(lastMonth, now);
        long lastMonthCount = userRepository.countUsersRegisteredBetween(previousMonth, lastMonth);
        
        if (lastMonthCount == 0) return thisMonthCount > 0 ? 100.0 : 0.0;
        return ((double) (thisMonthCount - lastMonthCount) / lastMonthCount) * 100;
    }
    
    @Override
    @Transactional(readOnly = true)
    public List<User> getRecentInactiveUsers(int days) {
        LocalDateTime cutoff = LocalDateTime.now().minusDays(days);
        return userRepository.findAll().stream()
                .filter(user -> user.getLastLoginAt() == null || 
                        user.getLastLoginAt().isBefore(cutoff))
                .limit(10)
                .collect(Collectors.toList());
    }
    
    @Override
    @Transactional(readOnly = true)
    public boolean hasRole(Long userId, String role) {
        return userRepository.findById(userId)
                .map(user -> user.getRoles().stream()
                        .anyMatch(r -> r.getName().equals(role)))
                .orElse(false);
    }
    
    @Override
    @Transactional(readOnly = true)
    public Map<String, Object> getUserActivitySummary(Long userId) {
        Map<String, Object> summary = new HashMap<>();
        
        // This would integrate with audit service
        summary.put("totalLogins", 0);
        summary.put("lastLogin", null);
        summary.put("eventsCreated", 0);
        summary.put("registrations", 0);
        
        return summary;
    }
    
    @Override
    @Transactional(readOnly = true)
    public List<UserDto> getUsersByRole(String role) {
        return userRepository.findAll().stream()
                .filter(user -> user.getRoles().stream()
                        .anyMatch(r -> r.getName().equals(role)))
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }
    
    @Override
    public void lockUser(Long userId, LocalDateTime lockUntil, String reason, Long lockedBy) throws UserException {
        log.info("Locking user ID: {} until: {}", userId, lockUntil);
        
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserException("User not found with ID: " + userId));
        
        user.setEnabled(false);
        user.setLockedUntil(lockUntil);
        userRepository.save(user);
        
        auditService.logAction("LOCK_USER", lockedBy, "USER", userId,
                user.getEmail() + " - Reason: " + reason, null, null, null, "SUCCESS", null, null);
    }
    
    @Override
    public void unlockUser(Long userId, Long unlockedBy) throws UserException {
        log.info("Unlocking user ID: {}", userId);
        
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserException("User not found with ID: " + userId));
        
        user.setEnabled(true);
        user.setLockedUntil(null);
        userRepository.save(user);
        
        auditService.logAction("UNLOCK_USER", unlockedBy, "USER", userId,
                user.getEmail(), null, null, null, "SUCCESS", null, null);
    }
    
    @Override
    @Transactional(readOnly = true)
    public List<UserDto> getOnlineUsers() {
        // This would check active sessions
        // Placeholder implementation
        return new ArrayList<>();
    }
    
    private UserDto mapToDto(User user) {
        return UserDto.builder()
                .id(user.getId())
                .username(user.getUsername())
                .email(user.getEmail())
                .firstName(user.getFirstName())
                .lastName(user.getLastName())
                .phoneNumber(user.getPhoneNumber())
                .enabled(user.isEnabled())
                .lastLoginAt(user.getLastLoginAt())
                .roles(user.getRoles().stream().map(Role::getName).collect(Collectors.toSet()))
                .createdAt(user.getCreatedAt())
                .build();
    }
    
    private UserProfileDto mapToProfileDto(User user) {
        return UserProfileDto.builder()
                .id(user.getId())
                .username(user.getUsername())
                .email(user.getEmail())
                .firstName(user.getFirstName())
                .lastName(user.getLastName())
                .phoneNumber(user.getPhoneNumber())
                .roles(user.getRoles().stream().map(Role::getName).collect(Collectors.toList()))
                .memberSince(user.getCreatedAt())
                .lastLogin(user.getLastLoginAt())
                .build();
    }
    
    private User cloneUser(User user) {
        User clone = new User();
        clone.setId(user.getId());
        clone.setUsername(user.getUsername());
        clone.setEmail(user.getEmail());
        clone.setFirstName(user.getFirstName());
        clone.setLastName(user.getLastName());
        clone.setPhoneNumber(user.getPhoneNumber());
        clone.setEnabled(user.isEnabled());
        clone.setRoles(new HashSet<>(user.getRoles()));
        return clone;
    }
}