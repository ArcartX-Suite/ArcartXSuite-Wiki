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
  # 注册到 Chemdah ConversationService 的主题名。
  # Chemdah 对话配置中的 theme 必须填写同一个名字。
  name: ArcartXConversation

ui:
  dialog-ui-id: AXS:conversation_menu
  selector-ui-id: AXS:conversation_selector_hud
  export-default-ui: true
  overwrite-exported-ui: false

interaction:
  npc-detect-range: 5.0
  suppress-reopen-ms: 1200
```

## Chemdah 对话主题配置

在需要使用 ArcartX UI 渲染的 Chemdah 对话文件里加入：

```yaml
theme: 'ArcartXConversation'
```

示例：

```yaml
example_conversation:
  name: '村民'
  theme: 'ArcartXConversation'
  dialog:
    - '你好，欢迎来到服务器。'
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
| `/axs conversation status` | 查看对话桥模块状态 |
| `/axs conversation reload` | 重载对话配置和 UI |

## UI / Packet

- 对话 UI ID：`AXS:conversation_menu`
- NPC 选择 HUD ID：`AXS:conversation_selector_hud`
- 服务端推对话帧（说话人、文本、选项列表），客户端推选项选择回包
