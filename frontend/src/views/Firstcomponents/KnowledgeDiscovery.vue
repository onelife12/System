<template>
    <div class="app">
        <!-- 顶部：按钮 + 信息提示 + 统计信息 -->
        <el-row align="middle" gutter="16" class="header-row">
            <el-col>
                <el-tooltip
                    content="基于知识图谱进行链接预测，发现弱关联知识"
                    placement="right"
                >
                    <el-button type="primary" @click="fetchKnowledgeResults">
                        关联知识发现
                    </el-button>
                </el-tooltip>
            </el-col>
            <el-col>
                <el-descriptions
                    title="关联知识发现统计"
                    :column="3"
                    size="small"
                    border
                >
                    <el-descriptions-item label="总关联词条对数">
                        {{ totalCount }}
                    </el-descriptions-item>
                    <el-descriptions-item label="预测正确关联数">
                        {{ correctCount }}
                    </el-descriptions-item>
                    <el-descriptions-item label="准确率">
                        {{ accuracy.toFixed(2) }}%
                    </el-descriptions-item>
                </el-descriptions>
            </el-col>
        </el-row>

        <!-- 主体：左表格 + 右饼图 -->
        <el-row gutter="16" style="margin-top: 20px;">
            <!-- 左侧：数据表格 -->
            <el-col :span="14">
                <el-table
                    v-if="paginatedData.length"
                    :data="paginatedData"
                    stripe
                    border
                    highlight-current-row
                    @current-change="onRowChange"
                    style="width: 100%;"
                >
                    <el-table-column
                        type="index"
                        label="编号"
                        width="80"
                        :index="(idx) => (currentPage - 1) * pageSize + idx + 1"
                    />
                    <el-table-column label="缺失三元组" width="180">
                        <template #default="{ row }">
                            <el-tooltip
                                effect="dark"
                                :content="formatTriple(row.input_triple)"
                                placement="top"
                            >
                                {{ formatTriple(row.input_triple) }}
                            </el-tooltip>
                        </template>
                    </el-table-column>
                    <el-table-column prop="gold_entity" label="标准答案" width="150" />
                    <el-table-column label="前十个预测结果" width="400">
                        <template #default="{ row }">
                            <div>
                                <div
                                    v-for="(pred, idx) in row.predictions.slice(0, 10)"
                                    :key="idx"
                                    :style="{ fontWeight: pred.split(' — ')[0] === row.gold_entity ? 'bold' : 'normal' }"
                                >
                                    {{ idx + 1 }}. {{ pred.split(' — ')[0] }}
                                </div>
                            </div>
                        </template>
                    </el-table-column>
                </el-table>

                <el-table
                    v-else
                    :data="[]"
                    empty-text='暂无预测结果，请点击"关联知识发现"'
                />

                <el-pagination
                    v-if="totalCount > pageSize"
                    background
                    layout="prev, pager, next, jumper, ->, total"
                    :page-size="pageSize"
                    :current-page="currentPage"
                    :total="totalCount"
                    @current-change="onPageChange"
                    style="margin-top: 16px; text-align: right;"
                />
            </el-col>

            <!-- 右侧：固定饼图容器 -->
            <el-col :span="10">
                <div class="chart-container" v-if="currentRow">
                    <div class="chart-title">知识发现预测结果</div>
                    <div class="chart-header">
                        <div>
                            <strong>缺失三元组：</strong>
                            {{ formatTriple(currentRow.input_triple) }}
                        </div>
                        <div style="margin-top: 8px;">
                            <strong>标准答案：</strong>
                            <span style="font-weight: bold">{{ currentRow.gold_entity }}</span>
                        </div>
                    </div>
                    <v-chart
                        :option="chartOptions"
                        autoresize
                        class="prediction-pie"
                    />
                </div>
            </el-col>
        </el-row>
    </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from 'vue'
import axios from 'axios'
import {
    ElRow,
    ElCol,
    ElButton,
    ElTooltip,
    ElTable,
    ElTableColumn,
    ElPagination,
    ElDescriptions,
    ElDescriptionsItem
} from 'element-plus'
import { use } from 'echarts/core'
import VChart from 'vue-echarts'
import { PieChart } from 'echarts/charts'
import { TitleComponent, TooltipComponent } from 'echarts/components'
import { CanvasRenderer } from 'echarts/renderers'
import { graphService } from "@/service/graph"

// 注册 ECharts 模块
use([PieChart, TitleComponent, TooltipComponent, CanvasRenderer])

const tableData = ref([])
const currentPage = ref(1)
const pageSize = 50
const currentRow = ref(null)

const paginatedData = computed(() => {
    const start = (currentPage.value - 1) * pageSize
    return tableData.value.slice(start, start + pageSize)
})

const correctCount = computed(() =>
    tableData.value.reduce(
        (cnt, row) =>
            row.predictions.slice(0, 10)
                .map(p => p.split(' — ')[0])
                .includes(row.gold_entity)
                ? cnt + 1
                : cnt,
        0
    )
)
const totalCount = computed(() => tableData.value.length)
const accuracy = computed(() =>
    totalCount.value > 0 ? (correctCount.value / totalCount.value) * 100 : 0
)

