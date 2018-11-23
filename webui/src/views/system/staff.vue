<template>
  <section>
    <!--工具条-->
    <el-row style="margin-bottom: 10px">
      <el-col :span="8">
        <el-input placeholder="请输入内容" v-model="filters.value" class="input-with-select">
          <el-select v-model="filters.name" slot="prepend" placeholder="请选择">
            <el-option label="用户名" value="user_name:Like"></el-option>
            <el-option label="用户ID" value="user_id:NumberEq"></el-option>
            <el-option label="Email" value="email:RightLike"></el-option>
          </el-select>
          <el-button slot="append" icon="el-icon-search" @click="queryList"></el-button>
        </el-input>
      </el-col>
      <el-col :offset="13" :span="3">
        <el-button type="primary" plain @click="handleAdd">添加用户</el-button>
      </el-col>
    </el-row>
    <!--列表-->
    <el-table :data="dataList" highlight-current-row v-loading="listLoading" style="width: 100%;">
      <el-table-column prop="id" label="ID" width="50">
      </el-table-column>
      <el-table-column prop="userId" label="用户ID" width="70">
      </el-table-column>
      <el-table-column prop="userName" label="姓名" width="100">
      </el-table-column>
      <el-table-column prop="email" label="Email" width="200">
      </el-table-column>
      <el-table-column prop="workCode" label="工号" width="100">
      </el-table-column>
      <el-table-column prop="updatedAt" label="修改时间" width="200" :formatter="formatUpdatedAtDate">
      </el-table-column>
      <el-table-column prop="createdBy" label="创建人" width="80">
      </el-table-column>
      <el-table-column prop="updatedBy" label="修改人" width="80">
      </el-table-column>
      <el-table-column label="操作">
        <template slot-scope="scope">
          <el-button type="warning" size="small" @click="handleRole(scope.$index, scope.row)">分配角色</el-button>
        </template>
      </el-table-column>
    </el-table>

    <!--工具条-->
    <el-col :span="24" style="padding: 10px 0px">
      <el-pagination layout="total, prev, pager, next"
                     @current-change="handleCurrentChange"
                     :page-size="20"
                     :total="total"
                     style="float:right; margin-right:-5px">
      </el-pagination>
    </el-col>

    <!--新增界面-->
    <el-dialog title="新增" :visible.sync="addFormVisible" :close-on-click-modal="false">
      <el-form :model="addForm" label-width="80px" :rules="addFormRules" ref="addForm">
        <el-form-item label="邮箱" prop="email">
          <el-col :span="12">
            <el-input placeholder="请输入内容" v-model="addForm.email">
              <template slot="append">@ele.me</template>
            </el-input>
          </el-col>
          <el-col :offset="2" :span="8">
            <el-button type="primary" @click.native="getUserByEmail">查询</el-button>
          </el-col>
        </el-form-item>
        <el-form-item label="姓名">
          <el-col :span="12">
            <el-input disabled v-model="addForm.userName" auto-complete="off"></el-input>
          </el-col>
        </el-form-item>
        <el-form-item label="工号">
          <el-col :span="12">
            <el-input disabled v-model="addForm.workCode" auto-complete="off"></el-input>
          </el-col>
        </el-form-item>
      </el-form>
      <div slot="footer" class="dialog-footer">
        <el-button @click.native="addFormVisible = false">取消</el-button>
        <el-button type="primary" @click.native="addSubmit">提交</el-button>
      </div>
    </el-dialog>

    <!--分配角色界面-->
    <el-dialog title="分配角色" :visible.sync="roleFormVisible" :close-on-click-modal="false">
      <el-table :data="roleList" ref="roleList" highlight-current-row v-loading="listLoading"
                @selection-change="selectChange" style="width: 100%;">
        <el-table-column type="selection" width="60">
        </el-table-column>
        <el-table-column prop="code" label="角色编码" width="180">
        </el-table-column>
        <el-table-column prop="name" label="角色名称" width="180">
        </el-table-column>
        <el-table-column prop="isSuper" label="超级用户" :formatter="formatIsSuper">
        </el-table-column>
      </el-table>
      <div slot="footer" class="dialog-footer">
        <el-button @click.native="roleFormVisible = false">取消</el-button>
        <el-button type="primary" @click.native="assignRoleToUser">提交</el-button>
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
        filters: {
          name: 'user_name:Like',
          value: ''
        },
        dataList: [],
        total: 0,
        page: 1,
        listLoading: false,

        addFormVisible: false,
        addFormRules: {
          email: [
            { required: true, message: '请输入邮箱', trigger: 'blur' }
          ]
        },
        //新增界面数据
        addForm: {
          email: '',
          userId: null,
          userName: '',
          workCode: ''
        },
        roleFormVisible: false,
        roleForm: {
          email: '',
          roleCodes: ''
        },
        roleList: []
      }
    },
    mounted() {
      this.queryList();
    },
    methods: {
      formatIsSuper(row, column) {
        return row.isSuper? '是' : '否';
      },
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
        Webapi.listStaff(params).then(
          res => {
            this.listLoading = false;
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
          email: '',
          userId: null,
          userName: '',
          workCode: ''
        };
      },
      getUserByEmail() {
        Webapi.getUserByEmail(this.addForm.email + '@ele.me').then(
          res => {
            this.listLoading = false;
            if (res.data.code === 200) {
              this.addForm = res.data.data;
              this.addForm.enabled=true;
              if(this.addForm.email) this.addForm.email = this.addForm.email.replace("@ele.me","");
            } else {
              this.$notify({
                title: '错误',
                message: res.message,
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
      addSubmit() {
        this.$refs.addForm.validate((valid) => {
          if (valid) {
            if (!this.addForm.userId) {
              this.$notify({
                title: '提示',
                message: '请先查询用户信息',
                type: 'warning'
              });
              return;
            }
            this.addForm.email = this.addForm.email+"@ele.me";
            Webapi.createStaff(this.addForm).then((res) => {
              if (res.data.code === 200) {
                this.$notify({
                  title: '提示',
                  message: '提交成功',
                  type: 'success'
                });
                this.$refs['addForm'].resetFields();
                this.addFormVisible = false;
                this.queryList();
              } else {
                this.$notify({
                  title: '错误',
                  message: res.message,
                  type: 'error'
                });
              }
            });
          }
        });
      },
      handleRole (index, row) {
        this.roleFormVisible = true;
        this.roleForm.email = row.email;
        Webapi.listRolesByEmail(row.email).then(
          res => {
            if (res.data.code === 200) {
              this.roleList = res.data.data;
              this.$nextTick(() => {
                this.roleList.forEach(row => {
                  if (row.isCheck) {
                    this.$refs.roleList.toggleRowSelection(row);
                  }
                });
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
            this.listLoading = false;
            this.$notify({
              title: '错误',
              message: error.message,
              type: 'error'
            });
          }
        );
      },
      selectChange(val) {
        this.roleForm.roleCodes = val.map(role => {
          return role.code;
        });
      },
      assignRoleToUser() {
        Webapi.assignRoleToUser(this.roleForm).then(
          res => {
            if (res.data.code === 200) {
              this.roleFormVisible = false;
              this.roleForm = {};
              this.$notify({
                title: '成功',
                message: '提交成功',
                type: 'success'
              });
            } else {
              this.$notify({
                title: '错误',
                message: res.message,
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
    }
  }

</script>

<style scoped>
  .el-input .el-select {
    width: 120px;
  }
</style>
