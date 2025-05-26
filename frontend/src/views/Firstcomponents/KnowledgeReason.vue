<template>
    <div class="bottom-section">
        <!-- 左侧控制面板 -->
        <div class="control-panel">
            <div class="panel-section">
                <h3>
                    数据控制
                    <el-tooltip
                        content="加载初始知识图谱数据，展示图谱的拓扑结构"
                        placement="right"
                        effect="dark"
                    >
                        <el-icon class="info-icon"><InfoFilled /></el-icon>
                    </el-tooltip>
                </h3>
                <el-button type="primary" @click="showLimitGraph" class="control-btn">
                    <el-icon><DataAnalysis /></el-icon>
                    加载初始图谱
                </el-button>
            </div>

            <div class="panel-section">
                <h3>
                    推理分析
                    <el-tooltip
                        content="基于知识图谱进行路径推理，分析节点间的关联关系"
                        placement="right"
                        effect="dark"
                    >
                        <el-icon class="info-icon"><InfoFilled /></el-icon>
                    </el-tooltip>
                </h3>
                <el-button type="success" @click="triggerPathAnalysis" class="control-btn">
                    <el-icon><Connection /></el-icon>
                    路径推理
                </el-button>
            </div>

            <div class="panel-section">
                <h3>
                    状态传播
                    <el-tooltip
                        content="分析系统状态的传播路径，追踪故障传播链路"
                        placement="right"
                        effect="dark"
                    >
                        <el-icon class="info-icon"><InfoFilled /></el-icon>
                    </el-tooltip>
                </h3>
                <el-button type="warning" @click="getTransportationPath" class="control-btn">
                    <el-icon><Promotion /></el-icon>
                    状态传播分析
                </el-button>
            </div>
        </div>

        <!-- 中间图谱展示区 -->
        <div class="graph-container">
            <div id="echarts-graph" class="graph"></div>
            <div class="graph-controls">
                <el-button-group>
                    <el-button type="primary" @click="zoomIn">
                        <el-icon><ZoomIn /></el-icon>
                    </el-button>
                    <el-button type="primary" @click="zoomOut">
                        <el-icon><ZoomOut /></el-icon>
                    </el-button>
                    <el-button type="primary" @click="resetView">
                        <el-icon><Refresh /></el-icon>
                    </el-button>
                </el-button-group>
            </div>
        </div>

        <!-- 右侧信息面板 -->
        <div class="info-panel" v-if="infoVisible">
            <div class="panel-header">
                <h3>节点详情</h3>
                <el-button type="text" @click="infoVisible = false">
                    <el-icon><Close /></el-icon>
                </el-button>
            </div>
            <div class="node-info">
                <div class="properties-section">
                    <h4>属性信息</h4>
                    <div v-for="(value, key) in selectedNode.properties" :key="key" class="property-item">
                        <span class="property-label">{{ key }}:</span>
                        <span class="property-value">{{ value }}</span>
                    </div>
                </div>
            </div>
        </div>
    </div>
</template>

<script setup>
import { ref, onMounted, onUnmounted } from 'vue';
import * as echarts from 'echarts';
import { useStore } from 'vuex';
import { ElMessage } from 'element-plus';
import { graphService } from '@/service/graph';
import { DataAnalysis, Connection, Promotion, ZoomIn, ZoomOut, Refresh, Close, InfoFilled } from '@element-plus/icons-vue';

const store = useStore();
const chart = ref(null);
const infoVisible = ref(false);
const selectedNode = ref({
    name: '',
    properties: {}
});

const lightColors = [
    'rgba(64, 158, 255, 0.8)',  // 蓝色
    'rgba(103, 194, 58, 0.8)',  // 绿色
    'rgba(230, 162, 60, 0.8)',  // 橙色
    'rgba(245, 108, 108, 0.8)', // 红色
    'rgba(144, 147, 153, 0.8)', // 灰色
    'rgba(64, 158, 255, 0.8)',  // 蓝色
    'rgba(103, 194, 58, 0.8)'   // 绿色
];

// 初始化图谱
const initECharts = () => {
    chart.value = echarts.init(document.getElementById('echarts-graph'));
    
    const option = {
        backgroundColor: 'transparent',
        tooltip: {
            trigger: 'item',
            formatter: function (params) {
                if (params.dataType === 'edge') {
                    return `关系: ${params.data.type || ''}`;
                }
                return `${params.data.name || ''}`;
            },
            backgroundColor: 'rgba(0,0,0,0.7)',
            borderColor: '#409EFF',
            textStyle: {
                color: '#fff'
            }
        },
        series: [{
            type: 'graph',
            layout: 'force',
            force: {
                repulsion: 200,
                edgeLength: 100
            },
            roam: true,
            label: {
                show: false
            },
            emphasis: {
                label: {
                    show: true
                }
            },
            lineStyle: {
                color: '#409EFF',
                curveness: 0,
                width: 2
            },
            edgeLabel: {
                show: false,
                formatter: '{c}',
                color: '#fff'
            },
            edgeSymbol: ['none', 'arrow'],
            itemStyle: {
                borderColor: '#fff',
                borderWidth: 2
            }
        }]
    };

    chart.value.setOption(option);
    chart.value.on('click', 'series.graph.data', handleNodeClick);
};

