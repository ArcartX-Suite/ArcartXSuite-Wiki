# EventPacket 事件引擎

## 功能定位

**通用触发器 + 动作链**模块。当指定事件发生时，按顺序执行一组动作（发 UI 包、播字幕、执行命令、派发邮件、授予称号等）。支持 9 种触发器和 11 种动作类型，覆盖服务端事件监听和客户端回包驱动两大场景。

## 依赖

| 类型 | 依赖 | 作用 | 缺少时表现 |
| --- | --- | --- | --- |
| 必需 | ArcartX | 接收 `client-packet` 触发器，执行 `ui-packet` / `combateffect.play` 等动作 | 客户端回包触发与 UI 发包不可用 |
| 可选 | PlaceholderAPI | `placeholder-*` / `papi-*` 触发器 | 这些触发器不生效，其余不受影响 |
| 可选 | MythicMobs | `mob-kill-count` 按 MythicMob ID 过滤 | MythicMob 击杀过滤不可用 |
| 可选 | AXS 其他模块 | 执行模块联动动作或接收模块信号 | 仅影响引用了该模块的动作/信号 |

## 启用步骤

```yaml
# config.yml
modules:
  eventpacket:
    enabled: true
```

---

## 配置结构（`ArcartXEventPacket.yml`）

```yaml
settings:
  refresh-interval-ticks: 20   # PAPI 轮询间隔（tick），默认 20（1秒）
  debug: false                 # 开启后每次动作执行会打印日志

packet-command:
  enabled: true                          # 是否启用 client-packet 预设
  packet-id: "ArcartXEventPacket"        # 客户端发包匹配的 packetId
  presets-directory: "eventpacket/packet-command-presets"  # 预设文件目录

rules:
  <规则ID>:
    enabled: true              # 是否启用
    trigger: join              # 触发器类型（见下文）
    repeatable: false          # 是否可重复触发（默认 false，每玩家只触发一次）
    cooldown: "5s"             # 冷却时间：10s / 5m / 2h / 1d / 500ms
    actions:                   # 动作列表（按顺序执行）
      - type: subtitle.play
        group-id: "welcome"
```

### 规则通用字段

| 字段 | 类型 | 默认值 | 说明 |
| --- | --- | --- | --- |
| `enabled` | boolean | `true` | 是否启用该规则 |
| `trigger` | string | 必填 | 触发器类型 |
| `repeatable` | boolean | `false` | 是否可重复触发；`false` 时每玩家仅触发一次（服务器重启后重置） |
| `cooldown` | string | 无 | 冷却时间，格式 `10s` / `5m` / `2h` / `1d` / `500ms` |
| `actions` | list | 必填 | 动作列表，每个动作是一个 `type` + 参数的 Map |

---

## 触发器详解

### 1. `join` — 玩家加入

玩家每次进入服务器时触发。

```yaml
rules:
  join_welcome:
    enabled: true
    trigger: join
    repeatable: true
    actions:
      - type: subtitle.play
        group-id: "welcome_back"
```

无额外字段。

---

### 2. `first-join` — 首次加入

玩家**首次**进入服务器时触发（`!player.hasPlayedBefore()`）。

```yaml
rules:
  first_join_guide:
    enabled: true
    trigger: first-join
    repeatable: false
    actions:
      - type: subtitle.play
        group-id: "welcome_cinematic"
      - type: questgps.offer
        quest-id: "main/tutorial"
        open-menu: true
      - type: command.dispatch
        executor: console
        command: "give {player_name} diamond 5"
```

无额外字段。通常搭配 `repeatable: false` 使用。

---

### 3. `quit` — 玩家退出

玩家退出服务器时触发。退出时玩家实体仍有效，可以向其他在线玩家发送 UI 包。

```yaml
rules:
  quit_notify:
    enabled: true
    trigger: quit
    repeatable: true
    actions:
      - type: ui-packet
        ui-id: "party_hud"
        packet-handler: "memberQuit"
        recipients:
          - all-online
        pack:
          player: "{player_name}"
          time: "{timestamp_local}"
```

