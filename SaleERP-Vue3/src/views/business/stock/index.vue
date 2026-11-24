<template>
  <div class="app-container erp-page">
    <erp-page-header
      eyebrow="仓储实时监控"
      title="库存实时台账"
      subtitle="实时洞察各仓库位与批次实物数、销售预占锁定及可用余额，支持上下限预警、效期监控与期初批量建账。"
      icon="Box"
      tone="blue"
      :badge="`共 ${total} 条批次`"
    >
      <template #actions>
        <el-button icon="Upload" plain @click="handleOpenOpeningImport" v-hasPermi="['business:stock:add']">期初导入</el-button>
        <el-button icon="AlarmClock" plain @click="handleWarningRemind" v-hasPermi="['business:stock:list']">触发预警</el-button>
        <el-button type="danger" plain icon="Bell" @click="handleReadWarningMessage" v-hasPermi="['business:stock:list']">
          未读预警 ({{ warningUnreadCount }})
        </el-button>
        <el-button type="primary" icon="Download" @click="handleExport" v-hasPermi="['business:stock:export']">导出</el-button>
      </template>
    </erp-page-header>

    <erp-stat-strip :items="stockStatItems" :columns="4" />

    <section class="erp-filter-card" v-show="showSearch">
      <el-form :model="queryParams" ref="queryRef" :inline="true">
        <el-form-item label="仓库" prop="warehouseId">
          <el-select v-model="queryParams.warehouseId" placeholder="请选择仓库" clearable filterable remote reserve-keyword remote-show-suffix :remote-method="loadWarehouseOptionList" :loading="warehouseListLoading" style="width: 195px">
            <el-option v-for="warehouse in warehouseSelectOptionList" :key="warehouse.warehouseId" :label="warehouse.warehouseName" :value="warehouse.warehouseId" />
          </el-select>
        </el-form-item>
        <el-form-item label="商品" prop="productId">
          <el-select v-model="queryParams.productId" placeholder="请选择商品" clearable filterable remote reserve-keyword remote-show-suffix :remote-method="loadProductOptionList" :loading="productListLoading" style="width: 195px">
            <el-option v-for="product in productSelectOptionList" :key="product.productId" :label="product.productName" :value="product.productId" />
          </el-select>
        </el-form-item>
        <el-form-item label="库位" prop="locationId">
          <el-select v-model="queryParams.locationId" placeholder="请选择库位" clearable filterable remote reserve-keyword remote-show-suffix :remote-method="loadLocationOptionList" :loading="locationListLoading" style="width: 175px">
            <el-option v-for="location in locationSelectOptionList" :key="location.locationId" :label="location.locationName" :value="location.locationId" />
          </el-select>
        </el-form-item>
        <el-form-item label="批次号" prop="batchNo">
          <el-input v-model="queryParams.batchNo" placeholder="请输入批次号" clearable style="width: 175px" @keyup.enter="handleQuery" />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" icon="Search" @click="handleQuery">搜索</el-button>
          <el-button icon="Refresh" @click="resetQuery">重置</el-button>
        </el-form-item>
      </el-form>
    </section>

    <el-alert v-if="isWarningQuery" title="当前仅展示触发库存预警的记录，方便仓库优先处理缺货或积压库存。" type="warning" :closable="false" />
    <el-alert v-if="isExpiringQuery" title="当前仅展示 30 天内到期或已过期的临期批次库存，请优先安排 FEFO 先进先出。" type="error" :closable="false" />

    <section class="erp-table-card">
      <div class="erp-toolbar">
        <div class="erp-toolbar-left">
          <erp-status-tabs :model-value="stockViewMode" :items="stockViewTabItems" @change="handleStockViewModeChange" />
        </div>
        <div class="erp-toolbar-right">
          <right-toolbar v-model:showSearch="showSearch" @queryTable="getList" />
        </div>
      </div>

      <el-table v-loading="loading" :data="stockList">
        <el-table-column label="商品 / 批次" align="left" prop="productId" min-width="195" show-overflow-tooltip>
          <template #default="scope">
            <div class="erp-cell-title">{{ scope.row.productName || getProductName(scope.row.productId) }}</div>
            <div class="erp-cell-sub">批次：{{ scope.row.batchNo || '-' }}</div>
          </template>
        </el-table-column>
        <el-table-column label="仓库 / 库位" align="left" min-width="175" show-overflow-tooltip>
          <template #default="scope">
            <div class="erp-cell-title">{{ scope.row.warehouseName || getWarehouseName(scope.row.warehouseId) }}</div>
            <div class="erp-cell-sub">{{ scope.row.locationName || getLocationName(scope.row.locationId) }}</div>
          </template>
        </el-table-column>
        <el-table-column label="效期 / 临期状态" align="center" min-width="155">
          <template #default="scope">
            <template v-if="scope.row.expireDate">
              <span class="erp-num">{{ parseTime(scope.row.expireDate, '{y}-{m}-{d}') }}</span>
              <el-tag v-if="getExpireTagInfo(scope.row.expireDate)" :type="getExpireTagInfo(scope.row.expireDate).type" size="small" style="margin-left: 6px">
                {{ getExpireTagInfo(scope.row.expireDate).label }}
              </el-tag>
            </template>
            <span v-else class="erp-cell-sub">长期有效</span>
          </template>
        </el-table-column>
        <el-table-column label="实物库存" align="right" prop="quantity" width="95">
          <template #default="scope">
            <span class="erp-amount">{{ scope.row.quantity }}</span>
          </template>
        </el-table-column>
        <el-table-column label="销售锁定" align="right" prop="lockedQuantity" width="95">
          <template #default="scope">
            <span :class="{ 'erp-amount is-warning': Number(scope.row.lockedQuantity || 0) > 0 }">
              {{ scope.row.lockedQuantity ?? 0 }}
            </span>
          </template>
        </el-table-column>
        <el-table-column label="可用库存" align="right" width="95">
          <template #default="scope">
            <span class="erp-amount is-primary">
              {{ (Number(scope.row.quantity || 0) - Number(scope.row.lockedQuantity || 0) - Number(scope.row.frozenQuantity || 0)).toFixed(2).replace(/\.00$/, '') }}
            </span>
          </template>
        </el-table-column>
        <el-table-column label="冻结数" align="right" prop="frozenQuantity" width="84" />
        <el-table-column label="预警状态" align="center" width="110">
          <template #default="scope">
            <el-tag :type="getStockWarningStatusType(scope.row)" effect="light">{{ getStockWarningStatusLabel(scope.row) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="安全区间" align="center" width="115">
          <template #default="scope">
            <span class="erp-cell-sub">{{ scope.row.warningMinQty ?? '-' }} ~ {{ scope.row.warningMaxQty ?? '-' }}</span>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="275" fixed="right" align="center">
          <template #default="scope">
            <el-button
              v-if="Number(scope.row.quantity || 0) - Number(scope.row.lockedQuantity || 0) - Number(scope.row.frozenQuantity || 0) > 0"
              link type="warning" icon="Lock"
              @click="handleOpenFreezeDialog(scope.row, 'freeze')"
              v-hasPermi="['business:stock:freeze', 'business:stock:edit']"
            >冻结</el-button>
            <el-button
              v-if="Number(scope.row.frozenQuantity || 0) > 0"
              link type="success" icon="Unlock"
              @click="handleOpenFreezeDialog(scope.row, 'unfreeze')"
              v-hasPermi="['business:stock:freeze', 'business:stock:edit']"
            >解冻</el-button>
            <el-button link type="primary" icon="Setting" @click="handleOpenThresholdDialog(scope.row)" v-hasPermi="['business:stock:edit']">预警阈值</el-button>
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
    </section>

    <!-- 库存预警阈值配置对话框 -->
    <el-dialog v-model="thresholdOpen" title="设置库存预警阈值" width="480px" append-to-body>
      <el-form :model="thresholdForm" label-width="110px">
        <el-form-item label="仓库">
          <span>{{ thresholdForm.warehouseName }}</span>
        </el-form-item>
        <el-form-item label="商品">
          <span>{{ thresholdForm.productName }}</span>
        </el-form-item>
        <el-form-item label="库位 / 批次">
          <span>{{ thresholdForm.locationName }} / {{ thresholdForm.batchNo || '-' }}</span>
        </el-form-item>
        <el-form-item label="当前库存数量">
          <el-tag type="info">{{ thresholdForm.quantity }}</el-tag>
        </el-form-item>
        <el-form-item label="最小预警值">
          <el-input-number v-model="thresholdForm.warningMinQty" :min="0" :precision="2" controls-position="right" style="width: 100%" placeholder="低于此值触发缺货预警" />
        </el-form-item>
        <el-form-item label="最大预警值">
          <el-input-number v-model="thresholdForm.warningMaxQty" :min="0" :precision="2" controls-position="right" style="width: 100%" placeholder="高于此值触发积压预警" />
        </el-form-item>
        <el-form-item label="批次效期到期日">
          <el-date-picker
            v-model="thresholdForm.expireDate"
            type="date"
            value-format="YYYY-MM-DD"
            placeholder="可选，设置批次效期以便临期预警"
            clearable
            style="width: 100%"
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <div class="dialog-footer">
          <el-button type="primary" :loading="thresholdSaving" @click="submitThresholdForm">确 定</el-button>
          <el-button @click="thresholdOpen = false">取 消</el-button>
        </div>
      </template>
    </el-dialog>

    <!-- 期初库存导入对话框 -->
    <el-dialog v-model="openingImport.open" title="期初库存批量建账导入" width="440px" append-to-body>
      <el-upload
        ref="openingUploadRef"
        :limit="1"
        accept=".xlsx, .xls"
        :headers="openingImport.headers"
        :action="openingImport.url"
        :disabled="openingImport.isUploading"
        :on-progress="() => (openingImport.isUploading = true)"
        :on-success="handleOpeningImportSuccess"
        :auto-upload="false"
        drag
      >
        <el-icon class="el-icon--upload"><upload-filled /></el-icon>
        <div class="el-upload__text">将期初库存 Excel 拖到此处，或<em>点击上传</em></div>
        <template #tip>
          <div class="el-upload__tip text-center">
            <span>导入后自动写入期初库存批次及变动流水，仅支持 xls / xlsx。</span>
            <el-link type="primary" :underline="false" style="font-size: 12px; margin-left: 6px" @click="handleDownloadOpeningTemplate">下载模板</el-link>
          </div>
        </template>
      </el-upload>
      <template #footer>
        <div class="dialog-footer">
          <el-button type="primary" :loading="openingImport.isUploading" @click="submitOpeningUpload">导 入</el-button>
          <el-button @click="openingImport.open = false">取 消</el-button>
        </div>
      </template>
    </el-dialog>

    <!-- 库存质检冻结 / 解冻对话框 -->
    <el-dialog v-model="freezeDialog.open" :title="freezeDialog.mode === 'freeze' ? '库存冻结（质检/残损隔离）' : '库存解冻（释放可用库存）'" width="460px" append-to-body>
      <el-form :model="freezeDialog" label-width="105px">
        <el-form-item label="商品 / 批次">
          <span>{{ freezeDialog.productName }}（{{ freezeDialog.batchNo || '无批次' }}）</span>
        </el-form-item>
        <el-form-item label="仓库 / 库位">
          <span>{{ freezeDialog.warehouseName }} / {{ freezeDialog.locationName || '-' }}</span>
        </el-form-item>
        <el-form-item :label="freezeDialog.mode === 'freeze' ? '当前可冻结' : '当前已冻结'">
          <el-tag :type="freezeDialog.mode === 'freeze' ? 'warning' : 'success'">{{ freezeDialog.maxQuantity }}</el-tag>
        </el-form-item>
        <el-form-item :label="freezeDialog.mode === 'freeze' ? '本次冻结数' : '本次解冻数'" required>
          <el-input-number v-model="freezeDialog.quantity" :min="0.01" :max="freezeDialog.maxQuantity" :precision="2" controls-position="right" style="width: 100%" />
        </el-form-item>
        <el-form-item label="原因说明">
          <el-input v-model="freezeDialog.reason" type="textarea" :rows="2" :placeholder="freezeDialog.mode === 'freeze' ? '如：质检抽检隔离、包装破损待处理' : '如：质检合格放行、复检通过'" />
        </el-form-item>
      </el-form>
      <template #footer>
        <div class="dialog-footer">
          <el-button type="primary" :loading="freezeDialog.submitting" @click="submitFreezeForm">确 定</el-button>
          <el-button @click="freezeDialog.open = false">取 消</el-button>
        </div>
      </template>
    </el-dialog>

    <!-- 未读库存预警消息抽屉 -->
    <el-dialog v-model="warningDrawerOpen" title="未读库存预警消息" width="560px" append-to-body>
      <div v-if="warningMessageList.length">
        <el-table :data="warningMessageList" size="small" max-height="360">
          <el-table-column label="预警标题" prop="messageTitle" min-width="180" show-overflow-tooltip />
          <el-table-column label="预警详情" prop="messageContent" min-width="220" show-overflow-tooltip />
          <el-table-column label="时间" prop="createTime" width="145" align="center" />
        </el-table>
      </div>
      <el-empty v-else description="暂无未读库存预警消息，当前库存安全" :image-size="70" />
      <template #footer>
        <div class="dialog-footer">
          <el-button type="warning" plain @click="handleJumpToWarningFilter">查看预警库存</el-button>
          <el-button v-if="warningMessageList.length" type="primary" @click="handleConfirmReadAllWarnings">全部标记已读</el-button>
          <el-button @click="warningDrawerOpen = false">关 闭</el-button>
        </div>
      </template>
    </el-dialog>
  </div>
</template>

<script setup name="Stock">
import { listStock, listWarningStock, remindWarningStock, updateStockWarningThreshold, freezeStock, unfreezeStock } from "@/api/business/stock"
import { getUnreadMessageCount, listPopupMessage, readAllMessage } from "@/api/business/message"
import { listWarehouse, getWarehouse } from "@/api/business/warehouse"
import { listProduct, getProduct } from "@/api/business/product"
import { listLocation, getLocation } from "@/api/business/location"
import { useRouter } from 'vue-router'
import { appendUniqueSelectOption, buildSelectOptionList, normalizeRemoteKeyword } from "@/utils/remoteSelect"
import { parseTime } from "@/utils/ruoyi"
import { getToken } from "@/utils/auth"

const { proxy } = getCurrentInstance()
const route = useRoute()
const router = useRouter()

const stockList = ref([])
const loading = ref(true)
const showSearch = ref(true)
const total = ref(0)
const isWarningQuery = ref(false)
const isExpiringQuery = ref(false)
const warningUnreadCount = ref(0)
const warehouseList = ref([])
const productList = ref([])
const locationList = ref([])
const warehouseListLoading = ref(false)
const productListLoading = ref(false)
const locationListLoading = ref(false)
const thresholdOpen = ref(false)
const thresholdSaving = ref(false)
const warningDrawerOpen = ref(false)
const warningMessageList = ref([])
const freezeDialog = reactive({
  open: false,
  mode: "freeze",
  submitting: false,
  stockId: undefined,
  warehouseName: "",
  productName: "",
  locationName: "",
  batchNo: "",
  maxQuantity: 0,
  quantity: 1,
  reason: ""
})
const openingImport = reactive({
  open: false,
  isUploading: false,
  headers: { Authorization: "Bearer " + getToken() },
  url: import.meta.env.VITE_APP_BASE_API + "/business/stock/importData"
})
const thresholdForm = ref({
  stockId: undefined,
  warehouseName: "",
  productName: "",
  locationName: "",
  batchNo: "",
  quantity: 0,
  warningMinQty: undefined,
  warningMaxQty: undefined,
  expireDate: undefined
})

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

const stockViewMode = computed(() => {
  if (isExpiringQuery.value) return "expiring"
  if (isWarningQuery.value) return "warning"
  return "all"
})

const stockViewTabItems = [
  { label: "全部库存", value: "all" },
  { label: "预警库存", value: "warning", dot: "amber" },
  { label: "30天临期批次", value: "expiring", dot: "rose" }
]

const stockStatItems = computed(() => {
  const rows = stockList.value || []
  const totalPhysical = rows.reduce((s, r) => s + Number(r.quantity || 0), 0)
  const totalLocked = rows.reduce((s, r) => s + Number(r.lockedQuantity || 0), 0)
  const totalAvail = rows.reduce((s, r) => s + Math.max(0, Number(r.quantity || 0) - Number(r.lockedQuantity || 0) - Number(r.frozenQuantity || 0)), 0)
  return [
    { label: "库存批次数", value: total.value, suffix: "条", hint: "匹配当前条件的在库批次", icon: "Box", tone: "blue" },
    { label: "本页实物总数", value: totalPhysical, suffix: "件", hint: "仓库账面在库实物合计", icon: "Goods", tone: "indigo" },
    { label: "本页销售锁定", value: totalLocked, suffix: "件", hint: "已审销售单防超卖预占", icon: "Lock", tone: "amber" },
    { label: "本页可用库存", value: totalAvail, suffix: "件", hint: "当前可售/可调拨数量", icon: "CircleCheck", tone: "emerald" }
  ]
})

function handleStockViewModeChange(mode) {
  if (mode === "warning") {
    handleWarningQuery()
  } else if (mode === "expiring") {
    handleExpiringQuery()
  } else {
    handleClearWarningQuery()
  }
}

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

// 批量补充库存列表依赖的仓库、商品和库位名称（优先复用后端已关联返回的名称，消除冗余 N+1 请求）。
function ensureStockReferenceOptionsLoaded(stockRowList) {
  const warehouseIdList = []
  const productIdList = []
  const locationIdList = []
  ;(stockRowList || []).forEach(stockRow => {
    if (stockRow.warehouseId != null && stockRow.warehouseId !== "") {
      if (stockRow.warehouseName) {
        appendUniqueSelectOption(warehouseList.value, { warehouseId: stockRow.warehouseId, warehouseName: stockRow.warehouseName }, "warehouseId")
      } else {
        warehouseIdList.push(stockRow.warehouseId)
      }
    }
    if (stockRow.productId != null && stockRow.productId !== "") {
      if (stockRow.productName) {
        appendUniqueSelectOption(productList.value, { productId: stockRow.productId, productName: stockRow.productName }, "productId")
      } else {
        productIdList.push(stockRow.productId)
      }
    }
    if (stockRow.locationId != null && stockRow.locationId !== "") {
      if (stockRow.locationName) {
        appendUniqueSelectOption(locationList.value, { locationId: stockRow.locationId, locationName: stockRow.locationName }, "locationId")
      } else {
        locationIdList.push(stockRow.locationId)
      }
    }
  })
  Promise.all([
    ...[...new Set(warehouseIdList)].map(warehouseId => ensureWarehouseOptionLoaded(warehouseId)),
    ...[...new Set(productIdList)].map(productId => ensureProductOptionLoaded(productId)),
    ...[...new Set(locationIdList)].map(locationId => ensureLocationOptionLoaded(locationId))
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
  const requestParams = {
    ...queryParams.value,
    params: isExpiringQuery.value ? { expiringQuery: "1" } : {}
  }
  queryAction(requestParams).then(response => {
    stockList.value = response.rows
    total.value = response.total
    ensureStockReferenceOptionsLoaded(response.rows)
  }).finally(() => {
    loading.value = false
  })
}

function getExpireTagInfo(expireDate) {
  if (!expireDate) return null
  const exp = new Date(String(expireDate).slice(0, 10) + "T00:00:00")
  if (Number.isNaN(exp.getTime())) return null
  const today = new Date()
  today.setHours(0, 0, 0, 0)
  const diffDays = Math.ceil((exp.getTime() - today.getTime()) / (1000 * 60 * 60 * 24))
  if (diffDays < 0) return { label: "已过期", type: "danger" }
  if (diffDays <= 7) return { label: `临期${diffDays}天`, type: "danger" }
  if (diffDays <= 30) return { label: `临期${diffDays}天`, type: "warning" }
  return { label: "效期正常", type: "success" }
}

function checkWarningReminder() {
  getUnreadMessageCount({ messageType: "stock_warning" }).then(unreadResponse => {
    warningUnreadCount.value = unreadResponse.unreadCount || 0
    return listPopupMessage({ messageType: "stock_warning", limitCount: 5 })
  }).then(popupResponse => {
    const popupMessageList = popupResponse.rows || []
    if (popupMessageList.length === 0) {
      return
    }
    const popupTitleList = popupMessageList.map(messageItem => messageItem.messageTitle)
    const popupContent = popupTitleList.join("；")
    proxy.$modal.confirm(`当前存在${warningUnreadCount.value}条库存预警消息：${popupContent}。是否立即标记为已读？`).then(() => {
      return readAllMessage({ messageType: "stock_warning" })
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
  listPopupMessage({ messageType: "stock_warning", limitCount: 20 }).then(res => {
    warningMessageList.value = res.rows || []
    warningUnreadCount.value = warningMessageList.value.length
    warningDrawerOpen.value = true
  }).catch(() => {
    warningMessageList.value = []
    warningDrawerOpen.value = true
  })
}

function handleConfirmReadAllWarnings() {
  readAllMessage({ messageType: "stock_warning" }).then(() => {
    warningUnreadCount.value = 0
    warningMessageList.value = []
    warningDrawerOpen.value = false
    proxy.$modal.msgSuccess("已全部标记为已读")
  })
}

function handleJumpToWarningFilter() {
  warningDrawerOpen.value = false
  handleWarningQuery()
}

function handleOpenFreezeDialog(row, mode) {
  const avail = Math.max(0, Number(row.quantity || 0) - Number(row.lockedQuantity || 0) - Number(row.frozenQuantity || 0))
  const frozen = Math.max(0, Number(row.frozenQuantity || 0))
  const maxQty = mode === "freeze" ? avail : frozen
  if (maxQty <= 0) {
    proxy.$modal.msgWarning(mode === "freeze" ? "当前无可用库存可冻结" : "当前冻结数量为0，无需解冻")
    return
  }
  freezeDialog.mode = mode
  freezeDialog.stockId = row.stockId
  freezeDialog.warehouseName = row.warehouseName || getWarehouseName(row.warehouseId)
  freezeDialog.productName = row.productName || getProductName(row.productId)
  freezeDialog.locationName = row.locationName || getLocationName(row.locationId)
  freezeDialog.batchNo = row.batchNo || ""
  freezeDialog.maxQuantity = Number(maxQty.toFixed(2))
  freezeDialog.quantity = Number(Math.min(maxQty, 1).toFixed(2))
  freezeDialog.reason = ""
  freezeDialog.open = true
}

function submitFreezeForm() {
  if (!freezeDialog.quantity || Number(freezeDialog.quantity) <= 0) {
    proxy.$modal.msgWarning("请输入大于0的数量")
    return
  }
  freezeDialog.submitting = true
  const action = freezeDialog.mode === "freeze" ? freezeStock : unfreezeStock
  action({
    stockId: freezeDialog.stockId,
    quantity: freezeDialog.quantity,
    reason: freezeDialog.reason
  }).then(() => {
    proxy.$modal.msgSuccess(freezeDialog.mode === "freeze" ? "库存已冻结隔离" : "库存已解冻放行")
    freezeDialog.open = false
    getList()
  }).finally(() => {
    freezeDialog.submitting = false
  })
}

function handleQuery() {
  isWarningQuery.value = false
  isExpiringQuery.value = false
  queryParams.value.pageNum = 1
  getList()
}

function handleWarningQuery() {
  isWarningQuery.value = true
  isExpiringQuery.value = false
  queryParams.value.pageNum = 1
  getList()
}

function handleExpiringQuery() {
  isExpiringQuery.value = true
  isWarningQuery.value = false
  queryParams.value.pageNum = 1
  getList()
}

// 退出库存预警/临期查看模式，恢复全部库存列表。
function handleClearWarningQuery() {
  isWarningQuery.value = false
  isExpiringQuery.value = false
  queryParams.value.pageNum = 1
  getList()
}

function resetQuery() {
  proxy.resetForm("queryRef")
  queryParams.value.productId = undefined
  isWarningQuery.value = false
  isExpiringQuery.value = false
  if (Object.keys(route.query || {}).length > 0) {
    router.replace({ path: route.path, query: {} })
  }
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
  const exportParams = { ...queryParams.value }
  if (isWarningQuery.value) {
    exportParams["params[warningOnly]"] = "1"
  }
  if (isExpiringQuery.value) {
    exportParams["params[expiringQuery]"] = "1"
  }
  proxy.download("business/stock/export", exportParams, `stock_${new Date().getTime()}.xlsx`)
}

function handleOpenThresholdDialog(row) {
  thresholdForm.value = {
    stockId: row.stockId,
    warehouseName: row.warehouseName || getWarehouseName(row.warehouseId),
    productName: row.productName || getProductName(row.productId),
    locationName: row.locationName || getLocationName(row.locationId),
    batchNo: row.batchNo || "",
    quantity: row.quantity ?? 0,
    warningMinQty: row.warningMinQty !== null && row.warningMinQty !== undefined ? Number(row.warningMinQty) : undefined,
    warningMaxQty: row.warningMaxQty !== null && row.warningMaxQty !== undefined ? Number(row.warningMaxQty) : undefined,
    expireDate: row.expireDate ? parseTime(row.expireDate, "{y}-{m}-{d}") : undefined
  }
  thresholdOpen.value = true
}

function submitThresholdForm() {
  const minVal = thresholdForm.value.warningMinQty
  const maxVal = thresholdForm.value.warningMaxQty
  if (minVal !== undefined && minVal !== null && maxVal !== undefined && maxVal !== null && Number(maxVal) < Number(minVal)) {
    proxy.$modal.msgWarning("最大预警值不能小于最小预警值")
    return
  }
  thresholdSaving.value = true
  updateStockWarningThreshold({
    stockId: thresholdForm.value.stockId,
    warningMinQty: minVal ?? null,
    warningMaxQty: maxVal ?? null,
    expireDate: thresholdForm.value.expireDate || null
  }).then(() => {
    proxy.$modal.msgSuccess("预警阈值与效期已更新")
    thresholdOpen.value = false
    getList()
  }).finally(() => {
    thresholdSaving.value = false
  })
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

function handleOpenOpeningImport() {
  openingImport.open = true
}

function handleDownloadOpeningTemplate() {
  proxy.download("business/stock/importTemplate", {}, `stock_opening_template_${new Date().getTime()}.xlsx`)
}

function submitOpeningUpload() {
  proxy.$refs["openingUploadRef"].submit()
}

function handleOpeningImportSuccess(response) {
  openingImport.open = false
  openingImport.isUploading = false
  proxy.$refs["openingUploadRef"].handleRemove()
  proxy.$alert("<div style='overflow: auto;max-height: 60vh;padding: 10px 16px;'>" + (response.msg || "导入完成") + "</div>", "导入结果", { dangerouslyUseHTMLString: true })
  getList()
}

// 初始化库存页筛选状态和基础数据，并行加载基础下拉与列表数据，消除空表闪烁。
async function initializePage() {
  isWarningQuery.value = route.query.warningQuery === "1"
  isExpiringQuery.value = route.query.expiringQuery === "1"
  queryParams.value.productId = route.query.productId ? Number(route.query.productId) : undefined
  if (queryParams.value.productId) {
    ensureProductOptionLoaded(queryParams.value.productId)
  }
  await Promise.all([
    Promise.resolve(initBasicData()),
    Promise.resolve(getList())
  ])
}

onMounted(() => {
  initializePage()
  checkWarningReminder()
})

onActivated(() => {
  if (route.query.warningQuery || route.query.expiringQuery || route.query.productId) {
    initializePage()
  }
})

// 监听同一路由下的查询参数变化，避免库存页在预警筛选切换后仍显示旧列表。
watch(() => route.fullPath, (currentRouteFullPath, previousRouteFullPath) => {
  if (currentRouteFullPath === previousRouteFullPath) {
    return
  }
  initializePage()
})
</script>
