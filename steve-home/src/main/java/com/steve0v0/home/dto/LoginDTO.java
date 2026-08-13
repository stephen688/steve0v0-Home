package com.steve0v0.home.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class LoginDTO {
    @NotBlank(message = "暗号不能为空")
    private String secret;
}
