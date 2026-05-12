<template>
  <div class="app-container">
    <el-alert
      title="统一待办中心汇总销售、采购、仓库、财务和经营管理中的待处理事项，具体审核和处理仍回到原业务页面完成。"
      type="info"
      :closable="false"
      class="mb8"
    />

    <el-row :gutter="12" class="todo-summary-row">
      <el-col :xs="24" :sm="12" :md="6">
        <div class="todo-summary-item">
          <div class="todo-summary-label">全部待办</div>
          <div class="todo-summary-value">{{ summaryData.totalCount || 0 }}</div>
        </div>
      </el-col>
      <el-col :xs="24" :sm="12" :md="6">
        <div class="todo-summary-item todo-summary-item-urgent">
          <div class="todo-summary-label">紧急事项</div>
          <div class="todo-summary-value">{{ summaryData.urgentCount || 0 }}</div>
        </div>
      </el-col>
      <el-col :xs="24" :sm="12" :md="6">
        <div class="todo-summary-item">
          <div class="todo-summary-label">审批待办</div>
          <div class="todo-summary-value">{{ summaryData.approvalCount || 0 }}</div>
        </div>
      </el-col>
      <el-col :xs="24" :sm="12" :md="6">
        <div class="todo-summary-item">
          <div class="todo-summary-label">财务待办</div>
          <div class="todo-summary-value">{{ summaryData.financeCount || 0 }}</div>
        </div>
      </el-col>
    </el-row>

    <el-form :model="queryParams" ref="queryRef" :inline="true" v-show="showSearch">
      <el-form-item label="待办类型" prop="todoType">
        <el-select v-model="queryParams.todoType" placeholder="请选择待办类型" clearable style="width: 220px">
          <el-option
            v-for="todoTypeOption in todoTypeOptionList"
            :key="todoTypeOption.value"
            :label="todoTypeOption.label"
            :value="todoTypeOption.value"
          />
        </el-select>
      </el-form-item>
      <el-form-item label="优先级" prop="priorityLevel">
        <el-select v-model="queryParams.priorityLevel" placeholder="请选择优先级" clearable style="width: 160px">
          <el-option label="紧急" value="urgent" />
          <el-option label="高" value="high" />
          <el-option label="普通" value="normal" />
        </el-select>
      </el-form-item>
      <el-form-item label="关键词" prop="keyword">
        <el-input v-model="queryParams.keyword" placeholder="标题、单号、对象" clearable style="width: 220px" @keyup.enter="handleQuery" />
      </el-form-item>
      <el-form-item>
        <el-button type="primary" icon="Search" @click="handleQuery">搜索</el-button>
        <el-button icon="Refresh" @click="resetQuery">重置</el-button>
        <el-button type="success" plain icon="RefreshRight" @click="getList">刷新</el-button>
      </el-form-item>
    </el-form>

    <el-row :gutter="10" class="mb8">
      <right-toolbar v-model:showSearch="showSearch" @queryTable="getList"></right-toolbar>
    </el-row>

    <el-table v-loading="loading" :data="filteredTodoList">
      <el-table-column label="优先级" align="center" prop="priorityLevel" width="90">
        <template #default="scope">
          <el-tag :type="getPriorityTagType(scope.row.priorityLevel)">{{ getPriorityLabel(scope.row.priorityLevel) }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column label="待办事项" align="left" prop="todoTitle" min-width="260" show-overflow-tooltip />
      <el-table-column label="类型" align="center" prop="todoType" width="130">
        <template #default="scope">
          <span>{{ getTodoTypeLabel(scope.row.todoType) }}</span>
        </template>
      </el-table-column>
      <el-table-column label="单号/编号" align="center" prop="sourceNo" min-width="130" show-overflow-tooltip />
      <el-table-column label="对象" align="center" prop="targetName" min-width="150" show-overflow-tooltip />
      <el-table-column label="金额/数量" align="right" prop="todoAmount" width="120">
        <template #default="scope">
          <span>{{ formatTodoAmount(scope.row) }}</span>
        </template>
      </el-table-column>
      <el-table-column label="未结金额" align="right" prop="remainAmount" width="120">
        <template #default="scope">
          <span>{{ formatAmount(scope.row.remainAmount) }}</span>
        </template>
      </el-table-column>
      <el-table-column label="到期时间" align="center" prop="dueTime" width="160">
        <template #default="scope">
          <span>{{ parseTime(scope.row.dueTime, '{y}-{m}-{d}') || '-' }}</span>
        </template>
      </el-table-column>
      <el-table-column label="已等待" align="center" prop="overdueDays" width="100">
        <template #default="scope">
          <span>{{ formatWaitingDays(scope.row) }}</span>
        </template>
      </el-table-column>
      <el-table-column label="操作" align="center" class-name="small-padding fixed-width" width="120">
        <template #default="scope">
          <el-button link type="primary" icon="Position" @click="handleTodo(scope.row)">{{ scope.row.actionName || "去处理" }}</el-button>
        </template>
      </el-table-column>
    </el-table>
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
  { label: "入库审核", value: "inbound_audit" },
  { label: "出库审核", value: "outbound_audit" },
  { label: "库存预警", value: "stock_warning" },
  { label: "应收逾期", value: "receivable_overdue" },
  { label: "应付到期", value: "payable_due" },
  { label: "预算审批", value: "budget_approve" },
  { label: "经营决议", value: "executive_action" }
]

