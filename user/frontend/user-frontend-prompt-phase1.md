# Claude Code Prompt - User Frontend (Phase 1: 프로젝트 설정 & 레이아웃)

## 프로젝트 정보
- **프로젝트명**: board-portal/user/frontend
- **프레임워크**: Vue.js 3.2.13
- **UI 라이브러리**: Vuetify 3.0.0
- **빌드 도구**: Vue CLI 5
- **포트**: 80

---

## 📋 Phase 1 목표
1. Vue.js 프로젝트 초기 설정
2. Vuetify 3 설치 및 설정
3. Vue Router 설정
4. Vuex Store 설정 (auth 모듈)
5. Axios 설정
6. 레이아웃 컴포넌트 (Navbar, Footer)
7. 메인 페이지 구현

---

## 1. 프로젝트 생성

```bash
# Vue CLI 설치
npm install -g @vue/cli

# 프로젝트 생성
vue create user-frontend

# 옵션 선택:
# - Vue 3
# - Router (history mode)
# - Vuex
# - Babel
# - Linter (ESLint + Prettier)
```

---

## 2. 의존성 설치

### 2.1 package.json

```json
{
  "name": "user-frontend",
  "version": "1.0.0",
  "private": true,
  "scripts": {
    "serve": "vue-cli-service serve --port 80",
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
    "date-fns": "^3.6.0"
  },
  "devDependencies": {
    "@vue/cli-service": "~5.0.0",
    "sass": "^1.32.0",
    "sass-loader": "^12.0.0"
  }
}
```

### 2.2 설치 명령

```bash
npm install

# Vuetify 3 설치
npm install vuetify@next
npm install @mdi/font

# 기타 라이브러리
npm install axios
npm install vuex-persistedstate
npm install date-fns
```

---

## 3. 프로젝트 구조

```
src/
├── main.js
├── App.vue
├── assets/
├── components/
│   ├── layout/
│   │   ├── Navbar.vue
│   │   └── Footer.vue
│   └── common/
│       └── LoadingSpinner.vue
├── views/
│   ├── Main.vue
│   ├── Login.vue
│   ├── Join.vue
│   ├── Error.vue
│   └── board/
│       ├── notice/
│       ├── free/
│       ├── gallery/
│       └── inquiry/
├── router/
│   └── index.js
├── store/
│   ├── index.js
│   └── modules/
│       └── auth.js
├── services/
│   ├── api.js
│   ├── authService.js
│   └── boardService.js
└── utils/
    └── errorHandler.js
```

---

## 4. vue.config.js

**프로젝트 루트에 생성**

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

## 5. Vuetify 설정

### 5.1 plugins/vuetify.js

**src/plugins/vuetify.js 생성**

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

## 6. Vuex Store 설정

### 6.1 store/index.js

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

### 6.2 store/modules/auth.js

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
    memberId: state => state.memberId,
    memberName: state => state.memberName,
    accessToken: state => state.accessToken
  }
}
```

---

## 7. Router 설정

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

## 8. Axios 설정

### services/api.js

```javascript
import axios from 'axios'
import store from '@/store'

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

// Response Interceptor (에러 처리는 Phase 2에서)
api.interceptors.response.use(
  response => response,
  error => {
    console.error('API Error:', error)
    return Promise.reject(error)
  }
)

export default api
```

---

## 9. 레이아웃 컴포넌트

### 9.1 App.vue

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
  components: {
    Navbar,
    FooterComponent
  }
}
</script>

<style>
html {
  overflow-y: auto !important;
}
</style>
```

### 9.2 components/layout/Navbar.vue

```vue
<template>
  <v-app-bar app color="primary" dark elevation="2">
    <v-toolbar-title @click="goHome" style="cursor: pointer" class="font-weight-bold">
      <v-icon class="mr-2">mdi-bulletin-board</v-icon>
      eBrain Portal
    </v-toolbar-title>
    
    <v-spacer></v-spacer>
    
    <v-btn text @click="goToBoard('notice')">
      <v-icon left>mdi-bullhorn</v-icon>
      공지사항
    </v-btn>
    <v-btn text @click="goToBoard('free')">
      <v-icon left>mdi-forum</v-icon>
      자유게시판
    </v-btn>
    <v-btn text @click="goToBoard('gallery')">
      <v-icon left>mdi-image-multiple</v-icon>
      갤러리
    </v-btn>
    <v-btn text @click="goToBoard('inquiry')">
      <v-icon left>mdi-help-circle</v-icon>
      문의
    </v-btn>
    
    <v-spacer></v-spacer>
    
    <template v-if="isLoggedIn">
      <v-chip color="white" text-color="primary" class="mr-3">
        <v-icon left>mdi-account-circle</v-icon>
        {{ memberName }}님
      </v-chip>
      <v-btn text @click="handleLogout">
        <v-icon left>mdi-logout</v-icon>
        로그아웃
      </v-btn>
    </template>
    <template v-else>
      <v-btn text @click="goToLogin">
        <v-icon left>mdi-login</v-icon>
        로그인
      </v-btn>
      <v-btn outlined @click="goToJoin">
        <v-icon left>mdi-account-plus</v-icon>
        회원가입
      </v-btn>
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
    
    const isLoggedIn = computed(() => store.getters['auth/isLoggedIn'])
    const memberName = computed(() => store.getters['auth/memberName'])
    
    const goHome = () => {
      router.push({ name: 'Main' })
    }
    
    const goToBoard = (type) => {
      router.push({ name: `${type.charAt(0).toUpperCase() + type.slice(1)}List` })
    }
    
    const goToLogin = () => {
      router.push({ name: 'Login' })
    }
    
    const goToJoin = () => {
      router.push({ name: 'Join' })
    }
    
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

### 9.3 components/layout/Footer.vue

```vue
<template>
  <v-footer app color="grey-lighten-3" class="pa-3">
    <v-container>
      <v-row>
        <v-col cols="12" class="text-center">
          <p class="text-body-2 mb-0">
            © 2024 eBrain Portal. All rights reserved.
          </p>
        </v-col>
      </v-row>
    </v-container>
  </v-footer>
