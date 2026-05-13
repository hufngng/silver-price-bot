package com.example.silverprice.service.notification;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class NotificationRouter {

    private final List<NotificationService> services;

    public void send(String message) {
        if (services.isEmpty()) {
            log.warn("No notification channel enabled");
            return;
        }
        services.forEach(s -> {
            try {
                s.send(message);
            } catch (Exception e) {
                log.error("{} failed: {}", s.getClass().getSimpleName(), e.getMessage());
            }
        });
    }
}
