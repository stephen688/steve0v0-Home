import { CalendarData, CourseItem, StudyRecordItem } from '../types/api';

export interface CalendarDay {
  date: string;
  day: number;
  currentMonth: boolean;
  today: boolean;
  recordCount: number;
  recordMinutes: number;
  recordDurationLabel: string;
  courseCount: number;
  courseLabel: string;
  entryCount: number;
  loadLevel: number;
  recordSubjects: string[];
}

function dateKey(date: Date): string {
  return [date.getFullYear(), date.getMonth() + 1, date.getDate()]
    .map((part, index) => index === 0 ? String(part) : String(part).padStart(2, '0'))
    .join('-');
}

function parseDate(value: string): Date {
  const [year, month, day] = value.split('-').map(Number);
  return new Date(year, month - 1, day);
}

function compactDuration(minutes: number): string {
  if (minutes < 60) return `${minutes}m`;
  const hours = Math.round((minutes / 60) * 10) / 10;
  return `${hours}h`;
}

function toCalendarDay(
  date: Date,
  currentMonth: boolean,
  today: string,
  dayRecords: StudyRecordItem[],
  dayCourses: CourseItem[]
): CalendarDay {
  const recordMinutes = dayRecords.reduce((total, record) => total + Math.max(0, Number(record.duration) || 0), 0);
  const entryCount = dayRecords.length + dayCourses.length;
  const loadScore = recordMinutes + dayCourses.length * 60;
  return {
    date: dateKey(date),
    day: date.getDate(),
    currentMonth,
    today: dateKey(date) === today,
    recordCount: dayRecords.length,
    recordMinutes,
    recordDurationLabel: dayRecords.length ? (recordMinutes ? compactDuration(recordMinutes) : `${dayRecords.length}记`) : '',
    courseCount: dayCourses.length,
    courseLabel: dayCourses.length ? `${dayCourses.length}课` : '',
    entryCount,
    loadLevel: entryCount ? (loadScore >= 180 ? 3 : loadScore > 60 ? 2 : 1) : 0,
    recordSubjects: dayRecords.map((record) => record.subject).slice(0, 2)
  };
}

export function buildCalendarDays(year: number, month: number, data: CalendarData | null): CalendarDay[] {
  const first = new Date(year, month - 1, 1);
  const last = new Date(year, month, 0);
  const leading = (first.getDay() + 6) % 7;
  const total = Math.ceil((leading + last.getDate()) / 7) * 7;
  const records = new Map<string, StudyRecordItem[]>();
  const courses = new Map<string, CourseItem[]>();
  (data?.records || []).forEach((record) => {
    const list = records.get(record.recordDate) || [];
    list.push(record);
    records.set(record.recordDate, list);
  });
  (data?.courses || []).forEach((course) => {
    const list = courses.get(course.date) || [];
    list.push(course);
    courses.set(course.date, list);
  });
  const today = dateKey(new Date());
  const days: CalendarDay[] = [];
  for (let index = 0; index < total; index += 1) {
    const date = new Date(year, month - 1, index - leading + 1);
    const key = dateKey(date);
    const dayRecords = records.get(key) || [];
    days.push(toCalendarDay(date, date.getMonth() === month - 1, today, dayRecords, courses.get(key) || []));
  }
  return days;
}

export function buildWeekDays(dateValue: string, data: CalendarData | null): CalendarDay[] {
  const selected = parseDate(dateValue);
  const monday = new Date(selected);
  const offset = (monday.getDay() + 6) % 7;
  monday.setDate(monday.getDate() - offset);
  const records = new Map<string, StudyRecordItem[]>();
  const courses = new Map<string, CourseItem[]>();
  (data?.records || []).forEach((record) => {
    const list = records.get(record.recordDate) || [];
    list.push(record);
    records.set(record.recordDate, list);
  });
  (data?.courses || []).forEach((course) => {
    const list = courses.get(course.date) || [];
    list.push(course);
    courses.set(course.date, list);
  });
  const today = dateKey(new Date());
  return Array.from({ length: 7 }, (_, index) => {
    const day = new Date(monday);
    day.setDate(monday.getDate() + index);
    const key = dateKey(day);
    const dayRecords = records.get(key) || [];
    return toCalendarDay(day, day.getMonth() === selected.getMonth(), today, dayRecords, courses.get(key) || []);
  });
}

export function getMonthLabel(year: number, month: number): string {
  return `${year} 年 ${String(month).padStart(2, '0')} 月`;
}

export function dateToLabel(value: string): string {
  const date = parseDate(value);
  return `${date.getMonth() + 1} 月 ${date.getDate()} 日`;
}
