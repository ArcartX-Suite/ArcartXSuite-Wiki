<template>
  <div class="home-bg">
    <!-- 基底 -->
    <div class="stage-base"></div>

    <!-- 舞台暗角（从中心向外变暗） -->
    <div class="stage-vignette"></div>

    <!-- 顶部穹顶弧光 -->
    <div class="dome dome-1" aria-hidden="true"></div>
    <div class="dome dome-2" aria-hidden="true"></div>
    <div class="dome dome-3" aria-hidden="true"></div>

    <!-- 主聚光灯 -->
    <div class="spotlight-main" aria-hidden="true"></div>
    <div class="spotlight-side" aria-hidden="true"></div>

    <!-- 声波弧线（装饰性） -->
    <svg class="soundwave" viewBox="0 0 1440 400" preserveAspectRatio="none" aria-hidden="true">
      <path d="M0,200 C240,120 480,280 720,200 S1200,120 1440,200" fill="none" />
      <path d="M0,200 C240,150 480,250 720,200 S1200,150 1440,200" fill="none" />
      <path d="M0,200 C240,80 480,320 720,200 S1200,80 1440,200" fill="none" />
    </svg>

    <!-- 音符 -->
    <div class="notes" aria-hidden="true">
      <span class="note">♪</span>
      <span class="note">♫</span>
      <span class="note">♩</span>
      <span class="note">♬</span>
      <span class="note">♭</span>
      <span class="note">♯</span>
      <span class="note">𝄞</span>
    </div>

    <!-- Logo 水印 -->
    <img src="/logo.svg" class="bg-logo" alt="" aria-hidden="true" />
  </div>
</template>

<style scoped>
.home-bg {
  position: fixed;
  inset: 0;
  z-index: -1;
  overflow: hidden;
  pointer-events: none;
}

/* 深色基底 */
.stage-base {
  position: absolute;
  inset: 0;
  background:
    radial-gradient(ellipse 80% 60% at 50% 10%, rgba(212,175,110,0.10) 0%, transparent 55%),
    radial-gradient(ellipse 60% 50% at 70% 20%, rgba(155,140,216,0.08) 0%, transparent 50%),
    #0c0d18;
}

:root:not(.dark) .stage-base {
  background:
    radial-gradient(ellipse 80% 60% at 50% 10%, rgba(212,175,110,0.07) 0%, transparent 55%),
    radial-gradient(ellipse 60% 50% at 70% 20%, rgba(155,140,216,0.05) 0%, transparent 50%),
    #f3f1ed;
}

/* 暗角效果 */
.stage-vignette {
  position: absolute;
  inset: 0;
  background: radial-gradient(ellipse 90% 90% at 50% 45%, transparent 0%, rgba(0,0,0,0.45) 100%);
}

:root:not(.dark) .stage-vignette {
  background: radial-gradient(ellipse 90% 90% at 50% 45%, transparent 0%, rgba(0,0,0,0.06) 100%);
}

/* 穹顶弧线 */
.dome {
  position: absolute;
  left: 50%;
  transform: translateX(-50%);
  border-radius: 50% 50% 0 0 / 100% 100% 0 0;
  border-top: 1px solid rgba(212, 175, 110, 0.18);
  border-left: 1px solid rgba(212, 175, 110, 0.06);
  border-right: 1px solid rgba(212, 175, 110, 0.06);
  border-bottom: none;
}

:root:not(.dark) .dome {
  border-top: 1px solid rgba(103, 80, 164, 0.14);
  border-left: 1px solid rgba(103, 80, 164, 0.05);
  border-right: 1px solid rgba(103, 80, 164, 0.05);
}

.dome-1 {
  width: 140vw;
  height: 70vh;
  top: -15vh;
  animation: domePulse 14s ease-in-out infinite alternate;
}

.dome-2 {
  width: 120vw;
  height: 60vh;
  top: -10vh;
  animation: domePulse 18s ease-in-out infinite alternate;
  animation-delay: -4s;
  border-top-color: rgba(212, 175, 110, 0.08);
}

.dome-3 {
  width: 100vw;
  height: 50vh;
  top: -5vh;
  animation: domePulse 22s ease-in-out infinite alternate;
  animation-delay: -8s;
  border-top-color: rgba(155, 140, 216, 0.10);
}

@keyframes domePulse {
  from { opacity: 0.4; transform: translateX(-50%) scale(1); }
  to   { opacity: 0.8; transform: translateX(-50%) scale(1.03); }
}

