<template>
   <div class="app-container erp-page">
      <erp-page-header
         eyebrow="系统监控"
         title="在线用户"
         subtitle="实时监测当前活跃登录会话、终端环境与 IP 来源，支持异常会话强制下线。"
         icon="Connection"
         tone="blue"
      >
         <template #actions>
            <el-button icon="Refresh" @click="getList">刷新会话</el-button>
         </template>
      </erp-page-header>

      <div class="erp-filter-card">
         <el-form :model="queryParams" ref="queryRef" :inline="true">
            <el-form-item label="登录地址" prop="ipaddr">
               <el-input
                  v-model="queryParams.ipaddr"
                  placeholder="请输入登录地址"
                  clearable
                  style="width: 220px"
                  @keyup.enter="handleQuery"
               />
            </el-form-item>
            <el-form-item label="用户名称" prop="userName">
               <el-input
                  v-model="queryParams.userName"
                  placeholder="请输入用户名称"
                  clearable
                  style="width: 220px"
                  @keyup.enter="handleQuery"
               />
            </el-form-item>
            <el-form-item>
               <el-button type="primary" icon="Search" @click="handleQuery">搜索</el-button>
               <el-button icon="Refresh" @click="resetQuery">重置</el-button>
            </el-form-item>
         </el-form>
      </div>

      <div class="erp-table-card">
         <el-table
            v-loading="loading"
            :data="onlineList.slice((pageNum - 1) * pageSize, pageNum * pageSize)"
            style="width: 100%;"
         >
            <el-table-column label="序号" width="52" type="index" align="center">
               <template #default="scope">
                  <span>{{ (pageNum - 1) * pageSize + scope.$index + 1 }}</span>
               </template>
            </el-table-column>
            <el-table-column label="会话编号" align="left" prop="tokenId" min-width="155" :show-overflow-tooltip="true">
               <template #default="scope">
                  <span class="erp-code-chip">{{ scope.row.tokenId ? String(scope.row.tokenId).slice(0, 14) + '…' : '-' }}</span>
               </template>
            </el-table-column>
            <el-table-column label="用户 / 部门" align="left" prop="userName" min-width="118" :show-overflow-tooltip="true">
               <template #default="scope">
                  <div class="erp-cell-title">{{ scope.row.userName }}</div>
                  <div class="erp-cell-sub">{{ scope.row.deptName || '-' }}</div>
               </template>
            </el-table-column>
            <el-table-column label="主机 / 地点" align="left" min-width="120" :show-overflow-tooltip="true">
               <template #default="scope">
                  <div>{{ scope.row.ipaddr }}</div>
                  <div class="erp-cell-sub">{{ scope.row.loginLocation || '-' }}</div>
               </template>
            </el-table-column>
            <el-table-column label="终端环境" align="left" min-width="120" :show-overflow-tooltip="true">
               <template #default="scope">
                  <div>{{ scope.row.browser || '-' }}</div>
                  <div class="erp-cell-sub">{{ scope.row.os || '-' }}</div>
               </template>
            </el-table-column>
            <el-table-column label="登录时间" align="center" prop="loginTime" width="136">
               <template #default="scope">
                  <span>{{ parseTime(scope.row.loginTime, '{y}-{m}-{d} {h}:{i}') }}</span>
               </template>
            </el-table-column>
            <el-table-column label="操作" align="center" width="84" fixed="right" class-name="small-padding fixed-width">
               <template #default="scope">
                  <div class="erp-row-actions">
                     <el-button link type="danger" icon="Delete" @click="handleForceLogout(scope.row)" v-hasPermi="['monitor:online:forceLogout']">强退</el-button>
                  </div>
               </template>
            </el-table-column>
         </el-table>

         <pagination v-show="total > 0" :total="total" v-model:page="pageNum" v-model:limit="pageSize" />
      </div>
   </div>
</template>

<script setup name="Online">
import { forceLogout, list as initData } from "@/api/monitor/online"
import { parseTime } from "@/utils/ruoyi"

const { proxy } = getCurrentInstance()

const onlineList = ref([])
const loading = ref(true)
const total = ref(0)
const pageNum = ref(1)
const pageSize = ref(10)

const queryParams = ref({
  ipaddr: undefined,
  userName: undefined
})

/** 查询登录日志列表 */
function getList() {
  loading.value = true
  initData(queryParams.value).then(response => {
    onlineList.value = response.rows
    total.value = response.total
    loading.value = false
  })
}

/** 搜索按钮操作 */
function handleQuery() {
  pageNum.value = 1
  getList()
}

/** 重置按钮操作 */
function resetQuery() {
  proxy.resetForm("queryRef")
  handleQuery()
}

/** 强退按钮操作 */
function handleForceLogout(row) {
  proxy.$modal.confirm('是否确认强退名称为"' + row.userName + '"的用户?').then(function () {
    return forceLogout(row.tokenId)
  }).then(() => {
    getList()
    proxy.$modal.msgSuccess("删除成功")
  }).catch(() => {})
}

getList()
</script>
