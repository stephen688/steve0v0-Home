<script setup lang="ts">
import { onMounted, ref } from 'vue'
import { Image, Plus, RefreshCw, Trash2 } from '@lucide/vue'
import PageHeader from '@/components/ui/PageHeader.vue'
import ConfirmDialog from '@/components/ui/ConfirmDialog.vue'
import { deleteMoment, getAdminMoments, resolveAssetUrl, type MomentItem } from '@/api/admin'
import { formatDateTime } from '@/lib/format'
import { useToast } from '@/composables/useToast'

const { showToast } = useToast()
const loading = ref(true)
const loadingMore = ref(false)
const error = ref('')
const moments = ref<MomentItem[]>([])
const page = ref(1)
const total = ref(0)
const hasMore = ref(false)
const deleteTarget = ref<MomentItem | null>(null)
const deleting = ref(false)

async function loadMoments(reset = true) {
  if (reset) {
    loading.value = true
    page.value = 1
  } else {
    loadingMore.value = true
  }
  error.value = ''
  try {
    const result = await getAdminMoments({ page: page.value, size: 9 })
    moments.value = reset ? result.list : [...moments.value, ...result.list]
    total.value = result.total
    hasMore.value = result.hasMore
  } catch (err) {
    error.value = err instanceof Error ? err.message : '动态列表加载失败'
  } finally {
    loading.value = false
    loadingMore.value = false
  }
}

async function loadMore() {
  if (!hasMore.value || loadingMore.value) return
  page.value += 1
  await loadMoments(false)
}

async function confirmDelete() {
  if (!deleteTarget.value) return
  deleting.value = true
  try {
    await deleteMoment(deleteTarget.value.id)
    showToast('动态已删除')
    deleteTarget.value = null
    await loadMoments(true)
  } catch (err) {
    showToast(err instanceof Error ? err.message : '删除失败', 'error')
  } finally {
    deleting.value = false
  }
}

onMounted(() => loadMoments())
</script>

<template>
  <div class="max-w-[1400px]">
    <PageHeader title="动态管理" description="按时间倒序查看生活动态；动态目前只支持发布和删除。">
      <template #actions>
        <button class="secondary-button" type="button" :disabled="loading" @click="loadMoments(true)"><RefreshCw class="h-4 w-4" :class="loading ? 'animate-spin' : ''" />刷新</button>
        <router-link class="primary-button no-underline" to="/moments/new"><Plus class="h-4 w-4" />发布动态</router-link>
      </template>
    </PageHeader>

    <div v-if="error" class="notice error-notice mb-4">{{ error }}</div>
    <div class="mb-4 mono muted-text text-xs">共 {{ total }} 条动态</div>

    <div v-if="loading" class="moment-grid">
      <div v-for="index in 6" :key="index" class="paper-card skeleton h-48" />
    </div>
    <div v-else-if="!moments.length" class="paper-card empty-state">
      <span class="empty-icon"><Image class="h-6 w-6" /></span>
      <h3>还没有动态</h3>
      <p>把最近的一段生活记录下来，文字和图片都可以。</p>
      <router-link class="primary-button mt-5 no-underline" to="/moments/new"><Plus class="h-4 w-4" />发布第一条动态</router-link>
    </div>
    <div v-else class="moment-grid">
      <article v-for="moment in moments" :key="moment.id" class="paper-card moment-card">
        <div class="flex items-start justify-between gap-3">
          <span class="tape-badge badge-category">{{ moment.mediaType === 'image' ? '图片动态' : '文字动态' }}</span>
          <button class="icon-button hover:text-destructive" type="button" aria-label="删除动态" title="删除" @click="deleteTarget = moment"><Trash2 class="h-4 w-4" /></button>
        </div>
        <p class="moment-content">{{ moment.content }}</p>
        <div v-if="moment.mediaType === 'image' && moment.images.length" class="media-grid">
          <img v-for="(image, index) in moment.images" :key="`${moment.id}-${image}-${index}`" :src="resolveAssetUrl(image)" :alt="`动态图片 ${index + 1}`" loading="lazy" />
        </div>
        <div class="moment-meta"><span>{{ moment.images.length ? `${moment.images.length} 张图片` : '纯文字' }}</span><span>{{ formatDateTime(moment.createdAt) }}</span></div>
      </article>
    </div>

    <div v-if="hasMore" class="mt-6 flex justify-center">
      <button class="secondary-button" type="button" :disabled="loadingMore" @click="loadMore"><RefreshCw v-if="loadingMore" class="h-4 w-4 animate-spin" />{{ loadingMore ? '加载中…' : '加载更多' }}</button>
    </div>

    <ConfirmDialog
      :open="Boolean(deleteTarget)"
      title="删除这条动态？"
      description="删除后不可恢复，动态关联的图片记录也会一并清理。"
      :loading="deleting"
      @cancel="deleteTarget = null"
      @confirm="confirmDelete"
    />
  </div>
</template>

<style scoped>
.no-underline { text-decoration: none; }
</style>
