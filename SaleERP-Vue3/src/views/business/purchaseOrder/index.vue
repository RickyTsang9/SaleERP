<template>
  <div class="app-container">
    <el-form :model="queryParams" ref="queryRef" :inline="true" v-show="showSearch" label-width="90px">
      <el-form-item label="采购单号" prop="orderNo">
        <el-input v-model="queryParams.orderNo" placeholder="请输入采购单号" clearable @keyup.enter="handleQuery" />
      </el-form-item>
      <el-form-item label="供应商" prop="supplierId">
        <el-select
          v-model="queryParams.supplierId"
          placeholder="请选择供应商"
          clearable
          filterable
          remote
          reserve-keyword
          remote-show-suffix
          :remote-method="loadSupplierOptionList"
          :loading="supplierListLoading"
        >
          <el-option v-for="supplier in supplierSelectOptionList" :key="supplier.supplierId" :label="supplier.supplierName" :value="supplier.supplierId" />
        </el-select>
      </el-form-item>
      <el-form-item label="状态" prop="status">
        <el-select v-model="queryParams.status" placeholder="请选择状态" clearable>
          <el-option
            v-for="purchaseOrderStatusOption in purchaseOrderStatusOptionList"
            :key="purchaseOrderStatusOption.value"
            :label="purchaseOrderStatusOption.label"
            :value="purchaseOrderStatusOption.value"
          />
        </el-select>
      </el-form-item>
      <el-form-item>
        <el-button type="primary" icon="Search" @click="handleQuery">搜索</el-button>
        <el-button icon="Refresh" @click="resetQuery">重置</el-button>
      </el-form-item>
    </el-form>

    <el-alert
      title="采购订单保存后直接进入待审核，审核时系统自动生成应付记录。待审核订单可维护，已审核、部分入库、已完成和已作废订单仅支持查看。"
      type="info"
      :closable="false"
      class="mb8"
    />

    <el-row :gutter="10" class="mb8">
      <el-col :span="1.5">
        <el-button type="primary" plain icon="Plus" @click="handleAdd" v-hasPermi="['business:purchaseOrder:add']">新增</el-button>
      </el-col>
      <el-col :span="1.5">
        <el-button type="success" plain icon="Edit" :disabled="!canUpdateSelected" @click="handleUpdate" v-hasPermi="['business:purchaseOrder:edit']">修改</el-button>
      </el-col>
      <el-col :span="1.5">
        <el-button type="primary" plain icon="OfficeBuilding" :disabled="ids.length !== 1" @click="handleViewSupplier" v-hasPermi="['business:supplier:list']">供应商资料</el-button>
      </el-col>
      <el-col :span="1.5">
        <el-button type="warning" plain icon="CircleCheck" :disabled="!canAuditSelected" @click="handleAudit" v-hasPermi="['business:purchaseOrder:audit']">审核</el-button>
      </el-col>
      <el-col :span="1.5">
        <el-button type="danger" plain icon="Delete" :disabled="!canDeleteSelected" @click="handleDelete" v-hasPermi="['business:purchaseOrder:remove']">删除</el-button>
      </el-col>
      <el-col :span="1.5">
        <el-button type="warning" plain icon="Download" @click="handleExport" v-hasPermi="['business:purchaseOrder:export']">导出</el-button>
      </el-col>
      <right-toolbar v-model:showSearch="showSearch" @queryTable="getList"></right-toolbar>
    </el-row>

    <el-table v-loading="loading" :data="purchaseOrderList" @selection-change="handleSelectionChange">
      <el-table-column type="selection" width="55" align="center" />
      <el-table-column label="采购单号" align="center" prop="orderNo" min-width="180" />
      <el-table-column label="供应商" align="center" prop="supplierId" min-width="180">
        <template #default="scope">
          <span>{{ getSupplierName(scope.row.supplierId) }}</span>
        </template>
      </el-table-column>
      <el-table-column label="采购日期" align="center" prop="purchaseDate" width="180">
        <template #default="scope">
          <span>{{ parseTime(scope.row.purchaseDate, '{y}-{m}-{d}') }}</span>
        </template>
      </el-table-column>
      <el-table-column label="采购数量" align="center" prop="totalQuantity" width="120">
        <template #default="scope">
          <span>{{ formatPurchaseQuantity(scope.row.totalQuantity) }}</span>
        </template>
      </el-table-column>
      <el-table-column label="已入库" align="center" prop="inboundQuantity" width="120">
        <template #default="scope">
          <span>{{ formatPurchaseQuantity(scope.row.inboundQuantity) }}</span>
        </template>
      </el-table-column>
      <el-table-column label="待入库" align="center" prop="remainQuantity" width="120">
        <template #default="scope">
          <span>{{ formatPurchaseQuantity(scope.row.remainQuantity) }}</span>
        </template>
      </el-table-column>
      <el-table-column label="总金额" align="center" prop="totalAmount" />
      <el-table-column label="状态" align="center" prop="status">
        <template #default="scope">
          <el-tag :type="getPurchaseOrderStatusType(scope.row.status)">{{ getPurchaseOrderStatusName(scope.row.status) }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column label="操作" align="center" class-name="small-padding fixed-width" width="500">
        <template #default="scope">
          <el-button v-if="isPendingPurchaseOrderStatus(scope.row.status)" link type="primary" icon="Edit" @click="handleUpdate(scope.row)" v-hasPermi="['business:purchaseOrder:edit']">修改</el-button>
          <el-button v-if="isPendingPurchaseOrderStatus(scope.row.status)" link type="success" icon="CircleCheck" @click="handleAudit(scope.row)" v-hasPermi="['business:purchaseOrder:audit']">审核</el-button>
          <el-button link type="primary" icon="OfficeBuilding" @click="handleViewSupplier(scope.row)" v-hasPermi="['business:supplier:list']">供应商</el-button>
          <el-button v-if="canViewLinkedPayable(scope.row.status)" link type="primary" icon="Money" @click="handleViewPayable(scope.row)" v-hasPermi="['business:payable:list']">应付</el-button>
          <el-button v-if="canViewLinkedInbound(scope.row.status)" link type="primary" icon="Box" @click="handleViewInbound(scope.row)" v-hasPermi="['business:inbound:list']">入库</el-button>
          <el-button v-if="canCancelPurchaseOrderStatus(scope.row.status)" link type="warning" icon="Close" @click="handleCancel(scope.row)" v-hasPermi="['business:purchaseOrder:edit']">作废</el-button>
          <el-button v-if="isPendingPurchaseOrderStatus(scope.row.status)" link type="primary" icon="Delete" @click="handleDelete(scope.row)" v-hasPermi="['business:purchaseOrder:remove']">删除</el-button>
          <el-button link type="primary" icon="View" @click="handleView(scope.row)">详情</el-button>
          <el-button link type="primary" icon="Printer" @click="handlePrint(scope.row)">打印</el-button>
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
        :title="isPurchaseOrderEditable() ? '采购订单状态由流程自动维护，当前弹窗仅允许待审核订单调整基础信息。' : '当前采购订单为只读查看模式，主单和明细都不可编辑。'"
        type="info"
        :closable="false"
        class="mb8"
      />
      <el-form ref="purchaseOrderRef" :model="form" :rules="rules" label-width="100px">
        <el-row>
          <el-col :span="12">
            <el-form-item label="采购单号" prop="orderNo">
              <el-input v-model="form.orderNo" placeholder="系统自动生成" disabled />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="供应商" prop="supplierId">
              <el-select
                v-model="form.supplierId"
                placeholder="请选择供应商"
                style="width: 100%"
                clearable
                filterable
                remote
                reserve-keyword
                remote-show-suffix
                :remote-method="loadSupplierOptionList"
                :loading="supplierListLoading"
                :disabled="!isPurchaseOrderEditable()"
              >
                <el-option v-for="supplier in supplierSelectOptionList" :key="supplier.supplierId" :label="supplier.supplierName" :value="supplier.supplierId" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="采购日期" prop="purchaseDate">
              <el-date-picker clearable v-model="form.purchaseDate" type="date" value-format="YYYY-MM-DD" placeholder="请选择采购日期" style="width: 100%" :disabled="!isPurchaseOrderEditable()" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="状态" prop="status">
              <el-select v-model="form.status" placeholder="状态由流程自动维护" style="width: 100%" disabled>
                <el-option
                  v-for="purchaseOrderStatusOption in purchaseOrderStatusOptionList"
                  :key="purchaseOrderStatusOption.value"
                  :label="purchaseOrderStatusOption.label"
                  :value="purchaseOrderStatusOption.value"
                />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="24">
            <el-form-item label="备注" prop="remark">
              <el-input v-model="form.remark" type="textarea" placeholder="请输入内容" :disabled="!isPurchaseOrderEditable()" />
            </el-form-item>
          </el-col>
        </el-row>

        <el-divider content-position="center">采购明细信息</el-divider>
        <el-row :gutter="10" class="mb8" v-if="isPurchaseOrderEditable()">
          <el-col :span="1.5">
            <el-button type="primary" icon="Plus" @click="handleAddPurchaseOrderItem">添加明细</el-button>
          </el-col>
          <el-col :span="1.5">
            <el-button type="danger" icon="Delete" @click="handleDeletePurchaseOrderItem">删除明细</el-button>
          </el-col>
        </el-row>
        <el-table :data="wmsPurchaseOrderItemList" :row-class-name="rowWmsPurchaseOrderItemIndex" @selection-change="handlePurchaseOrderItemSelectionChange" ref="wmsPurchaseOrderItem">
          <el-table-column v-if="isPurchaseOrderEditable()" type="selection" width="50" align="center" />
          <el-table-column label="序号" align="center" prop="index" width="50" />
          <el-table-column label="商品" prop="productId" min-width="220">
            <template #default="scope">
              <el-select
                v-if="isPurchaseOrderEditable()"
                v-model="scope.row.productId"
                placeholder="请选择商品"
                clearable
                filterable
                remote
                reserve-keyword
                remote-show-suffix
                :remote-method="loadProductOptionList"
                :loading="productListLoading"
                style="width: 100%"
              >
                <el-option v-for="product in productSelectOptionList" :key="product.productId" :label="product.productName" :value="product.productId" />
              </el-select>
              <span v-else>{{ getProductName(scope.row.productId) }}</span>
            </template>
          </el-table-column>
          <el-table-column label="数量" prop="quantity" width="150">
            <template #default="scope">
              <el-input-number v-if="isPurchaseOrderEditable()" v-model="scope.row.quantity" :min="1" @change="calculateAmount(scope.row)" />
              <span v-else>{{ scope.row.quantity }}</span>
            </template>
          </el-table-column>
          <el-table-column label="单价" prop="price" width="170">
            <template #default="scope">
              <el-input-number v-if="isPurchaseOrderEditable()" v-model="scope.row.price" :min="0" :precision="2" @change="calculateAmount(scope.row)" />
              <span v-else>{{ scope.row.price }}</span>
            </template>
          </el-table-column>
          <el-table-column label="金额" prop="amount" width="170">
            <template #default="scope">
              <el-input v-if="isPurchaseOrderEditable()" v-model="scope.row.amount" disabled />
              <span v-else>{{ scope.row.amount }}</span>
            </template>
          </el-table-column>
          <el-table-column label="已入库" prop="inboundQuantity" width="120">
            <template #default="scope">
              <span>{{ formatPurchaseQuantity(scope.row.inboundQuantity) }}</span>
            </template>
          </el-table-column>
          <el-table-column label="待入库" prop="remainQuantity" width="120">
            <template #default="scope">
              <span>{{ formatPurchaseQuantity(scope.row.remainQuantity ?? scope.row.quantity) }}</span>
            </template>
          </el-table-column>
        </el-table>
        <div style="margin-top: 15px; text-align: right; font-weight: bold; font-size: 16px;">
          总金额：{{ form.totalAmount || 0 }}
        </div>
      </el-form>
      <template #footer>
        <div class="dialog-footer">
          <el-button type="primary" @click="handlePurchaseOrderDialogPrimaryAction">{{ isPurchaseOrderEditable() ? "确 定" : "关 闭" }}</el-button>
          <el-button v-if="isPurchaseOrderEditable()" @click="cancel">取 消</el-button>
        </div>
      </template>
    </el-dialog>

    <el-dialog title="打印预览" v-model="printOpen" width="800px" append-to-body>
      <div id="printBox" class="print-container" style="padding: 20px; font-family: 'SimSun', '宋体', serif;">
        <h2 style="text-align: center; font-size: 24px; margin-bottom: 20px; letter-spacing: 2px;">采 购 订 单</h2>
        <el-row style="margin-bottom: 15px; font-size: 14px;">
          <el-col :span="12"><strong>采购单号：</strong>{{ form.orderNo }}</el-col>
          <el-col :span="12"><strong>采购日期：</strong>{{ parseTime(form.purchaseDate, '{y}-{m}-{d}') }}</el-col>
        </el-row>
        <el-row style="margin-bottom: 20px; font-size: 14px;">
          <el-col :span="12"><strong>供应商：</strong>{{ getSupplierName(form.supplierId) }}</el-col>
          <el-col :span="12"><strong>单据状态：</strong>{{ getPurchaseOrderStatusName(form.status) }}</el-col>
        </el-row>
        <table border="1" cellspacing="0" cellpadding="8" style="width: 100%; border-collapse: collapse; text-align: center; font-size: 14px; border-color: #333;">
          <thead>
            <tr style="background-color: #f5f5f5;">
              <th width="10%">序号</th>
              <th width="35%">商品名称</th>
              <th width="15%">数量</th>
              <th width="20%">单价(元)</th>
              <th width="20%">金额(元)</th>
            </tr>
          </thead>
          <tbody>
            <tr v-for="(purchaseOrderItem, purchaseOrderItemIndex) in wmsPurchaseOrderItemList" :key="purchaseOrderItemIndex">
              <td>{{ purchaseOrderItemIndex + 1 }}</td>
              <td>{{ getProductName(purchaseOrderItem.productId) }}</td>
              <td>{{ purchaseOrderItem.quantity }}</td>
              <td>{{ purchaseOrderItem.price }}</td>
              <td>{{ purchaseOrderItem.amount }}</td>
            </tr>
          </tbody>
        </table>
        <el-row style="margin-top: 15px; font-size: 14px;">
          <el-col :span="24" style="text-align: right; padding-right: 10px;">
            <strong>总金额：<span style="font-size: 18px;">¥ {{ form.totalAmount || '0.00' }}</span></strong>
          </el-col>
        </el-row>
        <el-row style="margin-top: 30px; font-size: 14px;">
          <el-col :span="8"><strong>制单人：</strong>{{ form.createBy || '系统管理员' }}</el-col>
          <el-col :span="16"><strong>备注：</strong>{{ form.remark || '无' }}</el-col>
        </el-row>
      </div>
      <template #footer>
        <div class="dialog-footer">
          <el-button type="primary" v-print="'#printBox'">确 认 打 印</el-button>
          <el-button @click="printOpen = false">关 闭</el-button>
        </div>
      </template>
    </el-dialog>
  </div>
</template>

<script setup name="BusinessPurchaseOrder">
import { listPurchaseOrder, getPurchaseOrder, delPurchaseOrder, addPurchaseOrder, updatePurchaseOrder, auditPurchaseOrder, cancelPurchaseOrder } from "@/api/business/purchaseOrder"
import { listSupplier, getSupplier } from "@/api/business/supplier"
import { listProduct, getProduct } from "@/api/business/product"
import { appendUniqueSelectOption, buildSelectOptionList, normalizeRemoteKeyword } from "@/utils/remoteSelect"

const { proxy } = getCurrentInstance()

const purchaseOrderList = ref([])
const wmsPurchaseOrderItemList = ref([])
const open = ref(false)
const loading = ref(true)
const showSearch = ref(true)
const ids = ref([])
const checkedPurchaseOrderItemIndexList = ref([])
const total = ref(0)
const title = ref("")
const isView = ref(false)
const printOpen = ref(false)
const supplierList = ref([])
const productList = ref([])
const supplierListLoading = ref(false)
const productListLoading = ref(false)

const purchaseOrderStatusOptionList = [
  { label: "待审核", value: "0", type: "warning" },
  { label: "已审核", value: "1", type: "success" },
  { label: "部分入库", value: "2", type: "primary" },
  { label: "已完成", value: "3", type: "success" },
  { label: "已作废", value: "4", type: "danger" }
]

const data = reactive({
  form: {},
  queryParams: {
    pageNum: 1,
    pageSize: 10,
    purchaseOrderId: undefined,
    orderNo: undefined,
    supplierId: undefined,
    status: undefined
  },
  rules: {
    supplierId: [{ required: true, message: "供应商不能为空", trigger: "change" }],
    purchaseDate: [{ required: true, message: "采购日期不能为空", trigger: "change" }]
  }
})

const { queryParams, form, rules } = toRefs(data)

const supplierSelectOptionList = computed(() => buildSupplierSelectOptionList())
const productSelectOptionList = computed(() => buildProductSelectOptionList())
const canUpdateSelected = computed(() => ids.value.length === 1 && purchaseOrderList.value.some(purchaseOrder => purchaseOrder.purchaseOrderId === ids.value[0] && isPendingPurchaseOrderStatus(purchaseOrder.status)))
const canAuditSelected = computed(() => ids.value.length === 1 && purchaseOrderList.value.some(purchaseOrder => purchaseOrder.purchaseOrderId === ids.value[0] && isPendingPurchaseOrderStatus(purchaseOrder.status)))
const canDeleteSelected = computed(() => ids.value.length > 0 && purchaseOrderList.value.filter(purchaseOrder => ids.value.includes(purchaseOrder.purchaseOrderId)).every(purchaseOrder => isPendingPurchaseOrderStatus(purchaseOrder.status)))

// 从路由参数恢复筛选条件，方便首页和其他业务页跳转后直接定位采购订单。
function initializeQueryParamsFromRoute() {
  const routeQuery = proxy?.$route?.query || {}
  queryParams.value.purchaseOrderId = convertRouteNumberValue(routeQuery.purchaseOrderId)
  queryParams.value.orderNo = routeQuery.orderNo || undefined
  queryParams.value.supplierId = convertRouteNumberValue(routeQuery.supplierId)
  queryParams.value.status = routeQuery.status || undefined
}

// 将路由中的数字参数转换为数值类型，避免空字符串或异常值影响筛选。
function convertRouteNumberValue(routeValue) {
  if (routeValue === undefined || routeValue === null || routeValue === "") {
    return undefined
  }
  const numberValue = Number(routeValue)
  return Number.isNaN(numberValue) ? undefined : numberValue
}

// 初始化页面基础主数据，保证首次进入即可按名称搜索供应商和商品。
async function initBasicData() {
  await Promise.all([
    loadSupplierOptionList(),
    loadProductOptionList()
  ])
}

// 远程加载供应商下拉选项，避免一次性加载全部供应商导致页面卡顿。
async function loadSupplierOptionList(searchKeyword) {
  supplierListLoading.value = true
  try {
    const response = await listSupplier({
      pageNum: 1,
      pageSize: 20,
      supplierName: normalizeRemoteKeyword(searchKeyword)
    })
    const supplierRowList = response.rows || []
    supplierRowList.forEach(supplierItem => {
      appendUniqueSelectOption(supplierList.value, supplierItem, "supplierId")
    })
  } finally {
    supplierListLoading.value = false
  }
}

// 远程加载商品下拉选项，减少采购明细编辑时的初始加载压力。
async function loadProductOptionList(searchKeyword) {
  productListLoading.value = true
  try {
    const response = await listProduct({
      pageNum: 1,
      pageSize: 20,
      productName: normalizeRemoteKeyword(searchKeyword)
    })
    const productRowList = response.rows || []
    productRowList.forEach(productItem => {
      appendUniqueSelectOption(productList.value, productItem, "productId")
    })
  } finally {
    productListLoading.value = false
  }
}

// 按供应商编号补齐列表、弹窗和打印所需的供应商名称。
async function ensureSupplierOptionLoaded(supplierIdList) {
  const pendingSupplierIdList = [...new Set((supplierIdList || []).filter(supplierId => supplierId !== undefined && supplierId !== null && supplierId !== ""))]
  for (const supplierId of pendingSupplierIdList) {
    if (supplierList.value.some(supplier => supplier.supplierId === supplierId)) {
      continue
    }
    try {
      const response = await getSupplier(supplierId)
      appendUniqueSelectOption(supplierList.value, response.data, "supplierId")
    } catch (error) {
      appendUniqueSelectOption(supplierList.value, { supplierId, supplierName: `历史供应商ID：${supplierId}` }, "supplierId")
    }
  }
}

// 按商品编号补齐明细和打印所需的商品名称，兼容历史商品编号回显。
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
      appendUniqueSelectOption(productList.value, { productId, productName: `历史商品ID：${productId}` }, "productId")
    }
  }
}

