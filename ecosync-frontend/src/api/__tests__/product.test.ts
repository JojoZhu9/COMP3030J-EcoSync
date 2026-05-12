import { describe, it, expect, vi, beforeEach } from 'vitest'

vi.mock('@/utils/request', () => {
  return {
    default: {
      get: vi.fn(),
      put: vi.fn(),
      post: vi.fn(),
      delete: vi.fn()
    }
  }
})

import request from '@/utils/request'
import { expiringApi, standardApi } from '../product'

describe('product API', () => {
  beforeEach(() => {
    vi.clearAllMocks()
  })

  it('expiringApi.getByStore sends GET to /expiring-products/store/:storeId', async () => {
    vi.mocked(request.get).mockResolvedValue([{ productId: 1, barcode: '6901234560001' }])

    await expiringApi.getByStore(1)

    expect(request.get).toHaveBeenCalledWith('/expiring-products/store/1')
  })

  it('expiringApi.updateStatus sends PUT with status', async () => {
    vi.mocked(request.put).mockResolvedValue('状态更新成功')

    await expiringApi.updateStatus(1, 'DISCARDED')

    expect(request.put).toHaveBeenCalledWith('/expiring-products/1/status', { status: 'DISCARDED' })
  })

  it('standardApi.getAll sends GET to /products', async () => {
    vi.mocked(request.get).mockResolvedValue([{ barcode: '6901234560001' }])

    await standardApi.getAll()

    expect(request.get).toHaveBeenCalledWith('/products')
  })

  it('standardApi.getByBarcode sends GET to /products/:barcode', async () => {
    vi.mocked(request.get).mockResolvedValue({ barcode: '6901234560001', productName: 'Teriyaki Chicken Bento' })

    await standardApi.getByBarcode('6901234560001')

    expect(request.get).toHaveBeenCalledWith('/products/6901234560001')
  })
})
