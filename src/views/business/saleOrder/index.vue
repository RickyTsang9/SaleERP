<template>
  <div class="app-container">
    <el-form :model="queryParams" ref="queryRef" :inline="true" v-show="showSearch">
      <el-form-item label="销售单号" prop="orderNo">
        <el-input v-model="queryParams.orderNo" placeholder="请输入销售单号" clearable style="width: 220px" @keyup.enter="handleQuery" />
      </el-form-item>
      <el-form-item label="客户编号" prop="customerId">
        <el-input v-model="queryParams.customerId" placeholder="请输入客户编号" clearable style="width: 180px" @keyup.enter="handleQuery" />
      </el-form-item>
      <el-form-item label="单据状态" prop="status">
        <el-input v-model="queryParams.status" placeholder="请输入单据状态" clearable style="width: 180px" @keyup.enter="handleQuery" />
      </el-form-item>
      <el-form-item label="付款状态" prop="paymentStatus">
        <el-input v-model="queryParams.paymentStatus" placeholder="请输入付款状态" clearable style="width: 180px" @keyup.enter="handleQuery" />
      </el-form-item>
      <el-form-item>
        <el-button type="primary" icon="Search" @click="handleQuery">搜索</el-button>
        <el-button icon="Refresh" @click="resetQuery">重置</el-button>
      </el-form-item>
    </el-form>

    <el-row :gutter="10" class="mb8">
      <el-col :span="1.5">
        <el-button type="primary" plain icon="Plus" @click="handleAdd" v-hasPermi="['business:saleOrder:add']">新增</el-button>
      </el-col>
      <el-col :span="1.5">
        <el-button type="success" plain icon="Edit" :disabled="single" @click="handleUpdate" v-hasPermi="['business:saleOrder:edit']">修改</el-button>
      </el-col>
      <el-col :span="1.5">
        <el-button type="warning" plain :disabled="single || selectedStatus !== 'draft'" @click="handleSubmit" v-hasPermi="['business:saleOrder:submit']">提交</el-button>
      </el-col>
      <el-col :span="1.5">
        <el-button type="warning" plain :disabled="single || selectedStatus !== 'submitted'" @click="handleManagerAudit" v-hasPermi="['business:saleOrder:managerAudit']">经理审核</el-button>
      </el-col>
      <el-col :span="1.5">
        <el-button type="danger" plain :disabled="single || selectedStatus !== 'manager_approved'" @click="handleManagerRollback" v-hasPermi="['business:saleOrder:managerAudit']">经理回退</el-button>
      </el-col>
      <el-col :span="1.5">
        <el-button type="warning" plain :disabled="single || selectedStatus !== 'manager_approved'" @click="handleFinanceAudit" v-hasPermi="['business:saleOrder:financeAudit']">财务审核</el-button>
      </el-col>
      <el-col :span="1.5">
        <el-button type="danger" plain :disabled="single || selectedStatus !== 'audited'" @click="handleFinanceRollback" v-hasPermi="['business:saleOrder:financeAudit']">财务回退</el-button>
      </el-col>
      <el-col :span="1.5">
        <el-button type="info" plain :disabled="single" @click="handleCancel" v-hasPermi="['business:saleOrder:edit']">作废</el-button>
      </el-col>
      <el-col :span="1.5">
        <el-button type="danger" plain icon="Delete" :disabled="multiple" @click="handleDelete" v-hasPermi="['business:saleOrder:remove']">删除</el-button>
      </el-col>
      <el-col :span="1.5">
        <el-button type="info" plain icon="Upload" @click="handleImport" v-hasPermi="['business:saleOrder:import']">导入</el-button>
      </el-col>
      <el-col :span="1.5">
        <el-button type="warning" plain icon="Download" @click="handleExport" v-hasPermi="['business:saleOrder:export']">导出</el-button>
      </el-col>
      <el-col :span="1.5">
        <el-button type="primary" plain :disabled="single" @click="handlePrintTemplate" v-hasPermi="['business:saleOrder:print']">打印模板</el-button>
      </el-col>
      <el-col :span="1.5">
        <el-button type="primary" plain :disabled="single" @click="handleStatusHistory" v-hasPermi="['business:saleOrder:query']">状态历史</el-button>
      </el-col>
      <right-toolbar v-model:showSearch="showSearch" @queryTable="getList"></right-toolbar>
    </el-row>

    <el-table v-loading="loading" :data="saleOrderList" @selection-change="handleSelectionChange">
      <el-table-column type="selection" width="55" align="center" />
      <el-table-column label="销售单编号" align="center" prop="saleOrderId" />
      <el-table-column label="销售单号" align="center" prop="orderNo" min-width="180" />
      <el-table-column label="客户编号" align="center" prop="customerId" />
      <el-table-column label="仓库编号" align="center" prop="warehouseId" />
      <el-table-column label="总数量" align="center" prop="totalQty" />
      <el-table-column label="总金额" align="center" prop="totalAmount" />
      <el-table-column label="单据状态" align="center" prop="status" />
      <el-table-column label="付款状态" align="center" prop="paymentStatus" />
      <el-table-column label="创建时间" align="center" prop="createTime" width="180">
        <template #default="scope">
          <span>{{ parseTime(scope.row.createTime) }}</span>
        </template>
      </el-table-column>
      <el-table-column label="操作" width="180" align="center" class-name="small-padding fixed-width">
        <template #default="scope">
          <el-button link type="primary" icon="Edit" @click="handleUpdate(scope.row)" v-hasPermi="['business:saleOrder:edit']">修改</el-button>
          <el-button link type="primary" icon="Delete" @click="handleDelete(scope.row)" v-hasPermi="['business:saleOrder:remove']">删除</el-button>
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
      <el-form ref="saleOrderRef" :model="form" :rules="rules" label-width="110px">
        <el-row>
          <el-col :span="12">
            <el-form-item label="客户编号" prop="customerId">
              <el-input-number v-model="form.customerId" :min="1" controls-position="right" style="width: 100%" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="仓库编号" prop="warehouseId">
              <el-input-number v-model="form.warehouseId" :min="1" controls-position="right" style="width: 100%" />
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
            <el-form-item label="单据状态" prop="status">
              <el-input v-model="form.status" placeholder="请输入单据状态" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="付款状态" prop="paymentStatus">
              <el-input v-model="form.paymentStatus" placeholder="请输入付款状态" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-row>
          <el-col :span="24">
            <el-form-item label="备注" prop="remark">
              <el-input v-model="form.remark" type="textarea" placeholder="请输入备注" />
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

    <el-dialog :title="upload.title" v-model="upload.open" width="400px" append-to-body>
      <el-upload
        ref="uploadRef"
        :limit="1"
        accept=".xlsx, .xls"
        :headers="upload.headers"
        :action="upload.url + '?updateSupport=' + upload.updateSupport"
        :disabled="upload.isUploading"
        :on-progress="handleFileUploadProgress"
        :on-success="handleFileSuccess"
        :on-change="handleFileChange"
        :on-remove="handleFileRemove"
        :auto-upload="false"
        drag
      >
        <el-icon class="el-icon--upload"><upload-filled /></el-icon>
        <div class="el-upload__text">将文件拖到此处，或<em>点击上传</em></div>
        <template #tip>
          <div class="el-upload__tip text-center">
            <div class="el-upload__tip">
              <el-checkbox v-model="upload.updateSupport" />是否更新已经存在的销售订单数据
            </div>
            <span>仅允许导入xls、xlsx格式文件。</span>
            <el-link type="primary" underline="never" style="font-size: 12px; vertical-align: baseline" @click="importTemplate">下载模板</el-link>
          </div>
        </template>
      </el-upload>
      <template #footer>
        <div class="dialog-footer">
          <el-button type="primary" @click="submitFileForm">确 定</el-button>
          <el-button @click="upload.open = false">取 消</el-button>
        </div>
      </template>
    </el-dialog>

    <el-dialog title="状态历史" v-model="statusHistoryOpen" width="920px" append-to-body>
      <el-table v-loading="statusHistoryLoading" :data="statusHistoryList">
        <el-table-column label="历史编号" align="center" prop="historyId" width="90" />
        <el-table-column label="原状态" align="center" prop="fromStatus" min-width="120" />
        <el-table-column label="新状态" align="center" prop="toStatus" min-width="120" />
        <el-table-column label="操作类型" align="center" prop="operationType" min-width="140" />
        <el-table-column label="审核角色" align="center" prop="auditRole" min-width="120" />
        <el-table-column label="操作人" align="center" prop="auditBy" min-width="120" />
        <el-table-column label="审核意见" align="center" prop="auditComment" min-width="200" />
        <el-table-column label="操作时间" align="center" prop="operateTime" width="180">
          <template #default="scope">
            <span>{{ parseTime(scope.row.operateTime) }}</span>
          </template>
        </el-table-column>
      </el-table>
    </el-dialog>
  </div>
