import type { SortedResult } from 'fumadocs-core/server';

interface SearchSection {
  id: string;
  title: string;
  content: string;
}

export interface SearchPage {
  url: string;
  title: string;
  description: string;
  content: string;
  context?: string;
  sections?: SearchSection[];
}

function normalize(value: string) {
  return value.normalize('NFKC').toLowerCase();
}

export function searchTerms(query: string) {
  return normalize(query).trim().split(/\s+/).filter(Boolean);
}

type SearchField = [text: string, weight: number];

function matchScore(terms: string[], fields: SearchField[]) {
  let score = 0;
  for (const term of terms) {
    const match = fields.find(([value]) => value.includes(term));
    if (!match) return 0;
    score += match[1];
  }
  return score;
}

export function searchWiki(pages: SearchPage[], query: string): SortedResult[] | 'empty' {
  const terms = searchTerms(query);
  if (!terms.length) return 'empty';

  const matches: { result: SortedResult; score: number }[] = [];
  for (const page of pages) {
    const title = normalize(page.title);
    const description = normalize(page.description);
    const context = normalize(page.context ?? '');
    const sections = (page.sections ?? []).map((section) => ({
      section,
      heading: normalize(section.title),
      content: normalize(section.content),
    }));
    const body = [normalize(page.content), ...sections.map(({ heading, content }) => `${heading} ${content}`)].join('\n');
    let score = matchScore(terms, [[title, 26], [description, 8], [body, 1], [context, 1]]);
    let result: SortedResult = {
      id: page.url, url: page.url, type: 'page', content: `${page.title} · ${page.description}`,
    };

    for (const { section, heading, content } of sections) {
      if (!terms.some((term) => heading.includes(term) || content.includes(term))) continue;
      const sectionScore = matchScore(terms, [[heading, 22], [title, 14], [content, 6], [description, 3], [context, 1]]);
      if (sectionScore <= score) continue;
      const url = `${page.url}#${encodeURIComponent(section.id)}`;
      result = { id: url, url, type: 'heading', content: `${section.title} · ${page.title}` };
      score = sectionScore;
    }

    if (score > 0) {
      const priority = /^\/docs\/[^/]+$/.test(page.url) ? 3 : page.url.startsWith('/docs/api/') ? -3 : 0;
      matches.push({ result, score: score + priority });
    }
  }
  return matches.sort((a, b) => b.score - a.score).slice(0, 20).map(({ result }) => result);
}
