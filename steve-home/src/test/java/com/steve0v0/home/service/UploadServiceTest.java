package com.steve0v0.home.service;

import com.steve0v0.home.common.exception.BusinessException;
import com.steve0v0.home.vo.MarkdownUploadVO;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockMultipartFile;

import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class UploadServiceTest {

    private final UploadService uploadService = new UploadService();

    @Test
    void parseMarkdownReturnsUtf8ContentAndSafeFilename() {
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "C:\\fakepath\\示例文章.md",
                "text/markdown",
                "# 标题\n\n正文".getBytes(StandardCharsets.UTF_8));

        MarkdownUploadVO result = uploadService.parseMarkdown(file);

        assertEquals("示例文章.md", result.getFileName());
        assertEquals("# 标题\n\n正文", result.getContent());
    }

    @Test
    void parseMarkdownRemovesUtf8Bom() {
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "article.markdown",
                "text/markdown",
                "\uFEFF# 标题".getBytes(StandardCharsets.UTF_8));

        MarkdownUploadVO result = uploadService.parseMarkdown(file);

        assertEquals("# 标题", result.getContent());
    }

    @Test
    void parseMarkdownRejectsUnsupportedExtension() {
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "article.txt",
                "text/plain",
                "# 标题".getBytes(StandardCharsets.UTF_8));

        BusinessException exception = assertThrows(
                BusinessException.class,
                () -> uploadService.parseMarkdown(file));

        assertEquals(400, exception.getCode());
        assertEquals("仅支持 .md 或 .markdown 文件", exception.getMessage());
    }

    @Test
    void parseMarkdownRejectsInvalidUtf8() {
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "article.md",
                "text/markdown",
                new byte[]{(byte) 0xC3, (byte) 0x28});

        BusinessException exception = assertThrows(
                BusinessException.class,
                () -> uploadService.parseMarkdown(file));

        assertEquals(400, exception.getCode());
        assertEquals("Markdown 文件必须使用 UTF-8 编码", exception.getMessage());
    }
}
