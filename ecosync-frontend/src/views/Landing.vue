<<template>
  <div class="landing-container">
    <!-- Hero 轮播 -->
    <section class="hero-carousel">
      <div class="hero-particles">
        <div v-for="n in 6" :key="n" class="particle" :style="particleStyle(n)"></div>
      </div>

      <el-carousel height="100vh" :interval="6000" arrow="hover" trigger="click" indicator-position="outside">
        <el-carousel-item>
          <div class="carousel-slide slide-1">
            <div class="hero-bg-grid"></div>
            <div class="hero-content">
              <el-tag
                type="danger"
                effect="dark"
                round
                class="pulse-tag animate-fade-in-down"
              >
                <el-icon class="tag-icon"><Stamp /></el-icon>
                Official & Authentic · Safety Guaranteed
              </el-tag>
              <h1 class="hero-title">
                <span class="title-line animate-fade-in-up" style="animation-delay: 0.1s">Rescue Delicious Food,</span>
                <span class="title-line animate-fade-in-up" style="animation-delay: 0.3s">
                  <span class="c-green">Embrace an Eco-Friendly Life</span>
                </span>
              </h1>
              <p class="hero-subtitle animate-fade-in-up" style="animation-delay: 0.5s">
                The official 7-ELEVEn near-expiry food platform powered by SMARTCHAIN RETAIL TECH.
                <br />Grab premium bentos, onigiri, and dairy products at ultra-low discounts.
              </p>
              <div class="hero-actions animate-fade-in-up" style="animation-delay: 0.7s">
                <el-button
                  type="primary"
                  size="large"
                  class="cta-btn primary-cta magnetic-btn"
                  round
                  @click="goLogin"
                >
                  Start Your Savings Journey
                  <el-icon class="el-icon--right"><ArrowRight /></el-icon>
                </el-button>
                <el-button
                  size="large"
                  class="cta-btn secondary-cta"
                  round
                  @click="scrollTo('features')"
                >
                  Explore More
                </el-button>
              </div>
            </div>
            <div class="scroll-indicator" @click="scrollTo('features')">
              <div class="mouse">
                <div class="wheel"></div>
              </div>
              <div class="scroll-text">Scroll to explore</div>
            </div>
          </div>
        </el-carousel-item>

        <el-carousel-item>
          <div class="carousel-slide slide-2">
            <div class="hero-bg-grid"></div>
            <div class="hero-content">
              <el-tag
                type="warning"
                effect="dark"
                round
                class="pulse-tag animate-fade-in-down"
              >
                <el-icon class="tag-icon"><TrendCharts /></el-icon>
                TECH EMPOWERMENT
              </el-tag>
              <h1 class="hero-title">
                <span class="title-line animate-fade-in-up" style="animation-delay: 0.1s">Sustainable Retail,</span>
                <span class="title-line animate-fade-in-up" style="animation-delay: 0.3s">
                  <span class="c-orange">Zero Food Waste</span>
                </span>
              </h1>
              <p class="hero-subtitle animate-fade-in-up" style="animation-delay: 0.5s">
                Empowering 7-ELEVEn with data-driven logistics to solve urban food waste.
                <br />Join us in building a greener future.
              </p>
              <div class="hero-actions animate-fade-in-up" style="animation-delay: 0.7s">
                <el-button
                  type="primary"
                  size="large"
                  class="cta-btn primary-cta orange-cta magnetic-btn"
                  round
                  @click="goLogin"
                >
                  Join Our Community
                  <el-icon class="el-icon--right"><ArrowRight /></el-icon>
                </el-button>
              </div>
            </div>
            <div class="scroll-indicator" @click="scrollTo('features')">
              <div class="mouse">
                <div class="wheel"></div>
              </div>
              <div class="scroll-text">Scroll to explore</div>
            </div>
          </div>
        </el-carousel-item>
      </el-carousel>
    </section>

    <!-- 数据统计区 -->
    <section class="stats-section">
      <div class="stats-container">
        <div v-for="(stat, index) in stats" :key="index" class="stat-item" :style="{ animationDelay: `${index * 0.15}s` }">
          <div class="stat-icon" :class="stat.color">
            <el-icon :size="28"><component :is="stat.icon" /></el-icon>
          </div>
          <div class="stat-number">{{ stat.value }}</div>
          <div class="stat-label">{{ stat.label }}</div>
        </div>
      </div>
    </section>

    <<!-- Features 区域 -->
    <section id="features" class="features-section">
      <div class="section-header">
        <span class="section-tag">CORE VALUES</span>
        <h2 class="section-title">Reshaping the Near-Expiry Ecosystem</h2>
        <div class="title-underline"></div>
        <p class="section-desc">Three pillars that define our commitment to sustainable retail innovation</p>
      </div>

      <div class="feature-row-flex">
        <div class="feature-col-flex">
          <div class="feature-card border-green" @mousemove="handleTilt" @mouseleave="resetTilt">
            <div class="card-shine"></div>
            <div class="icon-box bg-green">
              <el-icon :size="32"><Monitor /></el-icon>
            </div>
            <h3>Digitalization & Transparency</h3>
            <p>Real-time synchronization of inventory and expiration dates, purchased through official channels, completely eliminating information opacity.</p>
          </div>
        </div>

        <div class="feature-col-flex">
          <div class="feature-card border-orange" @mousemove="handleTilt" @mouseleave="resetTilt">
            <div class="card-shine"></div>
            <div class="icon-box bg-orange">
              <el-icon :size="32"><Discount /></el-icon>
            </div>
            <h3>Ultimate Cost-Effectiveness</h3>
            <p>Targeted at consumers with a strong demand for value. As the expiration date approaches, the system applies stepped smart discounts.</p>
          </div>
        </div>

        <div class="feature-col-flex">
          <div class="feature-card border-red" @mousemove="handleTilt" @mouseleave="resetTilt">
            <div class="card-shine"></div>
            <div class="icon-box bg-red">
              <el-icon :size="32"><Sunny /></el-icon>
            </div>
            <h3>Sustainable Consumption</h3>
            <p>Every order directly reduces urban food waste, helping 7-ELEVEn boost profitability while lightening the burden on our planet.</p>
          </div>
        </div>
      </div>
    </section>

    <!-- 流程展示 -->
    <section id="process" class="process-section">
      <div class="section-header">
        <span class="section-tag">HOW IT WORKS</span>
        <h2 class="section-title">Simple, Fast, Effective</h2>
        <div class="title-underline"></div>
      </div>

      <div class="process-container">
        <div v-for="(step, index) in processSteps" :key="index" class="process-step" :class="{ 'step-active': true }">
          <div class="step-connector" v-if="index < processSteps.length - 1">
            <div class="connector-line"></div>
          </div>
          <div class="step-number">{{ index + 1 }}</div>
          <div class="step-card">
            <div class="step-icon" :class="step.color">
              <el-icon :size="24"><component :is="step.icon" /></el-icon>
            </div>
            <h4>{{ step.title }}</h4>
            <p>{{ step.desc }}</p>
          </div>
        </div>
      </div>
    </section>

    <!-- About 区域 -->
    <section id="about" class="about-section">
      <div class="about-content">
        <div class="about-header">
          <span class="team-tag">OUR MISSION</span>
          <h2>About Bibilabo Group 8</h2>
          <div class="accent-line"></div>
        </div>

        <div class="about-grid">
          <div class="about-text">
            <p class="about-desc reveal-text">
              We are an innovative software engineering team dedicated to leveraging smart retail technology to solve modern urban challenges.
            </p>
            <p class="about-desc reveal-text">
              Our platform is deeply committed to <strong>United Nations Sustainable Development Goal 12 (SDG 12): Responsible Consumption and Production</strong>.
            </p>
            <p class="about-desc reveal-text">
              By implementing dynamic near-expiry product tracking and automated algorithmic discounting, we aim to minimize food waste and promote a circular economy within the 7-ELEVEn retail ecosystem.
            </p>
          </div>

          <div class="about-visual">
            <div class="sdg-badge">
              <div class="sdg-number">12</div>
              <div class="sdg-label">SDG</div>
            </div>
          </div>
        </div>

        <div class="tech-stack-row">
          <span v-for="tech in techStack" :key="tech" class="tech-item">
            {{ tech }}
          </span>
        </div>
      </div>
    </section>

    <!-- CTA 区域 -->
    <section class="cta-section">
      <div class="cta-bg-pattern"></div>
      <div class="cta-content">
        <h2>Ready to Make a Difference?</h2>
        <p>Join thousands of eco-conscious consumers saving money and the planet.</p>
        <el-button
          type="primary"
          size="large"
          class="cta-btn primary-cta magnetic-btn"
          round
          @click="goLogin"
        >
          Start Saving Now
          <el-icon class="el-icon--right"><ArrowRight /></el-icon>
        </el-button>
      </div>
    </section>

    <!-- Footer -->
    <footer class="footer">
      <div class="footer-content">
        <div class="footer-main">
          <div class="footer-brand">
            <div class="footer-logo">
              <span class="c-orange">7</span><span class="c-red">-</span><span class="c-green">ELEVEn</span>
            </div>
            <p class="brand-footer">Near-Expiry Product Management System</p>
            <p class="brand-desc">Transforming retail waste into consumer value through intelligent technology.</p>
          </div>
          <div class="footer-links">
            <div class="link-group">
              <h4>Platform</h4>
              <a href="#features">Features</a>
              <a href="#process">Process</a>
              <a href="#about">About Us</a>
            </div>
          </div>
        </div>
        <div class="footer-divider"></div>
        <div class="footer-bottom">
          <p class="copyright">© 2026 Developed by <strong>Group 8: Bibilabo</strong>. All Rights Reserved.</p>
          <p class="disclaimer">Custom-developed for 7-ELEVEn stores by Team Bibilabo.</p>
        </div>
      </div>
    </footer>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, onUnmounted } from 'vue'
