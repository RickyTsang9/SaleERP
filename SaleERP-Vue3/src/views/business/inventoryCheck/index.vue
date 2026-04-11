<template>
  <div class="app-container">
    <el-form :model="queryParams" ref="queryRef" :inline="true" v-show="showSearch">
      <el-form-item label="盘点单号" prop="checkNo">
        <el-input v-model="queryParams.checkNo" placeholder="请输入盘点单号" clearable style="width: 220px" @keyup.enter="handleQuery" />
      </el-form-item>
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
        <el-button type="primary" plain icon="Plus" @click="handleAdd" v-hasPermi="['business:inventoryCheck:add']">新增</el-button>
      </el-col>
      <el-col :span="1.5">
        <el-button type="success" plain icon="Edit" :disabled="!canUpdateSelected" @click="handleUpdate" v-hasPermi="['business:inventoryCheck:edit']">修改</el-button>
      </el-col>
      <el-col :span="1.5">
        <el-button type="info" plain icon="Upload" :disabled="!canSubmitSelected" @click="handleSubmit" v-hasPermi="['business:inventoryCheck:edit']">提交</el-button>
      </el-col>
      <el-col :span="1.5">
        <el-button type="warning" plain icon="CircleCheck" :disabled="!canAuditSelected" @click="handleAudit" v-hasPermi="['business:inventoryCheck:audit']">审核</el-button>
      </el-col>
      <el-col :span="1.5">
        <el-button type="danger" plain icon="Delete" :disabled="!canDeleteSelected" @click="handleDelete" v-hasPermi="['business:inventoryCheck:remove']">删除</el-button>
      </el-col>
      <el-col :span="1.5">
        <el-button type="warning" plain icon="Download" @click="handleExport" v-hasPermi="['business:inventoryCheck:export']">导出</el-button>
      </el-col>
      <right-toolbar v-model:showSearch="showSearch" @queryTable="getList"></right-toolbar>
    </el-row>

    <el-table v-loading="loading" :data="inventoryCheckList" @selection-change="handleSelectionChange">
      <el-table-column type="selection" width="55" align="center" />
      <el-table-column label="盘点编号" align="center" prop="checkId" />
      <el-table-column label="盘点单号" align="center" prop="checkNo" min-width="180" />
      <el-table-column label="仓库" align="center" prop="warehouseId" min-width="180">
        <template #default="scope">
          <span>{{ getWarehouseName(scope.row.warehouseId) }}</span>
        </template>
      </el-table-column>
      <el-table-column label="差异数量" align="center" prop="totalDiffQty" />
      <el-table-column label="差异金额" align="center" prop="totalDiffAmount" />
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
      <el-table-column label="操作" width="260" align="center" class-name="small-padding fixed-width">
        <template #default="scope">
          <el-button link type="primary" icon="List" @click="handleOpenInventoryCheckItem(scope.row)" v-hasPermi="['business:inventoryCheck:query']">明细</el-button>
          <el-button link type="primary" icon="Edit" :disabled="!isDraftStatus(scope.row.status)" @click="handleUpdate(scope.row)" v-hasPermi="['business:inventoryCheck:edit']">修改</el-button>
          <el-button link type="primary" icon="Upload" :disabled="!isDraftStatus(scope.row.status)" @click="handleSubmit(scope.row)" v-hasPermi="['business:inventoryCheck:edit']">提交</el-button>
          <el-button link type="primary" icon="CircleCheck" :disabled="!isSubmittedStatus(scope.row.status)" @click="handleAudit(scope.row)" v-hasPermi="['business:inventoryCheck:audit']">审核</el-button>
          <el-button link type="primary" icon="CircleClose" :disabled="!canCancelStatus(scope.row.status)" @click="handleCancelOrder(scope.row)" v-hasPermi="['business:inventoryCheck:edit']">作废</el-button>
          <el-button link type="primary" icon="Delete" :disabled="!isDraftStatus(scope.row.status)" @click="handleDelete(scope.row)" v-hasPermi="['business:inventoryCheck:remove']">删除</el-button>
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
      <el-alert
        title="仓库、状态和审核人请按流程维护，当前弹窗仅允许草稿盘点单修改基础信息。"
        type="info"
        :closable="false"
        class="mb8"
      />
      <el-form ref="inventoryCheckRef" :model="form" :rules="rules" label-width="110px">
        <el-row>
          <el-col :span="12">
            <el-form-item label="仓库" prop="warehouseId">
              <el-select v-model="form.warehouseId" placeholder="请选择仓库" clearable filterable remote reserve-keyword remote-show-suffix :remote-method="loadWarehouseOptionList" :loading="warehouseListLoading" style="width: 100%">
                <el-option
                  v-for="warehouse in warehouseSelectOptionList"
                  :key="warehouse.warehouseId"
                  :label="warehouse.warehouseName"
                  :value="warehouse.warehouseId"
                />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="状态" prop="status">
              <el-select v-model="form.status" style="width: 100%" placeholder="状态由流程自动维护" disabled>
                <el-option v-for="statusOption in statusOptions" :key="statusOption.value" :label="statusOption.label" :value="statusOption.value" />
              </el-select>
            </el-form-item>
          </el-col>
        </el-row>
        <el-row>
          <el-col :span="12">
            <el-form-item label="差异数量" prop="totalDiffQty">
              <el-input-number v-model="form.totalDiffQty" :precision="2" controls-position="right" style="width: 100%" :disabled="true" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="差异金额" prop="totalDiffAmount">
              <el-input-number v-model="form.totalDiffAmount" :precision="2" controls-position="right" style="width: 100%" :disabled="true" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-row>
          <el-col :span="12">
            <el-form-item label="审核人" prop="auditBy">
              <el-input v-model="form.auditBy" placeholder="审核人由审核动作自动填写" disabled />
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

    <el-dialog :title="inventoryCheckItemTitle" v-model="inventoryCheckItemOpen" width="1080px" append-to-body>
      <el-alert
        v-if="!isInventoryCheckItemEditable()"
        title="当前盘点单已提交或已审核，明细仅支持查看，不支持继续增删改。"
        type="info"
        :closable="false"
        class="mb8"
      />
      <el-row :gutter="10" class="mb8">
        <el-col :span="1.5">
          <el-button v-if="isInventoryCheckItemEditable()" type="primary" plain icon="Plus" @click="handleAddInventoryCheckItem" v-hasPermi="['business:inventoryCheck:add']">新增明细</el-button>
        </el-col>
        <el-col :span="1.5">
          <el-button v-if="isInventoryCheckItemEditable()" type="danger" plain icon="Delete" :disabled="inventoryCheckItemMultiple" @click="handleDeleteInventoryCheckItem" v-hasPermi="['business:inventoryCheck:remove']">删除明细</el-button>
        </el-col>
      </el-row>
      <el-table v-loading="inventoryCheckItemLoading" :data="inventoryCheckItemList" @selection-change="handleInventoryCheckItemSelectionChange">
        <el-table-column v-if="isInventoryCheckItemEditable()" type="selection" width="55" align="center" />
        <el-table-column label="明细编号" align="center" prop="checkItemId" />
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
        <el-table-column label="账面数量" align="center" prop="stockQty" />
        <el-table-column label="实盘数量" align="center" prop="actualQty" />
        <el-table-column label="差异数量" align="center" prop="diffQty" />
        <el-table-column label="单价" align="center" prop="price" />
        <el-table-column label="差异金额" align="center" prop="diffAmount" />
        <el-table-column v-if="isInventoryCheckItemEditable()" label="操作" width="180" align="center">
          <template #default="scope">
            <el-button link type="primary" icon="Edit" @click="handleUpdateInventoryCheckItem(scope.row)" v-hasPermi="['business:inventoryCheck:edit']">修改</el-button>
            <el-button link type="primary" icon="Delete" @click="handleDeleteInventoryCheckItem(scope.row)" v-hasPermi="['business:inventoryCheck:remove']">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-dialog>

    <el-dialog :title="inventoryCheckItemFormTitle" v-model="inventoryCheckItemFormOpen" width="860px" append-to-body>
      <el-form ref="inventoryCheckItemRef" :model="inventoryCheckItemForm" :rules="inventoryCheckItemRules" label-width="110px">
        <el-row>
          <el-col :span="12">
            <el-form-item label="商品" prop="productId">
              <el-select v-model="inventoryCheckItemForm.productId" placeholder="请选择商品" clearable filterable remote reserve-keyword remote-show-suffix :remote-method="loadProductOptionList" :loading="productListLoading" style="width: 100%">
                <el-option
                  v-for="product in productSelectOptionList"
                  :key="product.productId"
                  :label="product.productName"
                  :value="product.productId"
                />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="库位" prop="locationId">
              <el-select v-model="inventoryCheckItemForm.locationId" placeholder="请选择库位" clearable filterable remote reserve-keyword remote-show-suffix :remote-method="loadLocationOptionList" :loading="locationListLoading" style="width: 100%">
                <el-option
                  v-for="location in locationSelectOptionList"
                  :key="location.locationId"
                  :label="location.locationName"
                  :value="location.locationId"
                />
              </el-select>
            </el-form-item>
          </el-col>
        </el-row>
        <el-row>
          <el-col :span="12">
            <el-form-item label="批次号" prop="batchNo">
              <el-input v-model="inventoryCheckItemForm.batchNo" placeholder="请输入批次号" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="账面数量" prop="stockQty">
              <el-input-number v-model="inventoryCheckItemForm.stockQty" :precision="2" controls-position="right" style="width: 100%" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-row>
          <el-col :span="12">
            <el-form-item label="实盘数量" prop="actualQty">
              <el-input-number v-model="inventoryCheckItemForm.actualQty" :precision="2" controls-position="right" style="width: 100%" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="差异数量" prop="diffQty">
              <el-input-number v-model="inventoryCheckItemForm.diffQty" :precision="2" controls-position="right" style="width: 100%" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-row>
          <el-col :span="12">
            <el-form-item label="单价" prop="price">
              <el-input-number v-model="inventoryCheckItemForm.price" :precision="2" controls-position="right" style="width: 100%" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="差异金额" prop="diffAmount">
              <el-input-number v-model="inventoryCheckItemForm.diffAmount" :precision="2" controls-position="right" style="width: 100%" />
            </el-form-item>
          </el-col>
        </el-row>
      </el-form>
      <template #footer>
        <div class="dialog-footer">
          <el-button type="primary" @click="submitInventoryCheckItemForm">确 定</el-button>
          <el-button @click="cancelInventoryCheckItemForm">取 消</el-button>
        </div>
      </template>
    </el-dialog>
  </div>
