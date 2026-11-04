import type { BaseLayoutProps } from 'fumadocs-ui/layouts/shared';
import Image from 'next/image';

export const baseOptions: BaseLayoutProps = {
  nav: {
    title: (
      <span className="suite-docs-brand"><Image src={`${process.env.NEXT_PUBLIC_BASE_PATH ?? ''}/logo.svg`} width={32} height={32} alt="" /><span>Suite</span><span className="suite-docs-brand-caption">使用文档</span></span>
    ),
    url: '/',
  },
  disableThemeSwitch: true,
  links: [
    {
      text: '开始使用',
      url: '/docs/guide',
      active: 'nested-url',
    },
    {
      text: '模块目录',
      url: '/#modules',
    },
  ],
};
