package com.holiday.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import com.holiday.client.NagerApiClient;
import com.holiday.dto.HolidayDto;
import com.holiday.entity.Country;
import com.holiday.entity.Holiday;
import com.holiday.entity.HolidayId;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;


@Slf4j
@Service
@RequiredArgsConstructor
public class AsyncService {

    private final NagerApiClient nagerApiClient;

    @Async("holidayTaskExecutor")
    public CompletableFuture<List<Holiday>> processCountryAsync(
        Country country, int startYear, int endYear) {

        List<Holiday> result = new ArrayList<>();

        try {
            for (int year = startYear; year <= endYear; year++) {

                List<HolidayDto> dtoList = nagerApiClient.getHolidays(year,
                    country.getCountryCode());
                if (dtoList == null) {
                    continue;
                }

                for (HolidayDto dto : dtoList) {

                    HolidayId id = new HolidayId(dto.getDate(),
                        country.getCountryCode(),
                        dto.getName());

                    Holiday holiday = new Holiday();
                    holiday.setId(id);
                    holiday.setLocalName(dto.getLocalName());
                    holiday.setYear(year);
                    holiday.setCountry(country);
                    holiday.setFixed(dto.getFixed());
                    holiday.setGlobal(dto.getGlobal());
                    holiday.setCounties(dto.getCounties());
                    holiday.setTypes(dto.getTypes());

                    result.add(holiday);
                }
            }

        } catch (Exception ex) {
            log.error("국가 {} 공휴일 처리중 오류 발생: {}",
                country.getCountryCode(), ex.getMessage(), ex);
        }

        return CompletableFuture.completedFuture(result);
    }
}

