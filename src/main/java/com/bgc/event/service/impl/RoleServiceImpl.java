package com.bgc.event.service.impl;

/**
 * <pre>
 * - Project    : BGC EVENT
 * - Package    : com.bgc.event.service.impl
 * - File       : RoleServiceImpl.java
 * - Date       : 2026-02-27
 * - Author     : NTAGANIRA Heritier
 * - Desc       : Role CRUD with permission assignment
 * </pre>
 */

import com.bgc.event.dto.RoleDto;
import com.bgc.event.entity.Permission;
import com.bgc.event.entity.Role;
import com.bgc.event.repository.PermissionRepository;
import com.bgc.event.repository.RoleRepository;
import com.bgc.event.service.RoleService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Transactional
public class RoleServiceImpl implements RoleService {

    private final RoleRepository roleRepository;
    private final PermissionRepository permissionRepository;

    @Override
    public Role create(RoleDto dto) {
        // Ensure name is prefixed with ROLE_
        String name = dto.getName().toUpperCase();
        if (!name.startsWith("ROLE_")) name = "ROLE_" + name;

        if (roleRepository.existsByName(name))
            throw new RuntimeException("Role already exists: " + name);

        Role role = Role.builder()
            .name(name)
            .description(dto.getDescription())
            .build();

        // Assign selected permissions
        if (dto.getPermissionIds() != null) {
            dto.getPermissionIds().forEach(pid ->
                permissionRepository.findById(pid).ifPresent(role.getPermissions()::add)
            );
        }
        return roleRepository.save(role);
    }

    @Override
    public Role update(Long id, RoleDto dto) {
        Role role = roleRepository.findById(id).orElseThrow();
        role.setDescription(dto.getDescription());

        // Rebuild permissions from selected ids
        role.getPermissions().clear();
        if (dto.getPermissionIds() != null) {
            dto.getPermissionIds().forEach(pid ->
                permissionRepository.findById(pid).ifPresent(role.getPermissions()::add)
            );
        }
        return roleRepository.save(role);
    }

    @Override
    public void delete(Long id) {
        Role role = roleRepository.findById(id).orElseThrow();
        // Prevent deleting built-in roles
        if (role.getName().equals("ROLE_ADMIN"))
            throw new RuntimeException("Cannot delete the ROLE_ADMIN role");
        roleRepository.deleteById(id);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Role> findById(Long id) {
        return roleRepository.findById(id);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Role> findAll() {
        return roleRepository.findAll();
    }

    @Override
    public void assignPermission(Long roleId, Long permissionId) {
        Role role = roleRepository.findById(roleId).orElseThrow();
        Permission perm = permissionRepository.findById(permissionId).orElseThrow();
        role.getPermissions().add(perm);
        roleRepository.save(role);
    }

    @Override
    public void removePermission(Long roleId, Long permissionId) {
        Role role = roleRepository.findById(roleId).orElseThrow();
        role.getPermissions().removeIf(p -> p.getId().equals(permissionId));
        roleRepository.save(role);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Role> getAllRolesWithPermissions() {
        return roleRepository.findAllWithPermissions();
    }
}
