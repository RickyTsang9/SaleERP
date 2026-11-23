<template>
  <div class="app-container erp-page">
    <erp-page-header
      eyebrow="财务核算 · 资金流水"
      title="客户回款记录"
      subtitle="完整留痕每一笔销售回款与结算账户入账，支持差错冲红作废并自动回退应收余额与账户资金。"
      icon="CreditCard"
      tone="emerald"
      :badge="`共 ${total} 笔回款`"
    >
      <template #actions>
        <el-button type="primary" icon="Download" @click="handleExport" v-hasPermi="['business:receipt:export']">导出回款</el-button>
      </template>
    </erp-page-header>

    <erp-stat-strip :items="receiptStatItems" :columns="3" />

    <section class="erp-filter-card" v-show="showSearch">
      <el-form :model="queryParams" ref="queryRef" :inline="true">
        <el-form-item label="销售单号" prop="saleOrderNo">
          <el-input v-model="queryParams.saleOrderNo" placeholder="请输入销售单号" clearable style="width: 175px" @keyup.enter="handleQuery" />
        </el-form-item>
        <el-form-item label="客户" prop="customerId">
          <el-select v-model="queryParams.customerId" placeholder="请选择客户" clearable filterable remote reserve-keyword remote-show-suffix :remote-method="loadCustomerOptionList" :loading="customerListLoading" style="width: 195px">
            <el-option v-for="customer in customerSelectOptionList" :key="customer.customerId" :label="customer.customerName" :value="customer.customerId" />
          </el-select>
        </el-form-item>
        <el-form-item label="回款方式" prop="paymentMethod">
          <el-select v-model="queryParams.paymentMethod" placeholder="全部方式" clearable style="width: 145px">
            <el-option v-for="paymentMethodOption in receiptPaymentMethodOptionList" :key="paymentMethodOption.value" :label="paymentMethodOption.label" :value="paymentMethodOption.value" />
          </el-select>
        </el-form-item>
        <el-form-item label="回款时间">
          <el-date-picker
            v-model="receiptTimeRange"
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
          <span class="erp-cell-sub">若录入金额或账户有误，可执行【冲红】自动回退应收已收金额与结算账户余额</span>
        </div>
        <div class="erp-toolbar-right">
          <right-toolbar v-model:showSearch="showSearch" @queryTable="getList" />
        </div>
      </div>

      <el-table v-loading="loading" :data="receiptList">
        <el-table-column label="回款 / 应收单" align="left" min-width="126">
          <template #default="scope">
            <span class="erp-code-chip">{{ 'RC' + String(scope.row.receiptId || '').padStart(5, '0') }}</span>
            <div class="erp-cell-sub">{{ scope.row.receivableId ? ('AR' + String(scope.row.receivableId).padStart(5, '0')) : '-' }}</div>
          </template>
        </el-table-column>
        <el-table-column label="客户 / 来源销售单" align="left" min-width="165" show-overflow-tooltip>
          <template #default="scope">
            <div class="erp-cell-title">{{ scope.row.customerName || getCustomerName(scope.row.customerId, scope.row.saleOrderId) }}</div>
            <div class="erp-cell-sub">{{ scope.row.saleOrderNo || (scope.row.saleOrderId ? ('SO-' + scope.row.saleOrderId) : '-') }}</div>
          </template>
        </el-table-column>
        <el-table-column label="回款金额" align="right" prop="amount" width="108">
          <template #default="scope">
            <span class="erp-amount" :class="scope.row.status === 'voided' ? 'is-danger' : 'is-positive'">
              ¥{{ Number(scope.row.amount || 0).toFixed(2) }}
            </span>
          </template>
        </el-table-column>
        <el-table-column label="方式 / 结算账户" align="left" min-width="148" show-overflow-tooltip>
          <template #default="scope">
            <div class="erp-cell-title">{{ getReceiptPaymentMethodLabel(scope.row.paymentMethod) }}</div>
            <div class="erp-cell-sub">{{ scope.row.accountName || getAccountName(scope.row.accountId) }}</div>
          </template>
        </el-table-column>
        <el-table-column label="回款时间" align="center" prop="paymentTime" width="136">
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
              <el-button link type="primary" icon="View" @click="handleViewReceivable(scope.row)" v-hasPermi="['business:receivable:list']">应收</el-button>
              <el-button link type="info" icon="Printer" @click="handlePrintReceipt(scope.row)">凭证</el-button>
              <el-dropdown v-if="scope.row.saleOrderId || scope.row.status !== 'voided'" trigger="click">
                <el-button link type="primary">更多<el-icon class="el-icon--right"><ArrowDown /></el-icon></el-button>
                <template #dropdown>
                  <el-dropdown-menu>
                    <el-dropdown-item v-if="scope.row.saleOrderId" @click="handleViewSaleOrder(scope.row)">查看销售单</el-dropdown-item>
                    <el-dropdown-item v-if="scope.row.status !== 'voided'" divided @click="handleVoidReceipt(scope.row)"><span style="color: var(--el-color-danger)">冲红作废</span></el-dropdown-item>
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

