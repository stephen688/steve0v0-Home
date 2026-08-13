package com.steve0v0.home.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalTime;

/**
 * 新增课程请求
 */
@Data
public class CourseCreateDTO {
    @NotBlank(message = "课程名称不能为空")
    @Size(max = 200, message = "课程名称最长200个字符")
    private String name;

    @NotNull(message = "开始日期不能为空")
    private LocalDate startDate;

    @NotNull(message = "结束日期不能为空")
    private LocalDate endDate;

    @NotNull(message = "开始时间不能为空")
    private LocalTime startTime;

    @NotNull(message = "结束时间不能为空")
    private LocalTime endTime;

    @Size(max = 200, message = "上课地点最长200个字符")
    private String location;

    /** 星期，1-7表示周一至周日，仅每周重复课程使用 */
    private Integer dayOfWeek;

    /** 0-仅当天，1-每周重复，默认0 */
    private Integer isRepeated;
}
