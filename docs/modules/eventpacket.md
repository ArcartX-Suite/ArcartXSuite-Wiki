# EventPacket 事件引擎

## 功能定位

**通用触发器 + 动作链**模块。在以下事件发生时，串联执行一组动作：

- `join` / `first-join` / `quit`
- `placeholder-increase` / `placeholder-decrease` / `placeholder-threshold`
- `mob-kill-count`
- `command-signal`（由 `/AXS eventpacket fire` 手动触发，或由其他模块自动发射信号）
- `client-packet`（客户端回包触发预设指令）

### 核心特性

**触发器类型：**
- **`join`**：玩家加入服务器时触发
- **`first-join`**：玩家首次进服时触发（仅一次）
- **`quit`**：玩家退出服务器时触发
- **`placeholder-increase`**：指定 PAPI 值增长时触发
- **`placeholder-decrease`**：指定 PAPI 值减少时触发
- **`placeholder-threshold`**：指定 PAPI 值达到阈值时触发
- **`mob-kill-count`**：击杀指定 MythicMob 累计 N 次后触发
- **`command-signal`**：由 `/AXS eventpacket fire` 手动触发，或由其他模块（LoginView、OnlineRewards 等）自动发射信号
- **`client-packet`**：客户端通过 ArcartX Packet.send 回包触发

**动作链：**
- **console-command**：以控制台执行命令，支持 `{player}` 等变量
- **player-command**：以玩家身份执行命令
- **message**：发送消息给玩家
- **subtitle.play**：触发 Announcer 字幕播放
- **title**：发送 Minecraft 原生 Title/Subtitle
- **sound**：播放音效

**其他特性：**
- **延迟执行**：动作链支持配置延迟（ticks）
- **条件判断**：支持权限条件和 PAPI 条件
- **跨模块信号**：LoginView 登录成功、OnlineRewards 签到、EntityTracker Boss 结算等均会自动发射信号，可在 EventPacket 中监听

## 依赖

- 必需：ArcartX
- 可选：PlaceholderAPI（`placeholder-*` 触发器需要）

## 启用步骤

```yaml
modules:
  eventpacket:
    enabled: true
```

## 关键配置（`ArcartXEventPacket.yml`）

```yaml
settings:
  debug: false

rules:
  first_join_welcome:
    enabled: true
    trigger: first-join
    actions:
      - type: subtitle.play
        group-id: "welcome_cinematic"
      - type: chat.card
        card-id: "beginner_guide"
      - type: command.dispatch
        executor: console
        command: "give {player} diamond 5"
```

### 触发器类型

| 值 | 触发条件 |
| --- | --- |
| `join` | 玩家加入 |
| `first-join` | 首次加入 |
| `quit` | 玩家退出 |
| `placeholder-increase` | PAPI 数值增加 |
| `placeholder-decrease` | 数值减少 |
| `placeholder-threshold` | 首次达到阈值 |
| `mob-kill-count` | 击杀指定怪物累积满 N 次 |
| `command-signal` | 管理命令手动触发 |
| `client-packet` | 客户端回包触发预设指令 |

### 动作类型

| `type` | 说明 |
| --- | --- |
| `subtitle.play` | 播放字幕组 |
| `chat.card` | 推送聊天卡片 |
| `command.dispatch` | 执行命令 |
| `ui-packet` | 发送 UI 包 |
| `title.give` | 发放称号 |
| `questgps.offer` / `questgps.accept` | 任务导航 |
| `mail.send` | 发送邮件预设（需 Mail 模块） |
| `announcer.play` | 播放 Announcer 字幕组 |
| `combateffect.play` | 播放战斗特效 UI 包 |

### 模块联动信号

以下模块会在关键事件发生时自动向 EventPacket 发射 `command-signal`，可直接在规则中匹配：

| 信号名 | 来源模块 | 触发时机 | 携带变量 |
| --- | --- | --- | --- |
| `boss_settlement` | EntityTracker | Boss 死亡结算 | `boss_id`, `boss_name`, `settlement_id`, `rank`, `damage`, `total_damage`, `participant_count` |
| `signin_success` | OnlineRewards | 签到成功 | `streak`, `total`, `date`, `day_of_month` |
| `login_success` | LoginView | 登录成功 | `auth_mode` |
| `first_register` | LoginView | 首次注册 | `auth_mode` |
| `cdk_redeemed` | Mail | CDK 兑换成功 | `cdk_code`, `preset_id`, `preset_name` |

### 联动示例

#### 1. Boss 击杀庆祝

击杀世界 Boss → 播放庆祝字幕 + 按 Boss 发送对应邮件奖励 + 授予限时称号：

```yaml
rules:
  boss_settlement_celebration:
    enabled: true
    trigger: command-signal
    signal: "boss_settlement"
    repeatable: true
    cooldown: "3s"
    actions:
      - type: subtitle.play
        group-id: "boss_victory"
      - type: mail.send
        preset-id: "boss_reward_{boss_id}"
      - type: title.give
        title-id: "boss_slayer"
        duration: "7d"
```

#### 2. Boss 第一名全服公告

伤害排名第一的玩家获得全服字幕公告 + 专属称号：

