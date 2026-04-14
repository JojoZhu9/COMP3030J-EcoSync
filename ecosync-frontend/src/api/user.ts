import request from '@/utils/request'

// 登录接口
export const loginApi = (data: any) => {
  return request({
    url: '/users/login', // 注意：我们在 vite.config 里配了 /api 代理，这里直接写 /users/login
    method: 'post',
    data
  })
}

// 注册接口
export const registerApi = (data: any) => {
  return request({
    url: '/users',
    method: 'post',
    data
  })
}

// 获取当前用户信息 (可选，用于登录后展示角色)
export const getUserInfoApi = (id: number) => {
  return request({
    url: `/users/${id}`,
    method: 'get'
  })
}
