'use client';

import { useEffect, useRef, useState } from 'react';

const taglines = [
  'Suite — 组曲，亦是套装',
  '29 个乐章，一部组曲，一个插件',
  '无需 Java，轻松谱写华丽乐章',
  '模块天然联动，合奏远胜独奏',
  '从登录到击杀，每一帧都由你演奏',
];

const subs = [
  '序曲启程 · 变奏展开 · 华彩交织 · 终章回响',
  '独立运作、各具音色、按需启用',
  '配置文件就是你的乐谱，UI 会自己奏起来',
  '模块自成小调，汇成宏大交响',
  '不只是插件，是服务器玩法的交响组曲',
];

function sleep(ms: number) {
  return new Promise<void>((r) => setTimeout(r, ms));
}

export function HeroSubtagline() {
  const [tagline, setTagline] = useState('');
  const [sub, setSub] = useState('');
  const stoppedRef = useRef(false);

  useEffect(() => {
    stoppedRef.current = false;
    let idx = 0;

    async function typeText(text: string, target: 'tagline' | 'sub', speed: number) {
      for (let i = 0; i <= text.length; i++) {
        if (stoppedRef.current) return;
        if (target === 'tagline') setTagline(text.slice(0, i));
        else setSub(text.slice(0, i));
        await sleep(speed);
      }
    }

    async function eraseText(target: 'tagline' | 'sub', speed: number) {
      const current = target === 'tagline' ? tagline : sub;
      for (let i = current.length; i >= 0; i--) {
        if (stoppedRef.current) return;
        if (target === 'tagline') setTagline(current.slice(0, i));
        else setSub(current.slice(0, i));
        await sleep(speed);
      }
    }

    async function loop() {
      while (!stoppedRef.current) {
        const i = idx;
        await typeText(taglines[i], 'tagline', 60);
        if (stoppedRef.current) return;
        await sleep(200);
        await typeText(subs[i], 'sub', 40);
        if (stoppedRef.current) return;
        await sleep(3000);
        await eraseText('sub', 20);
        await eraseText('tagline', 20);
        await sleep(300);
        idx = (idx + 1) % taglines.length;
      }
    }

    loop();
    return () => {
      stoppedRef.current = true;
    };
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, []);

  return (
    <div className="mt-2 min-h-[72px]">
      <p className="m-0 text-lg font-semibold leading-relaxed text-[#e2e4eb]">
        <span>{tagline}</span>
        <span className="ml-0.5 inline-block font-light text-[#9b8cd8] animate-pulse">|</span>
      </p>
      <p className="m-0 mt-1.5 text-sm leading-relaxed text-[#a9adbd]">
        <span>{sub}</span>
      </p>
    </div>
  );
}
