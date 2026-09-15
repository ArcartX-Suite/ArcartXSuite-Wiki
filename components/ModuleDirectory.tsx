'use client';

import Link from 'next/link';
import { useEffect, useMemo, useRef, useState, type CSSProperties } from 'react';
import { modules, type ModuleInfo } from '@/lib/modules';

type FilterCategory = 'all' | ModuleInfo['category'];
type CatKey = 'free' | 'paid' | 'bonus';

const CATS: { key: FilterCategory; label: string }[] = [
  { key: 'all', label: '全部乐章' },
  { key: 'free', label: '序曲 · 免费' },
  { key: 'paid', label: '华彩 · 付费' },
  { key: 'bonus', label: '彩蛋 · 福利' },
];

const MOV: Record<CatKey, string> = { free: 'I', paid: 'II', bonus: 'III' };
const CAT_LABEL: Record<CatKey, string> = {
  free: '序曲 · 免费模块',
  paid: '华彩 · 付费模块',
  bonus: '彩蛋 · 福利模块',
};
const CAT_TAG: Record<CatKey, string> = { free: '免费', paid: '付费', bonus: '福利' };
const GROUP_KEYS: CatKey[] = ['free', 'paid', 'bonus'];

const mcVar = (color: string) => ({ '--mc': color }) as CSSProperties;

