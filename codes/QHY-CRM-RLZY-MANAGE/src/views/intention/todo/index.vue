<template>
  <div class="app-container">
    <el-tabs v-model="activeName" @tab-click="handleClick">
      <el-tab-pane label="我的任务" name="first"></el-tab-pane>
      <el-tab-pane label="下属的任务" name="second"
                   v-if="checkPermi(['todo:todo:child'])"
      ></el-tab-pane>
    </el-tabs>
    <el-form :model="queryParams" ref="queryForm" size="small" :inline="true" v-show="showSearch" label-width="68px">
      <el-form-item label="时间范围" prop="date">
        <el-date-picker
          v-model="queryParams.date"
          type="daterange"
          range-separator="至"
          value-format="yyyy-MM-dd"
          start-placeholder="开始日期"
          end-placeholder="结束日期">
        </el-date-picker>
      </el-form-item>
      <el-form-item label="任务状态" prop="status">
        <el-select v-model="queryParams.status" placeholder="请选择" filterable clearable>
          <el-option
            v-for="item in taskStatus"
            :key="item.dictValue"
            :label="item.dictLabel"
            :value="item.dictValue"
            @change="handleQuery"
          >
          </el-option>
        </el-select>
      </el-form-item>
      <el-form-item label="任务类型" prop="type">
        <el-select v-model="queryParams.type" placeholder="请选择" filterable clearable>
          <el-option
            v-for="item in taskType"
            :key="item.dictValue"
            :label="item.dictLabel"
            :value="item.dictValue"
            @change="handleQuery"
          >
          </el-option>
        </el-select>
      </el-form-item>
      <el-form-item label="接收人" prop="accepterId" v-if="activeName==='second'">
        <el-select v-model="queryParams.accepterId" placeholder="请选择" filterable clearable>
          <el-option
            v-for="user in userOptions"
            :key="user.userId"
            :label="user.userName"
            :value="user.userId"
            @change="handleQuery"
          ></el-option>
        </el-select>
      </el-form-item>
      <el-form-item>
        <el-button type="primary" icon="el-icon-search" size="mini" @click="handleQuery">搜索</el-button>
        <el-button icon="el-icon-refresh" size="mini" @click="resetQuery">重置</el-button>
      </el-form-item>
    </el-form>

    <el-row :gutter="10" class="mb8">
      <right-toolbar :showSearch.sync="showSearch" @queryTable="getList"></right-toolbar>
    </el-row>

    <el-table v-loading="loading" :data="taskList" @selection-change="handleSelectionChange">
      <el-table-column label="id" align="center" prop="id"/>
      <el-table-column label="客户名称" align="center" prop="customer.name"/>
      <el-table-column label="指派人" align="center" prop="assignUser.userName"/>
      <el-table-column label="接受人" align="center" prop="acceptUser.userName"/>
      <el-table-column label="任务事项" align="center" prop="feedback"/>
      <el-table-column label="任务状态" align="center">
        <template slot-scope="scope">
          <dict-tag :options="dict.type.task_status" :value="scope.row.status"/>
        </template>
      </el-table-column>
      <el-table-column label="任务类型" align="center">
        <template slot-scope="scope">
          <dict-tag :options="dict.type.task_type" :value="scope.row.type"/>
        </template>
      </el-table-column>
      <el-table-column label="创建时间" align="center" prop="createTime"/>
      <el-table-column label="备注" align="center" prop="remark"/>
      <el-table-column label="操作" align="center" class-name="small-padding fixed-width">
        <template slot-scope="scope">
          <!--            -->
          <el-button
            size="mini"
            type="text"
            icon="el-icon-search"
            v-if="scope.row.status === '3'"
            @click="handleSeeReason(scope.row)"
          >查看无法完成原因
          </el-button>
          <el-button
            size="mini"
            type="text"
            icon="el-icon-edit"
            v-if="scope.row.status === '2'"
            @click="updateTaskStatus(scope.row)"
            v-hasPermi="['task:task:edit_todo']"
          >完成
          </el-button>
          <el-button
            v-if="scope.row.status === '2'"
            size="mini"
            type="text"
            icon="el-icon-edit"
            @click="updateTaskStatus2(scope.row)"
            v-hasPermi="['task:task:edit_todo']"
          >无法完成
          </el-button>
          <el-button
            v-if="scope.row.status === '2'&&scope.row.handle==='1'"
            size="mini"
            type="text"
            icon="el-icon-edit"
            @click="handleUpdate(scope.row)"
            v-hasPermi="['task:task:update_todo']"
          >修改
          </el-button>
          <el-button
            v-if="scope.row.status === '2'&&scope.row.handle==='1'"
            size="mini"
            type="text"
            icon="el-icon-delete"
            @click="handleDelete(scope.row)"
            v-hasPermi="['task:task:remove_todo']"
          >删除
          </el-button>
        </template>
      </el-table-column>
    </el-table>

    <pagination
      v-show="total>0"
      :total="total"
      :page.sync="queryParams.pageNum"
      :limit.sync="queryParams.pageSize"
      @pagination="getList"
    />

    <!-- 无法完成任务输入原因对话框 -->
    <el-dialog :title="title" :visible.sync="openReason" width="500px" append-to-body>
      <el-form ref="formReason" :model="formReason" :rules="rulesReason" label-width="80px">
        <el-form-item label="原因" prop="reason">
          <el-input v-model="formReason.reason" type="textarea" placeholder="请输入无法完成任务的原因"
                    :disabled="formReason.status=='3'"/>
        </el-form-item>
      </el-form>
      <div slot="footer" class="dialog-footer">
        <el-button type="primary" v-if="formReason.status!='3'" @click="submitReasonForm">确 定</el-button>
        <el-button @click="cancelReason">取 消</el-button>
      </div>
    </el-dialog>

    <!-- 完成跟进任务需要填写客户跟进对话框 -->
    <el-dialog :title="title" :visible.sync="openRecord" width="500px" append-to-body>
      <el-form ref="formRecord" :model="formRecord" :rules="rulesRecord" label-width="80px">
        <el-form-item label="跟进方式" prop="traceType">
          <el-select v-model="formRecord.traceType" placeholder="请选择" filterable clearable>
            <el-option
              v-for="dict in traceTypeOptions"
              :key="dict.dictValue"
              :label="dict.dictLabel"
              :value="dict.dictValue"
            ></el-option>
          </el-select>
        </el-form-item>
        <el-form-item label="跟进时间" prop="createTime">
          <el-date-picker
            v-model="formRecord.createTime"
            value-format="yyyy-MM-dd"
            type="date"
            placeholder="请选择"
          ></el-date-picker>
        </el-form-item>
        <el-form-item label="跟进内容" prop="traceContent">
          <el-input type="textarea" v-model="formRecord.traceContent" placeholder="请输入"/>
        </el-form-item>
        <el-form-item label="图片" prop="tracePic">
          <el-upload
            class="avatar-uploader"
            :action="uploadFileUrl"
            accept=".jpg,.png,.JPG,.PNG,.jpeg"
            :show-file-list="false"
            :headers="headers"
            :on-error="handleUploadError"
            :on-success="handleSuccess"
            :on-exceed="handleExceed"
            :before-upload="beforeUpload">
            <img v-if="formRecord.tracePic" :src="formRecord.tracePic" class="avatar">
            <i v-else class="el-icon-plus avatar-uploader-icon"></i>
          </el-upload>
        </el-form-item>
        <el-form-item label="业务类型" prop="businessId">
          <el-select v-model="formRecord.businessId" placeholder="请选择" filterable clearable>
            <el-option
              v-for="business in businessList"
              :key="business.id"
              :label="business.name"
              :value="business.id"
            ></el-option>
          </el-select>
        </el-form-item>
        <el-form-item label="备注" prop="remark">
          <el-input v-model="formRecord.remark" type="textarea" placeholder="请输入内容"/>
        </el-form-item>
      </el-form>
      <div slot="footer" class="dialog-footer">
        <el-button type="primary" @click="submitRecordForm">确 定</el-button>
        <el-button @click="cancelRecord">取 消</el-button>
      </div>
    </el-dialog>

    <!-- 修改任务对话框 -->
    <el-dialog :title="title" :visible.sync="open" width="500px" append-to-body>
      <el-form ref="form" :model="form" :rules="rules" label-width="80px">
        <el-form-item label="客户名称" prop="customerName">
          <el-input v-model="form.customerName" placeholder="请输入客户id" disabled="disabled"/>
        </el-form-item>
        <el-form-item label="接收人" prop="accepterId">
          <el-select v-model="form.accepterId" placeholder="请选择" filterable clearable>
            <el-option
              v-for="user in userOptions"
              :key="user.userId"
              :label="user.userName"
              :value="user.userId"
            ></el-option>
          </el-select>
        </el-form-item>
        <el-form-item label="类型" prop="type">
          <el-select v-model="form.type" placeholder="请选择" filterable clearable>
            <el-option
              v-for="dict in taskType"
              :key="dict.dictValue"
              :label="dict.dictLabel"
              :value="dict.dictValue"
            ></el-option>
          </el-select>
        </el-form-item>
        <el-form-item label="任务事项" prop="feedback">
          <el-input v-model="form.feedback" placeholder="请输入任务事项"/>
        </el-form-item>
        <el-form-item label="备注" prop="remark">
          <el-input v-model="form.remark" type="textarea" placeholder="请输入内容"/>
        </el-form-item>
      </el-form>
      <div slot="footer" class="dialog-footer">
        <el-button type="primary" @click="submitForm">确 定</el-button>
        <el-button @click="cancel">取 消</el-button>
      </div>
    </el-dialog>

  </div>
