package com.holiday.entity;

import jakarta.persistence.*;
import java.time.LocalDate;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "Holiday")
@Getter
@Setter
@IdClass(HolidayId.class) // 복합키를 위한 클래스
public class Holiday {

    @Id
    @Column(name = "date")
    private LocalDate date;

    @Id
    @Column(name = "country_code", length = 2, insertable = false, updatable = false)
    private String countryCode;

    @Column(name = "local_name", length = 50)
    private String localName;

    @Column(name = "name", length = 50)
    private String name;

    @Column(name = "year")
    private Integer year;

    @ManyToOne
    @JoinColumn(name = "country_code")
    private Country country;
}