无额外字段。

---

### 4. `placeholder-increase` / `papi-increase` — PAPI 数值增长

指定 PAPI 占位符的数值增长时触发。服务端每 `refresh-interval-ticks` 轮询一次，比较前后值。

| 专属字段 | 类型 | 说明 |
| --- | --- | --- |
| `placeholder` | string | 完整 PAPI 占位符，如 `%player_level%` |
| `require-non-empty` | boolean | 是否要求新值非空（默认 `false`） |

```yaml
rules:
  level_up_notify:
    enabled: true
    trigger: papi-increase
    placeholder: "%player_level%"
    require-non-empty: true
    repeatable: true
    cooldown: "3s"
    actions:
      - type: subtitle.play
        group-id: "level_up"
      - type: command.dispatch
        executor: console
        command: "say {player_name} 升级了！{old_number} → {new_number}"
```

**可用上下文变量：** `{placeholder}`、`{old_value}`、`{new_value}`、`{old_number}`、`{new_number}`、`{delta_number}`、`{change_direction}`（值为 `increase`）。

---

### 5. `placeholder-decrease` / `papi-decrease` — PAPI 数值减少

与 `papi-increase` 相同，但在数值**减少**时触发。

| 专属字段 | 类型 | 说明 |
| --- | --- | --- |
| `placeholder` | string | 完整 PAPI 占位符 |
| `require-non-empty` | boolean | 是否要求新值非空 |

```yaml
rules:
  health_drop_warning:
    enabled: true
    trigger: papi-decrease
    placeholder: "%player_health%"
    repeatable: true
    cooldown: "10s"
    actions:
      - type: ui-packet
        ui-id: "combat_hud"
        packet-handler: "healthWarning"
        pack:
          health: "{new_number}"
          delta: "{delta_number}"
```

**可用上下文变量：** 同 `papi-increase`，`{change_direction}` 值为 `decrease`。

---

### 6. `placeholder-threshold` — PAPI 达到阈值

指定 PAPI 值**首次从低于阈值升至 ≥ 阈值**时触发（仅在跨越阈值时触发一次）。

| 专属字段 | 类型 | 说明 |
| --- | --- | --- |
| `placeholder` | string | 完整 PAPI 占位符 |
| `threshold` | number | 目标阈值 |
| `require-non-empty` | boolean | 是否要求新值非空 |

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
      - type: mail.send
        preset-id: "level_30_gift"
      - type: title.give
        title-id: "advanced_warrior"
        duration: "permanent"
```

**可用上下文变量：** 同 `papi-increase`。

---

### 7. `mob-kill-count` — 击杀怪物累积

玩家击杀指定类型怪物累积满 N 次后触发。进度持久化到本地文件，重启不丢失。

| 专属字段 | 类型 | 说明 |
| --- | --- | --- |
| `count` | int | 需要累积的击杀次数（默认 `1`） |
| `worlds` | list | 限定世界名（留空 = 不限） |
| `entity-types` | list | 限定原版实体类型如 `ZOMBIE`、`SKELETON`（留空 = 不限） |
| `mythic-mob-ids` | list | 限定 MythicMobs 的 mob ID（留空 = 不限） |

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
```

**可用上下文变量：** `{rule_id}`、`{kill_count}`、`{required_count}`、`{mob_world}`、`{mob_entity_type}`、`{mythic_mob_id}`。

::: tip 过滤逻辑
`worlds`、`entity-types`、`mythic-mob-ids` 三个列表是 AND 关系——只要配置了的列表就必须匹配；留空表示不限制。
:::

---

### 8. `command-signal` — 命令信号

由 `/axs eventpacket fire <信号名> <玩家> [key=value...]` 手动触发，或由其他模块自动发射。

| 专属字段 | 类型 | 说明 |
| --- | --- | --- |
| `signal` | string | 要匹配的信号名称 |

