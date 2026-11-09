import { readdirSync, readFileSync, existsSync } from 'node:fs';
import { join, posix, relative } from 'node:path';
import { fileURLToPath } from 'node:url';
import GithubSlugger from 'github-slugger';

const root = fileURLToPath(new URL('../', import.meta.url));
const docs = join(root, 'content/docs');
const walk = (dir) => readdirSync(dir, { withFileTypes: true }).flatMap((entry) => entry.isDirectory() ? walk(join(dir, entry.name)) : [join(dir, entry.name)]);
const files = walk(docs);
const pages = new Map(files.filter((file) => file.endsWith('.mdx')).map((file) => [relative(docs, file).replaceAll('\\', '/').replace(/(?:\/index)?\.mdx$/, '').replace(/^index$/, ''), file]));
const errors = [];
const withoutExamples = (text) => text.replace(/^```[^\n]*\n[\s\S]*?^```/gm, '');
const headingIds = new Map([...pages].map(([key, file]) => {
  const slugger = new GithubSlugger();
  const text = withoutExamples(readFileSync(file, 'utf8').replace(/\r\n/g, '\n'));
  return [key, new Set([...text.matchAll(/^#{1,6} (.+)$/gm)].map(([, heading]) => slugger.slug(heading.replace(/[`*]/g, ''))))];
}));
const privateDetails = /PacketGuard|NativeBridge|AbstractAXSModule|ModuleContext|setLookup|@ApiStability\.Internal|module-signature-public-keys|response-signature-public-keys|require-native-verify|HMAC|Ed25519|PBKDF2|BCrypt|Argon2|防重放|令牌校验|写操作令牌|加密实现|混淆流程|内部架构/iu;

for (const file of files) {
  const name = relative(root, file).replaceAll('\\', '/');
  const text = readFileSync(file, 'utf8');
  if (file.endsWith('.mdx')) {
    if (!/^---\r?\n[\s\S]*?title:[\s\S]*?description:[\s\S]*?\r?\n---/.test(text)) errors.push(`${name}: 缺少标题或描述`);
    const title = text.match(/^title:\s*(.+)$/m)?.[1];
    if (title && !/[\u3400-\u9fff]/u.test(title)) errors.push(`${name}: 页面标题须包含中文名称`);
    if (privateDetails.test(text)) errors.push(`${name}: 包含非公开实现信息`);
    if (/\bxc\b/iu.test(text)) errors.push(`${name}: 示例与说明须使用通用接入方称呼`);
    if (/(?:本次交付|本次实现|本次重构)(?:只|仅|已|未|完成|核对|验证|包括)|未执行(?:游戏内、?|崩服或?|性能|功能|自动化|端到端)+(?:测试)?/.test(text)) errors.push(`${name}: 开发、交付和测试执行记录不属于公开使用文档`);
    if (/已废弃|已弃用|已移除字段|兼容旧字段|旧格式兼容|旧版兼容配置|旧模式|向后兼容字段/.test(text)) errors.push(`${name}: 请只保留当前推荐配置，移除废弃字段与兼容写法介绍`);
    if ((text.match(/^```/gm)?.length ?? 0) % 2) errors.push(`${name}: 代码块未闭合`);
    // Ignore links inside examples, but validate every actual documentation link.
    const prose = withoutExamples(text.replace(/\r\n/g, '\n'));
    for (const [, example] of text.matchAll(/^\x60\x60\x60yaml\s*\r?\n([\s\S]*?)^\x60\x60\x60/gm)) {
      if (/^\s*\/[a-z]+(?:[ \t]|$)/im.test(example)) errors.push(`${name}: 游戏命令请使用 text 代码块`);
    }
    const body = text.replace(/^---\r?\n[\s\S]*?\r?\n---\s*/, '').trim();
    if (!body) errors.push(`${name}: 页面正文为空，不能只有标题和描述`);
    const lines = prose.split('\n');
    const columns = (line) => line.trim().replace(/^\||\|$/g, '').split(/(?<!\\)\|/).length;
    for (let i = 0; i < lines.length; i++) {
      if (!/^\|[ :|\-]+\|\s*$/.test(lines[i])) continue;
      if (!/^\|/.test(lines[i - 1] ?? '') || /^\|[ :|\-]+\|\s*$/.test(lines[i - 1])) {
        errors.push(`${name}: 表格分隔行缺少表头`);
        continue;
      }
      const count = columns(lines[i]);
      const headerCells = lines[i - 1].trim().replace(/^\||\|$/g, '').split(/(?<!\\)\|/).map((cell) => cell.trim());
      const explanationColumn = headerCells.findIndex((cell) => /^(说明|用途|功能|效果|中文含义|中文用途|中文作用与写法)$/.test(cell));
      const actionReference = /^(动作|动作类型|动作关键字|动作表单)$/.test(headerCells[0]);
      if (columns(lines[i - 1]) !== count) errors.push(`${name}: 表头与分隔行列数不一致`);
      for (let j = i + 1; j < lines.length && /^\|/.test(lines[j]); j++) {
        if (columns(lines[j]) !== count) errors.push(`${name}: 表格数据列数不一致，请检查未转义的竖线`);
        const cells = lines[j].trim().replace(/^\||\|$/g, '').split(/(?<!\\)\|/).map((cell) => cell.trim());
        if (explanationColumn > 0 && /`/.test(cells[0]) && !/[\u3400-\u9fff]/u.test(cells[explanationColumn] ?? '')) {
          errors.push(`${name}:${j + 1}: 技术标识缺少中文解释`);
        }
        if (actionReference && !/\x60[^\x60]+\x60|\[[^\]]*(?:示例|片段)[^\]]*\]\([^)]+\)/u.test(cells.slice(1).join(' '))) {
          errors.push(`${name}:${j + 1}: 动作需要实际写法、具体示例或完整示例链接`);
        }
      }
    }
    for (const [, heading] of prose.matchAll(/^#{2,6} (.+)$/gm)) {
      if (!/[\u3400-\u9fff]/u.test(heading)) errors.push(`${name}: 标题缺少中文用途说明 ${heading}`);
      if (/^[A-Za-z0-9_. -]+\s*节$/.test(heading)) errors.push(`${name}: 配置标题需要说明中文用途 ${heading}`);
    }
    if (/(^\|[^\n]+\n\|[ :|\-]+\n)(?!\|)/m.test(prose)) errors.push(`${name}: 表格只有表头，缺少实际条目`);
    if (/^- \[[^\]]+\]\([^)]+\)\s*$/m.test(prose)) errors.push(`${name}: 相关链接缺少用途说明`);
    if (/^description:.*(?:的使用说明与配置示例|可按需查阅的通用功能配置说明)/m.test(text)) errors.push(`${name}: 页面简介需要说明具体用途`);
    if (/见方法声明|见返回类型/.test(prose)) errors.push(`${name}: 参数或返回值缺少实际说明`);
    for (const [, , href] of prose.matchAll(/\[([^\]]+)\]\(([^\s)]+)\)/g)) {
      if (/^(?:https?:|mailto:)/.test(href)) continue;
      const target = href.split('#')[0].replace(/\.mdx$/, '').replace(/\/$/, '');
      const currentKey = relative(docs, file).replaceAll('\\', '/').replace(/(?:\/index)?\.mdx$/, '').replace(/^index$/, '');
      const key = !target ? currentKey : target.startsWith('/docs') ? target.replace(/^\/docs\/?/, '') : posix.normalize(posix.join(relative(docs, file).replaceAll('\\', '/').replace(/[^/]+$/, ''), target));
      if (!pages.has(key)) errors.push(`${name}: 无效链接 ${href}`);
      const hash = href.split('#')[1];
      if (hash && pages.has(key) && !headingIds.get(key)?.has(decodeURIComponent(hash))) errors.push(`${name}: 目录锚点不存在 ${href}`);
    }
  } else if (file.endsWith('meta.json')) {
    let meta;
    try { meta = JSON.parse(text); } catch { errors.push(`${name}: JSON 格式错误`); continue; }
    for (const item of meta.pages ?? []) {
      if (item.startsWith('---') || item === '...') continue;
      const target = join(file, '..', item);
      if (!existsSync(`${target}.mdx`) && !existsSync(join(target, 'meta.json'))) errors.push(`${name}: 导航目标不存在 ${item}`);
    }
  }
}
const moduleFolders = readdirSync(docs, { withFileTypes: true }).filter((entry) => entry.isDirectory() && !['guide', 'api'].includes(entry.name) && existsSync(join(docs, entry.name, 'meta.json')));
const moduleIds = new Set(moduleFolders.map(({ name }) => name));
const directorySource = readFileSync(join(root, 'lib/modules.ts'), 'utf8');
const moduleNames = new Map([...directorySource.matchAll(/slug: '([^']+)', name: '[^']+', displayName: '([^']+)'/g)]
  .map(([, id, name]) => [id, name]));
