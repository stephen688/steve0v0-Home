<script setup lang="ts">
withDefaults(defineProps<{
  open: boolean
  title: string
  description: string
  confirmLabel?: string
  loading?: boolean
}>(), {
  confirmLabel: '确认删除',
  loading: false
})

defineEmits<{
  cancel: []
  confirm: []
}>()
</script>

<template>
  <div
    v-if="open"
    class="dialog-layer"
    role="dialog"
    aria-modal="true"
    :aria-label="title"
    @keydown.esc="$emit('cancel')"
  >
    <button class="dialog-backdrop" type="button" aria-label="关闭确认框" @click="$emit('cancel')" />
    <div class="dialog-card">
      <div class="dialog-mark">!</div>
      <div>
        <h2 class="dialog-title">{{ title }}</h2>
        <p class="dialog-description">{{ description }}</p>
      </div>
      <div class="dialog-actions">
        <button class="secondary-button" type="button" :disabled="loading" @click="$emit('cancel')">取消</button>
        <button class="danger-button" type="button" :disabled="loading" @click="$emit('confirm')">
          <span v-if="loading" class="button-spinner" />
          {{ loading ? '处理中…' : confirmLabel }}
        </button>
      </div>
    </div>
  </div>
</template>
