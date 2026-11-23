<template>
  <div class="app-container erp-page">
    <erp-page-header
      eyebrow="财务核算 · 资金流水"
      title="供应商付款记录"
      subtitle="完整留痕每一笔采购付款与结算账户出账，支持差错冲红作废并自动回退应付余额与账户资金。"
      icon="Money"
      tone="blue"
      :badge="`共 ${total} 笔付款`"
    >
      <template #actions>
        <el-button type="primary" icon="Download" @click="handleExport" v-hasPermi="['business:payment:export']">导出付款</el-button>
      </template>
    </erp-page-header>

    <erp-stat-strip :items="paymentStatItems" :columns="3" />

    <section class="erp-filter-card" v-show="showSearch">
      <el-form :model="queryParams" ref="queryRef" :inline="true" label-width="82px">
        <el-form-item label="应付单号" prop="payableId">
          <el-input v-model="queryParams.payableId" placeholder="如 AP01001" clearable style="width: 165px" @keyup.enter="handleQuery" />
        </el-form-item>
        <el-form-item label="采购单号" prop="purchaseOrderNo">
          <el-input v-model="queryParams.purchaseOrderNo" placeholder="请输入采购单号" clearable style="width: 175px" @keyup.enter="handleQuery" />
        </el-form-item>
        <el-form-item label="供应商" prop="supplierId">
          <el-select v-model="queryParams.supplierId" placeholder="请选择供应商" clearable filterable remote reserve-keyword remote-show-suffix :remote-method="loadSupplierOptionList" :loading="supplierListLoading" style="width: 190px">
            <el-option v-for="supplier in supplierSelectOptionList" :key="supplier.supplierId" :label="supplier.supplierName" :value="supplier.supplierId" />
          </el-select>
        </el-form-item>
        <el-form-item label="付款方式" prop="paymentMethod">
          <el-select v-model="queryParams.paymentMethod" placeholder="全部方式" clearable style="width: 140px">
            <el-option v-for="paymentMethodOption in paymentMethodOptionList" :key="paymentMethodOption.value" :label="paymentMethodOption.label" :value="paymentMethodOption.value" />
          </el-select>
        </el-form-item>
        <el-form-item label="付款时间">
          <el-date-picker
            v-model="paymentTimeRange"
            type="datetimerange"
            value-format="YYYY-MM-DD HH:mm:ss"
            range-separator="-"
            start-placeholder="开始时间"
            end-placeholder="结束时间"
            style="width: 320px"
          />
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
          <span class="erp-cell-sub">若付款录入有误，支持执行【冲红】自动回退应付已付金额与资金账户余额</span>
        </div>
        <div class="erp-toolbar-right">
          <right-toolbar v-model:showSearch="showSearch" @queryTable="getList" />
        </div>
      </div>

      <el-table v-loading="loading" :data="paymentList">
        <el-table-column label="付款 / 应付单" align="left" min-width="126">
          <template #default="scope">
            <span class="erp-code-chip">{{ 'PM' + String(scope.row.paymentId || '').padStart(5, '0') }}</span>
            <div class="erp-cell-sub">{{ scope.row.payableId ? ('AP' + String(scope.row.payableId).padStart(5, '0')) : '-' }}</div>
          </template>
        </el-table-column>
        <el-table-column label="供应商 / 采购单" align="left" min-width="165" show-overflow-tooltip>
          <template #default="scope">
            <div class="erp-cell-title">{{ scope.row.supplierName || getSupplierName(scope.row.supplierId, scope.row.purchaseOrderId) }}</div>
            <div class="erp-cell-sub">{{ scope.row.purchaseOrderNo || getPurchaseOrderDisplayName(scope.row.purchaseOrderId) }}</div>
          </template>
        </el-table-column>
        <el-table-column label="付款金额" align="right" prop="amount" width="108">
          <template #default="scope">
            <span class="erp-amount" :class="scope.row.status === 'voided' ? 'is-danger' : 'is-primary'">
              ¥{{ Number(scope.row.amount || 0).toFixed(2) }}
            </span>
          </template>
        </el-table-column>
        <el-table-column label="方式 / 结算账户" align="left" min-width="148" show-overflow-tooltip>
          <template #default="scope">
            <div class="erp-cell-title">{{ getPaymentMethodLabel(scope.row.paymentMethod) }}</div>
            <div class="erp-cell-sub">{{ scope.row.accountName || getAccountName(scope.row.accountId) }}</div>
          </template>
        </el-table-column>
        <el-table-column label="付款时间" align="center" prop="paymentTime" width="136">
          <template #default="scope">
            <span>{{ parseTime(scope.row.paymentTime, '{y}-{m}-{d} {h}:{i}') }}</span>
          </template>
        </el-table-column>
        <el-table-column label="状态" align="center" prop="status" width="84">
          <template #default="scope">
            <el-tooltip v-if="scope.row.status === 'voided'" :content="scope.row.voidReason || '已冲红'" placement="top">
              <el-tag type="danger" effect="light" size="small">已冲红</el-tag>
            </el-tooltip>
            <el-tag v-else type="success" effect="light" size="small">正常</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="备注" align="left" prop="remark" min-width="110" show-overflow-tooltip />
        <el-table-column label="操作" width="192" fixed="right" align="center">
          <template #default="scope">
            <div class="erp-row-actions">
              <el-button link type="primary" icon="View" @click="handleViewPayable(scope.row)" v-hasPermi="['business:payable:list']">应付</el-button>
              <el-button link type="info" icon="Printer" @click="handlePrintPayment(scope.row)">凭证</el-button>
              <el-dropdown v-if="scope.row.purchaseOrderId || scope.row.status !== 'voided'" trigger="click">
                <el-button link type="primary">更多<el-icon class="el-icon--right"><ArrowDown /></el-icon></el-button>
                <template #dropdown>
                  <el-dropdown-menu>
                    <el-dropdown-item v-if="scope.row.purchaseOrderId" @click="handleViewPurchaseOrder(scope.row)">查看采购单</el-dropdown-item>
                    <el-dropdown-item v-if="scope.row.status !== 'voided'" divided @click="handleVoidPayment(scope.row)"><span style="color: var(--el-color-danger)">冲红作废</span></el-dropdown-item>
                  </el-dropdown-menu>
                </template>
              </el-dropdown>
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

    <document-print-dialog ref="printDialogRef" />
  </div>
