import request from '@/utils/request'

export const cartApi = {
  // 获取用户购物车
  getByUserId: (userId: number) => request({
    url: `/cart/user/${userId}`,
    method: 'get'
  }),

  // 加入购物车
  add: (data: { userId: number, productId: number, quantity: number }) => request({
    url: '/cart',
    method: 'post',
    data
  }),

  // 修改数量 (适配了后端使用 URL 参数 @RequestParam 接收数量的要求)
  updateQuantity: (cartItemId: number, quantity: number) => request({
    url: `/cart/${cartItemId}`,
    method: 'put',
    params: { quantity }
  }),

  // 删除单项
  deleteItem: (cartItemId: number) => request({
    url: `/cart/${cartItemId}`,
    method: 'delete'
  }),

  // 清空购物车
  clearCart: (userId: number) => request({
    url: `/cart/user/${userId}`,
    method: 'delete'
  })
}
