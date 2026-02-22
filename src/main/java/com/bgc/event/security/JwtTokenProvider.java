package com.bgc.event.security;

/**
 * <pre>
 * - Project    : BGC EVENT
 * - Package    : com.bgc.event.security
 * - File       : JwtTokenProvider.java
 * - Date       : 2026. 02. 22.
 * - User       : NTAGANIRA H.
 * - Desc       : JWT token generation and validation provider
 * </pre>
 */

import com.bgc.event.entity.User;
import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import java.security.Key;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Component
public class JwtTokenProvider {

    @Value("${app.jwt.secret:mySuperSecretKeyForJWTTokenGenerationAndValidation2026}")
    private String jwtSecret;

    @Value("${app.jwt.expiration:86400000}")
    private int jwtExpirationInMs;
    
    @Value("${app.jwt.refresh-expiration:604800000}")
    private int refreshExpirationInMs;

    private Key key() {
        return Keys.hmacShaKeyFor(jwtSecret.getBytes());
    }

    /**
     * Generate token from Authentication object
     */
    public String generateToken(Authentication authentication) {
        UserDetails userDetails = (UserDetails) authentication.getPrincipal();
        
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + jwtExpirationInMs);

        return Jwts.builder()
                .setSubject(userDetails.getUsername())
                .setIssuedAt(now)
                .setExpiration(expiryDate)
                .signWith(key(), SignatureAlgorithm.HS512)
                .compact();
    }
    
    /**
     * Generate token from User entity (used by AuthServiceImpl)
     */
    public String generateToken(User user) {
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + jwtExpirationInMs);
        
        Map<String, Object> claims = new HashMap<>();
        claims.put("userId", user.getId());
        claims.put("email", user.getEmail());
        claims.put("roles", user.getRoles().stream()
                .map(role -> role.getName())
                .collect(Collectors.toList()));
        claims.put("firstName", user.getFirstName());
        claims.put("lastName", user.getLastName());

        return Jwts.builder()
                .setClaims(claims)
                .setSubject(user.getEmail())
                .setIssuedAt(now)
                .setExpiration(expiryDate)
                .signWith(key(), SignatureAlgorithm.HS512)
                .compact();
    }
    
    /**
     * Generate refresh token
     */
    public String generateRefreshToken(User user) {
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + refreshExpirationInMs);
        
        return Jwts.builder()
                .setSubject(user.getEmail())
                .setIssuedAt(now)
                .setExpiration(expiryDate)
                .signWith(key(), SignatureAlgorithm.HS512)
                .compact();
    }

    /**
     * Get username from token
     */
    public String getUsernameFromToken(String token) {
        Claims claims = Jwts.parserBuilder()
                .setSigningKey(key())
                .build()
                .parseClaimsJws(token)
                .getBody();

        return claims.getSubject();
    }
    
    /**
     * Get email from token (alias for getUsernameFromToken)
     */
    public String getEmailFromToken(String token) {
        return getUsernameFromToken(token);
    }
    
    /**
     * Get email from refresh token
     */
    public String getEmailFromRefreshToken(String token) {
        return getUsernameFromToken(token);
    }
    
    /**
     * Get user ID from token
     */
    public Long getUserIdFromToken(String token) {
        Claims claims = Jwts.parserBuilder()
                .setSigningKey(key())
                .build()
                .parseClaimsJws(token)
                .getBody();
        
        return claims.get("userId", Long.class);
    }

    /**
     * Validate token
     */
    public boolean validateToken(String token) {
        try {
            Jwts.parserBuilder().setSigningKey(key()).build().parse(token);
            return true;
        } catch (MalformedJwtException ex) {
            log.error("Invalid JWT token");
        } catch (ExpiredJwtException ex) {
            log.error("Expired JWT token");
        } catch (UnsupportedJwtException ex) {
            log.error("Unsupported JWT token");
        } catch (IllegalArgumentException ex) {
            log.error("JWT claims string is empty");
        }
        return false;
    }
    
    /**
     * Validate refresh token
     */
    public boolean validateRefreshToken(String token) {
        return validateToken(token);
    }
    
    /**
     * Get token expiration in seconds
     */
    public long getExpirationInSeconds() {
        return jwtExpirationInMs / 1000;
    }
}