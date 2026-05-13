package com.example.silverprice.scheduler;

import com.example.silverprice.service.price.PriceProvider;
import com.example.silverprice.service.notification.NotificationRouter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class PriceScheduler {

    private final List<PriceProvider> fetchers;
    private final NotificationRouter notificationRouter;
    private final ZoneId appZone;

    @Scheduled(cron = "0 0 * * * *", zone = "${scheduler.timezone}")
    public void reportSilverPrice() {
        long start = System.currentTimeMillis();
        log.info("[{}] Job started", now());
        for (PriceProvider fetcher : fetchers) {
            try {
                String msg = fetcher.fetch();
                notificationRouter.send(msg);
                log.info("[{}] {} done", now(), fetcher.name());
            } catch (Exception e) {
                String errMsg = "❌ Failed to fetch " + fetcher.name() + ": " + e.getMessage();
                log.error(errMsg);
                notificationRouter.send(errMsg);
            }
        }
        log.info("[{}] Job done (elapsed: {}ms)", now(), System.currentTimeMillis() - start);
    }

    private String now() {
        return ZonedDateTime.now(appZone).format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
    }
}
