<template>
  <div>
    <el-row :gutter="12" style="margin-top: 12px">
      <el-col :span="24">
        <el-card>
          <template #header>对账中心</template>
          <el-form :model="reconciliationQueryParams" :inline="true" label-width="90px">
            <el-form-item label="对象名称">
              <el-input v-model="reconciliationQueryParams.targetName" placeholder="请输入客户或供应商" clearable style="width: 240px" @keyup.enter="handleReconciliationQuery" />
            </el-form-item>
            <el-form-item>
              <el-button type="primary" icon="Search" @click="handleReconciliationQuery">查询</el-button>
              <el-button icon="Refresh" @click="handleReconciliationReset">重置</el-button>
            </el-form-item>
          </el-form>
          <el-table :data="reconciliationList" border v-loading="reconciliationLoading">
            <el-table-column label="类型" align="center" prop="reconcileType" width="140">
              <template #default="scope">
                <el-tag :type="scope.row.reconcileType === 'receivable' ? 'success' : 'warning'">
                  {{ scope.row.reconcileType === "receivable" ? "应收对账" : "应付对账" }}
                </el-tag>
              </template>
            </el-table-column>
            <el-table-column label="对象名称" align="center" min-width="180">
              <template #default="scope">
                <span>{{ getReconciliationTargetName(scope.row) }}</span>
              </template>
            </el-table-column>
            <el-table-column label="应收应付金额" align="right" min-width="140">
              <template #default="scope">
                <span>￥{{ formatAmountValue(scope.row.dueAmount) }}</span>
              </template>
            </el-table-column>
            <el-table-column label="已收已付金额" align="right" min-width="140">
              <template #default="scope">
                <span>￥{{ formatAmountValue(scope.row.paidAmount) }}</span>
              </template>
            </el-table-column>
            <el-table-column label="未结金额" align="right" min-width="140">
              <template #default="scope">
                <span>￥{{ formatAmountValue(scope.row.remainAmount) }}</span>
              </template>
            </el-table-column>
            <el-table-column label="操作" align="center" width="140">
              <template #default="scope">
                <el-button link type="primary" @click="handleReconciliationAction(scope.row)">查看台账</el-button>
              </template>
            </el-table-column>
          </el-table>
          <pagination
            v-show="reconciliationTotal > 0"
            :total="reconciliationTotal"
            v-model:page="reconciliationQueryParams.pageNum"
            v-model:limit="reconciliationQueryParams.pageSize"
            @pagination="handleReconciliationQuery"
          />
        </el-card>
      </el-col>
    </el-row>

    <el-row :gutter="12" style="margin-top: 12px">
      <el-col :span="24">
        <el-card>
          <template #header>发票税务</template>
          <el-form :model="invoiceTaxQueryParams" :inline="true" label-width="90px">
            <el-form-item label="开始日期">
              <el-date-picker v-model="invoiceTaxQueryParams.startDate" type="date" value-format="YYYY-MM-DD" placeholder="请选择开始日期" clearable style="width: 180px" />
            </el-form-item>
            <el-form-item label="结束日期">
              <el-date-picker v-model="invoiceTaxQueryParams.endDate" type="date" value-format="YYYY-MM-DD" placeholder="请选择结束日期" clearable style="width: 180px" />
            </el-form-item>
            <el-form-item>
              <el-button type="primary" icon="Search" @click="handleInvoiceTaxQuery">查询</el-button>
              <el-button icon="Refresh" @click="handleInvoiceTaxReset">重置</el-button>
            </el-form-item>
          </el-form>
          <el-table :data="invoiceTaxList" border v-loading="invoiceTaxLoading">
            <el-table-column label="类型" align="center" prop="invoiceType" width="160">
              <template #default="scope">
                <el-tag :type="scope.row.invoiceType === 'sale_invoice' ? 'success' : 'warning'">
                  {{ scope.row.invoiceType === "sale_invoice" ? "销项发票" : "进项发票" }}
                </el-tag>
              </template>
            </el-table-column>
            <el-table-column label="来源单号" align="center" prop="sourceNo" />
            <el-table-column label="含税金额" align="right" min-width="120">
              <template #default="scope">
                <span>￥{{ formatAmountValue(scope.row.invoiceAmount) }}</span>
              </template>
            </el-table-column>
            <el-table-column label="不含税金额" align="right" min-width="120">
              <template #default="scope">
                <span>￥{{ formatAmountValue(scope.row.amountWithoutTax) }}</span>
              </template>
            </el-table-column>
            <el-table-column label="税额" align="right" min-width="120">
              <template #default="scope">
                <span>￥{{ formatAmountValue(scope.row.taxAmount) }}</span>
              </template>
            </el-table-column>
            <el-table-column label="业务时间" align="center" prop="billTime" width="180">
              <template #default="scope">
                <span>{{ parseTime(scope.row.billTime) }}</span>
              </template>
            </el-table-column>
          </el-table>
          <pagination
            v-show="invoiceTaxTotal > 0"
            :total="invoiceTaxTotal"
            v-model:page="invoiceTaxQueryParams.pageNum"
            v-model:limit="invoiceTaxQueryParams.pageSize"
            @pagination="handleInvoiceTaxQuery"
          />
        </el-card>
      </el-col>
    </el-row>

    <el-row :gutter="12" style="margin-top: 12px">
      <el-col :span="24">
        <el-card>
          <template #header>资金预测</template>
          <el-form :model="cashFlowForecastQueryParams" :inline="true" label-width="90px">
            <el-form-item label="预测天数">
              <el-input-number v-model="cashFlowForecastQueryParams.forecastDays" :min="1" :max="365" controls-position="right" style="width: 180px" />
            </el-form-item>
            <el-form-item>
              <el-button type="primary" icon="Search" @click="handleCashFlowRefresh">刷新预测</el-button>
            </el-form-item>
          </el-form>
          <el-row :gutter="12">
            <el-col :span="8">
              <el-card shadow="never">
                <div class="stat-title">预测应收</div>
                <div class="stat-value">￥{{ formatAmountValue(cashFlowForecastData.receivableAmount) }}</div>
              </el-card>
            </el-col>
            <el-col :span="8">
              <el-card shadow="never">
                <div class="stat-title">预测应付</div>
                <div class="stat-value">￥{{ formatAmountValue(cashFlowForecastData.payableAmount) }}</div>
              </el-card>
            </el-col>
            <el-col :span="8">
              <el-card shadow="never">
                <div class="stat-title">预测净现金流</div>
                <div class="stat-value">￥{{ formatAmountValue(cashFlowForecastData.netCashFlow) }}</div>
              </el-card>
            </el-col>
          </el-row>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script setup>
