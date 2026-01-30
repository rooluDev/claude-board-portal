# User Frontend - Technical Specification

## 프로젝트: board-portal/user/frontend

---

## 1. 프로젝트 설정

### 1.1 package.json

```json
{
  "name": "user-frontend",
  "version": "1.0.0",
  "private": true,
  "scripts": {
    "serve": "vue-cli-service serve",
    "build": "vue-cli-service build",
    "lint": "vue-cli-service lint"
  },
  "dependencies": {
    "vue": "^3.2.13",
    "vue-router": "^4.2.5",
    "vuex": "^4.0.2",
    "vuex-persistedstate": "^4.1.0",
    "vuetify": "^3.0.0",
    "@mdi/font": "^7.0.96",
    "axios": "^1.6.7",
    "date-fns": "^3.6.0",
    "moment": "^2.30.1"
  },
  "devDependencies": {
    "@vue/cli-service": "~5.0.0",
    "sass": "^1.32.0",
    "sass-loader": "^12.0.0"
  }
}
```

### 1.2 vue.config.js

```javascript
const { defineConfig } = require('@vue/cli-service')

module.exports = defineConfig({
  transpileDependencies: true,
  
  devServer: {
    port: 80,
    proxy: {
      '/api': {
        target: 'http://localhost:8081',
        changeOrigin: true
      }
    }
  },
  
  publicPath: process.env.NODE_ENV === 'production' ? '/' : '/',
  
  outputDir: 'dist',
  
  assetsDir: 'static',
  
  productionSourceMap: false
})
```

---

## 2. Router 설정

### router/index.js

```javascript
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
  // ... 갤러리, 문의게시판 라우트
  {
    path: '/error',
    name: 'Error',
    component: () => import('@/views/Error.vue')
  }
]

const router = createRouter({
  history: createWebHistory(process.env.BASE_URL),
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
```

---

## 3. Vuex Store

### store/index.js

```javascript
import { createStore } from 'vuex'
import createPersistedState from 'vuex-persistedstate'
import auth from './modules/auth'

export default createStore({
  modules: {
    auth
  },
  plugins: [
    createPersistedState({
      storage: window.localStorage,
      paths: ['auth']
    })
  ]
})
```

### store/modules/auth.js

```javascript
export default {
  namespaced: true,
  
  state: {
    accessToken: null,
    memberId: null,
    memberName: null,
    isLoggedIn: false
  },
  
  mutations: {
    SET_AUTH(state, { accessToken, memberId, memberName }) {
      state.accessToken = accessToken
      state.memberId = memberId
      state.memberName = memberName
      state.isLoggedIn = true
    },
    
    CLEAR_AUTH(state) {
      state.accessToken = null
      state.memberId = null
      state.memberName = null
      state.isLoggedIn = false
    }
  },
  
  actions: {
    login({ commit }, authData) {
      commit('SET_AUTH', authData)
    },
    
    logout({ commit }) {
      commit('CLEAR_AUTH')
    }
  },
  
  getters: {
    isLoggedIn: state => state.isLoggedIn,
    memberName: state => state.memberName
  }
}
```

---

## 4. Axios 설정

### services/api.js

```javascript
import axios from 'axios'
import store from '@/store'
import { ErrorCommandFactory } from '@/utils/errorHandler'

const api = axios.create({
  baseURL: process.env.VUE_APP_API_BASE_URL || 'http://localhost:8081/api',
  timeout: 10000,
  headers: {
    'Content-Type': 'application/json'
  }
})

// Request Interceptor (JWT 자동 추가)
api.interceptors.request.use(
  config => {
    const token = store.state.auth.accessToken
    if (token) {
      config.headers.Authorization = `Bearer ${token}`
    }
    return config
  },
  error => {
    return Promise.reject(error)
  }
)

// Response Interceptor (에러 처리)
api.interceptors.response.use(
  response => response,
  error => {
    // Command 패턴으로 에러 처리
    const command = ErrorCommandFactory.createCommand(error)
    command.execute()
    
    return Promise.reject(error)
  }
)

export default api
```

---

## 5. 에러 처리 (Command 패턴)

### utils/errorHandler.js

