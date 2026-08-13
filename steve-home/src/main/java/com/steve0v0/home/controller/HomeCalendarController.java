package com.steve0v0.home.controller;

import com.steve0v0.home.common.result.Result;
import com.steve0v0.home.service.CalendarService;
import com.steve0v0.home.vo.CalendarVO;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;

/**
 * 首页日历公开接口
 * 访客无需登录即可查看日历数据
 */
@RestController
@RequestMapping("/api/home/calendar")
@RequiredArgsConstructor
public class HomeCalendarController {

    private final CalendarService calendarService;

    /**
     * 查询日历数据
     *
     * @param view 视图模式：month / week，默认 month
     * @param date 当前查询日期，格式 yyyy-MM-dd，默认今天
     */
    @GetMapping
    public Result<CalendarVO> calendar(
            @RequestParam(required = false) String view,
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate date) {
        return Result.success(calendarService.getCalendar(view, date));
    }
}
