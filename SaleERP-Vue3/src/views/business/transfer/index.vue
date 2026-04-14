<template>
  <div class="app-container">
    <el-form :model="queryParams" ref="queryRef" :inline="true" v-show="showSearch" label-width="90px">
      <el-form-item label="调拨单号" prop="transferNo">
        <el-input v-model="queryParams.transferNo" placeholder="请输入调拨单号" clearable @keyup.enter="handleQuery" />
      </el-form-item>
      <el-form-item label="调出仓库" prop="outWarehouseId">
        <el-select v-model="queryParams.outWarehouseId" placeholder="请选择调出仓库" clearable filterable remote reserve-keyword remote-show-suffix :remote-method="loadWarehouseOptionList" :loading="warehouseListLoading">
          <el-option v-for="warehouse in warehouseSelectOptionList" :key="warehouse.warehouseId" :label="warehouse.warehouseName" :value="warehouse.warehouseId" />
        </el-select>
      </el-form-item>
      <el-form-item label="调入仓库" prop="inWarehouseId">
        <el-select v-model="queryParams.inWarehouseId" placeholder="请选择调入仓库" clearable filterable remote reserve-keyword remote-show-suffix :remote-method="loadWarehouseOptionList" :loading="warehouseListLoading">
          <el-option v-for="warehouse in warehouseSelectOptionList" :key="warehouse.warehouseId" :label="warehouse.warehouseName" :value="warehouse.warehouseId" />
        </el-select>
      </el-form-item>
      <el-form-item label="状态" prop="status">
        <el-select v-model="queryParams.status" placeholder="请选择状态" clearable>
          <el-option v-for="statusOption in transferStatusOptionList" :key="statusOption.value" :label="statusOption.label" :value="statusOption.value" />
        </el-select>
      </el-form-item>
      <el-form-item>
        <el-button type="primary" icon="Search" @click="handleQuery">搜索</el-button>
        <el-button icon="Refresh" @click="resetQuery">重置</el-button>
      </el-form-item>
    </el-form>

    <el-alert
      title="库存调拨保存后直接进入待审核，审核时系统会自动完成调出仓和调入仓的库存变更。"
      type="info"
      :closable="false"
      class="mb8"
    />

    <el-row :gutter="10" class="mb8">
      <el-col :span="1.5">
        <el-button type="primary" plain icon="Plus" @click="handleAdd" v-hasPermi="['business:transfer:add']">新增</el-button>
      </el-col>
      <el-col :span="1.5">
        <el-button type="success" plain icon="Edit" :disabled="!canUpdateSelected" @click="handleUpdate" v-hasPermi="['business:transfer:edit']">修改</el-button>
      </el-col>
      <el-col :span="1.5">
        <el-button type="warning" plain icon="CircleCheck" :disabled="!canAuditSelected" @click="handleAudit" v-hasPermi="['business:transfer:audit']">审核</el-button>
      </el-col>
      <el-col :span="1.5">
        <el-button type="danger" plain icon="Delete" :disabled="!canDeleteSelected" @click="handleDelete" v-hasPermi="['business:transfer:remove']">删除</el-button>
      </el-col>
      <el-col :span="1.5">
        <el-button type="warning" plain icon="Download" @click="handleExport" v-hasPermi="['business:transfer:export']">导出</el-button>
      </el-col>
      <right-toolbar v-model:showSearch="showSearch" @queryTable="getList"></right-toolbar>
    </el-row>

    <el-table v-loading="loading" :data="transferList" @selection-change="handleSelectionChange">
      <el-table-column type="selection" width="55" align="center" />
      <el-table-column label="调拨单号" align="center" prop="transferNo" />
      <el-table-column label="调出仓库" align="center" prop="outWarehouseId">
        <template #default="scope">
          <span>{{ getWarehouseName(scope.row.outWarehouseId) }}</span>
        </template>
      </el-table-column>
      <el-table-column label="调入仓库" align="center" prop="inWarehouseId">
        <template #default="scope">
          <span>{{ getWarehouseName(scope.row.inWarehouseId) }}</span>
        </template>
      </el-table-column>
      <el-table-column label="调拨日期" align="center" prop="transferDate" width="180">
        <template #default="scope">
          <span>{{ parseTime(scope.row.transferDate, '{y}-{m}-{d}') }}</span>
        </template>
      </el-table-column>
      <el-table-column label="总数量" align="center" prop="totalQuantity" />
      <el-table-column label="状态" align="center" prop="status">
        <template #default="scope">
          <el-tag :type="getTransferStatusType(scope.row.status)">{{ getTransferStatusName(scope.row.status) }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column label="操作" width="320" align="center" class-name="small-padding fixed-width">
        <template #default="scope">
          <el-button v-if="isPendingTransferStatus(scope.row.status)" link type="primary" icon="Edit" @click="handleUpdate(scope.row)" v-hasPermi="['business:transfer:edit']">修改</el-button>
          <el-button v-if="isPendingTransferStatus(scope.row.status)" link type="success" icon="CircleCheck" @click="handleAudit(scope.row)" v-hasPermi="['business:transfer:audit']">审核</el-button>
          <el-button v-if="canCancelTransferStatus(scope.row.status)" link type="warning" icon="Close" @click="handleCancelOrder(scope.row)" v-hasPermi="['business:transfer:edit']">作废</el-button>
          <el-button v-if="isPendingTransferStatus(scope.row.status)" link type="primary" icon="Delete" @click="handleDelete(scope.row)" v-hasPermi="['business:transfer:remove']">删除</el-button>
          <el-button link type="primary" icon="View" @click="handleView(scope.row)">详情</el-button>
        </template>
      </el-table-column>
    </el-table>
    
    <pagination
      v-show="total>0"
      :total="total"
      v-model:page="queryParams.pageNum"
      v-model:limit="queryParams.pageSize"
      @pagination="getList"
    />

    <el-dialog :title="title" v-model="open" width="800px" append-to-body>
      <el-alert
        :title="isTransferFormEditable() ? '库存调拨保存后会进入待审核，审核时系统自动完成库存转移。' : '当前为只读查看模式，调拨信息不可编辑。'"
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
              <el-select v-model="form.outWarehouseId" placeholder="请选择调出仓库" style="width: 100%" clearable filterable remote reserve-keyword remote-show-suffix :remote-method="loadWarehouseOptionList" :loading="warehouseListLoading" :disabled="!isTransferFormEditable()">
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
          <el-table-column label="商品" prop="productId">
            <template #default="scope">
              <el-select v-model="scope.row.productId" placeholder="请选择商品" v-if="isTransferFormEditable()" clearable filterable remote reserve-keyword remote-show-suffix :remote-method="loadProductOptionList" :loading="productListLoading">
                <el-option v-for="product in productSelectOptionList" :key="product.productId" :label="product.productName" :value="product.productId" />
              </el-select>
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
  </div>
</template>

<script setup name="BusinessTransfer">
import { listTransfer, getTransfer, delTransfer, addTransfer, updateTransfer, auditTransfer, cancelTransfer } from "@/api/business/transfer"
import { listWarehouse, getWarehouse } from "@/api/business/warehouse"
import { listProduct, getProduct } from "@/api/business/product"
import { appendUniqueSelectOption, buildSelectOptionList, normalizeRemoteKeyword } from "@/utils/remoteSelect"
import { parseTime } from "@/utils/ruoyi"

const { proxy } = getCurrentInstance()

const transferList = ref([])
const wmsTransferItemList = ref([])
const open = ref(false)
const loading = ref(true)
const showSearch = ref(true)
const ids = ref([])
const checkedTransferItemIndexList = ref([])
const total = ref(0)
const title = ref("")
const isView = ref(false)
const warehouseList = ref([])
const warehouseListLoading = ref(false)
const productList = ref([])
const productListLoading = ref(false)

const transferStatusOptionList = [
  { label: "待审核", value: "0", type: "warning" },
  { label: "已审核", value: "1", type: "success" },
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

const warehouseSelectOptionList = computed(() => buildWarehouseSelectOptionList())
const productSelectOptionList = computed(() => buildProductSelectOptionList())

const canUpdateSelected = computed(() => ids.value.length === 1 && transferList.value.some(transfer => transfer.transferId === ids.value[0] && isPendingTransferStatus(transfer.status)))
const canAuditSelected = computed(() => ids.value.length === 1 && transferList.value.some(transfer => transfer.transferId === ids.value[0] && isPendingTransferStatus(transfer.status)))
const canDeleteSelected = computed(() => ids.value.length > 0 && transferList.value.filter(transfer => ids.value.includes(transfer.transferId)).every(transfer => isPendingTransferStatus(transfer.status)))

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
      warehouseName: normalizedKeyword
    })
    warehouseList.value = normalizedKeyword ? response.rows || [] : buildSelectOptionList(response.rows || [], [], "warehouseId", "warehouseName", () => "仓库资料缺失")
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
      productName: normalizedKeyword
    })
    productList.value = normalizedKeyword ? response.rows || [] : buildSelectOptionList(response.rows || [], [], "productId", "productName", () => "商品资料缺失")
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

