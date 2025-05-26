<!-- riskReasoning.vue -->
<template>
  <el-container style="height: 100vh; background:#f0f8ff">
    <!-- 顶栏 -->
    <el-header style="background:#005fa3;color:#fff;display:flex;align-items:center;justify-content:space-between">
      <h1>飞行器故障风险归纳推理</h1>
      <el-button type="primary" icon="el-icon-download" @click="exportCsv" plain>导出 CSV</el-button>
    </el-header>

    <el-main>
      <!-- 推理流程说明 -->
      <el-card class="intro-card" shadow="hover" style="margin-bottom: 20px;">
        <template #header>📝 推理流程说明</template>
        <ol class="step-list">
          <li>加载飞行器故障信息</li>
          <li>加载归纳推理模型</li>
          <li>推理飞行器所发生故障的风险</li>
          <li>输出推理结果，给出风险等级及概率</li>
        </ol>
        <el-progress :percentage="100" :stroke-width="16" status="success" style="margin-top:10px"/>
      </el-card>

      <el-row :gutter="20">
        <!-- 左侧：推理结果列表 -->
        <el-col :span="16">
          <el-card
              v-for="(row, idx) in shownResults"
              :key="idx"
              class="result-card"
              shadow="hover"
          >
            <template #header>
              <strong>📌 推理结果 {{ idx + 1 }}</strong>
            </template>

            <!-- 输出描述 -->
            <div class="result-text">
              当前系统的风险为
              <el-tag style="margin-left:8px" type="warning" size="small">
                {{ mostProbLabel(row) }}
              </el-tag>
            </div>

            <!-- 故障文本 -->
            <div class="input-text">
              ✈️ 故障现象：<span class="highlight">{{ row.input }}</span>
            </div>

            <!-- 概率饼图 -->
            <v-chart
                :option="pieOption(row)"
                autoresize
                @mouseover="onHover"
                @mouseout="onLeave"
                @click="openDrawer"
                style="height: 300px"
            />
          </el-card>
        </el-col>

        <!-- 右侧：风险过滤 -->
        <el-col :span="8">
          <el-card class="side-card">
            <h3>📘 风险类别说明</h3>
            <el-checkbox-group v-model="activeKeys" size="small" @change="refresh">
              <el-checkbox
                  v-for="lab in labels"
                  :key="lab"
                  :label="lab"
                  style="margin:4px 0"
              >{{ lab }}</el-checkbox>
            </el-checkbox-group>
          </el-card>
        </el-col>
      </el-row>
    </el-main>

    <!-- Drawer 展示 -->
    <el-drawer v-model="drawer.show" :title="drawer.title" size="30%">
      <p style="font-weight:bold;margin-bottom:8px">
        概率：{{ drawer.percent }} %
      </p>
      <v-chart
          v-if="drawer.row"
          :option="barOption(drawer.row)"
          autoresize
          style="height:260px"
      />
    </el-drawer>
  </el-container>
</template>

<script setup>
import { ref, computed } from 'vue'
import VChart from 'vue-echarts'
import { use } from 'echarts/core'
import { PieChart, BarChart } from 'echarts/charts'
import { TitleComponent, TooltipComponent, LegendComponent, GridComponent } from 'echarts/components'
import { CanvasRenderer } from 'echarts/renderers'
import { ElMessage } from 'element-plus'
use([PieChart, BarChart, TitleComponent, TooltipComponent, LegendComponent, GridComponent, CanvasRenderer])

const labels = [
  '动力源失效风险','燃油系统故障风险','航空电子系统故障风险','机体结构故障风险',
  '执行部件失效风险','传感器失效风险','计算机故障风险','机载设备故障风险'
]
const colorMap = {
  动力源失效风险:'#5470C6',  燃油系统故障风险:'#91CC75',   航空电子系统故障风险:'#FAC858',
  机体结构故障风险:'#EE6666', 执行部件失效风险:'#73C0DE',  传感器失效风险:'#3BA272',
  计算机故障风险:'#FC8452',   机载设备故障风险:'#9A60B4'
}

