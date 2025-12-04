package com.holiday.repository;

import com.holiday.entity.Holiday;
import com.holiday.entity.HolidayId;
import org.springframework.data.jpa.repository.JpaRepository;

public interface HolidayRepository extends JpaRepository<Holiday, HolidayId> {

}