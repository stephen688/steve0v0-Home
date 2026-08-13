import { api } from '../../services/api';
import { CalendarData, CourseItem, MomentItem, ArticleListItem, StatsData, StatusData, StudyRecordItem } from '../../types/api';
import { formatDate, formatDateTime, formatMinutes, formatState, parseTags } from '../../utils/format';
import { buildCalendarDays, buildWeekDays, CalendarDay, dateToLabel, getMonthLabel } from '../../utils/calendar';

interface PreparedArticle extends ArticleListItem {
  tagList: string[];
  publishedText: string;
}

interface PreparedMoment extends MomentItem {
  createdText: string;
  mediaTypeLabel: string;
  imageLayout: string;
}

interface BarItem {
  label: string;
  value: number;
  height: number;
}

interface HeatmapCell {
  date: string;
  level: number;
}

interface CalendarOverview {
  activeDays: number;
  studyDuration: string;
  courseCount: number;
  entryCount: number;
}

interface CalendarAgendaItem {
  key: string;
  type: 'record' | 'course';
  typeLabel: string;
  title: string;
  meta: string;
}

type PomodoroMode = 'focus' | 'shortBreak' | 'longBreak';

interface PomodoroState {
  duration: number;
  focusDuration: number;
  task: string;
  remaining: number;
  endAt: number;
  running: boolean;
  completed: boolean;
  mode: PomodoroMode;
  completedFocusRounds: number;
}

interface PomodoroTick {
  index: number;
  angle: number;
  major: boolean;
}

interface PomodoroCycleMarker {
  index: number;
  state: 'done' | 'current' | 'upcoming';
}

const today = new Date();
const dateKey = (date: Date) => `${date.getFullYear()}-${String(date.getMonth() + 1).padStart(2, '0')}-${String(date.getDate()).padStart(2, '0')}`;
const POMODORO_STORAGE_KEY = 'steve-home-pomodoro';
const POMODORO_DURATIONS: Record<PomodoroMode, number> = {
  focus: 25,
  shortBreak: 5,
  longBreak: 20
};
const POMODORO_TICKS: PomodoroTick[] = Array.from({ length: 60 }, (_, index) => ({
  index,
  angle: index * 6,
  major: index % 5 === 0
}));

function clampFocusDuration(value: unknown, fallback = 25) {
  return Math.min(180, Math.max(1, Number(value) || fallback));
}

function isPomodoroMode(value: unknown): value is PomodoroMode {
  return value === 'focus' || value === 'shortBreak' || value === 'longBreak';
}

function formatPomodoroTime(seconds: number) {
  const safeSeconds = Math.max(0, seconds);
  return `${String(Math.floor(safeSeconds / 60)).padStart(2, '0')}:${String(safeSeconds % 60).padStart(2, '0')}`;
}

function durationForMode(mode: PomodoroMode, focusDuration: number) {
  return mode === 'focus' ? focusDuration : POMODORO_DURATIONS[mode];
}

function buildPomodoroState(mode: PomodoroMode = 'focus', focusDuration = 25, task = '', completedFocusRounds = 0): PomodoroState {
  const safeFocusDuration = clampFocusDuration(focusDuration);
  const duration = durationForMode(mode, safeFocusDuration);
  return {
    duration,
    focusDuration: safeFocusDuration,
    task,
    remaining: duration * 60,
    endAt: 0,
    running: false,
    completed: false,
    mode,
    completedFocusRounds: Math.min(4, Math.max(0, completedFocusRounds))
  };
}

function normalizePomodoroState(stored?: Partial<PomodoroState>): PomodoroState {
  if (!stored) return buildPomodoroState();
  const mode = isPomodoroMode(stored.mode) ? stored.mode : 'focus';
  const focusDuration = clampFocusDuration(stored.focusDuration ?? stored.duration);
  const duration = durationForMode(mode, focusDuration);
  const remaining = Math.min(duration * 60, Math.max(0, Number(stored.remaining) || 0));
  return {
    duration,
    focusDuration,
    task: String(stored.task || '').slice(0, 40),
    remaining,
    endAt: Number(stored.endAt) || 0,
    running: Boolean(stored.running),
    completed: Boolean(stored.completed),
    mode,
    completedFocusRounds: Math.min(4, Math.max(0, Number(stored.completedFocusRounds) || 0))
  };
}

