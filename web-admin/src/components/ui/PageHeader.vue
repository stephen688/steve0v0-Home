<script setup lang="ts">
defineProps<{
  title: string
  description?: string
}>()
</script>

<template>
  <div class="page-header">
    <div>
      <p class="eyebrow">小屋日志 / 管理</p>
      <h2 class="page-title">{{ title }}</h2>
      <p v-if="description" class="page-description">{{ description }}</p>
    </div>
    <div class="page-header-actions">
      <slot name="actions" />
    </div>
  </div>
</template>
