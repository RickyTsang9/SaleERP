<template>
  <div class="app-container erp-page">
    <erp-page-header
      eyebrow="销售履约中心 · 售后退货"
      title="销售退货管理"
      subtitle="处理客户退回商品的质检登记与审批，审核通过后自动回补库存并冲减客户应收余额。"
      icon="RefreshLeft"
      tone="rose"
      :badge="`共 ${total} 笔退货单`"
    >
      <template #actions>
        <el-button icon="Download" plain @click="handleExport" v-hasPermi="['business:saleReturn:export']">导出</el-button>
        <el-button type="primary" icon="Plus" @click="handleAdd" v-hasPermi="['business:saleReturn:add']">新建退货单</el-button>
      </template>
    </erp-page-header>

    <erp-stat-strip :items="saleReturnStatItems" :columns="4" />

    <section class="erp-filter-card" v-show="showSearch">
      <el-form :model="queryParams" ref="queryRef" :inline="true">
        <el-form-item label="退货单号" prop="returnNo">
          <el-input v-model="queryParams.returnNo" placeholder="请输入退货单号" clearable style="width: 195px" @keyup.enter="handleQuery" />
        </el-form-item>
        <el-form-item label="客户" prop="customerId">
          <el-select v-model="queryParams.customerId" placeholder="请选择客户" clearable filterable remote reserve-keyword remote-show-suffix :remote-method="loadCustomerOptionList" :loading="customerListLoading" style="width: 200px">
            <el-option v-for="customer in customerSelectOptionList" :key="customer.customerId" :label="customer.customerName" :value="customer.customerId" />
          </el-select>
        </el-form-item>
        <el-form-item label="仓库" prop="warehouseId">
          <el-select v-model="queryParams.warehouseId" placeholder="请选择仓库" clearable filterable remote reserve-keyword remote-show-suffix :remote-method="loadWarehouseOptionList" :loading="warehouseListLoading" style="width: 185px">
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
          <erp-status-tabs :model-value="queryParams.status ?? ''" :items="saleReturnStatusTabItems" @change="handleQuickStatus" />
        </div>
        <div class="erp-toolbar-right">
          <el-button type="success" plain icon="Edit" :disabled="!canUpdateSelected" @click="handleUpdate" v-hasPermi="['business:saleReturn:edit']">修改</el-button>
          <el-button type="info" plain icon="Upload" :disabled="!canSubmitSelected" @click="handleSubmit" v-hasPermi="['business:saleReturn:edit']">提交</el-button>
          <el-button type="warning" plain icon="CircleCheck" :disabled="!canAuditSelected" @click="handleAudit" v-hasPermi="['business:saleReturn:audit']">审核</el-button>
          <el-button type="danger" plain icon="Delete" :disabled="!canDeleteSelected" @click="handleDelete" v-hasPermi="['business:saleReturn:remove']">删除</el-button>
          <right-toolbar v-model:showSearch="showSearch" @queryTable="getList" />
        </div>
      </div>

      <el-table v-loading="loading" :data="saleReturnList" @selection-change="handleSelectionChange">
        <el-table-column type="selection" width="50" align="center" />
        <el-table-column label="退货单号" align="center" prop="returnNo" min-width="175">
          <template #default="scope">
            <span class="erp-code-chip">{{ scope.row.returnNo }}</span>
            <div v-if="scope.row.saleOrderNo" class="erp-cell-sub">来源：{{ scope.row.saleOrderNo }}</div>
          </template>
        </el-table-column>
        <el-table-column label="客户 / 退回仓" align="left" min-width="200" show-overflow-tooltip>
          <template #default="scope">
            <div class="erp-cell-title">{{ scope.row.customerName || getCustomerName(scope.row.customerId) }}</div>
            <div class="erp-cell-sub">仓库：{{ scope.row.warehouseName || getWarehouseName(scope.row.warehouseId) }}</div>
          </template>
        </el-table-column>
        <el-table-column label="退货类型" align="center" prop="returnType" width="110">
          <template #default="scope">
            <el-tag size="small" type="info" effect="plain">{{ scope.row.returnType === 'sale_return' ? '销售退货' : (scope.row.returnType || '销售退货') }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="退货数量" align="right" prop="totalQty" width="95" />
        <el-table-column label="退货金额" align="right" prop="totalAmount" width="115">
          <template #default="scope">
            <span class="erp-amount is-danger">¥{{ Number(scope.row.totalAmount || 0).toFixed(2) }}</span>
          </template>
        </el-table-column>
        <el-table-column label="状态" align="center" prop="status" width="110">
          <template #default="scope">
            <el-tooltip v-if="scope.row.status === 'draft' && scope.row.rejectReason" :content="`驳回原因：${scope.row.rejectReason}`" placement="top">
              <el-tag type="danger" effect="light">已驳回待改</el-tag>
            </el-tooltip>
            <el-tag v-else :type="getSaleReturnStatusType(scope.row.status)" effect="light">{{ getSaleReturnStatusLabel(scope.row.status) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="创建时间" align="center" prop="createTime" width="160">
          <template #default="scope">
            <span>{{ parseTime(scope.row.createTime) }}</span>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="295" fixed="right" align="center">
          <template #default="scope">
            <el-button link type="primary" icon="List" @click="handleOpenSaleReturnItem(scope.row)" v-hasPermi="['business:saleReturn:query']">明细</el-button>
            <el-button link type="info" icon="Printer" @click="handlePrintSaleReturn(scope.row)">打印</el-button>
            <el-button v-if="isDraftSaleReturnStatus(scope.row.status)" link type="primary" icon="Edit" @click="handleUpdate(scope.row)" v-hasPermi="['business:saleReturn:edit']">修改</el-button>
            <el-button v-if="isDraftSaleReturnStatus(scope.row.status)" link type="primary" icon="Upload" @click="handleSubmit(scope.row)" v-hasPermi="['business:saleReturn:edit']">提交</el-button>
            <el-button v-if="isSubmittedSaleReturnStatus(scope.row.status)" link type="success" icon="CircleCheck" @click="handleAudit(scope.row)" v-hasPermi="['business:saleReturn:audit']">审核</el-button>
            <el-button v-if="isSubmittedSaleReturnStatus(scope.row.status)" link type="warning" icon="RefreshLeft" @click="handleReject(scope.row)" v-hasPermi="['business:saleReturn:audit']">驳回</el-button>
            <el-button v-if="canCancelSaleReturnStatus(scope.row.status)" link type="warning" icon="Close" @click="handleCancelOrder(scope.row)" v-hasPermi="['business:saleReturn:edit']">作废</el-button>
            <el-button v-if="isDraftSaleReturnStatus(scope.row.status)" link type="danger" icon="Delete" @click="handleDelete(scope.row)" v-hasPermi="['business:saleReturn:remove']">删除</el-button>
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
        title="销售退货主单的状态和审核人由流程自动维护，当前弹窗仅允许草稿单调整基础信息。"
        type="info"
        :closable="false"
        class="mb8"
      />
      <el-form ref="saleReturnRef" :model="form" :rules="rules" label-width="110px">
        <el-row>
          <el-col :span="12">
            <el-form-item label="来源销售单" prop="saleOrderId">
              <el-select v-model="form.saleOrderId" placeholder="可选：关联已审销售单防止超退" clearable filterable style="width: 100%" @change="handleSourceSaleOrderChange">
                <el-option
                  v-for="so in sourceSaleOrderList"
                  :key="so.saleOrderId"
                  :label="`${so.orderNo} | ${so.customerName || ''}`"
                  :value="so.saleOrderId"
                />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="退货类型" prop="returnType">
              <el-input v-model="form.returnType" placeholder="默认 sale_return" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-row>
          <el-col :span="12">
            <el-form-item label="客户" prop="customerId">
              <el-select v-model="form.customerId" placeholder="请选择客户" clearable filterable remote reserve-keyword remote-show-suffix :remote-method="loadCustomerOptionList" :loading="customerListLoading" :disabled="!!form.saleOrderId" style="width: 100%">
                <el-option
                  v-for="customer in customerSelectOptionList"
                  :key="customer.customerId"
                  :label="customer.customerName"
                  :value="customer.customerId"
                />
              </el-select>
            </el-form-item>
          </el-col>
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
        </el-row>
        <el-row>
          <el-col :span="12">
            <el-form-item label="状态" prop="status">
              <el-select v-model="form.status" style="width: 100%" placeholder="状态由流程自动维护" disabled>
                <el-option v-for="statusOption in saleReturnStatusOptionList" :key="statusOption.value" :label="statusOption.label" :value="statusOption.value" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="审核人" prop="auditBy">
              <el-input v-model="form.auditBy" placeholder="审核人由审核动作自动填写" disabled />
            </el-form-item>
          </el-col>
        </el-row>
        <el-row>
          <el-col :span="12">
            <el-form-item label="总数量" prop="totalQty">
              <el-input-number v-model="form.totalQty" :min="0" :precision="2" controls-position="right" style="width: 100%" disabled />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="总金额" prop="totalAmount">
              <el-input-number v-model="form.totalAmount" :min="0" :precision="2" controls-position="right" style="width: 100%" disabled />
            </el-form-item>
          </el-col>
        </el-row>
        <el-row>
          <el-col :span="24">
            <el-form-item label="备注" prop="remark">
              <el-input v-model="form.remark" placeholder="请输入备注" />
            </el-form-item>
          </el-col>
        </el-row>
        <template v-if="!form.saleReturnId">
          <el-divider content-position="left">退货商品明细（可选，支持一体化建单）</el-divider>
          <div style="margin-bottom: 8px; display: flex; justify-content: space-between; align-items: center">
            <el-button type="primary" plain size="small" icon="Plus" @click="handleAddInlineReturnItem">添加退货商品</el-button>
            <span class="erp-cell-sub">合计 {{ inlineReturnSummary.qty.toFixed(2) }} 件 · ¥{{ inlineReturnSummary.amount.toFixed(2) }}</span>
          </div>
          <el-table :data="inlineReturnItems" size="small" border>
            <el-table-column label="商品" min-width="190">
              <template #default="scope">
                <el-select v-model="scope.row.productId" placeholder="请选择商品" clearable filterable remote reserve-keyword remote-show-suffix :remote-method="loadProductOptionList" :loading="productListLoading" style="width: 100%" @change="val => handleInlineReturnProductChange(scope.row, val)">
                  <el-option v-for="product in productSelectOptionList" :key="product.productId" :label="product.productName" :value="product.productId" />
                </el-select>
              </template>
            </el-table-column>
            <el-table-column label="退入库位" width="130">
              <template #default="scope">
                <el-select v-model="scope.row.locationId" placeholder="库位" clearable filterable size="small" style="width: 115px">
                  <el-option v-for="loc in locationList" :key="loc.locationId" :label="loc.locationName" :value="loc.locationId" />
                </el-select>
              </template>
            </el-table-column>
            <el-table-column label="批次号" width="115">
              <template #default="scope">
                <el-input v-model="scope.row.batchNo" placeholder="批次" size="small" />
              </template>
            </el-table-column>
            <el-table-column label="数量" width="108" align="center">
              <template #default="scope">
                <el-input-number v-model="scope.row.quantity" :min="0.01" :precision="2" size="small" controls-position="right" style="width: 94px" @change="recalcInlineReturnTotals" />
              </template>
            </el-table-column>
            <el-table-column label="单价" width="112" align="center">
              <template #default="scope">
                <el-input-number v-model="scope.row.price" :min="0" :precision="2" size="small" controls-position="right" style="width: 98px" @change="recalcInlineReturnTotals" />
              </template>
            </el-table-column>
            <el-table-column label="操作" width="56" align="center">
              <template #default="scope">
                <el-button link type="danger" icon="Delete" @click="handleRemoveInlineReturnItem(scope.$index)" />
              </template>
            </el-table-column>
          </el-table>
        </template>
      </el-form>
      <template #footer>
        <div class="dialog-footer">
          <el-button type="primary" @click="submitForm">确 定</el-button>
          <el-button @click="cancel">取 消</el-button>
        </div>
      </template>
    </el-dialog>

    <el-dialog :title="saleReturnItemTitle" v-model="saleReturnItemOpen" width="980px" append-to-body>
      <el-alert
        v-if="!isSaleReturnItemEditable()"
        title="当前销售退货单已提交、已审核或已作废，明细仅支持查看。"
        type="info"
        :closable="false"
        class="mb8"
      />
      <el-row :gutter="10" class="mb8">
        <el-col :span="1.5">
          <el-button v-if="isSaleReturnItemEditable()" type="primary" plain icon="Plus" @click="handleAddSaleReturnItem" v-hasPermi="['business:saleReturn:add']">新增明细</el-button>
        </el-col>
        <el-col :span="1.5">
          <el-button v-if="isSaleReturnItemEditable()" type="danger" plain icon="Delete" :disabled="saleReturnItemMultiple" @click="handleDeleteSaleReturnItem" v-hasPermi="['business:saleReturn:remove']">删除明细</el-button>
        </el-col>
      </el-row>
      <el-table v-loading="saleReturnItemLoading" :data="saleReturnItemList" @selection-change="handleSaleReturnItemSelectionChange">
        <el-table-column v-if="isSaleReturnItemEditable()" type="selection" width="55" align="center" />
        <el-table-column label="明细编号" align="center" prop="saleReturnItemId" />
        <el-table-column label="商品" align="center" prop="productId" min-width="180">
          <template #default="scope">
            <span>{{ getProductName(scope.row.productId) }}</span>
          </template>
        </el-table-column>
        <el-table-column label="退入库位" align="center" prop="locationId" min-width="120">
          <template #default="scope">
            <span>{{ getLocationName(scope.row.locationId) }}</span>
          </template>
        </el-table-column>
        <el-table-column label="批次号" align="center" prop="batchNo" min-width="120">
          <template #default="scope">
            <span>{{ scope.row.batchNo || '-' }}</span>
          </template>
        </el-table-column>
        <el-table-column label="数量" align="center" prop="quantity" />
        <el-table-column label="单价" align="center" prop="price" />
        <el-table-column label="金额" align="center" prop="amount" />
        <el-table-column v-if="isSaleReturnItemEditable()" label="操作" width="180" align="center">
          <template #default="scope">
            <el-button link type="primary" icon="Edit" @click="handleUpdateSaleReturnItem(scope.row)" v-hasPermi="['business:saleReturn:edit']">修改</el-button>
            <el-button link type="primary" icon="Delete" @click="handleDeleteSaleReturnItem(scope.row)" v-hasPermi="['business:saleReturn:remove']">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-dialog>

    <el-dialog :title="saleReturnItemFormTitle" v-model="saleReturnItemFormOpen" width="760px" append-to-body>
      <el-form ref="saleReturnItemRef" :model="saleReturnItemForm" :rules="saleReturnItemRules" label-width="110px">
        <el-row>
          <el-col :span="12">
            <el-form-item label="商品" prop="productId">
              <el-select v-model="saleReturnItemForm.productId" placeholder="请选择商品" clearable filterable remote reserve-keyword remote-show-suffix :remote-method="loadProductOptionList" :loading="productListLoading" style="width: 100%">
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
              <el-input-number v-model="saleReturnItemForm.quantity" :min="0.01" :precision="2" controls-position="right" style="width: 100%" @change="calculateSaleReturnItemAmount" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-row>
          <el-col :span="12">
            <el-form-item label="退入库位" prop="locationId">
              <el-select v-model="saleReturnItemForm.locationId" placeholder="可选：指定退回上架库位" clearable filterable style="width: 100%">
                <el-option v-for="loc in locationList" :key="loc.locationId" :label="loc.locationName" :value="loc.locationId" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="退回批次号" prop="batchNo">
              <el-input v-model="saleReturnItemForm.batchNo" placeholder="可选：原批次号（保留效期）" clearable />
            </el-form-item>
          </el-col>
        </el-row>
        <el-row>
          <el-col :span="12">
            <el-form-item label="单价" prop="price">
              <el-input-number v-model="saleReturnItemForm.price" :min="0" :precision="2" controls-position="right" style="width: 100%" @change="calculateSaleReturnItemAmount" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="金额" prop="amount">
              <el-input-number v-model="saleReturnItemForm.amount" :min="0" :precision="2" controls-position="right" style="width: 100%" disabled />
            </el-form-item>
          </el-col>
        </el-row>
      </el-form>
      <template #footer>
        <div class="dialog-footer">
          <el-button type="primary" @click="submitSaleReturnItemForm">确 定</el-button>
          <el-button @click="cancelSaleReturnItemForm">取 消</el-button>
        </div>
      </template>
    </el-dialog>

    <document-print-dialog ref="printDialogRef" />
  </div>
</template>

<script setup name="BusinessSaleReturn">
import DocumentPrintDialog from "@/components/DocumentPrintDialog/index.vue"
import { listSaleReturn, getSaleReturn, addSaleReturn, updateSaleReturn, delSaleReturn, submitSaleReturn, auditSaleReturn, rejectSaleReturn, cancelSaleReturn } from "@/api/business/saleReturn"
import { listSaleReturnItem, getSaleReturnItem, addSaleReturnItem, updateSaleReturnItem, delSaleReturnItem } from "@/api/business/saleReturnItem"
import { listSaleOrder } from "@/api/business/saleOrder"
import { listCustomer, getCustomer } from "@/api/business/customer"
import { listWarehouse, getWarehouse } from "@/api/business/warehouse"
import { listLocation } from "@/api/business/location"
import { listProduct, getProduct } from "@/api/business/product"
import { appendUniqueSelectOption, buildSelectOptionList, normalizeRemoteKeyword } from "@/utils/remoteSelect"
import { parseTime } from "@/utils/ruoyi"

const { proxy } = getCurrentInstance()

const saleReturnList = ref([])
const printDialogRef = ref(null)
const inlineReturnItems = ref([])
const inlineReturnSummary = computed(() => {
  const rows = inlineReturnItems.value || []
  const qty = rows.reduce((s, r) => s + Number(r.quantity || 0), 0)
  const amount = rows.reduce((s, r) => s + Number(r.quantity || 0) * Number(r.price || 0), 0)
  return { qty, amount }
})

function handleAddInlineReturnItem() {
  inlineReturnItems.value.push({ productId: undefined, locationId: undefined, batchNo: "", quantity: 1, price: 0 })
}

function handleRemoveInlineReturnItem(idx) {
  inlineReturnItems.value.splice(idx, 1)
  recalcInlineReturnTotals()
}

function handleInlineReturnProductChange(row, productId) {
  const prod = productList.value.find(p => p.productId === productId)
  if (prod) {
    row.price = Number(prod.salePrice || 0)
  }
  recalcInlineReturnTotals()
}

function recalcInlineReturnTotals() {
  if (!form.value.saleReturnId) {
    form.value.totalQty = Number(inlineReturnSummary.value.qty.toFixed(2))
    form.value.totalAmount = Number(inlineReturnSummary.value.amount.toFixed(2))
  }
}

async function handlePrintSaleReturn(row) {
  const res = await listSaleReturnItem({ pageNum: 1, pageSize: 500, saleReturnId: row.saleReturnId })
  const items = res.rows || []
  printDialogRef.value?.openPrint({
    docType: "saleReturn",
    title: "销售退货单",
    docNo: row.returnNo || `SR-${row.saleReturnId}`,
    defaultPaperMode: "triplicate",
    metaFields: [
      { label: "退货单号", value: row.returnNo || "-" },
      { label: "退货客户", value: row.customerName || getCustomerName(row.customerId) },
      { label: "退入仓库", value: row.warehouseName || getWarehouseName(row.warehouseId) },
      { label: "来源销售单", value: row.saleOrderNo || (row.saleOrderId ? `SO-${row.saleOrderId}` : "-") },
      { label: "单据状态", value: getSaleReturnStatusLabel(row.status) },
      { label: "制单日期", value: parseTime(row.createTime, "{y}-{m}-{d}") }
    ],
    columns: [
      { label: "商品名称", prop: "productName" },
      { label: "库位", prop: "locationName", width: "110px" },
      { label: "批次号", prop: "batchNo", width: "110px" },
      { label: "退货数量", prop: "quantity", width: "90px", align: "right" },
      { label: "退货单价", prop: "price", width: "100px", align: "right", type: "money", isPrice: true },
      { label: "退货金额", prop: "amount", width: "115px", align: "right", type: "money", isPrice: true }
    ],
    items: items.map(it => ({
      productName: it.productName || getProductName(it.productId),
      locationName: getLocationName(it.locationId),
      batchNo: it.batchNo || "-",
      quantity: it.quantity,
      price: it.price,
      amount: it.amount
    })),
    totalQty: row.totalQty,
    totalAmount: row.totalAmount,
    remark: row.remark || ""
  })
}
const customerList = ref([])
const customerListLoading = ref(false)
const warehouseList = ref([])
const warehouseListLoading = ref(false)
const productList = ref([])
const productListLoading = ref(false)
const sourceSaleOrderList = ref([])
const locationList = ref([])
const currentSaleReturnWarehouseId = ref(undefined)
const open = ref(false)
const loading = ref(true)
const showSearch = ref(true)
const ids = ref([])
const total = ref(0)
const title = ref("")
const saleReturnItemOpen = ref(false)
const saleReturnItemLoading = ref(false)
const saleReturnItemTitle = ref("")
const saleReturnItemList = ref([])
const currentSaleReturnId = ref(undefined)
const currentSaleReturnStatus = ref(undefined)
const selectedSaleReturnItemIds = ref([])
const saleReturnItemMultiple = ref(true)
const saleReturnItemFormOpen = ref(false)
const saleReturnItemFormTitle = ref("")

const saleReturnStatusOptionList = [
  { label: "草稿", value: "draft", type: "info" },
  { label: "已提交", value: "submitted", type: "warning" },
  { label: "已审核", value: "audited", type: "success" },
  { label: "已作废", value: "cancelled", type: "danger" }
]

const data = reactive({
  form: {},
  saleReturnItemForm: {},
  queryParams: {
    pageNum: 1,
    pageSize: 10,
    returnNo: undefined,
    returnType: undefined,
    customerId: undefined,
    warehouseId: undefined,
    status: undefined
  },
  rules: {
    returnType: [{ required: true, message: "退货类型不能为空", trigger: "blur" }],
    customerId: [{ required: true, message: "客户不能为空", trigger: "change" }],
    warehouseId: [{ required: true, message: "仓库不能为空", trigger: "change" }]
  },
  saleReturnItemRules: {
    productId: [{ required: true, message: "商品不能为空", trigger: "change" }],
    quantity: [{ required: true, message: "数量不能为空", trigger: "blur" }],
    price: [{ required: true, message: "单价不能为空", trigger: "blur" }]
  }
})

const { queryParams, form, rules, saleReturnItemForm, saleReturnItemRules } = toRefs(data)

const saleReturnStatusTabItems = [
  { label: "全部", value: "" },
  { label: "草稿", value: "draft", dot: "slate" },
  { label: "待审核", value: "submitted", dot: "amber" },
  { label: "已审核", value: "audited", dot: "emerald" },
  { label: "已作废", value: "cancelled", dot: "rose" }
]

const saleReturnStatItems = computed(() => {
  const rows = saleReturnList.value || []
  const draftCount = rows.filter(r => r.status === "draft").length
  const submittedCount = rows.filter(r => r.status === "submitted").length
  const pageAmount = rows.reduce((s, r) => s + Number(r.totalAmount || 0), 0)
  return [
    { label: "销售退货总数", value: total.value, suffix: "单", hint: "匹配当前筛选的全部退货单", icon: "RefreshLeft", tone: "rose" },
    { label: "本页草稿单", value: draftCount, suffix: "单", hint: "可补充退货商品与数量", icon: "EditPen", tone: "slate" },
    { label: "本页待审核", value: submittedCount, suffix: "单", hint: "审核后自动回库并冲应收", icon: "Stamp", tone: "amber" },
    { label: "本页退货总额", prefix: "¥", value: pageAmount.toFixed(2), hint: "本页客户退货金额合计", icon: "Money", tone: "indigo" }
  ]
})

function handleQuickStatus(val) {
  queryParams.value.status = val === "" ? undefined : val
  handleQuery()
}

const customerSelectOptionList = computed(() => buildCustomerSelectOptionList())
const warehouseSelectOptionList = computed(() => buildWarehouseSelectOptionList())
const productSelectOptionList = computed(() => buildProductSelectOptionList())

const canUpdateSelected = computed(() => ids.value.length === 1 && saleReturnList.value.some(saleReturn => saleReturn.saleReturnId === ids.value[0] && isDraftSaleReturnStatus(saleReturn.status)))
const canSubmitSelected = computed(() => ids.value.length === 1 && saleReturnList.value.some(saleReturn => saleReturn.saleReturnId === ids.value[0] && isDraftSaleReturnStatus(saleReturn.status)))
const canAuditSelected = computed(() => ids.value.length === 1 && saleReturnList.value.some(saleReturn => saleReturn.saleReturnId === ids.value[0] && isSubmittedSaleReturnStatus(saleReturn.status)))
const canDeleteSelected = computed(() => ids.value.length > 0 && saleReturnList.value.filter(saleReturn => ids.value.includes(saleReturn.saleReturnId)).every(saleReturn => isDraftSaleReturnStatus(saleReturn.status)))

// 初始化页面基础主数据，避免首次打开时下拉为空。
async function initBasicData() {
  await Promise.all([
    loadCustomerOptionList(),
    loadWarehouseOptionList()
  ])
}

// 从路由参数恢复筛选条件，保证看板或其他入口跳转后直接可用。
function initializeQueryParamsFromRoute() {
  const routeQuery = proxy?.$route?.query || {}
  queryParams.value.customerId = convertRouteNumberValue(routeQuery.customerId)
  queryParams.value.warehouseId = convertRouteNumberValue(routeQuery.warehouseId)
  queryParams.value.status = routeQuery.status || undefined
  queryParams.value.returnNo = routeQuery.returnNo || undefined
  queryParams.value.returnType = routeQuery.returnType || undefined
}

// 规范路由中的数字参数，避免空字符串影响下拉回显。
function convertRouteNumberValue(routeValue) {
  if (routeValue === undefined || routeValue === null || routeValue === "") {
    return undefined
  }
  const numberValue = Number(routeValue)
  return Number.isNaN(numberValue) ? undefined : numberValue
}

// 远程加载客户下拉选项，按需查询避免一次性拉全量客户。
async function loadCustomerOptionList(searchKeyword) {
  customerListLoading.value = true
  try {
    const normalizedKeyword = normalizeRemoteKeyword(searchKeyword)
    const response = await listCustomer({
      pageNum: 1,
      pageSize: 20,
      customerName: normalizedKeyword
    })
  customerList.value = normalizedKeyword ? response.rows || [] : buildSelectOptionList(response.rows || [], [], "customerId", "customerName", () => "客户资料缺失")
  } finally {
    customerListLoading.value = false
  }
}

// 远程加载仓库下拉选项，保持与其他业务页一致的名称搜索体验。
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

// 远程加载商品下拉选项，明细维护时按名称检索商品。
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

// 按客户编号补齐当前页需要展示的客户名称，避免列表出现裸编号。
async function ensureCustomerOptionLoaded(customerIdList) {
  const pendingCustomerIdList = [...new Set((customerIdList || []).filter(customerId => customerId !== undefined && customerId !== null && customerId !== ""))]
  for (const customerId of pendingCustomerIdList) {
    if (customerList.value.some(customer => customer.customerId === customerId)) {
      continue
    }
    try {
      const response = await getCustomer(customerId)
      appendUniqueSelectOption(customerList.value, response.data, "customerId")
    } catch (error) {
      appendUniqueSelectOption(customerList.value, { customerId, customerName: "客户资料缺失" }, "customerId")
    }
  }
}

// 按仓库编号补齐当前页需要展示的仓库名称，保证主表和弹窗都可读。
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

// 按商品编号补齐销售退货明细需要展示的商品名称。
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

// 批量补齐主单相关引用数据，优先复用后端关联字段，消除冗余 N+1 查询。
async function ensureSaleReturnReferenceOptionsLoaded(targetSaleReturnList = []) {
  const customerIdList = [queryParams.value.customerId, form.value.customerId]
  const warehouseIdList = [queryParams.value.warehouseId, form.value.warehouseId]
  ;(targetSaleReturnList || []).forEach(saleReturn => {
    if (!saleReturn) return
    if (saleReturn.customerId != null && saleReturn.customerName) {
      appendUniqueSelectOption(customerList.value, { customerId: saleReturn.customerId, customerName: saleReturn.customerName }, "customerId")
    } else if (saleReturn.customerId != null) {
      customerIdList.push(saleReturn.customerId)
    }
    if (saleReturn.warehouseId != null && saleReturn.warehouseName) {
      appendUniqueSelectOption(warehouseList.value, { warehouseId: saleReturn.warehouseId, warehouseName: saleReturn.warehouseName }, "warehouseId")
    } else if (saleReturn.warehouseId != null) {
      warehouseIdList.push(saleReturn.warehouseId)
    }
  })
  await Promise.all([
    ensureCustomerOptionLoaded(customerIdList),
    ensureWarehouseOptionLoaded(warehouseIdList)
  ])
}

// 补齐明细商品名称，避免退货明细仍然显示原始商品编号。
async function ensureSaleReturnItemReferenceOptionsLoaded(targetSaleReturnItemList = []) {
  const productIdList = [
    saleReturnItemForm.value.productId,
    ...targetSaleReturnItemList.map(saleReturnItem => saleReturnItem.productId)
  ]
  await ensureProductOptionLoaded(productIdList)
}

// 构造客户下拉列表，并对主数据缺失场景补充兜底占位。
function buildCustomerSelectOptionList() {
  return buildSelectOptionList(customerList.value, [queryParams.value.customerId, form.value.customerId], "customerId", "customerName", () => "客户资料缺失")
}

// 构造仓库下拉列表，并保证主数据缺失时可以稳定回显。
function buildWarehouseSelectOptionList() {
  return buildSelectOptionList(warehouseList.value, [queryParams.value.warehouseId, form.value.warehouseId], "warehouseId", "warehouseName", () => "仓库资料缺失")
}

// 构造商品下拉列表，并兼容主数据缺失时的兜底回显。
function buildProductSelectOptionList() {
  return buildSelectOptionList(productList.value, [saleReturnItemForm.value.productId], "productId", "productName", () => "商品资料缺失")
}

// 通过客户编号返回客户名称，未命中主数据时显示资料缺失提示。
function getCustomerName(customerId) {
  if (customerId === undefined || customerId === null || customerId === "") {
    return "-"
  }
  const customer = customerList.value.find(customerItem => customerItem.customerId === customerId)
  return customer?.customerName || "客户资料缺失"
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

// 将销售退货状态编码转换为中文文案。
function getSaleReturnStatusLabel(statusValue) {
  const statusOption = saleReturnStatusOptionList.find(optionItem => optionItem.value === statusValue)
  return statusOption?.label || statusValue || "未知状态"
}

// 根据销售退货状态返回标签类型，方便用户快速识别流程阶段。
function getSaleReturnStatusType(statusValue) {
  const statusOption = saleReturnStatusOptionList.find(optionItem => optionItem.value === statusValue)
  return statusOption?.type || "info"
}

// 判断当前销售退货是否仍为草稿，可继续维护。
function isDraftSaleReturnStatus(statusValue) {
  return statusValue === "draft"
}

// 判断当前销售退货是否已提交，允许执行审核动作。
function isSubmittedSaleReturnStatus(statusValue) {
  return statusValue === "submitted"
}

// 判断当前销售退货是否允许作废。
function canCancelSaleReturnStatus(statusValue) {
  return statusValue !== "audited" && statusValue !== "cancelled"
}

// 判断当前销售退货明细是否仍可编辑。
function isSaleReturnItemEditable() {
  return isDraftSaleReturnStatus(currentSaleReturnStatus.value)
}

// 在方法层兜底校验明细是否允许维护，防止绕过按钮直接调用。
function ensureSaleReturnItemEditable() {
  if (!isSaleReturnItemEditable()) {
    proxy.$modal.msgWarning("当前销售退货单仅支持查看明细")
    return false
  }
  return true
}

// 查询销售退货列表，并补齐客户、仓库名称展示数据。
async function getList() {
  loading.value = true
  try {
    const response = await listSaleReturn(queryParams.value)
    saleReturnList.value = response.rows || []
    total.value = response.total || 0
    await ensureSaleReturnReferenceOptionsLoaded(saleReturnList.value)
  } finally {
    loading.value = false
  }
}

// 关闭主弹窗并重置表单。
function cancel() {
  open.value = false
  reset()
}

function getLocationName(locationId) {
  if (!locationId) return "-"
  const loc = locationList.value.find(item => item.locationId === locationId)
  return loc?.locationName || `库位#${locationId}`
}

async function loadSourceSaleOrders() {
  try {
    const res = await listSaleOrder({ pageNum: 1, pageSize: 100, status: "audited" })
    sourceSaleOrderList.value = res.rows || []
  } catch (e) {}
}

function handleSourceSaleOrderChange(saleOrderId) {
  if (!saleOrderId) return
  const so = sourceSaleOrderList.value.find(item => item.saleOrderId === saleOrderId)
  if (so) {
    form.value.customerId = so.customerId
    form.value.warehouseId = so.warehouseId
    if (so.customerId && so.customerName) {
      appendUniqueSelectOption(customerList.value, { customerId: so.customerId, customerName: so.customerName }, "customerId")
    }
    if (so.warehouseId && so.warehouseName) {
      appendUniqueSelectOption(warehouseList.value, { warehouseId: so.warehouseId, warehouseName: so.warehouseName }, "warehouseId")
    }
  }
}

async function loadLocationsByWarehouse(warehouseId) {
  try {
    const res = await listLocation({ pageNum: 1, pageSize: 200, warehouseId: warehouseId || undefined, status: "0" })
    locationList.value = res.rows || []
  } catch (e) {}
}

// 重置销售退货主单表单，保持新增和编辑入口状态一致。
function reset() {
  form.value = {
    saleReturnId: undefined,
    saleOrderId: undefined,
    returnNo: undefined,
    returnType: "sale_return",
    customerId: undefined,
    warehouseId: undefined,
    totalQty: 0,
    totalAmount: 0,
    status: "draft",
    auditBy: undefined,
    auditTime: undefined,
    remark: undefined
  }
  inlineReturnItems.value = []
  proxy.resetForm("saleReturnRef")
}

// 重置销售退货明细表单，并回填当前主单编号。
function resetSaleReturnItemForm() {
  saleReturnItemForm.value = {
    saleReturnItemId: undefined,
    saleReturnId: currentSaleReturnId.value,
    productId: undefined,
    locationId: undefined,
    batchNo: "",
    quantity: 1,
    price: 0,
    amount: 0
  }
  proxy.resetForm("saleReturnItemRef")
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

// 同步表格选中状态，让顶部按钮与行内按钮保持一致。
function handleSelectionChange(selection) {
  ids.value = selection.map(saleReturn => saleReturn.saleReturnId)
}

// 打开新增销售退货弹窗。
function handleAdd() {
  reset()
  loadSourceSaleOrders()
  open.value = true
  title.value = "新增销售退货单"
}

// 打开编辑销售退货弹窗，并补齐客户、仓库回显选项。
async function handleUpdate(row) {
  reset()
  loadSourceSaleOrders()
  const saleReturnId = row?.saleReturnId || ids.value[0]
  if (!saleReturnId) {
    return
  }
  const response = await getSaleReturn(saleReturnId)
  form.value = response.data
  await ensureSaleReturnReferenceOptionsLoaded([form.value])
  open.value = true
  title.value = "修改销售退货单"
}

// 打开销售退货明细弹窗，并根据主单状态决定是否允许编辑。
function handleOpenSaleReturnItem(row) {
  currentSaleReturnId.value = row.saleReturnId
  currentSaleReturnStatus.value = row.status
  currentSaleReturnWarehouseId.value = row.warehouseId
  loadLocationsByWarehouse(row.warehouseId)
  selectedSaleReturnItemIds.value = []
  saleReturnItemMultiple.value = true
  saleReturnItemTitle.value = `销售退货明细 - ${row.returnNo}（${getSaleReturnStatusLabel(row.status)}）`
  saleReturnItemOpen.value = true
  getSaleReturnItemList()
}

// 查询销售退货明细列表，并补齐商品名称。
async function getSaleReturnItemList() {
  if (!currentSaleReturnId.value) {
    saleReturnItemList.value = []
    return
  }
  saleReturnItemLoading.value = true
  try {
    const response = await listSaleReturnItem({
      pageNum: 1,
      pageSize: 1000,
      saleReturnId: currentSaleReturnId.value
    })
    saleReturnItemList.value = response.rows || []
    await ensureSaleReturnItemReferenceOptionsLoaded(saleReturnItemList.value)
  } finally {
    saleReturnItemLoading.value = false
  }
}

// 同步明细选中项，控制批量删除按钮启用状态。
function handleSaleReturnItemSelectionChange(selection) {
  selectedSaleReturnItemIds.value = selection.map(saleReturnItem => saleReturnItem.saleReturnItemId)
  saleReturnItemMultiple.value = !selection.length
}

// 打开新增销售退货明细弹窗。
function handleAddSaleReturnItem() {
  if (!ensureSaleReturnItemEditable()) {
    return
  }
  resetSaleReturnItemForm()
  saleReturnItemFormOpen.value = true
  saleReturnItemFormTitle.value = "新增销售退货明细"
}

// 打开编辑销售退货明细弹窗，并补齐商品回显选项。
async function handleUpdateSaleReturnItem(row) {
  if (!ensureSaleReturnItemEditable()) {
    return
  }
  resetSaleReturnItemForm()
  const response = await getSaleReturnItem(row.saleReturnItemId)
  saleReturnItemForm.value = response.data
  await ensureSaleReturnItemReferenceOptionsLoaded([saleReturnItemForm.value])
  saleReturnItemFormOpen.value = true
  saleReturnItemFormTitle.value = "修改销售退货明细"
}

// 关闭销售退货明细编辑弹窗并重置表单。
function cancelSaleReturnItemForm() {
  saleReturnItemFormOpen.value = false
  resetSaleReturnItemForm()
}

// 根据数量和单价自动计算金额，减少用户手工输入错误。
function calculateSaleReturnItemAmount() {
  const quantityValue = Number(saleReturnItemForm.value.quantity || 0)
  const priceValue = Number(saleReturnItemForm.value.price || 0)
  saleReturnItemForm.value.amount = Math.round(quantityValue * priceValue * 100) / 100
}

// 保存销售退货明细，并刷新主表汇总数据。
function submitSaleReturnItemForm() {
  if (!ensureSaleReturnItemEditable()) {
    return
  }
  calculateSaleReturnItemAmount()
  proxy.$refs["saleReturnItemRef"].validate(valid => {
    if (!valid) {
      return
    }
    saleReturnItemForm.value.saleReturnId = currentSaleReturnId.value
    const submitMethod = saleReturnItemForm.value.saleReturnItemId !== undefined ? updateSaleReturnItem : addSaleReturnItem
    submitMethod(saleReturnItemForm.value).then(() => {
      proxy.$modal.msgSuccess(saleReturnItemForm.value.saleReturnItemId !== undefined ? "修改成功" : "新增成功")
      saleReturnItemFormOpen.value = false
      getSaleReturnItemList()
      getList()
    })
  })
}

// 删除销售退货明细，并同步刷新主单汇总。
function handleDeleteSaleReturnItem(row) {
  if (!ensureSaleReturnItemEditable()) {
    return
  }
  const saleReturnItemIds = row?.saleReturnItemId || selectedSaleReturnItemIds.value
  if (!saleReturnItemIds || saleReturnItemIds.length === 0) {
    return
  }
  proxy.$modal.confirm(`是否确认删除销售退货明细编号为"${saleReturnItemIds}"的数据项？`).then(function () {
    return delSaleReturnItem(saleReturnItemIds)
  }).then(() => {
    proxy.$modal.msgSuccess("删除成功")
    getSaleReturnItemList()
    getList()
  }).catch(() => {})
}

// 保存销售退货主单。
function submitForm() {
  proxy.$refs["saleReturnRef"].validate(valid => {
    if (!valid) {
      return
    }
    const isEdit = form.value.saleReturnId !== undefined
    const submitMethod = isEdit ? updateSaleReturn : addSaleReturn
    const payload = isEdit
      ? form.value
      : {
          ...form.value,
          items: (inlineReturnItems.value || [])
            .filter(it => it.productId && Number(it.quantity || 0) > 0)
            .map(it => ({
              productId: it.productId,
              locationId: it.locationId || undefined,
              batchNo: it.batchNo || undefined,
              quantity: Number(it.quantity),
              price: Number(it.price || 0)
            }))
        }
    submitMethod(payload).then(() => {
      proxy.$modal.msgSuccess(isEdit ? "修改成功" : "新增成功")
      inlineReturnItems.value = []
      open.value = false
      getList()
    })
  })
}

// 返回当前要操作的销售退货主单，兼容工具栏和行内入口，未选中时先提示用户。
function getTargetSaleReturnRow(row, actionText) {
  const saleReturnRow = row || saleReturnList.value.find(saleReturn => saleReturn.saleReturnId === ids.value[0])
  if (!saleReturnRow) {
    proxy.$modal.msgWarning(`请选择要${actionText}的销售退货单`)
    return undefined
  }
  return saleReturnRow
}

// 提交销售退货主单进入审核流程。
function handleSubmit(row) {
  const saleReturnRow = getTargetSaleReturnRow(row, "提交")
  if (!saleReturnRow) {
    return
  }
  proxy.$modal.confirm(`是否确认提交销售退货单号为"${saleReturnRow.returnNo}"的数据项？`).then(function () {
    return submitSaleReturn(saleReturnRow.saleReturnId)
  }).then(() => {
    proxy.$modal.msgSuccess("提交成功")
    getList()
  }).catch(() => {})
}

// 审核销售退货主单，审核后会回补库存并冲减应收。
function handleAudit(row) {
  const saleReturnRow = getTargetSaleReturnRow(row, "审核")
  if (!saleReturnRow) {
    return
  }
  proxy.$modal.confirm(`是否确认审核销售退货单号为"${saleReturnRow.returnNo}"的数据项？`).then(function () {
    return auditSaleReturn(saleReturnRow.saleReturnId)
  }).then(() => {
    proxy.$modal.msgSuccess("审核成功")
    getList()
  }).catch(() => {})
}

function handleReject(row) {
  proxy.$modal.prompt(`确认驳回销售退货单"${row.returnNo}"？请输入驳回原因：`).then(({ value }) => {
    return rejectSaleReturn(row.saleReturnId, value || "退货明细待核实")
  }).then(() => {
    proxy.$modal.msgSuccess("销售退货单已驳回至草稿")
    getList()
  }).catch(() => {})
}

// 作废销售退货主单，防止异常单据继续流转。
function handleCancelOrder(row) {
  const saleReturnRow = getTargetSaleReturnRow(row, "作废")
  if (!saleReturnRow) {
    return
  }
  proxy.$modal.confirm(`是否确认作废销售退货单号为"${saleReturnRow.returnNo}"的数据项？`).then(function () {
    return cancelSaleReturn(saleReturnRow.saleReturnId)
  }).then(() => {
    proxy.$modal.msgSuccess("作废成功")
    getList()
  }).catch(() => {})
}

// 删除销售退货主单，仅允许删除草稿单据。
function handleDelete(row) {
  const saleReturnIdList = row?.saleReturnId ? [row.saleReturnId] : ids.value
  if (!saleReturnIdList.length) {
    proxy.$modal.msgWarning("请选择要删除的销售退货单")
    return
  }
  proxy.$modal.confirm(`是否确认删除销售退货单编号为"${saleReturnIdList.join("、")}"的数据项？`).then(function () {
    return delSaleReturn(saleReturnIdList)
  }).then(() => {
    proxy.$modal.msgSuccess("删除成功")
    getList()
  }).catch(() => {})
}

// 导出销售退货列表，沿用系统统一导出方式。
function handleExport() {
  proxy.download("business/saleReturn/export", {
    ...queryParams.value
  }, `saleReturn_${new Date().getTime()}.xlsx`)
}

// 初始化页面筛选条件和基础资料，保证重复跳转到销售退货页时筛选结果会按最新参数刷新。
async function initializePage() {
  initializeQueryParamsFromRoute()
  await Promise.all([
    Promise.resolve(initBasicData()),
    Promise.resolve(getList())
  ])
}

// 监听同一路由下的查询参数变化，避免销售退货页仍保留上一次跳转的筛选状态。
watch(() => proxy?.$route?.fullPath, (currentRouteFullPath, previousRouteFullPath) => {
  if (currentRouteFullPath === previousRouteFullPath) {
    return
  }
  initializePage()
})

initializePage()
</script>
