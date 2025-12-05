package com.holiday.entity;

import com.holiday.util.StringListConverter;
import jakarta.persistence.*;
import java.util.List;
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

    @Column(name = "name", length = 200, insertable = false, updatable = false)
    private String name;

    @Column(name = "yyyy")
    private Integer year;

    @Column(name = "fixed")
    private Boolean fixed;

    @Column(name = "global")
    private Boolean global;

    @Column(name = "counties", columnDefinition = "TEXT")
    @Convert(converter = StringListConverter.class)
    private List<String> counties;

    @Column(name = "types", columnDefinition = "TEXT")
    @Convert(converter = StringListConverter.class)
    private List<String> types;
}
