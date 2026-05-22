# Announcer 播报系统

## 功能定位

服务器信息播报一站式方案，包含两大能力：

- **HUD 公告** — 常驻 / 轮播文字公告，可点击执行后台命令
- **打字机字幕** — 按帧播放文本动画，适用于剧情对白、任务提示、登录欢迎

**几乎零依赖，适合作为第一个验证模块。**

### 核心特性

**公告：**
- **轮播公告**：多条公告按配置顺序自动轮播，可配置每轮冷却和条间间隔
- **PAPI 解析**：公告文本支持 `%player_name%`、`%server_online%` 等变量，按接收玩家实时解析
- **点击命令**：每条公告可绑定控制台命令，玩家点击 HUD 后自动执行（`<player>` 变量替换为点击玩家名）
- **热重载**：`/axs announcer reload` 即时生效，无需重启

**字幕：**
- **打字机动画**：逐字播放文本，可控制动画时长、停留时间和自动文本长度计算
- **顺序播放**：字幕组按数字键顺序播放，最后一帧结束后自动关闭 HUD
- **PAPI 支持**：字幕文本支持 PlaceholderAPI 变量，按目标玩家解析
- **EventPacket 联动**：其他模块（如 LoginView、OnlineRewards）可通过 EventPacket 的 `subtitle.play` 动作触发字幕组

## 依赖

| 类型 | 依赖 | 作用 | 缺少时表现 |
| --- | --- | --- | --- |
| 必需 | ArcartX | 注册 HUD 公告和字幕 UI，向客户端发送公告/字幕包 | 模块无法正常展示 UI |
| 可选 | PlaceholderAPI | 解析公告、字幕文本中的 `%...%` 变量 | 文本照常发送，但 PAPI 变量保持原样 |
| 可选 | EventPacket 模块 | 通过 `subtitle.play` 动作触发字幕组 | 不影响 Announcer 自身轮播 |

## 启用步骤

```yaml
modules:
  announcer:
    enabled: true
```

## 配置

### 公告主配置（`ArcartXAnnouncer.yml`）

```yaml
settings:
  debug: false                         # true 时打印发包与点击回包日志
  ui-id: "AXS:announcer_hud"          # 公告 HUD 的 UI ID（支持列表格式，详见 [多 UI 同时发包](/guide/multi-ui)）
  register-ui-on-enable: true          # 启动/重载时自动注册 HUD
  overwrite-ui-file: false             # 是否强制覆盖已导出的 UI 文件
  auto-play: true                      # false 时 HUD 仍同步配置但不滚动
  check-interval-ticks: 20             # 后台检查周期（20 tick = 1 秒）
  cooldown-ms: 30000                   # 一整轮播完后的冷却时间（毫秒）
  between-entry-interval-ms: 30000     # 同一轮中两条公告之间的间隔（毫秒）

entries-directory: "entries"           # 公告条目目录，相对模块数据目录
```

### 公告条目字段详解

条目文件位于 `data/announcer/entries/*.yml`，同一文件可包含多条公告：

```yaml
# data/announcer/entries/default.yml
welcome:
  enabled: true
  text: "欢迎来到服务器，祝你游玩愉快。"    # 支持颜色代码和 PAPI 变量
  click-command: ""                          # 玩家点击 HUD 时执行的控制台命令

player_tip:
  enabled: true
  text: "你好，%player_name%。记得每日签到！"
  click-command: "say <player> 点击了公告"  # <player> 替换为点击玩家名
```

| 字段 | 类型 | 说明 |
| --- | --- | --- |
| `enabled` | boolean | `false` 时跳过该条目 |
| `text` | string | 公告文本，支持 `&` 颜色代码和 PAPI |
| `click-command` | string | 点击后以控制台身份执行的命令，留空则不执行 |

### 字幕主配置（`ArcartXAnnouncer.yml` 的 `subtitle` 节）

```yaml
subtitle:
  settings:
    debug: false
    ui-id: "AXS:subtitle_hud"            # 支持列表格式，详见 [多 UI 同时发包](/guide/multi-ui)
    register-ui-on-enable: true
    overwrite-ui-file: false
    groups-directory: "subtitle/groups"    # 字幕组目录，相对插件数据目录
    show-background: true                  # 是否显示字幕底部背景板
```

### 字幕组字段详解

字幕组文件位于 `data/announcer/subtitle/groups/*.yml`，**文件名即组 ID**，顶层键为数字，代表播放顺序：

```yaml
# data/announcer/subtitle/groups/welcome.yml
1:
  text: "&f欢迎，%player_name%。"    # 支持颜色代码和 PAPI
  length: 0                          # 打字机总字数；0 = 自动按可见文本长度计算
  time: 1400                         # 打字机动画时长（毫秒）
  keep: 1                            # 动画结束后停留时间（秒）

2:
  text: "&e这是打字机字幕示例。"
  length: 0
  time: 1800
  keep: 1.5                          # 最后一帧结束后自动关闭 HUD
```

| 字段 | 类型 | 说明 |
| --- | --- | --- |
| 顶层键 | int | 数字，系统按从小到大顺序播放 |
| `text` | string | 字幕文本，支持颜色代码和 PAPI |
| `length` | int | 打字机总字数；`0` 或负数则自动计算 |
| `time` | int | 打字机动画时长（毫秒） |
| `keep` | double | 动画结束后停留秒数，最后一帧结束后关闭 HUD |

## 命令

### 管理命令（权限：`arcartxsuite.admin`）

| 命令 | 说明 |
| --- | --- |
| `/axs announcer status` | 查看播报模块状态、公告条目数和字幕组数量 |
| `/axs announcer reload` | 重载播报配置和 HUD（公告 + 字幕一起重载） |
| `/axs announcer subtitle list` | 列出所有已加载的字幕组 ID |
| `/axs announcer subtitle play <玩家> <字幕组ID>` | 向在线玩家播放指定字幕组 |
| `/axs announcer subtitle stop <玩家>` | 立即停止玩家当前正在播放的字幕 |

## UI / Packet

| 功能 | UI ID | Packet 说明 |
| --- | --- | --- |
| 公告 HUD | `AXS:announcer_hud` | 服务端推 `init`（含所有条目列表），客户端点击推 `click` 回包（含条目 ID） |
| 字幕 HUD | `AXS:subtitle_hud` | 服务端按帧推 `play`（含 `text`、`length`、`time`、`keep`），组结束推 `close` |

### 公告 HUD Packet 字段

| 字段 | 说明 |
| --- | --- |
| `entries` | 所有启用的公告条目列表（`id`、`text`、`has-command`） |
| `current` | 当前显示的条目 ID |

### 字幕 HUD Packet 字段

| 字段 | 说明 |
| --- | --- |
| `text` | 当前帧字幕文本（已解析 PAPI） |
| `length` | 打字机总字数 |
| `time` | 动画时长（毫秒） |
| `keep` | 停留时间（秒） |
| `show-background` | 是否显示背景板 |

## EventPacket 联动

Announcer 通过 `SubtitlePlayable` capability 向 EventPacket 注册，支持以下动作：

| 动作类型 | 参数 | 说明 |
| --- | --- | --- |
| `subtitle.play` | `group-id` | 向触发玩家播放指定字幕组 |
| `subtitle.stop` | — | 停止触发玩家当前字幕 |

EventPacket 配置示例（`data/eventpacket/rules/join.yml`）：

```yaml
join_welcome_subtitle:
  enabled: true
  trigger: join
  repeatable: true
  actions:
    - type: subtitle.play
      group-id: "welcome"
```
