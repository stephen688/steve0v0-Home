<script setup lang="ts">
import { computed, onMounted, ref, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ArrowLeft, BookOpen, LoaderCircle, Save } from '@lucide/vue'
import { createStudyRecord, getStudyRecord, updateStudyRecord, type StudyRecordPayload } from '@/api/admin'
import { useToast } from '@/composables/useToast'

const route = useRoute()
const router = useRouter()
const { showToast } = useToast()
const isEditing = computed(() => Boolean(route.params.id))
const loading = ref(false)
const submitting = ref(false)
const error = ref('')
const fieldError = ref('')

const recordDate = ref(new Date().toISOString().slice(0, 10))
const subject = ref('')
const content = ref('')
const duration = ref<number | null>(null)

async function loadDetail() {
  if (!isEditing.value) return
  loading.value = true
  error.value = ''
  try {
    const record = await getStudyRecord(Number(route.params.id))
    recordDate.value = record.recordDate
    subject.value = record.subject
    content.value = record.content
    duration.value = record.duration
  } catch (err) {
    error.value = err instanceof Error ? err.message : '学习记录详情加载失败'
  } finally {
    loading.value = false
  }
}

async function submit() {
  fieldError.value = ''
  if (!recordDate.value || !subject.value.trim() || !content.value.trim() || !duration.value || duration.value < 1) {
    fieldError.value = '请完整填写日期、主题、内容和大于 0 的学习时长。'
    return
  }
  submitting.value = true
  const payload: StudyRecordPayload = { recordDate: recordDate.value, subject: subject.value.trim(), content: content.value.trim(), duration: duration.value }
  try {
    if (isEditing.value) {
      await updateStudyRecord(Number(route.params.id), payload)
      showToast('学习记录已更新')
    } else {
      await createStudyRecord(payload)
      showToast('学习记录已添加')
    }
    await router.push('/study-records')
  } catch (err) {
    showToast(err instanceof Error ? err.message : '学习记录保存失败', 'error')
  } finally {
    submitting.value = false
  }
}

onMounted(loadDetail)
watch(() => route.params.id, loadDetail)
</script>

<template>
  <div class="max-w-[680px]">
    <div class="page-header"><div><p class="eyebrow">Study log / {{ isEditing ? 'Edit' : 'New' }}</p><h2 class="page-title">{{ isEditing ? '编辑学习记录' : '新增学习记录' }}</h2><p class="page-description">学习记录会出现在首页日历，并参与学习时长统计。</p></div><router-link class="ghost-button no-underline" to="/study-records"><ArrowLeft class="h-4 w-4" />返回列表</router-link></div>

    <div v-if="error" class="notice error-notice mb-4">{{ error }}</div>
    <div v-if="loading" class="paper-card space-y-4 p-5"><div v-for="index in 4" :key="index" class="skeleton h-10 rounded-lg" /></div>
    <section v-else class="paper-card paper-card--ruled p-5">
      <h3 class="form-section-title">{{ isEditing ? '学习记录' : '记录学习' }}</h3>
      <div class="form-grid">
        <div><label class="field-label" for="record-date">学习日期 <span class="text-destructive">*</span></label><input id="record-date" v-model="recordDate" class="field-input" type="date" /></div>
        <div><label class="field-label" for="record-subject">学习主题 <span class="text-destructive">*</span></label><input id="record-subject" v-model="subject" class="field-input" maxlength="100" placeholder="例如：Spring Security JWT" /></div>
        <div><label class="field-label" for="record-content">学习内容 <span class="text-destructive">*</span></label><textarea id="record-content" v-model="content" class="field-textarea" placeholder="记下今天真正理解了什么、遇到了什么问题。" /></div>
        <div><label class="field-label" for="record-duration">学习时长（分钟） <span class="text-destructive">*</span></label><input id="record-duration" v-model.number="duration" class="field-input" type="number" min="1" placeholder="60" /><p class="field-hint">只填写实际学习时长，至少 1 分钟。</p></div>
        <div v-if="fieldError" class="notice error-notice">{{ fieldError }}</div>
        <div class="form-actions"><router-link class="secondary-button no-underline" to="/study-records">取消</router-link><button class="primary-button" type="button" :disabled="submitting" @click="submit"><LoaderCircle v-if="submitting" class="h-4 w-4 animate-spin" /><Save v-else class="h-4 w-4" />{{ isEditing ? '保存修改' : '保存记录' }}</button></div>
      </div>
    </section>
    <div class="mt-5 flex items-start gap-2 muted-text text-xs"><BookOpen class="mt-0.5 h-4 w-4 shrink-0" /><span>内容会按原文保存，建议写下可供未来复习的具体线索。</span></div>
  </div>
</template>

<style scoped>
.no-underline { text-decoration: none; }
</style>
