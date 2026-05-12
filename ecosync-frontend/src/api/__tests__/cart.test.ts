import { describe, it, expect, vi, beforeEach } from 'vitest'

vi.mock('@/utils/request', () => {
  const mockRequest = vi.fn()
  return { default: mockRequest }
})

import request from '@/utils/request'
import { cartApi } from '../cart'

const mockedRequest = vi.mocked(request)

describe('cart API', () => {
  beforeEach(() => {
    vi.clearAllMocks()
  })

  it('getByUserId sends GET to /cart/user/:userId', async () => {
    mockedRequest.mockResolvedValue([{ cartItemId: 1, productId: 1, quantity: 2 }])

    await cartApi.getByUserId(12)

    expect(mockedRequest).toHaveBeenCalledWith({
      url: '/cart/user/12',
      method: 'get'
    })
  })

  it('add sends POST to /cart', async () => {
    mockedRequest.mockResolvedValue('加入购物车成功')

    await cartApi.add({ userId: 12, productId: 1, quantity: 3 })

    expect(mockedRequest).toHaveBeenCalledWith({
      url: '/cart',
      method: 'post',
      data: { userId: 12, productId: 1, quantity: 3 }
    })
  })

  it('updateQuantity sends PUT with params', async () => {
    mockedRequest.mockResolvedValue('数量更新成功')

    await cartApi.updateQuantity(1, 5)

    expect(mockedRequest).toHaveBeenCalledWith({
      url: '/cart/1',
      method: 'put',
      params: { quantity: 5 }
    })
  })

  it('deleteItem sends DELETE to /cart/:id', async () => {
    mockedRequest.mockResolvedValue('商品已移除')

    await cartApi.deleteItem(1)

    expect(mockedRequest).toHaveBeenCalledWith({
      url: '/cart/1',
      method: 'delete'
    })
  })

  it('clearCart sends DELETE to /cart/user/:userId', async () => {
    mockedRequest.mockResolvedValue('购物车已清空')

    await cartApi.clearCart(12)

    expect(mockedRequest).toHaveBeenCalledWith({
      url: '/cart/user/12',
      method: 'delete'
    })
  })
})
