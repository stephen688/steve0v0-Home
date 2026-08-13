package com.steve0v0.home.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.time.LocalDate;

/**
 * 修改学习记录请求
 * 所有字段必填，采用全量更新
 */
@Data
public class StudyRecordUpdateDTO {
    @NotNull(message = "学习日期不能为空")
    private LocalDate recordDate;

    @NotBlank(message = "学习主题不能为空")
    @Size(max = 100, message = "学习主题最长100个字符")
    private String subject;

    @NotBlank(message = "学习内容不能为空")
    private String content;

    @NotNull(message = "学习时长不能为空")
    @Min(value = 1, message = "学习时长必须大于0")
    private Integer duration;
}
