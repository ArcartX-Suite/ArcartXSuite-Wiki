# LoginView 登录界面

## 功能定位

ArcartX UI 驱动的**登录/注册界面**，替代传统聊天框输入密码方式。支持两种运行模式：

- **`authme`**（默认）— 桥接已有的 AuthMe 数据库，登录/注册/改密全部走 AuthMe API
- **`standalone`** — 独立账户系统，使用 AXS 自带的数据库和 BCrypt hash

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
- **authlib-injector 一键配置**：内置自动下载 + 启动脚本生成，`/axs loginview setup-authlib` 一条命令完成 LittleSkin 接入；启动时自动检测 Agent 加载状态并输出配置指南
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
| `standalone` | 独立账户系统，使用 AXS 自带的 SQLite/MySQL 存储 |

### 正版/LittleSkin 免登录

```yaml
auth:
  premium-bypass:
    enabled: false
    message: '&a身份已验证，欢迎回来。'
```

启用后，通过以下方式认证的玩家无需输入密码，直接显示「进入服务器」按钮：

- **authlib-injector + LittleSkin** — 外置登录的玩家
- **Mojang 正版** — 已购买正版的玩家

**检测原理**：正版/LittleSkin 认证后玩家的 UUID 为 version 4（随机 UUID），离线模式玩家的 UUID 为 version 3（基于名称计算）。

> ℹ️ 服务器需在离线模式下运行（`online-mode=false`），通过 authlib-injector 接入 LittleSkin 或其他第三方认证服务。正版玩家的 UUID 由认证服务器下发，自然为 version 4。

#### 快速配置 authlib-injector

如果服务器尚未配置 authlib-injector，可使用内置命令一键完成：

```
/axs loginview setup-authlib
```

该命令会：
1. 从 LittleSkin 自动下载最新版 `authlib-injector.jar` 到插件目录
2. 自动检测服务端 jar 名称（paper.jar / purpur.jar 等）
3. 在服务器根目录生成 `start-littleskin.bat` 和 `start-littleskin.sh` 启动脚本
4. 使用生成的脚本重启服务器即可生效

> ⚠️ authlib-injector 是 JVM Agent，必须在服务器启动时通过 `-javaagent` 参数加载，无法在运行时动态注入。执行 setup-authlib 后需用新脚本重启服务器。

**运行时检测**：当 `premium-bypass.enabled: true` 时，LoginView 启动时会自动检测 authlib-injector 是否已加载。若未加载，会在控制台输出详细的警告和配置指南。

### UI 配置

```yaml
ui:
  ui-id: "AXS:LoginView"
  packet-id: "AXS_loginview"
  register-ui-on-enable: true
  overwrite-ui-files: false
  open-delay-ticks: 20        # 进服后延迟多少 tick 弹出
  close-on-login: true         # 登录成功后自动关闭 UI
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
  allow-commands-prefix: "login,register,l,reg,AXS"  # 例外命令前缀
  rehash-migrated-password-on-login: true  # AuthMe 迁移后首次登录时用 AXS hash 重新加密
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
| `/axs loginview setup-authlib` | 自动下载 authlib-injector 并生成启动脚本 |

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
- 玩家首次用旧密码登录后，如果 `rehash-migrated-password-on-login: true`，会自动用 AXS 的 BCrypt 重新加密

## UI / Packet 契约

- UI ID：`AXS:LoginView`
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
- 允许的命令前缀通过 `allow-commands-prefix` 白名单配置
- 密码错误计数，达到 `max-attempts` 后自动踢出
- `ClientPacketGuard` 同样保护 LoginView 回包，防止暴力破解

## EventPacket 联动

LoginView 在以下时机自动向 EventPacket 发射信号：

| 信号名 | 触发时机 | 携带变量 |
| --- | --- | --- |
| `login_success` | 玩家登录成功 | `auth_mode` |
| `first_register` | 玩家首次注册完成 | `auth_mode` |
| `premium_bypass` | 正版/LittleSkin 玩家免登录进服 | `auth_mode` |

可在 `ArcartXEventPacket.yml` 中配置对应规则实现欢迎动画、新手任务引导等联动效果。
