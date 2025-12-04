package com.holiday.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class InitResponseDto {

    private String status;
    private String message;
    private InitData data;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class InitData {
        private Integer totalCountries;
        private Integer totalYears;
        private Integer totalRecords;
        private LocalDateTime processedAt;
    }
}