```javascript
import router from '@/router'

// ErrorCommand 추상 클래스
class ErrorCommand {
  constructor(error) {
    this.error = error
  }
  
  execute() {
    throw new Error('execute() must be implemented')
  }
}

// 구체적인 Command 클래스들
class BoardNotFoundCommand extends ErrorCommand {
  execute() {
    alert('존재하지 않는 게시물입니다.')
    router.push({ name: 'Main' })
  }
}

class NotLoggedInCommand extends ErrorCommand {
  execute() {
    router.push({ name: 'Error' })
  }
}

class IllegalFileDataCommand extends ErrorCommand {
  execute() {
    alert('잘못된 파일 데이터입니다.')
    router.push({ name: 'Error' })
  }
}

class LoginFailCommand extends ErrorCommand {
  execute() {
    console.log('로그인 실패:', this.error.response.data.message)
  }
}

class NotMyBoardCommand extends ErrorCommand {
  execute() {
    console.log('본인의 게시물이 아닙니다.')
  }
}

class DefaultErrorCommand extends ErrorCommand {
  execute() {
    console.error('에러 발생:', this.error)
    alert('오류가 발생했습니다.')
  }
}

// Factory
export class ErrorCommandFactory {
  static createCommand(error) {
    if (!error.response || !error.response.data) {
      return new DefaultErrorCommand(error)
    }
    
    const errorCode = error.response.data.code
    
    switch (errorCode) {
      case 'A001': return new BoardNotFoundCommand(error)
      case 'A002': return new BoardNotFoundCommand(error) // FileNotFound
      case 'A003': return new BoardNotFoundCommand(error) // MemberNotFound
      case 'A005': return new NotLoggedInCommand(error)
      case 'A006': return new NotMyBoardCommand(error)
      case 'A007': return new LoginFailCommand(error)
      case 'A008': return new IllegalFileDataCommand(error)
      case 'A009': return new LoginFailCommand(error) // MemberIdExisted
      case 'A013': return new LoginFailCommand(error) // IllegalBoardData
      case 'A014': return new LoginFailCommand(error) // JoinFail
      default: return new DefaultErrorCommand(error)
    }
  }
}
```

---

## 6. API Service

### services/authService.js

```javascript
import api from './api'

export default {
  async login(memberId, password) {
    const response = await api.post('/login', { memberId, password })
    return response.data
  },
  
  async signup(memberId, password, memberName) {
    await api.post('/member', { memberId, password, memberName })
  },
  
  async checkDuplicate(memberId) {
    const response = await api.get('/member/check-duplicate', {
      params: { memberId }
    })
    return response.data.exists
  },
  
  async getCurrentMember() {
    const response = await api.get('/member')
    return response.data
  }
}
```

### services/boardService.js

```javascript
import api from './api'

export default {
  // 목록 조회
  async getList(boardType, params) {
    const response = await api.get(`/boards/${boardType}`, { params })
    return response.data
  },
  
  // 상세 조회
  async getDetail(boardType, id) {
    const response = await api.get(`/board/${boardType}/${id}`)
    return response.data
  },
  
  // 작성
  async create(boardType, formData) {
    const response = await api.post(`/board/${boardType}`, formData, {
      headers: { 'Content-Type': 'multipart/form-data' }
    })
    return response.data
  },
  
  // 수정
  async update(boardType, id, formData) {
    await api.put(`/board/${boardType}/${id}`, formData, {
      headers: { 'Content-Type': 'multipart/form-data' }
    })
  },
  
  // 삭제
  async delete(boardType, id) {
    await api.delete(`/board/${boardType}/${id}`)
  },
  
  // 조회수 증가
  async increaseView(boardType, id) {
    await api.patch(`/board/${boardType}/${id}/increase-view`)
  },
  
  // 작성자 확인
  async checkAuthor(boardType, id) {
    const response = await api.get(`/board/${boardType}/${id}/check-author`)
    return response.data.isAuthor
  }
}
```

---

## 7. 컴포넌트 예시

### views/board/free/FreeList.vue

