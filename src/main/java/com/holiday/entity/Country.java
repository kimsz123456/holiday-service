package com.holiday.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;
import java.util.List;

@Entity
@Table(name = "Country")
@Getter
@Setter
public class Country {

    @Id
    @Column(name = "country_code", length = 2)
    private String countryCode;

    @Column(name = "name", length = 50)
    private String name;

    @OneToMany(mappedBy = "country")
    private List<Holiday> holidays;
}