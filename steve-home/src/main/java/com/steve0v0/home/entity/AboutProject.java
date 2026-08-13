package com.steve0v0.home.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.extension.handlers.JacksonTypeHandler;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 关于页 GitHub 项目实体
 */
@Data
@TableName(value = "about_project", autoResultMap = true)
public class AboutProject {
    @TableId(type = IdType.AUTO)
    private Long id;
    private String name;
    private String description;
    private String githubUrl;
    @TableField(typeHandler = JacksonTypeHandler.class)
    private List<String> techTags;
    private Integer sort;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
