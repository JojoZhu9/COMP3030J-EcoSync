<template>
  <div class="category-page">
    <div class="brand-stripe"></div>
    <div class="category-header">
      <div class="title-main">
        <span class="c-green">Fresh</span> <span class="c-orange">Selection</span>
      </div>
      <div class="title-sub">Explore Daily Discounts by Category</div>
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
        <span class="promo-text">Real-time shelf updates every hour</span>
      </div>
    </div>

    <el-empty class="smartchain-empty" description="Select a department to view items near expiration">
      <template #image>
        <el-icon :size="60" class="empty-icon"><Compass /></el-icon>
      </template>
    </el-empty>
  </div>
</template>

<script setup lang="ts">
import { ref } from 'vue'
import {
  Menu, Food, Coffee, Bowl,
  IceTea, Dessert, Timer,
  Compass, IceCream
} from '@element-plus/icons-vue'
import { useRouter } from 'vue-router'

const router = useRouter()

const categories = ref([
  { name: 'Bento/Meals', icon: Bowl, color: '#ff7900', bg: '#fff7ed' },
  { name: 'Sandwich', icon: Food, color: '#2196F3', bg: '#eff6ff' },
  { name: 'Onigiri', icon: Menu, color: '#007934', bg: '#f0fdf4' },
  { name: 'Desserts', icon: IceCream, color: '#E91E63', bg: '#fdf2f8' },
  { name: 'Beverages', icon: Coffee, color: '#00BCD4', bg: '#ecfeff' },
  { name: 'Dairy/Other', icon: IceTea, color: '#9E9E9E', bg: '#f8fafc' }
])

const handleCategoryClick = (name: string) => {
  router.push({ path: '/home', query: { category: name } })
}
</script>

<style scoped>
.category-page { padding: 0 0 40px; background: #ffffff; min-height: 100vh; }
.brand-stripe { height: 4px; background: linear-gradient(to right, #ff7900 33%, #007934 33%, #007934 66%, #e2231a 66%); }
.category-header { padding: 30px 20px 20px; }
.title-main { font-size: 24px; font-weight: 900; letter-spacing: -0.5px; text-transform: uppercase; }
.c-green { color: #007934; }
.c-orange { color: #ff7900; }
.title-sub { font-size: 12px; color: #94a3b8; font-weight: 700; margin-top: 4px; text-transform: uppercase; letter-spacing: 1px; }
.category-grid { padding: 10px 20px; }
.category-tile { display: flex; flex-direction: column; align-items: center; margin-bottom: 24px; cursor: pointer; transition: transform 0.2s; }
.category-tile:active { transform: scale(0.92); }
.tile-icon-wrapper { width: 68px; height: 68px; border-radius: 20px; display: flex; align-items: center; justify-content: center; font-size: 28px; margin-bottom: 12px; position: relative; box-shadow: 0 4px 12px rgba(0,0,0,0.03); }
.status-dot { position: absolute; top: 12px; right: 12px; width: 6px; height: 6px; background: #48bb78; border-radius: 50%; border: 2px solid #fff; }
.tile-label { font-size: 13px; font-weight: 800; color: #475569; }
.promo-banner { margin: 10px 20px; padding: 12px; background: #f1f5f9; border-radius: 12px; display: flex; justify-content: center; }
.promo-content { display: flex; align-items: center; gap: 8px; color: #64748b; font-size: 12px; font-weight: 700; }
.smartchain-empty { margin-top: 20px; }
.empty-icon { color: #e2e8f0; }
:deep(.el-empty__description) { font-weight: 600; font-size: 13px; color: #cbd5e1; }
@media (min-width: 768px) { .category-grid { max-width: 600px; margin: 0 auto; } }
</style>