</template>

<script setup name="BusinessPayment">
import DocumentPrintDialog from "@/components/DocumentPrintDialog/index.vue"
import { listPayment, voidPayment } from "@/api/business/payment"
import { listAccount } from "@/api/business/account"
import { listSupplier, getSupplier } from "@/api/business/supplier"
import { getPurchaseOrder } from "@/api/business/purchaseOrder"
import { appendUniqueSelectOption, buildSelectOptionList, normalizeRemoteKeyword } from "@/utils/remoteSelect"
import { parseTime } from "@/utils/ruoyi"

const { proxy } = getCurrentInstance()
const route = useRoute()
const router = useRouter()

const paymentList = ref([])
const printDialogRef = ref(null)
const accountMap = ref({})
const loading = ref(true)
const showSearch = ref(true)
const total = ref(0)
const supplierList = ref([])
const supplierListLoading = ref(false)
const paymentTimeRange = ref([])
const purchaseOrderSupplierNameMap = ref({})
const purchaseOrderDisplayNameMap = ref({})
const supplierSelectOptionList = computed(() => buildSupplierSelectOptionList())

const paymentMethodOptionList = [
  { label: "银行转账", value: "bank_transfer" },
  { label: "微信支付", value: "wechat" },
  { label: "支付宝", value: "alipay" },
  { label: "现金", value: "cash" }
]

const data = reactive({
  queryParams: {
    pageNum: 1,
    pageSize: 10,
    payableId: undefined,
    purchaseOrderId: undefined,
    purchaseOrderNo: undefined,
    supplierId: undefined,
    paymentMethod: undefined
  }
})

