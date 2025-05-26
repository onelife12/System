<template>
  <el-container style="height:100vh;background-color:#f0f8ff;">
    <el-header class="header-bar">
      <h1>系统状态评估指标体系</h1>
      <div class="steps-wrapper">
        <el-steps :active="stepActive" :space="100" size="small"
                  process-status="process" finish-status="success" style="width:100%">
          <el-step title="数据载入" />
          <el-step title="特征提取" />
          <el-step title="模型推理" />
          <el-step title="评估指标" />
        </el-steps>
        <span v-if="stepPercent>0" class="percent-tag">{{ stepPercent }}%</span>
      </div>
    </el-header>

    <el-main>
      <el-card class="intro-card" shadow="hover" style="margin-bottom:20px;">
        <template #header>📝 功能简介</template>
        <p>
          流程概览：加载飞控系统故障数据 → 预处理（标签映射 + BERT Tokenizer 编码）
          → 加载并初始化已训练模型 → 模型推理 → 输出状态等级
          <strong>轻微 / 中等 / 严重 / 危急</strong> 及其概率。
        </p>
      </el-card>

      <el-row :gutter="20">
        <el-col :span="16">
          <el-card class="summary-card" shadow="hover" style="margin-bottom:20px;">
            <template #header>🧩 本批次结论摘要</template>
            <el-descriptions :column="1" border size="small">
              <el-descriptions-item label="样本数">{{ stats.total }}</el-descriptions-item>
              <el-descriptions-item label="最高状态等级">
                <el-tag type="danger">{{ stats.most_risky.predicted_label }}</el-tag>
              </el-descriptions-item>
            </el-descriptions>
          </el-card>

          <el-select v-model="selectedFile" placeholder="选择分类结果文件"
                     style="width:320px;margin-bottom:20px;" filterable @change="fetchResult">
            <el-option v-for="f in availableFiles" :key="f"
                       :label="formatFileLabel(f)" :value="f" />
          </el-select>

          <el-card v-for="(item,index) in results" :key="index"
                   class="result-card" shadow="hover">
            <template #header>📌 第 {{ index+1 }} 条文本</template>
            <div class="input-text">
              ✈️ <span class="highlight">{{ item.input }}</span>
            </div>

            <div class="label-section">
              当前系统状态等级为：
              <el-tag size="large" effect="dark">{{ item.predicted_label }}</el-tag>

            </div>

            <v-chart :option="getPieOption(item.probabilities)"
                     style="height:240px;width:100%;margin-top:12px;" />
          </el-card>
        </el-col>

        <el-col :span="8">
          <el-card class="matrix-card" shadow="hover" style="margin-bottom:20px;">
            <h3>🎯 混淆矩阵</h3>
            <div class="zoom-container" :class="{zoomed:isZoomed}"
                 @click="toggleZoom" @mousedown="startDrag"
                 @mousemove="onDrag" @mouseup="endDrag" @mouseleave="endDrag">
              <img ref="matrixImage" src="assets/confusion.png"
                   class="zoomable-image" :style="imageStyle" />
            </div>
          </el-card>
        </el-col>
      </el-row>
    </el-main>
  </el-container>
</template>

<script setup>
import { ref, computed, onMounted, onUnmounted } from 'vue'
import { graphService } from '@/service/graph'
import VChart from 'vue-echarts'
import { use } from 'echarts/core'
import { PieChart } from 'echarts/charts'
import { TitleComponent, TooltipComponent, LegendComponent } from 'echarts/components'
import { CanvasRenderer } from 'echarts/renderers'

use([PieChart, TitleComponent, TooltipComponent, LegendComponent, CanvasRenderer])

const results = ref([])
const availableFiles = ref([])
const selectedFile = ref('result_latest.json')
const stepActive = ref(0)
const stepPercent = ref(0)
const stepStatus = ref('process')
const stats = ref({ total: 0, most_risky: { predicted_label: '无' } })
const logs = ref([])
const logFilter = ref('')
const filteredLogs = computed(() => logs.value.filter(l => l.msg.includes(logFilter.value)))

function formatFileLabel(fn) {
  if (fn === 'result_latest.json') return '最新结果'
  const m = fn.match(/result_(\d{8})_(\d{6})/)
  if (!m) return fn
  const [_, d, t] = m
  return `${d.slice(4,6)}月${d.slice(6,8)}日 ${t.slice(0,2)}:${t.slice(2,4)}:${t.slice(4,6)}`
}
function getPieOption(probs) {
  return {
    tooltip: { trigger: 'item', formatter: '{b}: {d}%' },
    legend: { orient: 'vertical', left: 'left' },
    series: [{
      type: 'pie',
      radius: '60%',
      data: Object.entries(probs).map(([k, v]) => ({ name: k, value: v }))
    }]
  }
}

