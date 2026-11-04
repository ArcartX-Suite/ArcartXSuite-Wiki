import type { PageTree } from 'fumadocs-core/server';
import { source } from '@/lib/source';
import { taskGroups } from '@/lib/modules';

const original = source.pageTree;
const findFolder = (slug: string): PageTree.Folder | undefined => {
  const folder = original.children.find((node) => node.type === 'folder' && node.index?.url === `/docs/${slug}`);
  if (!folder || folder.type !== 'folder') return undefined;
  return { ...folder, children: folder.children.map((node) => node.type === 'page' && node.url === folder.index?.url ? { ...node, name: slug === 'guide' ? '阅读顺序' : slug === 'api' ? '接入概览' : '使用入门' } : node) };
};

const guide = findFolder('guide');
const guidePages = guide?.children.filter((node) => node.type === 'page' && node.url !== guide.index?.url) ?? [];
const corePages = new Set(['/docs/guide/first-run', '/docs/guide/commands', '/docs/guide/config-management']);

// Keep the URLs stable while grouping the sidebar by the reader's task.
export const documentationTree: PageTree.Root = {
  ...original,
  children: [
    ...original.children.filter((node) => node.type === 'page' && node.url === '/docs'),
    ...(guide?.index ? [{ ...guide.index, name: '阅读顺序' }] : []),
    {
      type: 'folder',
      name: '本体使用',
      children: guidePages.filter((node) => node.type === 'page' && corePages.has(node.url)),
    },
    {
      type: 'folder',
      name: '通用配置',
      children: guidePages.filter((node) => node.type === 'page' && !corePages.has(node.url)),
    },
    { type: 'separator', name: '模块使用' },
    ...Object.entries(taskGroups).map(([name, slugs]): PageTree.Folder => ({
      type: 'folder',
      name,
      children: slugs.map(findFolder).filter((node): node is PageTree.Folder => node !== undefined),
    })),
    { type: 'separator', name: '开发者内容' },
    ...[findFolder('api')].filter((node): node is PageTree.Folder => node !== undefined),
  ],
};
