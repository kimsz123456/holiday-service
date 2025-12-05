package com.holiday;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class HolidayServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(HolidayServiceApplication.class, args);
    }
}
