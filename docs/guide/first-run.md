---
title: plugins/ArcartXSuite/ArcartXAnnouncer.yml | ArcartXSuite Minecraft插件文档
description: plugins/ArcartXSuite/ArcartXAnnouncer.yml - ArcartXSuite Minecraft 服务器插件文档。 ArcartXSuite 我的世界服务器插件套件。
---

# 第一次启用流程

为避免一次开全部模块导致排查困难，<strong>强烈推荐“先单模块 → 再全量”</strong>的渐进流程。

## 推荐顺序

1. **`Announcer`** — 几乎零依赖，验证 ArcartX 桥接是否正常
2. **`EntityTracker`** — 验证 MythicMobs 联动和 HUD 注册
3. **`RGB`**（可选）— 验证 PlaceholderAPI 流程
4. **`Title`** — 引入数据库（SQLite），验证持久化
5. **`Chat`** / **`Mail`** — 业务量更大的模块，放最后

## 单模块启用步骤

以 `Announcer` 为例：

### 1. 改 config.yml

```yaml
modules:
  announcer:
    enabled: true
```

### 2. 改模块配置（可选）

```yaml
# plugins/ArcartXSuite/ArcartXAnnouncer.yml
entries:
  welcome:
    enabled: true
    text: "欢迎来到 ArcartX 测试服 — 你好，%player_name%。"
    click-command: "say <player> 点了公告"
```

### 3. reload 并验证

```
/axs reload announcer
/axs announcer status
```

### 4. 进游戏看 HUD

带 ArcartX 客户端的玩家进入服务器，几秒后 HUD 应滚出公告文本。

| 现象 | 排查 |
| --- | --- |
| 控制台 `bridge missing` | ArcartX 服务端插件没装 |
| UI 注册成功但 HUD 不出现 | 客户端 MOD 没装 |
| HUD 出现但文字不对 | YAML 缩进/引号问题 |

## 全开

确认 1-2 个模块跑通后，逐个打开其他模块：

```
/axs reload all
/axs status
```
