<template>
  <section>
    <!--工具条-->
    <el-row style="margin-bottom: 10px">
      <el-col :span="8">
        <el-input placeholder="请输入内容" v-model="filters.value" class="input-with-select">
          <el-select v-model="filters.name" slot="prepend" placeholder="请选择">
            <el-option label="配置名" value="robot_key:Like"></el-option>
          </el-select>
          <el-button slot="append" icon="el-icon-search" @click="queryList"></el-button>
        </el-input>
      </el-col>
      <el-col :offset="13" :span="3">
        <el-button type="primary" plain @click="handleAdd">添加配置</el-button>
      </el-col>
    </el-row>

    <!--列表-->
    <el-table :data="dataList" highlight-current-row v-loading="listLoading" style="width: 100%;">
      <el-table-column prop="id" label="ID" width="60">
      </el-table-column>
      <el-table-column prop="robotKey" label="配置名" width="150">
      </el-table-column>
      <el-table-column prop="robotValue" label="配置值" width="300">
      </el-table-column>
      <el-table-column prop="expireSeconds" label="过期时间" width="100">
      </el-table-column>
      <el-table-column prop="createdAt" label="创建时间" width="155" :formatter="formatCreatedAtDate">
      </el-table-column>
      <el-table-column prop="updatedAt" label="修改时间" width="155" :formatter="formatUpdatedAtDate">
      </el-table-column>
      <el-table-column label="操作">
        <template slot-scope="scope">
          <el-button type="warning" size="small" @click="handleEdit(scope.$index, scope.row)">编辑</el-button>
        </template>
      </el-table-column>
    </el-table>
    <!--工具条-->
    <el-col :span="24" style="padding: 10px 0">
      <el-pagination layout="total, sizes, prev, pager, next"
                     @current-change="handleCurrentChange"
                     :page-size="20"
                     :total="total"
                     style="float:right; margin-right:-5px">
      </el-pagination>
    </el-col>

    <!--新增界面-->
    <el-dialog title="新增" :visible.sync="addFormVisible" :close-on-click-modal="false">
      <el-form :model="addForm" label-width="120px" :rules="addFormRules" ref="addForm">
        <el-form-item label="配置名" prop="robotKey">
          <el-input v-model="addForm.robotKey" auto-complete="off"></el-input>
        </el-form-item>
        <el-form-item label="配置值" prop="robotValue">
          <el-input type="textarea" v-model="addForm.robotValue" :rows="5" auto-complete="off"></el-input>
        </el-form-item>
        <el-form-item label="过期时间(秒)" prop="expireSeconds">
          <el-input v-model="addForm.expireSeconds" auto-complete="off"></el-input>
        </el-form-item>
      </el-form>
      <div slot="footer" class="dialog-footer">
        <el-button @click.native="addFormVisible = false">取消</el-button>
        <el-button type="primary" @click.native="addSubmit">提交</el-button>
      </div>
    </el-dialog>

    <!--编辑界面-->
    <el-dialog title="编辑" :visible.sync="editFormVisible" :close-on-click-modal="false">
      <el-form :model="editForm" label-width="120px" :rules="editFormRules" ref="editForm">
        <el-form-item label="配置名" prop="robotKey">
          <el-input v-model="editForm.robotKey" auto-complete="off"></el-input>
        </el-form-item>
        <el-form-item label="配置值" prop="robotValue">
          <el-input type="textarea" v-model="editForm.robotValue" :rows="5" auto-complete="off"></el-input>
        </el-form-item>
        <el-form-item label="过期时间(秒)" prop="expireSeconds">
          <el-input v-model="editForm.expireSeconds" auto-complete="off"></el-input>
        </el-form-item>
      </el-form>
      <div slot="footer" class="dialog-footer">
        <el-button @click.native="editFormVisible = false">取消</el-button>
        <el-button type="primary" @click.native="editSubmit">提交</el-button>
      </div>
    </el-dialog>
  </section>
</template>

<script>
  import Webapi from '../../api/webapi';
  import {formatDate} from "../../common/date";
  export default {
    data() {
      return {
        // 查询
        filters: {
          name: 'robot_key:Like',
          value: ''
        },
        dataList: [],
        total: 0,
        page: 1,
        listLoading: false,
        // 新增
        addFormVisible: false,
        addFormRules: {
          buildingId: [
            { required: true, message: '请输入楼宇ID', trigger: 'blur' }
          ],
          floorNumber: [
            { required: true, message: '请输入楼层号', trigger: 'blur' }
          ],
          poiName: [
            { required: true, message: '请输入POI名称', trigger: 'blur' }
          ],
          poiCode: [
            { required: true, message: '请输入POI编码', trigger: 'blur' }
          ]
        },
        addForm: {},
        // 修改
        editFormVisible: false,
        editFormRules: {
          buildingId: [
            { required: true, message: '请输入楼宇ID', trigger: 'blur' }
          ],
          floorNumber: [
            { required: true, message: '请输入楼层号', trigger: 'blur' }
          ],
          poiName: [
            { required: true, message: '请输入POI名称', trigger: 'blur' }
          ],
          poiCode: [
            { required: true, message: '请输入POI编码', trigger: 'blur' }
          ]
        },
        editForm: {}
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
