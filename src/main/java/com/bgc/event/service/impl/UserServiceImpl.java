package com.bgc.event.service.impl;

/**
 * <pre>
 * - Project    : BGC EVENT
 * - Package    : com.bgc.event.service.impl
 * - File       : UserServiceImpl.java
 * - Date       : 2026-02-27
 * - Author     : NTAGANIRA Heritier
 * </pre>
 */

import com.bgc.event.dto.RegisterDto;
import com.bgc.event.entity.Role;
import com.bgc.event.entity.User;
import com.bgc.event.repository.RoleRepository;
import com.bgc.event.repository.UserRepository;
import com.bgc.event.service.UserService;
import lombok.RequiredArgsConstructor;

import org.hibernate.Hibernate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Transactional
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public User register(RegisterDto dto) {
        if (userRepository.existsByEmail(dto.getEmail())) {
            throw new RuntimeException("Email already registered: " + dto.getEmail());
        }

        User user = User.builder()
                .firstName(dto.getFirstName())
                .lastName(dto.getLastName())
                .email(dto.getEmail())
                .phoneNumber(dto.getPhoneNumber())
                .branch(dto.getBranch())
                .title(dto.getTitle())
                .arrivalDate(dto.getArrivalDate())
                .returnDate(dto.getReturnDate())
                .password(passwordEncoder.encode(dto.getPassword()))
                .enabled(true)
                .emailConfirmed(false)
                .build();

        // Assign default ROLE_USER
        roleRepository.findByName("ROLE_USER").ifPresent(role -> user.getRoles().add(role));

        return userRepository.save(user);
    }

    @Override
    @Transactional(readOnly = true)
    public List<User> findAll() {
        List<User> users = userRepository.findAll();
        users.forEach(user -> Hibernate.initialize(user.getRoles()));
        return users;
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<User> findById(Long id) {

        Optional<User> userOptional = userRepository.findById(id);
        userOptional.ifPresent(user -> Hibernate.initialize(user.getRoles()));

        return userOptional;
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<User> findByEmail(String email) {
        return userRepository.findByEmail(email);
    }

    @Override
    public User save(User user) {
        return userRepository.save(user);
    }

    @Override
    public void deleteById(Long id) {
        userRepository.deleteById(id);
    }

    @Override
    public void toggleEnabled(Long id) {
        userRepository.findById(id).ifPresent(user -> {
            user.setEnabled(!user.isEnabled());
            userRepository.save(user);
        });
    }

    @Override
    public void addRole(Long userId, Long roleId) {
        User user = userRepository.findById(userId).orElseThrow();
        Role role = roleRepository.findById(roleId).orElseThrow();
        user.getRoles().add(role);
        userRepository.save(user);
    }

    @Override
    public void removeRole(Long userId, Long roleId) {
        User user = userRepository.findById(userId).orElseThrow();
        user.getRoles().removeIf(r -> r.getId().equals(roleId));
        userRepository.save(user);
    }

    @Override
    @Transactional(readOnly = true)
    public long count() {
        return userRepository.count();
    }
}
