---
title: Mail 邮箱插件 | ArcartX-Suite Minecraft
description: ArcartX-Suite Mail 邮件系统，玩家写信、管理员预设派发、CDK 兑换、物品附件、跨服广播，我的世界服务器邮件插件。
---

# Mail 邮箱

::: tip 付费模块
本模块为付费模块。授权由 [云端平台](/guide/cloud-modules) 统一管理：在 [cloud.021209.xyz](https://cloud.021209.xyz) 购买/领取授权后，于「装备模块」页面勾选到对应服务器即可，无需填写 `password` 或 `license.yml`。
:::
## 功能定位

完整的游戏内邮箱系统：玩家写信、管理员预设派发、CDK 兑换、物品附件、货币手续费、跨服广播。

### 核心特性

**收件箱与写信：**
- **ArcartX UI 界面**：收件箱、写信、日志、管理四个独立 UI 界面
- **玩家写信**：支持标题、正文、物品附件和货币附件，可配置最大附件数和字数限制
- **手续费**：固定手续费 + 物品附件手续费 + 货币附件税率，每种货币独立税率
- **物品附件**：支持原版、NeigeItems、MythicMobs、MMOItems 等物品库产物
- **一键领取**：`/mail claimall` 批量领取所有未领取附件
- **自动过期**：可配置邮件保留天数、已领取/已删除邮件保留天数

**预设邮件：**
- **预设定义**：在 `data/mail/presets/*.yml` 中定义邮件模板，包含标题、正文、附件、命令
- **批量派发**：`/axs mail preset send` 支持指定玩家、全部在线、全部已注册三种目标
- **模块联动**：OnlineRewards 签到奖励、EntityTracker Boss 结算等均可通过预设邮件发奖

**CDK 系统：**
- **创建兑换码**：手动指定或自动生成，配置最大领取次数和有效期
- **玩家兑换**：`/mail cdk <code>` 输入兑换码领取对应预设邮件
- **管理工具**：查看、列表、删除 CDK，支持分页浏览

**安全与跨服：**
- **敏感词过滤**：写信内容支持敏感词、正则、物品材质和 Lore 正则过滤
- **禁止物品**：可按材质黑名单禁止特定物品作为附件
- **发信限制**：冷却时间、禁止自发、离线收信开关
- **多货币支持**：Vault 金币、PlayerPoints 点券、自定义货币（PAPI + 命令桥接）
- **CrossServer 跨服刷新**：其他子服发信/收信后广播 `refresh:<uuid>`，在线玩家收件箱自动刷新
- **数据持久化**：SQLite 或 MySQL

## 依赖

| 类型 | 依赖 | 作用 | 缺少时表现 |
| --- | --- | --- | --- |
| 必需 | ArcartX | 收件箱、写信、日志、管理 UI 和附件槽交互 | 模块无法提供可视化邮箱 |
| 必需 | PlaceholderAPI | `placeholder-command` 货币、条件判断和邮件文本变量 | 模块不会加载（`external-depends` 声明） |
| 可选 | Vault | 金币附件、手续费或 Vault 货币扣费 | Vault 货币功能关闭，物品邮件仍可用 |
| 可选 | PlayerPoints | 点券附件或点券扣费 | PlayerPoints 货币功能关闭 |
| 可选 | NeigeItems / MythicMobs / MMOItems | 物品附件来自对应物品库时保留识别信息 | 原版 ItemStack 附件正常；物品库专属识别不可用 |
| 可选 | 宿主 cross-server + Redis | 多服邮件 UI 刷新通知 | 单服邮件正常，跨服刷新关闭 |
| 可选 | MySQL 服务 | 多服共享邮件数据 | 默认 SQLite 可用；多服共享建议改 MySQL |

## 启用步骤

```yaml
modules:
  mail:
    enabled: true
```

## 配置

主配置文件：`data/mail/ArcartXMail.yml`（首次启动自动导出）。

### 调试与存储

```yaml
settings:
  debug: false              # 是否输出 Mail 模块调试日志

storage:
  mode: sqlite          # sqlite | mysql
  sqlite:
    file: mail.db
  mysql:
    host: 127.0.0.1
    port: 3306
    database: arcartxsuite
    username: root
    password: ""
  pool-size: 4

cross-server:
  enabled: false        # 多服 UI 刷新通知；数据本身需 MySQL 共享库
```

### UI 四界面

```yaml
ui:
  inbox-ui-id: AXS:mail_inbox
  compose-ui-id: AXS:mail_compose
  logs-ui-id: AXS:mail_logs
  admin-ui-id: AXS:mail_admin
  register-ui-on-enable: true
  overwrite-ui-files: false
  compose-inventory-title: AXS Mail Compose
  notify-card-id: "axs_mail_notify"   # 留空关闭新邮件聊天卡片
  notify-char-width-full: 31          # 聊天卡片全角字符宽度
  notify-char-width-half: 18          # 聊天卡片半角字符宽度
  notify-line-height: 36              # 聊天卡片行高
  notify-max-line-width: 750          # 聊天卡片最大行宽
  notify-text-offset-x: 170           # 聊天卡片文本 X 偏移
  notify-pad-right: 100               # 聊天卡片右侧内边距
  notify-base-height: 100             # 聊天卡片基础高度
  notify-min-width: 550               # 聊天卡片最小宽度
```

### 玩家写信与手续费

```yaml
player-send:
  enabled: true
  require-permission: true
  cooldown-seconds: 120
  base-fee: 0.0
  item-fee: 0.0
  fee-currency: money
  vault-tax-rate: 0.05             # 旧版兼容键；等价于 attachment-tax-rates.money
  attachment-tax-rates:
    money: 0.05
    points: 0.0
  allow-self-send: false
  allow-offline-send: true
  allow-vault-attachment: true     # 是否允许附带 Vault 金币
  max-attachments: 6
  subject-max-length: 48
  body-max-length: 400
```

货币通过宿主的 `CurrencyBridgeManager` 统一注册和管理，Mail 模块直接引用货币 ID（如 `money`、`points`）。常见货币提供者如下：

| 提供者 | 说明 | 依赖插件 |
| --- | --- | --- |
| `vault` | Vault 金币 | Vault |
| `playerpoints` | PlayerPoints 点券 | PlayerPoints |
| `placeholder-command` | 自定义货币（PAPI 余额 + 命令扣费/充值） | PlaceholderAPI |

`placeholder-command` 提供者需要配置余额占位符和扣费/充值命令，具体注册方式参见宿主配置。

### 审核与保留

```yaml
moderation:
  blocked-words: []
  blocked-patterns: []
  blocked-materials: [BEDROCK, COMMAND_BLOCK, BARRIER]
  blocked-lore-patterns: []

retention:
  cleanup-interval-ticks: 1200
  default-expire-after-days: 15
  claimed-retention-days: 7
  deleted-retention-days: 7
  allow-delete-with-unclaimed-attachments: false
```

### 预设邮件目录

```yaml
presets:
  directory: presets    # 相对 data/mail/
```

预设文件示例 `data/mail/presets/starter.yml`：

```yaml
preset:
  id: "starter"                   # 可选，默认使用文件名（不含 .yml）
  enabled: true                   # 是否启用该预设
  display-name: "新手欢迎信"       # 可选，默认与 ID 相同
  subject: "欢迎来到服务器"        # 邮件主题，默认使用 display-name
  body: |-
    这是一封 AXS Mail 模块的示例邮件。
    你可以通过 /axs mail preset send starter <player> 进行派发。
  expires-after-days: 15           # 邮件过期天数（默认 15）

  # 物品附件：material 为 Bukkit 材质名，支持 name/lore
  item-attachments:
    - material: "APPLE"
      amount: 8
      name: "§a欢迎苹果"
      lore:
        - "§7来自 AXS Mail 示例预设"

  # 货币附件：currency 对应 currencies 节中的货币 ID
  # currency-attachments:
  #   - currency: "money"
  #     amount: 88
  #     description: "新手金币礼包"

  # 兼容旧版的 Vault 货币附件，等效于 currency: money
  vault-attachment: 0.0

  claim-commands:
    - "tell <player> 你领取了示例邮件奖励。"
  claim-conditions: []             # 见下文「领取条件」

  # 内嵌固定 CDK 定义（也可通过命令 /axs mail cdk create 创建）
  cdks:
    - code: "STARTER2026"
      enabled: true
      max-claims: 100              # 最大领取次数
      expires-after: "30d"         # 有效期，支持 s/m/h/d/w
```

预设字段一览：

| 字段 | 类型 | 默认值 | 说明 |
| --- | --- | --- | --- |
| `id` | string | 文件名 | 预设唯一标识 |
| `enabled` | boolean | `true` | 是否启用 |
| `display-name` | string | 预设 ID | 管理命令与日志中的显示名 |
| `subject` | string | `display-name` | 邮件主题 |
| `body` | string | `""` | 邮件正文 |
| `expires-after-days` | int | `15` | 邮件自动过期天数 |
| `item-attachments` | list | `[]` | 物品附件，每项含 `material`、`amount`、`name`、`lore` |
| `currency-attachments` | list | `[]` | 货币附件，每项含 `currency`、`amount`、`description` |
| `vault-attachment` | double | `0` | 兼容旧版，等效于 `currency: money` 的附件 |
| `claim-commands` | list | `[]` | 领取时以控制台身份执行的命令；仅替换 `<player>`、`{player}`、`%player%` 三种玩家名 token，不等同于任意 PAPI 展开 |
| `cdks` | list | `[]` | 内嵌 CDK 列表；也可用 `cdk:` 定义单个 |

重载：`/axs reload mail`（同步预设与 CDK 到数据库）。

## 命令

### 管理命令（权限：`arcartxsuite.admin`）

| 命令 | 说明 |
| --- | --- |
| `/axs mail help` | 查看可用子命令 |
| `/axs mail status` | 查看邮箱模块、存储、预设和跨服状态 |
| `/axs mail open <玩家>` | 为在线玩家打开邮箱收件箱 |
| `/axs mail admin` | 打开管理 UI 界面（管理员查看全服邮件） |
| `/axs mail preset list` | 列出所有已加载的预设邮件 |
| `/axs mail preset send <预设ID> <目标>` | 按预设派发邮件。目标可填玩家名、`all-online` 或 `all-registered` |
| `/axs mail preset info <预设ID>` | 查看指定预设的详细信息 |
| `/axs mail preset delete <预设ID>` | 删除指定预设 |
| `/axs mail preset reload` | 重新加载预设文件并同步到数据库 |
| `/axs mail cdk create <预设ID> <兑换码\|auto> <最大领取数> <有效期>` | 创建 CDK 兑换码。`auto` 自动生成，有效期如 `1d`、`7d`、`permanent` |
| `/axs mail cdk info <兑换码>` | 查看 CDK 绑定预设、已领取次数、过期时间 |
| `/axs mail cdk list [页码]` | 分页查看所有已创建的 CDK |
| `/axs mail cdk delete <兑换码>` | 禁用并删除指定 CDK |

### 玩家命令（权限：`arcartxsuite.mail.use`，别名 `/axmail`、`/axsmail`）

| 命令 | 说明 |
| --- | --- |
| `/mail` 或 `/mail open` | 打开邮箱收件箱 |
| `/mail compose` | 打开写信界面，可给其他玩家写信并附带物品 |
| `/mail claimall` | 一键领取所有未领取邮件的附件和奖励 |
| `/mail deleteall` | 删除所有已读邮件 |
| `/mail cdk <兑换码>` | 使用兑换码领取对应邮件奖励 |

## PAPI

前缀：`%axsmail_*%`

| 占位符 | 返回值 | 说明 |
| --- | --- | --- |
| `%axsmail_unread_count%` | 数字 | 未读邮件数量 |
| `%axsmail_claimable_count%` | 数字 | 有附件但尚未领取的邮件数量 |
| `%axsmail_total_count%` | 数字 | 收件箱邮件总数 |

## 聊天卡片通知

当玩家在线时收到新邮件，Mail 模块会通过 ArcartX 聊天卡片在聊天栏推送通知。

配置项位于 `ArcartXMail.yml` 的 `ui` 节：

```yaml
ui:
  # 新邮件到达时在聊天栏显示的 ArcartX 卡片 ID；留空则不发送通知卡片。
  notify-card-id: "axs_mail_notify"
```

模块首次启动时自动将内置模板导出到 `plugins/ArcartX/chat_card/axs_mail_notify.yml`（文件已存在则不覆盖）。

卡片数据（`self.parent.data['key']`）：

| 键 | 说明 |
| --- | --- |
| `subject` | 邮件主题 |
| `senderName` | 发件人名称（系统邮件为 "系统"） |
| `sourceType` | 来源类型：`system`、`player`、`preset`、`cdk` |
| `hasAttachments` | 是否含附件（`true`/`false`） |

## EventPacket 联动

Mail 模块在 CDK 兑换成功时自动向 EventPacket 发射信号：

| 信号名 | 触发时机 | 携带变量 |
| --- | --- | --- |
| `cdk_redeemed` | CDK 兑换成功 | `cdk_code`, `preset_id`, `preset_name` |

可在 `ArcartXEventPacket.yml` 中配置对应规则实现兑换特效、字幕播报等联动效果。

## 领取条件（claim-conditions）

预设邮件与部分系统邮件可配置**领取门槛**，玩家点击领取附件/执行 `claim-commands` 前校验。语法见 **[条件系统（PAPI + Aria + JS）](/guide/conditions)**。

```yaml
# data/mail/presets/starter.yml 片段
preset:
  claim-conditions:
    - "%player_level% >= 10"                    # PAPI 行内
    - "%luckperms_groups% contains 新手"        # 权限组包含
    - "aria: return player.getHealth() > 0"     # Aria 脚本
    - "js: player.getWorld() == 'world'" # JS 脚本（`AriaPlayer` 门面直接返回世界名）
  claim-commands:
    # 仅支持 <player>、{player}、%player% 三种玩家名 token 替换
    - "eco give {player} 100"
```

| 写法 | 示例 |
| --- | --- |
| PAPI 行内 | `%player_level% >= 10` |
| 预设旧格式（兼容） | `%player_level%::GTE::10` |
| Aria 行内 | `aria: return player.getLevel() >= 10` |
| Aria 结构化 | `type: aria` + `script:` 多行块 |
| JS 行内 | `js: player.getLevel() >= 10` |
| JS 结构化 | `type: js` + `script:` 多行块 |
| 独立列表 | `claim-conditions:` 中使用 `aria:` / `js:` 行内前缀区分脚本类型 |

::: warning
- 未安装 PlaceholderAPI 时，PAPI 条件通常**不通过**，玩家无法领取。
- Aria 由 ArcartX 内置，随 `depend: [ArcartX]` 提供，始终可用；Aria 会在求值前展开脚本文本中的 `%...%` PAPI 占位符和 `{player}`。
- JS 需要 classpath 提供 JavaScript 引擎（Java 15+ 默认无 Nashorn）；JS 不做脚本级 `%...%` / `{player}` 预展开，取 PAPI 请使用 `player.papi()` 或 `player.papiNumber()`。两侧的 `player` 都是 `AriaPlayer` 门面，门面未覆盖的原生 API 使用 `player.bukkit()`。
- 所有 `claim-conditions` 条目为 **AND** 关系。
:::

## 配置诊断

Mail 模块声明了以下配置校验规则：

| 字段 | 类型 | 约束 |
|------|------|------|
| `storage.mode` | STRING | 必填，枚举 `sqlite` / `mysql` |
| `storage.pool-size` | INT | 范围 1–100 |
| `player-send.max-attachments` | INT | 范围 1–64 |
| `retention.completed-days` | INT | ≥ 1 |

动态节（用户可自由增删，不被结构同步覆盖）：
- `presets`

## 跨服配置

邮件数据需 **MySQL 共享库**；跨服仅同步「刷新通知」，不复制邮件正文。

```yaml
# ArcartXMail.yml
cross-server:
  enabled: false
```

连接参数见宿主 `config.yml` → `cross-server`。启用后，任一子服向玩家发信会广播 `refresh:<uuid>`，其他子服上该玩家在线时自动刷新收件箱 UI。

详见 [跨服功能配置指南](/guide/cross-server-setup)。

## UI / Packet

| 界面 | UI ID | 说明 |
| --- | --- | --- |
| 收件箱 | `AXS:mail_inbox` | 查看、领取、删除邮件 |
| 写信 | `AXS:mail_compose` | 填写标题正文、放入附件槽 |
| 日志 | `AXS:mail_logs` | 发信/收信记录 |
| 管理 | `AXS:mail_admin` | 管理员查看全服邮件 |

客户端回包动作（受 [ClientPacketGuard](/architecture/security) 限流）：`compose-send`、`claim`、`claimall`、`deleteall`、`cdk` 等。

完整变量与点击流参见 [UI Packet 数据全景](/ui-packet-data)。

## 故障排查

| 现象 | 排查 |
| --- | --- |
| 模块未加载 | 确认云端已装备 Mail 授权；`modules.mail.enabled: true` |
| 跨服收不到新邮件 | 各子服 `storage.mysql` 必须指向**同一库**；`cross-server.enabled: true` |
| 附件领取失败 | 检查 `claim-conditions`；背包满时部分奖励会发失败 |
| 写信被 silently 丢弃 | 命中 `moderation` 或 ClientPacketGuard 限流；查看控制台 debug |
