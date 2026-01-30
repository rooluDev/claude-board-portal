# Claude Code Prompt - User Frontend (Phase 3: 게시판 목록/상세)

## 📋 Phase 3 목표
1. 공지사항 목록/상세 페이지
2. 자유게시판 목록/상세 페이지
3. 재사용 컴포넌트 (Pagination, FileList, CommentList)
4. BoardService 구현

---

## 1. BoardService

### services/boardService.js

```javascript
import api from './api'

export default {
  async getList(boardType, params) {
    const response = await api.get(`/boards/${boardType}`, { params })
    return response.data
  },
  
  async getDetail(boardType, id) {
    const response = await api.get(`/board/${boardType}/${id}`)
    return response.data
  },
  
  async create(boardType, formData) {
    const response = await api.post(`/board/${boardType}`, formData, {
      headers: { 'Content-Type': 'multipart/form-data' }
    })
    return response.data
  },
  
  async update(boardType, id, formData) {
    await api.put(`/board/${boardType}/${id}`, formData, {
      headers: { 'Content-Type': 'multipart/form-data' }
    })
  },
  
  async delete(boardType, id) {
    await api.delete(`/board/${boardType}/${id}`)
  },
  
  async increaseView(boardType, id) {
    await api.patch(`/board/${boardType}/${id}/increase-view`)
  },
  
  async checkAuthor(boardType, id) {
    const response = await api.get(`/board/${boardType}/${id}/check-author`)
    return response.data.isAuthor
  }
}
```

---

## 2. 재사용 컴포넌트

### components/board/Pagination.vue

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
    totalPages: Number,
    currentPage: Number
  },
  emits: ['change'],
  
  setup(props, { emit }) {
    const currentPageModel = computed({
      get: () => props.currentPage + 1,
      set: (value) => emit('change', value - 1)
    })
    
    const handlePageChange = (page) => {
      emit('change', page - 1)
    }
    
    return { currentPageModel, handlePageChange }
  }
}
</script>
```

### components/board/FileList.vue

(Phase 1에서 작성한 user-frontend-components.md 참조)

### components/board/CommentList.vue

(Phase 1에서 작성한 user-frontend-components.md 참조)

---

## 3. 공지사항 목록

### views/board/notice/NoticeList.vue

```vue
<template>
  <v-container>
    <h1 class="mb-4">공지사항</h1>
    
    <v-card class="mb-4">
      <v-card-text>
        <!-- 검색 폼 간소화 -->
        <v-row>
          <v-col cols="12" md="10">
            <v-text-field
              v-model="searchText"
              label="검색어"
              prepend-inner-icon="mdi-magnify"
              outlined
              dense
            />
          </v-col>
          <v-col cols="12" md="2">
            <v-btn color="primary" @click="handleSearch" block>검색</v-btn>
          </v-col>
        </v-row>
      </v-card-text>
    </v-card>
    
    <v-card>
      <v-table>
        <thead>
          <tr>
            <th width="10%">번호</th>
            <th width="50%">제목</th>
            <th width="15%">작성자</th>
            <th width="15%">작성일</th>
            <th width="10%">조회수</th>
          </tr>
        </thead>
        <tbody>
          <tr v-if="boards.length === 0">
            <td colspan="5" class="text-center text-grey">등록된 게시물이 없습니다.</td>
          </tr>
          <tr v-for="board in boards" :key="board.boardId" 
              @click="goToView(board.boardId)" style="cursor:pointer">
            <td>
              <v-chip v-if="board.isFixed" color="error" size="small">고정</v-chip>
              <span v-else>{{ board.boardId }}</span>
            </td>
            <td>{{ board.title }}</td>
            <td>{{ board.authorName }}</td>
            <td>{{ formatDate(board.createdAt) }}</td>
            <td>{{ board.views }}</td>
          </tr>
        </tbody>
      </v-table>
      
      <v-card-text>
        <pagination 
          v-if="totalPages > 0"
          :total-pages="totalPages" 
          :current-page="currentPage" 
          @change="handlePageChange" 
        />
      </v-card-text>
    </v-card>
  </v-container>
</template>