// 处理节点点击
const handleNodeClick = (event) => {
    const node = event.data;
    selectedNode.value = {
        properties: node.properties || {}
    };
    infoVisible.value = true;
};

// 更新图谱
const updateGraph = () => {
    const nodes = (store.state.nodes || []).map((node, index) => ({
        id: node.id,
        name: node.properties?.name || node.name || '',
        symbolSize: 45,
        properties: node.properties || {},
        itemStyle: {
            color: lightColors[index % lightColors.length]
        },
        label: {
            show: false
        }
    }));

    const edges = (store.state.edges || []).map(edge => ({
        source: edge.source,
        target: edge.target,
        type: edge.type,
        label: {
            show: false,
            formatter: edge.type,
            color: '#fff'
        },
        symbol: ['none', 'arrow']
    }));

    const option = {
        series: [{
            data: nodes,
            edges: edges
        }]
    };

    chart.value.setOption(option);
};

// 加载有限制的图谱
const showLimitGraph = () => {
    graphService.getLimitedGraph()
        .then(data => {
            const { nodes, edges } = data;
            store.commit('SET_NODES', nodes);
            store.commit('SET_EDGES', edges);
            updateGraph();
        })
        .catch(error => {
            console.error('获取有限制的图数据失败:', error);
        });
};

// 推理路径
const triggerPathAnalysis = () => {
    try {
        graphService.sendNodes();
        graphService.reload()
            .then(data => {
                const { nodes, edges } = data;
                store.commit('SET_NODES', nodes);
                store.commit('SET_EDGES', edges);
                updateGraph();

                setTimeout(() => {
                    graphService.getPath()
                        .then(pathData => {
                            const { nodes: pathNodes, edges: pathEdges } = pathData;

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
                                    formatter: edge.type,
                                    color: '#fff'
                                },
                                symbol: ['none', 'arrow']
                            }));

                            const pathNodeIds = newNodes.map(node => node.id);

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
                                    await new Promise(resolve => setTimeout(resolve, 1000));
                                }

                                await new Promise(resolve => setTimeout(resolve, 2000));

                                const finalNodes = nodes.filter(node => pathNodeIds.includes(node.id));
                                const finalEdges = edges.filter(edge => pathNodeIds.includes(edge.source) && pathNodeIds.includes(edge.target));

                                const finalOption = {
                                    series: [{
                                        data: finalNodes.map(node => ({
                                            ...node,
                                            symbol: 'circle',
                                            symbolSize: 45,
                                            tooltip: {
                                                formatter: function(params) {
                                                    if (params.data.properties) {
                                                        let propertiesHtml = '<div style="padding: 10px;">';
                                                        for (const [key, value] of Object.entries(params.data.properties)) {
                                                            propertiesHtml += `<div style="margin: 5px 0;"><b>${key}:</b> ${value}</div>`;
                                                        }
                                                        propertiesHtml += '</div>';
                                                        return propertiesHtml;
                                                    }
                                                    return params.data.name || '';
                                                }
                                            }
                                        })),
                                        edges: finalEdges.map(edge => ({
                                            ...edge,
                                            symbol: ['none', 'arrow']
                                        }))
                                    }]
                                };
                                chart.value.setOption(finalOption);
                            };

                            highlightPath();
                        })
                        .catch(pathError => {
                            console.error('路径推理失败:', pathError);
                        });
                }, 1000);
            })
            .catch(reloadError => {
                console.error('重新加载数据失败:', reloadError);
            });
    } catch (error) {
        alert('操作失败: ' + error.message);
    }
};

// 获取状态传播路径
const getTransportationPath = async () => {
    try {
        const data = await graphService.getTransportPath();
        const { nodes, edges } = data;
        store.commit('SET_NODES', nodes);
        store.commit('SET_EDGES', edges);
        updateGraph();
    } catch (error) {
        console.error('获取状态传播路径失败:', error);
        ElMessage.error('获取状态传播路径失败，请稍后重试');
    }
};

// 图表控制
const zoomIn = () => {
    const option = chart.value.getOption();
    const zoom = option.series[0].zoom || 1;
    chart.value.setOption({
        series: [{
            zoom: zoom * 1.2
        }]
    });
};

const zoomOut = () => {
    const option = chart.value.getOption();
    const zoom = option.series[0].zoom || 1;
    chart.value.setOption({
        series: [{
            zoom: zoom * 0.8
        }]
    });
};

