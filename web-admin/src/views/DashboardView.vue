<script setup lang="ts">
import { computed, onMounted, ref } from 'vue'
import { Activity, ArrowRight, BookOpen, Clock3, FileText, Image, PenLine, RefreshCw } from '@lucide/vue'
import PageHeader from '@/components/ui/PageHeader.vue'
import {
  getAdminArticles,
  getAdminMoments,
  getStats,
  getStatus,
  getStudyRecords,
  type ArticleList,
  type MomentItem,
  type StatsItem,
  type StatusItem
} from '@/api/admin'
import { formatDateTime, formatDuration, stateLabel } from '@/lib/format'

const loading = ref(true)
const error = ref('')
const articles = ref<ArticleList[]>([])
const moments = ref<MomentItem[]>([])
const articleTotal = ref<number | null>(null)
const momentTotal = ref<number | null>(null)
const studyTotal = ref<number | null>(null)
const stats = ref<StatsItem | null>(null)
const status = ref<StatusItem | null>(null)

const statCards = computed(() => [
  { label: '文章总数', value: articleTotal.value === null ? '—' : `${articleTotal.value}`, note: '包含草稿', icon: FileText, tone: '' },
  { label: '动态总数', value: momentTotal.value === null ? '—' : `${momentTotal.value}`, note: '全部动态', icon: Image, tone: 'stat-card--accent' },
  { label: '学习记录', value: studyTotal.value === null ? '—' : `${studyTotal.value}`, note: '全部记录', icon: BookOpen, tone: 'stat-card--success' },
  { label: '本周学习时长', value: stats.value ? formatDuration(stats.value.weeklyStudyMinutes) : '—', note: stats.value ? `${stats.value.streakDays} 天连续学习` : '等待统计接口', icon: Clock3, tone: 'stat-card--info' }
])

const shortcuts = [
  { label: '写新文章', desc: '打开 Markdown 双栏编辑器', path: '/articles/new', icon: FileText },
  { label: '发布动态', desc: '记录一段生活片刻', path: '/moments/new', icon: Image },
  { label: '添加学习记录', desc: '登记今天的学习内容', path: '/study-records/new', icon: BookOpen },
  { label: '更新个人状态', desc: '让首页状态保持准确', path: '/status', icon: Activity }
]

const articleStatus = (article: ArticleList) => article.publishedAt ? '已发布' : '草稿'
const statusTone = computed(() => status.value ? `state-${status.value.state}` : '')

async function loadDashboard() {
  loading.value = true
  error.value = ''
  const results = await Promise.allSettled([
    getAdminArticles({ page: 1, size: 5 }),
    getAdminMoments({ page: 1, size: 3 }),
    getStudyRecords({ page: 1, size: 1 }),
    getStats(),
    getStatus()
  ])

  const articleResult = results[0]
  if (articleResult.status === 'fulfilled') {
    articles.value = articleResult.value.list
    articleTotal.value = articleResult.value.total
  }
  const momentResult = results[1]
  if (momentResult.status === 'fulfilled') {
    moments.value = momentResult.value.list
    momentTotal.value = momentResult.value.total
  }
  const studyResult = results[2]
  if (studyResult.status === 'fulfilled') studyTotal.value = studyResult.value.total
  const statsResult = results[3]
  if (statsResult.status === 'fulfilled') stats.value = statsResult.value
  const statusResult = results[4]
  if (statusResult.status === 'fulfilled') status.value = statusResult.value

  if (results.some((result) => result.status === 'rejected')) {
    error.value = '部分概览数据暂时无法读取，请确认后端服务状态。'
  }
  loading.value = false
}

onMounted(loadDashboard)
</script>

