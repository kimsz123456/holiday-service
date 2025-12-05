package com.holiday.repository;

import com.holiday.entity.Holiday;
import com.holiday.entity.HolidayId;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;

public interface HolidayRepository extends JpaRepository<Holiday, HolidayId> {

    @Query("SELECT h FROM Holiday h WHERE h.year = :year AND h.id.countryCode = :countryCode")
    List<Holiday> findByYearAndCountryCode(@Param("year") Integer year,
        @Param("countryCode") String countryCode);

    @Query("SELECT h FROM Holiday h WHERE " +
        "(h.year = :year) AND " +
        "(h.id.countryCode = :countryCode) AND " +
        "(:fromDate IS NULL OR h.id.date >= :fromDate) AND " +
        "(:toDate IS NULL OR h.id.date <= :toDate)")
    Page<Holiday> searchWithFilters(
        @Param("year") Integer year,
        @Param("countryCode") String countryCode,
        @Param("fromDate") LocalDate fromDate,
        @Param("toDate") LocalDate toDate,
        Pageable pageable
    );

    void deleteByYearAndIdCountryCode(Integer year, String countryCode);
}