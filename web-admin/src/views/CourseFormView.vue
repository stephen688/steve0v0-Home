<script setup lang="ts">
import { computed, onMounted, ref, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ArrowLeft, CalendarDays, LoaderCircle, Save } from '@lucide/vue'
import { createCourse, getCourse, updateCourse, type CoursePayload } from '@/api/admin'
import { formatTime } from '@/lib/format'
import { useToast } from '@/composables/useToast'

const route = useRoute()
const router = useRouter()
const { showToast } = useToast()
const isEditing = computed(() => Boolean(route.params.id))
const loading = ref(false)
const submitting = ref(false)
const error = ref('')
const fieldError = ref('')

const today = new Date().toISOString().slice(0, 10)
const name = ref('')
const startDate = ref(today)
const endDate = ref(today)
const startTime = ref('09:00')
const endTime = ref('10:00')
const location = ref('')
const isRepeated = ref(0)
const dayOfWeek = ref<number | null>(null)

watch(isRepeated, (value) => {
  if (value === 0) {
    endDate.value = startDate.value
    dayOfWeek.value = null
  }
})

watch(startDate, (value) => {
  if (isRepeated.value === 0) endDate.value = value
})

async function loadDetail() {
  if (!isEditing.value) return
  loading.value = true
  error.value = ''
  try {
    const course = await getCourse(Number(route.params.id))
    name.value = course.name
    startDate.value = course.startDate
    endDate.value = course.endDate
    startTime.value = formatTime(course.startTime)
    endTime.value = formatTime(course.endTime)
    location.value = course.location || ''
    isRepeated.value = course.isRepeated
    dayOfWeek.value = course.dayOfWeek
  } catch (err) {
    error.value = err instanceof Error ? err.message : '课程详情加载失败'
  } finally {
    loading.value = false
  }
}

async function submit() {
  fieldError.value = ''
  if (!name.value.trim() || !startDate.value || !endDate.value || !startTime.value || !endTime.value) {
    fieldError.value = '请完整填写课程名称、日期和时间。'
    return
  }
  if (endDate.value < startDate.value) {
    fieldError.value = '结束日期不能早于开始日期。'
    return
  }
  if (startDate.value === endDate.value && endTime.value <= startTime.value) {
    fieldError.value = '同一天课程的结束时间需要晚于开始时间。'
    return
  }
  if (isRepeated.value === 1 && !dayOfWeek.value) {
    fieldError.value = '每周重复课程需要选择星期。'
    return
  }
  submitting.value = true
  const payload: CoursePayload = {
    name: name.value.trim(),
    startDate: startDate.value,
    endDate: isRepeated.value === 0 ? startDate.value : endDate.value,
    startTime: startTime.value,
    endTime: endTime.value,
    location: location.value.trim(),
    dayOfWeek: isRepeated.value === 1 ? dayOfWeek.value : null,
    isRepeated: isRepeated.value
  }
  try {
    if (isEditing.value) {
      await updateCourse(Number(route.params.id), payload)
      showToast('课程已更新')
    } else {
      await createCourse(payload)
      showToast('课程已添加')
    }
    await router.push('/courses')
  } catch (err) {
    showToast(err instanceof Error ? err.message : '课程保存失败', 'error')
  } finally {
    submitting.value = false
  }
}

onMounted(loadDetail)
watch(() => route.params.id, loadDetail)
</script>

<template>
  <div class="max-w-[680px]">
    <div class="page-header"><div><p class="eyebrow">Schedule / {{ isEditing ? 'Edit' : 'New' }}</p><h2 class="page-title">{{ isEditing ? '编辑课程' : '新增课程' }}</h2><p class="page-description">手动录入课程安排，重复课程会在日期范围内按星期展开。</p></div><router-link class="ghost-button no-underline" to="/courses"><ArrowLeft class="h-4 w-4" />返回课程表</router-link></div>
    <div v-if="error" class="notice error-notice mb-4">{{ error }}</div>
    <div v-if="loading" class="paper-card space-y-4 p-5"><div v-for="index in 6" :key="index" class="skeleton h-10 rounded-lg" /></div>
    <section v-else class="paper-card paper-card--ruled p-5">
      <h3 class="form-section-title">{{ isEditing ? '课程信息' : '录入课程' }}</h3>
      <div class="form-grid">
        <div><label class="field-label" for="course-name">课程名称 <span class="text-destructive">*</span></label><input id="course-name" v-model="name" class="field-input" maxlength="200" placeholder="例如：高等数学" /></div>
        <div class="form-grid form-grid-two">
          <div><label class="field-label" for="course-start-date">开始日期 <span class="text-destructive">*</span></label><input id="course-start-date" v-model="startDate" class="field-input" type="date" /></div>
          <div><label class="field-label" for="course-end-date">结束日期 <span class="text-destructive">*</span></label><input id="course-end-date" v-model="endDate" class="field-input" type="date" :disabled="isRepeated === 0" /></div>
          <div><label class="field-label" for="course-start-time">上课时间 <span class="text-destructive">*</span></label><input id="course-start-time" v-model="startTime" class="field-input" type="time" /></div>
          <div><label class="field-label" for="course-end-time">下课时间 <span class="text-destructive">*</span></label><input id="course-end-time" v-model="endTime" class="field-input" type="time" /></div>
        </div>
        <div><label class="field-label" for="course-location">上课地点</label><input id="course-location" v-model="location" class="field-input" maxlength="200" placeholder="例如：A201" /></div>
        <div><label class="field-label" for="course-mode">重复模式 <span class="text-destructive">*</span></label><select id="course-mode" v-model.number="isRepeated" class="field-select"><option :value="0">仅当天</option><option :value="1">每周重复</option></select></div>
        <div v-if="isRepeated === 1"><label class="field-label" for="course-day">星期 <span class="text-destructive">*</span></label><select id="course-day" v-model.number="dayOfWeek" class="field-select"><option :value="null">请选择</option><option v-for="day in 7" :key="day" :value="day">周{{ ['一', '二', '三', '四', '五', '六', '日'][day - 1] }}</option></select></div>
        <div class="notice"><CalendarDays class="mt-0.5 h-4 w-4 shrink-0" /><span>{{ isRepeated === 1 ? '每周重复课程将在开始日期至结束日期之间按所选星期生成。' : '仅当天课程会自动将结束日期设为开始日期。' }}</span></div>
        <div v-if="fieldError" class="notice error-notice">{{ fieldError }}</div>
        <div class="form-actions"><router-link class="secondary-button no-underline" to="/courses">取消</router-link><button class="primary-button" type="button" :disabled="submitting" @click="submit"><LoaderCircle v-if="submitting" class="h-4 w-4 animate-spin" /><Save v-else class="h-4 w-4" />{{ isEditing ? '保存修改' : '保存课程' }}</button></div>
      </div>
    </section>
  </div>
</template>

<style scoped>
.no-underline { text-decoration: none; }
</style>
