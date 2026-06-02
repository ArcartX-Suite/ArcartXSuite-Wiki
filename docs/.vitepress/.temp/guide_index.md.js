import { ssrRenderAttrs } from "vue/server-renderer";
import { useSSRContext } from "vue";
import { _ as _export_sfc } from "./plugin-vue_export-helper.1tPrXgE0.js";
const __pageData = JSON.parse('{"title":"快速开始","description":"","frontmatter":{},"headers":[],"relativePath":"guide/index.md","filePath":"guide/index.md","lastUpdated":1778948966000}');
const _sfc_main = { name: "guide/index.md" };
function _sfc_ssrRender(_ctx, _push, _parent, _attrs, $props, $setup, $data, $options) {
  _push(`<div${ssrRenderAttrs(_attrs)}><h1 id="快速开始" tabindex="-1">快速开始 <a class="header-anchor" href="#快速开始" aria-label="Permalink to &quot;快速开始&quot;">​</a></h1><p>新接触 AXS 的服主请按下面顺序读完本章 — 整个过程大约 <strong>15 分钟</strong>就能让一个模块跑起来。</p><h2 id="路线图" tabindex="-1">路线图 <a class="header-anchor" href="#路线图" aria-label="Permalink to &quot;路线图&quot;">​</a></h2><ol><li><a href="./installation.html">安装</a> — 把 jar 丢进 <code>plugins/</code>，确认依赖、Java、MC 版本无误。</li><li><a href="./module-passwords.html">模块授权门控</a> — 理解 <code>modules.&lt;module&gt;.enabled</code> 与 license.yml 授权。</li><li><a href="./first-run.html">第一次启用流程</a> — 推荐的&quot;先开 1 个最小模块 → 全开&quot;流程。</li><li><a href="./commands.html">命令速查</a> — 管理命令（<code>/AXS</code>）与玩家命令一表打尽。</li><li><a href="./placeholders.html">PlaceholderAPI 速查</a> — 各模块的 PAPI 前缀与典型字段。</li></ol><h2 id="一句话总览" tabindex="-1">一句话总览 <a class="header-anchor" href="#一句话总览" aria-label="Permalink to &quot;一句话总览&quot;">​</a></h2><div class="language- vp-adaptive-theme"><button title="Copy Code" class="copy"></button><span class="lang"></span><pre class="shiki shiki-themes github-light github-dark vp-code" tabindex="0"><code><span class="line"><span>ArcartX 客户端 MOD  ──────  网络包 ──────  AXS 服务端 jar</span></span>
<span class="line"><span>     ↑ 渲染 UI / HUD                           ↑ 业务逻辑 / 数据库 / 桥接</span></span>
<span class="line"><span>     │                                          │</span></span>
<span class="line"><span>     └────── plugins/ArcartXSuite/ ─────────────┘</span></span>
<span class="line"><span>              ├── config.yml          总开关 + 模块授权</span></span>
<span class="line"><span>              ├── ArcartX*.yml        各模块配置</span></span>
<span class="line"><span>              ├── ui/                 ArcartX UI 模板</span></span>
<span class="line"><span>              ├── chat/, mail/, ...   模块子资源</span></span>
<span class="line"><span>              └── *.db                持久化数据</span></span></code></pre></div><div class="tip custom-block"><p class="custom-block-title">顺序很重要</p><p><strong>先安装 ArcartX 客户端 MOD，再装 AXS 服务端</strong>。AXS 在 <code>plugin.yml</code> 中 <code>depend: ArcartX</code>，服务端缺少 ArcartX 时 AXS <strong>不会启动</strong>。</p></div></div>`);
}
const _sfc_setup = _sfc_main.setup;
_sfc_main.setup = (props, ctx) => {
  const ssrContext = useSSRContext();
  (ssrContext.modules || (ssrContext.modules = /* @__PURE__ */ new Set())).add("guide/index.md");
  return _sfc_setup ? _sfc_setup(props, ctx) : void 0;
};
const index = /* @__PURE__ */ _export_sfc(_sfc_main, [["ssrRender", _sfc_ssrRender]]);
export {
  __pageData,
  index as default
};
