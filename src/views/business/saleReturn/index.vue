<template>
  <div class="app-container">
    <el-form :model="queryParams" ref="queryRef" :inline="true" v-show="showSearch">
      <el-form-item label="退货单号" prop="returnNo">
        <el-input v-model="queryParams.returnNo" placeholder="请输入退货单号" clearable style="width: 220px" @keyup.enter="handleQuery" />
      </el-form-item>
      <el-form-item label="退货类型" prop="returnType">
        <el-input v-model="queryParams.returnType" placeholder="请输入退货类型" clearable style="width: 180px" @keyup.enter="handleQuery" />
      </el-form-item>
      <el-form-item label="客户编号" prop="customerId">
        <el-input v-model="queryParams.customerId" placeholder="请输入客户编号" clearable style="width: 180px" @keyup.enter="handleQuery" />
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
        <el-button type="primary" plain icon="Plus" @click="handleAdd" v-hasPermi="['business:saleReturn:add']">新增</el-button>
      </el-col>
      <el-col :span="1.5">
        <el-button type="success" plain icon="Edit" :disabled="single" @click="handleUpdate" v-hasPermi="['business:saleReturn:edit']">修改</el-button>
      </el-col>
      <el-col :span="1.5">
        <el-button type="info" plain icon="Upload" :disabled="single" @click="handleSubmit" v-hasPermi="['business:saleReturn:edit']">提交</el-button>
      </el-col>
      <el-col :span="1.5">
        <el-button type="warning" plain icon="CircleCheck" :disabled="single" @click="handleAudit" v-hasPermi="['business:saleReturn:audit']">审核</el-button>
      </el-col>
      <el-col :span="1.5">
        <el-button type="danger" plain icon="Delete" :disabled="multiple" @click="handleDelete" v-hasPermi="['business:saleReturn:remove']">删除</el-button>
      </el-col>
      <el-col :span="1.5">
        <el-button type="warning" plain icon="Download" @click="handleExport" v-hasPermi="['business:saleReturn:export']">导出</el-button>
      </el-col>
      <right-toolbar v-model:showSearch="showSearch" @queryTable="getList"></right-toolbar>
    </el-row>

    <el-table v-loading="loading" :data="saleReturnList" @selection-change="handleSelectionChange">
      <el-table-column type="selection" width="55" align="center" />
      <el-table-column label="退货编号" align="center" prop="saleReturnId" />
      <el-table-column label="退货单号" align="center" prop="returnNo" min-width="180" />
      <el-table-column label="退货类型" align="center" prop="returnType" />
      <el-table-column label="客户编号" align="center" prop="customerId" />
      <el-table-column label="仓库编号" align="center" prop="warehouseId" />
      <el-table-column label="总数量" align="center" prop="totalQty" />
      <el-table-column label="总金额" align="center" prop="totalAmount" />
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
          <el-button link type="primary" icon="Edit" @click="handleUpdate(scope.row)" v-hasPermi="['business:saleReturn:edit']">修改</el-button>
          <el-button link type="primary" icon="Upload" @click="handleSubmit(scope.row)" v-hasPermi="['business:saleReturn:edit']">提交</el-button>
          <el-button link type="primary" icon="CircleCheck" @click="handleAudit(scope.row)" v-hasPermi="['business:saleReturn:audit']">审核</el-button>
          <el-button link type="primary" icon="CircleClose" @click="handleCancelOrder(scope.row)" v-hasPermi="['business:saleReturn:edit']">作废</el-button>
          <el-button link type="primary" icon="Delete" @click="handleDelete(scope.row)" v-hasPermi="['business:saleReturn:remove']">删除</el-button>
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
      <el-form ref="saleReturnRef" :model="form" :rules="rules" label-width="110px">
        <el-row>
          <el-col :span="12">
            <el-form-item label="退货类型" prop="returnType">
              <el-input v-model="form.returnType" placeholder="请输入退货类型" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="客户编号" prop="customerId">
              <el-input-number v-model="form.customerId" :min="1" controls-position="right" style="width: 100%" />
            </el-form-item>
          </el-col>
        </el-row>
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
            <el-form-item label="总数量" prop="totalQty">
              <el-input-number v-model="form.totalQty" :min="0" :precision="2" controls-position="right" style="width: 100%" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="总金额" prop="totalAmount">
              <el-input-number v-model="form.totalAmount" :min="0" :precision="2" controls-position="right" style="width: 100%" />
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