</template>

<script setup name="BusinessSaleOrder">
import { getToken } from "@/utils/auth"
import { listSaleOrder, getSaleOrder, addSaleOrder, updateSaleOrder, delSaleOrder, submitSaleOrder, managerAuditSaleOrder, managerRollbackSaleOrder, financeAuditSaleOrder, financeRollbackSaleOrder, cancelSaleOrder, getSaleOrderPrintTemplate, listSaleOrderStatusHistory } from "@/api/business/saleOrder"

const { proxy } = getCurrentInstance()

const saleOrderList = ref([])
const open = ref(false)
const loading = ref(true)
const showSearch = ref(true)
const ids = ref([])
const single = ref(true)
const multiple = ref(true)
const total = ref(0)
const selectedStatus = ref("")
const title = ref("")
const statusHistoryOpen = ref(false)
const statusHistoryLoading = ref(false)
const statusHistoryList = ref([])
const upload = reactive({
  open: false,
  title: "",
  isUploading: false,
  updateSupport: 0,
  headers: { Authorization: "Bearer " + getToken() },
  url: import.meta.env.VITE_APP_BASE_API + "/business/saleOrder/importData",
  selectedFile: null
})
const route = useRoute()

const data = reactive({
  form: {},
  queryParams: {
    pageNum: 1,
    pageSize: 10,
    saleOrderId: undefined,
    orderNo: undefined,
    customerId: undefined,
    status: undefined,
    paymentStatus: undefined
  },
  rules: {
    customerId: [{ required: true, message: "客户编号不能为空", trigger: "blur" }],
    warehouseId: [{ required: true, message: "仓库编号不能为空", trigger: "blur" }],
    totalQty: [{ required: true, message: "总数量不能为空", trigger: "blur" }],
    totalAmount: [{ required: true, message: "总金额不能为空", trigger: "blur" }]
  }
})

