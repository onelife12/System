<template>
    <el-container style="height: 100vh;">
        <!-- 设置 el-header 的背景色为蓝色 -->
        <el-header style="background-color: gray; color: white;">
            <!-- 这里可以添加头部内容，例如标题、导航栏等 -->
            <h1>基于知识图谱的复杂关联关系挖掘</h1>
        </el-header>
        <el-container>
            <!-- 设置 el-aside 的背景色为橙色 -->
            <el-aside width="100px" style="background-color: cornsilk;">
                <!-- 侧边栏内容，例如操作按钮 -->
                <div class="toolbar" style="padding: 0.5rem; background: #5d9a87">
                    <button @click="showLimitGraph" style="color: #c7bdbd;" class="btn btn-primary">
                        <i class="fas fa-project-diagram"></i> 图谱展示
                    </button>
                    <input v-model="dataLimit" type="number" placeholder="输入数据数量">
                </div>

                <div class="toolbar" style="padding: 0.5rem; background: #5d9a87">
                    <button @click="triggerPathAnalysis" style="color: #c7bdbd;" class="btn btn-success">
                        <i class="fas fa-sitemap"></i> 推理路径
                    </button>
                    <!-- 添加初始节点和终端节点输入框 -->
                    <input v-model="startNode" type="text" placeholder="初始节点">
                    <input v-model="endNode" type="text" placeholder="终端节点">
                    <button @click="sendNodesToBackend" style="color: #c7bdbd;" class="btn btn-info">
                        发送节点
                    </button>
                </div>
            </el-aside>
            <el-main>
                <!-- 主内容区域，放置图谱和节点信息面板 -->
                <div id="echarts-graph" style="width: 100%; height: 85vh;"></div>
                <div class="node-info-panel" id="infoPanel" :style="{ display: infoVisible ? 'block' : 'none' }">
                    <h5 class="mb-3" id="nodeTitle">{{ `ID: ${selectedNode.id}, Name: ${selectedNode.name}` }}</h5>
                    <div id="nodeProperties">
                        <div v-for="(value, key) in selectedNode.properties" :key="key">
                            <strong>{{ key }}:</strong> {{ value }}
                        </div>
                    </div>
                </div>
            </el-main>
        </el-container>
    </el-container>
</template>

