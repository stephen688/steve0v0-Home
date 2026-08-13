package com.steve0v0.home.controller;

import com.steve0v0.home.common.pagination.PageResult;
import com.steve0v0.home.common.pagination.PageRequest;
import com.steve0v0.home.common.result.Result;
import com.steve0v0.home.service.MomentService;
import com.steve0v0.home.vo.MomentListVO;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

/**
 * 公开动态接口
 * 访客无需登录即可查看生活动态流
 */
@RestController
@RequestMapping("/api/moments")
@RequiredArgsConstructor
public class MomentController {

    private final MomentService momentService;

    /**
     * 动态列表（分页）
     * order 支持 latest（默认）和 earliest。
     */
    @GetMapping
    public Result<PageResult<MomentListVO>> list(
            @ModelAttribute PageRequest pageRequest,
            @RequestParam(defaultValue = "latest") String order) {
        return Result.success(momentService.getMomentList(pageRequest.getPage(), pageRequest.getSize(), order));
    }
}