<script>
import { ref, onMounted, watch } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import boardService from '@/services/boardService'
import Pagination from '@/components/board/Pagination.vue'
import { format } from 'date-fns'

export default {
  name: 'NoticeList',
  components: { Pagination },
  
  setup() {
    const router = useRouter()
    const route = useRoute()
    
    const boards = ref([])
    const totalPages = ref(0)
    const currentPage = ref(0)
    const searchText = ref('')
    
    const fetchBoards = async () => {
      try {
        const params = {
          pageNum: route.query.page || 0,
          pageSize: 10,
          searchText: route.query.searchText
        }
        
        const response = await boardService.getList('notice', params)
        boards.value = response.content
        totalPages.value = response.totalPages
        currentPage.value = response.number
      } catch (error) {
        console.error('목록 조회 실패:', error)
      }
    }
    
    const handleSearch = () => {
      router.push({ 
        name: 'NoticeList', 
        query: { searchText: searchText.value, page: 0 } 
      })
    }
    
    const handlePageChange = (page) => {
      router.push({ 
        name: 'NoticeList', 
        query: { ...route.query, page } 
      })
    }
    
    const goToView = (id) => {
      router.push({ name: 'NoticeView', params: { id } })
    }
    
    const formatDate = (date) => {
      return format(new Date(date), 'yyyy-MM-dd')
    }
    
    onMounted(fetchBoards)
    watch(() => route.query, fetchBoards)
    
    return {
      boards,
      totalPages,
      currentPage,
      searchText,
      handleSearch,
      handlePageChange,
      goToView,
      formatDate
    }
  }
}
</script>
```

---

## 4. 공지사항 상세

### views/board/notice/NoticeView.vue

```vue
<template>
  <v-container>
    <v-card v-if="board">
      <v-card-title class="bg-grey-lighten-3">
        <v-chip v-if="board.isFixed" color="error" class="mr-2">고정</v-chip>
        <span class="text-h5">{{ board.title }}</span>
      </v-card-title>
      
      <v-card-subtitle class="py-3">
        <v-row>
          <v-col>작성자: {{ board.authorName }}</v-col>
          <v-col>작성일: {{ formatDate(board.createdAt) }}</v-col>
          <v-col>조회수: {{ board.views }}</v-col>
        </v-row>
      </v-card-subtitle>
      
      <v-divider />
      
      <v-card-text style="min-height: 300px; white-space: pre-wrap;">
        {{ board.content }}
      </v-card-text>
      
      <v-card-actions>
        <v-btn @click="goToList">목록</v-btn>
        <v-spacer />
      </v-card-actions>
    </v-card>
  </v-container>
</template>

<script>
import { ref, onMounted } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import boardService from '@/services/boardService'
import { format } from 'date-fns'

export default {
  name: 'NoticeView',
  
  setup() {
    const router = useRouter()
    const route = useRoute()
    const board = ref(null)
    
    const fetchBoard = async () => {
      try {
        // 조회수 증가
        await boardService.increaseView('notice', route.params.id)
        
        // 상세 조회
        board.value = await boardService.getDetail('notice', route.params.id)
      } catch (error) {
        console.error('상세 조회 실패:', error)
      }
    }
    
    const goToList = () => {
      router.push({ name: 'NoticeList' })
    }
    
    const formatDate = (date) => {
      return format(new Date(date), 'yyyy-MM-dd HH:mm')
    }
    
    onMounted(fetchBoard)
    
    return {
      board,
      goToList,
      formatDate
    }
  }
}
</script>
```

---

## 5. Router 업데이트

### router/index.js에 추가

```javascript
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
}
```

---

## Phase 3 완료 체크리스트

- [ ] BoardService 작성
- [ ] Pagination 컴포넌트
- [ ] NoticeList.vue
- [ ] NoticeView.vue
- [ ] FreeList.vue (NoticeList와 유사)
- [ ] FreeView.vue (NoticeView와 유사 + FileList + CommentList)
- [ ] Router 업데이트
- [ ] 테스트

자유게시판은 공지사항과 거의 동일하되, 파일 목록과 댓글 컴포넌트만 추가하면 됩니다!