let mockTimer = null
function startMock() {
  if (mockTimer) return
  mockTimer = setInterval(() => {
    stepPercent.value = Math.min(stepPercent.value + 8, 99)
    stepActive.value = Math.floor(stepPercent.value / 25)
  }, 800)
}
function finishProgress() {
  stepActive.value = 3
  stepPercent.value = 100
  stepStatus.value = 'success'
  clearInterval(mockTimer)
  mockTimer = null
}

async function fetchResult() {
  try {
    const data = await graphService.getResult(selectedFile.value)
    if (data.status === 'success') {
      results.value = data.data
      stats.value = data.stats || stats.value
      finishProgress()
    }
  } catch {
    mockResult()
  }
}

async function fetchFiles() {
  try {
    const data = await graphService.getResultFiles()
    if (data.status === 'success') availableFiles.value = data.files
  } catch {
    availableFiles.value = ['result_latest.json']
  }
}

async function fetchLogs() {
  try {
    const data = await graphService.getLogs()
    if (data.status === 'success') logs.value = data.data
  } catch {}
}

function mockResult() {
  results.value = [{
    input: '传感器异常',
    predicted_label: '中等',
    probabilities: { 轻微: 15, 中等: 55, 严重: 20, 危急: 10 }
  }]
  stats.value = { total: 1, most_risky: { predicted_label: '中等' } }
  finishProgress()
}

const isZoomed = ref(false)
const dragging = ref(false)
const offset = ref({ x: 0, y: 0 })
const startPos = ref({ x: 0, y: 0 })

const imageStyle = computed(() => isZoomed.value
    ? { transform: `scale(2) translate(${offset.value.x / 2}px, ${offset.value.y / 2}px)`, cursor: dragging.value ? 'grabbing' : 'grab' }
    : { transform: 'scale(1)', cursor: 'zoom-in' })

function toggleZoom() {
  isZoomed.value = !isZoomed.value
  if (!isZoomed.value) offset.value = { x: 0, y: 0 }
}
function startDrag(e) {
  if (!isZoomed.value) return
  dragging.value = true
  startPos.value = { x: e.clientX - offset.value.x, y: e.clientY - offset.value.y }
}
function onDrag(e) {
  if (!dragging.value) return
  offset.value = { x: e.clientX - startPos.value.x, y: e.clientY - startPos.value.y }
}
function endDrag() { dragging.value = false }

let interval = null
onMounted(() => {
  startMock()
  fetchFiles()
  fetchResult()
  fetchLogs()
  interval = setInterval(() => {
    fetchLogs()
    if (selectedFile.value === 'result_latest.json') fetchResult()
  }, 5000)
})
onUnmounted(() => {
  clearInterval(interval)
  if (mockTimer) clearInterval(mockTimer)
})
</script>

<style scoped>
.header-bar {
  background: #005fa3;
  color: #fff;
  padding: 8px 16px;
  position: sticky;
  top: 0;
  z-index: 1000;
  display: flex;
  align-items: center;
}
.header-bar h1 { margin: 0; font-size: 22px; font-weight: 600 }
.steps-wrapper {
  margin-left: auto;
  display: flex;
  align-items: center;
  gap: 8px;
  width: 360px;
  transform: scale(.8);
  transform-origin: top right;
}
.percent-tag { color: #fff; font-weight: 600; font-size: 14px }

.intro-card p { margin: 0; line-height: 1.6; font-size: 15px; color: #444 }
.summary-card { background: #eef9ff }
.result-card {
  margin-bottom: 20px;
  border-radius: 10px;
  background: #fff;
  box-shadow: 0 2px 6px rgba(0,0,0,.1)
}
.input-text { font-size: 16px; margin: 8px 0; color: #333 }
.input-text .highlight {
  font-weight: 700;
  color: #005fa3;
  background: #e0f3ff;
  padding: 2px 6px;
  border-radius: 4px;
}
.label-section { margin-top: 10px; font-size: 18px }
.matrix-card {
  background: #fff8ec;
  border-radius: 10px;
  padding: 20px;
  text-align: center;
}
.zoom-container {
  overflow: hidden;
  width: 100%;
  max-height: 450px;
  user-select: none;
  cursor: pointer;
}
.zoomable-image {
  transition: transform .3s ease;
  max-width: 100%;
}
.time { color: #888; margin-right: 6px; font-size: 12px }
.level-info { color: #444 }
.level-warn { color: #d97c0b }
.level-error { color: #d93025; font-weight: 700 }
</style>
