package com.holiday.config;

import com.holiday.client.NagerApiClient;
import com.holiday.exception.RestTemplateErrorHandler;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.client.RestTemplate;

@Configuration
public class RestTemplateConfig {

    @Value("${rest.template.connect-timeout:3000}")
    private int connectTimeout;

    @Value("${rest.template.read-timeout:3000}")
    private int readTimeout;

    @Value("${nager.api.base-url}")
    private String nagerApiBaseUrl;

    @Bean
    public RestTemplate restTemplate() {
        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(connectTimeout);
        factory.setReadTimeout(readTimeout);

        return new RestTemplateBuilder()
            .requestFactory(() -> factory)
            .errorHandler(new RestTemplateErrorHandler())
            .build();
    }

    @Bean
    public NagerApiClient nagerApiClient(RestTemplate restTemplate) {
        return new NagerApiClient(restTemplate, nagerApiBaseUrl);
    }
}
