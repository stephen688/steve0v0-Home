package com.steve0v0.home.controller;

import com.steve0v0.home.common.result.Result;
import com.steve0v0.home.dto.StatusUpdateDTO;
import com.steve0v0.home.service.StatusService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 管理端个人状态接口
 * 需要 JWT 鉴权
 */
@RestController
@RequestMapping("/api/admin/status")
@RequiredArgsConstructor
public class AdminStatusController {

    private final StatusService statusService;

    /**
     * 修改个人状态
     * 支持修改 state、currentTask、mood
     * 所有字段可选，只更新传入的字段
     */
    @PutMapping
    public Result<Void> update(@Valid @RequestBody StatusUpdateDTO dto) {
        statusService.updateStatus(dto);
        return Result.success();
    }
}
