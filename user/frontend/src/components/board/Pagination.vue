<template>
  <v-pagination
    v-model="currentPageModel"
    :length="totalPages"
    :total-visible="7"
    @update:modelValue="handlePageChange"
  />
</template>

<script>
import { computed } from 'vue'

export default {
  name: 'Pagination',
  props: {
    totalPages: {
      type: Number,
      required: true
    },
    currentPage: {
      type: Number,
      required: true
    }
  },
  emits: ['change'],

  setup(props, { emit }) {
    const currentPageModel = computed({
      get: () => props.currentPage + 1, // 0-based → 1-based
      set: (value) => emit('change', value - 1) // 1-based → 0-based
    })

    const handlePageChange = (page) => {
      emit('change', page - 1)
    }

    return {
      currentPageModel,
      handlePageChange
    }
  }
}
</script>