```yaml
rules:
  boss_celebration:
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

**可用上下文变量：** `{signal}`、`{command_signal}` + 信号携带的所有 key=value 自定义变量。

#### 模块自动发射的信号

| 信号名 | 来源模块 | 触发时机 | 携带变量 |
| --- | --- | --- | --- |
| `boss_settlement` | EntityTracker | Boss 死亡结算 | `boss_id`, `boss_name`, `settlement_id`, `rank`, `damage`, `total_damage`, `participant_count` |
| `signin_success` | OnlineRewards | 签到成功 | `streak`, `total`, `date`, `day_of_month` |
| `login_success` | LoginView | 登录成功 | `auth_mode` |
| `first_register` | LoginView | 首次注册 | `auth_mode` |
| `cdk_redeemed` | Mail | CDK 兑换成功 | `cdk_code`, `preset_id`, `preset_name` |

手动触发示例：

```bash
/axs eventpacket fire boss_settlement Steve boss_id=dragon boss_name=末影龙 rank=1 damage=50000
```

---

### 9. `client-packet` — 客户端回包触发

**这是 EventPacket 最核心的 UI 交互能力。** 客户端 UI 通过 `Packet.send(...)` 发包到服务端，EventPacket 匹配预设 ID 后执行对应动作链。

#### 工作原理

```
┌─────────────────────────────────────────────────────────┐
│                      客户端 (UI YAML)                    │
│                                                         │
│  按钮点击 → Packet.send('ArcartXEventPacket', '预设ID')  │
│                          │                              │
└──────────────────────────┼──────────────────────────────┘
                           │ ArcartX 客户端事件
                           ▼
┌──────────────────────────────────────────────────────────┐
│                    服务端 (EventPacket)                   │
│                                                         │
│  1. 收到 packetId = "ArcartXEventPacket"                 │
│  2. data[0] = "预设ID"                                   │
│  3. 在 packet-command-presets/ 目录查找匹配的预设          │
│  4. 执行预设中定义的命令列表                               │
│                                                         │
│  也可在 rules 中配置 trigger: client-packet 来执行         │
│  完整的动作链（ui-packet、subtitle.play 等）              │
└──────────────────────────────────────────────────────────┘
```

#### 方式一：预设文件（推荐，简单场景）

在 `plugins/ArcartXSuite/eventpacket/packet-command-presets/` 目录下创建 YAML 文件，每个顶层键就是一个预设 ID：

```yaml
# eventpacket/packet-command-presets/shop.yml

# 顶层键名就是 presetId，客户端通过 Packet.send('ArcartXEventPacket', '预设ID') 触发
open_shop:
  type: op          # 命令执行身份：op / console / player
  commands:
    - say <player> 打开了商店
    - openShop <player>

buy_item:
  type: console
  commands:
    - economy take <player> 100
    - give <player> diamond 1
    - say <player> 购买了钻石
```

**UI YAML 中发包：**

```yaml
# 某个 UI 文件中的按钮
controls:
  shop_button:
    type: Texture
    texture: "ui/btn_shop.png"
    action:
      click: |-
        Packet.send('ArcartXEventPacket', 'open_shop')
  buy_button:
    type: Texture
    texture: "ui/btn_buy.png"
    action:
      click: |-
        Packet.send('ArcartXEventPacket', 'buy_item')
```

> `Packet.send` 的第一个参数是 `packet-command.packet-id`（默认 `ArcartXEventPacket`），第二个参数是预设文件中的顶层键名。

::: tip type 说明
- **`op`**：临时给予玩家 OP 权限执行命令，执行后立即恢复
- **`console`**：以控制台身份执行
- **`player`**：以玩家自身身份执行（不提权）
:::

#### 方式二：rules 配置（完整动作链）

在 `ArcartXEventPacket.yml` 的 `rules` 中直接定义 `client-packet` 触发器。比预设文件更强大——可以使用所有 11 种动作类型。

```yaml
rules:
  client_open_quest:
    enabled: true
    trigger: client-packet
    signal: "open_quest_menu"       # 匹配的预设 ID
    repeatable: true
    cooldown: "1s"
    actions:
      - type: questgps.open
      - type: subtitle.play
        group-id: "quest_hint"
