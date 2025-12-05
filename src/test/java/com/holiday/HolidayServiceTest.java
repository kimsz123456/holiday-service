package com.holiday;

import com.holiday.client.NagerApiClient;
import com.holiday.dto.CountryDto;
import com.holiday.dto.HolidayDto;
import com.holiday.entity.Country;
import com.holiday.entity.Holiday;
import com.holiday.repository.CountryRepository;
import com.holiday.repository.HolidayRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
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

    @MockitoBean
    private NagerApiClient apiClient;

    @Autowired
    private HolidayRepository holidayRepository;

    @Autowired
    private CountryRepository countryRepository;

    @BeforeEach
    void setUp() {
        CountryDto korea = new CountryDto("KR", "South Korea");
        CountryDto usa = new CountryDto("US", "United States");
        when(apiClient.getCountries()).thenReturn(List.of(korea, usa));

        for (int year = 2021; year <= 2025; year++) {
            HolidayDto krHoliday = new HolidayDto();
            krHoliday.setDate(LocalDate.of(year, 1, 1));
            krHoliday.setLocalName("신정");
            krHoliday.setName("New Year");
            krHoliday.setCountryCode("KR");
            krHoliday.setFixed(true);
            krHoliday.setGlobal(true);
            krHoliday.setCounties(null);
            krHoliday.setTypes(List.of("Public"));

            HolidayDto usHoliday = new HolidayDto();
            usHoliday.setDate(LocalDate.of(year, 7, 4));
            usHoliday.setLocalName("Independence Day");
            usHoliday.setName("Independence Day");
            usHoliday.setCountryCode("US");
            usHoliday.setFixed(true);
            usHoliday.setGlobal(true);
            usHoliday.setCounties(null);
            usHoliday.setTypes(List.of("Public"));

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
        assertThat(holidayRepository.count()).isEqualTo(10);

        Country savedKorea = countryRepository.findById("KR").orElseThrow();
        assertThat(savedKorea.getName()).isEqualTo("South Korea");

        List<Holiday> kr2025 = holidayRepository.findByYearAndCountryCode(2025, "KR");
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
        mockMvc.perform(post("/api/holidays/init"));

        List<Holiday> before = holidayRepository.findByYearAndCountryCode(2025, "KR");
        assertThat(before).hasSize(1);
        assertThat(before.get(0).getLocalName()).isEqualTo("신정");

        HolidayDto updatedNewYear = new HolidayDto();
        updatedNewYear.setDate(LocalDate.of(2025, 1, 1));
        updatedNewYear.setLocalName("신정(갱신)");
        updatedNewYear.setName("New Year");
        updatedNewYear.setCountryCode("KR");
        updatedNewYear.setFixed(false);
        updatedNewYear.setGlobal(false);
        updatedNewYear.setCounties(null);
        updatedNewYear.setTypes(List.of("Public", "National"));

        HolidayDto lunarNewYear = new HolidayDto();
        lunarNewYear.setDate(LocalDate.of(2025, 2, 1));
        lunarNewYear.setLocalName("설날");
        lunarNewYear.setName("Lunar New Year");
        lunarNewYear.setCountryCode("KR");
        lunarNewYear.setFixed(true);
        lunarNewYear.setGlobal(true);
        lunarNewYear.setCounties(null);
        lunarNewYear.setTypes(List.of("Public"));

        HolidayDto independenceDay = new HolidayDto();
        independenceDay.setDate(LocalDate.of(2025, 3, 1));
        independenceDay.setLocalName("삼일절");
        independenceDay.setName("Independence Movement Day");
        independenceDay.setCountryCode("KR");
        independenceDay.setFixed(true);
        independenceDay.setGlobal(true);
        independenceDay.setCounties(null);
        independenceDay.setTypes(List.of("Public"));

        when(apiClient.getHolidays(2025, "KR"))
            .thenReturn(List.of(updatedNewYear, lunarNewYear, independenceDay));

        // When
        mockMvc.perform(put("/api/holidays/refresh")
                .param("year", "2025")
                .param("countryCode", "KR"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.status").value("SUCCESS"))
            .andExpect(jsonPath("$.message").value("재동기화 완료"))
            .andExpect(jsonPath("$.data.year").value(2025))
            .andExpect(jsonPath("$.data.countryCode").value("KR"))
            .andExpect(jsonPath("$.data.updatedRecords").value(1))
            .andExpect(jsonPath("$.data.insertedRecords").value(2));

        // Then
        List<Holiday> after = holidayRepository.findByYearAndCountryCode(2025, "KR");
        assertThat(after).hasSize(3);
        assertThat(after).extracting("localName")
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
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.status").value("SUCCESS"))
            .andExpect(jsonPath("$.message").value("삭제 완료"))
            .andExpect(jsonPath("$.data.year").value(2025))
            .andExpect(jsonPath("$.data.countryCode").value("KR"))
            .andExpect(jsonPath("$.data.deletedRecords").value(1));

        // Then
        List<Holiday> deleted = holidayRepository.findByYearAndCountryCode(2025, "KR");
        assertThat(deleted).isEmpty();
    }
}