function completePomodoroState(pomodoro: PomodoroState): PomodoroState {
  const completedFocusRounds = pomodoro.mode === 'focus'
    ? Math.min(4, pomodoro.completedFocusRounds + 1)
    : pomodoro.completedFocusRounds;
  return { ...pomodoro, remaining: 0, endAt: 0, running: false, completed: true, completedFocusRounds };
}

function buildPomodoroView(pomodoro: PomodoroState) {
  const totalSeconds = Math.max(1, pomodoro.duration * 60);
  const activeTicks = Math.min(60, Math.max(0, Math.ceil((pomodoro.remaining / totalSeconds) * 60)));
  const phaseLabels: Record<PomodoroMode, string> = { focus: '专注', shortBreak: '短休息', longBreak: '长休息' };
  const statusLabel = pomodoro.completed
    ? (pomodoro.mode === 'focus' ? '本轮完成' : '休息结束')
    : pomodoro.running
      ? (pomodoro.mode === 'focus' ? '正在专注' : '正在休息')
      : pomodoro.remaining < totalSeconds
        ? '已暂停'
        : '准备开始';
  const primaryLabel = pomodoro.running
    ? '暂停'
    : pomodoro.completed
      ? (pomodoro.mode === 'focus'
        ? (pomodoro.completedFocusRounds >= 4 ? '开始长休息' : '开始短休息')
        : '开始下一轮')
      : pomodoro.remaining < totalSeconds
        ? '继续计时'
        : pomodoro.mode === 'focus'
          ? '开始专注'
          : '开始休息';
  const currentRound = Math.min(4, pomodoro.completedFocusRounds + 1);
  const cycleMarkers: PomodoroCycleMarker[] = [1, 2, 3, 4].map((index) => ({
    index,
    state: index <= pomodoro.completedFocusRounds
      ? 'done'
      : (pomodoro.mode === 'focus' && index === currentRound ? 'current' : 'upcoming')
  }));
  return {
    pomodoroDisplay: formatPomodoroTime(pomodoro.remaining),
    pomodoroActiveTicks: activeTicks,
    pomodoroModeLabel: phaseLabels[pomodoro.mode],
    pomodoroStatusLabel: statusLabel,
    pomodoroPrimaryLabel: primaryLabel,
    pomodoroRoundLabel: pomodoro.completedFocusRounds >= 4 ? '四轮已完成' : `第 ${currentRound} / 4 轮`,
    pomodoroCycleMarkers: cycleMarkers
  };
}

function prepareArticle(item: ArticleListItem): PreparedArticle {
  return { ...item, tagList: parseTags(item.tags).slice(0, 5), publishedText: formatDate(item.publishedAt, 'YYYY-MM-DD') };
}

function prepareMoment(item: MomentItem): PreparedMoment {
  const images = item.images || [];
  return {
    ...item,
    images,
    createdText: formatDateTime(item.createdAt),
    mediaTypeLabel: images.length ? '生活' : (item.mediaType === 'text' ? '记录' : item.mediaType || '记录'),
    imageLayout: images.length === 1 ? 'single' : images.length === 2 ? 'double' : 'grid'
  };
}

function buildBars(stats: StatsData | null): BarItem[] {
  const values = stats?.weeklyPomodoroCounts || [];
  const max = Math.max(1, ...values.map((item) => item.count));
  return values.map((item) => ({
    label: formatDate(item.date, 'MM-DD'),
    value: item.count,
    height: Math.max(12, Math.round((item.count / max) * 100))
  }));
}

function buildHeatmap(stats: StatsData | null): HeatmapCell[] {
  const values = stats?.heatmap || [];
  const max = Math.max(1, ...values.map((item) => item.activityCount));
  return values.map((item) => ({ date: item.date, level: Math.min(4, Math.ceil((item.activityCount / max) * 4)) }));
}

function buildCalendarOverview(data: CalendarData | null): CalendarOverview {
  const records = data?.records || [];
  const courses = data?.courses || [];
  const activeDates = new Set<string>();
  records.forEach((item) => activeDates.add(item.recordDate));
  courses.forEach((item) => activeDates.add(item.date));
  const studyMinutes = records.reduce((total, item) => total + Math.max(0, Number(item.duration) || 0), 0);
  return {
    activeDays: activeDates.size,
    studyDuration: formatMinutes(studyMinutes),
    courseCount: courses.length,
    entryCount: records.length + courses.length
  };
}

