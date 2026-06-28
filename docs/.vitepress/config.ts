import { defineConfig } from 'vitepress'
import { withMermaid } from 'vitepress-plugin-mermaid'

/** 模块文档侧栏（指南 / 开发者 / 架构 / API / 云端 共用引用，避免重复维护） */
const moduleSidebar = [
  { text: '总览', link: '/modules/' },
  {
    text: '免费模块',
    collapsed: true,
    items: [
      { text: 'Announcer 播报', link: '/modules/announcer' },
      { text: 'EventPacket 事件引擎', link: '/modules/eventpacket' },
      { text: 'CombatEffect 战斗特效', link: '/modules/combateffect' },
      { text: 'LoginView 登录界面', link: '/modules/loginview' },
      { text: 'OnlineRewards 在线奖励', link: '/modules/onlinerewards' },
      { text: 'Pickup 拾取提示', link: '/modules/pickup' },
      { text: 'Prop 快捷道具', link: '/modules/prop' },
      { text: 'RGB 渐变文本', link: '/modules/rgb' },
      { text: 'Essentials 基础工具', link: '/modules/essentials' },
      { text: 'Regions 区域保护', link: '/modules/regions' },
      { text: 'Menu 通用菜单', link: '/modules/menu' },
      { text: 'AfkReward 挂机奖励', link: '/modules/afkreward' },
    ],
  },
  {
    text: '付费模块',
    collapsed: true,
    items: [
      { text: 'Fishing 钓鱼', link: '/modules/fishing' },
      { text: 'Title 称号', link: '/modules/title' },
      { text: 'Warehouse 仓库银行', link: '/modules/warehouse' },
      { text: 'Mail 邮箱', link: '/modules/mail' },
      { text: 'Lottery 抽奖', link: '/modules/lottery' },
      { text: 'QuestGPS 任务导航', link: '/modules/questgps' },
      { text: 'Map 地图', link: '/modules/map' },
      { text: 'Conversation 对话桥', link: '/modules/conversation' },
      { text: 'BattlePass 战令', link: '/modules/battlepass' },
      { text: 'Market 全球市场', link: '/modules/market' },
      { text: 'QQBot 群服互联', link: '/modules/qqbot' },
    ],
  },
  {
    text: '福利模块',
    collapsed: false,
    items: [
      { text: 'Chat 聊天', link: '/modules/chat' },
      { text: 'Tab 在线列表', link: '/modules/tab' },
      { text: 'EntityTracker 实体追踪', link: '/modules/entitytracker' },
    ],
  },
]

const architectureSidebar = [
  {
    text: '架构',
    items: [
      { text: '概览', link: '/architecture/' },
      { text: '系统架构', link: '/architecture/system-architecture' },
      { text: '模块化架构', link: '/architecture/modular' },
    ],
  },
  {
    text: '集成与通信',
    items: [
      { text: '桥接层 (Bridge)', link: '/architecture/bridges' },
      { text: '跨服通信 (CrossServer)', link: '/architecture/cross-server' },
      { text: '数据包流向', link: '/architecture/packet-flow' },
    ],
  },
  {
    text: '安全与资源',
    items: [
      { text: '客户端包守卫', link: '/architecture/security' },
      { text: 'Native 安全与模块加密', link: '/architecture/native-security' },
      { text: '资源加密 (.axb)', link: '/architecture/protected-resources' },
    ],
  },
  {
    text: '配置与 UI',
    items: [
      { text: '配置智能诊断', link: '/architecture/config-autofix' },
      { text: 'UI Packet 数据全景', link: '/ui-packet-data' },
    ],
  },
]

