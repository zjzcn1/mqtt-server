<template>
    <section>
        <el-row>
            <el-col :span="10">
            <el-card class="box-card" style="height: 95%">
                <div slot="header" class="clearfix">
                    <el-row>
                        <el-col :span="9">
                            <el-input placeholder="Topic" v-model="publishForm.email">
                            </el-input>
                        </el-col>
                        <el-col :offset="1" :span="9">
                            <el-checkbox-group v-model="checkboxGroup1">
                                <el-checkbox-button label="city" key="city">QoS0</el-checkbox-button>
                                <el-checkbox-button label="city" key="city">QoS1</el-checkbox-button>
                                <el-checkbox-button label="city" key="city">QoS2</el-checkbox-button>
                            </el-checkbox-group>
                        </el-col>
                        <el-col :offset="1" :span="4">
                            <el-button type="primary" @click.native="getUserByEmail" style="width:100%">Subscribe</el-button>
                        </el-col>
                    </el-row>
                </div>
                <el-table
                        :data="tableData"
                        stripe
                        style="width: 100%">
                    <el-table-column
                            prop="date"
                            label="Topic"
                            width="180">
                    </el-table-column>
                    <el-table-column
                            prop="name"
                            label="QoS"
                            width="120">
                    </el-table-column>
                    <el-table-column
                            prop="address"
                            label="Action">
                    </el-table-column>
                </el-table>
            </el-card>
            </el-col>
            <el-col :span="14" style="padding-left: 10px">
                <el-card class="box-card">
                    <div slot="header" class="clearfix">
                        <el-row>
                            <el-col :span="10">
                            <el-input placeholder="请输入内容" v-model="filters.value" class="input-with-select">
                                <el-select v-model="filters.name" slot="prepend" placeholder="请选择">
                                    <el-option label="Topic" value="robot_key:Like"></el-option>
                                    <el-option label="Payload" value="robot_key1:Like"></el-option>
                                </el-select>
                            </el-input>
                            </el-col>
                            <el-col :span="9" :offset="1">
                                <el-checkbox-group v-model="checkboxGroup1">
                                    <el-checkbox-button label="city" key="city">QoS0</el-checkbox-button>
                                    <el-checkbox-button label="city" key="city">QoS1</el-checkbox-button>
                                    <el-checkbox-button label="city" key="city">QoS2</el-checkbox-button>
                                </el-checkbox-group>
                            </el-col>
                        </el-row>
                    </div>
                    <el-row>
                        <el-col :span="24">
                            <el-collapse v-model="activeName" accordion>
                                <el-collapse-item title="一致性 Consistency" name="1">
                                    <div>与现实生活一致：与现实生活的流程、逻辑保持一致，遵循用户习惯的语言和概念；</div>
                                    <div>在界面中一致：所有的元素和结构需保持一致，比如：设计样式、图标和文本、元素的位置等。</div>
                                </el-collapse-item>
                                <el-collapse-item title="反馈 Feedback" name="2">
                                    <div>控制反馈：通过界面样式和交互动效让用户可以清晰的感知自己的操作；</div>
                                    <div>页面反馈：操作后，通过页面元素的变化清晰地展现当前状态。</div>
                                </el-collapse-item>
                                <el-collapse-item title="效率 Efficiency" name="3">
                                    <div>简化流程：设计简洁直观的操作流程；</div>
                                    <div>清晰明确：语言表达清晰且表意明确，让用户快速理解进而作出决策；</div>
                                    <div>帮助用户识别：界面简单直白，让用户快速识别而非回忆，减少用户记忆负担。</div>
                                </el-collapse-item>
                                <el-collapse-item title="可控 Controllability" name="4">
                                    <div>用户决策：根据场景可给予用户操作建议或安全提示，但不能代替用户进行决策；</div>
                                    <div>结果可控：用户可以自由的进行操作，包括撤销、回退和终止当前操作等。</div>
                                </el-collapse-item>
                            </el-collapse>
                        </el-col>
                    </el-row>
                </el-card>
            </el-col>
        </el-row>
    </section>
