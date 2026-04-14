<template>
  <div class="app-container">
    <el-form :model="queryParams" ref="queryRef" :inline="true" v-show="showSearch">
      <el-form-item label="出库单号" prop="outboundNo">
        <el-input v-model="queryParams.outboundNo" placeholder="请输入出库单号" clearable style="width: 220px" @keyup.enter="handleQuery" />
      </el-form-item>
      <el-form-item label="出库类型" prop="outboundType">
        <el-select v-model="queryParams.outboundType" placeholder="请选择出库类型" clearable style="width: 180px">
          <el-option
            v-for="outboundTypeOption in outboundTypeOptionList"
            :key="outboundTypeOption.value"
            :label="outboundTypeOption.label"
            :value="outboundTypeOption.value"
          />
        </el-select>
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
        <el-select v-model="queryParams.status" placeholder="请选择状态" clearable style="width: 180px">
          <el-option
            v-for="outboundStatusOption in outboundStatusOptionList"
            :key="outboundStatusOption.value"
            :label="outboundStatusOption.label"
            :value="outboundStatusOption.value"
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
        <el-button type="primary" plain icon="Plus" @click="handleAdd" v-hasPermi="['business:outbound:add']">新增</el-button>
      </el-col>
      <el-col :span="1.5">
        <el-button type="success" plain icon="Edit" :disabled="!canUpdateSelected" @click="handleUpdate" v-hasPermi="['business:outbound:edit']">修改</el-button>
      </el-col>
      <el-col :span="1.5">
        <el-button type="danger" plain icon="Delete" :disabled="!canDeleteSelected" @click="handleDelete" v-hasPermi="['business:outbound:remove']">删除</el-button>
      </el-col>
      <el-col :span="1.5">
        <el-button type="warning" plain icon="Download" @click="handleExport" v-hasPermi="['business:outbound:export']">导出</el-button>
      </el-col>
      <right-toolbar v-model:showSearch="showSearch" @queryTable="getList"></right-toolbar>
    </el-row>

    <el-table v-loading="loading" :data="outboundList" @selection-change="handleSelectionChange">
      <el-table-column type="selection" width="55" align="center" />
      <el-table-column label="出库编号" align="center" prop="outboundId" />
      <el-table-column label="出库单号" align="center" prop="outboundNo" min-width="180" />
      <el-table-column label="出库类型" align="center" prop="outboundType" min-width="120">
        <template #default="scope">
          <span>{{ getOutboundTypeLabel(scope.row.outboundType) }}</span>
        </template>
      </el-table-column>
      <el-table-column label="来源销售单" align="center" prop="saleOrderNo" min-width="180">
        <template #default="scope">
          <span>{{ getSaleOrderDisplayName(scope.row.saleOrderNo, scope.row.saleOrderId) }}</span>
        </template>
      </el-table-column>
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
      <el-table-column label="状态" align="center" prop="status" min-width="120">
        <template #default="scope">
          <el-tag :type="getOutboundStatusType(scope.row.status)">{{ getOutboundStatusLabel(scope.row.status) }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column label="配送状态" align="center" prop="deliveryStatus" min-width="120">
        <template #default="scope">
          <el-tag :type="getOutboundDeliveryStatusType(scope.row.deliveryStatus)">{{ getOutboundDeliveryStatusLabel(scope.row.deliveryStatus) }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column label="创建时间" align="center" prop="createTime" width="180">
        <template #default="scope">
          <span>{{ parseTime(scope.row.createTime) }}</span>
        </template>
      </el-table-column>
      <el-table-column label="操作" width="420" align="center" class-name="small-padding fixed-width">
        <template #default="scope">
          <el-button link type="primary" icon="List" @click="handleOpenOutboundItem(scope.row)" v-hasPermi="['business:outbound:query']">明细</el-button>
          <el-button link type="primary" icon="Tickets" @click="handleViewSaleOrder(scope.row)" v-hasPermi="['business:saleOrder:list']" v-if="scope.row.saleOrderId">销售单</el-button>
          <el-button link type="primary" icon="Edit" @click="handleUpdate(scope.row)" v-hasPermi="['business:outbound:edit']" v-if="scope.row.status === 'draft'">修改</el-button>
          <el-button link type="primary" icon="Upload" @click="handleSubmit(scope.row)" v-hasPermi="['business:outbound:edit']" v-if="scope.row.status === 'draft'">提交</el-button>
          <el-button link type="success" icon="CircleCheck" @click="handleAudit(scope.row)" v-hasPermi="['business:outbound:audit']" v-if="scope.row.status === 'submitted'">审核</el-button>
          <el-button link type="warning" icon="Close" @click="handleCancel(scope.row)" v-hasPermi="['business:outbound:edit']" v-if="scope.row.status !== 'audited' && scope.row.status !== 'cancelled'">作废</el-button>
          <el-button link type="primary" icon="Delete" @click="handleDelete(scope.row)" v-hasPermi="['business:outbound:remove']" v-if="scope.row.status === 'draft'">删除</el-button>
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
            <el-form-item label="来源销售单" prop="saleOrderNo">
              <el-input v-model="form.saleOrderNo" placeholder="未关联销售单" disabled />
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
            <el-form-item label="平台类型" prop="platformType">
              <el-input v-model="form.platformType" placeholder="请输入平台类型" :disabled="!isOutboundFormEditable()" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="店铺编号" prop="storeId">
              <el-input-number v-model="form.storeId" :min="0" controls-position="right" style="width: 100%" :disabled="!isOutboundFormEditable()" />
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
              <el-select v-model="outboundItemForm.productId" placeholder="请选择商品" clearable filterable remote reserve-keyword remote-show-suffix :remote-method="loadProductOptionList" :loading="productListLoading" style="width: 100%" :disabled="!!outboundItemForm.saleOrderItemId">
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
  </div>
</template>

<script setup name="BusinessOutbound">
import { listOutbound, getOutbound, addOutbound, updateOutbound, delOutbound, submitOutbound, auditOutbound, cancelOutbound } from "@/api/business/outbound"
import { listOutboundItem, getOutboundItem, addOutboundItem, updateOutboundItem, delOutboundItem } from "@/api/business/outboundItem"
import { getSaleOrder } from "@/api/business/saleOrder"
import { listCustomer, getCustomer } from "@/api/business/customer"
import { listWarehouse, getWarehouse } from "@/api/business/warehouse"
import { listProduct, getProduct } from "@/api/business/product"
import { listLocation, getLocation } from "@/api/business/location"
import { appendUniqueSelectOption, buildSelectOptionList, normalizeRemoteKeyword } from "@/utils/remoteSelect"
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
const canDeleteSelected = ref(false)
const total = ref(0)
const title = ref("")
const outboundItemOpen = ref(false)
const outboundItemLoading = ref(false)
const outboundItemTitle = ref("")
const outboundItemList = ref([])
const currentOutboundId = ref(undefined)
const currentOutboundStatus = ref(undefined)
const currentOutboundSaleOrderId = ref(undefined)
const selectedOutboundItemIds = ref([])
const outboundItemMultiple = ref(true)
const outboundItemFormOpen = ref(false)
const outboundItemFormTitle = ref("")
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
  { label: "配送中", value: "shipped", type: "warning" },
  { label: "已配送", value: "delivered", type: "success" }
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
    batchNo: [{ required: true, message: "批次号不能为空", trigger: "blur" }]
  }
})

