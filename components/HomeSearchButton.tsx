'use client';

import { Search } from 'lucide-react';
import { useSearchContext } from 'fumadocs-ui/provider';

export function HomeSearchButton() {
  const { setOpenSearch } = useSearchContext();
  return <button type="button" className="home-search-trigger" onClick={() => setOpenSearch(true)} aria-label="搜索全部文档"><Search size={15} aria-hidden="true" /><span>搜索文档</span><kbd>Ctrl K</kbd></button>;
}
