import request from '@/utils/request'

export const expiringApi = {
  getByStore: (storeId: number) => request.get(`/expiring-products/store/${storeId}`),
  updateStatus: (id: number, status: string) => request.put(`/expiring-products/${id}/status`, { status })
}

export const standardApi = {
  // 核心修复点：将 standard-products 改为 products
  getByBarcode: (barcode: string) => request.get(`/products/${barcode}`),
  getAll: () => request.get('/products')
}
