<template>
  <div class="app-container erp-page">
    <erp-page-header
      eyebrow="系统运维 · 数据安全"
      title="数据备份与容灾恢复"
      subtitle="支持全量快照备份、本地文件下载与二次确认容灾恢复，守护企业核心交易与账务数据安全。"
      icon="Coin"
      tone="emerald"
      :badge="`共 ${backupOverview.totalCount} 份备份`"
    >
      <template #actions>
        <el-button icon="Upload" plain @click="handleOpenUploadRestore" v-hasPermi="['business:dataBackup:restore']">上传离线包恢复</el-button>
        <el-button type="primary" icon="Plus" @click="handleManualBackup" v-hasPermi="['business:dataBackup:add']">立即手动备份</el-button>
      </template>
    </erp-page-header>

    <erp-stat-strip :items="backupStatItems" :columns="3" />

    <el-alert
      title="恢复操作会覆盖当前业务数据，建议先确认最近一次成功备份时间、备份状态和影响范围。仅备份成功的记录支持执行恢复。"
      type="warning"
      :closable="false"
    />

    <section class="erp-filter-card" v-show="showSearch">
      <el-form :model="queryParams" ref="queryRef" :inline="true">
        <el-form-item label="备份名称" prop="backupName">
          <el-input v-model="queryParams.backupName" placeholder="请输入备份名称" clearable style="width: 210px" @keyup.enter="handleQuery" />
        </el-form-item>
        <el-form-item label="备份类型" prop="backupType">
          <el-select v-model="queryParams.backupType" clearable style="width: 150px" placeholder="全部类型">
            <el-option v-for="backupTypeOption in backupTypeOptions" :key="backupTypeOption.value" :label="backupTypeOption.label" :value="backupTypeOption.value" />
          </el-select>
        </el-form-item>
        <el-form-item label="恢复状态" prop="restoreStatus">
          <el-select v-model="queryParams.restoreStatus" clearable style="width: 150px" placeholder="全部状态">
            <el-option v-for="restoreStatusOption in restoreStatusOptions" :key="restoreStatusOption.value" :label="restoreStatusOption.label" :value="restoreStatusOption.value" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" icon="Search" @click="handleQuery">搜索</el-button>
          <el-button icon="Refresh" @click="resetQuery">重置</el-button>
        </el-form-item>
      </el-form>
    </section>

    <section class="erp-table-card">
      <div class="erp-toolbar">
        <div class="erp-toolbar-left">
          <el-button type="warning" plain icon="RefreshRight" :disabled="!canRestoreSelected" @click="handleRestore" v-hasPermi="['business:dataBackup:restore']">恢复备份</el-button>
          <el-button type="danger" plain icon="Delete" :disabled="multiple" @click="handleDelete" v-hasPermi="['business:dataBackup:remove']">删除记录</el-button>
        </div>
        <div class="erp-toolbar-right">
          <right-toolbar v-model:showSearch="showSearch" @queryTable="handleQuery" />
        </div>
      </div>

      <el-table v-loading="loading" :data="dataBackupList" @selection-change="handleSelectionChange">
        <el-table-column type="selection" width="44" align="center" />
        <el-table-column label="备份名称" align="left" prop="backupName" min-width="185" show-overflow-tooltip>
          <template #default="scope">
            <div class="erp-cell-title">{{ scope.row.backupName }}</div>
            <div class="erp-cell-sub">表 {{ scope.row.tableCount || 0 }} 张 · 记录 {{ scope.row.recordCount || 0 }} 条</div>
          </template>
        </el-table-column>
        <el-table-column label="类型 / 状态" align="center" width="115">
          <template #default="scope">
            <el-tag :type="getBackupTypeTag(scope.row.backupType)" effect="plain" size="small">{{ getBackupTypeLabel(scope.row.backupType) }}</el-tag>
            <div class="erp-cell-sub" style="margin-top: 3px">
              <el-tag :type="scope.row.backupStatus === 'success' ? 'success' : 'danger'" effect="light" size="small">{{ getBackupStatusLabel(scope.row.backupStatus) }}</el-tag>
            </div>
          </template>
        </el-table-column>
        <el-table-column label="大小" align="right" prop="fileSize" width="84">
          <template #default="scope">
            <span class="erp-num">{{ formatFileSize(scope.row.fileSize) }}</span>
          </template>
        </el-table-column>
        <el-table-column label="恢复状态" align="center" prop="restoreStatus" width="98">
          <template #default="scope">
            <el-tag v-if="scope.row.restoreStatus" :type="scope.row.restoreStatus === 'success' ? 'success' : 'danger'" effect="light" size="small">{{ getRestoreStatusLabel(scope.row.restoreStatus) }}</el-tag>
            <span v-else class="erp-cell-sub">未恢复</span>
            <div v-if="scope.row.restoreBy" class="erp-cell-sub">{{ scope.row.restoreBy }}</div>
          </template>
        </el-table-column>
        <el-table-column label="创建时间" align="center" prop="createTime" width="136">
          <template #default="scope">
            <span>{{ parseTime(scope.row.createTime, '{y}-{m}-{d} {h}:{i}') }}</span>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="204" fixed="right" align="center">
          <template #default="scope">
            <div class="erp-row-actions">
              <el-button v-if="scope.row.backupStatus === 'success'" link type="primary" icon="Download" @click="handleDownloadBackup(scope.row)">下载</el-button>
              <el-button v-if="scope.row.backupStatus === 'success'" link type="warning" icon="RefreshRight" @click="handleRestore(scope.row)" v-hasPermi="['business:dataBackup:restore']">恢复</el-button>
              <el-button link type="danger" icon="Delete" @click="handleDelete(scope.row)" v-hasPermi="['business:dataBackup:remove']">删除</el-button>
            </div>
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
    </section>

    <!-- 上传离线备份包恢复弹窗 -->
    <el-dialog v-model="uploadRestoreOpen" title="上传离线备份包恢复" width="520px" append-to-body>
      <el-alert
        title="系统会在覆盖写入前自动生成一条【恢复前保护快照】，随后清空当前业务表并导入离线包数据。仅支持 SaleERP 导出的 .json.gz 或 .json 文件。"
        type="warning"
        :closable="false"
        style="margin-bottom: 14px"
      />
      <el-form label-width="96px">
        <el-form-item label="离线备份包" required>
          <el-upload
            ref="uploadRestoreRef"
            :auto-upload="false"
            :limit="1"
            accept=".gz,.json"
            :on-change="handleUploadFileChange"
            :on-remove="handleUploadFileRemove"
            style="width: 100%"
          >
            <el-button type="primary" plain icon="Upload">选择 .json.gz / .json 文件</el-button>
            <template #tip>
              <div class="el-upload__tip">单次上传 1 份完整离线备份包，上传并恢复后建议刷新当前业务页面。</div>
            </template>
          </el-upload>
        </el-form-item>
        <el-form-item label="二次确认词" required>
          <el-input v-model="uploadConfirmText" placeholder='请输入"立即恢复"以确认覆盖当前业务数据' />
        </el-form-item>
      </el-form>
      <template #footer>
        <div class="dialog-footer">
          <el-button type="danger" :loading="uploadingRestore" @click="submitUploadRestore">确认上传并恢复</el-button>
          <el-button @click="uploadRestoreOpen = false">取 消</el-button>
        </div>
      </template>
    </el-dialog>
  </div>
