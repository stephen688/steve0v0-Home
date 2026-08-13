package com.steve0v0.home.controller;

import com.steve0v0.home.common.result.Result;
import com.steve0v0.home.dto.AboutProfileUpdateDTO;
import com.steve0v0.home.service.AboutProfileService;
import com.steve0v0.home.vo.AboutProfileVO;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 管理端关于页个人资料接口
 * 需要 JWT 鉴权
 */
@RestController
@RequestMapping("/api/admin/about/profile")
@RequiredArgsConstructor
public class AdminAboutProfileController {

    private final AboutProfileService aboutProfileService;

    /**
     * 查询当前个人资料
     */
    @GetMapping
    public Result<AboutProfileVO> profile() {
        return Result.success(aboutProfileService.getProfile());
    }

    /**
     * 覆盖或清空个人资料
     */
    @PutMapping
    public Result<Void> update(@Valid @RequestBody AboutProfileUpdateDTO dto) {
        aboutProfileService.updateProfile(dto);
        return Result.success();
    }
}
