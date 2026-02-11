package com.banking.transaction.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "app.internal-api")
public class InternalApiProperties {

    private String key = "";

    public String getKey() { return key; }
    public void setKey(String key) { this.key = key; }
}
