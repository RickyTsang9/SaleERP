<template>
  <div class="app-container">
    <el-form :model="queryParams" ref="queryRef" :inline="true" v-show="showSearch" label-width="90px">
      <el-form-item label="采购单号" prop="purchaseOrderNo">
        <el-input
          v-model="queryParams.purchaseOrderNo"
          placeholder="请输入采购单号"
          clearable
          @keyup.enter="handleQuery"
        />
      </el-form-item>
      <el-form-item label="供应商" prop="supplierId">
        <el-select v-model="queryParams.supplierId" placeholder="请选择供应商" clearable filterable remote reserve-keyword remote-show-suffix :remote-method="loadSupplierOptionList" :loading="supplierListLoading">
          <el-option
            v-for="supplier in supplierSelectOptionList"
            :key="supplier.supplierId"
            :label="supplier.supplierName"
            :value="supplier.supplierId"
          />
        </el-select>
      </el-form-item>
      <el-form-item label="状态" prop="status">
        <el-select v-model="queryParams.status" placeholder="请选择状态" clearable>
          <el-option label="未付款" value="0" />
          <el-option label="部分付款" value="1" />
          <el-option label="已付款" value="2" />
        </el-select>
      </el-form-item>
      <el-form-item>
        <el-button type="primary" icon="Search" @click="handleQuery">搜索</el-button>
        <el-button icon="Refresh" @click="resetQuery">重置</el-button>
      </el-form-item>
    </el-form>

    <el-alert
      title="应付台账由采购订单审核和付款记录自动维护，当前页面仅支持调整应付日期和备注。"
      type="info"
      :closable="false"
      class="mb8"
    />

    <el-row :gutter="10" class="mb8">
      <el-col :span="1.5">
        <el-button
          type="warning"
          plain
          icon="Download"
          @click="handleExport"
          v-hasPermi="['business:payable:export']"
        >导出</el-button>
      </el-col>
      <right-toolbar v-model:showSearch="showSearch" @queryTable="getList"></right-toolbar>
    </el-row>

    <el-table v-loading="loading" :data="payableList" @selection-change="handleSelectionChange">
      <el-table-column type="selection" width="55" align="center" />
      <el-table-column label="应付账款ID" align="center" prop="payableId" />
      <el-table-column label="采购单号" align="center" prop="purchaseOrderId" min-width="180">
        <template #default="scope">
          <span>{{ getPurchaseOrderDisplayName(scope.row.purchaseOrderId) }}</span>
        </template>
      </el-table-column>
      <el-table-column label="供应商" align="center" prop="supplierId">
        <template #default="scope">
          <span>{{ getSupplierName(scope.row.supplierId, scope.row.purchaseOrderId) }}</span>
        </template>
      </el-table-column>
      <el-table-column label="应付金额" align="center" prop="amountDue" />
      <el-table-column label="已付金额" align="center" prop="amountPaid" />
      <el-table-column label="未付金额" align="center" prop="remainAmount" />
      <el-table-column label="状态" align="center" prop="status">
        <template #default="scope">
          <el-tag :type="getStatusType(scope.row.status)">{{ getStatusName(scope.row.status) }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column label="应付日期" align="center" prop="dueDate" width="180">
        <template #default="scope">
          <span>{{ parseTime(scope.row.dueDate, '{y}-{m}-{d}') }}</span>
        </template>
      </el-table-column>
      <el-table-column label="操作" align="center" class-name="small-padding fixed-width" width="260">
        <template #default="scope">
          <el-button link type="primary" icon="View" @click="handleViewPurchaseOrder(scope.row)" v-hasPermi="['business:purchaseOrder:list']">采购单</el-button>
          <el-button link type="primary" icon="Money" @click="handlePayment(scope.row)" v-hasPermi="['business:payment:add']" v-if="scope.row.status !== '2'">付款</el-button>
          <el-button link type="primary" icon="Edit" @click="handleUpdate(scope.row)" v-hasPermi="['business:payable:edit']">调整</el-button>
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

    <!-- 修改应付账款对话框 -->
    <el-dialog :title="title" v-model="open" width="500px" append-to-body>
      <el-alert
        title="当前仅允许调整应付日期和备注，应付金额、已付金额和状态由业务流程自动维护。"
        type="info"
        :closable="false"
        class="mb8"
      />
      <el-form ref="payableRef" :model="form" :rules="rules" label-width="100px">
        <el-form-item label="应付日期" prop="dueDate">
          <el-date-picker clearable
            v-model="form.dueDate"
            type="date"
            value-format="YYYY-MM-DD"
            placeholder="请选择应付日期">
          </el-date-picker>
        </el-form-item>
        <el-form-item label="备注" prop="remark">
          <el-input v-model="form.remark" type="textarea" placeholder="请输入内容" />
        </el-form-item>
      </el-form>
      <template #footer>
        <div class="dialog-footer">
          <el-button type="primary" @click="submitForm">保 存 调 整</el-button>
          <el-button @click="cancel">取 消</el-button>
        </div>
      </template>
    </el-dialog>

    <!-- 付款对话框 -->
    <el-dialog title="付款登记" v-model="paymentOpen" width="500px" append-to-body>
      <el-form ref="paymentRef" :model="paymentForm" :rules="paymentRules" label-width="100px">
        <el-form-item label="付款金额" prop="amount">
          <el-input-number v-model="paymentForm.amount" :min="0.01" :max="currentPayable.remainAmount" :precision="2" style="width: 100%" />
        </el-form-item>
        <el-form-item label="付款时间" prop="paymentTime">
          <el-date-picker clearable
            v-model="paymentForm.paymentTime"
            type="datetime"
            value-format="YYYY-MM-DD HH:mm:ss"
            placeholder="请选择付款时间"
            style="width: 100%">
          </el-date-picker>
        </el-form-item>
        <el-form-item label="付款方式" prop="paymentMethod">
          <el-select v-model="paymentForm.paymentMethod" placeholder="请选择付款方式" style="width: 100%">
            <el-option label="银行转账" value="银行转账" />
            <el-option label="微信" value="微信" />
            <el-option label="支付宝" value="支付宝" />
            <el-option label="现金" value="现金" />
          </el-select>
        </el-form-item>
        <el-form-item label="备注" prop="remark">
          <el-input v-model="paymentForm.remark" type="textarea" placeholder="请输入内容" />
        </el-form-item>
      </el-form>
      <template #footer>
        <div class="dialog-footer">
          <el-button type="primary" @click="submitPaymentForm">确 定</el-button>
          <el-button @click="cancelPayment">取 消</el-button>
        </div>
      </template>
    </el-dialog>
  </div>
</template>

<script setup name="BusinessPayable">
import { listPayable, getPayable, updatePayable } from "@/api/business/payable";
import { addPayment } from "@/api/business/payment";
import { listSupplier, getSupplier } from "@/api/business/supplier";
import { getPurchaseOrder } from "@/api/business/purchaseOrder";
import { appendUniqueSelectOption, buildSelectOptionList, normalizeRemoteKeyword } from "@/utils/remoteSelect";

const { proxy } = getCurrentInstance();
const route = useRoute();

const payableList = ref([]);
const open = ref(false);
const paymentOpen = ref(false);
const loading = ref(true);
const showSearch = ref(true);
const ids = ref([]);
const single = ref(true);
const total = ref(0);
const title = ref("");

const supplierList = ref([]);
const supplierListLoading = ref(false);
const currentPayable = ref({});
const purchaseOrderSupplierNameMap = ref({});
const purchaseOrderDisplayNameMap = ref({});
const supplierSelectOptionList = computed(() => buildSupplierSelectOptionList());

const data = reactive({
  form: {},
  queryParams: {
    pageNum: 1,
    pageSize: 10,
    purchaseOrderId: undefined,
    purchaseOrderNo: undefined,
    supplierId: undefined,
    status: undefined,
  },
  rules: {
  },
  paymentForm: {},
  paymentRules: {
    amount: [
      { required: true, message: "付款金额不能为空", trigger: "blur" }
    ],
    paymentTime: [
      { required: true, message: "付款时间不能为空", trigger: "change" }
    ]
  }
});

const { queryParams, form, rules, paymentForm, paymentRules } = toRefs(data);

// 根据首页或付款流水页传入的参数初始化应付台账筛选条件，保证落地页即开即用。
function initializeQueryParamsFromRoute() {
  if (route.query.purchaseOrderId)
  {
    queryParams.value.purchaseOrderId = route.query.purchaseOrderId;
  }
  if (route.query.purchaseOrderNo)
  {
    queryParams.value.purchaseOrderNo = route.query.purchaseOrderNo;
  }
  if (route.query.orderNo)
  {
    queryParams.value.purchaseOrderNo = route.query.orderNo;
  }
  if (route.query.supplierId)
  {
    queryParams.value.supplierId = Number(route.query.supplierId);
  }
  if (route.query.status)
  {
    queryParams.value.status = route.query.status;
  }
}

// 初始化供应商远程下拉数据，避免页面初始阶段一次性加载过多基础资料。
function initData() {
  loadSupplierOptionList();
}

// 远程加载供应商下拉选项，按需补充供应商名称数据，减少页面初始加载压力。
function loadSupplierOptionList(searchKeyword) {
  supplierListLoading.value = true;
  listSupplier({
    pageNum: 1,
    pageSize: 20,
    supplierName: normalizeRemoteKeyword(searchKeyword)
  }).then(response => {
    const supplierRowList = response.rows || [];
    supplierRowList.forEach(supplierItem => {
      appendUniqueSelectOption(supplierList.value, supplierItem, "supplierId");
    });
  }).finally(() => {
    supplierListLoading.value = false;
  });
}

// 同步供应商下拉选项，允许把历史编号回显更新成更适合用户理解的名称。
function syncSupplierOption(supplierId, supplierName) {
  if (supplierId === undefined || supplierId === null || supplierId === "" || !supplierName) {
    return;
  }
  const supplierIndex = supplierList.value.findIndex(supplierItem => supplierItem.supplierId === supplierId);
  if (supplierIndex >= 0) {
    supplierList.value[supplierIndex] = {
      ...supplierList.value[supplierIndex],
      supplierId: supplierId,
      supplierName: supplierName
    };
    return;
  }
  supplierList.value.push({
    supplierId: supplierId,
    supplierName: supplierName
  });
}

// 同步来源采购单显示名称，保证应付台账优先展示采购单号而不是内部编号。
function syncPurchaseOrderDisplayName(purchaseOrderId, orderNo) {
  if (purchaseOrderId === undefined || purchaseOrderId === null || purchaseOrderId === "") {
    return;
  }
  purchaseOrderDisplayNameMap.value[purchaseOrderId] = orderNo || `历史采购单ID：${purchaseOrderId}`;
}

// 判断当前供应商名称是否已经是可直接展示给用户的正式名称。
function isDirectSupplierNameResolved(supplierName) {
  return !!supplierName && !supplierName.startsWith("历史供应商ID：");
}

// 按采购订单编号补充当前页缺失的采购单号展示，减少台账列表出现内部编号字段。
function ensurePurchaseOrderDisplayLoaded(purchaseOrderId) {
  if (purchaseOrderId === undefined || purchaseOrderId === null || purchaseOrderId === "") {
    return Promise.resolve();
  }
  if (purchaseOrderDisplayNameMap.value[purchaseOrderId]) {
    return Promise.resolve();
  }
  return getPurchaseOrder(purchaseOrderId).then(response => {
    syncPurchaseOrderDisplayName(purchaseOrderId, response.data && response.data.orderNo);
  }).catch(() => {
    syncPurchaseOrderDisplayName(purchaseOrderId);
  });
}

// 按采购订单来源回补历史财务记录的供应商名称，减少页面出现裸供应商编号。
function ensureSupplierOptionLoadedByPurchaseOrder(supplierId, purchaseOrderId) {
  if (!purchaseOrderId) {
    syncSupplierOption(supplierId, `历史供应商ID：${supplierId}`);
    return Promise.resolve();
  }
  const cachedSupplierName = purchaseOrderSupplierNameMap.value[purchaseOrderId];
  if (cachedSupplierName) {
    syncSupplierOption(supplierId, cachedSupplierName);
    return Promise.resolve();
  }
  return getPurchaseOrder(purchaseOrderId).then(response => {
    syncPurchaseOrderDisplayName(purchaseOrderId, response.data && response.data.orderNo);
    const resolvedSupplierId = response.data && response.data.supplierId;
    if (resolvedSupplierId === undefined || resolvedSupplierId === null || resolvedSupplierId === "") {
      syncSupplierOption(supplierId, `历史供应商ID：${supplierId}`);
      return;
    }
    return getSupplier(resolvedSupplierId).then(supplierResponse => {
      const resolvedSupplierName = supplierResponse.data && supplierResponse.data.supplierName ? supplierResponse.data.supplierName : `供应商${resolvedSupplierId}`;
      purchaseOrderSupplierNameMap.value[purchaseOrderId] = resolvedSupplierName;
      syncSupplierOption(supplierId, resolvedSupplierName);
    }).catch(() => {
      syncSupplierOption(supplierId, `历史供应商ID：${supplierId}`);
    });
  }).catch(() => {
    syncSupplierOption(supplierId, `历史供应商ID：${supplierId}`);
  });
}

// 按供应商编号补充当前页面缺失的供应商名称，必要时按来源采购订单回补历史记录名称。
function ensureSupplierOptionLoaded(supplierId, purchaseOrderId) {
  if (supplierId === undefined || supplierId === null || supplierId === "") {
    return Promise.resolve();
  }
  const existingSupplier = supplierList.value.find(supplierItem => supplierItem.supplierId === supplierId);
  if (existingSupplier && isDirectSupplierNameResolved(existingSupplier.supplierName)) {
    return Promise.resolve();
  }
  return getSupplier(supplierId).then(response => {
    syncSupplierOption(response.data.supplierId, response.data.supplierName);
  }).catch(() => {
    return ensureSupplierOptionLoadedByPurchaseOrder(supplierId, purchaseOrderId);
  });
}

// 批量补充应付台账列表依赖的供应商名称，保证远程下拉模式下列表展示仍然可读。
function ensurePayableReferenceOptionsLoaded(payableRowList) {
  const purchaseOrderIdList = [...new Set((payableRowList || []).map(payableRow => payableRow.purchaseOrderId).filter(purchaseOrderId => purchaseOrderId !== undefined && purchaseOrderId !== null && purchaseOrderId !== ""))];
  (payableRowList || []).forEach(payableRow => {
    syncPurchaseOrderDisplayName(payableRow.purchaseOrderId, payableRow.purchaseOrderNo);
  });
  return Promise.all([
    ...(payableRowList || []).map(payableRow => ensureSupplierOptionLoaded(payableRow.supplierId, payableRow.purchaseOrderId)),
    ...purchaseOrderIdList.map(purchaseOrderId => ensurePurchaseOrderDisplayLoaded(purchaseOrderId))
  ]).catch(() => {});
}

// 组合供应商下拉选项，兼容历史应付台账中仍在使用的旧供应商编号回显。
function buildSupplierSelectOptionList() {
  return buildSelectOptionList(supplierList.value, [queryParams.value.supplierId], "supplierId", "supplierName", "历史供应商ID：");
}

// 根据供应商编号和来源采购订单返回供应商名称，优先展示回补后的正式供应商名称。
function getSupplierName(supplierId, purchaseOrderId) {
  const supplier = supplierList.value.find(item => item.supplierId === supplierId);
  if (supplier && supplier.supplierName) {
    return supplier.supplierName;
  }
  const purchaseOrderSupplierName = purchaseOrderSupplierNameMap.value[purchaseOrderId];
  if (purchaseOrderSupplierName) {
    return purchaseOrderSupplierName;
  }
  return supplierId ? `历史供应商ID：${supplierId}` : "";
}

// 根据采购订单编号返回采购单号，优先展示已回补的来源单号。
function getPurchaseOrderDisplayName(purchaseOrderId) {
  if (purchaseOrderId === undefined || purchaseOrderId === null || purchaseOrderId === "") {
    return "未关联采购单";
  }
  return purchaseOrderDisplayNameMap.value[purchaseOrderId] || `历史采购单ID：${purchaseOrderId}`;
}

function getStatusName(status) {
  const statusMap = { '0': '未付款', '1': '部分付款', '2': '已付款' };
  return statusMap[status] || '未知';
}

function getStatusType(status) {
  const typeMap = { '0': 'danger', '1': 'warning', '2': 'success' };
  return typeMap[status] || 'info';
}

/** 查询应付账款列表 */
function getList() {
  if (queryParams.value.purchaseOrderNo)
  {
    queryParams.value.purchaseOrderId = undefined;
  }
  loading.value = true;
  listPayable(queryParams.value).then(response => {
    payableList.value = response.rows;
    total.value = response.total;
    ensurePayableReferenceOptionsLoaded(response.rows);
  }).finally(() => {
    loading.value = false;
  });
}

// 取消按钮
function cancel() {
  open.value = false;
  reset();
}

function cancelPayment() {
  paymentOpen.value = false;
  resetPayment();
}

// 表单重置
function reset() {
  form.value = {
    payableId: undefined,
    dueDate: undefined,
    remark: undefined
  };
  proxy.resetForm("payableRef");
}

function resetPayment() {
  paymentForm.value = {
    paymentId: undefined,
    payableId: undefined,
    purchaseOrderId: undefined,
    purchaseOrderNo: undefined,
    supplierId: undefined,
    amount: undefined,
    paymentTime: new Date(),
    paymentMethod: "银行转账",
    remark: undefined
  };
  proxy.resetForm("paymentRef");
}

/** 搜索按钮操作 */
function handleQuery() {
  queryParams.value.pageNum = 1;
  getList();
}

/** 重置按钮操作 */
function resetQuery() {
  proxy.resetForm("queryRef");
  queryParams.value.purchaseOrderId = undefined;
  queryParams.value.purchaseOrderNo = undefined;
  handleQuery();
}

// 多选框选中数据
function handleSelectionChange(selection) {
  ids.value = selection.map(item => item.payableId);
  single.value = selection.length != 1;
}

// 打开应付台账调整弹窗时补充供应商名称，并明确当前只支持受控调整。
function handleUpdate(row) {
  reset();
  const payableId = row.payableId || ids.value[0];
  getPayable(payableId).then(response => {
    form.value = response.data;
    ensureSupplierOptionLoaded(response.data.supplierId, response.data.purchaseOrderId);
    open.value = true;
    title.value = "调整应付账款";
  });
}

/** 付款按钮操作 */
function handlePayment(row) {
  resetPayment();
  currentPayable.value = row;
  ensureSupplierOptionLoaded(row.supplierId, row.purchaseOrderId);
  paymentForm.value.payableId = row.payableId;
  paymentForm.value.purchaseOrderId = row.purchaseOrderId;
  paymentForm.value.purchaseOrderNo = row.purchaseOrderNo;
  paymentForm.value.supplierId = row.supplierId;
  paymentForm.value.amount = row.remainAmount;
  paymentOpen.value = true;
}

// 跳转到采购订单页，并按采购订单ID精确定位当前应付来源单据。
function handleViewPurchaseOrder(row) {
  proxy.$router.push({
    path: "/purchase/purchaseOrder",
    query: {
      purchaseOrderId: row.purchaseOrderId,
      orderNo: row.purchaseOrderNo
    }
  });
}

// 保存应付调整时仅提交当前台账记录，核心金额与状态由后端按受控规则处理。
function submitForm() {
  if (form.value.payableId === undefined) {
    proxy.$modal.msgWarning("应付台账请通过采购订单审核自动生成");
    return;
  }
  proxy.$refs["payableRef"].validate(valid => {
    if (valid) {
      updatePayable(form.value).then(response => {
        proxy.$modal.msgSuccess("调整成功");
        open.value = false;
        getList();
      });
    }
  });
}

/** 提交付款按钮 */
function submitPaymentForm() {
  proxy.$refs["paymentRef"].validate(valid => {
    if (valid) {
      addPayment(paymentForm.value).then(response => {
        proxy.$modal.msgSuccess("付款成功");
        paymentOpen.value = false;
        getList();
      });
    }
  });
}

/** 导出按钮操作 */
function handleExport() {
  proxy.download('business/payable/export', {
    ...queryParams.value
  }, `payable_${new Date().getTime()}.xlsx`)
}

initializeQueryParamsFromRoute();
initData();
getList();
</script>
