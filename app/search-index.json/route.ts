import { source } from '@/lib/source';
import { modules } from '@/lib/modules';
import type { SearchPage } from '@/lib/wiki-search';

export const dynamic = 'force-static';

export function GET() {
  const moduleBySlug = new Map(modules.map((module) => [module.slug, module]));
  return Response.json(source.getPages().map((page): SearchPage => {
    const paragraphs = new Map<string, string[]>();
    for (const { heading, content } of page.data.structuredData.contents) {
      const id = heading ?? '';
      const existing = paragraphs.get(id);
      if (existing) existing.push(content);
      else paragraphs.set(id, [content]);
    }
    const moduleInfo = moduleBySlug.get(page.slugs[0]);
    const context = moduleInfo && [
      moduleInfo.slug, moduleInfo.name, moduleInfo.displayName,
      page.slugs.length === 1 ? moduleInfo.keywords : '',
    ].join(' ');
    return {
      url: page.url,
      title: page.data.title,
      description: page.data.description ?? '',
      context,
      content: paragraphs.get('')?.join('\n') ?? '',
      sections: page.data.structuredData.headings.map(({ id, content }) => ({
        id,
        title: content,
        content: paragraphs.get(id)?.join('\n') ?? '',
      })),
    };
  }));
}
