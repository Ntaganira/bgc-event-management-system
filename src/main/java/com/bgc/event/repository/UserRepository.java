package com.bgc.event.repository;

/**
 * <pre>
 * - Project    : BGC EVENT
 * - Package    : com.bgc.event.repository
 * - File       : UserRepository.java
 * - Date       : 2026. 02. 22.
 * - User       : NTAGANIRA H.
 * - Desc       : Repository for User entity operations
 * </pre>
 */

import com.bgc.event.entity.User;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

       // Basic find methods
       @Cacheable(value = "users", key = "#email")
       Optional<User> findByEmail(String email);

       @Cacheable(value = "users", key = "#username")
       Optional<User> findByUsername(String username);

       // Existence checks
       boolean existsByEmail(String email);

       boolean existsByUsername(String username);

       // Find active users
       @Query("SELECT u FROM User u WHERE u.email = :email AND u.enabled = true AND u.deleted = false")
       Optional<User> findActiveUserByEmail(@Param("email") String email);

       @Query("SELECT u FROM User u WHERE u.username = :username AND u.enabled = true AND u.deleted = false")
       Optional<User> findActiveUserByUsername(@Param("username") String username);

       // Find by status
       Page<User> findByEnabledTrueAndDeletedFalse(Pageable pageable);

       Page<User> findByEnabledFalseAndDeletedFalse(Pageable pageable);

       Page<User> findByDeletedFalse(Pageable pageable);

       @Query("SELECT u FROM User u WHERE u.deleted = false ORDER BY u.createdAt DESC")
       Page<User> findAllActiveUsers(Pageable pageable);

       // Search users
       @Query("SELECT u FROM User u WHERE u.deleted = false AND (" +
                     "LOWER(u.firstName) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
                     "LOWER(u.lastName) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
                     "LOWER(u.email) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
                     "LOWER(u.username) LIKE LOWER(CONCAT('%', :search, '%')))")
       Page<User> searchUsers(@Param("search") String search, Pageable pageable);

       // Find by role
       @Query("SELECT u FROM User u JOIN u.roles r WHERE r.name = :roleName AND u.deleted = false")
       List<User> findByRoleName(@Param("roleName") String roleName);

       @Query("SELECT u FROM User u JOIN u.roles r WHERE r.name = :roleName AND u.deleted = false")
       Page<User> findByRoleName(@Param("roleName") String roleName, Pageable pageable);

       // Count queries
       long countByDeletedFalse();

       long countByEnabledTrueAndDeletedFalse();

       long countByEnabledFalseAndDeletedFalse();

       @Query("SELECT COUNT(u) FROM User u WHERE u.createdAt BETWEEN :startDate AND :endDate AND u.deleted = false")
       long countUsersRegisteredBetween(@Param("startDate") LocalDateTime startDate,
                     @Param("endDate") LocalDateTime endDate);

       @Query("SELECT COUNT(u) FROM User u WHERE u.lastLoginAt BETWEEN :start AND :end AND u.deleted = false")
       long countActiveUsersBetween(@Param("start") LocalDateTime start, @Param("end") LocalDateTime end);

       // Update operations
       @Modifying
       @Query("UPDATE User u SET u.lastLoginAt = :lastLogin WHERE u.id = :userId")
       int updateLastLogin(@Param("userId") Long userId, @Param("lastLogin") LocalDateTime lastLogin);

       @Modifying
       @Query("UPDATE User u SET u.failedAttempts = :failedAttempts WHERE u.email = :email")
       int updateFailedAttempts(@Param("failedAttempts") int failedAttempts, @Param("email") String email);

       @Modifying
       @Query("UPDATE User u SET u.lockedUntil = :lockedUntil WHERE u.email = :email")
       int lockUser(@Param("lockedUntil") LocalDateTime lockedUntil, @Param("email") String email);

       @Modifying
       @Query("UPDATE User u SET u.failedAttempts = 0, u.lockedUntil = NULL WHERE u.email = :email")
       int resetLock(@Param("email") String email);

       // Soft delete operations
       @Modifying
       @Query("UPDATE User u SET u.deleted = true, u.deletedAt = :now WHERE u.id = :userId")
       int softDelete(@Param("userId") Long userId, @Param("now") LocalDateTime now);

       @Modifying
       @Query("UPDATE User u SET u.deleted = false, u.deletedAt = NULL WHERE u.id = :userId")
       int restoreUser(@Param("userId") Long userId);

       // Find recently active/inactive
       @Query("SELECT u FROM User u WHERE u.lastLoginAt >= :since AND u.deleted = false ORDER BY u.lastLoginAt DESC")
       List<User> findRecentlyActive(@Param("since") LocalDateTime since, Pageable pageable);

       @Query("SELECT u FROM User u WHERE (u.lastLoginAt IS NULL OR u.lastLoginAt < :since) AND u.deleted = false")
       List<User> findInactiveUsers(@Param("since") LocalDateTime since, Pageable pageable);

       // Find by IDs with deleted filter
       @Query("SELECT u FROM User u WHERE u.id IN :ids AND u.deleted = false")
       List<User> findByIds(@Param("ids") List<Long> ids);

       // Get statistics
       @Query("SELECT FUNCTION('DATE', u.createdAt), COUNT(u) FROM User u " +
                     "WHERE u.createdAt BETWEEN :start AND :end AND u.deleted = false " +
                     "GROUP BY FUNCTION('DATE', u.createdAt) ORDER BY FUNCTION('DATE', u.createdAt)")
       List<Object[]> getUserRegistrationTrend(@Param("start") LocalDateTime start, @Param("end") LocalDateTime end);

       @Query("SELECT r.name, COUNT(u) FROM User u JOIN u.roles r " +
                     "WHERE u.deleted = false GROUP BY r.name")
       List<Object[]> countUsersByRole();
}