// 经营看板财务区块只负责对账、税票和资金预测展示，具体查询与跳转仍由父页编排。
defineProps({
  reconciliationQueryParams: {
    type: Object,
    required: true
  },
  reconciliationList: {
    type: Array,
    default: () => []
  },
  reconciliationLoading: {
    type: Boolean,
    default: false
  },
  reconciliationTotal: {
    type: Number,
    default: 0
  },
  invoiceTaxQueryParams: {
    type: Object,
    required: true
  },
  invoiceTaxList: {
    type: Array,
    default: () => []
  },
  invoiceTaxLoading: {
    type: Boolean,
    default: false
  },
  invoiceTaxTotal: {
    type: Number,
    default: 0
  },
  cashFlowForecastQueryParams: {
    type: Object,
    required: true
  },
  cashFlowForecastData: {
    type: Object,
    default: () => ({})
  },
  parseTime: {
    type: Function,
    required: true
  },
  formatAmountValue: {
    type: Function,
    required: true
  },
  getReconciliationTargetName: {
    type: Function,
    required: true
  }
})

const emit = defineEmits([
  "reconciliation-query",
  "reconciliation-reset",
  "reconciliation-action",
  "invoice-tax-query",
  "invoice-tax-reset",
  "cash-flow-refresh"
])

// 触发对账中心查询，由父页统一维护接口请求和分页状态。
function handleReconciliationQuery() {
  emit("reconciliation-query")
}

// 触发对账中心重置，避免子组件直接决定重置规则。
function handleReconciliationReset() {
  emit("reconciliation-reset")
}

// 触发对账台账跳转，保留父页对路由和业务类型的统一判断。
function handleReconciliationAction(reconciliationRow) {
  emit("reconciliation-action", reconciliationRow)
}

// 触发发票税务查询，由父页继续处理接口请求。
function handleInvoiceTaxQuery() {
  emit("invoice-tax-query")
}

// 触发发票税务重置，保留父页对日期条件的统一处理。
function handleInvoiceTaxReset() {
  emit("invoice-tax-reset")
}

// 触发资金预测刷新，父页负责联动资金压力窗口。
function handleCashFlowRefresh() {
  emit("cash-flow-refresh")
}
</script>

<style scoped>
.stat-title {
  color: #606266;
  font-size: 14px;
}

.stat-value {
  margin-top: 8px;
  color: #303133;
  font-size: 24px;
  font-weight: 600;
}
</style>
