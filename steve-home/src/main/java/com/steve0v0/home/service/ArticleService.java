package com.steve0v0.home.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.steve0v0.home.common.constant.Constants;
import com.steve0v0.home.common.exception.BusinessException;
import com.steve0v0.home.common.result.ResultCode;
import com.steve0v0.home.common.pagination.PageResult;
import com.steve0v0.home.dto.ArticleCreateDTO;
import com.steve0v0.home.dto.ArticleQueryDTO;
import com.steve0v0.home.entity.Article;
import com.steve0v0.home.mapper.ArticleMapper;
import com.steve0v0.home.vo.ArticleDetailVO;
import com.steve0v0.home.vo.ArticleListVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 文章服务
 * 处理文章的创建、发布、删除、列表查询、详情查询等业务逻辑
 * 包含草稿隔离、原子阅读数、预计阅读时长计算等核心功能
 */

@Slf4j
@Service
@RequiredArgsConstructor
public class ArticleService {

    private final ArticleMapper articleMapper;

    /**
     * 计算预计阅读时长（分钟）
     * 中文按字符数计算，英文按单词数估算，取 300 字/分钟
     */
    private int calculateReadTime(String content) {
        if (content == null || content.isBlank()) {
            return 1;
        }
        // 去除 Markdown 标记符号，粗略统计可读字符数
        String stripped = content.replaceAll("[#*`>\\-\\[\\]()!]", "").trim();
        int charCount = stripped.length();
        // 至少 1 分钟
        return Math.max(1, (int) Math.ceil(charCount / 300.0));
    }

    /**
     * 将实体转换为列表 VO
     */
    private ArticleListVO toListVO(Article article) {
        ArticleListVO vo = new ArticleListVO();
        vo.setId(article.getId());
        vo.setTitle(article.getTitle());
        vo.setSummary(article.getSummary());
        vo.setCoverImage(article.getCoverImage());
        vo.setCategory(article.getCategory());
        vo.setTags(article.getTags());
        vo.setViewCount(article.getViewCount());
        vo.setPublishedAt(article.getPublishedAt());
        vo.setReadTimeMinutes(calculateReadTime(article.getContent()));
        return vo;
    }

    /**
     * 将实体转换为详情 VO
     */
    private ArticleDetailVO toDetailVO(Article article) {
        ArticleDetailVO vo = new ArticleDetailVO();
        vo.setId(article.getId());
        vo.setTitle(article.getTitle());
        vo.setSummary(article.getSummary());
        vo.setContent(article.getContent());
        vo.setCoverImage(article.getCoverImage());
        vo.setCategory(article.getCategory());
        vo.setTags(article.getTags());
        vo.setStatus(article.getStatus());
        vo.setViewCount(article.getViewCount());
        vo.setCreatedAt(article.getCreatedAt());
        vo.setUpdatedAt(article.getUpdatedAt());
        vo.setPublishedAt(article.getPublishedAt());
        vo.setReadTimeMinutes(calculateReadTime(article.getContent()));
        return vo;
    }

    /**
     * 公开文章列表查询
     * 仅返回已发布文章（status=1），按 published_at DESC, id DESC 排序
     */
    public PageResult<ArticleListVO> getPublicArticleList(ArticleQueryDTO queryDTO) {
        Page<Article> page = new Page<>(queryDTO.getPage(), queryDTO.getSize());

        LambdaQueryWrapper<Article> wrapper = new LambdaQueryWrapper<>();

        // 公开接口强制只查已发布
        wrapper.eq(Article::getStatus, Constants.ARTICLE_STATUS_PUBLISHED);

        // 分类筛选
        if (queryDTO.getCategory() != null) {
            wrapper.eq(Article::getCategory, queryDTO.getCategory());
        }

        // 按 published_at DESC, id DESC 稳定排序
        wrapper.orderByDesc(Article::getPublishedAt)
               .orderByDesc(Article::getId);

        IPage<Article> result = articleMapper.selectPage(page, wrapper);
        List<ArticleListVO> list = result.getRecords().stream()
                .map(this::toListVO)
                .collect(Collectors.toList());
        return new PageResult<>(queryDTO.getPage(), queryDTO.getSize(), result.getTotal(), list);
    }

