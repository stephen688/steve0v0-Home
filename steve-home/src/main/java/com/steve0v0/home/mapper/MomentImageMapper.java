package com.steve0v0.home.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.steve0v0.home.entity.MomentImage;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * 动态图片数据访问层
 */
@Mapper
public interface MomentImageMapper extends BaseMapper<MomentImage> {

    /**
     * 批量查询多条动态的图片，避免 N+1 问题
     * 一次查出所有 moment_id 对应的图片，在 Java 中分组组装
     */
    @Select("<script>" +
            "SELECT * FROM moment_image WHERE moment_id IN " +
            "<foreach collection='momentIds' item='id' open='(' separator=',' close=')'>" +
            "#{id}" +
            "</foreach>" +
            " ORDER BY moment_id, sort" +
            "</script>")
    List<MomentImage> selectByMomentIds(List<Long> momentIds);
}
