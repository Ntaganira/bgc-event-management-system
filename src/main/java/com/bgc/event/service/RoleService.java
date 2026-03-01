package com.bgc.event.service;

import com.bgc.event.dto.RoleDto;
import com.bgc.event.entity.Role;
import java.util.List;
import java.util.Optional;

public interface RoleService {
    Role create(RoleDto dto);

    Role update(Long id, RoleDto dto);

    void delete(Long id);

    Optional<Role> findById(Long id);

    List<Role> findAll();

    void assignPermission(Long roleId, Long permissionId);

    void removePermission(Long roleId, Long permissionId);
}
