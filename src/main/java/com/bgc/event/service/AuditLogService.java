package com.bgc.event.service;

import com.bgc.event.entity.AuditLog;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface AuditLogService {
    void log(String username, String action, String entityName, String entityId, String details, String ip);
    Page<AuditLog> findAll(Pageable pageable);
    Page<AuditLog> search(String username, Pageable pageable);
}
