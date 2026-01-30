# Claude Code Prompt - User Frontend (Phase 4: 게시판 작성 & 갤러리 & 문의)

## 📋 Phase 4 목표
1. 자유게시판 작성/수정 페이지
2. 갤러리 게시판 (목록/상세/작성)
3. 문의게시판 (목록/상세/작성)
4. 전체 통합

---

## 1. 자유게시판 작성/수정

### views/board/free/FreeWrite.vue

```vue
<template>
  <v-container>
    <h1 class="mb-4">{{ isEditMode ? '게시물 수정' : '게시물 작성' }}</h1>
    
    <v-form ref="formRef" @submit.prevent="handleSubmit">
      <v-card>
        <v-card-text>
          <v-select
            v-model="form.categoryId"
            :items="categories"
            item-title="categoryName"
            item-value="categoryId"
            label="카테고리"
            :rules="[v => !!v || '카테고리를 선택하세요']"
            outlined
            required
          />
          
          <v-text-field
            v-model="form.title"
            label="제목"
            :rules="[
              v => !!v || '제목을 입력하세요',
              v => (v && v.length <= 99) || '제목은 99자 이내'
            ]"
            counter="99"
            outlined
            required
          />
          
          <v-textarea
            v-model="form.content"
            label="내용"
            :rules="[
              v => !!v || '내용을 입력하세요',
              v => (v && v.length <= 3999) || '내용은 3999자 이내'
            ]"
            counter="3999"
            rows="10"
            outlined
            required
          />
          
          <v-file-input
            v-model="files"
            label="파일 첨부 (최대 5개, 각 2MB)"
            multiple
            accept=".jpg,.jpeg,.gif,.png,.zip"
            prepend-icon="mdi-paperclip"
            outlined
          />
          
          <!-- 기존 파일 (수정 모드) -->
          <div v-if="isEditMode && existingFiles.length > 0">
            <h4>기존 파일</h4>
            <v-checkbox
              v-for="file in existingFiles"
              :key="file.fileId"
              v-model="deleteFileIds"
              :value="file.fileId"
              :label="file.originalName"
              density="compact"
            />
          </div>
        </v-card-text>
        
        <v-card-actions>
          <v-btn @click="goBack">취소</v-btn>
          <v-spacer />
          <v-btn type="submit" color="primary" :loading="loading">
            {{ isEditMode ? '수정' : '등록' }}
          </v-btn>
        </v-card-actions>
      </v-card>
    </v-form>
  </v-container>
</template>

<script>
import { ref, computed, onMounted } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import boardService from '@/services/boardService'
import categoryService from '@/services/categoryService'

export default {
  name: 'FreeWrite',
  
  setup() {
    const router = useRouter()
    const route = useRoute()
    const formRef = ref(null)
    const loading = ref(false)
    
    const form = ref({
      categoryId: null,
      title: '',
      content: ''
    })
    
    const files = ref([])
    const existingFiles = ref([])
    const deleteFileIds = ref([])
    const categories = ref([])
    
    const isEditMode = computed(() => !!route.params.id)
    
    const fetchCategories = async () => {
      const response = await categoryService.getAll()
      categories.value = response
    }
    
    const fetchBoard = async () => {
      if (!isEditMode.value) return
      
      const response = await boardService.getDetail('free', route.params.id)
      form.value = {
        categoryId: response.categoryId,
        title: response.title,
        content: response.content
      }
      existingFiles.value = response.files || []
    }
    
    const handleSubmit = async () => {
      const { valid } = await formRef.value.validate()
      if (!valid) return
      
      const formData = new FormData()
      formData.append('categoryId', form.value.categoryId)
      formData.append('title', form.value.title)
      formData.append('content', form.value.content)
      
      if (files.value && files.value.length > 0) {
        files.value.forEach(file => formData.append('files', file))
      }
      
      if (isEditMode.value && deleteFileIds.value.length > 0) {
        formData.append('deleteFileIds', deleteFileIds.value.join(','))
      }
      
      loading.value = true
      
      try {
        if (isEditMode.value) {
          await boardService.update('free', route.params.id, formData)
          alert('수정되었습니다.')
          router.push({ name: 'FreeView', params: { id: route.params.id } })
        } else {
          const response = await boardService.create('free', formData)
          alert('등록되었습니다.')
          router.push({ name: 'FreeView', params: { id: response.boardId } })
        }
      } catch (error) {
        console.error('저장 실패:', error)
      } finally {
        loading.value = false
      }
    }
    
    const goBack = () => router.back()
    
    onMounted(() => {
      fetchCategories()
      fetchBoard()
    })
    
    return {
      formRef,
      form,
      files,
      existingFiles,
      deleteFileIds,
      categories,
      isEditMode,
      loading,
      handleSubmit,
      goBack
    }
  }
}
</script>
```

