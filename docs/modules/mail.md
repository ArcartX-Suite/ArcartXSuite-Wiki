# Mail 邮箱

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
- **预设定义**：在 `mail/presets/*.yml` 中定义邮件模板，包含标题、正文、附件、命令
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
- **Redis 跨服广播**：多服邮件同步
- **数据持久化**：SQLite 或 MySQL

## 依赖

| 类型 | 依赖 | 作用 | 缺少时表现 |
| --- | --- | --- | --- |
| 必需 | ArcartX | 收件箱、写信、日志、管理 UI 和附件槽交互 | 模块无法提供可视化邮箱 |
| 可选 | PlaceholderAPI | `placeholder-command` 货币、条件判断和邮件文本变量 | 相关变量/条件不可用 |
| 可选 | Vault | 金币附件、手续费或 Vault 货币扣费 | Vault 货币功能关闭，物品邮件仍可用 |
| 可选 | PlayerPoints | 点券附件或点券扣费 | PlayerPoints 货币功能关闭 |
| 可选 | NeigeItems / MythicMobs / MMOItems | 物品附件来自对应物品库时保留识别信息 | 原版 ItemStack 附件正常；物品库专属识别不可用 |
| 可选 | Redis 服务 | 多服邮件广播和缓存刷新 | 单服邮件正常，跨服同步关闭 |
| 可选 | MySQL 服务 | 多服共享邮件数据 | 默认 SQLite 可用；多服共享建议改 MySQL |

## 启用步骤

```yaml
modules:
  mail:
    enabled: true
```

## 命令

### 管理命令（权限：`arcartxsuite.admin`）

| 命令 | 说明 |
| --- | --- |
| `/axs mail status` | 查看邮箱模块、存储、预设和 CDK 状态 |
| `/axs mail reload` | 重载邮箱配置、预设和 UI |
| `/axs mail open <玩家>` | 为在线玩家打开邮箱收件箱 |
| `/axs mail preset send <预设ID> <目标>` | 按预设派发邮件。目标可填玩家名、`all-online` 或 `all-registered` |
| `/axs mail cdk create <预设ID> <兑换码\|auto> <最大领取数> <有效期>` | 创建 CDK 兑换码。`auto` 自动生成，有效期如 `1d`、`7d`、`permanent` |
| `/axs mail cdk info <兑换码>` | 查看 CDK 绑定预设、已领取次数、过期时间 |
| `/axs mail cdk list [页码]` | 分页查看所有已创建的 CDK |
| `/axs mail cdk delete <兑换码>` | 禁用并删除指定 CDK |

### 玩家命令（权限：`arcartxsuite.mail.use`，别名 `/axmail`）

| 命令 | 说明 |
| --- | --- |
| `/mail` 或 `/mail open` | 打开邮箱收件箱 |
| `/mail compose` | 打开写信界面，可给其他玩家写信并附带物品 |
| `/mail claimall` | 一键领取所有未领取邮件的附件和奖励 |
| `/mail deleteall` | 删除所有已读邮件 |
| `/mail cdk <兑换码>` | 使用兑换码领取对应邮件奖励 |

## PAPI

前缀：`%AXSmail_*%`

| 占位符 | 返回值 | 说明 |
| --- | --- | --- |
| `%AXSmail_unread_count%` | 数字 | 未读邮件数量 |
| `%AXSmail_claimable_count%` | 数字 | 有附件但尚未领取的邮件数量 |
| `%AXSmail_total_count%` | 数字 | 收件箱邮件总数 |

## EventPacket 联动

Mail 模块在 CDK 兑换成功时自动向 EventPacket 发射信号：

| 信号名 | 触发时机 | 携带变量 |
| --- | --- | --- |
| `cdk_redeemed` | CDK 兑换成功 | `cdk_code`, `preset_id`, `preset_name` |

可在 `ArcartXEventPacket.yml` 中配置对应规则实现兑换特效、字幕播报等联动效果。
