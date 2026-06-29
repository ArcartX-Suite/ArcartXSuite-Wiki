---
title: LoginView 登录界面插件 | ArcartX-Suite Minecraft
description: ArcartX-Suite LoginView 登录注册面板，ArcartX UI 渲染，支持独立模式和 AuthMe 兼容，我的世界服务器登录认证插件。
---

# LoginView 登录界面

## 功能定位

ArcartX UI 驱动的**登录/注册界面**，替代传统聊天框输入密码方式。支持两种运行模式：

- **`authme`**（默认）— 桥接已有的 AuthMe 数据库，登录/注册/改密全部走 AuthMe API
- **`standalone`** — 独立账户系统，使用 ArcartX-Suite 自带的数据库和 BCrypt hash

玩家进服后自动弹出 ArcartX UI 登录面板，未登录前**锁定移动、聊天、命令、物品操作**。

### 核心特性

- **双模式运行**：`authme` 模式无缝桥接现有 AuthMe 数据库，`standalone` 模式完全独立运行
- **可视化 UI 登录**：替代传统聊天框密码输入，ArcartX UI 面板带服务器信息、在线人数和时间显示
- **完整认证流程**：登录、注册、修改密码三合一，UI 自动判断玩家是否已注册
- **全面安全锁定**：未登录前锁定移动、聊天、命令、物品栏点击、物品丢弃、方块交互
- **命令白名单**：`allow-commands-prefix` 配置允许在未登录时执行的命令前缀
- **暴力破解防护**：密码错误计数，达到最大尝试次数后自动踢出，配合 `ClientPacketGuard` 保护
- **密码安全策略**：可配置最小/最大密码长度
- **AuthMe 数据迁移**：内置迁移工具，支持 dry-run 预览，迁移后首次登录自动用 BCrypt 重新加密
- **数据存储**：SQLite 本地文件或 MySQL 远程数据库
- **可自定义消息**：登录成功、注册完成、密码错误等所有提示文本均可配置
- **延迟弹出**：进服后可配置延迟 tick 数再弹出 UI，避免客户端未就绪
- **正版/LittleSkin 免登录**：通过 authlib-injector（LittleSkin）或 Mojang 正版认证的玩家可免密码一键进服
- **authlib-injector 管理**：由 ArcartX-Suite 本体统一管理。启动时自动检测 Agent 加载状态并输出配置指南；`/axs auth setup` 一键下载 + 启动脚本生成
- **QQ 绑定面板**：微软正版和 LittleSkin 账号可分别配置是否需要 QQ 绑定才能进服；需要绑定的玩家通过 UI 面板输入验证码完成 QQ 关联
- **账号来源 PAPI**：提供微软正版 / LittleSkin / 离线 三类账号来源占位符，可用于 TAB、聊天、EventPacket 条件和其他 PAPI 消费场景
- **EventPacket 联动**：登录成功发射 `login_success` 信号，首次注册发射 `first_register` 信号，免登录发射 `premium_bypass` 信号

## 依赖

| 类型 | 依赖 | 作用 | 缺少时表现 |
| --- | --- | --- | --- |
| 必需 | ArcartX | 登录、注册、改密 UI 和客户端回包 | 模块无法提供可视化登录面板 |
| 按模式必需 | AuthMe | `auth.mode: authme` 时接管登录/注册/改密 | 配置为 `authme` 但未安装 AuthMe 时模块不会加载 |
| 可选 | MySQL 服务 | `standalone` 模式远程账户库，或 AuthMe 迁移源 | 默认 SQLite 可用；远程库功能不可用 |
| 可选 | EventPacket 模块 | 登录成功、首次注册后的事件联动 | 不影响登录本身 |

## 启用步骤

```yaml
modules:
  loginview:
    enabled: true
```

## 关键配置（`ArcartXLoginView.yml`）

### 认证模式

```yaml
auth:
  mode: "authme"    # authme | standalone
```

| 模式 | 说明 |
| --- | --- |
| `authme` | 桥接 AuthMe API，登录/注册/改密全部走 AuthMe |
| `standalone` | 独立账户系统，使用 ArcartX-Suite 自带的 SQLite/MySQL 存储 |

### 正版/LittleSkin 免登录

启用后，通过 LittleSkin 外置登录或 Mojang 正版认证的玩家，进服后**无需输入密码**，直接点击「进入服务器」按钮即可游玩。离线玩家仍需正常注册/登录。

#### 配置项

```yaml
auth:
  premium-bypass:
    enabled: false                         # 是否启用免登录
    message: '&a身份已验证，欢迎回来。'      # 免登录成功后的提示消息
```

#### 工作原理

