@echo off
chcp 65001 >nul
:: ═══════════════════════════════════════════════════════════════════
:: SnowLuma 自动下载 & 启动脚本 (Windows)
::
:: 功能：
::   1. 检测本地是否已安装 SnowLuma
::   2. 未安装时自动从 GitHub Release 下载最新版
::   3. 启动 SnowLuma（WebUI 默认端口 5099）
::
:: 用法：将此脚本放到服务端根目录，与 server.jar 同级
::       首次运行会自动下载到 snowluma/ 子目录
:: ═══════════════════════════════════════════════════════════════════

set SNOWLUMA_DIR=snowluma
set SNOWLUMA_REPO=SnowLuma/SnowLuma
set LAUNCHER=%SNOWLUMA_DIR%\launcher.bat

echo.
echo  ╔══════════════════════════════════════════════╗
echo  ║   SnowLuma - OneBot 11 实现端 (QQBot 依赖)  ║
echo  ╚══════════════════════════════════════════════╝
echo.

:: ─── 检测 Node.js ───
where node >nul 2>nul
if %ERRORLEVEL% NEQ 0 (
    echo [错误] 未检测到 Node.js，SnowLuma 需要 Node.js 18+ 运行环境
    echo        下载地址: https://nodejs.org/
    echo.
    pause
    exit /b 1
)

:: ─── 检测是否已安装 ───
if exist "%LAUNCHER%" (
    echo [SnowLuma] 已安装，正在启动...
    echo.
    goto :start
)

echo [SnowLuma] 未检测到本地安装，正在从 GitHub 下载最新版...
echo.

:: ─── 获取最新 Release 下载地址 ───
set API_URL=https://api.github.com/repos/%SNOWLUMA_REPO%/releases/latest

:: 使用 PowerShell 获取最新 release 中的 win zip 下载链接
for /f "delims=" %%U in ('powershell -NoProfile -Command ^
    "$r = Invoke-RestMethod -Uri '%API_URL%' -Headers @{'User-Agent'='AXS'}; ^
     $a = $r.assets | Where-Object { $_.name -match 'win' -and $_.name -match '\.zip$' } | Select-Object -First 1; ^
     if ($a) { $a.browser_download_url } ^
     else { $a2 = $r.assets | Where-Object { $_.name -match '\.zip$' } | Select-Object -First 1; if($a2){$a2.browser_download_url}else{''} }"') do set DL_URL=%%U

if "%DL_URL%"=="" (
    echo [错误] 未找到适用的 Release 资源，请手动下载:
    echo        https://github.com/%SNOWLUMA_REPO%/releases
    pause
    exit /b 1
)

echo [SnowLuma] 下载地址: %DL_URL%
echo.

:: ─── 下载 ───
set ZIP_FILE=snowluma-latest.zip
powershell -NoProfile -Command "Invoke-WebRequest -Uri '%DL_URL%' -OutFile '%ZIP_FILE%' -UseBasicParsing"

if not exist "%ZIP_FILE%" (
    echo [错误] 下载失败，请检查网络或手动下载
    pause
    exit /b 1
)

:: ─── 解压 ───
echo [SnowLuma] 正在解压...
if not exist "%SNOWLUMA_DIR%" mkdir "%SNOWLUMA_DIR%"
powershell -NoProfile -Command "Expand-Archive -Path '%ZIP_FILE%' -DestinationPath '%SNOWLUMA_DIR%' -Force"

:: 如果解压后多了一层目录，把内容提上来
for /f "delims=" %%D in ('dir /b /ad "%SNOWLUMA_DIR%"') do (
    if exist "%SNOWLUMA_DIR%\%%D\launcher.bat" (
        echo [SnowLuma] 整理目录结构...
        powershell -NoProfile -Command "Get-ChildItem '%SNOWLUMA_DIR%\%%D' | Move-Item -Destination '%SNOWLUMA_DIR%' -Force"
        rmdir "%SNOWLUMA_DIR%\%%D" 2>nul
    )
)

del "%ZIP_FILE%" 2>nul

:: ─── 检查解压结果 ───
if not exist "%LAUNCHER%" (
    echo [警告] 未找到 launcher.bat，尝试直接用 Node 启动...
    echo        如果启动失败，请手动检查 %SNOWLUMA_DIR% 目录
    echo.
)

echo [SnowLuma] 安装完成！
echo.

:start
echo ─────────────────────────────────────────
echo  WebUI 地址: http://127.0.0.1:5099
echo  首次登录密码将显示在下方日志中
echo  WS 适配器配置完成后，AXS QQBot 会自动连接
echo ─────────────────────────────────────────
echo.

cd /d "%SNOWLUMA_DIR%"
if exist "launcher.bat" (
    call launcher.bat
) else if exist "index.js" (
    node index.js
) else if exist "app.js" (
    node app.js
) else (
    echo [错误] 未找到启动入口，请检查 %SNOWLUMA_DIR% 目录
    pause
    exit /b 1
)

pause
