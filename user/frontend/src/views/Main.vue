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
            <v-progress-circular
              v-if="loading.notice"
              indeterminate
              color="primary"
              class="d-flex mx-auto mt-5"
            ></v-progress-circular>
            <v-list v-else-if="boards.notice.length > 0" dense>
              <v-list-item
                v-for="board in boards.notice"
                :key="board.id"
                @click="goToDetail('notice', board.id)"
                class="px-0"
              >
                <v-list-item-content>
                  <v-list-item-title class="text-truncate">
                    {{ board.title }}
                  </v-list-item-title>
                  <v-list-item-subtitle class="text-caption">
                    {{ formatDate(board.createdAt) }}
                  </v-list-item-subtitle>
                </v-list-item-content>
              </v-list-item>
            </v-list>
            <p v-else class="text-center text-grey mt-5">게시물이 없습니다</p>
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
            <v-progress-circular
              v-if="loading.free"
              indeterminate
              color="secondary"
              class="d-flex mx-auto mt-5"
            ></v-progress-circular>
            <v-list v-else-if="boards.free.length > 0" dense>
              <v-list-item
                v-for="board in boards.free"
                :key="board.id"
                @click="goToDetail('free', board.id)"
                class="px-0"
              >
                <v-list-item-content>
                  <v-list-item-title class="text-truncate">
                    {{ board.title }}
                  </v-list-item-title>
                  <v-list-item-subtitle class="text-caption">
                    {{ formatDate(board.createdAt) }} | {{ board.writerName }}
                  </v-list-item-subtitle>
                </v-list-item-content>
              </v-list-item>
            </v-list>
            <p v-else class="text-center text-grey mt-5">게시물이 없습니다</p>
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
            <v-progress-circular
              v-if="loading.gallery"
              indeterminate
              color="success"
              class="d-flex mx-auto mt-5"
            ></v-progress-circular>
            <v-list v-else-if="boards.gallery.length > 0" dense>
              <v-list-item
                v-for="board in boards.gallery"
                :key="board.id"
                @click="goToDetail('gallery', board.id)"
                class="px-0"
              >
                <v-list-item-content>
                  <v-list-item-title class="text-truncate">
                    {{ board.title }}
                  </v-list-item-title>
                  <v-list-item-subtitle class="text-caption">
                    {{ formatDate(board.createdAt) }} | {{ board.writerName }}
                  </v-list-item-subtitle>
                </v-list-item-content>
              </v-list-item>
            </v-list>
            <p v-else class="text-center text-grey mt-5">게시물이 없습니다</p>
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
            <v-progress-circular
              v-if="loading.inquiry"
              indeterminate
              color="warning"
              class="d-flex mx-auto mt-5"
            ></v-progress-circular>
            <v-list v-else-if="boards.inquiry.length > 0" dense>
              <v-list-item
                v-for="board in boards.inquiry"
                :key="board.id"
                @click="goToDetail('inquiry', board.id)"
                class="px-0"
              >
                <v-list-item-content>
                  <v-list-item-title class="text-truncate">
                    <v-icon v-if="board.isSecret" small color="warning" class="mr-1">
                      mdi-lock
                    </v-icon>
                    {{ board.title }}
                    <v-chip v-if="board.isAnswered" x-small color="success" class="ml-2">
                      답변완료
                    </v-chip>
                  </v-list-item-title>
                  <v-list-item-subtitle class="text-caption">
                    {{ formatDate(board.createdAt) }}
                  </v-list-item-subtitle>
                </v-list-item-content>
              </v-list-item>
            </v-list>
            <p v-else class="text-center text-grey mt-5">게시물이 없습니다</p>
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
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import boardService from '@/services/boardService'
import { format } from 'date-fns'

export default {
  name: 'Main',
  setup() {
    const router = useRouter()

    const boards = ref({
      notice: [],
      free: [],
      gallery: [],
      inquiry: []
    })

    const loading = ref({
      notice: true,
      free: true,
      gallery: true,
      inquiry: true
    })

    const fetchRecentBoards = async (boardType) => {
      try {
        loading.value[boardType] = true
        const params = {
          pageNum: 0,
          pageSize: 5,
          orderValue: 'createdAt',
          orderDirection: 'DESC'
        }
        const response = await boardService.getList(boardType, params)
        boards.value[boardType] = response.content || []
      } catch (error) {
        console.error(`Error fetching ${boardType} boards:`, error)
        boards.value[boardType] = []
      } finally {
        loading.value[boardType] = false
      }
    }

    const formatDate = (dateString) => {
      if (!dateString) return ''
      try {
        return format(new Date(dateString), 'yyyy-MM-dd')
      } catch (error) {
        return dateString
      }
    }

    const goToBoard = (type) => {
      router.push({ name: `${type.charAt(0).toUpperCase() + type.slice(1)}List` })
    }

    const goToDetail = (type, id) => {
      router.push({ name: `${type.charAt(0).toUpperCase() + type.slice(1)}View`, params: { id } })
    }

    onMounted(async () => {
      await Promise.all([
        fetchRecentBoards('notice'),
        fetchRecentBoards('free'),
        fetchRecentBoards('gallery'),
        fetchRecentBoards('inquiry')
      ])
    })

    return {
      boards,
      loading,
      formatDate,
      goToBoard,
      goToDetail
    }
  }
}
</script>
