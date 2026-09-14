import './global.css';
import type { ReactNode } from 'react';
import { RootProvider } from 'fumadocs-ui/provider';

export const metadata = {
  title: {
    default: 'ArcartX-Suite 文档',
    template: '%s | ArcartX-Suite',
  },
  description: 'ArcartX-Suite 是面向 ArcartX 客户端的 29 模块 Minecraft 服务器插件套件',
};

export default function Layout({ children }: { children: ReactNode }) {
  return (
    <html lang="zh-CN" className="dark" suppressHydrationWarning>
      <body>
        <RootProvider
          theme={{
            enabled: false,
            defaultTheme: 'dark',
          }}
        >
          {children}
        </RootProvider>
      </body>
    </html>
  );
}
