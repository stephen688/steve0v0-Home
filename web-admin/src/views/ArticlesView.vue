<script setup lang="ts">
import { onMounted, ref } from 'vue'
import { Eye, FileText, Pencil, Plus, RefreshCw, Trash2 } from '@lucide/vue'
import PageHeader from '@/components/ui/PageHeader.vue'
import ConfirmDialog from '@/components/ui/ConfirmDialog.vue'
import PaginationBar from '@/components/ui/PaginationBar.vue'
import { deleteArticle, getAdminArticles, type ArticleList } from '@/api/admin'
import { categoryLabel, formatDateTime, parseTags } from '@/lib/format'
import { useToast } from '@/composables/useToast'

const { showToast } = useToast()
const loading = ref(true)
const deleting = ref(false)
const error = ref('')
const articles = ref<ArticleList[]>([])
const page = ref(1)
const total = ref(0)
const hasMore = ref(false)
const category = ref('')
const status = ref<number | undefined>(undefined)
const deleteTarget = ref<ArticleList | null>(null)

async function loadArticles() {
  loading.value = true
  error.value = ''
  try {
    const result = await getAdminArticles({ page: page.value, size: 10, category: category.value || undefined, status: status.value })
    articles.value = result.list
    total.value = result.total
    hasMore.value = result.hasMore
  } catch (err) {
    error.value = err instanceof Error ? err.message : '文章列表加载失败'
  } finally {
    loading.value = false
  }
}

function setCategory(value: string) {
  category.value = value
  page.value = 1
  loadArticles()
}

function setStatus(value: number | undefined) {
  status.value = value
  page.value = 1
  loadArticles()
}

function changePage(nextPage: number) {
  page.value = nextPage
  loadArticles()
}

async function confirmDelete() {
  if (!deleteTarget.value) return
  deleting.value = true
  try {
    await deleteArticle(deleteTarget.value.id)
    showToast('文章已删除')
    deleteTarget.value = null
    await loadArticles()
  } catch (err) {
    showToast(err instanceof Error ? err.message : '删除失败', 'error')
  } finally {
    deleting.value = false
  }
}

onMounted(loadArticles)
</script>

<template>
  <div class="max-w-[1400px]">
    <PageHeader title="文章管理" description="管理已发布内容与草稿，列表只展示元信息，正文在详情页查看。">
      <template #actions>
        <button class="secondary-button" type="button" :disabled="loading" @click="loadArticles"><RefreshCw class="h-4 w-4" :class="loading ? 'animate-spin' : ''" />刷新</button>
        <router-link class="primary-button no-underline" to="/articles/new"><Plus class="h-4 w-4" />新建文章</router-link>
      </template>
    </PageHeader>

    <div v-if="error" class="notice error-notice mb-4">{{ error }}</div>

    <div class="filter-bar">
      <div class="flex flex-wrap items-center gap-3">
        <div class="filter-group" aria-label="文章分类筛选">
          <button class="filter-tab" :class="{ 'is-active': category === '' }" type="button" @click="setCategory('')">全部</button>
          <button class="filter-tab" :class="{ 'is-active': category === 'tech' }" type="button" @click="setCategory('tech')">技术博客</button>
          <button class="filter-tab" :class="{ 'is-active': category === 'life' }" type="button" @click="setCategory('life')">生活文章</button>
        </div>
        <div class="filter-group" aria-label="文章状态筛选">
          <button class="filter-tab" :class="{ 'is-active': status === undefined }" type="button" @click="setStatus(undefined)">全部状态</button>
          <button class="filter-tab" :class="{ 'is-active': status === 1 }" type="button" @click="setStatus(1)">已发布</button>
          <button class="filter-tab" :class="{ 'is-active': status === 0 }" type="button" @click="setStatus(0)">草稿</button>
        </div>
      </div>
      <span class="mono muted-text text-xs">{{ total }} 条文章</span>
    </div>

    <div class="table-shell">
      <div v-if="loading" class="space-y-3 p-5">
        <div v-for="index in 5" :key="index" class="skeleton h-12 rounded-lg" />
      </div>
      <div v-else-if="!articles.length" class="empty-state">
        <span class="empty-icon"><FileText class="h-6 w-6" /></span>
        <h3>没有匹配的文章</h3>
        <p>调整筛选条件，或新建一篇内容。</p>
        <router-link class="primary-button mt-5 no-underline" to="/articles/new"><Plus class="h-4 w-4" />新建文章</router-link>
      </div>
      <table v-else class="data-table">
        <thead><tr><th>标题</th><th>分类</th><th>状态</th><th>标签</th><th>发布 / 创建</th><th class="num">数据</th><th class="text-right">操作</th></tr></thead>
        <tbody>
          <tr v-for="article in articles" :key="article.id">
            <td>
              <router-link class="table-title-link" :to="`/articles/${article.id}`">{{ article.title }}</router-link>
              <p v-if="article.summary" class="mt-1 max-w-[320px] truncate text-xs muted-text">{{ article.summary }}</p>
            </td>
            <td><span class="tape-badge badge-category">{{ categoryLabel(article.category) }}</span></td>
            <td><span class="tape-badge" :class="article.status === 1 ? 'badge-published' : 'badge-draft'">{{ article.status === 1 ? '已发布' : '草稿' }}</span></td>
            <td><div class="flex max-w-[180px] flex-wrap gap-1"><span v-for="tag in parseTags(article.tags).slice(0, 3)" :key="tag" class="tag-chip">{{ tag }}</span><span v-if="parseTags(article.tags).length > 3" class="tag-chip">+{{ parseTags(article.tags).length - 3 }}</span></div></td>
            <td class="num mono muted-text text-xs">{{ formatDateTime(article.publishedAt) }}</td>
            <td class="num mono muted-text text-xs">{{ article.viewCount }} 阅读 · {{ article.readTimeMinutes }} 分钟</td>
            <td>
              <div class="flex justify-end gap-1">
                <router-link class="icon-button no-underline" :to="`/articles/${article.id}`" title="查看" aria-label="查看文章"><Eye class="h-4 w-4" /></router-link>
                <router-link class="icon-button no-underline" :to="`/articles/${article.id}/edit`" title="编辑" aria-label="编辑文章"><Pencil class="h-4 w-4" /></router-link>
                <button class="icon-button hover:text-destructive" type="button" title="删除" aria-label="删除文章" @click="deleteTarget = article"><Trash2 class="h-4 w-4" /></button>
              </div>
            </td>
          </tr>
        </tbody>
      </table>
    </div>
    <PaginationBar :page="page" :size="10" :total="total" :has-more="hasMore" @change="changePage" />

    <ConfirmDialog
      :open="Boolean(deleteTarget)"
      title="删除这篇文章？"
      description="删除后不可恢复，文章正文、草稿状态和阅读数据都会被移除。"
      :loading="deleting"
      @cancel="deleteTarget = null"
      @confirm="confirmDelete"
    />
  </div>
</template>

<style scoped>
.no-underline { text-decoration: none; }
</style>
