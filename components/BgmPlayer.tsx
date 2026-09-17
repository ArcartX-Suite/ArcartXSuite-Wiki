'use client';

import { useEffect, useRef, useState } from 'react';

/**
 * 全站背景音乐播放开关（2026-09-17 新版：Minecraft 式舒缓氛围，30s 无缝循环）。
 * 默认静音，由用户点击开启；音量压低到 0.5 作氛围背景。
 */
export function BgmPlayer() {
  const audioRef = useRef<HTMLAudioElement | null>(null);
  const [playing, setPlaying] = useState(false);

  useEffect(() => {
    const audio = audioRef.current;
    if (!audio) return;
    audio.volume = 0.5;
    const onPlay = () => setPlaying(true);
    const onPause = () => setPlaying(false);
    audio.addEventListener('play', onPlay);
    audio.addEventListener('pause', onPause);
    return () => {
      audio.removeEventListener('play', onPlay);
      audio.removeEventListener('pause', onPause);
    };
  }, []);

  const toggle = () => {
    const audio = audioRef.current;
    if (!audio) return;
    if (playing) {
      audio.pause();
    } else {
      void audio.play().catch(() => setPlaying(false));
    }
  };

  const src = `${process.env.NEXT_PUBLIC_BASE_PATH ?? ''}/bgm/suite-bgm-loop.mp3`;

  return (
    <>
      <audio ref={audioRef} src={src} loop preload="none" />
      <button
        type="button"
        className={playing ? 'bgm-btn bgm-on' : 'bgm-btn'}
        onClick={toggle}
        aria-label={playing ? '暂停背景音乐' : '播放背景音乐'}
        title={playing ? '暂停背景音乐' : '播放背景音乐'}
      >
        <span aria-hidden="true">♪</span>
      </button>
    </>
  );
}
