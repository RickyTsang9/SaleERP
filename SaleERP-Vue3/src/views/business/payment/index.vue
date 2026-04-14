<template>
  <div class="app-container">
    <el-form :model="queryParams" ref="queryRef" :inline="true" v-show="showSearch" label-width="90px">
      <el-form-item label="应付账款ID" prop="payableId">
        <el-input v-model="queryParams.payableId" placeholder="请输入应付账款ID" clearable @keyup.enter="handleQuery" />
      </el-form-item>
      <el-form-item label="采购单号" prop="purchaseOrderNo">
        <el-input v-model="queryParams.purchaseOrderNo" placeholder="请输入采购单号" clearable @keyup.enter="handleQuery" />
      </el-form-item>
      <el-form-item label="供应商" prop="supplierId">
        <el-select v-model="queryParams.supplierId" placeholder="请选择供应商" clearable filterable remote reserve-keyword remote-show-suffix :remote-method="loadSupplierOptionList" :loading="supplierListLoading">
          <el-option
            v-for="supplier in supplierSelectOptionList"
            :key="supplier.supplierId"
            :label="supplier.supplierName"
            :value="supplier.supplierId"
          />
        </el-select>
      </el-form-item>
      <el-form-item label="付款方式" prop="paymentMethod">
        <el-select v-model="queryParams.paymentMethod" placeholder="请选择付款方式" clearable>
          <el-option
            v-for="paymentMethodOption in paymentMethodOptionList"
            :key="paymentMethodOption.value"
            :label="paymentMethodOption.label"
            :value="paymentMethodOption.value"
          />
        </el-select>
      </el-form-item>
      <el-form-item label="付款时间" style="width: 360px">
        <el-date-picker
          v-model="paymentTimeRange"
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
      title="付款流水请从应付台账发起登记，当前页面仅用于查询和导出，不支持手工修改或删除。"
      type="info"
      :closable="false"
      class="mb8"
    />

    <el-row :gutter="10" class="mb8">
      <el-col :span="1.5">
        <el-button type="warning" plain icon="Download" @click="handleExport" v-hasPermi="['business:payment:export']">导出</el-button>
      </el-col>
      <right-toolbar v-model:showSearch="showSearch" @queryTable="getList"></right-toolbar>
    </el-row>

    <el-table v-loading="loading" :data="paymentList">
      <el-table-column label="付款记录ID" align="center" prop="paymentId" />
      <el-table-column label="应付账款ID" align="center" prop="payableId" />
      <el-table-column label="采购单号" align="center" prop="purchaseOrderId" min-width="180">
        <template #default="scope">
          <span>{{ getPurchaseOrderDisplayName(scope.row.purchaseOrderId) }}</span>
        </template>
      </el-table-column>
      <el-table-column label="供应商" align="center" prop="supplierId" min-width="180">
        <template #default="scope">
          <span>{{ getSupplierName(scope.row.supplierId, scope.row.purchaseOrderId) }}</span>
        </template>
      </el-table-column>
      <el-table-column label="付款金额" align="center" prop="amount" />
      <el-table-column label="付款时间" align="center" prop="paymentTime" width="180">
        <template #default="scope">
          <span>{{ parseTime(scope.row.paymentTime, '{y}-{m}-{d} {h}:{i}:{s}') }}</span>
        </template>
      </el-table-column>
      <el-table-column label="付款方式" align="center" prop="paymentMethod">
        <template #default="scope">
          <span>{{ getPaymentMethodLabel(scope.row.paymentMethod) }}</span>
        </template>
      </el-table-column>
      <el-table-column label="备注" align="center" prop="remark" min-width="180" show-overflow-tooltip />
      <el-table-column label="操作" width="120" align="center" class-name="small-padding fixed-width">
        <template #default="scope">
          <el-button link type="primary" icon="View" @click="handleViewPayable(scope.row)" v-hasPermi="['business:payable:list']">查看应付</el-button>
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

<script setup name="BusinessPayment">
import { listPayment } from "@/api/business/payment"
import { listSupplier, getSupplier } from "@/api/business/supplier"
import { getPurchaseOrder } from "@/api/business/purchaseOrder"
import { appendUniqueSelectOption, buildSelectOptionList, normalizeRemoteKeyword } from "@/utils/remoteSelect"
import { parseTime } from "@/utils/ruoyi"

const { proxy } = getCurrentInstance()
const route = useRoute()
const router = useRouter()

const paymentList = ref([])
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
  { label: "银行转账", value: "银行转账" },
  { label: "微信", value: "微信" },
  { label: "支付宝", value: "支付宝" },
  { label: "现金", value: "现金" },
  { label: "银行转账", value: "bank_transfer" },
  { label: "微信", value: "wechat" },
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

// 批量补充付款流水列表依赖的供应商名称，保证远程下拉模式下列表展示仍然可读。
function ensurePaymentReferenceOptionsLoaded(paymentRowList) {
  const purchaseOrderIdList = [...new Set((paymentRowList || []).map(paymentRow => paymentRow.purchaseOrderId).filter(purchaseOrderId => purchaseOrderId !== undefined && purchaseOrderId !== null && purchaseOrderId !== ""))]
  (paymentRowList || []).forEach(paymentRow => {
    syncPurchaseOrderDisplayName(paymentRow.purchaseOrderId, paymentRow.purchaseOrderNo)
  })
  Promise.all([
    ...(paymentRowList || []).map(paymentRow => ensureSupplierOptionLoaded(paymentRow.supplierId, paymentRow.purchaseOrderId)),
    ...purchaseOrderIdList.map(purchaseOrderId => ensurePurchaseOrderDisplayLoaded(purchaseOrderId))
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
  loading.value = true
  listPayment({
    ...queryParams.value,
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
  queryParams.value.purchaseOrderId = undefined
  queryParams.value.purchaseOrderNo = undefined
  paymentTimeRange.value = []
  handleQuery()
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
  await initBasicData()
  await getList()
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
