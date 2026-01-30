import api from './api'

export default {
  async create(data) {
    await api.post('/comment', data)
  },

  async delete(commentId) {
    await api.delete(`/comment/${commentId}`)
  }
}
