'use client';

import { useEffect, useMemo, useState, type ReactNode } from 'react';
import { RootProvider } from 'fumadocs-ui/provider';
import { SearchDialog, type SharedProps } from 'fumadocs-ui/components/dialog/search';
import { searchWiki, type SearchPage } from '@/lib/wiki-search';

let indexPromise: Promise<SearchPage[]> | undefined;

function WikiSearch(props: SharedProps) {
  const [search, setSearch] = useState('');
  const [pages, setPages] = useState<SearchPage[]>([]);
  const [failed, setFailed] = useState(false);
  useEffect(() => {
    if (!props.open) return;
    let active = true;
    if (!indexPromise) indexPromise = fetch(`${process.env.NEXT_PUBLIC_BASE_PATH ?? ''}/search-index.json`).then((response) => {
      if (!response.ok) throw new Error('文档索引加载失败');
      return response.json() as Promise<SearchPage[]>;
    }).catch((error) => { indexPromise = undefined; throw error; });
    setFailed(false);
    indexPromise.then((data) => { if (active) setPages(data); }).catch(() => { if (active) setFailed(true); });
    return () => { active = false; };
  }, [props.open]);
  const results = useMemo(() => searchWiki(pages, search), [pages, search]);
  return <SearchDialog {...props} search={search} onSearchChange={setSearch} results={results} isLoading={props.open && !pages.length && !failed} footer={failed ? <p role="alert" className="p-3 text-sm">搜索索引加载失败，请关闭后重试，或使用侧栏查找模块。</p> : <p className="p-3 text-xs text-fd-muted-foreground">支持中文功能、命令与 API 名称 · ↑↓ 选择 · Enter 打开 · Esc 关闭</p>} />;
}

export function WikiProvider({ children }: { children: ReactNode }) {
  return <RootProvider theme={{ enabled: false }} search={{ SearchDialog: WikiSearch }}>{children}</RootProvider>;
}
