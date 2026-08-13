<script setup lang="ts">
import { computed, ref } from 'vue'
import { useRoute } from 'vue-router'
import Sidebar from '@/components/layout/Sidebar.vue'
import TopBar from '@/components/layout/TopBar.vue'

const route = useRoute()
const mobileNavOpen = ref(false)

/**
 * 页面宽度分级
 * - wide:  最大 1400px，列表/仪表盘/卡片流
 * - narrow: 最大 680px，左对齐，表单页
 */
const contentWidth = computed(() => {
  const width = route.meta.width as 'wide' | 'narrow' | undefined
  if (width === 'narrow') return 'max-w-[680px]'
  return 'max-w-[1400px]'
})
</script>

<template>
  <div class="min-h-svh flex">
    <!-- 侧边栏：固定 240px -->
    <Sidebar :mobile-open="mobileNavOpen" @close="mobileNavOpen = false" />

    <!-- 主内容区：左侧留出侧边栏宽度 -->
    <div class="flex-1 ml-0 md:ml-[240px] flex flex-col min-w-0">
      <TopBar @open-menu="mobileNavOpen = true" />
      <main class="flex-1 px-6 lg:px-10 py-6 lg:py-8">
        <div class="page-enter" :class="contentWidth">
          <router-view />
        </div>
      </main>
      <!-- 底部页脚：航海日志署名 -->
      <footer class="px-6 lg:px-10 pb-6">
        <div class="flex items-center justify-between gap-4 border-t border-border/60 pt-4">
          <span class="mono text-[0.6875rem] tracking-[0.08em] text-muted-foreground/70 uppercase">
            Steve0v0 · Cabin Log
          </span>
          <span class="mono text-[0.6875rem] text-muted-foreground/60">
            v0.3 · 质感增强
          </span>
        </div>
      </footer>
    </div>
  </div>
</template>
