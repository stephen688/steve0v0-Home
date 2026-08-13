<script setup lang="ts">
import { onMounted, ref } from 'vue'
import { CalendarDays, Pencil, Plus, RefreshCw, Trash2 } from '@lucide/vue'
import PageHeader from '@/components/ui/PageHeader.vue'
import ConfirmDialog from '@/components/ui/ConfirmDialog.vue'
import PaginationBar from '@/components/ui/PaginationBar.vue'
import { deleteCourse, getCourses, type CourseItem } from '@/api/admin'
import { dayOfWeekLabel, formatDate, formatTime } from '@/lib/format'
import { useToast } from '@/composables/useToast'

const { showToast } = useToast()
const loading = ref(true)
const deleting = ref(false)
const error = ref('')
const courses = ref<CourseItem[]>([])
const page = ref(1)
const total = ref(0)
const hasMore = ref(false)
const deleteTarget = ref<CourseItem | null>(null)

async function loadCourses() {
  loading.value = true
  error.value = ''
  try {
    const result = await getCourses({ page: page.value, size: 10 })
    courses.value = result.list
    total.value = result.total
    hasMore.value = result.hasMore
  } catch (err) {
    error.value = err instanceof Error ? err.message : '课程列表加载失败'
  } finally {
    loading.value = false
  }
}

async function confirmDelete() {
  if (!deleteTarget.value) return
  deleting.value = true
  try {
    await deleteCourse(deleteTarget.value.id)
    showToast('课程已删除')
    deleteTarget.value = null
    await loadCourses()
  } catch (err) {
    showToast(err instanceof Error ? err.message : '删除失败', 'error')
  } finally {
    deleting.value = false
  }
}

onMounted(loadCourses)
</script>

<template>
  <div class="max-w-[1400px]">
    <PageHeader title="课程表" description="维护手动录入的单日课程和每周重复课程，首页日历会按日期范围展开。">
      <template #actions><button class="secondary-button" type="button" :disabled="loading" @click="loadCourses"><RefreshCw class="h-4 w-4" :class="loading ? 'animate-spin' : ''" />刷新</button><router-link class="primary-button no-underline" to="/courses/new"><Plus class="h-4 w-4" />新增课程</router-link></template>
    </PageHeader>

    <div v-if="error" class="notice error-notice mb-4">{{ error }}</div>
    <div class="table-shell">
      <div v-if="loading" class="space-y-3 p-5"><div v-for="index in 5" :key="index" class="skeleton h-12 rounded-lg" /></div>
      <div v-else-if="!courses.length" class="empty-state"><span class="empty-icon"><CalendarDays class="h-6 w-6" /></span><h3>还没有课程</h3><p>先录入一门课程，日历才会有可以追踪的安排。</p><router-link class="primary-button mt-5 no-underline" to="/courses/new"><Plus class="h-4 w-4" />新增第一门课程</router-link></div>
      <table v-else class="data-table">
        <thead><tr><th>课程名称</th><th>日期范围</th><th>时间</th><th>地点</th><th>重复模式</th><th class="text-right">操作</th></tr></thead>
        <tbody>
          <tr v-for="course in courses" :key="course.id">
            <td><router-link class="table-title-link" :to="`/courses/${course.id}/edit`">{{ course.name }}</router-link></td>
            <td class="mono text-xs">{{ formatDate(course.startDate) }} <span class="muted-text">→</span> {{ formatDate(course.endDate) }}</td>
            <td class="mono text-xs">{{ formatTime(course.startTime) }} - {{ formatTime(course.endTime) }}</td>
            <td class="muted-text">{{ course.location || '—' }}</td>
            <td><span class="tape-badge" :class="course.isRepeated === 1 ? 'badge-info' : 'badge-category'">{{ course.isRepeated === 1 ? `每周 ${dayOfWeekLabel(course.dayOfWeek)}` : '仅当天' }}</span></td>
            <td><div class="flex justify-end gap-1"><router-link class="icon-button no-underline" :to="`/courses/${course.id}/edit`" aria-label="编辑" title="编辑"><Pencil class="h-4 w-4" /></router-link><button class="icon-button hover:text-destructive" type="button" aria-label="删除" title="删除" @click="deleteTarget = course"><Trash2 class="h-4 w-4" /></button></div></td>
          </tr>
        </tbody>
      </table>
    </div>
    <PaginationBar :page="page" :size="10" :total="total" :has-more="hasMore" @change="page = $event; loadCourses()" />

    <ConfirmDialog :open="Boolean(deleteTarget)" title="删除这门课程？" description="删除后不可恢复，课程在首页日历中的所有实例也会被移除。" :loading="deleting" @cancel="deleteTarget = null" @confirm="confirmDelete" />
  </div>
</template>

<style scoped>
.no-underline { text-decoration: none; }
</style>
