package com.holiday.controller;

import com.holiday.dto.HolidayDto;
import com.holiday.dto.InitResponseDto;
import com.holiday.entity.Holiday;
import com.holiday.service.HolidayService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;
import lombok.RequiredArgsConstructor;

import java.time.LocalDate;


@Tag(name = "Holiday API", description = "공휴일 정보 관리 API")
@RestController
@RequestMapping("/api/holidays")
@RequiredArgsConstructor
public class HolidayController {

    private final HolidayService holidayService;

    @Operation(summary = "데이터 초기 적재", description = "최근 5년(2021~2025)의 모든 국가 공휴일 데이터를 일괄 적재")
    @PostMapping("/init")
    public InitResponseDto init() {
        return holidayService.initHolidays();
    }

    @Operation(summary = "공휴일 검색", description = "연도, 국가 코드, 날짜 범위로 공휴일 검색")
    @GetMapping("/search")
    public Page<HolidayDto> search(
        @Parameter(description = "연도 (형식: yyyy)")
        @RequestParam(required = true) Integer year,

        @Parameter(description = "국가 코드 (형식: ISO 3166-1 alpha-2)")
        @RequestParam(required = true) String countryCode,

        @Parameter(description = "검색 시작일 (형식: yyyy-MM-dd)")
        @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fromDate,

        @Parameter(description = "검색 종료일 (형식: yyyy-MM-dd)")
        @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate toDate,

        @Parameter(description = "정렬 방향 (asc 또는 desc, 기본값: asc)")
        @RequestParam(defaultValue = "asc") String sortDirection,

        @Parameter(description = "페이지 번호 (0부터 시작)")
        @RequestParam(defaultValue = "0") int page,

        @Parameter(description = "페이지 크기")
        @RequestParam(defaultValue = "20") int size
    ) {
        Sort sort = sortDirection.equalsIgnoreCase("desc")
            ? Sort.by("id.date").descending()
            : Sort.by("id.date").ascending();

        Pageable pageable = PageRequest.of(page, size, sort);

        return holidayService.searchHolidaysWithFilters(year, countryCode, fromDate, toDate,
            pageable);
    }
}
