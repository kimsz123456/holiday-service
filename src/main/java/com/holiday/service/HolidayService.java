package com.holiday.service;

import com.holiday.client.NagerApiClient;
import com.holiday.dto.CountryDto;
import com.holiday.dto.DeleteResponseDto;
import com.holiday.dto.HolidayDto;
import com.holiday.dto.InitResponseDto;
import com.holiday.dto.RefreshResponseDto;
import com.holiday.entity.Country;
import com.holiday.entity.Holiday;
import com.holiday.entity.HolidayId;
import com.holiday.repository.CountryRepository;
import com.holiday.repository.HolidayRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class HolidayService {

    private final HolidayRepository holidayRepository;
    private final CountryRepository countryRepository;
    private final NagerApiClient nagerApiClient;

    public InitResponseDto initHolidays() {
        log.info("공휴일 데이터 초기 적재 시작");
        int startYear = 2021;
        int endYear = 2025;
        int totalYears = endYear - startYear + 1;

        List<CountryDto> countries = nagerApiClient.getCountries();
        log.info("{}개 국가의 공휴일 데이터 수집 시작 ({}~{})", countries.size(), startYear, endYear);

        List<Holiday> allHolidays = countries.parallelStream()
            .flatMap(countryDto -> processCountry(countryDto, startYear, endYear).stream())
            .toList();

        holidayRepository.saveAll(allHolidays);
        log.info("총 {}개의 공휴일 데이터 저장 완료 - 국가: {}, 연도: {}, 레코드: {}",
            allHolidays.size(), countries.size(), totalYears, allHolidays.size());

        return InitResponseDto.builder()
            .status("SUCCESS")
            .message("데이터 적재 완료")
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
                HolidayId holidayId = new HolidayId(holidayDto.getDate(), country.getCountryCode(),
                    holidayDto.getName());

                Holiday holiday = new Holiday();
                holiday.setId(holidayId);
                holiday.setLocalName(holidayDto.getLocalName());
                holiday.setYear(year);
                holiday.setCountry(country);
                holiday.setFixed(holidayDto.getFixed());
                holiday.setGlobal(holidayDto.getGlobal());
                holiday.setCounties(holidayDto.getCounties());
                holiday.setTypes(holidayDto.getTypes());

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

    @Transactional
    public RefreshResponseDto refreshHolidays(Integer year, String countryCode) {
        log.info("공휴일 재동기화 시작 - year: {}, countryCode: {}", year, countryCode);

        List<Holiday> existingHolidays = holidayRepository.findByYearAndCountryCode(year,
            countryCode);
        log.debug("기존 데이터 {}개 조회됨", existingHolidays.size());

        Map<HolidayId, Holiday> existingMap = existingHolidays.stream()
            .collect(Collectors.toMap(Holiday::getId, h -> h));

        Country country = countryRepository.findById(countryCode)
            .orElseGet(() -> {
                log.info("국가 정보 없음 - 외부 API에서 조회: {}", countryCode);
                List<CountryDto> countries = nagerApiClient.getCountries();
                CountryDto countryDto = countries.stream()
                    .filter(c -> c.getCountryCode().equals(countryCode))
                    .findFirst()
                    .orElseThrow(
                        () -> new IllegalArgumentException("유효하지 않은 국가 코드입니다: " + countryCode));

                Country newCountry = new Country();
                newCountry.setCountryCode(countryDto.getCountryCode());
                newCountry.setName(countryDto.getName());
                return countryRepository.save(newCountry);
            });

        List<HolidayDto> holidays = nagerApiClient.getHolidays(year, countryCode);
        if (holidays == null) {
            holidays = new ArrayList<>();
        }
        log.debug("외부 API에서 {}개 공휴일 데이터 조회됨", holidays.size());

        int updatedCount = 0;
        int insertedCount = 0;
        Set<HolidayId> apiHolidayIds = new java.util.HashSet<>();

        List<Holiday> holidayEntities = new ArrayList<>();
        for (HolidayDto holidayDto : holidays) {
            HolidayId holidayId = new HolidayId(holidayDto.getDate(), countryCode,
                holidayDto.getName());
            apiHolidayIds.add(holidayId);

            Holiday holiday = new Holiday();
            holiday.setId(holidayId);
            holiday.setLocalName(holidayDto.getLocalName());
            holiday.setYear(year);
            holiday.setCountry(country);
            holiday.setFixed(holidayDto.getFixed());
            holiday.setGlobal(holidayDto.getGlobal());
            holiday.setCounties(holidayDto.getCounties());
            holiday.setTypes(holidayDto.getTypes());

            if (existingMap.containsKey(holidayId)) {
                updatedCount++;
            } else {
                insertedCount++;
            }

            holidayEntities.add(holiday);
        }

        holidayRepository.saveAll(holidayEntities);
        log.info("재동기화 완료 - year: {}, countryCode: {}, updated: {}, inserted: {}",
            year, countryCode, updatedCount, insertedCount);

        return RefreshResponseDto.builder()
            .status("SUCCESS")
            .message("재동기화 완료")
            .build();
    }

    @Transactional
    public DeleteResponseDto deleteHolidays(Integer year, String countryCode) {
        log.info("공휴일 삭제 시작 - year: {}, countryCode: {}", year, countryCode);

        countryRepository.findById(countryCode)
            .orElseThrow(() -> new IllegalArgumentException("유효하지 않은 국가 코드입니다: " + countryCode));

        List<Holiday> existingHolidays = holidayRepository.findByYearAndCountryCode(year,
            countryCode);
        int deletedCount = existingHolidays.size();

        holidayRepository.deleteByYearAndIdCountryCode(year, countryCode);
        log.info("공휴일 삭제 완료 - year: {}, countryCode: {}, deleted: {}개",
            year, countryCode, deletedCount);

        return DeleteResponseDto.builder()
            .status("SUCCESS")
            .message("삭제 완료")
            .build();
    }
}
