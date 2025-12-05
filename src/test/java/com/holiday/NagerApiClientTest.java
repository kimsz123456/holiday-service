package com.holiday;

import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import com.holiday.client.NagerApiClient;
import com.holiday.dto.CountryDto;
import com.holiday.dto.HolidayDto;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class NagerApiClientTest {

    private MockWebServer mockWebServer;
    private NagerApiClient apiClient;

    @BeforeEach
    void setUp() throws Exception {
        mockWebServer = new MockWebServer();
        mockWebServer.start();

        String baseUrl = mockWebServer.url("/").toString();

        apiClient = new NagerApiClient(new RestTemplate(), baseUrl);
    }

    @AfterEach
    void tearDown() throws Exception {
        mockWebServer.shutdown();
    }

    @Test
    void Country_JSON을_CountryDto로_정상_매핑() throws Exception {
        String jsonResponse = """
            [
                {"countryCode": "KR", "name": "South Korea"},
                {"countryCode": "US", "name": "United States"}
            ]
            """;

        mockWebServer.enqueue(new MockResponse()
            .setBody(jsonResponse)
            .addHeader("Content-Type", "application/json"));

        List<CountryDto> result = apiClient.getCountries();

        assertThat(result).hasSize(2);
        assertThat(result.get(0).getCountryCode()).isEqualTo("KR");
        assertThat(result.get(0).getName()).isEqualTo("South Korea");
    }

    @Test
    void Holiday_JSON을_HolidayDto로_정상_매핑() throws Exception {
        String jsonResponse = """
            [{
                "date": "2025-01-01",
                "localName": "새해",
                "name": "New Year's Day",
                "countryCode": "KR",
                "fixed": false,
                "global": true,
                "counties": null,
                "launchYear": null,
                "types": ["Public"]
            }]
            """;

        mockWebServer.enqueue(new MockResponse()
            .setBody(jsonResponse)
            .addHeader("Content-Type", "application/json"));

        List<HolidayDto> result = apiClient.getHolidays(2025, "KR");

        assertThat(result).hasSize(1);
        HolidayDto dto = result.get(0);
        assertThat(dto.getDate()).isEqualTo(LocalDate.of(2025, 1, 1));
        assertThat(dto.getLocalName()).isEqualTo("새해");
        assertThat(dto.getName()).isEqualTo("New Year's Day");
        assertThat(dto.getCountryCode()).isEqualTo("KR");
    }
}
