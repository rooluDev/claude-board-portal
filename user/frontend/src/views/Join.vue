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
