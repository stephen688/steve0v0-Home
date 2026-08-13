package com.steve0v0.home.controller;

import com.steve0v0.home.common.result.Result;
import com.steve0v0.home.dto.ArticleQueryDTO;
import com.steve0v0.home.service.ArticleService;
import com.steve0v0.home.vo.ArticleDetailVO;
import com.steve0v0.home.vo.ArticleListVO;
import com.steve0v0.home.common.pagination.PageResult;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

/**
 * 公开文章接口
 * 访客无需登录即可访问，仅返回已发布文章
 */
@RestController
@RequestMapping("/api/articles")
@RequiredArgsConstructor
public class ArticleController {

    private final ArticleService articleService;

    /**
     * 文章列表（分页 + 分类筛选）
     * 仅返回已发布文章，按发布时间倒序
     */
    @GetMapping
    public Result<PageResult<ArticleListVO>> list(ArticleQueryDTO queryDTO) {
        return Result.success(articleService.getPublicArticleList(queryDTO));
    }

    /**
     * 文章详情
     * 仅返回已发布文章，草稿/不存在统一返回 404
     * 每次访问阅读数原子 +1
     */
    @GetMapping("/{id}")
    public Result<ArticleDetailVO> detail(@PathVariable Long id) {
        return Result.success(articleService.getPublicArticleById(id));
    }
}
