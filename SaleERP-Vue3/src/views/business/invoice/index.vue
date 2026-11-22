<template>
  <div class="app-container erp-page invoice-ledger-page">
    <erp-page-header
      eyebrow="税务合规 · 发票管理"
      title="进销项发票台账"
      subtitle="全流程管理数电票、增值税专票与普票，自动价税分离、估算应纳增值税额并保留完整红冲留痕。"
      icon="Postcard"
      tone="violet"
      :badge="`共 ${total} 张发票`"
    >
      <template #actions>
        <el-button icon="Download" plain @click="handleExport" v-hasPermi="['business:invoice:export']">导出台账</el-button>
        <el-button type="success" plain icon="Plus" @click="handleAdd('purchase')" v-hasPermi="['business:invoice:add']">登记进项票</el-button>
        <el-button type="primary" icon="Plus" @click="handleAdd('sale')" v-hasPermi="['business:invoice:add']">登记销项票</el-button>
      </template>
    </erp-page-header>

    <erp-stat-strip :items="invoiceStatItems" :columns="4" />

    <section class="erp-filter-card" v-show="showSearch">
      <el-form :model="queryParams" ref="queryRef" :inline="true" label-width="82px">
        <el-form-item label="发票号码" prop="invoiceNo">
          <el-input v-model="queryParams.invoiceNo" placeholder="请输入发票号码" clearable style="width: 185px" @keyup.enter="handleQuery" />
        </el-form-item>
        <el-form-item label="发票类型" prop="invoiceType">
          <el-select v-model="queryParams.invoiceType" placeholder="全部类型" clearable style="width: 145px">
            <el-option label="全电数电票" value="electronic" />
            <el-option label="增值税专票" value="special" />
            <el-option label="增值税普票" value="normal" />
          </el-select>
        </el-form-item>
        <el-form-item label="往来单位" prop="targetName">
          <el-input v-model="queryParams.targetName" placeholder="客户或供应商名称" clearable style="width: 180px" @keyup.enter="handleQuery" />
        </el-form-item>
        <el-form-item label="状态" prop="status">
          <el-select v-model="queryParams.status" placeholder="全部状态" clearable style="width: 135px">
            <el-option label="正常有效" value="issued" />
            <el-option label="已红冲作废" value="red_void" />
            <el-option label="草稿" value="draft" />
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
          <erp-status-tabs :model-value="queryParams.invoiceDirection ?? ''" :items="directionTabItems" @change="handleQuickDirection" />
        </div>
        <div class="erp-toolbar-right">
          <right-toolbar v-model:showSearch="showSearch" @queryTable="refreshAll" />
        </div>
      </div>

      <el-table v-loading="loading" :data="invoiceList">
        <el-table-column label="发票号码" align="center" prop="invoiceNo" min-width="150" show-overflow-tooltip>
          <template #default="scope">
            <span class="erp-code-chip">{{ scope.row.invoiceNo }}</span>
          </template>
        </el-table-column>
        <el-table-column label="方向 / 票种" align="center" width="136">
          <template #default="scope">
            <el-tag size="small" :type="scope.row.invoiceDirection === 'sale' ? 'primary' : 'success'" effect="light" style="margin-right: 4px">
              {{ scope.row.invoiceDirection === 'sale' ? '销项' : '进项' }}
            </el-tag>
            <span class="erp-cell-sub">{{ formatInvoiceTypeShort(scope.row.invoiceType) }}</span>
          </template>
        </el-table-column>
        <el-table-column label="往来单位 / 关联单" align="left" prop="targetName" min-width="195" show-overflow-tooltip>
          <template #default="scope">
            <div class="erp-cell-title">{{ scope.row.targetName }}</div>
            <div class="erp-cell-sub">
              关联单号：
              <el-button
                v-if="scope.row.sourceOrderId && scope.row.sourceOrderNo"
                link
                type="primary"
                size="small"
                style="padding: 0; height: auto; font-size: 12px"
                @click="handleJumpSourceOrder(scope.row)"
              >
                {{ scope.row.sourceOrderNo }}
              </el-button>
              <span v-else>{{ scope.row.sourceOrderNo || '-' }}</span>
            </div>
          </template>
        </el-table-column>
        <el-table-column label="价税合计" align="right" prop="totalAmount" width="112">
          <template #default="scope">
            <span class="erp-amount is-primary">¥{{ formatMoney(scope.row.totalAmount) }}</span>
          </template>
        </el-table-column>
        <el-table-column label="不含税额" align="right" prop="amountWithoutTax" width="106">
          <template #default="scope">
            <span class="erp-num">¥{{ formatMoney(scope.row.amountWithoutTax) }}</span>
          </template>
        </el-table-column>
        <el-table-column label="税额 (税率)" align="right" width="122">
          <template #default="scope">
            <span class="erp-num">¥{{ formatMoney(scope.row.taxAmount) }} ({{ Number(scope.row.taxRate ?? 13) }}%)</span>
          </template>
        </el-table-column>
        <el-table-column label="开票日期" align="center" prop="invoiceDate" width="108">
          <template #default="scope">
            <span>{{ parseTime(scope.row.invoiceDate, '{y}-{m}-{d}') }}</span>
          </template>
        </el-table-column>
        <el-table-column label="状态" align="center" prop="status" width="96">
          <template #default="scope">
            <el-tooltip v-if="scope.row.status === 'red_void'" :content="scope.row.voidReason || '已红冲'" placement="top">
              <el-tag size="small" type="danger" effect="light">已红冲</el-tag>
            </el-tooltip>
            <el-tag v-else-if="scope.row.status === 'draft'" size="small" type="info" effect="light">草稿</el-tag>
            <el-tag v-else size="small" type="success" effect="light">正常有效</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="195" fixed="right" align="center">
          <template #default="scope">
            <el-button link type="info" icon="Printer" @click="handlePrintInvoice(scope.row)">打印</el-button>
            <el-button v-if="scope.row.status !== 'red_void'" link type="primary" icon="Edit" @click="handleEdit(scope.row)" v-hasPermi="['business:invoice:edit']">修改</el-button>
            <el-button v-if="scope.row.status !== 'red_void'" link type="danger" icon="RefreshLeft" @click="handleVoid(scope.row)" v-hasPermi="['business:invoice:void']">红冲</el-button>
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

    <el-dialog v-model="open" :title="dialogTitle" width="680px" append-to-body>
      <el-form ref="invoiceFormRef" :model="form" :rules="rules" label-width="110px">
        <el-row :gutter="14">
          <el-col :span="12">
            <el-form-item label="发票方向" prop="invoiceDirection">
              <el-radio-group v-model="form.invoiceDirection" @change="handleDirectionChange">
                <el-radio-button value="sale">销项发票</el-radio-button>
                <el-radio-button value="purchase">进项发票</el-radio-button>
              </el-radio-group>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="发票类型" prop="invoiceType">
              <el-select v-model="form.invoiceType" style="width: 100%">
                <el-option label="全电数电票" value="electronic" />
                <el-option label="增值税专用发票" value="special" />
                <el-option label="增值税普通发票" value="normal" />
              </el-select>
            </el-form-item>
          </el-col>
        </el-row>

        <el-row :gutter="14">
          <el-col :span="12">
            <el-form-item label="发票号码" prop="invoiceNo">
              <el-input v-model="form.invoiceNo" placeholder="请输入20位数电票号或8位票号" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="发票代码" prop="invoiceCode">
              <el-input v-model="form.invoiceCode" placeholder="数电票可留空" />
            </el-form-item>
          </el-col>
        </el-row>

        <el-row :gutter="14">
          <el-col :span="12">
            <el-form-item :label="form.invoiceDirection === 'sale' ? '关联销售单' : '关联采购单'">
              <el-select
                v-model="form.sourceOrderId"
                placeholder="可选，关联后自动带出往来单位与金额"
                clearable
                filterable
                style="width: 100%"
                @change="handlePickSourceOrder"
              >
                <el-option
                  v-for="ord in sourceOrderOptions"
                  :key="ord.id"
                  :label="`${ord.orderNo}（总额¥${formatMoney(ord.totalAmount)} / 已票¥${formatMoney(ord.invoicedAmount)}）`"
                  :value="ord.id"
                />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item :label="form.invoiceDirection === 'sale' ? '购方客户' : '销方供应商'" prop="targetId">
              <el-select v-model="form.targetId" placeholder="请选择往来单位" filterable style="width: 100%" @change="handlePickTarget">
                <el-option
                  v-for="t in targetOptions"
                  :key="t.id"
                  :label="t.name"
                  :value="t.id"
                />
              </el-select>
            </el-form-item>
          </el-col>
        </el-row>

        <el-row :gutter="14">
          <el-col :span="12">
            <el-form-item label="价税合计" prop="totalAmount">
              <el-input-number v-model="form.totalAmount" :min="0.01" :precision="2" controls-position="right" style="width: 100%" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="适用税率(%)" prop="taxRate">
              <el-select v-model="form.taxRate" style="width: 100%">
                <el-option label="13%（标准货物销售税率）" :value="13" />
                <el-option label="9%（运输/农产品税率）" :value="9" />
                <el-option label="6%（现代服务业税率）" :value="6" />
                <el-option label="3%（小规模征收率）" :value="3" />
                <el-option label="1%（小规模优惠征收率）" :value="1" />
                <el-option label="0%（免税）" :value="0" />
              </el-select>
            </el-form-item>
          </el-col>
        </el-row>

        <el-row :gutter="14">
          <el-col :span="12">
            <el-form-item label="不含税金额">
              <el-input :model-value="'¥' + previewTaxCalc.withoutTax" disabled />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="自动计算税额">
              <el-input :model-value="'¥' + previewTaxCalc.tax" disabled />
            </el-form-item>
          </el-col>
        </el-row>

        <el-row :gutter="14">
          <el-col :span="12">
            <el-form-item label="开票日期" prop="invoiceDate">
              <el-date-picker v-model="form.invoiceDate" type="date" value-format="YYYY-MM-DD" style="width: 100%" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="备注说明" prop="remark">
              <el-input v-model="form.remark" placeholder="选填备注" />
            </el-form-item>
          </el-col>
        </el-row>
      </el-form>
      <template #footer>
        <div class="dialog-footer">
          <el-button type="primary" :loading="saving" @click="submitForm">保 存</el-button>
          <el-button @click="open = false">取 消</el-button>
        </div>
      </template>
    </el-dialog>

    <document-print-dialog ref="printDialogRef" />
  </div>
