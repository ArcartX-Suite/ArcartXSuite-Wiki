<template>
  <div class="home-bg">
    <!-- 深色调音乐厅背景 -->
    <div class="stage-gradient"></div>

    <!-- 五线谱 -->
    <div class="staff staff-1" aria-hidden="true"></div>
    <div class="staff staff-2" aria-hidden="true"></div>
    <div class="staff staff-3" aria-hidden="true"></div>

    <!-- 暖金聚光 -->
    <div class="spotlight spotlight--gold"></div>
    <div class="spotlight spotlight--purple"></div>

    <!-- 漂浮音符 -->
    <div class="notes" aria-hidden="true">
      <span class="note">♪</span>
      <span class="note">♫</span>
      <span class="note">♩</span>
      <span class="note">♬</span>
      <span class="note">♭</span>
      <span class="note">♯</span>
      <span class="note">𝄞</span>
      <span class="note">♪</span>
    </div>

    <!-- 流动的旋律线 -->
    <div class="wave-line wave-1" aria-hidden="true"></div>
    <div class="wave-line wave-2" aria-hidden="true"></div>
    <div class="wave-line wave-3" aria-hidden="true"></div>
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

/* 舞台渐变：深色音乐厅氛围 */
.stage-gradient {
  position: absolute;
  inset: 0;
  background:
    radial-gradient(ellipse at 50% 0%, rgba(140, 110, 60, 0.08) 0%, transparent 50%),
    radial-gradient(ellipse at 80% 20%, rgba(103, 80, 164, 0.06) 0%, transparent 45%),
    radial-gradient(ellipse at 20% 80%, rgba(60, 40, 90, 0.05) 0%, transparent 45%),
    #0b0c15;
}

:root:not(.dark) .stage-gradient {
  background:
    radial-gradient(ellipse at 50% 0%, rgba(140, 110, 60, 0.06) 0%, transparent 50%),
    radial-gradient(ellipse at 80% 20%, rgba(103, 80, 164, 0.04) 0%, transparent 45%),
    #f8f6f3;
}

/* 五线谱 */
.staff {
  position: absolute;
  left: 0;
  right: 0;
  height: 70px;
  background-image: repeating-linear-gradient(
    to bottom,
    transparent,
    transparent 13px,
    rgba(180, 160, 120, 0.06) 13px,
    rgba(180, 160, 120, 0.06) 14px
  );
  opacity: 0.5;
  animation: staffDrift 18s ease-in-out infinite alternate;
}

.staff-1 { top: 15%; }
.staff-2 { top: 55%; animation-delay: -7s; animation-duration: 22s; }
.staff-3 { top: 82%; animation-delay: -12s; animation-duration: 25s; }

:root:not(.dark) .staff {
  background-image: repeating-linear-gradient(
    to bottom,
    transparent,
    transparent 13px,
    rgba(103, 80, 164, 0.04) 13px,
    rgba(103, 80, 164, 0.04) 14px
  );
}

@keyframes staffDrift {
  from { transform: translateX(-20px); opacity: 0.3; }
  to   { transform: translateX(20px);  opacity: 0.6; }
}

/* 暖金聚光 */
.spotlight {
  position: absolute;
  width: 600px;
  height: 600px;
  border-radius: 999px;
  filter: blur(80px);
  animation: spotDrift 12s ease-in-out infinite alternate;
}

.spotlight--gold {
  left: 45%;
  top: -10%;
  background: radial-gradient(circle, rgba(212, 175, 110, 0.12) 0%, transparent 70%);
}

.spotlight--purple {
  right: 5%;
  bottom: -5%;
  background: radial-gradient(circle, rgba(103, 80, 164, 0.10) 0%, transparent 70%);
  animation-delay: -5s;
}

:root:not(.dark) .spotlight--gold {
  background: radial-gradient(circle, rgba(212, 175, 110, 0.07) 0%, transparent 70%);
}

:root:not(.dark) .spotlight--purple {
  background: radial-gradient(circle, rgba(103, 80, 164, 0.05) 0%, transparent 70%);
}

@keyframes spotDrift {
  from { transform: translate3d(0, 0, 0) scale(1); }
  to   { transform: translate3d(-30px, 20px, 0) scale(1.12); }
}

/* 漂浮音符 */
.notes .note {
  position: absolute;
  font-size: 18px;
  color: rgba(212, 175, 110, 0.20);
  animation: noteFloat ease-in-out infinite alternate;
  user-select: none;
}

:root:not(.dark) .notes .note {
  color: rgba(103, 80, 164, 0.12);
}

.notes .note:nth-child(1) { left: 10%; top: 18%;  font-size: 20px; animation-duration: 6s;  animation-delay: -1s; }
.notes .note:nth-child(2) { left: 25%; top: 38%;  font-size: 14px; animation-duration: 8s;  animation-delay: -3s; }
.notes .note:nth-child(3) { left: 42%; top: 12%;  font-size: 22px; animation-duration: 7s;  animation-delay: -5s; }
.notes .note:nth-child(4) { left: 58%; top: 55%;  font-size: 16px; animation-duration: 9s;  animation-delay: -2s; }
.notes .note:nth-child(5) { left: 72%; top: 25%;  font-size: 18px; animation-duration: 7.5s; animation-delay: -4s; }
.notes .note:nth-child(6) { left: 85%; top: 48%;  font-size: 15px; animation-duration: 8s;  animation-delay: -6s; }
.notes .note:nth-child(7) { left: 15%; top: 62%;  font-size: 26px; animation-duration: 10s; animation-delay: -7s; }
.notes .note:nth-child(8) { left: 50%; top: 78%;  font-size: 17px; animation-duration: 6.5s; animation-delay: -8s; }

@keyframes noteFloat {
  from { transform: translateY(0) rotate(-5deg);  opacity: 0.15; }
  to   { transform: translateY(-16px) rotate(5deg); opacity: 0.35; }
}

/* 旋律波浪线 */
.wave-line {
  position: absolute;
  left: 0;
  right: 0;
  height: 2px;
  border-radius: 1px;
  background: linear-gradient(90deg,
    transparent 0%,
    rgba(180, 160, 120, 0.08) 20%,
    rgba(103, 80, 164, 0.12) 50%,
    rgba(180, 160, 120, 0.08) 80%,
    transparent 100%
  );
  animation: waveMove 10s ease-in-out infinite alternate;
}

.wave-1 { top: 30%;  animation-delay: -2s; }
.wave-2 { top: 50%;  animation-delay: -5s; animation-duration: 14s; }
.wave-3 { top: 68%;  animation-delay: -8s; animation-duration: 12s; }

:root:not(.dark) .wave-line {
  background: linear-gradient(90deg,
    transparent 0%,
    rgba(103, 80, 164, 0.05) 20%,
    rgba(212, 175, 110, 0.06) 50%,
    rgba(103, 80, 164, 0.05) 80%,
    transparent 100%
  );
}

@keyframes waveMove {
  from { transform: translateX(-60px); opacity: 0.4; }
  to   { transform: translateX(60px);  opacity: 0.8; }
}
</style>
