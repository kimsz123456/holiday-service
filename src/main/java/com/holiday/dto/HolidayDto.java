package com.holiday.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.holiday.entity.Holiday;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class HolidayDto {

    @JsonProperty("date")
    private LocalDate date;

    @JsonProperty("localName")
    private String localName;

    @JsonProperty("name")
    private String name;

    @JsonProperty("countryCode")
    private String countryCode;

    public HolidayDto(Holiday holiday) {
        this.date = holiday.getId().getDate();
        this.countryCode = holiday.getId().getCountryCode();
        this.localName = holiday.getLocalName();
        this.name = holiday.getName();
    }
}