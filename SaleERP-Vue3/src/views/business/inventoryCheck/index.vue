<template>
  <div class="app-container erp-page">
    <erp-page-header
      eyebrow="仓储作业 · 盘点核销"
      title="库存盘点管理"
      subtitle="支持一键载入仓库库存快照、盲盘模式防舞弊与连续条码扫描点验，审核后自动盈亏调账。"
      icon="Finished"
      tone="violet"
      :badge="`共 ${total} 笔盘点单`"
    >
      <template #actions>
        <el-button icon="Download" plain @click="handleExport" v-hasPermi="['business:inventoryCheck:export']">导出</el-button>
        <el-button type="primary" icon="Plus" @click="handleAdd" v-hasPermi="['business:inventoryCheck:add']">新建盘点单</el-button>
      </template>
    </erp-page-header>

    <erp-stat-strip :items="checkStatItems" :columns="4" />

    <section class="erp-filter-card" v-show="showSearch">
      <el-form :model="queryParams" ref="queryRef" :inline="true">
        <el-form-item label="盘点单号" prop="checkNo">
          <el-input v-model="queryParams.checkNo" placeholder="请输入盘点单号" clearable style="width: 195px" @keyup.enter="handleQuery" />
        </el-form-item>
        <el-form-item label="仓库" prop="warehouseId">
          <el-select v-model="queryParams.warehouseId" placeholder="请选择仓库" clearable filterable remote reserve-keyword remote-show-suffix :remote-method="loadWarehouseOptionList" :loading="warehouseListLoading" style="width: 195px">
            <el-option v-for="warehouse in warehouseSelectOptionList" :key="warehouse.warehouseId" :label="warehouse.warehouseName" :value="warehouse.warehouseId" />
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
          <erp-status-tabs :model-value="queryParams.status ?? ''" :items="checkStatusTabItems" @change="handleQuickStatus" />
        </div>
        <div class="erp-toolbar-right">
          <el-button type="success" plain icon="Edit" :disabled="!canUpdateSelected" @click="handleUpdate" v-hasPermi="['business:inventoryCheck:edit']">修改</el-button>
          <el-button type="info" plain icon="Upload" :disabled="!canSubmitSelected" @click="handleSubmit" v-hasPermi="['business:inventoryCheck:edit']">提交</el-button>
          <el-button type="warning" plain icon="CircleCheck" :disabled="!canAuditSelected" @click="handleAudit" v-hasPermi="['business:inventoryCheck:audit']">审核</el-button>
          <el-button type="danger" plain icon="Delete" :disabled="!canDeleteSelected" @click="handleDelete" v-hasPermi="['business:inventoryCheck:remove']">删除</el-button>
          <right-toolbar v-model:showSearch="showSearch" @queryTable="getList" />
        </div>
      </div>

      <el-table v-loading="loading" :data="inventoryCheckList" @selection-change="handleSelectionChange">
        <el-table-column type="selection" width="50" align="center" />
        <el-table-column label="盘点单号" align="center" prop="checkNo" min-width="180">
          <template #default="scope">
            <span class="erp-code-chip">{{ scope.row.checkNo }}</span>
          </template>
        </el-table-column>
        <el-table-column label="所属仓库" align="left" prop="warehouseId" min-width="170">
          <template #default="scope">
            <div class="erp-cell-title">{{ scope.row.warehouseName || getWarehouseName(scope.row.warehouseId) }}</div>
            <div class="erp-cell-sub">编号 #{{ scope.row.checkId }}</div>
          </template>
        </el-table-column>
        <el-table-column label="盈亏数量" align="right" prop="totalDiffQty" width="105">
          <template #default="scope">
            <span class="erp-amount" :class="Number(scope.row.totalDiffQty || 0) > 0 ? 'is-positive' : (Number(scope.row.totalDiffQty || 0) < 0 ? 'is-danger' : '')">
              {{ scope.row.totalDiffQty }}
            </span>
          </template>
        </el-table-column>
        <el-table-column label="盈亏金额" align="right" prop="totalDiffAmount" width="120">
          <template #default="scope">
            <span class="erp-amount" :class="Number(scope.row.totalDiffAmount || 0) > 0 ? 'is-positive' : (Number(scope.row.totalDiffAmount || 0) < 0 ? 'is-danger' : '')">
              ¥{{ Number(scope.row.totalDiffAmount || 0).toFixed(2) }}
            </span>
          </template>
        </el-table-column>
        <el-table-column label="状态" align="center" prop="status" width="115">
          <template #default="scope">
            <el-tooltip v-if="scope.row.status === 'draft' && scope.row.rejectReason" :content="`驳回原因：${scope.row.rejectReason}`" placement="top">
              <el-tag type="danger" effect="light">已驳回待改</el-tag>
            </el-tooltip>
            <el-tag v-else :type="getStatusTagType(scope.row.status)" effect="light">{{ getStatusLabel(scope.row.status) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="创建时间" align="center" prop="createTime" width="160">
          <template #default="scope">
            <span>{{ parseTime(scope.row.createTime) }}</span>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="250" fixed="right" align="center">
          <template #default="scope">
            <el-button link type="primary" icon="List" @click="handleOpenInventoryCheckItem(scope.row)" v-hasPermi="['business:inventoryCheck:query']">明细</el-button>
            <el-button link type="primary" icon="Printer" @click="handlePrintCheck(scope.row)">打印</el-button>
            <el-button link type="primary" icon="Edit" :disabled="!isDraftStatus(scope.row.status)" @click="handleUpdate(scope.row)" v-hasPermi="['business:inventoryCheck:edit']">修改</el-button>
            <el-button link type="primary" icon="Upload" :disabled="!isDraftStatus(scope.row.status)" @click="handleSubmit(scope.row)" v-hasPermi="['business:inventoryCheck:edit']">提交</el-button>
            <el-button link type="success" icon="CircleCheck" :disabled="!isSubmittedStatus(scope.row.status)" @click="handleAudit(scope.row)" v-hasPermi="['business:inventoryCheck:audit']">审核</el-button>
            <el-button v-if="isSubmittedStatus(scope.row.status)" link type="warning" icon="RefreshLeft" @click="handleReject(scope.row)" v-hasPermi="['business:inventoryCheck:audit']">驳回</el-button>
            <el-button link type="danger" icon="Delete" :disabled="!isDraftStatus(scope.row.status)" @click="handleDelete(scope.row)" v-hasPermi="['business:inventoryCheck:remove']">删除</el-button>
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

    <el-dialog :title="inventoryCheckItemTitle" v-model="inventoryCheckItemOpen" width="1160px" append-to-body>
      <el-alert
        v-if="!isInventoryCheckItemEditable()"
        title="当前盘点单已提交或已审核，明细仅支持查看，不支持继续增删改。"
        type="info"
        :closable="false"
        class="mb8"
      />
      <el-row :gutter="10" class="mb8" align="middle">
        <el-col :span="1.5" v-if="isInventoryCheckItemEditable()">
          <el-button type="success" plain icon="MagicStick" :loading="snapshotLoading" @click="handleLoadWarehouseSnapshot" v-hasPermi="['business:inventoryCheck:edit']">一键载入库存</el-button>
        </el-col>
        <el-col :span="1.5">
          <el-button v-if="isInventoryCheckItemEditable()" type="primary" plain icon="Plus" @click="handleAddInventoryCheckItem" v-hasPermi="['business:inventoryCheck:add']">新增明细</el-button>
        </el-col>
        <el-col :span="1.5">
          <el-button v-if="isInventoryCheckItemEditable()" type="danger" plain icon="Delete" :disabled="inventoryCheckItemMultiple" @click="handleDeleteInventoryCheckItem" v-hasPermi="['business:inventoryCheck:remove']">删除明细</el-button>
        </el-col>
        <el-col :span="10" v-if="isInventoryCheckItemEditable()">
          <el-input
            v-model="checkScanCode"
            placeholder="扫码或输入商品编码按回车（自动实盘 +1）"
            clearable
            style="width: 100%"
            @keyup.enter="handleCheckBarcodeScan"
          >
            <template #append>
              <el-button icon="FullScreen" :loading="checkScanLoading" @click="handleCheckBarcodeScan">扫码盘点</el-button>
            </template>
          </el-input>
        </el-col>
        <el-col :span="1.5">
          <el-switch v-model="blindCheckMode" active-text="盲盘模式" />
        </el-col>
      </el-row>
      <el-table v-loading="inventoryCheckItemLoading" :data="inventoryCheckItemList" @selection-change="handleInventoryCheckItemSelectionChange">
        <el-table-column v-if="isInventoryCheckItemEditable()" type="selection" width="44" align="center" />
        <el-table-column label="编号" align="center" prop="checkItemId" width="68" />
        <el-table-column label="商品" align="left" prop="productId" min-width="145" show-overflow-tooltip>
          <template #default="scope">
            <span>{{ getProductName(scope.row.productId) }}</span>
          </template>
        </el-table-column>
        <el-table-column label="库位" align="left" prop="locationId" min-width="115" show-overflow-tooltip>
          <template #default="scope">
            <span>{{ getLocationName(scope.row.locationId) }}</span>
          </template>
        </el-table-column>
        <el-table-column label="批次号" align="center" prop="batchNo" min-width="102" show-overflow-tooltip />
        <el-table-column label="账面数" align="right" prop="stockQty" width="76">
          <template #default="scope">
            <span>{{ blindCheckMode ? '***' : scope.row.stockQty }}</span>
          </template>
        </el-table-column>
        <el-table-column label="实盘数" align="right" prop="actualQty" width="76" />
        <el-table-column label="差异数" align="right" prop="diffQty" width="76">
          <template #default="scope">
            <span>{{ blindCheckMode ? '***' : scope.row.diffQty }}</span>
          </template>
        </el-table-column>
        <el-table-column label="单价" align="right" prop="price" width="76" />
        <el-table-column label="差异金额" align="right" prop="diffAmount" width="90">
          <template #default="scope">
            <span>{{ blindCheckMode ? '***' : scope.row.diffAmount }}</span>
          </template>
        </el-table-column>
        <el-table-column v-if="isInventoryCheckItemEditable()" label="操作" width="140" fixed="right" align="center">
          <template #default="scope">
            <div class="erp-row-actions">
              <el-button link type="primary" icon="Edit" @click="handleUpdateInventoryCheckItem(scope.row)" v-hasPermi="['business:inventoryCheck:edit']">修改</el-button>
              <el-button link type="danger" icon="Delete" @click="handleDeleteInventoryCheckItem(scope.row)" v-hasPermi="['business:inventoryCheck:remove']">删除</el-button>
            </div>
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
              <el-input-number v-model="inventoryCheckItemForm.stockQty" :min="0" :precision="2" controls-position="right" style="width: 100%" @change="calculateInventoryCheckItemDiffFields" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-row>
          <el-col :span="12">
            <el-form-item label="实盘数量" prop="actualQty">
              <el-input-number v-model="inventoryCheckItemForm.actualQty" :min="0" :precision="2" controls-position="right" style="width: 100%" @change="calculateInventoryCheckItemDiffFields" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="差异数量" prop="diffQty">
              <el-input-number v-model="inventoryCheckItemForm.diffQty" :precision="2" controls-position="right" style="width: 100%" :disabled="true" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-row>
          <el-col :span="12">
            <el-form-item label="单价" prop="price">
              <el-input-number v-model="inventoryCheckItemForm.price" :min="0" :precision="2" controls-position="right" style="width: 100%" @change="calculateInventoryCheckItemDiffFields" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="差异金额" prop="diffAmount">
              <el-input-number v-model="inventoryCheckItemForm.diffAmount" :precision="2" controls-position="right" style="width: 100%" :disabled="true" />
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

    <document-print-dialog ref="printDialogRef" />
  </div>
</template>

<script setup name="BusinessInventoryCheck">
import DocumentPrintDialog from "@/components/DocumentPrintDialog/index.vue"
import { listInventoryCheck, getInventoryCheck, addInventoryCheck, updateInventoryCheck, delInventoryCheck, submitInventoryCheck, auditInventoryCheck, rejectInventoryCheck, loadInventoryCheckStockSnapshot, cancelInventoryCheck } from "@/api/business/inventoryCheck"
import { listInventoryCheckItem, getInventoryCheckItem, addInventoryCheckItem, updateInventoryCheckItem, delInventoryCheckItem } from "@/api/business/inventoryCheckItem"
import { listWarehouse, getWarehouse } from "@/api/business/warehouse"
import { listProduct, getProduct, scanProductByCode } from "@/api/business/product"
import { listLocation, getLocation } from "@/api/business/location"
import { appendUniqueSelectOption, buildSelectOptionList, normalizeRemoteKeyword } from "@/utils/remoteSelect"
import { parseTime } from "@/utils/ruoyi"

const { proxy } = getCurrentInstance()
const route = useRoute()

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
const printDialogRef = ref(null)
const blindCheckMode = ref(false)
const snapshotLoading = ref(false)
const currentInventoryCheckStatus = ref("draft")
const selectedInventoryCheckItemIds = ref([])
const inventoryCheckItemMultiple = ref(true)
const inventoryCheckItemFormOpen = ref(false)
const inventoryCheckItemFormTitle = ref("")
const checkScanCode = ref("")
const checkScanLoading = ref(false)
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

const checkStatusTabItems = [
  { label: "全部", value: "" },
  { label: "草稿", value: "draft", dot: "slate" },
  { label: "待复核", value: "submitted", dot: "amber" },
  { label: "已审核", value: "audited", dot: "emerald" },
  { label: "已作废", value: "cancelled", dot: "rose" }
]

const checkStatItems = computed(() => {
  const rows = inventoryCheckList.value || []
  const draftCount = rows.filter(r => r.status === "draft").length
  const submittedCount = rows.filter(r => r.status === "submitted").length
  const netDiffAmt = rows.reduce((s, r) => s + Number(r.totalDiffAmount || 0), 0)
  return [
    { label: "盘点单总数", value: total.value, suffix: "单", hint: "匹配当前条件的全部盘点单", icon: "Finished", tone: "violet" },
    { label: "本页草稿盘点", value: draftCount, suffix: "单", hint: "支持快照载入与盲盘扫码", icon: "EditPen", tone: "slate" },
    { label: "本页待审核", value: submittedCount, suffix: "单", hint: "审核后自动调整库存台账", icon: "Stamp", tone: "amber" },
    { label: "本页净盈亏额", prefix: "¥", value: netDiffAmt.toFixed(2), hint: "本页差异金额净值合计", icon: "Money", tone: netDiffAmt >= 0 ? "emerald" : "rose" }
  ]
})

function handleQuickStatus(val) {
  queryParams.value.status = val === "" ? undefined : val
  handleQuery()
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
      warehouseName: "仓库资料缺失"
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
      productName: "商品资料缺失"
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
      locationName: "库位资料缺失"
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

// 组合仓库下拉选项，兼容历史盘点单中主数据缺失时的兜底回显。
function buildWarehouseSelectOptionList() {
  return buildSelectOptionList(warehouseList.value, [queryParams.value.warehouseId, form.value.warehouseId], "warehouseId", "warehouseName", () => "仓库资料缺失")
}

// 组合商品下拉选项，兼容历史盘点明细中主数据缺失时的兜底回显。
function buildProductSelectOptionList() {
  return buildSelectOptionList(productList.value, [inventoryCheckItemForm.value.productId], "productId", "productName", () => "商品资料缺失")
}

// 组合库位下拉选项，兼容历史盘点明细中主数据缺失时的兜底回显。
function buildLocationSelectOptionList() {
  return buildSelectOptionList(locationList.value, [inventoryCheckItemForm.value.locationId], "locationId", "locationName", () => "库位资料缺失")
}

// 根据仓库编号返回仓库名称，减少盘点单列表中的纯编号展示。
function getWarehouseName(warehouseId) {
  const warehouse = warehouseList.value.find(warehouseItem => warehouseItem.warehouseId === warehouseId)
  return warehouse ? warehouse.warehouseName : (warehouseId ? "仓库资料缺失" : "")
}

// 根据商品编号返回商品名称，帮助仓库人员快速识别盘点明细中的商品。
function getProductName(productId) {
  const product = productList.value.find(productItem => productItem.productId === productId)
  return product ? product.productName : (productId ? "商品资料缺失" : "")
}

// 根据库位编号返回库位名称，统一盘点明细的展示口径。
function getLocationName(locationId) {
  const location = locationList.value.find(locationItem => locationItem.locationId === locationId)
  return location ? location.locationName : (locationId ? "库位资料缺失" : "")
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

// 根据账面数量、实盘数量和单价自动回填差异字段，减少仓库人员重复心算。
function calculateInventoryCheckItemDiffFields() {
  const stockQuantityValue = Number(inventoryCheckItemForm.value.stockQty || 0)
  const actualQuantityValue = Number(inventoryCheckItemForm.value.actualQty || 0)
  const priceValue = Number(inventoryCheckItemForm.value.price || 0)
  const diffQuantityValue = Math.round((actualQuantityValue - stockQuantityValue) * 100) / 100
  inventoryCheckItemForm.value.diffQty = diffQuantityValue
  inventoryCheckItemForm.value.diffAmount = Math.round(diffQuantityValue * priceValue * 100) / 100
}

// 扫码盘点快捷匹配：存在同商品盘点明细直接实盘 +1，否则预填新增盘点明细表单。
async function handleCheckBarcodeScan() {
  if (!ensureInventoryCheckItemEditable("扫码盘点")) {
    return
  }
  const code = String(checkScanCode.value || "").trim()
  if (!code) {
    proxy.$modal.msgWarning("请扫描或输入商品条码/编码")
    return
  }
  checkScanLoading.value = true
  try {
    const res = await scanProductByCode(code)
    const product = res.data
    if (!product?.productId) return
    appendUniqueSelectOption(productList.value, product, "productId")
    const existingItem = (inventoryCheckItemList.value || []).find(item => item.productId === product.productId)
    if (existingItem) {
      const nextActualQty = Math.round((Number(existingItem.actualQty || 0) + 1) * 100) / 100
      const stockQty = Number(existingItem.stockQty || 0)
      const priceVal = Number(existingItem.price || product.costPrice || 0)
      const diffQty = Math.round((nextActualQty - stockQty) * 100) / 100
      const diffAmount = Math.round(diffQty * priceVal * 100) / 100
      await updateInventoryCheckItem({
        ...existingItem,
        actualQty: nextActualQty,
        diffQty,
        diffAmount
      })
      proxy.$modal.msgSuccess(`扫码盘点：${product.productName} 实盘 +1（当前 ${nextActualQty}）`)
      checkScanCode.value = ""
      getInventoryCheckItemList()
      getList()
    } else {
      const priceVal = Number(product.costPrice || 0)
      resetInventoryCheckItemForm()
      inventoryCheckItemForm.value.productId = product.productId
      inventoryCheckItemForm.value.stockQty = 0
      inventoryCheckItemForm.value.actualQty = 1
      inventoryCheckItemForm.value.price = priceVal
      calculateInventoryCheckItemDiffFields()
      inventoryCheckItemFormOpen.value = true
      inventoryCheckItemFormTitle.value = `扫码新增盘点明细 - ${product.productName}`
      checkScanCode.value = ""
    }
  } finally {
    checkScanLoading.value = false
  }
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
  checkScanCode.value = ""
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
  calculateInventoryCheckItemDiffFields()
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

// 返回当前要流转的盘点单编号，兼容行内操作和工具栏操作，未选中时先提示用户。
function getCurrentInventoryCheckId(row, actionText) {
  const checkId = row?.checkId || ids.value[0]
  if (!checkId) {
    proxy.$modal.msgWarning(`请选择要${actionText}的盘点单`)
    return undefined
  }
  return checkId
}

function handleSubmit(row) {
  const checkId = getCurrentInventoryCheckId(row, "提交")
  if (!checkId) {
    return
  }
  proxy.$modal.confirm('是否确认提交库存盘点单编号为"' + checkId + '"的数据项？').then(function () {
    return submitInventoryCheck(checkId)
  }).then(() => {
    getList()
    proxy.$modal.msgSuccess("提交成功")
  }).catch(() => {})
}

function handleAudit(row) {
  const checkId = getCurrentInventoryCheckId(row, "审核")
  if (!checkId) {
    return
  }
  proxy.$modal.confirm('是否确认审核库存盘点单编号为"' + checkId + '"的数据项？').then(function () {
    return auditInventoryCheck(checkId)
  }).then(() => {
    getList()
    proxy.$modal.msgSuccess("审核成功")
  }).catch(() => {})
}

function handleReject(row) {
  proxy.$modal.prompt(`确认驳回盘点单 ${row.checkNo || row.checkId} 重新复盘？请输入驳回原因：`).then(({ value }) => {
    return rejectInventoryCheck(row.checkId, value || "盘点差异待复核")
  }).then(() => {
    getList()
    proxy.$modal.msgSuccess("盘点单已驳回至草稿")
  }).catch(() => {})
}

async function handleLoadWarehouseSnapshot() {
  if (!currentCheckId.value) return
  snapshotLoading.value = true
  try {
    const res = await loadInventoryCheckStockSnapshot(currentCheckId.value)
    proxy.$modal.msgSuccess(res.msg || "成功载入仓库在库批次快照")
    getInventoryCheckItemList()
    getList()
  } finally {
    snapshotLoading.value = false
  }
}

async function handlePrintCheck(row) {
  const res = await listInventoryCheckItem({ pageNum: 1, pageSize: 1000, checkId: row.checkId })
  const items = res.rows || []
  await ensureInventoryCheckItemReferenceOptionsLoaded(items)
  const mappedItems = items.map(it => ({
    productName: getProductName(it.productId),
    locationName: getLocationName(it.locationId),
    batchNo: it.batchNo || '-',
    stockQty: it.stockQty ?? 0,
    actualQty: it.actualQty ?? 0,
    diffQty: it.diffQty ?? 0
  }))
  printDialogRef.value?.openPrint({
    docType: "inventory_check",
    title: "仓库实物盘点表",
    docNo: row.checkNo,
    defaultBlindMode: row.status === "draft",
    metaFields: [
      { label: "盘点单号", value: row.checkNo },
      { label: "盘点仓库", value: row.warehouseName || getWarehouseName(row.warehouseId) },
      { label: "单据状态", value: getStatusLabel(row.status) },
      { label: "制单时间", value: parseTime(row.createTime, "{y}-{m}-{d}") },
      { label: "差异总数", value: row.totalDiffQty ?? 0 },
      { label: "审核人", value: row.auditBy || "待审" }
    ],
    columns: [
      { label: "商品名称", prop: "productName" },
      { label: "库位", prop: "locationName", width: "130px" },
      { label: "批次号", prop: "batchNo", width: "110px" },
      { label: "账面数", prop: "stockQty", width: "90px", align: "right" },
      { label: "实盘数", prop: "actualQty", width: "90px", align: "right" },
      { label: "盈亏差异", prop: "diffQty", width: "90px", align: "right" }
    ],
    items: mappedItems,
    totalQty: mappedItems.reduce((s, r) => s + Number(r.actualQty || 0), 0),
    remark: row.remark
  })
}

function handleCancelOrder(row) {
  const checkId = getCurrentInventoryCheckId(row, "作废")
  if (!checkId) {
    return
  }
  proxy.$modal.confirm('是否确认作废库存盘点单编号为"' + checkId + '"的数据项？').then(function () {
    return cancelInventoryCheck(checkId)
  }).then(() => {
    getList()
    proxy.$modal.msgSuccess("作废成功")
  }).catch(() => {})
}

// 删除库存盘点单，兼容单条删除和批量删除，并在空选择时及时提醒用户。
function handleDelete(row) {
  const inventoryCheckIdList = row?.checkId ? [row.checkId] : ids.value
  if (!inventoryCheckIdList.length) {
    proxy.$modal.msgWarning("请选择要删除的盘点单")
    return
  }
  const displayCheckIds = inventoryCheckIdList.join("、")
  proxy.$modal.confirm('是否确认删除库存盘点单编号为"' + displayCheckIds + '"的数据项？').then(function () {
    return delInventoryCheck(inventoryCheckIdList)
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

// 判断当前是否来自首页一键盘点库存入口。
function shouldAutoOpenInventoryCheckFromRoute() {
  return route.query.mode === "create"
}

// 初始化页面基础数据和列表，并在需要时自动打开新建盘点单弹窗。
function initializePage() {
  initBasicData()
  getList()
  if (shouldAutoOpenInventoryCheckFromRoute()) {
    handleAdd()
  }
}

// 监听路由参数变化，保证首页重复进入盘点页时新增入口会重新生效。
watch(() => route.fullPath, (currentRouteFullPath, previousRouteFullPath) => {
  if (currentRouteFullPath === previousRouteFullPath) {
    return
  }
  initializePage()
})

// 监听盘点明细关键数字字段变化，实时展示差异数量和差异金额。
watch(
  () => [inventoryCheckItemForm.value.stockQty, inventoryCheckItemForm.value.actualQty, inventoryCheckItemForm.value.price],
  () => {
    calculateInventoryCheckItemDiffFields()
  }
)

initializePage()
</script>
