package com.steve0v0.home.service;

import com.steve0v0.home.common.constant.Constants;
import com.steve0v0.home.entity.StudyRecord;
import com.steve0v0.home.vo.CalendarVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.ZoneId;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 日历服务
 * 提供月视图和周视图的日历数据查询
 * 返回指定日期范围内的学习记录和课程实例
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class CalendarService {

    private final StudyRecordService studyRecordService;
    private final CourseService courseService;

    private static final ZoneId ZONE_ID = ZoneId.of(Constants.TIME_ZONE);

    /**
     * 查询日历数据
     *
     * @param view 视图模式：month / week，默认 month
     * @param date 当前查询日期，默认今天
     * @return 日历数据，包含学习记录和课程实例
     */
    public CalendarVO getCalendar(String view, LocalDate date) {
        if (date == null) {
            date = LocalDate.now(ZONE_ID);
        }
        if (view == null || view.isBlank()) {
            view = "month";
        }

        LocalDate startDate;
        LocalDate endDate;

        if ("week".equals(view)) {
            // 周视图：周一至周日
            startDate = date.with(DayOfWeek.MONDAY);
            endDate = date.with(DayOfWeek.SUNDAY);
        } else {
            // 月视图：当月1日到最后一天
            YearMonth yearMonth = YearMonth.from(date);
            startDate = yearMonth.atDay(1);
            endDate = yearMonth.atEndOfMonth();
        }

        // 查询学习记录
        List<StudyRecord> records = studyRecordService.getByDateRange(startDate, endDate);
        List<CalendarVO.RecordItem> recordItems = records.stream()
                .map(this::toRecordItem)
                .collect(Collectors.toList());

        // 查询课程实例
        List<CourseService.CourseCalendarItem> courseInstances =
                courseService.getCourseInstancesByDateRange(startDate, endDate);
        List<CalendarVO.CourseItem> courseItems = courseInstances.stream()
                .map(this::toCourseItem)
                .collect(Collectors.toList());

        CalendarVO vo = new CalendarVO();
        vo.setView(view);
        vo.setStartDate(startDate);
        vo.setEndDate(endDate);
        vo.setRecords(recordItems);
        vo.setCourses(courseItems);
        return vo;
    }

    private CalendarVO.RecordItem toRecordItem(StudyRecord record) {
        CalendarVO.RecordItem item = new CalendarVO.RecordItem();
        item.setId(record.getId());
        item.setRecordDate(record.getRecordDate());
        item.setSubject(record.getSubject());
        item.setDuration(record.getDuration());
        return item;
    }

    private CalendarVO.CourseItem toCourseItem(CourseService.CourseCalendarItem instance) {
        CalendarVO.CourseItem item = new CalendarVO.CourseItem();
        item.setId(instance.course().getId());
        item.setDate(instance.date());
        item.setName(instance.course().getName());
        item.setStartTime(instance.course().getStartTime().toString());
        item.setEndTime(instance.course().getEndTime().toString());
        item.setLocation(instance.course().getLocation());
        return item;
    }
}
