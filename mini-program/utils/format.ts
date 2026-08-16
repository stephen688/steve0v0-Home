export function formatDate(value: string | Date, format = 'YYYY-MM-DD'): string {
  const date = typeof value === 'string' ? parseDateValue(value) : value;
  if (Number.isNaN(date.getTime())) return '日期未知';
  const parts: Record<string, string> = {
    YYYY: String(date.getFullYear()),
    MM: String(date.getMonth() + 1).padStart(2, '0'),
    DD: String(date.getDate()).padStart(2, '0'),
    HH: String(date.getHours()).padStart(2, '0'),
    mm: String(date.getMinutes()).padStart(2, '0')
  };
  return format.replace(/YYYY|MM|DD|HH|mm/g, (token) => parts[token]);
}

function parseDateValue(value: string): Date {
  const normalized = String(value).trim();
  if (!normalized) return new Date('invalid');
  if (/^\d{4}-\d{2}-\d{2}$/.test(normalized)) return new Date(`${normalized}T00:00:00`);
  const isoDate = new Date(normalized);
  if (!Number.isNaN(isoDate.getTime())) return isoDate;
  return new Date(normalized.replace(/-/g, '/'));
}

export function formatMinutes(minutes: number | null | undefined): string {
  const value = Math.max(0, Number(minutes) || 0);
  if (value < 60) return `${value} 分钟`;
  const hours = Math.floor(value / 60);
  const rest = value % 60;
  return rest ? `${hours} 小时 ${rest} 分钟` : `${hours} 小时`;
}

export function parseTags(value: string | string[] | null | undefined): string[] {
  if (Array.isArray(value)) return value.filter(Boolean).map(String);
  if (!value) return [];
  const text = String(value).trim();
  if (!text) return [];
  try {
    const parsed = JSON.parse(text);
    if (Array.isArray(parsed)) return parsed.filter(Boolean).map(String);
  } catch {
    // 兼容历史逗号、中文顿号和空格分隔的标签。
  }
  return text.split(/[,，、|\s]+/).map((item) => item.trim()).filter(Boolean);
}

export function formatState(value: string | null | undefined): string {
  const map: Record<string, string> = {
    online: '在线',
    studying: '学习中',
    exercising: '运动中',
    busy: '忙碌',
    rest: '休息',
    resting: '休息'
  };
  return map[value || ''] || value || '状态待更新';
}

export function formatDateTime(value: string): string {
  return formatDate(value, 'YYYY-MM-DD HH:mm');
}
