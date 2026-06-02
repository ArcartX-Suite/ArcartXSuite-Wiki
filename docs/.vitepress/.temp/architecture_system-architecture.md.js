import { ssrRenderAttrs, ssrRenderStyle } from "vue/server-renderer";
import { useSSRContext } from "vue";
import { _ as _export_sfc } from "./plugin-vue_export-helper.1tPrXgE0.js";
const __pageData = JSON.parse('{"title":"ArcartXSuite 系统架构","description":"","frontmatter":{},"headers":[],"relativePath":"architecture/system-architecture.md","filePath":"architecture/system-architecture.md","lastUpdated":1779947711000}');
const _sfc_main = { name: "architecture/system-architecture.md" };
function _sfc_ssrRender(_ctx, _push, _parent, _attrs, $props, $setup, $data, $options) {
  _push(`<div${ssrRenderAttrs(_attrs)}><h1 id="arcartxsuite-系统架构" tabindex="-1">ArcartXSuite 系统架构 <a class="header-anchor" href="#arcartxsuite-系统架构" aria-label="Permalink to &quot;ArcartXSuite 系统架构&quot;">​</a></h1><h2 id="整体架构图" tabindex="-1">整体架构图 <a class="header-anchor" href="#整体架构图" aria-label="Permalink to &quot;整体架构图&quot;">​</a></h2><div class="language-mermaid vp-adaptive-theme"><button title="Copy Code" class="copy"></button><span class="lang">mermaid</span><pre class="shiki shiki-themes github-light github-dark vp-code" tabindex="0"><code><span class="line"><span style="${ssrRenderStyle({ "--shiki-light": "#24292E", "--shiki-dark": "#E1E4E8" })}">graph TB</span></span>
<span class="line"><span style="${ssrRenderStyle({ "--shiki-light": "#24292E", "--shiki-dark": "#E1E4E8" })}">    %% 客户端侧</span></span>
<span class="line"><span style="${ssrRenderStyle({ "--shiki-light": "#24292E", "--shiki-dark": "#E1E4E8" })}">    Client[ArcartX 客户端 MOD] --&gt;|网络包| Server</span></span>
<span class="line"><span style="${ssrRenderStyle({ "--shiki-light": "#24292E", "--shiki-dark": "#E1E4E8" })}">    </span></span>
<span class="line"><span style="${ssrRenderStyle({ "--shiki-light": "#24292E", "--shiki-dark": "#E1E4E8" })}">    %% 服务端侧</span></span>
<span class="line"><span style="${ssrRenderStyle({ "--shiki-light": "#24292E", "--shiki-dark": "#E1E4E8" })}">    Server[AXS 服务端 jar] --&gt;|业务逻辑| Business</span></span>
<span class="line"><span style="${ssrRenderStyle({ "--shiki-light": "#24292E", "--shiki-dark": "#E1E4E8" })}">    Server --&gt;|数据库| Database</span></span>
<span class="line"><span style="${ssrRenderStyle({ "--shiki-light": "#24292E", "--shiki-dark": "#E1E4E8" })}">    Server --&gt;|桥接| Bridge</span></span>
<span class="line"><span style="${ssrRenderStyle({ "--shiki-light": "#24292E", "--shiki-dark": "#E1E4E8" })}">    </span></span>
<span class="line"><span style="${ssrRenderStyle({ "--shiki-light": "#24292E", "--shiki-dark": "#E1E4E8" })}">    %% 业务逻辑层</span></span>
<span class="line"><span style="${ssrRenderStyle({ "--shiki-light": "#24292E", "--shiki-dark": "#E1E4E8" })}">    Business[业务逻辑层]</span></span>
<span class="line"><span style="${ssrRenderStyle({ "--shiki-light": "#24292E", "--shiki-dark": "#E1E4E8" })}">    Business --&gt; Module[模块系统]</span></span>
<span class="line"><span style="${ssrRenderStyle({ "--shiki-light": "#24292E", "--shiki-dark": "#E1E4E8" })}">    Business --&gt; Config[配置管理]</span></span>
<span class="line"><span style="${ssrRenderStyle({ "--shiki-light": "#24292E", "--shiki-dark": "#E1E4E8" })}">    Business --&gt; UI[UI渲染]</span></span>
<span class="line"><span style="${ssrRenderStyle({ "--shiki-light": "#24292E", "--shiki-dark": "#E1E4E8" })}">    </span></span>
<span class="line"><span style="${ssrRenderStyle({ "--shiki-light": "#24292E", "--shiki-dark": "#E1E4E8" })}">    %% 模块系统</span></span>
<span class="line"><span style="${ssrRenderStyle({ "--shiki-light": "#24292E", "--shiki-dark": "#E1E4E8" })}">    Module[模块系统] --&gt; Core[核心模块]</span></span>
<span class="line"><span style="${ssrRenderStyle({ "--shiki-light": "#24292E", "--shiki-dark": "#E1E4E8" })}">    Module --&gt; Paid[付费模块]</span></span>
<span class="line"><span style="${ssrRenderStyle({ "--shiki-light": "#24292E", "--shiki-dark": "#E1E4E8" })}">    Module --&gt; Free[免费模块]</span></span>
<span class="line"><span style="${ssrRenderStyle({ "--shiki-light": "#24292E", "--shiki-dark": "#E1E4E8" })}">    </span></span>
<span class="line"><span style="${ssrRenderStyle({ "--shiki-light": "#24292E", "--shiki-dark": "#E1E4E8" })}">    %% 文件系统结构（作为服务端的子部分）</span></span>
<span class="line"><span style="${ssrRenderStyle({ "--shiki-light": "#24292E", "--shiki-dark": "#E1E4E8" })}">    subgraph &quot;plugins/ArcartXSuite/ 目录结构&quot;</span></span>
<span class="line"><span style="${ssrRenderStyle({ "--shiki-light": "#24292E", "--shiki-dark": "#E1E4E8" })}">        ConfigYml[&quot;config.yml&lt;br/&gt;总开关 + 模块授权&quot;]</span></span>
<span class="line"><span style="${ssrRenderStyle({ "--shiki-light": "#24292E", "--shiki-dark": "#E1E4E8" })}">        ModuleConfigs[&quot;ArcartX*.yml&lt;br/&gt;各模块配置&quot;]</span></span>
<span class="line"><span style="${ssrRenderStyle({ "--shiki-light": "#24292E", "--shiki-dark": "#E1E4E8" })}">        UIDir[&quot;ui/&lt;br/&gt;UI模板文件&quot;]</span></span>
<span class="line"><span style="${ssrRenderStyle({ "--shiki-light": "#24292E", "--shiki-dark": "#E1E4E8" })}">        DataDirs[&quot;chat/, mail/, ...&lt;br/&gt;模块数据目录&quot;]</span></span>
<span class="line"><span style="${ssrRenderStyle({ "--shiki-light": "#24292E", "--shiki-dark": "#E1E4E8" })}">        DBFiles[&quot;*.db&lt;br/&gt;SQLite数据库&quot;]</span></span>
<span class="line"><span style="${ssrRenderStyle({ "--shiki-light": "#24292E", "--shiki-dark": "#E1E4E8" })}">    end</span></span>
<span class="line"><span style="${ssrRenderStyle({ "--shiki-light": "#24292E", "--shiki-dark": "#E1E4E8" })}">    </span></span>
<span class="line"><span style="${ssrRenderStyle({ "--shiki-light": "#24292E", "--shiki-dark": "#E1E4E8" })}">    %% 配置文件与功能的关系</span></span>
<span class="line"><span style="${ssrRenderStyle({ "--shiki-light": "#24292E", "--shiki-dark": "#E1E4E8" })}">    ConfigYml -.-&gt;|控制| Module</span></span>
<span class="line"><span style="${ssrRenderStyle({ "--shiki-light": "#24292E", "--shiki-dark": "#E1E4E8" })}">    ModuleConfigs -.-&gt;|配置| Module</span></span>
<span class="line"><span style="${ssrRenderStyle({ "--shiki-light": "#24292E", "--shiki-dark": "#E1E4E8" })}">    UIDirs -.-&gt;|模板| UI</span></span>
<span class="line"><span style="${ssrRenderStyle({ "--shiki-light": "#24292E", "--shiki-dark": "#E1E4E8" })}">    DataDirs -.-&gt;|数据| Module</span></span>
<span class="line"><span style="${ssrRenderStyle({ "--shiki-light": "#24292E", "--shiki-dark": "#E1E4E8" })}">    DBFiles -.-&gt;|存储| Database</span></span>
<span class="line"><span style="${ssrRenderStyle({ "--shiki-light": "#24292E", "--shiki-dark": "#E1E4E8" })}">    </span></span>
<span class="line"><span style="${ssrRenderStyle({ "--shiki-light": "#24292E", "--shiki-dark": "#E1E4E8" })}">    %% 数据库层</span></span>
<span class="line"><span style="${ssrRenderStyle({ "--shiki-light": "#24292E", "--shiki-dark": "#E1E4E8" })}">    Database[(数据库)] --&gt; SQLite[SQLite]</span></span>
<span class="line"><span style="${ssrRenderStyle({ "--shiki-light": "#24292E", "--shiki-dark": "#E1E4E8" })}">    Database --&gt; MySQL[MySQL]</span></span>
<span class="line"><span style="${ssrRenderStyle({ "--shiki-light": "#24292E", "--shiki-dark": "#E1E4E8" })}">    </span></span>
<span class="line"><span style="${ssrRenderStyle({ "--shiki-light": "#24292E", "--shiki-dark": "#E1E4E8" })}">    %% 桥接层</span></span>
<span class="line"><span style="${ssrRenderStyle({ "--shiki-light": "#24292E", "--shiki-dark": "#E1E4E8" })}">    Bridge[桥接层] --&gt; Packet[包桥接]</span></span>
<span class="line"><span style="${ssrRenderStyle({ "--shiki-light": "#24292E", "--shiki-dark": "#E1E4E8" })}">    Bridge --&gt; Client[客户端桥接]</span></span>
<span class="line"><span style="${ssrRenderStyle({ "--shiki-light": "#24292E", "--shiki-dark": "#E1E4E8" })}">    Bridge --&gt; Item[物品桥接]</span></span>
<span class="line"><span style="${ssrRenderStyle({ "--shiki-light": "#24292E", "--shiki-dark": "#E1E4E8" })}">    </span></span>
<span class="line"><span style="${ssrRenderStyle({ "--shiki-light": "#24292E", "--shiki-dark": "#E1E4E8" })}">    %% UI渲染</span></span>
<span class="line"><span style="${ssrRenderStyle({ "--shiki-light": "#24292E", "--shiki-dark": "#E1E4E8" })}">    UI --&gt;|渲染| HUD[HUD界面]</span></span>
<span class="line"><span style="${ssrRenderStyle({ "--shiki-light": "#24292E", "--shiki-dark": "#E1E4E8" })}">    UI --&gt;|渲染| Menu[菜单界面]</span></span>
<span class="line"><span style="${ssrRenderStyle({ "--shiki-light": "#24292E", "--shiki-dark": "#E1E4E8" })}">    </span></span>
<span class="line"><span style="${ssrRenderStyle({ "--shiki-light": "#24292E", "--shiki-dark": "#E1E4E8" })}">    %% 最终输出</span></span>
<span class="line"><span style="${ssrRenderStyle({ "--shiki-light": "#24292E", "--shiki-dark": "#E1E4E8" })}">    HUD --&gt;|显示| Client</span></span>
<span class="line"><span style="${ssrRenderStyle({ "--shiki-light": "#24292E", "--shiki-dark": "#E1E4E8" })}">    Menu --&gt;|交互| Client</span></span></code></pre></div><h2 id="组件说明" tabindex="-1">组件说明 <a class="header-anchor" href="#组件说明" aria-label="Permalink to &quot;组件说明&quot;">​</a></h2><h3 id="_1-客户端侧" tabindex="-1">1. 客户端侧 <a class="header-anchor" href="#_1-客户端侧" aria-label="Permalink to &quot;1. 客户端侧&quot;">​</a></h3><ul><li><strong>ArcartX 客户端 MOD</strong>: 玩家安装的客户端模组，负责UI渲染和HUD显示</li></ul><h3 id="_2-服务端核心" tabindex="-1">2. 服务端核心 <a class="header-anchor" href="#_2-服务端核心" aria-label="Permalink to &quot;2. 服务端核心&quot;">​</a></h3><ul><li><strong>AXS 服务端 jar</strong>: 服务端插件主程序，包含核心业务逻辑</li><li><strong>业务逻辑层</strong>: 处理模块管理、配置解析、UI渲染等核心功能</li><li><strong>模块系统</strong>: 管理 21 个功能模块的加载、卸载和交互</li><li><strong>配置管理</strong>: 统一管理所有配置文件和热重载</li><li><strong>UI渲染</strong>: 生成ArcartX UI界面并发送给客户端</li></ul><h3 id="_3-文件系统结构" tabindex="-1">3. 文件系统结构 <a class="header-anchor" href="#_3-文件系统结构" aria-label="Permalink to &quot;3. 文件系统结构&quot;">​</a></h3><p><code>plugins/ArcartXSuite/</code> 目录包含：</p><ul><li><strong>config.yml</strong>: 总开关配置和模块授权设置</li><li><em><em>ArcartX</em>.yml</em>*: 各模块的主配置文件</li><li><strong>ui/</strong>: ArcartX UI模板文件</li><li><strong>chat/, mail/, ...</strong>: 各模块的数据目录</li><li>*<strong>.db</strong>: SQLite数据库文件</li></ul><h3 id="_4-数据存储" tabindex="-1">4. 数据存储 <a class="header-anchor" href="#_4-数据存储" aria-label="Permalink to &quot;4. 数据存储&quot;">​</a></h3><ul><li><strong>SQLite</strong>: 默认的单机数据库</li><li><strong>MySQL</strong>: 可选的多服共享数据库</li></ul><h3 id="_5-桥接层" tabindex="-1">5. 桥接层 <a class="header-anchor" href="#_5-桥接层" aria-label="Permalink to &quot;5. 桥接层&quot;">​</a></h3><ul><li><strong>包桥接</strong>: 与ArcartX客户端的网络通信</li><li><strong>客户端桥接</strong>: 客户端功能调用</li><li><strong>物品桥接</strong>: 物品数据处理</li></ul><h2 id="数据流向" tabindex="-1">数据流向 <a class="header-anchor" href="#数据流向" aria-label="Permalink to &quot;数据流向&quot;">​</a></h2><ol><li><strong>客户端 → 服务端</strong>: 玩家操作通过网络包发送到服务端</li><li><strong>服务端处理</strong>: 业务逻辑层处理请求，读取配置，调用模块功能</li><li><strong>数据存储</strong>: 将数据保存到SQLite或MySQL数据库</li><li><strong>UI生成</strong>: 根据配置和数据生成UI界面</li><li><strong>服务端 → 客户端</strong>: 将UI数据通过网络包发送给客户端</li><li><strong>客户端渲染</strong>: 客户端MOD接收数据并渲染界面</li></ol><h2 id="模块架构" tabindex="-1">模块架构 <a class="header-anchor" href="#模块架构" aria-label="Permalink to &quot;模块架构&quot;">​</a></h2><div class="language-mermaid vp-adaptive-theme"><button title="Copy Code" class="copy"></button><span class="lang">mermaid</span><pre class="shiki shiki-themes github-light github-dark vp-code" tabindex="0"><code><span class="line"><span style="${ssrRenderStyle({ "--shiki-light": "#24292E", "--shiki-dark": "#E1E4E8" })}">graph TB</span></span>
<span class="line"><span style="${ssrRenderStyle({ "--shiki-light": "#24292E", "--shiki-dark": "#E1E4E8" })}">    subgraph &quot;模块系统架构&quot;</span></span>
<span class="line"><span style="${ssrRenderStyle({ "--shiki-light": "#24292E", "--shiki-dark": "#E1E4E8" })}">        Registry[模块注册表]</span></span>
<span class="line"><span style="${ssrRenderStyle({ "--shiki-light": "#24292E", "--shiki-dark": "#E1E4E8" })}">        Loader[模块加载器]</span></span>
<span class="line"><span style="${ssrRenderStyle({ "--shiki-light": "#24292E", "--shiki-dark": "#E1E4E8" })}">        Core[核心模块]</span></span>
<span class="line"><span style="${ssrRenderStyle({ "--shiki-light": "#24292E", "--shiki-dark": "#E1E4E8" })}">        Modules[功能模块]</span></span>
<span class="line"><span style="${ssrRenderStyle({ "--shiki-light": "#24292E", "--shiki-dark": "#E1E4E8" })}">        </span></span>
<span class="line"><span style="${ssrRenderStyle({ "--shiki-light": "#24292E", "--shiki-dark": "#E1E4E8" })}">        Registry --&gt; Loader</span></span>
<span class="line"><span style="${ssrRenderStyle({ "--shiki-light": "#24292E", "--shiki-dark": "#E1E4E8" })}">        Loader --&gt; Core</span></span>
<span class="line"><span style="${ssrRenderStyle({ "--shiki-light": "#24292E", "--shiki-dark": "#E1E4E8" })}">        Loader --&gt; Modules</span></span>
<span class="line"><span style="${ssrRenderStyle({ "--shiki-light": "#24292E", "--shiki-dark": "#E1E4E8" })}">        </span></span>
<span class="line"><span style="${ssrRenderStyle({ "--shiki-light": "#24292E", "--shiki-dark": "#E1E4E8" })}">        subgraph &quot;核心模块&quot;</span></span>
<span class="line"><span style="${ssrRenderStyle({ "--shiki-light": "#24292E", "--shiki-dark": "#E1E4E8" })}">            API[AXS-API]</span></span>
<span class="line"><span style="${ssrRenderStyle({ "--shiki-light": "#24292E", "--shiki-dark": "#E1E4E8" })}">            CoreJar[axs-core]</span></span>
<span class="line"><span style="${ssrRenderStyle({ "--shiki-light": "#24292E", "--shiki-dark": "#E1E4E8" })}">        end</span></span>
<span class="line"><span style="${ssrRenderStyle({ "--shiki-light": "#24292E", "--shiki-dark": "#E1E4E8" })}">        </span></span>
<span class="line"><span style="${ssrRenderStyle({ "--shiki-light": "#24292E", "--shiki-dark": "#E1E4E8" })}">        subgraph &quot;功能模块 (21个)&quot;</span></span>
<span class="line"><span style="${ssrRenderStyle({ "--shiki-light": "#24292E", "--shiki-dark": "#E1E4E8" })}">            Free[免费模块]</span></span>
<span class="line"><span style="${ssrRenderStyle({ "--shiki-light": "#24292E", "--shiki-dark": "#E1E4E8" })}">            Paid[付费模块]</span></span>
<span class="line"><span style="${ssrRenderStyle({ "--shiki-light": "#24292E", "--shiki-dark": "#E1E4E8" })}">        end</span></span>
<span class="line"><span style="${ssrRenderStyle({ "--shiki-light": "#24292E", "--shiki-dark": "#E1E4E8" })}">    end</span></span></code></pre></div><h2 id="配置文件层次" tabindex="-1">配置文件层次 <a class="header-anchor" href="#配置文件层次" aria-label="Permalink to &quot;配置文件层次&quot;">​</a></h2><div class="language-mermaid vp-adaptive-theme"><button title="Copy Code" class="copy"></button><span class="lang">mermaid</span><pre class="shiki shiki-themes github-light github-dark vp-code" tabindex="0"><code><span class="line"><span style="${ssrRenderStyle({ "--shiki-light": "#24292E", "--shiki-dark": "#E1E4E8" })}">graph LR</span></span>
<span class="line"><span style="${ssrRenderStyle({ "--shiki-light": "#24292E", "--shiki-dark": "#E1E4E8" })}">    subgraph &quot;配置文件层次&quot;</span></span>
<span class="line"><span style="${ssrRenderStyle({ "--shiki-light": "#24292E", "--shiki-dark": "#E1E4E8" })}">        Main[config.yml&lt;br/&gt;主配置]</span></span>
<span class="line"><span style="${ssrRenderStyle({ "--shiki-light": "#24292E", "--shiki-dark": "#E1E4E8" })}">        Module[ArcartX*.yml&lt;br/&gt;模块配置]</span></span>
<span class="line"><span style="${ssrRenderStyle({ "--shiki-light": "#24292E", "--shiki-dark": "#E1E4E8" })}">        Data[data/&lt;br/&gt;模块数据]</span></span>
<span class="line"><span style="${ssrRenderStyle({ "--shiki-light": "#24292E", "--shiki-dark": "#E1E4E8" })}">        </span></span>
<span class="line"><span style="${ssrRenderStyle({ "--shiki-light": "#24292E", "--shiki-dark": "#E1E4E8" })}">        Main --&gt;|控制| Module</span></span>
<span class="line"><span style="${ssrRenderStyle({ "--shiki-light": "#24292E", "--shiki-dark": "#E1E4E8" })}">        Module --&gt;|生成| Data</span></span>
<span class="line"><span style="${ssrRenderStyle({ "--shiki-light": "#24292E", "--shiki-dark": "#E1E4E8" })}">    end</span></span></code></pre></div><p>这个架构图清晰地展示了：</p><ol><li>客户端与服务端的分离</li><li>文件系统结构作为服务端的子部分</li><li>明确的数据流向和组件关系</li><li>模块系统的层次结构</li><li>配置文件的管理层次</li></ol></div>`);
}
const _sfc_setup = _sfc_main.setup;
_sfc_main.setup = (props, ctx) => {
  const ssrContext = useSSRContext();
  (ssrContext.modules || (ssrContext.modules = /* @__PURE__ */ new Set())).add("architecture/system-architecture.md");
  return _sfc_setup ? _sfc_setup(props, ctx) : void 0;
};
const systemArchitecture = /* @__PURE__ */ _export_sfc(_sfc_main, [["ssrRender", _sfc_ssrRender]]);
export {
  __pageData,
  systemArchitecture as default
};
