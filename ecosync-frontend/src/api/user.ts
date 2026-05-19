import request from '@/utils/request'

export const loginApi = (data: any) => {
  return request({
    url: '/users/login',
    method: 'post',
    data
  })
}

export const registerApi = (data: any) => {
  // 核心修复点：绝对不能在前端显式传递 balance（余额）与 status。
  // 防止被抓包后篡改余额，此类字段应交由后端 Spring Boot 用户服务默认初始化。
  return request({
    url: '/users',
    method: 'post',
    data: {
      username: data.username,
      passwordHash: data.password,
      role: data.role
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
