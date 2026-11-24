<template>
  <div class="app-container erp-page">
    <erp-page-header
      eyebrow="采购供应链 · 入库作业"
      title="采购入库管理"
      subtitle="管理采购收货与批次库位上架，支持条码枪扫码录入，审核通过后自动增加批次库存并同步应付。"
      icon="Download"
      tone="emerald"
      :badge="`共 ${total} 笔入库单`"
    >
      <template #actions>
        <el-button icon="Download" plain @click="handleExport" v-hasPermi="['business:inbound:export']">导出</el-button>
        <el-button type="primary" icon="Plus" @click="handleAdd" v-hasPermi="['business:inbound:add']">新建入库单</el-button>
      </template>
    </erp-page-header>

    <erp-stat-strip :items="inboundStatItems" :columns="4" @select="handleStatSelect" />

    <section class="erp-filter-card" v-show="showSearch">
      <el-form :model="queryParams" ref="queryRef" :inline="true">
        <el-form-item label="入库单号" prop="inboundNo">
          <el-input v-model="queryParams.inboundNo" placeholder="请输入入库单号" clearable style="width: 175px" @keyup.enter="handleQuery" />
        </el-form-item>
        <el-form-item label="采购单号" prop="purchaseOrderNo">
          <el-input v-model="queryParams.purchaseOrderNo" placeholder="请输入采购单号" clearable style="width: 175px" @keyup.enter="handleQuery" />
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
        <el-form-item label="入库日期">
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
          <erp-status-tabs :model-value="queryParams.status ?? ''" :items="inboundStatusTabItems" @change="handleQuickStatus" />
        </div>
        <div class="erp-toolbar-right">
          <el-button type="success" plain icon="Edit" :disabled="!canUpdateSelected" @click="handleUpdate" v-hasPermi="['business:inbound:edit']">修改</el-button>
          <el-button type="info" plain icon="Upload" :disabled="!canSubmitSelected" @click="handleSubmit()" v-hasPermi="['business:inbound:edit']">提交</el-button>
          <el-button type="warning" plain icon="CircleCheck" :disabled="!canAuditSelected" @click="handleAudit()" v-hasPermi="['business:inbound:audit']">审核</el-button>
          <el-button type="danger" plain icon="Delete" :disabled="!canDeleteSelected" @click="handleDelete()" v-hasPermi="['business:inbound:remove']">删除</el-button>
          <right-toolbar v-model:showSearch="showSearch" @queryTable="getList" />
        </div>
      </div>

      <el-table v-loading="loading" :data="inboundList" @selection-change="handleSelectionChange">
        <el-table-column type="selection" width="46" align="center" />
        <el-table-column label="入库单号" align="center" prop="inboundNo" min-width="150" show-overflow-tooltip>
          <template #default="scope">
            <span class="erp-code-chip">{{ scope.row.inboundNo }}</span>
          </template>
        </el-table-column>
        <el-table-column label="类型 / 来源单" align="left" min-width="150" show-overflow-tooltip>
          <template #default="scope">
            <div class="erp-cell-title">{{ getInboundTypeLabel(scope.row.inboundType) }}</div>
            <div class="erp-cell-sub">{{ getPurchaseOrderDisplayName(scope.row.purchaseOrderNo, scope.row.purchaseOrderId) || '手工建单' }}</div>
          </template>
        </el-table-column>
        <el-table-column label="供应商 / 仓库" align="left" min-width="175" show-overflow-tooltip>
          <template #default="scope">
            <div class="erp-cell-title">{{ scope.row.supplierName || getSupplierName(scope.row.supplierId, scope.row.purchaseOrderId) }}</div>
            <div class="erp-cell-sub">仓库：{{ scope.row.warehouseName || getWarehouseName(scope.row.warehouseId) }}</div>
          </template>
        </el-table-column>
        <el-table-column label="数量" align="right" prop="totalQty" width="76" />
        <el-table-column label="入库总额" align="right" prop="totalAmount" width="106">
          <template #default="scope">
            <span class="erp-amount is-primary">¥{{ Number(scope.row.totalAmount || 0).toFixed(2) }}</span>
          </template>
        </el-table-column>
        <el-table-column label="状态" align="center" prop="status" width="96">
          <template #default="scope">
            <el-tooltip v-if="scope.row.status === 'draft' && scope.row.rejectReason" :content="`驳回原因：${scope.row.rejectReason}`" placement="top">
              <el-tag type="danger" effect="light">已驳回待改</el-tag>
            </el-tooltip>
            <el-tag v-else :type="getInboundStatusType(scope.row.status)" effect="light">{{ getInboundStatusLabel(scope.row.status) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="创建时间" align="center" prop="createTime" width="142">
          <template #default="scope">
            <span>{{ parseTime(scope.row.createTime, '{y}-{m}-{d} {h}:{i}') }}</span>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="205" fixed="right" align="center">
          <template #default="scope">
            <div class="erp-row-actions">
              <el-button link type="primary" icon="List" @click="handleOpenInboundItem(scope.row)" v-hasPermi="['business:inbound:query']">明细</el-button>
              <el-button link type="primary" icon="Printer" @click="handlePrintInbound(scope.row)">打印</el-button>
              <el-button v-if="scope.row.status === 'submitted'" link type="success" icon="CircleCheck" @click="handleAudit(scope.row)" v-hasPermi="['business:inbound:audit']">审核</el-button>
              <el-button v-else-if="scope.row.status === 'draft'" link type="primary" icon="Upload" @click="handleSubmit(scope.row)" v-hasPermi="['business:inbound:edit']">提交</el-button>
              <el-dropdown v-if="scope.row.purchaseOrderId || scope.row.status !== 'audited'" trigger="click">
                <el-button link type="primary">更多<el-icon class="el-icon--right"><ArrowDown /></el-icon></el-button>
                <template #dropdown>
                  <el-dropdown-menu>
                    <el-dropdown-item v-if="scope.row.purchaseOrderId" v-hasPermi="['business:purchaseOrder:list']" @click="handleViewPurchaseOrder(scope.row)">查看采购单</el-dropdown-item>
                    <el-dropdown-item v-if="scope.row.status === 'draft'" v-hasPermi="['business:inbound:edit']" @click="handleUpdate(scope.row)">修改主单</el-dropdown-item>
                    <el-dropdown-item v-if="scope.row.status === 'submitted'" v-hasPermi="['business:inbound:audit']" @click="handleReject(scope.row)">审核驳回</el-dropdown-item>
                    <el-dropdown-item v-if="scope.row.status !== 'audited' && scope.row.status !== 'cancelled'" v-hasPermi="['business:inbound:edit']" @click="handleCancel(scope.row)">作废单据</el-dropdown-item>
                    <el-dropdown-item v-if="scope.row.status === 'draft'" v-hasPermi="['business:inbound:remove']" divided @click="handleDelete(scope.row)"><span style="color: var(--el-color-danger)">删除草稿</span></el-dropdown-item>
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
      <el-form ref="inboundRef" :model="form" :rules="rules" label-width="110px">
        <el-row>
          <el-col :span="12">
            <el-form-item label="入库类型" prop="inboundType">
              <el-select v-model="form.inboundType" placeholder="请选择入库类型" style="width: 100%" :disabled="!isInboundFormEditable() || hasLinkedPurchaseOrder()">
                <el-option
                  v-for="inboundTypeOption in inboundTypeOptionList"
                  :key="inboundTypeOption.value"
                  :label="inboundTypeOption.label"
                  :value="inboundTypeOption.value"
                />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="来源采购单" prop="purchaseOrderId">
              <el-select
                v-if="!form.inboundId"
                v-model="form.purchaseOrderId"
                placeholder="可选：搜索采购单号关联自动带入明细"
                clearable
                filterable
                remote
                reserve-keyword
                remote-show-suffix
                :remote-method="loadAvailablePurchaseOrders"
                :loading="poSelectLoading"
                style="width: 100%"
                @change="handlePurchaseOrderSelectChange"
              >
                <el-option
                  v-for="po in availablePurchaseOrderList"
                  :key="po.purchaseOrderId"
                  :label="`${po.orderNo} | ${po.supplierName || ''} (待入库 ${po.remainQuantity ?? po.totalQuantity ?? 0})`"
                  :value="po.purchaseOrderId"
                />
              </el-select>
              <el-input v-else v-model="form.purchaseOrderNo" placeholder="未关联采购单" disabled />
            </el-form-item>
          </el-col>
        </el-row>
        <el-row>
          <el-col :span="12">
            <el-form-item label="供应商" prop="supplierId">
              <el-select v-model="form.supplierId" placeholder="请选择供应商" clearable filterable remote reserve-keyword remote-show-suffix :remote-method="loadSupplierOptionList" :loading="supplierListLoading" style="width: 100%" :disabled="!isInboundFormEditable() || hasLinkedPurchaseOrder()">
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
              <el-select v-model="form.warehouseId" placeholder="请选择仓库" clearable filterable remote reserve-keyword remote-show-suffix :remote-method="loadWarehouseOptionList" :loading="warehouseListLoading" style="width: 100%" :disabled="!isInboundFormEditable()">
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
              <el-select v-model="form.status" placeholder="系统自动维护单据状态" disabled style="width: 100%">
                <el-option
                  v-for="inboundStatusOption in inboundStatusOptionList"
                  :key="inboundStatusOption.value"
                  :label="inboundStatusOption.label"
                  :value="inboundStatusOption.value"
                />
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
              <el-input v-model="form.auditBy" placeholder="系统审核时自动回填审核人" disabled />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="备注" prop="remark">
              <el-input v-model="form.remark" placeholder="请输入备注" :disabled="!isInboundFormEditable()" />
            </el-form-item>
          </el-col>
        </el-row>
      </el-form>
      <template #footer>
        <div class="dialog-footer">
          <el-button type="primary" @click="handleInboundDialogPrimaryAction">{{ isInboundFormEditable() ? "确 定" : "关 闭" }}</el-button>
          <el-button v-if="isInboundFormEditable()" @click="cancel">取 消</el-button>
        </div>
      </template>
    </el-dialog>

    <el-dialog :title="inboundItemTitle" v-model="inboundItemOpen" width="1140px" append-to-body>
      <el-alert
        v-if="!isInboundItemEditable() || isLinkedPurchaseOrderInboundItemMode()"
        :title="!isInboundItemEditable() ? '当前入库单已进入后续流程，明细仅支持查看。' : '来源采购单明细已自动带入，当前只需维护库位、批次和实收数量。'"
        type="info"
        :closable="false"
        class="mb8"
      />
      <el-row :gutter="10" class="mb8" align="middle">
        <el-col :span="1.5" v-if="isInboundItemEditable() && inboundItemList.length > 0">
          <el-button type="success" plain icon="MagicStick" @click="handleQuickFillInboundDefaults" v-hasPermi="['business:inbound:edit']">一键填充库位/批次</el-button>
        </el-col>
        <el-col :span="1.5" v-if="isInboundItemEditable() && inboundItemList.length > 0">
          <el-button type="primary" icon="Check" :loading="batchSaveInboundLoading" @click="handleBatchSaveInboundItems" v-hasPermi="['business:inbound:edit']">保存全部明细</el-button>
        </el-col>
        <el-col :span="1.5">
          <el-button
            v-if="isInboundItemEditable() && !isLinkedPurchaseOrderInboundItemMode()"
            type="primary"
            plain
            icon="Plus"
            @click="handleAddInboundItem"
            v-hasPermi="['business:inbound:add']"
          >新增明细</el-button>
        </el-col>
        <el-col :span="1.5">
          <el-button
            v-if="isInboundItemEditable() && !isLinkedPurchaseOrderInboundItemMode()"
            type="danger"
            plain
            icon="Delete"
            :disabled="inboundItemMultiple"
            @click="handleDeleteInboundItem"
            v-hasPermi="['business:inbound:remove']"
          >删除明细</el-button>
        </el-col>
        <el-col :span="1.5">
          <el-button plain icon="Download" @click="handleExportInboundItems" v-hasPermi="['business:inbound:export']">导出明细</el-button>
        </el-col>
        <el-col :span="9" v-if="isInboundItemEditable()" v-hasPermi="['business:inbound:add', 'business:inbound:edit']">
          <el-input
            v-model="inboundScanCode"
            placeholder="扫码入库：扫描/输入条码或商品编码按回车（自动匹配+1）"
            clearable
            style="width: 100%"
            @keyup.enter="handleInboundBarcodeScan"
          >
            <template #append>
              <el-button icon="FullScreen" :loading="inboundScanLoading" @click="handleInboundBarcodeScan">扫码录入</el-button>
            </template>
          </el-input>
        </el-col>
      </el-row>
      <el-table v-loading="inboundItemLoading" :data="inboundItemList" @selection-change="handleInboundItemSelectionChange">
        <el-table-column v-if="isInboundItemEditable() && !isLinkedPurchaseOrderInboundItemMode()" type="selection" width="46" align="center" />
        <el-table-column label="明细编号" align="center" prop="inboundItemId" width="80" />
        <el-table-column label="商品" align="left" prop="productId" min-width="150" show-overflow-tooltip>
          <template #default="scope">
            <span>{{ getProductName(scope.row.productId) }}</span>
          </template>
        </el-table-column>
        <el-table-column v-if="isLinkedPurchaseOrderInboundItemMode()" label="采购数量" align="right" prop="purchaseQuantity" width="84">
          <template #default="scope">
            <span>{{ formatInboundItemQuantity(scope.row.purchaseQuantity) }}</span>
          </template>
        </el-table-column>
        <el-table-column v-if="isLinkedPurchaseOrderInboundItemMode()" label="已入库" align="right" prop="receivedQuantity" width="78">
          <template #default="scope">
            <span>{{ formatInboundItemQuantity(scope.row.receivedQuantity) }}</span>
          </template>
        </el-table-column>
        <el-table-column v-if="isLinkedPurchaseOrderInboundItemMode()" label="待入库" align="right" prop="remainQuantity" width="78">
          <template #default="scope">
            <span>{{ formatInboundItemQuantity(scope.row.remainQuantity) }}</span>
          </template>
        </el-table-column>
        <el-table-column label="库位" align="left" prop="locationId" min-width="155">
          <template #default="scope">
            <el-select
              v-if="isInboundItemEditable()"
              v-model="scope.row.locationId"
              placeholder="选择库位"
              filterable
              size="small"
              style="width: 100%"
            >
              <el-option
                v-for="location in locationSelectOptionList"
                :key="location.locationId"
                :label="location.locationName"
                :value="location.locationId"
              />
            </el-select>
            <span v-else>{{ getLocationName(scope.row.locationId) }}</span>
          </template>
        </el-table-column>
        <el-table-column label="批次号" align="center" prop="batchNo" min-width="145">
          <template #default="scope">
            <el-input v-if="isInboundItemEditable()" v-model="scope.row.batchNo" size="small" placeholder="批次号" />
            <span v-else>{{ scope.row.batchNo }}</span>
          </template>
        </el-table-column>
        <el-table-column label="效期到期日" align="center" prop="expireDate" width="142">
          <template #default="scope">
            <el-date-picker
              v-if="isInboundItemEditable()"
              v-model="scope.row.expireDate"
              type="date"
              value-format="YYYY-MM-DD"
              size="small"
              placeholder="可选效期"
              clearable
              style="width: 128px"
            />
            <span v-else>{{ scope.row.expireDate ? parseTime(scope.row.expireDate, '{y}-{m}-{d}') : '-' }}</span>
          </template>
        </el-table-column>
        <el-table-column label="数量" align="right" prop="quantity" width="122">
          <template #default="scope">
            <el-input-number
              v-if="isInboundItemEditable()"
              v-model="scope.row.quantity"
              :min="0.01"
              :precision="2"
              size="small"
              controls-position="right"
              style="width: 105px"
              @change="handleInlineInboundRowChange(scope.row)"
            />
            <span v-else>{{ scope.row.quantity }}</span>
          </template>
        </el-table-column>
        <el-table-column label="单价" align="right" prop="price" width="86" />
        <el-table-column label="金额" align="right" prop="amount" width="96" />
        <el-table-column v-if="isInboundItemEditable()" label="操作" width="130" fixed="right" align="center">
          <template #default="scope">
            <div class="erp-row-actions">
              <el-button v-if="isInboundItemEditable()" link type="primary" icon="Edit" @click="handleUpdateInboundItem(scope.row)" v-hasPermi="['business:inbound:edit']">修改</el-button>
              <el-button v-if="isInboundItemEditable() && !isLinkedPurchaseOrderInboundItemMode()" link type="danger" icon="Delete" @click="handleDeleteInboundItem(scope.row)" v-hasPermi="['business:inbound:remove']">删除</el-button>
            </div>
          </template>
        </el-table-column>
      </el-table>
    </el-dialog>

    <el-dialog :title="inboundItemFormTitle" v-model="inboundItemFormOpen" width="760px" append-to-body>
      <el-form ref="inboundItemRef" :model="inboundItemForm" :rules="inboundItemRules" label-width="110px">
        <el-alert
          v-if="inboundItemForm.purchaseOrderItemId"
          :title="getInboundItemSourceProgressText(inboundItemForm)"
          type="info"
          :closable="false"
          class="mb8"
        />
        <el-row>
          <el-col :span="12">
            <el-form-item label="商品" prop="productId">
              <el-select v-model="inboundItemForm.productId" placeholder="请选择商品" clearable filterable remote reserve-keyword remote-show-suffix :remote-method="loadProductOptionList" :loading="productListLoading" style="width: 100%" :disabled="!!inboundItemForm.purchaseOrderItemId">
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
              <el-select v-model="inboundItemForm.locationId" placeholder="请选择库位" clearable filterable remote reserve-keyword remote-show-suffix :remote-method="loadLocationOptionList" :loading="locationListLoading" style="width: 100%">
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
              <el-input v-model="inboundItemForm.batchNo" placeholder="请输入批次号" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="效期到期日" prop="expireDate">
              <el-date-picker v-model="inboundItemForm.expireDate" type="date" value-format="YYYY-MM-DD" placeholder="可选，批次效期到期日" clearable style="width: 100%" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-row>
          <el-col :span="12">
            <el-form-item label="数量" prop="quantity">
              <el-input-number v-model="inboundItemForm.quantity" :min="0.01" :precision="2" controls-position="right" style="width: 100%" @change="syncInboundItemAmount" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="单价" prop="price">
              <el-input-number v-model="inboundItemForm.price" :min="0" :precision="2" controls-position="right" style="width: 100%" :disabled="!!inboundItemForm.purchaseOrderItemId" @change="syncInboundItemAmount" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-row>
          <el-col :span="12">
            <el-form-item label="金额" prop="amount">
              <el-input-number v-model="inboundItemForm.amount" :min="0" :precision="2" controls-position="right" style="width: 100%" disabled />
            </el-form-item>
          </el-col>
        </el-row>
      </el-form>
      <template #footer>
        <div class="dialog-footer">
          <el-button type="primary" @click="submitInboundItemForm">确 定</el-button>
          <el-button @click="cancelInboundItemForm">取 消</el-button>
        </div>
      </template>
    </el-dialog>

    <document-print-dialog ref="printDialogRef" />
  </div>
</template>

<script setup name="Inbound">
import DocumentPrintDialog from "@/components/DocumentPrintDialog/index.vue"
import { listInbound, getInbound, addInbound, updateInbound, delInbound, submitInbound, auditInbound, batchSubmitInbound, batchAuditInbound, rejectInbound, cancelInbound } from "@/api/business/inbound"
import { listInboundItem, getInboundItem, addInboundItem, updateInboundItem, batchUpdateInboundItem, delInboundItem } from "@/api/business/inboundItem"
import { listSupplier, getSupplier } from "@/api/business/supplier"
import { listPurchaseOrder, getPurchaseOrder } from "@/api/business/purchaseOrder"
import { listWarehouse, getWarehouse } from "@/api/business/warehouse"
import { listProduct, getProduct, scanProductByCode } from "@/api/business/product"
import { listLocation, getLocation } from "@/api/business/location"
import { appendUniqueSelectOption, buildSelectOptionList, normalizeRemoteKeyword } from "@/utils/remoteSelect"
import { buildDocumentSelectionState, resolveDocumentActionRow } from "@/utils/documentPage"
import { ERP_DATE_SHORTCUTS, buildTodayDateString } from "@/utils/erpFormat"
import { parseTime } from "@/utils/ruoyi"

const { proxy } = getCurrentInstance()
const route = useRoute()

const inboundList = ref([])
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
const dateRange = ref([])
const poSelectLoading = ref(false)
const batchSaveInboundLoading = ref(false)
const total = ref(0)
const title = ref("")
const inboundItemOpen = ref(false)
const inboundItemLoading = ref(false)
const inboundItemTitle = ref("")
const inboundItemList = ref([])
const currentInboundId = ref(undefined)
const currentInboundStatus = ref(undefined)
const currentInboundPurchaseOrderId = ref(undefined)
const currentInboundWarehouseId = ref(undefined)
const selectedInboundItemIds = ref([])
const inboundItemMultiple = ref(true)
const inboundItemFormOpen = ref(false)
const inboundItemFormTitle = ref("")
const inboundScanCode = ref("")
const inboundScanLoading = ref(false)
const printDialogRef = ref(null)
const supplierList = ref([])
const warehouseList = ref([])
const productList = ref([])
const locationList = ref([])
const purchaseOrderSupplierNameMap = ref({})
const availablePurchaseOrderList = ref([])
const supplierListLoading = ref(false)
const warehouseListLoading = ref(false)
const productListLoading = ref(false)
const locationListLoading = ref(false)

const inboundTypeOptionList = [
  { label: "采购入库", value: "purchase" }
]

const inboundStatusOptionList = [
  { label: "草稿", value: "draft", type: "info" },
  { label: "已提交", value: "submitted", type: "warning" },
  { label: "已审核", value: "audited", type: "success" },
  { label: "已作废", value: "cancelled", type: "danger" }
]

const data = reactive({
  form: {},
  inboundItemForm: {},
  queryParams: {
    pageNum: 1,
    pageSize: 10,
    inboundNo: undefined,
    purchaseOrderId: undefined,
    purchaseOrderNo: undefined,
    inboundType: undefined,
    supplierId: undefined,
    warehouseId: undefined,
    status: undefined
  },
  rules: {
    inboundType: [{ required: true, message: "入库类型不能为空", trigger: "change" }],
    supplierId: [{ required: true, message: "供应商不能为空", trigger: "change" }],
    warehouseId: [{ required: true, message: "仓库不能为空", trigger: "change" }]
  },
  inboundItemRules: {
    productId: [{ required: true, message: "商品不能为空", trigger: "change" }],
    locationId: [{ required: true, message: "库位不能为空", trigger: "change" }],
    batchNo: [{ required: true, message: "批次号不能为空", trigger: "blur" }],
    quantity: [{ required: true, message: "数量不能为空", trigger: "change" }],
    price: [{ required: true, message: "单价不能为空", trigger: "change" }]
  }
})

const { queryParams, form, rules, inboundItemForm, inboundItemRules } = toRefs(data)

const inboundStatusTabItems = [
  { label: "全部", value: "" },
  { label: "草稿", value: "draft", dot: "slate" },
  { label: "待审核", value: "submitted", dot: "amber" },
  { label: "已审核", value: "audited", dot: "emerald" },
  { label: "已作废", value: "cancelled", dot: "rose" }
]

const inboundStatItems = computed(() => {
  const rows = inboundList.value || []
  const draftCount = rows.filter(r => r.status === "draft").length
  const submittedCount = rows.filter(r => r.status === "submitted").length
  const pageAmount = rows.reduce((s, r) => s + Number(r.totalAmount || 0), 0)
  const curStatus = queryParams.value.status ?? ""
  return [
    { key: "all", statusVal: "", label: "入库单总数", value: total.value, suffix: "单", hint: "点击查看全部入库单", icon: "Download", tone: "emerald", clickable: true, active: curStatus === "" },
    { key: "draft", statusVal: "draft", label: "本页草稿待提交", value: draftCount, suffix: "单", hint: "点击筛选草稿入库单", icon: "EditPen", tone: "slate", clickable: true, active: curStatus === "draft" },
    { key: "submitted", statusVal: "submitted", label: "本页待审核入库", value: submittedCount, suffix: "单", hint: "点击筛选待审核入库单", icon: "Stamp", tone: "amber", clickable: true, active: curStatus === "submitted" },
    { key: "amt", label: "本页入库总额", prefix: "¥", value: pageAmount.toFixed(2), hint: "本页收货货值合计", icon: "Money", tone: "indigo" }
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
const locationSelectOptionList = computed(() => buildLocationSelectOptionList())

// 判断入库单是否还处于草稿状态，统一顶部按钮和行内按钮的可操作条件。
function isDraftInboundStatus(statusValue) {
  return statusValue === "draft"
}

// 判断当前入库单表单是否允许继续编辑，避免非草稿单据被其他入口打开后仍可修改。
function isInboundFormEditable() {
  return isDraftInboundStatus(form.value.status)
}

// 判断当前入库单是否已关联来源采购单，已关联时应锁定来源单自动带出的字段。
function hasLinkedPurchaseOrder() {
  return !!form.value.purchaseOrderId
}

// 判断当前入库明细是否来自来源采购单自动带入，已关联时不再允许手工新增或删除整行。
function isLinkedPurchaseOrderInboundItemMode() {
  return !!currentInboundPurchaseOrderId.value
}

// 统一处理入库单弹窗主按钮行为，避免只读场景下仍然触发保存提示。
function handleInboundDialogPrimaryAction() {
  if (!isInboundFormEditable()) {
    cancel()
    return
  }
  submitForm()
}

// 根据首页和采购订单等页面传入的参数初始化入库单筛选条件。
function initializeQueryParamsFromRoute() {
  queryParams.value.purchaseOrderId = convertRouteNumberValue(route.query.purchaseOrderId)
  queryParams.value.purchaseOrderNo = route.query.purchaseOrderNo || undefined
  queryParams.value.inboundType = route.query.inboundType || undefined
  queryParams.value.supplierId = convertRouteNumberValue(route.query.supplierId)
  queryParams.value.status = route.query.status || undefined
}

// 将路由中的数字参数转换为数值类型，避免空字符串或异常值影响筛选和表单初始化。
function convertRouteNumberValue(routeValue) {
  if (routeValue === undefined || routeValue === null || routeValue === "") {
    return undefined
  }
  const numberValue = Number(routeValue)
  return Number.isNaN(numberValue) ? undefined : numberValue
}

// 将路由中的来源采购单信息带入新增表单，减少采购转入库时的重复录入。
function applyRoutePurchaseOrderToForm() {
  if (!queryParams.value.purchaseOrderId) {
    return
  }
  form.value.purchaseOrderId = queryParams.value.purchaseOrderId
  form.value.purchaseOrderNo = queryParams.value.purchaseOrderNo
  form.value.supplierId = queryParams.value.supplierId
  form.value.inboundType = "purchase"
}

// 初始化入库单依赖的远程下拉数据，避免页面初始阶段一次性加载过多基础资料。
function initBasicData() {
  return Promise.all([
    loadSupplierOptionList(),
    loadWarehouseOptionList(),
    loadProductOptionList(),
    loadLocationOptionList()
  ])
}

// 远程加载供应商下拉选项，按需补充供应商名称数据，减少页面初始加载压力。
function loadSupplierOptionList(searchKeyword) {
  supplierListLoading.value = true
  listSupplier({
    pageNum: 1,
    pageSize: 20,
    status: "0",
    supplierName: normalizeRemoteKeyword(searchKeyword)
  }).then(response => {
    const supplierRowList = response.rows || []
    supplierRowList.forEach(supplierItem => {
      appendUniqueSelectOption(supplierList.value, supplierItem, "supplierId")
    })
  }).finally(() => {
    supplierListLoading.value = false
  })
}

// 远程加载仓库下拉选项，按需补充仓库名称数据，减少页面初始加载压力。
function loadWarehouseOptionList(searchKeyword) {
  warehouseListLoading.value = true
  listWarehouse({
    pageNum: 1,
    pageSize: 20,
    status: "0",
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
    status: "0",
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

// 远程加载库位下拉选项，按当前入库单仓库限定范围，防止跨仓错选库位。
function loadLocationOptionList(searchKeyword) {
  locationListLoading.value = true
  listLocation({
    pageNum: 1,
    pageSize: 50,
    status: "0",
    warehouseId: currentInboundWarehouseId.value || form.value.warehouseId || undefined,
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

// 同步供应商下拉选项，允许把历史编号回显更新成更适合用户理解的名称。
function syncSupplierOption(supplierId, supplierName) {
  if (supplierId === undefined || supplierId === null || supplierId === "" || !supplierName) {
    return
  }
  const supplierIndex = supplierList.value.findIndex(supplierItem => supplierItem.supplierId === supplierId)
  if (supplierIndex >= 0) {
    supplierList.value[supplierIndex] = {
      ...supplierList.value[supplierIndex],
      supplierId: supplierId,
      supplierName: supplierName
    }
    return
  }
  supplierList.value.push({
    supplierId: supplierId,
    supplierName: supplierName
  })
}

// 判断当前供应商名称是否已经是可直接展示给用户的正式名称。
function isDirectSupplierNameResolved(supplierName) {
  return !!supplierName && supplierName !== "供应商资料缺失" && !supplierName.startsWith("历史供应商ID：")
}

// 按来源采购订单回补历史入库记录的供应商名称，减少页面出现裸供应商编号。
function ensureSupplierOptionLoadedByPurchaseOrder(supplierId, purchaseOrderId) {
  if (!purchaseOrderId) {
    syncSupplierOption(supplierId, "供应商资料缺失")
    return Promise.resolve()
  }
  const cachedSupplierName = purchaseOrderSupplierNameMap.value[purchaseOrderId]
  if (cachedSupplierName) {
    syncSupplierOption(supplierId, cachedSupplierName)
    return Promise.resolve()
  }
  return getPurchaseOrder(purchaseOrderId).then(response => {
    const resolvedSupplierId = response.data && response.data.supplierId
    if (resolvedSupplierId === undefined || resolvedSupplierId === null || resolvedSupplierId === "") {
      syncSupplierOption(supplierId, "供应商资料缺失")
      return
    }
    return getSupplier(resolvedSupplierId).then(supplierResponse => {
      const resolvedSupplierName = supplierResponse.data && supplierResponse.data.supplierName ? supplierResponse.data.supplierName : `供应商${resolvedSupplierId}`
      purchaseOrderSupplierNameMap.value[purchaseOrderId] = resolvedSupplierName
      syncSupplierOption(supplierId, resolvedSupplierName)
    }).catch(() => {
      syncSupplierOption(supplierId, "供应商资料缺失")
    })
  }).catch(() => {
    syncSupplierOption(supplierId, "供应商资料缺失")
  })
}

// 按供应商编号补充当前页面缺失的供应商名称，必要时按来源采购订单回补历史记录名称。
function ensureSupplierOptionLoaded(supplierId, purchaseOrderId) {
  if (supplierId === undefined || supplierId === null || supplierId === "") {
    return Promise.resolve()
  }
  const existingSupplier = supplierList.value.find(supplierItem => supplierItem.supplierId === supplierId)
  if (existingSupplier && isDirectSupplierNameResolved(existingSupplier.supplierName)) {
    return Promise.resolve()
  }
  return getSupplier(supplierId).then(response => {
    syncSupplierOption(response.data.supplierId, response.data.supplierName)
  }).catch(() => {
    return ensureSupplierOptionLoadedByPurchaseOrder(supplierId, purchaseOrderId)
  })
}

// 按仓库编号补充当前页面缺失的仓库名称，避免远程下拉场景下只显示历史编号。
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

// 按商品编号补充当前页面缺失的商品名称，保证入库明细远程下拉场景下的名称回显稳定。
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

// 按库位编号补充当前页面缺失的库位名称，保证入库明细远程下拉场景下的名称回显稳定。
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

// 批量补充入库主单依赖的供应商与仓库名称，优先复用后端关联字段，消除冗余 N+1 查询。
function ensureInboundReferenceOptionsLoaded(inboundRowList) {
  const missingSupplierRows = []
  const warehouseIdList = []
  ;(inboundRowList || []).forEach(inboundRow => {
    if (inboundRow.supplierId != null && inboundRow.supplierName) {
      appendUniqueSelectOption(supplierList.value, { supplierId: inboundRow.supplierId, supplierName: inboundRow.supplierName }, "supplierId")
    } else {
      missingSupplierRows.push(inboundRow)
    }
    if (inboundRow.warehouseId != null && inboundRow.warehouseId !== "") {
      if (inboundRow.warehouseName) {
        appendUniqueSelectOption(warehouseList.value, { warehouseId: inboundRow.warehouseId, warehouseName: inboundRow.warehouseName }, "warehouseId")
      } else {
        warehouseIdList.push(inboundRow.warehouseId)
      }
    }
  })
  Promise.all([
    ...missingSupplierRows.map(inboundRow => ensureSupplierOptionLoaded(inboundRow.supplierId, inboundRow.purchaseOrderId)),
    ...[...new Set(warehouseIdList)].map(warehouseId => ensureWarehouseOptionLoaded(warehouseId))
  ]).catch(() => {})
}

// 批量补充入库明细依赖的商品与库位名称，保证远程下拉模式下明细展示仍然可读。
function ensureInboundItemReferenceOptionsLoaded(inboundItemRowList) {
  const productIdList = [...new Set((inboundItemRowList || []).map(inboundItemRow => inboundItemRow.productId).filter(productId => productId !== undefined && productId !== null && productId !== ""))]
  const locationIdList = [...new Set((inboundItemRowList || []).map(inboundItemRow => inboundItemRow.locationId).filter(locationId => locationId !== undefined && locationId !== null && locationId !== ""))]
  Promise.all([
    ...productIdList.map(productId => ensureProductOptionLoaded(productId)),
    ...locationIdList.map(locationId => ensureLocationOptionLoaded(locationId))
  ]).catch(() => {})
}

// 组合供应商下拉选项，兼容历史入库单中主数据缺失时的兜底回显。
function buildSupplierSelectOptionList() {
  return buildSelectOptionList(supplierList.value, [queryParams.value.supplierId, form.value.supplierId], "supplierId", "supplierName", () => "供应商资料缺失")
}

// 组合仓库下拉选项，兼容历史入库单中主数据缺失时的兜底回显。
function buildWarehouseSelectOptionList() {
  return buildSelectOptionList(warehouseList.value, [queryParams.value.warehouseId, form.value.warehouseId], "warehouseId", "warehouseName", () => "仓库资料缺失")
}

// 组合商品下拉选项，兼容历史入库明细中主数据缺失时的兜底回显。
function buildProductSelectOptionList() {
  return buildSelectOptionList(productList.value, [inboundItemForm.value.productId], "productId", "productName", () => "商品资料缺失")
}

// 组合库位下拉选项，兼容历史入库明细中主数据缺失时的兜底回显。
function buildLocationSelectOptionList() {
  const targetWarehouseId = currentInboundWarehouseId.value || form.value.warehouseId
  const filtered = targetWarehouseId
    ? locationList.value.filter(loc => (!loc.warehouseId || Number(loc.warehouseId) === Number(targetWarehouseId)) && loc.status !== "1")
    : locationList.value.filter(loc => loc.status !== "1")
  const referencedIds = [
    inboundItemForm.value.locationId,
    ...(inboundItemList.value || []).map(item => item.locationId)
  ]
  return buildSelectOptionList(filtered, referencedIds, "locationId", "locationName", () => "库位资料缺失")
}

// 根据供应商编号和来源采购订单返回供应商名称，优先展示回补后的正式供应商名称。
function getSupplierName(supplierId, purchaseOrderId) {
  const supplier = supplierList.value.find(supplierItem => supplierItem.supplierId === supplierId)
  if (supplier && supplier.supplierName) {
    return supplier.supplierName
  }
  const purchaseOrderSupplierName = purchaseOrderSupplierNameMap.value[purchaseOrderId]
  if (purchaseOrderSupplierName) {
    return purchaseOrderSupplierName
  }
  return supplierId ? "供应商资料缺失" : "未关联供应商"
}

// 根据仓库编号返回仓库名称，统一入库单页面展示口径。
function getWarehouseName(warehouseId) {
  const warehouse = warehouseList.value.find(warehouseItem => warehouseItem.warehouseId === warehouseId)
  return warehouse ? warehouse.warehouseName : (warehouseId ? "仓库资料缺失" : "")
}

// 根据商品编号返回商品名称，帮助用户在入库明细中快速确认商品。
function getProductName(productId) {
  const product = productList.value.find(productItem => productItem.productId === productId)
  return product ? product.productName : (productId ? "商品资料缺失" : "")
}

// 根据库位编号返回库位名称，统一入库明细展示口径。
function getLocationName(locationId) {
  const location = locationList.value.find(locationItem => locationItem.locationId === locationId)
  return location ? location.locationName : (locationId ? "库位资料缺失" : "")
}

// 返回入库类型中文名称，减少列表中的英文编码展示。
function getInboundTypeLabel(inboundTypeValue) {
  const inboundTypeOption = inboundTypeOptionList.find(typeOption => typeOption.value === inboundTypeValue)
  return inboundTypeOption ? inboundTypeOption.label : (inboundTypeValue || "未知类型")
}

// 返回入库状态中文名称，统一列表和表单展示口径。
function getInboundStatusLabel(statusValue) {
  const inboundStatusOption = inboundStatusOptionList.find(statusOption => statusOption.value === statusValue)
  return inboundStatusOption ? inboundStatusOption.label : (statusValue || "未知状态")
}

// 返回入库状态标签类型，帮助用户快速识别当前流程阶段。
function getInboundStatusType(statusValue) {
  const inboundStatusOption = inboundStatusOptionList.find(statusOption => statusOption.value === statusValue)
  return inboundStatusOption ? inboundStatusOption.type : "info"
}

// 根据采购单号和采购订单编号生成可读的来源采购单展示内容。
function getPurchaseOrderDisplayName(purchaseOrderNo, purchaseOrderId) {
  if (purchaseOrderNo) {
    return purchaseOrderNo
  }
  if (purchaseOrderId) {
    return "采购单资料缺失"
  }
  return "未关联采购单"
}

function getList() {
  loading.value = true
  return listInbound(proxy.addDateRange(queryParams.value, dateRange.value)).then(response => {
    inboundList.value = response.rows
    total.value = response.total
    ensureInboundReferenceOptionsLoaded(response.rows)
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
    inboundId: undefined,
    inboundNo: undefined,
    inboundType: "purchase",
    purchaseOrderId: undefined,
    purchaseOrderNo: undefined,
    supplierId: undefined,
    warehouseId: undefined,
    totalQty: 0,
    totalAmount: 0,
    status: "draft",
    auditBy: undefined,
    auditTime: undefined,
    remark: undefined
  }
  proxy.resetForm("inboundRef")
}

function resetInboundItemForm() {
  inboundItemForm.value = {
    inboundItemId: undefined,
    inboundId: currentInboundId.value,
    purchaseOrderItemId: undefined,
    productId: undefined,
    locationId: undefined,
    batchNo: undefined,
    expireDate: undefined,
    quantity: 0,
    price: 0,
    amount: 0,
    purchaseQuantity: undefined,
    receivedQuantity: undefined,
    remainQuantity: undefined
  }
  proxy.resetForm("inboundItemRef")
}

// 格式化来源采购数量、已入库和待入库字段，避免未关联时直接显示空白或 undefined。
function formatInboundItemQuantity(quantityValue) {
  if (quantityValue === undefined || quantityValue === null || quantityValue === "") {
    return "-"
  }
  return Number(quantityValue).toFixed(2)
}

// 根据数量和单价自动计算入库明细金额，避免仓库人员重复手工录金额。
function syncInboundItemAmount() {
  const quantityValue = Number(inboundItemForm.value.quantity || 0)
  const priceValue = Number(inboundItemForm.value.price || 0)
  inboundItemForm.value.amount = (quantityValue * priceValue).toFixed(2)
}

// 扫码入库快捷匹配：存在同商品明细直接 +1，否则预填新增明细表单。
async function handleInboundBarcodeScan() {
  if (!ensureInboundItemEditable("扫码录入")) {
    return
  }
  const code = String(inboundScanCode.value || "").trim()
  if (!code) {
    proxy.$modal.msgWarning("请扫描或输入商品条码/编码")
    return
  }
  inboundScanLoading.value = true
  try {
    const res = await scanProductByCode(code)
    const product = res.data
    if (!product?.productId) return
    appendUniqueSelectOption(productList.value, product, "productId")
    const existingItem = (inboundItemList.value || []).find(item => item.productId === product.productId)
    if (existingItem) {
      const nextQty = Number((Number(existingItem.quantity || 0) + 1).toFixed(2))
      const priceVal = Number(existingItem.price || product.costPrice || 0)
      await updateInboundItem({
        ...existingItem,
        quantity: nextQty,
        amount: Number((nextQty * priceVal).toFixed(2))
      })
      proxy.$modal.msgSuccess(`扫码入库：${product.productName} 数量 +1（当前 ${nextQty}）`)
      inboundScanCode.value = ""
      getInboundItemList()
      getList()
    } else if (isLinkedPurchaseOrderInboundItemMode()) {
      proxy.$modal.msgWarning(`采购关联入库单中不包含商品「${product.productName}」`)
    } else {
      resetInboundItemForm()
      inboundItemForm.value.productId = product.productId
      inboundItemForm.value.quantity = 1
      inboundItemForm.value.price = Number(product.costPrice || 0)
      inboundItemForm.value.amount = Number(product.costPrice || 0).toFixed(2)
      inboundItemFormOpen.value = true
      inboundItemFormTitle.value = `扫码新增入库明细 - ${product.productName}`
      inboundScanCode.value = ""
    }
  } finally {
    inboundScanLoading.value = false
  }
}

// 生成来源采购明细的进度提示，让仓库在编辑时能直接看到采购数量和剩余可收数量。
function getInboundItemSourceProgressText(targetInboundItem) {
  return `来源采购明细已带入：采购数量 ${formatInboundItemQuantity(targetInboundItem.purchaseQuantity)}，已入库 ${formatInboundItemQuantity(targetInboundItem.receivedQuantity)}，待入库 ${formatInboundItemQuantity(targetInboundItem.remainQuantity)}。`
}

function handleQuery() {
  queryParams.value.pageNum = 1
  getList()
}

function resetQuery() {
  dateRange.value = []
  proxy.resetForm("queryRef")
  queryParams.value.purchaseOrderId = undefined
  queryParams.value.purchaseOrderNo = undefined
  handleQuery()
}

function handleSelectionChange(selection) {
  const selectionState = buildDocumentSelectionState(selection, "inboundId")
  ids.value = selectionState.idList
  single.value = selectionState.single
  multiple.value = selectionState.multiple
  canUpdateSelected.value = selection.length === 1 && isDraftInboundStatus(selection[0].status)
  canSubmitSelected.value = selection.length > 0 && selection.every(selectionItem => isDraftInboundStatus(selectionItem.status))
  canAuditSelected.value = selection.length > 0 && selection.every(selectionItem => selectionItem.status === "submitted")
  canDeleteSelected.value = selection.length > 0 && selection.every(selectionItem => isDraftInboundStatus(selectionItem.status))
}

function loadAvailablePurchaseOrders(keyword) {
  poSelectLoading.value = true
  const orderNo = normalizeRemoteKeyword(keyword)
  Promise.all([
    listPurchaseOrder({ pageNum: 1, pageSize: 50, status: "1", orderNo }),
    listPurchaseOrder({ pageNum: 1, pageSize: 50, status: "2", orderNo })
  ]).then(([r1, r2]) => {
    const merged = [...(r1.rows || []), ...(r2.rows || [])]
    availablePurchaseOrderList.value = merged.filter(po => Number(po.remainQuantity ?? po.totalQuantity ?? 1) > 0)
  }).catch(() => {}).finally(() => {
    poSelectLoading.value = false
  })
}

function handleInlineInboundRowChange(row) {
  if (!row) return
  const qty = Number(row.quantity || 0)
  const price = Number(row.price || 0)
  row.amount = (qty * price).toFixed(2)
}

function handleQuickFillInboundDefaults() {
  if (!ensureInboundItemEditable("填充")) return
  const defaultLoc = locationSelectOptionList.value[0]?.locationId
  const defaultBatch = "B" + buildTodayDateString().replace(/-/g, "")
  let updated = 0
  ;(inboundItemList.value || []).forEach(row => {
    if (!row.locationId && defaultLoc) {
      row.locationId = defaultLoc
      updated++
    }
    if (!row.batchNo || !String(row.batchNo).trim()) {
      row.batchNo = defaultBatch
      updated++
    }
  })
  if (updated > 0) {
    proxy.$modal.msgSuccess("已自动填充缺失库位与今日批次号，核对后请点击「保存全部明细」")
  } else {
    proxy.$modal.msgInfo("当前明细均已填写库位与批次号")
  }
}

async function handleBatchSaveInboundItems() {
  if (!ensureInboundItemEditable("保存")) return
  if (!inboundItemList.value.length) return
  for (let i = 0; i < inboundItemList.value.length; i++) {
    const row = inboundItemList.value[i]
    if (!row.locationId) {
      proxy.$modal.msgWarning(`第 ${i + 1} 行请选择库位`)
      return
    }
    if (!row.batchNo || !String(row.batchNo).trim()) {
      proxy.$modal.msgWarning(`第 ${i + 1} 行请填写批次号`)
      return
    }
    if (!(Number(row.quantity || 0) > 0)) {
      proxy.$modal.msgWarning(`第 ${i + 1} 行数量必须大于 0`)
      return
    }
  }
  batchSaveInboundLoading.value = true
  try {
    const payload = inboundItemList.value.map(row => {
      const qty = Number(row.quantity || 0)
      const price = Number(row.price || 0)
      return {
        ...row,
        inboundId: currentInboundId.value,
        batchNo: String(row.batchNo).trim(),
        quantity: qty,
        amount: Number((qty * price).toFixed(2))
      }
    })
    const res = await batchUpdateInboundItem(payload)
    proxy.$modal.msgSuccess(res.msg || "入库明细已全部保存")
    getInboundItemList()
    getList()
  } finally {
    batchSaveInboundLoading.value = false
  }
}

function handleExportInboundItems() {
  if (!currentInboundId.value) return
  proxy.download("business/inboundItem/export", {
    inboundId: currentInboundId.value
  }, `inboundItem_${currentInboundId.value}_${new Date().getTime()}.xlsx`)
}

function handlePurchaseOrderSelectChange(poId) {
  if (!poId) {
    form.value.purchaseOrderNo = undefined
    return
  }
  const po = availablePurchaseOrderList.value.find(item => item.purchaseOrderId === poId)
  if (po) {
    form.value.purchaseOrderNo = po.orderNo
    form.value.inboundType = "purchase"
    form.value.supplierId = po.supplierId
    if (po.supplierId && po.supplierName) {
      appendUniqueSelectOption(supplierList.value, { supplierId: po.supplierId, supplierName: po.supplierName }, "supplierId")
    }
  }
}

function handleAdd() {
  reset()
  loadAvailablePurchaseOrders()
  applyRoutePurchaseOrderToForm()
  ensureSupplierOptionLoaded(form.value.supplierId, form.value.purchaseOrderId)
  open.value = true
  title.value = queryParams.value.purchaseOrderId ? "新增采购入库单" : "新增入库单"
}

function handleUpdate(row) {
  const currentInboundRow = row || inboundList.value.find(inboundItem => inboundItem.inboundId === ids.value[0])
  if (currentInboundRow && !isDraftInboundStatus(currentInboundRow.status)) {
    proxy.$modal.msgWarning("只有草稿状态的入库单才允许修改")
    return
  }
  reset()
  const inboundId = row?.inboundId || ids.value[0]
  getInbound(inboundId).then(response => {
    form.value = response.data
    ensureSupplierOptionLoaded(form.value.supplierId, form.value.purchaseOrderId)
    ensureWarehouseOptionLoaded(form.value.warehouseId)
    open.value = true
    title.value = "修改入库单"
  })
}

// 从入库单直接回跳来源采购订单，减少采购与仓库之间的来回查找。
function handleViewPurchaseOrder(row) {
  if (!row.purchaseOrderId) {
    return
  }
  proxy.$router.push({
    path: "/purchase/purchaseOrder",
    query: {
      purchaseOrderId: row.purchaseOrderId
    }
  })
}

// 打开入库明细弹窗时补充当前流程状态，帮助用户判断当前是可维护还是只读查看。
function handleOpenInboundItem(row) {
  currentInboundId.value = row.inboundId
  currentInboundStatus.value = row.status
  currentInboundPurchaseOrderId.value = row.purchaseOrderId
  currentInboundWarehouseId.value = row.warehouseId
  loadLocationOptionList()
  inboundItemTitle.value = "入库明细 - " + row.inboundNo + "（" + getInboundStatusLabel(row.status) + "）"
  inboundItemOpen.value = true
  inboundScanCode.value = ""
  selectedInboundItemIds.value = []
  inboundItemMultiple.value = true
  getInboundItemList()
}

// 判断当前入库明细是否允许继续维护，避免已提交或已审核单据继续改动明细。
function isInboundItemEditable() {
  return currentInboundStatus.value === "draft"
}

// 校验入库明细当前是否允许维护，避免只依赖按钮显示状态造成误操作。
function ensureInboundItemEditable(actionLabel) {
  if (!isInboundItemEditable()) {
    proxy.$modal.msgWarning("当前入库单已进入后续流程，不能" + actionLabel + "明细")
    return false
  }
  return true
}

function getInboundItemList() {
  if (!currentInboundId.value) {
    inboundItemList.value = []
    return
  }
  inboundItemLoading.value = true
  listInboundItem({
    pageNum: 1,
    pageSize: 1000,
    inboundId: currentInboundId.value
  }).then(response => {
    inboundItemList.value = response.rows || []
    ensureInboundItemReferenceOptionsLoaded(inboundItemList.value)
    inboundItemLoading.value = false
  }).catch(() => {
    inboundItemLoading.value = false
  })
}

function handleInboundItemSelectionChange(selection) {
  selectedInboundItemIds.value = selection.map(item => item.inboundItemId)
  inboundItemMultiple.value = !selection.length
}

// 新增入库明细前再次校验主单状态，避免通过其他入口绕过只读限制。
function handleAddInboundItem() {
  if (!ensureInboundItemEditable("新增")) {
    return
  }
  if (isLinkedPurchaseOrderInboundItemMode()) {
    proxy.$modal.msgWarning("来源采购单的入库明细由系统自动带入，不支持手工新增")
    return
  }
  resetInboundItemForm()
  inboundItemFormOpen.value = true
  inboundItemFormTitle.value = "新增入库明细"
}

// 修改入库明细前再次校验主单状态，确保非草稿单据只能查看明细。
function handleUpdateInboundItem(row) {
  if (!ensureInboundItemEditable("修改")) {
    return
  }
  resetInboundItemForm()
  const inboundItemId = row.inboundItemId
  getInboundItem(inboundItemId).then(response => {
    inboundItemForm.value = response.data
    ensureProductOptionLoaded(inboundItemForm.value.productId)
    ensureLocationOptionLoaded(inboundItemForm.value.locationId)
    syncInboundItemAmount()
    inboundItemFormOpen.value = true
    inboundItemFormTitle.value = "修改入库明细"
  })
}

function cancelInboundItemForm() {
  inboundItemFormOpen.value = false
  resetInboundItemForm()
}

// 保存入库明细前再次校验主单状态，避免后续流程中的明细被间接修改。
function submitInboundItemForm() {
  if (!ensureInboundItemEditable("保存")) {
    return
  }
  syncInboundItemAmount()
  proxy.$refs["inboundItemRef"].validate(valid => {
    if (valid) {
      inboundItemForm.value.inboundId = currentInboundId.value
      if (inboundItemForm.value.inboundItemId !== undefined) {
        updateInboundItem(inboundItemForm.value).then(() => {
          proxy.$modal.msgSuccess("修改成功")
          inboundItemFormOpen.value = false
          getInboundItemList()
          getList()
        })
      } else {
        addInboundItem(inboundItemForm.value).then(() => {
          proxy.$modal.msgSuccess("新增成功")
          inboundItemFormOpen.value = false
          getInboundItemList()
          getList()
        })
      }
    }
  })
}

// 删除入库明细前再次校验主单状态，避免非草稿单据的明细被批量删除。
function handleDeleteInboundItem(row) {
  if (!ensureInboundItemEditable("删除")) {
    return
  }
  const inboundItemIds = row ? row.inboundItemId : selectedInboundItemIds.value
  proxy.$modal.confirm('是否确认删除入库明细编号为"' + inboundItemIds + '"的数据项？').then(function () {
    return delInboundItem(inboundItemIds)
  }).then(() => {
    proxy.$modal.msgSuccess("删除成功")
    getInboundItemList()
    getList()
  }).catch(() => {})
}

function submitForm() {
  if (!isInboundFormEditable()) {
    proxy.$modal.msgWarning("只有草稿状态的入库单才允许保存")
    return
  }
  proxy.$refs["inboundRef"].validate(valid => {
    if (valid) {
      if (form.value.inboundId !== undefined) {
        updateInbound(form.value).then(() => {
          proxy.$modal.msgSuccess("修改成功")
          open.value = false
          getList()
        })
      } else {
        addInbound(form.value).then(() => {
          proxy.$modal.msgSuccess("新增成功")
          open.value = false
          getList()
        })
      }
    }
  })
}

function handleDelete(row) {
  if (row && !isDraftInboundStatus(row.status)) {
    proxy.$modal.msgWarning("只有草稿状态的入库单才允许删除")
    return
  }
  if (!row && !ids.value.length) {
    proxy.$modal.msgWarning("请选择要删除的入库单")
    return
  }
  if (!row && !canDeleteSelected.value) {
    proxy.$modal.msgWarning("只能删除草稿状态的入库单")
    return
  }
  const inboundIds = row?.inboundId || ids.value
  proxy.$modal.confirm('是否确认删除入库单编号为"' + inboundIds + '"的数据项？').then(function () {
    return delInbound(inboundIds)
  }).then(() => {
    getList()
    proxy.$modal.msgSuccess("删除成功")
  }).catch(() => {})
}

// 获取当前要处理的入库单，统一兼容行内操作和后续可能扩展的工具栏入口。
function getTargetInboundRow(row, actionText) {
  return resolveDocumentActionRow(
    row,
    ids.value,
    inboundList.value,
    "inboundId",
    actionText,
    "入库单",
    warningMessage => proxy.$modal.msgWarning(warningMessage)
  )
}

// 提交按钮操作，支持单条与多选批量提交。
function handleSubmit(row) {
  if (!row?.inboundId && ids.value.length > 1) {
    proxy.$modal.confirm(`是否确认批量提交选中的 ${ids.value.length} 笔入库单？`).then(() => {
      return batchSubmitInbound(ids.value)
    }).then(res => {
      proxy.$modal.msgSuccess(res.msg || "批量提交完成")
      getList()
    }).catch(() => {})
    return
  }
  const targetInboundRow = getTargetInboundRow(row, "提交")
  if (!targetInboundRow) {
    return
  }
  proxy.$modal.confirm('是否确认提交入库单号为"' + targetInboundRow.inboundNo + '"的数据项？').then(function () {
    return submitInbound(targetInboundRow.inboundId)
  }).then(() => {
    proxy.$modal.msgSuccess("提交成功")
    getList()
  }).catch(() => {})
}

// 审核按钮操作，支持单条与多选批量审核。
function handleAudit(row) {
  if (!row?.inboundId && ids.value.length > 1) {
    proxy.$modal.confirm(`是否确认批量审核选中的 ${ids.value.length} 笔入库单？审核后将自动增加库存并更新移动加权平均成本。`).then(() => {
      return batchAuditInbound(ids.value)
    }).then(res => {
      proxy.$modal.msgSuccess(res.msg || "批量审核完成")
      getList()
    }).catch(() => {})
    return
  }
  const targetInboundRow = getTargetInboundRow(row, "审核")
  if (!targetInboundRow) {
    return
  }
  proxy.$modal.confirm('是否确认审核入库单号为"' + targetInboundRow.inboundNo + '"的数据项？审核后将自动增加库存并按移动加权平均法更新商品成本价。').then(function () {
    return auditInbound(targetInboundRow.inboundId)
  }).then(() => {
    proxy.$modal.msgSuccess("审核成功，库存与加权平均成本已更新")
    getList()
  }).catch(() => {})
}

function handleReject(row) {
  proxy.$modal.prompt(`确认驳回入库单"${row.inboundNo}"？请输入驳回原因：`).then(({ value }) => {
    return rejectInbound(row.inboundId, value || "实收数量或批次效期需复核")
  }).then(() => {
    proxy.$modal.msgSuccess("入库单已驳回至草稿")
    getList()
  }).catch(() => {})
}

async function handlePrintInbound(row) {
  const res = await listInboundItem({ pageNum: 1, pageSize: 1000, inboundId: row.inboundId })
  const items = res.rows || []
  await ensureInboundItemReferenceOptionsLoaded(items)
  printDialogRef.value?.openPrint({
    docType: "inbound",
    title: "采购入库验收单",
    docNo: row.inboundNo,
    metaFields: [
      { label: "入库单号", value: row.inboundNo },
      { label: "来源采购单", value: getPurchaseOrderDisplayName(row.purchaseOrderNo, row.purchaseOrderId) },
      { label: "供应商", value: row.supplierName || getSupplierName(row.supplierId, row.purchaseOrderId) },
      { label: "收货仓库", value: row.warehouseName || getWarehouseName(row.warehouseId) },
      { label: "单据状态", value: getInboundStatusLabel(row.status) },
      { label: "收货时间", value: parseTime(row.createTime, "{y}-{m}-{d}") }
    ],
    columns: [
      { label: "商品名称", prop: "productName" },
      { label: "库位", prop: "locationName", width: "120px" },
      { label: "批次号", prop: "batchNo", width: "110px" },
      { label: "效期", prop: "expireDate", width: "100px", align: "center" },
      { label: "实收数", prop: "quantity", width: "85px", align: "right" },
      { label: "单价", prop: "price", width: "95px", align: "right", type: "money", isPrice: true },
      { label: "金额", prop: "amount", width: "105px", align: "right", type: "money", isPrice: true }
    ],
    items: items.map(it => ({
      productName: getProductName(it.productId),
      locationName: getLocationName(it.locationId),
      batchNo: it.batchNo || "-",
      expireDate: it.expireDate ? parseTime(it.expireDate, "{y}-{m}-{d}") : "-",
      quantity: it.quantity,
      price: it.price,
      amount: it.amount
    })),
    totalQty: row.totalQty,
    totalAmount: row.totalAmount,
    remark: row.remark,
    signatures: ["收货验收人", "仓库管理员", "送货人签字"]
  })
}

// 作废按钮操作，补充空选择保护，避免未来工具栏入口场景下出现静默无响应。
function handleCancel(row) {
  const targetInboundRow = getTargetInboundRow(row, "作废")
  if (!targetInboundRow) {
    return
  }
  proxy.$modal.confirm('是否确认作废入库单号为"' + targetInboundRow.inboundNo + '"的数据项？').then(function () {
    return cancelInbound(targetInboundRow.inboundId)
  }).then(() => {
    proxy.$modal.msgSuccess("作废成功")
    getList()
  }).catch(() => {})
}

function handleExport() {
  proxy.download("business/inbound/export", {
    ...queryParams.value
  }, `inbound_${new Date().getTime()}.xlsx`)
}

// 判断当前是否来自一键新建入库入口，支持首页入口和采购订单联动入口。
function shouldAutoOpenInboundFromRoute() {
  return route.query.mode === "create"
}

// 初始化页面基础数据和列表，并在需要时自动打开新建入库弹窗。
async function initializePage() {
  initializeQueryParamsFromRoute()
  await Promise.all([
    Promise.resolve(initBasicData()),
    Promise.resolve(getList())
  ])
  if (shouldAutoOpenInboundFromRoute()) {
    handleAdd()
  }
}

// 监听路由参数变化，保证从采购订单重复跳转到当前页面时列表和自动新建逻辑都会重新生效。
watch(() => route.fullPath, (currentRouteFullPath, previousRouteFullPath) => {
  if (currentRouteFullPath === previousRouteFullPath) {
    return
  }
  initializePage()
})

initializePage()
</script>
