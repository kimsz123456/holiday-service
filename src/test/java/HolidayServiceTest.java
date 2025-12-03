import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class HolidayServiceTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private NagerApiClient apiClient;

    @Autowired
    private HolidayRepository holidayRepository;

    @Autowired
    private CountryRepository countryRepository;

    @BeforeEach
    void setUp() {
        // Country DTO Mock
        CountryDto korea = new CountryDto("KR", "South Korea");
        CountryDto usa = new CountryDto("US", "United States");
        when(apiClient.getCountries()).thenReturn(List.of(korea, usa));

        // Holiday DTO Mock
        for (int year = 2021; year <= 2025; year++) {
            HolidayDto krHoliday = new HolidayDto();
            krHoliday.setDate(LocalDate.of(year, 1, 1));
            krHoliday.setLocalName("신정");
            krHoliday.setName("New Year");
            krHoliday.setCountryCode("KR");

            HolidayDto usHoliday = new HolidayDto();
            usHoliday.setDate(LocalDate.of(year, 7, 4));
            usHoliday.setLocalName("Independence Day");
            usHoliday.setName("Independence Day");
            usHoliday.setCountryCode("US");

            when(apiClient.getHolidays(year, "KR")).thenReturn(List.of(krHoliday));
            when(apiClient.getHolidays(year, "US")).thenReturn(List.of(usHoliday));
        }
    }

    @Test
    void init_DB저장() throws Exception {
        // When
        mockMvc.perform(post("/api/holidays/init"))
            .andExpect(status().isOk());

        // Then
        assertThat(countryRepository.count()).isEqualTo(2);
        assertThat(holidayRepository.count()).isEqualTo(10); // 5년 * 2개국 * 1개씩

        // 특정 데이터 검증
        Country savedKorea = countryRepository.findById("KR").orElseThrow();
        assertThat(savedKorea.getName()).isEqualTo("South Korea");

        List<Holiday> kr2025 = holidayRepository.findByYearAndCountryCode("KR", 2025);
        assertThat(kr2025).hasSize(1);
        assertThat(kr2025.get(0).getLocalName()).isEqualTo("신정");
    }

    @Test
    void search_저장된_데이터_검색() throws Exception {
        // Given
        mockMvc.perform(post("/api/holidays/init"));

        // When & Then
        mockMvc.perform(get("/api/holidays/search")
                .param("year", "2025")
                .param("countryCode", "KR"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.content[0].localName").value("신정"));
    }

    @Test
    void refresh_데이터_재동기화() throws Exception {
        // Given
        mockMvc.perform(post("/api/holidays/init"));

        // 새로운 데이터: 기존 1개 갱신, 1개 추가
        HolidayDto updatedHoliday = new HolidayDto();
        updatedHoliday.setDate(LocalDate.of(2025, 1, 1)); // 기존 신정 업데이트
        updatedHoliday.setLocalName("신정(갱신)");
        updatedHoliday.setName("New Year Updated");
        updatedHoliday.setCountryCode("KR");

        HolidayDto newHoliday = new HolidayDto();
        newHoliday.setDate(LocalDate.of(2025, 3, 1)); // 새로운 삼일절
        newHoliday.setLocalName("삼일절");
        newHoliday.setName("Independence Movement Day");
        newHoliday.setCountryCode("KR");

        when(apiClient.getHolidays(2025, "KR")).thenReturn(List.of(updatedHoliday, newHoliday));

        mockMvc.perform(put("/api/holidays/refresh")
                .param("year", "2025")
                .param("countryCode", "KR"))
            .andExpect(status().isOk());

        List<Holiday> kr2025 = holidayRepository.findByYearAndCountryCode("KR", 2025);
        assertThat(kr2025).hasSize(3);
        assertThat(kr2025).extracting("localName")
            .containsExactlyInAnyOrder("신정(갱신)", "설날", "삼일절");
    }

    @Test
    void delete_데이터_삭제() throws Exception {
        // Given
        mockMvc.perform(post("/api/holidays/init"));

        // When
        mockMvc.perform(delete("/api/holidays")
                .param("year", "2025")
                .param("countryCode", "KR"))
            .andExpect(status().isOk());

        // Then
        List<Holiday> deleted = holidayRepository.findByYearAndCountryCode("KR", 2025);
        assertThat(deleted).isEmpty();
    }
}