// scripts/generate-stats.mjs
// 从 content/docs 自动统计套件级数据，输出 lib/stats.json，供首页 hero 引用。
// 文档变化后运行 `npm run stats` 或直接 `npm run build`（prebuild 自动执行）。
import { existsSync, readdirSync, readFileSync, writeFileSync } from 'node:fs';
import { join } from 'node:path';
import { fileURLToPath } from 'node:url';

// 仓库根目录：以脚本自身位置定位，与启动时的工作目录无关
const repoRoot = fileURLToPath(new URL('../', import.meta.url));
const contentDir = join(repoRoot, 'content', 'docs');
// 非模块目录：不参与"模块内置"口径统计
const NON_MODULE_DIRS = new Set(['api', 'architecture', 'developer', 'guide']);
// UI 组件：功能特性名本身即为 UI/面板/HUD/菜单/窗口/列表/栏 的条目
const UI_KEYWORDS = /面板|界面|HUD|菜单|窗口|UI|列表|栏/;

function moduleDirs() {
  return readdirSync(contentDir, { withFileTypes: true })
    .filter((d) => d.isDirectory() && !NON_MODULE_DIRS.has(d.name))
    .map((d) => d.name)
    .sort();
}

/** 统计概览页"功能特性"表里，特性名本身即为 UI 组件的条目（按模块去重） */
function countUiComponents(dir) {
  const indexFile = join(contentDir, dir, 'index.mdx');
  if (!existsSync(indexFile)) return 0;
  const lines = readFileSync(indexFile, 'utf-8').split(/\r?\n/);
  let inFeatureTable = false;
  const names = new Set();
  for (const raw of lines) {
    const s = raw.trim();
    if (!s.startsWith('|')) {
      inFeatureTable = false;
      continue;
    }
    if (!inFeatureTable && /说明|特性/.test(s)) {
      inFeatureTable = true;
      continue;
    }
    if (!inFeatureTable) continue;
    const cells = s.replace(/^\||\|$/g, '').split('|').map((c) => c.trim());
    if (cells.length < 2) continue;
    const name = cells[0];
    // 排除表头/分隔行/API 方法名（含括号，如 isBound(UUID)）
    if (!name || name.includes('---') || name.includes('(')) continue;
    if (UI_KEYWORDS.test(name)) names.add(name);
  }
  return names.size;
}

/** 统计 commands.mdx 中列出的命令条目数（表格行或列表项） */
function countCommands(dir) {
  const file = join(contentDir, dir, 'commands.mdx');
  if (!existsSync(file)) return 0;
  let n = 0;
  for (const raw of readFileSync(file, 'utf-8').split(/\r?\n/)) {
    const s = raw.trim();
    if (s.startsWith('|')) {
      const cells = s.replace(/^\||\|$/g, '').split('|').map((c) => c.trim());
      if (cells[0] && /^`?\/[\w\-]/.test(cells[0])) n += 1;
    } else if (/^[-*]\s*`\/[\w\-]/.test(s)) {
      n += 1;
    }
  }
  return n;
}

/** 统计模块文档页数（不含 index 目录页） */
function countDocPages(dir) {
  let n = 0;
  for (const f of readdirSync(join(contentDir, dir), { withFileTypes: true })) {
    if (f.isFile() && /\.mdx$/.test(f.name) && f.name !== 'index.mdx') n += 1;
  }
  return n;
}

const dirs = moduleDirs();
const stats = {
  modules: dirs.length,
  uiComponents: dirs.reduce((sum, d) => sum + countUiComponents(d), 0),
  moduleCommands: dirs.reduce((sum, d) => sum + countCommands(d), 0),
  docPages: dirs.reduce((sum, d) => sum + countDocPages(d), 0),
  generatedAt: new Date().toISOString().slice(0, 10),
};

const out = join(repoRoot, 'lib', 'stats.json');
writeFileSync(out, JSON.stringify(stats, null, 2) + '\n', 'utf-8');
console.log(`[stats] modules=${stats.modules} uiComponents=${stats.uiComponents} moduleCommands=${stats.moduleCommands} docPages=${stats.docPages} -> lib/stats.json`);
