package com.bgc.event.repository;

import com.bgc.event.entity.Role;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface RoleRepository extends JpaRepository<Role, Long> {
    Optional<Role> findByName(String name);

    boolean existsByName(String name);

    @Query("""
                SELECT DISTINCT r FROM Role r
                LEFT JOIN FETCH r.permissions
            """)
    List<Role> findAllWithPermissions();
}
