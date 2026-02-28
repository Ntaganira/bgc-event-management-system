package com.bgc.event.service;

import com.bgc.event.dto.RegisterDto;
import com.bgc.event.entity.User;

import java.util.List;
import java.util.Optional;

public interface UserService {
    User register(RegisterDto dto);

    List<User> findAll();

    Optional<User> findById(Long id);

    Optional<User> findByEmail(String email);

    User save(User user);

    void deleteById(Long id);

    void toggleEnabled(Long id);

    void addRole(Long userId, Long roleId);

    void removeRole(Long userId, Long roleId);

    long count();
}
