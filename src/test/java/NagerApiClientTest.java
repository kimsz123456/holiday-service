import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.client.RestClientTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;

import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;
import static org.hamcrest.Matchers.containsString;

@RestClientTest(NagerApiClient.class)
class NagerApiClientTest {

  @Autowired
  private NagerApiClient apiClient;

  @Autowired
  private MockRestServiceServer mockServer;

  @Test
  void Country_JSON을_CountryDto로_정상_매핑() {
    // Given
    String jsonResponse = """
        [
            {"countryCode": "KR", "name": "South Korea"},
            {"countryCode": "US", "name": "United States"}
        ]
        """;

    mockServer.expect(requestTo(containsString("/AvailableCountries")))
        .andRespond(withSuccess(jsonResponse, MediaType.APPLICATION_JSON));

    // When
    List<CountryDto> result = apiClient.getCountries();

    // Then
    assertThat(result).hasSize(2);
    assertThat(result.get(0).getCountryCode()).isEqualTo("KR");
    assertThat(result.get(0).getName()).isEqualTo("South Korea");
  }

  @Test
  void Holiday_JSON을_HolidayDto로_정상_매핑() {
    // Given
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

    mockServer.expect(requestTo(containsString("/PublicHolidays/2025/KR")))
        .andRespond(withSuccess(jsonResponse, MediaType.APPLICATION_JSON));

    // When
    List<HolidayDto> result = apiClient.getHolidays(2025, "KR");

    // Then
    assertThat(result).hasSize(1);
    HolidayDto dto = result.get(0);
    assertThat(dto.getDate()).isEqualTo(LocalDate.of(2025, 1, 1));
    assertThat(dto.getLocalName()).isEqualTo("새해");
    assertThat(dto.getName()).isEqualTo("New Year's Day");
    assertThat(dto.getCountryCode()).isEqualTo("KR");
  }
}