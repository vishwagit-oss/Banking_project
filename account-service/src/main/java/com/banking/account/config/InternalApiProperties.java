package com.banking.account.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "app.internal-api")
public class InternalApiProperties {

    /** Shared secret for service-to-service calls. Must match transaction-service and notification-service. */
    private String key = "";

    public String getKey() { return key; }
    public void setKey(String key) { this.key = key; }
}
