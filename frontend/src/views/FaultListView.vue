<template>
  <div>
    <h1 style="color:#336666;margin-left: 30px;font-size: 25px">
      请设置条件查询飞行器故障
    </h1>
    <el-form ref="form" :model="form" style="margin-left: 30px">
      <el-row>
        <el-col :span="5">
          <el-form-item label="飞机型号">
            <el-select v-model="form.model" placeholder="请选择飞机型号" style="width: 150px">
              <el-option
                  v-for="option in modelOptions"
                  :key="option.value"
                  :label="option.label"
                  :value="option.value"></el-option>
            </el-select>
          </el-form-item>
        </el-col>
        <el-col :span="5">
          <el-form-item label="故障部位">
            <el-select v-model="form.position" placeholder="请选择故障部位" style="width: 150px">
              <el-option
                  v-for="option in positionOptions"
                  :key="option.value"
                  :label="option.label"
                  :value="option.value"></el-option>
            </el-select>
          </el-form-item>
        </el-col>
        <el-col :span="5">
          <el-form-item label="故障类型">
            <el-select v-model="form.type" placeholder="请选择故障类型" style="width: 150px">
              <el-option
                  v-for="option in typeOptions"
                  :key="option.value"
                  :label="option.label"
                  :value="option.value"></el-option>
            </el-select>
          </el-form-item>
        </el-col>
        <el-col :span="5">
          <el-form-item label="故障现象" style="width: 220px">
            <el-input v-model="form.phenomenon"></el-input>
          </el-form-item>
        </el-col>
        <el-col :span="1">
          <el-button type="primary" @click="select">查询</el-button>
        </el-col>
        <el-col :span="1">
          <el-button type="danger" @click="clear" style="margin-left: 10px">清空</el-button>
        </el-col>
      </el-row>
      <el-row>
        <el-table
            :data="tableData"
            stripe
            style="width: 100%">
          <el-table-column
              prop="id"
              label="ID"
              width="180">
          </el-table-column>
          <el-table-column
              prop="model"
              label="飞机型号"
              width="180">
          </el-table-column>
          <el-table-column
              prop="status"
              label="状 态">
          </el-table-column>
          <el-table-column
              prop="phenomenon"
              label="故障现象">
          </el-table-column>
          <el-table-column
              prop="position"
              label="故障部位">
          </el-table-column>
          <el-table-column prop="type" label="故障类型">
            <template #default="scope">
              <span>{{ mapFaultType(scope.row.type) }}</span>
            </template>
          </el-table-column>
          <el-table-column
              label="操 作"
              width="100">
            <template #default="scope">
              <el-button style="background-color: cadetblue;color: white" @click="handleClick(scope.row)" type="text"
                         size="small">详情
              </el-button>
            </template>
          </el-table-column>
        </el-table>
      </el-row>
    </el-form>
    <el-pagination style="margin-top: 20px"
                   @size-change="handleSizeChange"
                   @current-change="handleCurrentChange"
                   :total="this.total"
                   :current-page="this.PageData.pageNo"
                   :page-sizes="[5, 10]"
                   :page-size="this.PageData.pageSize"
                   layout="total, sizes, prev, pager, next">
    </el-pagination>
    <el-dialog
        v-model="dialogVisible"
        width="40%"
        @close="dialogVisible = false"
        style="width: 50%;height: 60%">
      <div class="button-group" style="display: flex;justify-content: center">
        <el-button @click="show('Relation')" :class="{active: RelationisActive}">关系图</el-button>
        <el-button @click="show('Information')" :class="{active: InformationisActive}">详细信息</el-button>
      </div>
      <div v-if="currentDialog === 'Relation'">
        <!-- 关系图的内容 -->
        <div id="app">
          <NetworkGraph :links="links" :nodes="nodes"/>
        </div>
      </div>
      <div v-if="currentDialog === 'Information'" style="margin-top: 40px; margin-left: 90px">
        <el-form :model="InformationForm" label-width="auto" style="max-width: 600px">
          <h1 style="margin-left: 10px">基本信息</h1>
          <el-row>
            <el-col :span="12">
              <el-form-item label="飞机型号">
                <el-input v-model="InformationForm.model" style="width: 150px"/>
              </el-form-item>
            </el-col>
            <el-col :span="12">
              <el-form-item label="飞机状态">
                <el-input v-model="InformationForm.status"/>
              </el-form-item>
            </el-col>
          </el-row>
          <el-form-item label="信息来源">
            <el-input v-model="InformationForm.source"/>
          </el-form-item>
          <h1 style="margin-left: 10px">故障信息</h1>
          <el-row>
            <el-col :span="12">
              <el-form-item label="故障位置" style="width: 250px">
                <el-input v-model="InformationForm.position"/>
              </el-form-item>
            </el-col>
            <el-col :span="12">
              <el-form-item label="故障类型">
                <el-input v-model="InformationForm.type"/>
              </el-form-item>
            </el-col>
          </el-row>
          <el-form-item label="故障现象">
            <el-input v-model="InformationForm.phenomenon"/>
          </el-form-item>
          <h1 style="margin-left: 10px">诊断详情</h1>
          <el-form-item label="故障原因">
            <el-input v-model="InformationForm.diagnosis"/>
          </el-form-item>
          <el-form-item label="处理措施">
            <textarea v-model="InformationForm.measure" :rows="3"
                      style="resize: none;width: 550px;font-size: 14px;font-family: 'Microsoft YaHei'"/>
          </el-form-item>
        </el-form>
      </div>
    </el-dialog>
  </div>
