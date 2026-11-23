<template>
  <el-dialog
    v-model="visible"
    :title="statementData?.reconcileTypeLabel || '标准往来对账单'"
    width="960px"
    append-to-body
  >
    <div class="statement-toolbar">
      <el-form :inline="true" size="small">
        <el-form-item label="对账期间">
          <el-date-picker
            v-model="dateRange"
            type="daterange"
            value-format="YYYY-MM-DD"
            range-separator="至"
            start-placeholder="开始日期"
            end-placeholder="结束日期"
            style="width: 240px"
          />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" icon="Search" @click="loadStatement">刷新对账单</el-button>
          <el-button type="success" plain icon="Download" :disabled="!statementData" @click="handleExportCsv">导出 CSV</el-button>
          <el-button type="warning" plain icon="Printer" :disabled="!statementData" v-print="printConfig">打印对账单</el-button>
        </el-form-item>
      </el-form>
    </div>

    <div id="reconciliationStatementPrintArea" v-loading="loading" class="statement-sheet">
      <template v-if="statementData">
        <div class="statement-header">
          <h3 class="statement-title">销售管理系统 · {{ statementData.reconcileTypeLabel }}</h3>
          <div class="statement-meta">
            <span>往来单位：<strong>{{ statementData.targetName }}</strong>（{{ statementData.targetCode || '-' }}）</span>
            <span>联系人：{{ statementData.contactPerson || '-' }} {{ statementData.contactPhone || '' }}</span>
            <span>制表日期：{{ statementData.statementDate }}</span>
          </div>
          <div class="statement-meta" v-if="statementData.startDate || statementData.endDate">
            <span>统计期间：{{ statementData.startDate || '不限' }} 至 {{ statementData.endDate || '不限' }}</span>
          </div>
        </div>

        <div class="statement-kpi-grid">
          <div class="statement-kpi-card">
            <div class="statement-kpi-label">{{ isReceivable ? '本期应收总额' : '本期应付总额' }}</div>
            <div class="statement-kpi-value">¥{{ formatMoney(statementData.totalDueAmount) }}</div>
          </div>
          <div class="statement-kpi-card">
            <div class="statement-kpi-label">{{ isReceivable ? '已收总额' : '已付总额' }}</div>
            <div class="statement-kpi-value">¥{{ formatMoney(statementData.totalPaidAmount) }}</div>
          </div>
          <div class="statement-kpi-card">
            <div class="statement-kpi-label">期末未结余额</div>
            <div class="statement-kpi-value is-highlight">¥{{ formatMoney(statementData.totalRemainAmount) }}</div>
          </div>
          <div class="statement-kpi-card">
            <div class="statement-kpi-label">其中逾期未结</div>
            <div class="statement-kpi-value" :class="{ 'is-danger': Number(statementData.overdueAmount || 0) > 0 }">
              ¥{{ formatMoney(statementData.overdueAmount) }}
            </div>
          </div>
        </div>

        <div class="statement-section-title">{{ isReceivable ? '一、应收账款明细' : '一、应付账款明细' }}</div>
        <el-table :data="statementData.billList || []" size="small" border class="mb16">
          <el-table-column label="单据编号" prop="billNo" width="102" align="center" />
          <el-table-column :label="isReceivable ? '来源销售单' : '来源采购单'" prop="sourceOrderNo" min-width="136" align="center" show-overflow-tooltip />
          <el-table-column :label="isReceivable ? '应收金额' : '应付金额'" align="right" width="98">
            <template #default="scope">¥{{ formatMoney(scope.row.amountDue) }}</template>
          </el-table-column>
          <el-table-column :label="isReceivable ? '已收金额' : '已付金额'" align="right" width="98">
            <template #default="scope">¥{{ formatMoney(scope.row.amountPaid) }}</template>
          </el-table-column>
          <el-table-column label="未结余额" align="right" width="98">
            <template #default="scope">¥{{ formatMoney(scope.row.remainAmount) }}</template>
          </el-table-column>
          <el-table-column label="状态" prop="statusLabel" width="80" align="center" />
          <el-table-column label="到期日" width="100" align="center">
            <template #default="scope">{{ formatDateOnly(scope.row.dueDate) }}</template>
          </el-table-column>
        </el-table>

        <div class="statement-section-title">{{ isReceivable ? '二、回款结算明细' : '二、付款结算明细' }}</div>
        <el-table :data="statementData.settlementList || []" size="small" border>
          <el-table-column label="流水编号" prop="settlementNo" width="102" align="center" />
          <el-table-column :label="isReceivable ? '来源销售单' : '来源采购单'" prop="sourceOrderNo" min-width="136" align="center" show-overflow-tooltip />
          <el-table-column :label="isReceivable ? '回款金额' : '付款金额'" align="right" width="102">
            <template #default="scope">¥{{ formatMoney(scope.row.amount) }}</template>
          </el-table-column>
          <el-table-column label="结算方式" prop="paymentMethodLabel" width="90" align="center" />
          <el-table-column label="结算时间" width="144" align="center">
            <template #default="scope">{{ parseTime(scope.row.settlementTime, '{y}-{m}-{d} {h}:{i}') }}</template>
          </el-table-column>
          <el-table-column label="备注" prop="remark" min-width="130" show-overflow-tooltip />
        </el-table>

        <div class="statement-footer-sign">
          <span>我方经办人签字：____________________</span>
          <span>对方财务确认盖章：____________________</span>
        </div>
      </template>
    </div>

    <template #footer>
      <el-button @click="visible = false">关 闭</el-button>
    </template>
  </el-dialog>
</template>

<script setup>
import { getReconciliationStatement } from "@/api/business/report"
import { parseTime } from "@/utils/ruoyi"

