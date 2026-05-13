package com.example.silverprice.service.price;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;

@Slf4j
@Service
@RequiredArgsConstructor
public class MuaVangBacPriceProvider implements PriceProvider {

    private static final String URL = "https://muavangbac.vn/";

    private final RestTemplate restTemplate;
    private final ZoneId appZone;

    @Override
    public String name() { return "muavangbac"; }

    @Override
    public String fetch() throws Exception {
        HttpHeaders headers = new HttpHeaders();
        headers.set("User-Agent", "Mozilla/5.0 (X11; Linux x86_64) AppleWebKit/537.36");
        headers.set("Referer", URL);

        ResponseEntity<String> response = restTemplate.exchange(
            URL, HttpMethod.GET, new HttpEntity<>(headers), String.class
        );
        Document doc = Jsoup.parse(response.getBody());

        // XAG/USD: second span.crypto-price (first is gold)
        Elements cryptoPrices = doc.select("span.crypto-price");
        String xagUsd = cryptoPrices.get(1).text().trim();

        // Phu Quy silver: each span.silver-icon-pq is inside h2,
        // the next sibling div holds buy/sell prices
        Elements pqSections = doc.select("span.silver-icon-pq");
        Element pieceDiv = pqSections.get(0).closest("h2").nextElementSibling();
        Element barDiv   = pqSections.get(1).closest("h2").nextElementSibling();

        String[] piece = extractBuySell(pieceDiv);
        String[] bar   = extractBuySell(barDiv);

        String now = ZonedDateTime.now(appZone).format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss"));

        return String.format(
            "🥈 *Silver Price - muavangbac.vn*\n\n" +
            "🌐 *XAG/USD:* `$%s`\n\n" +
            "🏷 *Phu Quy Silver Piece* _(VND/tael)_\n" +
            "  Buy:  `%s`\n" +
            "  Sell: `%s`\n\n" +
            "🏷 *Phu Quy Silver Bar 1kg* _(VND/kg)_\n" +
            "  Buy:  `%s`\n" +
            "  Sell: `%s`\n\n" +
            "🕐 `%s`\n" +
            "🔗 muavangbac.vn",
            xagUsd, piece[0], piece[1], bar[0], bar[1], now
        );
    }

    private String[] extractBuySell(Element section) {
        Elements prices = section.select("span.gold-price.d-none.d-lg-block");
        return new String[]{
            cleanPrice(prices.get(0).text()),
            cleanPrice(prices.get(1).text())
        };
    }

    private String cleanPrice(String raw) {
        String s = raw.trim();
        int idx = s.indexOf("đ");
        if (idx > 0) s = s.substring(0, idx).trim();
        return s.replaceAll("[^\\d,.]", "");
    }
}