</template>

<script>
  import Webapi from '../api/webapi';
  import {formatDate} from "../common/date";

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
        subscribeForm: {},
        filters: {
          name: 'robot_key:Like',
          value: ''
        },
        tableData: [{
          date: '2016-05-02',
          name: '王小虎',
          address: '上海市普陀区金沙江路 1518 弄'
        }, {
          date: '2016-05-04',
          name: '王小虎',
          address: '上海市普陀区金沙江路 1517 弄'
        }, {
          date: '2016-05-01',
          name: '王小虎',
          address: '上海市普陀区金沙江路 1519 弄'
        }, {
          date: '2016-05-03',
          name: '王小虎',
          address: '上海市普陀区金沙江路 1516 弄'
        }]
      }
    },
    mounted() {
      this.queryList();
    },
    methods: {
      formatCreatedAtDate(row, column) {
        let date = new Date(row.createdAt);
        return formatDate(date, 'yyyy-MM-dd hh:mm:ss');
      },
      formatUpdatedAtDate(row, column) {
        let date = new Date(row.updatedAt);
        return formatDate(date, 'yyyy-MM-dd hh:mm:ss');
      },
      handleCurrentChange(val) {
        this.page = val;
        this.queryList();
      },
      queryList() {
        let filter = this.filters.name.split(':');
        let params = {
          size: 20,
          current: this.page,
          filterName: filter[0],
          filterValue: this.filters.value,
          filterCond: filter[1]
        };

        this.listLoading = true;
        Webapi.listConfig(params).then(
          res => {
            this.listLoading = false;
            this.addFormVisible = false;
            if (res.data.code === 200) {
              this.dataList = res.data.data.items;
              this.total = res.data.data.total;
            } else {
              this.$notify({
                title: '错误',
                message: res.data.message,
                type: 'error'
              });
            }
          }
        ).catch(
          error => {
            this.listLoading = false;
            this.$notify({
              title: '错误',
              message: error.message,
              type: 'error'
            });
          }
        );
      },
      handleAdd() {
        this.addFormVisible = true;
        this.addForm = {
          robotKey: '',
          robotValue: '',
          expireSeconds: null
        };
      },
      addSubmit() {
        this.$refs.addForm.validate((valid) => {
          if (valid) {
            Webapi.createConfig(this.addForm).then(
              res => {
                if (res.data.code === 200) {
                  this.addFormVisible = false;
                  this.queryList();
                  this.$notify({
                    title: '成功',
                    message: '提交成功',
                    type: 'success'
                  });
                } else {
                  this.$notify({
                    title: '错误',
                    message: res.data.message,
                    type: 'error'
                  });
                }
              }
            ).catch(
              error => {
                this.addLoading = false;
                this.$notify({
                  title: '错误',
                  message: error.message,
                  type: 'error'
                });
              }
            );
          }
        });
      },
      handleEdit(index, row) {
        this.editFormVisible = true;
        this.editForm = Object.assign({}, row);
      },
      editSubmit() {
        this.$refs.editForm.validate((valid) => {
          if (valid) {
            Webapi.updateConfig(this.editForm).then(
              res => {
                if (res.data.code === 200) {
                  this.editFormVisible = false;
                  this.queryList();
                  this.$notify({
                    title: '成功',
                    message: '提交成功',
                    type: 'success'
                  });
                } else {
                  this.$notify({
                    title: '错误',
                    message: res.data.message,
                    type: 'error'
                  });
                }
              }
            ).catch(
              error => {
                this.$notify({
                  title: '错误',
                  message: error.message,
                  type: 'error'
                });
              }
            );
          }
        });
      }
    }
  }
</script>

<style scoped>
    .el-input .el-select {
        width: 120px;
    }
</style>
