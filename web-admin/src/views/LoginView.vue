<script setup lang="ts">
import { ref, computed, onUnmounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { Compass, LoaderCircle, Eye, EyeOff } from '@lucide/vue'
import { useAuthStore } from '@/stores/auth'
import { BusinessError } from '@/api/client'

const route = useRoute()
const router = useRouter()
const authStore = useAuthStore()

const secret = ref('')
const error = ref('')
const isLoading = ref(false)
const isRateLimited = ref(false)
const countdown = ref(0)
const showSecret = ref(false)
let countdownTimer: ReturnType<typeof setInterval> | null = null

const submitDisabled = computed(() => isLoading.value || isRateLimited.value || !secret.value.trim())
const buttonText = computed(() => {
  if (isRateLimited.value) return `${countdown.value}s 后重试`
  if (isLoading.value) return '登录中…'
  return '登录'
})

/**
 * 启动限流倒计时
 */
function startCountdown(seconds: number) {
  isRateLimited.value = true
  countdown.value = seconds
  countdownTimer = setInterval(() => {
    countdown.value -= 1
    if (countdown.value <= 0) {
      clearInterval(countdownTimer!)
      countdownTimer = null
      isRateLimited.value = false
    }
  }, 1000)
}

/**
 * 提交登录
 */
async function handleSubmit() {
  if (submitDisabled.value) return

  error.value = ''
  isLoading.value = true

  try {
    const success = await authStore.login(secret.value.trim())
    if (success) {
      const redirect = route.query.redirect as string | undefined
      router.push(redirect || '/')
      return
    }
  } catch (err: unknown) {
    if (err instanceof BusinessError && err.code === 429) {
      startCountdown(60)
      error.value = err.message || '尝试次数过多，请 1 分钟后重试'
    } else if (err instanceof BusinessError) {
      error.value = err.message || '暗号错误'
    } else if (err instanceof Error) {
      error.value = err.message
    } else {
      error.value = '登录失败，请稍后重试'
    }
  } finally {
    isLoading.value = false
  }
}

/**
 * 回车提交
 * 中文输入法选字时按回车也会触发 keydown，需跳过（e.isComposing）
 */
function handleKeydown(e: KeyboardEvent) {
  if (e.key === 'Enter' && !e.isComposing) {
    handleSubmit()
  }
}

onUnmounted(() => {
  if (countdownTimer) {
    clearInterval(countdownTimer)
  }
})
</script>

<template>
  <!-- 登录页强制深色皮革质感，始终使用 dark 变量 -->
  <div
    class="dark fixed inset-0 z-0 flex min-h-svh w-full items-center justify-center"
    style="background: radial-gradient(ellipse at top, hsl(24 25% 22%), hsl(24 20% 14%))"
  >
    <!-- 页面噪点层：登录页独立、更淡的噪点 -->
    <div
      class="pointer-events-none absolute inset-0"
      style="
        background-image: url(&quot;data:image/svg+xml,%3Csvg viewBox='0 0 128 128' xmlns='http://www.w3.org/2000/svg'%3E%3Cfilter id='noiseFilter'%3E%3CfeTurbulence type='fractalNoise' baseFrequency='0.85' numOctaves='4' stitchTiles='stitch'/%3E%3C/filter%3E%3Crect width='100%25' height='100%25' filter='url(%23noiseFilter)' opacity='0.03'/%3E%3C/svg%3E&quot;);
        background-size: 128px 128px;
      "
    />

    <!-- 笔记本封面卡片 -->
    <div
      class="login-cover relative z-10 w-full max-w-[400px] overflow-hidden rounded-2xl border border-border bg-card p-8 shadow-lg"
      style="box-shadow: var(--shadow-lg)"
    >
      <!-- 罗盘压印线稿：登录页唯一额外装饰 -->
      <div class="pointer-events-none absolute -right-8 -top-8 opacity-[0.08]">
        <svg width="220" height="220" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="0.6" class="text-foreground">
          <circle cx="12" cy="12" r="10" />
          <path d="M12 2v20M2 12h20" />
          <path d="m16.24 7.76-4.24 4.24-4.24-4.24M16.24 16.24l-4.24-4.24-4.24 4.24" />
          <polygon points="12,4 14,10 20,12 14,14 12,20 10,14 4,12 10,10" />
        </svg>
      </div>

      <div class="relative z-10">
        <!-- 顶部品牌 -->
        <div class="mb-8 text-center">
          <div
            class="mx-auto mb-4 flex h-14 w-14 items-center justify-center rounded-xl bg-primary text-primary-foreground shadow-md"
          >
            <Compass class="h-7 w-7" />
          </div>
          <h1 class="font-serif text-3xl font-normal tracking-tight text-foreground">小屋日志</h1>
          <p class="mt-1 text-sm text-muted-foreground">管理员登录</p>
        </div>

        <!-- 登录表单 -->
        <div class="space-y-4">
          <div>
            <label for="secret" class="mb-1.5 block text-sm font-medium text-foreground">
              暗号
            </label>
            <div class="relative">
              <input
                id="secret"
                v-model="secret"
                type="text"
                autocomplete="off"
                spellcheck="false"
                placeholder="请输入管理员暗号"
                class="w-full rounded-lg border bg-card px-4 py-2.5 pr-10 text-foreground placeholder:text-muted-foreground outline-none transition-all duration-fast focus:border-primary focus:ring-2 focus:ring-primary/30"
                :class="[
                  error ? 'border-destructive' : 'border-input',
                  { 'secret-masked': !showSecret }
                ]"
                :disabled="isLoading || isRateLimited"
                @keydown="handleKeydown"
              />
              <button
                type="button"
                class="absolute right-2 top-1/2 -translate-y-1/2 flex h-7 w-7 items-center justify-center rounded-md text-muted-foreground transition-colors hover:bg-muted hover:text-foreground"
                tabindex="-1"
                @click="showSecret = !showSecret"
              >
                <component :is="showSecret ? EyeOff : Eye" class="h-4 w-4" />
              </button>
            </div>
            <p v-if="error" class="mt-2 text-sm text-destructive">
              {{ error }}
            </p>
          </div>

          <button
            type="button"
            class="relative flex w-full items-center justify-center gap-2 rounded-lg bg-primary px-4 py-2.5 text-sm font-medium text-primary-foreground btn-primary-press disabled:cursor-not-allowed disabled:bg-muted disabled:text-muted-foreground disabled:shadow-none"
            :disabled="submitDisabled"
            @click="handleSubmit"
          >
            <LoaderCircle v-if="isLoading" class="h-4 w-4 animate-spin" />
            <span>{{ buttonText }}</span>
          </button>
        </div>

        <!-- 底部版权 -->
        <p class="mt-8 text-center text-xs text-muted-foreground">
          © 2026 steve0v0 的小屋 · 内容航海日志
        </p>
      </div>
    </div>
  </div>
</template>

<style scoped>
.animate-spin {
  animation: spin 1s linear infinite;
}
@keyframes spin {
  from {
    transform: rotate(0deg);
  }
  to {
    transform: rotate(360deg);
  }
}

/* 暗号遮罩：type=text + 圆点，保证中文输入法可用 */
.secret-masked {
  -webkit-text-security: disc;
  text-security: disc;
}
</style>