import { useRouter } from 'vue-router'
import {
  ArrowRight, Discount, Monitor, Sunny,
  Stamp, TrendCharts, Food, ShoppingCart,
  Timer, CircleCheck, Wallet
} from '@element-plus/icons-vue'

const router = useRouter()

// 平滑滚动
const scrollTo = (id: string) => {
  const element = document.getElementById(id)
  if (element) {
    element.scrollIntoView({ behavior: 'smooth', block: 'start' })
  }
}

// 粒子背景
const particleStyle = (n: number) => ({
  width: `${Math.random() * 300 + 100}px`,
  height: `${Math.random() * 300 + 100}px`,
  left: `${Math.random() * 100}%`,
  top: `${Math.random() * 100}%`,
  animationDuration: `${Math.random() * 20 + 15}s`,
  animationDelay: `${Math.random() * 5}s`
})

// 3D 卡片倾斜效果
const handleTilt = (e: MouseEvent) => {
  const card = e.currentTarget as HTMLElement
  const rect = card.getBoundingClientRect()
  const x = e.clientX - rect.left
  const y = e.clientY - rect.top
  const centerX = rect.width / 2
  const centerY = rect.height / 2
  const rotateX = (y - centerY) / 20
  const rotateY = (centerX - x) / 20

  card.style.transform = `perspective(1000px) rotateX(${rotateX}deg) rotateY(${rotateY}deg) translateY(-12px)`

  const shine = card.querySelector('.card-shine') as HTMLElement
  if (shine) {
    shine.style.background = `radial-gradient(circle at ${x}px ${y}px, rgba(255,255,255,0.2) 0%, transparent 60%)`
    shine.style.opacity = '1'
  }
}

