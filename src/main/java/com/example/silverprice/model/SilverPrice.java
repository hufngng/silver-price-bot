package com.example.silverprice.model;

import lombok.Data;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Data
public class SilverPrice {
    private double priceUsd;
    private double changePercent;
    private LocalDateTime fetchedAt;

    public String toMessage() {
        String sign = changePercent >= 0 ? "+" : "";
        return String.format(
            "🥈 *Silver Price - USD/oz*\n" +
            "💰 Price: `$%.2f`\n" +
            "📈 Change: `%s%.2f%%`\n" +
            "🕐 Updated: `%s`\n" +
            "🔗 Source: goldprice.org",
            priceUsd,
            sign, changePercent,
            fetchedAt.format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss"))
        );
    }
}
