<template>
  <div class="app-container">
    <el-form :model="queryParams" ref="queryRef" :inline="true" v-show="showSearch">
      <el-form-item label="退货单号" prop="returnNo">
        <el-input v-model="queryParams.returnNo" placeholder="请输入退货单号" clearable style="width: 220px" @keyup.enter="handleQuery" />
      </el-form-item>
      <el-form-item label="退货类型" prop="returnType">
        <el-input v-model="queryParams.returnType" placeholder="请输入退货类型" clearable style="width: 180px" @keyup.enter="handleQuery" />
      </el-form-item>
      <el-form-item label="供应商" prop="supplierId">
        <el-select v-model="queryParams.supplierId" placeholder="请选择供应商" clearable filterable remote reserve-keyword remote-show-suffix :remote-method="loadSupplierOptionList" :loading="supplierListLoading" style="width: 220px">
          <el-option
            v-for="supplier in supplierSelectOptionList"
            :key="supplier.supplierId"
            :label="supplier.supplierName"
            :value="supplier.supplierId"
          />
        </el-select>
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
          <el-option v-for="statusOption in purchaseReturnStatusOptionList" :key="statusOption.value" :label="statusOption.label" :value="statusOption.value" />
        </el-select>
      </el-form-item>
      <el-form-item>
        <el-button type="primary" icon="Search" @click="handleQuery">搜索</el-button>
        <el-button icon="Refresh" @click="resetQuery">重置</el-button>
      </el-form-item>
    </el-form>

    <el-alert
      title="采购退货审核后会自动扣减库存，并冲减供应商应付。草稿状态可维护，提交后仅支持查看。"
      type="info"
      :closable="false"
      class="mb8"
    />

    <el-row :gutter="10" class="mb8">
      <el-col :span="1.5">
        <el-button type="primary" plain icon="Plus" @click="handleAdd" v-hasPermi="['business:purchaseReturn:add']">新增</el-button>
      </el-col>
      <el-col :span="1.5">
        <el-button type="success" plain icon="Edit" :disabled="!canUpdateSelected" @click="handleUpdate" v-hasPermi="['business:purchaseReturn:edit']">修改</el-button>
      </el-col>
      <el-col :span="1.5">
        <el-button type="info" plain icon="Upload" :disabled="!canSubmitSelected" @click="handleSubmit" v-hasPermi="['business:purchaseReturn:edit']">提交</el-button>
      </el-col>
      <el-col :span="1.5">
        <el-button type="warning" plain icon="CircleCheck" :disabled="!canAuditSelected" @click="handleAudit" v-hasPermi="['business:purchaseReturn:audit']">审核</el-button>
      </el-col>
      <el-col :span="1.5">
        <el-button type="danger" plain icon="Delete" :disabled="!canDeleteSelected" @click="handleDelete" v-hasPermi="['business:purchaseReturn:remove']">删除</el-button>
      </el-col>
      <el-col :span="1.5">
        <el-button type="warning" plain icon="Download" @click="handleExport" v-hasPermi="['business:purchaseReturn:export']">导出</el-button>
      </el-col>
      <right-toolbar v-model:showSearch="showSearch" @queryTable="getList"></right-toolbar>
    </el-row>

    <el-table v-loading="loading" :data="purchaseReturnList" @selection-change="handleSelectionChange">
      <el-table-column type="selection" width="55" align="center" />
      <el-table-column label="退货编号" align="center" prop="purchaseReturnId" />
      <el-table-column label="退货单号" align="center" prop="returnNo" min-width="180" />
      <el-table-column label="退货类型" align="center" prop="returnType" />
      <el-table-column label="供应商" align="center" prop="supplierId" min-width="180">
        <template #default="scope">
          <span>{{ getSupplierName(scope.row.supplierId) }}</span>
        </template>
      </el-table-column>
      <el-table-column label="仓库" align="center" prop="warehouseId" min-width="180">
        <template #default="scope">
          <span>{{ getWarehouseName(scope.row.warehouseId) }}</span>
        </template>
      </el-table-column>
      <el-table-column label="总数量" align="center" prop="totalQty" />
      <el-table-column label="总金额" align="center" prop="totalAmount" />
      <el-table-column label="状态" align="center" prop="status">
        <template #default="scope">
          <el-tag :type="getPurchaseReturnStatusType(scope.row.status)">{{ getPurchaseReturnStatusLabel(scope.row.status) }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column label="创建时间" align="center" prop="createTime" width="180">
        <template #default="scope">
          <span>{{ parseTime(scope.row.createTime) }}</span>
        </template>
      </el-table-column>
      <el-table-column label="操作" width="300" align="center" class-name="small-padding fixed-width">
        <template #default="scope">
          <el-button link type="primary" icon="List" @click="handleOpenPurchaseReturnItem(scope.row)" v-hasPermi="['business:purchaseReturn:query']">明细</el-button>
          <el-button v-if="isDraftPurchaseReturnStatus(scope.row.status)" link type="primary" icon="Edit" @click="handleUpdate(scope.row)" v-hasPermi="['business:purchaseReturn:edit']">修改</el-button>
          <el-button v-if="isDraftPurchaseReturnStatus(scope.row.status)" link type="primary" icon="Upload" @click="handleSubmit(scope.row)" v-hasPermi="['business:purchaseReturn:edit']">提交</el-button>
          <el-button v-if="isSubmittedPurchaseReturnStatus(scope.row.status)" link type="success" icon="CircleCheck" @click="handleAudit(scope.row)" v-hasPermi="['business:purchaseReturn:audit']">审核</el-button>
          <el-button v-if="canCancelPurchaseReturnStatus(scope.row.status)" link type="warning" icon="Close" @click="handleCancelOrder(scope.row)" v-hasPermi="['business:purchaseReturn:edit']">作废</el-button>
          <el-button v-if="isDraftPurchaseReturnStatus(scope.row.status)" link type="primary" icon="Delete" @click="handleDelete(scope.row)" v-hasPermi="['business:purchaseReturn:remove']">删除</el-button>
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
        title="采购退货主单的状态和审核人由流程自动维护，当前弹窗仅允许草稿单调整基础信息。"
        type="info"
        :closable="false"
        class="mb8"
      />
      <el-form ref="purchaseReturnRef" :model="form" :rules="rules" label-width="110px">
        <el-row>
          <el-col :span="12">
            <el-form-item label="退货类型" prop="returnType">
              <el-input v-model="form.returnType" placeholder="请输入退货类型" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="供应商" prop="supplierId">
              <el-select v-model="form.supplierId" placeholder="请选择供应商" clearable filterable remote reserve-keyword remote-show-suffix :remote-method="loadSupplierOptionList" :loading="supplierListLoading" style="width: 100%">
                <el-option
                  v-for="supplier in supplierSelectOptionList"
                  :key="supplier.supplierId"
                  :label="supplier.supplierName"
                  :value="supplier.supplierId"
                />
              </el-select>
            </el-form-item>
          </el-col>
        </el-row>
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
                <el-option v-for="statusOption in purchaseReturnStatusOptionList" :key="statusOption.value" :label="statusOption.label" :value="statusOption.value" />
              </el-select>
            </el-form-item>
          </el-col>
        </el-row>
        <el-row>
          <el-col :span="12">
            <el-form-item label="总数量" prop="totalQty">
              <el-input-number v-model="form.totalQty" :min="0" :precision="2" controls-position="right" style="width: 100%" :disabled="true" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="总金额" prop="totalAmount">
              <el-input-number v-model="form.totalAmount" :min="0" :precision="2" controls-position="right" style="width: 100%" :disabled="true" />
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

    <el-dialog :title="purchaseReturnItemTitle" v-model="purchaseReturnItemOpen" width="980px" append-to-body>
      <el-alert
        v-if="!isPurchaseReturnItemEditable()"
        title="当前采购退货单已提交、已审核或已作废，明细仅支持查看。"
        type="info"
        :closable="false"
        class="mb8"
      />
      <el-row :gutter="10" class="mb8">
        <el-col :span="1.5">
          <el-button v-if="isPurchaseReturnItemEditable()" type="primary" plain icon="Plus" @click="handleAddPurchaseReturnItem" v-hasPermi="['business:purchaseReturn:add']">新增明细</el-button>
        </el-col>
        <el-col :span="1.5">
          <el-button v-if="isPurchaseReturnItemEditable()" type="danger" plain icon="Delete" :disabled="purchaseReturnItemMultiple" @click="handleDeletePurchaseReturnItem" v-hasPermi="['business:purchaseReturn:remove']">删除明细</el-button>
        </el-col>
      </el-row>
      <el-table v-loading="purchaseReturnItemLoading" :data="purchaseReturnItemList" @selection-change="handlePurchaseReturnItemSelectionChange">
        <el-table-column v-if="isPurchaseReturnItemEditable()" type="selection" width="55" align="center" />
        <el-table-column label="明细编号" align="center" prop="purchaseReturnItemId" />
        <el-table-column label="商品" align="center" prop="productId" min-width="180">
          <template #default="scope">
            <span>{{ getProductName(scope.row.productId) }}</span>
          </template>
        </el-table-column>
        <el-table-column label="数量" align="center" prop="quantity" />
        <el-table-column label="单价" align="center" prop="price" />
        <el-table-column label="金额" align="center" prop="amount" />
        <el-table-column v-if="isPurchaseReturnItemEditable()" label="操作" width="180" align="center">
          <template #default="scope">
            <el-button link type="primary" icon="Edit" @click="handleUpdatePurchaseReturnItem(scope.row)" v-hasPermi="['business:purchaseReturn:edit']">修改</el-button>
            <el-button link type="primary" icon="Delete" @click="handleDeletePurchaseReturnItem(scope.row)" v-hasPermi="['business:purchaseReturn:remove']">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-dialog>

    <el-dialog :title="purchaseReturnItemFormTitle" v-model="purchaseReturnItemFormOpen" width="760px" append-to-body>
      <el-form ref="purchaseReturnItemRef" :model="purchaseReturnItemForm" :rules="purchaseReturnItemRules" label-width="110px">
        <el-row>
          <el-col :span="12">
            <el-form-item label="商品" prop="productId">
              <el-select v-model="purchaseReturnItemForm.productId" placeholder="请选择商品" clearable filterable remote reserve-keyword remote-show-suffix :remote-method="loadProductOptionList" :loading="productListLoading" style="width: 100%">
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
            <el-form-item label="数量" prop="quantity">
              <el-input-number v-model="purchaseReturnItemForm.quantity" :min="0.01" :precision="2" controls-position="right" style="width: 100%" @change="calculatePurchaseReturnItemAmount" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-row>
          <el-col :span="12">
            <el-form-item label="单价" prop="price">
              <el-input-number v-model="purchaseReturnItemForm.price" :min="0" :precision="2" controls-position="right" style="width: 100%" @change="calculatePurchaseReturnItemAmount" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="金额" prop="amount">
              <el-input-number v-model="purchaseReturnItemForm.amount" :min="0" :precision="2" controls-position="right" style="width: 100%" disabled />
            </el-form-item>
          </el-col>
        </el-row>
      </el-form>
      <template #footer>
        <div class="dialog-footer">
          <el-button type="primary" @click="submitPurchaseReturnItemForm">确 定</el-button>
          <el-button @click="cancelPurchaseReturnItemForm">取 消</el-button>
        </div>
      </template>
    </el-dialog>
  </div>
</template>

<script setup name="BusinessPurchaseReturn">
import { listPurchaseReturn, getPurchaseReturn, addPurchaseReturn, updatePurchaseReturn, delPurchaseReturn, submitPurchaseReturn, auditPurchaseReturn, cancelPurchaseReturn } from "@/api/business/purchaseReturn"
import { listPurchaseReturnItem, getPurchaseReturnItem, addPurchaseReturnItem, updatePurchaseReturnItem, delPurchaseReturnItem } from "@/api/business/purchaseReturnItem"
import { listSupplier, getSupplier } from "@/api/business/supplier"
import { listWarehouse, getWarehouse } from "@/api/business/warehouse"
import { listProduct, getProduct } from "@/api/business/product"
import { appendUniqueSelectOption, buildSelectOptionList, normalizeRemoteKeyword } from "@/utils/remoteSelect"
import { parseTime } from "@/utils/ruoyi"

const { proxy } = getCurrentInstance()

const purchaseReturnList = ref([])
const supplierList = ref([])
const supplierListLoading = ref(false)
const warehouseList = ref([])
const warehouseListLoading = ref(false)
const productList = ref([])
const productListLoading = ref(false)
const open = ref(false)
const loading = ref(true)
const showSearch = ref(true)
const ids = ref([])
const total = ref(0)
const title = ref("")
const purchaseReturnItemOpen = ref(false)
const purchaseReturnItemLoading = ref(false)
const purchaseReturnItemTitle = ref("")
const purchaseReturnItemList = ref([])
const currentPurchaseReturnId = ref(undefined)
const currentPurchaseReturnStatus = ref(undefined)
const selectedPurchaseReturnItemIds = ref([])
const purchaseReturnItemMultiple = ref(true)
const purchaseReturnItemFormOpen = ref(false)
const purchaseReturnItemFormTitle = ref("")

const purchaseReturnStatusOptionList = [
  { label: "草稿", value: "draft", type: "info" },
  { label: "已提交", value: "submitted", type: "warning" },
  { label: "已审核", value: "audited", type: "success" },
  { label: "已作废", value: "cancelled", type: "danger" }
]

const data = reactive({
  form: {},
  purchaseReturnItemForm: {},
  queryParams: {
    pageNum: 1,
    pageSize: 10,
    returnNo: undefined,
    returnType: undefined,
    supplierId: undefined,
    warehouseId: undefined,
    status: undefined
  },
  rules: {
    returnType: [{ required: true, message: "退货类型不能为空", trigger: "blur" }],
    supplierId: [{ required: true, message: "供应商不能为空", trigger: "change" }],
    warehouseId: [{ required: true, message: "仓库不能为空", trigger: "change" }]
  },
  purchaseReturnItemRules: {
    productId: [{ required: true, message: "商品不能为空", trigger: "change" }],
    quantity: [{ required: true, message: "数量不能为空", trigger: "blur" }],
    price: [{ required: true, message: "单价不能为空", trigger: "blur" }]
  }
})

const { queryParams, form, rules, purchaseReturnItemForm, purchaseReturnItemRules } = toRefs(data)

const supplierSelectOptionList = computed(() => buildSupplierSelectOptionList())
const warehouseSelectOptionList = computed(() => buildWarehouseSelectOptionList())
const productSelectOptionList = computed(() => buildProductSelectOptionList())

const canUpdateSelected = computed(() => ids.value.length === 1 && purchaseReturnList.value.some(purchaseReturn => purchaseReturn.purchaseReturnId === ids.value[0] && isDraftPurchaseReturnStatus(purchaseReturn.status)))
const canSubmitSelected = computed(() => ids.value.length === 1 && purchaseReturnList.value.some(purchaseReturn => purchaseReturn.purchaseReturnId === ids.value[0] && isDraftPurchaseReturnStatus(purchaseReturn.status)))
const canAuditSelected = computed(() => ids.value.length === 1 && purchaseReturnList.value.some(purchaseReturn => purchaseReturn.purchaseReturnId === ids.value[0] && isSubmittedPurchaseReturnStatus(purchaseReturn.status)))
const canDeleteSelected = computed(() => ids.value.length > 0 && purchaseReturnList.value.filter(purchaseReturn => ids.value.includes(purchaseReturn.purchaseReturnId)).every(purchaseReturn => isDraftPurchaseReturnStatus(purchaseReturn.status)))

// 初始化页面基础主数据，保证首次打开即可按名称搜索。
async function initBasicData() {
  await Promise.all([
    loadSupplierOptionList(),
    loadWarehouseOptionList()
  ])
}

// 从路由参数恢复筛选条件，方便其他页面跳转后直接定位数据。
function initializeQueryParamsFromRoute() {
  const routeQuery = proxy?.$route?.query || {}
  queryParams.value.supplierId = convertRouteNumberValue(routeQuery.supplierId)
  queryParams.value.warehouseId = convertRouteNumberValue(routeQuery.warehouseId)
  queryParams.value.status = routeQuery.status || undefined
  queryParams.value.returnNo = routeQuery.returnNo || undefined
  queryParams.value.returnType = routeQuery.returnType || undefined
}

// 规范路由中的数字参数，避免空字符串进入查询条件。
function convertRouteNumberValue(routeValue) {
  if (routeValue === undefined || routeValue === null || routeValue === "") {
    return undefined
  }
  const numberValue = Number(routeValue)
  return Number.isNaN(numberValue) ? undefined : numberValue
}

// 远程加载供应商下拉选项，减少大数据量下拉卡顿。
async function loadSupplierOptionList(searchKeyword) {
  supplierListLoading.value = true
  try {
    const normalizedKeyword = normalizeRemoteKeyword(searchKeyword)
    const response = await listSupplier({
      pageNum: 1,
      pageSize: 20,
      supplierName: normalizedKeyword
    })
  supplierList.value = normalizedKeyword ? response.rows || [] : buildSelectOptionList(response.rows || [], [], "supplierId", "supplierName", () => "供应商资料缺失")
  } finally {
    supplierListLoading.value = false
  }
}

// 远程加载仓库下拉选项，保持与仓储模块一致的交互方式。
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

// 远程加载商品下拉选项，采购退货明细按名称检索商品。
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

// 按供应商编号补齐当前页面需要展示的供应商名称。
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
      appendUniqueSelectOption(supplierList.value, { supplierId, supplierName: "供应商资料缺失" }, "supplierId")
    }
  }
}

