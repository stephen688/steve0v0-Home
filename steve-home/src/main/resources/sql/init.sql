-- ============================================
-- steve0v0 小屋 · 数据库初始化脚本
-- 版本: v1.0  日期: 2026-08-11
-- 说明: 仅建表，不插入任何凭据记录
-- ============================================

-- 创建数据库
CREATE DATABASE IF NOT EXISTS steve_home DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

USE steve_home;

-- ============================================
-- 凭据表（暗号 BCrypt 哈希存储，支持修改）
-- ============================================
CREATE TABLE IF NOT EXISTS credential (
    id              BIGINT       NOT NULL AUTO_INCREMENT COMMENT '主键',
    secret_hash     VARCHAR(100) NOT NULL COMMENT '暗号 BCrypt 哈希',
    token_version   INT          NOT NULL DEFAULT 0 COMMENT 'Token 版本号，修改暗号时+1使旧Token失效',
    updated_at      DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='管理员凭据表';

-- 注意：不在 init.sql 中插入任何凭据记录。
-- 暗号通过应用首次启动时从环境变量 ADMIN_INITIAL_SECRET 读取并 BCrypt 哈希后写入。

-- ============================================
-- 文章表
-- ============================================
CREATE TABLE IF NOT EXISTS article (
    id              BIGINT       NOT NULL AUTO_INCREMENT COMMENT '主键',
    title           VARCHAR(200) NOT NULL COMMENT '标题',
    summary         VARCHAR(500)          DEFAULT NULL COMMENT '摘要',
    content         LONGTEXT              DEFAULT NULL COMMENT '正文（Markdown）',
    cover_image     VARCHAR(500)          DEFAULT NULL COMMENT '封面图 URL',
    category        VARCHAR(20)  NOT NULL DEFAULT 'tech' COMMENT '分类：tech-技术博客 / life-生活文章',
    tags            VARCHAR(200)          DEFAULT NULL COMMENT '标签，逗号分隔',
    status          TINYINT      NOT NULL DEFAULT 0 COMMENT '状态：0-草稿 1-已发布',
    view_count      INT          NOT NULL DEFAULT 0 COMMENT '阅读数',
    created_at      DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at      DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    published_at    DATETIME              DEFAULT NULL COMMENT '发布时间（首次发布时写入，不随编辑更新）',
    PRIMARY KEY (id),
    INDEX idx_category_status (category, status),
    INDEX idx_published_at (published_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='文章表';

-- ============================================
-- 动态表
-- ============================================
CREATE TABLE IF NOT EXISTS moment (
    id          BIGINT       NOT NULL AUTO_INCREMENT COMMENT '主键',
    content     TEXT                  DEFAULT NULL COMMENT '动态内容',
    media_type  VARCHAR(20)  NOT NULL DEFAULT 'text' COMMENT '媒体类型：text-纯文字 / image-多图 / music-音乐 / video-视频（第一阶段仅开放text和image）',
    media_url   VARCHAR(500)          DEFAULT NULL COMMENT '音乐/视频外链 URL（第一阶段不使用）',
    location    VARCHAR(200)          DEFAULT NULL COMMENT '朋友圈发布时携带的原始地点',
    created_at  DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at  DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (id),
    INDEX idx_created_at (created_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='动态表';

-- ============================================
-- 动态图片表
-- ============================================
CREATE TABLE IF NOT EXISTS moment_image (
    id          BIGINT       NOT NULL AUTO_INCREMENT COMMENT '主键',
    moment_id   BIGINT       NOT NULL COMMENT '动态 ID',
    url         VARCHAR(500) NOT NULL COMMENT '图片 URL',
    sort        INT          NOT NULL DEFAULT 0 COMMENT '排序',
    PRIMARY KEY (id),
    INDEX idx_moment_id_sort (moment_id, sort),
    CONSTRAINT fk_moment_image FOREIGN KEY (moment_id) REFERENCES moment(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='动态图片表';