</template>

<script setup name="BusinessInventoryCheck">
import { listInventoryCheck, getInventoryCheck, addInventoryCheck, updateInventoryCheck, delInventoryCheck, submitInventoryCheck, auditInventoryCheck, cancelInventoryCheck } from "@/api/business/inventoryCheck"
import { listInventoryCheckItem, getInventoryCheckItem, addInventoryCheckItem, updateInventoryCheckItem, delInventoryCheckItem } from "@/api/business/inventoryCheckItem"
import { listWarehouse, getWarehouse } from "@/api/business/warehouse"
import { listProduct, getProduct } from "@/api/business/product"
import { listLocation, getLocation } from "@/api/business/location"
import { appendUniqueSelectOption, buildSelectOptionList, normalizeRemoteKeyword } from "@/utils/remoteSelect"

const { proxy } = getCurrentInstance()

const inventoryCheckList = ref([])
const warehouseList = ref([])
const warehouseListLoading = ref(false)
const productList = ref([])
const productListLoading = ref(false)
const locationList = ref([])
const locationListLoading = ref(false)
const open = ref(false)
const loading = ref(true)
const showSearch = ref(true)
const ids = ref([])
const single = ref(true)
const multiple = ref(true)
const canUpdateSelected = ref(false)
const canSubmitSelected = ref(false)
const canAuditSelected = ref(false)
const canDeleteSelected = ref(false)
const total = ref(0)
const title = ref("")
const inventoryCheckItemOpen = ref(false)
const inventoryCheckItemLoading = ref(false)
const inventoryCheckItemTitle = ref("")
const inventoryCheckItemList = ref([])
const currentCheckId = ref(undefined)
const currentInventoryCheckStatus = ref("draft")
const selectedInventoryCheckItemIds = ref([])
const inventoryCheckItemMultiple = ref(true)
const inventoryCheckItemFormOpen = ref(false)
const inventoryCheckItemFormTitle = ref("")
const warehouseSelectOptionList = computed(() => buildWarehouseSelectOptionList())
const productSelectOptionList = computed(() => buildProductSelectOptionList())
const locationSelectOptionList = computed(() => buildLocationSelectOptionList())
const statusOptions = ref([
  { label: "草稿", value: "draft" },
  { label: "已提交", value: "submitted" },
  { label: "已审核", value: "audited" },
  { label: "已作废", value: "cancelled" }
])

