package com.steve0v0.home.security;

import com.steve0v0.home.common.constant.Constants;
import com.steve0v0.home.entity.Credential;
import com.steve0v0.home.mapper.CredentialMapper;
import io.jsonwebtoken.Claims;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtUtil jwtUtil;
    private final CredentialMapper credentialMapper;

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        String authHeader = request.getHeader(Constants.AUTHORIZATION_HEADER);
        if (StringUtils.hasText(authHeader) && authHeader.startsWith(Constants.BEARER_PREFIX)) {
            String token = authHeader.substring(Constants.BEARER_PREFIX.length());
            try {
                Claims claims = jwtUtil.parseToken(token);
                Long credentialId = Long.parseLong(claims.getSubject());
                Integer tokenVersion = claims.get("tv", Integer.class);

                // 校验数据库中的 tokenVersion 是否匹配
                Credential credential = credentialMapper.selectById(credentialId);
                if (credential != null && credential.getTokenVersion().equals(tokenVersion)) {
                    UsernamePasswordAuthenticationToken authentication =
                            new UsernamePasswordAuthenticationToken(credentialId, null,
                                    List.of(new SimpleGrantedAuthority("ROLE_ADMIN")));
                    SecurityContextHolder.getContext().setAuthentication(authentication);
                }
            } catch (Exception e) {
                log.debug("JWT认证失败: {}", e.getMessage());
            }
        }
        filterChain.doFilter(request, response);
    }
}
