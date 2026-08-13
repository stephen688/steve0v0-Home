package com.steve0v0.home.controller;

import com.steve0v0.home.common.result.Result;
import com.steve0v0.home.dto.AboutProjectCreateDTO;
import com.steve0v0.home.dto.AboutProjectUpdateDTO;
import com.steve0v0.home.service.AboutProjectService;
import com.steve0v0.home.vo.AboutProjectVO;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 管理端关于页 GitHub 项目接口
 * 需要 JWT 鉴权
 */
@RestController
@RequestMapping("/api/admin/about/projects")
@RequiredArgsConstructor
public class AdminAboutProjectController {

    private final AboutProjectService aboutProjectService;

    /**
     * 项目列表
     */
    @GetMapping
    public Result<List<AboutProjectVO>> list() {
        return Result.success(aboutProjectService.getProjectList());
    }

    /**
     * 项目详情
     */
    @GetMapping("/{id}")
    public Result<AboutProjectVO> detail(@PathVariable Long id) {
        return Result.success(aboutProjectService.getProjectById(id));
    }

    /**
     * 新增项目
     */
    @PostMapping
    public Result<Long> create(@Valid @RequestBody AboutProjectCreateDTO dto) {
        return Result.success(aboutProjectService.createProject(dto));
    }

    /**
     * 修改项目
     */
    @PutMapping("/{id}")
    public Result<Void> update(@PathVariable Long id, @Valid @RequestBody AboutProjectUpdateDTO dto) {
        aboutProjectService.updateProject(id, dto);
        return Result.success();
    }

    /**
     * 删除项目
     */
    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        aboutProjectService.deleteProject(id);
        return Result.success();
    }
}
