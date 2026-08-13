<script setup lang="ts">
import { onMounted, ref, watch } from 'vue'
import { Image, LoaderCircle, Save, UserRound } from '@lucide/vue'
import { getProfile, resolveAssetUrl, updateProfile } from '@/api/admin'
import { useToast } from '@/composables/useToast'

const { showToast } = useToast()
const loading = ref(true)
const submitting = ref(false)
const error = ref('')
const fieldError = ref('')
const name = ref('')
const avatarUrl = ref('')
const avatarBroken = ref(false)

watch(avatarUrl, () => { avatarBroken.value = false })

async function loadProfile() {
  loading.value = true
  error.value = ''
  try {
    const profile = await getProfile()
    name.value = profile?.name || ''
    avatarUrl.value = profile?.avatarUrl || ''
  } catch (err) {
    error.value = err instanceof Error ? err.message : '个人资料加载失败'
  } finally {
    loading.value = false
  }
}

async function submit() {
  fieldError.value = ''
  if (avatarUrl.value && !/^https:\/\//i.test(avatarUrl.value)) {
    fieldError.value = '头像地址必须是 HTTPS 外部地址；如需清空请留空。'
    return
  }
  submitting.value = true
  try {
    await updateProfile({ name: name.value.trim(), avatarUrl: avatarUrl.value.trim() })
    showToast('个人资料已保存')
  } catch (err) {
    showToast(err instanceof Error ? err.message : '个人资料保存失败', 'error')
  } finally {
    submitting.value = false
  }
}

onMounted(loadProfile)
</script>

<template>
  <div class="max-w-[680px]">
    <div class="page-header"><div><p class="eyebrow">About / Profile</p><h2 class="page-title">个人资料</h2><p class="page-description">姓名和头像会展示在关于页；这是覆盖式保存，留空即可清空。</p></div></div>
    <div v-if="error" class="notice error-notice mb-4">{{ error }}</div>
    <section v-if="loading" class="paper-card space-y-4 p-5"><div v-for="index in 3" :key="index" class="skeleton h-10 rounded-lg" /></section>
    <section v-else class="paper-card paper-card--ruled p-5">
      <h3 class="form-section-title">个人资料</h3>
      <div class="form-grid">
        <div class="flex flex-wrap items-center gap-5 border-b border-border pb-5">
          <div class="profile-preview">
            <img v-if="avatarUrl && !avatarBroken" :src="resolveAssetUrl(avatarUrl)" alt="头像预览" @error="avatarBroken = true" />
            <Image v-else class="h-8 w-8" />
          </div>
          <div><p class="font-serif text-lg">{{ name || '未填写姓名' }}</p><p class="mt-1 muted-text text-sm">头像预览</p></div>
        </div>
        <div><label class="field-label" for="profile-name">姓名</label><input id="profile-name" v-model="name" class="field-input" maxlength="100" placeholder="steve0v0" /></div>
        <div><label class="field-label" for="profile-avatar">头像 URL</label><input id="profile-avatar" v-model="avatarUrl" class="field-input" maxlength="500" placeholder="https://example.com/avatar.png" /><p class="field-hint">仅支持 HTTPS 外部地址；不通过此处上传文件。</p></div>
        <div v-if="fieldError" class="notice error-notice">{{ fieldError }}</div>
        <div class="form-actions"><button class="primary-button" type="button" :disabled="submitting" @click="submit"><LoaderCircle v-if="submitting" class="h-4 w-4 animate-spin" /><Save v-else class="h-4 w-4" />保存资料</button></div>
      </div>
    </section>
    <div class="mt-5 flex items-start gap-2 muted-text text-xs"><UserRound class="mt-0.5 h-4 w-4 shrink-0" /><span>公开资料只包含姓名与头像，技术栈和联系方式仍由小程序前端静态维护。</span></div>
  </div>
</template>
