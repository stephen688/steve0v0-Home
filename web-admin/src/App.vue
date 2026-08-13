<script setup lang="ts">
import { useThemeStore } from '@/stores/theme'
import ToastHost from '@/components/ui/ToastHost.vue'

// 确保主题 store 在应用启动时即被初始化
useThemeStore()
</script>

<template>
  <router-view />
  <ToastHost />
</template>
