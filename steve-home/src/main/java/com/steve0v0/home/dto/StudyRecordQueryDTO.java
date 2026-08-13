package com.steve0v0.home.dto;

import com.steve0v0.home.common.pagination.PageRequest;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDate;

/**
 * 学习记录列表查询参数
 * 支持按日期范围筛选
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class StudyRecordQueryDTO extends PageRequest {
    /** 开始日期，不传则不限制 */
    private LocalDate startDate;
    /** 结束日期，不传则不限制 */
    private LocalDate endDate;
}