// 按仓库编号补齐页面需要展示的仓库名称。
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

// 按商品编号补齐采购退货明细的商品名称。
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

// 批量补齐主单相关引用数据，保证列表和弹窗都以名称展示。
async function ensurePurchaseReturnReferenceOptionsLoaded(targetPurchaseReturnList = []) {
  const supplierIdList = [
    queryParams.value.supplierId,
    form.value.supplierId,
    ...targetPurchaseReturnList.map(purchaseReturn => purchaseReturn.supplierId)
  ]
  const warehouseIdList = [
    queryParams.value.warehouseId,
    form.value.warehouseId,
    ...targetPurchaseReturnList.map(purchaseReturn => purchaseReturn.warehouseId)
  ]
  await Promise.all([
    ensureSupplierOptionLoaded(supplierIdList),
    ensureWarehouseOptionLoaded(warehouseIdList)
  ])
}

// 批量补齐采购退货明细商品名称。
async function ensurePurchaseReturnItemReferenceOptionsLoaded(targetPurchaseReturnItemList = []) {
  const productIdList = [
    purchaseReturnItemForm.value.productId,
    ...targetPurchaseReturnItemList.map(purchaseReturnItem => purchaseReturnItem.productId)
  ]
  await ensureProductOptionLoaded(productIdList)
}

