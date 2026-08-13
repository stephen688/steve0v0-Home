<script setup lang="ts">
import { computed, ref, onMounted, onUnmounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { CalendarDays, FileText, Image, BookOpen, Menu } from '@lucide/vue'

defineEmits<{
  openMenu: []
}>()

const route = useRoute()
const router = useRouter()

const pageTitle = computed(() => {
  const metaTitle = route.meta.title as string | undefined
  if (metaTitle) return metaTitle
  if (route.name === 'Dashboard') return '仪表盘'
  return ''
})

const breadcrumb = computed(() => {
  const metaTitle = route.meta.title as string | undefined
  if (!metaTitle || route.name === 'Dashboard') return ''
  return metaTitle
})

const todayLabel = computed(() => {
  const now = new Date()
  const weekdays = ['日', '一', '二', '三', '四', '五', '六']
  const month = String(now.getMonth() + 1).padStart(2, '0')
  const day = String(now.getDate()).padStart(2, '0')
  return `${now.getFullYear()}-${month}-${day} · 周${weekdays[now.getDay()]}`
})

const quickActions = [
  { label: '写新文章', icon: FileText, path: '/articles/new' },
  { label: '发布动态', icon: Image, path: '/moments/new' },
  { label: '添加记录', icon: BookOpen, path: '/study-records/new' }
]

function navigate(path: string) {
  router.push(path)
}

// 滚动时给顶栏加阴影，模拟纸张分页
const isScrolled = ref(false)
function handleScroll() {
  isScrolled.value = window.scrollY > 8
}

onMounted(() => {
  window.addEventListener('scroll', handleScroll, { passive: true })
})

onUnmounted(() => {
  window.removeEventListener('scroll', handleScroll)
})
</script>

<template>
  <header
    class="sticky top-0 z-30 h-16 border-b border-border bg-background transition-shadow duration-fast"
    :class="isScrolled ? 'shadow-sm' : ''"
  >
    <div class="flex h-full items-center justify-between px-6 lg:px-10">
      <!-- 左侧：面包屑 + 页面标题 -->
      <div class="flex min-w-0 flex-col justify-center">
        <nav v-if="breadcrumb" class="breadcrumb mb-0.5" aria-label="面包屑">
          <span class="text-muted-foreground">小屋日志</span>
          <span class="breadcrumb-sep">/</span>
          <span class="breadcrumb-current">{{ breadcrumb }}</span>
        </nav>
        <h1 class="truncate font-serif text-2xl tracking-tight">{{ pageTitle || '仪表盘' }}</h1>
      </div>

      <!-- 右侧：日期 + 快速操作 + 管理员头像 -->
      <div class="flex items-center gap-3">
        <span class="topbar-date hidden xl:inline-flex">
          <CalendarDays class="h-3.5 w-3.5" />
          {{ todayLabel }}
        </span>

        <button
          v-for="action in quickActions"
          :key="action.path"
          type="button"
          class="hidden lg:flex items-center gap-2 rounded-lg border border-transparent bg-secondary px-3 py-2 text-sm font-medium text-secondary-foreground transition-colors duration-fast hover:bg-muted"
          @click="navigate(action.path)"
        >
          <component :is="action.icon" class="h-4 w-4" />
          <span>{{ action.label }}</span>
        </button>

        <button
          type="button"
          class="flex md:hidden h-9 w-9 items-center justify-center rounded-lg bg-secondary text-secondary-foreground transition-colors duration-fast hover:bg-muted"
          aria-label="打开导航"
          @click="$emit('openMenu')"
        >
          <Menu class="h-5 w-5" />
        </button>

        <div class="avatar" aria-hidden="true">S</div>
      </div>
    </div>
  </header>
</template>