<template>
  <div class="space-y-8 max-w-[1400px]">
    <PageHeader title="仪表盘" description="把近期内容、学习轨迹和当前状态收在同一页。">
      <template #actions>
        <button class="secondary-button" type="button" :disabled="loading" @click="loadDashboard">
          <RefreshCw class="h-4 w-4" :class="loading ? 'animate-spin' : ''" />
          刷新概览
        </button>
      </template>
    </PageHeader>

    <div v-if="error" class="notice error-notice">
      <span>{{ error }}</span>
    </div>

    <section class="grid grid-cols-1 gap-4 sm:grid-cols-2 lg:grid-cols-4" aria-label="数据概览">
      <div v-for="stat in statCards" :key="stat.label" class="paper-card stat-card" :class="stat.tone">
        <div class="stat-card-header">
          <span class="muted-text text-sm">{{ stat.label }}</span>
          <span class="stat-icon"><component :is="stat.icon" class="h-[18px] w-[18px]" /></span>
        </div>
        <div v-if="loading" class="skeleton loading-block mt-4 h-9" />
        <div v-else class="stat-value">{{ stat.value }}</div>
        <p class="stat-label">{{ stat.note }}</p>
      </div>
    </section>

    <section v-if="status" class="paper-card status-strip" :class="statusTone">
      <span class="status-dot" aria-hidden="true" />
      <strong>{{ stateLabel(status.state) }}</strong>
      <span v-if="status.currentTask" class="status-task">{{ status.currentTask }}</span>
      <span v-else class="muted-text">没有填写当前任务</span>
      <span v-if="status.mood" class="status-mood">「{{ status.mood }}」</span>
    </section>

    <section>
      <div class="section-heading">
        <div class="section-heading-left">
          <p class="eyebrow">Quick actions</p>
          <h3>快捷操作</h3>
        </div>
      </div>
      <div class="grid grid-cols-1 gap-4 sm:grid-cols-2 lg:grid-cols-4">
        <router-link
          v-for="item in shortcuts"
          :key="item.path"
          :to="item.path"
          class="paper-card paper-card-interactive shortcut-card no-underline"
        >
          <ArrowRight class="shortcut-arrow h-4 w-4" />
          <span class="shortcut-icon"><component :is="item.icon" class="h-5 w-5" /></span>
          <h3>{{ item.label }}</h3>
          <p>{{ item.desc }}</p>
        </router-link>
      </div>
    </section>

    <section class="grid grid-cols-1 gap-6 xl:grid-cols-[1.2fr_0.8fr]">
      <div>
        <div class="section-heading">
          <div class="section-heading-left">
            <p class="eyebrow">Latest entries</p>
            <h3>最近文章</h3>
          </div>
          <router-link class="ghost-button no-underline shrink-0" to="/articles">查看全部 <ArrowRight class="h-4 w-4" /></router-link>
        </div>
        <div class="table-shell">
          <div v-if="loading" class="space-y-3 p-5">
            <div v-for="index in 4" :key="index" class="skeleton h-12 rounded-lg" />
          </div>
          <div v-else-if="!articles.length" class="empty-state">
            <span class="empty-icon"><FileText class="h-6 w-6" /></span>
            <h3>还没有文章</h3>
            <p>从一篇短小的记录开始，建立你的内容航海日志。</p>
            <router-link class="primary-button mt-5 no-underline" to="/articles/new"><PenLine class="h-4 w-4" />写第一篇文章</router-link>
          </div>
          <table v-else class="data-table min-w-0">
            <thead><tr><th>标题</th><th>状态</th><th class="num">时间</th></tr></thead>
            <tbody>
              <tr v-for="article in articles" :key="article.id">
                <td><router-link class="table-title-link" :to="`/articles/${article.id}`">{{ article.title }}</router-link></td>
                <td><span class="tape-badge" :class="article.publishedAt ? 'badge-published' : 'badge-draft'">{{ articleStatus(article) }}</span></td>
                <td class="num mono muted-text">{{ formatDateTime(article.publishedAt) }}</td>
              </tr>
            </tbody>
          </table>
        </div>
      </div>

      <div>
        <div class="section-heading">
          <div class="section-heading-left">
            <p class="eyebrow">Life stream</p>
            <h3>最近动态</h3>
          </div>
          <router-link class="ghost-button no-underline shrink-0" to="/moments">查看全部 <ArrowRight class="h-4 w-4" /></router-link>
        </div>
        <div v-if="loading" class="space-y-3">
          <div v-for="index in 3" :key="index" class="paper-card skeleton h-28" />
        </div>
        <div v-else-if="!moments.length" class="paper-card empty-state">
          <span class="empty-icon"><Image class="h-6 w-6" /></span>
          <h3>还没有动态</h3>
          <p>记录今天发生的一件小事。</p>
        </div>
        <div v-else class="space-y-3">
          <article v-for="moment in moments" :key="moment.id" class="paper-card p-4">
            <p class="moment-content mt-0 line-clamp-3">{{ moment.content }}</p>
            <div class="moment-meta"><span>{{ moment.mediaType === 'image' ? `${moment.images.length} 张图片` : '文字动态' }}</span><span>{{ formatDateTime(moment.createdAt) }}</span></div>
          </article>
        </div>
      </div>
    </section>
  </div>
</template>

<style scoped>
.no-underline { text-decoration: none; }
</style>
