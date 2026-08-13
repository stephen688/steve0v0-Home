<script setup lang="ts">
import { computed, onMounted, onUnmounted, ref } from 'vue'
import { useRouter } from 'vue-router'
import MarkdownIt from 'markdown-it'
import { ArrowLeft, FileUp, ImagePlus, LoaderCircle, Save, Send, Upload } from '@lucide/vue'
import { createArticle, resolveAssetUrl, uploadImage, uploadMarkdown, type ArticlePayload } from '@/api/admin'
import { useToast } from '@/composables/useToast'
import { parseImportedArticle } from '@/lib/markdownImport'

const router = useRouter()
const { showToast } = useToast()
const markdown = new MarkdownIt({ html: false, breaks: true, linkify: true })

const title = ref('')
const summary = ref('')
const content = ref('')
const category = ref('tech')
const tags = ref('')
const coverImage = ref('')
const saving = ref(false)
const uploadingCover = ref(false)
const uploadingBodyImage = ref(false)
const importingMarkdown = ref(false)
const coverProgress = ref(0)
const bodyImageProgress = ref(0)
const titleError = ref('')
const bodyInput = ref<HTMLInputElement | null>(null)
const markdownInput = ref<HTMLInputElement | null>(null)
const coverInput = ref<HTMLInputElement | null>(null)
const editor = ref<HTMLTextAreaElement | null>(null)
const initialSnapshot = ref('')

const previewHtml = computed(() => markdown.render(content.value || '*在左侧输入 Markdown，右侧会实时预览。*'))
const wordCount = computed(() => content.value.replace(/\s/g, '').length)
const isDirty = computed(() => snapshot() !== initialSnapshot.value)

function snapshot() {
  return JSON.stringify({ title: title.value, summary: summary.value, content: content.value, category: category.value, tags: tags.value, coverImage: coverImage.value })
}

function markClean() {
  initialSnapshot.value = snapshot()
}

async function handleCoverUpload(event: Event) {
  const file = (event.target as HTMLInputElement).files?.[0]
  if (!file) return
  uploadingCover.value = true
  coverProgress.value = 0
  try {
    coverImage.value = await uploadImage(file, (progress) => { coverProgress.value = progress })
    showToast('封面图上传成功')
  } catch (err) {
    showToast(err instanceof Error ? err.message : '封面图上传失败', 'error')
  } finally {
    uploadingCover.value = false
    if (coverInput.value) coverInput.value.value = ''
  }
}

async function handleBodyImageUpload(event: Event) {
  const file = (event.target as HTMLInputElement).files?.[0]
  if (!file) return
  uploadingBodyImage.value = true
  bodyImageProgress.value = 0
  try {
    const url = await uploadImage(file, (progress) => { bodyImageProgress.value = progress })
    const textarea = editor.value
    const cursor = textarea?.selectionStart ?? content.value.length
    const snippet = `![图片描述](${url})`
    content.value = content.value.slice(0, cursor) + snippet + content.value.slice(textarea?.selectionEnd ?? cursor)
    showToast('图片已插入正文')
  } catch (err) {
    showToast(err instanceof Error ? err.message : '正文图片上传失败', 'error')
  } finally {
    uploadingBodyImage.value = false
    if (bodyInput.value) bodyInput.value.value = ''
  }
}

async function handleMarkdownImport(event: Event) {
  const file = (event.target as HTMLInputElement).files?.[0]
  if (!file) return
  if (content.value.trim() && !window.confirm('导入 Markdown 会替换当前正文，是否继续？')) {
    if (markdownInput.value) markdownInput.value.value = ''
    return
  }
  importingMarkdown.value = true
  try {
    const result = await uploadMarkdown(file)
    const parsed = parseImportedArticle(result.content)
    if (!title.value.trim() && parsed.title) title.value = parsed.title
    if (!summary.value.trim() && parsed.summary) summary.value = parsed.summary
    content.value = parsed.content
    showToast(`已导入 ${result.fileName}，右侧预览已自动更新`)
  } catch (err) {
    showToast(err instanceof Error ? err.message : 'Markdown 导入失败', 'error')
  } finally {
    importingMarkdown.value = false
    if (markdownInput.value) markdownInput.value.value = ''
  }
}

async function save(status: number) {
  titleError.value = title.value.trim() ? '' : '请填写文章标题'
  if (titleError.value) return
  saving.value = true
  const payload: ArticlePayload = {
    title: title.value.trim(),
    summary: summary.value.trim(),
    content: content.value,
    coverImage: coverImage.value.trim(),
    category: category.value,
    tags: tags.value.split(',').map((tag) => tag.trim()).filter(Boolean).join(','),
    status
  }
  try {
    await createArticle(payload)
    markClean()
    showToast(status === 1 ? '文章已发布' : '草稿已保存')
    await router.push('/articles')
  } catch (err) {
    showToast(err instanceof Error ? err.message : '文章保存失败', 'error')
  } finally {
    saving.value = false
  }
}

function handleShortcut(event: KeyboardEvent) {
  if ((event.ctrlKey || event.metaKey) && event.key.toLowerCase() === 's') {
    event.preventDefault()
    void save(0)
  }
}

function handleBeforeUnload(event: BeforeUnloadEvent) {
  if (!isDirty.value) return
  event.preventDefault()
  event.returnValue = ''
}

