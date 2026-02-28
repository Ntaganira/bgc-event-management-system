package com.bgc.event.security;

/**
 * <pre>
 * - Project    : BGC EVENT
 * - Package    : com.bgc.event.security
 * - File       : CustomUserDetailsService.java
 * - Date       : 2026-02-27
 * - Author     : NTAGANIRA Heritier
 * - Desc       : Loads user with roles+permissions using JOIN FETCH (no N+1, no EAGER)
 * </pre>
 */

import com.bgc.event.entity.User;
import com.bgc.event.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;

    @Override
    @Transactional(readOnly = true)
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {

        // Single query: user + roles + permissions via JOIN FETCH
        User user = userRepository.findByEmailWithRolesAndPermissions(email)
            .orElseThrow(() -> new UsernameNotFoundException("User not found: " + email));

        Set<GrantedAuthority> authorities = new HashSet<>();

        user.getRoles().forEach(role -> {
            // Grant the role itself  (e.g. ROLE_ADMIN)
            authorities.add(new SimpleGrantedAuthority(role.getName()));
            // Grant each individual permission  (e.g. CREATE_EVENT)
            role.getPermissions().forEach(perm ->
                authorities.add(new SimpleGrantedAuthority(perm.getName()))
            );
        });

        return org.springframework.security.core.userdetails.User
            .withUsername(user.getEmail())
            .password(user.getPassword())
            .authorities(authorities)
            .accountExpired(false)
            .accountLocked(!user.isEnabled())
            .credentialsExpired(false)
            .disabled(!user.isEnabled())
            .build();
    }
}
