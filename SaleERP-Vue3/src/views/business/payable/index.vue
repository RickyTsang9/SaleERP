<template>
  <div class="app-container erp-page">
    <erp-page-header
      eyebrow="财务核算 · 应付管理"
      title="应付账款台账"
      subtitle="按采购订单与供应商自动生成应付台账，支持账龄区间筛选、付款核销登记及到期日调整。"
      icon="Tickets"
      tone="amber"
      :badge="`共 ${total} 笔应付`"
    >
      <template #actions>
        <el-button type="success" plain icon="Finished" :disabled="!canBatchPaymentSelected" @click="handleOpenBatchPayment" v-hasPermi="['business:payment:add']">多单合并付款</el-button>
        <el-button type="primary" icon="Download" @click="handleExport" v-hasPermi="['business:payable:export']">导出应付</el-button>
      </template>
    </erp-page-header>

    <erp-stat-strip :items="payableStatItems" :columns="4" />

    <section class="erp-filter-card" v-show="showSearch">
      <el-form :model="queryParams" ref="queryRef" :inline="true" label-width="82px">
        <el-form-item label="采购单号" prop="purchaseOrderNo">
          <el-input v-model="queryParams.purchaseOrderNo" placeholder="请输入采购单号" clearable style="width: 185px" @keyup.enter="handleQuery" />
        </el-form-item>
        <el-form-item label="供应商" prop="supplierId">
          <el-select v-model="queryParams.supplierId" placeholder="请选择供应商" clearable filterable remote reserve-keyword remote-show-suffix :remote-method="loadSupplierOptionList" :loading="supplierListLoading" style="width: 195px">
            <el-option v-for="supplier in supplierSelectOptionList" :key="supplier.supplierId" :label="supplier.supplierName" :value="supplier.supplierId" />
          </el-select>
        </el-form-item>
        <el-form-item label="账龄区间">
          <el-select v-model="queryParams.params.agingBucket" placeholder="全部账龄" clearable style="width: 150px" @change="handleQuery">
            <el-option label="未设置到期日" value="no_due" />
            <el-option label="未到期" value="not_due" />
            <el-option label="逾期1-30天" value="1_30" />
            <el-option label="逾期31-60天" value="31_60" />
            <el-option label="逾期61天以上" value="61_plus" />
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
          <erp-status-tabs :model-value="queryParams.status ?? ''" :items="payableStatusTabItems" @change="handleQuickStatus" />
        </div>
        <div class="erp-toolbar-right">
          <right-toolbar v-model:showSearch="showSearch" @queryTable="getList" />
        </div>
      </div>

      <el-table v-loading="loading" :data="payableList" @selection-change="handleSelectionChange">
        <el-table-column type="selection" width="50" align="center" />
        <el-table-column label="应付 / 来源采购单" align="left" min-width="180">
          <template #default="scope">
            <span class="erp-code-chip">{{ 'AP' + String(scope.row.payableId || '').padStart(5, '0') }}</span>
            <div class="erp-cell-sub">{{ scope.row.purchaseOrderNo || getPurchaseOrderDisplayName(scope.row.purchaseOrderId) }}</div>
          </template>
        </el-table-column>
        <el-table-column label="供应商" align="left" prop="supplierId" min-width="180" show-overflow-tooltip>
          <template #default="scope">
            <div class="erp-cell-title">{{ scope.row.supplierName || getSupplierName(scope.row.supplierId, scope.row.purchaseOrderId) }}</div>
          </template>
        </el-table-column>
        <el-table-column label="应付金额" align="right" prop="amountDue" min-width="120">
          <template #default="scope">
            <span class="erp-amount" :class="{ 'is-danger': Number(scope.row.amountDue || 0) < 0 }">¥{{ Number(scope.row.amountDue || 0).toFixed(2) }}</span>
            <el-tag v-if="Number(scope.row.amountDue || 0) < 0" size="small" type="danger" effect="plain" style="margin-left: 4px">红字</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="已付金额" align="right" prop="amountPaid" min-width="115">
          <template #default="scope">
            <span class="erp-amount is-positive">¥{{ Number(scope.row.amountPaid || 0).toFixed(2) }}</span>
          </template>
        </el-table-column>
        <el-table-column label="未付余额" align="right" prop="remainAmount" min-width="115">
          <template #default="scope">
            <span class="erp-amount" :class="{ 'is-warning': Number(scope.row.remainAmount || 0) !== 0 }">
              ¥{{ Number(scope.row.remainAmount || 0).toFixed(2) }}
            </span>
          </template>
        </el-table-column>
        <el-table-column label="状态" align="center" prop="status" width="105">
          <template #default="scope">
            <el-tag :type="getStatusType(scope.row.status)" effect="light">{{ getStatusName(scope.row.status) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="应付日期" align="center" prop="dueDate" width="125">
          <template #default="scope">
            <span>{{ parseTime(scope.row.dueDate, '{y}-{m}-{d}') || '-' }}</span>
          </template>
        </el-table-column>
        <el-table-column label="操作" align="center" fixed="right" width="275">
          <template #default="scope">
            <el-button link type="primary" icon="View" @click="handleViewPurchaseOrder(scope.row)" v-hasPermi="['business:purchaseOrder:list']" v-if="scope.row.purchaseOrderId">采购单</el-button>
            <el-button
              link
              :type="Number(scope.row.amountDue || 0) < 0 ? 'danger' : 'primary'"
              icon="Money"
              @click="handlePayment(scope.row)"
              v-hasPermi="['business:payment:add']"
              v-if="scope.row.status !== '2'"
            >{{ Number(scope.row.amountDue || 0) < 0 ? '收回退款' : '付款' }}</el-button>
            <el-button link type="primary" icon="Tickets" @click="handleViewPaymentRecord(scope.row)" v-hasPermi="['business:payment:list']" v-if="Number(scope.row.amountPaid || 0) !== 0">流水</el-button>
            <el-button link type="primary" icon="Edit" @click="handleUpdate(scope.row)" v-hasPermi="['business:payable:edit']">调整</el-button>
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

    <!-- 付款 / 红字供应商退款对话框 -->
    <el-dialog :title="isCurrentRedInkPayable ? '登记供应商红字退款' : '付款登记'" v-model="paymentOpen" width="520px" append-to-body>
      <el-alert
        v-if="isCurrentRedInkPayable"
        title="该台账为采购退货/短装结案产生的红字待收供应商退款，确认后将向指定资金账户入账退款并核销红字台账。"
        type="warning"
        :closable="false"
        class="mb8"
      />
      <el-form ref="paymentRef" :model="paymentForm" :rules="paymentRules" label-width="105px">
        <el-form-item :label="isCurrentRedInkPayable ? '退款金额' : '付款金额'" prop="amount">
          <el-input-number v-model="paymentForm.amount" :min="0.01" :max="Math.abs(Number(currentPayable.remainAmount || 0))" :precision="2" style="width: 100%" />
        </el-form-item>
        <el-form-item :label="isCurrentRedInkPayable ? '退款时间' : '付款时间'" prop="paymentTime">
          <el-date-picker clearable
            v-model="paymentForm.paymentTime"
            type="datetime"
            value-format="YYYY-MM-DD HH:mm:ss"
            placeholder="请选择时间"
            style="width: 100%">
          </el-date-picker>
        </el-form-item>
        <el-form-item :label="isCurrentRedInkPayable ? '收款方式' : '付款方式'" prop="paymentMethod">
          <el-select v-model="paymentForm.paymentMethod" placeholder="请选择结算方式" style="width: 100%">
            <el-option label="银行转账" value="bank_transfer" />
            <el-option label="微信支付" value="wechat" />
            <el-option label="支付宝" value="alipay" />
            <el-option label="现金" value="cash" />
          </el-select>
        </el-form-item>
        <el-form-item label="结算账户" prop="accountId">
          <el-select v-model="paymentForm.accountId" placeholder="默认基本结算户" clearable style="width: 100%">
            <el-option
              v-for="acc in accountOptionList"
              :key="acc.accountId"
              :label="`${acc.accountName} (¥${acc.currentBalance})`"
              :value="acc.accountId"
            />
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

    <!-- 多单合并付款核销对话框 -->
    <el-dialog title="多单合并付款核销" v-model="batchPaymentDialog.open" width="540px" append-to-body>
      <el-alert
        :title="`已选中 ${batchPaymentDialog.payableIds.length} 笔同供应商应付单，合计未付余额 ¥${batchPaymentDialog.maxTotalAmount.toFixed(2)}，系统将按单据顺序自动分摊付款。`"
        type="info"
        :closable="false"
        class="mb8"
      />
      <el-form :model="batchPaymentDialog" label-width="110px">
        <el-form-item label="合并付款总额" required>
          <el-input-number v-model="batchPaymentDialog.totalAmount" :min="0.01" :max="batchPaymentDialog.maxTotalAmount" :precision="2" controls-position="right" style="width: 100%" />
        </el-form-item>
        <el-form-item label="付款方式" required>
          <el-select v-model="batchPaymentDialog.paymentMethod" style="width: 100%">
            <el-option label="银行转账" value="bank_transfer" />
            <el-option label="微信支付" value="wechat" />
            <el-option label="支付宝" value="alipay" />
            <el-option label="现金" value="cash" />
          </el-select>
        </el-form-item>
        <el-form-item label="结算账户">
          <el-select v-model="batchPaymentDialog.accountId" placeholder="默认基本结算户" clearable style="width: 100%">
            <el-option v-for="acc in accountOptionList" :key="acc.accountId" :label="`${acc.accountName} (¥${acc.currentBalance})`" :value="acc.accountId" />
          </el-select>
        </el-form-item>
        <el-form-item label="付款时间" required>
          <el-date-picker v-model="batchPaymentDialog.paymentTime" type="datetime" value-format="YYYY-MM-DD HH:mm:ss" style="width: 100%" />
        </el-form-item>
        <el-form-item label="备注">
          <el-input v-model="batchPaymentDialog.remark" placeholder="如：供应商月结一笔合并付款" />
        </el-form-item>
      </el-form>
      <template #footer>
        <div class="dialog-footer">
          <el-button type="primary" :loading="batchPaymentDialog.submitting" @click="submitBatchPayment">确认付款</el-button>
          <el-button @click="batchPaymentDialog.open = false">取 消</el-button>
        </div>
      </template>
    </el-dialog>
  </div>
</template>

<script setup name="Payable">
import { listPayable, getPayable, updatePayable } from "@/api/business/payable";
import { addPayment, batchAddPayment } from "@/api/business/payment";
import { listAccount } from "@/api/business/account";
import { listSupplier, getSupplier } from "@/api/business/supplier";
import { getPurchaseOrder } from "@/api/business/purchaseOrder";
import { appendUniqueSelectOption, buildSelectOptionList, normalizeRemoteKeyword } from "@/utils/remoteSelect";
import { parseTime } from "@/utils/ruoyi";

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
const accountOptionList = ref([]);
const supplierListLoading = ref(false);
const currentPayable = ref({});
const selectedPayableRows = ref([]);
const isCurrentRedInkPayable = computed(() => Number(currentPayable.value?.amountDue || 0) < 0);
const canBatchPaymentSelected = computed(() => {
  const rows = selectedPayableRows.value || [];
  if (rows.length < 2) return false;
  const validRows = rows.filter(r => r.status !== "2" && Number(r.amountDue || 0) > 0 && Number(r.remainAmount || 0) > 0);
  if (validRows.length !== rows.length) return false;
  return new Set(validRows.map(r => r.supplierId)).size === 1;
});
const batchPaymentDialog = reactive({
  open: false,
  submitting: false,
  payableIds: [],
  maxTotalAmount: 0,
  totalAmount: 0,
  paymentMethod: "bank_transfer",
  accountId: undefined,
  paymentTime: "",
  remark: ""
});
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
    params: {
      agingBucket: undefined
    }
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

const payableStatusTabItems = [
  { label: "全部", value: "" },
  { label: "未付款", value: "0", dot: "rose" },
  { label: "部分付款", value: "1", dot: "amber" },
  { label: "已结清", value: "2", dot: "emerald" }
];

const payableStatItems = computed(() => {
  const rows = payableList.value || [];
  const dueSum = rows.reduce((s, r) => s + Number(r.amountDue || 0), 0);
  const paidSum = rows.reduce((s, r) => s + Number(r.amountPaid || 0), 0);
  const remainSum = rows.reduce((s, r) => s + Number(r.remainAmount || 0), 0);
  return [
    { label: "应付单据总数", value: total.value, suffix: "笔", hint: "由采购审批自动入账", icon: "Tickets", tone: "amber" },
    { label: "本页应付总额", prefix: "¥", value: dueSum.toFixed(2), hint: "本页供应商应付款合计", icon: "Money", tone: "indigo" },
    { label: "本页已付金额", prefix: "¥", value: paidSum.toFixed(2), hint: "已核销供应商付款", icon: "CircleCheck", tone: "emerald" },
    { label: "本页待付余额", prefix: "¥", value: remainSum.toFixed(2), hint: "尚未结清的应付尾款", icon: "Warning", tone: "rose" }
  ];
});

function handleQuickStatus(val) {
  queryParams.value.status = val === "" ? undefined : val;
  handleQuery();
}

// 根据首页或付款流水页传入的参数初始化应付台账筛选条件，保证落地页即开即用。
function initializeQueryParamsFromRoute() {
  queryParams.value.purchaseOrderId = route.query.purchaseOrderId || undefined;
  queryParams.value.purchaseOrderNo = route.query.purchaseOrderNo || route.query.orderNo || undefined;
  queryParams.value.supplierId = route.query.supplierId ? Number(route.query.supplierId) : undefined;
  queryParams.value.status = route.query.status || undefined;
  queryParams.value.params.agingBucket = route.query.agingBucket || undefined;
  if (queryParams.value.params.agingBucket) {
    queryParams.value.status = undefined;
  }
}

// 初始化供应商远程下拉数据，避免页面初始阶段一次性加载过多基础资料。
function initData() {
  loadSupplierOptionList();
  listAccount({ pageNum: 1, pageSize: 50, status: "0" }).then(res => {
    accountOptionList.value = res.rows || [];
  }).catch(() => {});
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
  purchaseOrderDisplayNameMap.value[purchaseOrderId] = orderNo || "采购单资料缺失";
}

// 判断当前供应商名称是否已经是可直接展示给用户的正式名称。
function isDirectSupplierNameResolved(supplierName) {
  return !!supplierName && supplierName !== "供应商资料缺失" && !supplierName.startsWith("历史供应商ID：");
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
    syncSupplierOption(supplierId, "供应商资料缺失");
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
      syncSupplierOption(supplierId, "供应商资料缺失");
      return;
    }
    return getSupplier(resolvedSupplierId).then(supplierResponse => {
      const resolvedSupplierName = supplierResponse.data && supplierResponse.data.supplierName ? supplierResponse.data.supplierName : `供应商${resolvedSupplierId}`;
      purchaseOrderSupplierNameMap.value[purchaseOrderId] = resolvedSupplierName;
      syncSupplierOption(supplierId, resolvedSupplierName);
    }).catch(() => {
      syncSupplierOption(supplierId, "供应商资料缺失");
    });
  }).catch(() => {
    syncSupplierOption(supplierId, "供应商资料缺失");
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

// 批量补充应付台账列表依赖的供应商名称，优先复用后端关联字段，消除冗余 N+1 查询。
function ensurePayableReferenceOptionsLoaded(payableRowList) {
  const missingSupplierRows = [];
  const missingPoIds = [];
  (payableRowList || []).forEach(payableRow => {
    syncPurchaseOrderDisplayName(payableRow.purchaseOrderId, payableRow.purchaseOrderNo);
    if (payableRow.purchaseOrderId != null && payableRow.purchaseOrderId !== "" && !payableRow.purchaseOrderNo) {
      missingPoIds.push(payableRow.purchaseOrderId);
    }
    if (payableRow.supplierId != null && payableRow.supplierName) {
      appendUniqueSelectOption(supplierList.value, { supplierId: payableRow.supplierId, supplierName: payableRow.supplierName }, "supplierId");
    } else {
      missingSupplierRows.push(payableRow);
    }
  });
  return Promise.all([
    ...missingSupplierRows.map(payableRow => ensureSupplierOptionLoaded(payableRow.supplierId, payableRow.purchaseOrderId)),
    ...[...new Set(missingPoIds)].map(purchaseOrderId => ensurePurchaseOrderDisplayLoaded(purchaseOrderId))
  ]).catch(() => {});
}

// 组合供应商下拉选项，兼容历史应付台账中主数据缺失时的兜底回显。
function buildSupplierSelectOptionList() {
  return buildSelectOptionList(supplierList.value, [queryParams.value.supplierId], "supplierId", "supplierName", () => "供应商资料缺失");
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
  return supplierId ? "供应商资料缺失" : "";
}

// 根据采购订单编号返回采购单号，优先展示已回补的来源单号。
function getPurchaseOrderDisplayName(purchaseOrderId) {
  if (purchaseOrderId === undefined || purchaseOrderId === null || purchaseOrderId === "") {
    return "未关联采购单";
  }
  return purchaseOrderDisplayNameMap.value[purchaseOrderId] || "采购单资料缺失";
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
    paymentTime: parseTime(new Date(), "{y}-{m}-{d} {h}:{i}:{s}"),
    paymentMethod: "bank_transfer",
    accountId: undefined,
    remark: undefined
  };
  proxy.resetForm("paymentRef");
}

/** 搜索按钮操作 */
function handleQuery() {
  queryParams.value.pageNum = 1;
  getList();
}

function handleViewPaymentRecord(row) {
  if (!row?.payableId) return;
  proxy.$router.push({
    path: "/finance/payment",
    query: { payableId: String(row.payableId) }
  });
}

/** 重置按钮操作 */
function resetQuery() {
  proxy.resetForm("queryRef");
  queryParams.value.purchaseOrderId = undefined;
  queryParams.value.purchaseOrderNo = undefined;
  queryParams.value.status = undefined;
  queryParams.value.params.agingBucket = undefined;
  if (Object.keys(route.query || {}).length > 0) {
    proxy.$router.replace({ path: route.path, query: {} }).catch(() => {});
    return;
  }
  handleQuery();
}

// 多选框选中数据
function handleSelectionChange(selection) {
  selectedPayableRows.value = selection || [];
  ids.value = selection.map(item => item.payableId);
  single.value = selection.length != 1;
}

function handleOpenBatchPayment() {
  const rows = selectedPayableRows.value || [];
  if (!canBatchPaymentSelected.value) {
    proxy.$modal.msgWarning("请勾选至少2笔同一供应商的正向待付款应付台账");
    return;
  }
  const sumRemain = rows.reduce((s, r) => s + Number(r.remainAmount || 0), 0);
  batchPaymentDialog.payableIds = rows.map(r => r.payableId);
  batchPaymentDialog.maxTotalAmount = Number(sumRemain.toFixed(2));
  batchPaymentDialog.totalAmount = Number(sumRemain.toFixed(2));
  batchPaymentDialog.paymentMethod = "bank_transfer";
  batchPaymentDialog.accountId = undefined;
  batchPaymentDialog.paymentTime = parseTime(new Date(), "{y}-{m}-{d} {h}:{i}:{s}");
  batchPaymentDialog.remark = "";
  batchPaymentDialog.open = true;
}

function submitBatchPayment() {
  if (!batchPaymentDialog.totalAmount || Number(batchPaymentDialog.totalAmount) <= 0) {
    proxy.$modal.msgWarning("请输入大于0的合并付款金额");
    return;
  }
  batchPaymentDialog.submitting = true;
  batchAddPayment({
    payableIds: batchPaymentDialog.payableIds,
    totalAmount: batchPaymentDialog.totalAmount,
    paymentMethod: batchPaymentDialog.paymentMethod,
    accountId: batchPaymentDialog.accountId,
    paymentTime: batchPaymentDialog.paymentTime,
    remark: batchPaymentDialog.remark
  }).then(() => {
    proxy.$modal.msgSuccess("多单合并付款核销成功");
    batchPaymentDialog.open = false;
    getList();
  }).finally(() => {
    batchPaymentDialog.submitting = false;
  });
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
  paymentForm.value.amount = Math.abs(Number(row.remainAmount || 0));
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

// 初始化页面筛选条件和供应商下拉，保证从采购订单、付款页重复跳转时应付列表会立即刷新。
async function initializePage() {
  initializeQueryParamsFromRoute();
  await Promise.all([
    Promise.resolve(initData()),
    Promise.resolve(getList())
  ]);
}

// 监听同一路由下的查询参数变化，避免地址栏已切换但应付筛选仍停留在旧值。
watch(() => route.fullPath, (currentRouteFullPath, previousRouteFullPath) => {
  if (currentRouteFullPath === previousRouteFullPath) {
    return;
  }
  initializePage();
});

initializePage();
</script>
