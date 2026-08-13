package com.steve0v0.home.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.steve0v0.home.entity.StudyRecord;
import org.apache.ibatis.annotations.Mapper;

/**
 * 学习记录数据访问层
 */
@Mapper
public interface StudyRecordMapper extends BaseMapper<StudyRecord> {
}
