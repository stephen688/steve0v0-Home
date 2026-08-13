import { ENV } from '../config/env';
import { ApiResult } from '../types/api';

export class ApiError extends Error {
  statusCode: number;
  code: number;

  constructor(message: string, statusCode = 0, code = 0) {
    super(message);
    this.name = 'ApiError';
    this.statusCode = statusCode;
    this.code = code;
  }
}

function getBaseUrl(): string {
  const app = getApp<any>();
  return app?.globalData?.apiBase || ENV.apiBase;
}

export function request<T>(path: string, options: { method?: 'GET' | 'POST'; data?: Record<string, any> } = {}): Promise<T> {
  return new Promise((resolve, reject) => {
    wx.request({
      url: `${getBaseUrl()}${path}`,
      method: options.method || 'GET',
      data: options.data,
      timeout: 12000,
      header: {
        Accept: 'application/json',
        // 免费 ngrok 域名会向未携带此请求头的客户端返回浏览器提示页。
        'ngrok-skip-browser-warning': 'true'
      },
      success(response: any) {
        const payload = response.data as ApiResult<T>;
        if (response.statusCode < 200 || response.statusCode >= 300) {
          reject(new ApiError(payload?.message || '网络请求失败', response.statusCode, payload?.code || response.statusCode));
          return;
        }
        if (!payload || payload.code !== 200) {
          reject(new ApiError(payload?.message || '内容暂时无法加载', response.statusCode, payload?.code || 0));
          return;
        }
        resolve(payload.data);
      },
      fail(error: any) {
        reject(new ApiError(error?.errMsg || '网络连接失败，请稍后重试'));
      }
    });
  });
}
