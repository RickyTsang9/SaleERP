<template>
  <div class="app-container erp-page">
    <erp-page-header
      eyebrow="运营协同"
      title="统一待办中心"
      subtitle="实时汇总销售审批、采购入库、库存预警、财务逾期及客户跟进事项，点击直达业务页面闭环处理。"
      icon="Bell"
      tone="indigo"
      :badge="`共 ${summaryData.totalCount || 0} 项`"
    >
      <template #actions>
        <el-button type="primary" icon="RefreshRight" :loading="loading" @click="getList">刷新待办</el-button>
      </template>
    </erp-page-header>

    <erp-stat-strip :items="summaryStatItems" :columns="4" @select="handleStatCardSelect" />

    <section class="erp-filter-card" v-show="showSearch">
      <el-form :model="queryParams" ref="queryRef" :inline="true">
        <el-form-item label="待办类型" prop="todoType">
          <el-select v-model="queryParams.todoType" placeholder="全部类型" clearable style="width: 210px">
            <el-option
              v-for="todoTypeOption in todoTypeOptionList"
              :key="todoTypeOption.value"
              :label="todoTypeOption.label"
              :value="todoTypeOption.value"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="优先级" prop="priorityLevel">
          <el-select v-model="queryParams.priorityLevel" placeholder="全部优先级" clearable style="width: 150px">
            <el-option label="紧急" value="urgent" />
            <el-option label="高" value="high" />
            <el-option label="普通" value="normal" />
          </el-select>
        </el-form-item>
        <el-form-item label="关键词" prop="keyword">
          <el-input v-model="queryParams.keyword" placeholder="标题、单号、往来对象" clearable style="width: 220px" @keyup.enter="handleQuery" />
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
          <erp-status-tabs
            :model-value="queryParams.priorityLevel || ''"
            :items="priorityTabItems"
            @change="handlePriorityTabChange"
          />
        </div>
        <div class="erp-toolbar-right">
          <right-toolbar v-model:showSearch="showSearch" @queryTable="getList" />
        </div>
      </div>

      <el-table v-loading="loading" :data="filteredTodoList">
        <el-table-column label="优先级" align="center" prop="priorityLevel" width="90">
          <template #default="scope">
            <el-tag :type="getPriorityTagType(scope.row.priorityLevel)" effect="light">
              {{ getPriorityLabel(scope.row.priorityLevel) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="待办事项" align="left" prop="todoTitle" min-width="220" show-overflow-tooltip>
          <template #default="scope">
            <div class="erp-cell-title">{{ scope.row.todoTitle }}</div>
          </template>
        </el-table-column>
        <el-table-column label="业务类型" align="center" prop="todoType" width="124">
          <template #default="scope">
            <el-tag size="small" type="info" effect="plain">{{ getTodoTypeLabel(scope.row.todoType) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="单号/编号" align="center" prop="sourceNo" min-width="135" show-overflow-tooltip>
          <template #default="scope">
            <span class="erp-code-chip">{{ scope.row.sourceNo || '-' }}</span>
          </template>
        </el-table-column>
        <el-table-column label="关联对象" align="center" prop="targetName" min-width="130" show-overflow-tooltip />
        <el-table-column label="金额/数量" align="right" prop="todoAmount" width="115">
          <template #default="scope">
            <span class="erp-amount">{{ formatTodoAmount(scope.row) }}</span>
          </template>
        </el-table-column>
        <el-table-column label="未结金额" align="right" prop="remainAmount" width="110">
          <template #default="scope">
            <span class="erp-amount" :class="{ 'is-danger': Number(scope.row.remainAmount || 0) > 0 }">
              {{ formatAmount(scope.row.remainAmount) }}
            </span>
          </template>
        </el-table-column>
        <el-table-column label="到期时间" align="center" prop="dueTime" width="115">
          <template #default="scope">
            <span>{{ parseTime(scope.row.dueTime, '{y}-{m}-{d}') || '-' }}</span>
          </template>
        </el-table-column>
        <el-table-column label="等待/逾期" align="center" prop="overdueDays" width="96">
          <template #default="scope">
            <span :class="{ 'erp-amount is-danger': Number(scope.row.overdueDays || 0) > 0 }">
              {{ formatWaitingDays(scope.row) }}
            </span>
          </template>
        </el-table-column>
        <el-table-column label="操作" align="center" fixed="right" width="110">
          <template #default="scope">
            <el-button link type="primary" icon="Position" @click="handleTodo(scope.row)">
              {{ scope.row.actionName || "去处理" }}
            </el-button>
          </template>
        </el-table-column>
      </el-table>
    </section>
  </div>
</template>

<script setup name="BusinessTodoCenter">
import { getTodoCenter } from "@/api/business/report"
import { parseTime } from "@/utils/ruoyi"

const { proxy } = getCurrentInstance()
const router = useRouter()

const loading = ref(false)
const showSearch = ref(true)
const todoList = ref([])
const summaryData = ref({})

const data = reactive({
  queryParams: {
    todoType: undefined,
    priorityLevel: undefined,
    keyword: undefined
  }
})

const { queryParams } = toRefs(data)

const todoTypeOptionList = [
  { label: "销售经理审核", value: "sale_manager_audit" },
  { label: "销售财务审核", value: "sale_finance_audit" },
  { label: "采购审核", value: "purchase_audit" },
  { label: "采退审核", value: "purchase_return_audit" },
  { label: "销退审核", value: "sale_return_audit" },
  { label: "入库审核", value: "inbound_audit" },
  { label: "出库审核", value: "outbound_audit" },
  { label: "盘点审核", value: "inventory_check_audit" },
  { label: "调拨审核", value: "transfer_audit" },
  { label: "调拨待签收", value: "transfer_receive" },
  { label: "红字待退款", value: "red_refund_pending" },
  { label: "订单待开票", value: "order_invoice_pending" },
  { label: "库存预警", value: "stock_warning" },
  { label: "应收逾期", value: "receivable_overdue" },
  { label: "应付到期", value: "payable_due" },
  { label: "预算审批", value: "budget_approve" },
  { label: "经营决议", value: "executive_action" },
  { label: "客户跟进", value: "customer_follow_due" },
  { label: "库存临期", value: "stock_expiring" }
]

const summaryStatItems = computed(() => [
  {
    key: 'all',
    label: '全部待办',
    value: summaryData.value.totalCount || 0,
    suffix: '项',
    hint: '跨模块汇总待处理任务',
    icon: 'List',
    tone: 'indigo',
    clickable: true,
    active: !queryParams.value.priorityLevel && !queryParams.value.todoType
  },
  {
    key: 'urgent',
    label: '紧急事项',
    value: summaryData.value.urgentCount || 0,
    suffix: '项',
    hint: '需优先响应并立即处理',
    icon: 'Warning',
    tone: 'rose',
    clickable: true,
    active: queryParams.value.priorityLevel === 'urgent'
  },
  {
    key: 'approval',
    label: '审批待办',
    value: summaryData.value.approvalCount || 0,
    suffix: '项',
    hint: '销售/采购/出入库单据审核',
    icon: 'Stamp',
    tone: 'blue',
    clickable: false
  },
  {
    key: 'finance',
    label: '财务待办',
    value: summaryData.value.financeCount || 0,
    suffix: '项',
    hint: '逾期应收与到期应付提醒',
    icon: 'Wallet',
    tone: 'amber',
    clickable: false
  }
])

const priorityTabItems = computed(() => {
  const list = todoList.value || []
  return [
    { label: '全部优先级', value: '', count: list.length },
    { label: '紧急', value: 'urgent', dot: 'rose', count: list.filter(i => i.priorityLevel === 'urgent').length },
    { label: '高优先级', value: 'high', dot: 'amber', count: list.filter(i => i.priorityLevel === 'high').length },
    { label: '普通', value: 'normal', dot: 'slate', count: list.filter(i => i.priorityLevel === 'normal').length }
  ]
})

const filteredTodoList = computed(() => {
  const keyword = normalizeKeyword(queryParams.value.keyword)
  return todoList.value.filter(todoItem => {
    const todoTypeMatched = !queryParams.value.todoType || todoItem.todoType === queryParams.value.todoType
    const priorityMatched = !queryParams.value.priorityLevel || todoItem.priorityLevel === queryParams.value.priorityLevel
    const keywordMatched = !keyword || buildTodoSearchText(todoItem).includes(keyword)
    return todoTypeMatched && priorityMatched && keywordMatched
  })
})

function handleStatCardSelect(item) {
  if (item.key === 'all') {
    queryParams.value.priorityLevel = undefined
    queryParams.value.todoType = undefined
  } else if (item.key === 'urgent') {
    queryParams.value.priorityLevel = 'urgent'
  }
}

function handlePriorityTabChange(val) {
  queryParams.value.priorityLevel = val || undefined
}

function getList() {
  loading.value = true
  getTodoCenter().then(response => {
    const responseData = response.data || {}
    todoList.value = responseData.todoList || []
    summaryData.value = responseData.summary || {}
  }).finally(() => {
    loading.value = false
  })
}

function handleQuery() {}

function resetQuery() {
  proxy.resetForm("queryRef")
}

function handleTodo(todoItem) {
  const routeTarget = {
    path: todoItem.routePath || "/finance/report",
    query: {}
  }
  if (todoItem.routeQueryKey && todoItem.routeQueryValue !== undefined && todoItem.routeQueryValue !== null) {
    routeTarget.query[todoItem.routeQueryKey] = todoItem.routeQueryValue
  }
  router.push(routeTarget)
}

function normalizeKeyword(keywordValue) {
  return (keywordValue || "").toString().trim().toLowerCase()
}

function buildTodoSearchText(todoItem) {
  return [
    todoItem.todoTitle,
    todoItem.sourceNo,
    todoItem.targetName,
    getTodoTypeLabel(todoItem.todoType)
  ].filter(Boolean).join(" ").toLowerCase()
}

function getTodoTypeLabel(todoType) {
  const todoTypeOption = todoTypeOptionList.find(optionItem => optionItem.value === todoType)
  return todoTypeOption?.label || todoType || "未知类型"
}

function getPriorityLabel(priorityLevel) {
  if (priorityLevel === "urgent") return "紧急"
  if (priorityLevel === "high") return "高"
  return "普通"
}

function getPriorityTagType(priorityLevel) {
  if (priorityLevel === "urgent") return "danger"
  if (priorityLevel === "high") return "warning"
  return "info"
}

function formatAmount(amountValue) {
  const numberValue = Number(amountValue || 0)
  return numberValue.toFixed(2)
}

function formatTodoAmount(todoItem) {
  if (
    todoItem.todoType === "stock_warning"
    || todoItem.todoType === "stock_expiring"
    || todoItem.todoType === "transfer_audit"
    || todoItem.todoType === "transfer_receive"
  ) {
    return `${formatAmount(todoItem.todoAmount)} 件`
  }
  return formatAmount(todoItem.todoAmount)
}

function formatWaitingDays(todoItem) {
  const overdueDays = Number(todoItem.overdueDays || 0)
  if (todoItem.todoType === "receivable_overdue" || todoItem.todoType === "payable_due" || todoItem.todoType === "executive_action") {
    return overdueDays > 0 ? `逾期${overdueDays}天` : "-"
  }
  return overdueDays > 0 ? `${overdueDays}天` : "-"
}

getList()
</script>