onMounted(() => {
  markClean()
  window.addEventListener('keydown', handleShortcut)
  window.addEventListener('beforeunload', handleBeforeUnload)
})

onUnmounted(() => {
  window.removeEventListener('keydown', handleShortcut)
  window.removeEventListener('beforeunload', handleBeforeUnload)
})
</script>

<template>
  <div class="max-w-[1400px]">
    <div class="page-header">
      <div>
        <p class="eyebrow">Compose / Markdown</p>
        <h2 class="page-title">新建文章</h2>
        <p class="page-description">左侧写作，右侧预览；按 Ctrl/Cmd + S 可保存草稿。</p>
      </div>
      <div class="page-header-actions">
        <router-link class="ghost-button no-underline" to="/articles"><ArrowLeft class="h-4 w-4" />返回文章</router-link>
      </div>
    </div>

    <section class="paper-card paper-card--ruled mb-5 p-5">
      <h3 class="form-section-title">文章信息</h3>
      <div class="form-grid form-grid-two">
        <div class="sm:col-span-2">
          <label class="field-label" for="article-title">标题 <span class="text-destructive">*</span></label>
          <input id="article-title" v-model="title" class="field-input text-lg" :class="{ 'field-error': titleError }" placeholder="给这次航行留一个标题" maxlength="200" />
          <p v-if="titleError" class="field-error-text">{{ titleError }}</p>
        </div>
        <div>
          <label class="field-label" for="article-category">分类</label>
          <select id="article-category" v-model="category" class="field-select">
            <option value="tech">技术博客</option>
            <option value="life">生活文章</option>
          </select>
        </div>
        <div>
          <label class="field-label" for="article-tags">标签</label>
          <input id="article-tags" v-model="tags" class="field-input" placeholder="vue, spring, 复盘" />
          <p class="field-hint">使用英文逗号分隔。</p>
        </div>
        <div class="sm:col-span-2">
          <label class="field-label" for="article-summary">摘要</label>
          <textarea id="article-summary" v-model="summary" class="field-textarea min-h-[88px]" maxlength="500" placeholder="用一两句话告诉读者这篇文章记录了什么。" />
        </div>
        <div class="sm:col-span-2">
          <span class="field-label">封面图</span>
          <div class="flex flex-wrap items-start gap-4">
            <img v-if="coverImage" class="cover-preview w-48" :src="resolveAssetUrl(coverImage)" alt="文章封面预览" />
            <label class="upload-zone min-h-[90px] flex-1 cursor-pointer" for="cover-image-input">
              <Upload class="mb-2 h-5 w-5" />
              <span>{{ uploadingCover ? `上传中 ${coverProgress}%…` : '点击选择 JPG / PNG / GIF，最大 5MB' }}</span>
              <input id="cover-image-input" ref="coverInput" type="file" accept="image/jpeg,image/png,image/gif" :disabled="uploadingCover" @change="handleCoverUpload" />
            </label>
          </div>
        </div>
      </div>
    </section>

    <section class="split-editor">
      <div class="editor-pane">
        <div class="editor-pane-header flex items-center justify-between">
          <span>编辑</span>
          <div class="flex items-center gap-1">
            <label class="ghost-button min-h-0 cursor-pointer px-2 py-1 text-xs" for="markdown-input"><FileUp class="h-3.5 w-3.5" />{{ importingMarkdown ? '导入中…' : '导入 Markdown' }}<input id="markdown-input" ref="markdownInput" type="file" accept=".md,.markdown,text/markdown,text/plain" class="hidden" :disabled="importingMarkdown" @change="handleMarkdownImport" /></label>
            <label class="ghost-button min-h-0 cursor-pointer px-2 py-1 text-xs" for="body-image-input"><ImagePlus class="h-3.5 w-3.5" />{{ uploadingBodyImage ? `上传中 ${bodyImageProgress}%…` : '插入图片' }}<input id="body-image-input" ref="bodyInput" type="file" accept="image/jpeg,image/png,image/gif" class="hidden" :disabled="uploadingBodyImage" @change="handleBodyImageUpload" /></label>
          </div>
        </div>
        <textarea ref="editor" v-model="content" class="editor-textarea" spellcheck="false" placeholder="# 从这里开始\n\n记录你的思考、过程和结论……" aria-label="Markdown 编辑器" />
      </div>
      <div class="preview-pane">
        <div class="preview-pane-header">预览</div>
        <article class="preview-content article-body" v-html="previewHtml" />
      </div>
    </section>

    <div class="editor-statusbar">
      <span class="mono muted-text text-xs">{{ wordCount }} 字 · {{ isDirty ? '有未保存改动' : '未修改' }} · 快捷键 <span class="kbd">Ctrl</span> + <span class="kbd">S</span> 保存草稿</span>
      <div class="flex flex-wrap gap-2">
        <button class="secondary-button" type="button" :disabled="saving" @click="save(0)"><LoaderCircle v-if="saving" class="h-4 w-4 animate-spin" /><Save v-else class="h-4 w-4" />保存草稿</button>
        <button class="primary-button" type="button" :disabled="saving" @click="save(1)"><LoaderCircle v-if="saving" class="h-4 w-4 animate-spin" /><Send v-else class="h-4 w-4" />直接发布</button>
      </div>
    </div>
  </div>
</template>

<style scoped>
.no-underline { text-decoration: none; }
</style>
