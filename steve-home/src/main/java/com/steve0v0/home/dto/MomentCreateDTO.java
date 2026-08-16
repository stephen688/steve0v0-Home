package com.steve0v0.home.dto;

import jakarta.validation.constraints.Size;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 新建动态请求
 * content 必填，images 可选（仅当 mediaType=image 时使用）
 * 第一阶段 mediaType 仅允许 text 和 image
 */
@Data
public class MomentCreateDTO {
    @Size(max = 5000, message = "动态内容不能超过5000字")
    private String content;

    /** 媒体类型：text / image，默认 text */
    private String mediaType;

    /** 图片 URL 列表，仅当 mediaType=image 时使用 */
    @Size(max = 9, message = "每条动态最多上传9张图片")
    private List<String> images;

    /** 朋友圈原始地点；没有地点时留空 */
    @Size(max = 200, message = "动态地点不能超过200字")
    private String location;

    /**
     * 动态原始发布时间。搬运历史朋友圈时传入；留空则使用当前发布时间。
     */
    private LocalDateTime createdAt;
}
