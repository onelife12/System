import axios from 'axios';

const API_URL = 'http://localhost:9000/api';
// const API_URL = '/api';

export const graphService = {
    // 图谱相关接口
    getFullGraph: async () => {
        const response = await axios.get(`${API_URL}/graph`);
        // 解析后端返回的数据，提取 data 部分
        return response.data.data;
    },
    getPath: async () => {
        const response = await axios.get(`${API_URL}/path`);
        // 解析后端返回的数据，提取 data 部分
        return response.data.data;
    },
    getLimitedGraph: async (limit) => {
        const response = await axios.get(`${API_URL}/limit`, {
            params: {
                limit: 500
            }
        });
        return response.data.data;
    },
    reload: async () => {
        const response = await axios.get(`${API_URL}/reload`);
        return response.data.data;
    },
    // 新增方法，用于发送初始节点和终端节点信息到后端
    sendNodes: async (startNode, endNode) => {
        const response = await axios.post(`${API_URL}/sendNodes`, {
                startNode: "A320",
                endNode: "机体结构故障"

        });
        return response.data;
    },
    getTransportPath: async (faultPhenomenon = "TERR范围不一致消息") => {
        const response = await axios.post(`${API_URL}/transport`, {
            faultPhenomenon: faultPhenomenon
        });
        return response.data.data;
    },

    // 登录相关
    login: async (loginForm) => {
        const response = await axios.post(`${API_URL}/auth/user/doLogin`, loginForm);
        return response.data;
    },

    // // 结果文件相关
    // getResult: async (filename) => {
    //     const response = await axios.get(`${API_URL}/get_result/${filename}`);
    //     return response.data;
    // },
    //
    // getResultFiles: async () => {
    //     const response = await axios.get(`${API_URL}/list_result_files`);
    //     return response.data;
    // },
    //
    // getLogs: async () => {
    //     const response = await axios.get(`${API_URL}/logs`);
    //     return response.data;
    // },

    // 故障信息相关
    getFaultInformation: async () => {
        const response = await axios.get(`${API_URL}/fault/information`);
        return response.data;
    },

    // 诱发因素相关
    getInduceFactors: async (params) => {
        const response = await axios.get(`${API_URL}/induce/factors`, { params });
        return response.data;
    },

    updateInduceFactor: async (data) => {
        const response = await axios.post(`${API_URL}/induce/factors/update`, data);
        return response.data;
    },

    // 指标体系相关接口
    getResult: async (status = "飞控计算机故障") => {
        const response = await axios.post(`${API_URL}/eval`, {
            faultPhenomenon: status
        });
        return response.data;
    },

    // 弱关联知识发现
    getWeaklyRelatedKnowledge: async () => {
        const response = await axios.get(`${API_URL}/knowledge_discovery`);
        return response.data;
    },

}; 