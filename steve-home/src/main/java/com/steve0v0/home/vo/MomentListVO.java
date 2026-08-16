package com.steve0v0.home.vo;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 动态列表展示对象
 */
@Data
public class MomentListVO {
    private Long id;
    private String content;
    private String mediaType;
    private String mediaUrl;
    private String location;
    private LocalDateTime createdAt;
    /** 图片 URL 列表，按 sort 排序 */
    private List<String> images;
}
