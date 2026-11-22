<template>
  <div class="app-container erp-page">
    <erp-page-header
      eyebrow="财务核算 · 应收管理"
      title="应收账款台账"
      subtitle="实时跟踪客户应收、已收与待收余额，支持账龄区间筛选、一键登记回款与到期日调整。"
      icon="Wallet"
      tone="indigo"
      :badge="`共 ${total} 笔应收`"
    >
      <template #actions>
        <el-button icon="Download" plain @click="handleExport" v-hasPermi="['business:receivable:export']">导出</el-button>
        <el-button type="success" plain icon="Finished" :disabled="!canBatchReceiptSelected" @click="handleOpenBatchReceipt" v-hasPermi="['business:receipt:add']">多单合并核销</el-button>
        <el-button type="primary" icon="Money" :disabled="!canCreateReceiptForSelected" @click="handleReceipt" v-hasPermi="['business:receipt:add']">登记回款</el-button>
      </template>
    </erp-page-header>

    <erp-stat-strip :items="receivableStatItems" :columns="4" />

    <section class="erp-filter-card" v-show="showSearch">
      <el-form :model="queryParams" ref="queryRef" :inline="true">
        <el-form-item label="销售单号" prop="saleOrderNo">
          <el-input v-model="queryParams.saleOrderNo" placeholder="请输入销售单号" clearable style="width: 190px" @keyup.enter="handleQuery" />
        </el-form-item>
        <el-form-item label="客户" prop="customerId">
          <el-select v-model="queryParams.customerId" placeholder="请选择客户" clearable filterable remote reserve-keyword remote-show-suffix :remote-method="loadCustomerOptionList" :loading="customerListLoading" style="width: 195px">
            <el-option v-for="customer in customerSelectOptionList" :key="customer.customerId" :label="customer.customerName" :value="customer.customerId" />
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

    <el-alert v-if="queryParams.params.overdueQuery" title="当前仅展示已逾期且未结清的应收台账，点击重置可返回全部列表。" type="warning" :closable="false" />

    <section class="erp-table-card">
      <div class="erp-toolbar">
        <div class="erp-toolbar-left">
          <erp-status-tabs :model-value="queryParams.status ?? ''" :items="receivableStatusTabItems" @change="handleQuickStatus" />
        </div>
        <div class="erp-toolbar-right">
          <el-button type="success" plain icon="Edit" :disabled="single" @click="handleUpdate" v-hasPermi="['business:receivable:edit']">调整到期日</el-button>
          <right-toolbar v-model:showSearch="showSearch" @queryTable="getList" />
        </div>
      </div>

      <el-table v-loading="loading" :data="receivableList" @selection-change="handleSelectionChange">
        <el-table-column type="selection" width="50" align="center" />
        <el-table-column label="应收 / 来源单" align="left" min-width="180">
          <template #default="scope">
            <span class="erp-code-chip">{{ 'AR' + String(scope.row.receivableId || '').padStart(5, '0') }}</span>
            <div class="erp-cell-sub">
              <el-button
                v-if="scope.row.saleOrderId"
                link
                type="primary"
                size="small"
                style="padding: 0; height: auto; font-size: 12px"
                @click="handleViewSaleOrder(scope.row)"
              >
                {{ scope.row.saleOrderNo || ('SO-' + scope.row.saleOrderId) }}
              </el-button>
              <span v-else>{{ scope.row.saleOrderNo || '-' }}</span>
            </div>
          </template>
        </el-table-column>
        <el-table-column label="往来客户" align="left" prop="customerId" min-width="180" show-overflow-tooltip>
          <template #default="scope">
            <div class="erp-cell-title">{{ scope.row.customerName || getCustomerName(scope.row.customerId, scope.row.saleOrderId) }}</div>
          </template>
        </el-table-column>
        <el-table-column label="应收金额" align="right" prop="amountDue" min-width="120">
          <template #default="scope">
            <span class="erp-amount" :class="{ 'is-danger': Number(scope.row.amountDue || 0) < 0 }">¥{{ Number(scope.row.amountDue || 0).toFixed(2) }}</span>
            <el-tag v-if="Number(scope.row.amountDue || 0) < 0" size="small" type="danger" effect="plain" style="margin-left: 4px">红字</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="已收金额" align="right" prop="amountPaid" min-width="115">
          <template #default="scope">
            <span class="erp-amount is-positive">¥{{ Number(scope.row.amountPaid || 0).toFixed(2) }}</span>
          </template>
        </el-table-column>
        <el-table-column label="未收余额" align="right" prop="remainAmount" min-width="115">
          <template #default="scope">
            <span class="erp-amount" :class="{ 'is-danger': Number(scope.row.remainAmount || 0) !== 0 }">
              ¥{{ Number(scope.row.remainAmount || 0).toFixed(2) }}
            </span>
          </template>
        </el-table-column>
        <el-table-column label="应收状态" align="center" prop="status" width="105">
          <template #default="scope">
            <el-tag :type="getReceivableStatusType(scope.row.status)" effect="light">{{ getReceivableStatusLabel(scope.row.status) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="到期日期" align="center" prop="dueDate" width="125">
          <template #default="scope">
            <span>{{ parseTime(scope.row.dueDate, '{y}-{m}-{d}') || '-' }}</span>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="275" fixed="right" align="center">
          <template #default="scope">
            <el-button link type="primary" icon="View" @click="handleViewSaleOrder(scope.row)" v-hasPermi="['business:saleOrder:list']" v-if="scope.row.saleOrderId">销售单</el-button>
            <el-button
              link
              :type="Number(scope.row.amountDue || 0) < 0 ? 'danger' : 'primary'"
              icon="Money"
              @click="handleReceipt(scope.row)"
              v-hasPermi="['business:receipt:add']"
              v-if="canCreateReceipt(scope.row)"
            >{{ Number(scope.row.amountDue || 0) < 0 ? '办理退款' : '回款' }}</el-button>
            <el-button link type="primary" icon="Tickets" @click="handleViewReceiptRecord(scope.row)" v-hasPermi="['business:receipt:list']" v-if="canViewReceiptRecord(scope.row)">流水</el-button>
            <el-button link type="primary" icon="Edit" @click="handleUpdate(scope.row)" v-hasPermi="['business:receivable:edit']">调整</el-button>
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
        title="当前仅允许调整到期日期和备注，应收金额、已收金额和状态由业务流程自动维护。"
        type="info"
        :closable="false"
        class="mb8"
      />
      <el-form ref="receivableRef" :model="form" :rules="rules" label-width="110px">
        <el-row>
          <el-col :span="12">
            <el-form-item label="销售单编号" prop="saleOrderId">
              <el-input-number v-model="form.saleOrderId" :min="1" controls-position="right" style="width: 100%" disabled />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="客户" prop="customerId">
              <el-select v-model="form.customerId" placeholder="请选择客户" clearable filterable remote reserve-keyword remote-show-suffix :remote-method="loadCustomerOptionList" :loading="customerListLoading" style="width: 100%" disabled>
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
            <el-form-item label="应收金额" prop="amountDue">
              <el-input-number v-model="form.amountDue" :min="0" :precision="2" controls-position="right" style="width: 100%" disabled />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="已收金额" prop="amountPaid">
              <el-input-number v-model="form.amountPaid" :min="0" :precision="2" controls-position="right" style="width: 100%" disabled />
            </el-form-item>
          </el-col>
        </el-row>
        <el-row>
          <el-col :span="12">
            <el-form-item label="应收状态" prop="status">
              <el-select v-model="form.status" placeholder="系统根据金额自动维护状态" disabled style="width: 100%">
                <el-option
                  v-for="receivableStatusOption in receivableStatusOptionList"
                  :key="receivableStatusOption.value"
                  :label="receivableStatusOption.label"
                  :value="receivableStatusOption.value"
                />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="到期日期" prop="dueDate">
              <el-date-picker v-model="form.dueDate" type="date" value-format="YYYY-MM-DD" placeholder="请选择到期日期" style="width: 100%" />
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
          <el-button type="primary" @click="submitForm">保 存 调 整</el-button>
          <el-button @click="cancel">取 消</el-button>
        </div>
      </template>
    </el-dialog>

    <el-dialog :title="isCurrentRedInkRefund ? '红字应收办理客户退款' : '回款登记'" v-model="receiptOpen" width="680px" append-to-body>
      <el-alert
        :title="isCurrentRedInkRefund ? '该台账为销售退货/短装结案产生的红字待退客户款，确认后将从指定资金账户支出退款并同步核销台账。' : '回款成功后将自动更新应收台账金额、应收状态、资金账户余额与销售单付款状态。'"
        :type="isCurrentRedInkRefund ? 'warning' : 'info'"
        :closable="false"
        class="mb8"
      />
      <el-form ref="receiptRef" :model="receiptForm" :rules="receiptRules" label-width="110px">
        <el-row>
          <el-col :span="12">
            <el-form-item label="应收编号" prop="receivableId">
              <el-input-number v-model="receiptForm.receivableId" :min="1" controls-position="right" style="width: 100%" disabled />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="销售单编号" prop="saleOrderId">
              <el-input-number v-model="receiptForm.saleOrderId" :min="1" controls-position="right" style="width: 100%" disabled />
            </el-form-item>
          </el-col>
        </el-row>
        <el-row>
          <el-col :span="12">
            <el-form-item label="客户" prop="customerId">
              <el-select v-model="receiptForm.customerId" placeholder="请选择客户" clearable filterable remote reserve-keyword remote-show-suffix :remote-method="loadCustomerOptionList" :loading="customerListLoading" style="width: 100%" disabled>
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
            <el-form-item :label="isCurrentRedInkRefund ? '待退金额' : '待收金额'">
              <el-input-number :model-value="Math.abs(getReceivableRemainAmount(currentReceivable))" :min="0" :precision="2" controls-position="right" style="width: 100%" disabled />
            </el-form-item>
          </el-col>
        </el-row>
        <el-row>
          <el-col :span="12">
            <el-form-item :label="isCurrentRedInkRefund ? '本次退款金额' : '本次回款金额'" prop="amount">
              <el-input-number v-model="receiptForm.amount" :min="0.01" :max="Math.abs(getReceivableRemainAmount(currentReceivable))" :precision="2" controls-position="right" style="width: 100%" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="回款时间" prop="paymentTime">
              <el-date-picker v-model="receiptForm.paymentTime" type="datetime" value-format="YYYY-MM-DD HH:mm:ss" placeholder="请选择回款时间" style="width: 100%" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-row>
          <el-col :span="12">
            <el-form-item label="回款方式" prop="paymentMethod">
              <el-select v-model="receiptForm.paymentMethod" placeholder="请选择回款方式" style="width: 100%">
                <el-option
                  v-for="paymentMethodOption in receiptPaymentMethodOptionList"
                  :key="paymentMethodOption.value"
                  :label="paymentMethodOption.label"
                  :value="paymentMethodOption.value"
                />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="结算账户" prop="accountId">
              <el-select v-model="receiptForm.accountId" placeholder="默认基本结算户" clearable style="width: 100%">
                <el-option
                  v-for="acc in accountOptionList"
                  :key="acc.accountId"
                  :label="`${acc.accountName} (¥${acc.currentBalance})`"
                  :value="acc.accountId"
                />
              </el-select>
            </el-form-item>
          </el-col>
        </el-row>
        <el-row>
          <el-col :span="24">
            <el-form-item label="备注" prop="remark">
              <el-input v-model="receiptForm.remark" type="textarea" placeholder="请输入备注" />
            </el-form-item>
          </el-col>
        </el-row>
      </el-form>
      <template #footer>
        <div class="dialog-footer">
          <el-button type="primary" @click="submitReceiptForm">确 定</el-button>
          <el-button @click="cancelReceipt">取 消</el-button>
        </div>
      </template>
    </el-dialog>

    <!-- 多单合并回款核销弹窗 -->
    <el-dialog title="多单合并回款核销" v-model="batchReceiptDialog.open" width="540px" append-to-body>
      <el-alert
        :title="`已选中 ${batchReceiptDialog.receivableIds.length} 笔同客户应收单，合计未收余额 ¥${batchReceiptDialog.maxTotalAmount.toFixed(2)}，系统将按单据顺序自动分摊核销。`"
        type="info"
        :closable="false"
        class="mb8"
      />
      <el-form :model="batchReceiptDialog" label-width="110px">
        <el-form-item label="合并收款总额" required>
          <el-input-number v-model="batchReceiptDialog.totalAmount" :min="0.01" :max="batchReceiptDialog.maxTotalAmount" :precision="2" controls-position="right" style="width: 100%" />
        </el-form-item>
        <el-form-item label="回款方式" required>
          <el-select v-model="batchReceiptDialog.paymentMethod" style="width: 100%">
            <el-option v-for="opt in receiptPaymentMethodOptionList" :key="opt.value" :label="opt.label" :value="opt.value" />
          </el-select>
        </el-form-item>
        <el-form-item label="结算账户">
          <el-select v-model="batchReceiptDialog.accountId" placeholder="默认基本结算户" clearable style="width: 100%">
            <el-option v-for="acc in accountOptionList" :key="acc.accountId" :label="`${acc.accountName} (¥${acc.currentBalance})`" :value="acc.accountId" />
          </el-select>
        </el-form-item>
        <el-form-item label="回款时间" required>
          <el-date-picker v-model="batchReceiptDialog.paymentTime" type="datetime" value-format="YYYY-MM-DD HH:mm:ss" style="width: 100%" />
        </el-form-item>
        <el-form-item label="备注">
          <el-input v-model="batchReceiptDialog.remark" placeholder="如：客户月结一笔合并打款" />
        </el-form-item>
      </el-form>
      <template #footer>
        <div class="dialog-footer">
          <el-button type="primary" :loading="batchReceiptDialog.submitting" @click="submitBatchReceipt">确认核销</el-button>
          <el-button @click="batchReceiptDialog.open = false">取 消</el-button>
        </div>
      </template>
    </el-dialog>
  </div>
</template>

<script setup name="BusinessReceivable">
import { listReceivable, getReceivable, updateReceivable } from "@/api/business/receivable"
import { addReceipt, batchAddReceipt } from "@/api/business/receipt"
import { listAccount } from "@/api/business/account"
import { listCustomer, getCustomer } from "@/api/business/customer"
import { getSaleOrder } from "@/api/business/saleOrder"
import { appendUniqueSelectOption, buildSelectOptionList, normalizeRemoteKeyword } from "@/utils/remoteSelect"
import { parseTime } from "@/utils/ruoyi"

const { proxy } = getCurrentInstance()
const route = useRoute()
const router = useRouter()

const receivableList = ref([])
const customerList = ref([])
const accountOptionList = ref([])
const customerListLoading = ref(false)
const open = ref(false)
const receiptOpen = ref(false)
const loading = ref(true)
const showSearch = ref(true)
const ids = ref([])
const single = ref(true)
const total = ref(0)
const title = ref("")
const currentReceivable = ref({})
const selectedReceivableRows = ref([])
const selectedReceivableStatus = ref(undefined)
const selectedReceivableRemainAmount = ref(0)
const autoOpenReceiptHandled = ref(false)
const saleOrderCustomerNameMap = ref({})
const customerSelectOptionList = computed(() => buildCustomerSelectOptionList())
const isCurrentRedInkRefund = computed(() => Number(currentReceivable.value?.amountDue || 0) < 0)
const canCreateReceiptForSelected = computed(() => !single.value && selectedReceivableStatus.value !== "paid" && selectedReceivableRemainAmount.value !== 0)
const canBatchReceiptSelected = computed(() => {
  const rows = selectedReceivableRows.value || []
  if (rows.length < 2) return false
  const validRows = rows.filter(r => r.status !== "paid" && Number(r.amountDue || 0) > 0 && Number(r.remainAmount || 0) > 0)
  if (validRows.length !== rows.length) return false
  const custIds = new Set(validRows.map(r => r.customerId))
  return custIds.size === 1
})
const batchReceiptDialog = reactive({
  open: false,
  submitting: false,
  receivableIds: [],
  maxTotalAmount: 0,
  totalAmount: 0,
  paymentMethod: "bank_transfer",
  accountId: undefined,
  paymentTime: "",
  remark: ""
})

const receivableStatusOptionList = [
  { label: "未收款", value: "unpaid", type: "danger" },
  { label: "部分收款", value: "partial", type: "warning" },
  { label: "已收款", value: "paid", type: "success" }
]

const receiptPaymentMethodOptionList = [
  { label: "银行转账", value: "bank_transfer" },
  { label: "现金", value: "cash" },
  { label: "微信", value: "wechat" },
  { label: "支付宝", value: "alipay" }
]

const data = reactive({
  form: {},
  receiptForm: {},
  queryParams: {
    pageNum: 1,
    pageSize: 10,
    saleOrderId: undefined,
    saleOrderNo: undefined,
    customerId: undefined,
    status: undefined,
    params: {
      overdueQuery: undefined,
      agingBucket: undefined
    }
  },
  rules: {},
  receiptRules: {
    amount: [{ required: true, message: "本次回款金额不能为空", trigger: "blur" }],
    paymentTime: [{ required: true, message: "回款时间不能为空", trigger: "change" }],
    paymentMethod: [{ required: true, message: "回款方式不能为空", trigger: "change" }]
  }
})

const { queryParams, form, rules, receiptForm, receiptRules } = toRefs(data)

const receivableStatusTabItems = [
  { label: "全部", value: "" },
  { label: "未收款", value: "unpaid", dot: "rose" },
  { label: "部分收款", value: "partial", dot: "amber" },
  { label: "已收清", value: "paid", dot: "emerald" }
]

const receivableStatItems = computed(() => {
  const rows = receivableList.value || []
  const dueSum = rows.reduce((s, r) => s + Number(r.amountDue || 0), 0)
  const paidSum = rows.reduce((s, r) => s + Number(r.amountPaid || 0), 0)
  const remainSum = rows.reduce((s, r) => s + Number(r.remainAmount || 0), 0)
  return [
    { label: "应收单据总数", value: total.value, suffix: "笔", hint: "由销售终审自动入账", icon: "Wallet", tone: "indigo" },
    { label: "本页应收总额", prefix: "¥", value: dueSum.toFixed(2), hint: "本页应收金额合计", icon: "Money", tone: "blue" },
    { label: "本页已收金额", prefix: "¥", value: paidSum.toFixed(2), hint: "已核销客户回款总额", icon: "CircleCheck", tone: "emerald" },
    { label: "本页待收余额", prefix: "¥", value: remainSum.toFixed(2), hint: "未结清待催收欠款", icon: "Warning", tone: "rose" }
  ]
})

function handleQuickStatus(val) {
  queryParams.value.status = val === "" ? undefined : val
  handleQuery()
}

// 根据首页和经营看板传入的参数初始化应收台账筛选条件。
function initializeQueryParamsFromRoute() {
  queryParams.value.saleOrderId = route.query.saleOrderId || undefined
  queryParams.value.customerId = route.query.customerId ? Number(route.query.customerId) : undefined
  queryParams.value.status = route.query.status || undefined
  queryParams.value.params.overdueQuery = route.query.overdueQuery || undefined
  queryParams.value.params.agingBucket = route.query.agingBucket || undefined
  if (queryParams.value.params.overdueQuery || queryParams.value.params.agingBucket) {
    queryParams.value.status = undefined
  }
}

// 初始化客户远程下拉数据，避免页面初始阶段一次性加载过多基础资料。
function initBasicData() {
  loadCustomerOptionList()
  listAccount({ pageNum: 1, pageSize: 50, status: "0" }).then(res => {
    accountOptionList.value = res.rows || []
  }).catch(() => {})
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

// 同步客户下拉选项，允许把历史编号回显更新成更适合用户理解的名称。
function syncCustomerOption(customerId, customerName) {
  if (customerId === undefined || customerId === null || customerId === "" || !customerName) {
    return
  }
  const customerIndex = customerList.value.findIndex(customerItem => customerItem.customerId === customerId)
  if (customerIndex >= 0) {
    customerList.value[customerIndex] = {
      ...customerList.value[customerIndex],
      customerId: customerId,
      customerName: customerName
    }
    return
  }
  customerList.value.push({
    customerId: customerId,
    customerName: customerName
  })
}

// 判断当前客户名称是否已经是可直接展示给用户的正式名称。
function isDirectCustomerNameResolved(customerName) {
  return !!customerName && customerName !== "客户资料缺失" && !customerName.startsWith("历史客户ID：") && !customerName.endsWith("（历史单据）")
}

// 兼容已缓存的历史映射后缀，保证页面展示和表单回显都保持简洁可读。
function normalizeCustomerDisplayName(customerName) {
  return customerName ? customerName.replace("（历史单据）", "") : ""
}

// 按销售单来源回补历史财务记录的客户名称，减少页面出现裸客户编号。
function ensureCustomerOptionLoadedBySaleOrder(customerId, saleOrderId) {
  if (!saleOrderId) {
    syncCustomerOption(customerId, "客户资料缺失")
    return Promise.resolve()
  }
  const cachedCustomerName = saleOrderCustomerNameMap.value[saleOrderId]
  if (cachedCustomerName) {
    syncCustomerOption(customerId, cachedCustomerName)
    return Promise.resolve()
  }
  return getSaleOrder(saleOrderId).then(response => {
    const resolvedCustomerId = response.data?.customerId
    if (resolvedCustomerId === undefined || resolvedCustomerId === null || resolvedCustomerId === "") {
      syncCustomerOption(customerId, "客户资料缺失")
      return
    }
    return getCustomer(resolvedCustomerId).then(customerResponse => {
      const resolvedCustomerName = customerResponse.data?.customerName || `客户${resolvedCustomerId}`
      saleOrderCustomerNameMap.value[saleOrderId] = resolvedCustomerName
      syncCustomerOption(customerId, resolvedCustomerName)
    }).catch(() => {
      syncCustomerOption(customerId, "客户资料缺失")
    })
  }).catch(() => {
    syncCustomerOption(customerId, "客户资料缺失")
  })
}

// 按客户编号补充当前页面缺失的客户名称，必要时按来源销售单回补历史记录名称。
function ensureCustomerOptionLoaded(customerId, saleOrderId) {
  if (customerId === undefined || customerId === null || customerId === "") {
    return Promise.resolve()
  }
  const existingCustomer = customerList.value.find(customerItem => customerItem.customerId === customerId)
  if (existingCustomer && isDirectCustomerNameResolved(existingCustomer.customerName)) {
    return Promise.resolve()
  }
  return getCustomer(customerId).then(response => {
    syncCustomerOption(response.data.customerId, response.data.customerName)
  }).catch(() => {
    return ensureCustomerOptionLoadedBySaleOrder(customerId, saleOrderId)
  })
}

// 批量补充应收台账列表依赖的客户名称，优先复用后端关联字段，消除冗余 N+1 查询。
function ensureReceivableReferenceOptionsLoaded(receivableRowList) {
  const missingRows = []
  ;(receivableRowList || []).forEach(receivableRow => {
    if (receivableRow.customerId != null && receivableRow.customerName) {
      appendUniqueSelectOption(customerList.value, {
        customerId: receivableRow.customerId,
        customerName: normalizeCustomerDisplayName(receivableRow.customerName)
      }, "customerId")
    } else {
      missingRows.push(receivableRow)
    }
  })
  Promise.all(missingRows.map(receivableRow => ensureCustomerOptionLoaded(receivableRow.customerId, receivableRow.saleOrderId))).catch(() => {})
}

// 组合客户下拉选项，兼容历史应收台账中主数据缺失时的兜底回显。
function buildCustomerSelectOptionList() {
  return buildSelectOptionList(customerList.value, [queryParams.value.customerId, form.value.customerId, receiptForm.value.customerId], "customerId", "customerName", () => "客户资料缺失")
}

// 根据客户编号和来源销售单返回客户名称，优先展示回补后的正式客户名称。
function getCustomerName(customerId, saleOrderId) {
  const customer = customerList.value.find(customerItem => customerItem.customerId === customerId)
  if (customer && customer.customerName) {
    return normalizeCustomerDisplayName(customer.customerName)
  }
  const saleOrderCustomerName = saleOrderCustomerNameMap.value[saleOrderId]
  if (saleOrderCustomerName) {
    return saleOrderCustomerName
  }
  return customerId ? "客户资料缺失" : ""
}

// 返回应收状态中文名称，统一列表和表单展示口径。
function getReceivableStatusLabel(statusValue) {
  const receivableStatusOption = receivableStatusOptionList.find(statusOption => statusOption.value === statusValue)
  return receivableStatusOption ? receivableStatusOption.label : (statusValue || "未知状态")
}

// 返回应收状态标签类型，帮助用户快速识别回款进度。
function getReceivableStatusType(statusValue) {
  const receivableStatusOption = receivableStatusOptionList.find(statusOption => statusOption.value === statusValue)
  return receivableStatusOption ? receivableStatusOption.type : "info"
}

function getList() {
  loading.value = true
  listReceivable(queryParams.value).then(response => {
    receivableList.value = response.rows
    total.value = response.total
    ensureReceivableReferenceOptionsLoaded(response.rows)
    handleAutoOpenReceipt(response.rows || [])
  }).finally(() => {
    loading.value = false
  })
}

function cancel() {
  open.value = false
  reset()
}

// 关闭回款登记弹窗并重置表单，保证每次登记都以最新台账状态为准。
function cancelReceipt() {
  receiptOpen.value = false
  resetReceipt()
}

function reset() {
  form.value = {
    receivableId: undefined,
    saleOrderId: undefined,
    customerId: undefined,
    amountDue: 0,
    amountPaid: 0,
    status: "unpaid",
    dueDate: undefined,
    remark: undefined
  }
  proxy.resetForm("receivableRef")
}

// 初始化回款登记表单，默认使用系统常用的银行转账方式，减少财务录入成本。
function resetReceipt() {
  currentReceivable.value = {}
  receiptForm.value = {
    receiptId: undefined,
    receivableId: undefined,
    saleOrderId: undefined,
    customerId: undefined,
    amount: undefined,
    paymentTime: parseTime(new Date(), "{y}-{m}-{d} {h}:{i}:{s}"),
    paymentMethod: "bank_transfer",
    accountId: undefined,
    remark: undefined
  }
  proxy.resetForm("receiptRef")
}

function handleQuery() {
  queryParams.value.pageNum = 1
  getList()
}

function handleViewSaleOrder(row) {
  if (!row?.saleOrderId) return
  router.push({
    path: "/sales/saleOrder",
    query: { saleOrderId: String(row.saleOrderId) }
  })
}

function resetQuery() {
  proxy.resetForm("queryRef")
  queryParams.value.saleOrderId = undefined
  queryParams.value.saleOrderNo = undefined
  queryParams.value.status = undefined
  queryParams.value.params.overdueQuery = undefined
  queryParams.value.params.agingBucket = undefined
  if (Object.keys(route.query || {}).length > 0) {
    router.replace({ path: route.path, query: {} }).catch(() => {})
    return
  }
  handleQuery()
}

function handleSelectionChange(selection) {
  selectedReceivableRows.value = selection || []
  ids.value = selection.map(item => item.receivableId)
  single.value = selection.length !== 1
  selectedReceivableStatus.value = selection.length === 1 ? selection[0].status : undefined
  selectedReceivableRemainAmount.value = selection.length === 1 ? getReceivableRemainAmount(selection[0]) : 0
}

function handleOpenBatchReceipt() {
  const rows = selectedReceivableRows.value || []
  if (!canBatchReceiptSelected.value) {
    proxy.$modal.msgWarning("请勾选至少2笔同一客户的正向待收款应收台账")
    return
  }
  const sumRemain = rows.reduce((s, r) => s + Number(r.remainAmount || 0), 0)
  batchReceiptDialog.receivableIds = rows.map(r => r.receivableId)
  batchReceiptDialog.maxTotalAmount = Number(sumRemain.toFixed(2))
  batchReceiptDialog.totalAmount = Number(sumRemain.toFixed(2))
  batchReceiptDialog.paymentMethod = "bank_transfer"
  batchReceiptDialog.accountId = undefined
  batchReceiptDialog.paymentTime = parseTime(new Date(), "{y}-{m}-{d} {h}:{i}:{s}")
  batchReceiptDialog.remark = ""
  batchReceiptDialog.open = true
}

function submitBatchReceipt() {
  if (!batchReceiptDialog.totalAmount || Number(batchReceiptDialog.totalAmount) <= 0) {
    proxy.$modal.msgWarning("请输入大于0的合并核销金额")
    return
  }
  batchReceiptDialog.submitting = true
  batchAddReceipt({
    receivableIds: batchReceiptDialog.receivableIds,
    totalAmount: batchReceiptDialog.totalAmount,
    paymentMethod: batchReceiptDialog.paymentMethod,
    accountId: batchReceiptDialog.accountId,
    paymentTime: batchReceiptDialog.paymentTime,
    remark: batchReceiptDialog.remark
  }).then(() => {
    proxy.$modal.msgSuccess("多单合并回款核销成功")
    batchReceiptDialog.open = false
    getList()
  }).finally(() => {
    batchReceiptDialog.submitting = false
  })
}

// 打开应收台账调整弹窗时回填客户名称，并明确当前只支持受控调整。
function handleUpdate(row) {
  reset()
  const receivableId = row.receivableId || ids.value[0]
  getReceivable(receivableId).then(response => {
    form.value = response.data
    ensureCustomerOptionLoaded(form.value.customerId, form.value.saleOrderId)
    open.value = true
    title.value = "调整应收台账"
  })
}

// 返回当前应收台账的剩余待收金额，统一处理历史字符串金额和空值场景。
function getReceivableRemainAmount(receivableRow) {
  const remainAmountValue = Number(receivableRow?.remainAmount || 0)
  return Number.isFinite(remainAmountValue) ? remainAmountValue : 0
}

// 判断当前应收台账是否允许继续登记回款/办理红字退款，避免已结清单据重复登记。
function canCreateReceipt(receivableRow) {
  if (!receivableRow || receivableRow.status === "paid") return false
  const remain = getReceivableRemainAmount(receivableRow)
  return Number(receivableRow.amountDue || 0) < 0 ? remain < 0 : remain > 0
}

// 判断当前应收台账是否已经产生回款/退款流水，方便财务快速查看历史记录。
function canViewReceiptRecord(receivableRow) {
  return !!receivableRow && Number(receivableRow.amountPaid || 0) !== 0
}

// 清理自动打开回款登记的路由参数，避免刷新或重复查询后反复弹出回款窗口。
function clearAutoOpenReceiptQuery() {
  if (!route.query.autoOpenReceipt) {
    return
  }
  const nextQuery = { ...route.query }
  delete nextQuery.autoOpenReceipt
  router.replace({
    path: route.path,
    query: nextQuery
  })
}

// 从销售订单直达应收台账时自动打开回款登记，减少用户二次查找和重复点击。
function handleAutoOpenReceipt(receivableRowList) {
  if (!route.query.autoOpenReceipt || autoOpenReceiptHandled.value) {
    return
  }
  autoOpenReceiptHandled.value = true
  const availableReceivable = (receivableRowList || []).find(receivableRow => canCreateReceipt(receivableRow))
  clearAutoOpenReceiptQuery()
  if (!availableReceivable) {
    if (!receivableRowList || receivableRowList.length === 0) {
      proxy.$modal.msgWarning("当前销售单还没有可登记回款的应收台账")
      return
    }
    proxy.$modal.msgWarning("当前销售单没有待收金额，不能重复登记回款")
    return
  }
  handleReceipt(availableReceivable)
}

// 从应收台账直接发起回款登记，避免财务在流水页手工录入多个编号。
function handleReceipt(row) {
  resetReceipt()
  const receivableId = row?.receivableId || ids.value[0]
  if (receivableId === undefined) {
    proxy.$modal.msgWarning("请先选择需要登记回款的应收台账")
    return
  }
  getReceivable(receivableId).then(response => {
    if (!canCreateReceipt(response.data)) {
      proxy.$modal.msgWarning("当前应收台账没有待收金额，不能重复登记回款")
      return
    }
    currentReceivable.value = response.data
    receiptForm.value.receivableId = response.data.receivableId
    receiptForm.value.saleOrderId = response.data.saleOrderId
    receiptForm.value.customerId = response.data.customerId
    receiptForm.value.amount = Math.abs(getReceivableRemainAmount(response.data))
    ensureCustomerOptionLoaded(response.data.customerId, response.data.saleOrderId)
    receiptOpen.value = true
  })
}

// 从应收台账直接查看回款流水，方便财务核对每次回款明细。
function handleViewReceiptRecord(row) {
  if (!canViewReceiptRecord(row)) {
    proxy.$modal.msgWarning("当前应收台账还没有回款记录")
    return
  }
  proxy.$router.push({
    path: "/finance/receipt",
    query: {
      receivableId: row.receivableId,
      saleOrderId: row.saleOrderId,
      customerId: row.customerId
    }
  })
}

// 保存应收调整时仅提交当前台账记录，核心金额与状态由后端按受控规则处理。
function submitForm() {
  if (form.value.receivableId === undefined) {
    proxy.$modal.msgWarning("应收台账请通过销售单财务审核自动生成")
    return
  }
  proxy.$refs["receivableRef"].validate(valid => {
    if (valid) {
      updateReceivable(form.value).then(() => {
        proxy.$modal.msgSuccess("调整成功")
        open.value = false
        getList()
      })
    }
  })
}

// 提交回款登记后刷新应收台账，保证金额和状态立即回到最新口径。
function submitReceiptForm() {
  proxy.$refs["receiptRef"].validate(valid => {
    if (valid) {
      addReceipt(receiptForm.value).then(() => {
        proxy.$modal.msgSuccess("回款登记成功")
        receiptOpen.value = false
        getList()
      })
    }
  })
}

function handleExport() {
  proxy.download("business/receivable/export", {
    ...queryParams.value
  }, `receivable_${new Date().getTime()}.xlsx`)
}

// 初始化页面筛选条件和客户下拉，保证从首页、经营看板和销售订单重复跳转时数据会正确刷新。
async function initializePage() {
  autoOpenReceiptHandled.value = false
  initializeQueryParamsFromRoute()
  await Promise.all([
    Promise.resolve(initBasicData()),
    Promise.resolve(getList())
  ])
}

// 监听同一路由下的查询参数变化，保证更换销售单或客户后自动回款弹窗和列表筛选同步更新。
watch(() => route.fullPath, (currentRouteFullPath, previousRouteFullPath) => {
  if (currentRouteFullPath === previousRouteFullPath) {
    return
  }
  initializePage()
})

initializePage()
</script>
