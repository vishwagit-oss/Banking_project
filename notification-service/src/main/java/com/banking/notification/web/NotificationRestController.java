package com.banking.notification.web;

import com.banking.notification.entity.Notification;
import com.banking.notification.service.NotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/notifications")
@RequiredArgsConstructor
public class NotificationRestController {

    private final NotificationService notificationService;

    @PostMapping
    public ResponseEntity<Notification> create(@RequestBody Map<String, String> body) {
        Notification n = notificationService.create(
                body.get("accountNumber"),
                body.getOrDefault("type", "INFO"),
                body.getOrDefault("message", ""));
        return ResponseEntity.status(HttpStatus.CREATED).body(n);
    }

    @GetMapping("/account/{accountNumber}")
    public List<Notification> getByAccount(@PathVariable String accountNumber) {
        return notificationService.getByAccount(accountNumber);
    }
}
