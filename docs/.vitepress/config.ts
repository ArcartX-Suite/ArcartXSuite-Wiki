import { defineConfig } from 'vitepress'

export default defineConfig({
  lang: 'zh-CN',
  title: 'ArcartXSuite',
  description: 'ArcartX 服务器模块化插件 — 21 个主模块，开箱即用',
  base: '/ArcartXSuite/',
  head: [
    ['link', { rel: 'icon', href: '/ArcartXSuite/favicon.ico' }],
    ['meta', { name: 'theme-color', content: '#6750a4' }],
  ],

  themeConfig: {
    logo: '/logo.svg',
    siteTitle: 'ArcartXSuite',

    nav: [
      { text: '指南', link: '/guide/', activeMatch: '/guide/' },
      { text: '架构', link: '/architecture/', activeMatch: '/architecture/' },
      { text: 'API', link: '/api/', activeMatch: '/api/' },
      { text: '模块', link: '/modules/', activeMatch: '/modules/' },
      { text: '变更日志', link: '/appendix/changelog' },
      {
        text: '链接',
        items: [
          { text: 'ArcartX 官方文档', link: 'https://wiki.arcartx.com/docs' },
          { text: 'GitHub', link: 'https://github.com/xuanmomo233/ArcartXSuite' },
        ],
      },
    ],

    sidebar: {
      '/guide/': [
        {
          text: '快速开始',
          items: [
            { text: '概览', link: '/guide/' },
            { text: '安装', link: '/guide/installation' },
            { text: '模块授权门控', link: '/guide/module-passwords' },
            { text: '第一次启用流程', link: '/guide/first-run' },
            { text: '命令速查', link: '/guide/commands' },
            { text: 'PlaceholderAPI 速查', link: '/guide/placeholders' },
            { text: '货币系统配置', link: '/guide/currencies' },
            { text: '配置智能体检', link: '/guide/config-management' },
            { text: '多 UI 同时发包', link: '/guide/multi-ui' },
            { text: 'Proxy 代理端插件', link: '/guide/proxy-usage' },
          ],
        },
      ],
      '/architecture/': [
        {
          text: '架构',
          items: [
            { text: '概览', link: '/architecture/' },
            { text: '模块化架构', link: '/architecture/modular' },
            { text: '桥接层 (Bridge)', link: '/architecture/bridges' },
            { text: '客户端包守卫', link: '/architecture/security' },
            { text: '资源加密 (.axb)', link: '/architecture/protected-resources' },
            { text: '数据包流向', link: '/architecture/packet-flow' },
            { text: '配置智能诊断', link: '/architecture/config-autofix' },
          ],
        },
      ],
      '/modules/': [
        {
          text: '模块',
          items: [
            { text: '总览', link: '/modules/' },
          ],
        },
        {
          text: '免费模块',
          collapsed: false,
          items: [
            { text: 'Announcer 播报系统', link: '/modules/announcer' },
            { text: 'Chat 聊天', link: '/modules/chat' },
            { text: 'EventPacket 事件引擎', link: '/modules/eventpacket' },
            { text: 'CombatEffect 战斗特效', link: '/modules/combateffect' },
            { text: 'LoginView 登录界面', link: '/modules/loginview' },
            { text: 'OnlineRewards 在线奖励', link: '/modules/onlinerewards' },
            { text: 'Pickup 拾取提示', link: '/modules/pickup' },
            { text: 'Prop 快捷道具', link: '/modules/prop' },
            { text: 'RGB 渐变文本', link: '/modules/rgb' },
            { text: 'Essentials 基础工具', link: '/modules/essentials' },
            { text: 'Regions 区域保护', link: '/modules/regions' },
          ],
        },
        {
          text: '付费模块',
          collapsed: false,
          items: [
            { text: 'Title 称号', link: '/modules/title' },
            { text: 'Warehouse 仓库银行', link: '/modules/warehouse' },
            { text: 'Mail 邮箱', link: '/modules/mail' },
            { text: 'QuestGPS 任务导航', link: '/modules/questgps' },
            { text: 'Map 地图', link: '/modules/map' },
            { text: 'Conversation 对话桥', link: '/modules/conversation' },
            { text: 'Market 全球市场', link: '/modules/market' },
            { text: 'QQBot QQ群服互联', link: '/modules/qqbot' },
          ],
        },
        {
          text: '福利模块',
          collapsed: false,
          items: [
            { text: 'Tab 在线列表', link: '/modules/tab' },
            { text: 'EntityTracker 实体追踪', link: '/modules/entitytracker' },
          ],
        },
      ],
      '/api/': [
        {
          text: 'API 参考',
          items: [
            { text: '概览', link: '/api/' },
            { text: '模块生命周期', link: '/api/module-lifecycle' },
            { text: 'ModuleContext 上下文', link: '/api/module-context' },
            { text: '桥接 API', link: '/api/bridge-api' },
            { text: '事件', link: '/api/events' },
            { text: 'Capability 跨模块通信', link: '/api/capability' },
            { text: '消息外部化 (i18n)', link: '/api/i18n' },
          ],
        },
      ],
      '/appendix/': [
        {
          text: '附录',
          items: [
            { text: '变更日志', link: '/appendix/changelog' },
          ],
        },
      ],
    },

    socialLinks: [
      { icon: 'github', link: 'https://github.com/xuanmomo233/ArcartXSuite' },
    ],

    search: {
      provider: 'local',
      options: {
        translations: {
          button: { buttonText: '搜索文档', buttonAriaLabel: '搜索文档' },
          modal: {
            noResultsText: '未找到相关结果',
            resetButtonTitle: '清除查询',
            footer: { selectText: '选择', navigateText: '切换', closeText: '关闭' },
          },
        },
      },
    },

    footer: {
      message: '基于 GPL-3.0 许可发布',
      copyright: '© 2024-2026 墨墨啊',
    },

    outline: { level: [2, 3], label: '页面导航' },
    lastUpdated: { text: '最后更新' },
    docFooter: { prev: '上一页', next: '下一页' },
    returnToTopLabel: '回到顶部',
    sidebarMenuLabel: '菜单',
    darkModeSwitchLabel: '主题',
  },
})
