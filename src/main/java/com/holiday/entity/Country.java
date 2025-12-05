package com.holiday.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

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

    @JsonIgnore
    @OneToMany(mappedBy = "country")
    private List<Holiday> holidays;
}