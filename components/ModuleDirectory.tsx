'use client';
import { useMemo, useState, type CSSProperties } from 'react';
import Link from 'next/link';
import { ArrowUpRight, Search, X, Palette, PackageOpen, Megaphone, LogIn, Gift, Swords, Workflow, Wand2, Wrench, MapPin, Coffee, MessageSquareText, Award, MessagesSquare, Mail, Warehouse, Navigation, Map, ShoppingBag, Users, PanelsTopLeft, Fish, Sparkles, Flag, List, Pickaxe, Skull, type LucideIcon } from 'lucide-react';
import { modules, taskGroups } from '@/lib/modules';

const moduleIcons: Record<string, LucideIcon> = {
  rgb: Palette, pickup: PackageOpen, announcer: Megaphone, loginview: LogIn,
  onlinerewards: Gift, combateffect: Swords, eventpacket: Workflow, prop: Wand2,
  essentials: Wrench, regions: MapPin, afkreward: Coffee, tooltip: MessageSquareText,
  title: Award, conversation: MessagesSquare, mail: Mail, warehouse: Warehouse,
  questgps: Navigation, map: Map, market: ShoppingBag, qqbot: Users,
  menu: PanelsTopLeft, fishing: Fish, lottery: Sparkles, battlepass: Flag,
  chat: MessagesSquare, tab: List, entitytracker: Skull,
  vanilla: Pickaxe, abyss: Swords,
};
const groupAccents = ['#d8bd86', '#b5a0db', '#9eb9cb', '#b0bea0', '#c9a2a2'];

export function ModuleDirectory() {
  const [query, setQuery] = useState('');
  const [group, setGroup] = useState('全部模块');
  const groups = Object.keys(taskGroups);
  const visible = useMemo(() => modules.filter((module) => {
    const matches = `${module.name} ${module.displayName} ${module.description}`.toLowerCase().includes(query.trim().toLowerCase());
    return matches && (group === '全部模块' || taskGroups[group]?.includes(module.slug));
  }), [query, group]);
  return <div className="module-directory">
    <div className="directory-toolbar"><div className="directory-filters" role="group" aria-label="按用途筛选模块">
      {['全部模块', ...groups].map((name) => <button type="button" key={name} aria-pressed={group === name} onClick={() => setGroup(name)}>{name}</button>)}
    </div><div className="directory-search"><Search size={16} aria-hidden="true" /><label className="sr-only" htmlFor="module-search">搜索模块名称或功能</label><input id="module-search" type="search" placeholder="搜索功能或模块名称" value={query} onChange={(event) => setQuery(event.target.value)} />{query && <button type="button" aria-label="清空搜索" onClick={() => setQuery('')}><X size={14} /></button>}</div></div>
    <div className="directory-meta"><span>MODULE INDEX</span><span className="directory-count" role="status">{visible.length.toString().padStart(2, '0')} / {modules.length} 个模块</span></div>
    <div className="module-grid">{visible.map((module) => {
      const Icon = moduleIcons[module.slug] ?? PackageOpen;
      const groupIndex = groups.findIndex((name) => taskGroups[name].includes(module.slug));
      return <Link key={module.slug} href={`/docs/${module.slug}`} className="module-card" style={{ '--module-accent': groupAccents[groupIndex] } as CSSProperties}>
        <div className="module-card-top"><span className="module-english">{module.name}</span><ArrowUpRight size={16} aria-hidden="true" /></div>
        <div className="module-title"><span className="module-glyph"><Icon size={22} strokeWidth={1.5} aria-hidden="true" /></span><h3>{module.displayName}</h3></div>
        <p>{module.description}</p><span className="module-read"><span>{groups[groupIndex]}</span><span>使用与配置 →</span></span>
      </Link>;
    })}</div>
    {!visible.length && <div className="directory-empty"><Search size={26} strokeWidth={1} aria-hidden="true" /><h3>没有找到相关模块</h3><p>试试「地图」「奖励」等功能名称，或清除筛选。</p><button type="button" onClick={() => { setQuery(''); setGroup('全部模块'); }}>查看全部模块 <ArrowUpRight size={15} /></button></div>}
  </div>;
}
