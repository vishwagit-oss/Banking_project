package com.banking.notification.entity;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;

@Document(collection = "notifications")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Notification {

    @Id
    private String id;

    private String accountNumber;

    private String type;

    private String message;

    @Builder.Default
    private boolean readFlag = false;

    private Instant createdAt;
}
