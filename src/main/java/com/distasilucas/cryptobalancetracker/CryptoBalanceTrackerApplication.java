package com.distasilucas.cryptobalancetracker;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@EnableScheduling
@SpringBootApplication
public class CryptoBalanceTrackerApplication {

	public static void main(String[] args) {
		SpringApplication.run(CryptoBalanceTrackerApplication.class, args);
	}

}
