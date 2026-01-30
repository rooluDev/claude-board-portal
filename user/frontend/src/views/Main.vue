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
