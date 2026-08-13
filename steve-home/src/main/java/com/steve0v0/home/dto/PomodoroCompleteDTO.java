package com.steve0v0.home.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * 番茄钟完成请求
 * 登录用户完成倒计时后提交
 */
@Data
public class PomodoroCompleteDTO {
    /** 关联任务名称，可选 */
    @Size(max = 200, message = "任务名称最长200个字符")
    private String taskName;

    /** 倒计时设定时长，单位分钟，1-480 */
    @NotNull(message = "专注时长不能为空")
    @Min(value = 1, message = "专注时长最少1分钟")
    @Max(value = 480, message = "专注时长最多480分钟")
    private Integer duration;
}
