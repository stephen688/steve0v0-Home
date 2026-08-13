package com.steve0v0.home.vo;

import lombok.Data;

import java.util.List;

/**
 * 关于页 GitHub 项目展示对象
 */
@Data
public class AboutProjectVO {
    private Long id;
    private String name;
    private String description;
    private String githubUrl;
    private List<String> techTags;
    private Integer sort;
}
