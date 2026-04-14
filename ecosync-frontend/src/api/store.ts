import request from '@/utils/request'

export const storeApi = {
  // 修正后：去掉开头的 /api，因为 baseURL 已经包含了它
  getAll: () => {
    return request({
      url: '/stores', // 最终会拼接成 /api/stores
      method: 'get'
    })
  },

  getById: (id: number) => {
    return request({
      url: `/stores/${id}`,
      method: 'get'
    })
  }
}
