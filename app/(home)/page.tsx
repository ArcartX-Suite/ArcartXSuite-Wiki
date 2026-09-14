import Link from 'next/link';
import { ModuleDirectory } from '@/components/ModuleDirectory';
import { HomeBackground } from '@/components/HomeBackground';
import { HeroSubtagline } from '@/components/HeroSubtagline';

const heroActions = [
  { text: '快速开始', href: '/docs/guide', variant: 'brand' as const },
  { text: '模块开发', href: '/docs/developer', variant: 'alt' as const },
];

const stats = [
  { number: '29', label: '乐章' },
];

export default function HomePage() {
  return (
    <main className="relative text-neutral-200">
      <HomeBackground />

      {/* ============ Hero ============ */}
      <section className="relative z-10 px-6 pt-28 pb-12 md:pt-36 md:pb-16 lg:px-12">
        <div className="mx-auto max-w-4xl">
          <h1 className="bg-gradient-to-r from-white via-[#bfc6ff] to-[#22d3ee] bg-clip-text text-4xl font-black leading-[1.15] tracking-tight text-transparent drop-shadow-[0_0_60px_rgba(91,110,251,0.25)] md:text-6xl">
            Suite
          </h1>

          {/* 打字机轮播副标题 */}
          <HeroSubtagline />

          <p className="mt-6 text-base text-[#c6c8d3]">
            面向 ArcartX 客户端的 Minecraft 服务器插件套件，统一 UI 体验，模块间深度联动
          </p>

          <div className="mt-8 flex flex-wrap gap-3">
            {heroActions.map((action) => (
              <HeroButton key={action.text} {...action} />
            ))}
          </div>
        </div>
      </section>

      {/* ============ 模块目录 ============ */}
      <section id="module-directory" className="relative z-10 mx-auto max-w-7xl px-6 py-16 md:py-20 lg:px-12 scroll-mt-20">
        <div className="mb-10">
          <h2 className="text-2xl font-bold text-white md:text-3xl">模块目录</h2>
          <p className="mt-2 text-sm text-neutral-400">
            29 个模块如同 29 个乐章，每一个独立运作、各具音色、按需启用
          </p>
        </div>
        <ModuleDirectory />
      </section>

      {/* ============ 统计条 ============ */}
      <section className="relative z-10 px-6 py-12 lg:px-12">
        <div className="mx-auto flex max-w-4xl flex-wrap justify-center gap-12 md:gap-16">
          {stats.map((s) => (
            <div key={s.label} className="text-center">
              <div className="bg-gradient-to-br from-[#6750a4] to-[#5b6efb] bg-clip-text text-3xl font-bold text-transparent md:text-4xl">
                {s.number}
              </div>
              <div className="mt-1 text-sm text-neutral-400">{s.label}</div>
            </div>
          ))}
        </div>
      </section>

      {/* ============ 底部 ============ */}
      <footer className="relative z-10 border-t border-white/[0.06]">
        <div className="mx-auto flex max-w-7xl flex-col items-center gap-4 px-6 py-10 sm:flex-row sm:justify-between lg:px-12">
          <p className="text-sm text-neutral-500">
            Suite · 面向 ArcartX 客户端的全场景核心套件
          </p>
          <div className="flex gap-6 text-sm">
            <a href="https://arcartx.com" target="_blank" rel="noopener noreferrer" className="text-neutral-400 transition-colors hover:text-[#9b8cd8]">
              官方社区
            </a>
            <a href="https://github.com/ArcartX-Suite/ArcartXSuite-Wiki" target="_blank" rel="noopener noreferrer" className="text-neutral-400 transition-colors hover:text-[#9b8cd8]">
              GitHub
            </a>
          </div>
        </div>
      </footer>
    </main>
  );
}

function HeroButton({
  text,
  href,
  variant,
  external,
}: {
  text: string;
  href: string;
  variant: 'brand' | 'alt';
  external?: boolean;
}) {
  const className =
    variant === 'brand'
      ? 'rounded-full bg-[#6750a4] px-6 py-2.5 text-sm font-semibold text-white transition-colors hover:bg-[#7c6bbf]'
      : 'rounded-full border border-white/15 bg-white/[0.08] px-6 py-2.5 text-sm font-medium text-neutral-200 transition-all hover:border-[#9b8cd8]/40 hover:bg-white/14';

  if (external) {
    return (
      <a href={href} target="_blank" rel="noopener noreferrer" className={className}>
        {text}
      </a>
    );
  }
  return (
    <Link href={href} className={className}>
      {text}
    </Link>
  );
}
