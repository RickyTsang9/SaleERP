<template>
  <div class="app-container erp-page">
    <erp-page-header
      eyebrow="采购供应链 · 退换货"
      title="采购退货管理"
      subtitle="管理向上游供应商退货出库申请，审核通过后自动扣减库存并冲减应付账款。"
      icon="RefreshLeft"
      tone="amber"
      :badge="`共 ${total} 笔退货单`"
    >
      <template #actions>
        <el-button icon="Download" plain @click="handleExport" v-hasPermi="['business:purchaseReturn:export']">导出</el-button>
        <el-button type="primary" icon="Plus" @click="handleAdd" v-hasPermi="['business:purchaseReturn:add']">新建退货单</el-button>
      </template>
    </erp-page-header>

    <erp-stat-strip :items="purchaseReturnStatItems" :columns="4" @select="handleStatSelect" />

    <section class="erp-filter-card" v-show="showSearch">
      <el-form :model="queryParams" ref="queryRef" :inline="true">
        <el-form-item label="退货单号" prop="returnNo">
          <el-input v-model="queryParams.returnNo" placeholder="请输入退货单号" clearable style="width: 170px" @keyup.enter="handleQuery" />
        </el-form-item>
        <el-form-item label="来源采购单" prop="sourceOrderNo">
          <el-input v-model="queryParams.sourceOrderNo" placeholder="请输入采购单号" clearable style="width: 170px" @keyup.enter="handleQuery" />
        </el-form-item>
        <el-form-item label="供应商" prop="supplierId">
          <el-select v-model="queryParams.supplierId" placeholder="请选择供应商" clearable filterable remote reserve-keyword remote-show-suffix :remote-method="loadSupplierOptionList" :loading="supplierListLoading" style="width: 175px">
            <el-option v-for="supplier in supplierSelectOptionList" :key="supplier.supplierId" :label="supplier.supplierName" :value="supplier.supplierId" />
          </el-select>
        </el-form-item>
        <el-form-item label="仓库" prop="warehouseId">
          <el-select v-model="queryParams.warehouseId" placeholder="请选择仓库" clearable filterable remote reserve-keyword remote-show-suffix :remote-method="loadWarehouseOptionList" :loading="warehouseListLoading" style="width: 165px">
            <el-option v-for="warehouse in warehouseSelectOptionList" :key="warehouse.warehouseId" :label="warehouse.warehouseName" :value="warehouse.warehouseId" />
          </el-select>
        </el-form-item>
        <el-form-item label="退货日期">
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
          <erp-status-tabs :model-value="queryParams.status ?? ''" :items="returnStatusTabItems" @change="handleQuickStatus" />
        </div>
        <div class="erp-toolbar-right">
          <el-button type="success" plain icon="Edit" :disabled="!canUpdateSelected" @click="handleUpdate()" v-hasPermi="['business:purchaseReturn:edit']">修改</el-button>
          <el-button type="info" plain icon="Upload" :disabled="!canSubmitSelected" @click="handleSubmit()" v-hasPermi="['business:purchaseReturn:edit']">提交</el-button>
          <el-button type="warning" plain icon="CircleCheck" :disabled="!canAuditSelected" @click="handleAudit()" v-hasPermi="['business:purchaseReturn:audit']">审核</el-button>
          <el-button type="danger" plain icon="Delete" :disabled="!canDeleteSelected" @click="handleDelete()" v-hasPermi="['business:purchaseReturn:remove']">删除</el-button>
          <right-toolbar v-model:showSearch="showSearch" @queryTable="getList" />
        </div>
      </div>

      <el-table v-loading="loading" :data="purchaseReturnList" @selection-change="handleSelectionChange">
        <el-table-column type="selection" width="44" align="center" />
        <el-table-column label="退货单号" align="left" prop="returnNo" min-width="148" show-overflow-tooltip>
          <template #default="scope">
            <span class="erp-code-chip">{{ scope.row.returnNo }}</span>
            <div v-if="scope.row.purchaseOrderNo" class="erp-cell-sub">来源：{{ scope.row.purchaseOrderNo }}</div>
          </template>
        </el-table-column>
        <el-table-column label="供应商 / 仓库" align="left" min-width="148" show-overflow-tooltip>
          <template #default="scope">
            <div class="erp-cell-title">{{ scope.row.supplierName || getSupplierName(scope.row.supplierId) }}</div>
            <div class="erp-cell-sub">退回仓：{{ scope.row.warehouseName || getWarehouseName(scope.row.warehouseId) }}</div>
          </template>
        </el-table-column>
        <el-table-column label="数量 / 金额" align="right" width="112">
          <template #default="scope">
            <div class="erp-amount is-warning">¥{{ Number(scope.row.totalAmount || 0).toFixed(2) }}</div>
            <div class="erp-cell-sub">共 {{ scope.row.totalQty }} 件</div>
          </template>
        </el-table-column>
        <el-table-column label="状态" align="center" prop="status" width="84">
          <template #default="scope">
            <el-tooltip v-if="scope.row.status === 'draft' && scope.row.rejectReason" :content="`驳回原因：${scope.row.rejectReason}`" placement="top">
              <el-tag type="danger" effect="light" size="small">已驳回待改</el-tag>
            </el-tooltip>
            <el-tag v-else :type="getPurchaseReturnStatusType(scope.row.status)" effect="light" size="small">{{ getPurchaseReturnStatusLabel(scope.row.status) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="创建时间" align="center" prop="createTime" width="128">
          <template #default="scope">
            <span>{{ parseTime(scope.row.createTime, '{y}-{m}-{d} {h}:{i}') }}</span>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="248" fixed="right" align="center">
          <template #default="scope">
            <div class="erp-row-actions">
              <el-button link type="primary" icon="List" @click="handleOpenPurchaseReturnItem(scope.row)" v-hasPermi="['business:purchaseReturn:query']">明细</el-button>
              <el-button link type="info" icon="Printer" @click="handlePrintPurchaseReturn(scope.row)">打印</el-button>
              <el-button v-if="isSubmittedPurchaseReturnStatus(scope.row.status)" link type="success" icon="CircleCheck" @click="handleAudit(scope.row)" v-hasPermi="['business:purchaseReturn:audit']">审核</el-button>
              <el-button v-else-if="isDraftPurchaseReturnStatus(scope.row.status)" link type="primary" icon="Upload" @click="handleSubmit(scope.row)" v-hasPermi="['business:purchaseReturn:edit']">提交</el-button>
              <el-dropdown v-if="isDraftPurchaseReturnStatus(scope.row.status) || isSubmittedPurchaseReturnStatus(scope.row.status) || canCancelPurchaseReturnStatus(scope.row.status)" trigger="click">
                <el-button link type="primary">更多<el-icon class="el-icon--right"><ArrowDown /></el-icon></el-button>
                <template #dropdown>
                  <el-dropdown-menu>
                    <el-dropdown-item v-if="isDraftPurchaseReturnStatus(scope.row.status)" v-hasPermi="['business:purchaseReturn:edit']" @click="handleUpdate(scope.row)">修改主单</el-dropdown-item>
                    <el-dropdown-item v-if="isSubmittedPurchaseReturnStatus(scope.row.status)" v-hasPermi="['business:purchaseReturn:audit']" @click="handleReject(scope.row)">审核驳回</el-dropdown-item>
                    <el-dropdown-item v-if="canCancelPurchaseReturnStatus(scope.row.status)" v-hasPermi="['business:purchaseReturn:edit']" @click="handleCancelOrder(scope.row)">作废单据</el-dropdown-item>
                    <el-dropdown-item v-if="isDraftPurchaseReturnStatus(scope.row.status)" v-hasPermi="['business:purchaseReturn:remove']" divided @click="handleDelete(scope.row)"><span style="color: var(--el-color-danger)">删除草稿</span></el-dropdown-item>
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
            <el-form-item label="来源采购单" prop="purchaseOrderId">
              <el-select v-model="form.purchaseOrderId" placeholder="可选：搜索已入库采购单自动带出退货明细" clearable filterable remote reserve-keyword remote-show-suffix :remote-method="loadSourcePurchaseOrders" style="width: 100%" @change="handleSourcePurchaseOrderChange">
                <el-option
                  v-for="po in sourcePurchaseOrderList"
                  :key="po.purchaseOrderId"
                  :label="`${po.orderNo} | ${po.supplierName || ''}`"
                  :value="po.purchaseOrderId"
                />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="退货类型" prop="returnType">
              <el-select v-model="form.returnType" style="width: 100%" placeholder="请选择退货类型" disabled>
                <el-option label="采购退货" value="purchase_return" />
              </el-select>
            </el-form-item>
          </el-col>
        </el-row>
        <el-row>
          <el-col :span="12">
            <el-form-item label="供应商" prop="supplierId">
              <el-select v-model="form.supplierId" placeholder="请选择供应商" clearable filterable remote reserve-keyword remote-show-suffix :remote-method="loadSupplierOptionList" :loading="supplierListLoading" :disabled="!!form.purchaseOrderId" style="width: 100%">
                <el-option
                  v-for="supplier in supplierSelectOptionList"
                  :key="supplier.supplierId"
                  :label="supplier.supplierName"
                  :value="supplier.supplierId"
                />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="仓库" prop="warehouseId">
              <el-select v-model="form.warehouseId" placeholder="请选择仓库" clearable filterable remote reserve-keyword remote-show-suffix :remote-method="loadWarehouseOptionList" :loading="warehouseListLoading" style="width: 100%" @change="handleFormWarehouseChange">
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
                <el-option v-for="statusOption in purchaseReturnStatusOptionList" :key="statusOption.value" :label="statusOption.label" :value="statusOption.value" />
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
          <el-col :span="24">
            <el-form-item label="备注" prop="remark">
              <el-input v-model="form.remark" placeholder="请输入备注" />
            </el-form-item>
          </el-col>
        </el-row>
        <template v-if="!form.purchaseReturnId || isDraftPurchaseReturnStatus(form.status)">
          <el-divider content-position="left">退货商品明细（支持自动带出与一体化维护）</el-divider>
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
            <el-table-column label="退回库位" width="130">
              <template #default="scope">
                <el-select v-model="scope.row.locationId" placeholder="库位" clearable filterable size="small" style="width: 115px">
                  <el-option v-for="loc in locationList" :key="loc.locationId" :label="loc.locationName" :value="loc.locationId" />
                </el-select>
              </template>
            </el-table-column>
            <el-table-column label="退回批次" width="115">
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
        <el-col :span="1.5">
          <el-button plain icon="Download" @click="handleExportPurchaseReturnItems" v-hasPermi="['business:purchaseReturn:export']">导出明细</el-button>
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
        <el-table-column label="退回库位" align="center" prop="locationId" min-width="120">
          <template #default="scope">
            <span>{{ getLocationName(scope.row.locationId) }}</span>
          </template>
        </el-table-column>
        <el-table-column label="退回批次" align="center" prop="batchNo" min-width="120">
          <template #default="scope">
            <span>{{ scope.row.batchNo || '-' }}</span>
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
            <el-form-item label="退回库位" prop="locationId">
              <el-select v-model="purchaseReturnItemForm.locationId" placeholder="可选：指定退货扣减库位" clearable filterable style="width: 100%">
                <el-option v-for="loc in locationList" :key="loc.locationId" :label="loc.locationName" :value="loc.locationId" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="退回批次号" prop="batchNo">
              <el-input v-model="purchaseReturnItemForm.batchNo" placeholder="可选：精准扣减指定批次" clearable />
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

    <document-print-dialog ref="printDialogRef" />
  </div>
</template>

<script setup name="PurchaseReturn">
import DocumentPrintDialog from "@/components/DocumentPrintDialog/index.vue"
import { listPurchaseReturn, getPurchaseReturn, addPurchaseReturn, updatePurchaseReturn, delPurchaseReturn, submitPurchaseReturn, auditPurchaseReturn, batchSubmitPurchaseReturn, batchAuditPurchaseReturn, rejectPurchaseReturn, cancelPurchaseReturn } from "@/api/business/purchaseReturn"
import { listPurchaseReturnItem, getPurchaseReturnItem, addPurchaseReturnItem, updatePurchaseReturnItem, delPurchaseReturnItem } from "@/api/business/purchaseReturnItem"
import { listPurchaseOrder, getPurchaseOrder } from "@/api/business/purchaseOrder"
import { listInbound } from "@/api/business/inbound"
import { listInboundItem } from "@/api/business/inboundItem"
import { listSupplier, getSupplier } from "@/api/business/supplier"
import { listWarehouse, getWarehouse } from "@/api/business/warehouse"
import { listLocation } from "@/api/business/location"
import { listProduct, getProduct } from "@/api/business/product"
import { appendUniqueSelectOption, buildSelectOptionList, normalizeRemoteKeyword } from "@/utils/remoteSelect"
import { ERP_DATE_SHORTCUTS } from "@/utils/erpFormat"
import { parseTime } from "@/utils/ruoyi"

const { proxy } = getCurrentInstance()

const purchaseReturnList = ref([])
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
    row.price = Number(prod.costPrice || prod.purchasePrice || 0)
  }
  recalcInlineReturnTotals()
}

