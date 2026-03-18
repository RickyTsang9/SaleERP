<template>
  <div class="app-container">
    <el-form :model="queryParams" ref="queryRef" :inline="true" v-show="showSearch">
      <el-form-item label="备份名称" prop="backupName">
        <el-input v-model="queryParams.backupName" placeholder="请输入备份名称" clearable style="width: 220px" @keyup.enter="handleQuery" />
      </el-form-item>
      <el-form-item label="备份类型" prop="backupType">
        <el-select v-model="queryParams.backupType" clearable style="width: 160px" placeholder="请选择备份类型">
          <el-option v-for="backupTypeOption in backupTypeOptions" :key="backupTypeOption.value" :label="backupTypeOption.label" :value="backupTypeOption.value" />
        </el-select>
      </el-form-item>
      <el-form-item label="恢复状态" prop="restoreStatus">
        <el-select v-model="queryParams.restoreStatus" clearable style="width: 160px" placeholder="请选择恢复状态">
          <el-option v-for="restoreStatusOption in restoreStatusOptions" :key="restoreStatusOption.value" :label="restoreStatusOption.label" :value="restoreStatusOption.value" />
        </el-select>
      </el-form-item>
      <el-form-item>
        <el-button type="primary" icon="Search" @click="handleQuery">搜索</el-button>
        <el-button icon="Refresh" @click="resetQuery">重置</el-button>
      </el-form-item>
    </el-form>

    <el-row :gutter="10" class="mb8">
      <el-col :span="1.5">
        <el-button type="primary" plain icon="Plus" @click="handleManualBackup" v-hasPermi="['business:dataBackup:add']">手动备份</el-button>
      </el-col>
      <el-col :span="1.5">
        <el-button type="warning" plain icon="RefreshRight" :disabled="single" @click="handleRestore" v-hasPermi="['business:dataBackup:restore']">一键恢复</el-button>
      </el-col>
      <el-col :span="1.5">
        <el-button type="danger" plain icon="Delete" :disabled="multiple" @click="handleDelete" v-hasPermi="['business:dataBackup:remove']">删除记录</el-button>
      </el-col>
      <right-toolbar v-model:showSearch="showSearch" @queryTable="getList"></right-toolbar>
    </el-row>

    <el-table v-loading="loading" :data="dataBackupList" @selection-change="handleSelectionChange">
      <el-table-column type="selection" width="55" align="center" />
      <el-table-column label="备份编号" align="center" prop="backupId" width="100" />
      <el-table-column label="备份名称" align="center" prop="backupName" min-width="200" />
      <el-table-column label="备份类型" align="center" prop="backupType">
        <template #default="scope">
          <el-tag>{{ getBackupTypeLabel(scope.row.backupType) }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column label="备份状态" align="center" prop="backupStatus">
        <template #default="scope">
          <el-tag :type="scope.row.backupStatus === 'success' ? 'success' : 'danger'">{{ getBackupStatusLabel(scope.row.backupStatus) }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column label="表数量" align="center" prop="tableCount" />
      <el-table-column label="记录数量" align="center" prop="recordCount" />
      <el-table-column label="恢复状态" align="center" prop="restoreStatus">
        <template #default="scope">
          <el-tag v-if="scope.row.restoreStatus" :type="scope.row.restoreStatus === 'success' ? 'success' : 'danger'">{{ getRestoreStatusLabel(scope.row.restoreStatus) }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column label="恢复人" align="center" prop="restoreBy" />
      <el-table-column label="恢复时间" align="center" prop="restoreTime" width="180">
        <template #default="scope">
          <span>{{ parseTime(scope.row.restoreTime) }}</span>
        </template>
      </el-table-column>
      <el-table-column label="创建时间" align="center" prop="createTime" width="180">
        <template #default="scope">
          <span>{{ parseTime(scope.row.createTime) }}</span>
        </template>
      </el-table-column>
      <el-table-column label="操作" width="160" align="center" class-name="small-padding fixed-width">
        <template #default="scope">
          <el-button link type="primary" icon="RefreshRight" @click="handleRestore(scope.row)" v-hasPermi="['business:dataBackup:restore']">恢复</el-button>
          <el-button link type="primary" icon="Delete" @click="handleDelete(scope.row)" v-hasPermi="['business:dataBackup:remove']">删除</el-button>
        </template>
      </el-table-column>
    </el-table>

    <pagination
      v-show="total > 0"
      :total="total"
      v-model:page="queryParams.pageNum"
      v-model:limit="queryParams.pageSize"
      @pagination="getList"
    />
  </div>
</template>

<script setup name="BusinessDataBackup">
import { listDataBackup, manualBackup, restoreDataBackup, delDataBackup } from "@/api/business/dataBackup"

const { proxy } = getCurrentInstance()

const dataBackupList = ref([])
const loading = ref(true)
const showSearch = ref(true)
const ids = ref([])
const single = ref(true)
const multiple = ref(true)
const total = ref(0)
const backupTypeOptions = ref([
  { label: "手动备份", value: "manual" },
  { label: "自动备份", value: "auto" }
])
const restoreStatusOptions = ref([
  { label: "恢复成功", value: "success" },
  { label: "恢复失败", value: "failed" }
])

const data = reactive({
  queryParams: {
    pageNum: 1,
    pageSize: 10,
    backupName: undefined,
    backupType: undefined,
    restoreStatus: undefined
  }
})

const { queryParams } = toRefs(data)

function getList() {
  loading.value = true
  listDataBackup(queryParams.value).then(response => {
    dataBackupList.value = response.rows
    total.value = response.total
    loading.value = false
  })
}

function handleQuery() {
  queryParams.value.pageNum = 1
  getList()
}

function resetQuery() {
  proxy.resetForm("queryRef")
  handleQuery()
}

function handleSelectionChange(selection) {
  ids.value = selection.map(item => item.backupId)
  single.value = selection.length !== 1
  multiple.value = !selection.length
}

function handleManualBackup() {
  proxy.$modal.confirm("是否确认执行手动备份？").then(function () {
    return manualBackup()
  }).then(() => {
    getList()
    proxy.$modal.msgSuccess("手动备份成功")
  }).catch(() => {})
}

function handleRestore(row) {
  const backupId = row?.backupId || ids.value[0]
  proxy.$modal.confirm('是否确认基于备份编号为"' + backupId + '"的数据执行恢复？恢复会覆盖现有业务数据。').then(function () {
    return restoreDataBackup(backupId)
  }).then(() => {
    getList()
    proxy.$modal.msgSuccess("数据恢复成功")
  }).catch(() => {})
}

function handleDelete(row) {
  const backupIds = row?.backupId || ids.value
  const displayBackupIds = Array.isArray(backupIds) ? backupIds.join("、") : backupIds
  proxy.$modal.confirm('是否确认删除备份编号为"' + displayBackupIds + '"的数据项？').then(function () {
    return delDataBackup(backupIds)
  }).then(() => {
    getList()
    proxy.$modal.msgSuccess("删除成功")
  }).catch(() => {})
}

function getBackupTypeLabel(backupTypeValue) {
  const backupTypeOption = backupTypeOptions.value.find(item => item.value === backupTypeValue)
  return backupTypeOption ? backupTypeOption.label : backupTypeValue
}

function getBackupStatusLabel(backupStatusValue) {
  if (backupStatusValue === "success") {
    return "备份成功"
  }
  if (backupStatusValue === "failed") {
    return "备份失败"
  }
  return backupStatusValue
}

function getRestoreStatusLabel(restoreStatusValue) {
  const restoreStatusOption = restoreStatusOptions.value.find(item => item.value === restoreStatusValue)
  return restoreStatusOption ? restoreStatusOption.label : restoreStatusValue
}

getList()
</script>
