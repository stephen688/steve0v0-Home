package com.steve0v0.home.service;

import com.steve0v0.home.common.constant.Constants;
import com.steve0v0.home.entity.Pomodoro;
import com.steve0v0.home.entity.StudyRecord;
import com.steve0v0.home.vo.HeatmapItemVO;
import com.steve0v0.home.vo.StatsVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 学习数据统计服务
 * 聚合学习记录和番茄钟数据，提供：
 * - 今日学习时长、番茄钟数量和专注时长
 * - 本周学习时长、每日番茄钟数量
 * - 连续学习天数
 * - 最近30天热力图
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class StatsService {

    private final StudyRecordService studyRecordService;
    private final PomodoroService pomodoroService;

    private static final ZoneId ZONE_ID = ZoneId.of(Constants.TIME_ZONE);

    /**
     * 获取统计数据
     */
    public StatsVO getStats() {
        LocalDate today = LocalDate.now(ZONE_ID);

        // 今日数据
        List<StudyRecord> todayRecords = studyRecordService.getByDateRange(today, today);
        List<Pomodoro> todayPomodoros = pomodoroService.getByDateRange(today, today);

        int todayStudyMinutes = todayRecords.stream()
                .mapToInt(StudyRecord::getDuration)
                .sum();
        int todayPomodoroCount = todayPomodoros.size();
        int todayPomodoroMinutes = todayPomodoros.stream()
                .mapToInt(Pomodoro::getDuration)
                .sum();

        // 本周数据（周一至周日）
        LocalDate weekStart = today.with(DayOfWeek.MONDAY);
        LocalDate weekEnd = today.with(DayOfWeek.SUNDAY);
        List<StudyRecord> weekRecords = studyRecordService.getByDateRange(weekStart, weekEnd);
        List<Pomodoro> weekPomodoros = pomodoroService.getByDateRange(weekStart, weekEnd);

        int weeklyStudyMinutes = weekRecords.stream()
                .mapToInt(StudyRecord::getDuration)
                .sum();

        // 本周每日番茄钟数量
        Map<LocalDate, Long> weeklyPomodoroMap = weekPomodoros.stream()
                .collect(Collectors.groupingBy(
                        p -> p.getCompletedAt().toLocalDate(),
                        Collectors.counting()
                ));
        List<StatsVO.WeeklyPomodoroCount> weeklyPomodoroCounts = new ArrayList<>();
        for (int i = 0; i < 7; i++) {
            LocalDate day = weekStart.plusDays(i);
            int count = weeklyPomodoroMap.getOrDefault(day, 0L).intValue();
            weeklyPomodoroCounts.add(new StatsVO.WeeklyPomodoroCount(day, count));
        }

        // 连续学习天数
        int streakDays = calculateStreakDays(today);

        // 热力图（最近30天）
        List<HeatmapItemVO> heatmap = calculateHeatmap(today);

        StatsVO vo = new StatsVO();
        vo.setTodayStudyMinutes(todayStudyMinutes);
        vo.setTodayPomodoroCount(todayPomodoroCount);
        vo.setTodayPomodoroMinutes(todayPomodoroMinutes);
        vo.setWeeklyStudyMinutes(weeklyStudyMinutes);
        vo.setWeeklyPomodoroCounts(weeklyPomodoroCounts);
        vo.setStreakDays(streakDays);
        vo.setHeatmap(heatmap);
        return vo;
    }

    /**
     * 计算连续学习天数
     * 从今天向前计算，学习记录和番茄钟都算作活跃来源
     * 如果今天没有任何活动，连续天数为 0
     * 注意：连续天数不受热力图 30 天范围限制，查询全部历史记录
     */
    private int calculateStreakDays(LocalDate today) {
        // 查询全部历史记录来计算连续天数（从最早日期到今日）
        LocalDate startDate = LocalDate.EPOCH;
        List<StudyRecord> records = studyRecordService.getByDateRange(startDate, today);
        List<Pomodoro> pomodoros = pomodoroService.getByDateRange(startDate, today);

        Set<LocalDate> activeDates = new HashSet<>();
        records.forEach(r -> activeDates.add(r.getRecordDate()));
        pomodoros.forEach(p -> activeDates.add(p.getCompletedAt().toLocalDate()));

        int streak = 0;
        LocalDate date = today;
        while (activeDates.contains(date)) {
            streak++;
            date = date.minusDays(1);
        }
        return streak;
    }

    /**
     * 计算最近30天热力图数据
     * 每日活跃数量 = 当日学习记录数量 + 当日完成番茄钟数量
     */
    private List<HeatmapItemVO> calculateHeatmap(LocalDate today) {
        LocalDate startDate = today.minusDays(Constants.HEATMAP_DAYS - 1);
        List<StudyRecord> records = studyRecordService.getByDateRange(startDate, today);
        List<Pomodoro> pomodoros = pomodoroService.getByDateRange(startDate, today);

        // 按日期统计学习记录数量
        Map<LocalDate, Long> recordCounts = records.stream()
                .collect(Collectors.groupingBy(StudyRecord::getRecordDate, Collectors.counting()));

        // 按日期统计番茄钟数量
        Map<LocalDate, Long> pomodoroCounts = pomodoros.stream()
                .collect(Collectors.groupingBy(p -> p.getCompletedAt().toLocalDate(), Collectors.counting()));

        List<HeatmapItemVO> heatmap = new ArrayList<>();
        for (int i = 0; i < Constants.HEATMAP_DAYS; i++) {
            LocalDate date = startDate.plusDays(i);
            int recordCount = recordCounts.getOrDefault(date, 0L).intValue();
            int pomodoroCount = pomodoroCounts.getOrDefault(date, 0L).intValue();
            int activityCount = recordCount + pomodoroCount;

            HeatmapItemVO item = new HeatmapItemVO();
            item.setDate(date);
            item.setActivityCount(activityCount);
            heatmap.add(item);
        }
        return heatmap;
    }
}
