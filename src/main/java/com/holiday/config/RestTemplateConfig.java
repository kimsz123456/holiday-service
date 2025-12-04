package com.holiday.config;

import java.net.http.HttpClient;
import com.holiday.exception.RestTemplateErrorHandler;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.web.client.RestTemplate;

import java.time.Duration;

@Configuration
public class RestTemplateConfig {

    @Value("${rest.connect-timeout:3000}")
    private int connectTimeout;

    @Value("${rest.read-timeout:3000}")
    private int readTimeout;

    @Bean
    public RestTemplate restTemplate() {

        HttpClient httpClient = HttpClient.newBuilder()
            .connectTimeout(Duration.ofMillis(connectTimeout))
            .build();

        HttpComponentsClientHttpRequestFactory factory =
            new HttpComponentsClientHttpRequestFactory();

        factory.setConnectTimeout(connectTimeout);
        factory.setReadTimeout(readTimeout);

        return new RestTemplateBuilder()
            .requestFactory(() -> factory)
            .errorHandler(new RestTemplateErrorHandler())
            .build();
    }
}
