package com.steve0v0.home.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 动态实体
 * 对应数据库 moment 表，类似朋友圈的生活动态
 */
@Data
@TableName("moment")
public class Moment {
    @TableId(type = IdType.AUTO)
    private Long id;
    private String content;
    /** 媒体类型：text-纯文字 / image-多图 / music-音乐 / video-视频（第一阶段仅开放text和image） */
    private String mediaType;
    /** 音乐/视频外链 URL（第一阶段不使用） */
    private String mediaUrl;
    /** 朋友圈发布时携带的原始地点 */
    private String location;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
