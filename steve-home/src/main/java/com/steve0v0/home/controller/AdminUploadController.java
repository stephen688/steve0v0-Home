package com.steve0v0.home.controller;

import com.steve0v0.home.common.result.Result;
import com.steve0v0.home.service.UploadService;
import com.steve0v0.home.vo.MarkdownUploadVO;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.Map;

/**
 * 管理端文件上传接口
 * 需要 JWT 鉴权
 * 第一阶段仅支持图片上传（JPG/PNG/GIF），≤5MB
 */
@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
public class AdminUploadController {

    private final UploadService uploadService;

    /**
     * 上传图片
     * 校验文件签名 + 解码 + UUID 文件名
     * 返回可公开访问的 URL
     */
    @PostMapping("/upload")
    public Result<Map<String, String>> upload(@RequestParam("file") MultipartFile file) {
        String url = uploadService.uploadImage(file);
        return Result.success(Map.of("url", url));
    }

    /**
     * 导入 Markdown 文章正文。
     * 文件只在内存中完成校验和 UTF-8 解码，不保存到上传目录。
     */
    @PostMapping("/upload/markdown")
    public Result<MarkdownUploadVO> uploadMarkdown(@RequestParam("file") MultipartFile file) {
        return Result.success(uploadService.parseMarkdown(file));
    }
}
