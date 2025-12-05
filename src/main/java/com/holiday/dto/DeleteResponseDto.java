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
public class DeleteResponseDto {

    private String status;
    private String message;
    private DeleteData data;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class DeleteData {

        private Integer year;
        private String countryCode;
        private Integer deletedRecords;
        private LocalDateTime processedAt;
    }
}
