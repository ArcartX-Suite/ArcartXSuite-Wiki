'use client';

import Link from 'next/link';
import { useState, useMemo } from 'react';
import { modules, categoryLabels, categoryColors, type ModuleInfo } from '@/lib/modules';

type FilterCategory = 'all' | ModuleInfo['category'];

export function ModuleDirectory() {
  const [query, setQuery] = useState('');
  const [activeCategory, setActiveCategory] = useState<FilterCategory>('all');

  const filtered = useMemo(() => {
    const q = query.trim().toLowerCase();
    return modules
      .filter((m) => {
        const hitQuery =
          !q ||
          m.name.toLowerCase().includes(q) ||
          m.displayName.toLowerCase().includes(q) ||
          m.description.toLowerCase().includes(q);
        const hitCategory = activeCategory === 'all' || m.category === activeCategory;
        return hitQuery && hitCategory;
      })
      .sort((a, b) => a.name.localeCompare(b.name, 'en', { sensitivity: 'base' }));
  }, [query, activeCategory]);

  const categoryChips: { key: FilterCategory; label: string; count: number }[] = [
    { key: 'all', label: '全部', count: modules.length },
    ...(['free', 'paid', 'bonus'] as const).map((key) => ({
      key,
      label: categoryLabels[key],
      count: modules.filter((m) => m.category === key).length,
    })),
  ];

  return (
    <div>
      {/* 搜索 + 分类筛选 */}
      <div className="mb-8 rounded-2xl border border-white/[0.08] bg-white/[0.04] p-4 backdrop-blur-md">
        <div className="flex flex-col gap-3 sm:flex-row sm:items-center">
          <label className="relative block w-full sm:max-w-md">
            <span className="sr-only">搜索模块</span>
            <svg
              className="pointer-events-none absolute left-3.5 top-1/2 h-4 w-4 -translate-y-1/2 text-neutral-500"
              viewBox="0 0 24 24"
              fill="none"
              stroke="currentColor"
              aria-hidden
            >
              <circle cx="11" cy="11" r="7" strokeWidth={2} />
              <path d="M21 21l-4.3-4.3" strokeWidth={2} strokeLinecap="round" />
            </svg>
            <input
              type="search"
              value={query}
              onChange={(e) => setQuery(e.target.value)}
              placeholder="搜索模块名称或功能…"
              maxLength={40}
              className="h-11 w-full rounded-xl border border-white/[0.08] bg-white/[0.03] pl-10 pr-4 text-sm text-neutral-200 outline-none transition-colors placeholder:text-neutral-500 focus:border-[#9b8cd8]/40 focus:ring-2 focus:ring-[#9b8cd8]/10"
            />
          </label>
          <p className="text-xs text-neutral-500" aria-live="polite">
            显示 <span className="font-semibold tabular-nums text-[#9b8cd8]">{filtered.length}</span> / {modules.length} 个模块
          </p>
        </div>

        <div className="mt-3 flex flex-wrap items-center gap-2 border-t border-white/[0.06] pt-3">
          {categoryChips.map((chip) => (
            <FilterChip
              key={chip.key}
              label={chip.label}
              count={chip.count}
              active={activeCategory === chip.key}
              onClick={() => setActiveCategory(chip.key)}
            />
          ))}
        </div>
      </div>

      {/* 模块卡片网格 */}
      {filtered.length > 0 ? (
        <div className="grid grid-cols-1 gap-4 sm:grid-cols-2 lg:grid-cols-3 xl:grid-cols-4">
          {filtered.map((m) => (
            <ModuleCard key={m.slug} module={m} />
          ))}
        </div>
      ) : (
        <div className="rounded-2xl border border-white/[0.08] p-12 text-center text-sm text-neutral-500">
          没有匹配的模块，换个关键词或筛选试试。
        </div>
      )}
    </div>
  );
}

function FilterChip({
  label,
  count,
  active,
  onClick,
}: {
  label: string;
  count?: number;
  active: boolean;
  onClick: () => void;
}) {
  return (
    <button
      type="button"
      onClick={onClick}
      aria-pressed={active}
      className={`inline-flex shrink-0 items-center gap-1.5 rounded-full border px-3 py-1.5 text-xs font-medium transition-colors ${
        active
          ? 'border-[#6750a4] bg-[#6750a4] text-white'
          : 'border-white/[0.08] text-neutral-400 hover:border-[#9b8cd8]/30 hover:text-neutral-200'
      }`}
    >
      {label}
      {count !== undefined && (
        <span className={`tabular-nums ${active ? 'text-white/70' : 'text-neutral-400'}`}>
          {count}
        </span>
      )}
    </button>
  );
}

function ModuleCard({ module: m }: { module: ModuleInfo }) {
  return (
    <Link
      href={`/docs/${m.slug}`}
      className="group relative block overflow-hidden rounded-xl border border-white/[0.08] bg-white/[0.04] p-5 backdrop-blur-md transition-all duration-300 hover:-translate-y-1 hover:border-[#9b8cd8]/30 hover:bg-white/[0.07] hover:shadow-xl hover:shadow-[#6750a4]/10"
    >
      {/* 左侧色条 */}
      <div
        className="absolute inset-y-0 left-0 w-[3px] transition-all duration-300 group-hover:w-1"
        style={{ backgroundColor: m.color }}
      />
      <div className="relative pl-2">
        <div className="flex items-start justify-between">
          <div>
            <h3 className="text-base font-semibold text-neutral-100 transition-colors group-hover:text-[#9b8cd8]">
              {m.displayName}
            </h3>
            <p className="mt-0.5 text-xs text-neutral-500">{m.name}</p>
          </div>
          <span
            className="rounded-full border px-2 py-0.5 text-[10px] font-semibold"
            style={{
              borderColor: `${categoryColors[m.category]}30`,
              backgroundColor: `${categoryColors[m.category]}10`,
              color: categoryColors[m.category],
            }}
          >
            {categoryLabels[m.category]}
          </span>
        </div>
        <p className="mt-3 text-sm leading-relaxed text-neutral-400">
          {m.description}
        </p>
      </div>
    </Link>
  );
}
