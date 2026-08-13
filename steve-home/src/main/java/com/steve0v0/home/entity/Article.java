package com.steve0v0.home.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 文章实体
 * 对应数据库 article 表，包含技术博客和生活文章两类
 */
@Data
@TableName("article")
public class Article {
    @TableId(type = IdType.AUTO)
    private Long id;
    private String title;
    private String summary;
    private String content;
    private String coverImage;
    /** 分类：tech-技术博客 / life-生活文章 */
    private String category;
    /** 标签，逗号分隔 */
    private String tags;
    /** 状态：0-草稿 1-已发布 */
    private Integer status;
    private Integer viewCount;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    /** 发布时间，首次发布时写入，编辑不更新 */
    private LocalDateTime publishedAt;
}
