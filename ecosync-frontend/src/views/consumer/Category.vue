<template>
  <div class="category-container">
    <div class="header"><h2>全部分类</h2></div>
    <el-row :gutter="15">
      <el-col :span="8" v-for="cat in categories" :key="cat.name">
        <div class="cat-item" @click="handleCategoryClick(cat.name)">
          <div class="cat-icon" :style="{ backgroundColor: cat.color }">
            <el-icon><Menu /></el-icon>
          </div>
          <span class="cat-name">{{ cat.name }}</span>
        </div>
      </el-col>
    </el-row>
    <el-empty description="点击分类查看临期折扣" />
  </div>
</template>

<script setup lang="ts">
import { ref } from 'vue'
import { Menu } from '@element-plus/icons-vue'
import { useRouter } from 'vue-router'

const router = useRouter()
const categories = ref([
  { name: '便当主食', color: '#FF9800' },
  { name: '三明治', color: '#2196F3' },
  { name: '饭团', color: '#4CAF50' },
  { name: '甜点奶制品', color: '#E91E63' },
  { name: '饮料', color: '#00BCD4' },
  { name: '其他', color: '#9E9E9E' }
])

const handleCategoryClick = (name: string) => {
  router.push({ path: '/home', query: { category: name } })
}
</script>

<style scoped>
.category-container { padding: 20px; background: #fff; min-height: 100vh; }
.cat-item { display: flex; flex-direction: column; align-items: center; margin-bottom: 25px; cursor: pointer; }
.cat-icon { width: 50px; height: 50px; border-radius: 12px; display: flex; align-items: center; justify-content: center; color: white; font-size: 24px; margin-bottom: 8px; }
.cat-name { font-size: 13px; color: #666; }
</style>
