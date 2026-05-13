package com.example.silverprice.service.price;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;

@Slf4j
@Service
@RequiredArgsConstructor
public class GiaBacPriceProvider implements PriceProvider {

    private static final String BASE_URL   = "https://giabac.vn/";
    private static final String FILTER_URL = "https://giabac.vn/SilverInfo/FilterData";

    // filterType values matching the 3 unit tabs on the page
    private static final String TAB_CHI    = "#pills-home";
    private static final String TAB_LUONG  = "#pills-profile";
    private static final String TAB_KG     = "#pills-contact";

    private final RestTemplate restTemplate;
    private final ZoneId appZone;

    @Override
    public String name() { return "giabac"; }

    @Override
    public String fetch() throws Exception {
        String[] chi   = fetchUnit(TAB_CHI);
        String[] luong = fetchUnit(TAB_LUONG);
        String[] kg    = fetchUnit(TAB_KG);
        String updatedAt = fetchUpdatedAt();

        return String.format(
            "🥈 *Silver 999 - giabac.vn*\n\n" +
            "🏷 *Per Chi* _(VND/chi)_\n" +
            "  Buy:  `%s`\n" +
            "  Sell: `%s`\n\n" +
            "🏷 *Per Luong* _(VND/luong)_\n" +
            "  Buy:  `%s`\n" +
            "  Sell: `%s`\n\n" +
            "🏷 *Per Kilogram* _(VND/kg)_\n" +
            "  Buy:  `%s`\n" +
            "  Sell: `%s`\n\n" +
            "🕐 Updated: `%s`\n" +
            "🔗 giabac.vn",
            chi[0], chi[1], luong[0], luong[1], kg[0], kg[1], updatedAt
        );
    }

    private String[] fetchUnit(String filterType) throws Exception {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
        headers.set("User-Agent", "Mozilla/5.0 (X11; Linux x86_64) AppleWebKit/537.36");
        headers.set("Referer", BASE_URL);
        headers.set("X-Requested-With", "XMLHttpRequest");

        MultiValueMap<String, String> body = new LinkedMultiValueMap<>();
        body.add("filterType", filterType);

        ResponseEntity<String> response = restTemplate.postForEntity(
            FILTER_URL, new HttpEntity<>(body, headers), String.class
        );
        Document doc = Jsoup.parse(response.getBody());

        // First p.text-24px = buy (red), second = sell (green)
        Elements prices = doc.select("p.text-24px");
        if (prices.size() < 2) throw new Exception("Price elements not found for filterType=" + filterType);

        return new String[]{ prices.get(0).text().trim(), prices.get(1).text().trim() };
    }

    private String fetchUpdatedAt() {
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.set("User-Agent", "Mozilla/5.0 (X11; Linux x86_64) AppleWebKit/537.36");
            headers.set("Referer", BASE_URL);

            ResponseEntity<String> response = restTemplate.exchange(
                BASE_URL, HttpMethod.GET, new HttpEntity<>(headers), String.class
            );
            Document doc = Jsoup.parse(response.getBody());

            // e.g. "Bảng giá bạc Phú Quý  cập nhật 17:57 13/05/2026"
            String text = doc.select("#priceTable p.text-12px.fw-bold").text().trim();
            int idx = text.indexOf("cập nhật ");
            if (idx >= 0) return text.substring(idx + "cập nhật ".length()).trim();
        } catch (Exception e) {
            log.warn("Could not fetch updatedAt from giabac.vn: {}", e.getMessage());
        }
        return ZonedDateTime.now(appZone).format(DateTimeFormatter.ofPattern("HH:mm dd/MM/yyyy"));
    }
}