</template>

<script setup name="DataBackup">
import { listDataBackup, manualBackup, restoreDataBackup, delDataBackup, downloadDataBackup, uploadRestoreDataBackup } from "@/api/business/dataBackup"
import { parseTime } from "@/utils/ruoyi"

const { proxy } = getCurrentInstance()

const dataBackupList = ref([])
const loading = ref(true)
const showSearch = ref(true)
const ids = ref([])
const selectedBackupRowList = ref([])
const multiple = ref(true)
const total = ref(0)
const uploadRestoreOpen = ref(false)
const uploadingRestore = ref(false)
const uploadConfirmText = ref("")
const selectedUploadFile = ref(null)
const uploadRestoreRef = ref(null)
const backupTypeOptions = ref([
  { label: "手动备份", value: "manual" },
  { label: "自动备份", value: "auto" },
  { label: "恢复前保护快照", value: "pre_restore" },
  { label: "离线包恢复", value: "upload_restore" }
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

const backupStatItems = computed(() => [
  {
    label: "最近一次成功备份",
    value: getLatestSuccessfulBackupTitle(),
    hint: getLatestSuccessfulBackupDescription(),
    icon: "CircleCheck",
    tone: "emerald"
  },
  {
    label: "最近一次成功恢复",
    value: getLatestRestoreTitle(),
    hint: getLatestRestoreDescription(),
    icon: "RefreshRight",
    tone: "blue"
  },
  {
    label: "系统留存备份数",
    value: backupOverview.value.totalCount || 0,
    suffix: "份",
    hint: `当前系统共保留 ${backupOverview.value.totalCount || 0} 条备份记录`,
    icon: "Coin",
    tone: "indigo"
  }
])

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
    proxy.$modal.msgWarning("请选择要恢复的备份记录")
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

// 删除按钮操作，兼容单条删除和批量删除，并在空选择时及时提醒用户。
function handleDelete(row) {
  const backupIdList = row?.backupId ? [row.backupId] : ids.value
  if (!backupIdList.length) {
    proxy.$modal.msgWarning("请选择要删除的备份记录")
    return
  }
  const displayBackupText = buildDeleteBackupDisplayText(row)
  proxy.$modal.confirm(`是否确认删除“${displayBackupText}”？删除后将无法再基于该备份执行恢复。`).then(function () {
    return delDataBackup(backupIdList)
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

function getBackupTypeTag(backupTypeValue) {
  if (backupTypeValue === "pre_restore") return "warning"
  if (backupTypeValue === "upload_restore") return "success"
  if (backupTypeValue === "auto") return "info"
  return "primary"
}

function handleOpenUploadRestore() {
  uploadConfirmText.value = ""
  selectedUploadFile.value = null
  uploadRestoreRef.value?.clearFiles?.()
  uploadRestoreOpen.value = true
}

function handleUploadFileChange(file) {
  selectedUploadFile.value = file?.raw || null
}

function handleUploadFileRemove() {
  selectedUploadFile.value = null
}

async function submitUploadRestore() {
  if (!selectedUploadFile.value) {
    proxy.$modal.msgWarning("请先选择离线备份包文件（.json.gz 或 .json）")
    return
  }
  if (uploadConfirmText.value.trim() !== "立即恢复") {
    proxy.$modal.msgWarning('请输入确认词"立即恢复"')
    return
  }
  uploadingRestore.value = true
  try {
    const formData = new FormData()
    formData.append("file", selectedUploadFile.value)
    await uploadRestoreDataBackup(formData)
    proxy.$modal.msgSuccess("离线备份包恢复成功，已自动生成恢复前保护快照")
    uploadRestoreOpen.value = false
    await refreshPageData()
  } finally {
    uploadingRestore.value = false
  }
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

function formatFileSize(bytes) {
  const size = Number(bytes || 0)
  if (!size || size <= 0) return "-"
  if (size < 1024) return `${size} B`
  if (size < 1024 * 1024) return `${(size / 1024).toFixed(1)} KB`
  return `${(size / (1024 * 1024)).toFixed(2)} MB`
}

async function handleDownloadBackup(row) {
  try {
    const blob = await downloadDataBackup(row.backupId)
    const url = window.URL.createObjectURL(blob instanceof Blob ? blob : new Blob([blob]))
    const link = document.createElement("a")
    link.href = url
    link.download = `${row.backupName || "saleerp_backup"}.json.gz`
    document.body.appendChild(link)
    link.click()
    document.body.removeChild(link)
    window.URL.revokeObjectURL(url)
    proxy.$modal.msgSuccess("离线备份包下载成功")
  } catch (e) {
    proxy.$modal.msgError("下载备份包失败")
  }
}

refreshPageData()
</script>

<style scoped>
:deep(.erp-stat-card__value) {
  font-size: 17px;
  word-break: break-all;
  line-height: 1.35;
}

.backup-summary-card {
  padding: 18px 20px;
  border: 1px solid var(--app-border, var(--el-border-color-light));
  border-radius: 12px;
  background: linear-gradient(180deg, var(--app-panel-bg, #ffffff) 0%, var(--app-panel-bg-soft, #fbfcff) 100%);
  min-height: 116px;
}

.backup-summary-label {
  font-size: 14px;
  color: var(--el-text-color-secondary);
}

.backup-summary-value {
  margin-top: 10px;
  font-size: 20px;
  line-height: 1.3;
  font-weight: 600;
  color: var(--el-text-color-primary);
  word-break: break-all;
}

.backup-summary-desc {
  margin-top: 10px;
  font-size: 12px;
  color: var(--el-text-color-secondary);
}
</style>
