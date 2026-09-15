export function HomeBackground() {
  return (
    <div className="home-bg" aria-hidden="true">
      {/* 金色光晕基底 */}
      <div className="home-glow" />

      {/* 三颗淡音符点缀 */}
      <div className="home-notes">
        <span className="note n1">♪</span>
        <span className="note n2">♫</span>
        <span className="note n3">♩</span>
      </div>
    </div>
  );
}
