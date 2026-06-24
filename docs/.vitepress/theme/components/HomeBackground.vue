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
      <defs>
        <linearGradient id="swGrad" x1="0%" y1="0%" x2="100%" y2="0%">
          <stop offset="0%" stop-color="#d4af6e" stop-opacity="0"/>
          <stop offset="30%" stop-color="#d4af6e" stop-opacity="0.7"/>
          <stop offset="70%" stop-color="#9b8cd8" stop-opacity="0.7"/>
          <stop offset="100%" stop-color="#9b8cd8" stop-opacity="0"/>
        </linearGradient>
      </defs>
      <path d="M0,200 C240,120 480,280 720,200 S1200,120 1440,200" stroke="url(#swGrad)" fill="none" />
      <path d="M0,200 C240,150 480,250 720,200 S1200,150 1440,200" stroke="url(#swGrad)" fill="none" />
      <path d="M0,200 C240,80 480,320 720,200 S1200,80 1440,200" stroke="url(#swGrad)" fill="none" />
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

/* ========== 基底 ========== */
.stage-base {
  position: absolute;
  inset: 0;
  background:
    radial-gradient(ellipse 80% 60% at 50% 10%, rgba(212,175,110,0.20) 0%, transparent 55%),
    radial-gradient(ellipse 60% 50% at 70% 20%, rgba(155,140,216,0.16) 0%, transparent 50%),
    linear-gradient(180deg, #12131f 0%, #0c0d18 100%);
}

:root:not(.dark) .stage-base {
  background:
    radial-gradient(ellipse 80% 60% at 50% 10%, rgba(212,175,110,0.22) 0%, transparent 55%),
    radial-gradient(ellipse 60% 50% at 70% 20%, rgba(155,140,216,0.15) 0%, transparent 50%),
    linear-gradient(180deg, #faf8f5 0%, #f0ece6 100%);
}

/* ========== 暗角 ========== */
.stage-vignette {
  position: absolute;
  inset: 0;
  background: radial-gradient(ellipse 90% 90% at 50% 45%, transparent 0%, rgba(0,0,0,0.28) 100%);
}

:root:not(.dark) .stage-vignette {
  background: radial-gradient(ellipse 90% 90% at 50% 45%, transparent 0%, rgba(60,40,20,0.12) 100%);
}

/* ========== 穹顶弧线 ========== */
.dome {
  position: absolute;
  left: 50%;
  transform: translateX(-50%);
  border-radius: 50% 50% 0 0 / 100% 100% 0 0;
  border-top: 2px solid #b89a5e;
  border-left: 1px solid rgba(184, 154, 94, 0.20);
  border-right: 1px solid rgba(184, 154, 94, 0.20);
  border-bottom: none;
  box-shadow: 0 -4px 30px rgba(212, 175, 110, 0.35), 0 -8px 60px rgba(212, 175, 110, 0.12);
}

:root:not(.dark) .dome {
  border-top: 2px solid #7a5fb8;
  border-left: 1px solid rgba(122, 95, 184, 0.25);
  border-right: 1px solid rgba(122, 95, 184, 0.25);
  box-shadow: 0 -4px 20px rgba(155, 140, 216, 0.18);
}

.dome-1 { width: 140vw; height: 70vh; top: -15vh; animation: domePulse 14s ease-in-out infinite alternate; }
.dome-2 { width: 120vw; height: 60vh; top: -10vh; animation: domePulse 18s ease-in-out infinite alternate; animation-delay: -4s; border-top-color: #c4a86a; box-shadow: 0 -4px 20px rgba(196, 168, 106, 0.30), 0 -8px 50px rgba(196, 168, 106, 0.10); }
.dome-3 { width: 100vw; height: 50vh; top: -5vh; animation: domePulse 22s ease-in-out infinite alternate; animation-delay: -8s; border-top-color: #9b8cd8; box-shadow: 0 -4px 20px rgba(155, 140, 216, 0.25), 0 -8px 50px rgba(155, 140, 216, 0.10); }

:root:not(.dark) .dome-2 { border-top-color: #8a70c8; box-shadow: 0 -4px 16px rgba(138, 112, 200, 0.15); }
:root:not(.dark) .dome-3 { border-top-color: #a898e8; box-shadow: 0 -4px 16px rgba(168, 152, 232, 0.15); }

@keyframes domePulse {
  from { opacity: 0.6; transform: translateX(-50%) scale(1); }
  to   { opacity: 1; transform: translateX(-50%) scale(1.03); }
}

/* ========== 聚光灯 ========== */
.spotlight-main {
  position: absolute;
  width: 500px; height: 500px;
  left: 50%; top: 5%;
  transform: translateX(-50%);
  border-radius: 50%;
  background: radial-gradient(circle, rgba(212, 175, 110, 0.65) 0%, rgba(155, 140, 216, 0.30) 40%, transparent 70%);
  filter: blur(40px);
  animation: spotPulse 10s ease-in-out infinite alternate;
}

:root:not(.dark) .spotlight-main {
  background: radial-gradient(circle, rgba(212, 175, 110, 0.55) 0%, rgba(155, 140, 216, 0.28) 40%, transparent 70%);
}

.spotlight-side {
  position: absolute;
  width: 350px; height: 350px;
  right: 5%; top: 25%;
  border-radius: 50%;
  background: radial-gradient(circle, rgba(155, 140, 216, 0.50) 0%, transparent 60%);
  filter: blur(50px);
  animation: spotPulse 12s ease-in-out infinite alternate;
  animation-delay: -3s;
}

:root:not(.dark) .spotlight-side {
  background: radial-gradient(circle, rgba(155, 140, 216, 0.45) 0%, transparent 60%);
}

@keyframes spotPulse {
  from { transform: scale(1) translateX(-50%); opacity: 0.6; }
  to   { transform: scale(1.15) translateX(-50%); opacity: 1; }
}

/* ========== 声波线 ========== */
.soundwave {
  position: absolute;
  left: 0; right: 0;
  bottom: 22%;
  height: 200px; width: 100%;
  opacity: 0.70;
  overflow: visible;
}

:root:not(.dark) .soundwave {
  opacity: 0.80;
}

.soundwave path {
  stroke: #c4a86a;
  stroke-width: 2.5;
  fill: none;
  animation: waveShift 8s ease-in-out infinite alternate;
  filter: drop-shadow(0 0 12px rgba(196, 168, 106, 0.8)) drop-shadow(0 0 24px rgba(196, 168, 106, 0.4));
}

:root:not(.dark) .soundwave path {
  stroke: #7a5fb8;
  filter: drop-shadow(0 0 10px rgba(122, 95, 184, 0.5));
}

.soundwave path:nth-child(2) { animation-delay: -2s; stroke-opacity: 0.9; }
.soundwave path:nth-child(3) { animation-delay: -4s; stroke-opacity: 0.6; }

@keyframes waveShift {
  from { transform: translateX(-30px); }
  to   { transform: translateX(30px); }
}

/* ========== 音符 ========== */
.notes .note {
  position: absolute;
  font-size: 24px;
  color: #e0be78;
  text-shadow: 0 0 16px rgba(224, 190, 120, 0.9), 0 0 32px rgba(224, 190, 120, 0.5), 0 0 60px rgba(224, 190, 120, 0.2);
  animation: noteFloat ease-in-out infinite alternate;
  user-select: none;
  font-family: Georgia, serif;
}

:root:not(.dark) .notes .note {
  color: #5a3f8a;
  text-shadow: 0 0 12px rgba(122, 95, 184, 0.5), 0 0 24px rgba(122, 95, 184, 0.25);
}

.notes .note:nth-child(1) { left: 10%; top: 24%;  font-size: 26px; animation-duration: 6s;  animation-delay: -1s; }
.notes .note:nth-child(2) { left: 24%; top: 16%;  font-size: 20px; animation-duration: 8s;  animation-delay: -3s; }
.notes .note:nth-child(3) { left: 46%; top: 12%;  font-size: 30px; animation-duration: 7s;  animation-delay: -5s; }
.notes .note:nth-child(4) { left: 64%; top: 30%;  font-size: 22px; animation-duration: 9s;  animation-delay: -2s; }
.notes .note:nth-child(5) { left: 80%; top: 20%;  font-size: 24px; animation-duration: 7.5s; animation-delay: -4s; }
.notes .note:nth-child(6) { left: 90%; top: 38%;  font-size: 18px; animation-duration: 8s;  animation-delay: -6s; }
.notes .note:nth-child(7) { left: 36%; top: 40%;  font-size: 34px; animation-duration: 10s; animation-delay: -7s; }

@keyframes noteFloat {
  from { transform: translateY(0) rotate(-6deg); }
  to   { transform: translateY(-20px) rotate(6deg); }
}

/* ========== Logo 水印 ========== */
.bg-logo {
  position: absolute;
  right: -8%; top: 55%;
  transform: translateY(-50%) rotate(15deg);
  width: 480px; height: 480px;
  opacity: 0.22;
  filter: blur(2px);
  animation: logoDrift 20s ease-in-out infinite alternate;
  pointer-events: none;
  user-select: none;
}

:root:not(.dark) .bg-logo {
  opacity: 0.14;
}

@keyframes logoDrift {
  from { transform: translateY(-50%) rotate(15deg); }
  to   { transform: translateY(-50%) rotate(18deg) translateX(-15px); }
}
</style>