const visible = ref(false)
const loading = ref(false)
const dateRange = ref([])
const currentTarget = ref({ reconcileType: "receivable", targetId: "" })
const statementData = ref(null)

const isReceivable = computed(() => currentTarget.value.reconcileType === "receivable")
const printConfig = computed(() => ({
  id: "reconciliationStatementPrintArea",
  popTitle: `${statementData.value?.reconcileTypeLabel || "往来对账单"}-${statementData.value?.targetName || ""}`
}))

function openStatement(reconciliationRow) {
  if (!reconciliationRow?.targetId) {
    return
  }
  currentTarget.value = {
    reconcileType: reconciliationRow.reconcileType || "receivable",
    targetId: reconciliationRow.targetId
  }
  dateRange.value = []
  visible.value = true
  loadStatement()
}

function loadStatement() {
  loading.value = true
  const params = {
    reconcileType: currentTarget.value.reconcileType,
    targetId: currentTarget.value.targetId
  }
  if (dateRange.value && dateRange.value.length === 2) {
    params.startDate = dateRange.value[0]
    params.endDate = dateRange.value[1]
  }
  getReconciliationStatement(params).then(response => {
    statementData.value = response.data || null
  }).finally(() => {
    loading.value = false
  })
}

function formatMoney(value) {
  const num = Number(value || 0)
  return Number.isFinite(num) ? num.toFixed(2) : "0.00"
}

function formatDateOnly(value) {
  if (!value) return "-"
  return parseTime(value, "{y}-{m}-{d}")
}

function escapeCsvCell(value) {
  const text = value === null || value === undefined ? "" : String(value)
  return `"${text.replace(/"/g, '""')}"`
}

function handleExportCsv() {
  if (!statementData.value) return
  const d = statementData.value
  const lines = []
  lines.push([d.reconcileTypeLabel, d.targetName, `编码:${d.targetCode || "-"}`, `制表日期:${d.statementDate}`].map(escapeCsvCell).join(","))
  lines.push(["应收/应付总额", formatMoney(d.totalDueAmount), "已收/已付总额", formatMoney(d.totalPaidAmount), "期末未结余额", formatMoney(d.totalRemainAmount), "逾期未结", formatMoney(d.overdueAmount)].map(escapeCsvCell).join(","))
  lines.push("")
  lines.push(["单据编号", "来源单号", "应收/应付金额", "已收/已付金额", "未结余额", "状态", "到期日", "备注"].map(escapeCsvCell).join(","))
  ;(d.billList || []).forEach(row => {
    lines.push([row.billNo, row.sourceOrderNo || "-", formatMoney(row.amountDue), formatMoney(row.amountPaid), formatMoney(row.remainAmount), row.statusLabel, formatDateOnly(row.dueDate), row.remark || ""].map(escapeCsvCell).join(","))
  })
  lines.push("")
  lines.push(["结算流水号", "来源单号", "结算金额", "结算方式", "结算时间", "备注"].map(escapeCsvCell).join(","))
  ;(d.settlementList || []).forEach(row => {
    lines.push([row.settlementNo, row.sourceOrderNo || "-", formatMoney(row.amount), row.paymentMethodLabel, parseTime(row.settlementTime) || "-", row.remark || ""].map(escapeCsvCell).join(","))
  })
  const blob = new Blob(["\uFEFF" + lines.join("\r\n")], { type: "text/csv;charset=utf-8;" })
  const url = URL.createObjectURL(blob)
  const link = document.createElement("a")
  link.href = url
  link.download = `${d.reconcileTypeLabel}_${d.targetName}_${d.statementDate}.csv`
  document.body.appendChild(link)
  link.click()
  document.body.removeChild(link)
  URL.revokeObjectURL(url)
}

defineExpose({ openStatement })
</script>

<style scoped lang="scss">
.statement-toolbar {
  margin-bottom: 12px;
}
.statement-sheet {
  padding: 18px 20px;
  border-radius: 12px;
  border: 1px solid var(--erp-border);
  background: var(--erp-surface);
}
.statement-header {
  margin-bottom: 14px;
  padding-bottom: 10px;
  border-bottom: 1px solid var(--erp-border);
}
.statement-title {
  margin: 0 0 8px;
  text-align: center;
  font-size: 18px;
  font-weight: 700;
  color: var(--erp-heading);
}
.statement-meta {
  display: flex;
  flex-wrap: wrap;
  justify-content: space-between;
  gap: 8px;
  font-size: 12px;
  color: var(--erp-text-secondary);
}
.statement-kpi-grid {
  display: grid;
  grid-template-columns: repeat(4, minmax(0, 1fr));
  gap: 10px;
  margin-bottom: 14px;
}
.statement-kpi-card {
  padding: 12px 14px;
  border-radius: 10px;
  border: 1px solid var(--erp-border);
  background: var(--erp-surface-soft);
}
.statement-kpi-label {
  font-size: 12px;
  color: var(--erp-text-secondary);
}
.statement-kpi-value {
  margin-top: 4px;
  font-size: 16px;
  font-weight: 700;
  font-variant-numeric: tabular-nums;
  color: var(--erp-heading);
}
.statement-kpi-value.is-highlight {
  color: var(--erp-primary);
}
.statement-kpi-value.is-danger {
  color: var(--erp-danger);
}
.statement-section-title {
  margin: 14px 0 8px;
  font-size: 13px;
  font-weight: 600;
  color: var(--erp-heading);
}
.statement-footer-sign {
  display: flex;
  justify-content: space-between;
  margin-top: 20px;
  padding-top: 12px;
  border-top: 1px dashed var(--erp-border-strong);
  font-size: 12px;
  color: var(--erp-text-secondary);
}
.mb16 {
  margin-bottom: 16px;
}
</style>
