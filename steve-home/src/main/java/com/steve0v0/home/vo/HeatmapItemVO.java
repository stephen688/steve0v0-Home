package com.steve0v0.home.vo;

import lombok.Data;

import java.time.LocalDate;

/**
 * 热力图单日数据
 */
@Data
public class HeatmapItemVO {
    private LocalDate date;
    /** 活跃数量 = 学习记录数量 + 完成番茄钟数量 */
    private Integer activityCount;
}
