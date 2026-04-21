<template>
  <div class="landing-container">
    <header class="navbar">
      <div class="logo">
        <div class="brand-logo-mock">
          <span class="c-orange">7</span><span class="c-red">-</span><span class="c-green">ELEVEn</span>
        </div>
        <span class="logo-text divider">|</span>
        <span class="logo-text">Intelligent Near-Expiry Sales Platform</span>
      </div>
      <div class="nav-actions">
        <el-button type="success" color="#007934" round @click="goLogin">Staff / User Login</el-button>
      </div>
    </header>

    <section class="hero-section">
      <div class="hero-content">
        <el-tag type="danger" effect="dark" round class="pulse-tag">Official & Authentic · Safety Guaranteed</el-tag>
        <h1 class="hero-title">Rescue Delicious Food, Embrace an Eco-Friendly Life</h1>
        <p class="hero-subtitle">
          The official 7-ELEVEn near-expiry food platform powered by Smart Chain Retail Tech.<br />
          Grab premium bentos, onigiri, and dairy products at ultra-low discounts, eliminate information gaps, and say no to food waste.
        </p>
        <div class="hero-actions">
          <el-button type="primary" color="#e2231a" size="large" class="cta-btn" round @click="goLogin">
            Start Your Savings Journey <el-icon class="el-icon--right"><ArrowRight /></el-icon>
          </el-button>
          <el-button size="large" class="cta-btn outline-btn" round @click="goLogin">
            Find Nearby Stores <el-icon class="el-icon--right"><Location /></el-icon>
          </el-button>
        </div>
      </div>
    </section>

    <section class="features-section">
      <h2 class="section-title">Reshaping the Near-Expiry Ecosystem</h2>
      <el-row :gutter="40" class="feature-row">
        <el-col :xs="24" :sm="8" class="feature-col">
          <div class="feature-card">
            <el-icon class="f-icon c-green"><Monitor /></el-icon>
            <h3>Digitalization & Transparency</h3>
            <p>Say goodbye to traditional manual recording. Real-time synchronization of inventory and expiration dates, purchased through official channels, completely eliminating information opacity.</p>
          </div>
        </el-col>

        <el-col :xs="24" :sm="8" class="feature-col">
          <div class="feature-card">
            <el-icon class="f-icon c-orange"><Discount /></el-icon>
            <h3>Ultimate Cost-Effectiveness</h3>
            <p>Targeted at consumers with a strong demand for cost-effective food. As the expiration date approaches, the system automatically applies stepped smart discounts.</p>
          </div>
        </el-col>

        <el-col :xs="24" :sm="8" class="feature-col">
          <div class="feature-card">
            <el-icon class="f-icon c-red"><Sunny /></el-icon>
            <h3>Sustainable Responsible Consumption</h3>
            <p>Every order you place directly reduces urban food waste, helping 7-ELEVEn boost profitability while lightening the burden on our planet.</p>
          </div>
        </el-col>
      </el-row>
    </section>

    <section class="promo-section">
      <div class="promo-content">
        <h2 class="promo-title">Empowering Sustainable Retail</h2>
        <h3 class="promo-subtitle">Smart Chain Retail Tech × 7-ELEVEn</h3>
        <div class="promo-divider"></div>
        <p class="promo-text">
          Every day, countless high-quality food items face the risk of disposal simply due to approaching expiration dates. At <strong>Smart Chain Retail Tech</strong>, we believe in a smarter, greener future.
        </p>
        <p class="promo-text">
          Our intelligent platform bridges the gap between 7-ELEVEn stores and eco-conscious consumers. By leveraging real-time inventory tracking, dynamic pricing algorithms, and seamless digital integration, we successfully transform potential food waste into shared value.
        </p>
        <p class="promo-text highlight-text">
          Join us in our mission to revolutionize convenience store operations, boost profitability, and champion responsible consumption for a better planet.
        </p>
        <div class="promo-action">
          <el-button type="warning" color="#ff7900" size="large" round @click="goLogin">
            Explore the Platform
          </el-button>
        </div>
      </div>
    </section>

    <footer class="footer">
      <div class="footer-content">
        <p class="brand-footer"><span class="c-orange">7</span><span class="c-red">-</span><span class="c-green">ELEVEn</span> Near-Expiry Product Management System</p>
        <p class="copyright">© 2026 Developed by <strong>Group 8: Bibilabo</strong> (Smart Chain Retail Tech). All Rights Reserved.</p>
        <p class="disclaimer">Note: This system is custom-developed for 7-ELEVEn stores by Team Bibilabo, dedicated to reducing food waste.</p>
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
.landing-container { font-family: 'PingFang SC', 'Helvetica Neue', Arial, sans-serif; color: #2c3e50; background: #f8f9fa; }

/* 品牌配色 */
.c-green { color: #007934; }
.c-red { color: #e2231a; }
.c-orange { color: #ff7900; }

/* 导航栏 */
.navbar { display: flex; justify-content: space-between; align-items: center; padding: 15px 50px; background: rgba(255,255,255,0.95); position: sticky; top: 0; z-index: 1000; box-shadow: 0 2px 10px rgba(0,0,0,0.05); backdrop-filter: blur(10px); }
.logo { display: flex; align-items: center; gap: 12px; }
.brand-logo-mock { font-size: 26px; font-weight: 900; font-style: italic; letter-spacing: -1px; }
.logo-text { font-size: 16px; font-weight: 500; color: #666; }
.divider { color: #ddd; font-weight: 300; }

/* 首屏区域 */
.hero-section { background: linear-gradient(135deg, #e8f5e9 0%, #fff3e0 50%, #ffebee 100%); padding: 100px 20px; text-align: center; position: relative; overflow: hidden; }
.pulse-tag { margin-bottom: 20px; animation: pulse 2s infinite; }
@keyframes pulse { 0% { box-shadow: 0 0 0 0 rgba(226, 35, 26, 0.4); } 70% { box-shadow: 0 0 0 10px rgba(226, 35, 26, 0); } 100% { box-shadow: 0 0 0 0 rgba(226, 35, 26, 0); } }
.hero-title { font-size: 52px; font-weight: 800; color: #111; margin-bottom: 25px; letter-spacing: 1px; }
.hero-subtitle { font-size: 20px; color: #555; line-height: 1.8; margin-bottom: 40px; }
.hero-actions { display: flex; justify-content: center; gap: 20px; }
.cta-btn { font-size: 18px; padding: 25px 40px; font-weight: bold; }
.outline-btn { color: #007934; border-color: #007934; background: transparent; }
.outline-btn:hover { background: #f0f9f4; }

/* 优势区域 */
.features-section { padding: 80px 50px; background: #fff; }
.section-title { text-align: center; font-size: 32px; font-weight: bold; margin-bottom: 20px; color: #222; }
.feature-card { text-align: center; padding: 40px 25px; border-radius: 16px; background: #fafafa; border: 1px solid #eee; transition: all 0.3s; height: 100%; box-sizing: border-box; }
.feature-card:hover { transform: translateY(-10px); box-shadow: 0 15px 30px rgba(0,0,0,0.06); background: #fff; }
.f-icon { font-size: 45px; margin-bottom: 20px; }
.feature-card h3 { font-size: 20px; margin-bottom: 15px; color: #333; }
.feature-card p { color: #666; line-height: 1.6; font-size: 15px; }

/* 全英文宣传文案区域 (深色高级感) */
.promo-section { padding: 100px 20px; background: linear-gradient(135deg, #004d2c 0%, #007934 100%); color: white; text-align: center; }
.promo-content { max-width: 800px; margin: 0 auto; }
.promo-title { font-size: 38px; font-weight: 800; margin-bottom: 15px; letter-spacing: 1px; text-shadow: 0 2px 4px rgba(0,0,0,0.2); }
.promo-subtitle { font-size: 20px; color: #a7f3d0; font-weight: 400; margin-bottom: 30px; letter-spacing: 0.5px; }
.promo-divider { width: 60px; height: 4px; background: #e2231a; margin: 0 auto 35px; border-radius: 2px; }
.promo-text { font-size: 17px; line-height: 1.8; color: #e2e8f0; margin-bottom: 20px; text-align: center; font-weight: 300; }
.highlight-text { font-size: 20px; font-weight: 600; color: #ffce99; margin-top: 35px; margin-bottom: 40px; }
.promo-action { margin-top: 20px; }

/* 页脚 */
.footer { background: #1a1a1a; color: #888; padding: 50px 20px; text-align: center; }
.brand-footer { font-size: 20px; font-weight: bold; margin-bottom: 15px; color: #fff; }
.copyright { font-size: 14px; margin-bottom: 10px; }
.disclaimer { font-size: 12px; color: #555; }

@media (max-width: 768px) {
  .navbar { padding: 15px 20px; }
  .logo-text.divider, .logo-text { display: none; }
  .hero-title { font-size: 36px; }
  .hero-actions { flex-direction: column; }
  .features-section, .promo-section { padding: 60px 20px; }
  .promo-title { font-size: 28px; }
  .promo-text { font-size: 15px; text-align: left; }
}
</style>