const resetView = () => {
    chart.value.setOption({
        series: [{
            zoom: 1
        }]
    });
};

// 生命周期钩子
onMounted(() => {
    initECharts();
    window.addEventListener('resize', () => {
        if (chart.value) {
            chart.value.resize();
        }
    });
});

onUnmounted(() => {
    if (chart.value) {
        chart.value.dispose();
    }
    window.removeEventListener('resize', () => {
        if (chart.value) {
            chart.value.resize();
        }
    });
});
</script>

<style scoped>
.bottom-section {
    flex: 1;
    display: grid;
    grid-template-columns: 280px 1fr 320px;
    gap: 0;
    padding: 20px;
    min-height: 60vh;
}

.control-panel {
    background: linear-gradient(135deg, rgba(64, 158, 255, 0.1) 0%, rgba(64, 158, 255, 0.05) 100%);
    border-radius: 10px;
    padding: 20px;
    border: 1px solid rgba(64, 158, 255, 0.2);
    box-shadow: 0 0 20px rgba(64, 158, 255, 0.1);
    backdrop-filter: blur(10px);
    height: fit-content;
}

.panel-section {
    margin-bottom: 30px;
    background: rgba(0, 0, 0, 0.2);
    padding: 15px;
    border-radius: 8px;
    border: 1px solid rgba(64, 158, 255, 0.1);
}

.panel-section h3 {
    color: #409EFF;
    margin-bottom: 15px;
    font-size: 16px;
    text-transform: uppercase;
    letter-spacing: 1px;
    border-bottom: 1px solid rgba(64, 158, 255, 0.2);
    padding-bottom: 10px;
    display: flex;
    align-items: center;
    gap: 8px;
}

.info-icon {
    color: #409EFF;
    font-size: 16px;
    cursor: help;
    transition: all 0.3s ease;
}

.info-icon:hover {
    transform: scale(1.1);
    color: #66b1ff;
}

.control-btn {
    width: 100%;
    margin-top: 10px;
    background: linear-gradient(90deg, rgba(64, 158, 255, 0.2) 0%, rgba(64, 158, 255, 0.1) 100%);
    border: 1px solid rgba(64, 158, 255, 0.3);
    color: #409EFF;
    transition: all 0.3s ease;
}

.control-btn:hover {
    background: linear-gradient(90deg, rgba(64, 158, 255, 0.3) 0%, rgba(64, 158, 255, 0.2) 100%);
    transform: translateY(-2px);
    box-shadow: 0 5px 15px rgba(64, 158, 255, 0.2);
}

.graph-container {
    position: relative;
    background: linear-gradient(135deg, rgba(64, 158, 255, 0.1) 0%, rgba(64, 158, 255, 0.05) 100%);
    border-radius: 10px;
    overflow: hidden;
    border: 1px solid rgba(64, 158, 255, 0.2);
    box-shadow: 0 0 20px rgba(64, 158, 255, 0.1);
    backdrop-filter: blur(10px);
    margin: 0 20px;
    height: calc(100vh - 40vh - 40px);
    min-height: 500px;
}

.graph {
    width: 100%;
    height: 100%;
    min-height: 500px;
}

.graph-controls {
    position: absolute;
    bottom: 20px;
    right: 20px;
    background: rgba(0, 0, 0, 0.6);
    padding: 10px;
    border-radius: 8px;
    border: 1px solid rgba(64, 158, 255, 0.3);
    backdrop-filter: blur(5px);
}

.info-panel {
    background: linear-gradient(135deg, rgba(64, 158, 255, 0.1) 0%, rgba(64, 158, 255, 0.05) 100%);
    border-radius: 10px;
    padding: 20px;
    border: 1px solid rgba(64, 158, 255, 0.2);
    box-shadow: 0 0 20px rgba(64, 158, 255, 0.1);
    backdrop-filter: blur(10px);
    height: fit-content;
}

.panel-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    margin-bottom: 20px;
    padding-bottom: 10px;
    border-bottom: 1px solid rgba(64, 158, 255, 0.2);
}

.panel-header h3 {
    color: #409EFF;
    margin: 0;
    font-size: 18px;
    text-transform: uppercase;
    letter-spacing: 1px;
}

.node-info {
    color: #fff;
}

.properties-section {
    margin-top: 20px;
}

.properties-section h4 {
    color: #409EFF;
    margin-bottom: 15px;
    font-size: 16px;
    text-transform: uppercase;
    letter-spacing: 1px;
}

.property-item {
    margin-bottom: 10px;
    padding: 10px;
    background: rgba(0, 0, 0, 0.2);
    border-radius: 6px;
    border: 1px solid rgba(64, 158, 255, 0.1);
}

.property-label {
    color: #909399;
    margin-right: 10px;
    font-size: 14px;
}

.property-value {
    color: #fff;
    font-weight: 500;
}
</style> 