</template>

<script>
export default {
  name: 'FooterComponent'
}
</script>
```

---

## 10. 메인 페이지

### views/Main.vue

```vue
<template>
  <v-container fluid class="pa-4">
    <v-row>
      <v-col cols="12">
        <h1 class="text-h3 mb-4 text-center">eBrain Portal</h1>
        <p class="text-h6 text-center text-grey">
          공지사항, 자유게시판, 갤러리, 문의게시판을 이용하실 수 있습니다.
        </p>
      </v-col>
    </v-row>
    
    <v-row class="mt-6">
      <!-- 공지사항 -->
      <v-col cols="12" md="6" lg="3">
        <v-card elevation="3" hover>
          <v-card-title class="bg-primary white--text">
            <v-icon left color="white">mdi-bullhorn</v-icon>
            공지사항
          </v-card-title>
          <v-card-text style="min-height: 300px">
            <p class="text-center text-grey mt-5">게시물 미리보기는 Phase 2에서 구현</p>
          </v-card-text>
          <v-card-actions>
            <v-spacer></v-spacer>
            <v-btn color="primary" text @click="goToBoard('notice')">
              더보기
              <v-icon right>mdi-arrow-right</v-icon>
            </v-btn>
          </v-card-actions>
        </v-card>
      </v-col>
      
      <!-- 자유게시판 -->
      <v-col cols="12" md="6" lg="3">
        <v-card elevation="3" hover>
          <v-card-title class="bg-secondary white--text">
            <v-icon left color="white">mdi-forum</v-icon>
            자유게시판
          </v-card-title>
          <v-card-text style="min-height: 300px">
            <p class="text-center text-grey mt-5">게시물 미리보기는 Phase 2에서 구현</p>
          </v-card-text>
          <v-card-actions>
            <v-spacer></v-spacer>
            <v-btn color="secondary" text @click="goToBoard('free')">
              더보기
              <v-icon right>mdi-arrow-right</v-icon>
            </v-btn>
          </v-card-actions>
        </v-card>
      </v-col>
      
      <!-- 갤러리 -->
      <v-col cols="12" md="6" lg="3">
        <v-card elevation="3" hover>
          <v-card-title class="bg-success white--text">
            <v-icon left color="white">mdi-image-multiple</v-icon>
            갤러리
          </v-card-title>
          <v-card-text style="min-height: 300px">
            <p class="text-center text-grey mt-5">게시물 미리보기는 Phase 2에서 구현</p>
          </v-card-text>
          <v-card-actions>
            <v-spacer></v-spacer>
            <v-btn color="success" text @click="goToBoard('gallery')">
              더보기
              <v-icon right>mdi-arrow-right</v-icon>
            </v-btn>
          </v-card-actions>
        </v-card>
      </v-col>
      
      <!-- 문의게시판 -->
      <v-col cols="12" md="6" lg="3">
        <v-card elevation="3" hover>
          <v-card-title class="bg-warning white--text">
            <v-icon left color="white">mdi-help-circle</v-icon>
            문의게시판
          </v-card-title>
          <v-card-text style="min-height: 300px">
            <p class="text-center text-grey mt-5">게시물 미리보기는 Phase 2에서 구현</p>
          </v-card-text>
          <v-card-actions>
            <v-spacer></v-spacer>
            <v-btn color="warning" text @click="goToBoard('inquiry')">
              더보기
              <v-icon right>mdi-arrow-right</v-icon>
            </v-btn>
          </v-card-actions>
        </v-card>
      </v-col>
    </v-row>
  </v-container>
</template>

<script>
import { useRouter } from 'vue-router'

export default {
  name: 'Main',
  setup() {
    const router = useRouter()
    
    const goToBoard = (type) => {
      router.push({ name: `${type.charAt(0).toUpperCase() + type.slice(1)}List` })
    }
    
    return {
      goToBoard
    }
  }
}
</script>
```

---

## 11. main.js

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

## 12. 환경 변수

### .env.development

```
VUE_APP_API_BASE_URL=http://localhost:8081/api
```

### .env.production

```
VUE_APP_API_BASE_URL=http://3.35.111.101:8081/api
```

---

## 13. 테스트

```bash
# 개발 서버 실행
npm run serve

# 브라우저에서 확인
# http://localhost:80
```

---

## Phase 1 완료 체크리스트

- [ ] Vue.js 프로젝트 생성
- [ ] package.json 의존성 설치
- [ ] vue.config.js 설정
- [ ] Vuetify 플러그인 설정
- [ ] Vuex Store (auth 모듈) 작성
- [ ] Vue Router 설정
- [ ] Axios 기본 설정
- [ ] App.vue 작성
- [ ] Navbar.vue 작성
- [ ] Footer.vue 작성
- [ ] Main.vue 작성
- [ ] main.js 설정
- [ ] 환경 변수 파일 작성
- [ ] 개발 서버 실행 및 확인

---

## 다음 단계 (Phase 2)

Phase 2에서는:
- 로그인/회원가입 페이지
- 에러 처리 (Command 패턴)
- AuthService 구현

이 프롬프트를 Claude Code에 붙여넣고 실행하세요!