    /**
     * 公开文章详情查询
     * 仅返回已发布文章（status=1），草稿/已删除/不存在统一返回 404
     * 阅读数原子自增
     */
    public ArticleDetailVO getPublicArticleById(Long id) {
        LambdaQueryWrapper<Article> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Article::getId, id)
               .eq(Article::getStatus, Constants.ARTICLE_STATUS_PUBLISHED);
        Article article = articleMapper.selectOne(wrapper);
        if (article == null) {
            throw new BusinessException(ResultCode.NOT_FOUND);
        }
        // 原子递增阅读数，避免并发问题
        articleMapper.incrementViewCount(id);
        // 返回前更新 VO 中的阅读数
        article.setViewCount(article.getViewCount() + 1);
        return toDetailVO(article);
    }

    /**
     * 管理端文章列表查询
     * 可查看草稿和已发布，支持按分类和状态筛选
     */
    public PageResult<ArticleListVO> getAdminArticleList(ArticleQueryDTO queryDTO) {
        Page<Article> page = new Page<>(queryDTO.getPage(), queryDTO.getSize());
        LambdaQueryWrapper<Article> wrapper = new LambdaQueryWrapper<>();
        // 分类筛选
        if (queryDTO.getCategory() != null) {
            wrapper.eq(Article::getCategory, queryDTO.getCategory());
        }
        // 状态筛选（管理端可查草稿）
        if (queryDTO.getStatus() != null) {
            wrapper.eq(Article::getStatus, queryDTO.getStatus());
        }
        // 按创建时间倒序
        wrapper.orderByDesc(Article::getCreatedAt);

        IPage<Article> result = articleMapper.selectPage(page, wrapper);
        List<ArticleListVO> list = result.getRecords().stream()
                .map(this::toListVO)
                .collect(Collectors.toList());
        return new PageResult<>(queryDTO.getPage(), queryDTO.getSize(), result.getTotal(), list);
    }

    /**
     * 管理端文章详情查询
     * 可查看草稿
     */
    public ArticleDetailVO getAdminArticleById(Long id) {
        Article article = articleMapper.selectById(id);
        if (article == null) {
            throw new BusinessException(ResultCode.NOT_FOUND);
        }
        return toDetailVO(article);
    }

    /**
     * 新建文章
     * 若状态为已发布，写入 published_at
     */
    public Long createArticle(ArticleCreateDTO dto) {
        Article article = new Article();
        article.setTitle(dto.getTitle());
        article.setSummary(dto.getSummary());
        article.setContent(dto.getContent());
        article.setCoverImage(dto.getCoverImage());
        // 分类默认 tech
        article.setCategory(dto.getCategory() != null ? dto.getCategory() : Constants.CATEGORY_TECH);
        article.setTags(dto.getTags());
        // 状态默认草稿
        int status = dto.getStatus() != null ? dto.getStatus() : Constants.ARTICLE_STATUS_DRAFT;
        article.setStatus(status);
        article.setViewCount(0);
        // 首次发布时写入 published_at
        if (status == Constants.ARTICLE_STATUS_PUBLISHED) {
            article.setPublishedAt(LocalDateTime.now());
        }
        articleMapper.insert(article);
        log.info("新建文章成功 | id={} | title={}", article.getId(), article.getTitle());
        return article.getId();
    }

    /**
     * 删除文章（物理删除）
     */
    public void deleteArticle(Long id) {
        Article article = articleMapper.selectById(id);
        if (article == null) {
            throw new BusinessException(ResultCode.NOT_FOUND);
        }
        articleMapper.deleteById(id);
        log.info("删除文章成功 | id={} | title={}", id, article.getTitle());
    }
}
