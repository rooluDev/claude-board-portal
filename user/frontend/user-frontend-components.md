# User Frontend - Component Structure

## 프로젝트: board-portal/user/frontend

---

## 1. 컴포넌트 계층 구조

```
App.vue
├── Navbar.vue (전역)
├── Footer.vue (전역)
└── Router View
    ├── Main.vue
    ├── Login.vue
    ├── Join.vue
    ├── board/
    │   ├── notice/
    │   │   ├── NoticeList.vue
    │   │   └── NoticeView.vue
    │   ├── free/
    │   │   ├── FreeList.vue
    │   │   ├── FreeView.vue
    │   │   └── FreeWrite.vue
    │   ├── gallery/
    │   │   ├── GalleryList.vue
    │   │   ├── GalleryView.vue
    │   │   └── GalleryWrite.vue
    │   └── inquiry/
    │       ├── InquiryList.vue
    │       ├── InquiryView.vue
    │       └── InquiryWrite.vue
    └── Error.vue
```

---

## 2. Layout 컴포넌트

### 2.1 App.vue

```vue
<template>
  <v-app>
    <navbar />
    <v-main>
      <router-view />
    </v-main>
    <footer-component />
  </v-app>
</template>

<script>
import Navbar from '@/components/layout/Navbar.vue'
import FooterComponent from '@/components/layout/Footer.vue'

export default {
  name: 'App',
  components: { Navbar, FooterComponent }
}
</script>
```

### 2.2 Navbar.vue

```vue
<template>
  <v-app-bar app color="primary" dark>
    <v-toolbar-title @click="goHome" style="cursor:pointer">
      eBrain Portal
    </v-toolbar-title>
    
    <v-spacer></v-spacer>
    
    <v-btn text @click="goToBoard('notice')">공지사항</v-btn>
    <v-btn text @click="goToBoard('free')">자유게시판</v-btn>
    <v-btn text @click="goToBoard('gallery')">갤러리</v-btn>
    <v-btn text @click="goToBoard('inquiry')">문의</v-btn>
    
    <v-spacer></v-spacer>
    
    <template v-if="isLoggedIn">
      <span class="mr-4">{{ memberName }}님</span>
      <v-btn text @click="handleLogout">로그아웃</v-btn>
    </template>
    <template v-else>
      <v-btn text @click="goToLogin">로그인</v-btn>
      <v-btn text @click="goToJoin">회원가입</v-btn>
    </template>
  </v-app-bar>
</template>

<script>
import { computed } from 'vue'
import { useRouter } from 'vue-router'
import { useStore } from 'vuex'

export default {
  name: 'Navbar',
  setup() {
    const router = useRouter()
    const store = useStore()
    
    const isLoggedIn = computed(() => store.state.auth.isLoggedIn)
    const memberName = computed(() => store.state.auth.memberName)
    
    const goHome = () => router.push({ name: 'Main' })
    const goToBoard = (type) => router.push({ name: `${type.charAt(0).toUpperCase() + type.slice(1)}List` })
    const goToLogin = () => router.push({ name: 'Login' })
    const goToJoin = () => router.push({ name: 'Join' })
    
    const handleLogout = () => {
      store.dispatch('auth/logout')
      alert('로그아웃되었습니다.')
      router.push({ name: 'Main' })
    }
    
    return {
      isLoggedIn,
      memberName,
      goHome,
      goToBoard,
      goToLogin,
      goToJoin,
      handleLogout
    }
  }
}
</script>
```

---

## 3. 재사용 컴포넌트

### 3.1 BoardSearch.vue

```vue
<template>
  <v-card class="mb-4">
    <v-card-text>
      <v-row>
        <v-col cols="12" md="3">
          <v-text-field
            v-model="searchForm.startDate"
            type="date"
            label="시작일"
            outlined
            dense
          />
        </v-col>
        <v-col cols="12" md="3">
          <v-text-field
            v-model="searchForm.endDate"
            type="date"
            label="종료일"
            outlined
            dense
          />
        </v-col>
        <v-col cols="12" md="2">
          <v-select
            v-model="searchForm.category"
            :items="categories"
            item-title="categoryName"
            item-value="categoryId"
            label="카테고리"
            outlined
            dense
          />
        </v-col>
        <v-col cols="12" md="3">
          <v-text-field
            v-model="searchForm.searchText"
            label="검색어"
            outlined
            dense
          />
        </v-col>
        <v-col cols="12" md="1">
          <v-btn color="primary" @click="handleSearch" block>검색</v-btn>
        </v-col>
      </v-row>
    </v-card-text>
  </v-card>
</template>

<script>
import { ref, onMounted } from 'vue'
import categoryService from '@/services/categoryService'

export default {
  name: 'BoardSearch',
  emits: ['search'],
  
  setup(props, { emit }) {
    const searchForm = ref({
      startDate: '',
      endDate: '',
      category: -1,
      searchText: ''
    })
    
    const categories = ref([
      { categoryId: -1, categoryName: '전체' }
    ])
    
    const fetchCategories = async () => {
      try {
        const response = await categoryService.getAll()
        categories.value = [
          { categoryId: -1, categoryName: '전체' },
          ...response
        ]
      } catch (error) {
        console.error('카테고리 조회 실패:', error)
      }
    }
    
    const handleSearch = () => {
      emit('search', { ...searchForm.value })
    }
    
    onMounted(() => {
      fetchCategories()
    })
    
    return {
      searchForm,
      categories,
      handleSearch
    }
  }
}
</script>
```

