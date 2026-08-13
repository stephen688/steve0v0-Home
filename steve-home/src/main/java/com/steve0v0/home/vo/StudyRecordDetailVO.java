package com.steve0v0.home.vo;

import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 学习记录详情展示对象
 */
@Data
public class StudyRecordDetailVO {
    private Long id;
    private LocalDate recordDate;
    private String subject;
    private String content;
    /** 学习时长（分钟） */
    private Integer duration;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
