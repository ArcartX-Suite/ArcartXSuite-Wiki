---
title: ArcartX-Suite 系统架构 | ArcartX-Suite Minecraft插件架构文档
description: ArcartX-Suite 系统架构 - ArcartX-Suite Minecraft 服务器插件文档。 ArcartX-Suite 我的世界服务器插件套件。
---

# ArcartX-Suite 系统架构

## 整体架构图

```mermaid
graph TB
    %% 客户端侧
    Client[ArcartX 客户端 MOD] -->|网络包| Server
    
    %% 服务端侧
    Server[ArcartX-Suite 服务端 jar] -->|业务逻辑| Business
    Server -->|数据库| Database
    Server -->|桥接| Bridge
    
    %% 业务逻辑层
    Business[业务逻辑层]
    Business --> Module[模块系统]
    Business --> Config[配置管理]
    Business --> UI[UI渲染]
    
    %% 模块系统
    Module[模块系统] --> Core[核心模块]
    Module --> Modules[功能模块]
    
    %% 文件系统结构（作为服务端的子部分）
    subgraph "plugins/ArcartX-Suite/ 目录结构"
        ConfigYml["config.yml<br/>总开关"]
        ModuleConfigs["ArcartX*.yml<br/>各模块配置"]
        UIDir["ui/<br/>UI模板文件"]
        DataDirs["chat/, mail/, ...<br/>模块数据目录"]
        DBFiles["*.db<br/>SQLite数据库"]
    end
    
    %% 配置文件与功能的关系
    ConfigYml -.->|控制| Module
    ModuleConfigs -.->|配置| Module
    UIDirs -.->|模板| UI
    DataDirs -.->|数据| Module
    DBFiles -.->|存储| Database
    
    %% 数据库层
    Database[(数据库)] --> SQLite[SQLite]
    Database --> MySQL[MySQL]
    
    %% 桥接层
    Bridge[桥接层] --> Packet[包桥接]
    Bridge --> Client[客户端桥接]
    Bridge --> Item[物品桥接]
    
    %% UI渲染
    UI -->|渲染| HUD[HUD界面]
    UI -->|渲染| Menu[菜单界面]
    
    %% 最终输出
    HUD -->|显示| Client
    Menu -->|交互| Client
```

## 组件说明

### 1. 客户端侧
- **ArcartX 客户端 MOD**: 玩家安装的客户端模组，负责UI渲染和HUD显示

### 2. 服务端核心
- **ArcartX-Suite 服务端 jar**: 服务端插件主程序，包含核心业务逻辑
- **业务逻辑层**: 处理模块管理、配置解析、UI渲染等核心功能
- **模块系统**: 管理 26 个功能模块的加载、卸载和交互
- **配置管理**: 统一管理所有配置文件和热重载
- **UI渲染**: 生成ArcartX UI界面并发送给客户端

### 3. 文件系统结构
`plugins/ArcartX-Suite/` 目录包含：
- **config.yml**: 总开关配置
- **ArcartX*.yml**: 各模块的主配置文件
- **ui/**: ArcartX UI模板文件
- **chat/, mail/, ...**: 各模块的数据目录
- ***.db**: SQLite数据库文件

### 4. 数据存储
- **SQLite**: 默认的单机数据库
- **MySQL**: 可选的多服共享数据库

### 5. 桥接层
- **包桥接**: 与ArcartX客户端的网络通信
- **客户端桥接**: 客户端功能调用
- **物品桥接**: 物品数据处理

## 数据流向

1. **客户端 → 服务端**: 玩家操作通过网络包发送到服务端
2. **服务端处理**: 业务逻辑层处理请求，读取配置，调用模块功能
3. **数据存储**: 将数据保存到SQLite或MySQL数据库
4. **UI生成**: 根据配置和数据生成UI界面
5. **服务端 → 客户端**: 将UI数据通过网络包发送给客户端
6. **客户端渲染**: 客户端MOD接收数据并渲染界面

## 模块架构

```mermaid
graph TB
    subgraph "模块系统架构"
        Registry[模块注册表]
        Loader[模块加载器]
        Core[核心模块]
        Modules[功能模块]
        
        Registry --> Loader
        Loader --> Core
        Loader --> Modules
        
        subgraph "核心模块"
            API[ArcartX-Suite-API]
            CoreJar[ArcartX-Suite-core]
        end
        
        subgraph "功能模块 (26个)"
            Modules[功能模块]
        end
    end
```

## 配置文件层次

```mermaid
graph LR
    subgraph "配置文件层次"
        Main[config.yml<br/>主配置]
        Module[ArcartX*.yml<br/>模块配置]
        Data[data/<br/>模块数据]
        
        Main -->|控制| Module
        Module -->|生成| Data
    end
```

这个架构图清晰地展示了：
1. 客户端与服务端的分离
2. 文件系统结构作为服务端的子部分
3. 明确的数据流向和组件关系
4. 模块系统的层次结构
5. 配置文件的管理层次