```yaml
rules:
  boss_mvp_broadcast:
    enabled: true
    trigger: command-signal
    signal: "boss_settlement"
    repeatable: true
    cooldown: "3s"
    actions:
      - type: announcer.play
        group-id: "boss_mvp_{boss_id}"
      - type: title.give
        title-id: "boss_mvp_{boss_id}"
        duration: "30d"
      - type: command.dispatch
        executor: console
        command: "broadcast &6{player_name} &e以 &c{damage} &e伤害击败了 &c{boss_name}&e！"
```

::: tip 条件过滤
上述规则会对所有参与者触发。如需仅第一名触发，可配合 `command.dispatch` 用条件插件判断 `{rank}` 变量。
:::

#### 3. 新玩家注册引导流

首次注册 → 欢迎过场字幕 + 新手任务引导 + 欢迎礼包邮件：

```yaml
rules:
  welcome_new_player:
    enabled: true
    trigger: command-signal
    signal: "first_register"
    repeatable: false
    actions:
      - type: subtitle.play
        group-id: "welcome_cinematic"
      - type: questgps.offer
        quest-id: "main/tutorial"
        open-menu: true
      - type: mail.send
        preset-id: "welcome_gift"
      - type: title.give
        title-id: "newcomer"
        duration: "permanent"
```

#### 4. 登录成功提醒签到

每次登录成功 → 推送聊天卡片提醒签到：

```yaml
rules:
  login_signin_reminder:
    enabled: true
    trigger: command-signal
    signal: "login_success"
    repeatable: true
    cooldown: "30s"
    actions:
      - type: chat.card
        card-id: "signin_reminder"
        data:
          message: "今天还没签到哦！输入 /onlinerewards signin 签到领奖~"
```

#### 5. 签到连续7天里程碑

连续签到满 7 天 → 播放特效 + 里程碑邮件 + 授予称号：

```yaml
rules:
  signin_week_milestone:
    enabled: true
    trigger: command-signal
    signal: "signin_success"
    repeatable: true
    cooldown: "1d"
    actions:
      - type: command.dispatch
        executor: console
        command: "execute if entity {player_name}[scores={streak=7..}] run AXS eventpacket fire signin_7day {player_name}"

  signin_7day_reward:
    enabled: true
    trigger: command-signal
    signal: "signin_7day"
    repeatable: true
    cooldown: "7d"
    actions:
      - type: subtitle.play
        group-id: "signin_milestone_7"
      - type: mail.send
        preset-id: "signin_week_bonus"
      - type: title.give
        title-id: "diligent_adventurer"
        duration: "30d"
```

#### 6. CDK 兑换特效

玩家兑换 CDK → 播放战斗特效 + 成功字幕：

```yaml
rules:
  cdk_redeemed_effect:
    enabled: true
    trigger: command-signal
    signal: "cdk_redeemed"
    repeatable: true
    cooldown: "5s"
    actions:
      - type: combateffect.play
        pack: "{player_name};cdk_sparkle;{preset_name}"
      - type: subtitle.play
        group-id: "cdk_success"
      - type: chat.card
        card-id: "cdk_result"
        data:
          code: "{cdk_code}"
          reward: "{preset_name}"
```

#### 7. 击杀怪物累积触发副本

累计击杀 50 只僵尸 → 解锁副本任务：

```yaml
rules:
  zombie_dungeon_unlock:
    enabled: true
    trigger: mob-kill-count
    count: 50
    entity-types:
      - "ZOMBIE"
    worlds:
      - "world"
    repeatable: false
    actions:
      - type: questgps.offer
        quest-id: "dungeon/zombie_lair"
        open-menu: true
      - type: subtitle.play
        group-id: "dungeon_unlock"
      - type: chat.card
        card-id: "dungeon_available"
        data:
          dungeon: "僵尸巢穴"
          kills: "50"
```

#### 8. 等级提升联动

玩家等级达到 30 → 发放进阶礼包 + 开放新区域任务：

```yaml
rules:
  level_30_unlock:
    enabled: true
    trigger: placeholder-threshold
    placeholder: "%player_level%"
    threshold: 30
    require-non-empty: true
    repeatable: false
    actions:
      - type: subtitle.play
        group-id: "level_milestone_30"
      - type: mail.send
        preset-id: "level_30_gift"
      - type: questgps.offer
        quest-id: "main/advanced_zone"
        open-menu: false
      - type: title.give
        title-id: "advanced_warrior"
        duration: "permanent"
```

#### 9. 退出时 UI 通知

玩家退出 → 通知同队在线成员：

```yaml
rules:
  quit_notify_party:
    enabled: true
    trigger: quit
    repeatable: true
    actions:
      - type: ui-packet
        ui-id: "AXS:party_hud"
        packet-handler: "memberQuit"
        recipients:
          - all
        pack:
          player: "{player_name}"
          time: "{timestamp_local}"
```

## 命令

> 权限：`arcartxsuite.admin`

| 命令 | 说明 |
| --- | --- |
| `/AXS eventpacket status` | 查看事件引擎规则数量和模块状态 |
| `/AXS eventpacket reload` | 重载触发器配置 |
| `/AXS eventpacket fire <信号名> <玩家> [key=value...]` | 手动向指定玩家触发信号，可附带参数。用于调试规则配置 |
