package com.banking.notification.repository;

import com.banking.notification.entity.Notification;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

public interface NotificationRepository extends MongoRepository<Notification, String> {

    List<Notification> findByAccountNumberOrderByCreatedAtDesc(String accountNumber);
}
