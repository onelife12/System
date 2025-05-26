import axios from "axios";

const request = axios.create({
    timeout: 5000
});

request.interceptors.request.use(config => {
    const satoken = localStorage.getItem('satoken');
    console.log('request拦截器生效')
    if (satoken) {
        config.headers['satoken'] = 'swjtu EuxRrzg6mNpJgNESzyo5oJRKOhNVN5Eb';
    }
    config.headers["Content-Type"] = 'application/json;charset=UTF-8'
    return config
}), error => {
    return Promise.reject(error)
}

request.interceptors.response.use(
    response => {
        let res = response.data
        if (response.config.responseType === 'blob') {
            return res
        }
        if (typeof response === 'string') {
            res = res ? JSON.parse(res) : res
        }
        return res;
    },
    error => {
        console.log("err", error)
        return Promise.reject(error);
    }
)
export default request