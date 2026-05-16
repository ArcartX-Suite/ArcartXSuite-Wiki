# Announcer 播报系统

## 功能定位

服务器信息播报一站式方案，包含两大能力：

- **HUD 公告** — 常驻 / 轮播文字公告，可点击执行后台命令
- **打字机字幕** — 按帧播放文本动画，适用于剧情对白、任务提示、登录欢迎

**几乎零依赖，适合作为第一个验证模块。**

### 核心特性

- **轮播公告**：多条公告按配置顺序自动轮播，可配置每条停留时间和轮播冷却
- **PlaceholderAPI 解析**：公告文本支持 `%player_name%`、`%server_online%` 等 PAPI 变量，按接收玩家解析
- **点击命令**：每条公告可绑定控制台命令，玩家点击 HUD 后自动执行（支持 `<player>` 变量）
- **打字机字幕**：逐字/逐帧播放文本动画，可控制每帧速度、停留时间、文本内容
- **字幕组管理**：字幕定义放在独立目录 `subtitle/groups/*.yml`，支持热重载
- **EventPacket 联动**：其他模块（如 LoginView、OnlineRewards）可通过 EventPacket 的 `subtitle.play` 动作触发字幕播放
- **HUD 自动注册**：启动时自动将公告 HUD 和字幕 HUD 注册到 ArcartX，无需手动配置 UI 文件

## 依赖

| 类型 | 依赖 | 作用 | 缺少时表现 |
| --- | --- | --- | --- |
| 必需 | ArcartX | 注册 HUD 公告和字幕 UI，向客户端发送公告/字幕包 | 模块无法正常展示 UI |
| 可选 | PlaceholderAPI | 解析公告、字幕文本中的 `%...%` 变量 | 文本照常发送，但 PAPI 变量保持原样或按空值处理 |
| 可选 | EventPacket 模块 | 通过 `subtitle.play` 动作触发字幕组 | 不影响 Announcer 自身轮播，只是不能用 EventPacket 联动字幕 |

## 启用步骤

```yaml
modules:
  announcer:
    enabled: true
```

## 配置

Announcer 的配置分为公告和字幕两部分。

### 公告（`ArcartXAnnouncer.yml`）

```yaml
settings:
  debug: false
  ui-id: "AXS:announcer_hud"
  register-ui-on-enable: true
  overwrite-ui-file: false

entries:
  welcome:
    enabled: true
    text: "欢迎来到服务器 — 你好，%player_name%。"
    click-command: "say <player> 点了公告"
```

### 字幕

打字机字幕动画配置，位于同一配置文件的 `groups` 节：

```yaml
settings:
  debug: false
  ui-id: "AXS:subtitle_hud"
  register-ui-on-enable: true

groups:
  welcome_cinematic:
    frames:
      - text: "欢迎来到..."
        length: 0
        time: 1000
        keep: 0.5
      - text: "冒险开始！"
        length: 0
        time: 800
        keep: 1.0
```

## 命令

> 权限：`arcartxsuite.admin`

| 命令 | 说明 |
| --- | --- |
| `/axs announcer status` | 查看播报模块状态和公告/字幕组数量 |
| `/axs announcer reload` | 重载播报配置和 HUD |
| `/axs announcer subtitle list` | 列出所有已加载的字幕组 ID |
| `/axs announcer subtitle play <玩家> <字幕组ID>` | 向在线玩家播放打字机字幕动画 |
| `/axs announcer subtitle stop <玩家>` | 停止玩家当前正在播放的字幕 |

## UI / Packet

| 功能 | UI ID | 说明 |
| --- | --- | --- |
| 公告 HUD | `AXS:announcer_hud` | 服务端推 `init`，客户端点击推 `click` 回包 |
| 字幕 HUD | `AXS:subtitle_hud` | 服务端按帧推 `play`，组结束推 `close` |
