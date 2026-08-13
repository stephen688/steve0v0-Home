package com.steve0v0.home.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.steve0v0.home.common.constant.Constants;
import com.steve0v0.home.common.exception.BusinessException;
import com.steve0v0.home.common.pagination.PageResult;
import com.steve0v0.home.common.result.ResultCode;
import com.steve0v0.home.dto.CourseCreateDTO;
import com.steve0v0.home.dto.CourseQueryDTO;
import com.steve0v0.home.dto.CourseUpdateDTO;
import com.steve0v0.home.entity.Course;
import com.steve0v0.home.mapper.CourseMapper;
import com.steve0v0.home.vo.CourseVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 课程服务
 * 处理课程的增删改查，支持单日课程和每周重复课程
 * 日历查询时按日期范围生成课程实例
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class CourseService {

    private final CourseMapper courseMapper;

    /**
     * 将实体转换为 VO
     */
    private CourseVO toVO(Course course) {
        CourseVO vo = new CourseVO();
        vo.setId(course.getId());
        vo.setName(course.getName());
        vo.setStartDate(course.getStartDate());
        vo.setEndDate(course.getEndDate());
        vo.setStartTime(course.getStartTime());
        vo.setEndTime(course.getEndTime());
        vo.setLocation(course.getLocation());
        vo.setDayOfWeek(course.getDayOfWeek());
        vo.setIsRepeated(course.getIsRepeated());
        vo.setCreatedAt(course.getCreatedAt());
        vo.setUpdatedAt(course.getUpdatedAt());
        return vo;
    }

    /**
     * 校验课程数据完整性
     * - is_repeated=0：start_date=end_date，day_of_week 必须为 null
     * - is_repeated=1：必须提供 day_of_week(1-7)，start_date<=end_date
     * - 所有模式：start_time 必须早于 end_time
     */
    private void validateCourseRules(Integer isRepeated, LocalDate startDate, LocalDate endDate, Integer dayOfWeek,
                                     LocalTime startTime, LocalTime endTime) {
        if (isRepeated == null) {
            throw new BusinessException(ResultCode.BAD_REQUEST, "课程模式不能为空");
        }
        if (startTime != null && endTime != null && !startTime.isBefore(endTime)) {
            throw new BusinessException(ResultCode.BAD_REQUEST, "开始时间必须早于结束时间");
        }
        if (isRepeated == Constants.COURSE_SINGLE_DAY) {
            if (!startDate.equals(endDate)) {
                throw new BusinessException(ResultCode.BAD_REQUEST, "仅当天课程必须满足 start_date = end_date");
            }
            if (dayOfWeek != null) {
                throw new BusinessException(ResultCode.BAD_REQUEST, "仅当天课程不需要填写星期");
            }
        } else if (isRepeated == Constants.COURSE_WEEKLY_REPEAT) {
            if (startDate.isAfter(endDate)) {
                throw new BusinessException(ResultCode.BAD_REQUEST, "开始日期不能晚于结束日期");
            }
            if (dayOfWeek == null || dayOfWeek < 1 || dayOfWeek > 7) {
                throw new BusinessException(ResultCode.BAD_REQUEST, "每周重复课程必须填写星期（1-7）");
            }
        } else {
            throw new BusinessException(ResultCode.BAD_REQUEST, "课程模式无效，仅支持 0（仅当天）或 1（每周重复）");
        }
    }

    /**
     * 管理端课程列表查询
     * 按 start_date DESC, id DESC 排序
     */
    public PageResult<CourseVO> getAdminCourseList(CourseQueryDTO queryDTO) {
        Page<Course> page = new Page<>(queryDTO.getPage(), queryDTO.getSize());

        LambdaQueryWrapper<Course> wrapper = new LambdaQueryWrapper<>();
        wrapper.orderByDesc(Course::getStartDate)
               .orderByDesc(Course::getId);

        IPage<Course> result = courseMapper.selectPage(page, wrapper);
        List<CourseVO> list = result.getRecords().stream()
                .map(this::toVO)
                .collect(Collectors.toList());
        return new PageResult<>(queryDTO.getPage(), queryDTO.getSize(), result.getTotal(), list);
    }

    /**
     * 管理端课程详情查询
     */
    public CourseVO getAdminCourseById(Long id) {
        Course course = courseMapper.selectById(id);
        if (course == null) {
            throw new BusinessException(ResultCode.NOT_FOUND);
        }
        return toVO(course);
    }

    /**
     * 新增课程
     */
    public Long createCourse(CourseCreateDTO dto) {
        int isRepeated = dto.getIsRepeated() != null ? dto.getIsRepeated() : Constants.COURSE_SINGLE_DAY;
        validateCourseRules(isRepeated, dto.getStartDate(), dto.getEndDate(), dto.getDayOfWeek(),
                dto.getStartTime(), dto.getEndTime());

        Course course = new Course();
        course.setName(dto.getName());
        course.setStartDate(dto.getStartDate());
        course.setEndDate(dto.getEndDate());
        course.setStartTime(dto.getStartTime());
        course.setEndTime(dto.getEndTime());
        course.setLocation(dto.getLocation());
        course.setDayOfWeek(isRepeated == Constants.COURSE_SINGLE_DAY ? null : dto.getDayOfWeek());
        course.setIsRepeated(isRepeated);
        courseMapper.insert(course);
        log.info("新建课程成功 | id={} | name={} | repeated={}", course.getId(), course.getName(), isRepeated);
        return course.getId();
    }

    /**
     * 修改课程
     */
    public void updateCourse(Long id, CourseUpdateDTO dto) {
        Course course = courseMapper.selectById(id);
        if (course == null) {
            throw new BusinessException(ResultCode.NOT_FOUND);
        }
        validateCourseRules(dto.getIsRepeated(), dto.getStartDate(), dto.getEndDate(), dto.getDayOfWeek(),
                dto.getStartTime(), dto.getEndTime());

        course.setName(dto.getName());
        course.setStartDate(dto.getStartDate());
        course.setEndDate(dto.getEndDate());
        course.setStartTime(dto.getStartTime());
        course.setEndTime(dto.getEndTime());
        course.setLocation(dto.getLocation());
        course.setDayOfWeek(dto.getIsRepeated() == Constants.COURSE_SINGLE_DAY ? null : dto.getDayOfWeek());
        course.setIsRepeated(dto.getIsRepeated());
        courseMapper.updateById(course);
        log.info("修改课程成功 | id={}", id);
    }

    /**
     * 删除课程（物理删除）
     */
    public void deleteCourse(Long id) {
        Course course = courseMapper.selectById(id);
        if (course == null) {
            throw new BusinessException(ResultCode.NOT_FOUND);
        }
        courseMapper.deleteById(id);
        log.info("删除课程成功 | id={}", id);
    }

    /**
     * 查询指定日期范围内会出现的课程实例（供日历使用）
     * - 单日课程：start_date 在范围内则生成一个实例
     * - 每周重复课程：在日期范围内、且星期匹配的每一天生成一个实例
     *
     * @return 课程实例列表，每项包含课程信息和对应日期
     */
    public List<CourseCalendarItem> getCourseInstancesByDateRange(LocalDate startDate, LocalDate endDate) {
        // 查询可能在此范围内出现的课程：
        // 单日课程的 start_date 在范围内
        // 每周重复课程的日期范围与查询范围有交集
        LambdaQueryWrapper<Course> wrapper = new LambdaQueryWrapper<>();
        wrapper.and(w -> w
                // 单日课程：start_date 在范围内
                .and(w1 -> w1.eq(Course::getIsRepeated, Constants.COURSE_SINGLE_DAY)
                             .ge(Course::getStartDate, startDate)
                             .le(Course::getStartDate, endDate))
                // 每周重复课程：日期范围有交集
                .or(w2 -> w2.eq(Course::getIsRepeated, Constants.COURSE_WEEKLY_REPEAT)
                            .le(Course::getStartDate, endDate)
                            .ge(Course::getEndDate, startDate))
        );
        List<Course> courses = courseMapper.selectList(wrapper);

        List<CourseCalendarItem> instances = new ArrayList<>();
        for (Course course : courses) {
            if (course.getIsRepeated() == Constants.COURSE_SINGLE_DAY) {
                // 单日课程：直接添加
                instances.add(new CourseCalendarItem(course, course.getStartDate()));
            } else {
                // 每周重复课程：遍历范围内每一天，检查星期是否匹配
                LocalDate date = startDate;
                while (!date.isAfter(endDate)) {
                    // 检查日期是否在课程日期范围内
                    if (!date.isBefore(course.getStartDate()) && !date.isAfter(course.getEndDate())) {
                        // 检查星期是否匹配（day_of_week: 1=周一, 7=周日）
                        int dayOfWeekValue = date.getDayOfWeek().getValue();
                        if (dayOfWeekValue == course.getDayOfWeek()) {
                            instances.add(new CourseCalendarItem(course, date));
                        }
                    }
                    date = date.plusDays(1);
                }
            }
        }
        return instances;
    }

    /**
     * 日历课程实例，包含课程信息和该实例对应的日期
     */
    public record CourseCalendarItem(Course course, LocalDate date) {}
}
