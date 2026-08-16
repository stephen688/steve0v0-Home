package com.steve0v0.home.common.constant;

public final class Constants {
    private Constants() {}

    /** Authorization Header 前缀 */
    public static final String BEARER_PREFIX = "Bearer ";
    /** Authorization Header 名称 */
    public static final String AUTHORIZATION_HEADER = "Authorization";
    /** JWT Payload 中的 credentialId 键 */
    public static final String JWT_CLAIM_CREDENTIAL_ID = "sub";
    /** JWT Payload 中的 tokenVersion 键 */
    public static final String JWT_CLAIM_TOKEN_VERSION = "tv";
    /** 文章分类-技术博客 */
    public static final String CATEGORY_TECH = "tech";
    /** 文章分类-生活文章 */
    public static final String CATEGORY_LIFE = "life";
    /** 文章状态-草稿 */
    public static final int ARTICLE_STATUS_DRAFT = 0;
    /** 文章状态-已发布 */
    public static final int ARTICLE_STATUS_PUBLISHED = 1;
    /** 媒体类型-纯文字 */
    public static final String MEDIA_TYPE_TEXT = "text";
    /** 媒体类型-多图 */
    public static final String MEDIA_TYPE_IMAGE = "image";
    /** 默认分页大小 */
    public static final int DEFAULT_PAGE_SIZE = 10;
    /** 最大分页大小 */
    public static final int MAX_PAGE_SIZE = 50;

    // ==================== 第二阶段常量 ====================

    /** 番茄钟最小时长（分钟） */
    public static final int POMODORO_MIN_DURATION = 1;
    /** 番茄钟最大时长（分钟） */
    public static final int POMODORO_MAX_DURATION = 480;

    /** 课程模式-仅当天 */
    public static final int COURSE_SINGLE_DAY = 0;
    /** 课程模式-每周重复 */
    public static final int COURSE_WEEKLY_REPEAT = 1;

    /** 个人状态-在线 */
    public static final String STATUS_ONLINE = "online";
    /** 个人状态-学习中 */
    public static final String STATUS_STUDYING = "studying";
    /** 个人状态-运动中 */
    public static final String STATUS_EXERCISING = "exercising";
    /** 个人状态-忙碌 */
    public static final String STATUS_BUSY = "busy";
    /** 个人状态-休息 */
    public static final String STATUS_REST = "rest";

    /** 个人状态固定记录 ID */
    public static final long STATUS_RECORD_ID = 1L;

    /** 热力图统计天数 */
    public static final int HEATMAP_DAYS = 30;

    /** 系统时区 */
    public static final String TIME_ZONE = "Asia/Shanghai";
}
