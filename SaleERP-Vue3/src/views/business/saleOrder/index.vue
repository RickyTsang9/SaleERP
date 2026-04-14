<template>
  <div class="app-container">
    <el-form :model="queryParams" ref="queryRef" :inline="true" v-show="showSearch">
      <el-form-item label="销售单号" prop="orderNo">
        <el-input v-model="queryParams.orderNo" placeholder="请输入销售单号" clearable style="width: 220px" @keyup.enter="handleQuery" />
      </el-form-item>
      <el-form-item label="客户" prop="customerId">
        <el-select v-model="queryParams.customerId" placeholder="请选择客户" clearable filterable remote reserve-keyword remote-show-suffix :remote-method="loadCustomerOptionList" :loading="customerListLoading" style="width: 220px">
          <el-option
            v-for="customer in customerSelectOptionList"
            :key="customer.customerId"
            :label="customer.customerName"
            :value="customer.customerId"
          />
        </el-select>
      </el-form-item>
      <el-form-item label="单据状态" prop="status">
        <el-select v-model="queryParams.status" placeholder="请选择单据状态" clearable style="width: 180px">
          <el-option
            v-for="statusOption in saleOrderStatusOptionList"
            :key="statusOption.value"
            :label="statusOption.label"
            :value="statusOption.value"
          />
        </el-select>
      </el-form-item>
      <el-form-item label="付款状态" prop="paymentStatus">
        <el-select v-model="queryParams.paymentStatus" placeholder="请选择付款状态" clearable style="width: 180px">
          <el-option
            v-for="paymentStatusOption in saleOrderPaymentStatusOptionList"
            :key="paymentStatusOption.value"
            :label="paymentStatusOption.label"
            :value="paymentStatusOption.value"
          />
        </el-select>
      </el-form-item>
      <el-form-item>
        <el-button type="primary" icon="Search" @click="handleQuery">搜索</el-button>
        <el-button icon="Refresh" @click="resetQuery">重置</el-button>
      </el-form-item>
    </el-form>

    <el-row :gutter="10" class="mb8">
      <el-col :span="1.5">
        <el-button type="primary" plain icon="Plus" @click="handleAdd" v-hasPermi="['business:saleOrder:add']">新增</el-button>
      </el-col>
      <el-col :span="1.5">
        <el-button type="success" plain icon="Edit" :disabled="single || !canEditSelectedSaleOrder" @click="handleUpdate" v-hasPermi="['business:saleOrder:edit']">修改</el-button>
      </el-col>
      <el-col :span="1.5">
        <el-button type="warning" plain :disabled="single || selectedStatus !== 'draft'" @click="handleSubmit" v-hasPermi="['business:saleOrder:submit']">提交</el-button>
      </el-col>
      <el-col :span="1.5">
        <el-button type="warning" plain :disabled="single || selectedStatus !== 'submitted'" @click="handleManagerAudit" v-hasPermi="['business:saleOrder:managerAudit']">经理审核</el-button>
      </el-col>
      <el-col :span="1.5">
        <el-button type="danger" plain :disabled="single || selectedStatus !== 'manager_approved'" @click="handleManagerRollback" v-hasPermi="['business:saleOrder:managerAudit']">经理回退</el-button>
      </el-col>
      <el-col :span="1.5">
        <el-button type="warning" plain :disabled="single || selectedStatus !== 'manager_approved'" @click="handleFinanceAudit" v-hasPermi="['business:saleOrder:financeAudit']">财务审核</el-button>
      </el-col>
      <el-col :span="1.5">
        <el-button type="danger" plain :disabled="single || selectedStatus !== 'audited'" @click="handleFinanceRollback" v-hasPermi="['business:saleOrder:financeAudit']">财务回退</el-button>
      </el-col>
      <el-col :span="1.5">
        <el-button type="info" plain :disabled="single || !canCancelSelectedSaleOrder" @click="handleCancel" v-hasPermi="['business:saleOrder:edit']">作废</el-button>
      </el-col>
      <el-col :span="1.5">
        <el-button type="danger" plain icon="Delete" :disabled="multiple || !canDeleteSelectedSaleOrder" @click="handleDelete" v-hasPermi="['business:saleOrder:remove']">删除</el-button>
      </el-col>
      <el-col :span="1.5">
        <el-button type="info" plain icon="Upload" @click="handleImport" v-hasPermi="['business:saleOrder:import']">导入</el-button>
      </el-col>
      <el-col :span="1.5">
        <el-button type="warning" plain icon="Download" @click="handleExport" v-hasPermi="['business:saleOrder:export']">导出</el-button>
      </el-col>
      <el-col :span="1.5">
        <el-button type="primary" plain :disabled="single" @click="handlePrintTemplate" v-hasPermi="['business:saleOrder:print']">打印模板</el-button>
      </el-col>
      <el-col :span="1.5">
        <el-button type="primary" plain :disabled="single" @click="handleStatusHistory" v-hasPermi="['business:saleOrder:query']">状态历史</el-button>
      </el-col>
      <right-toolbar v-model:showSearch="showSearch" @queryTable="getList"></right-toolbar>
    </el-row>

    <el-table v-loading="loading" :data="saleOrderList" @selection-change="handleSelectionChange">
      <el-table-column type="selection" width="55" align="center" />
      <el-table-column label="销售单编号" align="center" prop="saleOrderId" />
      <el-table-column label="销售单号" align="center" prop="orderNo" min-width="180" />
      <el-table-column label="客户" align="center" prop="customerId" min-width="180">
        <template #default="scope">
          <span>{{ getCustomerName(scope.row.customerId) }}</span>
        </template>
      </el-table-column>
      <el-table-column label="仓库" align="center" prop="warehouseId" min-width="180">
        <template #default="scope">
          <span>{{ getWarehouseName(scope.row.warehouseId) }}</span>
        </template>
      </el-table-column>
      <el-table-column label="总数量" align="center" prop="totalQty" />
      <el-table-column label="总金额" align="center" prop="totalAmount" />
      <el-table-column label="应收金额" align="center" prop="receivableAmount" min-width="110">
        <template #default="scope">
          <span>{{ formatSaleOrderAmount(scope.row.receivableAmount) }}</span>
        </template>
      </el-table-column>
      <el-table-column label="已收金额" align="center" prop="receivedAmount" min-width="110">
        <template #default="scope">
          <span>{{ formatSaleOrderAmount(scope.row.receivedAmount) }}</span>
        </template>
      </el-table-column>
      <el-table-column label="未收金额" align="center" prop="remainReceivableAmount" min-width="110">
        <template #default="scope">
          <span>{{ formatSaleOrderAmount(scope.row.remainReceivableAmount) }}</span>
        </template>
      </el-table-column>
      <el-table-column label="待出库" align="center" prop="remainQuantity" min-width="110">
        <template #default="scope">
          <span>{{ formatSaleOrderQuantity(scope.row.remainQuantity) }}</span>
        </template>
      </el-table-column>
      <el-table-column label="单据状态" align="center" prop="status">
        <template #default="scope">
          <el-tag :type="getSaleOrderStatusType(scope.row.status)">{{ getSaleOrderStatusLabel(scope.row.status) }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column label="付款状态" align="center" prop="paymentStatus">
        <template #default="scope">
          <el-tag :type="getSaleOrderPaymentStatusType(scope.row.paymentStatus)">{{ getSaleOrderPaymentStatusLabel(scope.row.paymentStatus) }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column label="创建时间" align="center" prop="createTime" width="180">
        <template #default="scope">
          <span>{{ parseTime(scope.row.createTime) }}</span>
        </template>
      </el-table-column>
      <el-table-column label="操作" width="560" align="center" class-name="small-padding fixed-width">
        <template #default="scope">
          <el-button link type="primary" icon="List" @click="handleOpenSaleOrderItem(scope.row)" v-hasPermi="['business:saleOrder:query']">明细</el-button>
          <el-button link type="success" @click="handleViewOutbound(scope.row)" v-hasPermi="['business:outbound:list']" v-if="canViewOutbound(scope.row)">{{ getOutboundActionLabel(scope.row) }}</el-button>
          <el-button link type="primary" icon="Money" @click="handleViewReceivable(scope.row)" v-hasPermi="['business:receivable:list']" v-if="canViewReceivable(scope.row)">应收</el-button>
          <el-button link type="warning" icon="Money" @click="handleCreateReceipt(scope.row)" v-hasPermi="['business:receipt:add']" v-if="canCreateReceipt(scope.row)">登记回款</el-button>
          <el-button link type="primary" icon="Tickets" @click="handleViewReceipt(scope.row)" v-hasPermi="['business:receipt:list']" v-if="canViewReceipt(scope.row)">查看回款</el-button>
          <el-button link type="primary" icon="Edit" @click="handleUpdate(scope.row)" v-hasPermi="['business:saleOrder:edit']" v-if="canEditSaleOrder(scope.row)">修改</el-button>
          <el-button link type="primary" icon="Delete" @click="handleDelete(scope.row)" v-hasPermi="['business:saleOrder:remove']" v-if="canDeleteSaleOrder(scope.row)">删除</el-button>
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
      <el-form ref="saleOrderRef" :model="form" :rules="rules" label-width="110px">
        <el-row>
          <el-col :span="12">
            <el-form-item label="客户" prop="customerId">
              <el-select v-model="form.customerId" placeholder="请选择客户" clearable filterable remote reserve-keyword remote-show-suffix :remote-method="loadCustomerOptionList" :loading="customerListLoading" style="width: 100%">
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
            <el-form-item label="单据状态" prop="status">
              <el-select v-model="form.status" placeholder="系统自动维护单据状态" disabled style="width: 100%">
                <el-option
                  v-for="statusOption in saleOrderStatusOptionList"
                  :key="statusOption.value"
                  :label="statusOption.label"
                  :value="statusOption.value"
                />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="付款状态" prop="paymentStatus">
              <el-select v-model="form.paymentStatus" placeholder="系统自动维护付款状态" disabled style="width: 100%">
                <el-option
                  v-for="paymentStatusOption in saleOrderPaymentStatusOptionList"
                  :key="paymentStatusOption.value"
                  :label="paymentStatusOption.label"
                  :value="paymentStatusOption.value"
                />
              </el-select>
            </el-form-item>
          </el-col>
        </el-row>
        <el-row>
          <el-col :span="24">
            <el-form-item label="备注" prop="remark">
              <el-input v-model="form.remark" type="textarea" placeholder="请输入备注" />
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

    <el-dialog :title="saleOrderItemTitle" v-model="saleOrderItemOpen" width="980px" append-to-body>
      <el-alert
        v-if="!isSaleOrderItemEditable()"
        title="当前销售订单已进入后续流程，明细仅支持查看。"
        type="info"
        :closable="false"
        class="mb8"
      />
      <el-row :gutter="10" class="mb8">
        <el-col :span="1.5">
          <el-button
            v-if="isSaleOrderItemEditable()"
            type="primary"
            plain
            icon="Plus"
            @click="handleAddSaleOrderItem"
            v-hasPermi="['business:saleOrder:add']"
          >新增明细</el-button>
        </el-col>
        <el-col :span="1.5">
          <el-button
            v-if="isSaleOrderItemEditable()"
            type="danger"
            plain
            icon="Delete"
            :disabled="saleOrderItemMultiple"
            @click="handleDeleteSaleOrderItem"
            v-hasPermi="['business:saleOrder:remove']"
          >删除明细</el-button>
        </el-col>
      </el-row>
      <el-table v-loading="saleOrderItemLoading" :data="saleOrderItemList" @selection-change="handleSaleOrderItemSelectionChange">
        <el-table-column v-if="isSaleOrderItemEditable()" type="selection" width="55" align="center" />
        <el-table-column label="明细编号" align="center" prop="saleOrderItemId" />
        <el-table-column label="商品" align="center" prop="productId" min-width="180">
          <template #default="scope">
            <span>{{ getProductName(scope.row.productId) }}</span>
          </template>
        </el-table-column>
        <el-table-column label="库位" align="center" prop="locationId" min-width="180">
          <template #default="scope">
            <span>{{ getLocationName(scope.row.locationId) }}</span>
          </template>
        </el-table-column>
        <el-table-column label="批次号" align="center" prop="batchNo" />
        <el-table-column label="数量" align="center" prop="quantity" />
        <el-table-column label="已出库" align="center" prop="outboundQuantity" width="110">
          <template #default="scope">
            <span>{{ formatSaleOrderQuantity(scope.row.outboundQuantity) }}</span>
          </template>
        </el-table-column>
        <el-table-column label="待出库" align="center" prop="remainQuantity" width="110">
          <template #default="scope">
            <span>{{ formatSaleOrderQuantity(scope.row.remainQuantity) }}</span>
          </template>
        </el-table-column>
        <el-table-column label="单价" align="center" prop="price" />
        <el-table-column label="金额" align="center" prop="amount" />
        <el-table-column v-if="isSaleOrderItemEditable()" label="操作" width="180" align="center">
          <template #default="scope">
            <el-button v-if="isSaleOrderItemEditable()" link type="primary" icon="Edit" @click="handleUpdateSaleOrderItem(scope.row)" v-hasPermi="['business:saleOrder:edit']">修改</el-button>
            <el-button v-if="isSaleOrderItemEditable()" link type="primary" icon="Delete" @click="handleDeleteSaleOrderItem(scope.row)" v-hasPermi="['business:saleOrder:remove']">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-dialog>

    <el-dialog :title="saleOrderItemFormTitle" v-model="saleOrderItemFormOpen" width="760px" append-to-body>
      <el-form ref="saleOrderItemRef" :model="saleOrderItemForm" :rules="saleOrderItemRules" label-width="110px">
        <el-row>
          <el-col :span="12">
            <el-form-item label="商品" prop="productId">
              <el-select v-model="saleOrderItemForm.productId" placeholder="请选择商品" clearable filterable remote reserve-keyword remote-show-suffix :remote-method="loadProductOptionList" :loading="productListLoading" style="width: 100%">
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
              <el-select v-model="saleOrderItemForm.locationId" placeholder="请选择库位" clearable filterable remote reserve-keyword remote-show-suffix :remote-method="loadLocationOptionList" :loading="locationListLoading" style="width: 100%">
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
              <el-input v-model="saleOrderItemForm.batchNo" placeholder="请输入批次号" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="数量" prop="quantity">
              <el-input-number v-model="saleOrderItemForm.quantity" :min="0" :precision="2" controls-position="right" style="width: 100%" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-row>
          <el-col :span="12">
            <el-form-item label="单价" prop="price">
              <el-input-number v-model="saleOrderItemForm.price" :min="0" :precision="2" controls-position="right" style="width: 100%" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="金额" prop="amount">
              <el-input-number v-model="saleOrderItemForm.amount" :min="0" :precision="2" controls-position="right" style="width: 100%" />
            </el-form-item>
          </el-col>
        </el-row>
      </el-form>
      <template #footer>
        <div class="dialog-footer">
          <el-button type="primary" @click="submitSaleOrderItemForm">确 定</el-button>
          <el-button @click="cancelSaleOrderItemForm">取 消</el-button>
        </div>
      </template>
    </el-dialog>

    <el-dialog :title="upload.title" v-model="upload.open" width="400px" append-to-body>
      <el-upload
        ref="uploadRef"
        :limit="1"
        accept=".xlsx, .xls"
        :headers="upload.headers"
        :action="upload.url + '?updateSupport=' + upload.updateSupport"
        :disabled="upload.isUploading"
        :on-progress="handleFileUploadProgress"
        :on-success="handleFileSuccess"
        :on-change="handleFileChange"
        :on-remove="handleFileRemove"
        :auto-upload="false"
        drag
      >
        <el-icon class="el-icon--upload"><upload-filled /></el-icon>
        <div class="el-upload__text">将文件拖到此处，或<em>点击上传</em></div>
        <template #tip>
          <div class="el-upload__tip text-center">
            <div class="el-upload__tip">
              <el-checkbox v-model="upload.updateSupport" />是否更新已经存在的销售订单数据
            </div>
            <span>仅允许导入xls、xlsx格式文件。</span>
            <el-link type="primary" underline="never" style="font-size: 12px; vertical-align: baseline" @click="importTemplate">下载模板</el-link>
          </div>
        </template>
      </el-upload>
      <template #footer>
        <div class="dialog-footer">
          <el-button type="primary" @click="submitFileForm">确 定</el-button>
          <el-button @click="upload.open = false">取 消</el-button>
        </div>
      </template>
    </el-dialog>

    <el-dialog title="状态历史" v-model="statusHistoryOpen" width="920px" append-to-body>
      <el-table v-loading="statusHistoryLoading" :data="statusHistoryList">
        <el-table-column label="历史编号" align="center" prop="historyId" width="90" />
        <el-table-column label="原状态" align="center" prop="fromStatus" min-width="120">
          <template #default="scope">
            <span>{{ getSaleOrderStatusLabel(scope.row.fromStatus) }}</span>
          </template>
        </el-table-column>
        <el-table-column label="新状态" align="center" prop="toStatus" min-width="120">
          <template #default="scope">
            <span>{{ getSaleOrderStatusLabel(scope.row.toStatus) }}</span>
          </template>
        </el-table-column>
        <el-table-column label="操作类型" align="center" prop="operationType" min-width="140">
          <template #default="scope">
            <span>{{ getSaleOrderOperationTypeLabel(scope.row.operationType) }}</span>
          </template>
        </el-table-column>
        <el-table-column label="审核角色" align="center" prop="auditRole" min-width="120">
          <template #default="scope">
            <span>{{ getSaleOrderAuditRoleLabel(scope.row.auditRole) }}</span>
          </template>
        </el-table-column>
        <el-table-column label="操作人" align="center" prop="auditBy" min-width="120" />
        <el-table-column label="审核意见" align="center" prop="auditComment" min-width="200" />
        <el-table-column label="操作时间" align="center" prop="operateTime" width="180">
          <template #default="scope">
            <span>{{ parseTime(scope.row.operateTime) }}</span>
          </template>
        </el-table-column>
      </el-table>
    </el-dialog>

    <!-- 打印预览对话框 -->
    <el-dialog title="打印预览" v-model="printOpen" width="800px" append-to-body>
      <div id="printBox" class="print-container" style="padding: 20px; font-family: 'SimSun', '宋体', serif;">
        <h2 style="text-align: center; font-size: 24px; margin-bottom: 20px; letter-spacing: 2px;">销 售 订 单</h2>
        <el-row style="margin-bottom: 15px; font-size: 14px;">
          <el-col :span="12"><strong>销售单号：</strong>{{ printData.saleOrder.orderNo }}</el-col>
          <el-col :span="12"><strong>销售日期：</strong>{{ parseTime(printData.saleOrder.createTime, '{y}-{m}-{d}') }}</el-col>
        </el-row>
        <el-row style="margin-bottom: 20px; font-size: 14px;">
          <el-col :span="12"><strong>客户：</strong>{{ getCustomerName(printData.saleOrder.customerId) }}</el-col>
          <el-col :span="12"><strong>仓库：</strong>{{ getWarehouseName(printData.saleOrder.warehouseId) }}</el-col>
        </el-row>
        
        <table border="1" cellspacing="0" cellpadding="8" style="width: 100%; border-collapse: collapse; text-align: center; font-size: 14px; border-color: #333;">
          <thead>
            <tr style="background-color: #f5f5f5;">
              <th width="10%">序号</th>
              <th width="30%">商品</th>
              <th width="20%">库位</th>
              <th width="10%">数量</th>
              <th width="15%">单价(元)</th>
              <th width="15%">金额(元)</th>
            </tr>
          </thead>
          <tbody>
            <tr v-for="(item, index) in printData.itemList" :key="index">
              <td>{{ index + 1 }}</td>
              <td>{{ getProductName(item.productId) }}</td>
              <td>{{ getLocationName(item.locationId) }}</td>
              <td>{{ item.quantity }}</td>
              <td>{{ item.price }}</td>
              <td>{{ item.amount }}</td>
            </tr>
          </tbody>
        </table>
        
        <el-row style="margin-top: 15px; font-size: 14px;">
          <el-col :span="24" style="text-align: right; padding-right: 10px;">
            <strong>总数量：{{ printData.saleOrder.totalQty || '0' }} &nbsp;&nbsp;&nbsp; 总金额：<span style="font-size: 18px;">¥ {{ printData.saleOrder.totalAmount || '0.00' }}</span></strong>
          </el-col>
        </el-row>
        <el-row style="margin-top: 30px; font-size: 14px;">
          <el-col :span="8"><strong>制单人：</strong>{{ printData.saleOrder.createBy || '系统管理员' }}</el-col>
          <el-col :span="16"><strong>备注：</strong>{{ printData.saleOrder.remark || '无' }}</el-col>
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

<script setup name="BusinessSaleOrder">
import { getToken } from "@/utils/auth"
import { listSaleOrder, getSaleOrder, addSaleOrder, updateSaleOrder, delSaleOrder, submitSaleOrder, managerAuditSaleOrder, managerRollbackSaleOrder, financeAuditSaleOrder, financeRollbackSaleOrder, cancelSaleOrder, getSaleOrderPrintTemplate, listSaleOrderStatusHistory } from "@/api/business/saleOrder"
import { listSaleOrderItem, getSaleOrderItem, addSaleOrderItem, updateSaleOrderItem, delSaleOrderItem } from "@/api/business/saleOrderItem"
import { listCustomer, getCustomer } from "@/api/business/customer"
import { listWarehouse, getWarehouse } from "@/api/business/warehouse"
import { listProduct, getProduct } from "@/api/business/product"
import { listLocation, getLocation } from "@/api/business/location"
import { appendUniqueSelectOption, buildSelectOptionList, normalizeRemoteKeyword } from "@/utils/remoteSelect"
import { parseTime } from "@/utils/ruoyi"

const { proxy } = getCurrentInstance()

const saleOrderList = ref([])
const open = ref(false)
const loading = ref(true)
const showSearch = ref(true)
const ids = ref([])
const single = ref(true)
const multiple = ref(true)
const total = ref(0)
const selectedStatus = ref("")
const title = ref("")
const saleOrderItemOpen = ref(false)
const saleOrderItemLoading = ref(false)
const saleOrderItemTitle = ref("")
const saleOrderItemList = ref([])
const currentSaleOrderId = ref(undefined)
const currentSaleOrderStatus = ref("")
const selectedSaleOrderItemIds = ref([])
const saleOrderItemMultiple = ref(true)
const saleOrderItemFormOpen = ref(false)
const saleOrderItemFormTitle = ref("")
const statusHistoryOpen = ref(false)
const statusHistoryLoading = ref(false)
const statusHistoryList = ref([])
const printOpen = ref(false)
const printData = ref({
  saleOrder: {},
  itemList: []
})
const upload = reactive({
  open: false,
  title: "",
  isUploading: false,
  updateSupport: 0,
  headers: { Authorization: "Bearer " + getToken() },
  url: import.meta.env.VITE_APP_BASE_API + "/business/saleOrder/importData",
  selectedFile: null
})
const route = useRoute()
const customerList = ref([])
const warehouseList = ref([])
const productList = ref([])
const locationList = ref([])
const customerListLoading = ref(false)
const warehouseListLoading = ref(false)
const productListLoading = ref(false)
const locationListLoading = ref(false)

const saleOrderStatusOptionList = [
  { label: "草稿", value: "draft", type: "info" },
  { label: "已提交", value: "submitted", type: "warning" },
  { label: "经理已审核", value: "manager_approved", type: "primary" },
  { label: "财务已审核", value: "audited", type: "success" },
  { label: "已作废", value: "cancelled", type: "danger" }
]

const saleOrderPaymentStatusOptionList = [
  { label: "未收款", value: "unpaid", type: "danger" },
  { label: "部分收款", value: "partial", type: "warning" },
  { label: "已收款", value: "paid", type: "success" }
]

const data = reactive({
  form: {},
  saleOrderItemForm: {},
  queryParams: {
    pageNum: 1,
    pageSize: 10,
    saleOrderId: undefined,
    orderNo: undefined,
    customerId: undefined,
    status: undefined,
    paymentStatus: undefined
  },
  rules: {
    customerId: [{ required: true, message: "客户不能为空", trigger: "change" }],
    warehouseId: [{ required: true, message: "仓库不能为空", trigger: "change" }],
    totalQty: [{ required: true, message: "总数量不能为空", trigger: "blur" }],
    totalAmount: [{ required: true, message: "总金额不能为空", trigger: "blur" }]
  },
  saleOrderItemRules: {
    productId: [{ required: true, message: "商品不能为空", trigger: "change" }],
    locationId: [{ required: true, message: "库位不能为空", trigger: "change" }],
    batchNo: [{ required: true, message: "批次号不能为空", trigger: "blur" }]
  }
})

const { queryParams, form, rules, saleOrderItemForm, saleOrderItemRules } = toRefs(data)

const canEditSelectedSaleOrder = computed(() => isDraftSaleOrderStatus(selectedStatus.value))
const canDeleteSelectedSaleOrder = computed(() => isDraftSaleOrderStatus(selectedStatus.value))
const canCancelSelectedSaleOrder = computed(() => canCancelSaleOrderByStatus(selectedStatus.value))
const customerSelectOptionList = computed(() => buildCustomerSelectOptionList())
const warehouseSelectOptionList = computed(() => buildWarehouseSelectOptionList())
const productSelectOptionList = computed(() => buildProductSelectOptionList())
const locationSelectOptionList = computed(() => buildLocationSelectOptionList())

// 根据首页和经营看板传入的参数初始化销售订单筛选条件。
function initializeQueryParamsFromRoute() {
  queryParams.value.saleOrderId = route.query.saleOrderId || undefined
  queryParams.value.status = route.query.status || undefined
}

// 初始化客户和仓库远程下拉数据，避免页面初始阶段一次性加载过多基础资料。
function initBasicData() {
  loadCustomerOptionList()
  loadWarehouseOptionList()
  loadProductOptionList()
  loadLocationOptionList()
}

// 远程加载客户下拉选项，按需补充客户名称数据，减少页面初始加载压力。
function loadCustomerOptionList(searchKeyword) {
  customerListLoading.value = true
  listCustomer({
    pageNum: 1,
    pageSize: 20,
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

// 远程加载商品下拉选项，按需补充商品名称数据，减少销售订单明细编辑时的加载压力。
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

// 远程加载库位下拉选项，按需补充库位名称数据，减少销售订单明细编辑时的加载压力。
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

// 按商品编号补充当前页面缺失的商品名称，保证销售订单明细与打印预览的名称回显稳定。
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

// 按库位编号补充当前页面缺失的库位名称，保证销售订单明细与打印预览的名称回显稳定。
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

// 批量补充销售订单列表依赖的客户与仓库名称，保证远程下拉模式下列表展示仍然可读。
function ensureSaleOrderReferenceOptionsLoaded(saleOrderRowList) {
  const customerIdList = [...new Set((saleOrderRowList || []).map(saleOrderRow => saleOrderRow.customerId).filter(customerId => customerId !== undefined && customerId !== null && customerId !== ""))]
  const warehouseIdList = [...new Set((saleOrderRowList || []).map(saleOrderRow => saleOrderRow.warehouseId).filter(warehouseId => warehouseId !== undefined && warehouseId !== null && warehouseId !== ""))]
  return Promise.all([
    ...customerIdList.map(customerId => ensureCustomerOptionLoaded(customerId)),
    ...warehouseIdList.map(warehouseId => ensureWarehouseOptionLoaded(warehouseId))
  ]).catch(() => {})
}

// 批量补充销售订单明细依赖的商品与库位名称，保证明细列表和打印预览仍然可读。
function ensureSaleOrderItemReferenceOptionsLoaded(saleOrderItemRowList) {
  const productIdList = [...new Set((saleOrderItemRowList || []).map(saleOrderItemRow => saleOrderItemRow.productId).filter(productId => productId !== undefined && productId !== null && productId !== ""))]
  const locationIdList = [...new Set((saleOrderItemRowList || []).map(saleOrderItemRow => saleOrderItemRow.locationId).filter(locationId => locationId !== undefined && locationId !== null && locationId !== ""))]
  return Promise.all([
    ...productIdList.map(productId => ensureProductOptionLoaded(productId)),
    ...locationIdList.map(locationId => ensureLocationOptionLoaded(locationId))
  ]).catch(() => {})
}

// 组合客户下拉选项，兼容历史单据中缺失客户主数据时的兜底回显。
function buildCustomerSelectOptionList() {
  return buildSelectOptionList(customerList.value, [queryParams.value.customerId, form.value.customerId], "customerId", "customerName", () => "客户资料缺失")
}

// 组合仓库下拉选项，保证历史销售单缺少主数据时也能在弹窗中正常显示。
function buildWarehouseSelectOptionList() {
  return buildSelectOptionList(warehouseList.value, [queryParams.value.warehouseId, form.value.warehouseId], "warehouseId", "warehouseName", () => "仓库资料缺失")
}

// 组合商品下拉选项，兼容历史销售订单明细中主数据缺失时的兜底回显。
function buildProductSelectOptionList() {
  return buildSelectOptionList(productList.value, [saleOrderItemForm.value.productId], "productId", "productName", () => "商品资料缺失")
}

// 组合库位下拉选项，兼容历史销售订单明细中主数据缺失时的兜底回显。
function buildLocationSelectOptionList() {
  return buildSelectOptionList(locationList.value, [saleOrderItemForm.value.locationId], "locationId", "locationName", () => "库位资料缺失")
}

// 根据客户编号返回客户名称，减少页面出现内部编号带来的识别成本。
function getCustomerName(customerId) {
  const customer = customerList.value.find(customerItem => customerItem.customerId === customerId)
  return customer ? customer.customerName : (customerId ? "客户资料缺失" : "")
}

// 根据仓库编号返回仓库名称，统一列表、表单和打印预览展示口径。
function getWarehouseName(warehouseId) {
  const warehouse = warehouseList.value.find(warehouseItem => warehouseItem.warehouseId === warehouseId)
  return warehouse ? warehouse.warehouseName : (warehouseId ? "仓库资料缺失" : "")
}

// 根据商品编号返回商品名称，帮助用户在销售订单明细和打印预览中快速确认商品。
function getProductName(productId) {
  const product = productList.value.find(productItem => productItem.productId === productId)
  return product ? product.productName : (productId ? "商品资料缺失" : "")
}

// 根据库位编号返回库位名称，统一销售订单明细和打印预览展示口径。
function getLocationName(locationId) {
  const location = locationList.value.find(locationItem => locationItem.locationId === locationId)
  return location ? location.locationName : (locationId ? "库位资料缺失" : "")
}

// 返回销售单据状态中文名称，统一列表、表单和历史记录的展示口径。
function getSaleOrderStatusLabel(statusValue) {
  const saleOrderStatusOption = saleOrderStatusOptionList.find(statusOption => statusOption.value === statusValue)
  return saleOrderStatusOption ? saleOrderStatusOption.label : (statusValue || "未知状态")
}

// 返回销售单据状态标签类型，便于用户快速识别流程阶段。
function getSaleOrderStatusType(statusValue) {
  const saleOrderStatusOption = saleOrderStatusOptionList.find(statusOption => statusOption.value === statusValue)
  return saleOrderStatusOption ? saleOrderStatusOption.type : "info"
}

// 返回销售收款状态中文名称，统一页面展示。
function getSaleOrderPaymentStatusLabel(paymentStatusValue) {
  const paymentStatusOption = saleOrderPaymentStatusOptionList.find(statusOption => statusOption.value === paymentStatusValue)
  return paymentStatusOption ? paymentStatusOption.label : (paymentStatusValue || "未知状态")
}

// 返回销售收款状态标签类型，帮助用户快速区分回款进度。
function getSaleOrderPaymentStatusType(paymentStatusValue) {
  const paymentStatusOption = saleOrderPaymentStatusOptionList.find(statusOption => statusOption.value === paymentStatusValue)
  return paymentStatusOption ? paymentStatusOption.type : "info"
}

// 返回销售状态历史中的操作类型中文名称。
function getSaleOrderOperationTypeLabel(operationTypeValue) {
  const operationTypeMap = {
    submit: "提交",
    manager_audit: "经理审核",
    manager_rollback: "经理回退",
    finance_audit: "财务审核",
    finance_rollback: "财务回退",
    cancel: "作废"
  }
  return operationTypeMap[operationTypeValue] || (operationTypeValue || "未知操作")
}

// 返回销售状态历史中的审核角色中文名称。
function getSaleOrderAuditRoleLabel(auditRoleValue) {
  const auditRoleMap = {
    sales: "销售",
    manager: "经理",
    finance: "财务"
  }
  return auditRoleMap[auditRoleValue] || (auditRoleValue || "未知角色")
}

// 判断当前销售单是否仍处于草稿状态。
function isDraftSaleOrderStatus(statusValue) {
  return statusValue === "draft"
}

// 判断当前销售单是否允许作废。
function canCancelSaleOrderByStatus(statusValue) {
  return !!statusValue && statusValue !== "audited" && statusValue !== "cancelled"
}

// 判断行内是否允许修改销售单，避免已审核单据继续显示可编辑入口。
function canEditSaleOrder(saleOrderRow) {
  return isDraftSaleOrderStatus(saleOrderRow.status)
}

// 判断行内是否允许删除销售单，保持和顶部批量操作一致。
function canDeleteSaleOrder(saleOrderRow) {
  return isDraftSaleOrderStatus(saleOrderRow.status)
}

// 判断当前销售单是否仍有待出库数量，避免已履约完成的订单继续按“待发货”理解。
function hasPendingOutboundQuantity(saleOrderRow) {
  return Number(saleOrderRow?.remainQuantity || 0) > 0
}

// 判断当前销售单是否已经存在出库记录，便于履约完成后仍可回看关联出库单。
function hasCreatedOutboundRecord(saleOrderRow) {
  return Number(saleOrderRow?.outboundQuantity || 0) > 0
}

// 判断当前销售单是否已经进入应收台账阶段，方便销售和财务快速查看对应应收记录。
function canViewReceivable(saleOrderRow) {
  return !!saleOrderRow && (saleOrderRow.status === "audited" || Number(saleOrderRow.receivableAmount || 0) > 0 || Number(saleOrderRow.receivedAmount || 0) > 0)
}

// 判断当前销售单是否已经发生回款，避免在没有回款流水时展示误导性的查看入口。
function canViewReceipt(saleOrderRow) {
  return !!saleOrderRow && Number(saleOrderRow.receivedAmount || 0) > 0
}

// 判断当前销售单是否仍有待收金额，方便业务和财务直接发起回款登记。
function canCreateReceipt(saleOrderRow) {
  return canViewReceivable(saleOrderRow) && Number(saleOrderRow.remainReceivableAmount || 0) > 0
}

// 判断当前销售单是否允许进入销售出库，未出库完成时允许继续出库，已出库完成时保留查看入口。
function canViewOutbound(saleOrderRow) {
  return !!saleOrderRow && saleOrderRow.status === "audited" && (hasPendingOutboundQuantity(saleOrderRow) || hasCreatedOutboundRecord(saleOrderRow))
}

// 返回销售出库入口文案，避免已出库完成的订单继续显示“出库”造成误导。
function getOutboundActionLabel(saleOrderRow) {
  return hasPendingOutboundQuantity(saleOrderRow) ? "出库" : "查看出库"
}

// 格式化销售数量、已出库和待出库字段，避免未出库场景直接显示空白或 undefined。
function formatSaleOrderQuantity(quantityValue) {
  if (quantityValue === undefined || quantityValue === null || quantityValue === "") {
    return "0.00"
  }
  return Number(quantityValue).toFixed(2)
}

// 格式化销售单财务金额字段，避免应收、已收和未收金额直接显示空白或 undefined。
function formatSaleOrderAmount(amountValue) {
  if (amountValue === undefined || amountValue === null || amountValue === "") {
    return "0.00"
  }
  return Number(amountValue).toFixed(2)
}

function getList() {
  loading.value = true
  listSaleOrder(queryParams.value).then(response => {
    saleOrderList.value = response.rows
    total.value = response.total
    ensureSaleOrderReferenceOptionsLoaded(response.rows)
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
    saleOrderId: undefined,
    orderNo: undefined,
    customerId: undefined,
    warehouseId: undefined,
    totalQty: 0,
    totalAmount: 0,
    status: "draft",
    paymentStatus: "unpaid",
    auditBy: undefined,
    auditTime: undefined,
    managerAuditBy: undefined,
    managerAuditTime: undefined,
    remark: undefined
  }
  proxy.resetForm("saleOrderRef")
}

function resetSaleOrderItemForm() {
  saleOrderItemForm.value = {
    saleOrderItemId: undefined,
    saleOrderId: currentSaleOrderId.value,
    productId: undefined,
    locationId: undefined,
    batchNo: undefined,
    quantity: 0,
    price: 0,
    amount: 0
  }
  proxy.resetForm("saleOrderItemRef")
}

function handleQuery() {
  queryParams.value.pageNum = 1
  getList()
}

function resetQuery() {
  proxy.resetForm("queryRef")
  queryParams.value.saleOrderId = undefined
  handleQuery()
}

function handleSelectionChange(selection) {
  ids.value = selection.map(item => item.saleOrderId)
  single.value = selection.length !== 1
  multiple.value = !selection.length
  selectedStatus.value = selection.length === 1 ? selection[0].status : ""
}

function handleAdd() {
  reset()
  open.value = true
  title.value = "新增销售订单"
}

function handleUpdate(row) {
  reset()
  const saleOrderId = row.saleOrderId || ids.value[0]
  getSaleOrder(saleOrderId).then(response => {
    form.value = response.data
    ensureCustomerOptionLoaded(form.value.customerId)
    ensureWarehouseOptionLoaded(form.value.warehouseId)
    open.value = true
    title.value = "修改销售订单"
  })
}

// 打开销售订单明细弹窗时补充当前流程状态，帮助用户判断当前是可维护还是只读查看。
function handleOpenSaleOrderItem(row) {
  currentSaleOrderId.value = row.saleOrderId
  currentSaleOrderStatus.value = row.status
  saleOrderItemTitle.value = "销售订单明细 - " + row.orderNo + "（" + getSaleOrderStatusLabel(row.status) + "）"
  saleOrderItemOpen.value = true
  selectedSaleOrderItemIds.value = []
  saleOrderItemMultiple.value = true
  getSaleOrderItemList()
}

// 从销售单直接跳转到销售出库页面，未出库完的单据会自动进入新增出库流程。
function handleViewOutbound(row) {
  if (!canViewOutbound(row)) {
    return
  }
  const remainQuantityValue = Number(row.remainQuantity || 0)
  proxy.$router.push({
    path: "/sales/outbound",
    query: {
      saleOrderId: row.saleOrderId,
      mode: remainQuantityValue > 0 ? "create" : "list"
    }
  })
}

// 从销售单直接跳回应收台账，方便用户继续登记回款或查看到期信息。
function handleViewReceivable(row) {
  if (!canViewReceivable(row)) {
    return
  }
  proxy.$router.push({
    path: "/finance/receivable",
    query: {
      saleOrderId: row.saleOrderId
    }
  })
}

// 从销售单直接查看回款流水，仅按销售单定位，避免历史客户编号差异把真实回款过滤掉。
function handleViewReceipt(row) {
  if (!canViewReceipt(row)) {
    return
  }
  proxy.$router.push({
    path: "/finance/receipt",
    query: {
      saleOrderId: row.saleOrderId
    }
  })
}

// 从销售单直接跳到应收台账并自动打开回款登记，减少业务和财务来回查找台账的步骤。
function handleCreateReceipt(row) {
  if (!canCreateReceipt(row)) {
    proxy.$modal.msgWarning("当前销售单没有待收金额，不能重复登记回款")
    return
  }
  proxy.$router.push({
    path: "/finance/receivable",
    query: {
      saleOrderId: row.saleOrderId,
      autoOpenReceipt: "1"
    }
  })
}

// 判断当前销售订单明细是否允许继续维护，避免已提交或已审核单据继续改动明细。
function isSaleOrderItemEditable() {
  return currentSaleOrderStatus.value === "draft"
}

// 校验销售订单明细当前是否允许维护，避免只依赖按钮显示状态造成误操作。
function ensureSaleOrderItemEditable(actionLabel) {
  if (!isSaleOrderItemEditable()) {
    proxy.$modal.msgWarning("当前销售订单已进入后续流程，不能" + actionLabel + "明细")
    return false
  }
  return true
}

function getSaleOrderItemList() {
  if (!currentSaleOrderId.value) {
    saleOrderItemList.value = []
    return
  }
  saleOrderItemLoading.value = true
  listSaleOrderItem({
    pageNum: 1,
    pageSize: 1000,
    saleOrderId: currentSaleOrderId.value
  }).then(response => {
    saleOrderItemList.value = response.rows
    ensureSaleOrderItemReferenceOptionsLoaded(response.rows)
    saleOrderItemLoading.value = false
  }).catch(() => {
    saleOrderItemLoading.value = false
  })
}

function handleSaleOrderItemSelectionChange(selection) {
  selectedSaleOrderItemIds.value = selection.map(item => item.saleOrderItemId)
  saleOrderItemMultiple.value = !selection.length
}

// 新增销售订单明细前再次校验主单状态，避免通过其他入口绕过只读限制。
function handleAddSaleOrderItem() {
  if (!ensureSaleOrderItemEditable("新增")) {
    return
  }
  resetSaleOrderItemForm()
  saleOrderItemFormOpen.value = true
  saleOrderItemFormTitle.value = "新增销售订单明细"
}

// 修改销售订单明细前再次校验主单状态，确保非草稿单据只能查看明细。
function handleUpdateSaleOrderItem(row) {
  if (!ensureSaleOrderItemEditable("修改")) {
    return
  }
  resetSaleOrderItemForm()
  const saleOrderItemId = row.saleOrderItemId
  getSaleOrderItem(saleOrderItemId).then(response => {
    saleOrderItemForm.value = response.data
    ensureProductOptionLoaded(saleOrderItemForm.value.productId)
    ensureLocationOptionLoaded(saleOrderItemForm.value.locationId)
    saleOrderItemFormOpen.value = true
    saleOrderItemFormTitle.value = "修改销售订单明细"
  })
}

function cancelSaleOrderItemForm() {
  saleOrderItemFormOpen.value = false
  resetSaleOrderItemForm()
}

// 保存销售订单明细前再次校验主单状态，避免后续流程中的明细被间接修改。
function submitSaleOrderItemForm() {
  if (!ensureSaleOrderItemEditable("保存")) {
    return
  }
  proxy.$refs["saleOrderItemRef"].validate(valid => {
    if (valid) {
      saleOrderItemForm.value.saleOrderId = currentSaleOrderId.value
      if (saleOrderItemForm.value.saleOrderItemId !== undefined) {
        updateSaleOrderItem(saleOrderItemForm.value).then(() => {
          proxy.$modal.msgSuccess("修改成功")
          saleOrderItemFormOpen.value = false
          getSaleOrderItemList()
          getList()
        })
      } else {
        addSaleOrderItem(saleOrderItemForm.value).then(() => {
          proxy.$modal.msgSuccess("新增成功")
          saleOrderItemFormOpen.value = false
          getSaleOrderItemList()
          getList()
        })
      }
    }
  })
}

// 删除销售订单明细前再次校验主单状态，避免非草稿单据的明细被批量删除。
function handleDeleteSaleOrderItem(row) {
  if (!ensureSaleOrderItemEditable("删除")) {
    return
  }
  const saleOrderItemIds = row ? row.saleOrderItemId : selectedSaleOrderItemIds.value
  proxy.$modal.confirm('是否确认删除销售订单明细编号为"' + saleOrderItemIds + '"的数据项？').then(function () {
    return delSaleOrderItem(saleOrderItemIds)
  }).then(() => {
    proxy.$modal.msgSuccess("删除成功")
    getSaleOrderItemList()
    getList()
  }).catch(() => {})
}

function submitForm() {
  proxy.$refs["saleOrderRef"].validate(valid => {
    if (valid) {
      if (form.value.saleOrderId !== undefined) {
        updateSaleOrder(form.value).then(() => {
          proxy.$modal.msgSuccess("修改成功")
          open.value = false
          getList()
        })
      } else {
        addSaleOrder(form.value).then(() => {
          proxy.$modal.msgSuccess("新增成功")
          open.value = false
          getList()
        })
      }
    }
  })
}

function handleSubmit() {
  const saleOrderId = ids.value[0]
  submitSaleOrder(saleOrderId).then(() => {
    proxy.$modal.msgSuccess("提交成功")
    getList()
  })
}

// 经理审核按钮操作
function handleManagerAudit() {
  const saleOrderId = ids.value[0]
  proxy.$modal.prompt("请输入经理审核意见", "系统提示", {
    confirmButtonText: "确定",
    cancelButtonText: "取消",
    inputPlaceholder: "请输入审核意见（选填）",
    closeOnClickModal: false
  }).then(({ value }) => {
    return managerAuditSaleOrder(saleOrderId, { managerAuditComment: value })
  }).then(() => {
    proxy.$modal.msgSuccess("经理审核成功")
    getList()
  }).catch(() => {})
}

// 经理回退按钮操作
function handleManagerRollback() {
  const saleOrderId = ids.value[0]
  proxy.$modal.prompt("请输入经理回退意见", "系统提示", {
    confirmButtonText: "确定",
    cancelButtonText: "取消",
    inputPlaceholder: "请输入回退意见（选填）",
    closeOnClickModal: false
  }).then(({ value }) => {
    return managerRollbackSaleOrder(saleOrderId, { managerAuditComment: value })
  }).then(() => {
    proxy.$modal.msgSuccess("经理回退成功")
    getList()
  }).catch(() => {})
}

// 财务审核按钮操作
function handleFinanceAudit() {
  const saleOrderId = ids.value[0]
  proxy.$modal.prompt("请输入财务审核意见", "系统提示", {
    confirmButtonText: "确定",
    cancelButtonText: "取消",
    inputPlaceholder: "请输入审核意见（选填）",
    closeOnClickModal: false
  }).then(({ value }) => {
    return financeAuditSaleOrder(saleOrderId, { financeAuditComment: value })
  }).then(() => {
    proxy.$modal.msgSuccess("财务审核成功")
    getList()
  }).catch(() => {})
}

// 财务回退按钮操作
function handleFinanceRollback() {
  const saleOrderId = ids.value[0]
  proxy.$modal.prompt("请输入财务回退意见", "系统提示", {
    confirmButtonText: "确定",
    cancelButtonText: "取消",
    inputPlaceholder: "请输入回退意见（选填）",
    closeOnClickModal: false
  }).then(({ value }) => {
    return financeRollbackSaleOrder(saleOrderId, { financeAuditComment: value })
  }).then(() => {
    proxy.$modal.msgSuccess("财务回退成功")
    getList()
  }).catch(() => {})
}

function handleCancel() {
  const saleOrderId = ids.value[0]
  cancelSaleOrder(saleOrderId).then(() => {
    proxy.$modal.msgSuccess("作废成功")
    getList()
  })
}

function handleDelete(row) {
  const saleOrderIds = row.saleOrderId || ids.value
  proxy.$modal.confirm('是否确认删除销售订单编号为"' + saleOrderIds + '"的数据项？').then(function () {
    return delSaleOrder(saleOrderIds)
  }).then(() => {
    getList()
    proxy.$modal.msgSuccess("删除成功")
  }).catch(() => {})
}

function handleExport() {
  proxy.download("business/saleOrder/export", {
    ...queryParams.value
  }, `saleOrder_${new Date().getTime()}.xlsx`)
}

// 导入按钮操作
function handleImport() {
  upload.title = "销售订单导入"
  upload.open = true
  upload.selectedFile = null
}

// 下载导入模板
function importTemplate() {
  proxy.download("business/saleOrder/importTemplate", {}, `saleOrder_template_${new Date().getTime()}.xlsx`)
}

// 文件上传进度
function handleFileUploadProgress() {
  upload.isUploading = true
}

// 文件选择处理
function handleFileChange(file) {
  upload.selectedFile = file
}

// 文件删除处理
function handleFileRemove() {
  upload.selectedFile = null
  upload.isUploading = false
}

// 文件上传成功处理
function handleFileSuccess(response, file) {
  upload.open = false
  upload.isUploading = false
  proxy.$refs.uploadRef.handleRemove(file)
  proxy.$alert(response.msg, "导入结果", { dangerouslyUseHTMLString: true })
  getList()
}

// 提交上传文件
function submitFileForm() {
  const file = upload.selectedFile
  if (!file || file.length === 0 || !file.name.toLowerCase().endsWith('.xls') && !file.name.toLowerCase().endsWith('.xlsx')) {
    proxy.$modal.msgError("请选择后缀为 “xls”或“xlsx”的文件。")
    return
  }
  proxy.$refs.uploadRef.submit()
}

// 打印模板操作前补齐客户、仓库、商品和库位名称，避免预览时先闪出历史编号。
function handlePrintTemplate() {
  const saleOrderId = ids.value[0]
  getSaleOrderPrintTemplate(saleOrderId).then(response => {
    const printTemplateData = response.data || { saleOrder: {}, itemList: [] }
    printData.value = printTemplateData
    return Promise.all([
      ensureCustomerOptionLoaded(printTemplateData.saleOrder.customerId),
      ensureWarehouseOptionLoaded(printTemplateData.saleOrder.warehouseId),
      ensureSaleOrderItemReferenceOptionsLoaded(printTemplateData.itemList)
    ])
  }).then(() => {
    printOpen.value = true
  })
}

// 状态历史按钮操作
function handleStatusHistory() {
  const saleOrderId = ids.value[0]
  statusHistoryOpen.value = true
  statusHistoryLoading.value = true
  listSaleOrderStatusHistory(saleOrderId).then(response => {
    statusHistoryList.value = response.data || []
    statusHistoryLoading.value = false
  }).catch(() => {
    statusHistoryLoading.value = false
  })
}

// 初始化页面筛选条件和基础资料，保证首页、看板重复跳转到销售单页时数据会按最新地址刷新。
async function initializePage() {
  initializeQueryParamsFromRoute()
  await initBasicData()
  await getList()
}

// 监听同一路由下的查询参数变化，避免销售订单页沿用旧的状态或单据筛选。
watch(() => route.fullPath, (currentRouteFullPath, previousRouteFullPath) => {
  if (currentRouteFullPath === previousRouteFullPath) {
    return
  }
  initializePage()
})

initializePage()
</script>
