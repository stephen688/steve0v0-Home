package com.steve0v0.home.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.steve0v0.home.entity.Article;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Update;

/**
 * 文章数据访问层
 */
@Mapper
public interface ArticleMapper extends BaseMapper<Article> {

    /**
     * 原子递增阅读数
     * 直接在 SQL 层 +1，避免查出后 Java 中修改再保存的并发问题
     */
    @Update("UPDATE article SET view_count = view_count + 1 WHERE id = #{id}")
    int incrementViewCount(Long id);
}
