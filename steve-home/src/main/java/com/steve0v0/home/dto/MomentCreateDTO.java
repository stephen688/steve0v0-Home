package com.steve0v0.home.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.util.List;

/**
 * 新建动态请求
 * content 必填，images 可选（仅当 mediaType=image 时使用）
 * 第一阶段 mediaType 仅允许 text 和 image
 */
@Data
public class MomentCreateDTO {
    @NotBlank(message = "动态内容不能为空")
    private String content;

    /** 媒体类型：text / image，默认 text */
    private String mediaType;

    /** 图片 URL 列表，仅当 mediaType=image 时使用 */
    private List<String> images;
}
