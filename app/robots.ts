import type { MetadataRoute } from 'next';
import { BASE_URL } from '@/lib/site';

export const dynamic = 'force-static';

export default function robots(): MetadataRoute.Robots {
  return {
    rules: {
      userAgent: '*',
      allow: '/',
    },
    sitemap: new URL('sitemap.xml', BASE_URL).href,
  };
}
