package com.steve0v0.home.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * 新建文章请求
 * title 必填，其他字段可选
 */
@Data
public class ArticleCreateDTO {
    @NotBlank(message = "标题不能为空")
    @Size(max = 200, message = "标题最长200个字符")
    private String title;

    @Size(max = 500, message = "摘要最长500个字符")
    private String summary;

    private String content;
    private String coverImage;

    /** 分类：tech-技术博客 / life-生活文章，默认 tech */
    private String category;

    /** 标签，逗号分隔 */
    private String tags;

    /** 状态：0-草稿 1-已发布，默认草稿 */
    private Integer status;
}
