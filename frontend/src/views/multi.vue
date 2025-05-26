<template>
  <div class="page-container">

    <!-- 上部分：表格数据展示 -->
    <div class="top-section">
      <el-card>
        <template #header>
          <div class="card-header">
            <span>📌 系统诱发因素</span>
            <el-tooltip
                content="展示系统状态诱发因素提取结果"
                placement="top"
                effect="dark"
            >
              <el-icon class="info-icon"><InfoFilled /></el-icon>
            </el-tooltip>
          </div>
        </template>

        <div class="toolbar">
          <el-select v-model="selectedTypes" multiple placeholder="故障类型" class="big-select" style="width:220px" @change="refresh">
            <el-option v-for="t in typeOptions" :key="t" :label="t" :value="t" />
          </el-select>

          <el-select v-model="selectedStatuses" multiple placeholder="飞行状态" class="big-select" style="width:150px;margin-left:4px" @change="refresh">
            <el-option v-for="s in statusOptions" :key="s" :label="s" :value="s" />
          </el-select>

<!--          <el-input v-model.trim="keyword" placeholder="搜索故障现象 / 诊断…" clearable-->
<!--                    style="width:260px;margin-left:4px" @input="refresh">-->
<!--            <template #prefix><i class="el-icon-search" /></template>-->
<!--          </el-input>-->
        </div>

        <el-table :data="pagedData" class="big-table" border height="400" stripe>
          <el-table-column type="index" label="#" width="60" />
          <el-table-column prop="aircraftStatus" label="飞行状态" width="120" sortable />
          <el-table-column prop="faultDiagnosis" label="故障现象" width="160" sortable />
          <el-table-column prop="faultLocation" label="故障部位" width="150" sortable />
          <el-table-column prop="faultDiagnosis" label="故障诊断" />
          <el-table-column prop="treatmentMeasures" label="处理措施" />
          <el-table-column prop="faultType" label="故障类型" width="150">
            <template #default="{ row }">
              <el-tag :type="tagType(row.faultType)">{{ row.faultType || '未分类' }}</el-tag>
            </template>
          </el-table-column>
        </el-table>

        <!-- 分页 -->
        <el-pagination class="big-pagination" :current-page="currentPage"
                       :page-sizes="[5,10,15,20]" :page-size="pageSize"
                       layout="total, sizes, prev, pager, next, jumper"
                       :total="filteredData.length"
                       @size-change="sizeChange" @current-change="pageChange"/>
      </el-card>

    </div>

    <!-- 下部分：左右布局 -->
    <div class="bottom-section">
      <!-- 左半部分：指标体系 -->
      <div class="left-section">
        <el-card v-for="(item, index) in results" :key="index"
                 class="result-card" shadow="hover">
          <template #header>
            <div class="card-header">
              <span>📌 系统状态评估</span>
              <el-tooltip
                content="根据系统所处状态，进行系统状态评估，展示系统状态评估结果"
                placement="top"
                effect="dark"
              >
                <el-icon class="info-icon"><InfoFilled /></el-icon>
              </el-tooltip>
            </div>
          </template>
          <div class="input-text">
            ✈️ 系统状态：<span class="highlight">{{ item.input }}</span>
          </div>

          <div class="label-section">
            当前系统状态评估为：
            <el-tag size="large" effect="dark">{{ item.predicted_label }}</el-tag>
          </div>

          <v-chart :option="getPieOption(item.probabilities)"
                   style="height:240px;width:100%;margin-top:12px;" />
        </el-card>
      </div>

      <!-- 右半部分：风险推理 -->
      <div class="right-section">
        <el-card v-for="(row, idx) in shownResults" :key="idx"
                 class="result-card" shadow="hover">
          <template #header>
            <div class="card-header">
              <span>📌 飞行器故障风险推理</span>
              <el-tooltip
                content="根据飞行器发生故障，展示故障风险推理结果"
                placement="top"
                effect="dark"
              >
                <el-icon class="info-icon"><InfoFilled /></el-icon>
              </el-tooltip>
            </div>
          </template>
          <div class="input-text">
            ✈️ 飞行器故障现象：<span class="highlight">{{ row.input }}</span>
          </div>

          <div class="result-text">
            当前系统的风险为
            <el-tag style="margin-left:8px" type="warning" size="small">
              {{ mostProbLabel(row) }}
            </el-tag>
          </div>

          <v-chart :option="pieOption(row)"
                   autoresize
                   @mouseover="onHover"
                   @mouseout="onLeave"
                   @click="openDrawer"
                   style="height: 300px" />
        </el-card>
      </div>
    </div>

    <!-- Drawer 展示 -->
    <el-drawer v-model="drawer.show" :title="drawer.title" size="30%">
      <p style="font-weight:bold;margin-bottom:8px">
        概率：{{ drawer.percent }} %
      </p>
      <v-chart v-if="drawer.row"
               :option="barOption(drawer.row)"
               autoresize
               style="height:260px" />
    </el-drawer>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import VChart from 'vue-echarts'
