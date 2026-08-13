package com.steve0v0.home.vo;

import lombok.Data;

import java.time.LocalDate;
import java.util.List;

/**
 * 学习数据统计响应对象
 */
@Data
public class StatsVO {
    /** 今日学习时长（分钟） */
    private Integer todayStudyMinutes;
    /** 今日完成番茄钟数量 */
    private Integer todayPomodoroCount;
    /** 今日番茄钟专注总时长（分钟） */
    private Integer todayPomodoroMinutes;
    /** 本周学习时长合计（分钟） */
    private Integer weeklyStudyMinutes;
    /** 本周每日番茄钟数量，固定7个元素，周一到周日 */
    private List<WeeklyPomodoroCount> weeklyPomodoroCounts;
    /** 连续学习天数 */
    private Integer streakDays;
    /** 最近30天热力图数据 */
    private List<HeatmapItemVO> heatmap;

    /**
     * 本周每日番茄钟数量
     */
    @Data
    public static class WeeklyPomodoroCount {
        private LocalDate date;
        private Integer count;

        public WeeklyPomodoroCount(LocalDate date, Integer count) {
            this.date = date;
            this.count = count;
        }
    }
}
