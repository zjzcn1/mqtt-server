<template>
    <section>
        <el-container>
            <el-header>
                <el-col :span="2" class="logo">
                    <i class="fa fa-comments-o"></i>
                    {{collapsed?sysShortName:sysName}}
                </el-col>
                <el-col :span="20">
                    <el-menu router
                            default-active="/publish"
                            class="el-menu-demo"
                            mode="horizontal"
                            background-color="#16222b"
                            text-color="#f3f3f3"
                            active-text-color="#18D4D1">
                        <el-menu-item index="/publish">Publish</el-menu-item>
                        <el-menu-item index="/subscribe">Subscribe</el-menu-item>
                        <el-menu-item index="/status">Server Status</el-menu-item>
                    </el-menu>
                </el-col>
                <el-col :span="2">
                    <el-button type="text" size="medium" style="color: #f3f3f3" @click.native="clickStatus">
                        <i :class="(client&&client.connected) ? 'fa fa-user' : 'fa fa-user-times'"></i>
                        {{(client&&client.connected) ? 'Disconnect' : 'Connect'}}
                    </el-button>
                </el-col>
            </el-header>
            <el-main>
                <transition name="fade" mode="out-in">
                    <router-view></router-view>
                </transition>
                <!--<el-col :span="12">-->
                    <!--<el-card class="box-card" style="height: 100%">-->
                        <!--<div slot="header" class="clearfix">-->
                            <!--<el-row>-->
                                <!--<el-col :span="8">-->
                                    <!--<el-input placeholder="Topic" v-model="publishForm.email">-->
                                    <!--</el-input>-->
                                <!--</el-col>-->
                                <!--<el-col :offset="1" :span="7">-->
                                    <!--<el-checkbox-group v-model="checkboxGroup1">-->
                                        <!--<el-checkbox-button label="city" key="city">QoS0</el-checkbox-button>-->
                                        <!--<el-checkbox-button label="city" key="city">QoS1</el-checkbox-button>-->
                                        <!--<el-checkbox-button label="city" key="city">QoS2</el-checkbox-button>-->
                                    <!--</el-checkbox-group>-->
                                <!--</el-col>-->
                                <!--<el-col :offset="1" :span="3">-->
                                    <!--<el-checkbox v-model="connectForm.willRetain">Retained</el-checkbox>-->
                                <!--</el-col>-->
                                <!--<el-col :offset="1" :span="3">-->
                                    <!--<el-button type="primary" @click.native="getUserByEmail" style="width:100%">-->
                                        <!--Publish-->
                                    <!--</el-button>-->
                                <!--</el-col>-->
                            <!--</el-row>-->
                        <!--</div>-->
                        <!--<el-row>-->
                            <!--<el-col :span="24">-->
                                <!--<el-input type="textarea" :rows="25" placeholder="Message"-->
                                          <!--v-model="publishForm.userName"></el-input>-->
                            <!--</el-col>-->
                        <!--</el-row>-->
                    <!--</el-card>-->
                <!--</el-col>-->
                <!--<el-col :span="12">-->
                    <!--<el-card class="box-card" style="height: 95%">-->
                        <!--<div slot="header" class="clearfix">-->
                            <!--<el-row>-->
                                <!--<el-col :span="8">-->
                                    <!--<el-input placeholder="Topic" v-model="publishForm.email">-->
                                    <!--</el-input>-->
                                <!--</el-col>-->
                                <!--<el-col :offset="1" :span="7">-->
                                    <!--<el-checkbox-group v-model="checkboxGroup1">-->
                                        <!--<el-checkbox-button label="city" key="city">QoS0</el-checkbox-button>-->
                                        <!--<el-checkbox-button label="city" key="city">QoS1</el-checkbox-button>-->
                                        <!--<el-checkbox-button label="city" key="city">QoS2</el-checkbox-button>-->
                                    <!--</el-checkbox-group>-->
                                <!--</el-col>-->
                                <!--<el-col :offset="1" :span="3">-->
                                    <!--<el-button type="primary" @click.native="getUserByEmail" style="width:100%">-->
                                        <!--Subscribe-->
                                    <!--</el-button>-->
                                <!--</el-col>-->
                            <!--</el-row>-->
                        <!--</div>-->
                        <!--<el-row>-->
                            <!--<el-col :span="24">-->
                                <!--<el-collapse v-model="activeName" accordion>-->
                                    <!--<el-collapse-item title="一致性 Consistency" name="1">-->
                                        <!--<div>与现实生活一致：与现实生活的流程、逻辑保持一致，遵循用户习惯的语言和概念；</div>-->
                                        <!--<div>在界面中一致：所有的元素和结构需保持一致，比如：设计样式、图标和文本、元素的位置等。</div>-->
                                    <!--</el-collapse-item>-->
                                    <!--<el-collapse-item title="反馈 Feedback" name="2">-->
                                        <!--<div>控制反馈：通过界面样式和交互动效让用户可以清晰的感知自己的操作；</div>-->
                                        <!--<div>页面反馈：操作后，通过页面元素的变化清晰地展现当前状态。</div>-->
                                    <!--</el-collapse-item>-->
                                    <!--<el-collapse-item title="效率 Efficiency" name="3">-->
                                        <!--<div>简化流程：设计简洁直观的操作流程；</div>-->
                                        <!--<div>清晰明确：语言表达清晰且表意明确，让用户快速理解进而作出决策；</div>-->
                                        <!--<div>帮助用户识别：界面简单直白，让用户快速识别而非回忆，减少用户记忆负担。</div>-->
                                    <!--</el-collapse-item>-->
                                    <!--<el-collapse-item title="可控 Controllability" name="4">-->
                                        <!--<div>用户决策：根据场景可给予用户操作建议或安全提示，但不能代替用户进行决策；</div>-->
                                        <!--<div>结果可控：用户可以自由的进行操作，包括撤销、回退和终止当前操作等。</div>-->
                                    <!--</el-collapse-item>-->
                                <!--</el-collapse>-->
                            <!--</el-col>-->
                        <!--</el-row>-->
                    <!--</el-card>-->
                <!--</el-col>-->
            </el-main>
            <!--<header class="header">-->
            <!--<el-col :span="10" class="logo" :class="collapsed?'logo-collapse-width':'logo-width'">-->
            <!--<i class="fa fa-envira"></i>-->
            <!--{{collapsed?sysShortName:sysName}}-->
            <!--</el-col>-->
            <!--<el-col :span="4" class="user">-->
            <!--<el-button type="text" size="medium" style="color: #f3f3f3" @click.native="clickStatus">-->
            <!--<i :class="(client&&client.connected) ? 'fa fa-link' : 'fa fa-unlink'"></i>-->
            <!--{{(client&&client.connected) ? 'Connected' : 'Disconnected'}}-->
            <!--</el-button>-->
            <!--</el-col>-->
            <!---->
            <!--</header>-->
            <!--<div :span="24" class="content-container" style="height: 95%">-->
            <!--<el-col :span="12">-->
            <!--<el-card class="box-card" style="height: 100%">-->
            <!--<div slot="header" class="clearfix">-->
            <!--<el-row>-->
            <!--<el-col :span="8">-->
            <!--<el-input placeholder="Topic" v-model="publishForm.email">-->
            <!--</el-input>-->
            <!--</el-col>-->
            <!--<el-col :offset="1" :span="7">-->
            <!--<el-checkbox-group v-model="checkboxGroup1">-->
            <!--<el-checkbox-button label="city" key="city">QoS0</el-checkbox-button>-->
            <!--<el-checkbox-button label="city" key="city">QoS1</el-checkbox-button>-->
            <!--<el-checkbox-button label="city" key="city">QoS2</el-checkbox-button>-->
            <!--</el-checkbox-group>-->
            <!--</el-col>-->
            <!--<el-col :offset="1" :span="3">-->
            <!--<el-checkbox v-model="connectForm.willRetain">Retained</el-checkbox>-->
            <!--</el-col>-->
            <!--<el-col :offset="1" :span="3">-->
            <!--<el-button type="primary" @click.native="getUserByEmail" style="width:100%">Publish</el-button>-->
            <!--</el-col>-->
            <!--</el-row>-->
            <!--</div>-->
            <!--<el-row>-->
            <!--<el-col :span="24">-->
            <!--<el-input type="textarea" :rows="25" placeholder="Message" v-model="publishForm.userName"></el-input>-->
            <!--</el-col>-->
            <!--</el-row>-->
            <!--</el-card>-->
            <!--</el-col>-->
            <!--<el-col :span="12" style="margin-left:10px">-->
            <!--<el-card class="box-card" style="height: 95%">-->
            <!--<div slot="header" class="clearfix">-->
            <!--<el-row>-->
            <!--<el-col :span="8">-->
            <!--<el-input placeholder="Topic" v-model="publishForm.email">-->
            <!--</el-input>-->
            <!--</el-col>-->
            <!--<el-col :offset="1" :span="7">-->
            <!--<el-checkbox-group v-model="checkboxGroup1">-->
            <!--<el-checkbox-button label="city" key="city">QoS0</el-checkbox-button>-->
            <!--<el-checkbox-button label="city" key="city">QoS1</el-checkbox-button>-->
            <!--<el-checkbox-button label="city" key="city">QoS2</el-checkbox-button>-->
            <!--</el-checkbox-group>-->
            <!--</el-col>-->
            <!--<el-col :offset="1" :span="3">-->
            <!--<el-button type="primary" @click.native="getUserByEmail" style="width:100%">Subscribe-->
            <!--</el-button>-->
            <!--</el-col>-->
            <!--</el-row>-->
            <!--</div>-->
            <!--<el-row>-->
            <!--<el-col :span="24">-->
            <!--<el-collapse v-model="activeName" accordion>-->
            <!--<el-collapse-item title="一致性 Consistency" name="1">-->
            <!--<div>与现实生活一致：与现实生活的流程、逻辑保持一致，遵循用户习惯的语言和概念；</div>-->
            <!--<div>在界面中一致：所有的元素和结构需保持一致，比如：设计样式、图标和文本、元素的位置等。</div>-->
            <!--</el-collapse-item>-->
            <!--<el-collapse-item title="反馈 Feedback" name="2">-->
            <!--<div>控制反馈：通过界面样式和交互动效让用户可以清晰的感知自己的操作；</div>-->
            <!--<div>页面反馈：操作后，通过页面元素的变化清晰地展现当前状态。</div>-->
            <!--</el-collapse-item>-->
            <!--<el-collapse-item title="效率 Efficiency" name="3">-->
            <!--<div>简化流程：设计简洁直观的操作流程；</div>-->
            <!--<div>清晰明确：语言表达清晰且表意明确，让用户快速理解进而作出决策；</div>-->
            <!--<div>帮助用户识别：界面简单直白，让用户快速识别而非回忆，减少用户记忆负担。</div>-->
            <!--</el-collapse-item>-->
            <!--<el-collapse-item title="可控 Controllability" name="4">-->
            <!--<div>用户决策：根据场景可给予用户操作建议或安全提示，但不能代替用户进行决策；</div>-->
            <!--<div>结果可控：用户可以自由的进行操作，包括撤销、回退和终止当前操作等。</div>-->
            <!--</el-collapse-item>-->
            <!--</el-collapse>-->
            <!--</el-col>-->
            <!--</el-row>-->
            <!--</el-card>-->
            <!--</el-col>-->

            <!--</div>-->
        </el-container>
        <el-dialog title="Connect Info" :visible.sync="dialogVisible" :close-on-click-modal="false">
            <el-form :model="connectForm" label-width="120px" :rules="addFormRules">
                <el-form-item label="Client ID:">
                    <el-col :span="10">
                        <el-input v-model="connectForm.clientId">
                        </el-input>
                    </el-col>
                    <el-col :offset="2" :span="8">
                        <el-button type="primary" plain @click.native="getUserByEmail">Generate</el-button>
                    </el-col>
                </el-form-item>
                <el-form-item label="User Nme:">
                    <el-col :span="10">
                        <el-input v-model="connectForm.username"></el-input>
                    </el-col>
                </el-form-item>
                <el-form-item label="Password:">
                    <el-col :span="10">
                        <el-input v-model="connectForm.password"></el-input>
                    </el-col>
                </el-form-item>
                <el-form-item label="Keep Alive:">
                    <el-col :span="10">
                        <el-input v-model="connectForm.password"></el-input>
                    </el-col>
                    <el-col :span="13" :offset="1">
                        <el-checkbox v-model="connectForm.willRetain">Clean Session</el-checkbox>
                    </el-col>
                </el-form-item>
                <el-form-item label="Will Payload:">
                    <el-col :span="10">
                        <el-input v-model="connectForm.willMessage"></el-input>
                    </el-col>
                    <el-col :offset="1" :span="8">
                        <el-checkbox-group v-model="checkboxGroup1">
                            <el-checkbox-button label="city" key="city">QoS0</el-checkbox-button>
                            <el-checkbox-button label="city" key="city">QoS1</el-checkbox-button>
                            <el-checkbox-button label="city" key="city">QoS2</el-checkbox-button>
                        </el-checkbox-group>
                    </el-col>
                    <el-col :offset="1" :span="2">
                        <el-checkbox v-model="connectForm.willRetain">Retained</el-checkbox>
                    </el-col>
                </el-form-item>
            </el-form>
            <div slot="footer" class="dialog-footer">
                <el-button type="primary" @click.native="connect">Connect</el-button>
                <el-button @click.native="dialogVisible = false">Cancel</el-button>
            </div>
        </el-dialog>
    </section>
