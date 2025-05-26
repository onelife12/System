import axios from 'axios';
import store from '@/store/index'; // 导入 Vuex store

// 创建 Axios 实例
const http = axios.create({
    baseURL: '', // 您的 API 服务器地址
    timeout: 1000 * 60, // 设置超时时间
});

// 请求拦截器
http.interceptors.request.use(
    config => {
        console.log('http拦截器生效')
        // 从 Vuex store 获取 token
        const token = store.state.token;

        // 如果 token 存在且请求路径不是 'login'，则添加到请求头
        if (token && config.url.indexOf('login') === -1) {
            config.headers.token = `${token}`;
        }

        return config;
    },
    error => {
        return Promise.reject(error);
    }
);

export default http;