<script setup>
    import {onMounted, ref, reactive} from 'vue';
    import * as echarts from 'echarts';
    import {useStore} from 'vuex';
    import {graphService} from '../service/graph';

    const store = useStore();
    const chart = ref(null);
    const infoVisible = ref(false);
    const selectedNode = reactive({
        id: '',
        name: '',
        properties: {}
    });
    const dataLimit = ref('');
    // 新增初始节点和终端节点的响应式变量
    const startNode = ref('');
    const endNode = ref('');

    // 定义七种淡化色彩
    const lightColors = [
        'rgba(255, 192, 203, 0.8)', // 淡粉色
        'rgba(173, 216, 230, 0.8)', // 淡蓝色
        'rgba(255, 228, 181, 0.8)', // 淡橙色
        'rgba(144, 238, 144, 0.8)', // 淡绿色
        'rgba(255, 250, 205, 0.8)', // 淡黄色
        'rgba(216, 191, 216, 0.8)', // 淡紫色
        'rgba(255, 222, 173, 0.8)'  // 淡桃色
    ];

    onMounted(() => {
        initECharts();
        // store.dispatch('fetchGraph');
    });

    const initECharts = () => {
        chart.value = echarts.init(document.getElementById('echarts-graph'));

        const option = {
            tooltip: {},
            series: [
                {
                    type: 'graph',
                    layout: 'force',
                    force: {
                        repulsion: 100,
                        edgeLength: 100
                    },
                    roam: true,
                    label: {
                        show: false, // 默认不显示节点 label
                        formatter: (params) => params.data.id // 显示节点 id
                    },
                    emphasis: {
                        label: {
                            show: true, // 鼠标悬停时显示节点 label
                            formatter: (params) => params.data.name // 显示节点 name
                        },
                        edgeLabel: {
                          show: true // 鼠标悬浮在边上时显示边的标签
                        }
                    },
                    lineStyle: {
                        color: 'source',
                        curveness: 0 // 边为直线
                    },
                    // 设置为无向图
                    directed: false,
                    edgeLabel: {
                        show: false, // 默认不显示边的标签
                        formatter: (params) => params.data.type // 边的标签显示内容
                    },
                    // emphasis: {
                    //     edgeLabel: {
                    //         show: true // 鼠标悬浮在边上时显示边的标签
                    //     }
                    // }
                }
            ]
        };

        chart.value.setOption(option);

        chart.value.on('click', 'series.graph.data', (event) => {
            const node = event.data;
            selectedNode.id = node.id;
            selectedNode.name = node.name;
            selectedNode.properties = node.properties;
            infoVisible.value = true;
        });
    };

    const updateGraph = () => {
        const nodes = store.state.nodes.map((node, index) => ({
            id: node.id,
            name: node.name,
            symbolSize: 45,
            properties: node.properties,
            itemStyle: {
                color: lightColors[index % lightColors.length] // 循环使用颜色
            }
        }));
        const edges = store.state.edges.map(edge => ({
            source: edge.source,
            target: edge.target,
            type: edge.type,
            label: {
                show: false, // 默认不显示边的标签
                formatter: edge.type
            }
        }));

        const option = {
            series: [
                {
                    data: nodes,
                    edges: edges
                }
            ]
        };

        chart.value.setOption(option);
    };

    const showLimitGraph = async () => {
        const limit = parseInt(dataLimit.value);
        if (!isNaN(limit)) {
            try {
                const {nodes, edges} = await graphService.getLimitedGraph(limit);
                store.commit('SET_NODES', nodes);
                store.commit('SET_EDGES', edges);
                updateGraph();
            } catch (error) {
                console.error('获取有限制的图数据失败:', error);
            }
        }
    };

    const triggerPathAnalysis = async () => {
        try {
            // 先调用 reload 请求
            const {nodes, edges} = await graphService.reload();
            store.commit('SET_NODES', nodes);
            store.commit('SET_EDGES', edges);
            updateGraph();

            // 进行路径推理，这里假设后端接口返回三元组格式的路径数据
            const {nodes: pathNodes, edges: pathEdges} = await graphService.getPath();

            const newNodes = pathNodes.map((node, index) => ({
                id: node.id,
                name: node.name,
                symbolSize: 45,
                properties: node.properties,
                itemStyle: {
                    color: lightColors[index % lightColors.length] // 循环使用颜色
                }
            }));
            const newEdges = pathEdges.map(edge => ({
                source: edge.source,
                target: edge.target,
                type: edge.type,
                label: {
                    show: false, // 默认不显示边的标签
                    formatter: edge.type
                }
            }));

            // 收集路径节点的 id
            const pathNodeIds = newNodes.map(node => node.id);

            // 逐步高亮路径
            const highlightPath = async () => {
                const nodeIds = [];
                for (const edge of newEdges) {
                    if (!nodeIds.includes(edge.source)) {
                        nodeIds.push(edge.source);
                    }
                    if (!nodeIds.includes(edge.target)) {
                        nodeIds.push(edge.target);
                    }
                }

                for (const nodeId of nodeIds) {
                    const option = chart.value.getOption();
                    const nodeIndex = option.series[0].data.findIndex(node => node.id === nodeId);
                    if (nodeIndex !== -1) {
                        option.series[0].data[nodeIndex].itemStyle.color = 'red';
                        chart.value.setOption(option);
                    }
                    await new Promise(resolve => setTimeout(resolve, 1000)); // 延迟 2 秒
                }

                // 路径查找完成后，只展示路径
                const finalNodes = nodes.filter(node => pathNodeIds.includes(node.id));
                const finalEdges = edges.filter(edge => pathNodeIds.includes(edge.source) && pathNodeIds.includes(edge.target));

                const finalOption = {
                    series: [
                        {
                            data: finalNodes,
                            edges: finalEdges
                        }
                    ]
                };
                chart.value.setOption(finalOption);
            };

            highlightPath();
        } catch (error) {
            alert('操作失败: ' + error.message);
        }
    };

    // 新增方法，用于发送初始节点和终端节点信息到后端
    const sendNodesToBackend = async () => {
        try {
            const response = await graphService.sendNodes(startNode.value, endNode.value);
            console.log('节点信息发送成功:', response);
        } catch (error) {
            console.error('节点信息发送失败:', error);
        }
    };
</script>

<style scoped>
    #echarts-graph {
        width: 100%;
        height: 85vh;
        background: #f8f9fa;
        border-radius: 8px;
        box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
    }

    .toolbar {
        background: white;
        box-shadow: 0 2px 4px rgba(0, 0, 0, 0.1);
        margin-bottom: 0.5rem;
    }

    .node-info-panel {
        position: fixed;
        right: 1rem;
        top: 5rem;
        width: 300px;
        background: white;
        padding: 1rem;
        box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
        border-radius: 8px;
    }
</style>