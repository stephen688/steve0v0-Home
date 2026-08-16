package com.steve0v0.home.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 个人状态实体
 * 对应数据库 personal_status 表，单站点模式固定一条记录（id=1）
 */
@Data
@TableName("personal_status")
public class Status {
    /** 固定为 1 */
    private Long id;
    /** 状态：online/studying/exercising/busy/rest */
    private String state;
    private String currentTask;
    private String mood;
    private LocalDateTime updatedAt;
}