const { queryParams, form, rules, outboundItemForm, outboundItemRules } = toRefs(data)
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
  return form.value.deliveryStatus === "shipped" || form.value.deliveryStatus === "delivered"
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

// 远程加载库位下拉选项，按需补充库位名称数据，减少页面初始加载压力。
function loadLocationOptionList(searchKeyword) {
  locationListLoading.value = true
  return listLocation({
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

// 批量补充出库主单依赖的客户与仓库名称，保证远程下拉模式下列表展示仍然可读。
function ensureOutboundReferenceOptionsLoaded(outboundRowList) {
  const customerIdList = [...new Set((outboundRowList || []).map(outboundRow => outboundRow.customerId).filter(customerId => customerId !== undefined && customerId !== null && customerId !== ""))]
  const warehouseIdList = [...new Set((outboundRowList || []).map(outboundRow => outboundRow.warehouseId).filter(warehouseId => warehouseId !== undefined && warehouseId !== null && warehouseId !== ""))]
  Promise.all([
    ...customerIdList.map(customerId => ensureCustomerOptionLoaded(customerId)),
    ...warehouseIdList.map(warehouseId => ensureWarehouseOptionLoaded(warehouseId))
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
  return buildSelectOptionList(locationList.value, [outboundItemForm.value.locationId], "locationId", "locationName", () => "库位资料缺失")
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
  const deliveryStatusOption = deliveryStatusOptionList.find(statusOption => statusOption.value === deliveryStatusValue)
  return deliveryStatusOption ? deliveryStatusOption.label : deliveryStatusValue
}

// 返回配送状态标签类型，帮助用户快速识别履约进度。
function getOutboundDeliveryStatusType(deliveryStatusValue) {
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

// 生成来源销售明细的进度提示，让仓库和物流在编辑时能直接看到销售数量和剩余可发数量。
function getOutboundItemSourceProgressText(targetOutboundItem) {
  return `来源销售明细已带入：销售数量 ${formatOutboundItemQuantity(targetOutboundItem.saleQuantity)}，已出库 ${formatOutboundItemQuantity(targetOutboundItem.shippedQuantity)}，待出库 ${formatOutboundItemQuantity(targetOutboundItem.remainQuantity)}。`
}

function getList() {
  loading.value = true
  return listOutbound(queryParams.value).then(response => {
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
    quantity: 0,
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
  proxy.resetForm("queryRef")
  queryParams.value.saleOrderId = undefined
  handleQuery()
}

function handleSelectionChange(selection) {
  ids.value = selection.map(item => item.outboundId)
  single.value = selection.length !== 1
  multiple.value = !selection.length
  canUpdateSelected.value = selection.length === 1 && isDraftOutboundStatus(selection[0].status)
  canDeleteSelected.value = selection.length > 0 && selection.every(selectionItem => isDraftOutboundStatus(selectionItem.status))
}

async function handleAdd() {
  reset()
  await applyRouteSaleOrderToForm()
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
  outboundItemTitle.value = "出库明细 - " + row.outboundNo + "（" + getOutboundStatusLabel(row.status) + " / " + getOutboundDeliveryStatusLabel(row.deliveryStatus) + "）"
  outboundItemOpen.value = true
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

function handleSubmit(row) {
  proxy.$modal.confirm('是否确认提交出库单号为"' + row.outboundNo + '"的数据项？').then(function () {
    return submitOutbound(row.outboundId)
  }).then(() => {
    proxy.$modal.msgSuccess("提交成功")
    getList()
  }).catch(() => {})
}

function handleAudit(row) {
  proxy.$modal.confirm('是否确认审核出库单号为"' + row.outboundNo + '"的数据项？').then(function () {
    return auditOutbound(row.outboundId)
  }).then(() => {
    proxy.$modal.msgSuccess("审核成功")
    getList()
  }).catch(() => {})
}

function handleCancel(row) {
  proxy.$modal.confirm('是否确认作废出库单号为"' + row.outboundNo + '"的数据项？').then(function () {
    return cancelOutbound(row.outboundId)
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

// 判断当前是否来自销售订单的一键新建出库入口，满足条件时直接打开新增弹窗。
function shouldAutoOpenLinkedOutboundFromRoute() {
  return hasAutoCreateRouteMode() && outboundList.value.length === 0
}

// 初始化页面基础数据和列表，并在需要时自动打开来源销售单的新建出库弹窗。
async function initializePage() {
  initializeQueryParamsFromRoute()
  await initBasicData()
  await getList()
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

initializePage()
</script>
