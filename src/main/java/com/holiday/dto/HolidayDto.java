package com.holiday.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.holiday.entity.Holiday;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.List;

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

    @JsonProperty("fixed")
    private Boolean fixed;

    @JsonProperty("global")
    private Boolean global;

    @JsonProperty("counties")
    private List<String> counties;

    @JsonProperty("types")
    private List<String> types;

    public HolidayDto(Holiday holiday) {
        this.date = holiday.getId().getDate();
        this.countryCode = holiday.getId().getCountryCode();
        this.localName = holiday.getLocalName();
        this.name = holiday.getName();
        this.fixed = holiday.getFixed();
        this.global = holiday.getGlobal();
        this.counties = holiday.getCounties();
        this.types = holiday.getTypes();
    }
}