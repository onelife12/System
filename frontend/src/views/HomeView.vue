<template>
  <el-container>
    <el-header style="background-color: darkcyan">
      <h1 style="color: white;margin-top: 13px;margin-left: 550px">知 识 图 谱</h1>
    </el-header>
    <el-container>
      <el-aside width="200px" style="background-color: cadetblue;height: 885px">
        <el-menu
            style="width: 200px"
            default-active="1"
            class="el-menu-vertical-demo"
        >
          <el-sub-menu index="1">
            <template #title>图谱信息</template>
            <router-link to="/home/information" style="text-decoration: none; color: inherit">
              <el-menu-item index="1-4-1">基础信息看板</el-menu-item>
            </router-link>
          </el-sub-menu>
          <el-sub-menu index="2">
            <template #title>故障诊断工具</template>
            <router-link to="/home/faultlist" style="text-decoration: none; color: inherit">
              <el-menu-item index="2-1">故障列表</el-menu-item>
            </router-link>
            <router-link to="/home/chat" style="text-decoration: none; color: inherit">
              <el-menu-item index="2-2">智能问答</el-menu-item>
            </router-link>
          </el-sub-menu>
          <el-sub-menu index="3" @click="$router.push('/home/discoveryAndReason')">
            <template #title>图谱关联分析与推理</template>
            <!-- 子菜单项 -->
          </el-sub-menu>
          <el-sub-menu index="4" @click="$router.push('/home/multi')">
            <template #title>系统故障信息多维分析</template>
            <!-- 子菜单项 -->
          </el-sub-menu>
<!--          <el-sub-menu index="3">-->

<!--            <template #title>图谱关联分析与状态推理</template>-->
<!--            <router-link to="/home/index" style="text-decoration: none; color: inherit">-->
<!--              <el-menu-item index="2-1">弱关联知识发现</el-menu-item>-->
<!--            </router-link>-->
<!--            <router-link to="/home/deduce" style="text-decoration: none; color: inherit">-->
<!--              <el-menu-item index="2-2">复杂关联关系挖掘</el-menu-item>-->
<!--            </router-link>-->
<!--            <router-link to="/home/transport" style="text-decoration: none; color: inherit">-->
<!--              <el-menu-item index="2-3">状态不确定性传播</el-menu-item>-->
<!--            </router-link>-->
<!--            <router-link to="/home/all" style="text-decoration: none; color: inherit">-->
<!--              <el-menu-item index="2-4">整合</el-menu-item>-->
<!--            </router-link>-->
<!--            <router-link to="/home/find" style="text-decoration: none; color: inherit">-->
<!--              <el-menu-item index="2-4">find</el-menu-item>-->
<!--            </router-link>-->
<!--          </el-sub-menu>-->

<!--          <el-sub-menu index="4">-->
<!--            <template #title>系统故障信息多维分析</template>-->
<!--            <router-link to="/home/index" style="text-decoration: none; color: inherit">-->
<!--              <el-menu-item index="4-1">状态空间分割</el-menu-item>-->
<!--            </router-link>-->
<!--            <router-link to="/home/index" style="text-decoration: none; color: inherit">-->
<!--              <el-menu-item index="4-2">状态评估指标体系</el-menu-item>-->
<!--            </router-link>-->
<!--            <router-link to="/home/riskReason" style="text-decoration: none; color: inherit">-->
<!--              <el-menu-item index="4-3">飞行器故障风险归纳推理</el-menu-item>-->
<!--            </router-link>-->
<!--            <router-link to="/home/multi" style="text-decoration: none; color: inherit">-->
<!--              <el-menu-item index="4-4">整合</el-menu-item>-->
<!--            </router-link>-->
<!--          </el-sub-menu>-->
<!--          <el-sub-menu index="5">
            <template #title>研究内容三</template>
            <router-link to="/home/chat" style="text-decoration: none; color: inherit">
              <el-menu-item index="2-1">层次化组件建模</el-menu-item>
            </router-link>
            <router-link to="/home/guanjian" style="text-decoration: none; color: inherit">
              <el-menu-item index="2-2">关键要素发现</el-menu-item>
            </router-link>
            <router-link to="/home/chat" style="text-decoration: none; color: inherit">
              <el-menu-item index="2-2">多视角推荐</el-menu-item>
            </router-link>
          </el-sub-menu>-->
        </el-menu>
      </el-aside>
      <el-main style="background-color: white;opacity: 0.9">
        <router-view></router-view>
      </el-main>
    </el-container>
  </el-container>
</template>
<script>

export default {
  name: 'HomeView',
  methods: {
    switchPage(index) {
      console.log(index)
    }
  }
}
</script>

<style>

</style>
