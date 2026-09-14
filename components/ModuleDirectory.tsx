'use client';

import Link from 'next/link';
import { useState, useMemo } from 'react';
import { modules, categoryLabels, categoryColors, type ModuleInfo } from '@/lib/modules';

export function ModuleDirectory() {
  const [query, setQuery] = useState('');
  const [activeTag, setActiveTag] = useState<string | null>(null);

  const allTags = useMemo(() => {
    const map = new Map<string, number>();
    for (const m of modules) {
      for (const t of m.tags) {
        map.set(t, (map.get(t) ?? 0) + 1);
      }
    }
    return Array.from(map, ([label, count]) => ({ label, count }));
  }, []);

  const primaryLabels = ['免费', '付费', '福利', 'UI', '跨服', 'HUD'];
  const primaryTags = primaryLabels
    .map((label) => allTags.find((t) => t.label === label))
    .filter((t): t is NonNullable<typeof t> => Boolean(t));
  const moreTags = allTags.filter((t) => !primaryLabels.includes(t.label));

  const filtered = useMemo(() => {
    const q = query.trim().toLowerCase();
    return modules
      .filter((m) => {
        const hitQuery =
          !q ||
          m.name.toLowerCase().includes(q) ||
          m.displayName.toLowerCase().includes(q) ||
          m.description.toLowerCase().includes(q);
        const hitTag = !activeTag || m.tags.includes(activeTag);
        return hitQuery && hitTag;
      })
      .sort((a, b) => a.name.localeCompare(b.name, 'en', { sensitivity: 'base' }));
  }, [query, activeTag]);

  return (
    <div>
      {/* 搜索 + 标签筛选 */}
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
          <FilterChip label="全部" active={activeTag === null} onClick={() => setActiveTag(null)} />
          {primaryTags.map((tag) => (
            <FilterChip
              key={tag.label}
              label={tag.label}
              count={tag.count}
              active={activeTag === tag.label}
              onClick={() => setActiveTag(tag.label)}
            />
          ))}
          {moreTags.length > 0 && (
            <details className="group">
              <summary className="inline-flex cursor-pointer list-none items-center gap-1.5 rounded-full border border-white/[0.08] px-3 py-1.5 text-xs font-medium text-neutral-400 transition-colors hover:border-[#9b8cd8]/30 hover:text-neutral-200">
                更多 · {moreTags.length}
                <svg className="h-3.5 w-3.5 transition-transform group-open:rotate-180" viewBox="0 0 24 24" fill="none" stroke="currentColor">
                  <path d="m6 9 6 6 6-6" strokeWidth={2} strokeLinecap="round" strokeLinejoin="round" />
                </svg>
              </summary>
              <div className="mt-2 flex flex-wrap gap-2 rounded-xl border border-white/[0.08] bg-white/[0.04] p-3 backdrop-blur-md">
                {moreTags.map((tag) => (
                  <FilterChip
                    key={tag.label}
                    label={tag.label}
                    count={tag.count}
                    active={activeTag === tag.label}
                    onClick={() => setActiveTag(tag.label)}
                  />
                ))}
              </div>
            </details>
          )}
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
        <div className="mt-4 flex flex-wrap gap-1.5">
          {m.tags.map((tag) => (
            <span
              key={tag}
              className="rounded-md bg-white/[0.04] px-2 py-0.5 text-[10px] font-medium text-neutral-500"
            >
              {tag}
            </span>
          ))}
        </div>
      </div>
    </Link>
  );
}
