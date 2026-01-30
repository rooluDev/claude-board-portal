# Claude Code Prompt - User Frontend (Phase 2: 인증 & 에러 처리)

## 📋 Phase 2 목표
1. 로그인 페이지
2. 회원가입 페이지  
3. 에러 처리 (Command 패턴)
4. AuthService 구현
5. 에러 페이지

---

## 1. 에러 처리 (Command 패턴)

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
    console.log('로그인 실패:', this.error.response?.data?.message)
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
      case 'A002': return new BoardNotFoundCommand(error)
      case 'A003': return new BoardNotFoundCommand(error)
      case 'A005': return new NotLoggedInCommand(error)
      case 'A006': return new NotMyBoardCommand(error)
      case 'A007': return new LoginFailCommand(error)
      case 'A008': return new IllegalFileDataCommand(error)
      case 'A009': return new LoginFailCommand(error)
      case 'A013': return new LoginFailCommand(error)
      case 'A014': return new LoginFailCommand(error)
      default: return new DefaultErrorCommand(error)
    }
  }
}
```

### services/api.js 수정 (Response Interceptor)

```javascript
import { ErrorCommandFactory } from '@/utils/errorHandler'

// Response Interceptor 수정
api.interceptors.response.use(
  response => response,
  error => {
    // Command 패턴으로 에러 처리
    const command = ErrorCommandFactory.createCommand(error)
    command.execute()
    
    return Promise.reject(error)
  }
)
```

---

## 2. AuthService

### services/authService.js

```javascript
import api from './api'

export default {
  /**
   * 로그인
   */
  async login(memberId, password) {
    const response = await api.post('/login', {
      memberId,
      password
    })
    return response.data
  },
  
  /**
   * 회원가입
   */
  async signup(memberId, password, memberName) {
    await api.post('/member', {
      memberId,
      password,
      memberName
    })
  },
  
  /**
   * 아이디 중복 검사
   */
  async checkDuplicate(memberId) {
    const response = await api.get('/member/check-duplicate', {
      params: { memberId }
    })
    return response.data.exists
  },
  
  /**
   * 현재 로그인 회원 정보 조회
   */
  async getCurrentMember() {
    const response = await api.get('/member')
    return response.data
  }
}
```

---

## 3. 로그인 페이지

### views/Login.vue

```vue
<template>
  <v-container fluid class="fill-height" style="background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);">
    <v-row align="center" justify="center">
      <v-col cols="12" sm="8" md="5" lg="4">
        <v-card elevation="12" class="pa-4">
          <v-card-title class="text-h4 font-weight-bold text-center mb-4">
            <v-icon color="primary" size="large" class="mr-2">mdi-account-circle</v-icon>
            로그인
          </v-card-title>
          
          <v-card-text>
            <v-form ref="formRef" @submit.prevent="handleLogin">
              <v-text-field
                v-model="form.memberId"
                label="아이디"
                prepend-inner-icon="mdi-account"
                :rules="[rules.required]"
                outlined
                required
                autofocus
              />
              
              <v-text-field
                v-model="form.password"
                label="비밀번호"
                prepend-inner-icon="mdi-lock"
                type="password"
                :rules="[rules.required]"
                outlined
                required
              />
              
              <v-btn
                type="submit"
                color="primary"
                size="large"
                block
                class="mt-2"
                :loading="loading"
              >
                로그인
              </v-btn>
            </v-form>
            
            <v-divider class="my-4" />
            
            <div class="text-center">
              <p class="text-body-2">
                계정이 없으신가요?
                <router-link :to="{ name: 'Join' }" class="text-primary font-weight-bold">
                  회원가입
                </router-link>
              </p>
            </div>
          </v-card-text>
        </v-card>
      </v-col>
    </v-row>
  </v-container>
</template>

<script>
import { ref } from 'vue'
import { useRouter } from 'vue-router'
import { useStore } from 'vuex'
import authService from '@/services/authService'

