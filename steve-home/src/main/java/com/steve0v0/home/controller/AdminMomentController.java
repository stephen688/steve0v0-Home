package com.steve0v0.home.controller;

import com.steve0v0.home.common.pagination.PageResult;
import com.steve0v0.home.common.pagination.PageRequest;
import com.steve0v0.home.common.result.Result;
import com.steve0v0.home.dto.MomentCreateDTO;
import com.steve0v0.home.service.MomentService;
import com.steve0v0.home.vo.MomentListVO;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

/**
 * 管理端动态接口
 * 需要 JWT 鉴权，可管理生活动态
 */
@RestController
@RequestMapping("/api/admin/moments")
@RequiredArgsConstructor
public class AdminMomentController {

    private final MomentService momentService;

    /**
     * 管理端动态列表
     */
    @GetMapping
    public Result<PageResult<MomentListVO>> list(@ModelAttribute PageRequest pageRequest) {
        return Result.success(momentService.getAdminMomentList(pageRequest.getPage(), pageRequest.getSize()));
    }

    /**
     * 新建动态
     * 支持 text（纯文字）和 image（多图）两种类型
     */
    @PostMapping
    public Result<Long> create(@Valid @RequestBody MomentCreateDTO dto) {
        return Result.success(momentService.createMoment(dto));
    }

    /**
     * 删除动态（物理删除，关联图片自动清除）
     */
    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        momentService.deleteMoment(id);
        return Result.success();
    }
}