</template>

<script setup name="BusinessInvoice">
import { useRoute, useRouter } from "vue-router"
import DocumentPrintDialog from "@/components/DocumentPrintDialog/index.vue"
import { listInvoice, getInvoiceSummary, getInvoiceSourceOptions, getInvoice, addInvoice, updateInvoice, voidInvoice } from "@/api/business/invoice"
import { parseTime } from "@/utils/ruoyi"

const { proxy } = getCurrentInstance()
const route = useRoute()
const router = useRouter()
const printDialogRef = ref(null)

const loading = ref(true)
const showSearch = ref(true)
const invoiceList = ref([])
const total = ref(0)
const summary = ref({
  saleTotalAmount: 0,
  saleTaxAmount: 0,
  purchaseTotalAmount: 0,
  purchaseTaxAmount: 0,
  estimatedVatPayable: 0,
  voidCount: 0
})

const directionTabItems = [
  { label: "全部发票", value: "" },
  { label: "销项发票", value: "sale", dot: "indigo" },
  { label: "进项发票", value: "purchase", dot: "emerald" }
]

const invoiceStatItems = computed(() => [
  { label: "销项有效开票额", prefix: "¥", value: formatMoney(summary.value.saleTotalAmount), hint: `销项税额 ¥${formatMoney(summary.value.saleTaxAmount)}`, icon: "Postcard", tone: "indigo" },
  { label: "进项有效收票额", prefix: "¥", value: formatMoney(summary.value.purchaseTotalAmount), hint: `可抵扣进项税 ¥${formatMoney(summary.value.purchaseTaxAmount)}`, icon: "DocumentChecked", tone: "emerald" },
  { label: "预计应纳增值税", prefix: "¥", value: formatMoney(summary.value.estimatedVatPayable), hint: Number(summary.value.estimatedVatPayable || 0) >= 0 ? "销项税大于进项税（待缴）" : "进项留抵税额结余", icon: "Money", tone: Number(summary.value.estimatedVatPayable || 0) >= 0 ? "amber" : "emerald" },
  { label: "红冲作废发票", value: summary.value.voidCount || 0, suffix: "张", hint: "红冲记录保留完整税务留痕", icon: "RefreshLeft", tone: "rose" }
])

