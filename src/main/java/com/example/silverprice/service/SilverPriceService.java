package com.example.silverprice.service;

import com.example.silverprice.model.SilverPrice;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDateTime;

@Slf4j
@Service
@RequiredArgsConstructor
public class SilverPriceService {

    private static final String API_URL = "https://data-asg.goldprice.org/dbXRates/USD";

    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;

    public SilverPrice fetchSilverPrice() {
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.set("User-Agent", "Mozilla/5.0 (X11; Linux x86_64) AppleWebKit/537.36");
            headers.set("Referer", "https://goldprice.org/");

            ResponseEntity<String> response = restTemplate.exchange(
                API_URL, HttpMethod.GET, new HttpEntity<>(headers), String.class
            );
            return parseResponse(response.getBody());
        } catch (Exception e) {
            log.error("Failed to fetch silver price: {}", e.getMessage());
            throw new RuntimeException("Could not fetch silver price from goldprice.org", e);
        }
    }

    // Response: {"items":[{"xagPrice":86.34,"pcXag":1.61,...}]}
    private SilverPrice parseResponse(String body) throws Exception {
        log.debug("Raw response: {}", body);

        SilverPrice price = new SilverPrice();
        price.setFetchedAt(LocalDateTime.now());

        JsonNode item = objectMapper.readTree(body).path("items").get(0);
        price.setPriceUsd(item.path("xagPrice").asDouble());
        price.setChangePercent(item.path("pcXag").asDouble());

        return price;
    }
}
