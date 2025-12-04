package com.holiday.service;

import com.holiday.client.NagerApiClient;
import com.holiday.dto.CountryDto;
import com.holiday.dto.HolidayDto;
import com.holiday.dto.InitResponseDto;
import com.holiday.entity.Country;
import com.holiday.entity.Holiday;
import com.holiday.entity.HolidayId;
import com.holiday.repository.CountryRepository;
import com.holiday.repository.HolidayRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class HolidayService {

    private final HolidayRepository holidayRepository;
    private final CountryRepository countryRepository;
    private final NagerApiClient nagerApiClient;

    public InitResponseDto initHolidays() {
        int startYear = 2021;
        int endYear = 2025;
        int totalYears = endYear - startYear + 1;

        List<CountryDto> countries = nagerApiClient.getCountries();

        List<Holiday> allHolidays = countries.parallelStream()
            .flatMap(countryDto -> processCountry(countryDto, startYear, endYear).stream())
            .toList();

        holidayRepository.saveAll(allHolidays);

        InitResponseDto.InitData data = InitResponseDto.InitData.builder()
            .totalCountries(countries.size())
            .totalYears(totalYears)
            .totalRecords(allHolidays.size())
            .processedAt(LocalDateTime.now())
            .build();

        return InitResponseDto.builder()
            .status("SUCCESS")
            .message("데이터 적재 완료")
            .data(data)
            .build();
    }

    @Transactional
    public List<Holiday> processCountry(CountryDto countryDto, int startYear, int endYear) {
        Country country = new Country();
        country.setCountryCode(countryDto.getCountryCode());
        country.setName(countryDto.getName());
        countryRepository.save(country);

        List<Holiday> countryHolidays = new ArrayList<>();

        for (int year = startYear; year <= endYear; year++) {
            List<HolidayDto> holidays = nagerApiClient.getHolidays(year, country.getCountryCode());
            if (holidays == null) {
                continue;
            }

            for (HolidayDto holidayDto : holidays) {
                HolidayId holidayId = new HolidayId(holidayDto.getDate(), country.getCountryCode());

                Holiday holiday = new Holiday();
                holiday.setId(holidayId);
                holiday.setLocalName(holidayDto.getLocalName());
                holiday.setName(holidayDto.getName());
                holiday.setYear(year);
                holiday.setCountry(country);

                countryHolidays.add(holiday);
            }
        }

        return countryHolidays;
    }

    public Page<HolidayDto> searchHolidaysWithFilters(
        Integer year,
        String countryCode,
        java.time.LocalDate fromDate,
        java.time.LocalDate toDate,
        Pageable pageable
    ) {
        if (fromDate != null && fromDate.getYear() != year) {
            throw new IllegalArgumentException(
                String.format("검색 시작일의 연도(%d)가 선택한 연도(%d)와 일치하지 않습니다.", fromDate.getYear(), year)
            );
        }

        if (toDate != null && toDate.getYear() != year) {
            throw new IllegalArgumentException(
                String.format("검색 종료일의 연도(%d)가 선택한 연도(%d)와 일치하지 않습니다.", toDate.getYear(), year)
            );
        }

        if (fromDate != null && toDate != null && fromDate.isAfter(toDate)) {
            throw new IllegalArgumentException("검색 시작일이 종료일보다 늦을 수 없습니다.");
        }
        Page<Holiday> holidays = holidayRepository.searchWithFilters(year, countryCode, fromDate,
            toDate, pageable);
        return holidays.map(holiday -> new HolidayDto(holiday));
    }
}
