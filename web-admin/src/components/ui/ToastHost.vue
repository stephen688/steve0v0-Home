<script setup lang="ts">
import { CheckCircle2, CircleAlert, Info, X } from '@lucide/vue'
import { useToast, type ToastTone } from '@/composables/useToast'

const { toasts, dismissToast } = useToast()

function iconFor(tone: ToastTone) {
  return { success: CheckCircle2, error: CircleAlert, info: Info }[tone]
}
</script>

<template>
  <div class="toast-stack" aria-live="polite" aria-atomic="true">
    <div v-for="toast in toasts" :key="toast.id" class="toast-card" :class="`toast-${toast.tone}`">
      <component :is="iconFor(toast.tone)" class="h-4 w-4 shrink-0" />
      <span>{{ toast.message }}</span>
      <button class="toast-close" type="button" aria-label="关闭通知" @click="dismissToast(toast.id)">
        <X class="h-3.5 w-3.5" />
      </button>
    </div>
  </div>
</template>
