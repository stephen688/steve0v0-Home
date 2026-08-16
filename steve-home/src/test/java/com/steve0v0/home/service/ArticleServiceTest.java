package com.steve0v0.home.service;

import com.steve0v0.home.dto.ArticleUpdateDTO;
import com.steve0v0.home.entity.Article;
import com.steve0v0.home.mapper.ArticleMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ArticleServiceTest {

    @Mock
    private ArticleMapper articleMapper;

    @Test
    void updatePublishedArticlePreservesPublishTimeAndViewCount() {
        LocalDateTime publishedAt = LocalDateTime.of(2025, 8, 1, 12, 0);
        Article article = article(1, publishedAt);
        article.setViewCount(42);
        when(articleMapper.selectById(10L)).thenReturn(article);

        new ArticleService(articleMapper).updateArticle(10L, updateDto(1));

        Article updated = captureUpdatedArticle();
        assertEquals(publishedAt, updated.getPublishedAt());
        assertEquals(42, updated.getViewCount());
        assertEquals("修改后的标题", updated.getTitle());
    }

    @Test
    void publishingDraftSetsPublishTime() {
        when(articleMapper.selectById(10L)).thenReturn(article(0, null));

        new ArticleService(articleMapper).updateArticle(10L, updateDto(1));

        assertNotNull(captureUpdatedArticle().getPublishedAt());
    }

    @Test
    void savingPublishedArticleAsDraftPreservesOriginalPublishTime() {
        LocalDateTime publishedAt = LocalDateTime.of(2025, 8, 1, 12, 0);
        when(articleMapper.selectById(10L)).thenReturn(article(1, publishedAt));

        new ArticleService(articleMapper).updateArticle(10L, updateDto(0));

        Article updated = captureUpdatedArticle();
        assertEquals(0, updated.getStatus());
        assertEquals(publishedAt, updated.getPublishedAt());
    }

    private Article captureUpdatedArticle() {
        ArgumentCaptor<Article> captor = ArgumentCaptor.forClass(Article.class);
        verify(articleMapper).updateById(captor.capture());
        return captor.getValue();
    }

    private Article article(int status, LocalDateTime publishedAt) {
        Article article = new Article();
        article.setId(10L);
        article.setTitle("原标题");
        article.setStatus(status);
        article.setPublishedAt(publishedAt);
        return article;
    }

    private ArticleUpdateDTO updateDto(int status) {
        ArticleUpdateDTO dto = new ArticleUpdateDTO();
        dto.setTitle("修改后的标题");
        dto.setSummary("新摘要");
        dto.setContent("新正文");
        dto.setCoverImage("/upload/cover.png");
        dto.setCategory("tech");
        dto.setTags("Spring,Vue");
        dto.setStatus(status);
        return dto;
    }
}
