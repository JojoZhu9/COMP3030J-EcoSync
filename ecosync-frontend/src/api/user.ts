import request from '@/utils/request'

export const loginApi = (data: any) => {
  return request({
    url: '/users/login',
    method: 'post',
    data
  })
}

export const registerApi = (data: any) => {
  return request({
    url: '/users',
    method: 'post',
    data: {
      ...data,
      balance: 0, // 初始化余额
      status: 'NORMAL' // 默认状态
    }
  })
}

export const getUserInfoApi = (id: number) => {
  return request({
    url: `/users/${id}`,
    method: 'get'
  })
}

// 管理员：修改用户状态（封禁/解封）
export const updateUserStatusApi = (id: number, status: 'NORMAL' | 'BANNED') => {
  return request({
    url: `/users/${id}`,
    method: 'put',
    data: { status }
  })
}
