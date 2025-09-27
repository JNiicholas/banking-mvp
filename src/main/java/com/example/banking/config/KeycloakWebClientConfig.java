package com.example.banking.config;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.web.reactive.function.client.ExchangeStrategies;
import org.springframework.web.reactive.function.client.WebClient;


import reactor.netty.http.client.HttpClient;
import io.netty.channel.ChannelOption;
import java.time.Duration;

@Configuration
@RequiredArgsConstructor
public class KeycloakWebClientConfig {

    @Value("${keycloak.base-url}")
    private String baseUrl;

    @Value("${keycloak.timeouts.connect-ms:3000}")
    private long connectMs;

    @Value("${keycloak.timeouts.read-ms:5000}")
    private long readMs;

    @Bean("keycloakWebClient")
    public WebClient keycloakWebClient() {
        HttpClient httpClient = HttpClient.create()
                // connection timeout in milliseconds
                .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, (int) connectMs)
                // overall response timeout
                .responseTimeout(Duration.ofMillis(readMs))
                // enable gzip/deflate
                .compress(true);

        return WebClient.builder()
                .baseUrl(baseUrl)
                .clientConnector(new ReactorClientHttpConnector(httpClient))
                .exchangeStrategies(ExchangeStrategies.builder()
                        .codecs(conf -> conf.defaultCodecs().maxInMemorySize(1_048_576))
                        .build())
                .build();
    }
}