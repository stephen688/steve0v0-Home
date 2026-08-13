package com.steve0v0.home.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;

@Slf4j
@Component
public class JwtUtil {

    @Value("${steve.jwt.secret}")
    private String secret;

    @Value("${steve.jwt.expire:604800}")
    private long expireSeconds;

    private SecretKey key;

    @PostConstruct
    public void init() {
        byte[] keyBytes = secret.getBytes(StandardCharsets.UTF_8);
        if (keyBytes.length < 32) {
            throw new IllegalStateException("JWT密钥长度不足32字节，当前长度: " + keyBytes.length + "，请通过环境变量 JWT_SECRET 设置足够长的密钥");
        }
        this.key = Keys.hmacShaKeyFor(keyBytes);
        log.info("JWT密钥校验通过，长度: {} 字节", keyBytes.length);
    }

    public String generateToken(Long credentialId, Integer tokenVersion) {
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + expireSeconds * 1000);
        return Jwts.builder()
                .subject(String.valueOf(credentialId))
                .claim("tv", tokenVersion)
                .issuedAt(now)
                .expiration(expiryDate)
                .signWith(key)
                .compact();
    }

    public Claims parseToken(String token) {
        return Jwts.parser()
                .verifyWith(key)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    public Long getCredentialId(String token) {
        return Long.parseLong(parseToken(token).getSubject());
    }

    public Integer getTokenVersion(String token) {
        return parseToken(token).get("tv", Integer.class);
    }

    public long getExpireSeconds() {
        return expireSeconds;
    }
}
