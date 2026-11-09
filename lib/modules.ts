export interface ModuleInfo {
  slug: string;
  name: string;
  displayName: string;
  description: string;
  color: string;
  keywords?: string;
}

export const modules: ModuleInfo[] = [
  { slug: 'rgb', name: 'RGB', displayName: '渐变文本', description: '用渐变和扫光文字装饰聊天、称号与在线列表。', color: '#f472b6' },
  { slug: 'pickup', name: 'Pickup', displayName: '拾取提示', description: '显示拾取通知，或让玩家主动扫描附近掉落物。', color: '#84cc16', keywords: '附近战利品 掉落物清单 批量拾取 一键拾取' },
  { slug: 'announcer', name: 'Announcer', displayName: '公告与字幕', description: '轮播服务器公告，或为指定玩家播放字幕。', color: '#f59e0b' },
  { slug: 'loginview', name: 'LoginView', displayName: '登录认证', description: '为玩家提供登录、注册、条款确认与登录后传送界面。', color: '#3b82f6', keywords: '密码登录 正版登录 LittleSkin 代理认证 启动器授权' },
  { slug: 'onlinerewards', name: 'OnlineRewards', displayName: '在线奖励与签到', description: '按在线时长、签到记录和全服目标发放奖励。', color: '#22c55e', keywords: '在线礼包 时长奖励 签到补签' },
  { slug: 'combateffect', name: 'CombatEffect', displayName: '战斗特效', description: '展示伤害数字、连击提示与击杀特效。', color: '#ef4444' },
  { slug: 'eventpacket', name: 'EventPacket', displayName: '事件与动作', description: '把进服、任务和自定义信号组合成公告、奖励等动作。', color: '#a855f7', keywords: '首次进服 进服礼包 自定义事件' },
  { slug: 'prop', name: 'Prop', displayName: '快捷道具', description: '通过快捷栏与按键使用道具，获得即时或持续效果。', color: '#06b6d4' },
  { slug: 'essentials', name: 'Essentials', displayName: '基础工具', description: '配置传送、家、传送点、玩家状态与常用管理功能。', color: '#14b8a6', keywords: '飞行 设置家 回家 传送请求 封禁' },
  { slug: 'regions', name: 'Regions', displayName: '区域规则', description: '为选定区域设置建造、交互、传送等游戏规则。', color: '#557c53', keywords: '领地保护 圈地 禁止破坏' },
  { slug: 'afkreward', name: 'AfkReward', displayName: '挂机奖励', description: '在指定区域或原地挂机时，按周期发放奖励。', color: '#94a3b8' },
  { slug: 'tooltip', name: 'Tooltip', displayName: '物品提示', description: '展示兼容模组的物品说明与枪械浏览界面。', color: '#f97316', keywords: 'TACZ 神化词条 物品悬浮提示' },

  { slug: 'title', name: 'Title', displayName: '称号', description: '发放、装备与展示称号，配置分组、品质及套装效果。', color: '#557c53' },
  { slug: 'conversation', name: 'Conversation', displayName: 'NPC 对话', description: '用图形界面展示 NPC 对话、回复选项与过场演出。', color: '#c084fc' },
  { slug: 'mail', name: 'Mail', displayName: '邮箱', description: '发送邮件与物品附件，配置预设邮件和兑换码。', color: '#60a5fa', keywords: '收件箱 邮件附件 礼包码 CDK' },
  { slug: 'warehouse', name: 'Warehouse', displayName: '仓库与银行', description: '管理个人物资、共享仓库与多货币银行。', color: '#34d399', keywords: '个人仓库 自动入库 满背包 材料恢复 定期存款' },
  { slug: 'questgps', name: 'QuestGPS', displayName: '任务导航', description: '浏览 Chemdah 任务、追踪目标并显示导航提示。', color: '#818cf8' },
  { slug: 'map', name: 'Map', displayName: '地图与传送锚点', description: '浏览世界地图，管理路径点、导航与锚点传送。', color: '#2dd4bf', keywords: '大地图 小地图 地图标记 战争迷雾 跨服地图' },
  { slug: 'market', name: 'Market', displayName: '商店与拍卖', description: '提供系统商店、回收商店与玩家拍卖交易。', color: '#fb7185', keywords: '玩家商店 玩家店铺 NPC 兑换 以物换物 自动回收 回收保护 交易流水' },
  { slug: 'qqbot', name: 'QQBot', displayName: 'QQ 群互联', description: '同步群聊与游戏消息，提供账号绑定和群签到。', color: '#5b8def' },
  { slug: 'menu', name: 'Menu', displayName: '功能菜单', description: '创建功能导航，绑定命令、物品或 ESC 菜单。', color: '#f472b6', keywords: '暂停菜单 服务器菜单 指南针菜单' },
  { slug: 'fishing', name: 'Fishing', displayName: '钓鱼', description: '配置钓鱼小游戏、鱼类、水域、鱼竿和鱼饵。', color: '#38bdf8' },
  { slug: 'lottery', name: 'Lottery', displayName: '抽奖与开箱', description: '创建抽奖池、开箱动画、保底规则与积分兑换。', color: '#557c53' },
  { slug: 'battlepass', name: 'BattlePass', displayName: '战令', description: '配置赛季任务、经验等级与不同档位的奖励。', color: '#a78bfa' },

  { slug: 'chat', name: 'Chat', displayName: '聊天频道', description: '设置聊天频道、消息格式、私聊与物品展示。', color: '#557c53' },
  { slug: 'tab', name: 'Tab', displayName: '在线列表', description: '自定义玩家在线列表、排序、分组和显示内容。', color: '#557c53' },
  { slug: 'entitytracker', name: 'EntityTracker', displayName: 'Boss 与目标面板', description: '显示目标信息、Boss 伤害排名与结算奖励。', color: '#557c53' },
  { slug: 'vanilla', name: 'VanillaUI', displayName: '原版界面与背包', description: '替换原版容器界面，管理自定义配方，并在随身背包中访问个人仓库。', color: '#10b981', keywords: '工作台 熔炉 配方书 一键填料 额外背包' },
  { slug: 'abyss', name: 'Abyss', displayName: '地牢副本', description: '配置地牢、怪物波次、阶段任务、队伍与结算奖励。', color: '#fb923c' },
];

export const taskGroups: Record<string, string[]> = {
  "界面与展示": [
    "rgb",
    "pickup",
    "announcer",
    "loginview",
    "tooltip",
    "menu",
    "tab",
    "vanilla"
  ],
  "成长与奖励": [
    "onlinerewards",
    "afkreward",
    "title",
    "lottery",
    "battlepass"
  ],
  "玩法与交互": [
    "combateffect",
    "eventpacket",
    "prop",
    "conversation",
    "questgps",
    "map",
    "fishing",
    "entitytracker",
    "abyss"
  ],
  "管理与社交": [
    "essentials",
    "regions",
    "qqbot",
    "chat"
  ],
  "经济与物品": [
    "mail",
    "warehouse",
    "market"
  ]
};
