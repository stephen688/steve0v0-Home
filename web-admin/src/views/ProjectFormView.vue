<script setup lang="ts">
import { computed, onMounted, ref, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ArrowLeft, Code2, LoaderCircle, Plus, Save, X } from '@lucide/vue'
import { createProject, getProject, updateProject, type ProjectPayload } from '@/api/admin'
import { isValidGithubUrl } from '@/lib/format'
import { useToast } from '@/composables/useToast'

const route = useRoute()
const router = useRouter()
const { showToast } = useToast()
const isEditing = computed(() => Boolean(route.params.id))
const loading = ref(false)
const submitting = ref(false)
const error = ref('')
const fieldError = ref('')
const name = ref('')
const description = ref('')
const githubUrl = ref('')
const sort = ref(0)
const techTags = ref<string[]>([])
const tagDraft = ref('')

function addTag() {
  const tag = tagDraft.value.trim()
  if (!tag || techTags.value.length >= 10 || techTags.value.includes(tag)) return
  techTags.value = [...techTags.value, tag.slice(0, 50)]
  tagDraft.value = ''
}

function handleTagKeydown(event: KeyboardEvent) {
  if (event.key === 'Enter' || event.key === ',') {
    event.preventDefault()
    addTag()
  }
}

function removeTag(tag: string) {
  techTags.value = techTags.value.filter((item) => item !== tag)
}

async function loadDetail() {
  if (!isEditing.value) return
  loading.value = true
  error.value = ''
  try {
    const project = await getProject(Number(route.params.id))
    name.value = project.name
    description.value = project.description || ''
    githubUrl.value = project.githubUrl
    sort.value = project.sort
    techTags.value = project.techTags || []
  } catch (err) {
    error.value = err instanceof Error ? err.message : '项目详情加载失败'
  } finally {
    loading.value = false
  }
}

async function submit() {
  fieldError.value = ''
  if (!name.value.trim()) {
    fieldError.value = '请填写项目名称。'
    return
  }
  if (!isValidGithubUrl(githubUrl.value.trim())) {
    fieldError.value = 'GitHub 地址必须是 https://github.com/owner/repository 形式。'
    return
  }
  if (sort.value < 0) {
    fieldError.value = '排序值不能小于 0。'
    return
  }
  submitting.value = true
  const payload: ProjectPayload = { name: name.value.trim(), description: description.value.trim(), githubUrl: githubUrl.value.trim(), techTags: techTags.value, sort: Number(sort.value) || 0 }
  try {
    if (isEditing.value) {
      await updateProject(Number(route.params.id), payload)
      showToast('项目已更新')
    } else {
      await createProject(payload)
      showToast('项目已添加')
    }
    await router.push('/about/projects')
  } catch (err) {
    showToast(err instanceof Error ? err.message : '项目保存失败', 'error')
  } finally {
    submitting.value = false
  }
}

onMounted(loadDetail)
watch(() => route.params.id, loadDetail)
</script>

<template>
  <div class="max-w-[680px]">
    <div class="page-header"><div><p class="eyebrow">About / GitHub</p><h2 class="page-title">{{ isEditing ? '编辑项目' : '新增项目' }}</h2><p class="page-description">项目地址只接受 GitHub HTTPS 链接，技术标签最多 10 个。</p></div><router-link class="ghost-button no-underline" to="/about/projects"><ArrowLeft class="h-4 w-4" />返回项目</router-link></div>
    <div v-if="error" class="notice error-notice mb-4">{{ error }}</div>
    <div v-if="loading" class="paper-card space-y-4 p-5"><div v-for="index in 5" :key="index" class="skeleton h-10 rounded-lg" /></div>
    <section v-else class="paper-card paper-card--ruled p-5">
      <h3 class="form-section-title">{{ isEditing ? '项目信息' : '录入项目' }}</h3>
      <div class="form-grid">
        <div><label class="field-label" for="project-name">项目名称 <span class="text-destructive">*</span></label><input id="project-name" v-model="name" class="field-input" maxlength="100" placeholder="问学 Agent" /></div>
        <div><label class="field-label" for="project-description">项目简介</label><textarea id="project-description" v-model="description" class="field-textarea" maxlength="500" placeholder="这个项目解决了什么问题？" /></div>
        <div><label class="field-label" for="project-url">GitHub 地址 <span class="text-destructive">*</span></label><input id="project-url" v-model="githubUrl" class="field-input" :class="{ 'field-error': githubUrl && !isValidGithubUrl(githubUrl) }" maxlength="500" placeholder="https://github.com/owner/repository" /><p class="field-hint">必须是 HTTPS + github.com 域名，并包含 owner / repository。</p></div>
        <div><span class="field-label">技术标签</span><div class="flex gap-2"><input v-model="tagDraft" class="field-input" :disabled="techTags.length >= 10" placeholder="输入后按 Enter" @keydown="handleTagKeydown" /><button class="secondary-button shrink-0" type="button" :disabled="!tagDraft.trim() || techTags.length >= 10" @click="addTag"><Plus class="h-4 w-4" />添加</button></div><div v-if="techTags.length" class="tag-list"><span v-for="tag in techTags" :key="tag" class="tag-chip">{{ tag }}<button type="button" aria-label="移除标签" @click="removeTag(tag)"><X class="h-3 w-3" /></button></span></div><p class="field-hint">{{ techTags.length }} / 10</p></div>
        <div><label class="field-label" for="project-sort">排序值</label><input id="project-sort" v-model.number="sort" class="field-input" type="number" min="0" placeholder="0" /></div>
        <div class="notice"><Code2 class="mt-0.5 h-4 w-4 shrink-0" /><span>项目保存后会按排序值从小到大出现在关于页。</span></div>
        <div v-if="fieldError" class="notice error-notice">{{ fieldError }}</div>
        <div class="form-actions"><router-link class="secondary-button no-underline" to="/about/projects">取消</router-link><button class="primary-button" type="button" :disabled="submitting" @click="submit"><LoaderCircle v-if="submitting" class="h-4 w-4 animate-spin" /><Save v-else class="h-4 w-4" />{{ isEditing ? '保存修改' : '保存项目' }}</button></div>
      </div>
    </section>
  </div>
</template>

<style scoped>
.no-underline { text-decoration: none; }
</style>
