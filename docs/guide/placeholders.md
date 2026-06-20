---
title: PlaceholderAPI 占位符速查 | ArcartXSuite Minecraft插件文档
description: PlaceholderAPI 占位符速查 - ArcartXSuite Minecraft 服务器插件文档。 ArcartXSuite 我的世界服务器插件套件。
---

# PlaceholderAPI 占位符速查

本页列出 ArcartXSuite 对外输出的**全部 PlaceholderAPI 占位符**，包括每个字段的含义、返回值类型和使用示例。

> **前置条件**：服务器需安装 [PlaceholderAPI](https://www.spigotmc.org/resources/placeholderapi.6245/) 插件，AXS 会在启动时自动注册占位符。

---

## 占位符总览

AXS 共有 **14 个模块** 对外输出 PAPI 占位符：

| 模块 | 前缀 | 必装 PAPI? | 说明 |
| --- | --- | --- | --- |
| [AfkReward](/modules/afkreward) | `%axsafk_*%` | 可选 | 区域挂机状态、时长、奖励次数、区域人数 |
| [BattlePass](/modules/battlepass) | `%axsbattlepass_*%` | 可选 | 战令等级、XP、通行证层级、活跃任务数 |
| [EntityTracker](/modules/entitytracker) | `%axsentitytracker_*%` | 可选 | Boss 追踪、伤害排行、结算数据 |
| [Essentials](/modules/essentials) | `%axsess_*%` | 可选 | AFK、隐身、飞行、昵称等玩家状态 |
| [Title](/modules/title) | `%axstitle_*%` | 可选 | 称号前缀/后缀、装备状态、属性加成 |
| [RGB](/modules/rgb) | `%axsrgb_*%` | **必装** | 动态渐变彩色文本渲染 |
| [OnlineRewards](/modules/onlinerewards) | `%axsonlinerewards_*%` | 可选 | 在线时长、签到状态、排行榜 |
| [Mail](/modules/mail) | `%axsmail_*%` | 可选 | 邮箱未读数、可领取数 |
| [Chat](/modules/chat) | `%axschat_*%` | 可选 | 聊天频道、禁言状态 |
| [LoginView](/modules/loginview) | `%axsloginview_*%` | 可选 | 账号来源识别、正版/LittleSkin/离线判断 |
| [Warehouse](/modules/warehouse) | `%axswarehouse_*%` | 可选 | 仓库容量、银行余额 |
| [Market](/modules/market) | `%axsmarket_*%` | 可选 | 拍卖数量、商店数、回收数、Redis 状态 |
| [QQBot](/modules/qqbot) | `%axsqqbot_*%` | 可选 | OneBot 连接状态、玩家 QQ 绑定信息、群数量 |
| [Tab](/modules/tab) | `%axstab_*%` | 可选 | Tab 在线列表变量、玩家状态 |

---

## AfkReward 占位符

前缀：`%axsafk_<字段>%`

| 占位符 | 返回值 | 说明 |
| --- | --- | --- |
| `%axsafk_type%` | 文本 | 挂机类型：`区域挂机` / `原地挂机` / `未挂机` |
| `%axsafk_area%` | 文本 | 当前所在区域名称，未挂机时返回空 |
| `%axsafk_time%` | 文本 | 当前挂机时长，格式化（如 `15分32秒`） |
| `%axsafk_total_time%` | 文本 | 累计总挂机时长（格式化） |
| `%axsafk_today%` | 数字 | 今日已获得奖励次数 |
| `%axsafk_total%` | 数字 | 累计获得奖励总次数 |
| `%axsafk_players%` | 数字 | 当前区域同时挂机人数 |
| `%axsafk_next%` | 数字 | 距离下次奖励的剩余秒数 |
| `%axsafk_top_1_name%` | 文本 | 排行榜第 1 名玩家名称 |
| `%axsafk_top_1_time%` | 文本 | 排行榜第 1 名总时长（格式化） |
| `%axsafk_top_1_rewards%` | 数字 | 排行榜第 1 名总奖励次数 |

**使用示例**：
```
%axsafk_area%          → 返回 "温泉"
%axsafk_time%          → 返回 "15分32秒"
%axsafk_players%       → 返回 "12"
%axsafk_next%          → 返回 "742"
```

---

## EntityTracker 占位符

前缀：`%axsentitytracker_<字段>%`

### 全局信息（不需要玩家上下文）

| 占位符 | 返回值 | 说明 |
| --- | --- | --- |
| `%axsentitytracker_sort_mode%` | 文本 | 当前 Boss 排序模式（如 `SPAWN_ORDER`） |
| `%axsentitytracker_max_visible_bars%` | 数字 | 配置中允许同时显示的最大 Boss 血条数 |
| `%axsentitytracker_configured_boss_count%` | 数字 | 配置文件中定义的 Boss 总数 |
| `%axsentitytracker_damage_ranking_boss_count%` | 数字 | 开启了伤害排行功能的 Boss 数量 |
| `%axsentitytracker_max_damage_ranking_entries%` | 数字 | 伤害排行榜最大展示条目数 |
| `%axsentitytracker_active_session_count%` | 数字 | 当前活跃的 Boss 战斗会话数 |
| `%axsentitytracker_active_viewer_count%` | 数字 | 当前正在观察 Boss 的玩家总数 |

### 玩家视野中的 Boss（需要玩家上下文）

**当前 Boss**（视野中第 1 个 Boss 的快捷别名）：

| 占位符 | 说明 |
| --- | --- |
| `%axsentitytracker_current_<字段>%` | 等同于 `slot_1_<字段>`，获取视野中第一个 Boss 的信息 |

**按槽位获取**（视野中第 N 个 Boss）：

| 占位符 | 说明 |
| --- | --- |
| `%axsentitytracker_slot_<N>_<字段>%` | 获取第 N 个 Boss 的信息。N 从 1 开始，最大值由 `max_visible_bars` 决定 |

常用的 `<字段>` 值包括：`display_name`（Boss 名称）、`health_percent`（血量百分比）、`mob_id`（MythicMobs ID）、`viewer_rank`（玩家伤害排名）、`viewer_damage`（玩家累计伤害）、`top_<排名>_name`（排行第 N 名的玩家名）、`top_<排名>_damage`（排行第 N 名的伤害值）等。

**使用示例**：
```
%axsentitytracker_current_display_name%        → 返回视野中第一个 Boss 的名称
%axsentitytracker_current_health_percent%      → 返回血量百分比，如 "75.5"
%axsentitytracker_slot_2_display_name%         → 返回第 2 个 Boss 的名称
%axsentitytracker_slot_1_top_1_name%           → 返回第 1 个 Boss 伤害排行第 1 名的玩家名
%axsentitytracker_boss_count%                  → 返回该玩家视野中的 Boss 数量
%axsentitytracker_total_boss_count%            → 返回全服正在追踪的 Boss 总数
```

### 最近结算数据

| 占位符 | 说明 |
| --- | --- |
| `%axsentitytracker_last_<字段>%` | 获取该玩家参与的最近一次 Boss 击杀结算的信息 |

常用的 `<字段>` 值：`rank`（玩家排名）、`damage`（伤害值）、`boss_name`（Boss 名称）、`total_participants`（总参与人数）等。

**使用示例**：
```
%axsentitytracker_last_rank%                   → 返回最近结算中的排名，如 "1"
%axsentitytracker_last_damage%                 → 返回造成的伤害值
%axsentitytracker_last_boss_name%              → 返回被击杀的 Boss 名称
```

---

## Essentials 占位符

前缀：`%axsess_<字段>%`

| 占位符 | 返回值 | 说明 |
| --- | --- | --- |
| `%axsess_afk%` | `true`/`false` | 玩家是否处于 AFK 状态 |
| `%axsess_afk_symbol%` | 文本 | AFK 标记符号（如 `&7[AFK]`），未 AFK 时返回空 |
| `%axsess_vanish%` | `true`/`false` | 玩家是否处于隐身状态 |
| `%axsess_vanish_symbol%` | 文本 | 隐身标记符号（如 `&b[V]`），未隐身时返回空 |
| `%axsess_flying%` | `true`/`false` | 玩家是否正在飞行 |
| `%axsess_god%` | `true`/`false` | 玩家是否处于无敌模式 |
| `%axsess_nick%` | 文本 | 玩家昵称（未设置则返回玩家显示名） |

**使用示例**：
```
%axsess_afk%                                 → 返回 "true"
%axsess_vanish_symbol%                       → 返回 "&b[V]"
%axsess_nick%                                → 返回 "&6[VIP]&r Steve"
```

---

## Tab 占位符

前缀：`%axstab_<字段>%`（可通过 `settings.papi.expansion-id` 自定义 identifier）

### 全局/玩家维度（无 defId 前缀）

| 占位符 | 返回值 | 说明 |
| --- | --- | --- |
| `%axstab_ping%` | 数字 | 玩家当前 ping 值（毫秒） |
| `%axstab_ping_icon%` | 文本 | 根据 ping 值返回的信号图标 |
| `%axstab_pvp%` | `true`/`false` | 玩家是否处于 PvP 状态 |
| `%axstab_pvp_color%` | 文本 | PvP 状态对应颜色代码（由 `style.pvp-highlight.color` 配置，未启用或不在窗口时返回空） |
| `%axstab_vanished%` | `true`/`false` | 玩家是否处于隐身状态（公共可见性） |
| `%axstab_vanish_color%` | 文本 | 隐身状态对应颜色代码（由 `style.vanish-grey.color` 配置，未启用或未隐身时返回空） |
| `%axstab_uuid%` | 文本 | 玩家 UUID；`privacy.hide-uuid: true` 时只显示前 8 位 + `...` |
| `%axstab_ip%` | 文本 | 玩家 IP 地址；`privacy.hide-ip: true` 时隐藏最后一段 |

### 按 Definition 维度

格式：`%axstab_<defId>_<指标>%`

| 占位符 | 返回值 | 说明 |
| --- | --- | --- |
| `%axstab_<defId>_count%` | 数字 | 本服当前 definition 的可见玩家数（已应用 filters/pinned/maxEntries） |
| `%axstab_<defId>_total%` | 数字 | 本服 + 所有跨服节点合计可见玩家数 |
| `%axstab_<defId>_rank%` | 数字 | 当前玩家在本服排序中的位次（1 起；不可见返回 0） |
| `%axstab_<defId>_view%` | 文本 | 当前玩家所在的 view 名称（默认 `default`） |
| `%axstab_<defId>_page%` | 数字 | 当前玩家在该 definition 的页码（0 起） |

**使用示例**：
```
%axstab_ping%                                → 返回 "32"
%axstab_pvp_color%                           → 返回 "&c"
%axstab_default_count%                       → 返回 "25"
%axstab_default_rank%                        → 返回 "1"
```

---

## Title 占位符

前缀：`%axstitle_<字段>%`

### 统计信息

| 占位符 | 返回值 | 说明 |
| --- | --- | --- |
| `%axstitle_owned_count%` | 数字 | 玩家当前拥有的称号总数 |
| `%axstitle_hidden_count%` | 数字 | 玩家已隐藏的称号数量 |

### 聊天/Tab 前后缀（按称号组获取）

用于在聊天或 Tab 列表中显示玩家装备的称号。`<组ID>` 是称号配置中定义的 group ID。

| 占位符 | 返回值 | 说明 |
| --- | --- | --- |
| `%axstitle_chat_<组ID>_prefix%` | 文本 | 该组已装备称号的聊天前缀，未装备时返回空 |
| `%axstitle_chat_<组ID>_suffix%` | 文本 | 该组已装备称号的聊天后缀 |
| `%axstitle_tab_<组ID>_prefix%` | 文本 | 该组已装备称号的 Tab 前缀 |
| `%axstitle_tab_<组ID>_suffix%` | 文本 | 该组已装备称号的 Tab 后缀 |

### 装备状态

| 占位符 | 返回值 | 说明 |
| --- | --- | --- |
| `%axstitle_equipped_<组ID>_id%` | 文本 | 该组已装备称号的 ID，未装备时返回空 |
| `%axstitle_equipped_<组ID>_name%` | 文本 | 该组已装备称号的显示名称 |
| `%axstitle_equipped_<组ID>_group%` | 文本 | 该组已装备称号所属组的显示名 |
| `%axstitle_equipped_<组ID>_quality%` | 文本 | 该组已装备称号的品质名 |

### 称号查询

| 占位符 | 返回值 | 说明 |
| --- | --- | --- |
| `%axstitle_owned_<称号ID>%` | `true`/`false` | 玩家是否拥有指定称号 |
| `%axstitle_hidden_<称号ID>%` | `true`/`false` | 玩家是否隐藏了指定称号 |
| `%axstitle_remaining_<称号ID>%` | 数字/文本 | 称号剩余有效时间（毫秒），永久称号返回 `永久`，未拥有返回空 |

### 属性加成

| 占位符 | 返回值 | 说明 |
| --- | --- | --- |
| `%axstitle_display_attr_<属性键>%` | 数字 | 当前展示中称号的指定属性加成值 |
| `%axstitle_collection_attr_<属性键>%` | 数字 | 收藏加成（已拥有称号的图鉴属性加成） |
| `%axstitle_total_attr_<属性键>%` | 数字 | 展示 + 收藏的总属性加成 |

**使用示例**：
```
%axstitle_chat_combat_prefix%                → 返回 "combat" 组已装备称号的聊天前缀
%axstitle_equipped_combat_id%                → 返回 "combat" 组已装备的称号 ID
%axstitle_owned_勇者之证%                     → 返回 "true" 或 "false"
%axstitle_remaining_活动限定%                  → 返回剩余毫秒数或 "永久"
%axstitle_total_attr_attack%                 → 返回攻击属性加成值
```

---

## RGB 占位符

前缀：`%axsrgb_<条目ID>%`

> **注意**：此模块**必须安装 PAPI** 才能使用，因为渐变文本的渲染依赖占位符替换。

| 占位符 | 返回值 | 说明 |
| --- | --- | --- |
| `%axsrgb_<条目ID>%` | 彩色文本 | 渲染配置中定义的渐变文本条目。条目 ID 在 `ArcartXRGB.yml` 中配置 |

**使用示例**：
```
%axsrgb_server_name%                      → 返回经过渐变渲染的服务器名称文本
%axsrgb_vip_tag%                          → 返回渐变 VIP 标签
```

---

## OnlineRewards 占位符

前缀：`%axsonlinerewards_<字段>%`

### 个人在线数据

| 占位符 | 返回值 | 说明 |
| --- | --- | --- |
| `%axsonlinerewards_daily_minutes%` | 数字 | 今日在线分钟数（原始数值） |
| `%axsonlinerewards_weekly_minutes%` | 数字 | 本周在线分钟数 |
| `%axsonlinerewards_monthly_minutes%` | 数字 | 本月在线分钟数 |
| `%axsonlinerewards_total_minutes%` | 数字 | 总在线分钟数 |
| `%axsonlinerewards_daily_time%` | 文本 | 今日在线时间，格式化显示（如 `2小时30分`） |
| `%axsonlinerewards_weekly_time%` | 文本 | 本周在线时间（格式化） |
| `%axsonlinerewards_monthly_time%` | 文本 | 本月在线时间（格式化） |
| `%axsonlinerewards_total_time%` | 文本 | 总在线时间（格式化） |

### 签到数据

| 占位符 | 返回值 | 说明 |
| --- | --- | --- |
| `%axsonlinerewards_signin_signed_today%` | `true`/`false` | 今日是否已签到 |
| `%axsonlinerewards_signin_streak%` | 数字 | 连续签到天数 |
| `%axsonlinerewards_signin_total%` | 数字 | 累计签到天数 |

### 排行榜数据（不需要玩家上下文）

格式：`%axsonlinerewards_top_<范围>_<名次>_<字段>%`

- **范围**：`daily`（日）、`weekly`（周）、`monthly`（月）、`total`（总计）
- **名次**：1\~10
- **字段**：`name`（玩家名）、`minutes`（分钟数）、`time`（格式化时间）

**使用示例**：
```
%axsonlinerewards_daily_minutes%             → 返回 "150"（今日在线 150 分钟）
%axsonlinerewards_daily_time%                → 返回 "2小时30分"
%axsonlinerewards_signin_streak%             → 返回 "7"（连续签到 7 天）
%axsonlinerewards_top_daily_1_name%          → 返回今日在线排行第 1 名的玩家名
%axsonlinerewards_top_total_3_time%          → 返回总在线排行第 3 名的格式化时间
```

---

## Mail 占位符

前缀：`%axsmail_<字段>%`

| 占位符 | 返回值 | 说明 |
| --- | --- | --- |
| `%axsmail_unread_count%` | 数字 | 未读邮件数量。可用于在 Tab 或 HUD 上提示玩家有新邮件 |
| `%axsmail_claimable_count%` | 数字 | 有附件但尚未领取的邮件数量 |
| `%axsmail_total_count%` | 数字 | 收件箱中的邮件总数 |

**使用示例**：
```
%axsmail_unread_count%                       → 返回 "3"（有 3 封未读邮件）
%axsmail_claimable_count%                    → 返回 "1"（有 1 封可领取附件的邮件）
```

---

## Chat 占位符

前缀：`%axschat_<字段>%`

| 占位符 | 返回值 | 说明 |
| --- | --- | --- |
| `%axschat_current_channel%` | 文本 | 当前所在频道的 ID（如 `Global`、`Staff`） |
| `%axschat_current_channel_display%` | 文本 | 当前频道的显示名称（如 `全服`、`员工`） |
| `%axschat_reply_target%` | 文本 | 最近一次私聊对象的玩家名，没有时返回空 |
| `%axschat_spy_enabled%` | `true`/`false` | 社交监听是否开启 |
| `%axschat_ignore_count%` | 数字 | 已屏蔽的玩家数量 |
| `%axschat_muted%` | `true`/`false` | 当前是否处于被禁言状态 |

**使用示例**：
```
%axschat_current_channel_display%            → 返回 "全服"
%axschat_muted%                              → 返回 "false"（未被禁言）
```

---

## LoginView 占位符

前缀：`%axsloginview_<字段>%`

| 占位符 | 返回值 | 说明 |
| --- | --- | --- |
| `%axsloginview_account_type%` | `microsoft` / `littleskin` / `offline` | 玩家登录账号来源 |
| `%axsloginview_account_type_display%` | 文本 | 中文显示名：`微软正版` / `LittleSkin` / `离线` |
| `%axsloginview_account_type_name%` | 文本 | `account_type_display` 的兼容别名 |
| `%axsloginview_is_microsoft%` | `true` / `false` | 是否为微软正版账号 |
| `%axsloginview_is_littleskin%` | `true` / `false` | 是否为 LittleSkin 账号 |
| `%axsloginview_is_offline%` | `true` / `false` | 是否为离线账号 |
| `%axsloginview_is_premium%` | `true` / `false` | 是否为认证账号（微软正版或 LittleSkin） |

**使用示例**：
```
%axsloginview_account_type%                 → 返回 "microsoft"
%axsloginview_account_type_display%         → 返回 "微软正版"
%axsloginview_is_littleskin%                → 返回 "false"
```

**典型用途**：
- 在 EventPacket `conditions` 中按账号来源区分欢迎流程
- 在 Tab / 聊天前缀中显示玩家来源标签
- 与 QQBot / Mail / QuestGPS 联动时区分认证账号和离线账号

---

## Warehouse 占位符

前缀：`%axswarehouse_<字段>%`

### 仓库容量

| 占位符 | 返回值 | 说明 |
| --- | --- | --- |
| `%axswarehouse_total_items%` | 数字 | 个人仓库中的物品总数 |
| `%axswarehouse_personal_used%` | 数字 | 个人仓库已使用的格子数 |
| `%axswarehouse_personal_capacity%` | 数字 | 个人仓库的总容量（格子数） |

### 共享仓库

| 占位符 | 返回值 | 说明 |
| --- | --- | --- |
| `%axswarehouse_shared_owned_count%` | 数字 | 自己拥有的共享仓库数量 |
| `%axswarehouse_shared_joined_count%` | 数字 | 已加入的他人共享仓库数量 |

### 分类物品统计

| 占位符 | 返回值 | 说明 |
| --- | --- | --- |
| `%axswarehouse_category_<分类ID>_amount%` | 数字 | 指定分类中的物品数量。分类 ID 在仓库配置中定义 |

### 银行数据

| 占位符 | 返回值 | 说明 |
| --- | --- | --- |
| `%axswarehouse_bank_balance_<货币ID>%` | 数字 | 指定货币的银行余额（去除尾零） |
| `%axswarehouse_bank_fixed_active_<货币ID>%` | 数字 | 指定货币的活跃定期存款笔数 |
| `%axswarehouse_bank_fixed_matured_<货币ID>%` | 数字 | 指定货币的已到期定期存款笔数 |

**使用示例**：
```
%axswarehouse_total_items%                   → 返回 "42"（仓库中有 42 件物品）
%axswarehouse_personal_used%                 → 返回 "15"（已使用 15 格）
%axswarehouse_personal_capacity%             → 返回 "54"（最大 54 格）
%axswarehouse_category_weapon_amount%        → 返回 "weapon" 分类的物品数
%axswarehouse_bank_balance_gold%             → 返回 "10500"（金币余额）
%axswarehouse_bank_fixed_active_gold%        → 返回 "2"（2 笔活跃定期存款）
```

---

## Market 占位符

前缀：`%axsmarket_<字段>%`

| 占位符 | 返回值 | 说明 |
| --- | --- | --- |
| `%axsmarket_auction_count%` | 数字 | 当前活跃拍卖上架数量 |
| `%axsmarket_shop_count%` | 数字 | 已加载的系统商店数量 |
| `%axsmarket_recycle_count%` | 数字 | 回收表中的可回收物品条目数 |
| `%axsmarket_redis_status%` | 文本 | Redis 连接状态（`已连接`/`未启用`/`断开`） |
| `%axsmarket_my_listings%` | 数字 | 当前玩家的拍卖上架数量 |

**使用示例**：
```
%axsmarket_auction_count%                    → 返回 "128"（当前 128 件物品在售）
%axsmarket_redis_status%                     → 返回 "已连接"
%axsmarket_my_listings%                      → 返回 "3"（我有 3 件上架物品）
```

---

## QQBot 占位符

前缀：`%axsqqbot_<字段>%`

| 占位符 | 返回值 | 说明 |
| --- | --- | --- |
| `%axsqqbot_connected%` | `true`/`false` | OneBot 机器人是否连接 |
| `%axsqqbot_connected_display%` | 文本 | 连接状态的中文显示（`已连接`/`未连接`） |
| `%axsqqbot_is_bound%` | `true`/`false` | 当前玩家是否已绑定 QQ |
| `%axsqqbot_bound_qq%` | 数字 | 当前玩家绑定的 QQ 号（未绑定为空） |
| `%axsqqbot_bound_name%` | 文本 | 当前玩家绑定时记录的游戏名 |
| `%axsqqbot_group_count%` | 数字 | 已配置监听的 QQ 群数量 |

**使用示例**：
```
%axsqqbot_connected_display%                 → 返回 "已连接"
%axsqqbot_bound_qq%                          → 返回 "12345678"
%axsqqbot_is_bound%                          → 返回 "true"
```

---

## BattlePass 占位符

前缀：`%axsbattlepass_<字段>%`

| 占位符 | 返回值 | 说明 |
| --- | --- | --- |
| `%axsbattlepass_season%` | 文本 | 当前赛季 ID |
| `%axsbattlepass_season_display%` | 文本 | 当前赛季显示名称 |
| `%axsbattlepass_level%` | 数字 | 当前等级 |
| `%axsbattlepass_max_level%` | 数字 | 最大等级 |
| `%axsbattlepass_xp%` | 数字 | 当前 XP |
| `%axsbattlepass_xp_per_level%` | 数字 | 每级所需 XP |
| `%axsbattlepass_xp_needed%` | 数字 | 升级还需 XP |
| `%axsbattlepass_premium%` | 文本 | 高级版状态（`已激活`/`未激活`） |
| `%axsbattlepass_deluxe%` | 文本 | 典藏版状态（`已激活`/`未激活`） |
| `%axsbattlepass_tier%` | 文本 | 当前通行证层级（`免费`/`高级`/`典藏`） |
| `%axsbattlepass_active_tasks%` | 数字 | 当前未完成的任务数量 |

**使用示例**：
```
%axsbattlepass_level%                         → 返回 "12"
%axsbattlepass_tier%                          → 返回 "高级"
%axsbattlepass_active_tasks%                  → 返回 "5"
```

---

## 反向消费 PAPI

部分 AXS 模块会**读取其他插件的 PAPI 占位符**作为数据来源，这不需要额外配置——只需确保对应的占位符插件已安装。

| 模块 | 配置字段 | 用途说明 |
| --- | --- | --- |
| Tab | `tabs.<id>.pack` / `sort-papi-key` | 在 Tab 列表中为每个玩家渲染自定义行，并按占位符值排序 |
| EventPacket | `rules.<id>.placeholder` / `rules.<id>.conditions` | 监控占位符值变化，或在触发后按条件表达式判断是否执行规则动作 |
| Mail | `currencies.<id>.balance-placeholder` | 通过占位符读取玩家的货币余额，用于购买邮票等消费操作 |
| EntityTracker | `bosses.<id>.title-format` | Boss 名称格式中可嵌入占位符，先替换内置变量再走 PAPI |
