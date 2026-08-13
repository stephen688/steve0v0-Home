package com.steve0v0.home.controller;

import com.steve0v0.home.common.result.Result;
import com.steve0v0.home.dto.ArticleCreateDTO;
import com.steve0v0.home.dto.ArticleQueryDTO;
import com.steve0v0.home.common.pagination.PageResult;
import com.steve0v0.home.service.ArticleService;
import com.steve0v0.home.vo.ArticleDetailVO;
import com.steve0v0.home.vo.ArticleListVO;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

/**
 * 管理端文章接口
 * 需要 JWT 鉴权，可管理草稿和已发布文章
 */
@RestController
@RequestMapping("/api/admin/articles")
@RequiredArgsConstructor
public class AdminArticleController {

    private final ArticleService articleService;

    /**
     * 管理端文章列表
     * 可查看草稿和已发布，支持按分类和状态筛选
     */
    @GetMapping
    public Result<PageResult<ArticleListVO>> list(ArticleQueryDTO queryDTO) {
        return Result.success(articleService.getAdminArticleList(queryDTO));
    }

    /**
     * 管理端文章详情
     * 可查看草稿
     */
    @GetMapping("/{id}")
    public Result<ArticleDetailVO> detail(@PathVariable Long id) {
        return Result.success(articleService.getAdminArticleById(id));
    }

    /**
     * 新建文章
     * 可直接发布或保存为草稿
     */
    @PostMapping
    public Result<Long> create(@Valid @RequestBody ArticleCreateDTO dto) {
        return Result.success(articleService.createArticle(dto));
    }

    /**
     * 删除文章（物理删除）
     */
    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        articleService.deleteArticle(id);
        return Result.success();
    }
}
