import { ref } from 'vue'

export type ToastTone = 'success' | 'error' | 'info'

export interface ToastItem {
  id: number
  tone: ToastTone
  message: string
}

const toasts = ref<ToastItem[]>([])
let nextToastId = 1

export function useToast() {
  function dismissToast(id: number) {
    toasts.value = toasts.value.filter((toast) => toast.id !== id)
  }

  function showToast(message: string, tone: ToastTone = 'success', duration = tone === 'error' ? 5000 : 3500) {
    const id = nextToastId++
    toasts.value = [...toasts.value, { id, tone, message }].slice(-3)
    window.setTimeout(() => dismissToast(id), duration)
  }

  return { toasts, showToast, dismissToast }
}
