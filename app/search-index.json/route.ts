import { source } from '@/lib/source';

export const dynamic = 'force-static';

export function GET() {
  return Response.json(source.getPages().map((page) => ({
    url: page.url,
    title: page.data.title,
    description: page.data.description ?? '',
    content: [
      ...page.data.structuredData.headings.map((heading) => heading.content),
      ...page.data.structuredData.contents.map((section) => section.content),
    ].join('\n'),
  })));
}