// 批量补齐调拨主单相关引用数据，保证列表和弹窗都用名称展示。
async function ensureTransferReferenceOptionsLoaded(targetTransferList = []) {
  const warehouseIdList = [
    queryParams.value.outWarehouseId,
    queryParams.value.inWarehouseId,
    form.value.outWarehouseId,
    form.value.inWarehouseId,
    ...targetTransferList.map(transfer => transfer.outWarehouseId),
    ...targetTransferList.map(transfer => transfer.inWarehouseId)
  ]
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

// 判断库存调拨当前是否允许作废。
function canCancelTransferStatus(statusValue) {
  return statusValue !== "1" && statusValue !== "2"
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
    const response = await listTransfer(queryParams.value)
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
    status: "0",
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
  proxy.resetForm("queryRef")
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
    remark: ""
  })
  calculateTotal()
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

// 删除库存调拨主单，仅允许删除待审核单据。
function handleDelete(row) {
  const transferIds = row?.transferId || ids.value
  if (!transferIds || transferIds.length === 0) {
    return
  }
  proxy.$modal.confirm(`是否确认删除调拨单编号为"${transferIds}"的数据项？`).then(function () {
    return delTransfer(transferIds)
  }).then(() => {
    proxy.$modal.msgSuccess("删除成功")
    getList()
  }).catch(() => {})
}

// 审核库存调拨，审核后系统自动处理调出和调入库存。
function handleAudit(row) {
  const transferRow = row || transferList.value.find(transfer => transfer.transferId === ids.value[0])
  if (!transferRow) {
    return
  }
  proxy.$modal.confirm(`是否确认审核库存调拨单号为"${transferRow.transferNo}"的数据项？`).then(function () {
    return auditTransfer(transferRow.transferId)
  }).then(() => {
    proxy.$modal.msgSuccess("审核成功")
    getList()
  }).catch(() => {})
}

// 作废库存调拨，阻止异常调拨单继续流转。
function handleCancelOrder(row) {
  proxy.$modal.confirm(`是否确认作废库存调拨单号为"${row.transferNo}"的数据项？`).then(function () {
    return cancelTransfer(row.transferId)
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
  await initBasicData()
  await getList()
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
