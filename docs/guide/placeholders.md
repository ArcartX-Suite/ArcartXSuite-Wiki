# PlaceholderAPI 占位符速查

本页列出 ArcartXSuite 对外输出的**全部 PlaceholderAPI 占位符**，包括每个字段的含义、返回值类型和使用示例。

> **前置条件**：服务器需安装 [PlaceholderAPI](https://www.spigotmc.org/resources/placeholderapi.6245/) 插件，AXS 会在启动时自动注册占位符。

---

## 占位符总览

AXS 共有 **13 个模块** 对外输出 PAPI 占位符：

| 模块 | 前缀 | 必装 PAPI? | 说明 |
| --- | --- | --- | --- |
| [AfkReward](/modules/afkreward) | `%axsafk_*%` | 可选 | 区域挂机状态、时长、奖励次数、区域人数 |
| [EntityTracker](/modules/entitytracker) | `%AXSentitytracker_*%` | 可选 | Boss 追踪、伤害排行、结算数据 |
| [Essentials](/modules/essentials) | `%AXSess_*%` | 可选 | AFK、隐身、飞行、昵称等玩家状态 |
| [Title](/modules/title) | `%AXStitle_*%` | 可选 | 称号前缀/后缀、装备状态、属性加成 |
| [RGB](/modules/rgb) | `%arcartrgb_*%` | **必装** | 动态渐变彩色文本渲染 |
| [OnlineRewards](/modules/onlinerewards) | `%AXSonlinerewards_*%` | 可选 | 在线时长、签到状态、排行榜 |
| [Mail](/modules/mail) | `%AXSmail_*%` | 可选 | 邮箱未读数、可领取数 |
| [Chat](/modules/chat) | `%AXSchat_*%` | 可选 | 聊天频道、禁言状态 |
| [LoginView](/modules/loginview) | `%AXSloginview_*%` | 可选 | 账号来源识别、正版/LittleSkin/离线判断 |
| [Warehouse](/modules/warehouse) | `%AXSwarehouse_*%` | 可选 | 仓库容量、银行余额 |
| [Market](/modules/market) | `%AXSmarket_*%` | 可选 | 拍卖数量、商店数、回收数、Redis 状态 |
| [QQBot](/modules/qqbot) | `%AXSqqbot_*%` | 可选 | OneBot 连接状态、玩家 QQ 绑定信息、群数量 |
| [Tab](/modules/tab) | `%AXStab_*%` | 可选 | Tab 在线列表变量、玩家状态 |

---

## AfkReward 占位符

前缀：`%axsafk_<字段>%`

| 占位符 | 返回值 | 说明 |
| --- | --- | --- |
| `%axsafk_type%` | 文本 | 挂机类型：`区域挂机` / `未挂机` |
| `%axsafk_area%` | 文本 | 当前所在区域名称，未挂机时返回空 |
| `%axsafk_time%` | 文本 | 当前区域挂机时长，格式化（如 `15分32秒`） |
| `%axsafk_total_time%` | 文本 | 累计总挂机时长（格式化） |
| `%axsafk_today%` | 数字 | 今日已获得奖励次数 |
| `%axsafk_total%` | 数字 | 累计获得奖励总次数 |
| `%axsafk_players%` | 数字 | 当前区域同时挂机人数 |
| `%axsafk_next%` | 数字 | 距离下次奖励的剩余秒数 |

**使用示例**：
```
%axsafk_area%          → 返回 "温泉"
%axsafk_time%          → 返回 "15分32秒"
%axsafk_players%       → 返回 "12"
%axsafk_next%          → 返回 "742"
```

---

## EntityTracker 占位符

前缀：`%AXSentitytracker_<字段>%`

### 全局信息（不需要玩家上下文）

| 占位符 | 返回值 | 说明 |
| --- | --- | --- |
| `%AXSentitytracker_sort_mode%` | 文本 | 当前 Boss 排序模式（如 `SPAWN_ORDER`） |
| `%AXSentitytracker_max_visible_bars%` | 数字 | 配置中允许同时显示的最大 Boss 血条数 |
| `%AXSentitytracker_configured_boss_count%` | 数字 | 配置文件中定义的 Boss 总数 |
| `%AXSentitytracker_damage_ranking_boss_count%` | 数字 | 开启了伤害排行功能的 Boss 数量 |
| `%AXSentitytracker_max_damage_ranking_entries%` | 数字 | 伤害排行榜最大展示条目数 |
| `%AXSentitytracker_active_session_count%` | 数字 | 当前活跃的 Boss 战斗会话数 |
| `%AXSentitytracker_active_viewer_count%` | 数字 | 当前正在观察 Boss 的玩家总数 |

### 玩家视野中的 Boss（需要玩家上下文）

**当前 Boss**（视野中第 1 个 Boss 的快捷别名）：

| 占位符 | 说明 |
| --- | --- |
| `%AXSentitytracker_current_<字段>%` | 等同于 `slot_1_<字段>`，获取视野中第一个 Boss 的信息 |

**按槽位获取**（视野中第 N 个 Boss）：

| 占位符 | 说明 |
| --- | --- |
| `%AXSentitytracker_slot_<N>_<字段>%` | 获取第 N 个 Boss 的信息。N 从 1 开始，最大值由 `max_visible_bars` 决定 |

常用的 `<字段>` 值包括：`display_name`（Boss 名称）、`health_percent`（血量百分比）、`mob_id`（MythicMobs ID）、`viewer_rank`（玩家伤害排名）、`viewer_damage`（玩家累计伤害）、`top_<排名>_name`（排行第 N 名的玩家名）、`top_<排名>_damage`（排行第 N 名的伤害值）等。

**使用示例**：
```
%AXSentitytracker_current_display_name%        → 返回视野中第一个 Boss 的名称
%AXSentitytracker_current_health_percent%      → 返回血量百分比，如 "75.5"
%AXSentitytracker_slot_2_display_name%         → 返回第 2 个 Boss 的名称
%AXSentitytracker_slot_1_top_1_name%           → 返回第 1 个 Boss 伤害排行第 1 名的玩家名
%AXSentitytracker_boss_count%                  → 返回该玩家视野中的 Boss 数量
%AXSentitytracker_total_boss_count%            → 返回全服正在追踪的 Boss 总数
```

### 最近结算数据

| 占位符 | 说明 |
| --- | --- |
| `%AXSentitytracker_last_<字段>%` | 获取该玩家参与的最近一次 Boss 击杀结算的信息 |

常用的 `<字段>` 值：`rank`（玩家排名）、`damage`（伤害值）、`boss_name`（Boss 名称）、`total_participants`（总参与人数）等。

**使用示例**：
```
%AXSentitytracker_last_rank%                   → 返回最近结算中的排名，如 "1"
%AXSentitytracker_last_damage%                 → 返回造成的伤害值
%AXSentitytracker_last_boss_name%              → 返回被击杀的 Boss 名称
```

---

## Essentials 占位符

前缀：`%AXSess_<字段>%`

| 占位符 | 返回值 | 说明 |
| --- | --- | --- |
| `%AXSess_afk%` | `true`/`false` | 玩家是否处于 AFK 状态 |
| `%AXSess_afk_symbol%` | 文本 | AFK 标记符号（如 `&7[AFK]`），未 AFK 时返回空 |
| `%AXSess_vanish%` | `true`/`false` | 玩家是否处于隐身状态 |
| `%AXSess_vanish_symbol%` | 文本 | 隐身标记符号（如 `&b[V]`），未隐身时返回空 |
| `%AXSess_flying%` | `true`/`false` | 玩家是否正在飞行 |
| `%AXSess_god%` | `true`/`false` | 玩家是否处于无敌模式 |
| `%AXSess_nick%` | 文本 | 玩家昵称（未设置则返回玩家显示名） |

**使用示例**：
```
%AXSess_afk%                                 → 返回 "true"
%AXSess_vanish_symbol%                       → 返回 "&b[V]"
%AXSess_nick%                                → 返回 "&6[VIP]&r Steve"
```

---

## Tab 占位符

前缀：`%AXStab_<字段>%`（可通过 `settings.papi.expansion-id` 自定义 identifier）

### 全局/玩家维度（无 defId 前缀）

| 占位符 | 返回值 | 说明 |
| --- | --- | --- |
| `%AXStab_ping%` | 数字 | 玩家当前 ping 值（毫秒） |
| `%AXStab_ping_icon%` | 文本 | 根据 ping 值返回的信号图标 |
| `%AXStab_pvp%` | `1`/`0` | 玩家是否处于 PvP 状态 |
| `%AXStab_pvp_color%` | 文本 | PvP 状态对应颜色代码（由 `style.pvp-color` 配置） |
| `%AXStab_vanished%` | `1`/`0` | 玩家是否处于隐身状态（公共可见性） |
| `%AXStab_vanish_color%` | 文本 | 隐身状态对应颜色代码（由 `style.vanish-color` 配置，若 `vanish-grey-enabled` 关闭则返回空） |
| `%AXStab_uuid%` | 文本 | 玩家 UUID（若 `privacy.hide-uuid` 开启则返回空） |
| `%AXStab_ip%` | 文本 | 玩家 IP 地址（若 `privacy.hide-ip` 开启则返回空） |

### 按 Definition 维度

格式：`%AXStab_<defId>_<指标>%`

| 占位符 | 返回值 | 说明 |
| --- | --- | --- |
| `%AXStab_<defId>_count%` | 数字 | 本服当前 definition 的可见玩家数（已应用 filters/pinned/maxEntries） |
| `%AXStab_<defId>_total%` | 数字 | 本服 + 所有跨服节点合计可见玩家数 |
| `%AXStab_<defId>_rank%` | 数字 | 当前玩家在本服排序中的位次（1 起；不可见返回 0） |
| `%AXStab_<defId>_view%` | 文本 | 当前玩家所在的 view 名称（默认 `default`） |
| `%AXStab_<defId>_page%` | 数字 | 当前玩家在该 definition 的页码（0 起） |

**使用示例**：
```
%AXStab_ping%                                → 返回 "32"
%AXStab_pvp_color%                           → 返回 "&c"
%AXStab_default_count%                       → 返回 "25"
%AXStab_default_rank%                        → 返回 "1"
```

---

## Title 占位符

前缀：`%AXStitle_<字段>%`

### 统计信息

| 占位符 | 返回值 | 说明 |
| --- | --- | --- |
| `%AXStitle_owned_count%` | 数字 | 玩家当前拥有的称号总数 |
| `%AXStitle_hidden_count%` | 数字 | 玩家已隐藏的称号数量 |

### 聊天/Tab 前后缀（按称号组获取）

用于在聊天或 Tab 列表中显示玩家装备的称号。`<组ID>` 是称号配置中定义的 group ID。

| 占位符 | 返回值 | 说明 |
| --- | --- | --- |
| `%AXStitle_chat_<组ID>_prefix%` | 文本 | 该组已装备称号的聊天前缀，未装备时返回空 |
| `%AXStitle_chat_<组ID>_suffix%` | 文本 | 该组已装备称号的聊天后缀 |
| `%AXStitle_tab_<组ID>_prefix%` | 文本 | 该组已装备称号的 Tab 前缀 |
| `%AXStitle_tab_<组ID>_suffix%` | 文本 | 该组已装备称号的 Tab 后缀 |

### 装备状态

| 占位符 | 返回值 | 说明 |
| --- | --- | --- |
| `%AXStitle_equipped_<组ID>_id%` | 文本 | 该组已装备称号的 ID，未装备时返回空 |
| `%AXStitle_equipped_<组ID>_name%` | 文本 | 该组已装备称号的显示名称 |
| `%AXStitle_equipped_<组ID>_group%` | 文本 | 该组已装备称号所属组的显示名 |
| `%AXStitle_equipped_<组ID>_quality%` | 文本 | 该组已装备称号的品质名 |

### 称号查询

| 占位符 | 返回值 | 说明 |
| --- | --- | --- |
| `%AXStitle_owned_<称号ID>%` | `true`/`false` | 玩家是否拥有指定称号 |
| `%AXStitle_hidden_<称号ID>%` | `true`/`false` | 玩家是否隐藏了指定称号 |
| `%AXStitle_remaining_<称号ID>%` | 数字/文本 | 称号剩余有效时间（毫秒），永久称号返回 `永久`，未拥有返回空 |

### 属性加成

| 占位符 | 返回值 | 说明 |
| --- | --- | --- |
| `%AXStitle_display_attr_<属性键>%` | 数字 | 当前展示中称号的指定属性加成值 |
| `%AXStitle_collection_attr_<属性键>%` | 数字 | 收藏加成（已拥有称号的图鉴属性加成） |
| `%AXStitle_total_attr_<属性键>%` | 数字 | 展示 + 收藏的总属性加成 |

**使用示例**：
```
%AXStitle_chat_combat_prefix%                → 返回 "combat" 组已装备称号的聊天前缀
%AXStitle_equipped_combat_id%                → 返回 "combat" 组已装备的称号 ID
%AXStitle_owned_勇者之证%                     → 返回 "true" 或 "false"
%AXStitle_remaining_活动限定%                  → 返回剩余毫秒数或 "永久"
%AXStitle_total_attr_attack%                 → 返回攻击属性加成值
```

---

## RGB 占位符

前缀：`%arcartrgb_<条目ID>%`

> **注意**：此模块**必须安装 PAPI** 才能使用，因为渐变文本的渲染依赖占位符替换。

| 占位符 | 返回值 | 说明 |
| --- | --- | --- |
| `%arcartrgb_<条目ID>%` | 彩色文本 | 渲染配置中定义的渐变文本条目。条目 ID 在 `ArcartXRGB.yml` 中配置 |

**使用示例**：
```
%arcartrgb_server_name%                      → 返回经过渐变渲染的服务器名称文本
%arcartrgb_vip_tag%                          → 返回渐变 VIP 标签
```

---

## OnlineRewards 占位符

前缀：`%AXSonlinerewards_<字段>%`

### 个人在线数据

| 占位符 | 返回值 | 说明 |
| --- | --- | --- |
| `%AXSonlinerewards_daily_minutes%` | 数字 | 今日在线分钟数（原始数值） |
| `%AXSonlinerewards_weekly_minutes%` | 数字 | 本周在线分钟数 |
| `%AXSonlinerewards_monthly_minutes%` | 数字 | 本月在线分钟数 |
| `%AXSonlinerewards_total_minutes%` | 数字 | 总在线分钟数 |
| `%AXSonlinerewards_daily_time%` | 文本 | 今日在线时间，格式化显示（如 `2小时30分`） |
| `%AXSonlinerewards_weekly_time%` | 文本 | 本周在线时间（格式化） |
| `%AXSonlinerewards_monthly_time%` | 文本 | 本月在线时间（格式化） |
| `%AXSonlinerewards_total_time%` | 文本 | 总在线时间（格式化） |

### 签到数据

| 占位符 | 返回值 | 说明 |
| --- | --- | --- |
| `%AXSonlinerewards_signin_signed_today%` | `true`/`false` | 今日是否已签到 |
| `%AXSonlinerewards_signin_streak%` | 数字 | 连续签到天数 |
| `%AXSonlinerewards_signin_total%` | 数字 | 累计签到天数 |

### 排行榜数据（不需要玩家上下文）

格式：`%AXSonlinerewards_top_<范围>_<名次>_<字段>%`

- **范围**：`daily`（日）、`weekly`（周）、`monthly`（月）、`total`（总计）
- **名次**：1\~10
- **字段**：`name`（玩家名）、`minutes`（分钟数）、`time`（格式化时间）

**使用示例**：
```
%AXSonlinerewards_daily_minutes%             → 返回 "150"（今日在线 150 分钟）
%AXSonlinerewards_daily_time%                → 返回 "2小时30分"
%AXSonlinerewards_signin_streak%             → 返回 "7"（连续签到 7 天）
%AXSonlinerewards_top_daily_1_name%          → 返回今日在线排行第 1 名的玩家名
%AXSonlinerewards_top_total_3_time%          → 返回总在线排行第 3 名的格式化时间
```

---

## Mail 占位符

前缀：`%AXSmail_<字段>%`

| 占位符 | 返回值 | 说明 |
| --- | --- | --- |
| `%AXSmail_unread_count%` | 数字 | 未读邮件数量。可用于在 Tab 或 HUD 上提示玩家有新邮件 |
| `%AXSmail_claimable_count%` | 数字 | 有附件但尚未领取的邮件数量 |
| `%AXSmail_total_count%` | 数字 | 收件箱中的邮件总数 |

**使用示例**：
```
%AXSmail_unread_count%                       → 返回 "3"（有 3 封未读邮件）
%AXSmail_claimable_count%                    → 返回 "1"（有 1 封可领取附件的邮件）
```

---

## Chat 占位符

前缀：`%AXSchat_<字段>%`

| 占位符 | 返回值 | 说明 |
| --- | --- | --- |
| `%AXSchat_current_channel%` | 文本 | 当前所在频道的 ID（如 `Global`、`Staff`） |
| `%AXSchat_current_channel_display%` | 文本 | 当前频道的显示名称（如 `全服`、`员工`） |
| `%AXSchat_reply_target%` | 文本 | 最近一次私聊对象的玩家名，没有时返回空 |
| `%AXSchat_spy_enabled%` | `true`/`false` | 社交监听是否开启 |
| `%AXSchat_ignore_count%` | 数字 | 已屏蔽的玩家数量 |
| `%AXSchat_muted%` | `true`/`false` | 当前是否处于被禁言状态 |

**使用示例**：
```
%AXSchat_current_channel_display%            → 返回 "全服"
%AXSchat_muted%                              → 返回 "false"（未被禁言）
```

---

## LoginView 占位符

前缀：`%AXSloginview_<字段>%`

| 占位符 | 返回值 | 说明 |
| --- | --- | --- |
| `%AXSloginview_account_type%` | `microsoft` / `littleskin` / `offline` | 玩家登录账号来源 |
| `%AXSloginview_account_type_display%` | 文本 | 中文显示名：`微软正版` / `LittleSkin` / `离线` |
| `%AXSloginview_account_type_name%` | 文本 | `account_type_display` 的兼容别名 |
| `%AXSloginview_is_microsoft%` | `true` / `false` | 是否为微软正版账号 |
| `%AXSloginview_is_littleskin%` | `true` / `false` | 是否为 LittleSkin 账号 |
| `%AXSloginview_is_offline%` | `true` / `false` | 是否为离线账号 |
| `%AXSloginview_is_premium%` | `true` / `false` | 是否为认证账号（微软正版或 LittleSkin） |

**使用示例**：
```
%AXSloginview_account_type%                 → 返回 "microsoft"
%AXSloginview_account_type_display%         → 返回 "微软正版"
%AXSloginview_is_littleskin%                → 返回 "false"
```

**典型用途**：
- 在 EventPacket `conditions` 中按账号来源区分欢迎流程
- 在 Tab / 聊天前缀中显示玩家来源标签
- 与 QQBot / Mail / QuestGPS 联动时区分认证账号和离线账号

---

## Warehouse 占位符

前缀：`%AXSwarehouse_<字段>%`

### 仓库容量

| 占位符 | 返回值 | 说明 |
| --- | --- | --- |
| `%AXSwarehouse_total_items%` | 数字 | 个人仓库中的物品总数 |
| `%AXSwarehouse_personal_used%` | 数字 | 个人仓库已使用的格子数 |
| `%AXSwarehouse_personal_capacity%` | 数字 | 个人仓库的总容量（格子数） |

### 共享仓库

| 占位符 | 返回值 | 说明 |
| --- | --- | --- |
| `%AXSwarehouse_shared_owned_count%` | 数字 | 自己拥有的共享仓库数量 |
| `%AXSwarehouse_shared_joined_count%` | 数字 | 已加入的他人共享仓库数量 |

### 分类物品统计

| 占位符 | 返回值 | 说明 |
| --- | --- | --- |
| `%AXSwarehouse_category_<分类ID>_amount%` | 数字 | 指定分类中的物品数量。分类 ID 在仓库配置中定义 |

### 银行数据

| 占位符 | 返回值 | 说明 |
| --- | --- | --- |
| `%AXSwarehouse_bank_balance_<货币ID>%` | 数字 | 指定货币的银行余额（去除尾零） |
| `%AXSwarehouse_bank_fixed_active_<货币ID>%` | 数字 | 指定货币的活跃定期存款笔数 |
| `%AXSwarehouse_bank_fixed_matured_<货币ID>%` | 数字 | 指定货币的已到期定期存款笔数 |

**使用示例**：
```
%AXSwarehouse_total_items%                   → 返回 "42"（仓库中有 42 件物品）
%AXSwarehouse_personal_used%                 → 返回 "15"（已使用 15 格）
%AXSwarehouse_personal_capacity%             → 返回 "54"（最大 54 格）
%AXSwarehouse_category_weapon_amount%        → 返回 "weapon" 分类的物品数
%AXSwarehouse_bank_balance_gold%             → 返回 "10500"（金币余额）
%AXSwarehouse_bank_fixed_active_gold%        → 返回 "2"（2 笔活跃定期存款）
```

---

## Market 占位符

前缀：`%AXSmarket_<字段>%`

| 占位符 | 返回值 | 说明 |
| --- | --- | --- |
| `%AXSmarket_auction_count%` | 数字 | 当前活跃拍卖上架数量 |
| `%AXSmarket_shop_count%` | 数字 | 已加载的系统商店数量 |
| `%AXSmarket_recycle_count%` | 数字 | 回收表中的可回收物品条目数 |
| `%AXSmarket_redis_status%` | 文本 | Redis 连接状态（`已连接`/`未启用`/`断开`） |
| `%AXSmarket_my_listings%` | 数字 | 当前玩家的拍卖上架数量 |

**使用示例**：
```
%AXSmarket_auction_count%                    → 返回 "128"（当前 128 件物品在售）
%AXSmarket_redis_status%                     → 返回 "已连接"
%AXSmarket_my_listings%                      → 返回 "3"（我有 3 件上架物品）
```

---

## QQBot 占位符

前缀：`%AXSqqbot_<字段>%`

| 占位符 | 返回值 | 说明 |
| --- | --- | --- |
| `%AXSqqbot_connected%` | `true`/`false` | OneBot 机器人是否连接 |
| `%AXSqqbot_connected_display%` | 文本 | 连接状态的中文显示（`已连接`/`未连接`） |
| `%AXSqqbot_is_bound%` | `true`/`false` | 当前玩家是否已绑定 QQ |
| `%AXSqqbot_bound_qq%` | 数字 | 当前玩家绑定的 QQ 号（未绑定为空） |
| `%AXSqqbot_bound_name%` | 文本 | 当前玩家绑定时记录的游戏名 |
| `%AXSqqbot_group_count%` | 数字 | 已配置监听的 QQ 群数量 |

**使用示例**：
```
%AXSqqbot_connected_display%                 → 返回 "已连接"
%AXSqqbot_bound_qq%                          → 返回 "12345678"
%AXSqqbot_is_bound%                          → 返回 "true"
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