function handleQuickDirection(val) {
  queryParams.value.invoiceDirection = val === "" ? undefined : val
  handleQuery()
}

const open = ref(false)
const saving = ref(false)
const dialogTitle = ref("登记发票")
const customerList = ref([])
const supplierList = ref([])
const auditedSaleOrders = ref([])
const auditedPurchaseOrders = ref([])

const queryParams = ref({
  pageNum: 1,
  pageSize: 10,
  invoiceNo: undefined,
  invoiceDirection: undefined,
  invoiceType: undefined,
  targetName: undefined,
  status: undefined
})

const form = ref({})
const rules = {
  invoiceNo: [{ required: true, message: "发票号码不能为空", trigger: "blur" }],
  targetId: [{ required: true, message: "请选择往来单位", trigger: "change" }],
  totalAmount: [{ required: true, message: "价税合计不能为空", trigger: "blur" }],
  invoiceDate: [{ required: true, message: "请选择开票日期", trigger: "change" }]
}

const targetOptions = computed(() => {
  if (form.value.invoiceDirection === "sale") {
    return customerList.value.map(c => ({ id: c.customerId, name: c.customerName }))
  }
  return supplierList.value.map(s => ({ id: s.supplierId, name: s.supplierName }))
})

const sourceOrderOptions = computed(() => {
  if (form.value.invoiceDirection === "sale") {
    return auditedSaleOrders.value.map(o => ({
      id: o.saleOrderId,
      orderNo: o.orderNo,
      targetId: o.customerId,
      targetName: o.customerName,
      totalAmount: o.totalAmount,
      invoicedAmount: o.invoicedAmount || 0
    }))
  }
  return auditedPurchaseOrders.value.map(o => ({
    id: o.purchaseOrderId,
    orderNo: o.orderNo,
    targetId: o.supplierId,
    targetName: o.supplierName,
    totalAmount: o.totalAmount,
    invoicedAmount: o.invoicedAmount || 0
  }))
})

