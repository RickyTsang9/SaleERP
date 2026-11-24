<template>
  <div class="app-container erp-page">
    <erp-page-header
      eyebrow="库存管理 · 库存调拨"
      title="库存调拨管理"
      subtitle="支持【草稿 → 提交待审 → 调出仓审核出库(在途) → 调入仓确认签收】两阶段调拨流程，保留原批次效期。"
      icon="Switch"
      tone="indigo"
      :badge="`共 ${total} 笔调拨单`"
    >
      <template #actions>
        <el-button icon="Download" plain @click="handleExport" v-hasPermi="['business:transfer:export']">导出</el-button>
        <el-button type="primary" icon="Plus" @click="handleAdd" v-hasPermi="['business:transfer:add']">新建调拨单</el-button>
      </template>
    </erp-page-header>

    <erp-stat-strip :items="transferStatItems" :columns="4" @select="handleStatSelect" />

    <section class="erp-filter-card" v-show="showSearch">
      <el-form :model="queryParams" ref="queryRef" :inline="true" label-width="82px">
        <el-form-item label="调拨单号" prop="transferNo">
          <el-input v-model="queryParams.transferNo" placeholder="请输入调拨单号" clearable style="width: 170px" @keyup.enter="handleQuery" />
        </el-form-item>
        <el-form-item label="调出仓库" prop="outWarehouseId">
          <el-select v-model="queryParams.outWarehouseId" placeholder="请选择调出仓" clearable filterable remote reserve-keyword remote-show-suffix :remote-method="loadWarehouseOptionList" :loading="warehouseListLoading" style="width: 170px">
            <el-option v-for="warehouse in warehouseSelectOptionList" :key="warehouse.warehouseId" :label="warehouse.warehouseName" :value="warehouse.warehouseId" />
          </el-select>
        </el-form-item>
        <el-form-item label="调入仓库" prop="inWarehouseId">
          <el-select v-model="queryParams.inWarehouseId" placeholder="请选择调入仓" clearable filterable remote reserve-keyword remote-show-suffix :remote-method="loadWarehouseOptionList" :loading="warehouseListLoading" style="width: 170px">
            <el-option v-for="warehouse in warehouseSelectOptionList" :key="warehouse.warehouseId" :label="warehouse.warehouseName" :value="warehouse.warehouseId" />
          </el-select>
        </el-form-item>
        <el-form-item label="调拨日期">
          <el-date-picker
            v-model="dateRange"
            value-format="YYYY-MM-DD"
            type="daterange"
            range-separator="-"
            start-placeholder="开始日期"
            end-placeholder="结束日期"
            :shortcuts="ERP_DATE_SHORTCUTS"
            style="width: 240px"
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
          <erp-status-tabs :model-value="queryParams.status ?? ''" :items="transferStatusTabItems" @change="handleQuickStatus" />
        </div>
        <div class="erp-toolbar-right">
          <el-button type="success" plain icon="Edit" :disabled="!canUpdateSelected" @click="handleUpdate()" v-hasPermi="['business:transfer:edit']">修改</el-button>
          <el-button type="info" plain icon="Upload" :disabled="!canSubmitSelected" @click="handleSubmitTransfer()" v-hasPermi="['business:transfer:edit']">提交</el-button>
          <el-button type="warning" plain icon="CircleCheck" :disabled="!canAuditSelected" @click="handleAudit()" v-hasPermi="['business:transfer:audit']">审核</el-button>
          <el-button type="danger" plain icon="Delete" :disabled="!canDeleteSelected" @click="handleDelete()" v-hasPermi="['business:transfer:remove']">删除</el-button>
          <right-toolbar v-model:showSearch="showSearch" @queryTable="getList" />
        </div>
      </div>

      <el-table v-loading="loading" :data="transferList" @selection-change="handleSelectionChange">
        <el-table-column type="selection" width="46" align="center" />
        <el-table-column label="调拨单号" align="center" prop="transferNo" min-width="150" show-overflow-tooltip>
          <template #default="scope">
            <span class="erp-code-chip">{{ scope.row.transferNo }}</span>
          </template>
        </el-table-column>
        <el-table-column label="调拨路径（调出 → 调入）" align="left" min-width="195" show-overflow-tooltip>
          <template #default="scope">
            <div class="erp-cell-title">
              {{ scope.row.outWarehouseName || getWarehouseName(scope.row.outWarehouseId) }} → {{ scope.row.inWarehouseName || getWarehouseName(scope.row.inWarehouseId) }}
            </div>
            <div class="erp-cell-sub">调拨日期：{{ parseTime(scope.row.transferDate, '{y}-{m}-{d}') || '-' }}</div>
          </template>
        </el-table-column>
        <el-table-column label="调拨数量" align="right" prop="totalQuantity" width="90">
          <template #default="scope">
            <span class="erp-amount is-primary">{{ scope.row.totalQuantity }}</span>
          </template>
        </el-table-column>
        <el-table-column label="状态" align="center" prop="status" width="104">
          <template #default="scope">
            <el-tooltip v-if="scope.row.status === 'draft' && scope.row.rejectReason" :content="`驳回原因：${scope.row.rejectReason}`" placement="top">
              <el-tag type="danger" effect="light">已驳回待改</el-tag>
            </el-tooltip>
            <el-tag v-else :type="getTransferStatusType(scope.row.status)" effect="light">{{ getTransferStatusName(scope.row.status) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="245" fixed="right" align="center">
          <template #default="scope">
            <div class="erp-row-actions">
              <el-button v-if="scope.row.status === '0'" link type="success" icon="CircleCheck" @click="handleAudit(scope.row)" v-hasPermi="['business:transfer:audit']">审核调出</el-button>
              <el-button v-else-if="scope.row.status === 'in_transit'" link type="success" icon="Select" @click="handleReceiveTransfer(scope.row)" v-hasPermi="['business:transfer:audit', 'business:transfer:edit']">签收</el-button>
              <el-button v-else-if="scope.row.status === 'draft'" link type="primary" icon="Upload" @click="handleSubmitTransfer(scope.row)" v-hasPermi="['business:transfer:edit']">提交</el-button>
              <el-button link type="primary" icon="View" @click="handleView(scope.row)" v-hasPermi="['business:transfer:query']">详情</el-button>
              <el-button link type="info" icon="Printer" @click="handlePrintTransfer(scope.row)" v-hasPermi="['business:transfer:query']">打印</el-button>
              <el-dropdown v-if="isEditableTransferStatus(scope.row.status) || scope.row.status === '0' || scope.row.status === 'in_transit' || canCancelTransferStatus(scope.row.status)" trigger="click">
                <el-button link type="primary">更多<el-icon class="el-icon--right"><ArrowDown /></el-icon></el-button>
                <template #dropdown>
                  <el-dropdown-menu>
                    <el-dropdown-item v-if="isEditableTransferStatus(scope.row.status)" v-hasPermi="['business:transfer:edit']" @click="handleUpdate(scope.row)">修改调拨单</el-dropdown-item>
                    <el-dropdown-item v-if="scope.row.status === '0'" v-hasPermi="['business:transfer:audit']" @click="handleRejectTransfer(scope.row)">驳回至草稿</el-dropdown-item>
                    <el-dropdown-item v-if="scope.row.status === 'in_transit'" v-hasPermi="['business:transfer:audit', 'business:transfer:edit']" @click="handleRecallTransfer(scope.row)"><span style="color: var(--el-color-danger)">在途撤回</span></el-dropdown-item>
                    <el-dropdown-item v-if="canCancelTransferStatus(scope.row.status)" v-hasPermi="['business:transfer:edit']" @click="handleCancelOrder(scope.row)">作废单据</el-dropdown-item>
                    <el-dropdown-item v-if="isEditableTransferStatus(scope.row.status)" v-hasPermi="['business:transfer:remove']" divided @click="handleDelete(scope.row)"><span style="color: var(--el-color-danger)">删除草稿</span></el-dropdown-item>
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

    <el-dialog :title="title" v-model="open" width="800px" append-to-body>
      <el-alert
        :title="isTransferFormEditable() ? '调拨单保存后默认为草稿状态，请点击【提交】进入待审核；审核后调出仓扣减库存进入在途，调入仓签收后入账。' : '当前为只读查看模式，调拨信息不可编辑。'"
        type="info"
        :closable="false"
        class="mb8"
      />
      <el-form ref="transferRef" :model="form" :rules="rules" label-width="100px">
        <el-row>
          <el-col :span="12">
            <el-form-item label="调拨单号" prop="transferNo">
              <el-input v-model="form.transferNo" placeholder="系统自动生成" disabled />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="调拨日期" prop="transferDate">
              <el-date-picker clearable
                v-model="form.transferDate"
                type="date"
                value-format="YYYY-MM-DD"
                placeholder="请选择调拨日期"
                style="width: 100%"
                :disabled="!isTransferFormEditable()">
              </el-date-picker>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="调出仓库" prop="outWarehouseId">
              <el-select v-model="form.outWarehouseId" placeholder="请选择调出仓库" style="width: 100%" clearable filterable remote reserve-keyword remote-show-suffix :remote-method="loadWarehouseOptionList" :loading="warehouseListLoading" :disabled="!isTransferFormEditable()" @change="handleOutWarehouseChange">
                <el-option v-for="warehouse in warehouseSelectOptionList" :key="warehouse.warehouseId" :label="warehouse.warehouseName" :value="warehouse.warehouseId" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="调入仓库" prop="inWarehouseId">
              <el-select v-model="form.inWarehouseId" placeholder="请选择调入仓库" style="width: 100%" clearable filterable remote reserve-keyword remote-show-suffix :remote-method="loadWarehouseOptionList" :loading="warehouseListLoading" :disabled="!isTransferFormEditable()">
                <el-option v-for="warehouse in warehouseSelectOptionList" :key="warehouse.warehouseId" :label="warehouse.warehouseName" :value="warehouse.warehouseId" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="状态" prop="status">
              <el-select v-model="form.status" placeholder="系统自动维护状态" style="width: 100%" disabled>
                <el-option v-for="statusOption in transferStatusOptionList" :key="statusOption.value" :label="statusOption.label" :value="statusOption.value" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="24">
            <el-form-item label="备注" prop="remark">
              <el-input v-model="form.remark" type="textarea" placeholder="请输入内容" :disabled="!isTransferFormEditable()" />
            </el-form-item>
          </el-col>
        </el-row>

        <el-divider content-position="center">调拨明细信息</el-divider>
        <el-row :gutter="10" class="mb8" v-if="isTransferFormEditable()">
          <el-col :span="1.5">
            <el-button type="primary" icon="Plus" @click="handleAddWmsTransferItem">添加明细</el-button>
          </el-col>
          <el-col :span="1.5">
            <el-button type="danger" icon="Delete" @click="handleDeleteWmsTransferItem">删除明细</el-button>
          </el-col>
        </el-row>
        <el-table :data="wmsTransferItemList" :row-class-name="rowWmsTransferItemIndex" @selection-change="handleWmsTransferItemSelectionChange" ref="wmsTransferItem">
          <el-table-column v-if="isTransferFormEditable()" type="selection" width="50" align="center" />
          <el-table-column label="序号" align="center" prop="index" width="50"/>
          <el-table-column label="商品" prop="productId" min-width="210">
            <template #default="scope">
              <template v-if="isTransferFormEditable()">
                <el-select v-model="scope.row.productId" placeholder="请选择商品" clearable filterable remote reserve-keyword remote-show-suffix :remote-method="loadProductOptionList" :loading="productListLoading" style="width: 100%" @change="val => handleTransferProductSelect(scope.row, val)">
                  <el-option v-for="product in productSelectOptionList" :key="product.productId" :label="product.productName" :value="product.productId" />
                </el-select>
                <div v-if="scope.row.productId && scope.row.availQty !== undefined" style="font-size: 11px; margin-top: 2px;" :style="{ color: Number(scope.row.availQty) >= Number(scope.row.quantity || 0) ? 'var(--el-color-success)' : 'var(--el-color-danger)' }">
                  调出仓可用：{{ scope.row.availQty }}
                </div>
              </template>
              <span v-else>{{ getProductName(scope.row.productId) }}</span>
            </template>
          </el-table-column>
          <el-table-column label="调拨数量" prop="quantity" width="150">
            <template #default="scope">
              <el-input-number v-model="scope.row.quantity" :min="1" @change="calculateTotal" v-if="isTransferFormEditable()" />
              <span v-else>{{ scope.row.quantity }}</span>
            </template>
          </el-table-column>
          <el-table-column label="备注" prop="remark">
            <template #default="scope">
              <el-input v-model="scope.row.remark" placeholder="请输入备注" v-if="isTransferFormEditable()" />
              <span v-else>{{ scope.row.remark }}</span>
            </template>
          </el-table-column>
        </el-table>
        <div style="margin-top: 15px; text-align: right; font-weight: bold; font-size: 16px;">
          总数量：{{ form.totalQuantity || 0 }}
        </div>
      </el-form>
      <template #footer>
        <div class="dialog-footer">
          <el-button type="primary" @click="handleTransferDialogPrimaryAction">{{ isTransferFormEditable() ? "确 定" : "关 闭" }}</el-button>
          <el-button v-if="isTransferFormEditable()" @click="cancel">取 消</el-button>
        </div>
      </template>
    </el-dialog>

    <el-dialog title="确认签收并指定入库库位" v-model="receiveDialogOpen" width="460px" append-to-body>
      <el-form label-width="96px">
        <el-form-item label="调拨单号">
          <span class="erp-code-chip">{{ receiveTargetRow?.transferNo }}</span>
        </el-form-item>
        <el-form-item label="调入仓库">
          <span>{{ receiveTargetRow?.inWarehouseName || getWarehouseName(receiveTargetRow?.inWarehouseId) }}</span>
        </el-form-item>
        <el-form-item label="目标库位">
          <el-select v-model="receiveInLocationId" placeholder="可选：选择调入仓库目标库位（留空则默认首库位）" clearable filterable style="width: 100%">
            <el-option
              v-for="loc in receiveLocationOptions"
              :key="loc.locationId"
              :label="`${loc.locationName} (${loc.locationCode || loc.locationId})`"
              :value="loc.locationId"
            />
          </el-select>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="receiveDialogOpen = false">取 消</el-button>
        <el-button type="primary" :loading="receiveSubmitting" @click="submitReceiveTransfer">确认签收入库</el-button>
      </template>
    </el-dialog>

    <document-print-dialog ref="printDialogRef" />
  </div>
</template>

<script setup name="Transfer">
import DocumentPrintDialog from "@/components/DocumentPrintDialog/index.vue"
import { listTransfer, getTransfer, delTransfer, addTransfer, updateTransfer, submitTransfer, auditTransfer, batchSubmitTransfer, batchAuditTransfer, receiveTransfer, rejectTransfer, recallTransfer, cancelTransfer } from "@/api/business/transfer"
import { listStock } from "@/api/business/stock"
import { listWarehouse, getWarehouse } from "@/api/business/warehouse"
import { listLocation } from "@/api/business/location"
import { listProduct, getProduct } from "@/api/business/product"
import { appendUniqueSelectOption, buildSelectOptionList, normalizeRemoteKeyword } from "@/utils/remoteSelect"
import { ERP_DATE_SHORTCUTS } from "@/utils/erpFormat"
import { parseTime } from "@/utils/ruoyi"

const { proxy } = getCurrentInstance()

const transferList = ref([])
const printDialogRef = ref(null)
const receiveDialogOpen = ref(false)
const receiveSubmitting = ref(false)
const receiveTargetRow = ref(null)
const receiveInLocationId = ref(undefined)
const receiveLocationOptions = ref([])

async function handlePrintTransfer(row) {
  const res = await getTransfer(row.transferId)
  const detail = res.data || row
  const items = detail.wmsTransferItemList || []
  await ensureProductOptionLoaded(items.map(i => i.productId))
  printDialogRef.value?.openPrint({
    docType: "transfer",
    title: "仓库库存调拨单",
    docNo: detail.transferNo || `TF-${detail.transferId}`,
    defaultPaperMode: "triplicate",
    showAmount: false,
    metaFields: [
      { label: "调拨单号", value: detail.transferNo || "-" },
      { label: "调出仓库", value: detail.outWarehouseName || getWarehouseName(detail.outWarehouseId) },
      { label: "调入仓库", value: detail.inWarehouseName || getWarehouseName(detail.inWarehouseId) },
      { label: "调拨日期", value: parseTime(detail.transferDate || detail.createTime, "{y}-{m}-{d}") },
      { label: "单据状态", value: getTransferStatusName(detail.status) },
      { label: "制单人", value: detail.createBy || "-" }
    ],
    columns: [
      { label: "商品名称", prop: "productName" },
      { label: "调拨数量", prop: "quantity", width: "110px", align: "right" },
      { label: "备注", prop: "remark", width: "200px" }
    ],
    items: items.map(it => ({
      productName: it.productName || getProductName(it.productId),
      quantity: it.quantity,
      remark: it.remark || "-"
    })),
    totalQty: detail.totalQuantity || items.reduce((s, i) => s + Number(i.quantity || 0), 0),
    remark: detail.remark || ""
  })
}
const wmsTransferItemList = ref([])
const open = ref(false)
const loading = ref(true)
const showSearch = ref(true)
const ids = ref([])
const checkedTransferItemIndexList = ref([])
const total = ref(0)
const title = ref("")
const isView = ref(false)
const dateRange = ref([])
const warehouseList = ref([])
const warehouseListLoading = ref(false)
const productList = ref([])
const productListLoading = ref(false)

const transferStatusOptionList = [
  { label: "草稿", value: "draft", type: "info" },
  { label: "待审核", value: "0", type: "warning" },
  { label: "在途待签收", value: "in_transit", type: "primary" },
  { label: "已签收完成", value: "1", type: "success" },
  { label: "已作废", value: "2", type: "danger" }
]

const data = reactive({
  form: {},
  queryParams: {
    pageNum: 1,
    pageSize: 10,
    transferNo: undefined,
    outWarehouseId: undefined,
    inWarehouseId: undefined,
    status: undefined
  },
  rules: {
    outWarehouseId: [{ required: true, message: "调出仓库不能为空", trigger: "change" }],
    inWarehouseId: [{ required: true, message: "调入仓库不能为空", trigger: "change" }],
    transferDate: [{ required: true, message: "调拨日期不能为空", trigger: "change" }]
  }
})

const { queryParams, form, rules } = toRefs(data)

const transferStatusTabItems = [
  { label: "全部", value: "" },
  { label: "草稿", value: "draft", dot: "slate" },
  { label: "待审核", value: "0", dot: "amber" },
  { label: "在途待签收", value: "in_transit", dot: "blue" },
  { label: "已签收", value: "1", dot: "emerald" },
  { label: "已作废", value: "2", dot: "rose" }
]

const transferStatItems = computed(() => {
  const rows = transferList.value || []
  const pendingCount = rows.filter(r => r.status === "0").length
  const inTransitCount = rows.filter(r => r.status === "in_transit").length
  const totalQty = rows.reduce((s, r) => s + Number(r.totalQuantity || 0), 0)
  const curStatus = queryParams.value.status ?? ""
  return [
    { key: "all", statusVal: "", label: "调拨单总数", value: total.value, suffix: "单", hint: "点击查看全部调拨单", icon: "Switch", tone: "indigo", clickable: true, active: curStatus === "" },
    { key: "pending", statusVal: "0", label: "本页待审调出", value: pendingCount, suffix: "单", hint: "点击筛选待审核调拨单", icon: "Stamp", tone: "amber", clickable: true, active: curStatus === "0" },
    { key: "transit", statusVal: "in_transit", label: "本页在途待签", value: inTransitCount, suffix: "单", hint: "点击筛选在途待签收单据", icon: "Van", tone: "blue", clickable: true, active: curStatus === "in_transit" },
    { key: "qty", label: "本页调拨总量", value: totalQty, suffix: "件", hint: "当前页调拨货品总数量", icon: "Box", tone: "emerald" }
  ]
})

function handleStatSelect(item) {
  if (item && item.statusVal !== undefined) {
    handleQuickStatus(item.statusVal)
  }
}

function handleQuickStatus(val) {
  queryParams.value.status = val === "" ? undefined : val
  handleQuery()
}

const warehouseSelectOptionList = computed(() => buildWarehouseSelectOptionList())
const productSelectOptionList = computed(() => buildProductSelectOptionList())

const selectedRows = computed(() => transferList.value.filter(tf => ids.value.includes(tf.transferId)))
const canUpdateSelected = computed(() => selectedRows.value.length === 1 && isEditableTransferStatus(selectedRows.value[0].status))
const canSubmitSelected = computed(() => selectedRows.value.length > 0 && selectedRows.value.every(tf => isEditableTransferStatus(tf.status)))
const canAuditSelected = computed(() => selectedRows.value.length > 0 && selectedRows.value.every(tf => isPendingTransferStatus(tf.status)))
const canDeleteSelected = computed(() => selectedRows.value.length > 0 && selectedRows.value.every(tf => isEditableTransferStatus(tf.status)))

// 初始化页面基础主数据，保证首次进入即可进行名称搜索。
async function initBasicData() {
  await Promise.all([
    loadWarehouseOptionList(),
    loadProductOptionList()
  ])
}

// 从路由参数恢复筛选条件，方便从其他页面跳转后直接定位单据。
function initializeQueryParamsFromRoute() {
  const routeQuery = proxy?.$route?.query || {}
  queryParams.value.transferNo = routeQuery.transferNo || undefined
  queryParams.value.outWarehouseId = convertRouteNumberValue(routeQuery.outWarehouseId)
  queryParams.value.inWarehouseId = convertRouteNumberValue(routeQuery.inWarehouseId)
  queryParams.value.status = routeQuery.status || undefined
}

// 规范路由中的数字参数，避免空字符串干扰筛选和回显。
function convertRouteNumberValue(routeValue) {
  if (routeValue === undefined || routeValue === null || routeValue === "") {
    return undefined
  }
  const numberValue = Number(routeValue)
  return Number.isNaN(numberValue) ? undefined : numberValue
}

// 远程加载仓库下拉选项，减少一次性加载全部仓库的等待时间。
async function loadWarehouseOptionList(searchKeyword) {
  warehouseListLoading.value = true
  try {
    const normalizedKeyword = normalizeRemoteKeyword(searchKeyword)
    const response = await listWarehouse({
      pageNum: 1,
      pageSize: 20,
      status: "0",
      warehouseName: normalizedKeyword
    })
    ;(response.rows || []).forEach(item => appendUniqueSelectOption(warehouseList.value, item, "warehouseId"))
  } finally {
    warehouseListLoading.value = false
  }
}

// 远程加载商品下拉选项，让调拨明细按商品名称搜索。
async function loadProductOptionList(searchKeyword) {
  productListLoading.value = true
  try {
    const normalizedKeyword = normalizeRemoteKeyword(searchKeyword)
    const response = await listProduct({
      pageNum: 1,
      pageSize: 20,
      status: "0",
      productName: normalizedKeyword
    })
    ;(response.rows || []).forEach(item => appendUniqueSelectOption(productList.value, item, "productId"))
  } finally {
    productListLoading.value = false
  }
}

// 按仓库编号补齐列表和弹窗需要展示的仓库名称。
async function ensureWarehouseOptionLoaded(warehouseIdList) {
  const pendingWarehouseIdList = [...new Set((warehouseIdList || []).filter(warehouseId => warehouseId !== undefined && warehouseId !== null && warehouseId !== ""))]
  for (const warehouseId of pendingWarehouseIdList) {
    if (warehouseList.value.some(warehouse => warehouse.warehouseId === warehouseId)) {
      continue
    }
    try {
      const response = await getWarehouse(warehouseId)
      appendUniqueSelectOption(warehouseList.value, response.data, "warehouseId")
    } catch (error) {
      appendUniqueSelectOption(warehouseList.value, { warehouseId, warehouseName: "仓库资料缺失" }, "warehouseId")
    }
  }
}

// 按商品编号补齐调拨明细需要展示的商品名称。
async function ensureProductOptionLoaded(productIdList) {
  const pendingProductIdList = [...new Set((productIdList || []).filter(productId => productId !== undefined && productId !== null && productId !== ""))]
  for (const productId of pendingProductIdList) {
    if (productList.value.some(product => product.productId === productId)) {
      continue
    }
    try {
      const response = await getProduct(productId)
      appendUniqueSelectOption(productList.value, response.data, "productId")
    } catch (error) {
      appendUniqueSelectOption(productList.value, { productId, productName: "商品资料缺失" }, "productId")
    }
  }
}

// 批量补齐调拨主单相关引用数据，优先复用后端关联字段，消除冗余 N+1 查询。
async function ensureTransferReferenceOptionsLoaded(targetTransferList = []) {
  const warehouseIdList = [
    queryParams.value.outWarehouseId,
    queryParams.value.inWarehouseId,
    form.value.outWarehouseId,
    form.value.inWarehouseId
  ]
  ;(targetTransferList || []).forEach(transfer => {
    if (!transfer) return
    if (transfer.outWarehouseId != null && transfer.outWarehouseName) {
      appendUniqueSelectOption(warehouseList.value, { warehouseId: transfer.outWarehouseId, warehouseName: transfer.outWarehouseName }, "warehouseId")
    } else if (transfer.outWarehouseId != null) {
      warehouseIdList.push(transfer.outWarehouseId)
    }
    if (transfer.inWarehouseId != null && transfer.inWarehouseName) {
      appendUniqueSelectOption(warehouseList.value, { warehouseId: transfer.inWarehouseId, warehouseName: transfer.inWarehouseName }, "warehouseId")
    } else if (transfer.inWarehouseId != null) {
      warehouseIdList.push(transfer.inWarehouseId)
    }
  })
  await ensureWarehouseOptionLoaded(warehouseIdList)
}

// 批量补齐调拨明细相关引用数据，保证商品名称正常回显。
async function ensureTransferItemReferenceOptionsLoaded(targetTransferItemList = []) {
  const productIdList = [
    ...wmsTransferItemList.value.map(transferItem => transferItem.productId),
    ...targetTransferItemList.map(transferItem => transferItem.productId)
  ]
  await ensureProductOptionLoaded(productIdList)
}

// 构造仓库下拉选项，并兼容主数据缺失时的兜底回显。
function buildWarehouseSelectOptionList() {
  return buildSelectOptionList(warehouseList.value, [queryParams.value.outWarehouseId, queryParams.value.inWarehouseId, form.value.outWarehouseId, form.value.inWarehouseId], "warehouseId", "warehouseName", () => "仓库资料缺失")
}

// 构造商品下拉选项，并兼容主数据缺失时的兜底回显。
function buildProductSelectOptionList() {
  return buildSelectOptionList(productList.value, wmsTransferItemList.value.map(transferItem => transferItem.productId), "productId", "productName", () => "商品资料缺失")
}

// 通过仓库编号返回仓库名称，未命中主数据时显示资料缺失提示。
function getWarehouseName(warehouseId) {
  if (warehouseId === undefined || warehouseId === null || warehouseId === "") {
    return "-"
  }
  const warehouse = warehouseList.value.find(warehouseItem => warehouseItem.warehouseId === warehouseId)
  return warehouse?.warehouseName || "仓库资料缺失"
}

// 通过商品编号返回商品名称，未命中主数据时显示资料缺失提示。
function getProductName(productId) {
  if (productId === undefined || productId === null || productId === "") {
    return "-"
  }
  const product = productList.value.find(productItem => productItem.productId === productId)
  return product?.productName || "商品资料缺失"
}

// 返回库存调拨状态中文名称，提升列表可读性。
function getTransferStatusName(statusValue) {
  const statusOption = transferStatusOptionList.find(optionItem => optionItem.value === statusValue)
  return statusOption?.label || statusValue || "未知状态"
}

// 返回库存调拨状态标签类型，帮助用户快速识别流程阶段。
function getTransferStatusType(statusValue) {
  const statusOption = transferStatusOptionList.find(optionItem => optionItem.value === statusValue)
  return statusOption?.type || "info"
}

// 判断库存调拨是否仍为待审核状态。
function isPendingTransferStatus(statusValue) {
  return statusValue === "0"
}

function isEditableTransferStatus(statusValue) {
  return statusValue === "draft"
}

// 判断库存调拨当前是否允许作废。
function canCancelTransferStatus(statusValue) {
  return statusValue === "draft" || statusValue === "0"
}

// 判断当前弹窗是否允许编辑。
function isTransferFormEditable() {
  return !isView.value
}

// 处理弹窗主按钮动作，编辑模式保存，只读模式关闭。
function handleTransferDialogPrimaryAction() {
  if (isTransferFormEditable()) {
    submitForm()
    return
  }
  cancel()
}

// 查询库存调拨列表，并补齐仓库名称。
async function getList() {
  loading.value = true
  try {
    const response = await listTransfer(proxy.addDateRange(queryParams.value, dateRange.value))
    transferList.value = response.rows || []
    total.value = response.total || 0
    await ensureTransferReferenceOptionsLoaded(transferList.value)
  } finally {
    loading.value = false
  }
}

// 关闭弹窗并重置表单。
function cancel() {
  open.value = false
  reset()
}

// 重置库存调拨主单和明细表单。
function reset() {
  form.value = {
    transferId: undefined,
    transferNo: generateTransferNo(),
    outWarehouseId: undefined,
    inWarehouseId: undefined,
    transferDate: buildCurrentDateValue(),
    status: "draft",
    totalQuantity: 0,
    remark: undefined
  }
  wmsTransferItemList.value = []
  checkedTransferItemIndexList.value = []
  isView.value = false
  proxy.resetForm("transferRef")
}

// 生成默认调拨单号，保持现有系统生成方式不变。
function generateTransferNo() {
  return `TR${Date.now()}`
}

// 生成当前日期字符串，兼容日期组件的 value-format。
function buildCurrentDateValue() {
  const currentDate = new Date()
  const yearValue = currentDate.getFullYear()
  const monthValue = String(currentDate.getMonth() + 1).padStart(2, "0")
  const dayValue = String(currentDate.getDate()).padStart(2, "0")
  return `${yearValue}-${monthValue}-${dayValue}`
}

// 执行查询并回到第一页。
function handleQuery() {
  queryParams.value.pageNum = 1
  getList()
}

// 重置查询条件并重新拉取列表。
function resetQuery() {
  dateRange.value = []
  proxy.resetForm("queryRef")
  queryParams.value.status = undefined
  handleQuery()
}

// 同步表格选中状态，让顶部按钮和行内按钮规则一致。
function handleSelectionChange(selection) {
  ids.value = selection.map(transfer => transfer.transferId)
}

// 打开新增库存调拨弹窗。
function handleAdd() {
  reset()
  open.value = true
  title.value = "新增库存调拨"
}

// 打开编辑库存调拨弹窗，并补齐仓库与商品回显。
async function handleUpdate(row) {
  reset()
  const transferId = row?.transferId || ids.value[0]
  if (!transferId) {
    return
  }
  const response = await getTransfer(transferId)
  form.value = response.data
  wmsTransferItemList.value = response.data.wmsTransferItemList || []
  checkedTransferItemIndexList.value = []
  isView.value = false
  calculateTotal()
  await Promise.all([
    ensureTransferReferenceOptionsLoaded([form.value]),
    ensureTransferItemReferenceOptionsLoaded(wmsTransferItemList.value)
  ])
  open.value = true
  title.value = "修改库存调拨"
}

// 打开库存调拨详情弹窗，详情仅支持查看。
async function handleView(row) {
  reset()
  const response = await getTransfer(row.transferId)
  form.value = response.data
  wmsTransferItemList.value = response.data.wmsTransferItemList || []
  checkedTransferItemIndexList.value = []
  isView.value = true
  calculateTotal()
  await Promise.all([
    ensureTransferReferenceOptionsLoaded([form.value]),
    ensureTransferItemReferenceOptionsLoaded(wmsTransferItemList.value)
  ])
  open.value = true
  title.value = "调拨单详情"
}

// 为明细表格生成序号列。
function rowWmsTransferItemIndex({ row, rowIndex }) {
  row.index = rowIndex + 1
}

// 向调拨明细中新增一行空白数据。
function handleAddWmsTransferItem() {
  wmsTransferItemList.value.push({
    productId: undefined,
    quantity: 1,
    remark: "",
    availQty: undefined
  })
  calculateTotal()
}

async function handleTransferProductSelect(row, productId) {
  row.availQty = undefined
  if (!productId || !form.value.outWarehouseId) return
  try {
    const res = await listStock({ pageNum: 1, pageSize: 50, warehouseId: form.value.outWarehouseId, productId })
    const avail = (res.rows || []).reduce((s, r) => s + Math.max(0, Number(r.quantity || 0) - Number(r.lockedQuantity || 0) - Number(r.frozenQuantity || 0)), 0)
    row.availQty = Number(avail.toFixed(2))
  } catch (e) {}
}

function handleOutWarehouseChange() {
  ;(wmsTransferItemList.value || []).forEach(row => {
    if (row.productId) {
      handleTransferProductSelect(row, row.productId)
    } else {
      row.availQty = undefined
    }
  })
}

// 删除选中的调拨明细。
function handleDeleteWmsTransferItem() {
  if (checkedTransferItemIndexList.value.length === 0) {
    proxy.$modal.msgError("请先选择要删除的调拨明细数据")
    return
  }
  wmsTransferItemList.value = wmsTransferItemList.value.filter(transferItem => !checkedTransferItemIndexList.value.includes(transferItem.index))
  checkedTransferItemIndexList.value = []
  calculateTotal()
}

// 同步明细表格选中状态。
function handleWmsTransferItemSelectionChange(selection) {
  checkedTransferItemIndexList.value = selection.map(transferItem => transferItem.index)
}

// 计算调拨总数量，保持主单汇总实时更新。
function calculateTotal() {
  form.value.totalQuantity = wmsTransferItemList.value.reduce((totalQuantityValue, transferItem) => {
    const quantityValue = Number(transferItem.quantity || 0)
    return totalQuantityValue + quantityValue
  }, 0)
}

// 校验调拨明细完整性，避免用户提交空商品或空数量。
function validateTransferItemList() {
  if (wmsTransferItemList.value.length === 0) {
    proxy.$modal.msgError("请添加调拨明细")
    return false
  }
  const invalidTransferItemIndex = wmsTransferItemList.value.findIndex(transferItem => !transferItem.productId || Number(transferItem.quantity || 0) <= 0)
  if (invalidTransferItemIndex >= 0) {
    proxy.$modal.msgError(`第${invalidTransferItemIndex + 1}行请完善商品和调拨数量`)
    return false
  }
  return true
}

// 保存库存调拨主单及其明细。
function submitForm() {
  proxy.$refs["transferRef"].validate(valid => {
    if (!valid) {
      return
    }
    if (form.value.outWarehouseId === form.value.inWarehouseId) {
      proxy.$modal.msgError("调出仓库与调入仓库不能相同")
      return
    }
    if (!validateTransferItemList()) {
      return
    }
    calculateTotal()
    form.value.wmsTransferItemList = wmsTransferItemList.value
    const submitMethod = form.value.transferId !== undefined ? updateTransfer : addTransfer
    submitMethod(form.value).then(() => {
      proxy.$modal.msgSuccess(form.value.transferId !== undefined ? "修改成功" : "新增成功")
      open.value = false
      getList()
    })
  })
}

// 删除库存调拨主单，仅允许删除待审核单据，并在空选择时及时提醒用户。
function handleDelete(row) {
  const transferIdList = row?.transferId ? [row.transferId] : ids.value
  if (!transferIdList.length) {
    proxy.$modal.msgWarning("请选择要删除的调拨单")
    return
  }
  proxy.$modal.confirm(`是否确认删除调拨单编号为"${transferIdList.join("、")}"的数据项？`).then(function () {
    return delTransfer(transferIdList)
  }).then(() => {
    proxy.$modal.msgSuccess("删除成功")
    getList()
  }).catch(() => {})
}

// 返回当前要操作的库存调拨单，兼容工具栏和行内入口，未选中时先提示用户。
function getTargetTransferRow(row, actionText) {
  const transferRow = row || transferList.value.find(transfer => transfer.transferId === ids.value[0])
  if (!transferRow) {
    proxy.$modal.msgWarning(`请选择要${actionText}的调拨单`)
    return undefined
  }
  return transferRow
}

// 审核库存调拨（支持单条与多选批量审核）。
function handleAudit(row) {
  if (!row?.transferId && ids.value.length > 1) {
    proxy.$modal.confirm(`是否确认批量审核选中的 ${ids.value.length} 笔调拨单？审核后调出仓将扣减库存并进入【在途待签收】状态。`).then(() => {
      return batchAuditTransfer(ids.value)
    }).then(res => {
      proxy.$modal.msgSuccess(res.msg || "批量审核完成")
      getList()
    }).catch(() => {})
    return
  }
  const transferRow = getTargetTransferRow(row, "审核")
  if (!transferRow) {
    return
  }
  proxy.$modal.confirm(`是否确认审核调出库存调拨单号"${transferRow.transferNo}"？审核后调出仓将扣减库存并进入【在途待签收】状态。`).then(function () {
    return auditTransfer(transferRow.transferId)
  }).then(() => {
    proxy.$modal.msgSuccess("调出仓审核完成，货物已进入在途待签收状态")
    getList()
  }).catch(() => {})
}

function handleSubmitTransfer(row) {
  if (!row?.transferId && ids.value.length > 1) {
    proxy.$modal.confirm(`确认批量提交选中的 ${ids.value.length} 笔调拨单进入待审核？`).then(() => {
      return batchSubmitTransfer(ids.value)
    }).then(res => {
      proxy.$modal.msgSuccess(res.msg || "批量提交完成")
      getList()
    }).catch(() => {})
    return
  }
  const transferRow = getTargetTransferRow(row, "提交")
  if (!transferRow) return
  proxy.$modal.confirm(`确认提交库存调拨单"${transferRow.transferNo}"进入待审核？`).then(() => {
    return submitTransfer(transferRow.transferId)
  }).then(() => {
    proxy.$modal.msgSuccess("提交成功")
    getList()
  }).catch(() => {})
}

async function handleReceiveTransfer(row) {
  receiveTargetRow.value = row
  receiveInLocationId.value = undefined
  receiveLocationOptions.value = []
  receiveDialogOpen.value = true
  if (row?.inWarehouseId) {
    try {
      const res = await listLocation({ pageNum: 1, pageSize: 100, warehouseId: row.inWarehouseId, status: "0" })
      receiveLocationOptions.value = res.rows || []
      if (receiveLocationOptions.value.length > 0) {
        receiveInLocationId.value = receiveLocationOptions.value[0].locationId
      }
    } catch (e) {}
  }
}

async function submitReceiveTransfer() {
  if (!receiveTargetRow.value?.transferId) return
  receiveSubmitting.value = true
  try {
    await receiveTransfer(receiveTargetRow.value.transferId, receiveInLocationId.value)
    proxy.$modal.msgSuccess("调入签收完成，库存已入账")
    receiveDialogOpen.value = false
    getList()
  } finally {
    receiveSubmitting.value = false
  }
}

function handleRejectTransfer(row) {
  proxy.$modal.prompt(`确认驳回调拨单"${row.transferNo}"？请输入驳回原因：`).then(({ value }) => {
    return rejectTransfer(row.transferId, value || "调拨明细待核实")
  }).then(() => {
    proxy.$modal.msgSuccess("调拨单已驳回至草稿")
    getList()
  }).catch(() => {})
}

function handleRecallTransfer(row) {
  proxy.$modal.prompt(`确认撤回在途调拨单"${row.transferNo}"？撤回后已调出库存将全额退回原调出仓！请输入撤回原因：`).then(({ value }) => {
    return recallTransfer(row.transferId, value || "物流取消在途撤回")
  }).then(() => {
    proxy.$modal.msgSuccess("在途调拨已撤回，库存已恢复至原调出仓")
    getList()
  }).catch(() => {})
}

// 作废库存调拨，阻止异常调拨单继续流转。
function handleCancelOrder(row) {
  const transferRow = getTargetTransferRow(row, "作废")
  if (!transferRow) {
    return
  }
  proxy.$modal.confirm(`是否确认作废库存调拨单号为"${transferRow.transferNo}"的数据项？`).then(function () {
    return cancelTransfer(transferRow.transferId)
  }).then(() => {
    proxy.$modal.msgSuccess("作废成功")
    getList()
  }).catch(() => {})
}

// 导出库存调拨列表，沿用系统统一导出方式。
function handleExport() {
  proxy.download("business/transfer/export", {
    ...queryParams.value
  }, `transfer_${new Date().getTime()}.xlsx`)
}

// 初始化页面筛选条件和基础资料，保证重复跳转到调拨页时列表会按最新地址参数刷新。
async function initializePage() {
  initializeQueryParamsFromRoute()
  await Promise.all([
    Promise.resolve(initBasicData()),
    Promise.resolve(getList())
  ])
}

// 监听同一路由下的查询参数变化，避免库存调拨页继续显示旧的仓库或单号筛选结果。
watch(() => proxy?.$route?.fullPath, (currentRouteFullPath, previousRouteFullPath) => {
  if (currentRouteFullPath === previousRouteFullPath) {
    return
  }
  initializePage()
})

initializePage()
</script>