### 3.2 Pagination.vue

```vue
<template>
  <v-pagination
    v-model="currentPageModel"
    :length="totalPages"
    :total-visible="7"
    @update:modelValue="handlePageChange"
  />
</template>

<script>
import { computed } from 'vue'

export default {
  name: 'Pagination',
  props: {
    totalPages: {
      type: Number,
      required: true
    },
    currentPage: {
      type: Number,
      required: true
    }
  },
  emits: ['change'],
  
  setup(props, { emit }) {
    const currentPageModel = computed({
      get: () => props.currentPage + 1, // 0-based → 1-based
      set: (value) => emit('change', value - 1) // 1-based → 0-based
    })
    
    const handlePageChange = (page) => {
      emit('change', page - 1)
    }
    
    return {
      currentPageModel,
      handlePageChange
    }
  }
}
</script>
```

### 3.3 CommentList.vue

```vue
<template>
  <v-card class="mt-4">
    <v-card-title>댓글 ({{ comments.length }})</v-card-title>
    <v-card-text>
      <!-- 댓글 목록 -->
      <div v-for="comment in comments" :key="comment.commentId" class="mb-3">
        <div class="d-flex justify-space-between">
          <div>
            <strong>{{ comment.authorName }}</strong>
            <span class="text-caption ml-2">
              {{ formatDate(comment.createdAt) }}
            </span>
          </div>
          <v-btn
            v-if="canDelete(comment)"
            size="small"
            color="error"
            text
            @click="handleDelete(comment.commentId)"
          >
            삭제
          </v-btn>
        </div>
        <div class="mt-1">{{ comment.content }}</div>
        <v-divider class="mt-2" />
      </div>
      
      <!-- 댓글 작성 폼 -->
      <div v-if="isLoggedIn" class="mt-4">
        <v-textarea
          v-model="newComment"
          label="댓글 작성"
          rows="3"
          outlined
        />
        <v-btn color="primary" @click="handleSubmit">댓글 등록</v-btn>
      </div>
      <div v-else class="text-center mt-4">
        <p>댓글을 작성하려면 로그인이 필요합니다.</p>
      </div>
    </v-card-text>
  </v-card>
</template>

<script>
import { ref, computed } from 'vue'
import { useStore } from 'vuex'
import commentService from '@/services/commentService'
import { format } from 'date-fns'

export default {
  name: 'CommentList',
  props: {
    boardType: String,
    boardId: Number,
    comments: Array
  },
  emits: ['refresh'],
  
  setup(props, { emit }) {
    const store = useStore()
    const newComment = ref('')
    
    const isLoggedIn = computed(() => store.state.auth.isLoggedIn)
    const memberId = computed(() => store.state.auth.memberId)
    
    const canDelete = (comment) => {
      return comment.authorId === memberId.value
    }
    
    const handleSubmit = async () => {
      if (!newComment.value.trim()) {
        alert('댓글 내용을 입력하세요.')
        return
      }
      
      try {
        await commentService.create({
          boardType: props.boardType,
          boardId: props.boardId,
          content: newComment.value
        })
        
        newComment.value = ''
        emit('refresh')
        alert('댓글이 등록되었습니다.')
      } catch (error) {
        console.error('댓글 작성 실패:', error)
      }
    }
    
    const handleDelete = async (commentId) => {
      if (!confirm('정말 삭제하시겠습니까?')) return
      
      try {
        await commentService.delete(commentId)
        emit('refresh')
        alert('댓글이 삭제되었습니다.')
      } catch (error) {
        console.error('댓글 삭제 실패:', error)
      }
    }
    
    const formatDate = (date) => {
      return format(new Date(date), 'yyyy-MM-dd HH:mm')
    }
    
    return {
      newComment,
      isLoggedIn,
      canDelete,
      handleSubmit,
      handleDelete,
      formatDate
    }
  }
}
</script>
```

### 3.4 FileList.vue

