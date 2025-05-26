import { createApp } from 'vue'
import App from './App.vue'
import router from './router'
import store from '@/store/index'
import ElementPlus from 'element-plus'
import 'element-plus/dist/index.css'
import locale from 'element-plus/es/locale/lang/zh-cn'
import axios from "axios";


// 配置Axios拦截器
axios.interceptors.response.use(
    response => response,
    error => {
        if (error.response && error.response.status === 401) {
            router.push({ name: 'login' });
        }
        return Promise.reject(error);
    }
);

const app = createApp(App);

// 开发环境下启用 Vue Devtools
if (process.env.NODE_ENV === 'development') {
    app.config.devtools = true;
}

app.use(store)
   .use(router)
   .use(ElementPlus)
   .use(locale)
   .mount('#app');
