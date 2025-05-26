<template>
  <div class="dashboard-container">
    <!-- 顶部标题栏 -->
    <div class="header">
      <div class="title-section">
        <h1>系统状态传播</h1>
        <div class="subtitle">System State Propagation</div>
      </div>
    </div>

    <!-- 功能简介区域 -->
    <div class="function-intro">
      <h3>功能简介</h3>
      <ul>
        <li>1、加载系统状态信息</li>
        <li>2、加载状态推理模型</li>
        <li>3、推理系统状态的下一状态</li>
        <li>4、得出系统状态传播链路</li>
      </ul>
    </div>

    <!-- 主要内容区域 -->
    <div class="main-content">
      <!-- 左侧控制面板 -->
      <div class="control-panel">
        <div class="panel-section">
          <el-button type="primary" @click="getTransportationPath" class="control-btn">
            <el-icon><DataAnalysis /></el-icon>
            状态传播链路
          </el-button>
        </div>
      </div>

      <!-- 中间图谱展示区 -->
      <div class="graph-container">
        <div id="transportation-echarts-graph" class="graph"></div>
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
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, onUnmounted } from 'vue';
import * as echarts from 'echarts';
import { graphService } from '@/service/graph';
import { DataAnalysis, ZoomIn, ZoomOut, Refresh } from '@element-plus/icons-vue';

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

// 初始化图表
const initECharts = () => {
  chart.value = echarts.init(document.getElementById('transportation-echarts-graph'));

  const option = {
    backgroundColor: 'transparent',
    tooltip: {
      trigger: 'item',
      formatter: function (params) {
        if (params.dataType === 'edge') {
          return `关系: ${params.data.label.formatter}`;
        }
        return `${params.name} (${params.value})`;
      },
      backgroundColor: 'rgba(0,0,0,0.7)',
      borderColor: '#409EFF',
      textStyle: {
        color: '#fff'
      }
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
          formatter: '{b}',
          color: '#fff'
        },
        lineStyle: {
          color: 'source',
          curveness: 0, // 设置边为直线
          width: 2
        },
        edgeSymbol: ['none', 'arrow'], // 添加指示箭头
        edgeSymbolSize: 10,
        edgeLabel: {
          show: true,
          formatter: '{c}',
          color: '#fff'
        },
        itemStyle: {
          borderColor: '#fff',
          borderWidth: 2
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
            formatter: edge.type,
            color: '#fff'
          },
          symbol: ['none', 'arrow'] // 边添加箭头
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

// 图表控制
const zoomIn = () => {
  const option = chart.value.getOption();
  const zoom = option.series[0].zoom || 1;
  chart.value.setOption({
    series: [
      {
        zoom: zoom * 1.2
      }
    ]
  });
};

const zoomOut = () => {
  const option = chart.value.getOption();
  const zoom = option.series[0].zoom || 1;
  chart.value.setOption({
    series: [
      {
        zoom: zoom * 0.8
      }
    ]
  });
};

const resetView = () => {
  chart.value.setOption({
    series: [
      {
        zoom: 1
      }
    ]
  });
};

// 生命周期钩子
onMounted(() => {
  initECharts();
  onUnmounted(() => {
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
  grid-template-columns: 200px 1fr;
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