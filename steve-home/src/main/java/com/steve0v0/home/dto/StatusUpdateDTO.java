package com.steve0v0.home.dto;

import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * 修改个人状态请求
 * 所有字段可选，只更新传入的字段
 */
@Data
public class StatusUpdateDTO {
    /** 状态：online/studying/busy/rest */
    @Size(max = 20, message = "状态最长20个字符")
    private String state;

    @Size(max = 200, message = "当前任务最长200个字符")
    private String currentTask;

    @Size(max = 200, message = "心情签名最长200个字符")
    private String mood;
}
