package com.steve0v0.home.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.steve0v0.home.entity.Pomodoro;
import org.apache.ibatis.annotations.Mapper;

/**
 * 番茄钟完成记录数据访问层
 */
@Mapper
public interface PomodoroMapper extends BaseMapper<Pomodoro> {
}
