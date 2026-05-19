import request from '@/utils/request'

export const storeApi = {
  getAll: () => {
    return request({ url: '/stores', method: 'get' })
  },

  getById: (id: number) => {
    return request({ url: `/stores/${id}`, method: 'get' })
  },

  create: (data: any) => {
    return request({ url: '/stores', method: 'post', data })
  },

  update: (id: number, data: any) => {
    return request({ url: `/stores/${id}`, method: 'put', data })
  },

  delete: (id: number) => {
    return request({ url: `/stores/${id}`, method: 'delete' })
  }
}
