import api from './api'

export default {
  async getAll() {
    const response = await api.get('/categories')
    return response.data
  }
}
