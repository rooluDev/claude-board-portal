import axios from 'axios'
import store from '@/store'
import { ErrorCommandFactory } from '@/utils/errorHandler'

const api = axios.create({
  baseURL: import.meta.env.VITE_API_BASE_URL || 'http://localhost:8081/api',
  timeout: 10000,
  headers: {
    'Content-Type': 'application/json'
  }
})

// Request Interceptor (JWT 자동 추가)
api.interceptors.request.use(
  config => {
    const token = store.state.auth.accessToken
    if (token) {
      config.headers.Authorization = `Bearer ${token}`
    }
    return config
  },
  error => {
    return Promise.reject(error)
  }
)

// Response Interceptor (에러 처리)
api.interceptors.response.use(
  response => response,
  error => {
    // Command 패턴으로 에러 처리
    const command = ErrorCommandFactory.createCommand(error)
    command.execute()

    return Promise.reject(error)
  }
)

export default api
