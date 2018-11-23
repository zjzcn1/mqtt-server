<template>
    <section>
        <el-row>
            <el-col :span="24">
                <el-card class="box-card" style="height: 100%">
                    <div slot="header" class="clearfix">
                        <el-row>
                            <el-col :span="8">
                                <el-input placeholder="Topic" v-model="publishForm.email">
                                </el-input>
                            </el-col>
                            <el-col :offset="1" :span="7">
                                <el-checkbox-group v-model="checkboxGroup1">
                                    <el-checkbox-button label="city" key="city">QoS0</el-checkbox-button>
                                    <el-checkbox-button label="city" key="city">QoS1</el-checkbox-button>
                                    <el-checkbox-button label="city" key="city">QoS2</el-checkbox-button>
                                </el-checkbox-group>
                            </el-col>
                            <el-col :offset="1" :span="3">
                                <el-checkbox v-model="connectForm.willRetain">Retained</el-checkbox>
                            </el-col>
                            <el-col :offset="1" :span="3">
                                <el-button type="primary" @click.native="getUserByEmail" style="width:100%">
                                    Publish
                                </el-button>
                            </el-col>
                        </el-row>
                    </div>
                    <el-row>
                        <el-col :span="24">
                            <el-input type="textarea" :rows="25" placeholder="Message"
                                      v-model="publishForm.userName"></el-input>
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
        subscribeForm: {}
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
