import type { MetadataRoute } from 'next';

export default function robots(): MetadataRoute.Robots {
  return {
    rules: {
      userAgent: '*',
      allow: '/',
    },
    sitemap: 'https://arcartx-suite.github.io/ArcartXSuite-Wiki/sitemap.xml',
  };
}
