package com.steve0v0.home.vo;

import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

/**
 * 课程展示对象
 */
@Data
public class CourseVO {
    private Long id;
    private String name;
    private LocalDate startDate;
    private LocalDate endDate;
    private LocalTime startTime;
    private LocalTime endTime;
    private String location;
    /** 星期，1-7表示周一至周日 */
    private Integer dayOfWeek;
    /** 0-仅当天，1-每周重复 */
    private Integer isRepeated;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
