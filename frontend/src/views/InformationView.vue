<template>
  <div>
    <el-row :gutter="20" style="margin-top: 20px;">
      <el-col :span="12">
        <el-card style="margin-top: 40px">
          <div style="font-size: 24px;color: #336666; font-weight: bold">实体数量</div>
          <div style="font-size: 40px;color: #336666; font-weight: bold;margin-top: 10px">{{ entityCount }}</div>
        </el-card>
      </el-col>
      <el-col :span="12">
        <el-card style="margin-top: 40px">
          <div style="font-size: 24px;color: #336666; font-weight: bold">三元组关系数量</div>
          <div style="font-size: 40px;color: #336666; font-weight: bold;margin-top: 10px">{{ relationshipCount }}</div>
        </el-card>
      </el-col>
    </el-row>
    <el-row :gutter="20" style="margin-top: 50px">
      <el-col :span="8">
        <div ref="chart1" style="width: 100%; height: 400px;"></div>
      </el-col>
      <el-col :span="8">
        <div ref="chart2" style="width: 100%; height: 400px;"></div>
      </el-col>
      <el-col :span="8">
        <div ref="chart3" style="width: 100%; height: 400px;"></div>
      </el-col>
    </el-row>
  </div>
</template>

<script>
import { graphService } from '@/service/graph'
import * as echarts from 'echarts';
import http from "@/router/http.js"


export default {
  data() {
    return {
      entityCount: "",
      relationshipCount: "",
      //飞机类型数据
      typesInformation: [
        {type: 'A320', number: 23534},
        {type: 'A324', number: 2345},
        {type: '波音737', number: 11345},
      ],
      //事故类型数据
      accidentsInformation: [
        {type: "系统故障", number: 2354},
        {type: "传感器故障", number: 3354},
        {type: "控制单元故障", number: 1354},
        {type: "执行器机构故障", number: 2000},
      ],
      //事故准确率
      accuracy: 0.973
    };
  },
  created() {
    this.getNeo4jInformation()
  },
  mounted() {
    this.initCharts()
  },
  methods: {
    getNeo4jInformation() {
      graphService.getFaultInformation()
        .then(data => {
          this.entityCount = data.nodeCounts
          this.relationshipCount = data.relationCounts
        })
        .catch(error => {
          console.error('获取Neo4j信息失败:', error);
        });
    },
    initCharts() {
      const chart1 = echarts.init(this.$refs.chart1);
      const chart2 = echarts.init(this.$refs.chart2);
      const chart3 = echarts.init(this.$refs.chart3);

      const option1 = {
        title: {
          text: '飞机类型',
        },
        tooltip: {
          trigger: 'item',
          formatter: '飞机型号:  {b} <br/>数量:  {c} ({d}%)'
        },
        series: [
          {
            type: 'pie',
            data: this.typesInformation.map(item => ({
              value: item.number,
              name: item.type
            })),
            label: {
              fontSize: 16,
              fontFamily: 'Arial'
            },
            labelLine: {
              lineStyle: {
                width: 4
              }
            }
          },
        ],
      };

      const option2 = {
        title: {
          text: '事故类型',
        },
        tooltip: {
          trigger: 'item',
          formatter: '故障类型:  {b} <br/>数量:  {c} ({d}%)'
        },
        series: [
          {
            type: 'pie',
            data: this.accidentsInformation.map(item => ({
              value: item.number,
              name: item.type
            })),
            label: {
              fontSize: 16,
              fontFamily: 'Arial'
            },
            labelLine: {
              lineStyle: {
                width: 4
              }
            }
          },
        ],
      };

      const option3 = {
        title: {
          text: '诊断准确率',
        },
        tooltip: {
          trigger: 'item',
          formatter: '诊断正确率:  {b} <br/>数量:  {c} ({d}%)'
        },
        series: [
          {
            type: 'pie',
            radius: ['50%', '70%'],
            data: [
              {value: this.accuracy * 100, name: '正确'},
              {value: (1 - this.accuracy) * 100, name: '错误'},
            ],
            label: {
              fontSize: 16,
              fontFamily: 'Arial'
            },
            labelLine: {
              lineStyle: {
                width: 4
              }
            }
          },
        ],
      };
      chart1.setOption(option1);
      chart2.setOption(option2);
      chart3.setOption(option3);
    }
  },
}
</script>
<style scoped>
.el-card {
  text-align: center;
}
</style>