</template>

<script>
// import axios from "axios";
import NetworkGraph from "@/components/NetworkGraph.vue";
import { graphService } from '@/service/graph';

export default {
  name: 'App',
  components: {NetworkGraph},
  data() {
    return {
      nodes: [
        {id: 1, name: '飞行器'},
        {id: 2, name: '故障实例'},
        {id: 3, name: '发动机'},
        {id: 4, name: '发生国家'},
        {id: 5, name: '飞行类型'},
        {id: 6, name: '飞行阶段'},
        {id: 7, name: '故障现象'},
        {id: 8, name: '发生部位'},
        {id: 9, name: '故障原因'},
      ],
      links: [
        {source: 1, target: 3, name: '拥有'},
        {source: 1, target: 2, name: '发生故障'},
        {source: 2, target: 6, name: '发生故障'},
        {source: 2, target: 7, name: '故障现象'},
        {source: 2, target: 9, name: '发生原因'},
        {source: 2, target: 8, name: '故障部位'},
        {source: 2, target: 4, name: '发生国家'},
        {source: 2, target: 5, name: '飞行类型'},
      ],
      simulation: null, // 用于力导向图的模拟
      InformationForm: {
        source: '',
        model: '',
        status: '',
        phenomenon: '',
        position: '',
        diagnosis: '',
        type: 3,
        measure: ''
      },
      dialogVisible: false, // 控制对话框的显示状态
      RelationisActive: true,
      InformationisActive: false,
      currentDialog: 'Relation',
      form: {
        model: null,
        phenomenon: null,
        position: null,
        type: null
      },
      typeOptions: [
        {label: '传感器故障', value: 1},
        {label: '控制单元故障', value: 2},
        {label: '系统故障', value: 3},
        {label: '执行机构故障', value: 4},
      ],
      modelOptions: [
        {label: 'A320', value: 'A320'},
        {label: 'A340', value: 'A340'}
      ],
      positionOptions: [
        {label: '轮胎', value: '轮胎'},
        {label: '刹车', value: '刹车'}
      ],
      tableData: [],
      PageData: {
        pageNo: 1,
        pageSize: 10,
      },
      total: 10,
    }
  },
  created() {
    this.fetchData()
  },
  methods: {
    show(type) {
      this.currentDialog = type
      if (type === 'Relation') {
        this.RelationisActive = true;
        this.InformationisActive = false;
      } else {
        this.RelationisActive = false;
        this.InformationisActive = true
      }
    },
    serializeQueryParams(params) {
      const filteredParams = Object.entries(params).filter(([_, value]) => value !== null && value !== undefined && value !== '');
      // 将过滤后的对象转换为查询参数字符串
      return filteredParams.map(([key, value]) => encodeURIComponent(key) + '=' + encodeURIComponent(value)).join('&');
    },
    fetchData() {
      const params = {
        pageNo: this.PageData.pageNo,
        pageSize: this.PageData.pageSize,
        model: this.form.model,
        phenomenon: this.form.phenomenon,
        position: this.form.position,
        type: this.form.type
      };
      graphService.getFaultList(params)
          .then(response => {
            this.total = response.total;
            this.tableData = response.result;
          })
          .catch(error => {
            console.error('获取故障列表失败:', error);
          });
    },
    handleSizeChange(val) {
      this.PageData.page = 1;
      this.PageData.pageSize = val;
      this.fetchData()
    },
    handleCurrentChange(val) {
      this.PageData.pageNo = val;
      this.fetchData()
    },
    mapFaultType(type) {
      switch (type) {
        case 1:
          return '传感器故障';
        case 2:
          return '控制单元故障';
        case 3:
          return '系统故障';
        case 4:
          return '执行机构故障';
        default:
          return '未知类型';
      }
    },
    handleClick(row) {
      this.dialogVisible = true;
      this.InformationForm.model = row.model
      this.InformationForm.status = row.status
      this.InformationForm.source = row.source
      this.InformationForm.position = row.position
      this.InformationForm.type = this.mapFaultType(row.type)
      this.InformationForm.diagnosis = row.diagnosis
      this.InformationForm.phenomenon = row.phenomenon
      this.InformationForm.measure = row.measure
      console.log(row.model)
    },
    select() {
      this.fetchData()
    },
    clear() {
      this.form.model = null
      this.form.type = null
      this.form.position = null
      this.form.phenomenon = null
    },
  }
}
</script>

<style scoped>
.button-group button {
  /* 默认样式 */
  background-color: #ccc;
  color: #fff;
  border: none;
  padding: 10px 20px;
  cursor: pointer;
  transition: background-color 0.3s ease;
  width: 100px;
  height: 40px;
  font-size: 20px;
}

.button-group button.active {
  /* 激活状态下的样式 */
  background-color: #4CAF50;
}
</style>