// 批量补齐采购订单主单相关引用数据，保证列表和弹窗优先展示名称。
async function ensurePurchaseOrderReferenceOptionsLoaded(targetPurchaseOrderList = []) {
  const supplierIdList = [
    queryParams.value.supplierId,
    form.value.supplierId,
    ...targetPurchaseOrderList.map(purchaseOrder => purchaseOrder.supplierId)
  ]
  await ensureSupplierOptionLoaded(supplierIdList)
}

// 批量补齐采购订单明细相关引用数据，保证商品名称回显稳定。
async function ensurePurchaseOrderItemReferenceOptionsLoaded(targetPurchaseOrderItemList = []) {
  const productIdList = [
    ...wmsPurchaseOrderItemList.value.map(purchaseOrderItem => purchaseOrderItem.productId),
    ...targetPurchaseOrderItemList.map(purchaseOrderItem => purchaseOrderItem.productId)
  ]
  await ensureProductOptionLoaded(productIdList)
}

// 构造供应商下拉选项，并兼容历史供应商编号回显。
function buildSupplierSelectOptionList() {
  return buildSelectOptionList(supplierList.value, [queryParams.value.supplierId, form.value.supplierId], "supplierId", "supplierName", "历史供应商ID：")
}

// 构造商品下拉选项，并兼容采购明细中的历史商品编号回显。
function buildProductSelectOptionList() {
  return buildSelectOptionList(productList.value, wmsPurchaseOrderItemList.value.map(purchaseOrderItem => purchaseOrderItem.productId), "productId", "productName", "历史商品ID：")
}

