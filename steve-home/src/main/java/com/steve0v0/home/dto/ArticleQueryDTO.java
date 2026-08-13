package com.steve0v0.home.dto;

import com.steve0v0.home.common.pagination.PageRequest;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 文章列表查询参数
 * 公开接口和管理端接口共用，管理端可传 status 筛选草稿
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class ArticleQueryDTO extends PageRequest {
    /** 分类筛选：tech / life，不传则查全部 */
    private String category;

    /** 状态筛选：0-草稿 1-已发布，仅管理端使用 */
    private Integer status;
}
