package com.holiday.client;

import java.util.List;
import com.holiday.dto.CountryDto;
import com.holiday.dto.HolidayDto;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

@Slf4j
public class NagerApiClient {

    private final RestTemplate restTemplate;
    private final String baseUrl;

    public NagerApiClient(RestTemplate restTemplate, String baseUrl) {
        this.restTemplate = restTemplate;
        this.baseUrl = baseUrl;
    }

    public List<CountryDto> getCountries() {
        String url = baseUrl + "/AvailableCountries";
        log.debug("외부 API 호출 - 국가 목록 조회: {}", url);

        ResponseEntity<List<CountryDto>> response = restTemplate.exchange(
            url,
            HttpMethod.GET,
            null,
            new ParameterizedTypeReference<List<CountryDto>>() {
            }
        );

        List<CountryDto> countries = response.getBody();
        log.debug("국가 목록 조회 완료 - {}개 국가", countries != null ? countries.size() : 0);
        return countries;
    }

    public List<HolidayDto> getHolidays(int year, String countryCode) {
        String url = baseUrl + "/PublicHolidays/" + year + "/" + countryCode;
        log.debug("외부 API 호출 - 공휴일 조회: year={}, countryCode={}, url={}", year, countryCode, url);

        ResponseEntity<List<HolidayDto>> response = restTemplate.exchange(
            url,
            HttpMethod.GET,
            null,
            new ParameterizedTypeReference<List<HolidayDto>>() {
            }
        );

        List<HolidayDto> holidays = response.getBody();
        log.debug("공휴일 조회 완료 - year={}, countryCode={}, count={}",
            year, countryCode, holidays != null ? holidays.size() : 0);
        return holidays;
    }
}
