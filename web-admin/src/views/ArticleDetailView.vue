<script setup lang="ts">
import { onMounted, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import MarkdownIt from 'markdown-it'
import { ArrowLeft, CalendarDays, Clock3, Eye, RefreshCw, Trash2 } from '@lucide/vue'
import ConfirmDialog from '@/components/ui/ConfirmDialog.vue'
import { deleteArticle, getAdminArticle, resolveAssetUrl, type ArticleDetail } from '@/api/admin'
import { categoryLabel, formatDateTime, parseTags } from '@/lib/format'
import { useToast } from '@/composables/useToast'

const route = useRoute()
const router = useRouter()
const { showToast } = useToast()
const markdown = new MarkdownIt({ html: false, breaks: true, linkify: true })

const loading = ref(true)
const deleting = ref(false)
const error = ref('')
const article = ref<ArticleDetail | null>(null)
const showDelete = ref(false)

const articleId = () => Number(route.params.id)

async function loadArticle() {
  loading.value = true
  error.value = ''
  try {
    article.value = await getAdminArticle(articleId())
  } catch (err) {
    error.value = err instanceof Error ? err.message : '文章详情加载失败'
  } finally {
    loading.value = false
  }
}

async function confirmDelete() {
  if (!article.value) return
  deleting.value = true
  try {
    await deleteArticle(article.value.id)
    showToast('文章已删除')
    await router.push('/articles')
  } catch (err) {
    showToast(err instanceof Error ? err.message : '删除失败', 'error')
  } finally {
    deleting.value = false
    showDelete.value = false
  }
}

onMounted(loadArticle)
</script>

<template>
  <div class="max-w-[680px]">
    <div class="page-header">
      <div>
        <p class="eyebrow">Read / Article</p>
        <h2 class="page-title">文章详情</h2>
      </div>
      <div class="page-header-actions">
        <router-link class="ghost-button no-underline" to="/articles"><ArrowLeft class="h-4 w-4" />返回列表</router-link>
        <button v-if="article" class="danger-button" type="button" @click="showDelete = true"><Trash2 class="h-4 w-4" />删除</button>
      </div>
    </div>

    <div v-if="loading" class="paper-card space-y-4 p-6">
      <div class="skeleton h-9 w-3/4 rounded-lg" />
      <div class="skeleton h-4 w-1/2 rounded-lg" />
      <div class="skeleton h-48 rounded-lg" />
    </div>
    <div v-else-if="error" class="notice error-notice"><span>{{ error }}</span><button class="ghost-button ml-auto min-h-0 px-2 py-1" type="button" @click="loadArticle"><RefreshCw class="h-4 w-4" />重试</button></div>
    <article v-else-if="article" class="paper-card p-6 sm:p-8">
      <div class="flex flex-wrap items-center gap-2">
        <span class="tape-badge badge-category">{{ categoryLabel(article.category) }}</span>
        <span class="tape-badge" :class="article.status === 1 ? 'badge-published' : 'badge-draft'">{{ article.status === 1 ? '已发布' : '草稿' }}</span>
      </div>
      <h1 class="mt-4 text-3xl leading-tight">{{ article.title }}</h1>
      <p v-if="article.summary" class="mt-3 text-base muted-text">{{ article.summary }}</p>
      <div class="mt-5 flex flex-wrap gap-x-4 gap-y-2 border-y border-border py-3 mono muted-text text-xs">
        <span class="inline-flex items-center gap-1"><CalendarDays class="meta-icon h-3.5 w-3.5" />{{ formatDateTime(article.publishedAt || article.createdAt) }}</span>
        <span class="inline-flex items-center gap-1"><Eye class="meta-icon h-3.5 w-3.5" />{{ article.viewCount }} 阅读</span>
        <span class="inline-flex items-center gap-1"><Clock3 class="meta-icon h-3.5 w-3.5" />{{ article.readTimeMinutes }} 分钟阅读</span>
      </div>
      <img v-if="article.coverImage" class="mt-6 max-h-[360px] w-full rounded-xl object-cover" :src="resolveAssetUrl(article.coverImage)" alt="文章封面" />
      <div v-if="parseTags(article.tags).length" class="tag-list mb-7"><span v-for="tag in parseTags(article.tags)" :key="tag" class="tag-chip">{{ tag }}</span></div>
      <div class="article-body" v-html="markdown.render(article.content || '暂无正文。')" />
    </article>

    <ConfirmDialog
      :open="showDelete"
      title="删除这篇文章？"
      description="删除后不可恢复，文章正文、草稿状态和阅读数据都会被移除。"
      :loading="deleting"
      @cancel="showDelete = false"
      @confirm="confirmDelete"
    />
  </div>
</template>

<style scoped>
.no-underline { text-decoration: none; }
</style>
