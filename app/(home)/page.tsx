import Link from 'next/link';
import { ModuleDirectory } from '@/components/ModuleDirectory';
import { modules } from '@/lib/modules';
import stats from '@/lib/stats.json';

const heroStats = [
  { number: String(modules.length), unit: '乐章', caption: '即插即用模块' },
  { number: String(stats.uiComponents), unit: '个 UI', caption: '模块内置 UI 组件' },
  { number: '1', unit: '主调', caption: '唯一硬依赖：ArcartX' },
];

export default function HomePage() {
  return (
    <main className="home-page">

      {/* ============ 导航 ============ */}
      <header className="home-header">
        <div className="home-nav">
          <Link href="/" className="home-logo">
            <span className="home-logo-mark">♪</span>SUITE
          </Link>
          <nav className="home-nav-links" aria-label="主导航">
            <a href="#modules">曲目</a>
            <Link href="/docs/guide">文档</Link>
            <Link href="/docs/developer">开发者</Link>
            <a href="https://github.com/ArcartX-Suite" target="_blank" rel="noopener noreferrer">
              GitHub
            </a>
          </nav>
          <div className="home-nav-right">
            <Link href="/docs/guide" className="home-btn home-btn-brand">
              开始演奏
            </Link>
          </div>
        </div>
      </header>

      {/* ============ Hero ============ */}
      <section className="home-hero">
        <svg
          className="home-wave"
          viewBox="0 0 1200 260"
          preserveAspectRatio="none"
          aria-hidden="true"
        >
          <defs>
            <linearGradient id="homeWaveGrad" x1="0" y1="0" x2="1" y2="0">
              <stop offset="0" stopColor="#c4a86a" stopOpacity="0" />
              <stop offset="0.35" stopColor="#c4a86a" />
              <stop offset="0.65" stopColor="#e0be78" />
              <stop offset="1" stopColor="#e0be78" stopOpacity="0" />
            </linearGradient>
          </defs>
          <path d="M0,190 C120,120 240,60 360,90 S600,210 720,140 S1000,40 1200,110" />
          <path d="M0,190 C120,150 240,110 360,130 S600,210 720,170 S1000,90 1200,140" opacity="0.6" />
        </svg>

        <div className="relative mx-auto max-w-4xl px-6">
          <p className="home-eyebrow">ArcartX Suite</p>
          <h1>
            <span className="home-grad">SUITE</span>
          </h1>
          <p className="home-tagline">
            {modules.length} 个乐章，一部组曲，<b>一个插件</b>。
          </p>
          <div className="home-cta">
            <Link href="/docs/guide" className="home-btn home-btn-brand">
              开始演奏 →
            </Link>
            <a href="#modules" className="home-btn home-btn-ghost">
              翻阅曲目
            </a>
          </div>
          <div className="home-stats">
            {heroStats.map((s) => (
              <div key={s.unit} className="home-stat">
                <b>
                  {s.number}
                  <span className="u">{s.unit}</span>
                </b>
                <span>{s.caption}</span>
              </div>
            ))}
          </div>
        </div>
      </section>

      {/* ============ 模块目录 ============ */}
      <section id="modules" className="home-section">
        <div className="home-sec-head">
          <h2>曲目目录</h2>
          <p>每张封面是一支独立乐章——合奏起来，就是你服务器的主旋律。</p>
        </div>
        <ModuleDirectory />
      </section>

      {/* ============ 页脚 ============ */}
      <footer className="home-footer">
        <div className="home-foot">
          <span>Suite · 面向 ArcartX 客户端的全场景核心套件</span>
          <span style={{ display: 'flex', gap: 18 }}>
            <a href="https://arcartx.com" target="_blank" rel="noopener noreferrer">
              官方社区
            </a>
            <a
              href="https://github.com/ArcartX-Suite/ArcartXSuite-Wiki"
              target="_blank"
              rel="noopener noreferrer"
            >
              GitHub
            </a>
          </span>
        </div>
      </footer>
    </main>
  );
}
