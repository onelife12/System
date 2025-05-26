<template>
    <el-container>
        <el-header style="background-color: #409EFF; color: white;">
            <h1>系统状态传播</h1>
        </el-header>
        <el-container>
            <el-aside width="200px" style="background-color: #f5f5f5;">
                <el-menu default-active="1" class="el-menu-vertical-demo">
                    <el-menu-item index="1">
                        <el-button type="primary" @click="getTransportationPath" icon="el-icon-s-promotion">
                            状态传播链路
                        </el-button>
                    </el-menu-item>
                </el-menu>
            </el-aside>
            <el-main>
                <div id="transportation-echarts-graph" style="width: 100%; height: 85vh;"></div>
            </el-main>
        </el-container>
    </el-container>
</template>

<script setup>
    import { onMounted, ref } from 'vue';
    import * as echarts from 'echarts';
    import { graphService } from '@/service/graph';

    const API_URL = '/api';
    const chart = ref(null);
    const lightColors = [
        'rgba(255, 192, 203, 0.8)',
        'rgba(173, 216, 230, 0.8)',
        'rgba(255, 228, 181, 0.8)',
        'rgba(144, 238, 144, 0.8)',
        'rgba(255, 250, 205, 0.8)',
        'rgba(216, 191, 216, 0.8)',
        'rgba(255, 222, 173, 0.8)'
    ];

    const initECharts = () => {
        chart.value = echarts.init(document.getElementById('transportation-echarts-graph'));

        const option = {
            tooltip: {
                trigger: 'item',
                formatter: '{b} ({c})'
            },
            series: [
                {
                    type: 'graph',
                    layout: 'force',
                    force: {
                        repulsion: 200,
                        edgeLength: 100
                    },
                    roam: true,
                    label: {
                        show: true,
                        position: 'right',
                        formatter: '{b}'
                    },
                    lineStyle: {
                        color: 'source',
                        curveness: 0.3
                    },
                    edgeLabel: {
                        show: true,
                        formatter: '{c}'
                    }
                }
            ]
        };

        chart.value.setOption(option);
    };

    const getTransportationPath = () => {
        graphService.getTransportPath()
            .then(data => {
                const { nodes: pathNodes, edges: pathEdges } = data;
                const newNodes = pathNodes.map((node, index) => ({
                    id: node.id,
                    name: node.name,
                    symbolSize: 45,
                    properties: node.properties,
                    itemStyle: {
                        color: lightColors[index % lightColors.length]
                    }
                }));
                const newEdges = pathEdges.map(edge => ({
                    source: edge.source,
                    target: edge.target,
                    type: edge.type,
                    label: {
                        show: false,
                        formatter: edge.type
                    }
                }));

                const pathNodeIds = newNodes.map(node => node.id);

                const showNodesStepByStep = async () => {
                    for (let i = 0; i < newNodes.length; i++) {
                        const currentNodes = newNodes.slice(0, i + 1);
                        const currentEdges = newEdges.filter(edge =>
                            currentNodes.some(node => node.id === edge.source) &&
                            currentNodes.some(node => node.id === edge.target)
                        );

                        const option = {
                            series: [
                                {
                                    data: currentNodes,
                                    edges: currentEdges
                                }
                            ]
                        };

                        chart.value.setOption(option);
                        await new Promise(resolve => setTimeout(resolve, 1000));
                    }
                };

                showNodesStepByStep();
            })
            .catch(error => {
                console.error('获取状态传播路径失败:', error);
            });
    };

    onMounted(() => {
        initECharts();
    });
</script>

<style scoped>
    #transportation-echarts-graph {
        width: 100%;
        height: 85vh;
        background: #f8f9fa;
        border-radius: 8px;
        box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
    }
</style>