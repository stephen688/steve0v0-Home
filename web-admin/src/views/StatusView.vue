<script setup lang="ts">
import { onMounted, ref } from 'vue'
import { Activity, LoaderCircle, Save } from '@lucide/vue'
import { getStatus, updateStatus, type StatusItem } from '@/api/admin'
import { stateLabel } from '@/lib/format'
import { useToast } from '@/composables/useToast'

const { showToast } = useToast()
const loading = ref(true)
const submitting = ref(false)
const error = ref('')
const state = ref('online')
const currentTask = ref('')
const mood = ref('')
const initial = ref<StatusItem>({ state: 'online', currentTask: '', mood: '' })

async function loadStatus() {
  loading.value = true
  error.value = ''
  try {
    const result = await getStatus()
    state.value = result.state || 'online'
    currentTask.value = result.currentTask || ''
    mood.value = result.mood || ''
    initial.value = { state: state.value, currentTask: currentTask.value, mood: mood.value }
  } catch (err) {
    error.value = err instanceof Error ? err.message : '个人状态加载失败'
  } finally {
    loading.value = false
  }
}

async function submit() {
  const payload: Partial<StatusItem> = {}
  if (state.value !== initial.value.state) payload.state = state.value
  if (currentTask.value !== (initial.value.currentTask || '')) payload.currentTask = currentTask.value
  if (mood.value !== (initial.value.mood || '')) payload.mood = mood.value
  if (!Object.keys(payload).length) {
    showToast('没有需要保存的修改', 'info')
    return
  }
  submitting.value = true
  try {
    await updateStatus(payload)
    initial.value = { state: state.value, currentTask: currentTask.value, mood: mood.value }
    showToast('个人状态已更新')
  } catch (err) {
    showToast(err instanceof Error ? err.message : '状态保存失败', 'error')
  } finally {
    submitting.value = false
  }
}

onMounted(loadStatus)
</script>

<template>
  <div class="max-w-[680px]">
    <div class="page-header"><div><p class="eyebrow">Presence / Status</p><h2 class="page-title">个人状态</h2><p class="page-description">这里的内容会同步到首页，让访客知道你现在处于什么状态。</p></div></div>
    <div v-if="error" class="notice error-notice mb-4">{{ error }}</div>
    <section v-if="loading" class="paper-card space-y-4 p-5"><div v-for="index in 3" :key="index" class="skeleton h-10 rounded-lg" /></section>
    <section v-else class="paper-card paper-card--ruled p-5">
      <h3 class="form-section-title">状态信息</h3>
      <div class="form-grid">
        <div><label class="field-label" for="status-state">当前状态</label><select id="status-state" v-model="state" class="field-select"><option value="online">在线</option><option value="studying">学习中</option><option value="exercising">运动中</option><option value="busy">忙碌</option><option value="rest">休息</option></select><p class="field-hint">当前预览：{{ stateLabel(state) }}</p></div>
        <div><label class="field-label" for="status-task">当前正在做的事</label><input id="status-task" v-model="currentTask" class="field-input" maxlength="200" placeholder="例如：整理前端页面" /></div>
        <div><label class="field-label" for="status-mood">心情签名</label><input id="status-mood" v-model="mood" class="field-input" maxlength="200" placeholder="例如：慢慢来，比较快" /></div>
        <div class="notice"><Activity class="mt-0.5 h-4 w-4 shrink-0" /><span>留空表示清空对应内容；只提交发生变化的字段。</span></div>
        <div class="form-actions"><button class="primary-button" type="button" :disabled="submitting" @click="submit"><LoaderCircle v-if="submitting" class="h-4 w-4 animate-spin" /><Save v-else class="h-4 w-4" />保存状态</button></div>
      </div>
    </section>
  </div>
</template>
