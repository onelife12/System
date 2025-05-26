<template>
  <div class="pane">
    <!-- 工具栏 -->
    <div class="toolbar">
      <el-select v-model="selectedTypes" multiple placeholder="故障类型" class="big-select" style="width:220px" @change="refresh">
        <el-option v-for="t in typeOptions" :key="t" :label="t" :value="t" />
      </el-select>

      <el-select v-model="selectedStatuses" multiple placeholder="飞行状态" class="big-select" style="width:150px;margin-left:4px" @change="refresh">
        <el-option v-for="s in statusOptions" :key="s" :label="s" :value="s" />
      </el-select>

      <el-input v-model.trim="keyword" placeholder="搜索故障现象 / 诊断…" clearable
                style="width:260px;margin-left:4px" @input="refresh">
        <template #prefix><i class="el-icon-search" /></template>
      </el-input>

<!--      <el-button type="warning" class="big-btn" style="margin-left:4px"-->
<!--                 :disabled="!filteredData.length" @click="$emit('export', filteredData)">-->
<!--        导出 Excel-->
<!--      </el-button>-->
    </div>

    <!-- 表格 -->
    <el-table :data="pagedData" class="big-table" border height="calc(100% - 72px)" stripe>
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
  </div>
</template>

<script setup>
import { ref, computed } from 'vue'

/* ——— Props ——— */
// eslint-disable-next-line no-undef
const props = defineProps({ allData: Array, loading: Boolean })

/* ——— 内部状态 ——— */
const selectedTypes = ref([])
const selectedStatuses = ref([])
const keyword = ref('')
const pageSize = ref(10)
const currentPage = ref(1)

/* ——— 选项列表 ——— */
const typeOptions = computed(() => [...new Set(props.allData.map(d => d.faultType || '未分类'))])
const statusOptions = computed(() => [...new Set(props.allData.map(d => d.aircraftStatus))])

/* ——— 过滤 + 分页 ——— */
const filteredData = computed(() => props.allData.filter(item => {
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
function refresh () { currentPage.value = 1 }
function sizeChange (sz) { pageSize.value = sz; refresh() }
function pageChange (p) { currentPage.value = p }
function tagType (t) {
  return {
    机体结构故障: 'info',
    航空电子系统故障: 'success',
    动力系统故障: 'danger',
    机载设备故障: 'warning',
    飞行控制系统故障: 'primary'
  }[t] || ''
}
</script>

<style scoped>
.pane { height:100%; display:flex; flex-direction:column; }
.toolbar{ display:flex; align-items:center; gap:4px; padding:6px 0; }
.big-btn{ font-size:15px; }
.big-select .el-input__inner{ font-size:14px; }
.big-table .el-table__header th{ font-size:15px;font-weight:600;background:#fafafa }
.big-table .el-table__body td{ font-size:14px }
.big-pagination{ margin-top:6px; --el-pagination-font-size:14px }
</style>
