<script setup lang="ts">
import { computed, onUnmounted, ref } from 'vue'
import { useRouter } from 'vue-router'
import { ArrowLeft, ChevronDown, ChevronUp, ImagePlus, LoaderCircle, Send, Trash2 } from '@lucide/vue'
import { createMoment, uploadImage, type MomentPayload } from '@/api/admin'
import { useToast } from '@/composables/useToast'

interface UploadEntry {
  id: number
  name: string
  previewUrl: string
  url: string
  state: 'uploading' | 'done' | 'error'
  progress: number
  error?: string
}

const router = useRouter()
const { showToast } = useToast()
const content = ref('')
const createdAt = ref('')
const location = ref('')
const mediaType = ref<'text' | 'image'>('text')
const uploads = ref<UploadEntry[]>([])
const isDragging = ref(false)
const submitting = ref(false)
const error = ref('')
let nextUploadId = 1

const completedUploads = computed(() => uploads.value.filter((item) => item.state === 'done' && item.url))

function addFiles(files: FileList | File[]) {
  mediaType.value = 'image'
  const candidates = Array.from(files).filter((file) => file.type.startsWith('image/'))
  const remaining = Math.max(0, 9 - uploads.value.length)
  if (candidates.length > remaining) error.value = '每条动态最多上传 9 张图片'
  candidates.slice(0, remaining).forEach((file) => {
    if (!file.type.startsWith('image/')) return
    const entry: UploadEntry = {
      id: nextUploadId++,
      name: file.name,
      previewUrl: URL.createObjectURL(file),
      url: '',
      state: 'uploading',
      progress: 0
    }
    uploads.value.push(entry)
    void uploadEntry(entry, file)
  })
}

async function uploadEntry(entry: UploadEntry, file: File) {
  try {
    entry.url = await uploadImage(file, (progress) => { entry.progress = progress })
    entry.state = 'done'
  } catch (err) {
    entry.state = 'error'
    entry.error = err instanceof Error ? err.message : '上传失败'
  }
}

function removeUpload(entry: UploadEntry) {
  URL.revokeObjectURL(entry.previewUrl)
  uploads.value = uploads.value.filter((item) => item.id !== entry.id)
}

function moveUpload(index: number, direction: -1 | 1) {
  const target = index + direction
  if (target < 0 || target >= uploads.value.length) return
  const next = [...uploads.value]
  const [item] = next.splice(index, 1)
  next.splice(target, 0, item)
  uploads.value = next
}

function handleDrop(event: DragEvent) {
  event.preventDefault()
  isDragging.value = false
  if (event.dataTransfer?.files) addFiles(event.dataTransfer.files)
}

function handleImageInput(event: Event) {
  const files = (event.target as HTMLInputElement).files
  if (files) addFiles(files)
}

async function submit() {
  error.value = ''
  if (!content.value.trim() && !location.value.trim() && !completedUploads.value.length) {
    error.value = '请填写动态内容、地点或上传图片'
    return
  }
  if (mediaType.value === 'image' && uploads.value.some((item) => item.state === 'uploading')) {
    error.value = '图片仍在上传，请稍候'
    return
  }
  if (mediaType.value === 'image' && !completedUploads.value.length) {
    error.value = '图片动态至少需要一张图片'
    return
  }
  submitting.value = true
  const payload: MomentPayload = {
    content: content.value.trim(),
    mediaType: mediaType.value,
    images: completedUploads.value.map((item) => item.url),
    ...(location.value.trim() ? { location: location.value.trim() } : {}),
    ...(createdAt.value ? { createdAt: `${createdAt.value}:00` } : {})
  }
  try {
    await createMoment(payload)
    showToast('动态已发布')
    await router.push('/moments')
  } catch (err) {
    showToast(err instanceof Error ? err.message : '动态发布失败', 'error')
  } finally {
    submitting.value = false
  }
}

onUnmounted(() => uploads.value.forEach((entry) => URL.revokeObjectURL(entry.previewUrl)))
</script>