const { queryParams } = toRefs(data)

const paymentStatItems = computed(() => {
  const rows = paymentList.value || []
  const validAmt = rows.filter(r => r.status !== "voided").reduce((s, r) => s + Number(r.amount || 0), 0)
  const voidCount = rows.filter(r => r.status === "voided").length
  return [
    { label: "付款流水总数", value: total.value, suffix: "笔", hint: "匹配当前筛选的全部流水", icon: "Money", tone: "blue" },
    { label: "本页有效付款额", prefix: "¥", value: validAmt.toFixed(2), hint: "不含已冲红作废流水", icon: "CircleCheck", tone: "indigo" },
    { label: "本页冲红作废", value: voidCount, suffix: "笔", hint: "冲红后已自动回退余额", icon: "RefreshLeft", tone: "rose" }
  ]
})

// 根据首页或其他业务页传入的参数初始化付款流水筛选条件，保证落地页即开即用。
function initializeQueryParamsFromRoute() {
  queryParams.value.payableId = route.query.payableId || undefined
  queryParams.value.purchaseOrderId = route.query.purchaseOrderId || undefined
  queryParams.value.purchaseOrderNo = route.query.purchaseOrderNo || route.query.orderNo || undefined
  queryParams.value.supplierId = route.query.supplierId ? Number(route.query.supplierId) : undefined
}

// 初始化供应商远程下拉数据，避免页面初始阶段一次性加载过多基础资料。
function initBasicData() {
  loadSupplierOptionList()
  listAccount({ pageNum: 1, pageSize: 50 }).then(res => {
    const map = {}
    ;(res.rows || []).forEach(acc => {
      map[acc.accountId] = acc.accountName
    })
    accountMap.value = map
  }).catch(() => {})
}

function getAccountName(accountId) {
  if (!accountId) return "-"
  return accountMap.value[accountId] || `账户#${accountId}`
}

function handleVoidPayment(row) {
  proxy.$modal.prompt(`确认冲红付款单 PM${String(row.paymentId || '').padStart(5, '0')}（金额：¥${row.amount}）？请输入冲红作废原因：`).then(({ value }) => {
    if (!value || !value.trim()) {
      proxy.$modal.msgWarning("请填写冲红原因")
      return Promise.reject(new Error("empty reason"))
    }
    return voidPayment(row.paymentId, value.trim())
  }).then(() => {
    proxy.$modal.msgSuccess("付款冲红成功，应付余额与结算账户已同步回退")
    getList()
  }).catch(() => {})
}

// 远程加载供应商下拉选项，按需补充供应商名称数据，减少页面初始加载压力。
function loadSupplierOptionList(searchKeyword) {
  supplierListLoading.value = true
  listSupplier({
    pageNum: 1,
    pageSize: 20,
    supplierName: normalizeRemoteKeyword(searchKeyword)
  }).then(response => {
    const supplierRowList = response.rows || []
    supplierRowList.forEach(supplierItem => {
      appendUniqueSelectOption(supplierList.value, supplierItem, "supplierId")
    })
  }).finally(() => {
    supplierListLoading.value = false
  })
}

// 同步供应商下拉选项，允许把历史编号回显更新成更适合用户理解的名称。
function syncSupplierOption(supplierId, supplierName) {
  if (supplierId === undefined || supplierId === null || supplierId === "" || !supplierName) {
    return
  }
  const supplierIndex = supplierList.value.findIndex(supplierItem => supplierItem.supplierId === supplierId)
  if (supplierIndex >= 0) {
    supplierList.value[supplierIndex] = {
      ...supplierList.value[supplierIndex],
      supplierId: supplierId,
      supplierName: supplierName
    }
    return
  }
  supplierList.value.push({
    supplierId: supplierId,
    supplierName: supplierName
  })
}

