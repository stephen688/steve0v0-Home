import { createRouter, createWebHistory } from 'vue-router'
import { useAuthStore } from '@/stores/auth'
import LoginView from '@/views/LoginView.vue'
import AppLayout from '@/layouts/AppLayout.vue'

const router = createRouter({
  history: createWebHistory(),
  routes: [
    {
      path: '/login',
      name: 'Login',
      component: LoginView,
      meta: { public: true }
    },
    {
      path: '/',
      component: AppLayout,
      children: [
        {
          path: '',
          name: 'Dashboard',
          component: () => import('@/views/DashboardView.vue')
        },
        {
          path: '/articles',
          name: 'Articles',
          component: () => import('@/views/ArticlesView.vue'),
          meta: { title: '文章管理', width: 'wide' }
        },
        {
          path: '/articles/new',
          name: 'ArticleNew',
          component: () => import('@/views/ArticleFormView.vue'),
          meta: { title: '新建文章', width: 'wide' }
        },
        {
          path: '/articles/:id/edit',
          name: 'ArticleEdit',
          component: () => import('@/views/ArticleFormView.vue'),
          meta: { title: '编辑文章', width: 'wide' }
        },
        {
          path: '/articles/:id',
          name: 'ArticleDetail',
          component: () => import('@/views/ArticleDetailView.vue'),
          meta: { title: '文章详情', width: 'narrow' }
        },
        {
          path: '/moments',
          name: 'Moments',
          component: () => import('@/views/MomentsView.vue'),
          meta: { title: '动态管理', width: 'wide' }
        },
        {
          path: '/moments/new',
          name: 'MomentNew',
          component: () => import('@/views/MomentFormView.vue'),
          meta: { title: '发布动态', width: 'narrow' }
        },
        {
          path: '/study-records',
          name: 'StudyRecords',
          component: () => import('@/views/StudyRecordsView.vue'),
          meta: { title: '学习记录', width: 'wide' }
        },
        {
          path: '/study-records/new',
          name: 'StudyRecordNew',
          component: () => import('@/views/StudyRecordFormView.vue'),
          meta: { title: '新增学习记录', width: 'narrow' }
        },
        {
          path: '/study-records/:id/edit',
          name: 'StudyRecordEdit',
          component: () => import('@/views/StudyRecordFormView.vue'),
          meta: { title: '编辑学习记录', width: 'narrow' }
        },
        {
          path: '/courses',
          name: 'Courses',
          component: () => import('@/views/CoursesView.vue'),
          meta: { title: '课程表', width: 'wide' }
        },
        {
          path: '/courses/new',
          name: 'CourseNew',
          component: () => import('@/views/CourseFormView.vue'),
          meta: { title: '新增课程', width: 'narrow' }
        },
        {
          path: '/courses/:id/edit',
          name: 'CourseEdit',
          component: () => import('@/views/CourseFormView.vue'),
          meta: { title: '编辑课程', width: 'narrow' }
        },
        {
          path: '/status',
          name: 'Status',
          component: () => import('@/views/StatusView.vue'),
          meta: { title: '个人状态', width: 'narrow' }
        },
        {
          path: '/about/profile',
          name: 'AboutProfile',
          component: () => import('@/views/ProfileView.vue'),
          meta: { title: '个人资料', width: 'narrow' }
        },
        {
          path: '/about/projects',
          name: 'AboutProjects',
          component: () => import('@/views/ProjectsView.vue'),
          meta: { title: 'GitHub 项目', width: 'wide' }
        },
        {
          path: '/about/projects/new',
          name: 'ProjectNew',
          component: () => import('@/views/ProjectFormView.vue'),
          meta: { title: '新增项目', width: 'narrow' }
        },
        {
          path: '/about/projects/:id/edit',
          name: 'ProjectEdit',
          component: () => import('@/views/ProjectFormView.vue'),
          meta: { title: '编辑项目', width: 'narrow' }
        }
      ]
    }
  ]
})

// 路由守卫：未登录跳转登录页，已登录访问登录页则跳转首页
router.beforeEach((to) => {
  const authStore = useAuthStore()
  const isPublic = to.meta.public === true

  if (!isPublic && !authStore.isAuthenticated) {
    return { name: 'Login', query: { redirect: to.fullPath } }
  }

  if (to.name === 'Login' && authStore.isAuthenticated) {
    return { name: 'Dashboard' }
  }

  return true
})

export default router
