package com.bgc.event.service.impl;

/**
 * <pre>
 * - Project    : BGC EVENT
 * - Package    : com.bgc.event.service.impl
 * - File       : AuditServiceImpl.java
 * - Date       : 2026. 02. 21.
 * - User       : NTAGANIRA H.
 * - Desc       : Enhanced Audit Service implementation
 * </pre>
 */

import com.bgc.event.dto.AuditLogDto;
import com.bgc.event.dto.AuditSummaryDto;
import com.bgc.event.entity.AuditLog;
import com.bgc.event.entity.User;
import com.bgc.event.repository.AuditLogRepository;
import com.bgc.event.repository.UserRepository;
import com.bgc.event.service.AuditService;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import jakarta.servlet.http.HttpServletRequest;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuditServiceImpl implements AuditService {
    
    private final AuditLogRepository auditLogRepository;
    private final UserRepository userRepository;
    private final ObjectMapper objectMapper;
    
    @Override
    @Async
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void logAction(String action, Long userId, String entityType, Long entityId, 
                         String entityName, Object oldValues, Object newValues,
                         HttpServletRequest request, String status, 
                         String errorMessage, Long executionTimeMs) {
        
        try {
            // Get current request if not provided
            if (request == null) {
                ServletRequestAttributes attributes = 
                    (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
                request = attributes != null ? attributes.getRequest() : null;
            }
            
            // Get user details
            String username = null;
            String userEmail = null;
            String userRole = null;
            
            if (userId != null) {
                User user = userRepository.findById(userId).orElse(null);
                if (user != null) {
                    username = user.getUsername();
                    userEmail = user.getEmail();
                    userRole = user.getRoles().stream()
                            .map(r -> r.getName())
                            .collect(Collectors.joining(","));
                }
            }
            
            // Convert objects to JSON
            String oldValuesJson = oldValues != null ? objectMapper.writeValueAsString(oldValues) : null;
            String newValuesJson = newValues != null ? objectMapper.writeValueAsString(newValues) : null;
            
            // Generate changes summary
            String changesSummary = generateChangesSummary(oldValues, newValues);
            
            // Build audit log
            AuditLog auditLog = AuditLog.builder()
                    .action(action)
                    .actionCategory(null) // Will be set by @PrePersist
                    .userId(userId)
                    .username(username)
                    .userEmail(userEmail)
                    .userRole(userRole)
                    .entityType(entityType)
                    .entityId(entityId)
                    .entityName(entityName)
                    .ipAddress(getClientIp(request))
                    .userAgent(request != null ? request.getHeader("User-Agent") : null)
                    .requestMethod(request != null ? request.getMethod() : null)
                    .requestPath(request != null ? request.getRequestURI() : null)
                    .oldValues(oldValuesJson)
                    .newValues(newValuesJson)
                    .changesSummary(changesSummary)
                    .status(status)
                    .errorMessage(errorMessage)
                    .executionTimeMs(executionTimeMs != null ? executionTimeMs.intValue() : null)
                    .build();
            
            auditLogRepository.save(auditLog);
            
        } catch (Exception e) {
            // Log error but don't throw - audit logging should never break the main flow
            log.error("Failed to create audit log: {}", e.getMessage(), e);
        }
    }
    
    @Override
    public void logAction(String action, Long userId, String entityType, Long entityId, String details) {
        logAction(action, userId, entityType, entityId, null, null, null, 
                  null, "SUCCESS", null, null);
    }
    
    @Override
    public void logSuccess(String action, Long userId, String entityType, Long entityId,
                          String entityName, Object oldValues, Object newValues) {
        logAction(action, userId, entityType, entityId, entityName, oldValues, newValues,
                  null, "SUCCESS", null, null);
    }
    
    @Override
    public void logFailure(String action, Long userId, String entityType, Long entityId,
                          String errorMessage, Exception exception) {
        logAction(action, userId, entityType, entityId, null, null, null,
                  null, "FAILURE", errorMessage, null);
    }
    
    @Override
    public void logLogin(String email, boolean success, String failureReason, HttpServletRequest request) {
        String action = success ? "LOGIN_SUCCESS" : "LOGIN_FAILURE";
        String status = success ? "SUCCESS" : "FAILURE";
        
        // Find user by email for successful login
        Long userId = null;
        if (success) {
            User user = userRepository.findByEmail(email).orElse(null);
            userId = user != null ? user.getId() : null;
        }
        
        logAction(action, userId, "AUTH", null, email, null, null,
                  request, status, failureReason, null);
    }
    
    @Override
    public void logExport(Long userId, String entityType, String format, int recordCount) {
        String action = "EXPORT_" + entityType + "_" + format;
        logAction(action, userId, "EXPORT", null, 
                  recordCount + " records exported", null, null,
                  null, "SUCCESS", null, null);
    }
    
    @Override
    public Page<AuditLogDto> searchAuditLogs(LocalDateTime startDate, LocalDateTime endDate,
                                            Long userId, String entityType, 
                                            String actionCategory, String searchTerm,
                                            Pageable pageable) {
        
        Page<AuditLog> auditLogs = auditLogRepository.searchAuditLogs(
                startDate, endDate, userId, entityType, actionCategory, searchTerm, pageable);
        
        List<AuditLogDto> dtos = auditLogs.getContent().stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
        
        return new PageImpl<>(dtos, pageable, auditLogs.getTotalElements());
    }
    
    @Override
    public AuditSummaryDto getAuditSummary(LocalDateTime startDate, LocalDateTime endDate) {
        List<Object[]> activityByCategory = auditLogRepository.getActivityByCategory(startDate, endDate);
        List<Object[]> mostActiveEntities = auditLogRepository.getMostActiveEntities(startDate, endDate, Pageable.ofSize(5));
        
        Map<String, Long> categoryMap = activityByCategory.stream()
                .collect(Collectors.toMap(
                        arr -> (String) arr[0],
                        arr -> (Long) arr[1]
                ));
        
        List<Map<String, Object>> topEntities = mostActiveEntities.stream()
                .map(arr -> Map.of(
                        "entityType", arr[0],
                        "count", arr[1]
                ))
                .collect(Collectors.toList());
        
        return AuditSummaryDto.builder()
                .totalLogs(auditLogRepository.countByDateRange(startDate, endDate))
                .activityByCategory(categoryMap)
                .mostActiveEntities(topEntities)
                .build();
    }
    
    @Override
    public Map<String, Long> getUserActivityTimeline(Long userId, int days) {
        LocalDateTime endDate = LocalDateTime.now();
        LocalDateTime startDate = endDate.minusDays(days);
        
        List<Object[]> timeline = auditLogRepository.getAuditTrailByDay(startDate, endDate);
        
        Map<String, Long> activityMap = new LinkedHashMap<>();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        
        // Initialize all days
        for (int i = 0; i <= days; i++) {
            LocalDateTime date = startDate.plusDays(i);
            activityMap.put(formatter.format(date), 0L);
        }
        
        // Fill with actual data
        timeline.forEach(arr -> {
            String date = arr[0].toString();
            Long count = (Long) arr[1];
            activityMap.put(date, count);
        });
        
        return activityMap;
    }
    
    @Override
    @Transactional
    public int cleanOldLogs(LocalDateTime before) {
        // This would need a custom delete query
        // For now, return 0
        log.warn("Clean old logs called for before: {}", before);
        return 0;
    }
    
    private String getClientIp(HttpServletRequest request) {
        if (request == null) return null;
        
        String ip = request.getHeader("X-Forwarded-For");
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("Proxy-Client-IP");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("WL-Proxy-Client-IP");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("HTTP_CLIENT_IP");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("HTTP_X_FORWARDED_FOR");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getRemoteAddr();
        }
        
        // Handle multiple IPs (take first)
        if (ip != null && ip.contains(",")) {
            ip = ip.split(",")[0].trim();
        }
        
        return ip;
    }
    
    private String generateChangesSummary(Object oldValues, Object newValues) {
        if (oldValues == null || newValues == null) return null;
        
        try {
            Map<String, Object> oldMap = objectMapper.convertValue(oldValues, Map.class);
            Map<String, Object> newMap = objectMapper.convertValue(newValues, Map.class);
            
            List<String> changes = new ArrayList<>();
            
            for (Map.Entry<String, Object> entry : newMap.entrySet()) {
                String key = entry.getKey();
                Object newVal = entry.getValue();
                Object oldVal = oldMap.get(key);
                
                if (!Objects.equals(oldVal, newVal)) {
                    // Skip sensitive fields
                    if (key.toLowerCase().contains("password") || 
                        key.toLowerCase().contains("token")) {
                        changes.add(key + ": [HIDDEN]");
                    } else {
                        changes.add(key + ": '" + oldVal + "' → '" + newVal + "'");
                    }
                }
            }
            
            return changes.isEmpty() ? null : String.join(", ", changes);
            
        } catch (Exception e) {
            log.debug("Failed to generate changes summary", e);
            return null;
        }
    }
    
    private AuditLogDto convertToDto(AuditLog auditLog) {
        return AuditLogDto.builder()
                .id(auditLog.getId())
                .timestamp(auditLog.getCreatedAt())
                .action(auditLog.getAction())
                .actionCategory(auditLog.getActionCategory())
                .username(auditLog.getUsername())
                .userEmail(auditLog.getUserEmail())
                .userRole(auditLog.getUserRole())
                .entityType(auditLog.getEntityType())
                .entityId(auditLog.getEntityId())
                .entityName(auditLog.getEntityName())
                .ipAddress(auditLog.getIpAddress())
                .userAgent(auditLog.getUserAgent())
                .changesSummary(auditLog.getChangesSummary())
                .status(auditLog.getStatus())
                .executionTimeMs(auditLog.getExecutionTimeMs())
                .build();
    }
}