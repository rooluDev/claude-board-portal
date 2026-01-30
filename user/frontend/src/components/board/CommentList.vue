<template>
  <v-card class="mt-4">
    <v-card-title>댓글 ({{ comments.length }})</v-card-title>
    <v-card-text>
      <!-- 댓글 목록 -->
      <div v-for="comment in comments" :key="comment.commentId" class="mb-3">
        <div class="d-flex justify-space-between">
          <div>
            <strong>{{ comment.authorName }}</strong>
            <span class="text-caption ml-2">
              {{ formatDate(comment.createdAt) }}
            </span>
          </div>
          <v-btn
            v-if="canDelete(comment)"
            size="small"
            color="error"
            text
            @click="handleDelete(comment.commentId)"
          >
            삭제
          </v-btn>
        </div>
        <div class="mt-1">{{ comment.content }}</div>
        <v-divider class="mt-2" />
      </div>

      <!-- 댓글 작성 폼 -->
      <div v-if="isLoggedIn" class="mt-4">
        <v-textarea
          v-model="newComment"
          label="댓글 작성"
          rows="3"
          outlined
        />
        <v-btn color="primary" @click="handleSubmit">댓글 등록</v-btn>
      </div>
      <div v-else class="text-center mt-4">
        <p>댓글을 작성하려면 로그인이 필요합니다.</p>
      </div>
    </v-card-text>
  </v-card>
</template>

<script>
import { ref, computed } from 'vue'
import { useStore } from 'vuex'
import commentService from '@/services/commentService'
import { format } from 'date-fns'

export default {
  name: 'CommentList',
  props: {
    boardType: {
      type: String,
      required: true
    },
    boardId: {
      type: Number,
      required: true
    },
    comments: {
      type: Array,
      default: () => []
    }
  },
  emits: ['refresh'],

  setup(props, { emit }) {
    const store = useStore()
    const newComment = ref('')

    const isLoggedIn = computed(() => store.getters['auth/isLoggedIn'])
    const memberId = computed(() => store.getters['auth/memberId'])

    const canDelete = (comment) => {
      return comment.authorId === memberId.value
    }

    const handleSubmit = async () => {
      if (!newComment.value.trim()) {
        alert('댓글 내용을 입력하세요.')
        return
      }

      try {
        await commentService.create({
          boardType: props.boardType,
          boardId: props.boardId,
          content: newComment.value
        })

        newComment.value = ''
        emit('refresh')
        alert('댓글이 등록되었습니다.')
      } catch (error) {
        console.error('댓글 작성 실패:', error)
      }
    }

    const handleDelete = async (commentId) => {
      if (!confirm('정말 삭제하시겠습니까?')) return

      try {
        await commentService.delete(commentId)
        emit('refresh')
        alert('댓글이 삭제되었습니다.')
      } catch (error) {
        console.error('댓글 삭제 실패:', error)
      }
    }

    const formatDate = (date) => {
      return format(new Date(date), 'yyyy-MM-dd HH:mm')
    }

    return {
      newComment,
      isLoggedIn,
      canDelete,
      handleSubmit,
      handleDelete,
      formatDate
    }
  }
}
</script>
