package com.steve0v0.home.controller;

import com.steve0v0.home.common.pagination.PageResult;
import com.steve0v0.home.common.result.Result;
import com.steve0v0.home.dto.StudyRecordCreateDTO;
import com.steve0v0.home.dto.StudyRecordQueryDTO;
import com.steve0v0.home.dto.StudyRecordUpdateDTO;
import com.steve0v0.home.service.StudyRecordService;
import com.steve0v0.home.vo.StudyRecordDetailVO;
import com.steve0v0.home.vo.StudyRecordListVO;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

/**
 * 管理端学习记录接口
 * 需要 JWT 鉴权
 */
@RestController
@RequestMapping("/api/admin/study-records")
@RequiredArgsConstructor
public class AdminStudyRecordController {

    private final StudyRecordService studyRecordService;

    /**
     * 学习记录列表（支持日期范围筛选）
     */
    @GetMapping
    public Result<PageResult<StudyRecordListVO>> list(StudyRecordQueryDTO queryDTO) {
        return Result.success(studyRecordService.getAdminStudyRecordList(queryDTO));
    }

    /**
     * 学习记录详情
     */
    @GetMapping("/{id}")
    public Result<StudyRecordDetailVO> detail(@PathVariable Long id) {
        return Result.success(studyRecordService.getAdminStudyRecordById(id));
    }

    /**
     * 新增学习记录
     */
    @PostMapping
    public Result<Long> create(@Valid @RequestBody StudyRecordCreateDTO dto) {
        return Result.success(studyRecordService.createStudyRecord(dto));
    }

    /**
     * 修改学习记录
     */
    @PutMapping("/{id}")
    public Result<Void> update(@PathVariable Long id, @Valid @RequestBody StudyRecordUpdateDTO dto) {
        studyRecordService.updateStudyRecord(id, dto);
        return Result.success();
    }

    /**
     * 删除学习记录
     */
    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        studyRecordService.deleteStudyRecord(id);
        return Result.success();
    }
}