```

**UI YAML 对应：**
```yaml
action:
  click: |-
    Packet.send('ArcartXEventPacket', 'open_quest_menu')
```

::: warning 注意
`rules` 中的 `client-packet` 规则与预设文件共享同一个匹配池。如果预设文件和 rules 中有相同的 ID，预设文件的规则会优先（因为它们先被加载）。建议避免 ID 重名。
:::

---

## 动作类型详解

所有动作的参数值都支持 `{变量名}` 占位符替换。

### `command.dispatch` — 执行命令

| 参数 | 类型 | 说明 |
| --- | --- | --- |
| `command` | string | 要执行的命令（不需要带 `/`，可用变量） |
| `executor` | string | 执行身份：`op`（默认）/ `console` / `player` |

```yaml
- type: command.dispatch
  executor: console
  command: "give {player_name} diamond 5"
```

### `ui-packet` — 发送 UI 包（服务端 → 客户端）

**这是 EventPacket 向客户端 UI 发送数据的核心动作。**

| 参数 | 类型 | 说明 |
| --- | --- | --- |
| `ui-id` | string | 目标 UI 的注册 ID |
| `packet-handler` | string | UI YAML `packetHandler` 中的处理器名称 |
| `recipients` | list | 接收者：`self`（默认）/ `all-online` / `others` |
| `pack` | map/string | 发送给 UI 的数据负载 |

```yaml
- type: ui-packet
  ui-id: "quest_hud"
  packet-handler: "questCompleted"
  recipients:
    - self
  pack:
    player: "{player_name}"
    quest: "{signal}"
    time: "{timestamp_local}"
    level: "{player_level}"
```

**UI YAML 中接收：**

```yaml
ui:
  packetHandler:
    questCompleted: |-
      var.completedPlayer = packet['player']
      var.completedQuest = packet['quest']
      var.completedTime = packet['time']
      var.showNotification = true

controls:
  notification:
    type: Text
    visible: "var.showNotification == true"
    texts:
      - "{var.completedPlayer} 完成了任务 {var.completedQuest}"
```

::: tip recipients 说明
- **`self`**：仅发送给触发事件的玩家本人
- **`all-online`** / **`all`**：发送给所有在线玩家
- **`others`**：发送给除触发者以外的所有在线玩家
:::

### `subtitle.play` — 播放字幕组

| 参数 | 类型 | 说明 |
| --- | --- | --- |
| `group-id` | string | Announcer 模块中的字幕组 ID |

```yaml
- type: subtitle.play
  group-id: "welcome_cinematic"
```

### `announcer.play` — 播放 Announcer 字幕组

与 `subtitle.play` 功能相同，是别名。

### `chat.card` — 推送聊天卡片

| 参数 | 类型 | 说明 |
| --- | --- | --- |
| `card-id` | string | Chat 模块中的卡片 ID |
| `data` | map | 传递给卡片的数据 |

```yaml
- type: chat.card
  card-id: "quest_offer"
  data:
    quest: "main/tutorial"
    title: "新手引导"
```

### `title.give` — 授予称号

| 参数 | 类型 | 说明 |
| --- | --- | --- |
| `title-id` | string | Title 模块中的称号 ID |
| `duration` | string | 持续时间：`permanent` / `7d` / `30d` 等 |

```yaml
- type: title.give
  title-id: "boss_slayer"
  duration: "7d"
```

### `questgps.offer` — 提供任务

| 参数 | 类型 | 说明 |
| --- | --- | --- |
| `quest-id` | string | QuestGPS 任务 ID |
| `open-menu` | boolean | 是否同时打开任务菜单 |

```yaml
- type: questgps.offer
  quest-id: "main/tutorial"
  open-menu: true
