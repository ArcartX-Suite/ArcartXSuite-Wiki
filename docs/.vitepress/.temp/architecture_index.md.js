import { ssrRenderAttrs } from "vue/server-renderer";
import { useSSRContext } from "vue";
import { _ as _export_sfc } from "./plugin-vue_export-helper.1tPrXgE0.js";
const __pageData = JSON.parse('{"title":"架构","description":"","frontmatter":{},"headers":[],"relativePath":"architecture/index.md","filePath":"architecture/index.md","lastUpdated":1779943952000}');
const _sfc_main = { name: "architecture/index.md" };
function _sfc_ssrRender(_ctx, _push, _parent, _attrs, $props, $setup, $data, $options) {
  _push(`<div${ssrRenderAttrs(_attrs)}><h1 id="架构" tabindex="-1">架构 <a class="header-anchor" href="#架构" aria-label="Permalink to &quot;架构&quot;">​</a></h1><p>AXS 共享<strong>同一组反射桥、同一套客户端包守卫、同一种资源加密协议、同一份数据包流向约定</strong>。</p><h2 id="一图概览" tabindex="-1">一图概览 <a class="header-anchor" href="#一图概览" aria-label="Permalink to &quot;一图概览&quot;">​</a></h2><div class="language- vp-adaptive-theme"><button title="Copy Code" class="copy"></button><span class="lang"></span><pre class="shiki shiki-themes github-light github-dark vp-code" tabindex="0"><code><span class="line"><span>┌────────────────────────────────────────────────────────────────┐</span></span>
<span class="line"><span>│                       ArcartXSuite                             │</span></span>
<span class="line"><span>│                                                                │</span></span>
<span class="line"><span>│  ┌──────────┐   ┌──────────┐   ┌──────────┐   ┌────────────┐  │</span></span>
<span class="line"><span>│  │ Bridge   │   │ Security │   │ Config   │   │ Combat /   │  │</span></span>
<span class="line"><span>│  │ (反射桥) │   │ (Guard,  │   │ (.axb +  │   │ Util       │  │</span></span>
<span class="line"><span>│  │          │   │  Pwd)    │   │  Sync)   │   │            │  │</span></span>
<span class="line"><span>│  └────┬─────┘   └────┬─────┘   └────┬─────┘   └─────┬──────┘  │</span></span>
<span class="line"><span>│       │              │              │               │          │</span></span>
<span class="line"><span>│  ┌────▼──────────────▼──────────────▼───────────────▼─────┐    │</span></span>
<span class="line"><span>│  │            21 个 Module (config / service /             │    │</span></span>
<span class="line"><span>│  │            listener / placeholder / command)            │    │</span></span>
<span class="line"><span>│  └────┬───────────────────────────────────────────────┬───┘    │</span></span>
<span class="line"><span>│       │  sendPacket(player, uiId, handler, payload)   │        │</span></span>
<span class="line"><span>└───────┼───────────────────────────────────────────────┼────────┘</span></span>
<span class="line"><span>        │                                               │</span></span>
<span class="line"><span>   ┌────▼─────┐                                    ┌────▼─────┐</span></span>
<span class="line"><span>   │ ArcartX  │ ──────── WebSocket ─────────────── │ 客户端   │</span></span>
<span class="line"><span>   │ 服务端   │  ◄ Packet.send(packetId, action) ─ │ MOD      │</span></span>
<span class="line"><span>   └──────────┘                                    └──────────┘</span></span></code></pre></div><h2 id="模块化" tabindex="-1">模块化 <a class="header-anchor" href="#模块化" aria-label="Permalink to &quot;模块化&quot;">​</a></h2><ul><li><a href="./modular.html">Modular — 宿主 + 模块 Jar 架构</a>：模块加载、重载、UI 注册、开发指南</li></ul><h2 id="四个共享层" tabindex="-1">四个共享层 <a class="header-anchor" href="#四个共享层" aria-label="Permalink to &quot;四个共享层&quot;">​</a></h2><ul><li><a href="./bridges.html">Bridge — 反射桥</a>：全部通过反射 + 类名探测访问第三方 API</li><li><a href="./security.html">Security — ClientPacketGuard + 模块授权</a>：速率限制 + 授权门控</li><li><a href="./protected-resources.html">Protected Resources — <code>.axb</code> 加密资源</a>：YAML 加密打包协议</li><li><a href="./packet-flow.html">Packet Flow — init/update/close 协议</a>：UI 数据包五段式生命周期</li></ul><h2 id="开发者-api" tabindex="-1">开发者 API <a class="header-anchor" href="#开发者-api" aria-label="Permalink to &quot;开发者 API&quot;">​</a></h2><p>1.1.0 起提供独立的 <code>axs-api</code> 模块作为第三方开发的稳定接口层，详见 <a href="/ArcartXSuite/api/">API 参考</a>。</p><ul><li><a href="/ArcartXSuite/api/module-lifecycle.html">模块生命周期 — AXSModule / AbstractAXSModule / ModuleDescriptor</a></li><li><a href="/ArcartXSuite/api/module-context.html">ModuleContext 上下文 — 桥接、事件、命令、资源导出</a></li><li><a href="/ArcartXSuite/api/bridge-api.html">桥接 API — PacketBridgeAPI / ClientBridgeAPI / ItemBridgeAPI</a></li><li><a href="/ArcartXSuite/api/events.html">事件 — ModuleLifecycleEvent</a></li><li><a href="/ArcartXSuite/api/capability.html">Capability 跨模块通信</a></li></ul><h2 id="数据库" tabindex="-1">数据库 <a class="header-anchor" href="#数据库" aria-label="Permalink to &quot;数据库&quot;">​</a></h2><p>AXS 用 <strong>HikariCP + SQLite/MySQL 共存</strong>：</p><ul><li>默认 <code>mode: sqlite</code>，文件位于 <code>plugins/ArcartXSuite/&lt;module&gt;.db</code></li><li>改 <code>mode: mysql</code> 后填连接信息即可切换</li><li>所有模块用各自独立的连接池</li></ul><p>涉及持久化的模块：<code>title</code> / <code>mail</code> / <code>chat</code> / <code>onlinerewards</code> / <code>loginview</code> / <code>map</code> / <code>warehouse</code></p></div>`);
}
const _sfc_setup = _sfc_main.setup;
_sfc_main.setup = (props, ctx) => {
  const ssrContext = useSSRContext();
  (ssrContext.modules || (ssrContext.modules = /* @__PURE__ */ new Set())).add("architecture/index.md");
  return _sfc_setup ? _sfc_setup(props, ctx) : void 0;
};
const index = /* @__PURE__ */ _export_sfc(_sfc_main, [["ssrRender", _sfc_ssrRender]]);
export {
  __pageData,
  index as default
};
