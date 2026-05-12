<template>
  <div class="landing-container">
    <section class="hero-section">
      <div class="hero-bg-pattern"></div>
      <div class="hero-content">
        <el-tag type="danger" effect="dark" round class="pulse-tag">Official & Authentic · Safety Guaranteed</el-tag>
        <h1 class="hero-title">Rescue Delicious Food,<br/><span class="c-green">Embrace an Eco-Friendly Life</span></h1>
        <p class="hero-subtitle">
          The official 7-ELEVEn near-expiry food platform powered by SMARTCHAIN RETAIL TECH.<br />
          Grab premium bentos, onigiri, and dairy products at ultra-low discounts.
        </p>
        <div class="hero-actions">
          <el-button type="primary" size="large" class="cta-btn primary-cta" round @click="goLogin">
            Start Your Savings Journey <el-icon class="el-icon--right"><ArrowRight /></el-icon>
          </el-button>
          <el-button size="large" class="cta-btn outline-btn" round @click="goLogin">
            Find Nearby Stores <el-icon class="el-icon--right"><Location /></el-icon>
          </el-button>
        </div>
      </div>
    </section>

    <section class="features-section">
      <div class="section-header">
        <h2 class="section-title">Reshaping the Near-Expiry Ecosystem</h2>
        <div class="title-underline"></div>
      </div>

      <el-row :gutter="40" class="feature-row">
        <el-col :xs="24" :sm="8" class="feature-col">
          <div class="feature-card border-green">
            <div class="icon-box bg-green">
              <el-icon><Monitor /></el-icon>
            </div>
            <h3>Digitalization & Transparency</h3>
            <p>Real-time synchronization of inventory and expiration dates, purchased through official channels, completely eliminating information opacity.</p>
          </div>
        </el-col>

        <el-col :xs="24" :sm="8" class="feature-col">
          <div class="feature-card border-orange">
            <div class="icon-box bg-orange">
              <el-icon><Discount /></el-icon>
            </div>
            <h3>Ultimate Cost-Effectiveness</h3>
            <p>Targeted at consumers with a strong demand for value. As the expiration date approaches, the system applies stepped smart discounts.</p>
          </div>
        </el-col>

        <el-col :xs="24" :sm="8" class="feature-col">
          <div class="feature-card border-red">
            <div class="icon-box bg-red">
              <el-icon><Sunny /></el-icon>
            </div>
            <h3>Sustainable Consumption</h3>
            <p>Every order directly reduces urban food waste, helping 7-ELEVEn boost profitability while lightening the burden on our planet.</p>
          </div>
        </el-col>
      </el-row>
    </section>

    <section class="promo-section">
      <div class="promo-overlay"></div>
      <div class="promo-content">
        <h2 class="promo-title">Empowering Sustainable Retail</h2>
        <h3 class="promo-subtitle">SMARTCHAIN RETAIL TECH × 7-ELEVEn</h3>
        <div class="promo-divider"></div>
        <div class="promo-text-container">
          <p class="promo-text">
            Every day, countless high-quality food items face the risk of disposal simply due to approaching expiration dates. At <strong>SMARTCHAIN RETAIL TECH</strong>, we believe in a smarter, greener future.
          </p>
          <p class="promo-text highlight-text">
            Join us in our mission to revolutionize convenience store operations and champion responsible consumption for a better planet.
          </p>
        </div>
        <div class="promo-action">
          <el-button class="explore-btn" size="large" round @click="goLogin">
            Explore the Platform
          </el-button>
        </div>
      </div>
    </section>

    <footer class="footer">
      <div class="footer-content">
        <div class="footer-logo">
          <span class="c-orange">7</span><span class="c-red">-</span><span class="c-green">ELEVEn</span>
        </div>
        <p class="brand-footer">Near-Expiry Product Management System</p>
        <div class="footer-divider"></div>
        <p class="copyright">© 2026 Developed by <strong>Group 8: Bibilabo</strong>. All Rights Reserved.</p>
        <p class="disclaimer">Custom-developed for 7-ELEVEn stores by Team Bibilabo.</p>
        <div class="disclaimer">
          <p>
            We are an innovative development team dedicated to leveraging smart retail technology to solve urban challenges.
            Our project is deeply committed to <strong>United Nations Sustainable Development Goal 12 (SDG 12): Responsible Consumption and Production</strong>.
          </p>
          <p>
            By implementing dynamic near-expiry product tracking and automated discounting, we aim to minimize food waste and promote a circular economy within the 7-ELEVEn retail ecosystem.
          </p>
        </div>
      </div>
    </footer>
  </div>
</template>

<script setup lang="ts">
import { useRouter } from 'vue-router'
import {
  ArrowRight, Discount, Monitor, Sunny, Location
} from '@element-plus/icons-vue'

const router = useRouter()
const goLogin = () => router.push('/login')
</script>

<style scoped>
/* 原汁原味保留你所有的CSS样式 */
:root {
  --711-green: #007934;
  --711-red: #e2231a;
  --711-orange: #ff7900;
}

.landing-container {
  font-family: 'Inter', 'PingFang SC', sans-serif;
  color: #1a1a1a;
  background: #ffffff;
}