```

### `questgps.accept` — 接受任务

| 参数 | 类型 | 说明 |
| --- | --- | --- |
| `quest-id` | string | QuestGPS 任务 ID |

```yaml
- type: questgps.accept
  quest-id: "main/tutorial"
```

### `questgps.open` — 打开任务菜单

无参数。

```yaml
- type: questgps.open
```

### `questgps.track` — 追踪任务

| 参数 | 类型 | 说明 |
| --- | --- | --- |
| `quest-id` | string | QuestGPS 任务 ID |
| `task-id` | string | 可选，具体任务步骤 ID |

```yaml
- type: questgps.track
  quest-id: "main/tutorial"
  task-id: "step_1"
```

### `mail.send` — 发送邮件预设

| 参数 | 类型 | 说明 |
| --- | --- | --- |
| `preset-id` | string | Mail 模块中的邮件预设 ID |

```yaml
- type: mail.send
  preset-id: "welcome_gift"
```

### `combateffect.play` — 播放战斗特效

| 参数 | 类型 | 说明 |
| --- | --- | --- |
| `ui-id` | string | 可选，CombatEffect UI ID（留空使用模块默认） |
| `packet-handler` | string | 可选，处理器名（默认 `play`） |
| `pack` | any | 特效数据负载 |

```yaml
- type: combateffect.play
  pack: "{player_name};cdk_sparkle;{preset_name}"
```

---

## 上下文变量一览

所有动作参数中的 `{变量名}` 会在执行前自动替换。

### 玩家信息

| 变量 | 说明 |
| --- | --- |
| `{player_name}` | 触发玩家名 |
| `{player_display_name}` | 显示名 |
| `{player_uuid}` | UUID |
| `{player_world}` | 所在世界 |
| `{player_x}` / `{player_y}` / `{player_z}` | 坐标（整数） |
| `{player_health}` / `{player_max_health}` | 生命值 |
| `{player_level}` | 等级 |
| `{player_ping}` | 延迟 |

`{subject_*}` 与 `{player_*}` 等价。

### 接收者信息（仅 `ui-packet` 动作的 `pack` 中有效）

| 变量 | 说明 |
| --- | --- |
| `{receiver_name}` | 接收者玩家名 |
| `{receiver_display_name}` | 接收者显示名 |
| `{receiver_uuid}` | 接收者 UUID |
| `{receiver_role}` | 接收者类型：`self` / `all-online` / `others` |

### PAPI 触发器变量

| 变量 | 说明 |
| --- | --- |
| `{placeholder}` | 监听的占位符 |
| `{old_value}` / `{new_value}` | 变化前后的原始值 |
| `{old_number}` / `{new_number}` | 变化前后的数值 |
| `{delta_number}` | 变化量（new - old） |
| `{change_direction}` | `increase` 或 `decrease` |

### 信号变量

| 变量 | 说明 |
| --- | --- |
| `{signal}` / `{command_signal}` | 信号名称 |
| 自定义 key | `fire` 命令或模块发射时携带的 key=value 变量 |

### 时间戳

| 变量 | 说明 |
| --- | --- |
| `{timestamp_unix}` | Unix 秒数 |
| `{timestamp_local}` | 本地时间字符串 |

---

## 完整教程：client-packet 双向通信

### 场景说明

实现一个自定义 UI 按钮，点击后：
1. 客户端发包给服务端（`Packet.send`）
2. 服务端执行命令 + 向客户端 UI 回发数据（`ui-packet`）
3. 客户端 UI 接收数据并更新显示（`packetHandler`）

### 第一步：创建预设或规则

**方式 A — 预设文件（仅执行命令）：**

```yaml
# eventpacket/packet-command-presets/my_ui.yml
claim_daily:
  type: console
  commands:
    - give <player> diamond 1
    - say <player> 领取了每日奖励