<script setup name="BusinessReceipt">
import DocumentPrintDialog from "@/components/DocumentPrintDialog/index.vue"
import { listReceipt, voidReceipt } from "@/api/business/receipt"
import { listAccount } from "@/api/business/account"
import { listCustomer, getCustomer } from "@/api/business/customer"
import { getSaleOrder } from "@/api/business/saleOrder"
import { appendUniqueSelectOption, buildSelectOptionList, normalizeRemoteKeyword } from "@/utils/remoteSelect"
import { parseTime } from "@/utils/ruoyi"

const { proxy } = getCurrentInstance()
const route = useRoute()
const router = useRouter()

const receiptList = ref([])
const printDialogRef = ref(null)
const customerList = ref([])
const accountMap = ref({})
const customerListLoading = ref(false)
const loading = ref(true)
const showSearch = ref(true)
const total = ref(0)
const receiptTimeRange = ref([])
const saleOrderCustomerNameMap = ref({})
const customerSelectOptionList = computed(() => buildCustomerSelectOptionList())

const receiptPaymentMethodOptionList = [
  { label: "银行转账", value: "bank_transfer" },
  { label: "微信支付", value: "wechat" },
  { label: "支付宝", value: "alipay" },
  { label: "现金", value: "cash" }
]

const data = reactive({
  queryParams: {
    pageNum: 1,
    pageSize: 10,
    receivableId: undefined,
    saleOrderId: undefined,
    saleOrderNo: undefined,
    customerId: undefined,
    paymentMethod: undefined
  }
})

const { queryParams } = toRefs(data)

const receiptStatItems = computed(() => {
  const rows = receiptList.value || []
  const validAmt = rows.filter(r => r.status !== "voided").reduce((s, r) => s + Number(r.amount || 0), 0)
  const voidCount = rows.filter(r => r.status === "voided").length
  return [
    { label: "回款流水总数", value: total.value, suffix: "笔", hint: "匹配当前筛选的全部流水", icon: "CreditCard", tone: "indigo" },
    { label: "本页有效回款额", prefix: "¥", value: validAmt.toFixed(2), hint: "不含已冲红作废流水", icon: "CircleCheck", tone: "emerald" },
    { label: "本页冲红作废", value: voidCount, suffix: "笔", hint: "冲红后已自动回退台账与余额", icon: "RefreshLeft", tone: "rose" }
  ]
})

// 根据首页或其他业务页传入的参数初始化回款流水筛选条件，保证落地页即开即用。
function initializeQueryParamsFromRoute() {
  queryParams.value.receivableId = route.query.receivableId || undefined
  queryParams.value.saleOrderId = route.query.saleOrderId || undefined
  queryParams.value.customerId = route.query.customerId ? Number(route.query.customerId) : undefined
}

// 初始化客户远程下拉数据，避免页面初始阶段一次性加载过多基础资料。
function initBasicData() {
  loadCustomerOptionList()
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

function handlePrintReceipt(row) {
  const isRefund = Number(row.amount || 0) < 0
  const docNo = "RC" + String(row.receiptId || "").padStart(5, "0")
  printDialogRef.value?.openPrint({
    docType: "receipt",
    title: isRefund ? "销售红字退款凭证" : "销售收款记账凭证",
    docNo,
    defaultPaperMode: "triplicate",
    metaFields: [
      { label: "凭证编号", value: docNo },
      { label: "往来客户", value: row.customerName || getCustomerName(row.customerId, row.saleOrderId) },
      { label: "关联销售单", value: row.saleOrderNo || (row.saleOrderId ? `SO-${row.saleOrderId}` : "-") },
      { label: "关联应收单", value: row.receivableId ? `AR${String(row.receivableId).padStart(5, "0")}` : "-" },
      { label: "结算方式", value: getReceiptPaymentMethodLabel(row.paymentMethod) },
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
        summary: isRefund ? "销售退货/溢收客户退款" : "销售货款回款核销",
        orderNo: row.saleOrderNo || "-",
        accountName: row.accountName || getAccountName(row.accountId),
        amount: Number(row.amount || 0)
      }
    ],
    totalQty: 1,
    totalAmount: Number(row.amount || 0),
    remark: row.remark || ""
  })
}

