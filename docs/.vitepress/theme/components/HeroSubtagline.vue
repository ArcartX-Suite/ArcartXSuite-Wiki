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
  '服务端写配置，客户端出画面',
  '17 个模块，一套生态，一个插件',
  '不写 Java，照样做出完整玩法',
  '模块之间天然联动，1+1 远大于 2',
  '从登录到击杀，每一帧都由你定义',
]

const subs = [
  'YAML 驱动 · 模块联动 · 开箱即用',
  '一个插件覆盖：追踪 / 战斗 / 播报 / 聊天 / 登录 / 奖励',
  '为每一个 ArcartX 服务器构建完整体验',
  '配置文件就是你的代码，UI 会自己跑起来',
  '不只是插件，是服务器玩法的基础设施',
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
  color: var(--vp-c-text-1);
  margin: 0;
  line-height: 1.6;
}

.carousel-sub {
  font-size: 0.95rem;
  color: var(--vp-c-text-2);
  margin: 6px 0 0;
  line-height: 1.5;
}

.dark .carousel-tagline {
  color: #e2e4eb;
}

.dark .carousel-sub {
  color: #a9adbd;
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
