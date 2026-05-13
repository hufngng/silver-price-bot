package com.example.silverprice.scheduler;

import com.example.silverprice.model.SilverPrice;
import com.example.silverprice.service.NotificationRouter;
import com.example.silverprice.service.SilverPriceService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class PriceScheduler {

    private final SilverPriceService silverPriceService;
    private final NotificationRouter notificationRouter;

    @Scheduled(fixedRate = 3_600_000)
    public void reportSilverPrice() {
        log.info("Fetching silver price...");
        try {
            SilverPrice price = silverPriceService.fetchSilverPrice();
            notificationRouter.send(price.toMessage());
            log.info("Silver price: ${} USD/oz", price.getPriceUsd());
        } catch (Exception e) {
            log.error("Scheduler error: {}", e.getMessage());
            notificationRouter.send("❌ Failed to fetch silver price: " + e.getMessage());
        }
    }
}
