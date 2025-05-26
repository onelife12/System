<template>
    <div class="container">
        <el-card class="header-card">
            <template #header>
                <h1>飞机飞行信息</h1>
            </template>
            <div class="info-section">
                <div class="info-item">
                    <span class="info-label">型号</span>
                    <span class="info-value">{{ flightData.model }}</span>
                </div>
                <div class="info-item">
                    <span class="info-label">飞行阶段</span>
                    <span class="info-value">{{ flightData.phase }}</span>
                </div>
                <div class="info-item">
                    <span class="info-label">FCS 状态</span>
                    <span class="info-value">{{ flightData.fcs_status }}</span>
                </div>
                <div class="info-item">
                    <span class="info-label">环境</span>
                    <span class="info-value">{{ flightData.environment }}</span>
                </div>
            </div>
        </el-card>
        <el-card class="param-card">
            <template #header>
                <h2>飞行参数</h2>
            </template>
            <div class="param-value">{{ flightData.flight_param }}</div>
            <div id="flightChart" style="width: 100%; height: 400px;"></div>
        </el-card>
<!--        <el-card class="procedure-card">-->
<!--            <template #header>-->
<!--                <h2>飞行程序</h2>-->
<!--            </template>-->
<!--            <div class="procedure-value">{{ flightData.procedure }}</div>-->
<!--        </el-card>-->
        <el-card class="component-card">
            <template #header>
                <h2>关键组件</h2>
            </template>
            <div v-for="keyword in flightData.keywords" :key="keyword" class="keyword-group">
                <h3>{{ keyword }}</h3>
                <ul>
                    <li v-for="component in flightData.components[keyword]" :key="component.组件">
                        <span class="component-name">{{ component.组件 }}</span>：
                        <span class="component-intro">{{ component.介绍 }}</span>
                    </li>
                </ul>
            </div>
        </el-card>
    </div>
</template>

<script setup>
    import { onMounted, ref } from 'vue';
    import * as echarts from 'echarts';

    const flightData = ref({
        "model": "C919",
        "phase": "起飞",
        "fcs_status": "稳定",
        "environment": "晴天",
        "flight_param": "高度8500m，速度780km/h",
        "procedure": "起飞爬升阶段",
        "keywords": ["方向舵及方向舵配平控制系统"],
        "components": {
            "方向舵及方向舵配平控制系统": [
                {
                    "组件": "方向舵感力装置",
                    "介绍": "用于提供操纵反馈..."
                }
            ]
        }
    });

    onMounted(() => {
        const myChart = echarts.init(document.getElementById('flightChart'));
        const option = {
            title: {
                text: '飞行参数示意'
            },
            tooltip: {},
            xAxis: {
                data: ['高度', '速度']
            },
            yAxis: {},
            series: [
                {
                    name: '参数值',
                    type: 'bar',
                    data: [8500, 780]
                }
            ]
        };
        myChart.setOption(option);
    });
</script>

<style scoped>
    .container {
        padding: 20px;
    }

    .header-card,
    .param-card,
    .procedure-card,
    .component-card {
        margin-bottom: 20px;
        background-color: #f8f9fa;
        border-radius: 8px;
        box-shadow: 0 2px 4px rgba(0, 0, 0, 0.1);
    }

    .info-section {
        display: flex;
        flex-wrap: wrap;
        gap: 16px;
    }

    .info-item {
        display: flex;
        align-items: center;
        gap: 8px;
    }

    .info-label {
        font-weight: bold;
    }

    .param-value,
    .procedure-value {
        margin-bottom: 16px;
    }

    .keyword-group {
        margin-bottom: 16px;
    }

    .component-name {
        font-weight: bold;
    }
</style>