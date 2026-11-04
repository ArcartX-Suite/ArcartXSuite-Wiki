'use client';

import type { PageTree } from 'fumadocs-core/server';
import { SidebarItem, SidebarSeparator } from 'fumadocs-ui/layouts/docs/sidebar';
import { ChevronDown } from 'lucide-react';
import { usePathname } from 'next/navigation';
import { useEffect, useState } from 'react';

function normalizePath(path: string) {
  return path.replace(/\/$/, '');
}

function containsPage(node: PageTree.Node, path: string): boolean {
  if (node.type === 'page') return normalizePath(node.url) === path;
  return node.type === 'folder' && (
    (node.index !== undefined && normalizePath(node.index.url) === path) ||
    node.children.some((child) => containsPage(child, path))
  );
}

export function WikiSidebarItem({ item }: { item: PageTree.Item }) {
  const path = normalizePath(usePathname());
  return (
    <SidebarItem href={item.url} external={item.external} icon={item.icon}
      aria-current={normalizePath(item.url) === path ? 'page' : undefined}>
      {item.name}
    </SidebarItem>
  );
}

export function WikiSidebarFolder({ item }: { item: PageTree.Folder }) {
  const path = normalizePath(usePathname());
  const active = containsPage(item, path);
  const key = 'suite-wiki:folder:' + (item.index?.url ?? String(item.name));
  const [open, setOpen] = useState(active);

  useEffect(() => {
    if (active) sessionStorage.setItem(key, 'open');
    setOpen(active || sessionStorage.getItem(key) === 'open');
  }, [active, key, path]);

  function toggle() {
    const next = !open;
    setOpen(next);
    sessionStorage.setItem(key, next ? 'open' : 'closed');
  }

  const arrow = <ChevronDown size={16} className={open ? '' : '-rotate-90'} aria-hidden="true" />;
  return (
    <div>
      {item.index ? (
        <div className="wiki-sidebar-folder-index">
          <WikiSidebarItem item={item.index} />
          <button type="button" aria-label={String(item.name)} aria-expanded={open} onClick={toggle}>{arrow}</button>
        </div>
      ) : (
        <button type="button" className="wiki-sidebar-folder-toggle" aria-expanded={open} onClick={toggle}>
          {item.icon}{item.name}{arrow}
        </button>
      )}
      {open && (
        <div className="wiki-sidebar-folder-children">
          {item.children.map((child, index) => {
            if (child.type === 'folder') return <WikiSidebarFolder key={index} item={child} />;
            if (child.type === 'separator') return <SidebarSeparator key={index}>{child.name}</SidebarSeparator>;
            return <WikiSidebarItem key={child.url} item={child} />;
          })}
        </div>
      )}
    </div>
  );
}
