export interface ModuleInfo {
  slug: string;
  name: string;
  displayName: string;
  description: string;
  category: 'free' | 'paid' | 'bonus';
  tags: string[];
  hasUI: boolean;
  color: string;
}

export const modules: ModuleInfo[] = [
  // ── 免费模块 ──
  { slug: 'rgb', name: 'RGB', displayName: 'RGB 渐变文本', description: '渐变文本渲染，PAPI 输出扫光效果', category: 'free', tags: ['免费', 'PAPI'], hasUI: false, color: '#f472b6' },
  { slug: 'pickup', name: 'Pickup', displayName: 'Pickup 拾取提示', description: '物品拾取 HUD 弹出提示 + 扫描器模式', category: 'free', tags: ['免费', 'HUD'], hasUI: true, color: '#84cc16' },
  { slug: 'announcer', name: 'Announcer', displayName: 'Announcer 播报', description: '公告 / 字幕轮播系统，可点击执行命令', category: 'free', tags: ['免费', 'HUD'], hasUI: true, color: '#f59e0b' },
  { slug: 'loginview', name: 'LoginView', displayName: 'LoginView 登录界面', description: 'ArcartX UI 登录/注册面板，支持 AuthMe 桥接', category: 'free', tags: ['免费', 'UI'], hasUI: true, color: '#3b82f6' },
  { slug: 'onlinerewards', name: 'OnlineRewards', displayName: 'OnlineRewards 在线奖励', description: '在线奖励 / 每日签到 / 连续签到 / 补签卡', category: 'free', tags: ['免费'], hasUI: false, color: '#22c55e' },
  { slug: 'combateffect', name: 'CombatEffect', displayName: 'CombatEffect 战斗特效', description: '战斗特效（击杀/连击/伤害飘字），四插件属性来源自动检测', category: 'free', tags: ['免费', '战斗'], hasUI: false, color: '#ef4444' },
  { slug: 'eventpacket', name: 'EventPacket', displayName: 'EventPacket 事件引擎', description: '事件引擎，9种触发器 × 11种动作自由组合', category: 'free', tags: ['免费', '事件'], hasUI: false, color: '#a855f7' },
  { slug: 'prop', name: 'Prop', displayName: 'Prop 快捷道具', description: '快捷道具栏，客户端按键绑定临时属性加成', category: 'free', tags: ['免费', '按键绑定'], hasUI: false, color: '#06b6d4' },
  { slug: 'essentials', name: 'Essentials', displayName: 'Essentials 基础工具', description: '基础工具（传送/家/Warp/一键砍树/背包整理/UI 菜单）', category: 'free', tags: ['免费', 'UI'], hasUI: true, color: '#14b8a6' },
  { slug: 'regions', name: 'Regions', displayName: 'Regions 区域保护', description: '区域保护（40+ 标志/优先级继承/世界规则）', category: 'free', tags: ['免费', '保护'], hasUI: false, color: '#eab308' },
  { slug: 'afkreward', name: 'AfkReward', displayName: 'AfkReward 挂机奖励', description: '区域挂机 + 原地挂机双模式、周期命令奖励、排行榜', category: 'free', tags: ['免费'], hasUI: false, color: '#94a3b8' },
  { slug: 'extrabackpack', name: 'ExtraBackpack', displayName: 'ExtraBackpack 额外背包', description: '扩展背包（多分类额外槽位）', category: 'bonus', tags: ['福利', 'UI'], hasUI: true, color: '#8b5cf6' },
  { slug: 'tooltip', name: 'Tooltip', displayName: 'Tooltip 动态提示桥接', description: '物品提示桥接（TACZ/Apotheosis tooltip 采集）', category: 'free', tags: ['免费', '桥接'], hasUI: false, color: '#f97316' },
  { slug: 'vanilla', name: 'VanillaUI', displayName: 'VanillaUI 原版界面替换', description: '原版界面替换（工作台/熔炉 UI 化）', category: 'bonus', tags: ['福利', 'UI'], hasUI: true, color: '#10b981' },

  // ── 付费模块 ──
  { slug: 'title', name: 'Title', displayName: 'Title 称号', description: '分组称号系统，有效期/品质/套装属性/头顶显示/聊天Tab前缀', category: 'paid', tags: ['付费', '称号'], hasUI: false, color: '#fbbf24' },
  { slug: 'conversation', name: 'Conversation', displayName: 'Conversation 对话桥', description: 'NPC 对话引擎，Chemdah + Adyeshach 联动，ArcartX UI 渲染', category: 'paid', tags: ['付费', 'UI', 'NPC'], hasUI: true, color: '#c084fc' },
  { slug: 'mail', name: 'Mail', displayName: 'Mail 邮箱', description: '邮件系统，玩家写信/预设派发/CDK兑换/物品附件/跨服广播', category: 'paid', tags: ['付费', 'UI', '跨服'], hasUI: true, color: '#60a5fa' },
  { slug: 'warehouse', name: 'Warehouse', displayName: 'Warehouse 仓库银行', description: '仓库银行，个人/共享仓库、多货币银行、定期存款、二级密码', category: 'paid', tags: ['付费', 'UI', '跨服'], hasUI: true, color: '#34d399' },
  { slug: 'questgps', name: 'QuestGPS', displayName: 'QuestGPS 任务导航', description: 'Chemdah SSOT + overlay 白名单；动态分类菜单、路径寻路、指引 HUD', category: 'paid', tags: ['付费', 'HUD', 'Chemdah'], hasUI: true, color: '#818cf8' },
  { slug: 'map', name: 'Map', displayName: 'Map 地图', description: '大地图 / 锚点传送 / 玩家路径点 / 小地图 HUD', category: 'paid', tags: ['付费', 'HUD'], hasUI: true, color: '#2dd4bf' },
  { slug: 'market', name: 'Market', displayName: 'Market 全球市场', description: '全球市场插件，系统商店 + 玩家拍卖行 + 回收商店，多货币跨服同步', category: 'paid', tags: ['付费', 'UI', '跨服'], hasUI: true, color: '#fb7185' },
  { slug: 'qqbot', name: 'QQBot', displayName: 'QQBot 群服互联', description: 'QQ 群服互联插件，OneBot 11 双向消息同步、QQ-游戏账号绑定', category: 'paid', tags: ['付费', 'QQ'], hasUI: false, color: '#5b8def' },
  { slug: 'menu', name: 'Menu', displayName: 'Menu 通用菜单', description: '配置驱动 ArcartX 菜单系统，ESC 替换、命令/物品绑定', category: 'paid', tags: ['付费', 'UI'], hasUI: true, color: '#f472b6' },
  { slug: 'fishing', name: 'Fishing', displayName: 'Fishing 钓鱼', description: '钓鱼系统插件，星露谷风格钓鱼小游戏、多水域生态、钓鱼图鉴', category: 'paid', tags: ['付费', 'UI', '小游戏'], hasUI: true, color: '#38bdf8' },
  { slug: 'lottery', name: 'Lottery', displayName: 'Lottery 抽奖', description: '抽奖系统插件，CS 开箱滚动动画 + 原神祈愿卡池，纯色块 UI、保底机制', category: 'paid', tags: ['付费', 'UI', '小游戏'], hasUI: true, color: '#fbbf24' },
  { slug: 'battlepass', name: 'BattlePass', displayName: 'BattlePass 战令', description: '战令系统，三层通行证 + 日/周/赛季任务池 + ArcartX UI 面板', category: 'paid', tags: ['付费', 'UI'], hasUI: true, color: '#a78bfa' },

  // ── 福利模块 ──
  { slug: 'chat', name: 'Chat', displayName: 'Chat 聊天', description: '全频道聊天系统', category: 'bonus', tags: ['福利'], hasUI: false, color: '#22d3ee' },
  { slug: 'tab', name: 'Tab', displayName: 'Tab 在线列表', description: '自定义 Tab 列表面板', category: 'bonus', tags: ['福利'], hasUI: false, color: '#67e8f9' },
  { slug: 'entitytracker', name: 'EntityTracker', displayName: 'EntityTracker 实体追踪', description: 'Boss / 目标追踪面板', category: 'bonus', tags: ['福利', 'HUD'], hasUI: true, color: '#fde047' },
];

export const categoryLabels: Record<ModuleInfo['category'], string> = {
  free: '免费模块',
  paid: '付费模块',
  bonus: '福利模块',
};

export const categoryColors: Record<ModuleInfo['category'], string> = {
  free: '#22c55e',
  paid: '#f59e0b',
  bonus: '#a855f7',
};