<script setup name="BusinessSaleReturn">
import { listSaleReturn, getSaleReturn, addSaleReturn, updateSaleReturn, delSaleReturn, submitSaleReturn, auditSaleReturn, cancelSaleReturn } from "@/api/business/saleReturn"

const { proxy } = getCurrentInstance()

const saleReturnList = ref([])
const open = ref(false)
const loading = ref(true)
const showSearch = ref(true)
const ids = ref([])
const single = ref(true)
const multiple = ref(true)
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
    returnNo: undefined,
    returnType: undefined,
    customerId: undefined,
    warehouseId: undefined,
    status: undefined
  },
  rules: {
    returnType: [{ required: true, message: "退货类型不能为空", trigger: "blur" }],
    customerId: [{ required: true, message: "客户编号不能为空", trigger: "blur" }],
    warehouseId: [{ required: true, message: "仓库编号不能为空", trigger: "blur" }]
  }
})

const { queryParams, form, rules } = toRefs(data)

function getList() {
  loading.value = true
  listSaleReturn(queryParams.value).then(response => {
    saleReturnList.value = response.rows
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
    saleReturnId: undefined,
    returnNo: undefined,
    returnType: undefined,
    customerId: undefined,
    warehouseId: undefined,
    totalQty: 0,
    totalAmount: 0,
    status: "draft",
    auditBy: undefined,
    auditTime: undefined,
    remark: undefined
  }
  proxy.resetForm("saleReturnRef")
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
  ids.value = selection.map(item => item.saleReturnId)
  single.value = selection.length !== 1
  multiple.value = !selection.length
}

function handleAdd() {
  reset()
  open.value = true
  title.value = "新增销售退货单"
}

function handleUpdate(row) {
  reset()
  const saleReturnId = row.saleReturnId || ids.value
  getSaleReturn(saleReturnId).then(response => {
    form.value = response.data
    open.value = true
    title.value = "修改销售退货单"
  })
}

function submitForm() {
  proxy.$refs["saleReturnRef"].validate(valid => {
    if (valid) {
      if (form.value.saleReturnId !== undefined) {
        updateSaleReturn(form.value).then(() => {
          proxy.$modal.msgSuccess("修改成功")
          open.value = false
          getList()
        })
      } else {
        addSaleReturn(form.value).then(() => {
          proxy.$modal.msgSuccess("新增成功")
          open.value = false
          getList()
        })
      }
    }
  })
}

function handleSubmit(row) {
  const saleReturnId = row?.saleReturnId || ids.value[0]
  proxy.$modal.confirm('是否确认提交销售退货单编号为"' + saleReturnId + '"的数据项？').then(function () {
    return submitSaleReturn(saleReturnId)
  }).then(() => {
    getList()
    proxy.$modal.msgSuccess("提交成功")
  }).catch(() => {})
}

function handleAudit(row) {
  const saleReturnId = row?.saleReturnId || ids.value[0]
  proxy.$modal.confirm('是否确认审核销售退货单编号为"' + saleReturnId + '"的数据项？').then(function () {
    return auditSaleReturn(saleReturnId)
  }).then(() => {
    getList()
    proxy.$modal.msgSuccess("审核成功")
  }).catch(() => {})
}

function handleCancelOrder(row) {
  const saleReturnId = row?.saleReturnId || ids.value[0]
  proxy.$modal.confirm('是否确认作废销售退货单编号为"' + saleReturnId + '"的数据项？').then(function () {
    return cancelSaleReturn(saleReturnId)
  }).then(() => {
    getList()
    proxy.$modal.msgSuccess("作废成功")
  }).catch(() => {})
}

function handleDelete(row) {
  const saleReturnIds = row.saleReturnId || ids.value
  proxy.$modal.confirm('是否确认删除销售退货单编号为"' + saleReturnIds + '"的数据项？').then(function () {
    return delSaleReturn(saleReturnIds)
  }).then(() => {
    getList()
    proxy.$modal.msgSuccess("删除成功")
  }).catch(() => {})
}

function handleExport() {
  proxy.download("business/saleReturn/export", {
    ...queryParams.value
  }, `saleReturn_${new Date().getTime()}.xlsx`)
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

getList()
</script>
