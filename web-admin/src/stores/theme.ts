import { defineStore } from 'pinia'
import { ref, watchEffect } from 'vue'

type Theme = 'light' | 'dark' | 'system'

const THEME_KEY = 'steve_admin_theme'

/**
 * 主题状态管理
 * 支持浅色 / 深色 / 跟随系统，持久化到 localStorage
 */
export const useThemeStore = defineStore('theme', () => {
  const theme = ref<Theme>((localStorage.getItem(THEME_KEY) as Theme) || 'light')

  /**
   * 根据当前主题值计算是否应该应用深色模式
   */
  function isDarkMode(current: Theme): boolean {
    if (current === 'system') {
      return window.matchMedia('(prefers-color-scheme: dark)').matches
    }
    return current === 'dark'
  }

  /**
   * 应用主题到 documentElement
   */
  function applyTheme(current: Theme) {
    const root = document.documentElement
    if (isDarkMode(current)) {
      root.classList.add('dark')
    } else {
      root.classList.remove('dark')
    }
  }

  /**
   * 设置主题
   */
  function setTheme(value: Theme) {
    theme.value = value
    localStorage.setItem(THEME_KEY, value)
    applyTheme(value)
  }

  /**
   * 切换浅色 / 深色
   */
  function toggle() {
    setTheme(theme.value === 'dark' ? 'light' : 'dark')
  }

  // 初始化：立即应用一次，并监听系统主题变化
  applyTheme(theme.value)
  window.matchMedia('(prefers-color-scheme: dark)').addEventListener('change', () => {
    if (theme.value === 'system') {
      applyTheme('system')
    }
  })

  // 保持响应式同步
  watchEffect(() => {
    applyTheme(theme.value)
  })

  return {
    theme,
    setTheme,
    toggle
  }
})