const raw = [
  {
    input:'发动机油压持续下降伴随异响',
    probabilities:{
      动力源失效风险:0.45,燃油系统故障风险:0.15,航空电子系统故障风险:0.05,
      机体结构故障风险:0.10,执行部件失效风险:0.05,传感器失效风险:0.05,
      计算机故障风险:0.10,机载设备故障风险:0.05
    }
  },
  {
    input:'后机身监测到多点裂纹扩展',
    probabilities:{
      动力源失效风险:0.05,燃油系统故障风险:0.10,航空电子系统故障风险:0.05,
      机体结构故障风险:0.55,执行部件失效风险:0.10,传感器失效风险:0.05,
      计算机故障风险:0.05,机载设备故障风险:0.05
    }
  }
]

const activeKeys = ref([...labels])
const shownResults = computed(() =>
    raw.map(r => {
      const filtered = Object.fromEntries(Object.entries(r.probabilities).filter(([k]) => activeKeys.value.includes(k)))
      return { ...r, probabilities: filtered }
    })
)
const refresh = () => {}

const pieOption = row => ({
  tooltip: { trigger: 'item', formatter: '{b}: {d}%' },
  series: [{
    type: 'pie', radius: ['45%', '72%'], label: { formatter: '{b}: {d}%' },
    data: Object.entries(row.probabilities).map(([k, v]) => ({
      name: k, value: v, itemStyle: { color: colorMap[k] }, rowRef: row
    }))
  }]
})
const barOption = row => ({
  grid: { left: 40, right: 20, top: 10, bottom: 30 },
  xAxis: { type: 'value', max: 1 },
  yAxis: { type: 'category', data: Object.keys(row.probabilities) },
  series: [{ type: 'bar', data: Object.values(row.probabilities), itemStyle: { color: '#409EFF' } }]
})

function onHover(p) {
  if (p.componentSubType === 'pie') p.event.event.target.parentNode.style.cursor = 'pointer'
}
function onLeave() {}
const drawer = ref({ show: false, title: '', percent: 0, row: null })
const openDrawer = p => {
  drawer.value = { show: true, title: p.name, percent: Math.round(p.value * 100), row: p.data.rowRef }
}
const mostProbLabel = row => Object.entries(row.probabilities).sort((a, b) => b[1] - a[1])[0][0]

function exportCsv() {
  const lines = [['样本编号', ...labels]]
  raw.forEach((r, i) => {
    const line = [`样本${i + 1}`]
    labels.forEach(l => line.push(r.probabilities[l] ?? 0))
    lines.push(line)
  })
  const blob = new Blob([lines.map(l => l.join(',')).join('\\n')], { type: 'text/csv' })
  const url = URL.createObjectURL(blob)
  const a = document.createElement('a'); a.href = url; a.download = 'risk_reasoning.csv'; a.click()
  URL.revokeObjectURL(url)
  ElMessage.success('CSV 已下载')
}
</script>

<style scoped>
.result-card {
  margin-bottom: 24px;
  border-radius: 10px;
  background: #fff;
  box-shadow: 0 2px 6px rgba(0,0,0,.08);
}
.side-card {
  background: #fefefe;
  border-radius: 10px;
  padding: 20px;
}
.input-text {
  margin: 8px 0;
  font-size: 15px;
  color: #333;
}
.highlight {
  font-weight: bold;
  color: #005fa3;
  background: #e0f3ff;
  padding: 1px 4px;
  border-radius: 3px;
}
.result-text {
  margin: 8px 0;
  font-size: 16px;
  color: #444;
}
.step-list {
  padding-left: 20px;
  margin: 0;
  line-height: 1.8;
  color: #333;
  font-size: 15px;
}
</style>
