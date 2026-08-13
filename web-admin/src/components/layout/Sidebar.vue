<script setup lang="ts">
import { computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import {
  Compass,
  LayoutDashboard,
  FileText,
  Image,
  BookOpen,
  Calendar,
  Activity,
  User,
  Code,
  LogOut,
  Sun,
  Moon
} from '@lucide/vue'
import { useAuthStore } from '@/stores/auth'
import { useThemeStore } from '@/stores/theme'

interface NavItem {
  name: string
  path: string
  icon: unknown
}

interface NavGroup {
  label: string
  items: NavItem[]
}

const navGroups: NavGroup[] = [
  {
    label: '总览',
    items: [{ name: '仪表盘', path: '/', icon: LayoutDashboard }]
  },
  {
    label: '内容',
    items: [
      { name: '文章管理', path: '/articles', icon: FileText },
      { name: '动态管理', path: '/moments', icon: Image }
    ]
  },
  {
    label: '学习',
    items: [
      { name: '学习记录', path: '/study-records', icon: BookOpen },
      { name: '课程表', path: '/courses', icon: Calendar }
    ]
  },
  {
    label: '关于',
    items: [
      { name: '个人状态', path: '/status', icon: Activity },
      { name: '个人资料', path: '/about/profile', icon: User },
      { name: 'GitHub 项目', path: '/about/projects', icon: Code }
    ]
  }
]

const route = useRoute()
const router = useRouter()
const authStore = useAuthStore()
const themeStore = useThemeStore()

const isActive = (path: string) => {
  if (path === '/') {
    return route.path === '/'
  }
  return route.path.startsWith(path)
}

const themeIcon = computed(() => (themeStore.theme === 'dark' ? Sun : Moon))
const themeLabel = computed(() => (themeStore.theme === 'dark' ? '切换浅色' : '切换深色'))

withDefaults(defineProps<{
  mobileOpen?: boolean
}>(), {
  mobileOpen: false
})

defineEmits<{
  close: []
}>()

function handleLogout() {
  authStore.logout()
  router.push({ name: 'Login' })
}
</script>

<template>
  <button
    v-if="mobileOpen"
    class="fixed inset-0 z-40 bg-black/40 md:hidden"
    type="button"
    aria-label="关闭导航"
    @click="$emit('close')"
  />
  <aside
    class="fixed left-0 top-0 z-40 h-svh w-[280px] flex-col border-r border-border bg-secondary md:w-[240px]"
    :class="mobileOpen ? 'flex' : 'hidden md:flex'"
  >
    <!-- 品牌区 -->
    <div class="flex h-16 items-center gap-3 border-b border-border px-6">
      <div
        class="flex h-9 w-9 items-center justify-center rounded-lg bg-primary text-primary-foreground shadow-sm"
      >
        <Compass class="h-5 w-5" />
      </div>
      <div class="flex flex-col leading-tight">
        <span class="font-serif text-xl font-bold tracking-tight text-foreground">小屋日志</span>
        <span class="mono mt-0.5 text-[0.625rem] tracking-[0.16em] text-muted-foreground uppercase">Cabin Log</span>
      </div>
    </div>

    <!-- 导航 -->
    <nav class="flex-1 overflow-y-auto px-4 py-5">
      <ul v-for="group in navGroups" :key="group.label" class="mb-1">
        <li class="nav-section-label">{{ group.label }}</li>
        <li v-for="item in group.items" :key="item.path" class="mb-0.5">
          <router-link
            :to="item.path"
            :class="[
              'flex items-center gap-3 rounded-lg px-4 py-2.5 text-sm font-medium transition-colors duration-fast',
              isActive(item.path)
                ? 'bg-muted text-primary relative before:absolute before:left-0 before:top-1/2 before:-translate-y-1/2 before:h-5 before:w-[3px] before:rounded-r-full before:bg-primary'
                : 'text-muted-foreground hover:bg-muted hover:text-foreground'
            ]"
            @click="$emit('close')"
          >
            <component :is="item.icon" class="h-[18px] w-[18px]" />
            <span>{{ item.name }}</span>
          </router-link>
        </li>
      </ul>
    </nav>

    <!-- 底部：主题切换 + 退出 -->
    <div class="border-t border-border p-4">
      <div class="flex flex-col gap-1">
        <button
          type="button"
          class="flex w-full items-center gap-3 rounded-lg px-4 py-2.5 text-sm font-medium text-muted-foreground transition-colors duration-fast hover:bg-muted hover:text-foreground"
          @click="themeStore.toggle"
        >
          <component :is="themeIcon" class="h-[18px] w-[18px]" />
          <span>{{ themeLabel }}</span>
        </button>
        <button
          type="button"
          class="flex w-full items-center gap-3 rounded-lg px-4 py-2.5 text-sm font-medium text-muted-foreground transition-colors duration-fast hover:bg-muted hover:text-destructive"
          @click="handleLogout"
        >
          <LogOut class="h-[18px] w-[18px]" />
          <span>退出登录</span>
        </button>
      </div>
    </div>
  </aside>
</template>
