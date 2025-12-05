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
public class RefreshResponseDto {

    private String status;
    private String message;
    private RefreshData data;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class RefreshData {

        private Integer year;
        private String countryCode;
        private Integer updatedRecords;
        private Integer insertedRecords;
        private LocalDateTime processedAt;
    }
}
