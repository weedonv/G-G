package com.vlad.app.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Data;

import java.time.LocalDateTime;

@Entity
@Table(name = "audit_logs")
@Data
public class AuditLog {
    @Id
    private String requestId;
    private String requestUri;
    private LocalDateTime requestTimestamp;
    private int httpResponseCode;
    private String ipAddress;
    private String countryCode;
    private String ipProvider;
    private long timeLapsed;
}
