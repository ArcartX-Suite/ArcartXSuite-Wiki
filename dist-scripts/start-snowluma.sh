#!/bin/bash
# ═══════════════════════════════════════════════════════════════════
# SnowLuma 自动下载 & 启动脚本 (Linux)
#
# 功能：
#   1. 检测本地是否已安装 SnowLuma
#   2. 未安装时自动从 GitHub Release 下载最新版
#   3. 启动 SnowLuma（WebUI 默认端口 5099）
#
# 用法：chmod +x start-snowluma.sh && ./start-snowluma.sh
# ═══════════════════════════════════════════════════════════════════

SNOWLUMA_DIR="snowluma"
SNOWLUMA_REPO="SnowLuma/SnowLuma"

echo ""
echo "  ╔══════════════════════════════════════════════╗"
echo "  ║   SnowLuma - OneBot 11 实现端 (QQBot 依赖)  ║"
echo "  ╚══════════════════════════════════════════════╝"
echo ""

# ─── 检测 Node.js ───
if ! command -v node &> /dev/null; then
    echo "[错误] 未检测到 Node.js，SnowLuma 需要 Node.js 18+ 运行环境"
    echo "       安装: curl -fsSL https://deb.nodesource.com/setup_20.x | sudo -E bash - && sudo apt-get install -y nodejs"
    echo "       或:   https://nodejs.org/"
    exit 1
fi

NODE_VER=$(node -v | sed 's/v//' | cut -d. -f1)
if [ "$NODE_VER" -lt 18 ] 2>/dev/null; then
    echo "[警告] Node.js 版本过低 ($(node -v))，建议升级到 18+"
fi

# ─── 检测是否已安装 ───
if [ -f "$SNOWLUMA_DIR/launcher.sh" ] || [ -f "$SNOWLUMA_DIR/index.js" ]; then
    echo "[SnowLuma] 已安装，正在启动..."
    echo ""
else
    echo "[SnowLuma] 未检测到本地安装，正在从 GitHub 下载最新版..."
    echo ""

    # ─── 检测下载工具 ───
    if command -v curl &> /dev/null; then
        DL_CMD="curl -fsSL"
        DL_OUT="-o"
    elif command -v wget &> /dev/null; then
        DL_CMD="wget -q"
        DL_OUT="-O"
    else
        echo "[错误] 需要 curl 或 wget"
        exit 1
    fi

    # ─── 获取最新 Release ───
    API_URL="https://api.github.com/repos/${SNOWLUMA_REPO}/releases/latest"

    # 优先下载 linux zip，其次任意 zip
    DL_URL=$($DL_CMD "$API_URL" | grep -o '"browser_download_url":\s*"[^"]*linux[^"]*\.zip"' | head -1 | cut -d'"' -f4)
    if [ -z "$DL_URL" ]; then
        DL_URL=$($DL_CMD "$API_URL" | grep -o '"browser_download_url":\s*"[^"]*\.zip"' | head -1 | cut -d'"' -f4)
    fi

    if [ -z "$DL_URL" ]; then
        echo "[错误] 未找到适用的 Release 资源，请手动下载:"
        echo "       https://github.com/${SNOWLUMA_REPO}/releases"
        exit 1
    fi

    echo "[SnowLuma] 下载地址: $DL_URL"
    echo ""

    # ─── 下载 ───
    ZIP_FILE="snowluma-latest.zip"
    $DL_CMD "$DL_URL" $DL_OUT "$ZIP_FILE"

    if [ ! -f "$ZIP_FILE" ]; then
        echo "[错误] 下载失败，请检查网络或手动下载"
        exit 1
    fi

    # ─── 解压 ───
    echo "[SnowLuma] 正在解压..."
    mkdir -p "$SNOWLUMA_DIR"
    unzip -qo "$ZIP_FILE" -d "$SNOWLUMA_DIR"

    # 如果解压后多了一层目录，把内容提上来
    INNER=$(find "$SNOWLUMA_DIR" -mindepth 1 -maxdepth 1 -type d | head -1)
    if [ -n "$INNER" ] && [ -f "$INNER/launcher.sh" -o -f "$INNER/index.js" ]; then
        echo "[SnowLuma] 整理目录结构..."
        mv "$INNER"/* "$SNOWLUMA_DIR"/ 2>/dev/null
        mv "$INNER"/.* "$SNOWLUMA_DIR"/ 2>/dev/null
        rmdir "$INNER" 2>/dev/null
    fi

    rm -f "$ZIP_FILE"

    # 赋予执行权限
    chmod +x "$SNOWLUMA_DIR"/*.sh 2>/dev/null

    echo "[SnowLuma] 安装完成！"
    echo ""
fi

# ─── 启动 ───
echo "─────────────────────────────────────────"
echo " WebUI 地址: http://127.0.0.1:5099"
echo " 首次登录密码将显示在下方日志中"
echo " WS 适配器配置完成后，AXS QQBot 会自动连接"
echo "─────────────────────────────────────────"
echo ""

cd "$SNOWLUMA_DIR"
if [ -f "launcher.sh" ]; then
    exec ./launcher.sh
elif [ -f "index.js" ]; then
    exec node index.js
elif [ -f "app.js" ]; then
    exec node app.js
else
    echo "[错误] 未找到启动入口，请检查 $SNOWLUMA_DIR 目录"
    exit 1
fi
