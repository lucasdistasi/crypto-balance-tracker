package com.distasilucas.cryptobalancetracker;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.retry.annotation.EnableRetry;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;

@EnableCaching
@EnableRetry
@EnableScheduling
@EnableMethodSecurity
@SpringBootApplication
public class CryptoBalanceTrackerApplication {

    public static void main(String[] args) {
        SpringApplication.run(CryptoBalanceTrackerApplication.class, args);
    }

}
