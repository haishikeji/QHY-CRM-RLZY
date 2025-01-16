<template>
  <div class="app-container">
    <el-tabs v-model="activeName" @tab-click="handleClick">
      <el-tab-pane label="全部客户" name="first"></el-tab-pane>
      <el-tab-pane label="联系中客户" name="second"></el-tab-pane>
      <el-tab-pane label="今日已跟进" name="third"></el-tab-pane>
      <el-tab-pane label="30天未跟进" name="fourth"></el-tab-pane>
      <el-tab-pane label="从未跟进" name="fifth"></el-tab-pane>
    </el-tabs>
    <el-form :model="queryParams" ref="queryForm" size="small" :inline="true" v-show="showSearch" label-width="68px">
      <el-form-item label="客户名称" prop="name">
        <el-input
          v-model="queryParams.name"
          placeholder="请输入客户名称"
          clearable
          @keyup.enter.native="handleQuery"
        />
      </el-form-item>
      <el-form-item label="手机号码" prop="selectPhone">
        <el-input
          v-model="queryParams.selectPhone"
          placeholder="请输入手机号码"
          clearable
          @keyup.enter.native="handleQuery"
        />
      </el-form-item>
      <el-form-item label="省级" prop="provinceCode">
        <el-select v-model="queryParams.provinceCode" filterable clearable placeholder="请选择省级地区"
                   @change="getCity(queryParams.provinceCode,'add','query')">
          <el-option
            v-for="dict in provinceOptions"
            :key="dict.id"
            :label="dict.name"
            :value="dict.id"
          ></el-option>
        </el-select>
      </el-form-item>
      <el-form-item label="市级" prop="cityCode">
        <el-select v-model="queryParams.cityCode" filterable clearable placeholder="请选择市级地区"
                   @change="getArea(queryParams.cityCode,'add','query')">
          <el-option
            v-for="dict in cityOptions"
            :key="dict.id"
            :label="dict.name"
            :value="dict.id"
          ></el-option>
        </el-select>
      </el-form-item>
      <el-form-item label="区级" prop="areaCode">
        <el-select v-model="queryParams.areaCode" filterable clearable placeholder="请选择区级地区">
          <el-option
            v-for="dict in areaOptions"
            :key="dict.id"
            :label="dict.name"
            :value="dict.id"
          ></el-option>
        </el-select>
      </el-form-item>
      <el-form-item label="数据状态" prop="state">
        <el-select v-model="queryParams.state" placeholder="请选择" filterable clearable>
          <el-option
            v-for="dict in stateOptions"
            :key="dict.dictValue"
            :label="dict.dictLabel"
            :value="dict.dictValue"
          ></el-option>
        </el-select>
      </el-form-item>
      <el-form-item>
        <el-button type="primary" icon="el-icon-search" size="mini" @click="handleQuery">搜索</el-button>
        <el-button icon="el-icon-refresh" size="mini" @click="resetQuery">重置</el-button>
      </el-form-item>
    </el-form>

    <el-row :gutter="10" class="mb8">
      <el-col :span="1.5">
        <el-button
          type="primary"
          plain
          icon="el-icon-plus"
          size="mini"
          @click="handleImport"
        >导入
        </el-button>
      </el-col>
      <el-col :span="1.5">
        <el-button
          type="primary"
          plain
          icon="el-icon-plus"
          size="mini"
          @click="handleAdd"
          v-hasPermi="['customer:private:add']"
        >新增
        </el-button>
      </el-col>
      <el-col :span="1.5">
        <el-button
          type="success"
          plain
          icon="el-icon-edit"
          size="mini"
          :disabled="single"
          @click="handleUpdate"
          v-hasPermi="['customer:private:edit']"
        >修改
        </el-button>
      </el-col>
      <el-col :span="1.5">
        <el-button
          type="danger"
          plain
          icon="el-icon-delete"
          size="mini"
          :disabled="multiple"
          @click="handleDelete"
          v-hasPermi="['customer:private:remove']"
        >删除
        </el-button>
      </el-col>

      <right-toolbar :showSearch.sync="showSearch" @queryTable="getList"></right-toolbar>
    </el-row>

    <el-table v-loading="loading" :data="customerList" @selection-change="handleSelectionChange">
      <el-table-column type="selection" width="55" align="center"/>
      <el-table-column label="id" align="center" prop="id"/>
      <el-table-column label="客户名称" align="center" prop="name"/>
      <el-table-column label="手机号码" align="center" prop="phone"/>
      <el-table-column label="过期时间" align="center" prop="updateTime"/>
      <el-table-column label="微信号" align="center" prop="wechat"/>
      <el-table-column label="邮箱" align="center" prop="email"/>
      <el-table-column label="客户规模" align="center" prop="firmSize">
        <template slot-scope="scope">
          <dict-tag :options="dict.type.crm_customer_size" :value="scope.row.firmSize"/>
        </template>
      </el-table-column>
      <el-table-column label="客户类型" align="center" prop="type">
        <template slot-scope="scope">
          <dict-tag :options="dict.type.crm_customer_type" :value="scope.row.type"/>
        </template>
      </el-table-column>
      <el-table-column label="数据状态" align="center" prop="state">
        <template slot-scope="scope">
          <dict-tag :options="dict.type.crm_customer_state" :value="scope.row.state"/>
        </template>
      </el-table-column>
      <el-table-column label="销售人员" align="center" prop="mainSalerUser.userName"/>
      <el-table-column label="操作" align="center" class-name="small-padding fixed-width">
        <template slot-scope="scope">
          <el-button
            size="mini"
            type="text"
            icon="el-icon-search"
            @click="handleRoam(scope.row)"
          >流转记录
          </el-button>
          <el-button
            size="mini"
            type="text"
            icon="el-icon-edit"
            @click="handleFailure(scope.row)"
          >转失败客户
          </el-button>
          <el-button
            size="mini"
            type="text"
            icon="el-icon-edit"
            @click="handleFollow(scope.row)"
          >跟进记录
          </el-button>
          <el-button
            size="mini"
            type="text"
            icon="el-icon-edit"
            @click="handleLook(scope.row)"
          >查看详细
          </el-button>
          <el-button
            v-if="scope.row.status==='6'"
            size="mini"
            type="text"
            icon="el-icon-edit"
            v-hasPermi="['customer:private:edit']"
            @click="handleDefer(scope.row)"
          >申请延期
          </el-button>
          <el-button
            v-if="scope.row.status==='6'"
            size="mini"
            type="text"
            icon="el-icon-edit"
            @click="handleHelp(scope.row)"
            v-hasPermi="['customer:private:edit']"
          >求助
          </el-button>
          <el-button
            size="mini"
            type="text"
            icon="el-icon-edit"
            @click="handleWrite(scope.row)"
            v-hasPermi="['customer:private:write']"
          >写跟进
          </el-button>
          <el-button
            size="mini"
            type="text"
            icon="el-icon-edit"
            @click="handleBuild(scope.row)"
            v-hasPermi="['customer:private:build']"
          >建任务
          </el-button>
          <el-button
            size="mini"
            type="text"
            icon="el-icon-edit"
            @click="handleAssign(scope.row)"
            v-hasPermi="['customer:private:assign']"
          >指派
          </el-button>
          <el-button
            size="mini"
            type="text"
            icon="el-icon-edit"
            @click="handleSign(scope.row)"
            v-hasPermi="['customer:private:edit']"
          >签约
          </el-button>
          <el-button
            size="mini"
            type="text"
            icon="el-icon-edit"
            @click="handleUpdate(scope.row)"
            v-hasPermi="['customer:private:edit']"
          >修改
          </el-button>
          <el-button
            size="mini"
            type="text"
            icon="el-icon-delete"
            @click="handleDelete(scope.row)"
            v-hasPermi="['customer:private:remove']"
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

    <!-- 添加或修改客户对话框 -->
    <el-dialog :title="title" :visible.sync="open" width="500px" append-to-body>
      <el-form ref="form" :model="form" :rules="rules" label-width="80px">
        <el-form-item label="客户名称" prop="name">
          <el-input v-model="form.name" placeholder="请输入客户名称"/>
        </el-form-item>
        <el-form-item label="手机号码" prop="phone">
          <el-input v-model="form.phone" placeholder="请输入手机号码"/>
        </el-form-item>
        <el-form-item label="省级" prop="provinceCode">
          <el-select v-model="form.provinceCode" placeholder="请选择省级地区" @change="getCity(form.provinceCode,'add')">
            <el-option
              v-for="dict in provinceOptions"
              :key="dict.id"
              :label="dict.name"
              :value="dict.id"
            ></el-option>
          </el-select>
        </el-form-item>
        <el-form-item label="市级" prop="cityCode">
          <el-select v-model="form.cityCode" placeholder="请选择市级地区" @change="getArea(form.cityCode,'add')">
            <el-option
              v-for="dict in cityOptions"
              :key="dict.id"
              :label="dict.name"
              :value="dict.id"
            ></el-option>
          </el-select>
        </el-form-item>
        <el-form-item label="区级" prop="areaCode">
          <el-select v-model="form.areaCode" placeholder="请选择区级地区">
            <el-option
              v-for="dict in areaOptions"
              :key="dict.id"
              :label="dict.name"
              :value="dict.id"
            ></el-option>
          </el-select>
        </el-form-item>
        <el-form-item label="地址" prop="address">
          <el-input v-model="form.address" placeholder="请输入地址"/>
        </el-form-item>
        <el-form-item label="企业简介" prop="detail">
          <el-input v-model="form.detail" type="textarea" placeholder="请输入内容"/>
        </el-form-item>
        <el-form-item label="微信号" prop="wechat">
          <el-input v-model="form.wechat" placeholder="请输入微信号"/>
        </el-form-item>
        <el-form-item label="邮箱" prop="email">
          <el-input v-model="form.email" placeholder="请输入邮箱"/>
        </el-form-item>
        <el-form-item label="客户类型" prop="type">
          <el-select v-model="form.type" placeholder="请选择" filterable clearable :disabled="form.id!=null">
            <el-option
              v-for="dict in typeOptions"
              :key="dict.dictValue"
              :label="dict.dictLabel"
              :value="dict.dictValue"
            ></el-option>
          </el-select>
        </el-form-item>
        <el-form-item label="集团客户" prop="pid" v-if="this.form.type==='3'">
          <el-select v-model="form.pid" placeholder="请选择" filterable clearable >
            <el-option
              v-for="pCustomer in customerOptions"
              :key="pCustomer.id"
              :label="pCustomer.name"
              :value="pCustomer.id"
            ></el-option>
          </el-select>
        </el-form-item>
        <el-form-item label="客户规模" prop="firmSize">
          <el-select v-model="form.firmSize" placeholder="请选择" filterable clearable>
            <el-option
              v-for="dict in sizeOptions"
              :key="dict.dictValue"
              :label="dict.dictLabel"
              :value="dict.dictValue"
            ></el-option>
          </el-select>
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

    <!-- 指派   -->
    <el-dialog :title="titleAssign" :visible.sync="openAssign" width="500px" append-to-body>
      <el-form ref="formAssign" :model="formAssign" :rules="rulesAssign" label-width="80px">
        <el-form-item label="销售人员" prop="uid">
          <el-select v-model="formAssign.uid" placeholder="请选择" filterable clearable>
            <el-option
              v-for="user in userOptions"
              :key="user.userId"
              :label="user.userName"
              :value="user.userId"
            ></el-option>
          </el-select>
        </el-form-item>
      </el-form>
      <div slot="footer" class="dialog-footer">
        <el-button type="primary" @click="submitFormAssign">确 定</el-button>
        <el-button @click="cancelAssign">取 消</el-button>
      </div>
    </el-dialog>

    <!-- 申请延期 -->
    <el-dialog :title="titleDefer" :visible.sync="openDefer" width="500px" append-to-body>
      <el-form ref="formDefer" :model="formDefer" :rules="rulesDefer" label-width="80px">
        <el-form-item label="延期时间" prop="defer">
          <el-select v-model="formDefer.defer" placeholder="请选择" filterable clearable>
            <el-option label="1个月" value="1"></el-option>
            <el-option label="2个月" value="2"></el-option>
            <el-option label="3个月" value="3"></el-option>
          </el-select>
        </el-form-item>
        <el-form-item label="审核人员" prop="accepterId">
          <el-select v-model="formDefer.accepterId" placeholder="请选择" filterable clearable>
            <el-option
              v-for="user in accepterOptions"
              :key="user.userId"
              :label="user.userName"
              :value="user.userId"
            ></el-option>
          </el-select>
        </el-form-item>
        <el-form-item label="抄送人员" prop="copyIdList">
          <el-select v-model="formDefer.copyIdList" placeholder="请选择" multiple filterable clearable>
            <el-option
              v-for="user in copyOptions"
              :key="user.userId"
              :label="user.userName"
              :value="user.userId"
            ></el-option>
          </el-select>
        </el-form-item>
        <el-form-item label="申请原因" prop="content">
          <el-input v-model="formDefer.content" type="textarea" placeholder="请输入"/>
        </el-form-item>
      </el-form>
      <div slot="footer" class="dialog-footer">
        <el-button type="primary" @click="submitFormDefer">确 定</el-button>
        <el-button @click="cancelDefer">取 消</el-button>
      </div>
    </el-dialog>

    <!-- 写跟进 -->
    <el-dialog :title="titleWrite" :visible.sync="openWrite" width="500px" append-to-body>
      <el-form ref="formWrite" :model="formWrite" :rules="rulesWrite" label-width="80px">
        <el-form-item label="跟进方式" prop="traceType">
          <el-select v-model="formWrite.traceType" placeholder="请选择" filterable clearable>
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
            v-model="formWrite.createTime"
            value-format="yyyy-MM-dd"
            type="date"
            placeholder="请选择"
          ></el-date-picker>
        </el-form-item>
        <el-form-item label="跟进内容" prop="traceContent">
          <el-input type="textarea" v-model="formWrite.traceContent" placeholder="请输入"/>
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
            <img v-if="formWrite.tracePic" :src="formWrite.tracePic" class="avatar">
            <i v-else class="el-icon-plus avatar-uploader-icon"></i>
          </el-upload>
        </el-form-item>
        <el-form-item label="业务类型" prop="businessId">
          <el-select v-model="formWrite.businessId" placeholder="请选择" filterable clearable>
            <el-option
              v-for="business in businessList"
              :key="business.id"
              :label="business.name"
              :value="business.id"
            ></el-option>
          </el-select>
        </el-form-item>
        <el-form-item label="备注" prop="remark">
          <el-input v-model="formWrite.remark" type="textarea" placeholder="请输入内容"/>
        </el-form-item>
      </el-form>
      <div slot="footer" class="dialog-footer">
        <el-button type="primary" @click="submitFormWrite">确 定</el-button>
        <el-button @click="cancelWrite">取 消</el-button>
      </div>
    </el-dialog>

    <!-- 建任务 -->
    <el-dialog :title="titleBuild" :visible.sync="openBuild" width="500px" append-to-body>
      <el-form ref="formBuild" :model="formBuild" :rules="rulesBuild" label-width="80px">
        <el-form-item label="接收人" prop="accepterId">
          <el-select v-model="formBuild.accepterId" placeholder="请选择" filterable clearable>
            <el-option
              v-for="user in userOptions"
              :key="user.userId"
              :label="user.userName"
              :value="user.userId"
            ></el-option>
          </el-select>
        </el-form-item>
        <el-form-item label="类型" prop="type">
          <el-select v-model="formBuild.type" placeholder="请选择" filterable clearable>
            <el-option
              v-for="dict in taskTypeOptions"
              :key="dict.dictValue"
              :label="dict.dictLabel"
              :value="dict.dictValue"
            ></el-option>
          </el-select>
        </el-form-item>
        <el-form-item label="任务反馈" prop="feedback">
          <el-input type="textarea" v-model="formBuild.feedback" placeholder="请输入"/>
        </el-form-item>
        <el-form-item label="备注" prop="remark">
          <el-input v-model="formBuild.remark" type="textarea" placeholder="请输入内容"/>
        </el-form-item>
      </el-form>
      <div slot="footer" class="dialog-footer">
        <el-button type="primary" @click="submitFormBuild">确 定</el-button>
        <el-button @click="cancelBuild">取 消</el-button>
      </div>
    </el-dialog>

    <!-- 显示流转列表 -->
    <el-dialog title="流转记录" :visible.sync="roamOpen" width="1000px" append-to-body>
      <div>
        <el-table :data="roamList" style="margin-top: 10px">
          <el-table-column label="id" align="center" prop="id"/>
          <el-table-column label="客户名称" align="center" prop="crmCustomer.name"/>
          <el-table-column label="操作人" align="center" prop="createBy"/>
          <el-table-column label="操作时间" align="center" prop="createTime"/>
          <el-table-column label="内容" align="center" prop="content"/>
        </el-table>
      </div>
      <pagination
        v-show="roamTotal>0"
        :total="roamTotal"
        :page.sync="queryParamsRoam.pageNum"
        :limit.sync="queryParamsRoam.pageSize"
        @pagination="handleRoam"
      />
    </el-dialog>

    <!-- 显示跟进列表 -->
    <el-dialog title="跟进记录" :visible.sync="followOpen" width="1000px" append-to-body>
      <div>
        <el-table :data="followList" style="margin-top: 10px">
          <el-table-column label="id" align="center" prop="id"/>
          <el-table-column label="客户名称" align="center" prop="customer.name"/>
          <el-table-column label="指派人" align="center" prop="assignerUser.userName"/>
          <el-table-column label="销售员" align="center" prop="sysUser.userName"/>
          <el-table-column label="跟进方式" align="center" prop="traceType">
            <template slot-scope="scope">
              <dict-tag :options="dict.type.trace_type" :value="scope.row.traceType"/>
            </template>
          </el-table-column>
          <el-table-column label="业务类型" align="center" prop="business.name"></el-table-column>
          <el-table-column label="跟进时间" align="center" prop="createTime"></el-table-column>
          <el-table-column label="录入时间" align="center" prop="updateTime"></el-table-column>
          <el-table-column label="跟进内容" align="center" prop="traceContent"/>
          <el-table-column label="图片" align="center" prop="tracePic">
            <template v-if="scope.row.tracePic!=null&&scope.row.tracePic!==''" width="60" slot-scope="scope">
              <el-popover placement="top-start" title="" trigger="hover">
                <img :src="scope.row.tracePic" alt="" style="height: 200px">
                <img slot="reference" :src="scope.row.tracePic" style="width: 60px;height: 60px">
              </el-popover>
            </template>
          </el-table-column>
          <el-table-column label="备注" align="center" prop="remark"/>
        </el-table>
      </div>
      <pagination
        v-show="followTotal>0"
        :total="followTotal"
        :page.sync="queryParamsFollow.pageNum"
        :limit.sync="queryParamsFollow.pageSize"
        @pagination="handleFollow"
      />
    </el-dialog>

    <!-- 查看详细 -->
    <el-dialog :title="titleLook" :visible.sync="openLook" width="500px" append-to-body>
      <el-form ref="form" :model="form"  label-width="80px">
        <el-form-item label="客户名称" prop="name">
          <el-input v-model="form.name" placeholder="请输入客户名称" disabled/>
        </el-form-item>
        <el-form-item label="手机号码" prop="phone">
          <el-input v-model="form.phone" placeholder="请输入手机号码" disabled/>
        </el-form-item>

        <el-form-item label="省级" prop="provinceCode">
          <el-select v-model="form.provinceCode" placeholder="请选择省级地区" @change="getCity(form.provinceCode,'add')" disabled>
            <el-option
              v-for="dict in provinceOptions"
              :key="dict.id"
              :label="dict.name"
              :value="dict.id"
            ></el-option>
          </el-select>
        </el-form-item>
        <el-form-item label="市级" prop="cityCode">
          <el-select v-model="form.cityCode" placeholder="请选择市级地区" @change="getArea(form.cityCode,'add')" disabled>
            <el-option
              v-for="dict in cityOptions"
              :key="dict.id"
              :label="dict.name"
              :value="dict.id"
            ></el-option>
          </el-select>
        </el-form-item>
        <el-form-item label="区级" prop="areaCode">
          <el-select v-model="form.areaCode" placeholder="请选择区级地区" disabled>
            <el-option
              v-for="dict in areaOptions"
              :key="dict.id"
              :label="dict.name"
              :value="dict.id"
            ></el-option>
          </el-select>
        </el-form-item>
        <el-form-item label="地址" prop="address">
          <el-input v-model="form.address" placeholder="请输入地址" disabled/>
        </el-form-item>
        <el-form-item label="企业简介" prop="detail">
          <el-input v-model="form.detail" type="textarea" placeholder="请输入内容" disabled/>
        </el-form-item>
        <el-form-item label="备注" prop="remark">
          <el-input v-model="form.remark" type="textarea" placeholder="请输入内容" disabled/>
        </el-form-item>
      </el-form>
    </el-dialog>

    <!-- 用户导入对话框 -->
    <el-dialog :title="upload.title" :visible.sync="upload.open" width="400px" append-to-body>
      <el-upload
        ref="upload"
        :limit="1"
        accept=".xlsx, .xls"
        :headers="upload.headers"
        :action="upload.url + '?updateSupport=' + upload.updateSupport"
        :disabled="upload.isUploading"
        :on-progress="handleFileUploadProgress"
        :on-success="handleFileSuccess"
        :auto-upload="false"
        drag
      >
        <i class="el-icon-upload"></i>
        <div class="el-upload__text">将文件拖到此处，或<em>点击上传</em></div>
        <div class="el-upload__tip text-center" slot="tip">
          <span>仅允许导入xls、xlsx格式文件。</span>
          <el-link type="primary" :underline="false" style="font-size:12px;vertical-align: baseline;"
                   @click="importTemplate" target="_self">下载模板
          </el-link>
        </div>
      </el-upload>
      <div slot="footer" class="dialog-footer">
        <el-button type="primary" @click="submitFileForm">确 定</el-button>
        <el-button @click="upload.open = false">取 消</el-button>
      </div>
    </el-dialog>

  </div>
