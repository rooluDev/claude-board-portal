import api from './api'

export default {
  async getList(boardType, params) {
    const response = await api.get(`/boards/${boardType}`, { params })
    return response.data
  },

  async getDetail(boardType, id) {
    const response = await api.get(`/board/${boardType}/${id}`)
    return response.data
  },

  async create(boardType, formData) {
    const response = await api.post(`/board/${boardType}`, formData, {
      headers: { 'Content-Type': 'multipart/form-data' }
    })
    return response.data
  },

  async update(boardType, id, formData) {
    await api.put(`/board/${boardType}/${id}`, formData, {
      headers: { 'Content-Type': 'multipart/form-data' }
    })
  },

  async delete(boardType, id) {
    await api.delete(`/board/${boardType}/${id}`)
  },

  async increaseView(boardType, id) {
    await api.patch(`/board/${boardType}/${id}/increase-view`)
  },

  async checkAuthor(boardType, id) {
    const response = await api.get(`/board/${boardType}/${id}/check-author`)
    return response.data.isAuthor
  }
}
