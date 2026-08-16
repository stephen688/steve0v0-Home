export interface ApiResult<T> {
  code: number;
  message: string;
  data: T;
}

export interface PageResult<T> {
  page: number;
  size: number;
  total: number;
  hasMore: boolean;
  list: T[];
}

export interface CalendarData {
  view: 'month' | 'week';
  startDate: string;
  endDate: string;
  records: StudyRecordItem[];
  courses: CourseItem[];
}

export interface StudyRecordItem {
  id: number;
  recordDate: string;
  subject: string;
  duration: number;
}

export interface CourseItem {
  id: number;
  date: string;
  name: string;
  startTime: string;
  endTime: string;
  location: string;
}

export interface WeeklyPomodoroCount {
  date: string;
  count: number;
}

export interface HeatmapItem {
  date: string;
  activityCount: number;
}

export interface StatsData {
  todayStudyMinutes: number;
  todayPomodoroCount: number;
  todayPomodoroMinutes: number;
  weeklyStudyMinutes: number;
  weeklyPomodoroCounts: WeeklyPomodoroCount[];
  streakDays: number;
  heatmap: HeatmapItem[];
}

export interface StatusData {
  state: string;
  currentTask: string;
  mood: string;
}

export interface ArticleListItem {
  id: number;
  title: string;
  summary: string;
  coverImage: string;
  category: 'tech' | 'life' | string;
  tags: string;
  viewCount: number;
  publishedAt: string;
  readTimeMinutes: number;
}

export interface ArticleDetail extends ArticleListItem {
  content: string;
  status: number;
  createdAt: string;
  updatedAt: string;
}

export interface MomentItem {
  id: number;
  content: string;
  mediaType: string;
  mediaUrl: string;
  location: string;
  createdAt: string;
  images: string[];
}

export interface AboutProfile {
  name: string;
  avatarUrl: string;
}

export interface AboutProject {
  id: number;
  name: string;
  description: string;
  githubUrl: string;
  techTags: string[];
  sort: number;
}

export interface MarkdownLink {
  text: string;
  url: string;
}
