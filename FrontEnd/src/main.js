import { createApp } from 'vue'
import App from './App.vue'
import router from './router'
import { createPinia } from 'pinia'
import ElementPlus from 'element-plus'
import 'element-plus/dist/index.css'
import VChart from 'vue-echarts'
import './styles/index.scss'
import particles from './directives/particles'

const app = createApp(App)

app.use(createPinia())
  .use(router)
  .use(ElementPlus)
  .component('VChart', VChart)
  .directive('particles', particles)
  .mount('#app')