/* 主聚光灯 */
.spotlight-main {
  position: absolute;
  width: 500px;
  height: 500px;
  left: 50%;
  top: 5%;
  transform: translateX(-50%);
  border-radius: 50%;
  background: radial-gradient(circle, rgba(212, 175, 110, 0.18) 0%, rgba(155, 140, 216, 0.08) 40%, transparent 70%);
  filter: blur(40px);
  animation: spotPulse 10s ease-in-out infinite alternate;
}

:root:not(.dark) .spotlight-main {
  background: radial-gradient(circle, rgba(212, 175, 110, 0.12) 0%, rgba(155, 140, 216, 0.06) 40%, transparent 70%);
}

.spotlight-side {
  position: absolute;
  width: 350px;
  height: 350px;
  right: 5%;
  top: 25%;
  border-radius: 50%;
  background: radial-gradient(circle, rgba(155, 140, 216, 0.10) 0%, transparent 60%);
  filter: blur(50px);
  animation: spotPulse 12s ease-in-out infinite alternate;
  animation-delay: -3s;
}

:root:not(.dark) .spotlight-side {
  background: radial-gradient(circle, rgba(155, 140, 216, 0.07) 0%, transparent 60%);
}

@keyframes spotPulse {
  from { transform: scale(1) translateX(-50%); opacity: 0.6; }
  to   { transform: scale(1.15) translateX(-50%); opacity: 1; }
}

/* 声波弧线 SVG */
.soundwave {
  position: absolute;
  left: 0;
  right: 0;
  bottom: 20%;
  height: 200px;
  width: 100%;
  opacity: 0.12;
  overflow: visible;
}

.soundwave path {
  stroke: url(#waveGrad);
  stroke-width: 1.5;
  fill: none;
  animation: waveShift 8s ease-in-out infinite alternate;
}

.soundwave path:nth-child(2) {
  animation-delay: -2s;
  stroke-opacity: 0.7;
}

.soundwave path:nth-child(3) {
  animation-delay: -4s;
  stroke-opacity: 0.4;
}

@keyframes waveShift {
  from { transform: translateX(-30px); }
  to   { transform: translateX(30px); }
}

/* 音符 */
.notes .note {
  position: absolute;
  font-size: 20px;
  color: rgba(212, 175, 110, 0.35);
  animation: noteFloat ease-in-out infinite alternate;
  user-select: none;
  font-family: Georgia, serif;
}

:root:not(.dark) .notes .note {
  color: rgba(103, 80, 164, 0.22);
}

.notes .note:nth-child(1) { left: 8%;  top: 22%;  font-size: 22px; animation-duration: 6s;  animation-delay: -1s; }
.notes .note:nth-child(2) { left: 22%; top: 15%;  font-size: 16px; animation-duration: 8s;  animation-delay: -3s; }
.notes .note:nth-child(3) { left: 45%; top: 10%;  font-size: 26px; animation-duration: 7s;  animation-delay: -5s; }
.notes .note:nth-child(4) { left: 62%; top: 28%;  font-size: 18px; animation-duration: 9s;  animation-delay: -2s; }
.notes .note:nth-child(5) { left: 78%; top: 18%;  font-size: 20px; animation-duration: 7.5s; animation-delay: -4s; }
.notes .note:nth-child(6) { left: 88%; top: 35%;  font-size: 14px; animation-duration: 8s;  animation-delay: -6s; }
.notes .note:nth-child(7) { left: 35%; top: 38%;  font-size: 30px; animation-duration: 10s; animation-delay: -7s; }

@keyframes noteFloat {
  from { transform: translateY(0) rotate(-6deg);  opacity: 0.25; }
  to   { transform: translateY(-20px) rotate(6deg); opacity: 0.55; }
}

/* Logo 水印 */
.bg-logo {
  position: absolute;
  right: -8%;
  top: 55%;
  transform: translateY(-50%) rotate(15deg);
  width: 480px;
  height: 480px;
  opacity: 0.06;
  filter: blur(3px);
  animation: logoDrift 20s ease-in-out infinite alternate;
  pointer-events: none;
  user-select: none;
}

:root:not(.dark) .bg-logo {
  opacity: 0.04;
}

@keyframes logoDrift {
  from { transform: translateY(-50%) rotate(15deg); }
  to   { transform: translateY(-50%) rotate(18deg) translateX(-15px); }
}
</style>
