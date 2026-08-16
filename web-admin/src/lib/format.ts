export function formatDate(value: string | null | undefined): string {
  return value ? value.slice(0, 10) : '—'
}

export function formatDateTime(value: string | null | undefined): string {
  if (!value) return '—'
  return value.replace('T', ' ').slice(0, 16)
}

export function formatTime(value: string | null | undefined): string {
  return value ? value.slice(0, 5) : '—'
}

export function formatDuration(minutes: number | null | undefined): string {
  if (!minutes && minutes !== 0) return '—'
  if (minutes < 60) return `${minutes} 分钟`
  const hours = Math.floor(minutes / 60)
  const rest = minutes % 60
  return rest ? `${hours} 小时 ${rest} 分钟` : `${hours} 小时`
}

export function categoryLabel(category: string | null | undefined): string {
  return category === 'life' ? '生活文章' : '技术博客'
}

export function stateLabel(state: string | null | undefined): string {
  const labels: Record<string, string> = {
    online: '在线',
    studying: '学习中',
    exercising: '运动中',
    busy: '忙碌',
    rest: '休息'
  }
  return labels[state || ''] || '在线'
}

export function dayOfWeekLabel(day: number | null | undefined): string {
  return day ? `周${['一', '二', '三', '四', '五', '六', '日'][day - 1] || ''}` : '—'
}

export function parseTags(tags: string | null | undefined): string[] {
  return (tags || '').split(',').map((tag) => tag.trim()).filter(Boolean)
}

export function isValidGithubUrl(value: string): boolean {
  try {
    const url = new URL(value)
    return url.protocol === 'https:' && url.hostname.toLowerCase() === 'github.com' && url.pathname.split('/').filter(Boolean).length >= 2
  } catch {
    return false
  }
}