const filteredTodoList = computed(() => {
  const keyword = normalizeKeyword(queryParams.value.keyword)
  return todoList.value.filter(todoItem => {
    const todoTypeMatched = !queryParams.value.todoType || todoItem.todoType === queryParams.value.todoType
    const priorityMatched = !queryParams.value.priorityLevel || todoItem.priorityLevel === queryParams.value.priorityLevel
    const keywordMatched = !keyword || buildTodoSearchText(todoItem).includes(keyword)
    return todoTypeMatched && priorityMatched && keywordMatched
  })
})

// 加载统一待办中心数据，后端负责汇总业务口径，前端只处理展示和筛选。
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

// 执行筛选，当前列表为后端一次性汇总结果，筛选在前端即时完成。
function handleQuery() {
  // computed 会自动刷新筛选结果，这里保留方法便于搜索按钮和回车统一调用。
}

// 重置筛选条件，恢复展示全部待办。
function resetQuery() {
  proxy.resetForm("queryRef")
}

// 跳转到原业务页面处理待办，避免在待办中心重复实现各业务审核动作。
function handleTodo(todoItem) {
  const routeTarget = {
    path: todoItem.routePath || "/business/report",
    query: {}
  }
  if (todoItem.routeQueryKey && todoItem.routeQueryValue !== undefined && todoItem.routeQueryValue !== null) {
    routeTarget.query[todoItem.routeQueryKey] = todoItem.routeQueryValue
  }
  router.push(routeTarget)
}

// 归一化关键词，保证标题、单号和对象名称可以统一检索。
function normalizeKeyword(keywordValue) {
  return (keywordValue || "").toString().trim().toLowerCase()
}

// 组装待办搜索文本。
function buildTodoSearchText(todoItem) {
  return [
    todoItem.todoTitle,
    todoItem.sourceNo,
    todoItem.targetName,
    getTodoTypeLabel(todoItem.todoType)
  ].filter(Boolean).join(" ").toLowerCase()
}

// 返回待办类型中文名称。
function getTodoTypeLabel(todoType) {
  const todoTypeOption = todoTypeOptionList.find(optionItem => optionItem.value === todoType)
  return todoTypeOption?.label || todoType || "未知类型"
}

// 返回优先级中文名称。
function getPriorityLabel(priorityLevel) {
  if (priorityLevel === "urgent") {
    return "紧急"
  }
  if (priorityLevel === "high") {
    return "高"
  }
  return "普通"
}

// 返回优先级标签样式。
function getPriorityTagType(priorityLevel) {
  if (priorityLevel === "urgent") {
    return "danger"
  }
  if (priorityLevel === "high") {
    return "warning"
  }
  return "info"
}

// 格式化金额展示。
function formatAmount(amountValue) {
  const numberValue = Number(amountValue || 0)
  return numberValue.toFixed(2)
}

// 按待办类型展示金额或库存数量。
function formatTodoAmount(todoItem) {
  if (todoItem.todoType === "stock_warning") {
    return `${formatAmount(todoItem.todoAmount)} 件`
  }
  return formatAmount(todoItem.todoAmount)
}

// 格式化等待天数，区分逾期类和普通审批类待办。
function formatWaitingDays(todoItem) {
  const overdueDays = Number(todoItem.overdueDays || 0)
  if (todoItem.todoType === "receivable_overdue" || todoItem.todoType === "payable_due" || todoItem.todoType === "executive_action") {
    return overdueDays > 0 ? `逾期${overdueDays}天` : "-"
  }
  return overdueDays > 0 ? `${overdueDays}天` : "-"
}

getList()
</script>

<style scoped>
.todo-summary-row {
  margin-bottom: 12px;
}

.todo-summary-item {
  min-height: 86px;
  padding: 16px 18px;
  border: 1px solid #e5e7eb;
  border-radius: 6px;
  background: #ffffff;
}

.todo-summary-item-urgent {
  border-color: #f56c6c;
}

.todo-summary-label {
  color: #606266;
  font-size: 13px;
}

.todo-summary-value {
  margin-top: 10px;
  color: #303133;
  font-size: 28px;
  font-weight: 600;
  line-height: 1;
}
</style>
