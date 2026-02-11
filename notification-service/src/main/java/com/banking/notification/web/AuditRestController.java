package com.banking.notification.web;

import com.banking.notification.entity.AuditLog;
import com.banking.notification.service.AuditService;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/audit")
@RequiredArgsConstructor
public class AuditRestController {

    private final AuditService auditService;

    @PostMapping
    public ResponseEntity<AuditLog> record(@RequestBody Map<String, String> body) {
        AuditLog log = auditService.record(
                body.getOrDefault("eventType", "UNKNOWN"),
                body.getOrDefault("entityType", ""),
                body.get("entityId"),
                body.get("accountNumber"),
                body.get("details"));
        return ResponseEntity.status(HttpStatus.CREATED).body(log);
    }

    @GetMapping("/account/{accountNumber}")
    public List<AuditLog> getByAccount(@PathVariable String accountNumber) {
        return auditService.getByAccount(accountNumber);
    }

    @GetMapping("/account/{accountNumber}/range")
    public List<AuditLog> getByAccountAndRange(
            @PathVariable String accountNumber,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant from,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant to) {
        return auditService.getByAccountAndDateRange(accountNumber, from, to);
    }
}
