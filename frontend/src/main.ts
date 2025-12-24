import { createApp } from 'vue'
import './style.css'
import App from './App.vue'

// 1. 引入 Element Plus 和 样式
import ElementPlus from 'element-plus'
import 'element-plus/dist/index.css'

// 2. 引入 图标
import * as ElementPlusIconsVue from '@element-plus/icons-vue'

// 3. 引入 路由 (重要！)
import router from './router'

// 4. 引入 Pinia (如果你还没用到可以先注释，但建议保留)
import { createPinia } from 'pinia'

const app = createApp(App)

// 注册所有图标
for (const [key, component] of Object.entries(ElementPlusIconsVue)) {
  app.component(key, component)
}

app.use(createPinia())
app.use(ElementPlus)
app.use(router) // <--- 这一行必须有，否则路由不生效

app.mount('#app')