// 同步来源采购单显示名称，保证付款流水优先展示采购单号而不是内部编号。
function syncPurchaseOrderDisplayName(purchaseOrderId, orderNo) {
  if (purchaseOrderId === undefined || purchaseOrderId === null || purchaseOrderId === "") {
    return
  }
  purchaseOrderDisplayNameMap.value[purchaseOrderId] = orderNo || "采购单资料缺失"
}

// 判断当前供应商名称是否已经是可直接展示给用户的正式名称。
function isDirectSupplierNameResolved(supplierName) {
  return !!supplierName && supplierName !== "供应商资料缺失" && !supplierName.startsWith("历史供应商ID：")
}

// 按采购订单编号补充当前页缺失的采购单号展示，减少付款流水出现内部编号字段。
function ensurePurchaseOrderDisplayLoaded(purchaseOrderId) {
  if (purchaseOrderId === undefined || purchaseOrderId === null || purchaseOrderId === "") {
    return Promise.resolve()
  }
  if (purchaseOrderDisplayNameMap.value[purchaseOrderId]) {
    return Promise.resolve()
  }
  return getPurchaseOrder(purchaseOrderId).then(response => {
    syncPurchaseOrderDisplayName(purchaseOrderId, response.data && response.data.orderNo)
  }).catch(() => {
    syncPurchaseOrderDisplayName(purchaseOrderId)
  })
}

// 按采购订单来源回补历史付款记录的供应商名称，减少页面出现裸供应商编号。
function ensureSupplierOptionLoadedByPurchaseOrder(supplierId, purchaseOrderId) {
  if (!purchaseOrderId) {
    syncSupplierOption(supplierId, "供应商资料缺失")
    return Promise.resolve()
  }
  const cachedSupplierName = purchaseOrderSupplierNameMap.value[purchaseOrderId]
  if (cachedSupplierName) {
    syncSupplierOption(supplierId, cachedSupplierName)
    return Promise.resolve()
  }
  return getPurchaseOrder(purchaseOrderId).then(response => {
    syncPurchaseOrderDisplayName(purchaseOrderId, response.data && response.data.orderNo)
    const resolvedSupplierId = response.data && response.data.supplierId
    if (resolvedSupplierId === undefined || resolvedSupplierId === null || resolvedSupplierId === "") {
      syncSupplierOption(supplierId, "供应商资料缺失")
      return
    }
    return getSupplier(resolvedSupplierId).then(supplierResponse => {
      const resolvedSupplierName = supplierResponse.data && supplierResponse.data.supplierName ? supplierResponse.data.supplierName : `供应商${resolvedSupplierId}`
      purchaseOrderSupplierNameMap.value[purchaseOrderId] = resolvedSupplierName
      syncSupplierOption(supplierId, resolvedSupplierName)
    }).catch(() => {
      syncSupplierOption(supplierId, "供应商资料缺失")
    })
  }).catch(() => {
    syncSupplierOption(supplierId, "供应商资料缺失")
  })
}

// 按供应商编号补充当前页面缺失的供应商名称，必要时按来源采购订单回补历史记录名称。
function ensureSupplierOptionLoaded(supplierId, purchaseOrderId) {
  if (supplierId === undefined || supplierId === null || supplierId === "")
  {
    return Promise.resolve()
  }
  const existingSupplier = supplierList.value.find(supplierItem => supplierItem.supplierId === supplierId)
  if (existingSupplier && isDirectSupplierNameResolved(existingSupplier.supplierName))
  {
    return Promise.resolve()
  }
  return getSupplier(supplierId).then(response => {
    syncSupplierOption(response.data.supplierId, response.data.supplierName)
  }).catch(() => {
    return ensureSupplierOptionLoadedByPurchaseOrder(supplierId, purchaseOrderId)
  })
}

