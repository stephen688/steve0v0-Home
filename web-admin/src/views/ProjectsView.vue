<script setup lang="ts">
import { onMounted, ref } from 'vue'
import { Code2, ExternalLink, GitBranch, Pencil, Plus, RefreshCw, Trash2 } from '@lucide/vue'
import PageHeader from '@/components/ui/PageHeader.vue'
import ConfirmDialog from '@/components/ui/ConfirmDialog.vue'
import { deleteProject, getProjects, type ProjectItem } from '@/api/admin'
import { useToast } from '@/composables/useToast'

const { showToast } = useToast()
const loading = ref(true)
const error = ref('')
const projects = ref<ProjectItem[]>([])
const deleteTarget = ref<ProjectItem | null>(null)
const deleting = ref(false)

async function loadProjects() {
  loading.value = true
  error.value = ''
  try {
    projects.value = await getProjects()
  } catch (err) {
    error.value = err instanceof Error ? err.message : '项目列表加载失败'
  } finally {
    loading.value = false
  }
}

async function confirmDelete() {
  if (!deleteTarget.value) return
  deleting.value = true
  try {
    await deleteProject(deleteTarget.value.id)
    showToast('项目已删除')
    deleteTarget.value = null
    await loadProjects()
  } catch (err) {
    showToast(err instanceof Error ? err.message : '删除失败', 'error')
  } finally {
    deleting.value = false
  }
}

onMounted(loadProjects)
</script>

<template>
  <div class="max-w-[1400px]">
    <PageHeader title="GitHub 项目" description="维护关于页展示的开源项目；排序值越小越靠前。">
      <template #actions><button class="secondary-button" type="button" :disabled="loading" @click="loadProjects"><RefreshCw class="h-4 w-4" :class="loading ? 'animate-spin' : ''" />刷新</button><router-link class="primary-button no-underline" to="/about/projects/new"><Plus class="h-4 w-4" />新增项目</router-link></template>
    </PageHeader>

    <div v-if="error" class="notice error-notice mb-4">{{ error }}</div>
    <div v-if="loading" class="project-grid"><div v-for="index in 6" :key="index" class="paper-card skeleton h-48" /></div>
    <div v-else-if="!projects.length" class="paper-card empty-state"><span class="empty-icon"><Code2 class="h-6 w-6" /></span><h3>还没有项目</h3><p>添加一个 GitHub 仓库，让关于页有更完整的作品线索。</p><router-link class="primary-button mt-5 no-underline" to="/about/projects/new"><Plus class="h-4 w-4" />新增第一个项目</router-link></div>
    <div v-else class="project-grid">
      <article v-for="project in projects" :key="project.id" class="paper-card project-card">
        <div class="flex items-start justify-between gap-3"><div class="flex items-center gap-2"><GitBranch class="h-5 w-5 text-primary" /><h3>{{ project.name }}</h3></div><span class="project-sort-badge">#{{ project.sort }}</span></div>
        <p class="min-h-[44px]">{{ project.description || '暂无项目简介。' }}</p>
        <div class="tag-list"><span v-for="tag in project.techTags" :key="tag" class="tag-chip">{{ tag }}</span></div>
        <div class="project-meta"><a class="inline-flex items-center gap-1 text-primary underline" :href="project.githubUrl" target="_blank" rel="noreferrer">打开仓库 <ExternalLink class="h-3.5 w-3.5" /></a><div class="flex gap-1"><router-link class="icon-button no-underline" :to="`/about/projects/${project.id}/edit`" aria-label="编辑" title="编辑"><Pencil class="h-4 w-4" /></router-link><button class="icon-button hover:text-destructive" type="button" aria-label="删除" title="删除" @click="deleteTarget = project"><Trash2 class="h-4 w-4" /></button></div></div>
      </article>
    </div>

    <ConfirmDialog :open="Boolean(deleteTarget)" title="删除这个项目？" description="删除后不可恢复，关于页将不再展示该 GitHub 项目。" :loading="deleting" @cancel="deleteTarget = null" @confirm="confirmDelete" />
  </div>
</template>

<style scoped>
.no-underline { text-decoration: none; }
</style>
