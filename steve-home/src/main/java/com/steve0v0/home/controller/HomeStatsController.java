package com.steve0v0.home.controller;

import com.steve0v0.home.common.result.Result;
import com.steve0v0.home.service.StatsService;
import com.steve0v0.home.vo.StatsVO;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 学习数据统计公开接口
 * 访客无需登录即可查看统计数据
 */
@RestController
@RequestMapping("/api/home/stats")
@RequiredArgsConstructor
public class HomeStatsController {

    private final StatsService statsService;

    /**
     * 查询学习数据统计
     * 包含今日数据、本周数据、连续学习天数和热力图
     */
    @GetMapping
    public Result<StatsVO> stats() {
        return Result.success(statsService.getStats());
    }
}