/** 轮播切换：大长方形专辑卡 */
function Carousel({ items, autoplay, noOffset }: { items: ModuleInfo[]; autoplay: boolean; noOffset: number }) {
  const [idx, setIdx] = useState(0);
  const wrapRef = useRef<HTMLDivElement>(null);
  const trackRef = useRef<HTMLDivElement>(null);
  const timerRef = useRef<ReturnType<typeof setInterval> | null>(null);
  const lastWheelRef = useRef(0);
  const total = items.length;

  // 数据变化（筛选/搜索）时回到第一张
  useEffect(() => {
    setIdx(0);
  }, [items]);

  const step = () => {
    const track = trackRef.current;
    const first = track?.firstElementChild as HTMLElement | null;
    return first ? first.getBoundingClientRect().width + 24 : 0;
  };

  // 平移
  useEffect(() => {
    const track = trackRef.current;
    if (track && total > 0) track.style.transform = `translateX(${-idx * step()}px)`;
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [idx, total]);

  // 窗口缩放后重算
  useEffect(() => {
    const onResize = () => {
      const track = trackRef.current;
      if (track && total > 0) track.style.transform = `translateX(${-idx * step()}px)`;
    };
    window.addEventListener('resize', onResize);
    return () => window.removeEventListener('resize', onResize);
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [idx, total]);

  const stop = () => {
    if (timerRef.current) {
      clearInterval(timerRef.current);
      timerRef.current = null;
    }
  };

  const play = () => {
    if (!autoplay || total <= 1 || timerRef.current) return;
    timerRef.current = setInterval(() => setIdx((i) => (i + 1) % total), 5000);
  };

  useEffect(() => {
    play();
    return stop;
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [autoplay, total]);

  // 滚轮切换（原生 wheel，非 passive，可 preventDefault）
  useEffect(() => {
    const wrap = wrapRef.current;
    if (!wrap) return;
    const onWheel = (e: WheelEvent) => {
      if (Math.abs(e.deltaY) < 8) return;
      e.preventDefault();
      const now = Date.now();
      if (now - lastWheelRef.current < 600) return;
      lastWheelRef.current = now;
      setIdx((i) => (i + (e.deltaY > 0 ? 1 : -1) + total) % total);
    };
    wrap.addEventListener('wheel', onWheel, { passive: false });
    return () => wrap.removeEventListener('wheel', onWheel);
  }, [total]);

  if (total <= 1) {
    return (
      <div className="carousel">
        <div className="c-track" ref={trackRef}>
          {items.map((m) => (
            <CoverCard key={m.slug} m={m} no={noOffset + 1} />
          ))}
        </div>
      </div>
    );
  }

  return (
    <div className="carousel" ref={wrapRef} onMouseEnter={stop} onMouseLeave={play}>
      <button type="button" className="c-nav prev" aria-label="上一张" onClick={() => setIdx((i) => (i - 1 + total) % total)}>
        ‹
      </button>
      <div className="c-track" ref={trackRef}>
        {items.map((m, i) => (
          <CoverCard key={m.slug} m={m} no={noOffset + i + 1} />
        ))}
      </div>
      <button type="button" className="c-nav next" aria-label="下一张" onClick={() => setIdx((i) => (i + 1) % total)}>
        ›
      </button>
      <span className="c-counter">
        <b>{idx + 1}</b> / {total}
      </span>
    </div>
  );
}

function CoverCard({ m, no }: { m: ModuleInfo; no: number }) {
  return (
    <Link href={`/docs/${m.slug}`} className="c-card" style={mcVar(m.color)}>
      <div className="c-art">
        <span className="no">{String(no).padStart(2, '0')}</span>
        <span className="nm">{m.displayName}</span>
      </div>
      <div className="c-body">
        <div className="meta">
          <span>{m.name}</span>
          <span className={`tag ${m.category}`}>{CAT_TAG[m.category]}</span>
        </div>
        <h4>{m.displayName}</h4>
        <p>{m.description}</p>
        <span className="go">查看文档 →</span>
      </div>
    </Link>
  );
}

function CompactCard({ m, no }: { m: ModuleInfo; no: number }) {
  return (
    <Link href={`/docs/${m.slug}`} className="g-card" style={mcVar(m.color)}>
      <span className="g-art">{String(no).padStart(2, '0')}</span>
      <span className="g-body">
        <span className="row1">
          <b>{m.displayName}</b>
          <span className={`tag ${m.category}`} style={{ marginLeft: 'auto' }}>
            {CAT_TAG[m.category]}
          </span>
        </span>
        <span className="g-desc">{m.description}</span>
      </span>
    </Link>
  );
}

export function ModuleDirectory() {
  const [query, setQuery] = useState('');
  const [cat, setCat] = useState<FilterCategory>('all');
  const [expanded, setExpanded] = useState<Record<CatKey, boolean>>({
    free: false,
    paid: false,
    bonus: false,
  });

  const q = query.trim().toLowerCase();
  const searching = q.length > 0;

  const filtered = useMemo(
    () =>
      modules.filter((m) => {
        const hitCat = cat === 'all' || m.category === cat;
        const hitQuery =
          !q ||
          m.name.toLowerCase().includes(q) ||
          m.displayName.toLowerCase().includes(q) ||
          m.description.toLowerCase().includes(q);
        return hitCat && hitQuery;
      }),
    [cat, q],
  );

  const groups = useMemo(() => {
    const g: Partial<Record<CatKey, ModuleInfo[]>> = {};
    filtered.forEach((m) => {
      (g[m.category] ??= []).push(m);
    });
    return g;
  }, [filtered]);

  const allExpanded = GROUP_KEYS.every((k) => expanded[k]);
  const groupKeys = GROUP_KEYS.filter((k) => (groups[k]?.length ?? 0) > 0);

  // 全局曲目编号：按渲染顺序累计偏移（免费 01-12 / 付费 13-24 / 彩蛋 25-29）
  let running = 0;

  return (
    <div>
      <div className="home-chips">
        {CATS.map((c) => {
          const n = c.key === 'all' ? modules.length : modules.filter((m) => m.category === c.key).length;
          return (
            <button
              key={c.key}
              type="button"
              className={`home-chip ${cat === c.key ? 'on' : ''}`}
              aria-pressed={cat === c.key}
              onClick={() => setCat(c.key)}
            >
              {c.label} {n}
            </button>
          );
        })}
      </div>

      <div className="home-search">
        <input
          type="search"
          value={query}
          onChange={(e) => setQuery(e.target.value)}
          placeholder="搜索乐章名或功能…"
          maxLength={40}
        />
      </div>

      {!searching && (
        <div style={{ display: 'flex', justifyContent: 'center', marginBottom: 48 }}>
          <button
            type="button"
            className="home-toggle"
            onClick={() => {
              const next = !allExpanded;
              setExpanded({ free: next, paid: next, bonus: next });
            }}
          >
            {allExpanded ? '全部收起 ↑' : '全部展开 ↓'}
          </button>
        </div>
      )}

      {filtered.length === 0 ? (
        <div className="home-empty">没有匹配的乐章，换个关键词试试。</div>
      ) : (
        groupKeys.map((key) => {
          const items = groups[key]!;
          const offset = running;
          running += items.length;
          const isExpanded = searching || expanded[key];
          return (
            <div key={key} className="home-cat-block">
              <div className="home-cat-head">
                <div className="home-cat-title">
                  <span className="home-mov">movement {MOV[key]}</span>
                  <h3>{CAT_LABEL[key]}</h3>
                  <span className="cnt">{items.length} 首</span>
                </div>
                {!searching && (
                  <button
                    type="button"
                    className="home-toggle"
                    onClick={() => setExpanded((s) => ({ ...s, [key]: !s[key] }))}
                  >
                    {isExpanded ? '收起 ↑' : '全部展开 ↓'}
                  </button>
                )}
              </div>
              {isExpanded ? (
                <div className="home-grid">
                  {items.map((m, i) => (
                    <CompactCard key={m.slug} m={m} no={offset + i + 1} />
                  ))}
                </div>
              ) : (
                <Carousel items={items} autoplay noOffset={offset} />
              )}
            </div>
          );
        })
      )}
    </div>
  );
}