```vue
<template>
  <v-card v-if="files && files.length > 0" class="mt-4">
    <v-card-title>첨부파일 ({{ files.length }})</v-card-title>
    <v-card-text>
      <v-list>
        <v-list-item
          v-for="file in files"
          :key="file.fileId"
          @click="handleDownload(file.fileId, file.originalName)"
        >
          <v-list-item-title>
            <v-icon>mdi-file-download</v-icon>
            {{ file.originalName }}
            <span class="text-caption ml-2">({{ formatFileSize(file.size) }})</span>
          </v-list-item-title>
        </v-list-item>
      </v-list>
    </v-card-text>
  </v-card>
</template>

<script>
import fileService from '@/services/fileService'

export default {
  name: 'FileList',
  props: {
    files: Array
  },
  
  setup() {
    const handleDownload = async (fileId, fileName) => {
      try {
        await fileService.download(fileId, fileName)
      } catch (error) {
        console.error('파일 다운로드 실패:', error)
        alert('파일 다운로드에 실패했습니다.')
      }
    }
    
    const formatFileSize = (bytes) => {
      if (bytes < 1024) return bytes + ' B'
      if (bytes < 1024 * 1024) return (bytes / 1024).toFixed(2) + ' KB'
      return (bytes / (1024 * 1024)).toFixed(2) + ' MB'
    }
    
    return {
      handleDownload,
      formatFileSize
    }
  }
}
</script>
```

---

## 4. View 컴포넌트 상세

### 4.1 FreeWrite.vue (작성/수정)

```vue
<template>
  <v-container>
    <h1 class="mb-4">{{ isEditMode ? '게시물 수정' : '게시물 작성' }}</h1>
    
    <v-form ref="formRef" @submit.prevent="handleSubmit">
      <v-select
        v-model="form.categoryId"
        :items="categories"
        item-title="categoryName"
        item-value="categoryId"
        label="카테고리"
        :rules="[v => !!v || '카테고리를 선택하세요']"
        required
      />
      
      <v-text-field
        v-model="form.title"
        label="제목"
        :rules="[
          v => !!v || '제목을 입력하세요',
          v => (v && v.length <= 99) || '제목은 99자 이내로 입력하세요'
        ]"
        counter="99"
        required
      />
      
      <v-textarea
        v-model="form.content"
        label="내용"
        :rules="[
          v => !!v || '내용을 입력하세요',
          v => (v && v.length <= 3999) || '내용은 3999자 이내로 입력하세요'
        ]"
        counter="3999"
        rows="10"
        required
      />
      
      <v-file-input
        v-model="files"
        label="파일 첨부 (최대 5개, 각 2MB)"
        multiple
        accept=".jpg,.jpeg,.gif,.png,.zip"
        :rules="[validateFiles]"
        prepend-icon="mdi-paperclip"
      />
      
      <!-- 기존 파일 목록 (수정 모드) -->
      <div v-if="isEditMode && existingFiles.length > 0" class="mt-2">
        <h3>기존 파일</h3>
        <v-checkbox
          v-for="file in existingFiles"
          :key="file.fileId"
          v-model="deleteFileIds"
          :value="file.fileId"
          :label="file.originalName"
        />
      </div>
      
      <div class="mt-4">
        <v-btn type="submit" color="primary" class="mr-2">
          {{ isEditMode ? '수정' : '등록' }}
        </v-btn>
        <v-btn @click="goBack">취소</v-btn>
      </div>
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
    
    const validateFiles = (files) => {
      if (!files || files.length === 0) return true
      if (files.length > 5) return '파일은 최대 5개까지 첨부 가능합니다.'
      
      for (let file of files) {
        if (file.size > 2 * 1024 * 1024) {
          return `${file.name}은(는) 2MB를 초과합니다.`
        }
      }
      return true
    }
    
    const handleSubmit = async () => {
      const { valid } = await formRef.value.validate()
      if (!valid) return
      
      const formData = new FormData()
      formData.append('categoryId', form.value.categoryId)
      formData.append('title', form.value.title)
      formData.append('content', form.value.content)
      
      if (files.value && files.value.length > 0) {
        files.value.forEach(file => {
          formData.append('files', file)
        })
      }
      
      if (isEditMode.value && deleteFileIds.value.length > 0) {
        formData.append('deleteFileIds', deleteFileIds.value.join(','))
      }
      
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
      }
    }
    
    const goBack = () => {
      router.back()
    }
    
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
      validateFiles,
      handleSubmit,
      goBack
    }
  }
}
</script>
```

---

## 5. 공통 유틸리티

### utils/validators.js

```javascript
export const validators = {
  required: v => !!v || '필수 입력 항목입니다.',
  
  minLength: (min) => v => 
    (v && v.length >= min) || `최소 ${min}자 이상 입력하세요.`,
  
  maxLength: (max) => v => 
    (v && v.length <= max) || `최대 ${max}자 이하로 입력하세요.`,
  
  email: v => 
    !v || /.+@.+\..+/.test(v) || '올바른 이메일 형식이 아닙니다.',
  
  password: v => {
    if (!v) return '비밀번호를 입력하세요.'
    if (v.length < 4 || v.length > 20) return '비밀번호는 4-20자여야 합니다.'
    return true
  }
}
```

---

이 컴포넌트 구조 문서는 실제 구현 시 참고용입니다!
