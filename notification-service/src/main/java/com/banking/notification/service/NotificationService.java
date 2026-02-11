package com.banking.notification.service;

import com.banking.notification.entity.Notification;
import com.banking.notification.repository.NotificationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class NotificationService {

    private final NotificationRepository notificationRepository;

    @Transactional
    public Notification create(String accountNumber, String type, String message) {
        Notification n = Notification.builder()
                .accountNumber(accountNumber)
                .type(type)
                .message(message)
                .build();
        return notificationRepository.save(n);
    }

    @Transactional(readOnly = true)
    public List<Notification> getByAccount(String accountNumber) {
        return notificationRepository.findByAccountNumberOrderByCreatedAtDesc(accountNumber);
    }
}