// 批量补充付款流水列表依赖的供应商名称，优先复用后端关联字段，消除冗余 N+1 查询。
function ensurePaymentReferenceOptionsLoaded(paymentRowList) {
  const missingSupplierRows = []
  const missingPoIds = []
  ;(paymentRowList || []).forEach(paymentRow => {
    syncPurchaseOrderDisplayName(paymentRow.purchaseOrderId, paymentRow.purchaseOrderNo)
    if (paymentRow.purchaseOrderId != null && paymentRow.purchaseOrderId !== "" && !paymentRow.purchaseOrderNo) {
      missingPoIds.push(paymentRow.purchaseOrderId)
    }
    if (paymentRow.supplierId != null && paymentRow.supplierName) {
      appendUniqueSelectOption(supplierList.value, { supplierId: paymentRow.supplierId, supplierName: paymentRow.supplierName }, "supplierId")
    } else {
      missingSupplierRows.push(paymentRow)
    }
  })
  Promise.all([
    ...missingSupplierRows.map(paymentRow => ensureSupplierOptionLoaded(paymentRow.supplierId, paymentRow.purchaseOrderId)),
    ...[...new Set(missingPoIds)].map(purchaseOrderId => ensurePurchaseOrderDisplayLoaded(purchaseOrderId))
  ]).catch(() => {})
}

// 组合供应商下拉选项，兼容历史付款记录中主数据缺失时的兜底回显。
function buildSupplierSelectOptionList() {
  return buildSelectOptionList(supplierList.value, [queryParams.value.supplierId], "supplierId", "supplierName", () => "供应商资料缺失")
}

// 根据供应商编号和来源采购订单返回供应商名称，优先展示回补后的正式供应商名称。
function getSupplierName(supplierId, purchaseOrderId) {
  const supplier = supplierList.value.find(supplierItem => supplierItem.supplierId === supplierId)
  if (supplier && supplier.supplierName) {
    return supplier.supplierName
  }
  const purchaseOrderSupplierName = purchaseOrderSupplierNameMap.value[purchaseOrderId]
  if (purchaseOrderSupplierName) {
    return purchaseOrderSupplierName
  }
  return supplierId ? "供应商资料缺失" : ""
}

// 根据采购订单编号返回采购单号，优先展示已回补的来源单号。
function getPurchaseOrderDisplayName(purchaseOrderId) {
  if (purchaseOrderId === undefined || purchaseOrderId === null || purchaseOrderId === "") {
    return "未关联采购单"
  }
  return purchaseOrderDisplayNameMap.value[purchaseOrderId] || "采购单资料缺失"
}

// 返回付款方式中文名称，兼容历史记录中的中文值和代码值展示。
function getPaymentMethodLabel(paymentMethodValue) {
  const paymentMethodOption = paymentMethodOptionList.find(optionItem => optionItem.value === paymentMethodValue)
  return paymentMethodOption ? paymentMethodOption.label : (paymentMethodValue || "")
}

// 查询付款流水列表，并补充当前页需要展示的供应商名称。
function getList() {
  if (queryParams.value.purchaseOrderNo)
  {
    queryParams.value.purchaseOrderId = undefined
  }
  const normalizedPayableId = queryParams.value.payableId
    ? String(queryParams.value.payableId).trim().replace(/^AP-?/i, "").replace(/^0+(?=\d)/, "")
    : undefined
  loading.value = true
  listPayment({
    ...queryParams.value,
    payableId: normalizedPayableId || undefined,
    beginPaymentTime: paymentTimeRange.value && paymentTimeRange.value.length > 0 ? paymentTimeRange.value[0] : undefined,
    endPaymentTime: paymentTimeRange.value && paymentTimeRange.value.length > 1 ? paymentTimeRange.value[1] : undefined
  }).then(response => {
    paymentList.value = response.rows
    total.value = response.total
    ensurePaymentReferenceOptionsLoaded(response.rows)
  }).finally(() => {
    loading.value = false
  })
}

// 搜索付款流水时回到第一页，避免沿用历史分页导致用户误以为无数据。
function handleQuery() {
  queryParams.value.pageNum = 1
  getList()
}