```

**方式 B — rules 配置（命令 + UI 回包）：**

```yaml
# ArcartXEventPacket.yml
rules:
  claim_daily_with_feedback:
    enabled: true
    trigger: client-packet
    signal: "claim_daily"
    repeatable: true
    cooldown: "86400s"
    actions:
      - type: command.dispatch
        executor: console
        command: "give {player_name} diamond 1"
      - type: ui-packet
        ui-id: "daily_reward_ui"
        packet-handler: "claimResult"
        recipients:
          - self
        pack:
          success: "true"
          reward: "钻石 x1"
          player: "{player_name}"
          time: "{timestamp_local}"
```

### 第二步：UI YAML — 发包 + 接包

```yaml
# arcartx/ui/daily_reward_ui.yml
ui:
  background: true
  escClose: true
  screenScale: true
  packetHandler:
    # 接收服务端 ui-packet 动作发来的数据
    claimResult: |-
      var.claimSuccess = packet['success']
      var.rewardText = packet['reward']
      var.claimTime = packet['time']
      var.showResult = true

controls:
  root:
    type: Canvas
    width: 300
    height: 200
    center: true
    children:
      # 领取按钮 —— 点击发包给 EventPacket
      claim_button:
        type: Texture
        texture: "ui/btn_claim.png"
        width: 120
        height: 40
        x: 90
        y: 80
        action:
          click: |-
            Packet.send('ArcartXEventPacket', 'claim_daily')

      # 结果显示 —— 接收到服务端回包后显示
      result_text:
        type: Text
        visible: "var.showResult == true"
        x: 50
        y: 140
        fontSize: 14
        color: "#00FF00"
        texts:
          - "领取成功: {var.rewardText}"
          - "时间: {var.claimTime}"
```

### 第三步：发包流程图解

```
   客户端 UI                          服务端 EventPacket
   ──────────                         ─────────────────
   
   [点击按钮]
       │
       │ Packet.send('ArcartXEventPacket', 'claim_daily')
       │────────────────────────────────────►│
       │                                     │ 1. 匹配 packetId + presetId
       │                                     │ 2. 执行 command.dispatch
       │                                     │ 3. 执行 ui-packet
       │                                     │
       │◄────────────────────────────────────│
       │ sendPacket(player, uiId,            │
       │   "claimResult", {success, reward}) │
       │                                     │
   [packetHandler.claimResult 执行]
       │
   var.showResult = true
   var.rewardText = "钻石 x1"
       │
   [UI 自动刷新显示结果]
```

### 关键要点

1. **客户端 → 服务端**：`Packet.send('packetId', 'presetId')`，`packetId` 对应 `packet-command.packet-id` 配置
2. **服务端 → 客户端**：`ui-packet` 动作，通过 `ui-id` + `packet-handler` 指定目标 UI 和处理器
3. **UI 接包**：在 `ui.packetHandler.<handler名>` 中用 `packet['key']` 读取数据，赋值给 `var.*` 变量
4. **UI 使用数据**：controls 中通过 `{var.变量名}` 引用，`visible` 中通过 `var.变量名 == 值` 控制显示

---

## 联动示例

### Boss 击杀庆祝

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

### 新玩家注册引导流

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

### 击杀怪物解锁副本

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
```

### 等级提升联动

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
      - type: mail.send
        preset-id: "level_30_gift"
      - type: title.give
        title-id: "advanced_warrior"
        duration: "permanent"
      - type: questgps.offer
        quest-id: "main/advanced_zone"
```

### CDK 兑换特效

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
```

---

## 命令

> 权限：`arcartxsuite.admin`

| 命令 | 说明 | 示例 |
| --- | --- | --- |
| `/axs eventpacket status` | 查看规则数量和模块状态 | |
| `/axs eventpacket reload` | 重载配置和预设文件 | |
| `/axs eventpacket fire <信号名> <玩家> [key=value...]` | 手动触发信号，用于调试 | `/axs eventpacket fire boss_settlement Steve boss_id=dragon rank=1` |