function buildCalendarDetail(data: CalendarData | null, selectedDate: string) {
  const records = (data?.records || []).filter((item) => item.recordDate === selectedDate);
  const courses = (data?.courses || [])
    .filter((item) => item.date === selectedDate)
    .sort((left, right) => String(left.startTime || '').localeCompare(String(right.startTime || '')));
  const items: CalendarAgendaItem[] = [
    ...courses.map((item) => ({
      key: `course-${item.id}-${item.date}`,
      type: 'course' as const,
      typeLabel: '课程',
      title: item.name,
      meta: `${String(item.startTime || '').slice(0, 5)} — ${String(item.endTime || '').slice(0, 5)}${item.location ? ` · ${item.location}` : ''}`
    })),
    ...records.map((item) => ({
      key: `record-${item.id}-${item.recordDate}`,
      type: 'record' as const,
      typeLabel: '学习',
      title: item.subject,
      meta: formatMinutes(item.duration)
    }))
  ];
  const studyMinutes = records.reduce((total, item) => total + Math.max(0, Number(item.duration) || 0), 0);
  return {
    detailLabel: dateToLabel(selectedDate),
    detailRecords: records,
    detailCourses: courses,
    detailAgenda: items.slice(0, 3),
    detailEntryCount: items.length,
    detailOverflowCount: Math.max(0, items.length - 3),
    detailSummary: [
      records.length ? formatMinutes(studyMinutes) : '',
      courses.length ? `${courses.length} 节课程` : ''
    ].filter(Boolean).join(' · ')
  };
}