const previewTaxCalc = computed(() => {
  const totalAmt = Number(form.value.totalAmount || 0)
  const rate = Number(form.value.taxRate ?? 13)
  if (!totalAmt || totalAmt <= 0) return { withoutTax: "0.00", tax: "0.00" }
  const withoutTax = totalAmt / (1 + rate / 100)
  const tax = totalAmt - withoutTax
  return {
    withoutTax: withoutTax.toFixed(2),
    tax: tax.toFixed(2)
  }
})

function formatMoney(val) {
  const num = Number(val || 0)
  return Number.isFinite(num) ? num.toFixed(2) : "0.00"
}

function formatInvoiceType(t) {
  if (t === "special") return "增值税专票"
  if (t === "normal") return "增值税普票"
  return "全电数电票"
}

function formatInvoiceTypeShort(t) {
  if (t === "special") return "专票"
  if (t === "normal") return "普票"
  return "数电票"
}

async function getList() {
  loading.value = true
  try {
    const res = await listInvoice(queryParams.value)
    invoiceList.value = res.rows || []
    total.value = res.total || 0
  } finally {
    loading.value = false
  }
}

async function loadSummary() {
  const res = await getInvoiceSummary(queryParams.value)
  summary.value = res.data || summary.value
}

async function refreshAll() {
  await Promise.all([getList(), loadSummary()])
}

