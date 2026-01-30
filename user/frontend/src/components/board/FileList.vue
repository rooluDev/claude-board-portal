<template>
  <v-card v-if="files && files.length > 0" class="mt-4">
    <v-card-title>첨부파일 ({{ files.length }})</v-card-title>
    <v-card-text>
      <v-list>
        <v-list-item
          v-for="file in files"
          :key="file.fileId"
          @click="handleDownload(file.fileId, file.originalName)"
        >
          <template v-slot:prepend>
            <v-icon>mdi-file-download</v-icon>
          </template>
          <v-list-item-title>
            {{ file.originalName }}
            <span class="text-caption ml-2">({{ formatFileSize(file.size) }})</span>
          </v-list-item-title>
        </v-list-item>
      </v-list>
    </v-card-text>
  </v-card>
</template>

<script>
import fileService from '@/services/fileService'

export default {
  name: 'FileList',
  props: {
    files: {
      type: Array,
      default: () => []
    }
  },

  setup() {
    const handleDownload = async (fileId, fileName) => {
      try {
        await fileService.download(fileId, fileName)
      } catch (error) {
        console.error('파일 다운로드 실패:', error)
        alert('파일 다운로드에 실패했습니다.')
      }
    }

    const formatFileSize = (bytes) => {
      if (bytes < 1024) return bytes + ' B'
      if (bytes < 1024 * 1024) return (bytes / 1024).toFixed(2) + ' KB'
      return (bytes / (1024 * 1024)).toFixed(2) + ' MB'
    }

    return {
      handleDownload,
      formatFileSize
    }
  }
}
</script>
