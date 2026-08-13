import { request } from './http';
import {
  AboutProfile,
  AboutProject,
  ArticleDetail,
  ArticleListItem,
  CalendarData,
  MomentItem,
  PageResult,
  StatsData,
  StatusData
} from '../types/api';

export const api = {
  getStatus: () => request<StatusData>('/api/home/status'),
  getStats: () => request<StatsData>('/api/home/stats'),
  getCalendar: (view: 'month' | 'week', date: string) => request<CalendarData>('/api/home/calendar', { data: { view, date } }),
  getArticles: (category?: string, page = 1, size = 10) => request<PageResult<ArticleListItem>>('/api/articles', { data: { ...(category ? { category } : {}), page, size } }),
  getArticle: (id: number) => request<ArticleDetail>(`/api/articles/${id}`),
  getMoments: (page = 1, size = 10, order: 'latest' | 'earliest' = 'latest') => request<PageResult<MomentItem>>('/api/moments', { data: { page, size, order } }),
  getProfile: () => request<AboutProfile>('/api/about/profile'),
  getProjects: () => request<AboutProject[]>('/api/about/projects')
};