Page({
  data: {
    status: null as (StatusData & { stateLabel?: string }) | null,
    statusLoading: true,
    statusError: false,
    stats: null as StatsData | null,
    statsLoading: true,
    statsError: false,
    calendarData: null as CalendarData | null,
    calendarLoading: true,
    calendarError: false,
    calendarView: 'month' as 'month' | 'week',
    calendarYear: today.getFullYear(),
    calendarMonth: today.getMonth() + 1,
    selectedDate: dateKey(today),
    calendarTitle: getMonthLabel(today.getFullYear(), today.getMonth() + 1),
    calendarDays: [] as CalendarDay[],
    weekdays: ['一', '二', '三', '四', '五', '六', '日'],
    calendarOverview: { activeDays: 0, studyDuration: '0 分钟', courseCount: 0, entryCount: 0 } as CalendarOverview,
    detailOpen: false,
    detailLabel: dateToLabel(dateKey(today)),
    detailRecords: [] as StudyRecordItem[],
    detailCourses: [] as CourseItem[],
    detailAgenda: [] as CalendarAgendaItem[],
    detailEntryCount: 0,
    detailOverflowCount: 0,
    detailSummary: '',
    recentArticles: [] as PreparedArticle[],
    recentMoments: [] as PreparedMoment[],
    feedLoading: true,
    articleFeedError: false,
    momentFeedError: false,
    pomodoroOpen: false,
    pomodoro: buildPomodoroState(),
    pomodoroTicks: POMODORO_TICKS,
    pomodoroActiveTicks: 60,
    pomodoroDisplay: '25:00',
    pomodoroModeLabel: '专注',
    pomodoroStatusLabel: '准备开始',
    pomodoroPrimaryLabel: '开始专注',
    pomodoroRoundLabel: '第 1 / 4 轮',
    pomodoroCycleMarkers: buildPomodoroView(buildPomodoroState()).pomodoroCycleMarkers,
    pomodoroDurationInput: '25',
    pomodoroTaskInput: '',
    pomodoroInterval: 0
  },

  onLoad() {
    this.restorePomodoro();
    this.loadAll();
  },

  onShow() {
    this.syncPomodoro();
    if (this.data.pomodoro.running) this.startPomodoroInterval();
  },

  onHide() {
    this.persistPomodoro();
    this.stopPomodoroInterval();
  },

  onUnload() {
    this.stopPomodoroInterval();
  },

  onPullDownRefresh() {
    this.loadAll(true);
    setTimeout(() => wx.stopPullDownRefresh(), 1200);
  },

  loadAll(refresh = false) {
    if (refresh) {
      this.setData({ statusError: false, statsError: false, calendarError: false, articleFeedError: false, momentFeedError: false });
    }
    const currentDate = this.data.selectedDate;
    this.loadStatus();
    this.loadStats();
    this.loadCalendar(this.data.calendarView, currentDate);
    this.loadFeeds();
  },

  loadStatus() {
    this.setData({ statusLoading: true, statusError: false });
    api.getStatus().then((status) => {
      this.setData({ status: { ...status, stateLabel: formatState(status.state) }, statusLoading: false });
    }).catch(() => this.setData({ statusLoading: false, statusError: true }));
  },

  loadStats() {
    this.setData({ statsLoading: true, statsError: false });
    api.getStats().then((stats) => {
      this.setData({ stats, statsLoading: false, weeklyBars: buildBars(stats), heatmapCells: buildHeatmap(stats) });
    }).catch(() => this.setData({ statsLoading: false, statsError: true }));
  },

  loadCalendar(view?: 'month' | 'week', date?: string) {
    const targetView = view || this.data.calendarView;
    const targetDate = date || this.data.selectedDate;
    const targetDateObject = new Date(targetDate.replace(/-/g, '/'));
    const nextCalendarYear = targetDateObject.getFullYear();
    const nextCalendarMonth = targetDateObject.getMonth() + 1;
    this.setData({ calendarLoading: true, calendarError: false, calendarView: targetView, calendarYear: nextCalendarYear, calendarMonth: nextCalendarMonth });
    api.getCalendar(targetView, targetDate).then((calendarData) => {
      const calendarDate = targetView === 'month' ? new Date(nextCalendarYear, nextCalendarMonth - 1, 1) : targetDateObject;
      const days = targetView === 'month'
        ? buildCalendarDays(calendarDate.getFullYear(), calendarDate.getMonth() + 1, calendarData)
        : buildWeekDays(targetDate, calendarData);
      const title = targetView === 'month'
        ? getMonthLabel(calendarDate.getFullYear(), calendarDate.getMonth() + 1)
        : `${formatDate(calendarData.startDate, 'MM-DD')} — ${formatDate(calendarData.endDate, 'MM-DD')}`;
      this.setData({
        calendarData,
        calendarDays: days,
        calendarTitle: title,
        calendarOverview: buildCalendarOverview(calendarData),
        selectedDate: targetDate,
        ...buildCalendarDetail(calendarData, targetDate),
        calendarLoading: false
      });
    }).catch(() => this.setData({ calendarLoading: false, calendarError: true }));
  },

  loadFeeds() {
    this.setData({ feedLoading: true, articleFeedError: false, momentFeedError: false });
    Promise.all([
      api.getArticles(undefined, 1, 2).then((articles) => this.setData({ recentArticles: articles.list.map(prepareArticle) })).catch(() => this.setData({ articleFeedError: true })),
      api.getMoments(1, 2).then((moments) => this.setData({ recentMoments: moments.list.map(prepareMoment) })).catch(() => this.setData({ momentFeedError: true }))
    ]).finally(() => this.setData({ feedLoading: false }));
  },

  retryStatus() { this.loadStatus(); },
  retryStats() { this.loadStats(); },
  retryCalendar() { this.loadCalendar(); },
  retryFeeds() { this.loadFeeds(); },
  retryArticles() { api.getArticles(undefined, 1, 2).then((articles) => this.setData({ recentArticles: articles.list.map(prepareArticle), articleFeedError: false })).catch(() => this.setData({ articleFeedError: true })); },
  retryMoments() { api.getMoments(1, 2).then((moments) => this.setData({ recentMoments: moments.list.map(prepareMoment), momentFeedError: false })).catch(() => this.setData({ momentFeedError: true })); },

  switchCalendarView(event: WechatPageEvent) {
    const view = event.currentTarget?.dataset?.view as 'month' | 'week';
    this.loadCalendar(view, this.data.selectedDate);
  },

  shiftCalendar(event: WechatPageEvent) {
    const direction = Number(event.currentTarget?.dataset?.direction || 1);
    if (this.data.calendarView === 'month') {
      const next = new Date(this.data.calendarYear, this.data.calendarMonth - 1 + direction, 1);
      const nextDate = dateKey(next);
      this.setData({ calendarYear: next.getFullYear(), calendarMonth: next.getMonth() + 1, selectedDate: nextDate });
      this.loadCalendar('month', nextDate);
      return;
    }
    const next = new Date(this.data.selectedDate.replace(/-/g, '/'));
    next.setDate(next.getDate() + direction * 7);
    const nextDate = dateKey(next);
    this.setData({ selectedDate: nextDate });
    this.loadCalendar('week', nextDate);
  },

  selectDate(event: WechatPageEvent) {
    const selectedDate = String(event.currentTarget?.dataset?.date || '');
    if (!selectedDate || !this.data.calendarData) return;
    const selectedDateObject = new Date(selectedDate.replace(/-/g, '/'));
    if (this.data.calendarView === 'month'
      && (selectedDateObject.getFullYear() !== this.data.calendarYear || selectedDateObject.getMonth() + 1 !== this.data.calendarMonth)) {
      this.loadCalendar('month', selectedDate);
      return;
    }
    this.setData({ selectedDate, ...buildCalendarDetail(this.data.calendarData, selectedDate), detailOpen: false });
  },

  openDetail() {
    if (this.data.detailEntryCount) this.setData({ detailOpen: true });
  },

  closeDetail() { this.setData({ detailOpen: false }); },

  openArticle(event: WechatPageEvent) {
    const id = event.detail?.id || event.currentTarget?.dataset?.id;
    if (id) wx.navigateTo({ url: `/pages/blog-detail/blog-detail?id=${id}` });
  },

  openBlog() { wx.switchTab({ url: '/pages/blog/blog' }); },
  openMoments() { wx.switchTab({ url: '/pages/moments/moments' }); },

  previewImages(event: WechatPageEvent) {
    const images = event.detail?.images || [];
    const current = event.detail?.current || images[0];
    if (images.length) wx.previewImage({ current, urls: images });
  },

  onPomodoroDurationInput(event: WechatPageEvent) {
    const value = String(event.detail?.value || '').replace(/\D/g, '').slice(0, 3);
    this.setData({ pomodoroDurationInput: value });
  },

  onPomodoroTaskInput(event: WechatPageEvent) {
    this.setData({ pomodoroTaskInput: String(event.detail?.value || '').slice(0, 40) });
  },

  openPomodoro() {
    this.setData({ pomodoroOpen: true });
  },

  closePomodoro() {
    this.setData({ pomodoroOpen: false });
  },

  noop() {},

  switchPomodoroMode(event: WechatPageEvent) {
    const mode = event.currentTarget?.dataset?.mode as PomodoroMode;
    if (!isPomodoroMode(mode) || mode === this.data.pomodoro.mode) return;
    if (this.data.pomodoro.running) {
      wx.showToast({ title: '请先暂停当前计时', icon: 'none' });
      return;
    }
    const completedFocusRounds = mode === 'focus' && this.data.pomodoro.completedFocusRounds >= 4
      ? 0
      : this.data.pomodoro.completedFocusRounds;
    const pomodoro = buildPomodoroState(mode, clampFocusDuration(this.data.pomodoroDurationInput), this.data.pomodoroTaskInput.trim(), completedFocusRounds);
    this.updatePomodoro(pomodoro);
  },

  adjustPomodoroDuration(event: WechatPageEvent) {
    if (this.data.pomodoro.running) {
      wx.showToast({ title: '计时中不可修改', icon: 'none' });
      return;
    }
    const delta = Number(event.currentTarget?.dataset?.delta || 0);
    const duration = clampFocusDuration(Number(this.data.pomodoroDurationInput) + delta);
    this.setData({ pomodoroDurationInput: String(duration) });
    this.applyPomodoroInputs();
  },

  applyPomodoroInputs() {
    if (this.data.pomodoro.running) {
      wx.showToast({ title: '计时中不可修改', icon: 'none' });
      return;
    }
    const focusDuration = clampFocusDuration(this.data.pomodoroDurationInput);
    const task = String(this.data.pomodoroTaskInput || '').trim();
    const pomodoro = buildPomodoroState(
      this.data.pomodoro.mode,
      focusDuration,
      task,
      this.data.pomodoro.completedFocusRounds
    );
    this.setData({ pomodoroDurationInput: String(focusDuration), pomodoroTaskInput: task });
    this.updatePomodoro(pomodoro);
  },

  togglePomodoro() {
    if (this.data.pomodoro.completed) {
      this.advancePomodoro(true);
      return;
    }
    if (this.data.pomodoro.running) {
      this.syncPomodoro();
      const pomodoro = { ...this.data.pomodoro, running: false, endAt: 0 };
      this.updatePomodoro(pomodoro);
      this.stopPomodoroInterval();
      return;
    }
    let current = this.data.pomodoro as PomodoroState;
    if (current.remaining === current.duration * 60) {
      const focusDuration = clampFocusDuration(this.data.pomodoroDurationInput, current.focusDuration);
      const task = String(this.data.pomodoroTaskInput || '').trim();
      if (focusDuration !== current.focusDuration || task !== current.task) {
        current = buildPomodoroState(current.mode, focusDuration, task, current.completedFocusRounds);
        this.setData({ pomodoroDurationInput: String(focusDuration), pomodoroTaskInput: task });
      }
    }
    const remaining = Math.max(1, current.remaining);
    const pomodoro = { ...current, running: true, endAt: Date.now() + remaining * 1000 };
    this.updatePomodoro(pomodoro);
    this.startPomodoroInterval();
  },

  resetPomodoro() {
    const pomodoro = buildPomodoroState(
      this.data.pomodoro.mode,
      clampFocusDuration(this.data.pomodoroDurationInput, this.data.pomodoro.focusDuration),
      this.data.pomodoroTaskInput || this.data.pomodoro.task || '',
      this.data.pomodoro.completedFocusRounds
    );
    this.stopPomodoroInterval();
    this.updatePomodoro(pomodoro);
  },

  skipPomodoroPhase() {
    wx.showModal({
      title: '跳过当前阶段？',
      content: this.data.pomodoro.mode === 'focus' ? '未完成的专注不会计入本轮。' : '将直接进入下一轮专注。',
      confirmText: '跳过',
      confirmColor: '#c94f36',
      success: (result: { confirm: boolean }) => {
        if (result.confirm) this.advancePomodoro(false);
      }
    });
  },

  advancePomodoro(startImmediately = false) {
    const current = this.data.pomodoro as PomodoroState;
    let nextMode: PomodoroMode;
    let completedFocusRounds = current.completedFocusRounds;
    if (current.mode === 'focus') {
      nextMode = completedFocusRounds >= 4 ? 'longBreak' : 'shortBreak';
    } else {
      nextMode = 'focus';
      if (current.mode === 'longBreak' || completedFocusRounds >= 4) completedFocusRounds = 0;
    }
    const next = buildPomodoroState(nextMode, current.focusDuration, current.task, completedFocusRounds);
    this.stopPomodoroInterval();
    this.updatePomodoro(next);
    if (startImmediately) this.togglePomodoro();
  },

  startPomodoroInterval() {
    this.stopPomodoroInterval();
    const interval = setInterval(() => this.syncPomodoro(), 1000) as unknown as number;
    this.setData({ pomodoroInterval: interval });
  },

  stopPomodoroInterval() {
    if (this.data.pomodoroInterval) clearInterval(this.data.pomodoroInterval as unknown as number);
    this.setData({ pomodoroInterval: 0 });
  },

  syncPomodoro() {
    const pomodoro = this.data.pomodoro;
    if (!pomodoro?.running || !pomodoro.endAt) return;
    const remaining = Math.max(0, Math.ceil((pomodoro.endAt - Date.now()) / 1000));
    const completed = remaining === 0;
    const next = completed
      ? completePomodoroState(pomodoro)
      : { ...pomodoro, remaining, running: true, completed: false };
    this.updatePomodoro(next);
    if (completed) {
      this.stopPomodoroInterval();
      wx.vibrateShort({ type: 'medium' });
      wx.showToast({ title: pomodoro.mode === 'focus' ? '本轮专注完成' : '休息结束', icon: 'none' });
    }
  },

  updatePomodoro(pomodoro: PomodoroState) {
    this.setData({ pomodoro, ...buildPomodoroView(pomodoro) });
    this.persistPomodoro(pomodoro);
  },

  persistPomodoro(target?: PomodoroState) {
    const pomodoro = target || this.data.pomodoro;
    if (!pomodoro) return;
    wx.setStorageSync(POMODORO_STORAGE_KEY, pomodoro);
  },

  restorePomodoro() {
    const stored = wx.getStorageSync(POMODORO_STORAGE_KEY) as Partial<PomodoroState> | undefined;
    if (!stored) return;
    const normalized = normalizePomodoroState(stored);
    const remaining = normalized.running && normalized.endAt ? Math.max(0, Math.ceil((normalized.endAt - Date.now()) / 1000)) : normalized.remaining;
    const completed = remaining === 0;
    const pomodoro = completed && normalized.running
      ? completePomodoroState(normalized)
      : { ...normalized, remaining, running: normalized.running && !completed, completed: normalized.completed || completed };
    this.setData({
      pomodoroDurationInput: String(pomodoro.focusDuration),
      pomodoroTaskInput: pomodoro.task,
      pomodoro,
      ...buildPomodoroView(pomodoro)
    });
    this.persistPomodoro(pomodoro);
  }
});
