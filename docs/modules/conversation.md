# Conversation 对话桥

## 功能定位

将 Chemdah 对话系统桥接到 ArcartX UI，配合 Adyeshach NPC 实现可视化对话面板。

### 核心特性

- **可视化对话面板**：替代传统聊天栏对话，ArcartX UI 渲染说话人头像、文本和选项按钮
- **Chemdah 桥接**：自动拦截 Chemdah 对话事件，将对话帧推送到 ArcartX UI
- **Adyeshach NPC 联动**：NPC 检测范围可配置，靠近 NPC 自动触发对话
- **选项交互**：客户端显示对话选项列表，玩家点击后回包触发 Chemdah 后续流程
- **HUD 自动注册**：启动时自动注册对话面板 UI

## 依赖

| 类型 | 依赖 | 作用 | 缺少时表现 |
| --- | --- | --- | --- |
| 必需 | ArcartX | 注册对话面板 UI，并接收玩家点击选项回包 | 模块无法显示可视化对话 |
| 必需 | Chemdah | 提供任务/对话流程、对话帧和选项执行 | 模块不会加载 |
| 可选 | Adyeshach | 读取附近 NPC、做 NPC 对话入口和选择器展示 | Chemdah 对话桥仍可用，NPC 靠近触发/选择器不可用 |
| 可选 | EventPacket 模块 | 用事件动作触发对话相关流程 | 不影响 Chemdah 原生对话桥 |

## 启用步骤

```yaml
modules:
  conversation:
    enabled: true
```

同时需要在 Chemdah 的对应对话配置中指定 AXS 注册的对话主题：

```yaml
theme: 'ArcartXConversation'
```

没有设置这个 `theme` 时，Chemdah 仍会使用自己的默认对话主题，Conversation 模块虽然已加载，但玩家不会看到 ArcartX 的可视化对话 UI。

## 关键配置（`ArcartXConversation.yml`）

```yaml
debug: false

theme:
  name: ArcartXConversation

client:
  # 支持列表格式，详见 [多 UI 同时发包](/guide/multi-ui)
  dialog-ui-id: AXS:conversation_menu
  selector-ui-id: AXS:conversation_selector_hud
  register-ui-on-enable: true
  overwrite-ui-files: false

interaction:
  enabled: true
  scan-range: 6.0
  scan-period-ticks: 10
  selector-sticky-ms: 1500
  open-cooldown-ms: 350
  reply-debounce-ms: 250
  suppress-reopen-ms: 500

keybinds:
  confirm:
    name: AXS_CONVERSATION_CONFIRM
    default-key: F
  previous:
    name: AXS_CONVERSATION_PREVIOUS
    default-key: NUMPAD_8
  next:
    name: AXS_CONVERSATION_NEXT
    default-key: NUMPAD_2
```

## NPC 外观配置（`npc-appearances`）

模块启动 / 重载时，自动为指定 Adyeshach NPC 应用 ArcartX 模型与动画，无需手动命令。

依赖 **ArcartX** 和 **Adyeshach** 同时可用；`npc` 字段匹配 NPC 显示名、自定义名或 Adyeshach ID（忽略大小写）。

### 模式 A — 持久默认动画（`setDefaultState`）

不填 `animation-speed`，调用 `setDefaultState(state, animation)`，动画持续生效。

```yaml
npc-appearances:
  - npc: "村长老王"
    model: npc_village_elder
    scale: 1.0
    state: idle
    animation: idle_loop
```

### 模式 B — 一次性播放（`playAnimation`，带速度控制）

填写 `animation-speed > 0`，调用 `playAnimation(animation, speed, transitionTime, keepTime)`。

```yaml
npc-appearances:
  - npc: "铁匠张三"
    model: npc_blacksmith
    scale: 1.0
    animation: hammer_swing
    animation-speed: 1.5      # > 0 时启用此模式（1.0 = 正常速度）
    transition-time: 100      # 过渡时间，毫秒，默认 5
    keep-time: -1             # 持续时间，毫秒，-1 = 播放完整动画
```

### 字段说明

| 字段 | 必填 | 默认值 | 说明 |
| --- | --- | --- | --- |
| `npc` | ✓ | — | NPC 显示名 / 自定义名 / Adyeshach ID |
| `model` | ✓ | — | ArcartX 模型 ID |
| `scale` | | `1.0` | 模型缩放比例 |
| `state` | | — | 模式 A：动画状态名（如 `idle`） |
| `animation` | | — | 动画名称（两种模式均用此字段） |
| `animation-speed` | | `0` | **> 0 时启用模式 B**；模式 A 不填此项 |
| `transition-time` | | `5` | 模式 B：过渡时间（毫秒） |
| `keep-time` | | `-1` | 模式 B：持续时间（毫秒），`-1` 播放完整动画 |

配置修改后执行 `/axs conversation reload` 即可重新应用。

---

## Chemdah 对话主题配置

在需要使用 ArcartX UI 渲染的 Chemdah 对话文件里加入：

```yaml
theme: 'ArcartXConversation'
```

Chemdah的conversation示例：

```yaml
__option__:
  theme: 'ArcartXConversation'
  title: '{name}'
欢迎词_0:
  npc id: 'Adyeshach 小师妹'
  npc:
    - '&f&l你好!少侠{{ sender }}'
    - '&f&l欢迎来到流云琼阁'
    - '&7&l(使用&e鼠标&7点击选项即可)'
  format: generic
  player:
    - reply: '&f&l你好!'
      then: |
        goto 欢迎词_0_1
    - reply: '&f&l再见!'
      then: |
        close
```

`ArcartXConversation` 必须与 `ArcartXConversation.yml` 中的 `theme.name` 保持一致。如果你改了 `theme.name`，Chemdah 对话文件里的 `theme` 也要同步修改。

修改后执行：

```txt
/axs conversation reload
```

如果 Chemdah 本身不会热重载对话配置，还需要按你的 Chemdah 管理方式重载 Chemdah 或重启服务器。

## 常见配置问题

| 现象 | 常见原因 | 处理方式 |
| --- | --- | --- |
| Conversation 模块已加载，但仍显示 Chemdah 默认聊天栏对话 | Chemdah 对话文件没有写 `theme: 'ArcartXConversation'` | 给对应对话补上 theme 并重载 |
| 控制台显示已注册 `ArcartXConversation`，但某个 NPC 不弹 AXS UI | 只有部分对话配置了 theme | 检查该 NPC 对应的 Chemdah 对话文件 |
| 修改 `ArcartXConversation.yml` 的 `theme.name` 后全部失效 | Chemdah 侧仍写旧主题名 | 两边主题名保持一致 |

## 命令

> 权限：`arcartxsuite.admin`

| 命令 | 说明 |
| --- | --- |
| `/axs conversation status` | 查看对话桥模块状态（交互增强、NPC 桥接等就绪情况） |
| `/axs conversation reload` | 重载对话配置、UI，并重新应用 `npc-appearances` |
| `/axs conversation adyeshach setModel <名称> <modelID> <scale>` | 即时为指定 NPC 设置模型 |
| `/axs conversation adyeshach setAnimation <名称> <state> <animName>` | 即时为指定 NPC 设置持久默认动画 |
| `/axs conversation adyeshach playAnimation <名称> <动画名> <速度> [过渡ms] [持续ms]` | 即时为指定 NPC 一次性播放动画（可调速度） |

## UI / Packet

- 对话 UI ID：`AXS:conversation_menu`
- NPC 选择 HUD ID：`AXS:conversation_selector_hud`
- 服务端推对话帧（说话人、文本、选项列表），客户端推选项选择回包
