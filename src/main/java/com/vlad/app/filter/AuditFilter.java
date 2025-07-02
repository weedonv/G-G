package com.vlad.app.filter;

import com.vlad.app.entity.AuditLog;
import com.vlad.app.repository.AuditLogRepository;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class AuditFilter extends OncePerRequestFilter {
    public static final String ISP = "isp";
    public static final String COUNTRY_CODE = "countryCode";

    private final AuditLogRepository auditLogRepository;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        long startTime = System.currentTimeMillis();
        String requestId = UUID.randomUUID().toString();
        String requestUri = request.getRequestURI();
        LocalDateTime requestTimestamp = LocalDateTime.now();
        String ipAddress = request.getRemoteAddr();

        filterChain.doFilter(request, response);

        String isp = getOrDefaultAttribute(request,ISP);
        String countryCode = getOrDefaultAttribute(request,COUNTRY_CODE);
        int httpResponseCode = response.getStatus();
        long timeLapsed = System.currentTimeMillis() - startTime;

        if (!requestUri.startsWith("/h2")) { // filter h2 db calls when using the console
            AuditLog auditLog = new AuditLog();
            auditLog.setRequestId(requestId);
            auditLog.setRequestUri(requestUri);
            auditLog.setRequestTimestamp(requestTimestamp);
            auditLog.setHttpResponseCode(httpResponseCode);
            auditLog.setIpAddress(ipAddress);
            auditLog.setCountryCode(countryCode);
            auditLog.setIpProvider(isp);
            auditLog.setTimeLapsed(timeLapsed);

            auditLogRepository.save(auditLog);
        }
    }

    private String getOrDefaultAttribute(HttpServletRequest request, String attributeName) {
        Object attribute = request.getAttribute(attributeName);
        if (attribute != null) {
            return attribute.toString();
        }
        return "unknown";
    }

}