const docsHome = readFileSync(join(docs, 'index.mdx'), 'utf8');
const logGuide = readFileSync(join(docs, 'guide/config-management.mdx'), 'utf8');
const requiredModuleSections = [
  ['第一次使用', '部署流程'],
  ['检查是否配置成功', '配置验证'],
  ['继续配置', '相关文档'],
];
for (const { name } of moduleFolders) {
  if (!logGuide.includes(`（${name}）`)) errors.push(`${name}: 日志说明缺少该模块的排查入口`);
  for (const page of ['index', 'configuration', 'commands']) if (!existsSync(join(docs, name, `${page}.mdx`))) errors.push(`${name}: 缺少 ${page} 页面`);
  if (!existsSync(join(docs, name, 'index.mdx'))) continue;
  const index = readFileSync(join(docs, name, 'index.mdx'), 'utf8');
  const title = index.match(/^title: "([^"]+)"/m)?.[1];
  const navTitle = JSON.parse(readFileSync(join(docs, name, 'meta.json'), 'utf8')).title;
  if (!title || title !== navTitle || title !== moduleNames.get(name) || !docsHome.includes(`[${title}](/docs/${name})`)) {
    errors.push(`${name}: 中文名称须在模块目录、侧栏、模块首页和文档入口一致`);
  }
  for (const headings of requiredModuleSections) {
    if (!headings.some((heading) => index.includes(`## ${heading}\n`) || index.includes(`## ${heading}\r\n`))) {
      errors.push(`${name}: 缺少 ${headings.join(' / ')}`);
    }
  }
}
if (moduleFolders.length !== 29) errors.push(`模块数应为 29，实际为 ${moduleFolders.length}`);
const apiIndex = readFileSync(join(docs, 'api/index.mdx'), 'utf8');
const apiGuidePages = new Set(['index', 'quick-start', 'examples', 'data-types']);
const apiNavigation = JSON.parse(readFileSync(join(docs, 'api/meta.json'), 'utf8')).pages;
const apiSections = ['获取接口', '方法声明', '参数与返回值', '调用注意事项'];
for (const [key, file] of pages) {
  if (!key.startsWith('api/')) continue;
  const slug = key.slice(4);
  if (apiGuidePages.has(slug)) continue;
  const prose = withoutExamples(readFileSync(file, 'utf8').replace(/\r\n/g, '\n'));
  const provider = prose.match(/^\| 提供模块 \| (.+) \|$/m)?.[1];
  const linked = provider?.match(/^\[([a-z0-9]+)\]\(\/docs\/([a-z0-9]+)(?:\/[^)]*)?\)$/);
  if (provider !== 'Suite 宿主' && provider !== '调用方实现'
    && (!linked || linked[1] !== linked[2] || !moduleIds.has(linked[1]))) {
    errors.push(`${key}: 提供模块须显示实际模块 ID`);
  }
  if (!prose.includes('| 完整类型 |') || apiSections.some((section) => !prose.includes(`## ${section}\n`))) {
    errors.push(`${key}: API 参考缺少完整类型或标准章节`);
  }
  if (!apiNavigation.includes(slug) || !apiIndex.includes(`(/docs/${key})`)) {
    errors.push(`${key}: API 参考须同时出现在侧栏和接口索引`);
  }
}
if (errors.length) { console.error(errors.join('\n')); process.exitCode = 1; }
else console.log(`文档检查通过：${pages.size} 页，${moduleFolders.length} 个模块；日志覆盖、中文标题、链接说明、表格结构、锚点与公开内容边界有效。`);
