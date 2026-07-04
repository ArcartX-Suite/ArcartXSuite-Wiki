# Menu 通用菜单使用指南

本指南基于 ArcartX-Suite Menu 模块的源码实现与 [`modules/menu.md`](/modules/menu) 参考文档，面向服主与配置人员，系统讲解如何通过 YAML 配置驱动 ArcartX UI 菜单。所有示例均可在服务器上直接运行，关键步骤已加注中文注释。

::: tip 付费模块
Menu 为付费模块，需在 [云端平台](https://cloud.021209.xyz) 购买/领取授权后，于「装备模块」页面勾选到对应服务器。详细授权说明见 [云端授权](/guide/cloud-modules)。
:::

---

## 一、基本引入方法

### 1.1 启用模块

在 ArcartX-Suite 主配置 `config.yml` 中启用 Menu 模块：

```yaml
modules:
  menu:
    enabled: true
```

重启或执行 `/axs menu reload` 后，插件会自动：

1. 加载 `plugins/ArcartX-Suite/modules/ArcartX-Suite-Menu-*.jar`。
2. 在数据目录下生成 `ArcartXMenu.yml`（主配置）、`messages.yml`（消息文本）。
3. 在 `menus/` 目录下导出默认示例菜单 `example.yml` 与 `esc_main.yml`。
4. 向 ArcartX 客户端注册 UI：`ui/menu_panel.yml`、`ui/menu_esc.yml`。

### 1.2 文件结构

```text
plugins/ArcartX-Suite/
├── modules/ArcartX-Suite-Menu-*.jar   # 模块 jar
├── data/menu/
│   ├── ArcartXMenu.yml                # 模块主配置
│   ├── messages.yml                   # 提示消息
│   ├── ui/
│   │   ├── menu_panel.yml             # 居中面板 UI
│   │   └── menu_esc.yml               # ESC 左侧滑出 UI
│   └── menus/                         # 菜单定义目录
│       ├── example.yml
│       └── esc_main.yml
```

### 1.3 常用命令

| 命令 | 权限 | 说明 |
| --- | --- | --- |
| `/menu open <菜单ID>` | `arcartxsuite.menu.use` | 打开指定菜单 |
| `/menu list` | `arcartxsuite.menu.use` | 列出已加载菜单 |
| `/axs menu reload` | `axs.menu.reload` | 重载 Menu 模块配置与菜单定义 |
| `/axs menu open <ID> [玩家]` | `axs.menu.open.other` | 管理员代开菜单 |

---

## 二、核心 API 参数说明

Menu 模块没有传统意义上的 JavaScript API，其「API」是一套声明式 YAML 配置字段。掌握这些字段即可配置出任意复杂菜单。

### 2.1 菜单级字段（MenuDefinition）

| 字段 | 类型 | 必填 | 默认值 | 说明 |
| --- | --- | --- | --- | --- |
| `id` | string | 是 | — | 菜单唯一标识，全小写+下划线，用于 `open:` 动作与命令引用 |
| `title` | string | 是 | `id` | 菜单标题，支持 `&` 颜色代码与 MiniMessage |
| `layout` | string | 否 | `panel` | 布局：`panel`（居中面板）或 `esc`（左侧滑出，替代 ESC 界面） |
| `columns` | int | 否 | 2 | 每行列数，至少为 1 |
| `buttons-per-page` | int | 否 | 12 | 每页最大按钮数，至少为 1 |
| `permission` | string | 否 | 空 | 打开菜单所需权限节点，留空表示无限制 |
| `match-esc` | boolean | 否 | false | 是否拦截 ESC 键显示此菜单（仅 `layout: esc` 生效） |
| `open-requirements` | list | 否 | 空 | 打开菜单的前置条件，全部满足才能打开 |
| `open-actions` | list | 否 | 空 | 打开菜单时执行的动作列表 |
| `close-actions` | list | 否 | 空 | 关闭菜单时执行的动作列表 |
| `commands` | list | 否 | 空 | 注册为玩家命令，如 `shop` 对应 `/shop` |
| `command-regex` | list | 否 | 空 | 命令正则匹配，用于更灵活的命令拦截 |
| `item-binds` | list | 否 | 空 | 将菜单绑定到手持/副手物品 |
| `pages` | list/map | 否 | `[main]` | 页面列表，每个页面包含一组按钮 |
| `footer-buttons` | map | 否 | 空 | 页脚固定按钮（通常用于 `esc` 布局） |

### 2.2 页面字段（MenuPageDefinition）

```yaml
pages:
  - id: main           # 页面 ID，用于 page: 动作切换
    title: "&f首页"     # 页面副标题
    buttons:           # 按钮集合
      home: { ... }
```

::: info 简写形式
如果菜单只有一页且不需要副标题，可直接写 `buttons:` 在根级别，模块会自动生成 `id: main` 的默认页面。
:::

### 2.3 按钮字段（MenuButtonDefinition）

| 字段 | 类型 | 必填 | 默认值 | 说明 |
| --- | --- | --- | --- | --- |
| `text` | string | 否 | 按钮 ID | 按钮显示文字 |
| `order` | int | 否 | 0 | 排列顺序，数字越小越靠前 |
| `permission` | string | 否 | 空 | 按钮级权限，无权限玩家**看不到**该按钮 |
| `requirements` / `view-conditions` | list | 否 | 空 | 可见条件，不满足时按钮**隐藏** |
| `condition` / `use-conditions` | list | 否 | 空 | 使用条件，不满足时仍可显示并触发 `deny-message` |
| `deny-message` | string | 否 | 空 | 使用条件未通过时的提示消息 |
| `icon` | map | 否 | 空 | 按钮图标配置，见 2.4 |
| `client-action` | string | 否 | 空 | 客户端原生动作：`options`（打开设置）、`quit`（退出游戏），一般用于页脚 |
| `actions` | list | 否 | 空 | 点击后执行的动作列表 |

### 2.4 图标字段（MenuIconDefinition）

| 字段 | 类型 | 说明 |
| --- | --- | --- |
| `material` | string | Minecraft 物品 ID，如 `DIAMOND`、`PLAYER_HEAD` |
| `amount` | int | 物品堆叠数量，默认 1 |
| `name` | string | 图标悬浮名称 |
| `lore` | list | 图标描述，每行一个字符串 |
| `custom-model-data` | int | CustomModelData，用于资源包自定义模型 |
| `glow` | boolean | 是否附加隐藏附魔光效 |
| `skull-texture` | string | 玩家头颅贴图，支持 base64 或 http(s) URL |
| `color` | string | 皮革盔甲染色，如 `#FF5555` |
| `source` / `id` | string | 外部物品库：`neigeitems`、`mythicmobs`、`overture` |
| `mmo-type` / `mmo-id` | string | MMOItems 物品定位 |
| `texture` | string | ArcartX 自定义贴图，写入 `icon` NBT |
| `texture-url` / `url` | string | ArcartX URL 贴图，写入 `url` NBT |
| `nbt` | map | 任意字符串 NBT 键值对 |
| `json` | string | 直接提供完整物品 JSON，会短路其他图标字段 |

### 2.5 动作类型表（MenuActionType）

Menu 模块内置以下动作，格式统一为 `"<类型>: <参数>"`：

| 类型 | 别名 | 示例 | 行为 |
| --- | --- | --- | --- |
| `command` | `cmd`、`player` | `command: spawn` | 以玩家身份执行命令，自动去掉开头 `/` |
| `console` | `op` | `console: eco give {player} 100` | 以控制台身份执行命令 |
| `message` | `msg`、`tell` | `message: "&a操作成功"` | 向玩家发送消息 |
| `open` | `menu` | `open: shop` | 打开另一个菜单 |
| `page` | — | `page: next` | 切换到同菜单的其他页面 |
| `close` | — | `close` | 关闭当前菜单 |
| `sound` | — | `sound: UI_BUTTON_CLICK\|1\|1` | 播放音效，格式 `音效名\|音量\|音调` |

::: warning 不支持的动作
源码中**未实现** `title`、`subtitle`、`actionbar`、`delay`、`back`、`refresh` 等动作。若需要标题/动作栏/延迟，请通过 `command:` 调用其他插件命令，或使用 EventPacket 模块实现。
:::

### 2.6 条件字段别名速查

| 语义 | 可用字段名 | 不满足表现 |
| --- | --- | --- |
| 可见条件 | `requirements`、`view-conditions`、`viewConditions`、`conditions`、`aria-conditions`、`ariaConditions` | 按钮从 UI 移除 |
| 使用条件 | `condition`、`use-conditions`、`useConditions`、`click-conditions`、`clickConditions`、`aria-condition`、`ariaCondition` | 按钮仍显示，点击后发送 `deny-message` |

条件支持三种写法：

```yaml
# 1. PlaceholderAPI 行内表达式
requirements:
  - "%player_level% >= 10"
  - "%player_world% == world"

# 2. Aria 脚本（需 Blink 系插件注入 BlinkAriaHost）
condition:
  - "aria: return player.getLevel() >= 10"

# 3. JS 脚本（通过共享条件引擎，自动识别 JS 语法）
condition:
  - "return player.getLevel() >= 10"
```

---

## 三、完整初始化配置示例

以下是一份可直接放入 `plugins/ArcartX-Suite/data/menu/ArcartXMenu.yml` 的主配置示例：

```yaml
config-version: 1

# 调试开关：开启后会在控制台输出更详细的菜单加载日志
debug:
  enabled: false

# 客户端通信与 UI 注册配置
client:
  packet-id: "AXS_MENU"                 # 服务端与客户端约定的包 ID，一般无需修改
  panel-ui-id: "AXS:menu_panel"         # 居中面板 UI 标识
  esc-ui-id: "AXS:menu_esc"             # ESC 菜单 UI 标识
  register-ui-on-enable: true           # 启用时自动向 ArcartX 注册 UI
  overwrite-ui-files: false             # 是否在重载时覆盖 UI 文件（开发阶段可设为 true）
  esc-menu-id: "esc_main"               # 默认 ESC 菜单 ID

# 菜单模块行为设置
settings:
  menus-directory: "menus"              # 菜单定义文件存放目录（相对 data/menu）
  default-layout: "panel"               # 默认布局：panel 或 esc
  columns: 2                            # 默认每行列数
  buttons-per-page: 12                  # 默认每页按钮数
  click-cooldown-ms: 300                # 按钮点击冷却（毫秒），防止玩家连点刷包
  close-on-action: true                 # command/console 动作执行后是否自动关闭菜单
  notify-open-failed: true              # 打开失败时是否提示玩家

  # 全局物品绑定：任意匹配的物品右键即可打开指定菜单
  item-binds:
    - menu: example
      material: NETHER_STAR
      name-contains: "服务器菜单"
      action: RIGHT_CLICK

# 提示消息，支持 {player} 与 & 颜色代码
messages:
  prefix: "&3◆ &6ArcartXSuite &7| &r"
  no-permission: "&c你没有权限执行此操作。"
  player-only: "&c该命令只能由玩家执行。"
  menu-not-found: "&c未找到菜单: &f{menu}"
  menu-open-failed: "&c无法打开菜单 &f{menu}&c: &7{reason}"
  menu-open-success: "&a已打开菜单 &f{menu}"
  button-unavailable: "&c该按钮当前不可用。"
  button-condition-denied: "&c条件未满足，无法使用该按钮。"
  page-empty: "&7当前页没有可用按钮。"
  reload-success: "&aMenu 模块配置已重载。"
  reload-failed: "&cMenu 模块重载失败: &7{error}"
```

保存后执行 `/axs menu reload` 即可生效。

---

## 四、常见使用场景实现代码

所有示例均保存到 `plugins/ArcartX-Suite/data/menu/menus/` 目录下的 `.yml` 文件中。

### 4.1 基础功能菜单

创建一个简单的服务器功能入口菜单，包含仓库、邮件、传送等按钮。

```yaml
# 文件：menus/server_main.yml

id: server_main
title: "&f&l服务器功能"
layout: panel
columns: 3                    # 每行 3 个按钮
buttons-per-page: 9           # 每页最多 9 个按钮

# 注册命令：玩家输入 /func 或 /function 即可打开
commands:
  - "func"
  - "function"

pages:
  - id: main
    title: "&f功能列表"
    buttons:
      warehouse:
        text: "&f仓库系统"
        order: 0
        icon:
          material: CHEST
          name: "&e仓库"
          lore:
            - "&7点击打开个人仓库"
        actions:
          - "command: warehouse open"
          - "close"

      mail:
        text: "&f邮件系统"
        order: 1
        icon:
          material: PAPER
          name: "&e邮件"
        actions:
          - "command: mail open"
          - "close"

      teleport:
        text: "&f传送菜单"
        order: 2
        icon:
          material: ENDER_PEARL
          name: "&e传送"
        actions:
          - "open: teleport_menu"      # 打开另一个菜单

      help:
        text: "&f帮助"
        order: 3
        # 不加 close，执行命令后菜单保持打开
        actions:
          - "command: help"
```

### 4.2 多级嵌套菜单

通过 `open:` 动作实现菜单跳转，同一文件内可用 `---` 分隔多个菜单定义。

```yaml
# 文件：menus/teleport_menu.yml

id: teleport_menu
title: "&f&l传送菜单"
layout: panel
columns: 2
buttons-per-page: 6

pages:
  - id: main
    title: "&f选择传送点"
    buttons:
      spawn:
        text: "&f出生点"
        order: 0
        icon:
          material: COMPASS
          name: "&a传送到出生点"
        actions:
          - "command: spawn"
          - "close"

      home:
        text: "&f我的家"
        order: 1
        icon:
          material: RED_BED
          name: "&a回到家"
        actions:
          - "command: home"
          - "close"

      back:
        text: "&7« 返回主菜单"
        order: 2
        actions:
          - "open: server_main"        # 返回上一级菜单
          - "close"
```

### 4.3 带图标的菜单

Menu 支持多种图标来源：直接物品、外部物品库、ArcartX 自定义贴图、完整 JSON。

```yaml
# 文件：menus/icon_demo.yml

id: icon_demo
title: "&f&l图标展示"
layout: panel
columns: 3
buttons-per-page: 9

pages:
  - id: main
    title: "&f多种图标来源"
    buttons:
      # 1. 基础 Minecraft 物品 + CustomModelData
      basic_icon:
        text: "&f基础图标"
        order: 0
        icon:
          material: DIAMOND_SWORD
          name: "&b钻石剑"
          lore:
            - "&7这是基础物品图标"
          custom-model-data: 10001        # 配合资源包使用
          glow: true                      # 附魔光效
        actions:
          - "close"

      # 2. 玩家头颅（base64 或 URL）
      skull_icon:
        text: "&f自定义头颅"
        order: 1
        icon:
          material: PLAYER_HEAD
          name: "&e玩家头颅"
          # 支持直接填写 http(s) URL，或 base64 编码的皮肤值
          skull-texture: "https://textures.minecraft.net/texture/xxx"
        actions:
          - "close"

      # 3. 外部物品库：NeigeItems
      neige_icon:
        text: "&fNeigeItems"
        order: 2
        icon:
          source: neigeitems              # 或简写 neige
          id: "herb_bundle"               # 替换为你的真实物品 ID
          name: "&aNeigeItems 物品"
        actions:
          - "close"

      # 4. 外部物品库：MythicMobs
      mythic_icon:
        text: "&fMythicMobs"
        order: 3
        icon:
          source: mythicmobs              # 或简写 mythic
          id: "MagicSword"
          name: "&cMythicMobs 物品"
        actions:
          - "close"

      # 5. 外部物品库：MMOItems（需 type + id）
      mmo_icon:
        text: "&fMMOItems"
        order: 4
        icon:
          source: mmoitems                # 或简写 mmo
          mmo-type: SWORD
          mmo-id: STEEL_SWORD
          name: "&6MMOItems 物品"
        actions:
          - "close"

      # 6. ArcartX 自定义贴图（无需资源包 CustomModelData）
      custom_texture:
        text: "&fAX 贴图"
        order: 5
        icon:
          material: PAPER
          name: "&b自定义贴图"
          # 将 resource/item_icon/ 下的贴图写入 icon NBT
          texture: "item"
          # 或用于原版 GUI 的 url NBT
          # texture-url: "menu/demo_icon.png"
        actions:
          - "close"
```

### 4.4 禁用状态菜单

通过 `requirements` 与 `condition` 组合，实现「无权限隐藏」与「条件不满足变灰提示」两种禁用状态。

```yaml
# 文件：menus/vip_features.yml

id: vip_features
title: "&f&lVIP 专属"
layout: panel
columns: 2

# 打开菜单需要 VIP 权限
permission: "arcartxsuite.vip.menu"

pages:
  - id: main
    title: "&fVIP 功能"
    buttons:
      # 普通玩家看不到此按钮（硬性隐藏）
      admin_only:
        text: "&c管理员礼包"
        order: 0
        permission: "arcartxsuite.admin.gift"    # 无权限直接隐藏
        icon:
          material: NETHER_STAR
        actions:
          - "command: give %player_name% diamond 64"
          - "close"

      # 等级不足时仍然显示，但点击提示条件未满足
      level_reward:
        text: "&f等级奖励"
        order: 1
        condition:
          - "%player_level% >= 20"
        deny-message: "&c需要达到 20 级才能领取"
        icon:
          material: EXPERIENCE_BOTTLE
        actions:
          - "console: exp give %player_name% 100"
          - "message: "&a获得 100 点经验"
          - "close"

      # 周末双倍奖励：不满足时按钮完全不显示
      weekend_double:
        text: "&f周末双倍"
        order: 2
        requirements:
          - type: aria
            script: "var d = new Date().getDay(); return d == 0 || d == 6;"
        icon:
          material: SUNFLOWER
        actions:
          - "message: "&a领取周末双倍奖励"
          - "close"
```

### 4.5 ESC 暂停界面菜单

将菜单绑定到 ESC 键，替代原版暂停界面。

```yaml
# 文件：menus/esc_main.yml

id: esc_main
title: "&f&l菜单"
layout: esc                      # 必须使用 esc 布局
columns: 2
buttons-per-page: 64
match-esc: true                  # 拦截 ESC 键

pages:
  - id: main
    title: "&f快捷入口"
    buttons:
      shop:
        text: "&f商城"
        order: 0
        actions:
          - "command: market shop"
          - "close"

      warehouse:
        text: "&f仓库"
        order: 1
        actions:
          - "command: warehouse open"
          - "close"

      back_to_game:
        text: "&f返回游戏"
        order: 2
        actions:
          - "close"

# 页脚按钮始终显示在 ESC 菜单底部
footer-buttons:
  options:
    text: "&f游戏选项"
    client-action: "options"     # 打开原版设置界面
  quit:
    text: "&f退出游戏"
    client-action: "quit"        # 退出到主菜单
```

::: warning 重要
`layout: esc` 与 `match-esc: true` 仅对 `esc-menu-id` 指定的菜单生效。若修改了默认 ESC 菜单，请同步更新 `ArcartXMenu.yml` → `client.esc-menu-id`。
:::

---

## 五、事件处理机制

Menu 模块的事件分为两类：服务端生命周期事件与客户端回包事件。

### 5.1 打开/关闭事件

通过 `open-actions` 与 `close-actions` 实现菜单生命周期监听：

```yaml
id: event_demo
title: "&f&l事件演示"
layout: panel

open-actions:
  - "message: "&a你打开了事件演示菜单"
  - "sound: ENTITY_EXPERIENCE_ORB_PICKUP|1|1"
  - "console: log %player_name% 打开 event_demo"

close-actions:
  - "message: "&7菜单已关闭"
  - "sound: UI_BUTTON_CLICK|0.5|1.2"

pages:
  - id: main
    buttons:
      close_btn:
        text: "&c关闭"
        actions:
          - "close"
```

### 5.2 点击事件

玩家点击按钮时，服务端按顺序执行 `actions` 列表。每个动作返回是否「需要关闭菜单」，最终由 `close-on-action` 与动作类型共同决定是否关闭。

```yaml
pages:
  - id: main
    buttons:
      reward:
        text: "&f领取奖励"
        actions:
          # 1. 播放点击音效
          - "sound: UI_BUTTON_CLICK|1|1"
          # 2. 以控制台身份发奖励
          - "console: eco give %player_name% 100"
          # 3. 提示玩家
          - "message: "&a获得 &e100 &a金币"
          # 4. 关闭菜单（close 动作）
          - "close"
```

### 5.3 页面切换事件

`page:` 动作用于同菜单内的页面切换，支持页面 ID 或翻页关键字：

```yaml
pages:
  - id: main
    buttons:
      next:
        text: "&f下一页 »"
        actions:
          - "page: page2"          # 切换到 id 为 page2 的页面

      prev:
        text: "&7« 上一页"
        actions:
          - "page: prev"           # 支持 prev/previous/-/< 与 next/+/>

  - id: page2
    title: "&f第二页"
    buttons:
      back:
        text: "&7« 返回"
        actions:
          - "page: main"
```

### 5.4 客户端回包事件

ArcartX 客户端通过 `Packet.send('AXS_MENU', ...)` 向服务端发送以下动作：

| 客户端动作 | 服务端处理 | 说明 |
| --- | --- | --- |
| `refresh` | `refreshMenu(player)` | 客户端触发刷新，服务端重新推送当前页数据 |
| `esc_open` | `openEscMenu(player)` | 玩家按 ESC 键，打开默认 ESC 菜单 |
| `close` | `closeMenu(player)` | 关闭当前菜单，执行 `close-actions` |
| `page` | `changePage(player, token)` | 翻页或切换页面 |
| `click` | `handleButtonClick(player, buttonId, false)` | 点击普通按钮 |
| `footer` | `handleButtonClick(player, buttonId, true)` | 点击页脚按钮 |

一般服主无需手动处理这些回包，但可以通过 UI 文件（`ui/menu_panel.yml`）自定义客户端触发时机。

---

## 六、样式自定义方法

### 6.1 UI 文件位置

Menu 模块渲染依赖 ArcartX UI 文件：

- 居中面板：`plugins/ArcartX-Suite/data/menu/ui/menu_panel.yml`
- ESC 菜单：`plugins/ArcartX-Suite/data/menu/ui/menu_esc.yml`

修改 UI 后，将 `ArcartXMenu.yml` 中的 `client.overwrite-ui-files` 设为 `true` 并执行 `/axs menu reload`，或手动删除 UI 文件让插件重新导出。

### 6.2 常用样式调整

`menu_panel.yml` 中的关键节点：

```yaml
# 主面板尺寸与位置
主面板:
  type: Canvas
  attribute:
    point: ~middle_center          # 居中
    width: 1032                    # 面板宽度
    height: 760                    # 面板高度

# 背景颜色与圆角
外壳:
  type: Texture
  attribute:
    normal: ~95,95,95,220          # RGBA 背景色
    shape: ~round_rect             # 圆角矩形
    radius: 24                     # 圆角半径

# 标题文字
标题:
  type: Text
  attribute:
    x: 36
    y: 28
    texts: "var.title"             # 绑定菜单 title 字段
    fontSize: 64
```

### 6.3 按钮样式

按钮外观由 `self.entry['enabled']` 决定，可在 UI 文件中修改普通态与禁用态颜色：

```yaml
菜单按钮:
  type: Texture
  attribute:
    normal: "self.entry != null && self.entry['enabled'] ? '95,95,95,255' : '55,55,55,200'"
    hover: "self.entry != null && self.entry['enabled'] ? '255,255,255,255' : '55,55,55,200'"
```

### 6.4 自定义贴图

推荐通过按钮 `icon.texture` 或 `icon.texture-url` 实现无资源包贴图：

```yaml
icon:
  material: PAPER
  texture: "vip/badge"              # 对应 ArcartX resource/item_icon/vip/badge.png
```

更多 ArcartX 贴图机制见 [自定义物品贴图](/modules/menu#arcartx-自定义贴图-nbt) 与 `ui/menu_panel.yml` 中的 `seticon` 逻辑。

---

## 七、性能优化建议

### 7.1 控制每页按钮数量

- 每页按钮不宜超过 `columns × 5`（如 4 列 × 5 行 = 20 个）。
- 大量按钮请拆分为多个 `pages`，避免单次发包数据过大。
- 使用 `buttons-per-page` 限制每页渲染数量。

### 7.2 合理使用条件系统

- `requirements`（可见条件）会在每次渲染时计算，复杂 Aria/JS 脚本会显著增加服务端开销。
- 对于变化不频繁的条件，建议配合 EventPacket 或模块自身缓存机制减少实时计算。
- 避免在单个按钮上叠加过多 `condition` 行，所有条件为 `AND` 关系，每行都会执行。

### 7.3 图标解析优化

- 外部物品库图标（MythicMobs/NeigeItems/MMOItems）生成成本高于基础 `material` 图标。
- 若按钮不依赖动态玩家上下文，尽量使用 `material` + `custom-model-data` 或 `texture`。
- 避免在 `lore` 中使用大量 `%placeholder%` 实时变量，除非确实需要。

### 7.4 点击冷却与关闭策略

- `settings.click-cooldown-ms` 默认 300ms，可根据需要调大（如 500ms）防止连点。
- `settings.close-on-action` 为 `true` 时，`command`/`console` 动作后自动关闭菜单，减少并发打开风险。

### 7.5 命令与物品绑定

- `command-regex` 正则表达式在启动时预编译，避免使用过于复杂的回溯正则。
- `item-binds` 全局绑定会在玩家点击物品时遍历匹配，全局绑定数量建议控制在 20 条以内。

### 7.6 开发阶段建议

- 开启 `debug.enabled: true` 观察菜单加载与回包日志。
- 修改 UI 时使用 `overwrite-ui-files: true`，正式服建议关闭以防误覆盖。
- 使用 `/menu list` 检查菜单是否正确加载，使用 `/menu open <id>` 快速测试。

---

## 八、跨模块调用示例

其他模块或外部插件可通过 Capability 调用 Menu 模块打开菜单：

```java
import xuanmo.arcartxsuite.api.capability.MenuOpenable;
import xuanmo.arcartxsuite.api.context.ModuleContext;

// 获取 Menu 能力
MenuOpenable menu = moduleContext.getCapability(MenuOpenable.class);
if (menu != null) {
    boolean opened = menu.openMenu(player, "server_main");
    if (opened) {
        // 打开成功后的逻辑
    }
}
```

Capability 的获取方式取决于你的模块上下文，详见 [Capability 教程](/guide/developer/capability-guide)。

---

## 九、故障排查速查表

| 现象 | 可能原因 | 解决方案 |
| --- | --- | --- |
| 菜单打不开 | `modules.menu.enabled` 未开启 / 菜单 ID 错误 | 检查 `config.yml` 与 `/menu list` |
| 命令绑定无效 | 正则语法错误 / 权限不足 | 查看控制台警告，检查 `permission` |
| 物品绑定无效 | material 名错误 / 未触发对应点击 | 核对物品名与 `action` 值 |
| 图标不显示 | `material` 为空或无效 / `json` 短路 | 检查 `icon` 配置，避免 `json` 与其他字段混用 |
| 按钮灰色点不了 | `condition` 未通过 / Aria 未部署 | 查看 `deny-message`，无 Blink 时改用 JS 条件 |
| 按钮不显示 | `requirements` 或 `permission` 未通过 | 使用 `debug.enabled: true` 查看条件评估 |
| ESC 无菜单 | `esc-menu-id` 配置错误 / 无 `match-esc: true` 菜单 | 检查 `ArcartXMenu.yml` 与 ESC 菜单定义 |
| 重载后 UI 未更新 | `overwrite-ui-files: false` | 设为 `true` 或手动删除 UI 文件后重载 |

---

> 本指南示例均基于 ArcartX-Suite Menu 模块源码实现，若后续版本更新导致字段变化，请以 [modules/menu.md](/modules/menu) 最新参考文档为准。
