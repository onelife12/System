<template>
    <div class="dashboard-container">
        <el-tabs v-model="activeTab" type="border-card" class="main-tabs">
            <el-tab-pane label="关联知识发现" name="knowledge">
                <KnowledgeDiscovery />
            </el-tab-pane>
            <el-tab-pane label="推理分析" name="graph">
                <KnowledgeReason />
            </el-tab-pane>
        </el-tabs>
    </div>
</template>

<script setup>
import { ref } from 'vue';
import KnowledgeDiscovery from '@/views/Firstcomponents/KnowledgeDiscovery.vue';
import KnowledgeReason from '@/views/Firstcomponents/KnowledgeReason.vue';

const activeTab = ref('knowledge');
</script>

<style scoped>
.dashboard-container {
    min-height: 100vh;
    background: linear-gradient(135deg, #1a1a1a 0%, #2c3e50 100%);
    color: #fff;
    padding: 0;
    position: relative;
    overflow-y: auto;
    height: 100vh;
}

.dashboard-container::before {
    content: '';
    position: absolute;
    top: 0;
    left: 0;
    right: 0;
    bottom: 0;
    background: 
        linear-gradient(90deg, rgba(255,255,255,0.03) 1px, transparent 1px) 0 0 / 50px 50px,
        linear-gradient(rgba(255,255,255,0.03) 1px, transparent 1px) 0 0 / 50px 50px;
    pointer-events: none;
}

.main-tabs {
    height: 100vh;
    background: transparent;
}

:deep(.el-tabs__content) {
    height: calc(100vh - 40px);
    overflow-y: auto;
}

:deep(.el-tabs__header) {
    margin: 0;
    background: linear-gradient(90deg, rgba(64, 158, 255, 0.1) 0%, rgba(64, 158, 255, 0.05) 100%);
    border-bottom: 1px solid rgba(64, 158, 255, 0.2);
}

:deep(.el-tabs__item) {
    color: #fff;
    height: 40px;
    line-height: 40px;
    font-size: 16px;
    transition: all 0.3s ease;
}

:deep(.el-tabs__item.is-active) {
    color: #409EFF;
    background: rgba(64, 158, 255, 0.1);
}

:deep(.el-tabs__item:hover) {
    color: #409EFF;
}

:deep(.el-tabs__nav-wrap::after) {
    background-color: rgba(64, 158, 255, 0.2);
}

:deep(.el-tabs--border-card) {
    border: none;
    box-shadow: none;
}

:deep(.el-tabs--border-card > .el-tabs__header) {
    border: none;
}

:deep(.el-tabs--border-card > .el-tabs__content) {
    border: none;
}

/* 自定义滚动条样式 */
.dashboard-container::-webkit-scrollbar {
    width: 8px;
}

.dashboard-container::-webkit-scrollbar-track {
    background: rgba(0, 0, 0, 0.2);
    border-radius: 4px;
}

.dashboard-container::-webkit-scrollbar-thumb {
    background: rgba(64, 158, 255, 0.3);
    border-radius: 4px;
}

.dashboard-container::-webkit-scrollbar-thumb:hover {
    background: rgba(64, 158, 255, 0.5);
}
</style>