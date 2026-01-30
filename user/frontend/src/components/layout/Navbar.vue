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