<template>
  <div class="max-w-[680px]">
    <div class="page-header">
      <div><p class="eyebrow">Life stream / Compose</p><h2 class="page-title">发布动态</h2><p class="page-description">一段文字，或一段文字加上几张图片。</p></div>
      <router-link class="ghost-button no-underline" to="/moments"><ArrowLeft class="h-4 w-4" />返回动态</router-link>
    </div>

    <section class="paper-card paper-card--ruled p-5">
      <h3 class="form-section-title">动态内容</h3>
      <div class="form-grid">
        <div>
          <label class="field-label" for="moment-content">内容 <span class="text-destructive">*</span></label>
          <textarea id="moment-content" v-model="content" class="field-textarea min-h-[180px]" placeholder="今天发生了什么？" maxlength="5000" />
          <p class="field-hint text-right">{{ content.length }} / 5000</p>
        </div>

        <div>
          <label class="field-label" for="moment-created-at">原始发布时间</label>
          <input id="moment-created-at" v-model="createdAt" class="field-input" type="datetime-local" />
          <p class="field-hint">搬运历史动态时填写朋友圈时间；留空则按发布时刻记录。</p>
        </div>

        <div>
          <label class="field-label" for="moment-location">原始地点</label>
          <input id="moment-location" v-model="location" class="field-input" maxlength="200" placeholder="例如：广州市·小洲村" />
          <p class="field-hint">朋友圈没有显示地点时留空。</p>
        </div>

        <div>
          <span class="field-label">媒体类型</span>
          <div class="filter-group w-fit">
            <button class="filter-tab" :class="{ 'is-active': mediaType === 'text' }" type="button" @click="mediaType = 'text'">文字</button>
            <button class="filter-tab" :class="{ 'is-active': mediaType === 'image' }" type="button" @click="mediaType = 'image'">图片</button>
          </div>
        </div>

        <div v-if="mediaType === 'image'">
          <span class="field-label">图片</span>
          <label class="upload-zone cursor-pointer" :class="{ 'is-dragging': isDragging }" for="moment-images" @dragover.prevent="isDragging = true" @dragleave.prevent="isDragging = false" @drop="handleDrop">
            <ImagePlus class="mb-2 h-6 w-6" />
            <strong>拖拽或点击上传图片</strong>
            <span class="mt-1 text-xs">最多 9 张，上传后可调整顺序</span>
            <input id="moment-images" type="file" accept="image/jpeg,image/png,image/gif" multiple @change="handleImageInput" />
          </label>
          <div v-if="uploads.length" class="upload-list">
            <div v-for="(entry, index) in uploads" :key="entry.id" class="upload-item">
              <img class="upload-thumb" :src="entry.previewUrl" :alt="entry.name" />
              <div class="upload-progress">
                <strong>{{ entry.name }}</strong>
                <small>{{ entry.state === 'uploading' ? '上传中…' : entry.state === 'done' ? '已上传' : entry.error }}</small>
                <div v-if="entry.state === 'uploading'" class="progress-track"><div class="progress-value" :style="{ width: `${entry.progress}%` }" /></div>
              </div>
              <div class="flex gap-1">
                <button class="icon-button" type="button" :disabled="index === 0" aria-label="上移" @click="moveUpload(index, -1)"><ChevronUp class="h-4 w-4" /></button>
                <button class="icon-button" type="button" :disabled="index === uploads.length - 1" aria-label="下移" @click="moveUpload(index, 1)"><ChevronDown class="h-4 w-4" /></button>
                <button class="icon-button hover:text-destructive" type="button" aria-label="移除图片" @click="removeUpload(entry)"><Trash2 class="h-4 w-4" /></button>
              </div>
            </div>
          </div>
        </div>

        <div v-if="error" class="notice error-notice">{{ error }}</div>
        <div class="form-actions">
          <router-link class="secondary-button no-underline" to="/moments">取消</router-link>
          <button class="primary-button" type="button" :disabled="submitting" @click="submit"><LoaderCircle v-if="submitting" class="h-4 w-4 animate-spin" /><Send v-else class="h-4 w-4" />发布动态</button>
        </div>
      </div>
    </section>
  </div>
</template>

<style scoped>
.no-underline { text-decoration: none; }
</style>
