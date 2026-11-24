<template>
  <div class="app-container erp-page">
    <erp-page-header
      eyebrow="销售管理 · 销售出库"
      title="销售出库管理"
      subtitle="管理拣货核验、条码扫描、物流配送与标准送货单打印，审核后扣减实物库存并释放预占。"
      icon="Upload"
      tone="blue"
      :badge="`共 ${total} 笔出库单`"
    >
      <template #actions>
        <el-button icon="Download" plain @click="handleExport" v-hasPermi="['business:outbound:export']">导出</el-button>
        <el-button type="primary" icon="Plus" @click="handleAdd" v-hasPermi="['business:outbound:add']">新建出库单</el-button>
      </template>
    </erp-page-header>

    <erp-stat-strip :items="outboundStatItems" :columns="4" @select="handleStatSelect" />

    <section class="erp-filter-card" v-show="showSearch">
      <el-form :model="queryParams" ref="queryRef" :inline="true">
        <el-form-item label="出库单号" prop="outboundNo">
          <el-input v-model="queryParams.outboundNo" placeholder="请输入出库单号" clearable style="width: 170px" @keyup.enter="handleQuery" />
        </el-form-item>
        <el-form-item label="来源单号" prop="sourceOrderNo">
          <el-input v-model="queryParams.sourceOrderNo" placeholder="请输入销售单号" clearable style="width: 170px" @keyup.enter="handleQuery" />
        </el-form-item>
        <el-form-item label="客户" prop="customerId">
          <el-select v-model="queryParams.customerId" placeholder="请选择客户" clearable filterable remote reserve-keyword remote-show-suffix :remote-method="loadCustomerOptionList" :loading="customerListLoading" style="width: 175px">
            <el-option v-for="customer in customerSelectOptionList" :key="customer.customerId" :label="customer.customerName" :value="customer.customerId" />
          </el-select>
        </el-form-item>
        <el-form-item label="仓库" prop="warehouseId">
          <el-select v-model="queryParams.warehouseId" placeholder="请选择仓库" clearable filterable remote reserve-keyword remote-show-suffix :remote-method="loadWarehouseOptionList" :loading="warehouseListLoading" style="width: 165px">
            <el-option v-for="warehouse in warehouseSelectOptionList" :key="warehouse.warehouseId" :label="warehouse.warehouseName" :value="warehouse.warehouseId" />
          </el-select>
        </el-form-item>
        <el-form-item label="出库日期">
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
          <erp-status-tabs :model-value="queryParams.status ?? ''" :items="outboundStatusTabItems" @change="handleQuickStatus" />
        </div>
        <div class="erp-toolbar-right">
          <el-button type="success" plain icon="Edit" :disabled="!canUpdateSelected" @click="handleUpdate" v-hasPermi="['business:outbound:edit']">修改</el-button>
          <el-button type="info" plain icon="Upload" :disabled="!canSubmitSelected" @click="handleSubmit()" v-hasPermi="['business:outbound:edit']">提交</el-button>
          <el-button type="warning" plain icon="CircleCheck" :disabled="!canAuditSelected" @click="handleAudit()" v-hasPermi="['business:outbound:audit']">审核</el-button>
          <el-button type="danger" plain icon="Delete" :disabled="!canDeleteSelected" @click="handleDelete()" v-hasPermi="['business:outbound:remove']">删除</el-button>
          <right-toolbar v-model:showSearch="showSearch" @queryTable="getList" />
        </div>
      </div>

      <el-table v-loading="loading" :data="outboundList" @selection-change="handleSelectionChange">
        <el-table-column type="selection" width="46" align="center" />
        <el-table-column label="出库单号" align="center" prop="outboundNo" min-width="150" show-overflow-tooltip>
          <template #default="scope">
            <span class="erp-code-chip">{{ scope.row.outboundNo }}</span>
          </template>
        </el-table-column>
        <el-table-column label="类型 / 来源单" align="left" min-width="145" show-overflow-tooltip>
          <template #default="scope">
            <div class="erp-cell-title">{{ getOutboundTypeLabel(scope.row.outboundType) }}</div>
            <div class="erp-cell-sub">{{ getSaleOrderDisplayName(scope.row.saleOrderNo, scope.row.saleOrderId) || '直接出库' }}</div>
          </template>
        </el-table-column>
        <el-table-column label="客户 / 发货仓" align="left" min-width="165" show-overflow-tooltip>
          <template #default="scope">
            <div class="erp-cell-title">{{ scope.row.customerName || getCustomerName(scope.row.customerId) }}</div>
            <div class="erp-cell-sub">仓库：{{ scope.row.warehouseName || getWarehouseName(scope.row.warehouseId) }}</div>
          </template>
        </el-table-column>
        <el-table-column label="数量" align="right" prop="totalQty" width="72" />
        <el-table-column label="出库总额" align="right" prop="totalAmount" width="104">
          <template #default="scope">
            <span class="erp-amount is-primary">¥{{ Number(scope.row.totalAmount || 0).toFixed(2) }}</span>
          </template>
        </el-table-column>
        <el-table-column label="单据状态" align="center" prop="status" width="94">
          <template #default="scope">
            <el-tooltip v-if="scope.row.status === 'draft' && scope.row.rejectReason" :content="`驳回原因：${scope.row.rejectReason}`" placement="top">
              <el-tag type="danger" effect="light">已驳回待改</el-tag>
            </el-tooltip>
            <el-tag v-else :type="getOutboundStatusType(scope.row.status)" effect="light">{{ getOutboundStatusLabel(scope.row.status) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="配送状态" align="center" prop="deliveryStatus" width="88">
          <template #default="scope">
            <el-tag :type="getOutboundDeliveryStatusType(scope.row.deliveryStatus)" effect="plain">{{ getOutboundDeliveryStatusLabel(scope.row.deliveryStatus) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="创建时间" align="center" prop="createTime" width="136">
          <template #default="scope">
            <span>{{ parseTime(scope.row.createTime, '{y}-{m}-{d} {h}:{i}') }}</span>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="215" fixed="right" align="center">
          <template #default="scope">
            <div class="erp-row-actions">
              <el-button link type="primary" icon="List" @click="handleOpenOutboundItem(scope.row)" v-hasPermi="['business:outbound:query']">明细</el-button>
              <el-button link type="primary" icon="Printer" @click="handlePrintOutbound(scope.row)">打印</el-button>
              <el-button v-if="scope.row.status === 'submitted'" link type="success" icon="CircleCheck" @click="handleAudit(scope.row)" v-hasPermi="['business:outbound:audit']">审核</el-button>
              <el-button v-else-if="scope.row.status === 'draft'" link type="primary" icon="Upload" @click="handleSubmit(scope.row)" v-hasPermi="['business:outbound:edit']">提交</el-button>
              <el-button
                v-else-if="scope.row.status === 'audited' && (!scope.row.deliveryStatus || scope.row.deliveryStatus === 'pending')"
                link type="primary" icon="Van"
                @click="handleOpenDeliveryDialog(scope.row, 'shipping')"
                v-hasPermi="['business:outbound:edit', 'business:outbound:audit']"
              >配送</el-button>
              <el-button
                v-else-if="scope.row.status === 'audited' && scope.row.deliveryStatus === 'shipping'"
                link type="success" icon="Select"
                @click="handleOpenDeliveryDialog(scope.row, 'delivered')"
                v-hasPermi="['business:outbound:edit', 'business:outbound:audit']"
              >签收</el-button>
              <el-dropdown v-if="scope.row.saleOrderId || scope.row.status !== 'audited'" trigger="click">
                <el-button link type="primary">更多<el-icon class="el-icon--right"><ArrowDown /></el-icon></el-button>
                <template #dropdown>
                  <el-dropdown-menu>
                    <el-dropdown-item v-if="scope.row.saleOrderId" v-hasPermi="['business:saleOrder:list']" @click="handleViewSaleOrder(scope.row)">查看销售单</el-dropdown-item>
                    <el-dropdown-item v-if="scope.row.status === 'draft'" v-hasPermi="['business:outbound:edit']" @click="handleUpdate(scope.row)">修改主单</el-dropdown-item>
                    <el-dropdown-item v-if="scope.row.status === 'submitted'" v-hasPermi="['business:outbound:audit']" @click="handleReject(scope.row)">审核驳回</el-dropdown-item>
                    <el-dropdown-item v-if="scope.row.status !== 'audited' && scope.row.status !== 'cancelled'" v-hasPermi="['business:outbound:edit']" @click="handleCancel(scope.row)">作废单据</el-dropdown-item>
                    <el-dropdown-item v-if="scope.row.status === 'draft'" v-hasPermi="['business:outbound:remove']" divided @click="handleDelete(scope.row)"><span style="color: var(--el-color-danger)">删除草稿</span></el-dropdown-item>
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
      <el-form ref="outboundRef" :model="form" :rules="rules" label-width="110px">
        <el-row>
          <el-col :span="12">
            <el-form-item label="出库类型" prop="outboundType">
              <el-select v-model="form.outboundType" placeholder="请选择出库类型" style="width: 100%" :disabled="!isOutboundFormEditable() || hasLinkedSaleOrder()">
                <el-option
                  v-for="outboundTypeOption in outboundTypeOptionList"
                  :key="outboundTypeOption.value"
                  :label="outboundTypeOption.label"
                  :value="outboundTypeOption.value"
                />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="来源销售单" prop="saleOrderId">
              <el-select
                v-if="!form.outboundId"
                v-model="form.saleOrderId"
                placeholder="可选：搜索已审核销售单自动带入发货明细"
                clearable
                filterable
                remote
                reserve-keyword
                remote-show-suffix
                :remote-method="loadAvailableSaleOrders"
                :loading="soSelectLoading"
                style="width: 100%"
                @change="handleSaleOrderSelectChange"
              >
                <el-option
                  v-for="so in availableSaleOrderList"
                  :key="so.saleOrderId"
                  :label="`${so.orderNo} | ${so.customerName || ''} (待出 ${so.remainQuantity ?? so.totalQty ?? 0})`"
                  :value="so.saleOrderId"
                />
              </el-select>
              <el-input v-else v-model="form.saleOrderNo" placeholder="未关联销售单" disabled />
            </el-form-item>
          </el-col>
        </el-row>
        <el-row>
          <el-col :span="12">
            <el-form-item label="客户" prop="customerId">
              <el-select v-model="form.customerId" placeholder="请选择客户" clearable filterable remote reserve-keyword remote-show-suffix :remote-method="loadCustomerOptionList" :loading="customerListLoading" style="width: 100%" :disabled="!isOutboundFormEditable() || hasLinkedSaleOrder()">
                <el-option
                  v-for="customer in customerSelectOptionList"
                  :key="customer.customerId"
                  :label="customer.customerName"
                  :value="customer.customerId"
                />
              </el-select>
            </el-form-item>
          </el-col>
        </el-row>
        <el-row>
          <el-col :span="12">
            <el-form-item label="仓库" prop="warehouseId">
              <el-select v-model="form.warehouseId" placeholder="请选择仓库" clearable filterable remote reserve-keyword remote-show-suffix :remote-method="loadWarehouseOptionList" :loading="warehouseListLoading" style="width: 100%" :disabled="!isOutboundFormEditable() || hasLinkedSaleOrder()">
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
                  v-for="outboundStatusOption in outboundStatusOptionList"
                  :key="outboundStatusOption.value"
                  :label="outboundStatusOption.label"
                  :value="outboundStatusOption.value"
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
            <el-form-item label="来源单号" prop="sourceOrderNo">
              <el-input v-model="form.sourceOrderNo" placeholder="请输入来源单号" :disabled="!isOutboundFormEditable() || hasLinkedSaleOrder()" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="承运商" prop="carrier">
              <el-input v-model="form.carrier" placeholder="配送中或已配送时请输入承运商" :disabled="!canEditOutboundLogisticsInfo()" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-row>
          <el-col :span="12">
            <el-form-item label="运单号" prop="waybillNo">
              <el-input v-model="form.waybillNo" placeholder="配送中或已配送时请输入运单号" :disabled="!canEditOutboundLogisticsInfo()" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="运费" prop="freightCost">
              <el-input-number v-model="form.freightCost" :min="0" :precision="2" controls-position="right" style="width: 100%" :disabled="!canEditOutboundLogisticsInfo()" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-row>
          <el-col :span="12">
            <el-form-item label="配送状态" prop="deliveryStatus">
              <el-select v-model="form.deliveryStatus" placeholder="请选择配送状态" style="width: 100%" :disabled="!isOutboundFormEditable()" @change="handleOutboundDeliveryStatusChange">
                <el-option
                  v-for="deliveryStatusOption in deliveryStatusOptionList"
                  :key="deliveryStatusOption.value"
                  :label="deliveryStatusOption.label"
                  :value="deliveryStatusOption.value"
                />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="备注" prop="remark">
              <el-input v-model="form.remark" placeholder="请输入备注" :disabled="!isOutboundFormEditable()" />
            </el-form-item>
          </el-col>
        </el-row>
      </el-form>
      <template #footer>
        <div class="dialog-footer">
          <el-button type="primary" @click="handleOutboundDialogPrimaryAction">{{ isOutboundFormEditable() ? "确 定" : "关 闭" }}</el-button>
          <el-button v-if="isOutboundFormEditable()" @click="cancel">取 消</el-button>
        </div>
      </template>
    </el-dialog>

    <el-dialog :title="outboundItemTitle" v-model="outboundItemOpen" width="980px" append-to-body>
      <el-alert
        v-if="!isOutboundItemEditable() || isLinkedSaleOrderOutboundItemMode()"
        :title="!isOutboundItemEditable() ? '当前销售出库单已进入后续流程，明细仅支持查看。' : '来源销售单明细已自动带入，当前只需维护配送相关主单信息。'"
        type="info"
        :closable="false"
        class="mb8"
      />
      <el-row :gutter="10" class="mb8">
        <el-col :span="1.5">
          <el-button
            v-if="isOutboundItemEditable() && !isLinkedSaleOrderOutboundItemMode()"
            type="primary"
            plain
            icon="Plus"
            @click="handleAddOutboundItem"
            v-hasPermi="['business:outbound:add']"
          >新增明细</el-button>
        </el-col>
        <el-col :span="1.5">
          <el-button
            v-if="isOutboundItemEditable() && !isLinkedSaleOrderOutboundItemMode()"
            type="danger"
            plain
            icon="Delete"
            :disabled="outboundItemMultiple"
            @click="handleDeleteOutboundItem"
            v-hasPermi="['business:outbound:remove']"
          >删除明细</el-button>
        </el-col>
        <el-col :span="1.5">
          <el-button plain icon="Download" @click="handleExportOutboundItems" v-hasPermi="['business:outbound:export']">导出明细</el-button>
        </el-col>
        <el-col :span="10" v-if="isOutboundItemEditable()" v-hasPermi="['business:outbound:add', 'business:outbound:edit']">
          <el-input
            v-model="outboundScanCode"
            placeholder="扫码出库：扫描/输入条码或商品编码按回车（自动匹配+1）"
            clearable
            style="width: 100%"
            @keyup.enter="handleOutboundBarcodeScan"
          >
            <template #append>
              <el-button icon="FullScreen" :loading="outboundScanLoading" @click="handleOutboundBarcodeScan">扫码录入</el-button>
            </template>
          </el-input>
        </el-col>
      </el-row>
      <el-table v-loading="outboundItemLoading" :data="outboundItemList" @selection-change="handleOutboundItemSelectionChange">
        <el-table-column v-if="isOutboundItemEditable() && !isLinkedSaleOrderOutboundItemMode()" type="selection" width="55" align="center" />
        <el-table-column label="明细编号" align="center" prop="outboundItemId" />
        <el-table-column label="商品" align="center" prop="productId" min-width="180">
          <template #default="scope">
            <span>{{ getProductName(scope.row.productId) }}</span>
          </template>
        </el-table-column>
        <el-table-column label="销售数量" align="center" prop="saleQuantity" width="110">
          <template #default="scope">
            <span>{{ formatOutboundItemQuantity(scope.row.saleQuantity) }}</span>
          </template>
        </el-table-column>
        <el-table-column label="已出库" align="center" prop="shippedQuantity" width="110">
          <template #default="scope">
            <span>{{ formatOutboundItemQuantity(scope.row.shippedQuantity) }}</span>
          </template>
        </el-table-column>
        <el-table-column label="待出库" align="center" prop="remainQuantity" width="110">
          <template #default="scope">
            <span>{{ formatOutboundItemQuantity(scope.row.remainQuantity) }}</span>
          </template>
        </el-table-column>
        <el-table-column label="库位" align="center" prop="locationId" min-width="180">
          <template #default="scope">
            <span>{{ getLocationName(scope.row.locationId) }}</span>
          </template>
        </el-table-column>
        <el-table-column label="批次号" align="center" prop="batchNo" />
        <el-table-column label="数量" align="center" prop="quantity" />
        <el-table-column label="单价" align="center" prop="price" />
        <el-table-column label="金额" align="center" prop="amount" />
        <el-table-column v-if="isOutboundItemEditable()" label="操作" width="180" align="center">
          <template #default="scope">
            <el-button v-if="isOutboundItemEditable()" link type="primary" icon="Edit" @click="handleUpdateOutboundItem(scope.row)" v-hasPermi="['business:outbound:edit']">修改</el-button>
            <el-button v-if="isOutboundItemEditable() && !isLinkedSaleOrderOutboundItemMode()" link type="primary" icon="Delete" @click="handleDeleteOutboundItem(scope.row)" v-hasPermi="['business:outbound:remove']">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-dialog>

    <el-dialog :title="outboundItemFormTitle" v-model="outboundItemFormOpen" width="760px" append-to-body>
      <el-form ref="outboundItemRef" :model="outboundItemForm" :rules="outboundItemRules" label-width="110px">
        <el-alert
          v-if="outboundItemForm.saleOrderItemId"
          :title="getOutboundItemSourceProgressText(outboundItemForm)"
          type="info"
          :closable="false"
          class="mb8"
        />
        <el-row>
          <el-col :span="12">
            <el-form-item label="商品" prop="productId">
              <el-select v-model="outboundItemForm.productId" placeholder="请选择商品" clearable filterable remote reserve-keyword remote-show-suffix :remote-method="loadProductOptionList" :loading="productListLoading" style="width: 100%" :disabled="!!outboundItemForm.saleOrderItemId" @change="handleOutboundProductSelect">
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
              <el-select v-model="outboundItemForm.locationId" placeholder="请选择库位" clearable filterable remote reserve-keyword remote-show-suffix :remote-method="loadLocationOptionList" :loading="locationListLoading" style="width: 100%" :disabled="!!outboundItemForm.saleOrderItemId">
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
              <el-input v-model="outboundItemForm.batchNo" placeholder="请输入批次号" :disabled="!!outboundItemForm.saleOrderItemId" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="数量" prop="quantity">
              <el-input-number v-model="outboundItemForm.quantity" :min="0.01" :precision="2" controls-position="right" style="width: 100%" @change="syncOutboundItemAmount" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-row>
          <el-col :span="12">
            <el-form-item label="单价" prop="price">
              <el-input-number v-model="outboundItemForm.price" :min="0" :precision="2" controls-position="right" style="width: 100%" :disabled="!!outboundItemForm.saleOrderItemId" @change="syncOutboundItemAmount" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="金额" prop="amount">
              <el-input-number v-model="outboundItemForm.amount" :min="0" :precision="2" controls-position="right" style="width: 100%" disabled />
            </el-form-item>
          </el-col>
        </el-row>
      </el-form>
      <template #footer>
        <div class="dialog-footer">
          <el-button type="primary" @click="submitOutboundItemForm">确 定</el-button>
          <el-button @click="cancelOutboundItemForm">取 消</el-button>
        </div>
      </template>
    </el-dialog>

    <document-print-dialog ref="printDialogRef" />

    <!-- 出库配送物流状态流转弹窗 -->
    <el-dialog v-model="deliveryDialog.open" :title="deliveryDialog.deliveryStatus === 'delivered' ? '确认客户签收' : '开始发货配送'" width="480px" append-to-body>
      <el-form :model="deliveryDialog" label-width="95px">
        <el-form-item label="出库单号">
          <span class="erp-code-chip">{{ deliveryDialog.outboundNo }}</span>
        </el-form-item>
        <el-form-item label="目标状态">
          <el-tag :type="deliveryDialog.deliveryStatus === 'delivered' ? 'success' : 'primary'">
            {{ deliveryDialog.deliveryStatus === 'delivered' ? '已签收送达' : '配送运输中' }}
          </el-tag>
        </el-form-item>
        <el-form-item label="承运商" :required="deliveryDialog.deliveryStatus === 'shipping'">
          <el-input v-model="deliveryDialog.carrier" placeholder="如：顺丰速运、德邦物流、自有车队" />
        </el-form-item>
        <el-form-item label="运单号">
          <el-input v-model="deliveryDialog.waybillNo" placeholder="请输入快递/物流运单号" />
        </el-form-item>
        <el-form-item label="运费(元)">
          <el-input-number v-model="deliveryDialog.freightCost" :min="0" :precision="2" controls-position="right" style="width: 100%" />
        </el-form-item>
      </el-form>
      <template #footer>
        <div class="dialog-footer">
          <el-button type="primary" :loading="deliveryDialog.submitting" @click="submitDeliveryForm">确 定</el-button>
          <el-button @click="deliveryDialog.open = false">取 消</el-button>
        </div>
      </template>
    </el-dialog>
  </div>
</template>

<script setup name="Outbound">
import DocumentPrintDialog from "@/components/DocumentPrintDialog/index.vue"
import { listOutbound, getOutbound, addOutbound, updateOutbound, delOutbound, submitOutbound, auditOutbound, batchSubmitOutbound, batchAuditOutbound, rejectOutbound, updateOutboundDelivery, cancelOutbound } from "@/api/business/outbound"
import { listOutboundItem, getOutboundItem, addOutboundItem, updateOutboundItem, delOutboundItem } from "@/api/business/outboundItem"
import { recommendStockBatch } from "@/api/business/stock"
import { listSaleOrder, getSaleOrder } from "@/api/business/saleOrder"
import { listCustomer, getCustomer } from "@/api/business/customer"
import { listWarehouse, getWarehouse } from "@/api/business/warehouse"
import { listProduct, getProduct, scanProductByCode } from "@/api/business/product"
import { listLocation, getLocation } from "@/api/business/location"
import { appendUniqueSelectOption, buildSelectOptionList, normalizeRemoteKeyword } from "@/utils/remoteSelect"
import { buildDocumentSelectionState, resolveDocumentActionRow } from "@/utils/documentPage"
import { ERP_DATE_SHORTCUTS } from "@/utils/erpFormat"
import { parseTime } from "@/utils/ruoyi"

const { proxy } = getCurrentInstance()
const route = useRoute()

const outboundList = ref([])
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
const availableSaleOrderList = ref([])
const soSelectLoading = ref(false)
const total = ref(0)
const title = ref("")
const outboundItemOpen = ref(false)
const outboundItemLoading = ref(false)
const outboundItemTitle = ref("")
const outboundItemList = ref([])
const currentOutboundId = ref(undefined)
const currentOutboundStatus = ref(undefined)
const currentOutboundSaleOrderId = ref(undefined)
const currentOutboundWarehouseId = ref(undefined)
const printDialogRef = ref(null)
const deliveryDialog = reactive({
  open: false,
  submitting: false,
  outboundId: undefined,
  outboundNo: "",
  deliveryStatus: "shipping",
  carrier: "",
  waybillNo: "",
  freightCost: 0
})
const selectedOutboundItemIds = ref([])
const outboundItemMultiple = ref(true)
const outboundItemFormOpen = ref(false)
const outboundItemFormTitle = ref("")
const outboundScanCode = ref("")
const outboundScanLoading = ref(false)
const customerList = ref([])
const warehouseList = ref([])
const productList = ref([])
const locationList = ref([])
const customerListLoading = ref(false)
const warehouseListLoading = ref(false)
const productListLoading = ref(false)
const locationListLoading = ref(false)

const outboundTypeOptionList = [
  { label: "销售出库", value: "sale" }
]

const outboundStatusOptionList = [
  { label: "草稿", value: "draft", type: "info" },
  { label: "已提交", value: "submitted", type: "warning" },
  { label: "已审核", value: "audited", type: "success" },
  { label: "已作废", value: "cancelled", type: "danger" }
]

const deliveryStatusOptionList = [
  { label: "待配送", value: "pending", type: "info" },
  { label: "配送中", value: "shipping", type: "warning" },
  { label: "已签收", value: "delivered", type: "success" }
]

// 校验承运商字段，要求配送中或已配送阶段必须维护承运信息。
function validateOutboundCarrier(rule, value, callback) {
  if (shouldMaintainOutboundLogisticsInfo() && (!value || value.trim() === "")) {
    callback(new Error("配送中或已配送时承运商不能为空"))
    return
  }
  callback()
}

// 校验运单号字段，要求配送中或已配送阶段必须维护运单号。
function validateOutboundWaybillNo(rule, value, callback) {
  if (shouldMaintainOutboundLogisticsInfo() && (!value || value.trim() === "")) {
    callback(new Error("配送中或已配送时运单号不能为空"))
    return
  }
  callback()
}

const data = reactive({
  form: {},
  outboundItemForm: {},
  queryParams: {
    pageNum: 1,
    pageSize: 10,
    saleOrderId: undefined,
    sourceOrderNo: undefined,
    outboundNo: undefined,
    outboundType: undefined,
    customerId: undefined,
    warehouseId: undefined,
    status: undefined
  },
  rules: {
    outboundType: [{ required: true, message: "出库类型不能为空", trigger: "change" }],
    customerId: [{ required: true, message: "客户不能为空", trigger: "change" }],
    warehouseId: [{ required: true, message: "仓库不能为空", trigger: "change" }],
    deliveryStatus: [{ required: true, message: "配送状态不能为空", trigger: "change" }],
    carrier: [{ validator: validateOutboundCarrier, trigger: "blur" }],
    waybillNo: [{ validator: validateOutboundWaybillNo, trigger: "blur" }]
  },
  outboundItemRules: {
    productId: [{ required: true, message: "商品不能为空", trigger: "change" }],
    locationId: [{ required: true, message: "库位不能为空", trigger: "change" }],
    batchNo: [{ required: true, message: "批次号不能为空", trigger: "blur" }],
    quantity: [
      { required: true, message: "出库数量不能为空", trigger: "change" },
      { validator: (_rule, val, cb) => Number(val) > 0 ? cb() : cb(new Error("出库数量必须大于0")), trigger: "change" }
    ]
  }
})

const { queryParams, form, rules, outboundItemForm, outboundItemRules } = toRefs(data)

const outboundStatusTabItems = [
  { label: "全部", value: "" },
  { label: "草稿", value: "draft", dot: "slate" },
  { label: "待审核", value: "submitted", dot: "amber" },
  { label: "已审核", value: "audited", dot: "emerald" },
  { label: "已作废", value: "cancelled", dot: "rose" }
]

const outboundStatItems = computed(() => {
  const rows = outboundList.value || []
  const submittedCount = rows.filter(r => r.status === "submitted").length
  const shippingCount = rows.filter(r => r.deliveryStatus === "shipping" || r.deliveryStatus === "shipped").length
  const pageAmount = rows.reduce((s, r) => s + Number(r.totalAmount || 0), 0)
  const curStatus = queryParams.value.status ?? ""
  return [
    { key: "all", statusVal: "", label: "出库单总数", value: total.value, suffix: "单", hint: "点击查看全部出库单", icon: "Upload", tone: "blue", clickable: true, active: curStatus === "" },
    { key: "submitted", statusVal: "submitted", label: "本页待审核出库", value: submittedCount, suffix: "单", hint: "点击筛选待审核出库单", icon: "Stamp", tone: "amber", clickable: true, active: curStatus === "submitted" },
    { key: "audited", statusVal: "audited", label: "本页配送中", value: shippingCount, suffix: "单", hint: "点击筛选已审核出库单", icon: "Van", tone: "indigo", clickable: true, active: curStatus === "audited" },
    { key: "amt", label: "本页出库总额", prefix: "¥", value: pageAmount.toFixed(2), hint: "本页发货商品总金额", icon: "Money", tone: "emerald" }
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
const customerSelectOptionList = computed(() => buildCustomerSelectOptionList())
const warehouseSelectOptionList = computed(() => buildWarehouseSelectOptionList())
const productSelectOptionList = computed(() => buildProductSelectOptionList())
const locationSelectOptionList = computed(() => buildLocationSelectOptionList())

// 判断出库单是否还处于草稿状态，统一顶部按钮和行内按钮的可操作条件。
function isDraftOutboundStatus(statusValue) {
  return statusValue === "draft"
}

// 判断当前出库单表单是否允许继续编辑，避免非草稿单据被其他入口打开后仍可修改。
function isOutboundFormEditable() {
  return isDraftOutboundStatus(form.value.status)
}

// 判断当前出库单是否已经关联来源销售单，关联后客户、仓库和来源单号由系统同步维护。
function hasLinkedSaleOrder() {
  return !!form.value.saleOrderId
}

// 判断当前出库明细是否来自来源销售单自动带入，已关联时不再允许手工新增或删除整行。
function isLinkedSaleOrderOutboundItemMode() {
  return !!currentOutboundSaleOrderId.value
}

// 统一处理销售出库单弹窗主按钮行为，避免只读场景下仍然触发保存提示。
function handleOutboundDialogPrimaryAction() {
  if (!isOutboundFormEditable()) {
    cancel()
    return
  }
  submitForm()
}

// 判断当前配送阶段是否需要维护承运信息，用于承运商和运单号的联动校验。
function shouldMaintainOutboundLogisticsInfo() {
  return form.value.deliveryStatus === "shipping" || form.value.deliveryStatus === "shipped" || form.value.deliveryStatus === "delivered"
}

// 判断当前承运信息是否允许编辑，避免待配送阶段误填物流信息。
function canEditOutboundLogisticsInfo() {
  return isOutboundFormEditable() && shouldMaintainOutboundLogisticsInfo()
}

// 根据配送状态规范物流字段，避免待配送阶段残留旧物流信息影响用户判断。
function normalizeOutboundLogisticsInfoByDeliveryStatus() {
  if (shouldMaintainOutboundLogisticsInfo()) {
    return
  }
  form.value.carrier = undefined
  form.value.waybillNo = undefined
  form.value.freightCost = 0
}

// 处理配送状态切换后的物流字段联动，及时清理校验结果并同步字段值。
function handleOutboundDeliveryStatusChange() {
  normalizeOutboundLogisticsInfoByDeliveryStatus()
  if (proxy.$refs["outboundRef"]) {
    proxy.$refs["outboundRef"].clearValidate(["carrier", "waybillNo"])
    if (shouldMaintainOutboundLogisticsInfo()) {
      proxy.$nextTick(() => {
        proxy.$refs["outboundRef"].validateField(["carrier", "waybillNo"])
      })
    }
  }
}

// 将路由中的编号参数转换为数值，避免筛选条件和表单回填时出现字符串编号。
function convertRouteNumberValue(routeValue) {
  if (routeValue === undefined || routeValue === null || routeValue === "") {
    return undefined
  }
  const numberValue = Number(routeValue)
  return Number.isNaN(numberValue) ? undefined : numberValue
}

// 根据首页传入的参数初始化出库单筛选条件。
function initializeQueryParamsFromRoute() {
  queryParams.value.saleOrderId = convertRouteNumberValue(route.query.saleOrderId)
  queryParams.value.status = route.query.status || undefined
}

// 初始化出库单依赖的远程下拉数据，避免页面初始阶段一次性加载过多基础资料。
function initBasicData() {
  return Promise.all([
    loadCustomerOptionList(),
    loadWarehouseOptionList(),
    loadProductOptionList(),
    loadLocationOptionList()
  ])
}

// 将来源销售单带入当前出库单表单，减少仓库与物流人员重复录入客户、仓库和来源单号。
function applyRouteSaleOrderToForm() {
  if (!queryParams.value.saleOrderId) {
    return Promise.resolve()
  }
  form.value.saleOrderId = queryParams.value.saleOrderId
  form.value.outboundType = "sale"
  return getSaleOrder(queryParams.value.saleOrderId).then(response => {
    const saleOrderData = response.data || {}
    form.value.saleOrderNo = saleOrderData.orderNo
    form.value.sourceOrderNo = saleOrderData.orderNo
    form.value.customerId = saleOrderData.customerId
    form.value.warehouseId = saleOrderData.warehouseId
    ensureCustomerOptionLoaded(form.value.customerId)
    ensureWarehouseOptionLoaded(form.value.warehouseId)
  })
}

// 远程加载客户下拉选项，按需补充客户名称数据，减少页面初始加载压力。
function loadCustomerOptionList(searchKeyword) {
  customerListLoading.value = true
  return listCustomer({
    pageNum: 1,
    pageSize: 20,
    status: "0",
    customerName: normalizeRemoteKeyword(searchKeyword)
  }).then(response => {
    const customerRowList = response.rows || []
    customerRowList.forEach(customerItem => {
      appendUniqueSelectOption(customerList.value, customerItem, "customerId")
    })
  }).finally(() => {
    customerListLoading.value = false
  })
}

// 远程加载仓库下拉选项，按需补充仓库名称数据，减少页面初始加载压力。
function loadWarehouseOptionList(searchKeyword) {
  warehouseListLoading.value = true
  return listWarehouse({
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
  return listProduct({
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

// 远程加载库位下拉选项，按当前出库单仓库限定范围，避免跨仓选库位。
function loadLocationOptionList(searchKeyword) {
  locationListLoading.value = true
  return listLocation({
    pageNum: 1,
    pageSize: 50,
    status: "0",
    warehouseId: currentOutboundWarehouseId.value || form.value.warehouseId || undefined,
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

// 按客户编号补充当前页面缺失的客户名称，保证列表、表单和筛选的名称回显稳定。
function ensureCustomerOptionLoaded(customerId) {
  if (customerId === undefined || customerId === null || customerId === "") {
    return Promise.resolve()
  }
  if (customerList.value.some(customerItem => customerItem.customerId === customerId)) {
    return Promise.resolve()
  }
  return getCustomer(customerId).then(response => {
    appendUniqueSelectOption(customerList.value, response.data, "customerId")
  }).catch(() => {
    appendUniqueSelectOption(customerList.value, {
      customerId: customerId,
      customerName: "客户资料缺失"
    }, "customerId")
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

// 按商品编号补充当前页面缺失的商品名称，保证出库明细远程下拉场景下的名称回显稳定。
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

// 按库位编号补充当前页面缺失的库位名称，保证出库明细远程下拉场景下的名称回显稳定。
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

// 批量补充出库主单依赖的客户与仓库名称，优先复用后端关联字段，消除冗余 N+1 查询。
function ensureOutboundReferenceOptionsLoaded(outboundRowList) {
  const customerIdList = []
  const warehouseIdList = []
  ;(outboundRowList || []).forEach(outboundRow => {
    if (outboundRow.customerId != null && outboundRow.customerId !== "") {
      if (outboundRow.customerName) {
        appendUniqueSelectOption(customerList.value, { customerId: outboundRow.customerId, customerName: outboundRow.customerName }, "customerId")
      } else {
        customerIdList.push(outboundRow.customerId)
      }
    }
    if (outboundRow.warehouseId != null && outboundRow.warehouseId !== "") {
      if (outboundRow.warehouseName) {
        appendUniqueSelectOption(warehouseList.value, { warehouseId: outboundRow.warehouseId, warehouseName: outboundRow.warehouseName }, "warehouseId")
      } else {
        warehouseIdList.push(outboundRow.warehouseId)
      }
    }
  })
  Promise.all([
    ...[...new Set(customerIdList)].map(customerId => ensureCustomerOptionLoaded(customerId)),
    ...[...new Set(warehouseIdList)].map(warehouseId => ensureWarehouseOptionLoaded(warehouseId))
  ]).catch(() => {})
}

// 批量补充出库明细依赖的商品与库位名称，保证远程下拉模式下明细展示仍然可读。
function ensureOutboundItemReferenceOptionsLoaded(outboundItemRowList) {
  const productIdList = [...new Set((outboundItemRowList || []).map(outboundItemRow => outboundItemRow.productId).filter(productId => productId !== undefined && productId !== null && productId !== ""))]
  const locationIdList = [...new Set((outboundItemRowList || []).map(outboundItemRow => outboundItemRow.locationId).filter(locationId => locationId !== undefined && locationId !== null && locationId !== ""))]
  Promise.all([
    ...productIdList.map(productId => ensureProductOptionLoaded(productId)),
    ...locationIdList.map(locationId => ensureLocationOptionLoaded(locationId))
  ]).catch(() => {})
}

// 组合客户下拉选项，兼容历史出库单中客户主数据缺失时的兜底回显。
function buildCustomerSelectOptionList() {
  return buildSelectOptionList(customerList.value, [queryParams.value.customerId, form.value.customerId], "customerId", "customerName", () => "客户资料缺失")
}

// 组合仓库下拉选项，兼容历史出库单中主数据缺失时的兜底回显。
function buildWarehouseSelectOptionList() {
  return buildSelectOptionList(warehouseList.value, [queryParams.value.warehouseId, form.value.warehouseId], "warehouseId", "warehouseName", () => "仓库资料缺失")
}

// 组合商品下拉选项，兼容历史出库明细中主数据缺失时的兜底回显。
function buildProductSelectOptionList() {
  return buildSelectOptionList(productList.value, [outboundItemForm.value.productId], "productId", "productName", () => "商品资料缺失")
}

// 组合库位下拉选项，兼容历史出库明细中主数据缺失时的兜底回显。
function buildLocationSelectOptionList() {
  const targetWarehouseId = currentOutboundWarehouseId.value || form.value.warehouseId
  const filtered = targetWarehouseId
    ? locationList.value.filter(loc => (!loc.warehouseId || Number(loc.warehouseId) === Number(targetWarehouseId)) && loc.status !== "1")
    : locationList.value.filter(loc => loc.status !== "1")
  return buildSelectOptionList(filtered, [outboundItemForm.value.locationId], "locationId", "locationName", () => "库位资料缺失")
}

// 根据来源销售单号和销售单编号生成可读的来源销售单展示内容。
function getSaleOrderDisplayName(saleOrderNo, saleOrderId) {
  if (saleOrderNo) {
    return saleOrderNo
  }
  if (saleOrderId) {
    return "销售单资料缺失"
  }
  return "未关联销售单"
}

// 根据客户编号返回客户名称，减少出库单列表中的内部编号展示。
function getCustomerName(customerId) {
  const customer = customerList.value.find(customerItem => customerItem.customerId === customerId)
  return customer ? customer.customerName : (customerId ? "客户资料缺失" : "未关联客户")
}

// 根据仓库编号返回仓库名称，统一出库单页面展示口径。
function getWarehouseName(warehouseId) {
  const warehouse = warehouseList.value.find(warehouseItem => warehouseItem.warehouseId === warehouseId)
  return warehouse ? warehouse.warehouseName : (warehouseId ? "仓库资料缺失" : "")
}

// 根据商品编号返回商品名称，帮助用户在出库明细中快速确认商品。
function getProductName(productId) {
  const product = productList.value.find(productItem => productItem.productId === productId)
  return product ? product.productName : (productId ? "商品资料缺失" : "")
}

// 根据库位编号返回库位名称，统一出库明细展示口径。
function getLocationName(locationId) {
  const location = locationList.value.find(locationItem => locationItem.locationId === locationId)
  return location ? location.locationName : (locationId ? "库位资料缺失" : "")
}

// 返回出库类型中文名称，减少列表中的英文编码展示。
function getOutboundTypeLabel(outboundTypeValue) {
  const outboundTypeOption = outboundTypeOptionList.find(typeOption => typeOption.value === outboundTypeValue)
  return outboundTypeOption ? outboundTypeOption.label : (outboundTypeValue || "未知类型")
}

// 返回出库状态中文名称，统一列表和表单展示口径。
function getOutboundStatusLabel(statusValue) {
  const outboundStatusOption = outboundStatusOptionList.find(statusOption => statusOption.value === statusValue)
  return outboundStatusOption ? outboundStatusOption.label : (statusValue || "未知状态")
}

// 返回出库状态标签类型，帮助用户快速识别当前流程阶段。
function getOutboundStatusType(statusValue) {
  const outboundStatusOption = outboundStatusOptionList.find(statusOption => statusOption.value === statusValue)
  return outboundStatusOption ? outboundStatusOption.type : "info"
}

// 返回配送状态中文名称，减少列表中的英文编码展示。
function getOutboundDeliveryStatusLabel(deliveryStatusValue) {
  if (!deliveryStatusValue) {
    return "未维护"
  }
  if (deliveryStatusValue === "shipped") {
    return "配送中"
  }
  const deliveryStatusOption = deliveryStatusOptionList.find(statusOption => statusOption.value === deliveryStatusValue)
  return deliveryStatusOption ? deliveryStatusOption.label : deliveryStatusValue
}

// 返回配送状态标签类型，帮助用户快速识别履约进度。
function getOutboundDeliveryStatusType(deliveryStatusValue) {
  if (deliveryStatusValue === "shipped") {
    return "warning"
  }
  const deliveryStatusOption = deliveryStatusOptionList.find(statusOption => statusOption.value === deliveryStatusValue)
  return deliveryStatusOption ? deliveryStatusOption.type : "info"
}

// 格式化来源销售数量、已出库和待出库字段，避免未关联时直接显示空白或 undefined。
function formatOutboundItemQuantity(quantityValue) {
  if (quantityValue === undefined || quantityValue === null || quantityValue === "") {
    return "-"
  }
  return Number(quantityValue).toFixed(2)
}

// 根据数量和单价自动计算出库明细金额，避免仓库与物流人员重复手工录金额。
function syncOutboundItemAmount() {
  const quantityValue = Number(outboundItemForm.value.quantity || 0)
  const priceValue = Number(outboundItemForm.value.price || 0)
  outboundItemForm.value.amount = (quantityValue * priceValue).toFixed(2)
}

// 扫码出库快捷匹配：存在同商品明细直接 +1，否则预填新增出库明细表单。
async function handleOutboundBarcodeScan() {
  if (!ensureOutboundItemEditable("扫码录入")) {
    return
  }
  const code = String(outboundScanCode.value || "").trim()
  if (!code) {
    proxy.$modal.msgWarning("请扫描或输入商品条码/编码")
    return
  }
  outboundScanLoading.value = true
  try {
    const res = await scanProductByCode(code)
    const product = res.data
    if (!product?.productId) return
    appendUniqueSelectOption(productList.value, product, "productId")
    const existingItem = (outboundItemList.value || []).find(item => item.productId === product.productId)
    if (existingItem) {
      const nextQty = Number((Number(existingItem.quantity || 0) + 1).toFixed(2))
      const priceVal = Number(existingItem.price || product.salePrice || 0)
      await updateOutboundItem({
        ...existingItem,
        quantity: nextQty,
        amount: Number((nextQty * priceVal).toFixed(2))
      })
      proxy.$modal.msgSuccess(`扫码出库：${product.productName} 数量 +1（当前 ${nextQty}）`)
      outboundScanCode.value = ""
      getOutboundItemList()
      getList()
    } else if (isLinkedSaleOrderOutboundItemMode()) {
      proxy.$modal.msgWarning(`销售关联出库单中不包含商品「${product.productName}」`)
    } else {
      resetOutboundItemForm()
      outboundItemForm.value.productId = product.productId
      outboundItemForm.value.quantity = 1
      outboundItemForm.value.price = Number(product.salePrice || 0)
      outboundItemForm.value.amount = Number(product.salePrice || 0).toFixed(2)
      outboundItemFormOpen.value = true
      outboundItemFormTitle.value = `扫码新增出库明细 - ${product.productName}`
      outboundScanCode.value = ""
    }
  } finally {
    outboundScanLoading.value = false
  }
}

// 生成来源销售明细的进度提示，让仓库和物流在编辑时能直接看到销售数量和剩余可发数量。
function getOutboundItemSourceProgressText(targetOutboundItem) {
  return `来源销售明细已带入：销售数量 ${formatOutboundItemQuantity(targetOutboundItem.saleQuantity)}，已出库 ${formatOutboundItemQuantity(targetOutboundItem.shippedQuantity)}，待出库 ${formatOutboundItemQuantity(targetOutboundItem.remainQuantity)}。`
}

function getList() {
  loading.value = true
  return listOutbound(proxy.addDateRange(queryParams.value, dateRange.value)).then(response => {
    outboundList.value = response.rows
    total.value = response.total
    ensureOutboundReferenceOptionsLoaded(response.rows)
  }).finally(() => {
    loading.value = false
  })
}

function cancel() {
  open.value = false
  reset()
  clearAutoCreateRouteMode().catch(() => {})
}

// 判断当前地址是否来自销售订单的一键新建模式，避免普通列表页误触发自动弹窗。
function hasAutoCreateRouteMode() {
  return route.query.mode === "create" && !!queryParams.value.saleOrderId
}

// 清理地址中的自动新建标记，避免保存后刷新页面再次重复弹出新增来源销售单出库弹窗。
function clearAutoCreateRouteMode() {
  if (!route.query.mode) {
    return Promise.resolve()
  }
  const nextQuery = { ...route.query }
  delete nextQuery.mode
  return proxy.$router.replace({
    path: route.path,
    query: nextQuery
  })
}

// 保存成功后统一收口弹窗与路由状态，避免来源销售单的一键新建入口重复触发自动弹窗。
function handleOutboundSaveSuccess(successMessage) {
  proxy.$modal.msgSuccess(successMessage)
  open.value = false
  clearAutoCreateRouteMode().catch(() => {})
  getList()
}

function reset() {
  form.value = {
    outboundId: undefined,
    outboundNo: undefined,
    outboundType: "sale",
    customerId: undefined,
    saleOrderId: undefined,
    saleOrderNo: undefined,
    warehouseId: undefined,
    totalQty: 0,
    totalAmount: 0,
    status: "draft",
    auditBy: undefined,
    auditTime: undefined,
    platformType: undefined,
    storeId: undefined,
    sourceOrderNo: undefined,
    carrier: undefined,
    waybillNo: undefined,
    freightCost: 0,
    deliveryStatus: "pending",
    remark: undefined
  }
  proxy.resetForm("outboundRef")
}

function resetOutboundItemForm() {
  outboundItemForm.value = {
    outboundItemId: undefined,
    outboundId: currentOutboundId.value,
    saleOrderItemId: undefined,
    productId: undefined,
    locationId: undefined,
    batchNo: undefined,
    quantity: 1,
    price: 0,
    amount: 0,
    saleQuantity: undefined,
    shippedQuantity: undefined,
    remainQuantity: undefined
  }
  proxy.resetForm("outboundItemRef")
}

function handleQuery() {
  queryParams.value.pageNum = 1
  getList()
}

function resetQuery() {
  dateRange.value = []
  proxy.resetForm("queryRef")
  queryParams.value.saleOrderId = undefined
  queryParams.value.status = undefined
  queryParams.value.sourceOrderNo = undefined
  queryParams.value.pageNum = 1
  if (Object.keys(route.query || {}).length > 0) {
    proxy.$router.replace({ path: route.path, query: {} }).catch(() => {})
    return
  }
  handleQuery()
}

function handleOpenDeliveryDialog(row, targetStatus) {
  deliveryDialog.outboundId = row.outboundId
  deliveryDialog.outboundNo = row.outboundNo || ""
  deliveryDialog.deliveryStatus = targetStatus
  deliveryDialog.carrier = row.carrier || ""
  deliveryDialog.waybillNo = row.waybillNo || ""
  deliveryDialog.freightCost = Number(row.freightCost || 0)
  deliveryDialog.open = true
}

function submitDeliveryForm() {
  if (deliveryDialog.deliveryStatus === "shipping" && !String(deliveryDialog.carrier || "").trim()) {
    proxy.$modal.msgWarning("开始配送时请填写承运商")
    return
  }
  deliveryDialog.submitting = true
  updateOutboundDelivery(deliveryDialog.outboundId, {
    deliveryStatus: deliveryDialog.deliveryStatus,
    carrier: deliveryDialog.carrier,
    waybillNo: deliveryDialog.waybillNo,
    freightCost: deliveryDialog.freightCost
  }).then(() => {
    proxy.$modal.msgSuccess(deliveryDialog.deliveryStatus === "delivered" ? "已确认客户签收" : "已登记开始配送")
    deliveryDialog.open = false
    getList()
  }).finally(() => {
    deliveryDialog.submitting = false
  })
}

function handleSelectionChange(selection) {
  const selectionState = buildDocumentSelectionState(selection, "outboundId")
  ids.value = selectionState.idList
  single.value = selectionState.single
  multiple.value = selectionState.multiple
  canUpdateSelected.value = selection.length === 1 && isDraftOutboundStatus(selection[0].status)
  canSubmitSelected.value = selection.length > 0 && selection.every(selectionItem => isDraftOutboundStatus(selectionItem.status))
  canAuditSelected.value = selection.length > 0 && selection.every(selectionItem => selectionItem.status === "submitted")
  canDeleteSelected.value = selection.length > 0 && selection.every(selectionItem => isDraftOutboundStatus(selectionItem.status))
}

function loadAvailableSaleOrders(keyword) {
  soSelectLoading.value = true
  listSaleOrder({
    pageNum: 1,
    pageSize: 50,
    status: "audited",
    orderNo: normalizeRemoteKeyword(keyword)
  }).then(res => {
    availableSaleOrderList.value = (res.rows || []).filter(so => Number(so.remainQuantity ?? so.totalQty ?? 1) > 0)
  }).catch(() => {}).finally(() => {
    soSelectLoading.value = false
  })
}

function handleSaleOrderSelectChange(soId) {
  if (!soId) {
    form.value.saleOrderNo = undefined
    form.value.sourceOrderNo = undefined
    return
  }
  const so = availableSaleOrderList.value.find(item => item.saleOrderId === soId)
  if (so) {
    form.value.saleOrderNo = so.orderNo
    form.value.sourceOrderNo = so.orderNo
    form.value.outboundType = "sale"
    form.value.customerId = so.customerId
    form.value.warehouseId = so.warehouseId
    ensureCustomerOptionLoaded(so.customerId)
    ensureWarehouseOptionLoaded(so.warehouseId)
  }
}

function handleExportOutboundItems() {
  if (!currentOutboundId.value) return
  proxy.download("business/outboundItem/export", {
    outboundId: currentOutboundId.value
  }, `outboundItem_${currentOutboundId.value}_${new Date().getTime()}.xlsx`)
}

async function handleAdd() {
  reset()
  loadAvailableSaleOrders()
  await applyRouteSaleOrderToForm()
  if (form.value.saleOrderId && form.value.saleOrderNo && !availableSaleOrderList.value.some(s => s.saleOrderId === form.value.saleOrderId)) {
    availableSaleOrderList.value.unshift({
      saleOrderId: form.value.saleOrderId,
      orderNo: form.value.saleOrderNo,
      customerName: getCustomerName(form.value.customerId),
      remainQuantity: undefined
    })
  }
  open.value = true
  title.value = queryParams.value.saleOrderId ? "新增来源销售单出库" : "新增销售出库单"
}

function handleUpdate(row) {
  const currentOutboundRow = row || outboundList.value.find(outboundItem => outboundItem.outboundId === ids.value[0])
  if (currentOutboundRow && !isDraftOutboundStatus(currentOutboundRow.status)) {
    proxy.$modal.msgWarning("只有草稿状态的销售出库单才允许修改")
    return
  }
  reset()
  const outboundId = row?.outboundId || ids.value[0]
  getOutbound(outboundId).then(response => {
    form.value = response.data
    ensureCustomerOptionLoaded(form.value.customerId)
    ensureWarehouseOptionLoaded(form.value.warehouseId)
    if (!form.value.deliveryStatus) {
      form.value.deliveryStatus = "pending"
    }
    normalizeOutboundLogisticsInfoByDeliveryStatus()
    open.value = true
    title.value = "修改销售出库单"
  })
}

// 从出库单直接回跳来源销售订单，减少销售、仓库和物流之间的来回查找。
function handleViewSaleOrder(row) {
  if (!row.saleOrderId) {
    return
  }
  proxy.$router.push({
    path: "/sales/saleOrder",
    query: {
      saleOrderId: row.saleOrderId
    }
  })
}

// 打开出库明细弹窗时补充单据与配送状态，帮助用户快速判断当前是否可维护。
function handleOpenOutboundItem(row) {
  currentOutboundId.value = row.outboundId
  currentOutboundStatus.value = row.status
  currentOutboundSaleOrderId.value = row.saleOrderId
  currentOutboundWarehouseId.value = row.warehouseId
  loadLocationOptionList()
  outboundItemTitle.value = "出库明细 - " + row.outboundNo + "（" + getOutboundStatusLabel(row.status) + " / " + getOutboundDeliveryStatusLabel(row.deliveryStatus) + "）"
  outboundItemOpen.value = true
  outboundScanCode.value = ""
  selectedOutboundItemIds.value = []
  outboundItemMultiple.value = true
  getOutboundItemList()
}

// 判断当前出库明细是否允许继续维护，避免已提交或已审核单据继续改动明细。
function isOutboundItemEditable() {
  return currentOutboundStatus.value === "draft"
}

// 校验出库明细当前是否允许维护，避免只依赖按钮显示状态造成误操作。
function ensureOutboundItemEditable(actionLabel) {
  if (!isOutboundItemEditable()) {
    proxy.$modal.msgWarning("当前销售出库单已进入后续流程，不能" + actionLabel + "明细")
    return false
  }
  return true
}

function getOutboundItemList() {
  if (!currentOutboundId.value) {
    outboundItemList.value = []
    return
  }
  outboundItemLoading.value = true
  listOutboundItem({
    pageNum: 1,
    pageSize: 1000,
    outboundId: currentOutboundId.value
  }).then(response => {
    outboundItemList.value = response.rows
    ensureOutboundItemReferenceOptionsLoaded(response.rows)
    outboundItemLoading.value = false
  }).catch(() => {
    outboundItemLoading.value = false
  })
}

function handleOutboundItemSelectionChange(selection) {
  selectedOutboundItemIds.value = selection.map(item => item.outboundItemId)
  outboundItemMultiple.value = !selection.length
}

// 新增出库明细前再次校验主单状态，避免通过其他入口绕过只读限制。
function handleAddOutboundItem() {
  if (!ensureOutboundItemEditable("新增")) {
    return
  }
  if (isLinkedSaleOrderOutboundItemMode()) {
    proxy.$modal.msgWarning("来源销售单的出库明细由系统自动带入，不支持手工新增")
    return
  }
  resetOutboundItemForm()
  outboundItemFormOpen.value = true
  outboundItemFormTitle.value = "新增出库明细"
}

// 修改出库明细前再次校验主单状态，确保非草稿单据只能查看明细。
function handleUpdateOutboundItem(row) {
  if (!ensureOutboundItemEditable("修改")) {
    return
  }
  resetOutboundItemForm()
  const outboundItemId = row.outboundItemId
  getOutboundItem(outboundItemId).then(response => {
    outboundItemForm.value = response.data
    ensureProductOptionLoaded(outboundItemForm.value.productId)
    ensureLocationOptionLoaded(outboundItemForm.value.locationId)
    syncOutboundItemAmount()
    outboundItemFormOpen.value = true
    outboundItemFormTitle.value = "修改出库明细"
  })
}

function cancelOutboundItemForm() {
  outboundItemFormOpen.value = false
  resetOutboundItemForm()
}

// 保存出库明细前再次校验主单状态，避免后续流程中的明细被间接修改。
function submitOutboundItemForm() {
  if (!ensureOutboundItemEditable("保存")) {
    return
  }
  syncOutboundItemAmount()
  proxy.$refs["outboundItemRef"].validate(valid => {
    if (valid) {
      outboundItemForm.value.outboundId = currentOutboundId.value
      if (outboundItemForm.value.outboundItemId !== undefined) {
        updateOutboundItem(outboundItemForm.value).then(() => {
          proxy.$modal.msgSuccess("修改成功")
          outboundItemFormOpen.value = false
          getOutboundItemList()
          getList()
        })
      } else {
        addOutboundItem(outboundItemForm.value).then(() => {
          proxy.$modal.msgSuccess("新增成功")
          outboundItemFormOpen.value = false
          getOutboundItemList()
          getList()
        })
      }
    }
  })
}

// 删除出库明细前再次校验主单状态，避免非草稿单据的明细被批量删除。
function handleDeleteOutboundItem(row) {
  if (!ensureOutboundItemEditable("删除")) {
    return
  }
  const outboundItemIds = row ? row.outboundItemId : selectedOutboundItemIds.value
  proxy.$modal.confirm('是否确认删除出库明细编号为"' + outboundItemIds + '"的数据项？').then(function () {
    return delOutboundItem(outboundItemIds)
  }).then(() => {
    proxy.$modal.msgSuccess("删除成功")
    getOutboundItemList()
    getList()
  }).catch(() => {})
}

function submitForm() {
  if (!isOutboundFormEditable()) {
    proxy.$modal.msgWarning("只有草稿状态的销售出库单才允许保存")
    return
  }
  proxy.$refs["outboundRef"].validate(valid => {
    if (valid) {
      if (form.value.outboundId !== undefined) {
        updateOutbound(form.value).then(() => {
          handleOutboundSaveSuccess("修改成功")
        })
      } else {
        addOutbound(form.value).then(() => {
          handleOutboundSaveSuccess("新增成功")
        })
      }
    }
  })
}

function handleDelete(row) {
  if (row && !isDraftOutboundStatus(row.status)) {
    proxy.$modal.msgWarning("只有草稿状态的销售出库单才允许删除")
    return
  }
  if (!row && !ids.value.length) {
    proxy.$modal.msgWarning("请选择要删除的销售出库单")
    return
  }
  if (!row && !canDeleteSelected.value) {
    proxy.$modal.msgWarning("只能删除草稿状态的销售出库单")
    return
  }
  const outboundIds = row?.outboundId || ids.value
  proxy.$modal.confirm('是否确认删除销售出库单编号为"' + outboundIds + '"的数据项？').then(function () {
    return delOutbound(outboundIds)
  }).then(() => {
    getList()
    proxy.$modal.msgSuccess("删除成功")
  }).catch(() => {})
}

// 获取当前要处理的销售出库单，统一兼容行内操作和后续可能扩展的工具栏入口。
function getTargetOutboundRow(row, actionText) {
  return resolveDocumentActionRow(
    row,
    ids.value,
    outboundList.value,
    "outboundId",
    actionText,
    "销售出库单",
    warningMessage => proxy.$modal.msgWarning(warningMessage)
  )
}

// 提交按钮操作，支持单条与多选批量提交。
function handleSubmit(row) {
  if (!row?.outboundId && ids.value.length > 1) {
    proxy.$modal.confirm(`是否确认批量提交选中的 ${ids.value.length} 笔出库单？`).then(() => {
      return batchSubmitOutbound(ids.value)
    }).then(res => {
      proxy.$modal.msgSuccess(res.msg || "批量提交完成")
      getList()
    }).catch(() => {})
    return
  }
  const targetOutboundRow = getTargetOutboundRow(row, "提交")
  if (!targetOutboundRow) {
    return
  }
  proxy.$modal.confirm('是否确认提交出库单号为"' + targetOutboundRow.outboundNo + '"的数据项？').then(function () {
    return submitOutbound(targetOutboundRow.outboundId)
  }).then(() => {
    proxy.$modal.msgSuccess("提交成功")
    getList()
  }).catch(() => {})
}

// 审核按钮操作，支持单条与多选批量审核。
function handleAudit(row) {
  if (!row?.outboundId && ids.value.length > 1) {
    proxy.$modal.confirm(`是否确认批量审核选中的 ${ids.value.length} 笔出库单？审核后将正式核销库存锁定并扣减实物库存。`).then(() => {
      return batchAuditOutbound(ids.value)
    }).then(res => {
      proxy.$modal.msgSuccess(res.msg || "批量审核完成")
      getList()
    }).catch(() => {})
    return
  }
  const targetOutboundRow = getTargetOutboundRow(row, "审核")
  if (!targetOutboundRow) {
    return
  }
  proxy.$modal.confirm('是否确认审核出库单号为"' + targetOutboundRow.outboundNo + '"的数据项？审核后将正式核销库存锁定并扣减实物库存。').then(function () {
    return auditOutbound(targetOutboundRow.outboundId)
  }).then(() => {
    proxy.$modal.msgSuccess("审核成功，出库实物扣减与流水已记账")
    getList()
  }).catch(() => {})
}

function handleReject(row) {
  proxy.$modal.prompt(`确认驳回出库单"${row.outboundNo}"？请输入驳回原因：`).then(({ value }) => {
    return rejectOutbound(row.outboundId, value || "拣货数量或物流单号待核实")
  }).then(() => {
    proxy.$modal.msgSuccess("出库单已驳回至草稿")
    getList()
  }).catch(() => {})
}

async function handleOutboundProductSelect(productId) {
  if (!productId || outboundItemForm.value.saleOrderItemId || !currentOutboundWarehouseId.value) return
  try {
    const res = await recommendStockBatch(currentOutboundWarehouseId.value, productId)
    const firstBatch = (res.data || [])[0]
    if (firstBatch) {
      outboundItemForm.value.locationId = firstBatch.locationId
      outboundItemForm.value.batchNo = firstBatch.batchNo
      ensureLocationOptionLoaded(firstBatch.locationId)
      proxy.$modal.msgSuccess(`已按 FEFO 近效期优先自动匹配批次：${firstBatch.batchNo || '默认批次'}`)
    }
  } catch (e) {
    // ignore
  }
}

async function handlePrintOutbound(row) {
  const res = await listOutboundItem({ pageNum: 1, pageSize: 1000, outboundId: row.outboundId })
  const items = res.rows || []
  await ensureOutboundItemReferenceOptionsLoaded(items)
  printDialogRef.value?.openPrint({
    docType: "outbound",
    title: "销售出库送货单",
    docNo: row.outboundNo,
    defaultPaperMode: "triplicate",
    metaFields: [
      { label: "送货单号", value: row.outboundNo },
      { label: "客户名称", value: row.customerName || getCustomerName(row.customerId) },
      { label: "来源销售单", value: getSaleOrderDisplayName(row.saleOrderNo, row.saleOrderId) },
      { label: "发货仓库", value: row.warehouseName || getWarehouseName(row.warehouseId) },
      { label: "物流承运", value: row.carrier ? `${row.carrier} (${row.waybillNo || '-'})` : "自提/专车配送" },
      { label: "发货日期", value: parseTime(row.createTime, "{y}-{m}-{d}") }
    ],
    columns: [
      { label: "商品名称", prop: "productName" },
      { label: "库位", prop: "locationName", width: "120px" },
      { label: "批次号", prop: "batchNo", width: "110px" },
      { label: "实发数量", prop: "quantity", width: "90px", align: "right" },
      { label: "单价", prop: "price", width: "100px", align: "right", type: "money", isPrice: true },
      { label: "金额小计", prop: "amount", width: "115px", align: "right", type: "money", isPrice: true }
    ],
    items: items.map(it => ({
      productName: getProductName(it.productId),
      locationName: getLocationName(it.locationId),
      batchNo: it.batchNo || "-",
      quantity: it.quantity,
      price: it.price,
      amount: it.amount
    })),
    totalQty: row.totalQty,
    totalAmount: row.totalAmount,
    remark: row.remark
  })
}

// 作废按钮操作，补充空选择保护，避免未来工具栏入口场景下出现静默无响应。
function handleCancel(row) {
  const targetOutboundRow = getTargetOutboundRow(row, "作废")
  if (!targetOutboundRow) {
    return
  }
  proxy.$modal.confirm('是否确认作废出库单号为"' + targetOutboundRow.outboundNo + '"的数据项？').then(function () {
    return cancelOutbound(targetOutboundRow.outboundId)
  }).then(() => {
    proxy.$modal.msgSuccess("作废成功")
    getList()
  }).catch(() => {})
}

function handleExport() {
  proxy.download("business/outbound/export", {
    ...queryParams.value
  }, `outbound_${new Date().getTime()}.xlsx`)
}

// 判断当前是否来自销售订单的一键新建出库入口，支持分批发货（只要当前无未提交草稿出库单即自动拉起建单）。
function shouldAutoOpenLinkedOutboundFromRoute() {
  return hasAutoCreateRouteMode() && !outboundList.value.some(r => r.status === "draft")
}

// 初始化页面基础数据和列表，并在需要时自动打开来源销售单的新建出库弹窗。
async function initializePage() {
  initializeQueryParamsFromRoute()
  await Promise.all([
    Promise.resolve(initBasicData()),
    Promise.resolve(getList())
  ])
  if (shouldAutoOpenLinkedOutboundFromRoute()) {
    handleAdd()
  }
}

// 监听路由参数变化，保证从销售订单重复跳转到当前页面时筛选条件和自动新建出库逻辑同步刷新。
watch(() => route.fullPath, (currentRouteFullPath, previousRouteFullPath) => {
  if (currentRouteFullPath === previousRouteFullPath) {
    return
  }
  initializePage()
})

onActivated(() => {
  initializeQueryParamsFromRoute()
  getList()
})

initializePage()
</script>