const { queryParams, form, rules } = toRefs(data)

function getList() {
  loading.value = true
  listSaleOrder(queryParams.value).then(response => {
    saleOrderList.value = response.rows
    total.value = response.total
  }).finally(() => {
    loading.value = false
  })
}

function cancel() {
  open.value = false
  reset()
}

function reset() {
  form.value = {
    saleOrderId: undefined,
    orderNo: undefined,
    customerId: undefined,
    warehouseId: undefined,
    totalQty: 0,
    totalAmount: 0,
    status: "draft",
    paymentStatus: "unpaid",
    auditBy: undefined,
    auditTime: undefined,
    managerAuditBy: undefined,
    managerAuditTime: undefined,
    remark: undefined
  }
  proxy.resetForm("saleOrderRef")
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
  ids.value = selection.map(item => item.saleOrderId)
  single.value = selection.length !== 1
  multiple.value = !selection.length
  selectedStatus.value = selection.length === 1 ? selection[0].status : ""
}

function handleAdd() {
  reset()
  open.value = true
  title.value = "新增销售订单"
}

function handleUpdate(row) {
  reset()
  const saleOrderId = row.saleOrderId || ids.value[0]
  getSaleOrder(saleOrderId).then(response => {
    form.value = response.data
    open.value = true
    title.value = "修改销售订单"
  })
}

