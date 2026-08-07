# 统一存储架构

> 自 1.4.0 起，ArcartXSuite 引入宿主统一数据源管理器（`StorageManager`），集中管控所有模块的数据库连接池生命周期。

## 背景

在 1.4.0 之前，每个模块各自创建 HikariCP 连接池、各自调用 `initialize()`/`shutdown()`。由于没有集中管控，18 个模块中有 11 个存在生命周期 bug：

| 问题类型 | 涉及模块 | 症状 |
|---|---|---|
| 缺 `initialize()` | lottery、battlepass | 连接池未初始化，玩家进服报「数据源不可用」 |
| 缺 `shutdown()` | mail、chat、qqbot、loginview、eventpacket、essentials、warehouse、extrabackpack、regions | 连接池泄漏，重启模块时旧连接池不关闭 |

1.4.0 通过宿主统一管控连接池生命周期，彻底消除此类问题。

## 架构设计

### 双模式

| 模式 | 触发条件 | 连接池归属 | 说明 |
|---|---|---|---|
| **共享模式**（推荐） | 模块未配置独立 MySQL（`storage.mode` 为 sqlite 或未填） | 本体 `StorageManager` | 模块复用本体连接池，无需自行管理生命周期 |
| **自建模式**（向后兼容） | 模块配置了 `storage.mode: mysql` | 模块自己 | 模块自建 HikariCP，旧 MySQL 数据库零改动 |

### MySQL 模式

所有模块共享同一个 HikariCP 连接池，连同一个数据库，各模块用 `table-prefix` 隔离表。

- 连接数从 N×poolSize 降为 1 个池
- 配置统一在本体 `config.yml` 的 `storage` 节
- 模块 config.yml 的 `storage` 节可删除（除非需要覆盖）

### SQLite 模式

各模块仍使用各自的 `<moduleId>.db` 文件（如 `lottery.db`、`battlepass.db`），本体按模块名分配独立连接（maximumPoolSize=1）。

- 数据不合并，旧 `.db` 文件零迁移
- 各模块的 SQLite 文件路径不变

## 本体配置

本体 `config.yml` 新增全局 `storage` 节：

```yaml
# ========== 统一数据源（本体管控，1.4.0+）==========
storage:
  # 存储模式：sqlite（单服，默认）/ mysql（跨服群组共享同一数据库）
  mode: "sqlite"
  # MySQL 连接配置（mode=mysql 时生效）
  mysql:
    host: "127.0.0.1"
    port: 3306
    database: "arcartxsuite"
    username: "root"
    password: ""
  # MySQL 连接池大小（sqlite 固定为 1，此项无效）
  pool-size: 8
```

## 模块级覆盖

如果模块在自己的 `ArcartX<Module>.yml` 中填写了 `storage.mode: mysql` 等字段，模块会优先使用自己的配置自建连接池（走旧路径），不影响旧 MySQL 数据库。

```yaml
# data/lottery/config.yml
storage:
  mode: "mysql"
  host: "127.0.0.1"
  database: "arcartxsuite"
  table-prefix: "lottery_"
```

不填则自动使用本体全局配置。

## 旧用户迁移指南

### MySQL 用户

**如果各模块原来连的是同一个库**：直接把连接信息填到本体 `config.yml` 的 `storage` 节即可，模块配置里的 `storage` 节可删除。表前缀不变，数据零改动。

**如果各模块原来连的是不同的库**：保持模块配置里的 `storage` 节不变（走自建模式），或手动把各库的表迁到统一库。

### SQLite 用户

**无需任何操作**。各模块 `.db` 文件路径不变，数据不动。唯一的变化是连接池生命周期由本体统一管理（不再出现遗漏 `initialize()`/`shutdown()` 的问题）。

## API 层

### StorageManager 接口

```java
public interface StorageManager {
    DataSource getDataSource();                    // 本体共享数据源
    StorageDescriptor getDescriptor();             // 全局存储描述符
    boolean isAvailable();                         // 数据源是否可用
    Connection getConnection() throws SQLException; // 获取连接
    DataSource getModuleDataSource(String moduleId, String sqliteFileName) throws SQLException; // 模块独立 SQLite 数据源
}
```

通过 `ModuleContext.storageManager()` 获取。

### AbstractModuleRepository 双模式构造

```java
// 共享模式（推荐）
public JdbcMyRepository(StorageManager sm, String moduleId,
                        String tablePrefix, String sqliteFileName, Logger logger) {
    super("AXS-MyModule", sm, moduleId, tablePrefix, sqliteFileName, logger);
}

// 自建模式（向后兼容）
public JdbcMyRepository(File dataFolder, StorageConfig cfg, Logger logger) {
    super("AXS-MyModule", dataFolder, cfg.toDescriptor(), logger);
}
```

### hasOverride() 判断

模块的 storage config record 提供 `hasOverride()` 方法：

```java
public boolean hasOverride() {
    return "mysql".equalsIgnoreCase(mode);  // mode=mysql → 自建模式
}
```

## 模块集成示例

```java
@Override
protected void startService() throws Exception {
    JdbcMyRepository repo;
    if (configuration.storage().hasOverride()) {
        // 自建模式：模块自建连接池（向后兼容旧 MySQL 配置）
        repo = new JdbcMyRepository(dataFolder, configuration.storage(), logger);
    } else {
        // 共享模式：复用本体数据源
        repo = new JdbcMyRepository(storageManager, "mymodule",
            configuration.storage().tablePrefix(),
            configuration.storage().sqliteFileName(), logger);
    }
    repo.initialize();
    repository = repo;

    service = new MyService(repository, ...);
    service.start();
}

@Override
protected void stopService() {
    if (service != null) { service.shutdown(); service = null; }
    if (repository != null) { repository.shutdown(); repository = null; }
}
```

## 已改造模块（18 个）

| 模块 | 共享模式 | 自建模式 |
|---|---|---|---|
| lottery | ✅ | ✅ |
| battlepass | ✅ | ✅ |
| market | ✅ | ✅ |
| mail | ✅ | ✅ |
| chat | ✅ | ✅ |
| warehouse | ✅ | ✅ |
| fishing | ✅ | ✅ |
| onlinerewards | ✅ | ✅ |
| title | ✅ | ✅ |
| map | ✅ | ✅ |
| afkreward | ✅ | ✅ |
| eventpacket | ✅ | ✅ |
| essentials | ✅ | ✅ |
| extrabackpack | ✅ | ✅ |
| regions | ✅ | ✅ |
| qqbot | ✅ | ✅ |
| loginview | ✅ | ✅ |
| entitytracker | ✅ | ✅ |

## 相关文件

- `axs-api/.../storage/StorageManager.java` — 宿主数据源管理器接口
- `axs-api/.../storage/AbstractModuleRepository.java` — 存储层基类（双模式）
- `src/main/.../storage/StorageManagerImpl.java` — 宿主实现
- `src/main/resources/config.yml` — 全局 storage 配置节
- `.devin/rules/storage-layer-guide.md` — 存储层开发规范
