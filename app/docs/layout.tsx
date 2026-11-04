import { DocsLayout } from 'fumadocs-ui/layouts/docs';
import type { ReactNode } from 'react';
import { baseOptions } from '@/app/layout.config';
import { documentationTree } from '@/lib/navigation';

export default function Layout({ children }: { children: ReactNode }) {
  return (
    <DocsLayout
      tree={documentationTree}
      sidebar={{ defaultOpenLevel: 0 }}
      {...baseOptions}
    >
      {children}
    </DocsLayout>
  );
}
