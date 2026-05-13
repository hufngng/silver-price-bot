package com.example.silverprice;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class SilverPriceApplication {
    public static void main(String[] args) {
        SpringApplication.run(SilverPriceApplication.class, args);
    }
}
