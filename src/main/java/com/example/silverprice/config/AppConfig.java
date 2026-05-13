package com.example.silverprice.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

import java.time.ZoneId;

@Configuration
public class AppConfig {

    @Value("${scheduler.timezone:UTC}")
    private String timezone;

    @Bean
    public ZoneId appZone() {
        try {
            return ZoneId.of(timezone);
        } catch (Exception e) {
            return ZoneId.of("UTC");
        }
    }

    @Bean
    public RestTemplate restTemplate() {
        return new RestTemplate();
    }

    @Bean
    public ObjectMapper objectMapper() {
        return new ObjectMapper().registerModule(new JavaTimeModule());
    }
}
