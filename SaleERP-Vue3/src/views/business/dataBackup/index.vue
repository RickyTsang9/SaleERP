<template>
  <div class="app-container">
    <el-alert
      title="恢复操作会覆盖当前业务数据，建议先确认最近一次成功备份时间、备份状态和影响范围。仅备份成功的记录支持执行恢复。"
      type="warning"
      :closable="false"
      class="mb8"
    />

    <el-row :gutter="12" class="mb8">
      <el-col :xs="24" :sm="8">
        <div class="backup-summary-card">
          <div class="backup-summary-label">最近一次成功备份</div>
          <div class="backup-summary-value">{{ getLatestSuccessfulBackupTitle() }}</div>
          <div class="backup-summary-desc">{{ getLatestSuccessfulBackupDescription() }}</div>
        </div>
      </el-col>
      <el-col :xs="24" :sm="8">
        <div class="backup-summary-card">
          <div class="backup-summary-label">最近一次成功恢复</div>
          <div class="backup-summary-value">{{ getLatestRestoreTitle() }}</div>
          <div class="backup-summary-desc">{{ getLatestRestoreDescription() }}</div>
        </div>
      </el-col>
      <el-col :xs="24" :sm="8">
        <div class="backup-summary-card">
          <div class="backup-summary-label">备份记录概览</div>
          <div class="backup-summary-value">{{ backupOverview.totalCount }}</div>
          <div class="backup-summary-desc">当前系统共保留 {{ backupOverview.totalCount }} 条备份记录</div>
        </div>
      </el-col>
    </el-row>

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
        <el-button type="warning" plain icon="RefreshRight" :disabled="!canRestoreSelected" @click="handleRestore" v-hasPermi="['business:dataBackup:restore']">恢复备份</el-button>
      </el-col>
      <el-col :span="1.5">
        <el-button type="danger" plain icon="Delete" :disabled="multiple" @click="handleDelete" v-hasPermi="['business:dataBackup:remove']">删除记录</el-button>
      </el-col>
      <right-toolbar v-model:showSearch="showSearch" @queryTable="handleQuery"></right-toolbar>
    </el-row>

    <el-table v-loading="loading" :data="dataBackupList" @selection-change="handleSelectionChange">
      <el-table-column type="selection" width="55" align="center" />
      <el-table-column label="备份名称" align="center" prop="backupName" min-width="200" />
      <el-table-column label="备份类型" align="center" prop="backupType" width="120">
        <template #default="scope">
          <el-tag>{{ getBackupTypeLabel(scope.row.backupType) }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column label="备份状态" align="center" prop="backupStatus" width="120">
        <template #default="scope">
          <el-tag :type="scope.row.backupStatus === 'success' ? 'success' : 'danger'">{{ getBackupStatusLabel(scope.row.backupStatus) }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column label="表数量" align="center" prop="tableCount" width="110" />
      <el-table-column label="记录数量" align="center" prop="recordCount" min-width="120" />
      <el-table-column label="恢复状态" align="center" prop="restoreStatus" width="120">
        <template #default="scope">
          <el-tag v-if="scope.row.restoreStatus" :type="scope.row.restoreStatus === 'success' ? 'success' : 'danger'">{{ getRestoreStatusLabel(scope.row.restoreStatus) }}</el-tag>
          <span v-else>未恢复</span>
        </template>
      </el-table-column>
      <el-table-column label="恢复人" align="center" prop="restoreBy" width="120" />
      <el-table-column label="恢复时间" align="center" prop="restoreTime" width="180">
        <template #default="scope">
          <span>{{ parseTime(scope.row.restoreTime) || "未恢复" }}</span>
        </template>
      </el-table-column>
      <el-table-column label="创建时间" align="center" prop="createTime" width="180">
        <template #default="scope">
          <span>{{ parseTime(scope.row.createTime) }}</span>
        </template>
      </el-table-column>
      <el-table-column label="操作" width="180" align="center" class-name="small-padding fixed-width">
        <template #default="scope">
          <el-button
            v-if="scope.row.backupStatus === 'success'"
            link
            type="primary"
            icon="RefreshRight"
            @click="handleRestore(scope.row)"
            v-hasPermi="['business:dataBackup:restore']"
          >恢复</el-button>
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
const selectedBackupRowList = ref([])
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
const latestSuccessfulBackup = ref(undefined)
const latestRestoreBackup = ref(undefined)
const backupOverview = ref({
  totalCount: 0
})

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
const canRestoreSelected = computed(() => selectedBackupRowList.value.length === 1 && selectedBackupRowList.value[0].backupStatus === "success")

// 查询备份记录列表，并同步页面上的记录总数展示。
async function getList() {
  loading.value = true
  try {
    const response = await listDataBackup(queryParams.value)
    dataBackupList.value = response.rows || []
    total.value = response.total || 0
    selectedBackupRowList.value = []
    ids.value = []
    multiple.value = true
  } finally {
    loading.value = false
  }
}

// 查询备份概览，补充最近成功备份和最近恢复成功信息。
async function loadBackupOverview() {
  const [latestSuccessfulBackupResponse, latestRestoreBackupResponse, latestBackupListResponse] = await Promise.all([
    listDataBackup({ pageNum: 1, pageSize: 1, backupStatus: "success" }),
    listDataBackup({ pageNum: 1, pageSize: 1, restoreStatus: "success" }),
    listDataBackup({ pageNum: 1, pageSize: 1 })
  ])
  latestSuccessfulBackup.value = latestSuccessfulBackupResponse.rows?.[0]
  latestRestoreBackup.value = latestRestoreBackupResponse.rows?.[0]
  backupOverview.value = {
    totalCount: latestBackupListResponse.total || 0
  }
}

// 并行刷新列表和概览卡片，保证风险提示区与表格数据一致。
async function refreshPageData() {
  await Promise.all([
    getList(),
    loadBackupOverview()
  ])
}

// 搜索按钮操作
async function handleQuery() {
  queryParams.value.pageNum = 1
  await getList()
}

// 重置按钮操作
async function resetQuery() {
  proxy.resetForm("queryRef")
  await handleQuery()
}

// 多选框选中数据
function handleSelectionChange(selection) {
  selectedBackupRowList.value = selection
  ids.value = selection.map(backupItem => backupItem.backupId)
  multiple.value = !selection.length
}

// 返回最近成功备份标题，方便管理员第一眼确认可恢复的最新基线。
function getLatestSuccessfulBackupTitle() {
  return latestSuccessfulBackup.value?.backupName || "暂无成功备份"
}

// 返回最近成功备份描述信息，提示最近可用备份的时间和规模。
function getLatestSuccessfulBackupDescription() {
  if (!latestSuccessfulBackup.value) {
    return "建议先执行一次手动备份，再考虑恢复操作。"
  }
  const createTimeText = parseTime(latestSuccessfulBackup.value.createTime) || "未知时间"
  return `${createTimeText}，共 ${latestSuccessfulBackup.value.tableCount || 0} 张表、${latestSuccessfulBackup.value.recordCount || 0} 条记录`
}

// 返回最近成功恢复标题，帮助管理员快速确认最近一次恢复动作。
function getLatestRestoreTitle() {
  return latestRestoreBackup.value?.backupName || "暂无成功恢复"
}

// 返回最近成功恢复描述，说明最近一次恢复的执行人和时间。
function getLatestRestoreDescription() {
  if (!latestRestoreBackup.value) {
    return "当前还没有成功恢复记录。"
  }
  const restoreTimeText = parseTime(latestRestoreBackup.value.restoreTime) || "未知时间"
  return `${restoreTimeText}，执行人：${latestRestoreBackup.value.restoreBy || "系统"}`
}

// 执行手动备份，成功后同步刷新列表和概览卡片。
function handleManualBackup() {
  proxy.$modal.confirm("是否确认立即执行手动备份？建议在高风险操作前先创建新的可恢复节点。").then(function () {
    return manualBackup()
  }).then(async () => {
    await refreshPageData()
    proxy.$modal.msgSuccess("手动备份成功")
  }).catch(() => {})
}

// 获取当前要恢复的备份记录，统一处理工具栏和行内按钮入口。
function getCurrentRestoreBackupRow(row) {
  if (row?.backupId) {
    return row
  }
  return selectedBackupRowList.value[0]
}

// 执行恢复前二次确认，要求输入固定确认词降低误操作概率。
function handleRestore(row) {
  const currentRestoreBackupRow = getCurrentRestoreBackupRow(row)
  if (!currentRestoreBackupRow) {
    return
  }
  if (currentRestoreBackupRow.backupStatus !== "success") {
    proxy.$modal.msgWarning("仅备份成功的记录支持恢复")
    return
  }
  proxy.$modal.prompt(`恢复“${currentRestoreBackupRow.backupName}”会覆盖现有业务数据，请输入“立即恢复”继续操作。`).then(function ({ value }) {
    if (value !== "立即恢复") {
      proxy.$modal.msgWarning("确认词不匹配，已取消恢复操作")
      return Promise.reject(new Error("restore cancelled"))
    }
    return restoreDataBackup(currentRestoreBackupRow.backupId)
  }).then(async () => {
    await refreshPageData()
    proxy.$modal.msgSuccess("数据恢复成功")
  }).catch(() => {})
}

// 生成删除提示文案，单条删除优先展示备份名称，减少误删风险。
function buildDeleteBackupDisplayText(row) {
  if (row?.backupId) {
    return row.backupName || `备份记录 ${row.backupId}`
  }
  return `已选中的 ${ids.value.length} 条备份记录`
}

// 删除按钮操作
function handleDelete(row) {
  const backupIds = row?.backupId || ids.value
  const displayBackupText = buildDeleteBackupDisplayText(row)
  proxy.$modal.confirm(`是否确认删除“${displayBackupText}”？删除后将无法再基于该备份执行恢复。`).then(function () {
    return delDataBackup(backupIds)
  }).then(async () => {
    await refreshPageData()
    proxy.$modal.msgSuccess("删除成功")
  }).catch(() => {})
}

// 返回备份类型中文名称，统一页面展示口径。
function getBackupTypeLabel(backupTypeValue) {
  const backupTypeOption = backupTypeOptions.value.find(backupTypeItem => backupTypeItem.value === backupTypeValue)
  return backupTypeOption ? backupTypeOption.label : backupTypeValue
}

// 返回备份状态中文名称，帮助管理员快速识别可恢复记录。
function getBackupStatusLabel(backupStatusValue) {
  if (backupStatusValue === "success") {
    return "备份成功"
  }
  if (backupStatusValue === "failed") {
    return "备份失败"
  }
  return backupStatusValue
}

// 返回恢复状态中文名称，统一恢复结果显示口径。
function getRestoreStatusLabel(restoreStatusValue) {
  const restoreStatusOption = restoreStatusOptions.value.find(restoreStatusItem => restoreStatusItem.value === restoreStatusValue)
  return restoreStatusOption ? restoreStatusOption.label : restoreStatusValue
}

refreshPageData()
</script>

<style scoped>
.backup-summary-card {
  padding: 18px 20px;
  border: 1px solid var(--el-border-color-light);
  border-radius: 12px;
  background: linear-gradient(180deg, #ffffff 0%, #fbfcff 100%);
  min-height: 116px;
}

.backup-summary-label {
  font-size: 14px;
  color: var(--el-text-color-secondary);
}

.backup-summary-value {
  margin-top: 10px;
  font-size: 24px;
  line-height: 1.2;
  font-weight: 600;
  color: var(--el-text-color-primary);
}

.backup-summary-desc {
  margin-top: 10px;
  font-size: 12px;
  color: var(--el-text-color-secondary);
}
</style>
