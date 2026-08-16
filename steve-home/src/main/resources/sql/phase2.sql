-- ============================================
-- steve0v0 小屋 · 第二阶段数据库脚本
-- 版本: v1.0  日期: 2026-08-11
-- 说明: 在第一阶段 init.sql 执行完成后执行
-- ============================================

USE steve_home;

-- ============================================
-- 学习记录表
-- ============================================
CREATE TABLE IF NOT EXISTS study_record (
    id           BIGINT       NOT NULL AUTO_INCREMENT COMMENT '主键',
    record_date  DATE         NOT NULL COMMENT '学习日期',
    subject      VARCHAR(100) NOT NULL COMMENT '主题或科目',
    content      TEXT         NOT NULL COMMENT '学习内容',
    duration     INT          NOT NULL COMMENT '学习时长，单位分钟',
    created_at   DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at   DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (id),
    INDEX idx_record_date (record_date)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='学习记录表';

-- ============================================
-- 课程表
-- ============================================
CREATE TABLE IF NOT EXISTS course (
    id           BIGINT       NOT NULL AUTO_INCREMENT COMMENT '主键',
    name         VARCHAR(200) NOT NULL COMMENT '课程名称',
    start_date   DATE         NOT NULL COMMENT '开始日期',
    end_date     DATE         NOT NULL COMMENT '结束日期',
    start_time   TIME         NOT NULL COMMENT '开始时间',
    end_time     TIME         NOT NULL COMMENT '结束时间',
    location     VARCHAR(200)          DEFAULT NULL COMMENT '上课地点',
    day_of_week  TINYINT               DEFAULT NULL COMMENT '星期，1-7表示周一至周日',
    is_repeated  TINYINT(1)   NOT NULL DEFAULT 0 COMMENT '0-仅当天，1-每周重复',
    created_at   DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at   DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (id),
    INDEX idx_course_date_range (start_date, end_date),
    INDEX idx_course_repeat (is_repeated, day_of_week)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='课程表';

-- ============================================
-- 个人状态表
-- ============================================
CREATE TABLE IF NOT EXISTS personal_status (
    id            BIGINT       NOT NULL COMMENT '主键，固定为1',
    state         VARCHAR(20)  NOT NULL DEFAULT 'online' COMMENT '状态：online/studying/exercising/busy/rest',
    current_task  VARCHAR(200)          DEFAULT NULL COMMENT '当前正在做的事',
    mood          VARCHAR(200)          DEFAULT NULL COMMENT '心情签名',
    updated_at    DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='个人状态表';

-- ============================================
-- 番茄钟完成记录表
-- ============================================
CREATE TABLE IF NOT EXISTS pomodoro (
    id           BIGINT       NOT NULL AUTO_INCREMENT COMMENT '主键',
    task_name    VARCHAR(200)          DEFAULT NULL COMMENT '关联任务',
    duration     INT          NOT NULL COMMENT '倒计时设定时长，单位分钟',
    completed_at DATETIME     NOT NULL COMMENT '完成时间',
    created_at   DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (id),
    INDEX idx_completed_at (completed_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='番茄钟完成记录表';
