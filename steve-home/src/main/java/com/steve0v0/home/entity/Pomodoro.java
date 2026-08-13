package com.steve0v0.home.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 番茄钟完成记录实体
 * 对应数据库 pomodoro 表，只保存已完成的番茄钟记录
 * duration 是专注时长的唯一真源，不记录 start_time/end_time
 */
@Data
@TableName("pomodoro")
public class Pomodoro {
    @TableId(type = IdType.AUTO)
    private Long id;
    /** 关联任务，自由文本 */
    private String taskName;
    /** 倒计时设定时长，单位分钟 */
    private Integer duration;
    /** 完成时间 */
    private LocalDateTime completedAt;
    private LocalDateTime createdAt;
}