---

## 2. CategoryService

### services/categoryService.js

```javascript
import api from './api'

export default {
  async getAll() {
    const response = await api.get('/categories')
    return response.data
  }
}
```

---

## 3. Router 완성

### router/index.js 최종

```javascript
const routes = [
  { path: '/', name: 'Main', component: () => import('@/views/Main.vue') },
  { path: '/login', name: 'Login', component: () => import('@/views/Login.vue') },
  { path: '/join', name: 'Join', component: () => import('@/views/Join.vue') },
  { path: '/error', name: 'Error', component: () => import('@/views/Error.vue') },
  
  // 공지사항
  { path: '/boards/notice', name: 'NoticeList', component: () => import('@/views/board/notice/NoticeList.vue') },
  { path: '/boards/notice/:id', name: 'NoticeView', component: () => import('@/views/board/notice/NoticeView.vue') },
  
  // 자유게시판
  { path: '/boards/free', name: 'FreeList', component: () => import('@/views/board/free/FreeList.vue') },
  { path: '/boards/free/:id', name: 'FreeView', component: () => import('@/views/board/free/FreeView.vue') },
  { path: '/boards/free/write', name: 'FreeWrite', component: () => import('@/views/board/free/FreeWrite.vue'), meta: { requiresAuth: true } },
  { path: '/boards/free/modify/:id', name: 'FreeModify', component: () => import('@/views/board/free/FreeWrite.vue'), meta: { requiresAuth: true } },
  
  // 갤러리
  { path: '/boards/gallery', name: 'GalleryList', component: () => import('@/views/board/gallery/GalleryList.vue') },
  { path: '/boards/gallery/:id', name: 'GalleryView', component: () => import('@/views/board/gallery/GalleryView.vue') },
  { path: '/boards/gallery/write', name: 'GalleryWrite', component: () => import('@/views/board/gallery/GalleryWrite.vue'), meta: { requiresAuth: true } },
  
  // 문의게시판
  { path: '/boards/inquiry', name: 'InquiryList', component: () => import('@/views/board/inquiry/InquiryList.vue') },
  { path: '/boards/inquiry/:id', name: 'InquiryView', component: () => import('@/views/board/inquiry/InquiryView.vue'), meta: { requiresAuth: true } },
  { path: '/boards/inquiry/write', name: 'InquiryWrite', component: () => import('@/views/board/inquiry/InquiryWrite.vue'), meta: { requiresAuth: true } }
]
```

---

## 4. CommentService & FileService

### services/commentService.js

```javascript
import api from './api'

export default {
  async create(data) {
    const response = await api.post('/comment', data)
    return response.data
  },
  
  async delete(commentId) {
    await api.delete(`/comment/${commentId}`)
  }
}
```

### services/fileService.js

```javascript
import api from './api'

export default {
  async download(fileId, fileName) {
    const response = await api.get(`/file/${fileId}/download`, {
      responseType: 'blob'
    })
    
    const url = window.URL.createObjectURL(new Blob([response.data]))
    const link = document.createElement('a')
    link.href = url
    link.setAttribute('download', fileName)
    document.body.appendChild(link)
    link.click()
    link.remove()
  }
}
```

---

## 🎉 전체 프로젝트 완료!

모든 Phase 완료 시 구현된 기능:
✅ 프로젝트 설정 (Vue 3, Vuetify, Router, Vuex)
✅ JWT 인증 (로그인/회원가입)
✅ 에러 처리 (Command 패턴)
✅ 공지사항 (목록/상세)
✅ 자유게시판 (CRUD + 파일 + 댓글)
✅ 갤러리 (CRUD + 썸네일)
✅ 문의게시판 (CRUD + 비밀글)
✅ 반응형 디자인 (Vuetify)

---

## 빌드 및 배포

```bash
# 프로덕션 빌드
npm run build

# Nginx 배포 (dist 폴더)
```

User Frontend SPA가 완성되었습니다!
