package com.bgc.event.service.impl;

import com.bgc.event.entity.AuditLog;
import com.bgc.event.repository.AuditLogRepository;
import com.bgc.event.service.AuditLogService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class AuditLogServiceImpl implements AuditLogService {

    private final AuditLogRepository auditLogRepository;

    @Override
    @Async
    public void log(String username, String action, String entityName, String entityId, String details, String ip) {
        AuditLog log = AuditLog.builder()
            .username(username)
            .action(action)
            .entityName(entityName)
            .entityId(entityId)
            .details(details)
            .ipAddress(ip)
            .build();
        auditLogRepository.save(log);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<AuditLog> findAll(Pageable pageable) {
        return auditLogRepository.findAllByOrderByCreatedAtDesc(pageable);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<AuditLog> search(String username, Pageable pageable) {
        if (username == null || username.isBlank()) return findAll(pageable);
        return auditLogRepository.findByUsernameContainingIgnoreCaseOrderByCreatedAtDesc(username, pageable);
    }
}
