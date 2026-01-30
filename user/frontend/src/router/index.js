import { createRouter, createWebHistory } from 'vue-router'
import store from '@/store'

const routes = [
  {
    path: '/',
    name: 'Main',
    component: () => import('@/views/Main.vue')
  },
  {
    path: '/login',
    name: 'Login',
    component: () => import('@/views/Login.vue')
  },
  {
    path: '/join',
    name: 'Join',
    component: () => import('@/views/Join.vue')
  },
  {
    path: '/boards/notice',
    name: 'NoticeList',
    component: () => import('@/views/board/notice/NoticeList.vue')
  },
  {
    path: '/boards/notice/:id',
    name: 'NoticeView',
    component: () => import('@/views/board/notice/NoticeView.vue')
  },
  {
    path: '/boards/free',
    name: 'FreeList',
    component: () => import('@/views/board/free/FreeList.vue')
  },
  {
    path: '/boards/free/:id',
    name: 'FreeView',
    component: () => import('@/views/board/free/FreeView.vue')
  },
  {
    path: '/boards/free/write',
    name: 'FreeWrite',
    component: () => import('@/views/board/free/FreeWrite.vue'),
    meta: { requiresAuth: true }
  },
  {
    path: '/boards/free/modify/:id',
    name: 'FreeModify',
    component: () => import('@/views/board/free/FreeWrite.vue'),
    meta: { requiresAuth: true }
  },
  {
    path: '/boards/gallery',
    name: 'GalleryList',
    component: () => import('@/views/board/gallery/GalleryList.vue')
  },
  {
    path: '/boards/gallery/:id',
    name: 'GalleryView',
    component: () => import('@/views/board/gallery/GalleryView.vue')
  },
  {
    path: '/boards/gallery/write',
    name: 'GalleryWrite',
    component: () => import('@/views/board/gallery/GalleryWrite.vue'),
    meta: { requiresAuth: true }
  },
  {
    path: '/boards/gallery/modify/:id',
    name: 'GalleryModify',
    component: () => import('@/views/board/gallery/GalleryWrite.vue'),
    meta: { requiresAuth: true }
  },
  {
    path: '/boards/inquiry',
    name: 'InquiryList',
    component: () => import('@/views/board/inquiry/InquiryList.vue')
  },
  {
    path: '/boards/inquiry/:id',
    name: 'InquiryView',
    component: () => import('@/views/board/inquiry/InquiryView.vue'),
    meta: { requiresAuth: true }
  },
  {
    path: '/boards/inquiry/write',
    name: 'InquiryWrite',
    component: () => import('@/views/board/inquiry/InquiryWrite.vue'),
    meta: { requiresAuth: true }
  },
  {
    path: '/boards/inquiry/modify/:id',
    name: 'InquiryModify',
    component: () => import('@/views/board/inquiry/InquiryWrite.vue'),
    meta: { requiresAuth: true }
  },
  {
    path: '/error',
    name: 'Error',
    component: () => import('@/views/Error.vue')
  }
]

const router = createRouter({
  history: createWebHistory(import.meta.env.BASE_URL),
  routes
})

// Navigation Guard
router.beforeEach((to, from, next) => {
  const isLoggedIn = store.state.auth.isLoggedIn

  if (to.meta.requiresAuth && !isLoggedIn) {
    alert('로그인이 필요합니다.')
    next({ name: 'Login' })
  } else {
    next()
  }
})

export default router
