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

- 必需：ArcartX、Chemdah、Adyeshach

## 启用步骤

```yaml
modules:
  conversation:
    enabled: true
```

## 关键配置（`ArcartXConversation.yml`）

```yaml
settings:
  debug: false
  ui-id: "AXS:conversation_panel"
  register-ui-on-enable: true
  overwrite-ui-file: false
  npc-detect-range: 5.0
```

## 命令

> 权限：`arcartxsuite.admin`

| 命令 | 说明 |
| --- | --- |
| `/AXS conversation status` | 查看对话桥模块状态 |
| `/AXS conversation reload` | 重载对话配置和 UI |

## UI / Packet

- UI ID：`AXS:conversation_panel`
- 服务端推对话帧（说话人、文本、选项列表），客户端推选项选择回包