const data = reactive({
  form: {},
  inventoryCheckItemForm: {},
  queryParams: {
    pageNum: 1,
    pageSize: 10,
    checkNo: undefined,
    warehouseId: undefined,
    status: undefined
  },
  rules: {
    warehouseId: [{ required: true, message: "仓库不能为空", trigger: "change" }]
  },
  inventoryCheckItemRules: {
    productId: [{ required: true, message: "商品不能为空", trigger: "change" }],
    locationId: [{ required: true, message: "库位不能为空", trigger: "change" }],
    batchNo: [{ required: true, message: "批次号不能为空", trigger: "blur" }],
    actualQty: [{ required: true, message: "实盘数量不能为空", trigger: "blur" }]
  }
})

const { queryParams, form, rules, inventoryCheckItemForm, inventoryCheckItemRules } = toRefs(data)

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

// 远程加载商品下拉选项，按需补充商品名称数据，减少盘点明细编辑时的识别成本。
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

// 远程加载库位下拉选项，按需补充库位名称数据，减少盘点明细编辑时的识别成本。
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

// 按商品编号补充当前页面缺失的商品名称，保证盘点明细展示和编辑回显稳定。
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

// 按库位编号补充当前页面缺失的库位名称，保证盘点明细展示和编辑回显稳定。
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

