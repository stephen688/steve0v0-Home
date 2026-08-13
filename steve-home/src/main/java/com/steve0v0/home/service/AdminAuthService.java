package com.steve0v0.home.service;

import com.steve0v0.home.common.exception.BusinessException;
import com.steve0v0.home.common.result.ResultCode;
import com.steve0v0.home.dto.ChangeSecretDTO;
import com.steve0v0.home.entity.Credential;
import com.steve0v0.home.mapper.CredentialMapper;
import com.steve0v0.home.security.JwtUtil;
import com.steve0v0.home.security.RateLimitService;
import com.steve0v0.home.vo.LoginVO;
import jakarta.annotation.PostConstruct;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;

@Slf4j
@Service
@RequiredArgsConstructor
public class AdminAuthService {

    private final CredentialMapper credentialMapper;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;
    private final RateLimitService rateLimitService;

    @Value("${ADMIN_INITIAL_SECRET:}")
    private String initialSecret;

    /**
     * 首次启动初始化暗号
     * 检查 credential 表是否有记录，若无则从环境变量 ADMIN_INITIAL_SECRET 读取并初始化
     */
    @PostConstruct
    public void initializeCredential() {
        Long count = credentialMapper.selectCount(null);
        if (count == 0) {
            if (!StringUtils.hasText(initialSecret)) {
                log.error("首次启动但未设置 ADMIN_INITIAL_SECRET 环境变量，无法初始化暗号");
                throw new IllegalStateException("首次启动需要设置 ADMIN_INITIAL_SECRET 环境变量");
            }
            Credential credential = new Credential();
            credential.setSecretHash(passwordEncoder.encode(initialSecret));
            credential.setTokenVersion(0);
            credential.setUpdatedAt(LocalDateTime.now());
            credentialMapper.insert(credential);
            log.info("首次启动：已从 ADMIN_INITIAL_SECRET 初始化暗号，请尽快修改");
        } else {
            log.info("凭据已存在，跳过初始化");
        }
    }

    /**
     * 暗号登录
     */
    public LoginVO login(String secret, HttpServletRequest request) {
        String ip = getClientIp(request);

        // 限流检查
        if (rateLimitService.isBlocked(ip)) {
            log.warn("登录限流 | IP: {}", ip);
            throw new BusinessException(ResultCode.TOO_MANY_REQUESTS);
        }

        // 查询凭据
        Credential credential = credentialMapper.selectById(1L);
        if (credential == null) {
            log.error("凭据记录不存在");
            throw new BusinessException(ResultCode.INTERNAL_ERROR, "系统未初始化");
        }

        // 校验暗号
        if (!passwordEncoder.matches(secret, credential.getSecretHash())) {
            rateLimitService.recordFailure(ip);
            log.warn("暗号错误 | IP: {}", ip);
            throw new BusinessException(ResultCode.UNAUTHORIZED, "暗号错误");
        }

        // 登录成功
        rateLimitService.recordSuccess(ip);
        log.info("登录成功 | IP: {}", ip);

        String token = jwtUtil.generateToken(credential.getId(), credential.getTokenVersion());
        long expireAt = System.currentTimeMillis() + jwtUtil.getExpireSeconds() * 1000;

        return LoginVO.builder()
                .token(token)
                .expireAt(expireAt)
                .build();
    }

    /**
     * 修改暗号
     */
    public LoginVO changeSecret(ChangeSecretDTO dto, HttpServletRequest request) {
        Credential credential = credentialMapper.selectById(1L);
        if (credential == null) {
            throw new BusinessException(ResultCode.INTERNAL_ERROR, "系统未初始化");
        }

        // 校验旧暗号
        if (!passwordEncoder.matches(dto.getOldSecret(), credential.getSecretHash())) {
            log.warn("修改暗号失败：旧暗号错误 | IP: {}", getClientIp(request));
            throw new BusinessException(ResultCode.UNAUTHORIZED, "旧暗号错误");
        }

        // 更新暗号和 tokenVersion
        credential.setSecretHash(passwordEncoder.encode(dto.getNewSecret()));
        credential.setTokenVersion(credential.getTokenVersion() + 1);
        credential.setUpdatedAt(LocalDateTime.now());
        credentialMapper.updateById(credential);

        log.info("暗号修改成功 | IP: {}", getClientIp(request));

        // 返回新 Token
        String token = jwtUtil.generateToken(credential.getId(), credential.getTokenVersion());
        long expireAt = System.currentTimeMillis() + jwtUtil.getExpireSeconds() * 1000;

        return LoginVO.builder()
                .token(token)
                .expireAt(expireAt)
                .build();
    }

    private String getClientIp(HttpServletRequest request) {
        String ip = request.getHeader("X-Forwarded-For");
        if (!StringUtils.hasText(ip) || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("X-Real-IP");
        }
        if (!StringUtils.hasText(ip) || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getRemoteAddr();
        }
        return ip != null ? ip.split(",")[0].trim() : "unknown";
    }
}
