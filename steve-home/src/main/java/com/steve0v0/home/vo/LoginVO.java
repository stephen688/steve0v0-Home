package com.steve0v0.home.vo;

import lombok.Data;
import lombok.Builder;

@Data
@Builder
public class LoginVO {
    private String token;
    private long expireAt;
}