export default {
  name: 'Login',
  setup() {
    const router = useRouter()
    const store = useStore()
    const formRef = ref(null)
    const loading = ref(false)
    
    const form = ref({
      memberId: '',
      password: ''
    })
    
    const rules = {
      required: v => !!v || '필수 입력 항목입니다.'
    }
    
    const handleLogin = async () => {
      const { valid } = await formRef.value.validate()
      if (!valid) return
      
      loading.value = true
      
      try {
        const response = await authService.login(
          form.value.memberId,
          form.value.password
        )
        
        // Vuex에 인증 정보 저장
        store.dispatch('auth/login', {
          accessToken: response.accessToken,
          memberId: response.memberId,
          memberName: response.memberName
        })
        
        alert(`${response.memberName}님 환영합니다!`)
        router.push({ name: 'Main' })
        
      } catch (error) {
        console.error('로그인 실패:', error)
      } finally {
        loading.value = false
      }
    }
    
    return {
      formRef,
      form,
      rules,
      loading,
      handleLogin
    }
  }
}
</script>
```

---

## 4. 회원가입 페이지

### views/Join.vue

```vue
<template>
  <v-container fluid class="fill-height" style="background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);">
    <v-row align="center" justify="center">
      <v-col cols="12" sm="10" md="6" lg="5">
        <v-card elevation="12" class="pa-4">
          <v-card-title class="text-h4 font-weight-bold text-center mb-4">
            <v-icon color="primary" size="large" class="mr-2">mdi-account-plus</v-icon>
            회원가입
          </v-card-title>
          
          <v-card-text>
            <v-form ref="formRef" @submit.prevent="handleSignup">
              <!-- 아이디 -->
              <div class="mb-3">
                <v-row>
                  <v-col cols="8">
                    <v-text-field
                      v-model="form.memberId"
                      label="아이디 (4-20자)"
                      prepend-inner-icon="mdi-account"
                      :rules="[rules.required, rules.memberId]"
                      :readonly="isDuplicateChecked"
                      outlined
                      required
                      counter="20"
                    />
                  </v-col>
                  <v-col cols="4">
                    <v-btn
                      color="secondary"
                      size="large"
                      block
                      :disabled="!form.memberId || isDuplicateChecked"
                      @click="handleCheckDuplicate"
                    >
                      중복 검사
                    </v-btn>
                  </v-col>
                </v-row>
                <v-alert v-if="duplicateCheckMessage" :type="duplicateCheckType" dense>
                  {{ duplicateCheckMessage }}
                </v-alert>
              </div>
              
              <!-- 비밀번호 -->
              <v-text-field
                v-model="form.password"
                label="비밀번호 (4-20자)"
                prepend-inner-icon="mdi-lock"
                type="password"
                :rules="[rules.required, rules.password]"
                outlined
                required
                counter="20"
              />
              
              <!-- 비밀번호 확인 -->
              <v-text-field
                v-model="form.passwordConfirm"
                label="비밀번호 확인"
                prepend-inner-icon="mdi-lock-check"
                type="password"
                :rules="[rules.required, rules.passwordConfirm]"
                outlined
                required
              />
              
              <!-- 이름 -->
              <v-text-field
                v-model="form.memberName"
                label="이름 (2-5자, 한글/영문)"
                prepend-inner-icon="mdi-account-circle"
                :rules="[rules.required, rules.memberName]"
                outlined
                required
                counter="5"
              />
              
              <v-btn
                type="submit"
                color="primary"
                size="large"
                block
                class="mt-4"
                :loading="loading"
              >
                회원가입
              </v-btn>
            </v-form>
            
            <v-divider class="my-4" />
            
            <div class="text-center">
              <p class="text-body-2">
                이미 계정이 있으신가요?
                <router-link :to="{ name: 'Login' }" class="text-primary font-weight-bold">
                  로그인
                </router-link>
              </p>
            </div>
          </v-card-text>
        </v-card>
      </v-col>
    </v-row>
  </v-container>
</template>

<script>
import { ref, watch } from 'vue'
import { useRouter } from 'vue-router'
import authService from '@/services/authService'