function handleVoidReceipt(row) {
  proxy.$modal.prompt(`确认冲红回款单 RC${String(row.receiptId || '').padStart(5, '0')}（金额：¥${row.amount}）？请输入冲红作废原因：`).then(({ value }) => {
    if (!value || !value.trim()) {
      proxy.$modal.msgWarning("请填写冲红原因")
      return Promise.reject(new Error("empty reason"))
    }
    return voidReceipt(row.receiptId, value.trim())
  }).then(() => {
    proxy.$modal.msgSuccess("回款冲红成功，应收余额与结算账户已同步回退")
    getList()
  }).catch(() => {})
}

// 远程加载客户下拉选项，按需补充客户名称数据，减少页面初始加载压力。
function loadCustomerOptionList(searchKeyword) {
  customerListLoading.value = true
  listCustomer({
    pageNum: 1,
    pageSize: 20,
    customerName: normalizeRemoteKeyword(searchKeyword)
  }).then(response => {
    const customerRowList = response.rows || []
    customerRowList.forEach(customerItem => {
      appendUniqueSelectOption(customerList.value, customerItem, "customerId")
    })
  }).finally(() => {
    customerListLoading.value = false
  })
}

// 同步客户下拉选项，允许把历史编号回显更新成更适合用户理解的名称。
function syncCustomerOption(customerId, customerName) {
  if (customerId === undefined || customerId === null || customerId === "" || !customerName) {
    return
  }
  const customerIndex = customerList.value.findIndex(customerItem => customerItem.customerId === customerId)
  if (customerIndex >= 0) {
    customerList.value[customerIndex] = {
      ...customerList.value[customerIndex],
      customerId: customerId,
      customerName: customerName
    }
    return
  }
  customerList.value.push({
    customerId: customerId,
    customerName: customerName
  })
}

// 判断当前客户名称是否已经是可直接展示给用户的正式名称。
function isDirectCustomerNameResolved(customerName) {
  return !!customerName && customerName !== "客户资料缺失" && !customerName.startsWith("历史客户ID：") && !customerName.endsWith("（历史单据）")
}

// 兼容已缓存的历史映射后缀，保证页面展示和筛选回显都保持简洁可读。
function normalizeCustomerDisplayName(customerName) {
  return customerName ? customerName.replace("（历史单据）", "") : ""
}

// 按销售单来源回补历史回款记录的客户名称，减少页面出现裸客户编号。
function ensureCustomerOptionLoadedBySaleOrder(customerId, saleOrderId) {
  if (!saleOrderId) {
    syncCustomerOption(customerId, "客户资料缺失")
    return Promise.resolve()
  }
  const cachedCustomerName = saleOrderCustomerNameMap.value[saleOrderId]
  if (cachedCustomerName) {
    syncCustomerOption(customerId, cachedCustomerName)
    return Promise.resolve()
  }
  return getSaleOrder(saleOrderId).then(response => {
    const resolvedCustomerId = response.data?.customerId
    if (resolvedCustomerId === undefined || resolvedCustomerId === null || resolvedCustomerId === "") {
      syncCustomerOption(customerId, "客户资料缺失")
      return
    }
    return getCustomer(resolvedCustomerId).then(customerResponse => {
      const resolvedCustomerName = customerResponse.data?.customerName || `客户${resolvedCustomerId}`
      saleOrderCustomerNameMap.value[saleOrderId] = resolvedCustomerName
      syncCustomerOption(customerId, resolvedCustomerName)
    }).catch(() => {
      syncCustomerOption(customerId, "客户资料缺失")
    })
  }).catch(() => {
    syncCustomerOption(customerId, "客户资料缺失")
  })
}

// 按客户编号补充当前页面缺失的客户名称，必要时按来源销售单回补历史记录名称。
function ensureCustomerOptionLoaded(customerId, saleOrderId) {
  if (customerId === undefined || customerId === null || customerId === "")
  {
    return Promise.resolve()
  }
  const existingCustomer = customerList.value.find(customerItem => customerItem.customerId === customerId)
  if (existingCustomer && isDirectCustomerNameResolved(existingCustomer.customerName))
  {
    return Promise.resolve()
  }
  return getCustomer(customerId).then(response => {
    syncCustomerOption(response.data.customerId, response.data.customerName)
  }).catch(() => {
    return ensureCustomerOptionLoadedBySaleOrder(customerId, saleOrderId)
  })
}

