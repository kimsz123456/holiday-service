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
import java.util.concurrent.CompletableFuture;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class HolidayService {

    private final HolidayRepository holidayRepository;
    private final CountryRepository countryRepository;
    private final NagerApiClient nagerApiClient;
    private final AsyncService asyncService;


    public InitResponseDto initHolidays() {
        log.info("공휴일 데이터 초기 적재 시작");
        int startYear = 2021;
        int endYear = 2025;
        int totalYears = endYear - startYear + 1;

        List<CountryDto> countries = nagerApiClient.getCountries();
        log.info("{}개 국가의 공휴일 수집 시작 ({}~{})",
            countries.size(), startYear, endYear);

        List<Country> savedCountries = countries.stream()
            .map(dto -> {
                Country country = new Country();
                country.setCountryCode(dto.getCountryCode());
                country.setName(dto.getName());
                return country;
            })
            .toList();

        countryRepository.saveAll(savedCountries);

        List<CompletableFuture<List<Holiday>>> futures = savedCountries.stream()
            .map(country -> asyncService.processCountryAsync(country, startYear, endYear))
            .toList();

        List<Holiday> allHolidays = futures.stream()
            .map(CompletableFuture::join)
            .flatMap(List::stream)
            .toList();

        holidayRepository.saveAll(allHolidays);
        log.info("총 {}개의 공휴일 데이터 저장 완료 - 국가: {}, 연도: {}, 레코드: {}",
            allHolidays.size(), countries.size(), totalYears, allHolidays.size());

        return InitResponseDto.builder()
            .status("SUCCESS")
            .message("데이터 적재 완료")
            .build();
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

        Country country = countryRepository.findById(countryCode)
            .orElseGet(() -> {
                log.info("국가 정보 없음 - 외부 API에서 조회: {}", countryCode);
                CountryDto countryDto = nagerApiClient.getCountries().stream()
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

        List<Holiday> holidayEntities = holidays.stream().map(holidayDto -> {
            Holiday holiday = new Holiday();
            holiday.setId(new HolidayId(holidayDto.getDate(), countryCode, holidayDto.getName()));
            holiday.setLocalName(holidayDto.getLocalName());
            holiday.setYear(year);
            holiday.setCountry(country);
            holiday.setFixed(holidayDto.getFixed());
            holiday.setGlobal(holidayDto.getGlobal());
            holiday.setCounties(holidayDto.getCounties());
            holiday.setTypes(holidayDto.getTypes());
            return holiday;
        }).toList();

        holidayRepository.saveAll(holidayEntities);

        log.info("재동기화 완료 - year: {}, countryCode: {}", year, countryCode);

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