// 根据供应商编号返回供应商名称，未命中主数据时显示历史编号。
function getSupplierName(supplierId) {
  if (supplierId === undefined || supplierId === null || supplierId === "") {
    return "-"
  }
  const supplier = supplierList.value.find(supplierItem => supplierItem.supplierId === supplierId)
  return supplier?.supplierName || `历史供应商ID：${supplierId}`
}

// 根据商品编号返回商品名称，未命中主数据时显示历史编号。
function getProductName(productId) {
  if (productId === undefined || productId === null || productId === "") {
    return "-"
  }
  const product = productList.value.find(productItem => productItem.productId === productId)
  return product?.productName || `历史商品ID：${productId}`
}

// 统一格式化采购数量相关字段，避免列表中出现空值或 undefined。
function formatPurchaseQuantity(quantityValue) {
  if (quantityValue === undefined || quantityValue === null || quantityValue === "") {
    return "0.00"
  }
  return Number(quantityValue).toFixed(2)
}

// 返回采购订单状态中文名称，让列表、弹窗和打印的状态展示保持一致。
function getPurchaseOrderStatusName(statusValue) {
  const purchaseOrderStatusOption = purchaseOrderStatusOptionList.find(optionItem => optionItem.value === statusValue)
  return purchaseOrderStatusOption?.label || statusValue || "未知状态"
}

