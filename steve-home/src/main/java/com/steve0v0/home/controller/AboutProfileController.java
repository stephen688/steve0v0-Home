package com.steve0v0.home.controller;

import com.steve0v0.home.common.result.Result;
import com.steve0v0.home.service.AboutProfileService;
import com.steve0v0.home.vo.AboutProfileVO;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 公开关于页个人资料接口
 */
@RestController
@RequestMapping("/api/about/profile")
@RequiredArgsConstructor
public class AboutProfileController {

    private final AboutProfileService aboutProfileService;

    /**
     * 查询个人资料
     */
    @GetMapping
    public Result<AboutProfileVO> profile() {
        return Result.success(aboutProfileService.getProfile());
    }
}
