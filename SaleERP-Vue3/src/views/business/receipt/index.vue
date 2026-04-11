<template>
  <div class="app-container">
    <el-form :model="queryParams" ref="queryRef" :inline="true" v-show="showSearch">
      <el-form-item label="应收编号" prop="receivableId">
        <el-input v-model="queryParams.receivableId" placeholder="请输入应收编号" clearable style="width: 180px" @keyup.enter="handleQuery" />
      </el-form-item>
      <el-form-item label="销售单编号" prop="saleOrderId">
        <el-input v-model="queryParams.saleOrderId" placeholder="请输入销售单编号" clearable style="width: 180px" @keyup.enter="handleQuery" />
      </el-form-item>
      <el-form-item label="客户" prop="customerId">
        <el-select v-model="queryParams.customerId" placeholder="请选择客户" clearable filterable remote reserve-keyword remote-show-suffix :remote-method="loadCustomerOptionList" :loading="customerListLoading" style="width: 220px">
          <el-option
            v-for="customer in customerSelectOptionList"
            :key="customer.customerId"
            :label="customer.customerName"
            :value="customer.customerId"
          />
        </el-select>
      </el-form-item>
      <el-form-item label="回款方式" prop="paymentMethod">
        <el-select v-model="queryParams.paymentMethod" placeholder="请选择回款方式" clearable style="width: 180px">
          <el-option
            v-for="paymentMethodOption in receiptPaymentMethodOptionList"
            :key="paymentMethodOption.value"
            :label="paymentMethodOption.label"
            :value="paymentMethodOption.value"
          />
        </el-select>
      </el-form-item>
      <el-form-item label="回款时间" style="width: 360px">
        <el-date-picker
          v-model="receiptTimeRange"
          type="datetimerange"
          value-format="YYYY-MM-DD HH:mm:ss"
          range-separator="-"
          start-placeholder="开始时间"
          end-placeholder="结束时间"
        />
      </el-form-item>
      <el-form-item>
        <el-button type="primary" icon="Search" @click="handleQuery">搜索</el-button>
        <el-button icon="Refresh" @click="resetQuery">重置</el-button>
      </el-form-item>
    </el-form>

    <el-alert
      title="回款流水请从应收台账发起登记，当前页面仅用于查询和导出，不支持手工新增、修改或删除。"
      type="info"
      :closable="false"
      class="mb8"
    />

    <el-row :gutter="10" class="mb8">
      <el-col :span="1.5">
        <el-button type="warning" plain icon="Download" @click="handleExport" v-hasPermi="['business:receipt:export']">导出</el-button>
      </el-col>
      <right-toolbar v-model:showSearch="showSearch" @queryTable="getList"></right-toolbar>
    </el-row>

    <el-table v-loading="loading" :data="receiptList">
      <el-table-column label="回款编号" align="center" prop="receiptId" />
      <el-table-column label="应收编号" align="center" prop="receivableId" />
      <el-table-column label="销售单编号" align="center" prop="saleOrderId" />
      <el-table-column label="客户" align="center" prop="customerId" min-width="180">
        <template #default="scope">
          <span>{{ getCustomerName(scope.row.customerId, scope.row.saleOrderId) }}</span>
        </template>
      </el-table-column>
      <el-table-column label="回款金额" align="center" prop="amount" />
      <el-table-column label="回款时间" align="center" prop="paymentTime" width="180">
        <template #default="scope">
          <span>{{ parseTime(scope.row.paymentTime, '{y}-{m}-{d} {h}:{i}:{s}') }}</span>
        </template>
      </el-table-column>
      <el-table-column label="回款方式" align="center" prop="paymentMethod">
        <template #default="scope">
          <span>{{ getReceiptPaymentMethodLabel(scope.row.paymentMethod) }}</span>
        </template>
      </el-table-column>
      <el-table-column label="备注" align="center" prop="remark" min-width="180" show-overflow-tooltip />
      <el-table-column label="操作" width="220" align="center" class-name="small-padding fixed-width">
        <template #default="scope">
          <el-button link type="primary" icon="View" @click="handleViewReceivable(scope.row)" v-hasPermi="['business:receivable:list']">查看应收</el-button>
          <el-button link type="primary" icon="Tickets" @click="handleViewSaleOrder(scope.row)" v-hasPermi="['business:saleOrder:list']" v-if="scope.row.saleOrderId">查看销售单</el-button>
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

