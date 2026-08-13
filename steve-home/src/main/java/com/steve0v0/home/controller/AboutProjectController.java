package com.steve0v0.home.controller;

import com.steve0v0.home.common.result.Result;
import com.steve0v0.home.service.AboutProjectService;
import com.steve0v0.home.vo.AboutProjectVO;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 公开关于页 GitHub 项目接口
 */
@RestController
@RequestMapping("/api/about/projects")
@RequiredArgsConstructor
public class AboutProjectController {

    private final AboutProjectService aboutProjectService;

    /**
     * 查询公开项目列表
     */
    @GetMapping
    public Result<List<AboutProjectVO>> list() {
        return Result.success(aboutProjectService.getProjectList());
    }
}