import { use } from 'echarts/core'
import { PieChart, BarChart } from 'echarts/charts'
import { TitleComponent, TooltipComponent, LegendComponent, GridComponent } from 'echarts/components'
import { CanvasRenderer } from 'echarts/renderers'
import { InfoFilled } from '@element-plus/icons-vue'
import { ElMessage } from 'element-plus'

use([PieChart, BarChart, TitleComponent, TooltipComponent, LegendComponent, GridComponent, CanvasRenderer])
import {graphService} from "@/service/graph.js";
// 查询表单数据
const queryForm = ref({
  dateRange: [],
  status: '',
  faultType: '',
  severity: ''
})

// 查询和重置方法
const handleQuery = () => {
  // 实现查询逻辑
  console.log('查询条件：', queryForm.value)
}

const resetQuery = () => {
  queryForm.value = {
    dateRange: [],
    status: '',
    faultType: '',
    severity: ''
  }
}

// 表格数据
const tableData = ref([
  {
    aircraftStatus: '巡航',
    faultDiagnosis: '发动机油压持续下降',
    faultLocation: '发动机系统',
    treatmentMeasures: '检查油路系统，更换机油滤芯',
    faultType: '动力系统故障'
  },
  {
    aircraftStatus: '起飞',
    faultDiagnosis: '起落架无法完全收起',
    faultLocation: '起落架系统',
    treatmentMeasures: '检查液压系统，更换密封圈',
    faultType: '机体结构故障'
  }
])

// 指标体系数据
const results = ref([]);

// 获取评估结果
const fetchResults = async () => {
    try {
        const response = await graphService.getResult();

        if (response && response.data) {
            results.value = [{
                input: response.data.fault,
                predicted_label: response.data.predicted_label,
                probabilities: response.data.probabilities
            }];
        }
    } catch (error) {

        console.error('获取评估结果失败:', error);
        ElMessage.error('获取评估结果失败');

    }
};

// 在组件挂载时获取数据
onMounted(() => {
    fetchResults();
});

// 风险推理数据
const labels = [
  '动力源失效风险', '燃油系统故障风险', '航空电子系统故障风险', '机体结构故障风险',
  '执行部件失效风险', '传感器失效风险', '计算机故障风险', '机载设备故障风险'
]

const colorMap = {
  动力源失效风险: '#5470C6', 燃油系统故障风险: '#91CC75', 航空电子系统故障风险: '#FAC858',
  机体结构故障风险: '#EE6666', 执行部件失效风险: '#73C0DE', 传感器失效风险: '#3BA272',
  计算机故障风险: '#FC8452', 机载设备故障风险: '#9A60B4'
}

const raw = [
  {
    input: '发动机油压持续下降伴随异响',
    probabilities: {
      动力源失效风险: 0.45, 燃油系统故障风险: 0.15, 航空电子系统故障风险: 0.05,
      机体结构故障风险: 0.10, 执行部件失效风险: 0.05, 传感器失效风险: 0.05,
      计算机故障风险: 0.10, 机载设备故障风险: 0.05
    }
  }
]

const activeKeys = ref([...labels])
const shownResults = computed(() =>
  raw.map(r => {
    const filtered = Object.fromEntries(
      Object.entries(r.probabilities).filter(([k]) => activeKeys.value.includes(k))
    )
    return { ...r, probabilities: filtered }
  })
)

// 图表配置
const getPieOption = (probs) => ({
  tooltip: { trigger: 'item', formatter: '{b}: {d}%' },
  legend: { orient: 'vertical', left: 'left' },
  series: [{
    type: 'pie',
    radius: '60%',
    data: Object.entries(probs).map(([k, v]) => ({ name: k, value: v }))
  }]
})

const pieOption = row => ({
  tooltip: { trigger: 'item', formatter: '{b}: {d}%' },
  series: [{
    type: 'pie',
    radius: ['45%', '72%'],
    label: { formatter: '{b}: {d}%' },
    data: Object.entries(row.probabilities).map(([k, v]) => ({
      name: k,
      value: v,
      itemStyle: { color: colorMap[k] },
      rowRef: row
    }))
  }]
})

const barOption = row => ({
  grid: { left: 40, right: 20, top: 10, bottom: 30 },
  xAxis: { type: 'value', max: 1 },
  yAxis: { type: 'category', data: Object.keys(row.probabilities) },
  series: [{ type: 'bar', data: Object.values(row.probabilities), itemStyle: { color: '#409EFF' } }]
})

// 事件处理
function onHover(p) {
  if (p.componentSubType === 'pie') {
    p.event.event.target.parentNode.style.cursor = 'pointer'
  }
}

function onLeave() {}

