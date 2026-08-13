package com.steve0v0.home.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.steve0v0.home.entity.AboutProject;
import org.apache.ibatis.annotations.Mapper;

/**
 * 关于页 GitHub 项目数据访问层
 */
@Mapper
public interface AboutProjectMapper extends BaseMapper<AboutProject> {
}
