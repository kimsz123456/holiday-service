package com.holiday.scheduler;

import com.holiday.dto.CountryDto;
import com.holiday.client.NagerApiClient;
import com.holiday.service.HolidayService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.Year;
import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class HolidayScheduler {

    private final HolidayService holidayService;
    private final NagerApiClient nagerApiClient;

    @Scheduled(cron = "0 0 1 2 1 ?", zone = "Asia/Seoul")
    public void autoSyncHolidays() {
        log.info("=== 공휴일 자동 동기화 시작: {} ===", LocalDateTime.now());

        int currentYear = Year.now().getValue();
        int previousYear = currentYear - 1;

        log.info("동기화 대상 연도: {} (전년도), {} (금년도)", previousYear, currentYear);

        List<CountryDto> countries = nagerApiClient.getCountries();
        log.info("총 {} 개 국가 동기화 시작", countries.size());

        int successCount = 0;
        int failureCount = 0;

        for (CountryDto country : countries) {
            String countryCode = country.getCountryCode();

            try {
                holidayService.refreshHolidays(previousYear, countryCode);
                log.debug("성공: {} ({}) - 전년도 ({})", country.getName(), countryCode, previousYear);
                successCount++;
            } catch (Exception e) {
                log.error("실패: {} ({}) - 전년도 ({}): {}", country.getName(), countryCode, previousYear, e.getMessage());
                failureCount++;
            }

            try {
                holidayService.refreshHolidays(currentYear, countryCode);
                log.debug("성공: {} ({}) - 금년도 ({})", country.getName(), countryCode, currentYear);
                successCount++;
            } catch (Exception e) {
                log.error("실패: {} ({}) - 금년도 ({}): {}", country.getName(), countryCode, currentYear, e.getMessage());
                failureCount++;
            }
        }

        log.info("=== 공휴일 자동 동기화 완료: {} ===", LocalDateTime.now());
        log.info("동기화 결과 - 성공: {}, 실패: {}, 총 작업: {}",
            successCount, failureCount, (successCount + failureCount));
    }
}
