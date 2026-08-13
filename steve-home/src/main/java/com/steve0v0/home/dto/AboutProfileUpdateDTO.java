package com.steve0v0.home.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * 修改关于页个人资料请求
 * 使用完整覆盖语义，空字符串表示清空字段
 */
@Data
public class AboutProfileUpdateDTO {
    @NotNull(message = "姓名不能为空")
    @Size(max = 100, message = "姓名最长100个字符")
    private String name;

    @NotNull(message = "头像地址不能为空")
    @Size(max = 500, message = "头像地址最长500个字符")
    private String avatarUrl;
}