</template>

<script>

  import Webapi from '../api/webapi'
  import mqtt from 'mqtt'

  export default {
    data() {
      return {
        sysName: 'MQTT',
        sysShortName: '',
        collapsed: false,
        sysUserName: 'Connected',
        dialogVisible: false,
        client: null,
        connectForm: {},
        publishForm: {},
        subscribeForm: {}
      }
    },
    methods: {
      clickStatus() {
        if (this.client && this.client.conneted) {
          this.$confirm('Confirm disconnect?', 'Disconnect', {
            type: 'warning'
          }).then(() => {
            this.client.end();
          });
        } else {
          this.dialogVisible = true;
        }
      },
      connect() {
        this.client = mqtt.connect('ws://localhost:8081/mqtt', {
          clientId: 'mqttjs',
          username: 'admin',
          password: 'admin'
        });

        this.client.subscribe("/notify/message");

        this.client.on("message", function (topic, payload) {
          alert([topic, payload].join(": "))
        });

        // this.client = mqtt.connect('ws://localhost:8081/mqtt', {
        //   clientId: 'mqttjs',
        //   username: 'admin',
        //   password: 'admin'
        // });
        //
        // this.client.on('connect', () => {
        //   this.dialogVisible = false;
        //   this.$notify({
        //     title: 'Info',
        //     message: 'Connect is success',
        //     type: 'success'
        //   });
        //   this.client.subscribe('/notify/message', (err) => {
        //     if (err) {
        //       this.$notify({
        //         title: 'Info',
        //         message: 'subscribe is error',
        //         type: 'error'
        //       });
        //     }
        //   });
        // });
        //
        // this.client.on('message', (topic, message) => {
        //   console.log(message.toString());
        //   this.$notify({
        //     title: 'message',
        //     message: message.toString(),
        //     type: 'success'
        //   });
        //   // this.client.end();
        // })

        // let qtt = {};
        // qtt.aa = '发布';
        // qtt.bb = '消息！';
        //
        // setInterval(function () {
        //   this.client.publish('/notify/message', JSON.stringify(qtt), {qos: 1, retain: true});
        // }, 10000);
      }
    },
    mounted() {
      Webapi.getCurrentUser().then((res) => {
        if (res.data && res.data.code === 200) {
          this.sysUserName = res.data.data.userName;
        }
      });
    }
  }

</script>

<style scoped lang="scss">
    $header-color: #16222b;
    $header-height: 60px;

    .el-header {
        background-color: #16222b;
        color: #f3f3f3;
        text-align: center;
        height: $header-height;
        line-height: $header-height;
    }

    .logo {
        height: $header-height;
        font-size: 20px;
        vertical-align: middle;
        color: #f3f3f3;
        padding-right: 20px;
        i {
            font-size: 28px;
        }
        .icon {
            height: 28px;
            width: 28px;
        }
    }

</style>