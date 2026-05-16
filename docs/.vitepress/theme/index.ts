import DefaultTheme from 'vitepress/theme'
import './styles/custom.css'
import HomeBackground from './components/HomeBackground.vue'
import HeroSubtagline from './components/HeroSubtagline.vue'
import { h } from 'vue'

export default {
  extends: DefaultTheme,
  Layout() {
    return h(DefaultTheme.Layout, null, {
      'layout-top': () => h(HomeBackground),
      'home-hero-info-after': () => h(HeroSubtagline),
    })
  },
}
