export function HomeBackground() {
  return (
    <div className="home-bg">
      {/* 基底 */}
      <div className="stage-base" />

      {/* 舞台暗角 */}
      <div className="stage-vignette" />

      {/* 顶部穹顶弧光 */}
      <div className="dome dome-1" aria-hidden="true" />
      <div className="dome dome-2" aria-hidden="true" />
      <div className="dome dome-3" aria-hidden="true" />

      {/* 主聚光灯 */}
      <div className="spotlight-main" aria-hidden="true" />
      <div className="spotlight-side" aria-hidden="true" />

      {/* 声波弧线 */}
      <svg className="soundwave" viewBox="0 0 1440 400" preserveAspectRatio="none" aria-hidden="true">
        <defs>
          <linearGradient id="swGrad" x1="0%" y1="0%" x2="100%" y2="0%">
            <stop offset="0%" stopColor="#d4af6e" stopOpacity="0" />
            <stop offset="30%" stopColor="#d4af6e" stopOpacity="0.7" />
            <stop offset="70%" stopColor="#9b8cd8" stopOpacity="0.7" />
            <stop offset="100%" stopColor="#9b8cd8" stopOpacity="0" />
          </linearGradient>
        </defs>
        <path d="M0,200 C240,120 480,280 720,200 S1200,120 1440,200" stroke="url(#swGrad)" fill="none" />
        <path d="M0,200 C240,150 480,250 720,200 S1200,150 1440,200" stroke="url(#swGrad)" fill="none" />
        <path d="M0,200 C240,80 480,320 720,200 S1200,80 1440,200" stroke="url(#swGrad)" fill="none" />
      </svg>

      {/* 音符 */}
      <div className="notes" aria-hidden="true">
        <span className="note n1">♪</span>
        <span className="note n2">♫</span>
        <span className="note n3">♩</span>
        <span className="note n4">♬</span>
        <span className="note n5">♭</span>
        <span className="note n6">♯</span>
        <span className="note n7">𝄞</span>
      </div>

      {/* Logo 水印 */}
      <img src={`${process.env.NEXT_PUBLIC_BASE_PATH ?? ''}/logo.svg`} className="bg-logo" alt="" aria-hidden="true" />
    </div>
  );
}
