package com.steve0v0.home.service;

import com.steve0v0.home.common.constant.Constants;
import com.steve0v0.home.dto.PomodoroCompleteDTO;
import com.steve0v0.home.entity.Pomodoro;
import com.steve0v0.home.mapper.PomodoroMapper;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;

/**
 * 番茄钟服务
 * 只保存已完成的番茄钟记录，不支持补录
 * duration 是专注时长的唯一真源
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class PomodoroService {

    private final PomodoroMapper pomodoroMapper;

    /**
     * 保存已完成的番茄钟记录
     * 服务端自动生成 completed_at，不接收客户端时间
     */
    public Long completePomodoro(PomodoroCompleteDTO dto) {
        Pomodoro pomodoro = new Pomodoro();
        pomodoro.setTaskName(dto.getTaskName());
        pomodoro.setDuration(dto.getDuration());
        pomodoro.setCompletedAt(LocalDateTime.now(ZoneId.of(Constants.TIME_ZONE)));
        pomodoroMapper.insert(pomodoro);
        log.info("番茄钟完成记录保存成功 | id={} | duration={}min", pomodoro.getId(), pomodoro.getDuration());
        return pomodoro.getId();
    }

    /**
     * 查询指定日期范围内完成的番茄钟记录（供统计使用）
     */
    public List<Pomodoro> getByDateRange(LocalDate startDate, LocalDate endDate) {
        LambdaQueryWrapper<Pomodoro> wrapper = new LambdaQueryWrapper<>();
        // completed_at 是 DATETIME，需要转换为日期范围比较
        wrapper.ge(Pomodoro::getCompletedAt, startDate.atStartOfDay())
               .le(Pomodoro::getCompletedAt, endDate.atTime(23, 59, 59));
        return pomodoroMapper.selectList(wrapper);
    }
}