async function ensureDialogOptionsLoaded(force = false) {
  if (!force && (customerList.value.length || supplierList.value.length)) {
    return
  }
  try {
    const res = await getInvoiceSourceOptions()
    const payload = res.data || {}
    customerList.value = payload.customers || []
    supplierList.value = payload.suppliers || []
    auditedSaleOrders.value = payload.saleOrders || []
    auditedPurchaseOrders.value = payload.purchaseOrders || []
  } catch (e) {
    // 静默兜底，防止弹窗被阻断
  }
}

function handleJumpSourceOrder(row) {
  if (!row || !row.sourceOrderId) return
  if (row.invoiceDirection === "sale") {
    router.push({ path: "/sales/saleOrder", query: { saleOrderId: String(row.sourceOrderId) } })
  } else {
    router.push({ path: "/purchase/purchaseOrder", query: { purchaseOrderId: String(row.sourceOrderId) } })
  }
}

function handleQuery() {
  queryParams.value.pageNum = 1
  refreshAll()
}

function resetQuery() {
  proxy.resetForm("queryRef")
  handleQuery()
}

function buildToday() {
  const d = new Date()
  return `${d.getFullYear()}-${String(d.getMonth() + 1).padStart(2, "0")}-${String(d.getDate()).padStart(2, "0")}`
}

async function handleAdd(direction = "sale") {
  await ensureDialogOptionsLoaded(true)
  form.value = {
    invoiceId: undefined,
    invoiceDirection: direction,
    invoiceType: "electronic",
    invoiceNo: "",
    invoiceCode: "",
    sourceOrderId: undefined,
    sourceOrderNo: "",
    targetId: undefined,
    targetName: "",
    taxRate: 13,
    totalAmount: undefined,
    invoiceDate: buildToday(),
    status: "issued",
    remark: ""
  }
  dialogTitle.value = direction === "sale" ? "登记销项发票" : "登记进项发票"
  open.value = true
}

async function handleEdit(row) {
  await ensureDialogOptionsLoaded()
  const res = await getInvoice(row.invoiceId)
  form.value = {
    ...res.data,
    taxRate: Number(res.data?.taxRate ?? 13)
  }
  dialogTitle.value = "修改发票信息"
  open.value = true
}

function handleDirectionChange() {
  form.value.sourceOrderId = undefined
  form.value.sourceOrderNo = ""
  form.value.targetId = undefined
  form.value.targetName = ""
}

function handlePickSourceOrder(orderId) {
  const picked = sourceOrderOptions.value.find(o => o.id === orderId)
  if (!picked) {
    form.value.sourceOrderNo = ""
    return
  }
  form.value.sourceOrderNo = picked.orderNo
  form.value.targetId = picked.targetId
  form.value.targetName = picked.targetName || ""
  if (!form.value.totalAmount) {
    const remain = Number(picked.totalAmount || 0) - Number(picked.invoicedAmount || 0)
    form.value.totalAmount = remain > 0 ? Number(remain.toFixed(2)) : Number(picked.totalAmount || 0)
  }
}

function handlePickTarget(targetId) {
  const picked = targetOptions.value.find(t => t.id === targetId)
  if (picked) {
    form.value.targetName = picked.name
  }
}

function submitForm() {
  proxy.$refs["invoiceFormRef"].validate(async valid => {
    if (!valid) return
    saving.value = true
    try {
      if (form.value.invoiceId) {
        await updateInvoice(form.value)
        proxy.$modal.msgSuccess("发票更新成功")
      } else {
        await addInvoice(form.value)
        proxy.$modal.msgSuccess("发票登记成功")
      }
      open.value = false
      refreshAll()
    } finally {
      saving.value = false
    }
  })
}