function recalcInlineReturnTotals() {
  if (!form.value.purchaseReturnId || isDraftPurchaseReturnStatus(form.value.status)) {
    form.value.totalQty = Number(inlineReturnSummary.value.qty.toFixed(2))
    form.value.totalAmount = Number(inlineReturnSummary.value.amount.toFixed(2))
  }
}

async function handlePrintPurchaseReturn(row) {
  const res = await listPurchaseReturnItem({ pageNum: 1, pageSize: 500, purchaseReturnId: row.purchaseReturnId })
  const items = res.rows || []
  await Promise.all([
    ensurePurchaseReturnItemReferenceOptionsLoaded(items),
    loadLocationsByWarehouse(row.warehouseId)
  ])
  printDialogRef.value?.openPrint({
    docType: "purchaseReturn",
    title: "采购退货出库单",
    docNo: row.returnNo || `PR-${row.purchaseReturnId}`,
    defaultPaperMode: "triplicate",
    metaFields: [
      { label: "退货单号", value: row.returnNo || "-" },
      { label: "退货供应商", value: row.supplierName || getSupplierName(row.supplierId) },
      { label: "退货出库仓", value: row.warehouseName || getWarehouseName(row.warehouseId) },
      { label: "来源采购单", value: row.purchaseOrderNo || (row.purchaseOrderId ? `PO-${row.purchaseOrderId}` : "-") },
      { label: "单据状态", value: getPurchaseReturnStatusLabel(row.status) },
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
const supplierList = ref([])
const supplierListLoading = ref(false)
const warehouseList = ref([])
const warehouseListLoading = ref(false)
const productList = ref([])
const productListLoading = ref(false)
const sourcePurchaseOrderList = ref([])
const locationList = ref([])
const dateRange = ref([])
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
    sourceOrderNo: undefined,
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

const returnStatusTabItems = [
  { label: "全部", value: "" },
  { label: "草稿", value: "draft", dot: "slate" },
  { label: "待审核", value: "submitted", dot: "amber" },
  { label: "已审核", value: "audited", dot: "emerald" },
  { label: "已作废", value: "cancelled", dot: "rose" }
]

const purchaseReturnStatItems = computed(() => {
  const rows = purchaseReturnList.value || []
  const draftCount = rows.filter(r => r.status === "draft").length
  const submittedCount = rows.filter(r => r.status === "submitted").length
  const pageAmount = rows.reduce((s, r) => s + Number(r.totalAmount || 0), 0)
  const curStatus = queryParams.value.status ?? ""
  return [
    { key: "all", statusVal: "", label: "采购退货总数", value: total.value, suffix: "单", hint: "点击查看全部退货单", icon: "RefreshLeft", tone: "amber", clickable: true, active: curStatus === "" },
    { key: "draft", statusVal: "draft", label: "本页草稿单", value: draftCount, suffix: "单", hint: "点击筛选草稿退货单", icon: "EditPen", tone: "slate", clickable: true, active: curStatus === "draft" },
    { key: "submitted", statusVal: "submitted", label: "本页待审核", value: submittedCount, suffix: "单", hint: "点击筛选待审核退货单", icon: "Stamp", tone: "blue", clickable: true, active: curStatus === "submitted" },
    { key: "amt", label: "本页退货总额", prefix: "¥", value: pageAmount.toFixed(2), hint: "本页采购退货金额合计", icon: "Money", tone: "rose" }
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

const supplierSelectOptionList = computed(() => buildSupplierSelectOptionList())
const warehouseSelectOptionList = computed(() => buildWarehouseSelectOptionList())
const productSelectOptionList = computed(() => buildProductSelectOptionList())

const selectedRows = computed(() => purchaseReturnList.value.filter(pr => ids.value.includes(pr.purchaseReturnId)))
const canUpdateSelected = computed(() => selectedRows.value.length === 1 && isDraftPurchaseReturnStatus(selectedRows.value[0].status))
const canSubmitSelected = computed(() => selectedRows.value.length > 0 && selectedRows.value.every(pr => isDraftPurchaseReturnStatus(pr.status)))
const canAuditSelected = computed(() => selectedRows.value.length > 0 && selectedRows.value.every(pr => isSubmittedPurchaseReturnStatus(pr.status)))
const canDeleteSelected = computed(() => selectedRows.value.length > 0 && selectedRows.value.every(pr => isDraftPurchaseReturnStatus(pr.status)))

// 初始化页面基础主数据，保证首次打开即可按名称搜索。
async function initBasicData() {
  await Promise.all([
    loadSupplierOptionList(),
    loadWarehouseOptionList(),
    loadProductOptionList()
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
      status: "0",
      supplierName: normalizedKeyword
    })
    ;(response.rows || []).forEach(item => appendUniqueSelectOption(supplierList.value, item, "supplierId"))
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
      status: "0",
      warehouseName: normalizedKeyword
    })
    ;(response.rows || []).forEach(item => appendUniqueSelectOption(warehouseList.value, item, "warehouseId"))
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
      status: "0",
      productName: normalizedKeyword
    })
    ;(response.rows || []).forEach(item => appendUniqueSelectOption(productList.value, item, "productId"))
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

// 批量补齐主单相关引用数据，优先复用后端关联字段，消除冗余 N+1 查询。
async function ensurePurchaseReturnReferenceOptionsLoaded(targetPurchaseReturnList = []) {
  const supplierIdList = [queryParams.value.supplierId, form.value.supplierId]
  const warehouseIdList = [queryParams.value.warehouseId, form.value.warehouseId]
  ;(targetPurchaseReturnList || []).forEach(purchaseReturn => {
    if (!purchaseReturn) return
    if (purchaseReturn.supplierId != null && purchaseReturn.supplierName) {
      appendUniqueSelectOption(supplierList.value, { supplierId: purchaseReturn.supplierId, supplierName: purchaseReturn.supplierName }, "supplierId")
    } else if (purchaseReturn.supplierId != null) {
      supplierIdList.push(purchaseReturn.supplierId)
    }
    if (purchaseReturn.warehouseId != null && purchaseReturn.warehouseName) {
      appendUniqueSelectOption(warehouseList.value, { warehouseId: purchaseReturn.warehouseId, warehouseName: purchaseReturn.warehouseName }, "warehouseId")
    } else if (purchaseReturn.warehouseId != null) {
      warehouseIdList.push(purchaseReturn.warehouseId)
    }
  })
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
  const inlineIds = (inlineReturnItems.value || []).map(it => it.productId)
  return buildSelectOptionList(productList.value, [purchaseReturnItemForm.value.productId, ...inlineIds], "productId", "productName", () => "商品资料缺失")
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
    const response = await listPurchaseReturn(proxy.addDateRange(queryParams.value, dateRange.value))
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

function getLocationName(locationId) {
  if (!locationId) return "-"
  const loc = locationList.value.find(item => item.locationId === locationId)
  return loc?.locationName || `库位#${locationId}`
}

async function loadSourcePurchaseOrders(keyword) {
  try {
    const orderNo = normalizeRemoteKeyword(keyword)
    const [r2, r3] = await Promise.all([
      listPurchaseOrder({ pageNum: 1, pageSize: 50, status: "2", orderNo }),
      listPurchaseOrder({ pageNum: 1, pageSize: 50, status: "3", orderNo })
    ])
    sourcePurchaseOrderList.value = [...(r2.rows || []), ...(r3.rows || [])]
  } catch (e) {}
}

async function handleSourcePurchaseOrderChange(purchaseOrderId) {
  if (!purchaseOrderId) return
  const po = sourcePurchaseOrderList.value.find(item => item.purchaseOrderId === purchaseOrderId)
  if (po) {
    form.value.supplierId = po.supplierId
    if (po.supplierId && po.supplierName) {
      appendUniqueSelectOption(supplierList.value, { supplierId: po.supplierId, supplierName: po.supplierName }, "supplierId")
    }
  }
  try {
    const [poDetailRes, inbRes] = await Promise.all([
      getPurchaseOrder(purchaseOrderId).catch(() => null),
      listInbound({ pageNum: 1, pageSize: 10, purchaseOrderId, status: "audited" }).catch(() => null)
    ])
    const auditedInbounds = inbRes?.rows || []
    let inboundItems = []
    if (auditedInbounds.length > 0) {
      const firstInbound = auditedInbounds[0]
      if (!form.value.warehouseId && firstInbound.warehouseId) {
        form.value.warehouseId = firstInbound.warehouseId
        await ensureWarehouseOptionLoaded([firstInbound.warehouseId])
        await loadLocationsByWarehouse(firstInbound.warehouseId)
      }
      const inbItemRes = await listInboundItem({ pageNum: 1, pageSize: 200, inboundId: firstInbound.inboundId }).catch(() => null)
      inboundItems = inbItemRes?.rows || []
    }
    if (inboundItems.length > 0) {
      await ensureProductOptionLoaded(inboundItems.map(it => it.productId))
      inlineReturnItems.value = inboundItems.map(it => ({
        productId: it.productId,
        locationId: it.locationId || undefined,
        batchNo: it.batchNo || "",
        quantity: Number(it.quantity || 1),
        price: Number(it.price || 0)
      }))
      recalcInlineReturnTotals()
      proxy.$modal.msgSuccess(`已自动带入来源入库商品明细（共 ${inboundItems.length} 行），可按实际退货数量调整`)
    } else if (poDetailRes?.data?.wmsPurchaseOrderItemList?.length) {
      const poItems = poDetailRes.data.wmsPurchaseOrderItemList
      await ensureProductOptionLoaded(poItems.map(it => it.productId))
      inlineReturnItems.value = poItems.map(it => ({
        productId: it.productId,
        locationId: undefined,
        batchNo: "",
        quantity: Number(it.inboundQuantity || it.quantity || 1),
        price: Number(it.price || 0)
      }))
      recalcInlineReturnTotals()
      proxy.$modal.msgSuccess(`已自动带入来源采购商品明细（共 ${poItems.length} 行）`)
    }
  } catch (e) {}
}

async function loadLocationsByWarehouse(warehouseId) {
  try {
    const res = await listLocation({ pageNum: 1, pageSize: 200, warehouseId: warehouseId || undefined, status: "0" })
    locationList.value = res.rows || []
  } catch (e) {}
}

// 重置采购退货主单表单。
function reset() {
  form.value = {
    purchaseReturnId: undefined,
    purchaseOrderId: undefined,
    returnNo: undefined,
    returnType: "purchase_return",
    supplierId: undefined,
    warehouseId: undefined,
    totalQty: 0,
    totalAmount: 0,
    status: "draft",
    auditBy: undefined,
    auditTime: undefined,
    remark: undefined
  }
  inlineReturnItems.value = []
  proxy.resetForm("purchaseReturnRef")
}

// 重置采购退货明细表单，并回填当前主单编号。
function resetPurchaseReturnItemForm() {
  purchaseReturnItemForm.value = {
    purchaseReturnItemId: undefined,
    purchaseReturnId: currentPurchaseReturnId.value,
    productId: undefined,
    locationId: undefined,
    batchNo: "",
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
  dateRange.value = []
  proxy.resetForm("queryRef")
  queryParams.value.status = undefined
  queryParams.value.sourceOrderNo = undefined
  handleQuery()
}

// 同步表格选中状态，让顶部按钮和行内按钮规则一致。
function handleSelectionChange(selection) {
  ids.value = selection.map(purchaseReturn => purchaseReturn.purchaseReturnId)
}

function handleFormWarehouseChange(warehouseId) {
  ;(inlineReturnItems.value || []).forEach(item => { item.locationId = undefined })
  if (warehouseId) {
    loadLocationsByWarehouse(warehouseId)
  } else {
    locationList.value = []
  }
}

// 打开新增采购退货弹窗。
function handleAdd() {
  reset()
  locationList.value = []
  loadSourcePurchaseOrders()
  open.value = true
  title.value = "新增采购退货单"
}

// 打开编辑采购退货弹窗，并补齐供应商和仓库回显。
async function handleUpdate(row) {
  reset()
  loadSourcePurchaseOrders()
  const purchaseReturnId = row?.purchaseReturnId || ids.value[0]
  if (!purchaseReturnId) {
    return
  }
  const [response, itemRes] = await Promise.all([
    getPurchaseReturn(purchaseReturnId),
    listPurchaseReturnItem({ pageNum: 1, pageSize: 1000, purchaseReturnId })
  ])
  form.value = response.data
  const items = itemRes.rows || []
  await Promise.all([
    ensurePurchaseReturnReferenceOptionsLoaded([form.value]),
    ensurePurchaseReturnItemReferenceOptionsLoaded(items)
  ])
  if (form.value.warehouseId) {
    await loadLocationsByWarehouse(form.value.warehouseId)
  }
  inlineReturnItems.value = items.map(it => ({
    purchaseReturnItemId: it.purchaseReturnItemId,
    productId: it.productId,
    locationId: it.locationId || undefined,
    batchNo: it.batchNo || "",
    quantity: Number(it.quantity || 1),
    price: Number(it.price || 0)
  }))
  open.value = true
  title.value = "修改采购退货单"
}

function handleExportPurchaseReturnItems() {
  if (!currentPurchaseReturnId.value) return
  proxy.download("business/purchaseReturnItem/export", {
    purchaseReturnId: currentPurchaseReturnId.value
  }, `purchaseReturnItem_${currentPurchaseReturnId.value}_${new Date().getTime()}.xlsx`)
}

// 打开采购退货明细弹窗，并根据主单状态决定是否允许编辑。
function handleOpenPurchaseReturnItem(row) {
  currentPurchaseReturnId.value = row.purchaseReturnId
  currentPurchaseReturnStatus.value = row.status
  loadLocationsByWarehouse(row.warehouseId)
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

// 保存采购退货主单（新增与草稿修改均支持一体化提交商品明细）。
function submitForm() {
  proxy.$refs["purchaseReturnRef"].validate(valid => {
    if (!valid) {
      return
    }
    const isEdit = form.value.purchaseReturnId !== undefined
    const submitMethod = isEdit ? updatePurchaseReturn : addPurchaseReturn
    const payload = {
      ...form.value,
      items: (inlineReturnItems.value || [])
        .filter(it => it.productId && Number(it.quantity || 0) > 0)
        .map(it => ({
          purchaseReturnItemId: it.purchaseReturnItemId,
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

// 返回当前要操作的采购退货主单，兼容工具栏和行内入口，未选中时先提示用户。
function getTargetPurchaseReturnRow(row, actionText) {
  const purchaseReturnRow = row || purchaseReturnList.value.find(purchaseReturn => purchaseReturn.purchaseReturnId === ids.value[0])
  if (!purchaseReturnRow) {
    proxy.$modal.msgWarning(`请选择要${actionText}的采购退货单`)
    return undefined
  }
  return purchaseReturnRow
}

// 提交采购退货主单进入审核流程（支持单条与批量）。
function handleSubmit(row) {
  if (!row?.purchaseReturnId && ids.value.length > 1) {
    proxy.$modal.confirm(`是否确认批量提交选中的 ${ids.value.length} 笔采购退货单？`).then(() => {
      return batchSubmitPurchaseReturn(ids.value)
    }).then(res => {
      proxy.$modal.msgSuccess(res.msg || "批量提交完成")
      getList()
    }).catch(() => {})
    return
  }
  const purchaseReturnRow = getTargetPurchaseReturnRow(row, "提交")
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

// 审核采购退货主单，审核后会扣减库存并冲减应付（支持单条与批量）。
function handleAudit(row) {
  if (!row?.purchaseReturnId && ids.value.length > 1) {
    proxy.$modal.confirm(`是否确认批量审核选中的 ${ids.value.length} 笔采购退货单？审核后将自动扣减库存并冲减应付。`).then(() => {
      return batchAuditPurchaseReturn(ids.value)
    }).then(res => {
      proxy.$modal.msgSuccess(res.msg || "批量审核完成")
      getList()
    }).catch(() => {})
    return
  }
  const purchaseReturnRow = getTargetPurchaseReturnRow(row, "审核")
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

function handleReject(row) {
  proxy.$modal.prompt(`确认驳回采购退货单"${row.returnNo}"？请输入驳回原因：`).then(({ value }) => {
    return rejectPurchaseReturn(row.purchaseReturnId, value || "采购退货明细待核实")
  }).then(() => {
    proxy.$modal.msgSuccess("采购退货单已驳回至草稿")
    getList()
  }).catch(() => {})
}

// 作废采购退货主单，避免异常退货单继续流转。
function handleCancelOrder(row) {
  const purchaseReturnRow = getTargetPurchaseReturnRow(row, "作废")
  if (!purchaseReturnRow) {
    return
  }
  proxy.$modal.confirm(`是否确认作废采购退货单号为"${purchaseReturnRow.returnNo}"的数据项？`).then(function () {
    return cancelPurchaseReturn(purchaseReturnRow.purchaseReturnId)
  }).then(() => {
    proxy.$modal.msgSuccess("作废成功")
    getList()
  }).catch(() => {})
}

// 删除采购退货主单，仅允许删除草稿单据。
function handleDelete(row) {
  const purchaseReturnIdList = row?.purchaseReturnId ? [row.purchaseReturnId] : ids.value
  if (!purchaseReturnIdList.length) {
    proxy.$modal.msgWarning("请选择要删除的采购退货单")
    return
  }
  proxy.$modal.confirm(`是否确认删除采购退货单编号为"${purchaseReturnIdList.join("、")}"的数据项？`).then(function () {
    return delPurchaseReturn(purchaseReturnIdList)
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
  await Promise.all([
    Promise.resolve(initBasicData()),
    Promise.resolve(getList())
  ])
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
