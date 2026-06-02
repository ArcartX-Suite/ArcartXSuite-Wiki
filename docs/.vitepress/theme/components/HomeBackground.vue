<template>
  <div class="home-bg">
    <!-- Grid overlay -->
    <div class="home-bg-grid"></div>
    <!-- Glowing blobs -->
    <div class="glow glow--purple"></div>
    <div class="glow glow--blue"></div>
    <!-- Cyan particles floating up -->
    <div class="particles" aria-hidden="true">
      <span></span><span></span><span></span><span></span>
      <span></span><span></span><span></span><span></span>
    </div>
  </div>
</template>

<style scoped>
.home-bg {
  position: fixed;
  inset: 0;
  z-index: -1;
  overflow: hidden;
  pointer-events: none;
  background: radial-gradient(circle at 50% 40%, #1b1d31 0%, #070912 60%, #03040a 100%);
}

/* Light mode override */
:root:not(.dark) .home-bg {
  background: radial-gradient(circle at 50% 40%, #f5f3ff 0%, #ede9fe 40%, #fff 100%);
}

/* Dark grid lines */
.home-bg-grid {
  position: absolute;
  inset: 0;
  background-image:
    linear-gradient(rgba(255,255,255,0.025) 1px, transparent 1px),
    linear-gradient(90deg, rgba(255,255,255,0.025) 1px, transparent 1px);
  background-size: 48px 48px;
}

:root:not(.dark) .home-bg-grid {
  background-image:
    linear-gradient(rgba(103,80,164,0.06) 1px, transparent 1px),
    linear-gradient(90deg, rgba(103,80,164,0.06) 1px, transparent 1px);
}

/* Glow blobs */
.glow {
  position: absolute;
  width: 520px;
  height: 520px;
  border-radius: 999px;
  filter: blur(72px);
  opacity: 0.56;
  animation: drift 9s ease-in-out infinite alternate;
}

.glow--purple {
  left: 5%;
  top: 10%;
  background: #6750a4;
}

.glow--blue {
  right: -8%;
  bottom: -8%;
  background: #5b6efb;
  animation-delay: -3s;
}

:root:not(.dark) .glow--purple {
  background: #c4b5fd;
  opacity: 0.35;
}
:root:not(.dark) .glow--blue {
  background: #93c5fd;
  opacity: 0.3;
}

@keyframes drift {
  from { transform: translate3d(0, 0, 0) scale(1); }
  to   { transform: translate3d(38px, -24px, 0) scale(1.08); }
}

/* Cyan square particles floating upward */
.particles span {
  position: absolute;
  width: 6px;
  height: 6px;
  background: rgba(34, 211, 238, 0.55);
  box-shadow: 0 0 12px rgba(34, 211, 238, 0.65);
  animation: floatUp linear infinite;
  opacity: 0;
}

:root:not(.dark) .particles span {
  background: rgba(103, 80, 164, 0.35);
  box-shadow: 0 0 12px rgba(103, 80, 164, 0.25);
}

.particles span:nth-child(1) { left:  9%; bottom:  7%; animation-duration: 7s;   animation-delay: -1s; }
.particles span:nth-child(2) { left: 18%; bottom:  2%; animation-duration: 9s;   animation-delay: -4s; }
.particles span:nth-child(3) { left: 30%; bottom: 11%; animation-duration: 8s;   animation-delay: -6s; }
.particles span:nth-child(4) { left: 48%; bottom:  5%; animation-duration: 10s;  animation-delay: -3s; }
.particles span:nth-child(5) { left: 61%; bottom: 12%; animation-duration: 7.5s; animation-delay: -5s; }
.particles span:nth-child(6) { left: 75%; bottom:  4%; animation-duration: 9.5s; animation-delay: -2s; }
.particles span:nth-child(7) { left: 88%; bottom: 10%; animation-duration: 8.5s; animation-delay: -7s; }
.particles span:nth-child(8) { left: 95%; bottom:  0%; animation-duration: 11s;  animation-delay: -8s; }

@keyframes floatUp {
  0%   { transform: translateY(0) scale(0.8);      opacity: 0; }
  12%  {                                            opacity: 1; }
  82%  {                                            opacity: 0.9; }
  100% { transform: translateY(-100vh) scale(1.35); opacity: 0; }
}
</style>