```vue
<template>
  <v-container>
    <h1 class="mb-4">자유게시판</h1>
    
    <!-- 검색 폼 -->
    <board-search @search="handleSearch" />
    
    <!-- 게시물 목록 -->
    <v-table>
      <thead>
        <tr>
          <th>번호</th>
          <th>제목</th>
          <th>작성자</th>
          <th>작성일</th>
          <th>조회수</th>
        </tr>
      </thead>
      <tbody>
        <tr v-for="board in boards" :key="board.boardId" 
            @click="goToView(board.boardId)" style="cursor:pointer">
          <td>{{ board.boardId }}</td>
          <td>{{ board.title }}</td>
          <td>{{ board.authorName }}</td>
          <td>{{ formatDate(board.createdAt) }}</td>
          <td>{{ board.views }}</td>
        </tr>
      </tbody>
    </v-table>
    
    <!-- 페이지네이션 -->
    <pagination :total-pages="totalPages" :current-page="currentPage" 
                @change="handlePageChange" />
    
    <!-- 글쓰기 버튼 -->
    <v-btn color="primary" @click="goToWrite" v-if="isLoggedIn">글쓰기</v-btn>
  </v-container>
</template>

<script>
import { ref, onMounted, computed } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { useStore } from 'vuex'
import boardService from '@/services/boardService'
import BoardSearch from '@/components/board/BoardSearch.vue'
import Pagination from '@/components/board/Pagination.vue'
import { format } from 'date-fns'

export default {
  name: 'FreeList',
  components: { BoardSearch, Pagination },
  
  setup() {
    const router = useRouter()
    const route = useRoute()
    const store = useStore()
    
    const boards = ref([])
    const totalPages = ref(0)
    const currentPage = ref(0)
    
    const isLoggedIn = computed(() => store.state.auth.isLoggedIn)
    
    const fetchBoards = async () => {
      try {
        const params = {
          pageNum: route.query.page || 0,
          pageSize: 10,
          startDate: route.query.startDate,
          endDate: route.query.endDate,
          category: route.query.category,
          searchText: route.query.searchText
        }
        
        const response = await boardService.getList('free', params)
        boards.value = response.content
        totalPages.value = response.totalPages
        currentPage.value = response.number
      } catch (error) {
        console.error('목록 조회 실패:', error)
      }
    }
    
    const handleSearch = (searchParams) => {
      router.push({ 
        name: 'FreeList', 
        query: { ...searchParams, page: 0 } 
      })
    }
    
    const handlePageChange = (page) => {
      router.push({ 
        name: 'FreeList', 
        query: { ...route.query, page } 
      })
    }
    
    const goToView = (id) => {
      router.push({ name: 'FreeView', params: { id } })
    }
    
    const goToWrite = () => {
      router.push({ name: 'FreeWrite' })
    }
    
    const formatDate = (date) => {
      return format(new Date(date), 'yyyy-MM-dd')
    }
    
    onMounted(() => {
      fetchBoards()
    })
    
    // route.query 변경 감지
    watch(() => route.query, () => {
      fetchBoards()
    })
    
    return {
      boards,
      totalPages,
      currentPage,
      isLoggedIn,
      handleSearch,
      handlePageChange,
      goToView,
      goToWrite,
      formatDate
    }
  }
}
</script>
```

---

## 8. Vuetify 플러그인

### plugins/vuetify.js

```javascript
import 'vuetify/styles'
import { createVuetify } from 'vuetify'
import * as components from 'vuetify/components'
import * as directives from 'vuetify/directives'
import '@mdi/font/css/materialdesignicons.css'

export default createVuetify({
  components,
  directives,
  theme: {
    defaultTheme: 'light',
    themes: {
      light: {
        colors: {
          primary: '#1976D2',
          secondary: '#424242',
          accent: '#82B1FF',
          error: '#FF5252',
          info: '#2196F3',
          success: '#4CAF50',
          warning: '#FFC107'
        }
      }
    }
  }
})
```

---

## 9. main.js

```javascript
import { createApp } from 'vue'
import App from './App.vue'
import router from './router'
import store from './store'
import vuetify from './plugins/vuetify'

const app = createApp(App)

app.use(router)
app.use(store)
app.use(vuetify)

app.mount('#app')
```

---

## 10. 환경 변수

### .env.development
```
VUE_APP_API_BASE_URL=http://localhost:8081/api
```

### .env.production
```
VUE_APP_API_BASE_URL=http://3.35.111.101:8081/api
```

---

## 11. 빌드 및 배포

```bash
# 개발 서버
npm run serve

# 프로덕션 빌드
npm run build

# Nginx 배포 (dist 폴더)
```

### Nginx 설정 예시
```nginx
server {
    listen 80;
    server_name 3.35.111.101;
    
    root /var/www/user-frontend/dist;
    index index.html;
    
    location / {
        try_files $uri $uri/ /index.html;
    }
    
    location /api {
        proxy_pass http://localhost:8081;
    }
}
```
