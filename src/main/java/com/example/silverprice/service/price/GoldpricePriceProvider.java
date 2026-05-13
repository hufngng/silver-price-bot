package com.example.silverprice.service.price;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;

@Slf4j
@Service
@RequiredArgsConstructor
public class GoldpricePriceProvider implements PriceProvider {

    private static final String API_URL = "https://data-asg.goldprice.org/dbXRates/USD";

    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;
    private final ZoneId appZone;

    @Override
    public String name() { return "goldprice"; }

    @Override
    public String fetch() throws Exception {
        HttpHeaders headers = new HttpHeaders();
        headers.set("User-Agent", "Mozilla/5.0 (X11; Linux x86_64) AppleWebKit/537.36");
        headers.set("Referer", "https://goldprice.org/");

        ResponseEntity<String> response = restTemplate.exchange(
            API_URL, HttpMethod.GET, new HttpEntity<>(headers), String.class
        );

        // Response: {"items":[{"xagPrice":86.34,"pcXag":1.61,...}]}
        JsonNode item = objectMapper.readTree(response.getBody()).path("items").get(0);
        double priceUsd = item.path("xagPrice").asDouble();
        double changePercent = item.path("pcXag").asDouble();

        String sign = changePercent >= 0 ? "+" : "";
        String now = ZonedDateTime.now(appZone).format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss"));

        return String.format(
            "🥈 *Silver Price - USD/oz*\n" +
            "💰 Price: `$%.2f`\n" +
            "📈 Change: `%s%.2f%%`\n" +
            "🕐 Updated: `%s`\n" +
            "🔗 Source: goldprice.org",
            priceUsd, sign, changePercent, now
        );
    }
}
