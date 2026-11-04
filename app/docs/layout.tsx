import { DocsLayout } from 'fumadocs-ui/layouts/docs';
import type { ReactNode } from 'react';
import { baseOptions } from '@/app/layout.config';
import { documentationTree } from '@/lib/navigation';
import { WikiSidebarFolder, WikiSidebarItem } from '@/components/WikiSidebar';

export default function Layout({ children }: { children: ReactNode }) {
  return (
    <DocsLayout
      tree={documentationTree}
      sidebar={{ components: { Folder: WikiSidebarFolder, Item: WikiSidebarItem } }}
      {...baseOptions}
    >
      {children}
    </DocsLayout>
  );
}
