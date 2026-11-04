import './global.css';
import type { ReactNode } from 'react';
import { WikiProvider } from '@/components/WikiProvider';
import { I18nProvider } from 'fumadocs-ui/i18n';

export const metadata = {
  title: {
    default: 'Suite 使用文档',
    template: '%s | Suite',
  },
  description: 'ArcartX Suite 使用手册：模块功能、操作步骤、配置示例与命令速查。',
};

export default function Layout({ children }: { children: ReactNode }) {
  return (
    <html lang="zh-CN" className="dark" style={{ colorScheme: 'dark' }}>
      <body>
        <a href="#main-content" className="skip-link">跳至正文</a>
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
              editOnGithub: '在 GitHub 上编辑',
            }}
          >
            <WikiProvider>{children}</WikiProvider>
        </I18nProvider>
      </body>
    </html>
  );
}
