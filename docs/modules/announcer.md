# Announcer 播报系统

## 功能定位

服务器信息播报一站式方案，包含两大能力：

- **HUD 公告** — 滚动轮播文字公告，文字从右向左滚动，可点击执行后台命令
- **打字机字幕** — 按帧播放文本动画，适用于剧情对白、任务提示、登录欢迎

**几乎零依赖，适合作为第一个验证模块。**

### 核心特性

**公告：**
- **滚动轮播**：多条公告按配置顺序自动轮播，文字从屏幕右侧向左滚动，滚完自动隐藏
- **PAPI 解析**：公告文本支持 `%player_name%`、`%server_online%` 等变量，按接收玩家实时解析
- **点击命令**：每条公告可绑定控制台命令，玩家点击 HUD 后自动执行（`<player>` 变量替换为点击玩家名）
- **手动广播**：通过命令加入队列或立即广播自定义文本，适合紧急通知
- **热重载**：`/axs announcer reload` 即时生效，HUD 不中断

**字幕：**
- **打字机动画**：逐字播放文本，可控制动画时长、停留时间和自动文本长度计算
- **顺序播放**：字幕组按数字键顺序播放，最后一帧结束后自动关闭 HUD
- **组级 UI 覆盖**：每个字幕组可指定独立的 UI ID，不同场景使用不同样式
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
  text-width-font-size: 60             # 滚动文字宽度估算字体大小（自适应坐标），调大背景停留更久