function submitForm() {
  proxy.$refs["saleOrderRef"].validate(valid => {
    if (valid) {
      if (form.value.saleOrderId !== undefined) {
        updateSaleOrder(form.value).then(() => {
          proxy.$modal.msgSuccess("修改成功")
          open.value = false
          getList()
        })
      } else {
        addSaleOrder(form.value).then(() => {
          proxy.$modal.msgSuccess("新增成功")
          open.value = false
          getList()
        })
      }
    }
  })
}

function handleSubmit() {
  const saleOrderId = ids.value[0]
  submitSaleOrder(saleOrderId).then(() => {
    proxy.$modal.msgSuccess("提交成功")
    getList()
  })
}

// 经理审核按钮操作
function handleManagerAudit() {
  const saleOrderId = ids.value[0]
  proxy.$modal.prompt("请输入经理审核意见", "系统提示", {
    confirmButtonText: "确定",
    cancelButtonText: "取消",
    inputPlaceholder: "请输入审核意见（选填）",
    closeOnClickModal: false
  }).then(({ value }) => {
    return managerAuditSaleOrder(saleOrderId, { managerAuditComment: value })
  }).then(() => {
    proxy.$modal.msgSuccess("经理审核成功")
    getList()
  }).catch(() => {})
}

// 经理回退按钮操作
function handleManagerRollback() {
  const saleOrderId = ids.value[0]
  proxy.$modal.prompt("请输入经理回退意见", "系统提示", {
    confirmButtonText: "确定",
    cancelButtonText: "取消",
    inputPlaceholder: "请输入回退意见（选填）",
    closeOnClickModal: false
  }).then(({ value }) => {
    return managerRollbackSaleOrder(saleOrderId, { managerAuditComment: value })
  }).then(() => {
    proxy.$modal.msgSuccess("经理回退成功")
    getList()
  }).catch(() => {})
}

// 财务审核按钮操作
function handleFinanceAudit() {
  const saleOrderId = ids.value[0]
  proxy.$modal.prompt("请输入财务审核意见", "系统提示", {
    confirmButtonText: "确定",
    cancelButtonText: "取消",
    inputPlaceholder: "请输入审核意见（选填）",
    closeOnClickModal: false
  }).then(({ value }) => {
    return financeAuditSaleOrder(saleOrderId, { financeAuditComment: value })
  }).then(() => {
    proxy.$modal.msgSuccess("财务审核成功")
    getList()
  }).catch(() => {})
}

// 财务回退按钮操作
function handleFinanceRollback() {
  const saleOrderId = ids.value[0]
  proxy.$modal.prompt("请输入财务回退意见", "系统提示", {
    confirmButtonText: "确定",
    cancelButtonText: "取消",
    inputPlaceholder: "请输入回退意见（选填）",
    closeOnClickModal: false
  }).then(({ value }) => {
    return financeRollbackSaleOrder(saleOrderId, { financeAuditComment: value })
  }).then(() => {
    proxy.$modal.msgSuccess("财务回退成功")
    getList()
  }).catch(() => {})
}

function handleCancel() {
  const saleOrderId = ids.value[0]
  cancelSaleOrder(saleOrderId).then(() => {
    proxy.$modal.msgSuccess("作废成功")
    getList()
  })
}

function handleDelete(row) {
  const saleOrderIds = row.saleOrderId || ids.value
  proxy.$modal.confirm('是否确认删除销售订单编号为"' + saleOrderIds + '"的数据项？').then(function () {
    return delSaleOrder(saleOrderIds)
  }).then(() => {
    getList()
    proxy.$modal.msgSuccess("删除成功")
  }).catch(() => {})
}

function handleExport() {
  proxy.download("business/saleOrder/export", {
    ...queryParams.value
  }, `saleOrder_${new Date().getTime()}.xlsx`)
}

