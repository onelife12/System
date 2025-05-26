import { createStore } from 'vuex';

export default createStore({
    state() {
        return {
            satoken: 'swjtu' + localStorage.getItem('satoken') || null,
            nodes: [],
            edges: []
        };
    },
    mutations: {
        SET_TOKEN(state, satoken) {
            state.satoken = satoken;
        },
        REMOVE_TOKEN(state) {
            state.satoken = null;
        },
        SET_NODES(state, nodes) {
            state.nodes = nodes
        },
        SET_EDGES(state, edges) {
            state.edges = edges
        }
    },
    actions: {
        setToken({ commit }, satoken) {
            commit('SET_TOKEN', satoken);
        },
        removeToken({ commit }) {
            commit('REMOVE_TOKEN');
        }
    }
});