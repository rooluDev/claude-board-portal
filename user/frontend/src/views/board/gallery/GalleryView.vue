<template>
  <v-container>
    <v-card v-if="board">
      <v-card-title class="bg-grey-lighten-3">
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

      <!-- 이미지 파일 목록 -->
      <file-list :files="board.files" />

      <v-card-actions>
        <v-btn @click="goToList">목록</v-btn>
        <v-spacer />
        <v-btn v-if="isAuthor" color="warning" @click="goToEdit">수정</v-btn>
        <v-btn v-if="isAuthor" color="error" @click="handleDelete">삭제</v-btn>
      </v-card-actions>
    </v-card>

    <!-- 댓글 목록 -->
    <comment-list
      v-if="board"
      :board-type="'gallery'"
      :board-id="Number(route.params.id)"
      :comments="board.comments || []"
      @refresh="fetchBoard"
    />
  </v-container>
</template>

<script>
import { ref, onMounted, computed } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { useStore } from 'vuex'
import boardService from '@/services/boardService'
import FileList from '@/components/board/FileList.vue'
import CommentList from '@/components/board/CommentList.vue'
import { format } from 'date-fns'

export default {
  name: 'GalleryView',
  components: { FileList, CommentList },

  setup() {
    const router = useRouter()
    const route = useRoute()
    const store = useStore()
    const board = ref(null)
    const isAuthor = ref(false)

    const isLoggedIn = computed(() => store.getters['auth/isLoggedIn'])

    const fetchBoard = async () => {
      try {
        await boardService.increaseView('gallery', route.params.id)
        board.value = await boardService.getDetail('gallery', route.params.id)

        if (isLoggedIn.value) {
          isAuthor.value = await boardService.checkAuthor('gallery', route.params.id)
        }
      } catch (error) {
        console.error('상세 조회 실패:', error)
      }
    }

    const goToList = () => {
      router.push({ name: 'GalleryList' })
    }

    const goToEdit = () => {
      router.push({ name: 'GalleryModify', params: { id: route.params.id } })
    }

    const handleDelete = async () => {
      if (!confirm('정말 삭제하시겠습니까?')) return

      try {
        await boardService.delete('gallery', route.params.id)
        alert('삭제되었습니다.')
        router.push({ name: 'GalleryList' })
      } catch (error) {
        console.error('삭제 실패:', error)
      }
    }

    const formatDate = (date) => {
      return format(new Date(date), 'yyyy-MM-dd HH:mm')
    }

    onMounted(fetchBoard)

    return {
      route,
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
