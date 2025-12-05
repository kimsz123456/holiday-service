package com.holiday.scheduler;

import com.holiday.client.NagerApiClient;
import com.holiday.dto.CountryDto;
import com.holiday.service.HolidayService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Year;
import java.util.List;

import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class HolidaySchedulerTest {

    @Mock
    private HolidayService holidayService;

    @Mock
    private NagerApiClient nagerApiClient;

    @InjectMocks
    private HolidayScheduler holidayScheduler;

    private List<CountryDto> mockCountries;
    private int currentYear;
    private int previousYear;

    @BeforeEach
    void setUp() {
        currentYear = Year.now().getValue();
        previousYear = currentYear - 1;

        mockCountries = List.of(
            new CountryDto("KR", "South Korea"),
            new CountryDto("US", "United States"),
            new CountryDto("JP", "Japan")
        );
    }

    @Test
    void autoSyncHolidays_모든_국가_전년도_금년도_동기화_성공() {
        when(nagerApiClient.getCountries()).thenReturn(mockCountries);

        holidayScheduler.autoSyncHolidays();

        verify(nagerApiClient, times(1)).getCountries();

        verify(holidayService, times(1)).refreshHolidays(previousYear, "KR");
        verify(holidayService, times(1)).refreshHolidays(currentYear, "KR");

        verify(holidayService, times(1)).refreshHolidays(previousYear, "US");
        verify(holidayService, times(1)).refreshHolidays(currentYear, "US");

        verify(holidayService, times(1)).refreshHolidays(previousYear, "JP");
        verify(holidayService, times(1)).refreshHolidays(currentYear, "JP");

        verify(holidayService, times(6)).refreshHolidays(anyInt(), anyString());
    }

    @Test
    void autoSyncHolidays_일부_국가_실패해도_다른_국가_계속_처리() {
        when(nagerApiClient.getCountries()).thenReturn(mockCountries);

        doThrow(new RuntimeException("API 호출 실패"))
            .when(holidayService).refreshHolidays(previousYear, "KR");
        doThrow(new RuntimeException("데이터 없음"))
            .when(holidayService).refreshHolidays(currentYear, "US");

        holidayScheduler.autoSyncHolidays();

        verify(nagerApiClient, times(1)).getCountries();

        verify(holidayService, times(1)).refreshHolidays(previousYear, "KR");
        verify(holidayService, times(1)).refreshHolidays(currentYear, "KR");

        verify(holidayService, times(1)).refreshHolidays(previousYear, "US");
        verify(holidayService, times(1)).refreshHolidays(currentYear, "US");

        verify(holidayService, times(1)).refreshHolidays(previousYear, "JP");
        verify(holidayService, times(1)).refreshHolidays(currentYear, "JP");

        verify(holidayService, times(6)).refreshHolidays(anyInt(), anyString());
    }

    @Test
    void autoSyncHolidays_국가_목록_비어있으면_동기화_안함() {
        when(nagerApiClient.getCountries()).thenReturn(List.of());

        holidayScheduler.autoSyncHolidays();

        verify(nagerApiClient, times(1)).getCountries();
        verify(holidayService, never()).refreshHolidays(anyInt(), anyString());
    }

    @Test
    void autoSyncHolidays_단일_국가만_있어도_정상_동작() {
        List<CountryDto> singleCountry = List.of(new CountryDto("KR", "South Korea"));
        when(nagerApiClient.getCountries()).thenReturn(singleCountry);

        holidayScheduler.autoSyncHolidays();

        verify(nagerApiClient, times(1)).getCountries();
        verify(holidayService, times(1)).refreshHolidays(previousYear, "KR");
        verify(holidayService, times(1)).refreshHolidays(currentYear, "KR");
        verify(holidayService, times(2)).refreshHolidays(anyInt(), anyString());
    }

    @Test
    void autoSyncHolidays_국가_조회_실패시_동기화_안함() {
        when(nagerApiClient.getCountries()).thenThrow(new RuntimeException("API 연결 실패"));

        try {
            holidayScheduler.autoSyncHolidays();
        } catch (Exception e) {
        }

        verify(nagerApiClient, times(1)).getCountries();
        verify(holidayService, never()).refreshHolidays(anyInt(), anyString());
    }
}