// 导入按钮操作
function handleImport() {
  upload.title = "销售订单导入"
  upload.open = true
  upload.selectedFile = null
}

// 下载导入模板
function importTemplate() {
  proxy.download("business/saleOrder/importTemplate", {}, `saleOrder_template_${new Date().getTime()}.xlsx`)
}

// 文件上传进度
function handleFileUploadProgress() {
  upload.isUploading = true
}

// 文件选择处理
function handleFileChange(file) {
  upload.selectedFile = file
}

// 文件删除处理
function handleFileRemove() {
  upload.selectedFile = null
  upload.isUploading = false
}

// 文件上传成功处理
function handleFileSuccess(response, file) {
  upload.open = false
  upload.isUploading = false
  proxy.$refs.uploadRef.handleRemove(file)
  proxy.$alert(response.msg, "导入结果", { dangerouslyUseHTMLString: true })
  getList()
}

// 提交上传文件
function submitFileForm() {
  const file = upload.selectedFile
  if (!file || file.length === 0 || !file.name.toLowerCase().endsWith('.xls') && !file.name.toLowerCase().endsWith('.xlsx')) {
    proxy.$modal.msgError("请选择后缀为 “xls”或“xlsx”的文件。")
    return
  }
  proxy.$refs.uploadRef.submit()
}

// 打印模板操作
function handlePrintTemplate() {
  const saleOrderId = ids.value[0]
  getSaleOrderPrintTemplate(saleOrderId).then(response => {
    const printData = response.data || {}
    const saleOrder = printData.saleOrder || {}
    const itemList = printData.itemList || []
    const itemRowsHtml = itemList.map((item, itemIndex) => {
      return "<tr>" +
        "<td>" + (itemIndex + 1) + "</td>" +
        "<td>" + (item.productId || "") + "</td>" +
        "<td>" + (item.locationId || "") + "</td>" +
        "<td>" + (item.batchNo || "") + "</td>" +
        "<td>" + (item.quantity || 0) + "</td>" +
        "<td>" + (item.price || 0) + "</td>" +
        "<td>" + (item.amount || 0) + "</td>" +
      "</tr>"
    }).join("")
    const printWindow = window.open("", "_blank")
    printWindow.document.write("<html><head><title>销售订单打印模板</title><style>body{font-family:Arial;padding:24px;}table{width:100%;border-collapse:collapse;}th,td{border:1px solid #dcdfe6;padding:6px;text-align:center;}h2{margin-bottom:12px;}p{margin:4px 0;}</style></head><body>")
    printWindow.document.write("<h2>销售订单打印模板</h2>")
    printWindow.document.write("<p>销售单号：" + (saleOrder.orderNo || "") + "</p>")
    printWindow.document.write("<p>客户编号：" + (saleOrder.customerId || "") + "</p>")
    printWindow.document.write("<p>仓库编号：" + (saleOrder.warehouseId || "") + "</p>")
    printWindow.document.write("<p>总数量：" + (saleOrder.totalQty || 0) + "，总金额：" + (saleOrder.totalAmount || 0) + "</p>")
    printWindow.document.write("<table><thead><tr><th>序号</th><th>商品编号</th><th>库位编号</th><th>批次号</th><th>数量</th><th>单价</th><th>金额</th></tr></thead><tbody>" + itemRowsHtml + "</tbody></table>")
    printWindow.document.write("</body></html>")
    printWindow.document.close()
    printWindow.focus()
    printWindow.print()
  })
}

// 状态历史按钮操作
function handleStatusHistory() {
  const saleOrderId = ids.value[0]
  statusHistoryOpen.value = true
  statusHistoryLoading.value = true
  listSaleOrderStatusHistory(saleOrderId).then(response => {
    statusHistoryList.value = response.data || []
    statusHistoryLoading.value = false
  }).catch(() => {
    statusHistoryLoading.value = false
  })
}

if (route.query.saleOrderId)
{
  queryParams.value.saleOrderId = route.query.saleOrderId
}
getList()
</script>
