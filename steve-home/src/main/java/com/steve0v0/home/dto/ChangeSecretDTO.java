package com.steve0v0.home.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class ChangeSecretDTO {
    @NotBlank(message = "旧暗号不能为空")
    private String oldSecret;

    @NotBlank(message = "新暗号不能为空")
    @Size(min = 6, max = 50, message = "新暗号长度需在6-50个字符之间")
    private String newSecret;
}
