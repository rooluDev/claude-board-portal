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
