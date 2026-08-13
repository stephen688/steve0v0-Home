package com.steve0v0.home.dto;

import com.steve0v0.home.common.pagination.PageRequest;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 课程列表查询参数
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class CourseQueryDTO extends PageRequest {
}
