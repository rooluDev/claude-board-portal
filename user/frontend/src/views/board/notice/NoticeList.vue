<template>
  <v-container>
    <h1 class="mb-4">공지사항</h1>

    <v-card class="mb-4">
      <v-card-text>
        <!-- 검색 폼 간소화 -->
        <v-row>
          <v-col cols="12" md="10">
            <v-text-field
              v-model="searchText"
              label="검색어"
              prepend-inner-icon="mdi-magnify"
              outlined
              dense
            />
          </v-col>
          <v-col cols="12" md="2">
            <v-btn color="primary" @click="handleSearch" block>검색</v-btn>
          </v-col>
        </v-row>
      </v-card-text>
    </v-card>

    <v-card>
      <v-table>
        <thead>
          <tr>
            <th width="10%">번호</th>
            <th width="50%">제목</th>
            <th width="15%">작성자</th>
            <th width="15%">작성일</th>
            <th width="10%">조회수</th>
          </tr>
        </thead>
        <tbody>
          <tr v-if="boards.length === 0">
            <td colspan="5" class="text-center text-grey">등록된 게시물이 없습니다.</td>
          </tr>
          <tr v-for="board in boards" :key="board.boardId"
              @click="goToView(board.boardId)" style="cursor:pointer">
            <td>
              <v-chip v-if="board.isFixed" color="error" size="small">고정</v-chip>
              <span v-else>{{ board.boardId }}</span>
            </td>
            <td>{{ board.title }}</td>
            <td>{{ board.authorName }}</td>
            <td>{{ formatDate(board.createdAt) }}</td>
            <td>{{ board.views }}</td>
          </tr>
        </tbody>
      </v-table>

      <v-card-text>
        <pagination
          v-if="totalPages > 0"
          :total-pages="totalPages"
          :current-page="currentPage"
          @change="handlePageChange"
        />
      </v-card-text>
    </v-card>
  </v-container>
</template>

<script>
import { ref, onMounted, watch } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import boardService from '@/services/boardService'
import Pagination from '@/components/board/Pagination.vue'
import { format } from 'date-fns'

export default {
  name: 'NoticeList',
  components: { Pagination },

  setup() {
    const router = useRouter()
    const route = useRoute()

    const boards = ref([])
    const totalPages = ref(0)
    const currentPage = ref(0)
    const searchText = ref('')

    const fetchBoards = async () => {
      try {
        const params = {
          pageNum: route.query.page || 0,
          pageSize: 10,
          searchText: route.query.searchText
        }

        const response = await boardService.getList('notice', params)
        boards.value = response.content
        totalPages.value = response.totalPages
        currentPage.value = response.number
      } catch (error) {
        console.error('목록 조회 실패:', error)
      }
    }

    const handleSearch = () => {
      router.push({
        name: 'NoticeList',
        query: { searchText: searchText.value, page: 0 }
      })
    }

    const handlePageChange = (page) => {
      router.push({
        name: 'NoticeList',
        query: { ...route.query, page }
      })
    }

    const goToView = (id) => {
      router.push({ name: 'NoticeView', params: { id } })
    }

    const formatDate = (date) => {
      return format(new Date(date), 'yyyy-MM-dd')
    }

    onMounted(fetchBoards)
    watch(() => route.query, fetchBoards)

    return {
      boards,
      totalPages,
      currentPage,
      searchText,
      handleSearch,
      handlePageChange,
      goToView,
      formatDate
    }
  }
}
</script>