| 玩家类型 | 判定依据 | 进服体验 |
|---|---|---|
| 微软正版 | 玩家名在 Mojang 正版库存在（UUID v3 或 v4 均可） | 显示「进入服务器」按钮，一键进服 |
| LittleSkin 外置登录 | 玩家名不在 Mojang，UUID 为 v4（Yggdrasil 分配） | 显示「进入服务器」按钮，一键进服 |
| 离线/盗版 | UUID 为 v3 且玩家名不在 Mojang | 正常显示注册/登录界面 |

> 账号判定由 ArcartX-Suite 本体[统一账号识别服务](../api/module-context#账号识别服务)提供。服务在玩家预登录阶段异步查询 Mojang 正版库：**只要玩家名在 Mojang 存在即视为微软正版**（无论 UUID 是 v3 离线 UUID 还是 v4 在线 UUID），从而正确识别「微软正版但未关联 LittleSkin」（其 UUID 通常为 v3）的玩家；玩家名不在 Mojang 且 UUID 为 v4 的视为 LittleSkin；其余视为离线。

### PlaceholderAPI

前缀：`%axsloginview_<字段>%`

| 占位符 | 返回值 | 说明 |
| --- | --- | --- |
| `%axsloginview_account_type%` | `microsoft` / `littleskin` / `offline` | 账号来源标识，最适合写进 EventPacket `conditions` |
| `%axsloginview_account_type_display%` | 文本 | 中文显示名：`微软正版` / `LittleSkin` / `离线` |
| `%axsloginview_account_type_name%` | 文本 | 与 `account_type_display` 相同，兼容别名 |
| `%axsloginview_is_microsoft%` | `true` / `false` | 是否为微软正版账号 |
| `%axsloginview_is_littleskin%` | `true` / `false` | 是否为 LittleSkin 账号 |
| `%axsloginview_is_offline%` | `true` / `false` | 是否为离线账号 |
| `%axsloginview_is_premium%` | `true` / `false` | 是否为认证账号（微软正版或 LittleSkin） |

示例：

```text
%axsloginview_account_type%           -> microsoft
%axsloginview_account_type_display%   -> 微软正版
%axsloginview_is_littleskin%          -> false
```

在 EventPacket 中的典型写法：

```yaml
conditions:
  - "%axsloginview_account_type% == littleskin"
```

---

#### 从零配置教程

##### 前置条件

- 服务器运行在 `online-mode=false`（`server.properties` 中设置）
- 已安装 ArcartX-Suite 且 LoginView 模块已启用

##### 第一步：启用 premium-bypass

编辑 `plugins/ArcartX-Suite/data/loginview/ArcartXLoginView.yml`：

```yaml
auth:
  mode: "standalone"       # 或 "authme"
  premium-bypass:
    enabled: true          # ← 改为 true
    message: '&a身份已验证，欢迎回来。'
```

重载配置：
```
/axs loginview reload
```

##### 第二步：安装 authlib-injector（一键命令）

在游戏内或控制台执行：

```
/axs auth setup
```

该命令会自动完成：
1. 从官方源下载最新版 `authlib-injector.jar` 到 `plugins/ArcartX-Suite/`
2. 检测服务端 jar 名称（paper.jar / purpur.jar / spigot.jar 等）
3. 在服务器根目录生成启动脚本：
   - 若 `config.yml` 的 `auth.yggdrasil-source` **包含 `?mixed`**（默认）：生成 `start-mixed-auth.bat` / `start-mixed-auth.sh`（先启动本地混合代理，再启动服务器）
   - 若 `auth.yggdrasil-source` **不包含 `?mixed`**（纯 LittleSkin）：生成 `start-littleskin.bat` / `start-littleskin.sh`
4. **自动修改现有启动脚本**（如 `run.bat`），注入 `-javaagent` 参数（原脚本备份为 `.bak`）

##### 第三步：重启服务器

```
stop
```

然后选择以下任一方式启动：

**方式 A：使用生成的新脚本**

在服务器根目录找到生成的启动脚本（默认混合登录为 `start-mixed-auth.bat` / `.sh`；纯 LittleSkin 为 `start-littleskin.bat` / `.sh`），双击或执行它。

**方式 B：使用原脚本（已被自动修改）**

如果 `/axs auth setup` 检测到你的原启动脚本（如 `run.bat`）并成功修改，直接用原脚本启动即可。修改前的原文件已备份为 `run.bat.bak`。

**方式 C：手动修改启动命令**

::: danger 混合登录必须使用 start-mixed-auth 脚本
若 `config.yml` 的 `auth.yggdrasil-source` 包含 `?mixed`（默认），**不能**直接写 `-javaagent` 启动服务器。authlib-injector 的 `premain` 时序要求代理进程必须先于服务器存在，因此必须使用 `/axs auth setup` 生成的 `start-mixed-auth` 脚本（它会先启动本地混合代理，再启动服务器）。
:::

仅当使用**纯 LittleSkin**（`auth.yggdrasil-source` 不含 `?mixed`）时，才可直接在启动命令中加入 `-javaagent`：

```bat
java -javaagent:plugins/ArcartX-Suite/authlib-injector.jar=https://littleskin.cn/api/yggdrasil -Xmx4G -jar paper.jar nogui
```

##### 第四步：验证

服务器启动后，控制台应该看到：
```
[ArcartX-Suite] LoginView premium-bypass: authlib-injector 已检测到，正版/LittleSkin 免登录已启用。
```

如果看到黄色警告框提示「未检测到 authlib-injector」，说明启动命令中没有正确加载 Agent，请检查第三步。

##### 第五步：玩家体验

- **已绑定 QQ 的认证玩家**（微软正版 / LittleSkin）→ 显示「进入服务器」按钮，点击即可进服
- **未绑定 QQ、但当前账号类型要求绑定**的认证玩家 → 显示绑定提示 UI → 在 QQ 群发送 `#绑定 <玩家名>` 获取验证码 → 在 UI 中输入验证码 → 绑定成功后显示「进入服务器」
- **未绑定 QQ、但当前账号类型不要求绑定**的认证玩家（如 `microsoft-require-bind: false` 时的微软正版）→ 直接显示「进入服务器」按钮
- **离线玩家**进服 → 正常注册/登录界面

---

#### 常见问题

**Q: 如果不使用 LittleSkin，使用其他 Yggdrasil 服务器怎么办？**

修改生成的启动脚本中的 API 地址（如 `start-mixed-auth.bat` 或 `start-littleskin.bat`）：
```
-javaagent:plugins/ArcartX-Suite/authlib-injector.jar=https://你的yggdrasil地址/api/yggdrasil
```

**Q: 启用后离线玩家还能注册/登录吗？**

可以。免登录仅对识别为正版的玩家（微软正版 / LittleSkin）生效，离线玩家不受影响，仍走正常注册/登录流程。

**Q: 玩家改名后会怎样？**

LittleSkin/正版玩家的 UUID 由认证服务器分配，改名不会影响 UUID，免登录仍然有效。

**Q: 如何回退/关闭免登录？**

将配置改回 `enabled: false` 并 `/axs loginview reload` 即可。无需卸载 authlib-injector。

**Q: `/axs auth setup` 修改了我的启动脚本，如何恢复？**

原始脚本已备份为 `.bak` 文件（如 `run.bat.bak`），直接重命名回来即可。

### UI 配置

```yaml
ui:
  ui-id: ArcartX-Suite:LoginView
  packet-id: AXS_loginview
  # 默认使用第一套紧凑登录 UI；可改为 login_view_menu.yml 使用纯色块主菜单风格 UI。
  ui-file: login_view.yml
  register-ui-on-enable: true
  overwrite-ui-files: false
  open-delay-ticks: 20
  close-on-login: true
```

### 安全配置

```yaml
security:
  min-password-length: 6
  max-password-length: 64
  max-attempts: 5              # 最大尝试次数
  kick-on-max-attempts: true   # 达到最大次数后踢出
  lock-movement: true          # 未登录时锁定移动
  lock-chat: true              # 未登录时锁定聊天
  lock-commands: true          # 未登录时锁定命令
  allow-commands-prefix: "login,register,l,reg,ArcartX-Suite"  # 例外命令前缀
  rehash-migrated-password-on-login: true  # AuthMe 迁移后首次登录时用 ArcartX-Suite hash 重新加密
```

### 存储配置

```yaml
storage:
  mode: "sqlite"               # sqlite | mysql
  sqlite:
    file: "loginview.db"
  mysql:
    host: "127.0.0.1"
    port: 3306
    database: "minecraft"
    username: "root"
    password: ""
  table-prefix: "AXS_loginview_"
```

### 消息自定义

```yaml
messages:
  title-login: "登录服务器"
  title-register: "注册账号"
  login-success: "&a登录成功。"
  register-success: "&a注册完成，已自动登录。"
  change-success: "&a密码已修改。"
  password-mismatch: "&c两次输入的密码不一致。"
  password-too-short: "&c密码太短。"
  password-too-long: "&c密码太长。"
  wrong-password: "&c密码错误。"
  already-registered: "&c你已经注册过账号。"
  not-registered: "&e你还没有注册，请先设置密码。"
  locked: "&e请先完成登录。"
  kicked: "&c密码错误次数过多。"
```

## 命令

> 权限：`arcartxsuite.admin`

| 命令 | 说明 |
| --- | --- |
| `/axs loginview status` | 查看登录模块、模式、UI 和账户库状态 |
| `/axs loginview reload` | 重载登录视图配置、UI 和账户服务 |
| `/axs loginview open <玩家>` | 为在线玩家打开登录视图界面，一般用于调试 |
| `/axs loginview migrate-authme [dry-run]` | 从 AuthMe 迁移密码哈希。加 `dry-run` 只预览不执行 |
| `/axs loginview migration-commands` | 显示停用 AuthMe 后的安全操作步骤 |
| `/axs auth setup` | 自动下载 authlib-injector 并生成启动脚本（本体命令） |

### QQ 绑定面板

当 `qq-binding.enabled: true` 且 `premium-bypass.enabled: true` 时：

| 玩家状态 | 进服后 UI | 操作流程 |
|---|---|---|
| 已绑定 QQ | 显示「进入服务器」按钮 | 点击直接进服 |
| 未绑定 QQ，但当前账号类型**不要求绑定** | 显示「进入服务器」按钮 | 点击直接进服 |
| 未绑定 QQ，且当前账号类型**要求绑定** | 显示绑定提示 + 验证码输入框 | ① QQ 群 `#绑定 <玩家名>` → ② UI 输入 6 位验证码 → ③ 验证通过后显示「进入服务器」按钮 |
| 离线账号 | 正常注册/登录界面 | 按常规流程注册/登录 |

配置（`ArcartXLoginView.yml`）：

```yaml
qq-binding:
  enabled: true
  # 微软正版账号是否需要绑定 QQ 才能进入游戏
  microsoft-require-bind: false
  # LittleSkin 账号是否需要绑定 QQ 才能进入游戏
  littleskin-require-bind: true
  # 未绑定 QQ 的正版/LittleSkin 玩家在登录面板看到的提示
  bind-prompt: '&e你尚未绑定 QQ
&7请在 QQ 群发送: #绑定 {name}
&7获取验证码后在下方输入'
```

## AuthMe 迁移

如果你从 AuthMe 迁移到 LoginView 的 `standalone` 模式：

### 1. 配置迁移源

```yaml
authme-migration:
  source:
    jdbc-url: "jdbc:sqlite:plugins/AuthMe/AuthMe.db"
    username: ""
    password: ""
    table: "authme"
  columns:
    name: "username"
    real-name: "realname"
    password: "password"
    salt: "salt"
  imported-hash-algorithm: "AUTHME_BCRYPT"
  batch-size: 200
```

### 2. 先干跑确认

```
/axs loginview migrate-authme dry-run
```

### 3. 正式迁移

```
/axs loginview migrate-authme
```

迁移后：
- AuthMe 的密码 hash 原样复制，**不会破解明文**
- 玩家首次用旧密码登录后，如果 `rehash-migrated-password-on-login: true`，会自动用 ArcartX-Suite 的 BCrypt 重新加密

## UI / Packet 契约

- UI ID：`ArcartX-Suite:LoginView`
- Packet ID：`AXS_loginview`

### 服务端 → 客户端

| handler | payload |
| --- | --- |
| `init` | `{type, title, mode, registered, premiumBypass, playerName, serverName, online, maxPlayers, address, time, message}` |
| `result` | `{success, message}` |
| `close` | `{message}` |

### 客户端 → 服务端

| action | data |
| --- | --- |
| `login` | `[password]` |
| `register` | `[password, confirmPassword]` |
| `change_password` | `[oldPassword, newPassword, confirmPassword]` |
| `bypass_enter` | `[]` — 正版/LittleSkin 免登录玩家点击「进入服务器」|
| `refresh` | `[]` |

## 安全特性

- 未登录时**锁定**：移动、聊天、命令、物品栏点击、物品丢弃、方块交互
- **无敌保护**：登录面板打开期间未认证玩家自动开启 `invulnerable`，防止被攻击或杀死；登录成功后自动解除
- 允许的命令前缀通过 `allow-commands-prefix` 白名单配置
- 密码错误计数，达到 `max-attempts` 后自动踢出
- `ClientPacketGuard` 同样保护 LoginView 回包，防止暴力破解

## EventPacket 联动

LoginView 在以下时机自动向 EventPacket 发射信号：

| 信号名 | 触发时机 | 携带变量 |
| --- | --- | --- |
| `login_success` | 玩家登录成功 | `auth_mode`, `account_type`, `account_type_display`, `account_premium` |
| `first_register` | 玩家首次注册完成 | `auth_mode`, `account_type`, `account_type_display`, `account_premium` |
| `premium_bypass` | 正版/LittleSkin 玩家免登录进服 | `auth_mode`, `account_type`, `account_type_display`, `account_premium` |

可在 `ArcartXEventPacket.yml` 中配置对应规则实现欢迎动画、新手任务引导等联动效果。

示例：

```yaml
welcome_official_player:
  enabled: true
  trigger: command-signal
  signal: "premium_bypass"
  conditions:
    - "%axsloginview_account_type% == microsoft"
  actions:
    - type: subtitle.play
      group-id: "welcome_cinematic"
```