// 返回采购订单状态标签类型，帮助用户快速识别流程阶段。
function getPurchaseOrderStatusType(statusValue) {
  const purchaseOrderStatusOption = purchaseOrderStatusOptionList.find(optionItem => optionItem.value === statusValue)
  return purchaseOrderStatusOption?.type || "info"
}

// 判断采购订单是否仍为待审核状态。
function isPendingPurchaseOrderStatus(statusValue) {
  return statusValue === "0"
}

// 判断采购订单当前是否允许作废。
function canCancelPurchaseOrderStatus(statusValue) {
  return statusValue === "0" || statusValue === "2"
}

// 判断当前采购订单是否已经生成应付，方便用户快速跳回应付台账。
function canViewLinkedPayable(statusValue) {
  return ["1", "2", "3"].includes(statusValue)
}

// 判断当前采购订单是否已进入采购入库阶段，方便用户查看后续入库单。
function canViewLinkedInbound(statusValue) {
  return ["1", "2", "3"].includes(statusValue)
}

// 判断当前采购订单弹窗是否允许编辑。
function isPurchaseOrderEditable() {
  return !isView.value
}

// 处理弹窗主按钮动作，编辑模式保存，只读模式关闭。
function handlePurchaseOrderDialogPrimaryAction() {
  if (isPurchaseOrderEditable()) {
    submitForm()
    return
  }
  cancel()
}

