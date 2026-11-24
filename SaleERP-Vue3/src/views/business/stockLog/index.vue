<template>
  <div class="app-container erp-page">
    <erp-page-header
      eyebrow="仓储实时监控 · 审计溯源"
      title="库存变动流水"
      subtitle="全量记录采购入库、销售出库、退货、盘点及调拨触发的每一笔批次数量与金额变动。"
      icon="Document"
      tone="indigo"
      :badge="`共 ${total} 条流水`"
    >
      <template #actions>
        <el-button type="primary" icon="Download" @click="handleExport" v-hasPermi="['business:stockLog:export']">导出流水</el-button>
      </template>
    </erp-page-header>

    <erp-stat-strip :items="stockLogStatItems" :columns="4" />

    <section class="erp-filter-card" v-show="showSearch">
      <el-form :model="queryParams" ref="queryRef" :inline="true">
        <el-form-item label="仓库" prop="warehouseId">
          <el-select v-model="queryParams.warehouseId" placeholder="请选择仓库" clearable filterable remote reserve-keyword remote-show-suffix :remote-method="loadWarehouseOptionList" :loading="warehouseListLoading" style="width: 185px">
            <el-option v-for="warehouse in warehouseSelectOptionList" :key="warehouse.warehouseId" :label="warehouse.warehouseName" :value="warehouse.warehouseId" />
          </el-select>
        </el-form-item>
        <el-form-item label="商品" prop="productId">
          <el-select v-model="queryParams.productId" placeholder="请选择商品" clearable filterable remote reserve-keyword remote-show-suffix :remote-method="loadProductOptionList" :loading="productListLoading" style="width: 185px">
            <el-option v-for="product in productSelectOptionList" :key="product.productId" :label="product.productName" :value="product.productId" />
          </el-select>
        </el-form-item>
        <el-form-item label="单据类型" prop="billType">
          <el-select v-model="queryParams.billType" placeholder="全部类型" clearable style="width: 150px">
            <el-option v-for="billTypeOption in billTypeOptionList" :key="billTypeOption.value" :label="billTypeOption.label" :value="billTypeOption.value" />
          </el-select>
        </el-form-item>
        <el-form-item label="单据号" prop="billNo">
          <el-input v-model="queryParams.billNo" placeholder="请输入单据号" clearable style="width: 180px" @keyup.enter="handleQuery" />
        </el-form-item>
        <el-form-item label="批次号" prop="batchNo">
          <el-input v-model="queryParams.batchNo" placeholder="请输入批次号" clearable style="width: 155px" @keyup.enter="handleQuery" />
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
          <erp-status-tabs :model-value="queryParams.inOut ?? ''" :items="inOutTabItems" @change="handleQuickInOut" />
        </div>
        <div class="erp-toolbar-right">
          <right-toolbar v-model:showSearch="showSearch" @queryTable="getList" />
        </div>
      </div>

      <el-table v-loading="loading" :data="stockLogList">
        <el-table-column label="来源单据" align="left" min-width="185" show-overflow-tooltip>
          <template #default="scope">
            <el-button
              v-if="canJumpSourceBill(scope.row)"
              link
              type="primary"
              style="padding: 0; height: auto"
              @click="handleJumpSourceBill(scope.row)"
            >
              <span class="erp-code-chip">{{ scope.row.billNo || ('#' + scope.row.billId) }}</span>
            </el-button>
            <span v-else class="erp-code-chip">{{ scope.row.billNo || ('#' + scope.row.stockLogId) }}</span>
            <div class="erp-cell-sub">{{ getBillTypeLabel(scope.row.billType) }}</div>
          </template>
        </el-table-column>
        <el-table-column label="商品 / 批次" align="left" min-width="185" show-overflow-tooltip>
          <template #default="scope">
            <div class="erp-cell-title">{{ scope.row.productName || getProductName(scope.row.productId) }}</div>
            <div class="erp-cell-sub">批次：{{ scope.row.batchNo || '-' }}</div>
          </template>
        </el-table-column>
        <el-table-column label="仓库 / 库位" align="left" min-width="165" show-overflow-tooltip>
          <template #default="scope">
            <div class="erp-cell-title">{{ scope.row.warehouseName || getWarehouseName(scope.row.warehouseId) }}</div>
            <div class="erp-cell-sub">{{ scope.row.locationName || getLocationName(scope.row.locationId) }}</div>
          </template>
        </el-table-column>
        <el-table-column label="方向" align="center" prop="inOut" width="88">
          <template #default="scope">
            <el-tag :type="getInOutTagType(scope.row.inOut)" effect="light">{{ getInOutLabel(scope.row.inOut) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="变动数量" align="right" prop="quantity" width="96">
          <template #default="scope">
            <span class="erp-amount" :class="scope.row.inOut === 'in' ? 'is-positive' : 'is-danger'">
              {{ scope.row.inOut === 'in' ? '+' : '-' }}{{ scope.row.quantity }}
            </span>
          </template>
        </el-table-column>
        <el-table-column label="结存变化" align="center" width="125">
          <template #default="scope">
            <span class="erp-num">{{ scope.row.beforeQty }} → <strong>{{ scope.row.afterQty }}</strong></span>
          </template>
        </el-table-column>
        <el-table-column label="单价 / 金额" align="right" width="125">
          <template #default="scope">
            <template v-if="scope.row.amount === null || scope.row.amount === undefined">
              <div class="erp-cell-sub">权限脱敏</div>
            </template>
            <template v-else>
              <div class="erp-amount">¥{{ Number(scope.row.amount || 0).toFixed(2) }}</div>
              <div class="erp-cell-sub">单价 ¥{{ Number(scope.row.price || 0).toFixed(2) }}</div>
            </template>
          </template>
        </el-table-column>
        <el-table-column label="发生时间" align="center" prop="createTime" width="165">
          <template #default="scope">
            <span>{{ parseTime(scope.row.createTime) }}</span>
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
  </div>
</template>

<script setup name="StockLog">
import { listStockLog } from "@/api/business/stockLog"
import { listWarehouse, getWarehouse } from "@/api/business/warehouse"
import { listProduct, getProduct } from "@/api/business/product"
import { listLocation, getLocation } from "@/api/business/location"
import { appendUniqueSelectOption, buildSelectOptionList, normalizeRemoteKeyword } from "@/utils/remoteSelect"
import { parseTime } from "@/utils/ruoyi"

const { proxy } = getCurrentInstance()
const route = useRoute()
const router = useRouter()

const stockLogList = ref([])
const warehouseList = ref([])
const warehouseListLoading = ref(false)
const productList = ref([])
const productListLoading = ref(false)
const locationList = ref([])
const locationListLoading = ref(false)
const loading = ref(true)
const showSearch = ref(true)
const total = ref(0)
const warehouseSelectOptionList = computed(() => buildWarehouseSelectOptionList())
const productSelectOptionList = computed(() => buildProductSelectOptionList())
const locationSelectOptionList = computed(() => buildLocationSelectOptionList())

const billTypeOptionList = [
  { label: "采购入库", value: "inbound" },
  { label: "销售出库", value: "outbound" },
  { label: "采购退货", value: "purchase_return" },
  { label: "销售订单出库", value: "sale_order" },
  { label: "销售订单回退", value: "sale_order_rollback" },
  { label: "销售退货", value: "sale_return" },
  { label: "库存盘点", value: "inventory_check" },
  { label: "库存调拨", value: "transfer" },
  { label: "库存冻结", value: "stock_freeze" },
  { label: "库存解冻", value: "stock_unfreeze" }
]

const inOutOptionList = [
  { label: "入库", value: "in" },
  { label: "出库", value: "out" }
]

const data = reactive({
  queryParams: {
    pageNum: 1,
    pageSize: 10,
    warehouseId: undefined,
    productId: undefined,
    locationId: undefined,
    batchNo: undefined,
    billType: undefined,
    billNo: undefined,
    inOut: undefined
  }
})

const { queryParams } = toRefs(data)

const inOutTabItems = [
  { label: "全部流水", value: "" },
  { label: "入库增加", value: "in", dot: "emerald" },
  { label: "出库扣减", value: "out", dot: "rose" }
]

const stockLogStatItems = computed(() => {
  const rows = stockLogList.value || []
  const inQty = rows.filter(r => r.inOut === "in").reduce((s, r) => s + Number(r.quantity || 0), 0)
  const outQty = rows.filter(r => r.inOut === "out").reduce((s, r) => s + Number(r.quantity || 0), 0)
  const totalAmt = rows.reduce((s, r) => s + Number(r.amount || 0), 0)
  return [
    { label: "变动流水总数", value: total.value, suffix: "笔", hint: "完整留痕可审计追溯", icon: "Document", tone: "indigo" },
    { label: "本页入库总量", value: inQty, suffix: "件", hint: "入库/退货/盘盈类增加", icon: "TopRight", tone: "emerald" },
    { label: "本页出库总量", value: outQty, suffix: "件", hint: "出库/退供/盘亏类扣减", icon: "BottomRight", tone: "rose" },
    { label: "本页涉及货值", prefix: "¥", value: totalAmt.toFixed(2), hint: "本页变动金额绝对值合计", icon: "Money", tone: "blue" }
  ]
})

function handleQuickInOut(val) {
  queryParams.value.inOut = val === "" ? undefined : val
  handleQuery()
}

// 根据其他业务页传入的参数初始化库存流水筛选条件，保证落地页即开即用。
function initializeQueryParamsFromRoute() {
  queryParams.value.productId = route.query.productId ? Number(route.query.productId) : undefined
  queryParams.value.warehouseId = route.query.warehouseId ? Number(route.query.warehouseId) : undefined
  queryParams.value.locationId = route.query.locationId ? Number(route.query.locationId) : undefined
  queryParams.value.batchNo = route.query.batchNo || undefined
}

// 初始化远程下拉基础数据，避免页面初始阶段一次性加载过多基础资料。
function initBasicData() {
  loadWarehouseOptionList()
  loadProductOptionList()
  loadLocationOptionList()
}

// 远程加载仓库下拉选项，按需补充仓库名称数据，减少页面初始加载压力。
function loadWarehouseOptionList(searchKeyword) {
  warehouseListLoading.value = true
  listWarehouse({
    pageNum: 1,
    pageSize: 20,
    warehouseName: normalizeRemoteKeyword(searchKeyword)
  }).then(response => {
    const warehouseRowList = response.rows || []
    warehouseRowList.forEach(warehouseItem => {
      appendUniqueSelectOption(warehouseList.value, warehouseItem, "warehouseId")
    })
  }).finally(() => {
    warehouseListLoading.value = false
  })
}

// 远程加载商品下拉选项，按需补充商品名称数据，减少页面初始加载压力。
function loadProductOptionList(searchKeyword) {
  productListLoading.value = true
  listProduct({
    pageNum: 1,
    pageSize: 20,
    productName: normalizeRemoteKeyword(searchKeyword)
  }).then(response => {
    const productRowList = response.rows || []
    productRowList.forEach(productItem => {
      appendUniqueSelectOption(productList.value, productItem, "productId")
    })
  }).finally(() => {
    productListLoading.value = false
  })
}

// 远程加载库位下拉选项，按需补充库位名称数据，减少页面初始加载压力。
function loadLocationOptionList(searchKeyword) {
  locationListLoading.value = true
  listLocation({
    pageNum: 1,
    pageSize: 20,
    locationName: normalizeRemoteKeyword(searchKeyword)
  }).then(response => {
    const locationRowList = response.rows || []
    locationRowList.forEach(locationItem => {
      appendUniqueSelectOption(locationList.value, locationItem, "locationId")
    })
  }).finally(() => {
    locationListLoading.value = false
  })
}

// 按仓库编号补充当前页面缺失的仓库名称，保证列表和筛选回显稳定。
function ensureWarehouseOptionLoaded(warehouseId) {
  if (warehouseId === undefined || warehouseId === null || warehouseId === "")
  {
    return Promise.resolve()
  }
  if (warehouseList.value.some(warehouseItem => warehouseItem.warehouseId === warehouseId))
  {
    return Promise.resolve()
  }
  return getWarehouse(warehouseId).then(response => {
    appendUniqueSelectOption(warehouseList.value, response.data, "warehouseId")
  }).catch(() => {
    appendUniqueSelectOption(warehouseList.value, {
      warehouseId: warehouseId,
      warehouseName: "仓库资料缺失"
    }, "warehouseId")
  })
}

// 按商品编号补充当前页面缺失的商品名称，保证列表和筛选回显稳定。
function ensureProductOptionLoaded(productId) {
  if (productId === undefined || productId === null || productId === "")
  {
    return Promise.resolve()
  }
  if (productList.value.some(productItem => productItem.productId === productId))
  {
    return Promise.resolve()
  }
  return getProduct(productId).then(response => {
    appendUniqueSelectOption(productList.value, response.data, "productId")
  }).catch(() => {
    appendUniqueSelectOption(productList.value, {
      productId: productId,
      productName: "商品资料缺失"
    }, "productId")
  })
}

// 按库位编号补充当前页面缺失的库位名称，保证列表和筛选回显稳定。
function ensureLocationOptionLoaded(locationId) {
  if (locationId === undefined || locationId === null || locationId === "")
  {
    return Promise.resolve()
  }
  if (locationList.value.some(locationItem => locationItem.locationId === locationId))
  {
    return Promise.resolve()
  }
  return getLocation(locationId).then(response => {
    appendUniqueSelectOption(locationList.value, response.data, "locationId")
  }).catch(() => {
    appendUniqueSelectOption(locationList.value, {
      locationId: locationId,
      locationName: "库位资料缺失"
    }, "locationId")
  })
}

// 批量补充库存流水列表依赖的名称数据，优先复用后端关联字段，消除冗余 N+1 查询。
function ensureStockLogReferenceOptionsLoaded(stockLogRowList) {
  const warehouseIdList = []
  const productIdList = []
  const locationIdList = []
  ;(stockLogRowList || []).forEach(stockLogRow => {
    if (stockLogRow.warehouseId != null && stockLogRow.warehouseId !== "") {
      if (stockLogRow.warehouseName) {
        appendUniqueSelectOption(warehouseList.value, { warehouseId: stockLogRow.warehouseId, warehouseName: stockLogRow.warehouseName }, "warehouseId")
      } else {
        warehouseIdList.push(stockLogRow.warehouseId)
      }
    }
    if (stockLogRow.productId != null && stockLogRow.productId !== "") {
      if (stockLogRow.productName) {
        appendUniqueSelectOption(productList.value, { productId: stockLogRow.productId, productName: stockLogRow.productName }, "productId")
      } else {
        productIdList.push(stockLogRow.productId)
      }
    }
    if (stockLogRow.locationId != null && stockLogRow.locationId !== "") {
      if (stockLogRow.locationName) {
        appendUniqueSelectOption(locationList.value, { locationId: stockLogRow.locationId, locationName: stockLogRow.locationName }, "locationId")
      } else {
        locationIdList.push(stockLogRow.locationId)
      }
    }
  })
  Promise.all([
    ...[...new Set(warehouseIdList)].map(warehouseId => ensureWarehouseOptionLoaded(warehouseId)),
    ...[...new Set(productIdList)].map(productId => ensureProductOptionLoaded(productId)),
    ...[...new Set(locationIdList)].map(locationId => ensureLocationOptionLoaded(locationId))
  ]).catch(() => {})
}

// 组合仓库下拉选项，兼容历史库存流水中主数据缺失时的兜底回显。
function buildWarehouseSelectOptionList() {
  return buildSelectOptionList(warehouseList.value, [queryParams.value.warehouseId], "warehouseId", "warehouseName", () => "仓库资料缺失")
}

// 组合商品下拉选项，兼容历史库存流水中主数据缺失时的兜底回显。
function buildProductSelectOptionList() {
  return buildSelectOptionList(productList.value, [queryParams.value.productId], "productId", "productName", () => "商品资料缺失")
}

// 组合库位下拉选项，兼容历史库存流水中主数据缺失时的兜底回显。
function buildLocationSelectOptionList() {
  return buildSelectOptionList(locationList.value, [queryParams.value.locationId], "locationId", "locationName", () => "库位资料缺失")
}

// 根据仓库编号返回仓库名称，减少库存流水中的纯编号展示。
function getWarehouseName(warehouseId) {
  const warehouse = warehouseList.value.find(warehouseItem => warehouseItem.warehouseId === warehouseId)
  return warehouse ? warehouse.warehouseName : (warehouseId ? "仓库资料缺失" : "")
}

// 根据商品编号返回商品名称，帮助仓库人员快速确认当前流水对应的商品。
function getProductName(productId) {
  const product = productList.value.find(productItem => productItem.productId === productId)
  return product ? product.productName : (productId ? "商品资料缺失" : "")
}

// 根据库位编号返回库位名称，统一库存流水的展示口径。
function getLocationName(locationId) {
  const location = locationList.value.find(locationItem => locationItem.locationId === locationId)
  return location ? location.locationName : (locationId ? "库位资料缺失" : "")
}

// 返回单据类型中文名称，帮助仓库人员快速理解每条流水的来源。
function getBillTypeLabel(billTypeValue) {
  const billTypeOption = billTypeOptionList.find(optionItem => optionItem.value === billTypeValue)
  return billTypeOption ? billTypeOption.label : (billTypeValue || "")
}

// 返回出入库方向中文名称，统一流水方向展示口径。
function getInOutLabel(inOutValue) {
  const inOutOption = inOutOptionList.find(optionItem => optionItem.value === inOutValue)
  return inOutOption ? inOutOption.label : (inOutValue || "")
}

// 返回出入库方向标签类型，帮助仓库人员快速识别库存是流入还是流出。
function getInOutTagType(inOutValue) {
  return inOutValue === "in" ? "success" : "warning"
}

const billTypeRouteMap = {
  inbound: { path: "/purchase/inbound", noKey: "inboundNo" },
  outbound: { path: "/sales/outbound", noKey: "outboundNo" },
  sale_order: { path: "/sales/saleOrder", queryKey: "saleOrderId" },
  sale_order_rollback: { path: "/sales/saleOrder", queryKey: "saleOrderId" },
  sale_return: { path: "/sales/saleReturn", noKey: "returnNo" },
  purchase_return: { path: "/purchase/purchaseReturn", noKey: "returnNo" },
  inventory_check: { path: "/inventory/inventoryCheck", noKey: "checkNo" },
  transfer: { path: "/inventory/transfer", noKey: "transferNo" },
  transfer_out: { path: "/inventory/transfer", noKey: "transferNo" },
  transfer_in: { path: "/inventory/transfer", noKey: "transferNo" },
  stock_freeze: { path: "/inventory/stock", productKey: "productId" },
  stock_unfreeze: { path: "/inventory/stock", productKey: "productId" }
}

function canJumpSourceBill(row) {
  return !!(row && billTypeRouteMap[row.billType])
}

function handleJumpSourceBill(row) {
  const target = billTypeRouteMap[row?.billType]
  if (!target) return
  const query = {}
  if (target.queryKey && row.billId) {
    query[target.queryKey] = String(row.billId)
  } else if (target.noKey && row.billNo) {
    query[target.noKey] = String(row.billNo)
  } else if (target.productKey && row.productId) {
    query[target.productKey] = String(row.productId)
  }
  router.push({ path: target.path, query })
}

// 查询库存流水列表，并补充当前页需要展示的名称数据。
function getList() {
  loading.value = true
  listStockLog(queryParams.value).then(response => {
    stockLogList.value = response.rows
    total.value = response.total
    ensureStockLogReferenceOptionsLoaded(response.rows)
  }).finally(() => {
    loading.value = false
  })
}

// 搜索库存流水时回到第一页，避免沿用历史分页导致用户误以为无数据。
function handleQuery() {
  queryParams.value.pageNum = 1
  getList()
}

// 重置筛选条件，恢复库存流水默认查询口径。
function resetQuery() {
  proxy.resetForm("queryRef")
  handleQuery()
}

// 导出库存流水时复用当前筛选条件，保证导出的数据与页面所见一致。
function handleExport() {
  proxy.download("business/stockLog/export", {
    ...queryParams.value
  }, `stock_log_${new Date().getTime()}.xlsx`)
}

// 初始化页面筛选条件和基础资料，保证从库存台账重复跳转到流水页时查询条件会立即刷新。
async function initializePage() {
  initializeQueryParamsFromRoute()
  await Promise.all([
    Promise.resolve(initBasicData()),
    Promise.resolve(getList())
  ])
}

// 监听同一路由下的查询参数变化，避免库存流水仍然停留在旧的商品或库位筛选。
watch(() => route.fullPath, (currentRouteFullPath, previousRouteFullPath) => {
  if (currentRouteFullPath === previousRouteFullPath) {
    return
  }
  initializePage()
})

initializePage()
</script>
