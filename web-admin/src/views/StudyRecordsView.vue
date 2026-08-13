<script setup lang="ts">
import { onMounted, ref } from 'vue'
import { BookOpen, Pencil, Plus, RefreshCw, Trash2 } from '@lucide/vue'
import PageHeader from '@/components/ui/PageHeader.vue'
import ConfirmDialog from '@/components/ui/ConfirmDialog.vue'
import PaginationBar from '@/components/ui/PaginationBar.vue'
import { deleteStudyRecord, getStudyRecords, type StudyRecordList } from '@/api/admin'
import { formatDateTime, formatDuration } from '@/lib/format'
import { useToast } from '@/composables/useToast'

const { showToast } = useToast()
const loading = ref(true)
const deleting = ref(false)
const error = ref('')
const records = ref<StudyRecordList[]>([])
const startDate = ref('')
const endDate = ref('')
const page = ref(1)
const total = ref(0)
const hasMore = ref(false)
const deleteTarget = ref<StudyRecordList | null>(null)

async function loadRecords() {
  loading.value = true
  error.value = ''
  try {
    const result = await getStudyRecords({ page: page.value, size: 10, startDate: startDate.value || undefined, endDate: endDate.value || undefined })
    records.value = result.list
    total.value = result.total
    hasMore.value = result.hasMore
  } catch (err) {
    error.value = err instanceof Error ? err.message : '学习记录加载失败'
  } finally {
    loading.value = false
  }
}

function applyFilters() {
  page.value = 1
  loadRecords()
}

async function confirmDelete() {
  if (!deleteTarget.value) return
  deleting.value = true
  try {
    await deleteStudyRecord(deleteTarget.value.id)
    showToast('学习记录已删除')
    deleteTarget.value = null
    await loadRecords()
  } catch (err) {
    showToast(err instanceof Error ? err.message : '删除失败', 'error')
  } finally {
    deleting.value = false
  }
}

onMounted(loadRecords)
</script>

<template>
  <div class="max-w-[1400px]">
    <PageHeader title="学习记录" description="用日期范围回看学习轨迹，编辑内容会同步到首页日历和统计。">
      <template #actions>
        <button class="secondary-button" type="button" :disabled="loading" @click="loadRecords"><RefreshCw class="h-4 w-4" :class="loading ? 'animate-spin' : ''" />刷新</button>
        <router-link class="primary-button no-underline" to="/study-records/new"><Plus class="h-4 w-4" />新增记录</router-link>
      </template>
    </PageHeader>

    <div v-if="error" class="notice error-notice mb-4">{{ error }}</div>
    <div class="paper-card mb-4 p-4">
      <div class="flex flex-wrap items-end gap-3">
        <div class="min-w-[180px] flex-1"><label class="field-label" for="study-start">开始日期</label><input id="study-start" v-model="startDate" class="field-input" type="date" /></div>
        <div class="min-w-[180px] flex-1"><label class="field-label" for="study-end">结束日期</label><input id="study-end" v-model="endDate" class="field-input" type="date" /></div>
        <button class="secondary-button" type="button" @click="applyFilters">应用筛选</button>
        <button v-if="startDate || endDate" class="ghost-button" type="button" @click="startDate = ''; endDate = ''; applyFilters()">清除</button>
      </div>
    </div>

    <div class="table-shell">
      <div v-if="loading" class="space-y-3 p-5"><div v-for="index in 5" :key="index" class="skeleton h-12 rounded-lg" /></div>
      <div v-else-if="!records.length" class="empty-state"><span class="empty-icon"><BookOpen class="h-6 w-6" /></span><h3>还没有学习记录</h3><p>从今天开始，把学过的内容留下来。</p><router-link class="primary-button mt-5 no-underline" to="/study-records/new"><Plus class="h-4 w-4" />新增第一条记录</router-link></div>
      <table v-else class="data-table">
        <thead><tr><th>日期</th><th>主题</th><th class="num">学习时长</th><th>创建时间</th><th class="text-right">操作</th></tr></thead>
        <tbody>
          <tr v-for="record in records" :key="record.id">
            <td class="mono">{{ record.recordDate }}</td>
            <td><router-link class="table-title-link" :to="`/study-records/${record.id}/edit`">{{ record.subject }}</router-link></td>
            <td class="num mono">{{ formatDuration(record.duration) }}</td>
            <td class="mono muted-text text-xs">{{ formatDateTime(record.createdAt) }}</td>
            <td><div class="flex justify-end gap-1"><router-link class="icon-button no-underline" :to="`/study-records/${record.id}/edit`" aria-label="编辑" title="编辑"><Pencil class="h-4 w-4" /></router-link><button class="icon-button hover:text-destructive" type="button" aria-label="删除" title="删除" @click="deleteTarget = record"><Trash2 class="h-4 w-4" /></button></div></td>
          </tr>
        </tbody>
      </table>
    </div>
    <PaginationBar :page="page" :size="10" :total="total" :has-more="hasMore" @change="page = $event; loadRecords()" />

    <ConfirmDialog :open="Boolean(deleteTarget)" title="删除这条学习记录？" description="删除后不可恢复，首页日历与学习统计也会随之更新。" :loading="deleting" @cancel="deleteTarget = null" @confirm="confirmDelete" />
  </div>
</template>

<style scoped>
.no-underline { text-decoration: none; }
</style>