<script setup name="BusinessReceipt">
import { listReceipt } from "@/api/business/receipt"
import { listCustomer, getCustomer } from "@/api/business/customer"
import { getSaleOrder } from "@/api/business/saleOrder"
import { appendUniqueSelectOption, buildSelectOptionList, normalizeRemoteKeyword } from "@/utils/remoteSelect"

const { proxy } = getCurrentInstance()
const route = useRoute()
const router = useRouter()

const receiptList = ref([])
const customerList = ref([])
const customerListLoading = ref(false)
const loading = ref(true)
const showSearch = ref(true)
const total = ref(0)
const receiptTimeRange = ref([])
const saleOrderCustomerNameMap = ref({})
const customerSelectOptionList = computed(() => buildCustomerSelectOptionList())

const receiptPaymentMethodOptionList = [
  { label: "银行转账", value: "bank_transfer" },
  { label: "现金", value: "cash" },
  { label: "微信", value: "wechat" },
  { label: "支付宝", value: "alipay" },
  { label: "银行转账", value: "银行转账" },
  { label: "现金", value: "现金" },
  { label: "微信", value: "微信" },
  { label: "支付宝", value: "支付宝" }
]

const data = reactive({
  queryParams: {
    pageNum: 1,
    pageSize: 10,
    receivableId: undefined,
    saleOrderId: undefined,
    customerId: undefined,
    paymentMethod: undefined
  }
})

const { queryParams } = toRefs(data)

// 根据首页或其他业务页传入的参数初始化回款流水筛选条件，保证落地页即开即用。
function initializeQueryParamsFromRoute() {
  if (route.query.receivableId)
  {
    queryParams.value.receivableId = route.query.receivableId
  }
  if (route.query.saleOrderId)
  {
    queryParams.value.saleOrderId = route.query.saleOrderId
  }
  if (route.query.customerId)
  {
    queryParams.value.customerId = Number(route.query.customerId)
  }
}

// 初始化客户远程下拉数据，避免页面初始阶段一次性加载过多基础资料。
function initBasicData() {
  loadCustomerOptionList()
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
  return !!customerName && !customerName.startsWith("历史客户ID：") && !customerName.endsWith("（历史单据）")
}

// 兼容已缓存的历史映射后缀，保证页面展示和筛选回显都保持简洁可读。
function normalizeCustomerDisplayName(customerName) {
  return customerName ? customerName.replace("（历史单据）", "") : ""
}

// 按销售单来源回补历史回款记录的客户名称，减少页面出现裸客户编号。
function ensureCustomerOptionLoadedBySaleOrder(customerId, saleOrderId) {
  if (!saleOrderId) {
    syncCustomerOption(customerId, `历史客户ID：${customerId}`)
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
      syncCustomerOption(customerId, `历史客户ID：${customerId}`)
      return
    }
    return getCustomer(resolvedCustomerId).then(customerResponse => {
      const resolvedCustomerName = customerResponse.data?.customerName || `客户${resolvedCustomerId}`
      saleOrderCustomerNameMap.value[saleOrderId] = resolvedCustomerName
      syncCustomerOption(customerId, resolvedCustomerName)
    }).catch(() => {
      syncCustomerOption(customerId, `历史客户ID：${customerId}`)
    })
  }).catch(() => {
    syncCustomerOption(customerId, `历史客户ID：${customerId}`)
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

// 批量补充回款流水列表依赖的客户名称，保证远程下拉模式下列表展示仍然可读。
function ensureReceiptReferenceOptionsLoaded(receiptRowList) {
  Promise.all((receiptRowList || []).map(receiptRow => ensureCustomerOptionLoaded(receiptRow.customerId, receiptRow.saleOrderId))).catch(() => {})
}

// 组合客户下拉选项，兼容历史回款记录中仍在使用的旧客户编号回显。
function buildCustomerSelectOptionList() {
  return buildSelectOptionList(customerList.value, [queryParams.value.customerId], "customerId", "customerName", "历史客户ID：")
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
  return customerId ? `历史客户ID：${customerId}` : ""
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

initializeQueryParamsFromRoute()
initBasicData()
getList()
</script>