const resetTilt = (e: MouseEvent) => {
  const card = e.currentTarget as HTMLElement
  card.style.transform = ''
  const shine = card.querySelector('.card-shine') as HTMLElement
  if (shine) {
    shine.style.opacity = '0'
  }
}

// 统计数据
const stats = [
  { value: '50K+', label: 'Active Users', icon: 'User', color: 'text-green' },
  { value: '1.2M', label: 'Items Rescued', icon: 'Food', color: 'text-orange' },
  { value: '35%', label: 'Avg. Savings', icon: 'Wallet', color: 'text-red' },
  { value: '200+', label: 'Store Partners', icon: 'Shop', color: 'text-green' }
]

// 流程步骤
const processSteps = [
  {
    title: 'Scan & Discover',
    desc: 'Browse real-time near-expiry inventory from your local 7-ELEVEn stores.',
    icon: 'Search',
    color: 'bg-green'
  },
  {
    title: 'Smart Discount',
    desc: 'AI-powered dynamic pricing ensures you get the best deal based on expiry proximity.',
    icon: 'Discount',
    color: 'bg-orange'
  },
  {
    title: 'Quick Pickup',
    desc: 'Secure your items with one tap and pick them up within 2 hours.',
    icon: 'Timer',
    color: 'bg-red'
  },
  {
    title: 'Save & Sustain',
    desc: 'Enjoy quality food at reduced prices while contributing to zero food waste.',
    icon: 'CircleCheck',
    color: 'bg-green'
  }
]

