package com.steve0v0.home.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.steve0v0.home.entity.Course;
import org.apache.ibatis.annotations.Mapper;

/**
 * 课程数据访问层
 */
@Mapper
public interface CourseMapper extends BaseMapper<Course> {
}
