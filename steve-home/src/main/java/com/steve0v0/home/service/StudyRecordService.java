package com.steve0v0.home.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.steve0v0.home.common.exception.BusinessException;
import com.steve0v0.home.common.pagination.PageResult;
import com.steve0v0.home.common.result.ResultCode;
import com.steve0v0.home.dto.StudyRecordCreateDTO;
import com.steve0v0.home.dto.StudyRecordQueryDTO;
import com.steve0v0.home.dto.StudyRecordUpdateDTO;
import com.steve0v0.home.entity.StudyRecord;
import com.steve0v0.home.mapper.StudyRecordMapper;
import com.steve0v0.home.vo.StudyRecordDetailVO;
import com.steve0v0.home.vo.StudyRecordListVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 学习记录服务
 * 处理学习记录的增删改查，支持按日期范围筛选
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class StudyRecordService {

    private final StudyRecordMapper studyRecordMapper;

    /**
     * 将实体转换为列表 VO
     */
    private StudyRecordListVO toListVO(StudyRecord record) {
        StudyRecordListVO vo = new StudyRecordListVO();
        vo.setId(record.getId());
        vo.setRecordDate(record.getRecordDate());
        vo.setSubject(record.getSubject());
        vo.setDuration(record.getDuration());
        vo.setCreatedAt(record.getCreatedAt());
        return vo;
    }

    /**
     * 将实体转换为详情 VO
     */
    private StudyRecordDetailVO toDetailVO(StudyRecord record) {
        StudyRecordDetailVO vo = new StudyRecordDetailVO();
        vo.setId(record.getId());
        vo.setRecordDate(record.getRecordDate());
        vo.setSubject(record.getSubject());
        vo.setContent(record.getContent());
        vo.setDuration(record.getDuration());
        vo.setCreatedAt(record.getCreatedAt());
        vo.setUpdatedAt(record.getUpdatedAt());
        return vo;
    }

    /**
     * 管理端学习记录列表查询
     * 支持按日期范围筛选，按 record_date DESC, id DESC 排序
     */
    public PageResult<StudyRecordListVO> getAdminStudyRecordList(StudyRecordQueryDTO queryDTO) {
        Page<StudyRecord> page = new Page<>(queryDTO.getPage(), queryDTO.getSize());

        LambdaQueryWrapper<StudyRecord> wrapper = new LambdaQueryWrapper<>();
        if (queryDTO.getStartDate() != null) {
            wrapper.ge(StudyRecord::getRecordDate, queryDTO.getStartDate());
        }
        if (queryDTO.getEndDate() != null) {
            wrapper.le(StudyRecord::getRecordDate, queryDTO.getEndDate());
        }
        wrapper.orderByDesc(StudyRecord::getRecordDate)
               .orderByDesc(StudyRecord::getId);

        IPage<StudyRecord> result = studyRecordMapper.selectPage(page, wrapper);
        List<StudyRecordListVO> list = result.getRecords().stream()
                .map(this::toListVO)
                .collect(Collectors.toList());
        return new PageResult<>(queryDTO.getPage(), queryDTO.getSize(), result.getTotal(), list);
    }

    /**
     * 管理端学习记录详情查询
     */
    public StudyRecordDetailVO getAdminStudyRecordById(Long id) {
        StudyRecord record = studyRecordMapper.selectById(id);
        if (record == null) {
            throw new BusinessException(ResultCode.NOT_FOUND);
        }
        return toDetailVO(record);
    }

    /**
     * 新增学习记录
     */
    public Long createStudyRecord(StudyRecordCreateDTO dto) {
        StudyRecord record = new StudyRecord();
        record.setRecordDate(dto.getRecordDate());
        record.setSubject(dto.getSubject());
        record.setContent(dto.getContent());
        record.setDuration(dto.getDuration());
        studyRecordMapper.insert(record);
        log.info("新建学习记录成功 | id={} | date={} | subject={}", record.getId(), record.getRecordDate(), record.getSubject());
        return record.getId();
    }

    /**
     * 修改学习记录
     */
    public void updateStudyRecord(Long id, StudyRecordUpdateDTO dto) {
        StudyRecord record = studyRecordMapper.selectById(id);
        if (record == null) {
            throw new BusinessException(ResultCode.NOT_FOUND);
        }
        record.setRecordDate(dto.getRecordDate());
        record.setSubject(dto.getSubject());
        record.setContent(dto.getContent());
        record.setDuration(dto.getDuration());
        studyRecordMapper.updateById(record);
        log.info("修改学习记录成功 | id={}", id);
    }

    /**
     * 删除学习记录（物理删除）
     */
    public void deleteStudyRecord(Long id) {
        StudyRecord record = studyRecordMapper.selectById(id);
        if (record == null) {
            throw new BusinessException(ResultCode.NOT_FOUND);
        }
        studyRecordMapper.deleteById(id);
        log.info("删除学习记录成功 | id={}", id);
    }

    /**
     * 查询指定日期范围内的学习记录（供日历和统计使用）
     */
    public List<StudyRecord> getByDateRange(LocalDate startDate, LocalDate endDate) {
        LambdaQueryWrapper<StudyRecord> wrapper = new LambdaQueryWrapper<>();
        wrapper.ge(StudyRecord::getRecordDate, startDate)
               .le(StudyRecord::getRecordDate, endDate);
        return studyRecordMapper.selectList(wrapper);
    }
}
