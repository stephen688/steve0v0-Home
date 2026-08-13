<script setup lang="ts">
import { computed } from 'vue'
import { ChevronLeft, ChevronRight } from '@lucide/vue'

const props = defineProps<{
  page: number
  size: number
  total: number
  hasMore: boolean
}>()

const emit = defineEmits<{
  change: [page: number]
}>()

const totalPages = computed(() => Math.max(1, Math.ceil(props.total / props.size)))
const canPrevious = computed(() => props.page > 1)
const canNext = computed(() => props.hasMore || props.page < totalPages.value)
</script>

<template>
  <div v-if="total > 0" class="pagination-bar">
    <span class="page-indicator">共 <strong>{{ total }}</strong> 条 · 第 <strong>{{ page }}</strong> / {{ totalPages }} 页</span>
    <div class="pagination-actions">
      <button class="icon-button" type="button" :disabled="!canPrevious" aria-label="上一页" @click="emit('change', page - 1)">
        <ChevronLeft class="h-4 w-4" />
      </button>
      <button class="icon-button" type="button" :disabled="!canNext" aria-label="下一页" @click="emit('change', page + 1)">
        <ChevronRight class="h-4 w-4" />
      </button>
    </div>
  </div>
</template>
