package com.steve0v0.home.entity;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 关于页个人资料实体
 * 单站点模式固定一条记录（id=1）
 */
@Data
@TableName("about_profile")
public class AboutProfile {
    @TableId
    private Long id;
    private String name;
    private String avatarUrl;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