.c-green { color: #007934; }
.c-red { color: #e2231a; }
.c-orange { color: #ff7900; }

.bg-green { background: #007934; }
.bg-red { background: #e2231a; }
.bg-orange { background: #ff7900; }

/* Hero Section */
.hero-section {
  min-height: 85vh;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 0 20px;
  background: linear-gradient(135deg, #f0fdf4 0%, #fff7ed 50%, #fef2f2 100%);
  position: relative;
  overflow: hidden;
}
.hero-bg-pattern {
  position: absolute;
  width: 100%; height: 100%;
  background-image: radial-gradient(#007934 0.5px, transparent 0.5px);
  background-size: 30px 30px;
  opacity: 0.05;
}
.hero-content { position: relative; z-index: 10; text-align: center; max-width: 900px; }
.pulse-tag { margin-bottom: 30px; border: none; font-weight: bold; font-size: 14px; padding: 12px 20px; }
.hero-title { font-size: 64px; font-weight: 900; line-height: 1.1; margin-bottom: 25px; color: #002d14; letter-spacing: -2px; }
.hero-subtitle { font-size: 22px; color: #4b5563; line-height: 1.6; margin-bottom: 45px; font-weight: 400; }

.cta-btn { font-size: 18px; padding: 28px 45px; font-weight: 700; transition: all 0.3s ease; }
.primary-cta { background-color: #e2231a !important; border: none; box-shadow: 0 10px 20px rgba(226, 35, 26, 0.2); }
.primary-cta:hover { transform: translateY(-3px); box-shadow: 0 15px 30px rgba(226, 35, 26, 0.3); }
.outline-btn { color: #007934; border: 2px solid #007934; background: transparent; }
.outline-btn:hover { background: rgba(0, 121, 52, 0.05); }

/* Features Section */
.features-section { padding: 120px 60px; background: #fff; }
.section-header { text-align: center; margin-bottom: 80px; }
.section-title { font-size: 40px; font-weight: 800; color: #002d14; margin-bottom: 15px; }
.title-underline { width: 80px; height: 5px; background: #ff7900; margin: 0 auto; border-radius: 10px; }

.feature-card {
  padding: 50px 35px;
  border-radius: 24px;
  background: #fff;
  border: 1px solid #f0f0f0;
  transition: all 0.4s cubic-bezier(0.4, 0, 0.2, 1);
  height: 100%;
  box-sizing: border-box;
  text-align: left;
}
.feature-card:hover { transform: translateY(-12px); box-shadow: 0 30px 60px rgba(0,0,0,0.08); }
.icon-box {
  width: 70px; height: 70px;
  border-radius: 18px;
  display: flex; align-items: center; justify-content: center;
  font-size: 32px; color: white;
  margin-bottom: 30px;
}
.feature-card h3 { font-size: 22px; font-weight: 800; margin-bottom: 15px; color: #111; }
.feature-card p { color: #6b7280; line-height: 1.7; font-size: 16px; margin: 0; }

.border-green:hover { border-bottom: 6px solid #007934; }
.border-orange:hover { border-bottom: 6px solid #ff7900; }
.border-red:hover { border-bottom: 6px solid #e2231a; }

/* Promo Section */
.promo-section {
  padding: 140px 20px;
  background: url('https://images.unsplash.com/photo-1578916171728-46686eac8d58?auto=format&fit=crop&q=80&w=2000') no-repeat center center;
  background-size: cover;
  position: relative;
  color: white;
  text-align: center;
}
.promo-overlay {
  position: absolute; top: 0; left: 0; width: 100%; height: 100%;
  background: linear-gradient(135deg, rgba(0,77,44,0.95) 0%, rgba(0,45,20,0.9) 100%);
}
.promo-content { position: relative; z-index: 10; max-width: 850px; margin: 0 auto; }
.promo-title { font-size: 48px; font-weight: 900; margin-bottom: 10px; letter-spacing: -1px; }
.promo-subtitle { font-size: 20px; color: #4ade80; font-weight: 500; margin-bottom: 35px; }
.promo-divider { width: 50px; height: 4px; background: #e2231a; margin: 0 auto 40px; }
.promo-text { font-size: 19px; line-height: 1.8; color: #d1d5db; margin-bottom: 25px; font-weight: 300; }
.highlight-text { font-size: 22px; font-weight: 700; color: #ffce99; margin-top: 40px; }

.explore-btn {
  background-color: #ff7900 !important;
  color: white !important;
  border: none;
  font-weight: 800;
  padding: 25px 50px;
  font-size: 18px;
  box-shadow: 0 10px 25px rgba(255, 121, 0, 0.4);
}
.about-section {
  padding: 60px 20px;
  background-color: #111; /* 黑色背景，衔接原本底部的黑色区域 */
  color: #fff;
  text-align: center;
}
.about-content {
  max-width: 800px;
  margin: 0 auto;
}
.about-content h2 { color: #007934; margin-bottom: 20px; }
.about-content p { line-height: 1.8; color: #ccc; }

/* Footer */
.footer { background: #0a0a0a; color: #9ca3af; padding: 80px 20px; text-align: center; }
.footer-logo { font-size: 36px; font-weight: 900; font-style: italic; margin-bottom: 10px; }
.brand-footer { font-size: 18px; font-weight: 600; margin-bottom: 30px; color: #fff; letter-spacing: 1px; }
.footer-divider { width: 100%; max-width: 1200px; height: 1px; background: rgba(255,255,255,0.1); margin: 0 auto 30px; }
.copyright { font-size: 14px; margin-bottom: 8px; }
.disclaimer { font-size: 12px; opacity: 0.5; }

@media (max-width: 768px) {
  .hero-title { font-size: 42px; }
  .hero-actions { flex-direction: column; gap: 15px; }
  .features-section { padding: 80px 25px; }
  .promo-title { font-size: 32px; }
  .feature-card { padding: 40px 25px; }
}
</style>
