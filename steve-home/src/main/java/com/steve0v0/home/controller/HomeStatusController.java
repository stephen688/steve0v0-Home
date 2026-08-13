package com.steve0v0.home.controller;

import com.steve0v0.home.common.result.Result;
import com.steve0v0.home.service.StatusService;
import com.steve0v0.home.vo.StatusVO;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 个人状态公开接口
 * 访客无需登录即可查看当前状态
 */
@RestController
@RequestMapping("/api/home/status")
@RequiredArgsConstructor
public class HomeStatusController {

    private final StatusService statusService;

    /**
     * 查询当前个人状态
     * 没有记录时返回默认状态（online）
     */
    @GetMapping
    public Result<StatusVO> status() {
        return Result.success(statusService.getStatus());
    }
}
