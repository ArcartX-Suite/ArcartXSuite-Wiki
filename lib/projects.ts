import { modules, type ModuleInfo } from '@/lib/modules';

export interface ProjectInfo {
  slug: string;
  title: string;
  description?: string;
  color?: string;
  entry: string;
  order: number;
}

/**
 * 从模块列表生成 ProjectSwitcher 选项。
 * 文档区（guide/architecture/api/developer/modules）不作为独立项目。
 */
export function getProjects(): ProjectInfo[] {
  return modules
    .map((m: ModuleInfo) => ({
      slug: m.slug,
      title: m.displayName,
      description: m.description,
      color: m.color,
      entry: `/docs/${m.slug}`,
      order: 100,
    }))
    .sort((a, b) => a.title.localeCompare(b.title, 'zh-CN'));
}
