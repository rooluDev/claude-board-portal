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
