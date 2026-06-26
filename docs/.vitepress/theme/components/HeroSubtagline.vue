<template>
  <div class="hero-carousel">
    <p class="carousel-tagline">
      <span>{{ displayTagline }}</span>
      <span class="cursor">|</span>
    </p>
    <p class="carousel-sub">
      <span>{{ displaySub }}</span>
    </p>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, onUnmounted } from 'vue'

const taglines = [
  'Suite — 组曲，亦是套装',
  '26+ 个乐章，一部组曲，一个插件',
  '无需 Java，轻松谱写华丽乐章',
  '模块天然联动，合奏远胜独奏',
  '从登录到击杀，每一帧都由你演奏',
]

const subs = [
  '序曲启程 · 变奏展开 · 华彩交织 · 终章回响',
  '独立运作、各具音色、按需启用',
  '配置文件就是你的乐谱，UI 会自己奏起来',
  '模块自成小调，汇成宏大交响',
  '不只是插件，是服务器玩法的交响组曲',
]

const index = ref(0)
const displayTagline = ref('')
const displaySub = ref('')
let stopped = false

function sleep(ms: number) {
  return new Promise<void>(r => setTimeout(r, ms))
}

async function typeText(text: string, target: 'tagline' | 'sub', speed = 60) {
  for (let i = 0; i <= text.length; i++) {
    if (stopped) return
    if (target === 'tagline') displayTagline.value = text.slice(0, i)
    else displaySub.value = text.slice(0, i)
    await sleep(speed)
  }
}

async function eraseText(target: 'tagline' | 'sub', speed = 30) {
  const current = target === 'tagline' ? displayTagline.value : displaySub.value
  for (let i = current.length; i >= 0; i--) {
    if (stopped) return
    if (target === 'tagline') displayTagline.value = current.slice(0, i)
    else displaySub.value = current.slice(0, i)
    await sleep(speed)
  }
}

async function loop() {
  while (!stopped) {
    const i = index.value
    await typeText(taglines[i], 'tagline')
    await sleep(200)
    await typeText(subs[i], 'sub', 40)
    await sleep(3000)
    await eraseText('sub', 20)
    await eraseText('tagline', 20)
    await sleep(300)
    index.value = (index.value + 1) % taglines.length
  }
}

onMounted(() => {
  stopped = false
  loop()
})

onUnmounted(() => {
  stopped = true
})
</script>

<style scoped>
.hero-carousel {
  margin-top: 8px;
  min-height: 72px;
}

.carousel-tagline {
  font-size: 1.1rem;
  font-weight: 600;
  color: #e2e4eb;
  margin: 0;
  line-height: 1.6;
}

.carousel-sub {
  font-size: 0.95rem;
  color: #a9adbd;
  margin: 6px 0 0;
  line-height: 1.5;
}

.cursor {
  display: inline-block;
  margin-left: 2px;
  font-weight: 300;
  color: var(--vp-c-brand-1);
  animation: blink 1s step-end infinite;
}

@keyframes blink {
  0%, 100% { opacity: 1; }
  50%      { opacity: 0; }
}
</style>
