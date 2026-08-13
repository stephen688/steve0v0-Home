package com.steve0v0.home.vo;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 文章列表展示对象
 * 不包含正文 content，减少列表接口数据量
 */
@Data
public class ArticleListVO {
    private Long id;
    private String title;
    private String summary;
    private String coverImage;
    private String category;
    private String tags;
    private Integer viewCount;
    private LocalDateTime publishedAt;

    /** 预计阅读时长（分钟），后端按字数实时计算 */
    private Integer readTimeMinutes;
}
