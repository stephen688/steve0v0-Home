import { defineStore } from 'pinia'
import { computed, ref } from 'vue'
import { login as loginApi, type LoginResult } from '@/api/auth'

const TOKEN_KEY = 'steve_admin_token'
const EXPIRE_KEY = 'steve_admin_expire'

/**
 * 认证状态管理
 * 负责 token 的存取、登录/登出、登录态判断
 */
export const useAuthStore = defineStore('auth', () => {
  const token = ref<string | null>(localStorage.getItem(TOKEN_KEY))
  const expireAt = ref<number | null>(
    localStorage.getItem(EXPIRE_KEY) ? Number(localStorage.getItem(EXPIRE_KEY)) : null
  )
  const loading = ref(false)
  const error = ref<string | null>(null)

  const isAuthenticated = computed(() => {
    if (!token.value) return false
    if (!expireAt.value) return true
    return Date.now() < expireAt.value
  })

  /**
   * 存储登录凭证
   */
  function persistCredentials(result: LoginResult) {
    token.value = result.token
    expireAt.value = result.expireAt
    localStorage.setItem(TOKEN_KEY, result.token)
    localStorage.setItem(EXPIRE_KEY, String(result.expireAt))
  }

  /**
   * 暗号登录
   */
  async function login(secret: string) {
    loading.value = true
    error.value = null
    try {
      const result = await loginApi(secret)
      persistCredentials(result)
      return true
    } catch (err: unknown) {
      error.value = err instanceof Error ? err.message : '登录失败'
      throw err
    } finally {
      loading.value = false
    }
  }

  /**
   * 登出：清除本地凭证
   */
  function logout() {
    token.value = null
    expireAt.value = null
    localStorage.removeItem(TOKEN_KEY)
    localStorage.removeItem(EXPIRE_KEY)
  }

  return {
    token,
    expireAt,
    loading,
    error,
    isAuthenticated,
    login,
    logout
  }
})
