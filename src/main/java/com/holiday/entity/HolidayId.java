package com.holiday.entity;

import jakarta.persistence.Embeddable;
import java.io.Serializable;
import java.time.LocalDate;
import java.util.Objects;
import lombok.Getter;
import lombok.Setter;

@Embeddable
@Getter
@Setter
public class HolidayId implements Serializable {

    private LocalDate date;
    private String countryCode;

    public HolidayId() {
    }

    public HolidayId(LocalDate date, String countryCode) {
        this.date = date;
        this.countryCode = countryCode;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof HolidayId that)) {
            return false;
        }
        return Objects.equals(date, that.date) &&
            Objects.equals(countryCode, that.countryCode);
    }

    @Override
    public int hashCode() {
        return Objects.hash(date, countryCode);
    }
}