const drawer = ref({ show: false, title: '', percent: 0, row: null })

const openDrawer = p => {
  drawer.value = {
    show: true,
    title: p.name,
    percent: Math.round(p.value * 100),
    row: p.data.rowRef
  }
}

const mostProbLabel = row => Object.entries(row.probabilities).sort((a, b) => b[1] - a[1])[0][0]

const selectedTypes = ref([])
const selectedStatuses = ref([])
const keyword = ref('')

const typeOptions = ref(['机体结构故障', '航空电子系统故障', '动力系统故障', '机载设备故障', '飞行控制系统故障'])
const statusOptions = ref(['起飞', '巡航', '降落', '地面'])

const tagType = (t) => {
  return {
    机体结构故障: 'info',
    航空电子系统故障: 'success',
    动力系统故障: 'danger',
    机载设备故障: 'warning',
    飞行控制系统故障: 'primary'
  }[t] || ''
}

/* ——— 内部状态 ——— */
const pageSize = ref(10)
const currentPage = ref(1)

/* ——— 过滤 + 分页 ——— */
const filteredData = computed(() => tableData.value.filter(item => {
  const typeOK = !selectedTypes.value.length || selectedTypes.value.includes(item.faultType)
  const statOK = !selectedStatuses.value.length || selectedStatuses.value.includes(item.aircraftStatus)
  const kw = keyword.value.trim()
  const kwOK = !kw || Object.values(item).some(v => String(v).includes(kw))
  return typeOK && statOK && kwOK
}))

const pagedData = computed(() => {
  const start = (currentPage.value - 1) * pageSize.value
  return filteredData.value.slice(start, start + pageSize.value)
})

/* ——— 辅助函数 ——— */
function refresh() { currentPage.value = 1 }
function sizeChange(sz) { pageSize.value = sz; refresh() }
function pageChange(p) { currentPage.value = p }
</script>

<style scoped>
.page-container {
  display: flex;
  flex-direction: column;
  min-height: 100vh;
  background: #f0f8ff;
  overflow: hidden;
}

.top-section {
  display: flex;
  flex-direction: column;
  padding: 20px;
  background: #fff;
  border-bottom: 1px solid #e0e0e0;
  height: 400px; /* 固定高度 */
  flex-shrink: 0; /* 防止压缩 */
}

.toolbar {
  display: flex;
  align-items: center;
  gap: 4px;
  padding: 6px 0;
  margin-bottom: 16px;
}

.big-table {
  flex: 1;
  overflow: hidden;
}

.big-table :deep(.el-table__body-wrapper) {
  overflow-y: auto;
  height: calc(100% - 40px);
}

.big-pagination {
  margin-top: 16px;
  --el-pagination-font-size: 14px;
}

.bottom-section {
  flex: 1;
  display: flex;
  padding: 20px;
  gap: 20px;
  background: #f0f8ff;
  min-height: 0; /* 允许内容区域收缩 */
}

.left-section,
.right-section {
  flex: 1;
  display: flex;
  flex-direction: column;
  gap: 20px;
  background: #fff;
  border-radius: 8px;
  padding: 20px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.08);
  min-height: 0; /* 允许内容区域收缩 */
}

.result-card {
  flex: 1;
  min-height: 0; /* 允许内容区域收缩 */
  display: flex;
  flex-direction: column;
}

.card-header {
  display: flex;
  align-items: center;
  gap: 8px;
  padding-bottom: 10px;
  border-bottom: 1px solid #ebeef5;
}

.info-icon {
  color: #909399;
  cursor: help;
  transition: all 0.3s ease;
}

.info-icon:hover {
  color: #409EFF;
  transform: scale(1.1);
}

.input-text {
  margin: 12px 0;
  font-size: 15px;
  color: #333;
}

.highlight {
  font-weight: 600;
  color: #005fa3;
  background: #e0f3ff;
  padding: 2px 6px;
  border-radius: 4px;
}

.label-section {
  margin-top: 16px;
  font-size: 18px;
  font-weight: 500;
}

.result-text {
  margin: 12px 0;
  font-size: 16px;
  color: #444;
}

.query-form {
  padding: 10px;
}

/* 响应式布局 */
@media (max-width: 1024px) {
  .top-section {
    flex-direction: column;
  }

  .query-card {
    width: 100%;
  }

  .bottom-section {
    flex-direction: column;
  }

  .left-section,
  .right-section {
    width: 100%;
  }
}

.toolbar {
  display: flex;
  align-items: center;
  gap: 4px;
  padding: 6px 0;
}

.big-select .el-input__inner {
  font-size: 14px;
}

.big-table .el-table__header th {
  font-size: 15px;
  font-weight: 600;
  background: #fafafa;
}

.big-table .el-table__body td {
  font-size: 14px;
}

.big-pagination {
  margin-top: 6px;
  --el-pagination-font-size: 14px;
}
</style>