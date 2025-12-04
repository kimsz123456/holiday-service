package com.holiday.controller;

import com.holiday.dto.InitResponseDto;
import com.holiday.service.HolidayService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.*;
import lombok.RequiredArgsConstructor;


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

}
