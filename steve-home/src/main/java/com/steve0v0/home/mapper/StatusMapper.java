package com.steve0v0.home.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.steve0v0.home.entity.Status;
import org.apache.ibatis.annotations.Mapper;

/**
 * 个人状态数据访问层
 */
@Mapper
public interface StatusMapper extends BaseMapper<Status> {
}
