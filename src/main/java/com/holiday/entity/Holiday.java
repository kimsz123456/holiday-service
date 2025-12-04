package com.holiday.entity;

import jakarta.persistence.*;
import java.time.LocalDate;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "Holiday")
@Getter
@Setter
public class Holiday {

    @EmbeddedId
    private HolidayId id;

    @MapsId("countryCode")
    @ManyToOne
    @JoinColumn(name = "country_code")
    private Country country;

    @Column(name = "local_name", length = 200)
    private String localName;

    @Column(name = "name", length = 200)
    private String name;

    @Column(name = "yyyy")
    private Integer year;
}
