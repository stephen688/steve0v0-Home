package com.steve0v0.home.vo;

import lombok.Data;

import java.time.LocalDate;
import java.util.List;

/**
 * 日历响应对象
 * 包含指定日期范围内的学习记录和课程实例
 */
@Data
public class CalendarVO {
    /** 视图模式：month / week */
    private String view;
    private LocalDate startDate;
    private LocalDate endDate;
    /** 学习记录列表 */
    private List<RecordItem> records;
    /** 课程实例列表 */
    private List<CourseItem> courses;

    /**
     * 日历中的学习记录条目
     * 只包含日历格子展示所需字段
     */
    @Data
    public static class RecordItem {
        private Long id;
        private LocalDate recordDate;
        private String subject;
        /** 学习时长（分钟） */
        private Integer duration;
    }

    /**
     * 日历中的课程实例条目
     * 每周重复课程会按日期生成多个实例
     */
    @Data
    public static class CourseItem {
        private Long id;
        /** 该课程实例对应的日期 */
        private LocalDate date;
        private String name;
        /** 开始时间（HH:mm） */
        private String startTime;
        /** 结束时间（HH:mm） */
        private String endTime;
        private String location;
    }
}
