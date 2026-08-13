package com.steve0v0.home.vo;

import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * Markdown 文件导入结果。
 */
@Data
@AllArgsConstructor
public class MarkdownUploadVO {
    private String fileName;
    private String content;
}