function handleVoid(row) {
  proxy.$modal.prompt(`确认对发票【${row.invoiceNo}】执行红冲作废？请输入红冲原因：`).then(({ value }) => {
    if (!value || !value.trim()) {
      proxy.$modal.msgWarning("请填写红冲原因")
      return Promise.reject(new Error("empty"))
    }
    return voidInvoice(row.invoiceId, value.trim())
  }).then(() => {
    proxy.$modal.msgSuccess("发票已红冲作废")
    refreshAll()
  }).catch(() => {})
}

function handlePrintInvoice(row) {
  printDialogRef.value?.openPrint({
    docType: "invoice",
    title: row.invoiceDirection === "sale" ? "销项发票登记单" : "进项发票登记单",
    docNo: row.invoiceNo || `INV-${row.invoiceId}`,
    defaultPaperMode: "a4",
    metaFields: [
      { label: "发票号码", value: row.invoiceNo || "-" },
      { label: "发票代码", value: row.invoiceCode || "数电票无代码" },
      { label: "发票方向", value: row.invoiceDirection === "sale" ? "销项发票" : "进项发票" },
      { label: "发票种类", value: formatInvoiceType(row.invoiceType) },
      { label: "往来单位", value: row.targetName || "-" },
      { label: "关联业务单", value: row.sourceOrderNo || "-" },
      { label: "开票日期", value: parseTime(row.invoiceDate, "{y}-{m}-{d}") },
      { label: "发票状态", value: row.status === "red_void" ? `已红冲（${row.voidReason || "-"}）` : (row.status === "draft" ? "草稿" : "正常有效") }
    ],
    columns: [
      { label: "应税项目", prop: "itemName" },
      { label: "不含税金额", prop: "amountWithoutTax", width: "130px", align: "right", type: "money" },
      { label: "税率", prop: "taxRate", width: "80px", align: "center" },
      { label: "税额", prop: "taxAmount", width: "120px", align: "right", type: "money" },
      { label: "价税合计", prop: "totalAmount", width: "130px", align: "right", type: "money" }
    ],
    items: [
      {
        itemName: `${row.invoiceDirection === "sale" ? "商品销售" : "商品采购"}（${row.sourceOrderNo || row.targetName || "-"}）`,
        amountWithoutTax: Number(row.amountWithoutTax || 0),
        taxRate: `${Number(row.taxRate ?? 13)}%`,
        taxAmount: Number(row.taxAmount || 0),
        totalAmount: Number(row.totalAmount || 0)
      }
    ],
    totalQty: 1,
    totalAmount: Number(row.totalAmount || 0),
    remark: row.remark || ""
  })
}

function handleExport() {
  proxy.download("business/invoice/export", { ...queryParams.value }, `invoice_${Date.now()}.xlsx`)
}

if (route.query.invoiceDirection) {
  queryParams.value.invoiceDirection = String(route.query.invoiceDirection)
}
refreshAll()
</script>

<style scoped lang="scss">
.mb12 {
  margin-bottom: 12px;
}

.fin-kpi-card {
  padding: 16px 18px;
  border-radius: 12px;
  border: 1px solid var(--el-border-color-lighter);
  background: var(--el-bg-color);
  box-shadow: 0 1px 4px rgba(0, 0, 0, 0.03);

  .kpi-label {
    font-size: 13px;
    color: var(--el-text-color-secondary);
  }

  .kpi-val {
    margin-top: 8px;
    font-size: 22px;
    font-weight: 700;
    color: var(--el-text-color-primary);

    &.text-primary {
      color: var(--el-color-primary);
    }
    &.text-success {
      color: var(--el-color-success);
    }
    &.text-warning {
      color: var(--el-color-warning);
    }
  }

  .kpi-sub {
    margin-top: 6px;
    font-size: 12px;
    color: var(--el-text-color-secondary);
  }
}
</style>
