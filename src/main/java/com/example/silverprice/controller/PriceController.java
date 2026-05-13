package com.example.silverprice.controller;

import com.example.silverprice.model.SilverPrice;
import com.example.silverprice.service.NotificationRouter;
import com.example.silverprice.service.SilverPriceService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/price")
@RequiredArgsConstructor
public class PriceController {

    private final SilverPriceService silverPriceService;
    private final NotificationRouter notificationRouter;

    @PostMapping("/trigger")
    public ResponseEntity<SilverPrice> trigger() {
        SilverPrice price = silverPriceService.fetchSilverPrice();
        notificationRouter.send(price.toMessage());
        return ResponseEntity.ok(price);
    }
}
