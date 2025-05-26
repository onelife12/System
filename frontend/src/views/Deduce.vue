<template>
  <div class="dashboard-container">
    <!-- 顶部标题栏 -->
    <div class="header">
      <div class="title-section">
        <h1>基于知识图谱的复杂关联关系挖掘</h1>
        <div class="subtitle">Knowledge Graph Analysis</div>
      </div>
      <div class="time-section">
        <div class="current-time">{{ currentTime }}</div>
        <div class="current-date">{{ currentDate }}</div>
      </div>
    </div>

    <!-- 功能简介区域 -->
    <div class="function-intro">
      <h3>功能简介</h3>
      <ul>
        <li>1、加载图谱信息</li>
        <li>2、图谱节点与关系嵌入</li>
        <li>3、加载推理模型</li>
        <li>4、推理节点之间的最优路径</li>
      </ul>
    </div>

    <!-- 主要内容区域 -->
    <div class="main-content">
      <!-- 左侧控制面板 -->
      <div class="control-panel">
        <div class="panel-section">
          <el-button type="primary" @click="showLimitGraph" class="control-btn">
            <el-icon><DataAnalysis /></el-icon>
            加载初始图谱
          </el-button>
        </div>

        <div class="panel-section">
          <el-button type="success" @click="triggerPathAnalysis" class="control-btn">
            <el-icon><Connection /></el-icon>
            推理路径
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
  </div>
</template>

<script setup>
import { ref, onMounted, onUnmounted } from 'vue';
import * as echarts from 'echarts';
import { useStore } from 'vuex';
import { graphService } from '@/service/graph';
import { DataAnalysis, Connection, ZoomIn, ZoomOut, Refresh, Close } from '@element-plus/icons-vue';
import {ElMessage} from "element-plus";

const store = useStore();
const chart = ref(null);
const infoVisible = ref(false);
const selectedNode = ref({
  id: '',
  properties: {}
});
const currentTime = ref('');
const currentDate = ref('');

const lightColors = [
  'rgba(64, 158, 255, 0.8)',  // 蓝色
  'rgba(103, 194, 58, 0.8)',  // 绿色
  'rgba(230, 162, 60, 0.8)',  // 橙色
  'rgba(245, 108, 108, 0.8)', // 红色
  'rgba(144, 147, 153, 0.8)', // 灰色
  'rgba(64, 158, 255, 0.8)',  // 蓝色
  'rgba(103, 194, 58, 0.8)'   // 绿色
];

// 更新时间
const updateDateTime = () => {
  const now = new Date();
  currentTime.value = now.toLocaleTimeString();
  currentDate.value = now.toLocaleDateString();
};

// 初始化图表
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
        show: false,
        position: 'right',
        formatter: function(params) {
          return params.data.name || '';
        },
        color: '#fff',
        backgroundColor: 'rgba(0,0,0,0.7)',
        padding: [4, 8],
        borderRadius: 4
      },
      emphasis: {
        label: {
          show: true
        }
        // itemStyle: {
        //   shadowBlur: 0,
        //   shadowColor: 'transparent'
        // }
      },
      lineStyle: {
        color: '#409EFF',
        curveness: 0, // 边改为直线
        width: 2
      },
      edgeLabel: {
        show: false, // 边信息默认不显示
        formatter: '{c}',
        color: '#fff'
      },
      edgeSymbol: ['none', 'arrow'], // 边添加箭头
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
  
  // 点击时显示该节点的标签
  const option = chart.value.getOption();
  const nodeIndex = option.series[0].data.findIndex(n => n.id === node.id);
  if (nodeIndex !== -1) {
    option.series[0].data[nodeIndex].label = {
      show: true
    };
    chart.value.setOption(option);
  }
};

