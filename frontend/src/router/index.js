import {createRouter, createWebHistory} from 'vue-router'
import HomeView from '../views/HomeView.vue'
import ChatView from "@/views/ChatView.vue";
import FaultListView from "@/views/FaultListView.vue";
import InformationView from "@/views/InformationView.vue";
import Deduce from "@/views/Deduce.vue";
import Transportation from "@/views/Transportation";
import IndexSystem from "@/views/zhiBiaoTiXi"
import Induce from "@/views/Youfayinsu"
import Guanjian from "@/views/guanjianyaosu"


import store from "@/store";
import RiskReasoning from "@/views/riskReasoning.vue";
import All from "@/views/DiscoveryAndReason.vue"
import Multi from "@/views/multi.vue";
import QueryPane from "@/views/QueryPane.vue";
import KnowledgeFind from "@/views/knowledgeFind.vue";

const routes = [
    {
        path: '/home',
        name: 'home',
        component: HomeView,
        meta: { requiresAuth: true },
        children: [
            {
                path: 'information',
                component: InformationView,
                meta: { requiresAuth: true },
            },
            {
                path: 'chat',
                component: ChatView,
                meta: { requiresAuth: true },
            },
            {
                path: 'faultList',
                component: FaultListView,
                meta: { requiresAuth: true },
            },
            {
                path: 'deduce',
                component: Deduce,
                meta: {requiresAuth: true},
            },
            {
                path: 'transport',
                component: Transportation,
                meta: {requiresAuth: true},
            },
            {
                path: 'index',
                component: IndexSystem,
                meta: {requiresAuth: true},

            },
            {
                path: 'induce',
                component: Induce,
                meta: {requiresAuth: true},

            },
            {
                path: 'guanjian',
                component: Guanjian,
                meta: {requiresAuth: true},

            },
            {
                path: 'riskReason',
                component: RiskReasoning,
                meta: {requiresAuth: true},

            },
            {
                path: 'discoveryAndReason',
                component: All,
                meta: {requiresAuth: true},

            },
            {
                path: 'multi',
                component: Multi,
                meta: {requiresAuth: true},

            },
            {
                path: 'fengge',
                component: QueryPane,
                meta: {requiresAuth: true},

            },
            {
                path: 'find',
                component: KnowledgeFind,
                meta: {requiresAuth: true},

            }



        ]
    },
    {
        path: '/login',
        name: 'login',
        component: () => import('../views/LoginView.vue')
    },
    {
        path: '/',
        redirect: '/login'
    }
]

const router = createRouter({
    history: createWebHistory(process.env.BASE_URL),
    routes
})

// 全局路由守卫
// router.beforeEach((to, from, next) => {
//     const requiresAuth = to.matched.some(record => record.meta.requiresAuth);
//     const token = window.localStorage.getItem('satoken');
//
//     if (requiresAuth && !token) {
//         // 如果 token 为空，重定向到登录页面
//         next('/login');
//     } else {
//         next(); // 否则继续
//     }
// });
export default router
