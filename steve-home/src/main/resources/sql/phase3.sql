-- ============================================
-- steve0v0 小屋 · 第三阶段数据库脚本
-- 版本: v1.0  日期: 2026-08-11
-- 说明: 在第一阶段和第二阶段脚本执行完成后执行
-- ============================================

USE steve_home;

-- ============================================
-- 关于页单例个人资料表
-- ============================================
CREATE TABLE IF NOT EXISTS about_profile (
    id         BIGINT       NOT NULL COMMENT '固定为1',
    name       VARCHAR(100) NOT NULL DEFAULT '' COMMENT '个人姓名',
    avatar_url VARCHAR(500) NOT NULL DEFAULT '' COMMENT '外部HTTPS头像地址',
    created_at DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='关于页单例个人资料';

-- ============================================
-- 关于页 GitHub 项目表
-- ============================================
CREATE TABLE IF NOT EXISTS about_project (
    id          BIGINT       NOT NULL AUTO_INCREMENT COMMENT '主键',
    name        VARCHAR(100) NOT NULL COMMENT '项目名称',
    description VARCHAR(500)          DEFAULT NULL COMMENT '项目简介',
    github_url  VARCHAR(500) NOT NULL COMMENT 'GitHub仓库HTTPS地址',
    tech_tags   JSON         NOT NULL COMMENT '技术标签JSON数组',
    sort        INT          NOT NULL DEFAULT 0 COMMENT '展示排序，数值越小越靠前',
    created_at  DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at  DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (id),
    INDEX idx_about_project_sort (sort, id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='关于页GitHub项目';
