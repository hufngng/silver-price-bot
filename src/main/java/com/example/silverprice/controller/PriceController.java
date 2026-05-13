package com.example.silverprice.controller;

import com.example.silverprice.service.price.PriceProvider;
import com.example.silverprice.service.notification.NotificationRouter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/price")
@RequiredArgsConstructor
public class PriceController {

    private final List<PriceProvider> fetchers;
    private final NotificationRouter notificationRouter;
    private final ZoneId appZone;

    @PostMapping("/trigger")
    public ResponseEntity<Map<String, String>> trigger() {
        long start = System.currentTimeMillis();
        log.info("POST /api/price/trigger called at {}", now());

        Map<String, String> results = new LinkedHashMap<>();
        for (PriceProvider fetcher : fetchers) {
            try {
                String msg = fetcher.fetch();
                notificationRouter.send(msg);
                results.put(fetcher.name(), "ok");
            } catch (Exception e) {
                results.put(fetcher.name(), "error: " + e.getMessage());
            }
        }

        log.info("Trigger done (elapsed: {}ms)", System.currentTimeMillis() - start);
        return ResponseEntity.ok(results);
    }

    private String now() {
        return ZonedDateTime.now(appZone).format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
    }
}
