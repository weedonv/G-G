package com.vlad.app.repository;

import com.vlad.app.entity.AuditLog;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AuditLogRepository extends JpaRepository<AuditLog, String> {
}
