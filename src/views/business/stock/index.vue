<template>
  <div class="app-container">
    <el-form :model="queryParams" ref="queryRef" :inline="true" v-show="showSearch">
      <el-form-item label="仓库" prop="warehouseId">
        <el-select v-model="queryParams.warehouseId" placeholder="请选择仓库" clearable filterable remote reserve-keyword remote-show-suffix :remote-method="loadWarehouseOptionList" :loading="warehouseListLoading" style="width: 220px">
          <el-option
            v-for="warehouse in warehouseSelectOptionList"
            :key="warehouse.warehouseId"
            :label="warehouse.warehouseName"
            :value="warehouse.warehouseId"
          />
        </el-select>
      </el-form-item>
      <el-form-item label="商品" prop="productId">
        <el-select v-model="queryParams.productId" placeholder="请选择商品" clearable filterable remote reserve-keyword remote-show-suffix :remote-method="loadProductOptionList" :loading="productListLoading" style="width: 220px">
          <el-option
            v-for="product in productSelectOptionList"
            :key="product.productId"
            :label="product.productName"
            :value="product.productId"
          />
        </el-select>
      </el-form-item>
      <el-form-item label="库位" prop="locationId">
        <el-select v-model="queryParams.locationId" placeholder="请选择库位" clearable filterable remote reserve-keyword remote-show-suffix :remote-method="loadLocationOptionList" :loading="locationListLoading" style="width: 220px">
          <el-option
            v-for="location in locationSelectOptionList"
            :key="location.locationId"
            :label="location.locationName"
            :value="location.locationId"
          />
        </el-select>
      </el-form-item>
      <el-form-item label="批次号" prop="batchNo">
        <el-input v-model="queryParams.batchNo" placeholder="请输入批次号" clearable style="width: 200px" @keyup.enter="handleQuery" />
      </el-form-item>
      <el-form-item>
        <el-button type="primary" icon="Search" @click="handleQuery">搜索</el-button>
        <el-button icon="Refresh" @click="resetQuery">重置</el-button>
        <el-button type="warning" icon="Warning" @click="handleWarningQuery">{{ isWarningQuery ? "刷新预警结果" : "预警查询" }}</el-button>
        <el-button v-if="isWarningQuery" type="info" plain icon="View" @click="handleClearWarningQuery">查看全部库存</el-button>
      </el-form-item>
    </el-form>

    <el-alert
      v-if="isWarningQuery"
      title="当前仅展示触发库存预警的记录，方便仓库优先处理异常库存。"
      type="warning"
      :closable="false"
      class="mb8"
    />

    <el-alert
      title="库存台账由入库、出库、盘点、调拨等业务单据自动维护，当前页面仅用于查询、预警处理和查看变动流水。"
      type="info"
      :closable="false"
      class="mb8"
    />

    <el-row :gutter="10" class="mb8">
      <el-col :span="1.5">
        <el-button type="warning" plain icon="Download" @click="handleExport" v-hasPermi="['business:stock:export']">导出</el-button>
      </el-col>
      <el-col :span="3">
        <el-button type="warning" plain icon="AlarmClock" @click="handleWarningRemind" v-hasPermi="['business:stock:list']">
          触发预警提醒
        </el-button>
      </el-col>
      <el-col :span="3">
        <el-button type="danger" plain icon="Bell" @click="handleReadWarningMessage" v-hasPermi="['business:stock:list']">
          预警消息已读({{ warningUnreadCount }})
        </el-button>
      </el-col>
      <right-toolbar v-model:showSearch="showSearch" @queryTable="getList"></right-toolbar>
    </el-row>

    <el-table v-loading="loading" :data="stockList">
      <el-table-column label="库存编号" align="center" prop="stockId" />
      <el-table-column label="仓库" align="center" prop="warehouseId" min-width="180">
        <template #default="scope">
          <span>{{ getWarehouseName(scope.row.warehouseId) }}</span>
        </template>
      </el-table-column>
      <el-table-column label="商品" align="center" prop="productId" min-width="180">
        <template #default="scope">
          <span>{{ getProductName(scope.row.productId) }}</span>
        </template>
      </el-table-column>
      <el-table-column label="库位" align="center" prop="locationId" min-width="180">
        <template #default="scope">
          <span>{{ getLocationName(scope.row.locationId) }}</span>
        </template>
      </el-table-column>
      <el-table-column label="批次号" align="center" prop="batchNo" />
      <el-table-column label="库存数量" align="center" prop="quantity" />
      <el-table-column label="锁定数量" align="center" prop="lockedQuantity" />
      <el-table-column label="冻结数量" align="center" prop="frozenQuantity" />
      <el-table-column label="预警状态" align="center" min-width="120">
        <template #default="scope">
          <el-tag :type="getStockWarningStatusType(scope.row)">{{ getStockWarningStatusLabel(scope.row) }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column label="最小预警值" align="center" prop="warningMinQty" />
      <el-table-column label="最大预警值" align="center" prop="warningMaxQty" />
      <el-table-column label="创建时间" align="center" prop="createTime" width="180">
        <template #default="scope">
          <span>{{ parseTime(scope.row.createTime) }}</span>
        </template>
      </el-table-column>
      <el-table-column label="操作" width="220" align="center" class-name="small-padding fixed-width">
        <template #default="scope">
          <el-button link type="primary" icon="Document" @click="handleStockLog(scope.row)" v-hasPermi="['business:stockLog:list']">变动流水</el-button>
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

<script setup name="BusinessStock">
import { listStock, listWarningStock, remindWarningStock } from "@/api/business/stock"
import { getUnreadMessageCount, listPopupMessage, readAllMessage } from "@/api/business/message"
import { listWarehouse, getWarehouse } from "@/api/business/warehouse"
import { listProduct, getProduct } from "@/api/business/product"
import { listLocation, getLocation } from "@/api/business/location"
import { useRouter } from 'vue-router'
import { appendUniqueSelectOption, buildSelectOptionList, normalizeRemoteKeyword } from "@/utils/remoteSelect"
import { parseTime } from "@/utils/ruoyi"

const { proxy } = getCurrentInstance()
const route = useRoute()
const router = useRouter()

const stockList = ref([])
const loading = ref(true)
const showSearch = ref(true)
const total = ref(0)
const isWarningQuery = ref(false)
const warningUnreadCount = ref(0)
const warehouseList = ref([])
const productList = ref([])
const locationList = ref([])
const warehouseListLoading = ref(false)
const productListLoading = ref(false)
const locationListLoading = ref(false)

const data = reactive({
  queryParams: {
    pageNum: 1,
    pageSize: 10,
    warehouseId: undefined,
    productId: undefined,
    locationId: undefined,
    batchNo: undefined
  }
})

const { queryParams } = toRefs(data)
const warehouseSelectOptionList = computed(() => buildWarehouseSelectOptionList())
const productSelectOptionList = computed(() => buildProductSelectOptionList())
const locationSelectOptionList = computed(() => buildLocationSelectOptionList())

// 初始化库存台账依赖的远程下拉数据，避免页面初始阶段一次性加载过多基础资料。
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

// 按仓库编号补充当前页面缺失的仓库名称，保证列表、表单和筛选的名称回显稳定。
function ensureWarehouseOptionLoaded(warehouseId) {
  if (warehouseId === undefined || warehouseId === null || warehouseId === "") {
    return Promise.resolve()
  }
  if (warehouseList.value.some(warehouseItem => warehouseItem.warehouseId === warehouseId)) {
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

// 按商品编号补充当前页面缺失的商品名称，避免远程下拉场景下只显示历史编号。
function ensureProductOptionLoaded(productId) {
  if (productId === undefined || productId === null || productId === "") {
    return Promise.resolve()
  }
  if (productList.value.some(productItem => productItem.productId === productId)) {
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

// 按库位编号补充当前页面缺失的库位名称，保证库存台账各处的名称回显稳定。
function ensureLocationOptionLoaded(locationId) {
  if (locationId === undefined || locationId === null || locationId === "") {
    return Promise.resolve()
  }
  if (locationList.value.some(locationItem => locationItem.locationId === locationId)) {
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

// 批量补充库存列表依赖的仓库、商品和库位名称，保证远程下拉模式下列表展示仍然可读。
function ensureStockReferenceOptionsLoaded(stockRowList) {
  const warehouseIdList = [...new Set((stockRowList || []).map(stockRow => stockRow.warehouseId).filter(warehouseId => warehouseId !== undefined && warehouseId !== null && warehouseId !== ""))]
  const productIdList = [...new Set((stockRowList || []).map(stockRow => stockRow.productId).filter(productId => productId !== undefined && productId !== null && productId !== ""))]
  const locationIdList = [...new Set((stockRowList || []).map(stockRow => stockRow.locationId).filter(locationId => locationId !== undefined && locationId !== null && locationId !== ""))]
  Promise.all([
    ...warehouseIdList.map(warehouseId => ensureWarehouseOptionLoaded(warehouseId)),
    ...productIdList.map(productId => ensureProductOptionLoaded(productId)),
    ...locationIdList.map(locationId => ensureLocationOptionLoaded(locationId))
  ]).catch(() => {})
}

// 组合仓库下拉选项，兼容历史库存记录中主数据缺失时的兜底回显。
function buildWarehouseSelectOptionList() {
  return buildSelectOptionList(warehouseList.value, [queryParams.value.warehouseId], "warehouseId", "warehouseName", () => "仓库资料缺失")
}

// 组合商品下拉选项，保证历史库存记录中主数据缺失时也能正常回显。
function buildProductSelectOptionList() {
  return buildSelectOptionList(productList.value, [queryParams.value.productId], "productId", "productName", () => "商品资料缺失")
}

// 组合库位下拉选项，兼容历史库存记录中主数据缺失时的兜底回显。
function buildLocationSelectOptionList() {
  return buildSelectOptionList(locationList.value, [queryParams.value.locationId], "locationId", "locationName", () => "库位资料缺失")
}

// 根据仓库编号返回仓库名称，减少库存台账中的纯编号展示。
function getWarehouseName(warehouseId) {
  const warehouse = warehouseList.value.find(warehouseItem => warehouseItem.warehouseId === warehouseId)
  return warehouse ? warehouse.warehouseName : (warehouseId ? "仓库资料缺失" : "")
}

// 根据商品编号返回商品名称，帮助仓库人员快速确认当前库存对象。
function getProductName(productId) {
  const product = productList.value.find(productItem => productItem.productId === productId)
  return product ? product.productName : (productId ? "商品资料缺失" : "")
}

// 根据库位编号返回库位名称，统一库存台账的展示口径。
function getLocationName(locationId) {
  const location = locationList.value.find(locationItem => locationItem.locationId === locationId)
  return location ? location.locationName : (locationId ? "库位资料缺失" : "")
}

// 判断当前库存是否低于最小预警值，帮助仓库人员快速识别缺货风险。
function isStockBelowWarningMin(stockRow) {
  const quantityValue = Number(stockRow.quantity ?? 0)
  const warningMinQtyValue = Number(stockRow.warningMinQty ?? 0)
  return warningMinQtyValue > 0 && quantityValue < warningMinQtyValue
}

// 判断当前库存是否高于最大预警值，帮助仓库人员快速识别积压风险。
function isStockAboveWarningMax(stockRow) {
  const quantityValue = Number(stockRow.quantity ?? 0)
  const warningMaxQtyValue = Number(stockRow.warningMaxQty ?? 0)
  return warningMaxQtyValue > 0 && quantityValue > warningMaxQtyValue
}

// 返回库存预警状态中文名称，让仓库页直接展示当前库存是否异常。
function getStockWarningStatusLabel(stockRow) {
  if (isStockBelowWarningMin(stockRow)) {
    return "低于最小值"
  }
  if (isStockAboveWarningMax(stockRow)) {
    return "高于最大值"
  }
  return "正常"
}

// 返回库存预警状态标签类型，方便仓库人员快速区分异常类型。
function getStockWarningStatusType(stockRow) {
  if (isStockBelowWarningMin(stockRow)) {
    return "danger"
  }
  if (isStockAboveWarningMax(stockRow)) {
    return "warning"
  }
  return "success"
}

function getList() {
  loading.value = true
  const queryAction = isWarningQuery.value ? listWarningStock : listStock
  queryAction(queryParams.value).then(response => {
    stockList.value = response.rows
    total.value = response.total
    ensureStockReferenceOptionsLoaded(response.rows)
  }).finally(() => {
    loading.value = false
  })
}

function checkWarningReminder() {
  getUnreadMessageCount("stock_warning").then(unreadResponse => {
    warningUnreadCount.value = unreadResponse.unreadCount || 0
    return listPopupMessage("stock_warning", 5)
  }).then(popupResponse => {
    const popupMessageList = popupResponse.rows || []
    if (popupMessageList.length === 0) {
      return
    }
    const popupTitleList = popupMessageList.map(messageItem => messageItem.messageTitle)
    const popupContent = popupTitleList.join("；")
    proxy.$modal.confirm(`当前存在${warningUnreadCount.value}条库存预警消息：${popupContent}。是否立即标记为已读？`).then(() => {
      return readAllMessage("stock_warning")
    }).then(() => {
      warningUnreadCount.value = 0
      proxy.$modal.msgSuccess("库存预警消息已标记为已读")
    }).catch(() => {})
  }).catch(() => {})
}

function handleWarningRemind() {
  remindWarningStock().then(response => {
    const warningCount = response.warningCount || 0
    if (warningCount === 0) {
      proxy.$modal.msgSuccess("当前没有库存预警数据")
      return
    }
    isWarningQuery.value = true
    queryParams.value.pageNum = 1
    getList()
    proxy.$modal.msgSuccess(`触发成功，当前预警商品共${warningCount}条`)
    checkWarningReminder()
  })
}

function handleReadWarningMessage() {
  if (warningUnreadCount.value === 0) {
    proxy.$modal.msgSuccess("当前没有未读库存预警消息")
    return
  }
  proxy.$modal.confirm(`是否将${warningUnreadCount.value}条库存预警消息全部标记为已读？`).then(() => {
    return readAllMessage("stock_warning")
  }).then(() => {
    warningUnreadCount.value = 0
    proxy.$modal.msgSuccess("操作成功")
  })
    .catch(() => {})
}

function handleQuery() {
  isWarningQuery.value = false
  queryParams.value.pageNum = 1
  getList()
}

function handleWarningQuery() {
  isWarningQuery.value = true
  queryParams.value.pageNum = 1
  getList()
}

// 退出库存预警查看模式，恢复全部库存列表，减少仓库人员来回重置筛选的操作。
function handleClearWarningQuery() {
  isWarningQuery.value = false
  queryParams.value.pageNum = 1
  getList()
}

function resetQuery() {
  proxy.resetForm("queryRef")
  isWarningQuery.value = false
  handleQuery()
}

function handleAdd() {
  proxy.$modal.msgWarning("库存台账由业务单据自动维护，不支持手工新增")
}

function handleUpdate() {
  proxy.$modal.msgWarning("库存台账由业务单据自动维护，不支持手工修改")
}

function submitForm() {
  proxy.$modal.msgWarning("库存台账由业务单据自动维护，不支持手工保存")
}

function handleDelete() {
  proxy.$modal.msgWarning("库存台账由业务单据自动维护，不支持手工删除")
}

function handleExport() {
  proxy.download("business/stock/export", {
    ...queryParams.value
  }, `stock_${new Date().getTime()}.xlsx`)
}

function handleStockLog(row) {
  router.push({
    path: '/inventory/stockLog',
    query: {
      warehouseId: row.warehouseId,
      productId: row.productId,
      locationId: row.locationId,
      batchNo: row.batchNo
    }
  })
}

// 初始化库存页筛选状态和基础数据，保证从首页库存预警重复跳转时查询结果能够及时更新。
async function initializePage() {
  isWarningQuery.value = route.query.warningQuery === "1"
  await initBasicData()
  await getList()
}

onMounted(() => {
  initializePage()
  checkWarningReminder()
})

// 监听同一路由下的查询参数变化，避免库存页在预警筛选切换后仍显示旧列表。
watch(() => route.fullPath, (currentRouteFullPath, previousRouteFullPath) => {
  if (currentRouteFullPath === previousRouteFullPath) {
    return
  }
  initializePage()
})
</script>
