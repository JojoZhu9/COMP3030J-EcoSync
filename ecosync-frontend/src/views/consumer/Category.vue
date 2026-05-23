<template>
  <div class="category-page">
    <div class="category-header">
      <div class="title-main">
        <span class="c-green">7-ELEVEN</span> <span class="c-orange">{{ $t('consumer.category.fresh') }}</span>
      </div>
      <div class="title-sub">{{ $t('consumer.category.subtitle') }}</div>
    </div>

    <div class="category-grid">
      <el-row :gutter="16">
        <el-col :span="8" v-for="cat in categories" :key="cat.key">
          <div class="category-tile" @click="handleCategoryClick(cat.key)">
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
        <span class="promo-text">{{ $t('consumer.category.stocksUpdate') }}</span>
      </div>
    </div>

    <div style="height: 100px;"></div>
  </div>
</template>

<script setup lang="ts">
import { computed } from 'vue'
import { useI18n } from 'vue-i18n'
import {
  Menu, Food, Coffee, Bowl,
  IceTea, IceCream, Timer
} from '@element-plus/icons-vue'
import { useRouter } from 'vue-router'

const { t } = useI18n()
const router = useRouter()

const categories = computed(() => [
  { key: 'Bento', name: t('consumer.category.bento'), icon: Bowl, color: '#ff7900', bg: '#fff7ed' },
  { key: 'Sandwich', name: t('consumer.category.sandwich'), icon: Food, color: '#2196F3', bg: '#eff6ff' },
  { key: 'Onigiri', name: t('consumer.category.onigiri'), icon: Menu, color: '#007934', bg: '#f0fdf4' },
  { key: 'Dessert', name: t('consumer.category.dessert'), icon: IceCream, color: '#E91E63', bg: '#fdf2f8' },
  { key: 'Drinks', name: t('consumer.category.drinks'), icon: Coffee, color: '#00BCD4', bg: '#ecfeff' },
  { key: 'Bakery', name: t('consumer.category.bakery'), icon: IceTea, color: '#9E9E9E', bg: '#f8fafc' }
])

const handleCategoryClick = (name: string) => {
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
