import { describe, it, expect, vi, beforeEach } from 'vitest'

vi.mock('@/utils/request', () => {
  const mockRequest = vi.fn()
  return { default: mockRequest }
})

import request from '@/utils/request'
import { loginApi, registerApi, getUserInfoApi, updateUserStatusApi } from '../user'

const mockedRequest = vi.mocked(request)

describe('user API', () => {
  beforeEach(() => {
    vi.clearAllMocks()
  })

  it('loginApi sends POST to /users/login', async () => {
    mockedRequest.mockResolvedValue('登录成功，Token: abc123')

    const result = await loginApi({ username: 'user_alice', password: '1' })

    expect(mockedRequest).toHaveBeenCalledWith({
      url: '/users/login',
      method: 'post',
      data: { username: 'user_alice', password: '1' }
    })
    expect(result).toContain('登录成功')
  })

  it('registerApi sends POST without balance/status for security', async () => {
    mockedRequest.mockResolvedValue('用户创建成功')

    const result = await registerApi({ username: 'new_user', passwordHash: '123' })

    expect(mockedRequest).toHaveBeenCalledWith({
      url: '/users',
      method: 'post',
      data: { username: 'new_user', password: undefined, passwordHash: '123', rawPassword: undefined, role: undefined }
    })
  })

  it('getUserInfoApi sends GET to /users/:id', async () => {
    mockedRequest.mockResolvedValue({ userId: 12, username: 'user_alice' })

    await getUserInfoApi(12)

    expect(mockedRequest).toHaveBeenCalledWith({
      url: '/users/12',
      method: 'get'
    })
  })

  it('updateUserStatusApi sends PUT with status', async () => {
    mockedRequest.mockResolvedValue('用户更新成功')

    await updateUserStatusApi(16, 'BANNED')

    expect(mockedRequest).toHaveBeenCalledWith({
      url: '/users/16',
      method: 'put',
      data: { status: 'BANNED' }
    })
  })
})