entries-directory: "entries"           # 公告条目目录，相对模块数据目录
```

**配置字段详解：**

| 字段 | 类型 | 默认值 | 说明 |
| --- | --- | --- | --- |
| `debug` | boolean | `false` | 开启后在控制台打印每次 `display` 发包内容和玩家点击回包信息 |
| `ui-id` | string / list | `"AXS:announcer_hud"` | 公告 HUD 的 UI ID，支持列表格式同时向多个 UI 发包 |
| `register-ui-on-enable` | boolean | `true` | 模块启动/重载时是否自动向 ArcartX 注册 HUD |
| `overwrite-ui-file` | boolean | `false` | 是否在每次启动时强制覆盖 `plugins/ArcartXSuite/ui/announcer_hud.yml` |
| `auto-play` | boolean | `true` | 是否自动轮播公告；`false` 时 HUD 仍注册但不主动滚动 |
| `check-interval-ticks` | long | `20` | 后台定时任务检查周期（tick），20 tick = 1 秒 |
| `cooldown-ms` | long | `30000` | 全部公告播完一整轮后的冷却等待时间（毫秒） |
| `between-entry-interval-ms` | long | `30000` | 同一轮中，上一条公告显示结束后切到下一条公告的等待时间（毫秒） |
| `text-width-font-size` | int | `60` | 服务端估算滚动文字宽度的字体大小参数（自适应坐标单位）。CJK 字符宽度 ≈ 此值，Latin 字符宽度 ≈ 此值 × 0.55。调大此值会让背景条显示更久，调小则更快隐藏 |
| `entries-directory` | string | `"entries"` | 公告条目所在目录，路径相对模块数据目录 `data/announcer/` |

### 公告条目

条目文件位于 `data/announcer/entries/*.yml`。**目录下所有 `.yml` 文件都会被加载**，按文件名字典序排列，同一文件可包含多条公告，根键即为条目 ID。

```yaml
# data/announcer/entries/default.yml
welcome:
  enabled: true
  text: "欢迎来到服务器，祝你游玩愉快。"    # 支持颜色代码和 PAPI 变量
  click-command: ""                          # 留空表示不执行命令

player_tip:
  enabled: true
  text: "你好，%player_name%。记得每日签到！"
  click-command: "say <player> 点击了公告"  # <player> 替换为点击玩家名
```

多文件示例——可将不同类型的公告分到不同文件：

```
data/announcer/entries/
├── default.yml         # 通用公告
├── events.yml          # 活动公告
└── tips.yml            # 小贴士
```

| 字段 | 类型 | 默认值 | 说明 |
| --- | --- | --- | --- |
| `enabled` | boolean | `true` | `false` 时跳过该条目，不下发到客户端 |
| `text` | string | `""` | 公告文本，支持 `&` 颜色代码和 PlaceholderAPI 变量 |
| `click-command` | string | `""` | 玩家点击 HUD 时以**控制台身份**执行的命令，`<player>` 替换为点击者名字。留空表示该条公告不可点击 |

### 字幕主配置（`ArcartXAnnouncer.yml` 的 `subtitle` 节）

```yaml
subtitle:
  settings:
    debug: false
    ui-id: "AXS:subtitle_hud"            # 支持列表格式，详见 [多 UI 同时发包](/guide/multi-ui)
    register-ui-on-enable: true
    overwrite-ui-file: false
    groups-directory: "subtitle/groups"    # 字幕组目录，相对模块数据目录
    show-background: true                  # 是否显示字幕底部背景板
```

| 字段 | 类型 | 默认值 | 说明 |
| --- | --- | --- | --- |
| `debug` | boolean | `false` | 开启后打印每帧 `play` 和 `close` 包内容 |
| `ui-id` | string / list | `"AXS:subtitle_hud"` | 字幕 HUD 的 UI ID，支持列表格式 |
| `register-ui-on-enable` | boolean | `true` | 模块启动时是否自动注册字幕 HUD |
| `overwrite-ui-file` | boolean | `false` | 是否强制覆盖 `ui/subtitle_hud.yml` |
| `groups-directory` | string | `"subtitle/groups"` | 字幕组文件所在目录，相对模块数据目录 |
| `show-background` | boolean | `true` | 是否在字幕 HUD 底部显示半透明背景板 |

### 字幕组

字幕组文件位于 `data/announcer/subtitle/groups/*.yml`，**文件名（不含 `.yml`）即组 ID**。顶层键为数字，按数字从小到大顺序播放：

```yaml
# data/announcer/subtitle/groups/welcome.yml
# 可选：组级 UI ID 覆盖，不填则使用全局 subtitle.settings.ui-id
# ui-id: "AXS:custom_subtitle_hud"
# 或列表格式：
# ui-id:
#   - "AXS:subtitle_hud_1"
#   - "AXS:subtitle_hud_2"

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

| 字段 | 类型 | 默认值 | 说明 |
| --- | --- | --- | --- |
| 顶层键 | int | — | 数字，系统按从小到大顺序播放 |
| `text` | string | `""` | 字幕文本，支持颜色代码和 PAPI |
| `length` | int | `0` | 打字机总字数；`0` 或负数则自动按可见文本长度计算（去除颜色代码后） |
| `time` | int | `1000` | 打字机动画时长（毫秒），即从 0 字到 `length` 字的过渡时间 |
| `keep` | double | `1.0` | 当前帧动画结束后继续停留的秒数。最后一帧的 `keep` 结束后自动关闭 HUD |
| `ui-id` | string / list | 继承全局 | **组级可选**，指定本字幕组使用哪个 UI 播放，不填则使用 `subtitle.settings.ui-id` |

## 命令

### 管理命令（权限：`arcartxsuite.admin`）

| 命令 | 说明 |
| --- | --- |
| `/axs announcer help` | 查看 Announcer 模块所有可用命令 |
| `/axs announcer status` | 查看模块状态：活跃公告数、已初始化玩家数、字幕组数、播放中玩家数和待播队列 |
| `/axs reload announcer` | 重载配置和 HUD（公告 + 字幕一起重载），HUD 不中断 |
| `/axs announcer broadcast <文本>` | 将一条自定义文本加入广播队列，当前公告展示结束后立即播报，不受冷却限制 |
| `/axs announcer broadcastnow <文本>` | 立即广播，强制打断当前正在展示的公告 |
| `/axs announcer subtitle list` | 列出所有已加载的字幕组 ID |
| `/axs announcer subtitle play <玩家> <字幕组ID>` | 向在线玩家播放指定字幕组。如果该玩家有字幕正在播放，会先终止旧的 |
| `/axs announcer subtitle stop <玩家>` | 立即停止玩家当前正在播放的字幕并关闭字幕 HUD |

**示例：**

```bash
# 查看状态
/axs announcer status

# 维护通知（加入队列，当前公告结束后播报）
/axs announcer broadcast &c服务器将于10分钟后维护，请及时保存进度！

# 紧急通知（立即打断当前公告）
/axs announcer broadcastnow &c&l紧急通知：服务器即将重启

# 播放字幕
/axs announcer subtitle play Steve welcome

# 停止字幕
/axs announcer subtitle stop Steve

# 重载配置
/axs reload announcer
```

::: tip 重载命令
Announcer 模块的重载命令是 `/axs reload announcer`（全局统一格式），而非 `/axs announcer reload`。在模块子命令中输入 `reload` 会提示正确用法。
:::

## UI / Packet

| 功能 | UI ID | 默认打开方式 | Packet 说明 |
| --- | --- | --- | --- |
| 公告 HUD | `AXS:announcer_hud` | `defaultOpen: true`（自动打开） | 服务端推 `display` 包控制滚动文字显示；客户端点击时推 `AXS_announcer_click` 回包 |
| 字幕 HUD | `AXS:subtitle_hud` | `defaultOpen: false`（按需打开） | 服务端按帧推 `play` 包驱动打字机动画；字幕组播完推 `close` 包关闭 HUD |

### 公告 HUD `display` 包字段

服务端每次切换公告或手动广播时向客户端推送：

| 字段 | 类型 | 说明 |
| --- | --- | --- |
| `isShow` | boolean | `true` 显示公告并开始滚动，`false` 隐藏 HUD |
| `id` | string | 当前公告条目 ID（手动广播时为 `"manual"`） |
| `text` | string | 公告文本（已解析 PAPI 和颜色代码） |
| `clickable` | boolean | 该条公告是否可点击（即 `click-command` 非空） |
| `revision` | string | 服务端时间戳 + 序号，用于客户端去重 |
| `textWidth` | int | 服务端估算的文字渲染宽度（自适应坐标），客户端用于判断滚动结束和隐藏背景 |

客户端点击时向服务端发送 `AXS_announcer_click` 包，携带当前条目 ID。

### 字幕 HUD `play` 包字段

服务端每播放一帧字幕时推送：

| 字段 | 类型 | 说明 |
| --- | --- | --- |
| `text` | string | 当前帧字幕文本（已解析 PAPI） |
| `length` | int | 打字机总字数 |
| `time` | int | 动画时长（毫秒） |
| `showBackground` | boolean | 是否显示背景板（来自全局配置 `subtitle.settings.show-background`） |

字幕组全部帧播完后，服务端推送空的 `close` 包，客户端重置字幕状态。

## EventPacket 联动

Announcer 通过 `SubtitlePlayable` capability 向 EventPacket 注册，支持以下动作：

| 动作类型 | 参数 | 说明 |
| --- | --- | --- |
| `subtitle.play` | `group-id` | 向触发玩家播放指定字幕组 |

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

## 完整运行流程

### 公告轮播流程

1. 模块启动 → 从 `entries-directory` 加载所有 `.yml` 文件中 `enabled: true` 的条目
2. 服务端定时任务每 `check-interval-ticks` tick 运行一次
3. 到达播出时间 → 取下一条公告 → 向所有在线玩家推送 `display` 包
4. 客户端收到 `display` 包 → 文字从屏幕右侧开始向左滚动 → 滚出左边界后自动隐藏背景
5. 等待 `between-entry-interval-ms` → 播下一条
6. 全部条目播完 → 等待 `cooldown-ms` → 从头开始

### 手动广播流程

1. 管理员执行 `/axs announcer broadcast 维护通知`
2. 文本加入内部队列
3. 下一次定时任务检查时优先消费队列 → 推送 `display` 包（`id` = `"manual"`）
4. `broadcastnow` 则跳过等待，立即打断当前展示并推送

### 字幕播放流程

1. 管理员或 EventPacket 触发 `/axs announcer subtitle play Steve welcome`
2. 服务端查找 `welcome.yml` 字幕组
3. 按数字键顺序依次推送 `play` 包
4. 客户端每帧执行打字机动画（`Lerp` 补间），动画结束后停留 `keep` 秒
5. 全部帧播完 → 服务端推送 `close` 包 → 客户端重置字幕 HUD
