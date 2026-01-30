<template>
  <v-container>
    <v-card v-if="board">
      <v-card-title class="bg-grey-lighten-3">
        <v-icon v-if="board.isSecret" class="mr-2">mdi-lock</v-icon>
        <span class="text-h5">{{ board.title }}</span>
      </v-card-title>

      <v-card-subtitle class="py-3">
        <v-row>
          <v-col>작성자: {{ board.authorName }}</v-col>
          <v-col>작성일: {{ formatDate(board.createdAt) }}</v-col>
          <v-col>
            상태:
            <v-chip :color="board.isAnswered ? 'success' : 'warning'" size="small" class="ml-2">
              {{ board.isAnswered ? '답변완료' : '대기중' }}
            </v-chip>
          </v-col>
        </v-row>
      </v-card-subtitle>

      <v-divider />

      <v-card-text style="min-height: 300px; white-space: pre-wrap;">
        {{ board.content }}
      </v-card-text>

      <!-- 첨부파일 목록 -->
      <file-list :files="board.files" />

      <!-- 답변 (있을 경우) -->
      <div v-if="board.answer" class="ma-4">
        <v-divider class="mb-4" />
        <v-card color="blue-lighten-5">
          <v-card-title class="text-h6">
            <v-icon left>mdi-comment-check</v-icon>
            답변
          </v-card-title>
          <v-card-text style="white-space: pre-wrap;">
            {{ board.answer }}
          </v-card-text>
          <v-card-subtitle>
            답변일: {{ formatDate(board.answeredAt) }}
          </v-card-subtitle>
        </v-card>
      </div>

      <v-card-actions>
        <v-btn @click="goToList">목록</v-btn>
        <v-spacer />
        <v-btn v-if="isAuthor && !board.isAnswered" color="warning" @click="goToEdit">수정</v-btn>
        <v-btn v-if="isAuthor && !board.isAnswered" color="error" @click="handleDelete">삭제</v-btn>
      </v-card-actions>
    </v-card>
  </v-container>
</template>

<script>
import { ref, onMounted, computed } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { useStore } from 'vuex'
import boardService from '@/services/boardService'
import FileList from '@/components/board/FileList.vue'
import { format } from 'date-fns'

export default {
  name: 'InquiryView',
  components: { FileList },

  setup() {
    const router = useRouter()
    const route = useRoute()
    const store = useStore()
    const board = ref(null)
    const isAuthor = ref(false)

    const isLoggedIn = computed(() => store.getters['auth/isLoggedIn'])

    const fetchBoard = async () => {
      try {
        await boardService.increaseView('inquiry', route.params.id)
        board.value = await boardService.getDetail('inquiry', route.params.id)

        if (isLoggedIn.value) {
          isAuthor.value = await boardService.checkAuthor('inquiry', route.params.id)
        }
      } catch (error) {
        console.error('상세 조회 실패:', error)
      }
    }

    const goToList = () => {
      router.push({ name: 'InquiryList' })
    }

    const goToEdit = () => {
      router.push({ name: 'InquiryModify', params: { id: route.params.id } })
    }

    const handleDelete = async () => {
      if (!confirm('정말 삭제하시겠습니까?')) return

      try {
        await boardService.delete('inquiry', route.params.id)
        alert('삭제되었습니다.')
        router.push({ name: 'InquiryList' })
      } catch (error) {
        console.error('삭제 실패:', error)
      }
    }

    const formatDate = (date) => {
      return format(new Date(date), 'yyyy-MM-dd HH:mm')
    }

    onMounted(fetchBoard)

    return {
      board,
      isAuthor,
      goToList,
      goToEdit,
      handleDelete,
      formatDate
    }
  }
}
</script>
