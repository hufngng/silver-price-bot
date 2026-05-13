package com.example.silverprice.service.notification;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import com.example.silverprice.util.MessageUtil;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
@ConditionalOnProperty(name = "notification.telegram.enabled", havingValue = "true", matchIfMissing = true)
public class TelegramService implements NotificationService {

    private static final String TELEGRAM_API = "https://api.telegram.org/bot%s/sendMessage";
    private static final int MAX_LENGTH = 4096;

    @Value("${telegram.bot.token}")
    private String botToken;

    @Value("${telegram.chat.id}")
    private String chatId;

    private final RestTemplate restTemplate;

    @Override
    public void send(String message) {
        List<String> chunks = MessageUtil.split(message, MAX_LENGTH);
        for (int i = 0; i < chunks.size(); i++) {
            sendChunk(chunks.get(i));
            if (chunks.size() > 1) {
                log.info("Telegram: sent part {}/{}", i + 1, chunks.size());
            }
        }
    }

    private void sendChunk(String text) {
        String url = String.format(TELEGRAM_API, botToken);

        Map<String, Object> body = new HashMap<>();
        body.put("chat_id", chatId);
        body.put("text", text);
        body.put("parse_mode", "Markdown");

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        try {
            ResponseEntity<String> response = restTemplate.postForEntity(
                url, new HttpEntity<>(body, headers), String.class
            );
            if (response.getStatusCode().is2xxSuccessful()) {
                log.info("Telegram: message sent");
            } else {
                log.error("Telegram: error {}", response.getStatusCode());
            }
        } catch (Exception e) {
            log.error("Telegram: exception {}", e.getMessage());
        }
    }
}
