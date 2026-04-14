import { fileURLToPath, URL } from 'node:url'
import { defineConfig } from 'vite'
import vue from '@vitejs/plugin-vue'

export default defineConfig({
  plugins: [vue()],
  resolve: {
    alias: {
      '@': fileURLToPath(new URL('./src', import.meta.url))
    }
  },
  server: {
    proxy: {
      // 这里的 '/api' 是指：匹配所有以 /api 开头的请求
      '/api': {
        target: 'http://localhost:8080',
        changeOrigin: true,
        // 【关键】直接删掉原来的 rewrite 行！
        // 这样前端发的 /api/users/login
        // 转发到后端依然是 /api/users/login
      }
    }
  }
})