const chartOptions = reactive({
    tooltip: {
        trigger: 'item',
        formatter: '{b}: {d}%'
    },
    legend: { show: false },
    series: [
        {
            name: '预测概率分布',
            type: 'pie',
            radius: '60%',
            label: {
                show: true,
                position: 'outside',
                formatter: '{b}: {d}%'
            },
            data: []
        }
    ]
})

async function fetchKnowledgeResults() {
    try {
        const { data } = await axios.post('http://localhost:5000/knowledge_discovery')
        // const data = await graphService.getWeaklyRelatedKnowledge()
        if (Array.isArray(data)) {
            tableData.value = data
            currentPage.value = 1
            if (data.length) onRowChange(data[0])
        }
    } catch (err) {
        console.error('获取数据失败：', err)
    }
}

function onRowChange(row) {
    currentRow.value = row || null
    if (!row || !Array.isArray(row.predictions)) {
        chartOptions.series[0].data = []
        return
    }

    // 前十条：解析 name 和 score
    const top10 = row.predictions.slice(0, 10).map(item => {
        const [name, sc] = item.split(' — ')
        return { name, score: parseFloat(sc) }
    })

    // softmax 转为概率
    const exps = top10.map(p => Math.exp(p.score))
    const sumExp = exps.reduce((a, b) => a + b, 0)
    const probs = exps.map(e => (e / sumExp) * 100)

    // 构建饼图数据，标准答案加粗
    chartOptions.series[0].data = top10.map((p, i) => ({
        name: p.name,
        value: parseFloat(probs[i].toFixed(2)),
        label: {
            fontWeight: p.name === row.gold_entity ? 'bold' : 'normal'
        }
    }))
}

function onPageChange(page) {
    currentPage.value = page
    currentRow.value = null
    chartOptions.series[0].data = []
}

const formatTriple = arr => (Array.isArray(arr) ? arr.join(' → ') : '')

// Add mock data on component mount
onMounted(() => {
    const mockData = [{
        input_triple: ['飞机', '组成部分', '?'],
        gold_entity: '机翼',
        predictions: [
            '机翼 — 0.90',
            '发动机 — 0.78',
            '机身 — 0.70',
            '尾翼 — 0.60',
            '起落架 — 0.55',
            '螺旋桨 — 0.40',
            '驾驶舱 — 0.30',
            '襟翼 — 0.20',
            '副翼 — 0.10',
            '安定面 — 0.05'
        ]
    },{
        input_triple: ['波音747', '制造商', '?'],
        gold_entity: '波音',
        predictions: [
            '波音 — 0.95',
            '空客 — 0.50',
            '麦道 — 0.30',
            '伊尔库特 — 0.10',
            '图波列夫 — 0.05'
        ]
    }]

    tableData.value = mockData
    if (mockData.length) {
        onRowChange(mockData[0])
    }
})
</script>

<style scoped>
.app {
    padding: 20px;
    background: #fff;
    min-height: 100vh;
}

.header-row {
    margin-bottom: 20px;
    background: #f5f7fa;
    padding: 15px;
    border-radius: 8px;
    border: 1px solid #e4e7ed;
}

.chart-container {
    position: sticky;
    top: 20px;
    background: #fff;
    padding: 12px;
    border: 1px solid #e4e7ed;
    border-radius: 4px;
    box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.1);
}

.chart-title {
    text-align: center;
    font-size: 16px;
    font-weight: bold;
    margin-bottom: 12px;
    color: #303133;
}

.chart-header {
    margin-bottom: 16px;
    font-size: 14px;
    line-height: 1.5;
    color: #606266;
}

.chart-header strong {
    color: #303133;
}

.prediction-pie {
    width: 100%;
    height: 400px;
}

:deep(.el-table) {
    background-color: #fff;
}

:deep(.el-table th) {
    background-color: #f5f7fa;
    color: #303133;
}

:deep(.el-table td) {
    color: #606266;
}

:deep(.el-descriptions) {
    background-color: #fff;
}

:deep(.el-descriptions__title) {
    color: #303133;
}

:deep(.el-descriptions__label) {
    color: #606266;
    background-color: #f5f7fa;
}

:deep(.el-descriptions__content) {
    color: #303133;
    background-color: #fff;
}

:deep(.el-button--primary) {
    background-color: #409EFF;
    border-color: #409EFF;
    color: #fff;
}

:deep(.el-button--primary:hover) {
    background-color: #66b1ff;
    border-color: #66b1ff;
}

:deep(.el-pagination) {
    background-color: #fff;
}

:deep(.el-pagination .el-pagination__total),
:deep(.el-pagination .el-pagination__jump) {
    color: #606266;
}

:deep(.el-pagination .btn-prev),
:deep(.el-pagination .btn-next) {
    background-color: #fff;
    color: #303133;
    border: 1px solid #dcdfe6;
}

:deep(.el-pagination .el-pager li) {
    background-color: #fff;
    color: #303133;
    border: 1px solid #dcdfe6;
}

:deep(.el-pagination .el-pager li.active) {
    background-color: #409EFF;
    color: #fff;
    border-color: #409EFF;
}

:deep(.el-pagination .el-pager li:hover) {
    color: #409EFF;
    border-color: #409EFF;
}

:deep(.el-pagination .el-pager li.active:hover) {
    color: #fff;
    border-color: #409EFF;
}
</style> 