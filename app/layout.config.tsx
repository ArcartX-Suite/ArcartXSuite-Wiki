import type { BaseLayoutProps } from 'fumadocs-ui/layouts/shared';

export const baseOptions: BaseLayoutProps = {
  nav: {
    title: (
      <span className="font-bold text-lg">ArcartX-Suite</span>
    ),
    url: '/',
  },
  disableThemeSwitch: true,
  links: [
    {
      text: '文档',
      url: '/docs/guide',
      active: 'nested-url',
    },
    {
      text: '模块',
      url: '/docs/modules',
      active: 'nested-url',
    },
    {
      text: 'GitHub',
      url: 'https://github.com/ArcartX-Suite/ArcartXSuite-Wiki',
      active: 'nested-url',
      external: true,
    },
  ],
};