// 更新图谱
const updateGraph = () => {
  const nodes = store.state.nodes.map((node, index) => ({
    id: node.id,
    name: node.properties?.name || node.name || '',
    symbolSize: 45,
    properties: node.properties || {},
    itemStyle: {
      color: lightColors[index % lightColors.length]
    },
    label: {
      show: false // 确保初始状态下标签是隐藏的
    }
  }));

  const edges = store.state.edges.map(edge => ({
    source: edge.source,
    target: edge.target,
    type: edge.type,
    label: {
      show: false,
      formatter: edge.type,
      color: '#fff'
    },
    symbol: ['none', 'arrow'] // 边添加箭头
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
    sendNodesToBackend()
    // 先调用 reload 请求
    graphService.reload()
        .then(data => {
          const { nodes, edges } = data;
          store.commit('SET_NODES', nodes);
          store.commit('SET_EDGES', edges);
          updateGraph();

          // 进行路径推理，这里假设后端接口返回三元组格式的路径数据
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
                  symbol: ['none', 'arrow'] // 边添加箭头
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
                    await new Promise(resolve => setTimeout(resolve, 1000)); // 延迟 1 秒
                  }

                  // 路径查找完成后，只展示路径
                  const finalNodes = nodes.filter(node => pathNodeIds.includes(node.id));
                  const finalEdges = edges.filter(edge => pathNodeIds.includes(edge.source) && pathNodeIds.includes(edge.target));

                  const finalOption = {
                    series: [{
                      data: finalNodes,
                      edges: finalEdges.map(edge => ({
                        ...edge,
                        symbol: ['none', 'arrow'] // 边添加箭头
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
        })
        .catch(reloadError => {
          console.error('重新加载数据失败:', reloadError);
        });
  } catch (error) {
    alert('操作失败: ' + error.message);
  }
};
const sendNodesToBackend = async () => {
  try {
    const response = await graphService.sendNodes();
    const { nodes, edges } = response.data;
    store.commit('SET_NODES', nodes);
    store.commit('SET_EDGES', edges);
    // updateGraph(nodes, edges, true);
  } catch (error) {
    console.error('推理失败:', error);
    ElMessage.error('推理失败，请稍后重试');
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
  updateDateTime();
  const timer = setInterval(updateDateTime, 1000);
  onUnmounted(() => {
    clearInterval(timer);
    if (chart.value) {
      chart.value.dispose();
    }
  });
});
</script>

<style scoped>
.dashboard-container {
  min-height: 100vh;
  background: linear-gradient(135deg, #1a1a1a 0%, #2c3e50 100%);
  color: #fff;
  padding: 0;
  position: relative;
  overflow: hidden;
}

.dashboard-container::before {
  content: '';
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background:
      linear-gradient(90deg, rgba(255, 255, 255, 0.03) 1px, transparent 1px) 0 0 / 50px 50px,
      linear-gradient(rgba(255, 255, 255, 0.03) 1px, transparent 1px) 0 0 / 50px 50px;
  pointer-events: none;
}

.header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 20px 40px;
  background: linear-gradient(90deg, rgba(64, 158, 255, 0.1) 0%, rgba(64, 158, 255, 0.05) 100%);
  border-bottom: 1px solid rgba(64, 158, 255, 0.2);
  position: relative;
  margin-bottom: 0;
}

.header::after {
  content: '';
  position: absolute;
  bottom: 0;
  left: 0;
  right: 0;
  height: 1px;
  background: linear-gradient(90deg, transparent, rgba(64, 158, 255, 0.5), transparent);
}

.title-section h1 {
  margin: 0;
  font-size: 28px;
  color: #409EFF;
  text-shadow: 0 0 10px rgba(64, 158, 255, 0.3);
  letter-spacing: 1px;
}

.subtitle {
  color: #909399;
  font-size: 14px;
  margin-top: 5px;
  letter-spacing: 2px;
}

.time-section {
  text-align: right;
  background: rgba(64, 158, 255, 0.1);
  padding: 10px 20px;
  border-radius: 8px;
  border: 1px solid rgba(64, 158, 255, 0.2);
}

.current-time {
  font-size: 24px;
  color: #409EFF;
  font-weight: bold;
  text-shadow: 0 0 10px rgba(64, 158, 255, 0.3);
}

.current-date {
  color: #909399;
  font-size: 14px;
  margin-top: 5px;
}

.function-intro {
  background: rgba(64, 158, 255, 0.1);
  padding: 10px 20px; /* 减少上下高度 */
  border-radius: 8px;
  border: 1px solid rgba(64, 158, 255, 0.2);
  margin: 20px;
}

.function-intro h3 {
  color: #409EFF;
  margin-bottom: 10px;
}

.function-intro ul {
  list-style-type: none;
  padding: 0;
}

.function-intro li {
  margin-bottom: 5px;
}

.main-content {
  display: grid;
  grid-template-columns: 200px 1fr 320px;
  gap: 20px;
  height: calc(100vh - 220px);
  padding: 20px;
  position: relative;
}

.control-panel {
  background: linear-gradient(135deg, rgba(64, 158, 255, 0.1) 0%, rgba(64, 158, 255, 0.05) 100%);
  border-radius: 10px;
  padding: 20px;
  border: 1px solid rgba(64, 158, 255, 0.2);
  box-shadow: 0 0 20px rgba(64, 158, 255, 0.1);
  backdrop-filter: blur(10px);
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
}

.graph {
  width: 100%;
  height: 100%;
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

.info-item {
  margin-bottom: 15px;
  background: rgba(0, 0, 0, 0.2);
  padding: 10px;
  border-radius: 6px;
  border: 1px solid rgba(64, 158, 255, 0.1);
}

.label {
  color: #909399;
  margin-right: 10px;
  font-size: 14px;
}

.value {
  color: #409EFF;
  font-weight: 500;
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

:deep(.el-button) {
  background: linear-gradient(90deg, rgba(64, 158, 255, 0.2) 0%, rgba(64, 158, 255, 0.1) 100%);
  border: 1px solid rgba(64, 158, 255, 0.3);
  color: #409EFF;
  transition: all 0.3s ease;
}

:deep(.el-button:hover) {
  background: linear-gradient(90deg, rgba(64, 158, 255, 0.3) 0%, rgba(64, 158, 255, 0.2) 100%);
  transform: translateY(-2px);
  box-shadow: 0 5px 15px rgba(64, 158, 255, 0.2);
}

:deep(.el-button-group .el-button) {
  border-radius: 0;
}

:deep(.el-button-group .el-button:first-child) {
  border-top-left-radius: 4px;
  border-bottom-left-radius: 4px;
}

:deep(.el-button-group .el-button:last-child) {
  border-top-right-radius: 4px;
  border-bottom-right-radius: 4px;
}
</style>