// 批量补充盘点单列表依赖的仓库名称，保证远程下拉模式下列表展示仍然可读。
function ensureInventoryCheckReferenceOptionsLoaded(inventoryCheckRowList) {
  const warehouseIdList = [...new Set((inventoryCheckRowList || []).map(inventoryCheckRow => inventoryCheckRow.warehouseId).filter(warehouseId => warehouseId !== undefined && warehouseId !== null && warehouseId !== ""))]
  Promise.all(warehouseIdList.map(warehouseId => ensureWarehouseOptionLoaded(warehouseId))).catch(() => {})
}

// 批量补充盘点明细依赖的商品和库位名称，保证远程下拉模式下明细展示仍然可读。
function ensureInventoryCheckItemReferenceOptionsLoaded(inventoryCheckItemRowList) {
  const productIdList = [...new Set((inventoryCheckItemRowList || []).map(inventoryCheckItemRow => inventoryCheckItemRow.productId).filter(productId => productId !== undefined && productId !== null && productId !== ""))]
  const locationIdList = [...new Set((inventoryCheckItemRowList || []).map(inventoryCheckItemRow => inventoryCheckItemRow.locationId).filter(locationId => locationId !== undefined && locationId !== null && locationId !== ""))]
  Promise.all([
    ...productIdList.map(productId => ensureProductOptionLoaded(productId)),
    ...locationIdList.map(locationId => ensureLocationOptionLoaded(locationId))
  ]).catch(() => {})
}

// 组合仓库下拉选项，兼容历史盘点单中仍在使用的旧仓库编号回显。
function buildWarehouseSelectOptionList() {
  return buildSelectOptionList(warehouseList.value, [queryParams.value.warehouseId, form.value.warehouseId], "warehouseId", "warehouseName", "历史仓库ID：")
}

// 组合商品下拉选项，兼容历史盘点明细中仍在使用的旧商品编号回显。
function buildProductSelectOptionList() {
  return buildSelectOptionList(productList.value, [inventoryCheckItemForm.value.productId], "productId", "productName", "历史商品ID：")
}

// 组合库位下拉选项，兼容历史盘点明细中仍在使用的旧库位编号回显。
function buildLocationSelectOptionList() {
  return buildSelectOptionList(locationList.value, [inventoryCheckItemForm.value.locationId], "locationId", "locationName", "历史库位ID：")
}

// 根据仓库编号返回仓库名称，减少盘点单列表中的纯编号展示。
function getWarehouseName(warehouseId) {
  const warehouse = warehouseList.value.find(warehouseItem => warehouseItem.warehouseId === warehouseId)
  return warehouse ? warehouse.warehouseName : (warehouseId ? `历史仓库ID：${warehouseId}` : "")
}

// 根据商品编号返回商品名称，帮助仓库人员快速识别盘点明细中的商品。
function getProductName(productId) {
  const product = productList.value.find(productItem => productItem.productId === productId)
  return product ? product.productName : (productId ? `历史商品ID：${productId}` : "")
}

// 根据库位编号返回库位名称，统一盘点明细的展示口径。
function getLocationName(locationId) {
  const location = locationList.value.find(locationItem => locationItem.locationId === locationId)
  return location ? location.locationName : (locationId ? `历史库位ID：${locationId}` : "")
}

