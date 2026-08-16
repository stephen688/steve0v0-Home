import client, { type ApiResult } from './client'

export interface PageResult<T> {
  page: number
  size: number
  total: number
  hasMore: boolean
  list: T[]
}

export interface ArticleList {
  id: number
  title: string
  summary: string | null
  coverImage: string | null
  category: string
  tags: string | null
  status: number
  viewCount: number
  publishedAt: string | null
  readTimeMinutes: number
}

export interface ArticleDetail extends ArticleList {
  content: string | null
  createdAt: string
  updatedAt: string
}

export interface ArticlePayload {
  title: string
  summary: string
  content: string
  coverImage: string
  category: string
  tags: string
  status: number
}

export interface MomentItem {
  id: number
  content: string
  mediaType: string
  mediaUrl: string | null
  location: string | null
  createdAt: string
  images: string[]
}

export interface MomentPayload {
  content: string
  mediaType: 'text' | 'image'
  images: string[]
  location?: string
  createdAt?: string
}

export interface StudyRecordList {
  id: number
  recordDate: string
  subject: string
  duration: number
  createdAt: string
}

export interface StudyRecordDetail extends StudyRecordList {
  content: string
  updatedAt: string
}

export interface StudyRecordPayload {
  recordDate: string
  subject: string
  content: string
  duration: number
}

export interface CourseItem {
  id: number
  name: string
  startDate: string
  endDate: string
  startTime: string
  endTime: string
  location: string | null
  dayOfWeek: number | null
  isRepeated: number
  createdAt: string
  updatedAt: string
}

export interface CoursePayload {
  name: string
  startDate: string
  endDate: string
  startTime: string
  endTime: string
  location: string
  dayOfWeek: number | null
  isRepeated: number
}

export interface StatusItem {
  state: string
  currentTask: string | null
  mood: string | null
}

export interface ProfileItem {
  name: string | null
  avatarUrl: string | null
}

export interface ProjectItem {
  id: number
  name: string
  description: string | null
  githubUrl: string
  techTags: string[]
  sort: number
}

export interface ProjectPayload {
  name: string
  description: string
  githubUrl: string
  techTags: string[]
  sort: number
}

export interface StatsItem {
  todayStudyMinutes: number
  todayPomodoroCount: number
  todayPomodoroMinutes: number
  weeklyStudyMinutes: number
  weeklyPomodoroCounts: Array<{ date: string; count: number }>
  streakDays: number
  heatmap: Array<{ date: string; activityCount: number }>
}

function unwrap<T>(response: { data: ApiResult<T> }): T {
  return response.data.data
}

export function getAdminArticles(params: {
  category?: string
  status?: number
  page?: number
  size?: number
} = {}) {
  return client.get<ApiResult<PageResult<ArticleList>>>('/admin/articles', { params }).then(unwrap)
}

export function getAdminArticle(id: number) {
  return client.get<ApiResult<ArticleDetail>>(`/admin/articles/${id}`).then(unwrap)
}

export function createArticle(payload: ArticlePayload) {
  return client.post<ApiResult<number>>('/admin/articles', payload).then(unwrap)
}

export function updateArticle(id: number, payload: ArticlePayload) {
  return client.put<ApiResult<null>>(`/admin/articles/${id}`, payload).then(unwrap)
}

export function deleteArticle(id: number) {
  return client.delete<ApiResult<null>>(`/admin/articles/${id}`).then(unwrap)
}

export function getAdminMoments(params: { page?: number; size?: number } = {}) {
  return client.get<ApiResult<PageResult<MomentItem>>>('/admin/moments', { params }).then(unwrap)
}

export function createMoment(payload: MomentPayload) {
  return client.post<ApiResult<number>>('/admin/moments', payload).then(unwrap)
}

export function deleteMoment(id: number) {
  return client.delete<ApiResult<null>>(`/admin/moments/${id}`).then(unwrap)
}

export function getStudyRecords(params: {
  startDate?: string
  endDate?: string
  page?: number
  size?: number
} = {}) {
  return client.get<ApiResult<PageResult<StudyRecordList>>>('/admin/study-records', { params }).then(unwrap)
}

