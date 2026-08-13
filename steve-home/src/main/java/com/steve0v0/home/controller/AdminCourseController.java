package com.steve0v0.home.controller;

import com.steve0v0.home.common.pagination.PageResult;
import com.steve0v0.home.common.result.Result;
import com.steve0v0.home.dto.CourseCreateDTO;
import com.steve0v0.home.dto.CourseQueryDTO;
import com.steve0v0.home.dto.CourseUpdateDTO;
import com.steve0v0.home.service.CourseService;
import com.steve0v0.home.vo.CourseVO;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

/**
 * 管理端课程接口
 * 需要 JWT 鉴权
 */
@RestController
@RequestMapping("/api/admin/courses")
@RequiredArgsConstructor
public class AdminCourseController {

    private final CourseService courseService;

    /**
     * 课程列表
     */
    @GetMapping
    public Result<PageResult<CourseVO>> list(CourseQueryDTO queryDTO) {
        return Result.success(courseService.getAdminCourseList(queryDTO));
    }

    /**
     * 课程详情
     */
    @GetMapping("/{id}")
    public Result<CourseVO> detail(@PathVariable Long id) {
        return Result.success(courseService.getAdminCourseById(id));
    }

    /**
     * 新增课程
     * 支持仅当天和每周重复两种模式
     */
    @PostMapping
    public Result<Long> create(@Valid @RequestBody CourseCreateDTO dto) {
        return Result.success(courseService.createCourse(dto));
    }

    /**
     * 修改课程
     */
    @PutMapping("/{id}")
    public Result<Void> update(@PathVariable Long id, @Valid @RequestBody CourseUpdateDTO dto) {
        courseService.updateCourse(id, dto);
        return Result.success();
    }

    /**
     * 删除课程
     */
    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        courseService.deleteCourse(id);
        return Result.success();
    }
}