// 构造供应商下拉列表，并兼容主数据缺失时的兜底回显。
function buildSupplierSelectOptionList() {
  return buildSelectOptionList(supplierList.value, [queryParams.value.supplierId, form.value.supplierId], "supplierId", "supplierName", () => "供应商资料缺失")
}

// 构造仓库下拉列表，并兼容主数据缺失时的兜底回显。
function buildWarehouseSelectOptionList() {
  return buildSelectOptionList(warehouseList.value, [queryParams.value.warehouseId, form.value.warehouseId], "warehouseId", "warehouseName", () => "仓库资料缺失")
}

// 构造商品下拉列表，并兼容主数据缺失时的兜底回显。
function buildProductSelectOptionList() {
  return buildSelectOptionList(productList.value, [purchaseReturnItemForm.value.productId], "productId", "productName", () => "商品资料缺失")
}

// 通过供应商编号返回供应商名称，未命中主数据时显示资料缺失提示。
function getSupplierName(supplierId) {
  if (supplierId === undefined || supplierId === null || supplierId === "") {
    return "-"
  }
  const supplier = supplierList.value.find(supplierItem => supplierItem.supplierId === supplierId)
  return supplier?.supplierName || "供应商资料缺失"
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

// 将采购退货状态编码转换为中文文案。
function getPurchaseReturnStatusLabel(statusValue) {
  const statusOption = purchaseReturnStatusOptionList.find(optionItem => optionItem.value === statusValue)
  return statusOption?.label || statusValue || "未知状态"
}

// 根据采购退货状态返回标签类型，帮助用户快速识别流程阶段。
function getPurchaseReturnStatusType(statusValue) {
  const statusOption = purchaseReturnStatusOptionList.find(optionItem => optionItem.value === statusValue)
  return statusOption?.type || "info"
}

// 判断采购退货主单是否仍为草稿状态。
function isDraftPurchaseReturnStatus(statusValue) {
  return statusValue === "draft"
}

// 判断采购退货主单是否已提交，可进入审核。
function isSubmittedPurchaseReturnStatus(statusValue) {
  return statusValue === "submitted"
}

// 判断采购退货主单当前是否允许作废。
function canCancelPurchaseReturnStatus(statusValue) {
  return statusValue !== "audited" && statusValue !== "cancelled"
}

// 判断采购退货明细是否允许编辑。
function isPurchaseReturnItemEditable() {
  return isDraftPurchaseReturnStatus(currentPurchaseReturnStatus.value)
}

// 在方法层校验采购退货明细是否允许维护。
function ensurePurchaseReturnItemEditable() {
  if (!isPurchaseReturnItemEditable()) {
    proxy.$modal.msgWarning("当前采购退货单仅支持查看明细")
    return false
  }
  return true
}

// 查询采购退货列表，并补齐供应商和仓库名称。
async function getList() {
  loading.value = true
  try {
    const response = await listPurchaseReturn(queryParams.value)
    purchaseReturnList.value = response.rows || []
    total.value = response.total || 0
    await ensurePurchaseReturnReferenceOptionsLoaded(purchaseReturnList.value)
  } finally {
    loading.value = false
  }
}

// 关闭主弹窗并重置表单。
function cancel() {
  open.value = false
  reset()
}

// 重置采购退货主单表单。
function reset() {
  form.value = {
    purchaseReturnId: undefined,
    returnNo: undefined,
    returnType: undefined,
    supplierId: undefined,
    warehouseId: undefined,
    totalQty: 0,
    totalAmount: 0,
    status: "draft",
    auditBy: undefined,
    auditTime: undefined,
    remark: undefined
  }
  proxy.resetForm("purchaseReturnRef")
}

// 重置采购退货明细表单，并回填当前主单编号。
function resetPurchaseReturnItemForm() {
  purchaseReturnItemForm.value = {
    purchaseReturnItemId: undefined,
    purchaseReturnId: currentPurchaseReturnId.value,
    productId: undefined,
    quantity: 1,
    price: 0,
    amount: 0
  }
  proxy.resetForm("purchaseReturnItemRef")
}

// 执行查询并回到第一页。
function handleQuery() {
  queryParams.value.pageNum = 1
  getList()
}

// 重置查询条件并重新加载列表。
function resetQuery() {
  proxy.resetForm("queryRef")
  handleQuery()
}

// 同步表格选中状态，让顶部按钮和行内按钮规则一致。
function handleSelectionChange(selection) {
  ids.value = selection.map(purchaseReturn => purchaseReturn.purchaseReturnId)
}

// 打开新增采购退货弹窗。
function handleAdd() {
  reset()
  open.value = true
  title.value = "新增采购退货单"
}

// 打开编辑采购退货弹窗，并补齐供应商和仓库回显。
async function handleUpdate(row) {
  reset()
  const purchaseReturnId = row?.purchaseReturnId || ids.value[0]
  if (!purchaseReturnId) {
    return
  }
  const response = await getPurchaseReturn(purchaseReturnId)
  form.value = response.data
  await ensurePurchaseReturnReferenceOptionsLoaded([form.value])
  open.value = true
  title.value = "修改采购退货单"
}

// 打开采购退货明细弹窗，并根据主单状态决定是否允许编辑。
function handleOpenPurchaseReturnItem(row) {
  currentPurchaseReturnId.value = row.purchaseReturnId
  currentPurchaseReturnStatus.value = row.status
  selectedPurchaseReturnItemIds.value = []
  purchaseReturnItemMultiple.value = true
  purchaseReturnItemTitle.value = `采购退货明细 - ${row.returnNo}（${getPurchaseReturnStatusLabel(row.status)}）`
  purchaseReturnItemOpen.value = true
  getPurchaseReturnItemList()
}

// 查询采购退货明细列表，并补齐商品名称。
async function getPurchaseReturnItemList() {
  if (!currentPurchaseReturnId.value) {
    purchaseReturnItemList.value = []
    return
  }
  purchaseReturnItemLoading.value = true
  try {
    const response = await listPurchaseReturnItem({
      pageNum: 1,
      pageSize: 1000,
      purchaseReturnId: currentPurchaseReturnId.value
    })
    purchaseReturnItemList.value = response.rows || []
    await ensurePurchaseReturnItemReferenceOptionsLoaded(purchaseReturnItemList.value)
  } finally {
    purchaseReturnItemLoading.value = false
  }
}

// 同步明细选中项，控制批量删除按钮状态。
function handlePurchaseReturnItemSelectionChange(selection) {
  selectedPurchaseReturnItemIds.value = selection.map(purchaseReturnItem => purchaseReturnItem.purchaseReturnItemId)
  purchaseReturnItemMultiple.value = !selection.length
}

// 打开新增采购退货明细弹窗。
function handleAddPurchaseReturnItem() {
  if (!ensurePurchaseReturnItemEditable()) {
    return
  }
  resetPurchaseReturnItemForm()
  purchaseReturnItemFormOpen.value = true
  purchaseReturnItemFormTitle.value = "新增采购退货明细"
}

// 打开编辑采购退货明细弹窗，并补齐商品回显。
async function handleUpdatePurchaseReturnItem(row) {
  if (!ensurePurchaseReturnItemEditable()) {
    return
  }
  resetPurchaseReturnItemForm()
  const response = await getPurchaseReturnItem(row.purchaseReturnItemId)
  purchaseReturnItemForm.value = response.data
  await ensurePurchaseReturnItemReferenceOptionsLoaded([purchaseReturnItemForm.value])
  purchaseReturnItemFormOpen.value = true
  purchaseReturnItemFormTitle.value = "修改采购退货明细"
}

// 关闭采购退货明细编辑弹窗并重置表单。
function cancelPurchaseReturnItemForm() {
  purchaseReturnItemFormOpen.value = false
  resetPurchaseReturnItemForm()
}

// 根据数量和单价自动计算金额，减少人工录入错误。
function calculatePurchaseReturnItemAmount() {
  const quantityValue = Number(purchaseReturnItemForm.value.quantity || 0)
  const priceValue = Number(purchaseReturnItemForm.value.price || 0)
  purchaseReturnItemForm.value.amount = Math.round(quantityValue * priceValue * 100) / 100
}

// 保存采购退货明细，并同步刷新主单汇总数据。
function submitPurchaseReturnItemForm() {
  if (!ensurePurchaseReturnItemEditable()) {
    return
  }
  calculatePurchaseReturnItemAmount()
  proxy.$refs["purchaseReturnItemRef"].validate(valid => {
    if (!valid) {
      return
    }
    purchaseReturnItemForm.value.purchaseReturnId = currentPurchaseReturnId.value
    const submitMethod = purchaseReturnItemForm.value.purchaseReturnItemId !== undefined ? updatePurchaseReturnItem : addPurchaseReturnItem
    submitMethod(purchaseReturnItemForm.value).then(() => {
      proxy.$modal.msgSuccess(purchaseReturnItemForm.value.purchaseReturnItemId !== undefined ? "修改成功" : "新增成功")
      purchaseReturnItemFormOpen.value = false
      getPurchaseReturnItemList()
      getList()
    })
  })
}

// 删除采购退货明细，并同步刷新主单汇总数据。
function handleDeletePurchaseReturnItem(row) {
  if (!ensurePurchaseReturnItemEditable()) {
    return
  }
  const purchaseReturnItemIds = row?.purchaseReturnItemId || selectedPurchaseReturnItemIds.value
  if (!purchaseReturnItemIds || purchaseReturnItemIds.length === 0) {
    return
  }
  proxy.$modal.confirm(`是否确认删除采购退货明细编号为"${purchaseReturnItemIds}"的数据项？`).then(function () {
    return delPurchaseReturnItem(purchaseReturnItemIds)
  }).then(() => {
    proxy.$modal.msgSuccess("删除成功")
    getPurchaseReturnItemList()
    getList()
  }).catch(() => {})
}

// 保存采购退货主单。
function submitForm() {
  proxy.$refs["purchaseReturnRef"].validate(valid => {
    if (!valid) {
      return
    }
    const submitMethod = form.value.purchaseReturnId !== undefined ? updatePurchaseReturn : addPurchaseReturn
    submitMethod(form.value).then(() => {
      proxy.$modal.msgSuccess(form.value.purchaseReturnId !== undefined ? "修改成功" : "新增成功")
      open.value = false
      getList()
    })
  })
}

// 提交采购退货主单进入审核流程。
function handleSubmit(row) {
  const purchaseReturnRow = row || purchaseReturnList.value.find(purchaseReturn => purchaseReturn.purchaseReturnId === ids.value[0])
  if (!purchaseReturnRow) {
    return
  }
  proxy.$modal.confirm(`是否确认提交采购退货单号为"${purchaseReturnRow.returnNo}"的数据项？`).then(function () {
    return submitPurchaseReturn(purchaseReturnRow.purchaseReturnId)
  }).then(() => {
    proxy.$modal.msgSuccess("提交成功")
    getList()
  }).catch(() => {})
}

// 审核采购退货主单，审核后会扣减库存并冲减应付。
function handleAudit(row) {
  const purchaseReturnRow = row || purchaseReturnList.value.find(purchaseReturn => purchaseReturn.purchaseReturnId === ids.value[0])
  if (!purchaseReturnRow) {
    return
  }
  proxy.$modal.confirm(`是否确认审核采购退货单号为"${purchaseReturnRow.returnNo}"的数据项？`).then(function () {
    return auditPurchaseReturn(purchaseReturnRow.purchaseReturnId)
  }).then(() => {
    proxy.$modal.msgSuccess("审核成功")
    getList()
  }).catch(() => {})
}

// 作废采购退货主单，避免异常退货单继续流转。
function handleCancelOrder(row) {
  proxy.$modal.confirm(`是否确认作废采购退货单号为"${row.returnNo}"的数据项？`).then(function () {
    return cancelPurchaseReturn(row.purchaseReturnId)
  }).then(() => {
    proxy.$modal.msgSuccess("作废成功")
    getList()
  }).catch(() => {})
}

// 删除采购退货主单，仅允许删除草稿单据。
function handleDelete(row) {
  const purchaseReturnIds = row?.purchaseReturnId || ids.value
  if (!purchaseReturnIds || purchaseReturnIds.length === 0) {
    return
  }
  proxy.$modal.confirm(`是否确认删除采购退货单编号为"${purchaseReturnIds}"的数据项？`).then(function () {
    return delPurchaseReturn(purchaseReturnIds)
  }).then(() => {
    proxy.$modal.msgSuccess("删除成功")
    getList()
  }).catch(() => {})
}

// 导出采购退货列表，沿用系统统一导出方式。
function handleExport() {
  proxy.download("business/purchaseReturn/export", {
    ...queryParams.value
  }, `purchaseReturn_${new Date().getTime()}.xlsx`)
}

// 初始化页面筛选条件和基础资料，保证重复跳转到采购退货页时列表会按最新参数刷新。
async function initializePage() {
  initializeQueryParamsFromRoute()
  await initBasicData()
  await getList()
}

// 监听同一路由下的查询参数变化，避免采购退货页沿用旧的供应商或仓库筛选。
watch(() => proxy?.$route?.fullPath, (currentRouteFullPath, previousRouteFullPath) => {
  if (currentRouteFullPath === previousRouteFullPath) {
    return
  }
  initializePage()
})

initializePage()
</script>
