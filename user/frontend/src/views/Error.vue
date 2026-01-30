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