export default {
  name: 'Join',
  setup() {
    const router = useRouter()
    const formRef = ref(null)
    const loading = ref(false)
    
    const form = ref({
      memberId: '',
      password: '',
      passwordConfirm: '',
      memberName: ''
    })
    
    const isDuplicateChecked = ref(false)
    const duplicateCheckMessage = ref('')
    const duplicateCheckType = ref('success')
    
    const rules = {
      required: v => !!v || '필수 입력 항목입니다.',
      
      memberId: v => {
        if (!v) return '아이디를 입력하세요.'
        if (v.length < 4 || v.length > 20) return '아이디는 4-20자여야 합니다.'
        return true
      },
      
      password: v => {
        if (!v) return '비밀번호를 입력하세요.'
        if (v.length < 4 || v.length > 20) return '비밀번호는 4-20자여야 합니다.'
        
        // 동일 문자 3개 연속 검사
        for (let i = 0; i < v.length - 2; i++) {
          if (v[i] === v[i+1] && v[i] === v[i+2]) {
            return '동일 문자 3개 연속 사용 불가'
          }
        }
        
        // 아이디와 동일 검사
        if (v === form.value.memberId) {
          return '비밀번호는 아이디와 같을 수 없습니다.'
        }
        
        return true
      },
      
      passwordConfirm: v => {
        if (!v) return '비밀번호 확인을 입력하세요.'
        if (v !== form.value.password) return '비밀번호가 일치하지 않습니다.'
        return true
      },
      
      memberName: v => {
        if (!v) return '이름을 입력하세요.'
        if (v.length < 2 || v.length > 5) return '이름은 2-5자여야 합니다.'
        if (!/^[가-힣a-zA-Z]+$/.test(v)) return '이름은 한글 또는 영문만 가능합니다.'
        return true
      }
    }
    
    // 아이디 변경 시 중복 검사 초기화
    watch(() => form.value.memberId, () => {
      isDuplicateChecked.value = false
      duplicateCheckMessage.value = ''
    })
    
    const handleCheckDuplicate = async () => {
      if (!form.value.memberId) {
        alert('아이디를 입력하세요.')
        return
      }
      
      try {
        const exists = await authService.checkDuplicate(form.value.memberId)
        
        if (exists) {
          duplicateCheckType.value = 'error'
          duplicateCheckMessage.value = '이미 사용 중인 아이디입니다.'
          isDuplicateChecked.value = false
        } else {
          duplicateCheckType.value = 'success'
          duplicateCheckMessage.value = '사용 가능한 아이디입니다.'
          isDuplicateChecked.value = true
        }
      } catch (error) {
        console.error('중복 검사 실패:', error)
      }
    }
    
    const handleSignup = async () => {
      if (!isDuplicateChecked.value) {
        alert('아이디 중복 검사를 해주세요.')
        return
      }
      
      const { valid } = await formRef.value.validate()
      if (!valid) return
      
      loading.value = true
      
      try {
        await authService.signup(
          form.value.memberId,
          form.value.password,
          form.value.memberName
        )
        
        alert('회원가입이 완료되었습니다. 로그인해주세요.')
        router.push({ name: 'Login' })
        
      } catch (error) {
        console.error('회원가입 실패:', error)
      } finally {
        loading.value = false
      }
    }
    
    return {
      formRef,
      form,
      rules,
      loading,
      isDuplicateChecked,
      duplicateCheckMessage,
      duplicateCheckType,
      handleCheckDuplicate,
      handleSignup
    }
  }
}
</script>
```

---

## 5. 에러 페이지

### views/Error.vue

```vue
<template>
  <v-container fluid class="fill-height">
    <v-row align="center" justify="center">
      <v-col cols="12" sm="8" md="6" lg="4">
        <v-card elevation="6" class="text-center pa-6">
          <v-icon color="error" size="100">mdi-alert-circle</v-icon>
          
          <v-card-title class="text-h4 justify-center mt-4">
            오류 발생
          </v-card-title>
          
          <v-card-text class="text-h6 text-grey">
            {{ errorMessage || '알 수 없는 오류가 발생했습니다.' }}
          </v-card-text>
          
          <v-card-actions class="justify-center mt-4">
            <v-btn color="primary" size="large" @click="goHome">
              <v-icon left>mdi-home</v-icon>
              메인으로 이동
            </v-btn>
          </v-card-actions>
        </v-card>
      </v-col>
    </v-row>
  </v-container>
</template>

<script>
import { ref } from 'vue'
import { useRouter, useRoute } from 'vue-router'

export default {
  name: 'Error',
  setup() {
    const router = useRouter()
    const route = useRoute()
    
    const errorMessage = ref(route.query.message || '')
    
    const goHome = () => {
      router.push({ name: 'Main' })
    }
    
    return {
      errorMessage,
      goHome
    }
  }
}
</script>
```

---

## 6. Router에 라우트 추가

**router/index.js 업데이트는 이미 Phase 1에서 완료**

---

## Phase 2 완료 체크리스트

- [ ] ErrorCommandFactory 작성
- [ ] api.js Response Interceptor 업데이트
- [ ] AuthService 작성
- [ ] Login.vue 작성
- [ ] Join.vue 작성
- [ ] Error.vue 작성
- [ ] 로그인 테스트
- [ ] 회원가입 테스트 (중복 검사, 유효성 검증)
- [ ] 에러 처리 테스트

---

## 테스트

```bash
# 개발 서버 실행
npm run serve

# 테스트 시나리오:
# 1. 회원가입 (아이디 중복 검사, 비밀번호 검증)
# 2. 로그인
# 3. Navbar에서 로그인 상태 확인
# 4. 로그아웃
```

다음 Phase 3에서는 게시판 목록/상세 페이지를 구현합니다!
