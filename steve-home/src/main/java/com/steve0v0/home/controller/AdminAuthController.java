package com.steve0v0.home.controller;

import com.steve0v0.home.common.result.Result;
import com.steve0v0.home.dto.ChangeSecretDTO;
import com.steve0v0.home.dto.LoginDTO;
import com.steve0v0.home.service.AdminAuthService;
import com.steve0v0.home.vo.LoginVO;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

/**
 * 管理员认证控制器
 * 提供暗号登录和修改暗号两个接口
 */
@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
public class AdminAuthController {

    private final AdminAuthService adminAuthService;

    /**
     * 暗号登录
     * 校验暗号通过后返回 JWT，前端将 JWT 存入本地并在后续管理请求中携带
     */
    @PostMapping("/auth")
    public Result<LoginVO> login(@Valid @RequestBody LoginDTO dto, HttpServletRequest request) {
        return Result.success(adminAuthService.login(dto.getSecret(), request));
    }

    /**
     * 修改暗号
     * 校验旧暗号后更新为新暗号，tokenVersion +1 使旧 Token 立即失效
     * 返回包含新 tokenVersion 的 JWT
     */
    @PostMapping("/auth/change-secret")
    public Result<LoginVO> changeSecret(@Valid @RequestBody ChangeSecretDTO dto, HttpServletRequest request) {
        return Result.success(adminAuthService.changeSecret(dto, request));
    }
}
