package com.holiday.client;

import java.util.List;
import com.holiday.dto.CountryDto;
import com.holiday.dto.HolidayDto;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

public class NagerApiClient {

    private final RestTemplate restTemplate;
    private final String baseUrl;

    public NagerApiClient(RestTemplate restTemplate, String baseUrl) {
        this.restTemplate = restTemplate;
        this.baseUrl = baseUrl;
    }

    public List<CountryDto> getCountries() {
        String url = baseUrl + "/AvailableCountries";

        ResponseEntity<List<CountryDto>> response = restTemplate.exchange(
            url,
            HttpMethod.GET,
            null,
            new ParameterizedTypeReference<List<CountryDto>>() {
            }
        );

        return response.getBody();
    }

    public List<HolidayDto> getHolidays(int year, String countryCode) {
        String url = baseUrl + "/PublicHolidays/" + year + "/" + countryCode;

        ResponseEntity<List<HolidayDto>> response = restTemplate.exchange(
            url,
            HttpMethod.GET,
            null,
            new ParameterizedTypeReference<List<HolidayDto>>() {
            }
        );

        return response.getBody();
    }
}
