<template>
  <div class="app-container">
    <el-form :model="queryParams" ref="queryRef" :inline="true" v-show="showSearch">
      <el-form-item label="盘点单号" prop="checkNo">
        <el-input v-model="queryParams.checkNo" placeholder="请输入盘点单号" clearable style="width: 220px" @keyup.enter="handleQuery" />
      </el-form-item>
      <el-form-item label="仓库编号" prop="warehouseId">
        <el-input v-model="queryParams.warehouseId" placeholder="请输入仓库编号" clearable style="width: 180px" @keyup.enter="handleQuery" />
      </el-form-item>
      <el-form-item label="状态" prop="status">
        <el-select v-model="queryParams.status" clearable style="width: 160px" placeholder="请选择状态">
          <el-option v-for="statusOption in statusOptions" :key="statusOption.value" :label="statusOption.label" :value="statusOption.value" />
        </el-select>
      </el-form-item>
      <el-form-item>
        <el-button type="primary" icon="Search" @click="handleQuery">搜索</el-button>
        <el-button icon="Refresh" @click="resetQuery">重置</el-button>
      </el-form-item>
    </el-form>

    <el-row :gutter="10" class="mb8">
      <el-col :span="1.5">
        <el-button type="primary" plain icon="Plus" @click="handleAdd" v-hasPermi="['business:inventoryCheck:add']">新增</el-button>
      </el-col>
      <el-col :span="1.5">
        <el-button type="success" plain icon="Edit" :disabled="!canUpdateSelected" @click="handleUpdate" v-hasPermi="['business:inventoryCheck:edit']">修改</el-button>
      </el-col>
      <el-col :span="1.5">
        <el-button type="info" plain icon="Upload" :disabled="!canSubmitSelected" @click="handleSubmit" v-hasPermi="['business:inventoryCheck:edit']">提交</el-button>
      </el-col>
      <el-col :span="1.5">
        <el-button type="warning" plain icon="CircleCheck" :disabled="!canAuditSelected" @click="handleAudit" v-hasPermi="['business:inventoryCheck:audit']">审核</el-button>
      </el-col>
      <el-col :span="1.5">
        <el-button type="danger" plain icon="Delete" :disabled="!canDeleteSelected" @click="handleDelete" v-hasPermi="['business:inventoryCheck:remove']">删除</el-button>
      </el-col>
      <el-col :span="1.5">
        <el-button type="warning" plain icon="Download" @click="handleExport" v-hasPermi="['business:inventoryCheck:export']">导出</el-button>
      </el-col>
      <right-toolbar v-model:showSearch="showSearch" @queryTable="getList"></right-toolbar>
    </el-row>

    <el-table v-loading="loading" :data="inventoryCheckList" @selection-change="handleSelectionChange">
      <el-table-column type="selection" width="55" align="center" />
      <el-table-column label="盘点编号" align="center" prop="checkId" />
      <el-table-column label="盘点单号" align="center" prop="checkNo" min-width="180" />
      <el-table-column label="仓库编号" align="center" prop="warehouseId" />
      <el-table-column label="差异数量" align="center" prop="totalDiffQty" />
      <el-table-column label="差异金额" align="center" prop="totalDiffAmount" />
      <el-table-column label="状态" align="center" prop="status">
        <template #default="scope">
          <el-tag :type="getStatusTagType(scope.row.status)">{{ getStatusLabel(scope.row.status) }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column label="创建时间" align="center" prop="createTime" width="180">
        <template #default="scope">
          <span>{{ parseTime(scope.row.createTime) }}</span>
        </template>
      </el-table-column>
      <el-table-column label="操作" width="180" align="center" class-name="small-padding fixed-width">
        <template #default="scope">
          <el-button link type="primary" icon="Edit" :disabled="!isDraftStatus(scope.row.status)" @click="handleUpdate(scope.row)" v-hasPermi="['business:inventoryCheck:edit']">修改</el-button>
          <el-button link type="primary" icon="Upload" :disabled="!isDraftStatus(scope.row.status)" @click="handleSubmit(scope.row)" v-hasPermi="['business:inventoryCheck:edit']">提交</el-button>
          <el-button link type="primary" icon="CircleCheck" :disabled="!isSubmittedStatus(scope.row.status)" @click="handleAudit(scope.row)" v-hasPermi="['business:inventoryCheck:audit']">审核</el-button>
          <el-button link type="primary" icon="CircleClose" :disabled="!canCancelStatus(scope.row.status)" @click="handleCancelOrder(scope.row)" v-hasPermi="['business:inventoryCheck:edit']">作废</el-button>
          <el-button link type="primary" icon="Delete" :disabled="!isDraftStatus(scope.row.status)" @click="handleDelete(scope.row)" v-hasPermi="['business:inventoryCheck:remove']">删除</el-button>
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

    <el-dialog :title="title" v-model="open" width="860px" append-to-body>
      <el-form ref="inventoryCheckRef" :model="form" :rules="rules" label-width="110px">
        <el-row>
          <el-col :span="12">
            <el-form-item label="仓库编号" prop="warehouseId">
              <el-input-number v-model="form.warehouseId" :min="1" controls-position="right" style="width: 100%" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="状态" prop="status">
              <el-select v-model="form.status" style="width: 100%" placeholder="请选择状态">
                <el-option v-for="statusOption in statusOptions" :key="statusOption.value" :label="statusOption.label" :value="statusOption.value" />
              </el-select>
            </el-form-item>
          </el-col>
        </el-row>
        <el-row>
          <el-col :span="12">
            <el-form-item label="差异数量" prop="totalDiffQty">
              <el-input-number v-model="form.totalDiffQty" :precision="2" controls-position="right" style="width: 100%" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="差异金额" prop="totalDiffAmount">
              <el-input-number v-model="form.totalDiffAmount" :precision="2" controls-position="right" style="width: 100%" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-row>
          <el-col :span="12">
            <el-form-item label="审核人" prop="auditBy">
              <el-input v-model="form.auditBy" placeholder="请输入审核人" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="备注" prop="remark">
              <el-input v-model="form.remark" placeholder="请输入备注" />
            </el-form-item>
          </el-col>
        </el-row>
      </el-form>
      <template #footer>
        <div class="dialog-footer">
          <el-button type="primary" @click="submitForm">确 定</el-button>
          <el-button @click="cancel">取 消</el-button>
        </div>
      </template>
    </el-dialog>
  </div>
</template>

<script setup name="BusinessInventoryCheck">
import { listInventoryCheck, getInventoryCheck, addInventoryCheck, updateInventoryCheck, delInventoryCheck, submitInventoryCheck, auditInventoryCheck, cancelInventoryCheck } from "@/api/business/inventoryCheck"

const { proxy } = getCurrentInstance()

const inventoryCheckList = ref([])
const open = ref(false)
const loading = ref(true)
const showSearch = ref(true)
const ids = ref([])
const single = ref(true)
const multiple = ref(true)
const canUpdateSelected = ref(false)
const canSubmitSelected = ref(false)
const canAuditSelected = ref(false)
const canDeleteSelected = ref(false)
const total = ref(0)
const title = ref("")
const statusOptions = ref([
  { label: "草稿", value: "draft" },
  { label: "已提交", value: "submitted" },
  { label: "已审核", value: "audited" },
  { label: "已作废", value: "cancelled" }
])

const data = reactive({
  form: {},
  queryParams: {
    pageNum: 1,
    pageSize: 10,
    checkNo: undefined,
    warehouseId: undefined,
    status: undefined
  },
  rules: {
    warehouseId: [{ required: true, message: "仓库编号不能为空", trigger: "blur" }]
  }
})

const { queryParams, form, rules } = toRefs(data)

function getList() {
  loading.value = true
  listInventoryCheck(queryParams.value).then(response => {
    inventoryCheckList.value = response.rows
    total.value = response.total
    loading.value = false
  })
}

function cancel() {
  open.value = false
  reset()
}

function reset() {
  form.value = {
    checkId: undefined,
    checkNo: undefined,
    warehouseId: undefined,
    status: "draft",
    totalDiffQty: 0,
    totalDiffAmount: 0,
    auditBy: undefined,
    auditTime: undefined,
    remark: undefined
  }
  proxy.resetForm("inventoryCheckRef")
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
  ids.value = selection.map(item => item.checkId)
  single.value = selection.length !== 1
  multiple.value = !selection.length
  canUpdateSelected.value = selection.length === 1 && isDraftStatus(selection[0].status)
  canSubmitSelected.value = selection.length === 1 && isDraftStatus(selection[0].status)
  canAuditSelected.value = selection.length === 1 && isSubmittedStatus(selection[0].status)
  canDeleteSelected.value = selection.length > 0 && selection.every(item => isDraftStatus(item.status))
}

function handleAdd() {
  reset()
  open.value = true
  title.value = "新增库存盘点单"
}

function handleUpdate(row) {
  reset()
  const checkId = row.checkId || ids.value
  getInventoryCheck(checkId).then(response => {
    form.value = response.data
    open.value = true
    title.value = "修改库存盘点单"
  })
}

function submitForm() {
  proxy.$refs["inventoryCheckRef"].validate(valid => {
    if (valid) {
      if (form.value.checkId !== undefined) {
        updateInventoryCheck(form.value).then(() => {
          proxy.$modal.msgSuccess("修改成功")
          open.value = false
          getList()
        })
      } else {
        addInventoryCheck(form.value).then(() => {
          proxy.$modal.msgSuccess("新增成功")
          open.value = false
          getList()
        })
      }
    }
  })
}

function handleSubmit(row) {
  const checkId = row?.checkId || ids.value[0]
  proxy.$modal.confirm('是否确认提交库存盘点单编号为"' + checkId + '"的数据项？').then(function () {
    return submitInventoryCheck(checkId)
  }).then(() => {
    getList()
    proxy.$modal.msgSuccess("提交成功")
  }).catch(() => {})
}

function handleAudit(row) {
  const checkId = row?.checkId || ids.value[0]
  proxy.$modal.confirm('是否确认审核库存盘点单编号为"' + checkId + '"的数据项？').then(function () {
    return auditInventoryCheck(checkId)
  }).then(() => {
    getList()
    proxy.$modal.msgSuccess("审核成功")
  }).catch(() => {})
}

function handleCancelOrder(row) {
  const checkId = row?.checkId || ids.value[0]
  proxy.$modal.confirm('是否确认作废库存盘点单编号为"' + checkId + '"的数据项？').then(function () {
    return cancelInventoryCheck(checkId)
  }).then(() => {
    getList()
    proxy.$modal.msgSuccess("作废成功")
  }).catch(() => {})
}

function handleDelete(row) {
  const checkIds = row?.checkId || ids.value
  const displayCheckIds = Array.isArray(checkIds) ? checkIds.join("、") : checkIds
  proxy.$modal.confirm('是否确认删除库存盘点单编号为"' + displayCheckIds + '"的数据项？').then(function () {
    return delInventoryCheck(checkIds)
  }).then(() => {
    getList()
    proxy.$modal.msgSuccess("删除成功")
  }).catch(() => {})
}

function handleExport() {
  proxy.download("business/inventoryCheck/export", {
    ...queryParams.value
  }, `inventoryCheck_${new Date().getTime()}.xlsx`)
}

function getStatusLabel(statusValue) {
  const statusOption = statusOptions.value.find(item => item.value === statusValue)
  return statusOption ? statusOption.label : statusValue
}

function getStatusTagType(statusValue) {
  if (statusValue === "draft") {
    return "info"
  }
  if (statusValue === "submitted") {
    return "warning"
  }
  if (statusValue === "audited") {
    return "success"
  }
  if (statusValue === "cancelled") {
    return "danger"
  }
  return "info"
}

function isDraftStatus(statusValue) {
  return statusValue === "draft"
}

function isSubmittedStatus(statusValue) {
  return statusValue === "submitted"
}

function canCancelStatus(statusValue) {
  return statusValue === "draft" || statusValue === "submitted"
}

getList()
</script>