// 判断当前盘点明细是否允许继续编辑，避免已提交单据绕过主流程修改明细。
function isInventoryCheckItemEditable() {
  return currentInventoryCheckStatus.value === "draft"
}

// 在执行盘点明细增删改前再次校验主单状态，避免非草稿单据继续维护明细。
function ensureInventoryCheckItemEditable(actionLabel) {
  if (!isInventoryCheckItemEditable())
  {
    proxy.$modal.msgWarning(`当前盘点单已提交或已审核，不能继续${actionLabel}明细`)
    return false
  }
  return true
}

function getList() {
  loading.value = true
  listInventoryCheck(queryParams.value).then(response => {
    inventoryCheckList.value = response.rows
    total.value = response.total
    ensureInventoryCheckReferenceOptionsLoaded(response.rows)
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
    checkId: undefined,
    checkNo: undefined,
    warehouseId: undefined,
    status: "draft",
    totalDiffQty: 0,
    totalDiffAmount: 0,
    auditBy: undefined,
    auditTime: undefined,
    remark: undefined
  }
  proxy.resetForm("inventoryCheckRef")
}

// 初始化盘点明细表单，默认带入当前盘点单编号，减少仓库用户重复录入成本。
function resetInventoryCheckItemForm() {
  inventoryCheckItemForm.value = {
    checkItemId: undefined,
    checkId: currentCheckId.value,
    productId: undefined,
    locationId: undefined,
    batchNo: undefined,
    stockQty: 0,
    actualQty: 0,
    diffQty: 0,
    price: 0,
    diffAmount: 0
  }
  proxy.resetForm("inventoryCheckItemRef")
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
  ids.value = selection.map(item => item.checkId)
  single.value = selection.length !== 1
  multiple.value = !selection.length
  canUpdateSelected.value = selection.length === 1 && isDraftStatus(selection[0].status)
  canSubmitSelected.value = selection.length === 1 && isDraftStatus(selection[0].status)
  canAuditSelected.value = selection.length === 1 && isSubmittedStatus(selection[0].status)
  canDeleteSelected.value = selection.length > 0 && selection.every(item => isDraftStatus(item.status))
}

function handleAdd() {
  reset()
  open.value = true
  title.value = "新增库存盘点单"
}

// 打开盘点单编辑弹窗时补充仓库名称，并保持状态与审核人只读展示。
function handleUpdate(row) {
  reset()
  const checkId = row.checkId || ids.value[0]
  getInventoryCheck(checkId).then(response => {
    form.value = response.data
    ensureWarehouseOptionLoaded(response.data.warehouseId)
    open.value = true
    title.value = "修改库存盘点单"
  })
}

// 打开盘点明细弹窗时同步主单状态，后续明细的增删改权限都以主单状态为准。
function handleOpenInventoryCheckItem(row) {
  currentCheckId.value = row.checkId
  currentInventoryCheckStatus.value = row.status
  inventoryCheckItemTitle.value = "库存盘点明细 - " + row.checkNo
  inventoryCheckItemOpen.value = true
  selectedInventoryCheckItemIds.value = []
  inventoryCheckItemMultiple.value = true
  getInventoryCheckItemList()
}

function getInventoryCheckItemList() {
  if (!currentCheckId.value) {
    inventoryCheckItemList.value = []
    return
  }
  inventoryCheckItemLoading.value = true
  listInventoryCheckItem({
    pageNum: 1,
    pageSize: 1000,
    checkId: currentCheckId.value
  }).then(response => {
    inventoryCheckItemList.value = response.rows
    ensureInventoryCheckItemReferenceOptionsLoaded(response.rows)
  }).catch(() => {
  }).finally(() => {
    inventoryCheckItemLoading.value = false
  })
}

function handleInventoryCheckItemSelectionChange(selection) {
  selectedInventoryCheckItemIds.value = selection.map(item => item.checkItemId)
  inventoryCheckItemMultiple.value = !selection.length
}

function handleAddInventoryCheckItem() {
  if (!ensureInventoryCheckItemEditable("新增")) {
    return
  }
  resetInventoryCheckItemForm()
  inventoryCheckItemFormOpen.value = true
  inventoryCheckItemFormTitle.value = "新增库存盘点明细"
}

function handleUpdateInventoryCheckItem(row) {
  if (!ensureInventoryCheckItemEditable("修改")) {
    return
  }
  resetInventoryCheckItemForm()
  const checkItemId = row.checkItemId
  getInventoryCheckItem(checkItemId).then(response => {
    inventoryCheckItemForm.value = response.data
    ensureProductOptionLoaded(response.data.productId)
    ensureLocationOptionLoaded(response.data.locationId)
    inventoryCheckItemFormOpen.value = true
    inventoryCheckItemFormTitle.value = "修改库存盘点明细"
  })
}

function cancelInventoryCheckItemForm() {
  inventoryCheckItemFormOpen.value = false
  resetInventoryCheckItemForm()
}

function submitInventoryCheckItemForm() {
  if (!ensureInventoryCheckItemEditable("保存")) {
    return
  }
  proxy.$refs["inventoryCheckItemRef"].validate(valid => {
    if (valid) {
      inventoryCheckItemForm.value.checkId = currentCheckId.value
      if (inventoryCheckItemForm.value.checkItemId !== undefined) {
        updateInventoryCheckItem(inventoryCheckItemForm.value).then(() => {
          proxy.$modal.msgSuccess("修改成功")
          inventoryCheckItemFormOpen.value = false
          getInventoryCheckItemList()
          getList()
        })
      } else {
        addInventoryCheckItem(inventoryCheckItemForm.value).then(() => {
          proxy.$modal.msgSuccess("新增成功")
          inventoryCheckItemFormOpen.value = false
          getInventoryCheckItemList()
          getList()
        })
      }
    }
  })
}

function handleDeleteInventoryCheckItem(row) {
  if (!ensureInventoryCheckItemEditable("删除")) {
    return
  }
  const checkItemIds = row ? row.checkItemId : selectedInventoryCheckItemIds.value
  proxy.$modal.confirm('是否确认删除库存盘点明细编号为"' + checkItemIds + '"的数据项？').then(function () {
    return delInventoryCheckItem(checkItemIds)
  }).then(() => {
    proxy.$modal.msgSuccess("删除成功")
    getInventoryCheckItemList()
    getList()
  }).catch(() => {})
}

function submitForm() {
  proxy.$refs["inventoryCheckRef"].validate(valid => {
    if (valid) {
      if (form.value.checkId !== undefined) {
        updateInventoryCheck(form.value).then(() => {
          proxy.$modal.msgSuccess("修改成功")
          open.value = false
          getList()
        })
      } else {
        addInventoryCheck(form.value).then(() => {
          proxy.$modal.msgSuccess("新增成功")
          open.value = false
          getList()
        })
      }
    }
  })
}

function handleSubmit(row) {
  const checkId = row?.checkId || ids.value[0]
  proxy.$modal.confirm('是否确认提交库存盘点单编号为"' + checkId + '"的数据项？').then(function () {
    return submitInventoryCheck(checkId)
  }).then(() => {
    getList()
    proxy.$modal.msgSuccess("提交成功")
  }).catch(() => {})
}

function handleAudit(row) {
  const checkId = row?.checkId || ids.value[0]
  proxy.$modal.confirm('是否确认审核库存盘点单编号为"' + checkId + '"的数据项？').then(function () {
    return auditInventoryCheck(checkId)
  }).then(() => {
    getList()
    proxy.$modal.msgSuccess("审核成功")
  }).catch(() => {})
}

function handleCancelOrder(row) {
  const checkId = row?.checkId || ids.value[0]
  proxy.$modal.confirm('是否确认作废库存盘点单编号为"' + checkId + '"的数据项？').then(function () {
    return cancelInventoryCheck(checkId)
  }).then(() => {
    getList()
    proxy.$modal.msgSuccess("作废成功")
  }).catch(() => {})
}

function handleDelete(row) {
  const checkIds = row?.checkId || ids.value
  const displayCheckIds = Array.isArray(checkIds) ? checkIds.join("、") : checkIds
  proxy.$modal.confirm('是否确认删除库存盘点单编号为"' + displayCheckIds + '"的数据项？').then(function () {
    return delInventoryCheck(checkIds)
  }).then(() => {
    getList()
    proxy.$modal.msgSuccess("删除成功")
  }).catch(() => {})
}

function handleExport() {
  proxy.download("business/inventoryCheck/export", {
    ...queryParams.value
  }, `inventoryCheck_${new Date().getTime()}.xlsx`)
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

function isDraftStatus(statusValue) {
  return statusValue === "draft"
}

function isSubmittedStatus(statusValue) {
  return statusValue === "submitted"
}

function canCancelStatus(statusValue) {
  return statusValue === "draft" || statusValue === "submitted"
}

initBasicData()
getList()
</script>
