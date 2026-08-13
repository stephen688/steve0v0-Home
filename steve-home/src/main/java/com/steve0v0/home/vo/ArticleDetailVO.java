package com.steve0v0.home.vo;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 文章详情展示对象
 * 包含完整正文和所有元信息
 */
@Data
public class ArticleDetailVO {
    private Long id;
    private String title;
    private String summary;
    private String content;
    private String coverImage;
    private String category;
    private String tags;
    private Integer status;
    private Integer viewCount;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private LocalDateTime publishedAt;

    /** 预计阅读时长（分钟） */
    private Integer readTimeMinutes;
}
