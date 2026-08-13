package com.steve0v0.home.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 学习记录实体
 * 对应数据库 study_record 表，记录每日学习活动
 */
@Data
@TableName("study_record")
public class StudyRecord {
    @TableId(type = IdType.AUTO)
    private Long id;
    private LocalDate recordDate;
    private String subject;
    private String content;
    /** 学习时长，单位分钟 */
    private Integer duration;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