// 批量补充回款流水列表依赖的客户名称，优先复用后端关联字段，消除冗余 N+1 查询。
function ensureReceiptReferenceOptionsLoaded(receiptRowList) {
  const missingRows = []
  ;(receiptRowList || []).forEach(receiptRow => {
    if (receiptRow.customerId != null && receiptRow.customerName) {
      appendUniqueSelectOption(customerList.value, {
        customerId: receiptRow.customerId,
        customerName: normalizeCustomerDisplayName(receiptRow.customerName)
      }, "customerId")
    } else {
      missingRows.push(receiptRow)
    }
  })
  Promise.all(missingRows.map(receiptRow => ensureCustomerOptionLoaded(receiptRow.customerId, receiptRow.saleOrderId))).catch(() => {})
}

// 组合客户下拉选项，兼容历史回款记录中主数据缺失时的兜底回显。
function buildCustomerSelectOptionList() {
  return buildSelectOptionList(customerList.value, [queryParams.value.customerId], "customerId", "customerName", () => "客户资料缺失")
}

// 根据客户编号和来源销售单返回客户名称，优先展示回补后的正式客户名称。
function getCustomerName(customerId, saleOrderId) {
  const customer = customerList.value.find(customerItem => customerItem.customerId === customerId)
  if (customer && customer.customerName) {
    return normalizeCustomerDisplayName(customer.customerName)
  }
  const saleOrderCustomerName = saleOrderCustomerNameMap.value[saleOrderId]
  if (saleOrderCustomerName) {
    return saleOrderCustomerName
  }
  return customerId ? "客户资料缺失" : ""
}

// 返回回款方式中文名称，兼容历史记录中的中文值和代码值展示。
function getReceiptPaymentMethodLabel(paymentMethodValue) {
  const paymentMethodOption = receiptPaymentMethodOptionList.find(optionItem => optionItem.value === paymentMethodValue)
  return paymentMethodOption ? paymentMethodOption.label : (paymentMethodValue || "")
}

// 查询回款流水列表，并补充当前页需要展示的客户名称。
function getList() {
  loading.value = true
  const requestParams = {
    ...queryParams.value,
    beginPaymentTime: receiptTimeRange.value && receiptTimeRange.value.length > 0 ? receiptTimeRange.value[0] : undefined,
    endPaymentTime: receiptTimeRange.value && receiptTimeRange.value.length > 1 ? receiptTimeRange.value[1] : undefined
  }
  listReceipt(requestParams).then(response => {
    receiptList.value = response.rows
    total.value = response.total
    ensureReceiptReferenceOptionsLoaded(response.rows)
  }).finally(() => {
    loading.value = false
  })
}

// 搜索回款流水时回到第一页，避免沿用历史分页导致用户误以为无数据。
function handleQuery() {
  queryParams.value.pageNum = 1
  getList()
}

// 重置筛选条件并清空时间范围，恢复回款流水默认查询口径。
function resetQuery() {
  proxy.resetForm("queryRef")
  queryParams.value.receivableId = undefined
  queryParams.value.saleOrderId = undefined
  queryParams.value.saleOrderNo = undefined
  receiptTimeRange.value = []
  handleQuery()
}

// 从回款流水直接回到应收台账，方便财务继续查看剩余未收金额和到期信息。
function handleViewReceivable(receiptRow) {
  router.push({
    path: "/finance/receivable",
    query: {
      saleOrderId: receiptRow.saleOrderId,
      customerId: receiptRow.customerId
    }
  })
}

// 从回款流水直接回到销售单，方便业务和财务核对来源订单状态。
function handleViewSaleOrder(receiptRow) {
  if (!receiptRow.saleOrderId)
  {
    proxy.$modal.msgWarning("当前回款流水缺少来源销售单编号")
    return
  }
  router.push({
    path: "/sales/saleOrder",
    query: {
      saleOrderId: receiptRow.saleOrderId
    }
  })
}

// 导出回款流水时复用当前筛选条件，保证导出的数据与页面所见一致。
function handleExport() {
  proxy.download("business/receipt/export", {
    ...queryParams.value,
    beginPaymentTime: receiptTimeRange.value && receiptTimeRange.value.length > 0 ? receiptTimeRange.value[0] : undefined,
    endPaymentTime: receiptTimeRange.value && receiptTimeRange.value.length > 1 ? receiptTimeRange.value[1] : undefined
  }, `receipt_${new Date().getTime()}.xlsx`)
}

// 初始化页面筛选条件和客户下拉，保证从应收或销售单重复跳转时回款流水会立即刷新。
async function initializePage() {
  initializeQueryParamsFromRoute()
  await Promise.all([
    Promise.resolve(initBasicData()),
    Promise.resolve(getList())
  ])
}

// 监听同一路由下的查询参数变化，避免回款流水仍然停留在旧的单据或客户筛选条件。
watch(() => route.fullPath, (currentRouteFullPath, previousRouteFullPath) => {
  if (currentRouteFullPath === previousRouteFullPath) {
    return
  }
  initializePage()
})

initializePage()
</script>
