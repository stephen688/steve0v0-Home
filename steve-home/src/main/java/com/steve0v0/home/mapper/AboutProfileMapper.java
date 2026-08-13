package com.steve0v0.home.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.steve0v0.home.entity.AboutProfile;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;

/**
 * 关于页个人资料数据访问层
 */
@Mapper
public interface AboutProfileMapper extends BaseMapper<AboutProfile> {

    /**
     * 固定更新 id=1 的单例资料
     */
    @Insert("""
            INSERT INTO about_profile (id, name, avatar_url, created_at, updated_at)
            VALUES (#{id}, #{name}, #{avatarUrl}, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP)
            ON DUPLICATE KEY UPDATE
                name = #{name},
                avatar_url = #{avatarUrl},
                updated_at = CURRENT_TIMESTAMP
            """)
    int upsert(AboutProfile profile);
}
