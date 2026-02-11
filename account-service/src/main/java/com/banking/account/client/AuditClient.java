package com.banking.account.client;

import com.banking.account.config.InternalApiProperties;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.Map;

/**
 * Sends audit events to notification-service. Failures are logged but do not block the main flow.
 */
@Component
public class AuditClient {

    private static final String INTERNAL_API_HEADER = "X-Internal-Api-Key";

    private final RestTemplate restTemplate;
    private final String notificationServiceUrl;
    private final String internalApiKey;

    public AuditClient(RestTemplate restTemplate,
                       InternalApiProperties internalApi,
                       @Value("${notification-service.url:http://localhost:8083}") String notificationServiceUrl) {
        this.restTemplate = restTemplate;
        this.notificationServiceUrl = notificationServiceUrl.replaceAll("/$", "");
        this.internalApiKey = internalApi.getKey();
    }

    /** Fire-and-forget: record event. Swallows errors so audit failure does not break operations. */
    public void record(String eventType, String entityType, String entityId, String accountNumber, String details) {
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.set(INTERNAL_API_HEADER, internalApiKey);
            Map<String, String> body = Map.of(
                    "eventType", eventType != null ? eventType : "UNKNOWN",
                    "entityType", entityType != null ? entityType : "",
                    "entityId", entityId != null ? entityId : "",
                    "accountNumber", accountNumber != null ? accountNumber : "",
                    "details", details != null ? details : ""
            );
            restTemplate.postForObject(
                    notificationServiceUrl + "/api/audit",
                    new HttpEntity<>(body, headers),
                    Object.class);
        } catch (Exception e) {
            // Do not fail the main operation; log if you have a logger
        }
    }
}
