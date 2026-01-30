export default {
  namespaced: true,

  state: {
    accessToken: null,
    memberId: null,
    memberName: null,
    isLoggedIn: false
  },

  mutations: {
    SET_AUTH(state, { accessToken, memberId, memberName }) {
      state.accessToken = accessToken
      state.memberId = memberId
      state.memberName = memberName
      state.isLoggedIn = true
    },

    CLEAR_AUTH(state) {
      state.accessToken = null
      state.memberId = null
      state.memberName = null
      state.isLoggedIn = false
    }
  },

  actions: {
    login({ commit }, authData) {
      commit('SET_AUTH', authData)
    },

    logout({ commit }) {
      commit('CLEAR_AUTH')
    }
  },

  getters: {
    isLoggedIn: state => state.isLoggedIn,
    memberId: state => state.memberId,
    memberName: state => state.memberName,
    accessToken: state => state.accessToken
  }
}
