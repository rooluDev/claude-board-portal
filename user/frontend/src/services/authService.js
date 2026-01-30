import api from './api'

export default {
  /**
   * 로그인
   */
  async login(memberId, password) {
    const response = await api.post('/login', {
      memberId,
      password
    })
    return response.data
  },

  /**
   * 회원가입
   */
  async signup(memberId, password, memberName) {
    await api.post('/member', {
      memberId,
      password,
      memberName
    })
  },

  /**
   * 아이디 중복 검사
   */
  async checkDuplicate(memberId) {
    const response = await api.get('/member/check-duplicate', {
      params: { memberId }
    })
    return response.data.exists
  },

  /**
   * 현재 로그인 회원 정보 조회
   */
  async getCurrentMember() {
    const response = await api.get('/member')
    return response.data
  }
}