// 技术栈
const techStack = ['Spring Boot 3', 'Vue 3', 'MyBatis', 'MySQL 8.0', 'Redis', 'Docker']

const goLogin = () => router.push('/login')

onMounted(() => {
  // 移除滚动监听，不再需要
})

onUnmounted(() => {
  // 移除滚动监听
})
</script>

<style scoped>
@import url('https://fonts.googleapis.com/css2?family=Inter:wght@300;400;500;600;700;800;900&display=swap');

:root {
  --711-green: #007934;
  --711-red: #e2231a;
  --711-orange: #ff7900;
  --dark-bg: #0a0a0a;
  --card-bg: #111111;
}

.landing-container {
  font-family: 'Inter', 'PingFang SC', 'Microsoft YaHei', sans-serif;
  color: #1a1a1a;
  background: #ffffff;
  overflow-x: hidden;
}

.c-green { color: #007934; }
.c-red { color: #e2231a; }
.c-orange { color: #ff7900; }
.bg-green { background: linear-gradient(135deg, #007934, #00994d); }
.bg-red { background: linear-gradient(135deg, #e2231a, #ff4444); }
.bg-orange { background: linear-gradient(135deg, #ff7900, #ff9944); }
.text-green { color: #007934; }
.text-red { color: #e2231a; }
.text-orange { color: #ff7900; }

/* ======== Hero 轮播 ======== */
.hero-carousel {
  position: relative;
  padding-top: 76px; /* 原有导航栏高度 70px + 顶部彩条 6px */
}

.hero-particles {
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  overflow: hidden;
  z-index: 1;
  pointer-events: none;
}

.particle {
  position: absolute;
  border-radius: 50%;
  background: radial-gradient(circle, rgba(0, 121, 52, 0.08) 0%, transparent 70%);
  animation: float-particle linear infinite;
}

@keyframes float-particle {
  0% { transform: translate(0, 0) rotate(0deg); opacity: 0; }
  10% { opacity: 1; }
  90% { opacity: 1; }
  100% { transform: translate(100px, -100px) rotate(360deg); opacity: 0; }
}

.carousel-slide {
  height: 100%;
  display: flex;
  align-items: center;
  justify-content: center;
  position: relative;
  overflow: hidden;
}

.slide-1 {
  background: linear-gradient(135deg, #f0fdf4 0%, #fff7ed 50%, #fef2f2 100%);
}
.slide-2 {
  background: linear-gradient(135deg, #fff7ed 0%, #f0fdf4 50%, #f0f9ff 100%);
}

.hero-bg-grid {
  position: absolute;
  width: 100%;
  height: 100%;
  background-image:
    linear-gradient(rgba(0, 121, 52, 0.03) 1px, transparent 1px),
    linear-gradient(90deg, rgba(0, 121, 52, 0.03) 1px, transparent 1px);
  background-size: 60px 60px;
  mask-image: radial-gradient(ellipse at center, black 40%, transparent 80%);
}

.hero-content {
  position: relative;
  z-index: 10;
  text-align: center;
  max-width: 900px;
  padding: 0 20px;
}

.pulse-tag {
  margin-bottom: 30px;
  border: none;
  font-weight: bold;
  font-size: 14px;
  padding: 12px 24px;
  box-shadow: 0 4px 15px rgba(226, 35, 26, 0.2);
  animation: pulse 2s infinite;
}

.tag-icon {
  margin-right: 6px;
  vertical-align: middle;
}

@keyframes pulse {
  0%, 100% { box-shadow: 0 4px 15px rgba(226, 35, 26, 0.2); }
  50% { box-shadow: 0 4px 25px rgba(226, 35, 26, 0.4); }
}

.hero-title {
  font-size: 64px;
  font-weight: 900;
  line-height: 1.1;
  margin-bottom: 25px;
  color: #002d14;
  letter-spacing: -2px;
}

.title-line {
  display: block;
}

.hero-subtitle {
  font-size: 22px;
  color: #4b5563;
  line-height: 1.6;
  margin-bottom: 45px;
  font-weight: 400;
  max-width: 700px;
  margin-left: auto;
  margin-right: auto;
}

.hero-actions {
  display: flex;
  gap: 20px;
  justify-content: center;
  align-items: center;
}

.cta-btn {
  font-size: 18px;
  padding: 24px 40px;
  font-weight: 700;
  transition: all 0.3s ease;
  height: auto;
}

.primary-cta {
  background: linear-gradient(135deg, #e2231a, #ff4444) !important;
  border: none;
  box-shadow: 0 10px 30px rgba(226, 35, 26, 0.25);
}

.primary-cta:hover {
  transform: translateY(-3px);
  box-shadow: 0 20px 40px rgba(226, 35, 26, 0.35);
}

.orange-cta {
  background: linear-gradient(135deg, #ff7900, #ff9944) !important;
  box-shadow: 0 10px 30px rgba(255, 121, 0, 0.25);
}

.secondary-cta {
  background: transparent !important;
  border: 2px solid #111 !important;
  color: #111 !important;
}

.secondary-cta:hover {
  background: #111 !important;
  color: #fff !important;
  transform: translateY(-3px);
}

/* 滚动指示器 */
.scroll-indicator {
  position: absolute;
  bottom: 40px;
  left: 50%;
  transform: translateX(-50%);
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 10px;
  cursor: pointer;
  z-index: 10;
  animation: bounce 2s infinite;
}

.mouse {
  width: 26px;
  height: 40px;
  border: 2px solid #111;
  border-radius: 13px;
  position: relative;
}

.wheel {
  width: 4px;
  height: 8px;
  background: #111;
  border-radius: 2px;
  position: absolute;
  top: 8px;
  left: 50%;
  transform: translateX(-50%);
  animation: scroll-wheel 1.5s infinite;
}

.scroll-text {
  font-size: 12px;
  font-weight: 600;
  letter-spacing: 1px;
  text-transform: uppercase;
  color: #666;
}

@keyframes bounce {
  0%, 20%, 50%, 80%, 100% { transform: translateX(-50%) translateY(0); }
  40% { transform: translateX(-50%) translateY(-10px); }
  60% { transform: translateX(-50%) translateY(-5px); }
}

@keyframes scroll-wheel {
  0% { opacity: 1; top: 8px; }
  100% { opacity: 0; top: 20px; }
}

/* ======== 动画类 ======== */
.animate-fade-in-down {
  animation: fadeInDown 0.8s ease-out forwards;
  opacity: 0;
}

.animate-fade-in-up {
  animation: fadeInUp 0.8s ease-out forwards;
  opacity: 0;
}

@keyframes fadeInDown {
  from { opacity: 0; transform: translateY(-30px); }
  to { opacity: 1; transform: translateY(0); }
}

@keyframes fadeInUp {
  from { opacity: 0; transform: translateY(30px); }
  to { opacity: 1; transform: translateY(0); }
}

/* ======== 数据统计区 ======== */
.stats-section {
  background: #fff;
  padding: 60px 20px;
  position: relative;
  z-index: 2;
}

.stats-container {
  max-width: 1200px;
  margin: 0 auto;
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 40px;
}

.stat-item {
  text-align: center;
  padding: 30px;
  border-radius: 20px;
  background: #fafafa;
  border: 1px solid #f0f0f0;
  transition: all 0.3s ease;
  animation: fadeInUp 0.6s ease-out forwards;
  opacity: 0;
}

.stat-item:hover {
  transform: translateY(-5px);
  box-shadow: 0 20px 40px rgba(0,0,0,0.06);
  border-color: #e0e0e0;
}

.stat-icon {
  width: 60px;
  height: 60px;
  border-radius: 16px;
  display: flex;
  align-items: center;
  justify-content: center;
  margin: 0 auto 16px;
  background: #f0fdf4;
}

.stat-number {
  font-size: 36px;
  font-weight: 900;
  color: #111;
  margin-bottom: 8px;
  letter-spacing: -1px;
}

.stat-label {
  font-size: 15px;
  color: #666;
  font-weight: 500;
}

/* ======== Features 区域 ======== */
.features-section {
  padding: 120px 60px;
  background: #fff;
}

.section-header {
  text-align: center;
  margin-bottom: 80px;
}

.section-tag {
  display: inline-block;
  font-size: 13px;
  font-weight: 800;
  letter-spacing: 3px;
  color: #007934;
  margin-bottom: 16px;
  background: #f0fdf4;
  padding: 8px 16px;
  border-radius: 20px;
}

.section-title {
  font-size: 42px;
  font-weight: 900;
  color: #002d14;
  margin-bottom: 15px;
  letter-spacing: -1px;
}

.title-underline {
  width: 80px;
  height: 5px;
  background: linear-gradient(90deg, #007934, #ff7900);
  margin: 0 auto;
  border-radius: 10px;
}

.section-desc {
  font-size: 18px;
  color: #666;
  margin-top: 20px;
  max-width: 600px;
  margin-left: auto;
  margin-right: auto;
  line-height: 1.6;
}

/* ======== Features 卡片居中 ======== */
.feature-row {
  max-width: 1300px;
  margin: 0 auto;
  justify-content: center !important;
}

.feature-col {
  display: flex;
  justify-content: center;
}

.feature-card {
  padding: 50px 35px;
  border-radius: 24px;
  background: #fff;
  border: 1px solid #f0f0f0;
  transition: all 0.1s ease-out;
  height: 100%;
  box-sizing: border-box;
  text-align: left;
  position: relative;
  overflow: hidden;
  cursor: pointer;
  max-width: 400px;
  width: 100%;
}

.card-shine {
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  opacity: 0;
  transition: opacity 0.3s;
  pointer-events: none;
  z-index: 1;
}

.feature-card:hover {
  box-shadow: 0 30px 60px rgba(0,0,0,0.08);
  border-color: transparent;
}

.icon-box {
  width: 70px;
  height: 70px;
  border-radius: 18px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 32px;
  color: white;
  margin-bottom: 30px;
  position: relative;
  z-index: 2;
  box-shadow: 0 10px 30px rgba(0,0,0,0.1);
}

.feature-card h3 {
  font-size: 22px;
  font-weight: 800;
  margin-bottom: 15px;
  color: #111;
  position: relative;
  z-index: 2;
}

.feature-card p {
  color: #6b7280;
  line-height: 1.7;
  font-size: 16px;
  margin: 0;
  position: relative;
  z-index: 2;
}

/* ======== Features 卡片居中（Flex 版本） ======== */
.feature-row-flex {
  display: flex;
  justify-content: center;
  align-items: stretch;
  gap: 40px;
  max-width: 1300px;
  margin: 0 auto;
  flex-wrap: wrap;
}

.feature-col-flex {
  flex: 1;
  min-width: 300px;
  max-width: 400px;
  display: flex;
  justify-content: center;
}

.feature-card {
  padding: 50px 35px;
  border-radius: 24px;
  background: #fff;
  border: 1px solid #f0f0f0;
  transition: all 0.1s ease-out;
  height: 100%;
  box-sizing: border-box;
  text-align: left;
  position: relative;
  overflow: hidden;
  cursor: pointer;
  width: 100%;
}

/* 响应式：小屏幕时单列 */
@media (max-width: 768px) {
  .feature-col-flex {
    flex: 0 0 100%;
    max-width: 100%;
  }
}

.border-green { border-bottom: 4px solid transparent; }
.border-green:hover { border-bottom: 4px solid #007934; }
.border-orange { border-bottom: 4px solid transparent; }
.border-orange:hover { border-bottom: 4px solid #ff7900; }
.border-red { border-bottom: 4px solid transparent; }
.border-red:hover { border-bottom: 4px solid #e2231a; }

/* ======== 流程展示 ======== */
.process-section {
  padding: 120px 60px;
  background: linear-gradient(180deg, #fff 0%, #fafafa 100%);
}

.process-container {
  max-width: 1200px;
  margin: 0 auto;
  display: flex;
  justify-content: space-between;
  gap: 30px;
  position: relative;
}

.process-step {
  flex: 1;
  text-align: center;
  position: relative;
}

.step-connector {
  position: absolute;
  top: 30px;
  left: 60%;
  width: 80%;
  height: 2px;
}

.connector-line {
  width: 100%;
  height: 100%;
  background: linear-gradient(90deg, #007934, #ff7900);
  opacity: 0.3;
}

.step-number {
  width: 60px;
  height: 60px;
  border-radius: 50%;
  background: #fff;
  border: 2px solid #e0e0e0;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 20px;
  font-weight: 800;
  color: #111;
  margin: 0 auto 24px;
  position: relative;
  z-index: 2;
  transition: all 0.3s ease;
}

.process-step:hover .step-number {
  background: #111;
  color: #fff;
  border-color: #111;
  transform: scale(1.1);
}

.step-card {
  background: #fff;
  padding: 40px 30px;
  border-radius: 20px;
  border: 1px solid #f0f0f0;
  transition: all 0.3s ease;
}

.step-card:hover {
  box-shadow: 0 20px 40px rgba(0,0,0,0.06);
  transform: translateY(-5px);
}

.step-icon {
  width: 50px;
  height: 50px;
  border-radius: 14px;
  display: flex;
  align-items: center;
  justify-content: center;
  color: white;
  margin: 0 auto 20px;
  font-size: 20px;
}

.step-card h4 {
  font-size: 18px;
  font-weight: 800;
  margin-bottom: 12px;
  color: #111;
}

.step-card p {
  font-size: 15px;
  color: #666;
  line-height: 1.6;
  margin: 0;
}

/* ======== About 区域 ======== */
.about-section {
  padding: 120px 60px;
  background-color: #111111;
  color: #fff;
  position: relative;
  overflow: hidden;
}

.about-section::before {
  content: '';
  position: absolute;
  top: -50%;
  right: -10%;
  width: 600px;
  height: 600px;
  background: radial-gradient(circle, rgba(0, 121, 52, 0.1) 0%, transparent 70%);
  border-radius: 50%;
}

.about-content {
  max-width: 1100px;
  margin: 0 auto;
  position: relative;
  z-index: 2;
}

.about-header {
  text-align: center;
  margin-bottom: 60px;
}

.team-tag {
  color: #007934;
  font-weight: 800;
  letter-spacing: 3px;
  font-size: 13px;
}

.about-content h2 {
  font-size: 42px;
  color: #fff;
  margin: 15px 0;
  font-weight: 900;
  letter-spacing: -1px;
}

.accent-line {
  width: 50px;
  height: 3px;
  background: linear-gradient(90deg, #e2231a, #ff7900);
  margin: 0 auto;
}

.about-grid {
  display: grid;
  grid-template-columns: 2fr 1fr;
  gap: 60px;
  align-items: center;
  margin-bottom: 50px;
}

.about-text {
  text-align: left;
}

.about-desc {
  line-height: 1.8;
  color: #9ca3af;
  font-size: 17px;
  margin-bottom: 24px;
  font-weight: 300;
}

.about-desc strong {
  color: #fff;
  font-weight: 700;
}

.about-visual {
  display: flex;
  justify-content: center;
  align-items: center;
}

.sdg-badge {
  width: 180px;
  height: 180px;
  border-radius: 50%;
  background: linear-gradient(135deg, #007934, #00b34a);
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  box-shadow: 0 20px 60px rgba(0, 121, 52, 0.3);
  position: relative;
}

.sdg-badge::after {
  content: '';
  position: absolute;
  inset: -10px;
  border-radius: 50%;
  border: 2px solid rgba(0, 121, 52, 0.3);
  animation: ripple 2s infinite;
}

@keyframes ripple {
  0% { transform: scale(1); opacity: 1; }
  100% { transform: scale(1.2); opacity: 0; }
}

.sdg-number {
  font-size: 64px;
  font-weight: 900;
  color: #fff;
  line-height: 1;
}

.sdg-label {
  font-size: 18px;
  font-weight: 700;
  color: rgba(255,255,255,0.9);
  letter-spacing: 2px;
}

.tech-stack-row {
  display: flex;
  justify-content: center;
  gap: 15px;
  flex-wrap: wrap;
  margin-top: 50px;
}

.tech-item {
  background: #1a1a1a;
  padding: 14px 28px;
  border-radius: 30px;
  border: 1px solid #333;
  color: #007934;
  font-weight: 700;
  font-size: 14px;
  transition: all 0.3s ease;
  cursor: default;
}

.tech-item:hover {
  border-color: #007934;
  box-shadow: 0 0 20px rgba(0, 121, 52, 0.2);
  transform: translateY(-2px);
  color: #fff;
  background: #007934;
}

/* ======== CTA 区域 ======== */
.cta-section {
  padding: 100px 60px;
  background: linear-gradient(135deg, #f0fdf4 0%, #fff7ed 100%);
  text-align: center;
  position: relative;
  overflow: hidden;
}

.cta-bg-pattern {
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background-image: radial-gradient(#007934 1px, transparent 1px);
  background-size: 40px 40px;
  opacity: 0.05;
}

.cta-content {
  position: relative;
  z-index: 2;
  max-width: 700px;
  margin: 0 auto;
}

.cta-content h2 {
  font-size: 40px;
  font-weight: 900;
  color: #002d14;
  margin-bottom: 20px;
  letter-spacing: -1px;
}

.cta-content p {
  font-size: 18px;
  color: #666;
  margin-bottom: 40px;
  line-height: 1.6;
}

/* ======== Footer ======== */
.footer {
  background: #0a0a0a;
  color: #9ca3af;
  position: relative;
}

.footer-content {
  padding: 60px 60px 40px;
  max-width: 1200px;
  margin: 0 auto;
}

.footer-main {
  display: grid;
  grid-template-columns: 2fr 1fr;
  gap: 60px;
  margin-bottom: 40px;
}

.footer-brand {
  max-width: 400px;
}

.footer-logo {
  font-size: 36px;
  font-weight: 900;
  font-style: italic;
  margin-bottom: 12px;
}

.brand-footer {
  font-size: 18px;
  font-weight: 600;
  margin-bottom: 12px;
  color: #fff;
  letter-spacing: 1px;
}

.brand-desc {
  font-size: 15px;
  color: #666;
  line-height: 1.6;
}

.footer-links {
  display: flex;
  gap: 60px;
}

.link-group h4 {
  color: #fff;
  font-size: 14px;
  font-weight: 700;
  margin-bottom: 20px;
  letter-spacing: 1px;
}

.link-group a {
  display: block;
  color: #9ca3af;
  text-decoration: none;
  margin-bottom: 12px;
  font-size: 14px;
  transition: color 0.3s;
  cursor: pointer;
}

.link-group a:hover {
  color: #007934;
}

.footer-divider {
  width: 100%;
  height: 1px;
  background: rgba(255,255,255,0.1);
  margin-bottom: 30px;
}

.footer-bottom {
  text-align: center;
}

.copyright {
  font-size: 14px;
  margin-bottom: 8px;
  color: #666;
}

.disclaimer {
  font-size: 12px;
  opacity: 0.5;
}

/* ======== 响应式 ======== */
@media (max-width: 1024px) {
  .stats-container {
    grid-template-columns: repeat(2, 1fr);
  }

  .process-container {
    flex-wrap: wrap;
  }

  .process-step {
    flex: 0 0 calc(50% - 15px);
  }

  .step-connector {
    display: none;
  }

  .about-grid {
    grid-template-columns: 1fr;
  }

  .footer-main {
    grid-template-columns: 1fr;
  }
}

@media (max-width: 768px) {
  .hero-title {
    font-size: 36px;
    letter-spacing: -1px;
  }

  .hero-subtitle {
    font-size: 18px;
  }

  .hero-actions {
    flex-direction: column;
  }

  .features-section {
    padding: 80px 25px;
  }

  .section-title {
    font-size: 32px;
  }

  .stats-container {
    grid-template-columns: repeat(2, 1fr);
    gap: 20px;
  }

  .process-step {
    flex: 0 0 100%;
  }

  .about-section {
    padding: 80px 25px;
  }

  .cta-section {
    padding: 80px 25px;
  }

  .footer-content {
    padding: 40px 25px;
  }

  .footer-links {
    flex-direction: column;
    gap: 30px;
  }
}
</style>
