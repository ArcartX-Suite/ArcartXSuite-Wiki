import DefaultTheme from 'vitepress/theme'
import './styles/custom.css'
import HomeBackground from './components/HomeBackground.vue'
import HeroSubtagline from './components/HeroSubtagline.vue'
import { h } from 'vue'

// 空组件：彻底移除主题切换按钮
const EmptyComponent = { render: () => null }

export default {
  extends: DefaultTheme,
  Layout() {
    return h(DefaultTheme.Layout, null, {
      'layout-top': () => h(HomeBackground),
      'home-hero-info-after': () => h(HeroSubtagline),
    })
  },
  enhanceApp({ app }) {
    app.component('VPSwitchAppearance', EmptyComponent)
    app.component('VPNavBarAppearance', EmptyComponent)
    app.component('VPNavScreenAppearance', EmptyComponent)
  },
}