</template>

<script>
import {
  addAudit,
  addCustomer,
  addOrUpdateSalerAssign,
  delCustomer,
  getAccepterAndCopyList,
  getCustomer,
  helpCustomer,
  listCustomerNotPage,
  listPrivateCustomer,
  signCustomer,
  updateCustomer
} from "@/api/customer/intentionCustomer";
import {getUser, getUserProfile, listUserNotPage} from "@/api/system/user";
import {listAreaAll} from "@/api/area/area";
import {getToken} from "@/utils/auth";
import {addProgess, getBusinessList, listProgess} from "@/api/progess/progess";
import {addTask} from "@/api/task/task";
import {listTranslog} from "@/api/translog/translog";
import commonUrl from "@/utils/url";

export default {
  name: "Customer",
  dicts: ['crm_customer_state', 'crm_customer_size', 'crm_customer_type', 'trace_type', 'task_type', 'task_status'],
  data() {
    return {
      businessList: [],
      roamOpen:false,
      roamList:[],
      followOpen:false,
      followList:[],
      uploadFileUrl: process.env.VUE_APP_BASE_API + "/api/oss/upload", // 上传的图片服务器地址
      headers: {
        Authorization: "Bearer " + getToken(),
      },
      activeName: "first",
      // 弹出层标题
      titleAssign: "",
      // 是否显示弹出层
      openAssign: false,
      // 省级
      provinceOptions: [],
      // 市级
      cityOptions: [],
      // 区级
      areaOptions: [],
      userOptions: [],
      stateOptions: [],
      sizeOptions: [],
      typeOptions: [],
      traceTypeOptions: [],
      taskTypeOptions: [],
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
      followTotal: 0,
      roamTotal: 0,
      // 客户表格数据
      customerOptions: [],
      customerList: [],
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
        name: null,
        phone: null,
        rankTag: null,
        //status: 1,
        queryStatusList: ["1", "6"],
        state: null,
        provinceCode: null,
        cityCode: null,
        areaCode: null,
        address: null,
        detail: null,
        wechat: null,
        email: null,
        type: null,
        firmSize: null,
        oper: "0",
        selectPhone:null,
      },
      // 查询参数
      queryParamsFollow: {
        orderByColumn: 'm.create_time',
        isAsc: 'desc',
        pageNum: 1,
        pageSize: 10,
        cid: null,
      },
      // 查询参数
      queryParamsRoam: {
        orderByColumn: 'm.create_time',
        isAsc: 'desc',
        pageNum: 1,
        pageSize: 10,
        cid: null,
      },
      // 表单参数
      form: {
        mainSalerUser: {},
        assistSalerUser: {}
      },
      formAssign: {},
      formWrite: {},
      formDefer: {},
      formBuild: {},
      // 表单校验
      rules: {
        pid: [
          {required: true, message: "集团客户不能为空", trigger: "blur"}
        ],
        name: [
          {required: true, message: "客户名称不能为空", trigger: "blur"}
        ],
        phone: [
          { required: true, message: "手机号码不能为空", trigger: "blur" },
          {
            pattern: /^1[3|4|5|6|7|8|9][0-9]\d{8}$/,
            message: "请输入正确的手机号码",
            trigger: "blur"
          }
        ],
        provinceCode: [
          {required: true, message: "省级不能为空", trigger: "blur"}
        ],
        address: [
          {required: true, message: "详细地址不能为空", trigger: "blur"}
        ],
        email: [
          {required: false, message: "邮箱地址不能为空", trigger: "blur"},
          {type: 'email', message: '请输入正确的邮箱地址', trigger: 'blur'}
        ],
        type: [
          {required: true, message: "客户类型不能为空", trigger: "blur"},
        ],
        firmSize: [
          {required: true, message: "客户规模不能为空", trigger: "blur"},
        ],
      },
      // 表单校验
      rulesDefer: {
        defer: [
          {required: true, message: "延期时间不能为空", trigger: "blur"},
        ],
        accepterId: [
          {required: true, message: "接受人员不能为空", trigger: "blur"},
        ],
        copyIdList: [
          {required: true, message: "抄送人员不能为空", trigger: "blur"},
        ],
        content: [
          {required: true, message: "申请原因不能为空", trigger: "blur"},
        ],
      },
      // 表单校验
      rulesAssign: {
        uid: [
          {required: true, message: "销售人员不能为空", trigger: "blur"},
        ],
      },
      // 表单校验
      rulesWrite: {
        createTime: [
          {required: true, message: "跟进时间不能为空", trigger: "blur"},
        ],
        traceType: [
          {required: true, message: "跟进方式不能为空", trigger: "blur"},
        ],
        traceContent: [
          {required: true, message: "跟进内容不能为空", trigger: "blur"},
        ],
        businessId: [
          {required: true, message: "业务类型不能为空", trigger: "blur"},
        ],
      },
      rulesBuild: {
        accepterId: [
          {required: true, message: "接受人不能为空", trigger: "blur"},
        ],
        feedback: [
          {required: true, message: "任务反馈不能为空", trigger: "blur"},
        ],
        type: [
          {required: true, message: "类型不能为空", trigger: "blur"},
        ],
      },
      cid: null,
      uid: null,
      openDefer: false,
      titleDefer: '',
      openWrite: false,
      titleWrite: '',
      openBuild: false,
      titleBuild: '',
      titleLook: '',
      openLook: false,
      loginUser: {},
      accepterOptions: [],
      copyOptions: [],
      upload: {
        // 是否显示弹出层（用户导入）
        open: false,
        // 弹出层标题（用户导入）
        title: "",
        // 是否禁用上传
        isUploading: false,
        // 是否更新已经存在的用户数据
        updateSupport: 0,
        // 设置上传的请求头部
        headers: {Authorization: "Bearer " + getToken()},
        // 上传的地址
        url: process.env.VUE_APP_BASE_API + "/customer/intention/importDataPrivate"
      },
    };
  },
  created() {
    this.getCustomerList()
    this.getBusinessList()
    this.getLoginUser()
    this.getProvince()
    this.getList();
    this.getUserList();
    this.getDicts("crm_customer_state").then(response => {
      this.stateOptions = response.data;
    });
    this.getDicts("crm_customer_size").then(response => {
      this.sizeOptions = response.data;
    });
    this.getDicts("crm_customer_type").then(response => {
      this.typeOptions = response.data;
    });
    this.getDicts("trace_type").then(response => {
      this.traceTypeOptions = response.data;
    });
    this.getDicts("task_type").then(response => {
      this.taskTypeOptions = response.data;
    });
  },
  methods: {
    /** 查询业务类型列表 */
    getBusinessList() {
      getBusinessList().then(response => {
        this.businessList = response.data;
      });
    },
    /** 下载模板操作 */
    importTemplate() {
      const script = document.createElement('a')
      script.setAttribute('href', commonUrl.openseasExcel)
      script.setAttribute('target', '_self')
      script.setAttribute('download', '私有客户模板.xlsx')
      document.body.appendChild(script)
      script.click()
      document.body.removeChild(script)
    },
    // 文件上传中处理
    handleFileUploadProgress(event, file, fileList) {
      this.upload.isUploading = true;
    },
    // 文件上传成功处理
    handleFileSuccess(response, file, fileList) {
      this.upload.open = false;
      this.upload.isUploading = false;
      this.$refs.upload.clearFiles();
      this.$alert("<div style='overflow: auto;overflow-x: hidden;max-height: 70vh;padding: 10px 20px 0;'>" + response.msg + "</div>", "导入结果", {dangerouslyUseHTMLString: true});
      this.getList();
    },
    // 提交上传文件
    submitFileForm() {
      this.$refs.upload.submit();
    },
    /** 导入按钮操作 */
    handleImport() {
      this.upload.title = "客户导入";
      this.upload.open = true;
    },
    //打开跟进记录
    handleRoam(val) {
      if(val&&val.id){
        this.cid = val.id
        this.queryParamsRoam.cid = val.id
      }else {
        this.queryParamsRoam.cid=this.cid
      }
      listTranslog(this.queryParamsRoam).then(response => {
        this.roamList = response.rows;
        this.roamTotal = response.total;
        this.roamOpen = true;
      });
    },
    //打开跟进记录
    handleFollow(val) {
      if(val&&val.id){
        this.cid = val.id
        this.queryParamsFollow.cid = val.id
      }else {
        this.queryParamsFollow.cid=this.cid
      }
      listProgess(this.queryParamsFollow).then(response => {
        this.followList = response.rows;
        this.followTotal = response.total;
        this.followOpen = true;
      });
    },
    /** 查询接受列表 */
    getAccepterAndCopyList() {
      getAccepterAndCopyList().then(response => {
        this.accepterOptions = response.data.accepterUserList;
        this.copyOptions = response.data.copyUserList;
      });
    },
    // 取消按钮
    cancelDefer() {
      this.openDefer = false;
      this.resetDefer();
    },
    /** 提交按钮 */
    submitFormDefer() {
      this.$refs["formDefer"].validate(valid => {
        if (valid) {
          this.formDefer.cid=this.cid
          addAudit(this.formDefer).then(response => {
            this.$modal.msgSuccess("保存成功");
            this.openDefer = false;
            this.getList();
          });
        }
      });
    },
    // 表单重置
    resetDefer() {
      this.formDefer = {
        id: null,
        cid: null,
        sponsorId: null,
        accepterId: null,
        copyId: null,
        content: null,
        copyIdList:null,
        status: null,
        createBy: null,
        createTime: null,
        updateBy: null,
        updateTime: null,
        remark: null,
        defer: null,
      };
      this.uid = null
      this.resetForm("formDefer");
    },
    /** 查看详细按钮操作 */
    handleLook(row) {
      this.reset();
      const id = row.id
      getCustomer(id).then(response => {
        this.form = response.data;
        this.openLook = true;
        this.titleLook = "详细信息";
        this.getCity(this.form.provinceCode, "edit");
        this.getArea(this.form.cityCode, "edit");
      });
    },
    /** 申请延期按钮操作 */
    handleDefer(row) {
      this.resetDefer();
      this.cid = row.id
      this.openDefer = true;
      this.titleDefer = "申请延期";
      this.getAccepterAndCopyList()
    },
    getCustomerList() {
      let query = {
        type: "2"
      }
      listCustomerNotPage(query).then(response => {
        this.customerOptions = response.data;
      });
    },
    getLoginUser() {
      getUserProfile().then(response => {
        this.loginUser = response.data
      });
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
      this.formWrite.tracePic = res.url;
    },
    // 图片上传失败
    handleUploadError(err) {
      this.$message.error("上传失败, 请重试");
    },
    // 表单重置
    resetWrite() {
      this.formWrite = {
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
      };
      this.uid = null
      this.resetForm("formWrite");
    },
    // 表单重置
    resetBuild() {
      this.formBuild = {
        id: null,
        cid: null,
        assignerId: null,
        accepterId: null,
        feedback: null,
        status: null,
        type: null,
        createBy: null,
        createTime: null,
        updateBy: null,
        updateTime: null,
        remark: null,
      };
      this.uid = null
      this.resetForm("formBuild");
    },
    /** 写跟进按钮操作 */
    handleWrite(row) {
      this.resetWrite();
      this.cid = row.id
      this.uid = row.mainSalerUser.userId
      this.openWrite = true;
      this.titleWrite = "我的跟进";
    },
    /** 建任务按钮操作 */
    handleBuild(row) {
      this.resetBuild();
      this.cid = row.id
      this.openBuild = true;
      this.titleBuild = "创建任务";
    },
    handleClick(tab) {
      this.queryParams.oper = tab.index
      this.getList()
    },
    /** 提交按钮 */
    submitFormBuild() {
      this.$refs["formBuild"].validate(valid => {
        if (valid) {
          this.formBuild.cid = this.cid
          addTask(this.formBuild).then(response => {
            this.$modal.msgSuccess("保存成功");
            this.openBuild = false;
            this.getList();
          });
        }
      });
    },
    // 取消按钮
    cancelBuild() {
      this.openBuild = false;
      this.resetBuild();
    },
    /** 提交按钮 */
    submitFormWrite() {
      this.$refs["formWrite"].validate(valid => {
        if (valid) {
          this.formWrite.uid = this.loginUser.userId
          this.formWrite.cid = this.cid
          this.formWrite.assignerId = this.loginUser.userId
          addProgess(this.formWrite).then(response => {
            this.$modal.msgSuccess("保存成功");
            this.openWrite = false;
            this.getList();
          });
        }
      });
    },
    // 取消按钮
    cancelWrite() {
      this.openWrite = false;
      this.resetWrite();
    },
    // 取消按钮
    cancelAssign() {
      this.openAssign = false;
      this.resetAssign();
    },
    /** 提交按钮 */
    submitFormAssign() {
      this.$refs["formAssign"].validate(valid => {
        if (valid) {
          let data = {
            cid: this.cid,
            uid: this.formAssign.uid,
          }
          addOrUpdateSalerAssign(data).then(response => {
            this.$modal.msgSuccess("保存成功");
            this.openAssign = false;
            this.getList();
          });
        }
      });
    },
    //获取区级地区
    getArea(val, type, oper) {
      this.areaOptions = []
      if (type !== "edit" && oper !== "query") {
        this.$set(this.form, 'areaCode', '');
      }
      if (oper === "query") {
        this.$set(this.queryParams, 'areaCode', '');
      }
      const data = {
        parentId: val,
        isEnabled: 1
      }
      listAreaAll(data).then(response => {
        this.areaOptions = response.data;
      });
    },
    //获取市级地区
    getCity(val, type, oper) {
      this.cityOptions = []
      this.areaOptions = []
      if (type !== "edit" && oper !== "query") {
        this.$set(this.form, 'cityCode', '');
        this.$set(this.form, 'areaCode', '');
      }
      if (oper === "query") {
        this.$set(this.queryParams, 'cityCode', '');
        this.$set(this.queryParams, 'areaCode', '');
      }
      let data = {
        parentId: val,
        isEnabled: 1
      }
      listAreaAll(data).then(response => {
        this.cityOptions = response.data;
      });
    },
    //获取省级
    getProvince() {
      const data = {
        parentId: 0,
        isEnabled: 1
      }
      listAreaAll(data).then(response => {
        this.provinceOptions = response.data;
      });
    },
    /** 查询销售列表 */
    getUserList() {
      listUserNotPage().then(response => {
        this.userOptions = response.data;
      });
    },
    /** 查询客户列表 */
    getList() {
      this.loading = true;
      listPrivateCustomer(this.queryParams).then(response => {
        this.customerList=[]
        if(this.queryParams.oper=='1'||this.queryParams.oper=='2'||this.queryParams.oper=='3'){
          for (const e of response.rows) {
            this.customerList.push(e.customer)
          }
        }else {
          this.customerList = response.rows;
        }
        this.total = response.total;
        this.loading = false;
      });
    },
    // 取消按钮
    cancel() {
      this.open = false;
      this.reset();
    },
    // 表单重置
    reset() {
      this.form = {
        id: null,
        name: null,
        phone: null,
        rankTag: null,
        status: "0",
        state: null,
        provinceCode: null,
        cityCode: null,
        areaCode: null,
        address: null,
        detail: null,
        wechat: null,
        email: null,
        type: null,
        firmSize: null,
        createBy: null,
        createTime: null,
        updateBy: null,
        updateTime: null,
        remark: null,
        mainSalerUser: {},
        assistSalerUser: {},
        pid: null,
      };
      this.resetForm("form");
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
      this.title = "添加客户";
      this.getCustomerList()
    },
    /** 修改按钮操作 */
    handleUpdate(row) {
      this.reset();
      const id = row.id || this.ids
      getCustomer(id).then(response => {
        this.form = response.data;
        if (response.data.pid === 0) {
          this.form.pid = null
        }
        this.open = true;
        this.title = "修改客户";
        if(response.data.provinceCode!=null&&response.data.provinceCode!==''){
          this.getCity(this.form.provinceCode, "edit");
          this.getArea(this.form.cityCode, "edit");
        }
      });
    },
    // 表单重置
    resetAssign() {
      this.formAssign = {
        uid: null,
        userName: null
      };
      this.cid = null
      this.resetForm("formAssign");
    },
    /** 指派按钮操作 */
    handleAssign(row) {
      if(row.provinceCode==null||row.provinceCode===''){
        this.$modal.msgError("请先完善客户信息！");
        return;
      }
      this.resetAssign()
      this.openAssign = true;
      this.cid = row.id;
      this.titleAssign = "指派人员";
      if (row.mainSalerUser != null) {
        const id = row.mainSalerUser.userId
        getUser(id).then(response => {
          this.formAssign.userName = response.data.userName;
          this.formAssign.uid = row.mainSalerUser.userId;
        });
      }
    },
    /** 转失败按钮操作 */
    handleFailure(row) {
      this.$modal.confirm('是否确认将客户编号为"' + row.id + '"的数据项转为失败客户？').then(function() {
        let data = {
          state: "2",
          status:"0",
          id: row.id
        }
        return updateCustomer(data);
      }).then(() => {
        this.$modal.msgSuccess("操作成功！");
        this.getList();
      }).catch(() => {});
    },
    /** 求助按钮操作 */
    handleHelp(row) {
      if(row.provinceCode==null||row.provinceCode===''){
        this.$modal.msgError("请先完善客户信息！");
        return;
      }
      this.$modal.confirm('是否确认客户编号为"' + row.id + '"的数据项进行求助？').then(function() {
        const data = {id: row.id,status: "4"}
        return helpCustomer(data);
      }).then(() => {
        this.$modal.msgSuccess("求助成功！");
        this.getList();
      }).catch(() => {});
    },
    /** 签约按钮操作 */
    handleSign(row) {
      if(row.provinceCode==null||row.provinceCode===''){
        this.$modal.msgError("请先完善客户信息！");
        return;
      }
      this.$modal.confirm('是否确认签约客户编号为"' + row.id + '"的数据项？').then(function() {
        let data = {
          status: "2",
          id: row.id
        }
        return signCustomer(data);
      }).then(() => {
        this.$modal.msgSuccess("签约成功！");
        this.getList();
      }).catch(() => {});
    },
    /** 提交按钮 */
    submitForm() {
      this.$refs["form"].validate(valid => {
        if (valid) {
          if (this.form.id != null) {
            updateCustomer(this.form).then(response => {
              this.$modal.msgSuccess("修改成功");
              this.open = false;
              this.getList();
            });
          } else {
            this.form.status = "1"
            addCustomer(this.form).then(response => {
              this.$modal.msgSuccess("新增成功");
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
      this.$modal.confirm('是否确认删除客户编号为"' + ids + '"的数据项？').then(function () {
        return delCustomer(ids);
      }).then(() => {
        this.getList();
        this.$modal.msgSuccess("删除成功");
      }).catch(() => {
      });
    },
    /** 导出按钮操作 */
    handleExport() {
      this.download('customer/customer/export', {
        ...this.queryParams
      }, `customer_${new Date().getTime()}.xlsx`)
    }
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
