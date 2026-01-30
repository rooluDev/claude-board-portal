<template>
  <v-container>
    <h1 class="mb-4">{{ isEditMode ? '갤러리 수정' : '갤러리 작성' }}</h1>

    <v-form ref="formRef" @submit.prevent="handleSubmit">
      <v-card>
        <v-card-text>
          <v-select
            v-model="form.categoryId"
            :items="categories"
            item-title="categoryName"
            item-value="categoryId"
            label="카테고리"
            :rules="[v => !!v || '카테고리를 선택하세요']"
            outlined
            required
          />

          <v-text-field
            v-model="form.title"
            label="제목"
            :rules="[
              v => !!v || '제목을 입력하세요',
              v => (v && v.length <= 99) || '제목은 99자 이내'
            ]"
            counter="99"
            outlined
            required
          />

          <v-textarea
            v-model="form.content"
            label="내용"
            :rules="[
              v => !!v || '내용을 입력하세요',
              v => (v && v.length <= 3999) || '내용은 3999자 이내'
            ]"
            counter="3999"
            rows="10"
            outlined
            required
          />

          <v-file-input
            v-model="files"
            label="이미지 첨부 (최대 5개, 각 2MB)"
            multiple
            accept=".jpg,.jpeg,.gif,.png"
            prepend-icon="mdi-image"
            outlined
          />

          <!-- 기존 파일 (수정 모드) -->
          <div v-if="isEditMode && existingFiles.length > 0">
            <h4>기존 파일</h4>
            <v-checkbox
              v-for="file in existingFiles"
              :key="file.fileId"
              v-model="deleteFileIds"
              :value="file.fileId"
              :label="file.originalName"
              density="compact"
            />
          </div>
        </v-card-text>

        <v-card-actions>
          <v-btn @click="goBack">취소</v-btn>
          <v-spacer />
          <v-btn type="submit" color="primary" :loading="loading">
            {{ isEditMode ? '수정' : '등록' }}
          </v-btn>
        </v-card-actions>
      </v-card>
    </v-form>
  </v-container>
</template>

<script>
import { ref, computed, onMounted } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import boardService from '@/services/boardService'
import categoryService from '@/services/categoryService'

export default {
  name: 'GalleryWrite',

  setup() {
    const router = useRouter()
    const route = useRoute()
    const formRef = ref(null)
    const loading = ref(false)

    const form = ref({
      categoryId: null,
      title: '',
      content: ''
    })

    const files = ref([])
    const existingFiles = ref([])
    const deleteFileIds = ref([])
    const categories = ref([])

    const isEditMode = computed(() => !!route.params.id)

    const fetchCategories = async () => {
      try {
        const response = await categoryService.getAll()
        categories.value = response
      } catch (error) {
        console.error('카테고리 조회 실패:', error)
      }
    }

    const fetchBoard = async () => {
      if (!isEditMode.value) return

      try {
        const response = await boardService.getDetail('gallery', route.params.id)
        form.value = {
          categoryId: response.categoryId,
          title: response.title,
          content: response.content
        }
        existingFiles.value = response.files || []
      } catch (error) {
        console.error('게시물 조회 실패:', error)
      }
    }

    const handleSubmit = async () => {
      const { valid } = await formRef.value.validate()
      if (!valid) return

      const formData = new FormData()
      formData.append('categoryId', form.value.categoryId)
      formData.append('title', form.value.title)
      formData.append('content', form.value.content)

      if (files.value && files.value.length > 0) {
        files.value.forEach(file => formData.append('files', file))
      }

      if (isEditMode.value && deleteFileIds.value.length > 0) {
        formData.append('deleteFileIds', deleteFileIds.value.join(','))
      }

      loading.value = true

      try {
        if (isEditMode.value) {
          await boardService.update('gallery', route.params.id, formData)
          alert('수정되었습니다.')
          router.push({ name: 'GalleryView', params: { id: route.params.id } })
        } else {
          const response = await boardService.create('gallery', formData)
          alert('등록되었습니다.')
          router.push({ name: 'GalleryView', params: { id: response.boardId } })
        }
      } catch (error) {
        console.error('저장 실패:', error)
      } finally {
        loading.value = false
      }
    }

    const goBack = () => router.back()

    onMounted(() => {
      fetchCategories()
      fetchBoard()
    })

    return {
      formRef,
      form,
      files,
      existingFiles,
      deleteFileIds,
      categories,
      isEditMode,
      loading,
      handleSubmit,
      goBack
    }
  }
}
</script>
