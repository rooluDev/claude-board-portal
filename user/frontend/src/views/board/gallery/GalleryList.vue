<template>
  <v-container>
    <h1 class="mb-4">갤러리</h1>

    <v-card class="mb-4">
      <v-card-text>
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

    <!-- 갤러리 그리드 레이아웃 -->
    <v-row v-if="boards.length > 0">
      <v-col v-for="board in boards" :key="board.boardId" cols="12" sm="6" md="4" lg="3">
        <v-card hover @click="goToView(board.boardId)" style="cursor: pointer">
          <v-img
            :src="board.thumbnailUrl || '/placeholder-image.png'"
            height="200"
            cover
          >
            <template v-slot:placeholder>
              <v-row class="fill-height ma-0" align="center" justify="center">
                <v-icon size="64" color="grey-lighten-2">mdi-image</v-icon>
              </v-row>
            </template>
          </v-img>
          <v-card-title class="text-subtitle-1">{{ board.title }}</v-card-title>
          <v-card-subtitle>
            <div>{{ board.authorName }}</div>
            <div class="text-caption">
              {{ formatDate(board.createdAt) }} · 조회 {{ board.views }}
            </div>
          </v-card-subtitle>
        </v-card>
      </v-col>
    </v-row>

    <v-card v-else class="pa-10 text-center">
      <p class="text-grey">등록된 게시물이 없습니다.</p>
    </v-card>

    <v-card-text>
      <pagination
        v-if="totalPages > 0"
        :total-pages="totalPages"
        :current-page="currentPage"
        @change="handlePageChange"
      />
    </v-card-text>

    <v-card-actions v-if="isLoggedIn" class="mt-4">
      <v-spacer />
      <v-btn color="primary" @click="goToWrite">
        <v-icon left>mdi-pencil</v-icon>
        글쓰기
      </v-btn>
    </v-card-actions>
  </v-container>
</template>

<script>
import { ref, onMounted, watch, computed } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { useStore } from 'vuex'
import boardService from '@/services/boardService'
import Pagination from '@/components/board/Pagination.vue'
import { format } from 'date-fns'

export default {
  name: 'GalleryList',
  components: { Pagination },

  setup() {
    const router = useRouter()
    const route = useRoute()
    const store = useStore()

    const boards = ref([])
    const totalPages = ref(0)
    const currentPage = ref(0)
    const searchText = ref('')

    const isLoggedIn = computed(() => store.getters['auth/isLoggedIn'])

    const fetchBoards = async () => {
      try {
        const params = {
          pageNum: route.query.page || 0,
          pageSize: 12,
          searchText: route.query.searchText
        }

        const response = await boardService.getList('gallery', params)
        boards.value = response.content
        totalPages.value = response.totalPages
        currentPage.value = response.number
      } catch (error) {
        console.error('목록 조회 실패:', error)
      }
    }

    const handleSearch = () => {
      router.push({
        name: 'GalleryList',
        query: { searchText: searchText.value, page: 0 }
      })
    }

    const handlePageChange = (page) => {
      router.push({
        name: 'GalleryList',
        query: { ...route.query, page }
      })
    }

    const goToView = (id) => {
      router.push({ name: 'GalleryView', params: { id } })
    }

    const goToWrite = () => {
      router.push({ name: 'GalleryWrite' })
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
      isLoggedIn,
      handleSearch,
      handlePageChange,
      goToView,
      goToWrite,
      formatDate
    }
  }
}
</script>
