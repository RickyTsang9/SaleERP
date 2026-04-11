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
      <el-form-item label="单据类型" prop="billType">
        <el-select v-model="queryParams.billType" placeholder="请选择单据类型" clearable style="width: 180px">
          <el-option
            v-for="billTypeOption in billTypeOptionList"
            :key="billTypeOption.value"
            :label="billTypeOption.label"
            :value="billTypeOption.value"
          />
        </el-select>
      </el-form-item>
      <el-form-item label="单据号" prop="billNo">
        <el-input v-model="queryParams.billNo" placeholder="请输入单据号" clearable style="width: 200px" @keyup.enter="handleQuery" />
      </el-form-item>
      <el-form-item label="出入库方向" prop="inOut">
        <el-select v-model="queryParams.inOut" placeholder="请选择出入库方向" clearable style="width: 180px">
          <el-option
            v-for="directionOption in inOutOptionList"
            :key="directionOption.value"
            :label="directionOption.label"
            :value="directionOption.value"
          />
        </el-select>
      </el-form-item>
      <el-form-item>
        <el-button type="primary" icon="Search" @click="handleQuery">搜索</el-button>
        <el-button icon="Refresh" @click="resetQuery">重置</el-button>
      </el-form-item>
    </el-form>

    <el-row :gutter="10" class="mb8">
      <el-col :span="1.5">
        <el-button type="warning" plain icon="Download" @click="handleExport" v-hasPermi="['business:stockLog:export']">导出</el-button>
      </el-col>
      <right-toolbar v-model:showSearch="showSearch" @queryTable="getList"></right-toolbar>
    </el-row>

    <el-table v-loading="loading" :data="stockLogList">
      <el-table-column label="流水编号" align="center" prop="stockLogId" />
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
      <el-table-column label="单据类型" align="center" prop="billType">
        <template #default="scope">
          <span>{{ getBillTypeLabel(scope.row.billType) }}</span>
        </template>
      </el-table-column>
      <el-table-column label="单据号" align="center" prop="billNo" min-width="180" show-overflow-tooltip />
      <el-table-column label="单据编号" align="center" prop="billId" />
      <el-table-column label="出入库方向" align="center" prop="inOut">
        <template #default="scope">
          <el-tag :type="getInOutTagType(scope.row.inOut)">{{ getInOutLabel(scope.row.inOut) }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column label="变动数量" align="center" prop="quantity" />
      <el-table-column label="单价" align="center" prop="price" />
      <el-table-column label="金额" align="center" prop="amount" />
      <el-table-column label="变动前数量" align="center" prop="beforeQty" />
      <el-table-column label="变动后数量" align="center" prop="afterQty" />
      <el-table-column label="创建时间" align="center" prop="createTime" width="180">
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
  </div>
</template>

<script setup name="BusinessStockLog">
import { listStockLog } from "@/api/business/stockLog"
import { listWarehouse, getWarehouse } from "@/api/business/warehouse"
import { listProduct, getProduct } from "@/api/business/product"
import { listLocation, getLocation } from "@/api/business/location"
import { appendUniqueSelectOption, buildSelectOptionList, normalizeRemoteKeyword } from "@/utils/remoteSelect"

const { proxy } = getCurrentInstance()
const route = useRoute()

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
  { label: "库存调拨", value: "transfer" }
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

// 根据其他业务页传入的参数初始化库存流水筛选条件，保证落地页即开即用。
function initializeQueryParamsFromRoute() {
  if (route.query.productId)
  {
    queryParams.value.productId = Number(route.query.productId)
  }
  if (route.query.warehouseId)
  {
    queryParams.value.warehouseId = Number(route.query.warehouseId)
  }
  if (route.query.locationId)
  {
    queryParams.value.locationId = Number(route.query.locationId)
  }
  if (route.query.batchNo)
  {
    queryParams.value.batchNo = route.query.batchNo
  }
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
      warehouseName: `历史仓库ID：${warehouseId}`
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
      productName: `历史商品ID：${productId}`
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
      locationName: `历史库位ID：${locationId}`
    }, "locationId")
  })
}

// 批量补充库存流水列表依赖的名称数据，保证远程下拉模式下列表展示仍然可读。
function ensureStockLogReferenceOptionsLoaded(stockLogRowList) {
  const warehouseIdList = [...new Set((stockLogRowList || []).map(stockLogRow => stockLogRow.warehouseId).filter(warehouseId => warehouseId !== undefined && warehouseId !== null && warehouseId !== ""))]
  const productIdList = [...new Set((stockLogRowList || []).map(stockLogRow => stockLogRow.productId).filter(productId => productId !== undefined && productId !== null && productId !== ""))]
  const locationIdList = [...new Set((stockLogRowList || []).map(stockLogRow => stockLogRow.locationId).filter(locationId => locationId !== undefined && locationId !== null && locationId !== ""))]
  Promise.all([
    ...warehouseIdList.map(warehouseId => ensureWarehouseOptionLoaded(warehouseId)),
    ...productIdList.map(productId => ensureProductOptionLoaded(productId)),
    ...locationIdList.map(locationId => ensureLocationOptionLoaded(locationId))
  ]).catch(() => {})
}

// 组合仓库下拉选项，兼容历史库存流水中仍在使用的旧仓库编号回显。
function buildWarehouseSelectOptionList() {
  return buildSelectOptionList(warehouseList.value, [queryParams.value.warehouseId], "warehouseId", "warehouseName", "历史仓库ID：")
}

// 组合商品下拉选项，兼容历史库存流水中仍在使用的旧商品编号回显。
function buildProductSelectOptionList() {
  return buildSelectOptionList(productList.value, [queryParams.value.productId], "productId", "productName", "历史商品ID：")
}

// 组合库位下拉选项，兼容历史库存流水中仍在使用的旧库位编号回显。
function buildLocationSelectOptionList() {
  return buildSelectOptionList(locationList.value, [queryParams.value.locationId], "locationId", "locationName", "历史库位ID：")
}

// 根据仓库编号返回仓库名称，减少库存流水中的纯编号展示。
function getWarehouseName(warehouseId) {
  const warehouse = warehouseList.value.find(warehouseItem => warehouseItem.warehouseId === warehouseId)
  return warehouse ? warehouse.warehouseName : (warehouseId ? `历史仓库ID：${warehouseId}` : "")
}

// 根据商品编号返回商品名称，帮助仓库人员快速确认当前流水对应的商品。
function getProductName(productId) {
  const product = productList.value.find(productItem => productItem.productId === productId)
  return product ? product.productName : (productId ? `历史商品ID：${productId}` : "")
}

// 根据库位编号返回库位名称，统一库存流水的展示口径。
function getLocationName(locationId) {
  const location = locationList.value.find(locationItem => locationItem.locationId === locationId)
  return location ? location.locationName : (locationId ? `历史库位ID：${locationId}` : "")
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

initializeQueryParamsFromRoute()
initBasicData()
getList()
</script>