</template>

<script>
import {listTask, getTask, delTask, addTask, updateTask} from "@/api/task/task";
import filterData from '@/utils/filterData'
import {checkPermi} from "@/utils/permission";
import {getUserProfile, listUserNotPage} from "@/api/system/user";
import {getToken} from "@/utils/auth";
import {addProgess, getBusinessList} from "@/api/progess/progess";

export default {
  name: "Task",
  dicts: ['task_status', 'task_type'],
  data() {
    return {
      businessList: [],
      uploadFileUrl: process.env.VUE_APP_BASE_API + "/api/oss/upload", // 上传的图片服务器地址
      headers: {
        Authorization: "Bearer " + getToken(),
      },
      //日期范围
      dateRange: [],
      activeName: 'first',
      taskStatus: [],
      taskType: [],
      // 遮罩层
      loading: true,
      // 选中数组
      ids: [],
      // 非单个禁用
      single: true,
      // 非多个禁用
      multiple: true,
      // 显示搜索条件
      showSearch: true,
      // 总条数
      total: 0,
      // 任务表格数据
      taskList: [],
      // 弹出层标题
      title: "",
      // 是否显示弹出层
      open: false,
      // 查询参数
      queryParams: {
        orderByColumn: 'm.create_time',
        isAsc: 'desc',
        pageNum: 1,
        pageSize: 10,
        cid: null,
        assignerId: null,
        accepterId: null,
        feedback: null,
        status: null,
        type: null,
        date: null,
        oper: '0'
      },
      // 表单参数
      form: {},
      formReason: {},
      // 表单校验
      rules: {
        customerName: [
          {required: true, message: "客户名称不能为空", trigger: "blur"},
        ],
        accepterId: [
          {required: true, message: "接受人不能为空", trigger: "blur"},
        ],
        feedback: [
          {required: true, message: "任务事项不能为空", trigger: "blur"},
        ],
        type: [
          {required: true, message: "类型不能为空", trigger: "blur"},
        ],
      },
      rulesReason: {
        reason: [{required: true, message: "原因不能为空", trigger: "blur"}]
      },
      status: [],
      types: [],
      openReason: false,
      openRecord: false,
      formRecord: {},
      rulesRecord: {
        businessId: [
          {required: true, message: "业务类型不能为空", trigger: "blur"},
        ],
        createTime: [
          {required: true, message: "跟进时间不能为空", trigger: "blur"},
        ],
        traceType: [
          {required: true, message: "跟进方式不能为空", trigger: "blur"},
        ],
        traceContent: [
          {required: true, message: "跟进内容不能为空", trigger: "blur"},
        ],
      },
      traceTypeOptions: [],
      loginUser: {},
      cid: null,
      taskId: null,
      zhipaiUserId: null,
      userOptions: [],
    };
  },
  created() {
    this.getList();
    this.getBusinessList();
    this.getUserList();
    this.getLoginUser()
    this.getDicts("task_status").then(response => {
      this.taskStatus = response.data;
    });
    this.getDicts("task_type").then(response => {
      this.taskType = response.data;
    });
    this.getDicts("trace_type").then(response => {
      this.traceTypeOptions = response.data;
    });
  },
  methods: {
    /** 查询业务类型列表 */
    getBusinessList() {
      getBusinessList().then(response => {
        this.businessList = response.data;
      });
    },
    /** 查询用户列表 */
    getUserList() {
      listUserNotPage().then(response => {
        this.userOptions = response.data;
      });
    },
    updateTaskStatus(row) {
      this.taskId = row.id
      if (row.type === '2') {
        this.$modal.confirm('是否确认完成？').then(function () {
          return updateTask({id: row.id, status: '1'});
        }).then(() => {
          this.getList();
          this.$modal.msgSuccess("操作成功");
        }).catch(() => {
        });
      } else {
        this.openRecord = true;
        this.title = "我的跟进";
        this.cid = row.cid;
        this.zhipaiUserId = row.assignerId;
      }
    },
    handleSeeReason(row) {
      getTask(row.id).then(response => {
        this.formReason = response.data;
        this.openReason = true;
        this.title = "无法完成";
      });
    },
    checkPermi,
    handleClick(tab, event) {
      if(tab.index==0){
        this.activeName="first"
      }else {
        this.activeName="second"
      }
      this.queryParams.oper = tab.index
      this.getList()
    },
    /** 查询任务列表 */
    getList() {
      this.loading = true;
      listTask(this.queryParams).then(response => {
        this.taskList = response.rows;
        this.total = response.total;
        this.loading = false;
      });
    },
    // 取消按钮
    cancel() {
      this.open = false;
      this.reset();
    },
    // 取消按钮
    cancelReason() {
      this.openReason = false;
      this.reset();
    },
    // 表单重置
    reset() {
      this.form = {
        id: null,
        cid: null,
        assignerId: null,
        accepterId: null,
        feedback: null,
        status: "0",
        type: null,
        createBy: null,
        createTime: null,
        updateBy: null,
        updateTime: null,
        remark: null,
        oper: '1'
      };
      this.resetForm("form");
      this.resetForm("formReason");
    },
    /** 搜索按钮操作 */
    handleQuery() {
      this.queryParams.pageNum = 1;
      this.getList();
    },
    /** 重置按钮操作 */
    resetQuery() {
      this.resetForm("queryForm");
      this.handleQuery();
    },
    // 多选框选中数据
    handleSelectionChange(selection) {
      this.ids = selection.map(item => item.id)
      this.single = selection.length !== 1
      this.multiple = !selection.length
    },
    /** 新增按钮操作 */
    handleAdd() {
      this.reset();
      this.open = true;
      this.title = "添加任务";
    },
    /** 无法完成按钮操作 */
    updateTaskStatus2(row) {
      const id = row.id || this.ids
      getTask(id).then(response => {
        this.formReason = response.data;
        this.openReason = true;
        this.title = "无法完成";
      });
    },
    /** 提交按钮 */
    submitReasonForm() {
      this.$refs["formReason"].validate(valid => {
        if (valid) {
          if (this.formReason.id != null) {
            this.formReason.status = '3';
            updateTask(this.formReason).then(response => {
              this.$modal.msgSuccess("修改成功");
              this.openReason = false;
              this.getList();
            });
          }
        }
      });
    },
    /** 修改按钮操作 */
    handleUpdate(row) {
      this.reset();
      const id = row.id || this.ids
      getTask(id).then(response => {
        this.form = response.data;
        this.form.customerName = this.form.customer?.name;
        this.open = true;
        this.title = "修改任务";
      });
    },
    /** 提交按钮 */
    submitForm() {
      this.$refs["form"].validate(valid => {
        if (valid) {
          if (this.form.id != null) {
            this.form.status = '2';
            updateTask(this.form).then(response => {
              this.$modal.msgSuccess("修改成功");
              this.open = false;
              this.getList();
            });
          }
        }
      });
    },
    /** 删除按钮操作 */
    handleDelete(row) {
      const ids = row.id || this.ids;
      this.$modal.confirm('是否确认删除任务编号为"' + ids + '"的数据项？').then(function () {
        return delTask(ids);
      }).then(() => {
        this.getList();
        this.$modal.msgSuccess("删除成功");
      }).catch(() => {
      });
    },
    /** 导出按钮操作 */
    handleExport() {
      this.download('task/task/export', {
        ...this.queryParams
      }, `task_${new Date().getTime()}.xlsx`)
    },
    getLoginUser() {
      getUserProfile().then(response => {
        this.loginUser = response.data
      });
    },
    resetRecord() {
      this.formRecord = {
        businessId:null,
        id: null,
        cid: null,
        assignerId: null,
        uid: null,
        traceType: null,
        traceContent: null,
        tracePic: null,
        createBy: null,
        createTime: null,
        updateBy: null,
        updateTime: null,
        remark: null,
        taskId: null
      };
      this.resetForm("formRecord");
    },
    // 图片上传前校检格式和大小
    beforeUpload(file) {
      const isLt2M = file.size / 1024 / 1024 < 2;
      if (!isLt2M) {
        this.$message.error('上传图片大小不能超过 2MB!');
      }
      return isLt2M;
    },
    // 图片文件个数超出
    handleExceed() {
      this.$message.error(`只允许上传单个文件`);
    },
    handleSuccess(res, file) {
      this.$message.success("上传成功");
      this.formRecord.tracePic = res.url;
    },
    // 图片上传失败
    handleUploadError(err) {
      this.$message.error("上传失败, 请重试");
    },
    /** 提交跟进记录 */
    submitRecordForm() {
      this.$refs["formRecord"].validate(valid => {
        if (valid) {
          this.formRecord.uid = this.loginUser.userId
          this.formRecord.cid = this.cid
          this.formRecord.assignerId = this.zhipaiUserId
          this.formRecord.taskId = this.taskId
          addProgess(this.formRecord).then(response => {
            this.$modal.msgSuccess("保存成功");
            this.openRecord = false;
            this.getList();
          });
        }
      });
    },
    // 取消按钮
    cancelRecord() {
      this.openRecord = false;
      this.resetRecord();
    },
  }
};
</script>
<style>
.avatar-uploader .el-upload {
  border: 1px dashed #d9d9d9;
  border-radius: 6px;
  cursor: pointer;
  position: relative;
  overflow: hidden;
}

.avatar-uploader .el-upload:hover {
  border-color: #409EFF;
}

.avatar-uploader-icon {
  font-size: 28px;
  color: #8c939d;
  width: 178px;
  height: 178px;
  line-height: 178px;
  text-align: center;
}

.avatar {
  width: 178px;
  height: 178px;
  display: block;
}
</style>
