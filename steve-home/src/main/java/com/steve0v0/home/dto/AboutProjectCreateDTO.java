package com.steve0v0.home.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

/**
 * 新增 GitHub 项目请求
 */
@Data
public class AboutProjectCreateDTO {
    @NotBlank(message = "项目名称不能为空")
    @Size(max = 100, message = "项目名称最长100个字符")
    private String name;

    @Size(max = 500, message = "项目简介最长500个字符")
    private String description;

    @NotBlank(message = "GitHub地址不能为空")
    @Size(max = 500, message = "GitHub地址最长500个字符")
    private String githubUrl;

    @Size(max = 10, message = "技术标签最多10个")
    private List<@NotBlank(message = "技术标签不能为空") @Size(max = 50, message = "技术标签最长50个字符") String> techTags = new ArrayList<>();

    @Min(value = 0, message = "排序值不能小于0")
    private Integer sort = 0;
}
