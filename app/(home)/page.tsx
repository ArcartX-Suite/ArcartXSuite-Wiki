import Image from 'next/image';
import Link from 'next/link';
import { ArrowDown, ArrowRight, ArrowUpRight, BookOpen, Code2, SlidersHorizontal, Terminal } from 'lucide-react';
import { ModuleDirectory } from '@/components/ModuleDirectory';
import { HomeSearchButton } from '@/components/HomeSearchButton';
import { modules, taskGroups } from '@/lib/modules';

const assetBase = process.env.NEXT_PUBLIC_BASE_PATH ?? '';
const firstSteps = [
  { number: '01', icon: BookOpen, title: '了解本体使用', text: '安装、绑定模块，完成主配置与验证。', href: '/docs/guide/first-run', label: '本体使用' },
  { number: '02', icon: SlidersHorizontal, title: '掌握通用配置', text: '使用选区、邮件派发、奖励与其他通用功能。', href: '/docs/guide/shared-functions', label: '通用配置' },
  { number: '03', icon: Terminal, title: '开发插件桥接', text: '通过公开 API 接入你自己的插件。', href: '/docs/api', label: '开发者内容' },
];

export default function HomePage() {
  return <main className="wiki-home" id="main-content">
    <header className="wiki-header"><div className="wiki-header-inner">
      <Link href="/" className="wiki-brand" aria-label="Suite 使用文档首页"><Image src={`${assetBase}/logo.svg`} width={42} height={42} alt="" /><span>Suite</span><span className="brand-label">WIKI</span></Link>
      <nav aria-label="主导航"><Link href="/docs/guide">使用指南</Link><a href="#modules">模块目录</a><Link href="/docs/api">开发 API <ArrowUpRight size={12} aria-hidden="true" /></Link></nav>
      <HomeSearchButton />
    </div></header>

    <section className="wiki-hero" aria-labelledby="suite-title">
      <div className="hero-copy">
        <p className="eyebrow"><span className="eyebrow-line" /> ARCARTX SUITE <span className="eyebrow-slash">/</span> 官方使用文档</p>
        <h1 id="suite-title">SUITE<span className="wordmark-period">.</span></h1>
        <p className="hero-tagline">让每个功能，<span>奏成你的世界。</span></p>
        <p className="hero-description">{modules.length} 个模块，自由编排你的 Minecraft 服务器。<br className="desktop-break" />从功能使用到配置细节，在这里找到每一步。</p>
        <div className="hero-actions"><Link className="primary-link" href="/docs/guide">开始使用 <ArrowRight size={17} /></Link><a className="secondary-link" href="#modules">探索模块 <ArrowDown size={15} /></a></div>
        <div className="hero-facts"><div><strong>{modules.length.toString().padStart(2, '0')}</strong><span>功能模块</span></div><span className="fact-divider" /><div><strong>{Object.keys(taskGroups).length.toString().padStart(2, '0')}</strong><span>使用场景</span></div><span className="fact-divider" /><Link href="/docs/api"><Code2 size={22} /><span>公开开发 API <ArrowUpRight size={12} /></span></Link></div>
      </div>

      <div className="suite-art" aria-hidden="true">
        <div className="art-orbit orbit-one" /><div className="art-orbit orbit-two" />
        <div className="record-disc"><div className="record-label"><span>THE SUITE COLLECTION</span><span className="record-monogram">S</span><span>ARCARTX · VOL. 01</span></div></div>
        <div className="record-sleeve">
          <div className="sleeve-top"><span>ARCARTX</span><span>MODULE COLLECTION</span></div>
          <div className="sleeve-wordmark">Suite</div>
          <div className="sleeve-score"><svg viewBox="0 0 360 180" fill="none"><defs><linearGradient id="score-gold" x1="0" y1="0" x2="360" y2="0"><stop stopColor="#b59bdd" /><stop offset="1" stopColor="#d9bc81" /></linearGradient></defs>{[0, 1, 2, 3, 4].map((i) => <path key={i} d={`M-20 ${70+i*12} C80 ${-35+i*20} 210 ${220-i*20} 380 ${55+i*13}`} stroke="url(#score-gold)" strokeOpacity={0.22+i*0.09} strokeWidth="1" />)}</svg><Image src={`${assetBase}/logo.svg`} width={170} height={170} alt="" priority /></div>
          <div className="sleeve-bottom"><span>独立成章<br /><b>合而为序</b></span><span className="sleeve-edition">{modules.length}<small>MODULES</small></span></div>
        </div>
        <div className="art-caption"><span className="caption-rule" /><span>A COLLECTION OF POSSIBILITIES</span><span className="caption-star">✳</span></div>
      </div>
    </section>

    <section className="getting-started" aria-labelledby="start-title"><div className="start-heading"><p className="eyebrow">THE FIRST MOVEMENT</p><h2 id="start-title">从这里，开启你的组曲。</h2><p>先跑通一个功能，再组合出你的玩法。</p></div><div className="quick-links">{firstSteps.map(({ number, icon: Icon, title, text, href, label }) => <Link href={href} key={href}><div className="quick-link-top"><span>{number} / {label}</span><Icon size={19} strokeWidth={1.5} /></div><h3>{title}<ArrowUpRight size={17} /></h3><p>{text}</p></Link>)}</div></section>

    <section id="modules" className="wiki-modules" aria-labelledby="modules-title"><div className="section-heading"><div><p className="eyebrow">THE COLLECTION <span>/</span> 模块目录</p><h2 id="modules-title">各有其声，<span>自由组合。</span></h2></div><p>按功能选择模块。<br />使用方法、配置项与命令，一步直达。</p></div><ModuleDirectory /></section>

    <section className="developer-entry" aria-labelledby="developer-title"><div><p className="eyebrow">FOR DEVELOPERS</p><h2 id="developer-title">下一段旋律，<br /><span>由你续写。</span></h2><p>通过公开 API，把菜单、称号、邮件与玩法<br className="desktop-break" />接入你自己的插件。</p><Link href="/docs/api" className="developer-link">第三方插件接入 <ArrowUpRight size={17} /></Link></div><div className="code-preview"><div className="code-preview-bar"><span><i /><i /><i /></span><span>打开一个 Suite 菜单</span><Code2 size={15} /></div><pre aria-label="公开菜单 API 调用示例"><code><span className="code-muted">{'// 让你的插件，与 Suite 合奏'}</span>{'\n'}<span className="code-type">MenuOpenable</span> menu ={'\n'}    <span className="code-type">AxsCapabilities</span>.get(MenuOpenable.class);{'\n\n'}<span className="code-keyword">if</span> (menu != <span className="code-keyword">null</span>) {'{'}{'\n'}    menu.openMenu(player, <span className="code-string">{'"example"'}</span>);{'\n'}{'}'}</code></pre><div className="code-preview-foot">公开接口 · 完整示例 · 参数参考 <span>JAVA</span></div></div></section>

    <footer className="wiki-footer"><Link href="/" className="wiki-brand"><Image src={`${assetBase}/logo.svg`} width={32} height={32} alt="" /><span>Suite</span></Link><p>独立成章，合而为序。<span>ArcartX Suite 使用文档</span></p><a href="#main-content">回到序章 ↑</a></footer>
  </main>;
}
