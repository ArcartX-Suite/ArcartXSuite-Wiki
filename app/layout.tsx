import './global.css';
import type { ReactNode } from 'react';
import { RootProvider } from 'fumadocs-ui/provider';
import { I18nProvider } from 'fumadocs-ui/i18n';
import { HomeBackground } from '@/components/HomeBackground';

export const metadata = {
  title: {
    default: 'Suite 文档',
    template: '%s | Suite',
  },
  description: 'Suite 是面向 ArcartX 客户端的 29 模块 Minecraft 服务器插件套件',
};

export default function Layout({ children }: { children: ReactNode }) {
  return (
    <html lang="zh-CN" className="dark" suppressHydrationWarning>
      <body>
        <HomeBackground />
        <RootProvider
          theme={{
            enabled: false,
            defaultTheme: 'dark',
          }}
        >
          <I18nProvider
            locale="zh"
            translations={{
              search: '搜索',
              searchNoResult: '未找到结果',
              toc: '本页目录',
              tocNoHeadings: '无标题',
              lastUpdate: '最后更新于',
              chooseLanguage: '选择语言',
              nextPage: '下一页',
              previousPage: '上一页',
              chooseTheme: '主题',
              editOnGithub: '在 GitHub 上编辑',
            }}
          >
            {children}
          </I18nProvider>
        </RootProvider>
      </body>
    </html>
  );
}