// 重置筛选条件并清空时间范围，恢复付款流水默认查询口径。
function resetQuery() {
  proxy.resetForm("queryRef")
  queryParams.value.payableId = undefined
  queryParams.value.purchaseOrderId = undefined
  queryParams.value.purchaseOrderNo = undefined
  paymentTimeRange.value = []
  handleQuery()
}

function handleViewPurchaseOrder(paymentRow) {
  if (!paymentRow?.purchaseOrderId) return
  router.push({
    path: "/purchase/purchaseOrder",
    query: { purchaseOrderId: String(paymentRow.purchaseOrderId) }
  })
}

function handlePrintPayment(row) {
  const isRefund = Number(row.amount || 0) < 0
  const docNo = "PM" + String(row.paymentId || "").padStart(5, "0")
  printDialogRef.value?.openPrint({
    docType: "payment",
    title: isRefund ? "供应商红字退款入账凭证" : "采购付款记账凭证",
    docNo,
    defaultPaperMode: "triplicate",
    metaFields: [
      { label: "凭证编号", value: docNo },
      { label: "往来供应商", value: row.supplierName || getSupplierName(row.supplierId, row.purchaseOrderId) },
      { label: "关联采购单", value: row.purchaseOrderNo || getPurchaseOrderDisplayName(row.purchaseOrderId) },
      { label: "关联应付单", value: row.payableId ? `AP${String(row.payableId).padStart(5, "0")}` : "-" },
      { label: "结算方式", value: getPaymentMethodLabel(row.paymentMethod) },
      { label: "结算账户", value: row.accountName || getAccountName(row.accountId) },
      { label: "记账时间", value: parseTime(row.paymentTime, "{y}-{m}-{d} {h}:{i}:{s}") },
      { label: "凭证状态", value: row.status === "voided" ? `已冲红（${row.voidReason || "-"}）` : "正常有效" }
    ],
    columns: [
      { label: "款项摘要", prop: "summary" },
      { label: "关联单号", prop: "orderNo", width: "160px" },
      { label: "结算账户", prop: "accountName", width: "170px" },
      { label: "金额(元)", prop: "amount", width: "130px", align: "right", type: "money" }
    ],
    items: [
      {
        summary: isRefund ? "采购退货/短装收回供应商退款" : "采购货款支付核销",
        orderNo: row.purchaseOrderNo || "-",
        accountName: row.accountName || getAccountName(row.accountId),
        amount: Number(row.amount || 0)
      }
    ],
    totalQty: 1,
    totalAmount: Number(row.amount || 0),
    remark: row.remark || ""
  })
}

// 从付款流水直接回到应付台账，方便财务继续查看剩余未付金额和付款状态。
function handleViewPayable(paymentRow) {
  router.push({
    path: "/finance/payable",
    query: {
      purchaseOrderId: paymentRow.purchaseOrderId,
      purchaseOrderNo: paymentRow.purchaseOrderNo,
      supplierId: paymentRow.supplierId
    }
  })
}

// 导出付款流水时复用当前筛选条件，保证导出的数据与页面所见一致。
function handleExport() {
  proxy.download("business/payment/export", {
    ...queryParams.value,
    beginPaymentTime: paymentTimeRange.value && paymentTimeRange.value.length > 0 ? paymentTimeRange.value[0] : undefined,
    endPaymentTime: paymentTimeRange.value && paymentTimeRange.value.length > 1 ? paymentTimeRange.value[1] : undefined
  }, `payment_${new Date().getTime()}.xlsx`)
}

// 初始化页面筛选条件和供应商下拉，保证从应付台账重复跳转时付款流水能按新条件刷新。
async function initializePage() {
  initializeQueryParamsFromRoute()
  await Promise.all([
    Promise.resolve(initBasicData()),
    Promise.resolve(getList())
  ])
}

// 监听同一路由下的查询参数变化，避免付款流水列表停留在旧的采购单或供应商筛选。
watch(() => route.fullPath, (currentRouteFullPath, previousRouteFullPath) => {
  if (currentRouteFullPath === previousRouteFullPath) {
    return
  }
  initializePage()
})

initializePage()
</script>
