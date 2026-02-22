package com.bgc.event.repository;

/**
 * <pre>
 * - Project    : BGC EVENT
 * - Package    : com.bgc.event.repository
 * - File       : RoleRepository.java
 * - Date       : 2026. 02. 22.
 * - User       : NTAGANIRA H.
 * - Desc       : Repository for Role entity operations
 * </pre>
 */

import com.bgc.event.entity.Role;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.Set;

@Repository
public interface RoleRepository extends JpaRepository<Role, Long> {
    
    /**
     * Find role by name
     */
    @Cacheable(value = "roles", key = "#name")
    Optional<Role> findByName(String name);
    
    /**
     * Find roles by names
     */
    @Cacheable(value = "roles", key = "#names")
    List<Role> findByNameIn(Set<String> names);
    
    /**
     * Check if role exists by name
     */
    boolean existsByName(String name);
    
    /**
     * Find all roles with their permissions (eager fetching)
     */
    @Query("SELECT DISTINCT r FROM Role r LEFT JOIN FETCH r.permissions")
    List<Role> findAllWithPermissions();
    
    /**
     * Find roles by user ID
     */
    @Query("SELECT r FROM Role r JOIN r.users u WHERE u.id = :userId")
    List<Role> findByUserId(@Param("userId") Long userId);
    
    /**
     * Find roles by permission name
     */
    @Query("SELECT r FROM Role r JOIN r.permissions p WHERE p.name = :permissionName")
    List<Role> findByPermissionName(@Param("permissionName") String permissionName);
    
    /**
     * Find roles by resource and action
     */
    @Query("SELECT r FROM Role r JOIN r.permissions p WHERE p.resource = :resource AND p.action = :action")
    List<Role> findByResourceAndAction(@Param("resource") String resource, @Param("action") String action);
    
    /**
     * Count users by role
     */
    @Query("SELECT COUNT(u) FROM Role r JOIN r.users u WHERE r.id = :roleId")
    long countUsersByRoleId(@Param("roleId") Long roleId);
    
    /**
     * Get all role names
     */
    @Query("SELECT r.name FROM Role r")
    List<String> findAllRoleNames();
    
    /**
     * Find default roles (PUBLIC, etc.)
     */
    @Query("SELECT r FROM Role r WHERE r.name IN ('PUBLIC', 'USER')")
    List<Role> findDefaultRoles();
    
    /**
     * Find admin roles
     */
    @Query("SELECT r FROM Role r WHERE r.name LIKE '%ADMIN%'")
    List<Role> findAdminRoles();
    
    /**
     * Get role with permissions by name
     */
    @Query("SELECT r FROM Role r LEFT JOIN FETCH r.permissions WHERE r.name = :name")
    Optional<Role> findByNameWithPermissions(@Param("name") String name);
    
    /**
     * Delete role if no users assigned
     */
    @Query("SELECT CASE WHEN COUNT(u) = 0 THEN true ELSE false END FROM Role r LEFT JOIN r.users u WHERE r.id = :roleId")
    boolean isRoleUnused(@Param("roleId") Long roleId);
}