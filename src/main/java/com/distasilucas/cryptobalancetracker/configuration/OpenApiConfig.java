package com.distasilucas.cryptobalancetracker.configuration;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI openAPI() {
        var info = new Info()
            .title("Crypto Balance Tracker")
            .version("v1.0.0")
            .description("REST API to add cryptocurrencies with their respective quantity and platform to retrieve wide information about balances distribution");

        return new OpenAPI()
            .info(info);
    }
}
