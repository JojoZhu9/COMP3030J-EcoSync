<template>
  <div class="category-page">
    <div class="category-header">
      <div class="title-main">
        <span class="c-green">7-ELEVEN</span> <span class="c-orange">Fresh</span>
      </div>
      <div class="title-sub">SmartChain Retail Tech - Category Selection</div>
    </div>

    <div class="category-grid">
      <el-row :gutter="16">
        <el-col :span="8" v-for="cat in categories" :key="cat.name">
          <div class="category-tile" @click="handleCategoryClick(cat.name)">
            <div class="tile-icon-wrapper" :style="{ background: cat.bg }">
              <el-icon :style="{ color: cat.color }">
                <component :is="cat.icon" />
              </el-icon>
              <div class="status-dot"></div>
            </div>
            <span class="tile-label">{{ cat.name }}</span>
          </div>
        </el-col>
      </el-row>
    </div>

    <div class="promo-banner">
      <div class="promo-content">
        <el-icon><Timer /></el-icon>
        <span class="promo-text">Stocks update every 60 mins</span>
      </div>
    </div>

    <div style="height: 100px;"></div>
  </div>
</template>

<script setup lang="ts">
import { ref } from 'vue'
import {
  Menu, Food, Coffee, Bowl,
  IceTea, IceCream, Timer
} from '@element-plus/icons-vue'
import { useRouter } from 'vue-router'

const router = useRouter()

const categories = ref([
  { name: 'Bento', icon: Bowl, color: '#ff7900', bg: '#fff7ed' },
  { name: 'Sandwich', icon: Food, color: '#2196F3', bg: '#eff6ff' },
  { name: 'Onigiri', icon: Menu, color: '#007934', bg: '#f0fdf4' },
  { name: 'Dessert', icon: IceCream, color: '#E91E63', bg: '#fdf2f8' },
  { name: 'Drinks', icon: Coffee, color: '#00BCD4', bg: '#ecfeff' },
  { name: 'Bakery', icon: IceTea, color: '#9E9E9E', bg: '#f8fafc' }
])

const handleCategoryClick = (name: string) => {
  // 跳轉回首頁，並帶上分類參數
  router.push({
    path: '/home',
    query: { category: name }
  })
}
</script>

<style scoped>
.category-page {
  background: #fff;
  min-height: 100vh;
  /* 確保內容不被手機狀態欄或導航欄遮蓋 */
  padding-top: env(safe-area-inset-top);
}

.category-header { padding: 40px 20px 20px; }
.title-main { font-size: 24px; font-weight: 900; letter-spacing: -0.5px; }
.c-green { color: #007934; }
.c-orange { color: #ff7900; }
.title-sub { font-size: 13px; color: #94a3b8; margin-top: 4px; font-weight: 600; }

.category-grid { padding: 10px 20px; }
.category-tile {
  display: flex;
  flex-direction: column;
  align-items: center;
  margin-bottom: 24px;
  cursor: pointer;
  transition: transform 0.2s;
}
.category-tile:active { transform: scale(0.9); }

.tile-icon-wrapper {
  width: 68px;
  height: 68px;
  border-radius: 20px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 28px;
  margin-bottom: 10px;
  position: relative;
  box-shadow: 0 4px 12px rgba(0,0,0,0.03);
}

.status-dot {
  position: absolute;
  top: 10px;
  right: 10px;
  width: 7px;
  height: 7px;
  background: #10b981;
  border-radius: 50%;
  border: 2px solid #fff;
}

.tile-label { font-size: 13px; font-weight: 700; color: #334155; }

.promo-banner {
  margin: 10px 20px;
  padding: 14px;
  background: #f8fafc;
  border-radius: 14px;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 8px;
  font-size: 12px;
  font-weight: 700;
  color: #64748b;
}
</style>
