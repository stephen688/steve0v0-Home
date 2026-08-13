import client from './client'

export interface LoginResult {
  token: string
  expireAt: number
}

/**
 * 管理员暗号登录
 * @param secret 暗号
 */
export async function login(secret: string): Promise<LoginResult> {
  const response = await client.post('/admin/auth', { secret })
  return response.data.data as LoginResult
}