export function getStudyRecord(id: number) {
  return client.get<ApiResult<StudyRecordDetail>>(`/admin/study-records/${id}`).then(unwrap)
}

export function createStudyRecord(payload: StudyRecordPayload) {
  return client.post<ApiResult<number>>('/admin/study-records', payload).then(unwrap)
}

export function updateStudyRecord(id: number, payload: StudyRecordPayload) {
  return client.put<ApiResult<null>>(`/admin/study-records/${id}`, payload).then(unwrap)
}

export function deleteStudyRecord(id: number) {
  return client.delete<ApiResult<null>>(`/admin/study-records/${id}`).then(unwrap)
}

export function getCourses(params: { page?: number; size?: number } = {}) {
  return client.get<ApiResult<PageResult<CourseItem>>>('/admin/courses', { params }).then(unwrap)
}

export function getCourse(id: number) {
  return client.get<ApiResult<CourseItem>>(`/admin/courses/${id}`).then(unwrap)
}

export function createCourse(payload: CoursePayload) {
  return client.post<ApiResult<number>>('/admin/courses', payload).then(unwrap)
}

export function updateCourse(id: number, payload: CoursePayload) {
  return client.put<ApiResult<null>>(`/admin/courses/${id}`, payload).then(unwrap)
}

export function deleteCourse(id: number) {
  return client.delete<ApiResult<null>>(`/admin/courses/${id}`).then(unwrap)
}

export function getStatus() {
  return client.get<ApiResult<StatusItem>>('/home/status').then(unwrap)
}

export function updateStatus(payload: Partial<StatusItem>) {
  return client.put<ApiResult<null>>('/admin/status', payload).then(unwrap)
}

export function getProfile() {
  return client.get<ApiResult<ProfileItem>>('/admin/about/profile').then(unwrap)
}

export function updateProfile(payload: ProfileItem) {
  return client.put<ApiResult<null>>('/admin/about/profile', payload).then(unwrap)
}

export function getProjects() {
  return client.get<ApiResult<ProjectItem[]>>('/admin/about/projects').then(unwrap)
}

export function getProject(id: number) {
  return client.get<ApiResult<ProjectItem>>(`/admin/about/projects/${id}`).then(unwrap)
}

export function createProject(payload: ProjectPayload) {
  return client.post<ApiResult<number>>('/admin/about/projects', payload).then(unwrap)
}

export function updateProject(id: number, payload: ProjectPayload) {
  return client.put<ApiResult<null>>(`/admin/about/projects/${id}`, payload).then(unwrap)
}

export function deleteProject(id: number) {
  return client.delete<ApiResult<null>>(`/admin/about/projects/${id}`).then(unwrap)
}

export function getStats() {
  return client.get<ApiResult<StatsItem>>('/home/stats').then(unwrap)
}

export async function uploadImage(file: File, onProgress?: (progress: number) => void): Promise<string> {
  const formData = new FormData()
  formData.append('file', file)
  const result = await client.post<ApiResult<{ url: string }>>('/admin/upload', formData, {
    headers: { 'Content-Type': 'multipart/form-data' },
    onUploadProgress: (event) => {
      if (event.total) onProgress?.(Math.round((event.loaded / event.total) * 100))
    }
  })
  onProgress?.(100)
  return unwrap(result).url
}

export interface MarkdownUploadResult {
  fileName: string
  content: string
}

export async function uploadMarkdown(file: File): Promise<MarkdownUploadResult> {
  const formData = new FormData()
  formData.append('file', file)
  const result = await client.post<ApiResult<MarkdownUploadResult>>('/admin/upload/markdown', formData, {
    headers: { 'Content-Type': 'multipart/form-data' }
  })
  return unwrap(result)
}

export function resolveAssetUrl(url: string | null | undefined): string {
  if (!url) return ''
  if (/^https?:\/\//i.test(url)) return url
  const configuredBase = (import.meta.env.VITE_API_BASE as string | undefined)?.replace(/\/$/, '')
  if (configuredBase) {
    const origin = configuredBase.replace(/\/api$/, '')
    return `${origin}${url.startsWith('/') ? url : `/${url}`}`
  }
  return url
}
