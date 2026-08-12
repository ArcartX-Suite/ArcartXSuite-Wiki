---
title: Tooltip 动态提示桥接插件 | ArcartX-Suite Minecraft
description: ArcartX-Suite Tooltip 模块，接收客户端 Forge mod 采集的 TACZ 枪属性和 Apotheosis 神化词条 tooltip 数据，注入聊天物品预览 Lore，提供跨模块数据查询 API。
---

# Tooltip 动态提示桥接

**Tooltip** 模块是 ArcartX-Suite 与客户端 Forge mod（[ArcartX-Suite-Mod](https://github.com/xuanmomo233/ArcartX-Suite-Mod)）之间的桥梁，用于采集 TACZ 枪械和 Apotheosis 神化词条等 mod 动态生成的 tooltip 文本，并注入到 ArcartX 自定义 UI 的物品预览中。

## 解决的问题

ArcartX 自定义 UI 的物品预览（如聊天物品展示）默认只读取物品 NBT 中的 `display.Lore` 标签。但 TACZ 枪属性（伤害、穿甲、爆头倍率等）和 Apotheosis affix 描述是由 mod 在客户端 `ItemTooltipEvent` 中动态生成的，不存储在 NBT 中，因此 ArcartX UI 无法显示这些信息。

Tooltip 模块通过以下方式解决：

1. **客户端 mod** 监听 `ItemTooltipEvent`，采集完整 tooltip 文本行和结构化数据
2. 通过 ArcartX 自定义网络包发送给服务端
3. **Tooltip 模块** 接收并缓存数据
4. Chat 等模块在序列化物品预览时，从缓存取出 tooltip 文本行注入 NBT Lore

## 功能概览

| 功能 | 说明 |
|------|------|
| **接收客户端 tooltip 数据** | 监听 `AXS_TOOLTIP_DATA` 网络包，解析 tooltip 文本行和结构化数据 |
| **去重缓存** | 按玩家 + 物品指纹（物品注册名）去重，TTL 60 秒 |
| **聊天预览 Lore 注入** | Chat 模块在 `itemToJson` 前调用，将 tooltip 文本行写入物品 `display.Lore` |
| **跨模块数据查询 API** | 提供 `TooltipDataCapable` Capability，供其他模块查询 tooltip 数据 |
| **玩家退出自动清理** | 监听 `PlayerQuitEvent`，清理离线玩家的缓存数据 |

## 依赖

| 依赖 | 是否必须 | 用途 |
|------|----------|------|
| ArcartX | ✅ 必须 | 网络包通信基础 |
| ArcartX-Suite-Mod（客户端） | ✅ 必须 | 客户端 Forge mod，采集 tooltip 数据 |

::: warning 客户端 mod 必需
本模块需要所有玩家安装 [ArcartX-Suite-Mod](https://github.com/xuanmomo233/ArcartX-Suite-Mod/releases) 客户端 mod。未安装的玩家不会发送 tooltip 数据，聊天预览中也不会显示动态 tooltip 内容。
:::

## 命令

### 管理员命令

| 命令 | 说明 |
|------|------|
| `/axs tooltip help` | 查看可用子命令 |
| `/axs tooltip status` | 查看模块缓存状态（缓存玩家数、数据包 ID、TTL） |

## 权限

本模块无独立权限节点。

## PlaceholderAPI 占位符

本模块不注册 PlaceholderAPI 占位符扩展。

## 配置

本模块无需配置文件，启用即用。

在 `config.yml` 中启用：

```yaml
modules:
  tooltip:
    enabled: true
```

## 网络包协议

### 客户端 → 服务端

| packet ID | 说明 |
|-----------|------|
| `AXS_TOOLTIP_DATA` | 客户端 mod 在 `ItemTooltipEvent` 中采集 tooltip 数据后发送 |

::: tip 客户端发送冷却
`ItemTooltipEvent` 每帧触发（约 60 次/秒），客户端 mod 内置 **2 秒冷却**：同一物品指纹在 2 秒内不会重复发送，避免网络流量爆炸。服务端缓存 TTL 为 60 秒，即使客户端冷却导致数据延迟，聊天预览仍能命中缓存。
:::

**数据格式**（`List<String> data`）：

| 索引 | 字段 | 说明 |
|------|------|------|
| `data[0]` | 物品指纹 | 物品注册名（如 `tacz:ak47` 或 `minecraft:diamond_sword`），用于服务端缓存匹配 |
| `data[1]` | 物品类型 ID | 同 `data[0]`，保留用于扩展 |
| `data[2]` | tooltip 文本行 | JSON 数组字符串，如 `["伤害: 15","穿甲: 50%"]` |
| `data[3]` | 结构化数据 | JSON 对象字符串，包含 TACZ 枪属性数值字段 |

### 结构化数据字段（TACZ 枪械）

```json
{
  "gunId": "tacz:ak47",
  "damage": 15.0,
  "bulletAmount": 1,
  "armorIgnore": 0.5,
  "headshotMultiplier": 1.5,
  "weight": 0.8,
  "movementSpeedPenalty": -0.24,
  "maxAmmo": 30,
  "currentAmmo": 30,
  "level": 5,
  "type": "rifle"
}
```

## Capability API

本模块注册 `TooltipDataCapable` 能力，供其他模块查询 tooltip 数据。

### TooltipDataCapable

```java
public interface TooltipDataCapable {
    /** 获取玩家当前手持物品的 tooltip 文本行 */
    List<String> getTooltipLines(Player player);

    /** 获取玩家指定物品的 tooltip 文本行 */
    List<String> getTooltipLines(Player player, ItemStack itemStack);

    /** 获取玩家当前手持物品的结构化数据（JSON 字符串） */
    String getStructuredData(Player player);

    /** 将 tooltip 文本行注入物品 NBT Lore，用于物品预览序列化 */
    ItemStack injectLore(ItemStack itemStack, List<String> lines);

    /** 判断指定物品是否有缓存的 tooltip 数据 */
    boolean hasTooltipData(Player player, ItemStack itemStack);
}
```

### 使用示例

```java
public class MyModule extends AbstractAXSModule {
    private Supplier<TooltipDataCapable> tooltipSupplier;

    @Override
    protected void startService() {
        tooltipSupplier = () -> getCapability(TooltipDataCapable.class);

        // 示例：获取玩家手持物品的 tooltip 文本行
        TooltipDataCapable tooltip = tooltipSupplier.get();
        if (tooltip != null) {
            List<String> lines = tooltip.getTooltipLines(player);
            // lines 包含 TACZ 枪属性和 Apotheosis affix 文本行
        }
    }
}
```

## 与 Chat 模块的集成

Chat 模块在构建聊天物品预览时，自动调用 Tooltip 模块注入 Lore：

```
玩家发送聊天消息（手持物品）
  → ChatService.buildItemPreview()
    → 查询 TooltipDataCapable.getTooltipLines()
    → 调用 TooltipDataCapable.injectLore() 写入 NBT Lore
    → ItemBridgeAPI.itemToJson() 序列化
      → 客户端 ArcartX UI 渲染物品预览（含动态 tooltip）
```

::: tip 自动集成
Chat 模块已内置 Tooltip 数据注入逻辑，无需额外配置。只要 Tooltip 模块和 Chat 模块同时启用，聊天物品预览会自动显示 TACZ 枪属性和 Apotheosis affix。
:::

## 架构

```
TooltipModule (AbstractAXSModule, ModuleCommandHandler)
├── TooltipPacketHandler (ClientPacketHandler)
│   └── 接收 AXS_TOOLTIP_DATA 网络包
│       └── 解析 JSON → 存入 TooltipDataCache
├── TooltipDataCache (去重缓存)
│   ├── 按玩家 UUID 分区
│   ├── 按物品指纹（物品注册名）去重
│   └── TTL 60 秒自动过期
├── TooltipDataCapability (TooltipDataCapable 实现)
│   ├── getTooltipLines() — 查询缓存
│   ├── getStructuredData() — 查询结构化数据
│   ├── injectLore() — 注入 NBT Lore
│   └── hasTooltipData() — 判断是否有数据
├── TooltipJsonParser (JSON 解析)
│   └── 解析客户端发来的 JSON 格式 tooltip 数据
├── TooltipAdminCommand (管理员命令)
│   ├── help — 查看帮助
│   └── status — 查看缓存状态
├── TooltipPlayerListener (Bukkit 事件监听)
│   └── PlayerQuitEvent → 清理玩家缓存
├── PacketGuard (频率限制)
│   └── moduleKey: "tooltip"
└── 注册 Capability
    └── TooltipDataCapable.class → TooltipDataCapability
```

## 客户端 mod 安装

1. 从 [GitHub Release](https://github.com/xuanmomo233/ArcartX-Suite-Mod/releases) 下载 `ArcartX-Suite-Mod-*.jar`
2. 放入客户端 `mods/` 目录
3. 确保 ArcartX mod 已安装
4. 启动游戏，悬停物品时 mod 会自动采集 tooltip 数据并发送给服务端

## 相关文档

- [Capability 跨模块通信](/api/capability) — `TooltipDataCapable` 接口文档
- [Chat 聊天模块](/modules/chat) — 聊天物品预览功能
- [ArcartX-Suite-Mod GitHub](https://github.com/xuanmomo233/ArcartX-Suite-Mod) — 客户端 mod 源码