// 查询采购订单列表，并补齐供应商名称回显。
async function getList() {
  loading.value = true
  try {
    const response = await listPurchaseOrder(queryParams.value)
    purchaseOrderList.value = response.rows || []
    total.value = response.total || 0
    await ensurePurchaseOrderReferenceOptionsLoaded(purchaseOrderList.value)
  } finally {
    loading.value = false
  }
}

// 关闭弹窗并重置采购订单表单。
function cancel() {
  open.value = false
  reset()
}

// 重置采购订单主单和明细数据，保证再次打开弹窗时不残留上一次输入。
function reset() {
  form.value = {
    purchaseOrderId: undefined,
    orderNo: generatePurchaseOrderNo(),
    supplierId: undefined,
    purchaseDate: buildCurrentDateValue(),
    totalAmount: "0.00",
    status: "0",
    remark: undefined
  }
  wmsPurchaseOrderItemList.value = []
  checkedPurchaseOrderItemIndexList.value = []
  isView.value = false
  proxy.resetForm("purchaseOrderRef")
}

// 生成默认采购单号，保持系统当前以时间戳生成单号的方式。
function generatePurchaseOrderNo() {
  return `PO${Date.now()}`
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

// 重置查询条件并重新拉取采购订单列表。
function resetQuery() {
  proxy.resetForm("queryRef")
  queryParams.value.purchaseOrderId = undefined
  handleQuery()
}

// 同步表格选中状态，让顶部按钮和行内按钮遵循同一套规则。
function handleSelectionChange(selection) {
  ids.value = selection.map(purchaseOrder => purchaseOrder.purchaseOrderId)
}

// 打开新增采购订单弹窗。
function handleAdd() {
  reset()
  open.value = true
  title.value = "新增采购订单"
}

// 打开编辑采购订单弹窗，并补齐供应商和商品名称回显。
async function handleUpdate(row) {
  reset()
  const purchaseOrderId = row?.purchaseOrderId || ids.value[0]
  if (!purchaseOrderId) {
    return
  }
  const response = await getPurchaseOrder(purchaseOrderId)
  form.value = response.data
  wmsPurchaseOrderItemList.value = response.data.wmsPurchaseOrderItemList || []
  checkedPurchaseOrderItemIndexList.value = []
  calculateTotal()
  await Promise.all([
    ensurePurchaseOrderReferenceOptionsLoaded([form.value]),
    ensurePurchaseOrderItemReferenceOptionsLoaded(wmsPurchaseOrderItemList.value)
  ])
  open.value = true
  title.value = "修改采购订单"
}

// 打开采购订单详情弹窗，只读展示主单和明细信息。
async function handleView(row) {
  reset()
  const purchaseOrderId = row.purchaseOrderId
  const response = await getPurchaseOrder(purchaseOrderId)
  form.value = response.data
  wmsPurchaseOrderItemList.value = response.data.wmsPurchaseOrderItemList || []
  checkedPurchaseOrderItemIndexList.value = []
  isView.value = true
  calculateTotal()
  await Promise.all([
    ensurePurchaseOrderReferenceOptionsLoaded([form.value]),
    ensurePurchaseOrderItemReferenceOptionsLoaded(wmsPurchaseOrderItemList.value)
  ])
  open.value = true
  title.value = "采购订单详情"
}

// 打开采购订单打印预览，并提前补齐供应商和商品名称。
async function handlePrint(row) {
  reset()
  const purchaseOrderId = row.purchaseOrderId
  const response = await getPurchaseOrder(purchaseOrderId)
  form.value = response.data
  wmsPurchaseOrderItemList.value = response.data.wmsPurchaseOrderItemList || []
  calculateTotal()
  await Promise.all([
    ensurePurchaseOrderReferenceOptionsLoaded([form.value]),
    ensurePurchaseOrderItemReferenceOptionsLoaded(wmsPurchaseOrderItemList.value)
  ])
  printOpen.value = true
}

// 跳转到应付台账，并按采购订单精准定位对应台账记录。
function handleViewPayable(row) {
  proxy.$router.push({
    path: "/finance/payable",
    query: {
      purchaseOrderId: row.purchaseOrderId,
      orderNo: row.orderNo
    }
  })
}

// 跳转到供应商资料页，并按来源供应商自动带出查询条件，方便采购补看主数据。
function handleViewSupplier(row) {
  const purchaseOrderRow = row || purchaseOrderList.value.find(purchaseOrder => purchaseOrder.purchaseOrderId === ids.value[0])
  if (!purchaseOrderRow?.supplierId) {
    return
  }
  proxy.$router.push({
    path: "/purchase/supplier",
    query: {
      supplierId: purchaseOrderRow.supplierId
    }
  })
}

// 跳转到采购入库页，并按供应商和采购入库类型缩小查询范围。
function handleViewInbound(row) {
  const hasRemainQuantity = Number(row.remainQuantity || 0) > 0
  proxy.$router.push({
    path: "/purchase/inbound",
    query: {
      purchaseOrderId: row.purchaseOrderId,
      purchaseOrderNo: row.orderNo,
      supplierId: row.supplierId,
      inboundType: "purchase",
      mode: hasRemainQuantity ? "create" : "list"
    }
  })
}

// 维护采购订单明细序号，方便删除和校验时定位行号。
function rowWmsPurchaseOrderItemIndex({ row, rowIndex }) {
  row.index = rowIndex + 1
}

// 新增采购订单明细行，并初始化默认数量、单价和金额。
function handleAddPurchaseOrderItem() {
  if (!isPurchaseOrderEditable()) {
    proxy.$modal.msgError("当前采购订单不可编辑")
    return
  }
  wmsPurchaseOrderItemList.value.push({
    productId: undefined,
    quantity: 1,
    price: 0,
    amount: "0.00"
  })
  calculateTotal()
}

// 删除选中的采购订单明细行。
function handleDeletePurchaseOrderItem() {
  if (!isPurchaseOrderEditable()) {
    proxy.$modal.msgError("当前采购订单不可编辑")
    return
  }
  if (checkedPurchaseOrderItemIndexList.value.length === 0) {
    proxy.$modal.msgError("请先选择要删除的采购订单明细数据")
    return
  }
  wmsPurchaseOrderItemList.value = wmsPurchaseOrderItemList.value.filter(purchaseOrderItem => !checkedPurchaseOrderItemIndexList.value.includes(purchaseOrderItem.index))
  checkedPurchaseOrderItemIndexList.value = []
  calculateTotal()
}

// 同步采购订单明细表格的选中序号。
function handlePurchaseOrderItemSelectionChange(selection) {
  checkedPurchaseOrderItemIndexList.value = selection.map(purchaseOrderItem => purchaseOrderItem.index)
}

// 按数量和单价计算采购订单明细金额，并同步刷新主单总金额。
function calculateAmount(row) {
  const quantityValue = Number(row.quantity || 0)
  const priceValue = Number(row.price || 0)
  row.amount = (quantityValue * priceValue).toFixed(2)
  calculateTotal()
}

// 汇总采购订单明细金额，保持主单总金额与明细同步。
function calculateTotal() {
  let totalAmountValue = 0
  wmsPurchaseOrderItemList.value.forEach(purchaseOrderItem => {
    totalAmountValue += Number(purchaseOrderItem.amount || 0)
  })
  form.value.totalAmount = totalAmountValue.toFixed(2)
}

// 提交采购订单表单，新增和修改都统一经过明细校验。
function submitForm() {
  proxy.$refs.purchaseOrderRef.validate(async valid => {
    if (!valid) {
      return
    }
    if (!isPurchaseOrderEditable()) {
      cancel()
      return
    }
    if (wmsPurchaseOrderItemList.value.length === 0) {
      proxy.$modal.msgError("请添加采购明细")
      return
    }
    for (let purchaseOrderItemIndex = 0; purchaseOrderItemIndex < wmsPurchaseOrderItemList.value.length; purchaseOrderItemIndex++) {
      const purchaseOrderItem = wmsPurchaseOrderItemList.value[purchaseOrderItemIndex]
      if (!purchaseOrderItem.productId) {
        proxy.$modal.msgError(`第${purchaseOrderItemIndex + 1}行请选择商品`)
        return
      }
    }
    form.value.wmsPurchaseOrderItemList = wmsPurchaseOrderItemList.value
    if (form.value.purchaseOrderId) {
      await updatePurchaseOrder(form.value)
      proxy.$modal.msgSuccess("修改成功")
    } else {
      await addPurchaseOrder(form.value)
      proxy.$modal.msgSuccess("新增成功")
    }
    open.value = false
    getList()
  })
}

// 删除采购订单，顶部批量和行内单个删除都走同一逻辑。
function handleDelete(row) {
  const purchaseOrderIds = row?.purchaseOrderId || ids.value
  proxy.$modal.confirm(`是否确认删除采购订单编号为"${Array.isArray(purchaseOrderIds) ? purchaseOrderIds.join("、") : purchaseOrderIds}"的数据项？`).then(function () {
    return delPurchaseOrder(purchaseOrderIds)
  }).then(() => {
    getList()
    proxy.$modal.msgSuccess("删除成功")
  }).catch(() => {})
}

// 审核采购订单，审核通过后由系统自动生成应付记录。
function handleAudit(row) {
  const targetPurchaseOrder = row || purchaseOrderList.value.find(purchaseOrder => purchaseOrder.purchaseOrderId === ids.value[0])
  if (!targetPurchaseOrder) {
    return
  }
  proxy.$modal.confirm(`是否确认审核采购订单编号为"${targetPurchaseOrder.orderNo}"的数据项？`).then(function () {
    return auditPurchaseOrder(targetPurchaseOrder.purchaseOrderId)
  }).then(() => {
    getList()
    proxy.$modal.msgSuccess("审核成功")
  }).catch(() => {})
}

// 作废采购订单，避免已完成和已作废订单重复进入作废流程。
function handleCancel(row) {
  proxy.$modal.confirm(`是否确认作废采购订单编号为"${row.orderNo}"的数据项？`).then(function () {
    return cancelPurchaseOrder(row.purchaseOrderId)
  }).then(() => {
    getList()
    proxy.$modal.msgSuccess("作废成功")
  }).catch(() => {})
}

// 导出采购订单列表，保留当前筛选条件。
function handleExport() {
  proxy.download("business/purchaseOrder/export", {
    ...queryParams.value
  }, `purchaseOrder_${Date.now()}.xlsx`)
}

initializeQueryParamsFromRoute()
initBasicData()
getList()
</script>
