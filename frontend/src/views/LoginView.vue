<template>
  <div class="login-page">
    <el-container style="height: 100vh;">
      <el-aside width="50%">
        <div class="welcome-text">
          <p style="font-family: 'Microsoft YaHei';font-size: 80px;color: white;font-weight: bold">Hi，你好！</p>
          <p style="font-family: 'Microsoft YaHei';font-size: 50px;color: white">欢迎进入飞行器故障诊断系统</p>
          <p style="font-size: 20px;color: white">Hi, Welcome to the aircraft fault diagnosis system</p>
        </div>
      </el-aside>
      <el-main>
        <div class="login-box">
          <h1>登 录</h1>
          <el-form ref="loginForm" :model="loginForm" :rules="rules" label-width="auto">
            <el-form-item prop="phone">
              <el-input v-model="loginForm.phone" placeholder="请输入电话号码"
                        style="width: 450px;margin-left: 50px;margin-top: 30px;height: 40px"></el-input>
            </el-form-item>
            <el-form-item prop="password">
              <el-input type="password" v-model="loginForm.password" placeholder="请输入密码(6-16位数字和字母的组合)"
                        style="width: 450px;margin-left: 50px;margin-top: 20px;height: 40px"></el-input>
            </el-form-item>
            <el-form-item>
              <el-checkbox style="margin-left: 50px;margin-top: 20px">记住密码</el-checkbox>
            </el-form-item>
            <el-form-item>
              <el-button type="primary"
                         style="background-color: darkcyan;width: 450px;margin-left: 50px;height: 45px;margin-top: 40px"
                         @click="login">登录
              </el-button>
            </el-form-item>
            <el-form-item>
              <el-link type="info" style="margin-left: 50px">立即注册</el-link>
              <el-link type="info" style="margin-left: 320px">忘记密码</el-link>
            </el-form-item>
          </el-form>
        </div>
      </el-main>
    </el-container>
  </div>
</template>

<script>
import { graphService } from '@/service/graph'
import store from "@/store";

export default {
  data() {
    return {
      loginForm: {
        phone: '',
        password: ''
      },
      rules: {
        phone: [
          {required: true, message: "请输入电话号码", trigger: 'blur'}
        ]
      }
    };
  },
  methods: {
    async login() {
      try {
        const response = await graphService.login(this.loginForm);
        if (response.code === 200) {
          this.$message({
            type: "success",
            message: "登录成功"
          });
          window.localStorage.setItem('satoken', 'swjtu '+response.data.tokenValue)
          store.dispatch('setToken', 'swjtu'+response.data.tokenValue)
          this.$router.push('/home/information');
        } else {
          this.$message({
            type: "error",
            message: "用户名或密码错误"
          })
        }
      } catch (error) {
        this.$message({
          type: "error",
          message: "登录失败"
        })
      }
    }
  }
};
</script>

<style>
.el-form-item__error {
  margin-left: 52px;
}
.login-page {
  display: flex;
  justify-content: space-between;
  align-items: center;
  height: 100vh;
}

.welcome-text {
  display: flex;
  flex-direction: column;
  align-items: center;
  height: 100vh; /* 使容器占据整个视口高度 */
  padding-top: 250px;
  box-sizing: border-box;
  text-align: left;
}

.welcome-text p {
  line-height: 2;
  margin: 0;
}

.login-box {
  background-color: white;
  height: 600px;
  width: 550px;
  border-radius: 8px;
  margin-top: 120px;
  margin-left: 50px;
}

.login-box h1 {
  color: darkcyan;
  font-size: 30px;
  margin-left: 230px;
  padding-top: 50px;
}
</style>