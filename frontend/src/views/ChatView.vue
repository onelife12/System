<template>
  <div class="box">
    <div class="title">
      <span class="title-hn" style="font-weight: bold">智能助手</span>
    </div>
    <div id="content" class="content">
      <div v-for="(item,index) in info" :key="index">
        <div class="info_r info_default" v-if="item.type == 'leftinfo'">
          <span class="circle circle_r"></span>
          <!--          背景灰框-->
          <div class="con_r con_text" style="width: auto;height: auto">
            <div>{{ item.content }}</div>
            <div v-for="(item2,index) in item.question" :key="index">
              <div class="con_que" @click="clickRobot(item2.content,item2.id)">
                <div class="czkj-question-msg">
                  {{ item2.index }}
                  {{ item2.content }}
                </div>
              </div>
            </div>
          </div>
          <div class="time_r">{{ item.time }}</div>
        </div>
        <div class="info_l" v-if="item.type == 'rightinfo'">
          <!--          背景灰框-->
          <div class="con_r con_text" style="width: auto;height: auto;background-color: #3163C5">
            <span class="con_l">{{ item.content }}</span>
          </div>
          <span class="circle circle_l" style="margin-left: 10px"></span>
          <div class="time_l">{{ item.time }}</div>
        </div>
      </div>
    </div>
    <div class="setproblem">
            <textarea
                placeholder="请输入您的问题..."
                style="height: 68px;width: 100%;resize:none;padding-right:80px;outline: none;border-color:#ccc;border-radius:5px;"
                id="text"
                v-model="customerText"
                @keyup.enter="sentMsg()"
            ></textarea>
      <el-button @click="sentMsg()" class="buttonsend">
        <span style="vertical-align: 4px;">发送</span>
      </el-button>
    </div>
  </div>
</template>
<script>
import { graphService } from '@/service/graph';

export default {
  data() {
    return {
      customerText: "",
      info: [
        {
          type: 'leftinfo',
          time: this.getTodayTime(),
          name: "robot",
          content: "您好，欢迎使用!",
          question: ''
        }
      ],
      timer: null,
    }
  },
  created() {
  },
  methods: {
    // 用户发送消息
    sentMsg() {
      // 问题为this.customerText.trim(),将他发送到后端
      clearTimeout(this.timer)
      let text = this.customerText.trim()
      if (text != '') {
        var obj = {
          type: 'rightinfo',
          time: this.getTodayTime(),
          content: text,
        }
        this.info.push(obj)
        this.appendRobotMsg(this.customerText)
        this.customerText = ''
        this.$nextTick(() => {
          var contentHeight = document.getElementById('content')
          contentHeight.scrollTop = contentHeight.scrollHeight
        })
      }
    },
    async fetchAnswer() {
        return await graphService.chat(this.customerText.trim());
    },
    // 机器人回答消息
    async appendRobotMsg(text) {
      clearTimeout(this.timer)
      text = text.trim()
      let answer = await this.fetchAnswer();
      console.log(answer)
      let obj = {
        type: "leftinfo",
        time: this.getTodayTime(),
        name: "robot",
        content: answer,
        question: [],
      }
      this.info.push(obj)
      this.$nextTick(() => {
        var contentHeight = document.getElementById('content')
        contentHeight.scrollTop = contentHeight.scrollHeight
      })
    },
    getTodayTime() {
      // 获取当前时间
      var day = new Date()
      let seconds = day.getSeconds()
      if (seconds < 10) {
        seconds = "0" + seconds
      } else {
        seconds = seconds
      }
      let minutes = day.getMinutes()
      if (minutes < 10) {
        minutes = "0" + minutes
      } else {
        minutes = minutes
      }
      let time =
          day.getFullYear() +
          "-" +
          (day.getMonth() + 1) +
          "-" +
          day.getDate() +
          " " +
          day.getHours() +
          ":" +
          minutes +
          ":" +
          seconds
      return time
    }
  }
}
</script>
<style scoped>
.box {
  width: 100%;
  height: 100%;
  background-color: #fafafa;
  position: relative;
  padding: 20px;
}

#content {
  height: 700px;
  overflow-y: scroll;
  font-size: 14px;
  width: 100%;
}

.circle {
  display: inline-block;
  width: 34px;
  height: 34px;
  border-radius: 50%;
  background-color: white;
}

.con_text {
  color: #333;
  margin-bottom: 5px;
}

.con_que {
  color: #1c88ff;
  height: 30px;
  line-height: 30px;
  cursor: pointer;
}

.info_r {
  position: relative;
}

.circle_r {
  position: absolute;
  left: 0%;
  background-image: url("../assets/robot.png");
}

.circle_l {
  background-color: white;
  background-image: url("../assets/user.png");
}


.con_r {
  display: inline-block;
  width: 55%;
  min-height: 55px;
  background-color: #e2e2e2;
  border-radius: 6px;
  padding: 10px;
  margin-left: 40px;
}

.time_r {
  margin-left: 45px;
  color: #999999;
  font-size: 12px;
}

.info_l {
  text-align: right;
  color: #ffffff;
  color: #3163C5;
  margin-top: 10px;
}

.pic_l {
  width: 13px;
  height: 17px;
  margin: 8px 10px;
}

.time_l {
  margin-right: 45px;
  color: #999999;
  font-size: 12px;
  margin-top: 5px;
}

.con_l {
  display: inline-block;
  width: 220px;
  min-height: 51px;
  border-radius: 6px;
  padding: 10px;
  text-align: left;
  color: #fff;
  margin-right: 5px;
}


.setproblem {
  width: 90%;
  height: 68px;
  background-color: #ffffff;
  position: relative;
  margin-top: 20px;
  padding-bottom: 20px;
}

.setproblem textarea {
  margin-bottom: 10px;
  color: #999999;
  padding: 10px;
  box-sizing: border-box;
}

.buttonsend {
  background: #3163C5;
  opacity: 1;
  border-radius: 4px;
  font-size: 10px;
  color: #ffffff;
  position: absolute;
  right: -10%;
  top: 30%;
  cursor: pointer;
  border: none;
}

.czkj-question-msg {
  float: left;
  font-size: 14px;
  color: #3163C5;
}

</style>