import { DocsLayout } from 'fumadocs-ui/layouts/docs';
import type { ReactNode } from 'react';
import { baseOptions } from '@/app/layout.config';
import { source } from '@/lib/source';
import { getProjects } from '@/lib/projects';
import { ProjectSwitcher } from '@/components/ProjectSwitcher';

export default function Layout({ children }: { children: ReactNode }) {
  const projects = getProjects();

  return (
    <DocsLayout
      tree={source.pageTree}
      {...baseOptions}
      sidebar={{
        tabs: false,
        banner: (
          <ProjectSwitcher
            options={projects.map((project) => ({
              slug: project.slug,
              title: project.title,
              description: project.description,
              url: project.entry,
              urls: [project.entry],
              color: project.color,
            }))}
          />
        ),
      }}
    >
      {children}
    </DocsLayout>
  );
}
