<template>
  <div class="app-container">
    <el-form :model="queryParams" ref="queryRef" :inline="true" v-show="showSearch">
      <el-form-item label="销售单编号" prop="saleOrderId">
        <el-input v-model="queryParams.saleOrderId" placeholder="请输入销售单编号" clearable style="width: 200px" @keyup.enter="handleQuery" />
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
      <el-form-item label="应收状态" prop="status">
        <el-select v-model="queryParams.status" placeholder="请选择应收状态" clearable style="width: 180px">
          <el-option
            v-for="receivableStatusOption in receivableStatusOptionList"
            :key="receivableStatusOption.value"
            :label="receivableStatusOption.label"
            :value="receivableStatusOption.value"
          />
        </el-select>
      </el-form-item>
      <el-form-item>
        <el-button type="primary" icon="Search" @click="handleQuery">搜索</el-button>
        <el-button icon="Refresh" @click="resetQuery">重置</el-button>
      </el-form-item>
    </el-form>

    <el-alert
      v-if="queryParams.params.overdueQuery"
      title="当前仅展示已逾期且未结清的应收台账，点击重置可返回全部列表。"
      type="warning"
      :closable="false"
      class="mb8"
    />

    <el-alert
      title="应收台账由销售单财务审核和回款记录自动维护，当前页面支持登记回款，并仅允许调整到期日期和备注。"
      type="info"
      :closable="false"
      class="mb8"
    />

    <el-row :gutter="10" class="mb8">
      <el-col :span="1.5">
        <el-button type="primary" plain icon="Money" :disabled="!canCreateReceiptForSelected" @click="handleReceipt" v-hasPermi="['business:receipt:add']">回款</el-button>
      </el-col>
      <el-col :span="1.5">
        <el-button type="success" plain icon="Edit" :disabled="single" @click="handleUpdate" v-hasPermi="['business:receivable:edit']">调整</el-button>
      </el-col>
      <el-col :span="1.5">
        <el-button type="warning" plain icon="Download" @click="handleExport" v-hasPermi="['business:receivable:export']">导出</el-button>
      </el-col>
      <right-toolbar v-model:showSearch="showSearch" @queryTable="getList"></right-toolbar>
    </el-row>

    <el-table v-loading="loading" :data="receivableList" @selection-change="handleSelectionChange">
      <el-table-column type="selection" width="55" align="center" />
      <el-table-column label="应收编号" align="center" prop="receivableId" />
      <el-table-column label="销售单编号" align="center" prop="saleOrderId" />
      <el-table-column label="客户" align="center" prop="customerId" min-width="180">
        <template #default="scope">
          <span>{{ getCustomerName(scope.row.customerId, scope.row.saleOrderId) }}</span>
        </template>
      </el-table-column>
      <el-table-column label="应收金额" align="center" prop="amountDue" />
      <el-table-column label="已收金额" align="center" prop="amountPaid" />
      <el-table-column label="未收金额" align="center" prop="remainAmount" />
      <el-table-column label="应收状态" align="center" prop="status">
        <template #default="scope">
          <el-tag :type="getReceivableStatusType(scope.row.status)">{{ getReceivableStatusLabel(scope.row.status) }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column label="到期日期" align="center" prop="dueDate" width="180">
        <template #default="scope">
          <span>{{ parseTime(scope.row.dueDate, '{y}-{m}-{d}') }}</span>
        </template>
      </el-table-column>
      <el-table-column label="操作" width="260" align="center" class-name="small-padding fixed-width">
        <template #default="scope">
          <el-button link type="primary" icon="Money" @click="handleReceipt(scope.row)" v-hasPermi="['business:receipt:add']" v-if="canCreateReceipt(scope.row)">回款</el-button>
          <el-button link type="primary" icon="Tickets" @click="handleViewReceiptRecord(scope.row)" v-hasPermi="['business:receipt:list']" v-if="canViewReceiptRecord(scope.row)">查看回款</el-button>
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

    <el-dialog title="回款登记" v-model="receiptOpen" width="680px" append-to-body>
      <el-alert
        title="回款成功后将自动更新应收台账金额、应收状态和销售单付款状态。"
        type="info"
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
            <el-form-item label="待收金额">
              <el-input-number :model-value="getReceivableRemainAmount(currentReceivable)" :min="0" :precision="2" controls-position="right" style="width: 100%" disabled />
            </el-form-item>
          </el-col>
        </el-row>
        <el-row>
          <el-col :span="12">
            <el-form-item label="本次回款金额" prop="amount">
              <el-input-number v-model="receiptForm.amount" :min="0.01" :max="getReceivableRemainAmount(currentReceivable)" :precision="2" controls-position="right" style="width: 100%" />
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
  </div>
</template>

<script setup name="BusinessReceivable">
import { listReceivable, getReceivable, updateReceivable } from "@/api/business/receivable"
import { addReceipt } from "@/api/business/receipt"
import { listCustomer, getCustomer } from "@/api/business/customer"
import { getSaleOrder } from "@/api/business/saleOrder"
import { appendUniqueSelectOption, buildSelectOptionList, normalizeRemoteKeyword } from "@/utils/remoteSelect"

const { proxy } = getCurrentInstance()
const route = useRoute()
const router = useRouter()

const receivableList = ref([])
const customerList = ref([])
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
const selectedReceivableStatus = ref(undefined)
const selectedReceivableRemainAmount = ref(0)
const autoOpenReceiptHandled = ref(false)
const saleOrderCustomerNameMap = ref({})
const customerSelectOptionList = computed(() => buildCustomerSelectOptionList())
const canCreateReceiptForSelected = computed(() => !single.value && selectedReceivableStatus.value !== "paid" && selectedReceivableRemainAmount.value > 0)

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
    customerId: undefined,
    status: undefined,
    params: {
      overdueQuery: undefined
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

// 根据首页和经营看板传入的参数初始化应收台账筛选条件。
function initializeQueryParamsFromRoute() {
  if (route.query.saleOrderId)
  {
    queryParams.value.saleOrderId = route.query.saleOrderId
  }
  if (route.query.customerId)
  {
    queryParams.value.customerId = Number(route.query.customerId)
  }
  if (route.query.status)
  {
    queryParams.value.status = route.query.status
  }
  if (route.query.overdueQuery)
  {
    queryParams.value.status = undefined
    queryParams.value.params.overdueQuery = route.query.overdueQuery
  }
}

// 初始化客户远程下拉数据，避免页面初始阶段一次性加载过多基础资料。
function initBasicData() {
  loadCustomerOptionList()
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
  return !!customerName && !customerName.startsWith("历史客户ID：") && !customerName.endsWith("（历史单据）")
}

// 兼容已缓存的历史映射后缀，保证页面展示和表单回显都保持简洁可读。
function normalizeCustomerDisplayName(customerName) {
  return customerName ? customerName.replace("（历史单据）", "") : ""
}

// 按销售单来源回补历史财务记录的客户名称，减少页面出现裸客户编号。
function ensureCustomerOptionLoadedBySaleOrder(customerId, saleOrderId) {
  if (!saleOrderId) {
    syncCustomerOption(customerId, `历史客户ID：${customerId}`)
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
      syncCustomerOption(customerId, `历史客户ID：${customerId}`)
      return
    }
    return getCustomer(resolvedCustomerId).then(customerResponse => {
      const resolvedCustomerName = customerResponse.data?.customerName || `客户${resolvedCustomerId}`
      saleOrderCustomerNameMap.value[saleOrderId] = resolvedCustomerName
      syncCustomerOption(customerId, resolvedCustomerName)
    }).catch(() => {
      syncCustomerOption(customerId, `历史客户ID：${customerId}`)
    })
  }).catch(() => {
    syncCustomerOption(customerId, `历史客户ID：${customerId}`)
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

// 批量补充应收台账列表依赖的客户名称，保证远程下拉模式下列表展示仍然可读。
function ensureReceivableReferenceOptionsLoaded(receivableRowList) {
  Promise.all((receivableRowList || []).map(receivableRow => ensureCustomerOptionLoaded(receivableRow.customerId, receivableRow.saleOrderId))).catch(() => {})
}

// 组合客户下拉选项，兼容历史应收台账中仍在使用的旧客户编号回显。
function buildCustomerSelectOptionList() {
  return buildSelectOptionList(customerList.value, [queryParams.value.customerId, form.value.customerId, receiptForm.value.customerId], "customerId", "customerName", "历史客户ID：")
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
  return customerId ? `历史客户ID：${customerId}` : ""
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
    paymentTime: new Date(),
    paymentMethod: "bank_transfer",
    remark: undefined
  }
  proxy.resetForm("receiptRef")
}

function handleQuery() {
  queryParams.value.pageNum = 1
  getList()
}

function resetQuery() {
  proxy.resetForm("queryRef")
  queryParams.value.params.overdueQuery = undefined
  handleQuery()
}

function handleSelectionChange(selection) {
  ids.value = selection.map(item => item.receivableId)
  single.value = selection.length !== 1
  selectedReceivableStatus.value = selection.length === 1 ? selection[0].status : undefined
  selectedReceivableRemainAmount.value = selection.length === 1 ? getReceivableRemainAmount(selection[0]) : 0
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

// 判断当前应收台账是否允许继续登记回款，避免已收清单据重复登记。
function canCreateReceipt(receivableRow) {
  return !!receivableRow && receivableRow.status !== "paid" && getReceivableRemainAmount(receivableRow) > 0
}

// 判断当前应收台账是否已经产生回款流水，方便财务快速查看历史回款记录。
function canViewReceiptRecord(receivableRow) {
  return !!receivableRow && Number(receivableRow.amountPaid || 0) > 0
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
    receiptForm.value.amount = getReceivableRemainAmount(response.data)
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

initializeQueryParamsFromRoute()
initBasicData()
getList()
</script>
