package com.steve0v0.home.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

/**
 * 课程实体
 * 对应数据库 course 表，支持单日课程和每周重复课程
 */
@Data
@TableName("course")
public class Course {
    @TableId(type = IdType.AUTO)
    private Long id;
    private String name;
    private LocalDate startDate;
    private LocalDate endDate;
    private LocalTime startTime;
    private LocalTime endTime;
    private String location;
    /** 星期，1-7表示周一至周日，仅每周重复课程使用 */
    private Integer dayOfWeek;
    /** 0-仅当天，1-每周重复 */
    private Integer isRepeated;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
