package com.bgc.event.repository;

import com.bgc.event.entity.Permission;
import com.bgc.event.entity.Role;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.Set;

@Repository
public interface PermissionRepository extends JpaRepository<Permission, Long> {
    Optional<Permission> findByName(String name);

    @Query("""
                SELECT DISTINCT r FROM Role r
                LEFT JOIN FETCH r.permissions
            """)
    List<Role> findAllWithPermissions();

    @Query("""
                SELECT p FROM Role r
                JOIN r.permissions p
                WHERE r.id = :roleId
            """)
    Set<Permission> findPermissionsByRoleId(Long roleId);
}
