package com.steve0v0.home.controller;

import com.steve0v0.home.common.result.Result;
import com.steve0v0.home.dto.PomodoroCompleteDTO;
import com.steve0v0.home.service.PomodoroService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 番茄钟接口
 * POST /api/home/pomodoro 需要 JWT 鉴权
 * 未登录用户仅使用前端本地倒计时，不调用此接口
 */
@RestController
@RequestMapping("/api/home/pomodoro")
@RequiredArgsConstructor
public class HomePomodoroController {

    private final PomodoroService pomodoroService;

    /**
     * 保存已完成的番茄钟记录
     * 登录用户完成倒计时后提交
     * 服务端自动生成 completed_at，不接收客户端时间
     */
    @PostMapping
    public Result<Long> complete(@Valid @RequestBody PomodoroCompleteDTO dto) {
        return Result.success(pomodoroService.completePomodoro(dto));
    }
}