export default withMermaid(defineConfig<any>({
  lang: 'zh-CN',
  appearance: 'force-dark',
  title: 'ArcartX-Suite',
  description: 'ArcartX-Suite 是面向 ArcartX 客户端的 26 模块 Minecraft 服务器插件套件，涵盖全球市场、抽奖开箱、钓鱼、称号、战令、仓库等核心玩法，自带 ArcartX UI 界面，我的世界服务器开发者的首选插件。',
  base: '/ArcartXSuite-Wiki/',
  head: [
    ['script', {}, `(function(){try{localStorage.setItem('vitepress-theme-appearance','dark')}catch(e){}})();`],
    ['style', {}, `html{background:#070912;color-scheme:dark}html.dark{background:#070912}`],
    ['link', { rel: 'icon', href: '/ArcartXSuite-Wiki/favicon.ico' }],
    ['meta', { name: 'theme-color', content: '#6750a4' }],
    ['meta', { name: 'description', content: 'ArcartX-Suite 是面向 ArcartX 客户端框架的 26 模块 Minecraft 服务器插件套件，涵盖聊天、战斗、播报、经济交易、全球市场、抽奖、钓鱼等核心玩法，自带 ArcartX UI 界面。' }],
    ['meta', { name: 'keywords', content: 'ArcartX-Suite, ArcartXSuite, ArcartX, Minecraft插件, 服务器插件, 全球市场插件, 抽奖插件, 开箱插件, 钓鱼插件, 称号插件, 邮箱插件, 仓库插件, 战令插件, 拍卖行, 玩家商店, Minecraft UI, 服务器模组' }],
    ['meta', { property: 'og:title', content: 'ArcartX-Suite - 26个Minecraft服务器模块插件套件' }],
    ['meta', { property: 'og:description', content: 'ArcartX-Suite 提供 26 个功能模块，涵盖经济交易、全球市场、抽奖开箱、钓鱼、称号、战令等核心玩法，自带 ArcartX UI 界面。' }],
    ['meta', { property: 'og:type', content: 'website' }],
  ],

  themeConfig: {
    logo: '/logo.svg',
    siteTitle: 'ArcartX-Suite',

    nav: [
      {
        text: '指南',
        link: '/guide/',
        activeMatch: '^/guide/(?!developer|cloud-modules)',
      },
      {
        text: '开发者',
        link: '/guide/developer/',
        activeMatch: '/guide/developer/',
      },
      {
        text: '架构',
        link: '/architecture/',
        activeMatch: '/architecture/',
      },
      {
        text: 'API',
        link: '/api/',
        activeMatch: '/api/',
      },
      {
        text: '模块',
        link: '/modules/',
        activeMatch: '/modules/',
      },
      {
        text: '云端授权',
        link: '/guide/cloud-modules',
        activeMatch: '/guide/cloud-modules',
      },
      {
        text: '链接',
        items: [
          { text: 'ArcartX 官方文档', link: 'https://wiki.arcartx.com/docs' },
          { text: 'GitHub', link: 'https://github.com/xuanmomo233/ArcartXSuite-Wiki' },
        ],
      },
    ],

    sidebar: {
      // ── 服主指南（不含开发者 / 云端，避免顶栏与侧栏归属冲突）──
      '/guide/': [
        {
          text: '入门',
          items: [
            { text: '概览', link: '/guide/' },
            { text: '安装', link: '/guide/installation' },
            { text: '模块启用', link: '/guide/module-enablement' },
            { text: '第一次启用', link: '/guide/first-run' },
          ],
        },
        {
          text: '配置与运维',
          items: [
            { text: '命令速查', link: '/guide/commands' },
            { text: 'PlaceholderAPI', link: '/guide/placeholders' },
            { text: '条件系统', link: '/guide/conditions' },
            { text: '货币系统', link: '/guide/currencies' },
            { text: '配置智能体检', link: '/guide/config-management' },
            { text: '多 UI 发包', link: '/guide/multi-ui' },
          ],
        },
        {
          text: '跨服与群组',
          items: [
            { text: '跨服功能配置', link: '/guide/cross-server-setup' },
            { text: 'Warehouse 多服部署', link: '/guide/warehouse-cross-server' },
            { text: 'Proxy 代理端', link: '/guide/proxy-usage' },
          ],
        },
      ],

      // ── 云端授权（独立顶栏 → 独立侧栏）──
      '/guide/cloud-modules': [
        {
          text: '云端授权',
          items: [
            { text: '平台与服主配置', link: '/guide/cloud-modules' },
          ],
        },
        {
          text: '运维',
          items: [
            { text: 'sync / update 命令', link: '/guide/commands#云端模块管理' },
          ],
        },
        {
          text: '延伸阅读',
          collapsed: true,
          items: [
            { text: '模块启用开关', link: '/guide/module-enablement' },
            { text: '安装与 Bootstrap', link: '/guide/installation' },
            { text: 'Native 模块加密', link: '/architecture/native-security' },
            { text: '使用第三方模块', link: '/guide/developer/using-third-party-modules' },
          ],
        },
      ],

      // ── 开发者（仅开发向文档，API 通过顶栏切换）──
      '/guide/developer/': [
        {
          text: '开发者指南',
          items: [
            { text: '概览', link: '/guide/developer/' },
            { text: '开发第三方模块', link: '/guide/developer/module-development' },
            { text: '使用第三方模块', link: '/guide/developer/using-third-party-modules' },
            { text: 'Capability 教程', link: '/guide/developer/capability-guide' },
          ],
        },
        {
          text: '相关文档',
          collapsed: true,
          items: [
            { text: '→ API 参考', link: '/api/' },
            { text: 'ArcartXSuite-Core（开源 SDK）', link: 'https://github.com/xuanmomo233/ArcartXSuite-Core' },
            { text: '模块化架构', link: '/architecture/modular' },
            { text: 'Capability API', link: '/api/capability' },
            { text: 'ModuleContext', link: '/api/module-context' },
          ],
        },
      ],

      // ── 架构 + UI Packet 全景（同套侧栏，避免跳转后侧栏消失）──
      '/architecture/': architectureSidebar,
      '/ui-packet-data': architectureSidebar,

      // ── 模块 ──
      '/modules/': [{ text: '模块', items: moduleSidebar }],

      // ── API ──
      '/api/': [
        {
          text: 'API 参考',
          items: [{ text: '概览', link: '/api/' }],
        },
        {
          text: '模块开发',
          items: [
            { text: '模块生命周期', link: '/api/module-lifecycle' },
            { text: 'ModuleContext', link: '/api/module-context' },
            { text: '消息外部化 (i18n)', link: '/api/i18n' },
          ],
        },
        {
          text: '桥接与协作',
          items: [
            { text: '桥接 API', link: '/api/bridge-api' },
            { text: '事件', link: '/api/events' },
            { text: 'Capability API', link: '/api/capability' },
          ],
        },
        {
          text: '教程',
          collapsed: true,
          items: [
            { text: 'Capability 开发教程', link: '/guide/developer/capability-guide' },
            { text: '开发第三方模块', link: '/guide/developer/module-development' },
          ],
        },
      ],
    },

    socialLinks: [
      { icon: 'github', link: 'https://github.com/xuanmomo233/ArcartXSuite-Wiki' },
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
  },
}))
