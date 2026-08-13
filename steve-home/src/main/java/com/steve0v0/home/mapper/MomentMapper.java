package com.steve0v0.home.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.steve0v0.home.entity.Moment;
import org.apache.ibatis.annotations.Mapper;

/**
 * 动态数据访问层
 */
@Mapper
public interface MomentMapper extends BaseMapper<Moment> {
}
