package com.steve0v0.home.service;

import com.steve0v0.home.common.exception.BusinessException;
import com.steve0v0.home.common.result.ResultCode;
import com.steve0v0.home.dto.AboutProfileUpdateDTO;
import com.steve0v0.home.entity.AboutProfile;
import com.steve0v0.home.mapper.AboutProfileMapper;
import com.steve0v0.home.vo.AboutProfileVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.net.URI;

/**
 * 关于页个人资料服务
 * 单站点模式固定使用 id=1 的记录
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class AboutProfileService {

    private static final long PROFILE_ID = 1L;

    private final AboutProfileMapper aboutProfileMapper;

    /**
     * 查询个人资料
     * 没有记录时返回空资料
     */
    public AboutProfileVO getProfile() {
        AboutProfile profile = aboutProfileMapper.selectById(PROFILE_ID);
        AboutProfileVO vo = new AboutProfileVO();
        if (profile == null) {
            vo.setName("");
            vo.setAvatarUrl("");
        } else {
            vo.setName(profile.getName() != null ? profile.getName() : "");
            vo.setAvatarUrl(profile.getAvatarUrl() != null ? profile.getAvatarUrl() : "");
        }
        return vo;
    }

    /**
     * 覆盖个人资料
     * 使用数据库原子 upsert 逻辑
     */
    public void updateProfile(AboutProfileUpdateDTO dto) {
        if (!dto.getAvatarUrl().isEmpty() && !isHttpsUrl(dto.getAvatarUrl())) {
            throw new BusinessException(ResultCode.BAD_REQUEST, "头像地址必须是有效的 HTTPS URL");
        }

        AboutProfile profile = new AboutProfile();
        profile.setId(PROFILE_ID);
        profile.setName(dto.getName());
        profile.setAvatarUrl(dto.getAvatarUrl());
        aboutProfileMapper.upsert(profile);
        log.info("修改关于页个人资料成功");
    }

    private boolean isHttpsUrl(String value) {
        try {
            URI uri = URI.create(value);
            return "https".equalsIgnoreCase(uri.getScheme()) && uri.getHost() != null;
        } catch (IllegalArgumentException e) {
            return false;
        }
    }
}
