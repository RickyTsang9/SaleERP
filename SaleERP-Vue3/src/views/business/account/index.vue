<template>
  <div class="app-container erp-page fin-account-period-page">
    <erp-page-header
      eyebrow="财务分析 · 资金与关账"
      title="资金账户与月结关账"
      subtitle="管理各结算账户余额、收支流水与账户互转，并按会计期间执行月结关账与反结账。"
      icon="CreditCard"
      tone="indigo"
      :badge="`在用 ${summary.activeCount || 0} 个结算户`"
    >
      <template #actions>
        <el-button icon="Refresh" plain @click="handleRefreshActiveTab">刷新数据</el-button>
        <el-button v-if="activeTab === 'accounts'" plain icon="Download" @click="handleExportAccounts" v-hasPermi="['business:account:list']">导出账户</el-button>
        <el-button v-if="activeTab === 'journals'" plain icon="Download" @click="handleExportJournals" v-hasPermi="['business:account:list']">导出流水</el-button>
        <el-button v-if="activeTab !== 'periods'" plain icon="Switch" @click="handleOpenTransfer" v-hasPermi="['business:account:edit']">账户互转</el-button>
        <el-button v-if="activeTab !== 'periods'" type="success" plain icon="Money" @click="handleOpenOtherFund" v-hasPermi="['business:account:edit']">登记日常收支</el-button>
        <el-button v-if="activeTab === 'accounts'" type="primary" icon="Plus" @click="handleAddAccount" v-hasPermi="['business:account:edit']">新增结算账户</el-button>
        <el-button v-if="activeTab === 'periods'" type="primary" icon="Lock" @click="handleOpenClosePeriod" v-hasPermi="['business:periodClose:manage']">执行月结关账</el-button>
      </template>
    </erp-page-header>

    <erp-stat-strip :items="accountStatItems" :columns="4" />

    <section class="erp-table-card">
      <div class="erp-toolbar">
        <div class="erp-toolbar-left">
          <erp-status-tabs
            :model-value="activeTab"
            :items="[
              { label: '资金结算账户台账', value: 'accounts', dot: 'indigo' },
              { label: '资金收支流水', value: 'journals', dot: 'emerald' },
              { label: '会计期间月结关账', value: 'periods', dot: 'amber' }
            ]"
            @change="handleTabChange"
          />
        </div>
        <div class="erp-toolbar-right">
          <el-button v-if="activeTab === 'accounts'" size="small" type="primary" plain icon="Plus" @click="handleAddAccount" v-hasPermi="['business:account:edit']">新增账户</el-button>
          <el-button v-else-if="activeTab === 'journals'" size="small" type="success" plain icon="Money" @click="handleOpenOtherFund" v-hasPermi="['business:account:edit']">登记收支</el-button>
          <el-button v-else size="small" type="primary" plain icon="Lock" @click="handleOpenClosePeriod" v-hasPermi="['business:periodClose:manage']">执行月结</el-button>
        </div>
      </div>

      <template v-if="activeTab === 'accounts'">
        <el-table v-loading="accountLoading" :data="accountList">
          <el-table-column label="账户编码 / 名称" align="left" min-width="185" show-overflow-tooltip>
            <template #default="scope">
              <div class="erp-cell-title">
                {{ scope.row.accountName }}
                <el-tag v-if="scope.row.isDefault === 'Y'" type="warning" size="small" effect="light" style="margin-left: 4px">默认</el-tag>
              </div>
              <div class="erp-cell-sub">{{ scope.row.accountCode }}</div>
            </template>
          </el-table-column>
          <el-table-column label="账户类型" align="center" prop="accountType" width="102">
            <template #default="scope">
              <el-tag :type="getAccountTypeTag(scope.row.accountType)" effect="light" size="small">{{ formatAccountType(scope.row.accountType) }}</el-tag>
            </template>
          </el-table-column>
          <el-table-column label="开户行 / 账号" align="left" min-width="175" show-overflow-tooltip>
            <template #default="scope">
              <div class="erp-cell-title">{{ scope.row.bankName || '-' }}</div>
              <div class="erp-cell-sub">{{ scope.row.bankNo || '-' }}</div>
            </template>
          </el-table-column>
          <el-table-column label="期初余额" align="right" prop="openingBalance" width="112">
            <template #default="scope">
              <span class="erp-num">¥{{ formatMoney(scope.row.openingBalance) }}</span>
            </template>
          </el-table-column>
          <el-table-column label="实时余额" align="right" prop="currentBalance" width="122">
            <template #default="scope">
              <span class="erp-amount is-primary">¥{{ formatMoney(scope.row.currentBalance) }}</span>
            </template>
          </el-table-column>
          <el-table-column label="状态" align="center" prop="status" width="76">
            <template #default="scope">
              <el-tag :type="scope.row.status === '0' ? 'success' : 'danger'" effect="light" size="small">{{ scope.row.status === '0' ? '正常' : '停用' }}</el-tag>
            </template>
          </el-table-column>
          <el-table-column label="操作" width="208" fixed="right" align="center">
            <template #default="scope">
              <div class="erp-row-actions">
                <el-button link type="info" icon="Document" @click="handleViewAccountJournal(scope.row)">流水</el-button>
                <el-button link type="primary" icon="Edit" @click="handleEditAccount(scope.row)" v-hasPermi="['business:account:edit']">编辑</el-button>
                <el-button v-if="scope.row.isDefault !== 'Y' && scope.row.status === '0'" link type="warning" icon="Star" @click="handleSetDefaultAccount(scope.row)" v-hasPermi="['business:account:edit']">设默认</el-button>
              </div>
            </template>
          </el-table-column>
        </el-table>
      </template>

      <template v-else-if="activeTab === 'journals'">
        <div class="journal-filter-bar">
          <el-form :inline="true" :model="journalQuery" size="default">
            <el-form-item label="资金账户">
              <el-select v-model="journalQuery.accountId" placeholder="全部账户" clearable style="width: 168px" @change="handleJournalQuery">
                <el-option v-for="acc in accountList" :key="acc.accountId" :label="acc.accountName" :value="acc.accountId" />
              </el-select>
            </el-form-item>
            <el-form-item label="方向">
              <el-select v-model="journalQuery.direction" placeholder="全部" clearable style="width: 100px" @change="handleJournalQuery">
                <el-option label="收入" value="in" />
                <el-option label="支出" value="out" />
              </el-select>
            </el-form-item>
            <el-form-item label="业务类型">
              <el-select v-model="journalQuery.bizType" placeholder="全部类型" clearable style="width: 138px" @change="handleJournalQuery">
                <el-option label="期初建账" value="opening" />
                <el-option label="销售收款" value="receipt" />
                <el-option label="收款作废冲回" value="receipt_void" />
                <el-option label="采购付款" value="payment" />
                <el-option label="付款作废冲回" value="payment_void" />
                <el-option label="销售退款" value="sale_refund" />
                <el-option label="采购退回退款" value="purchase_refund" />
                <el-option label="账户转入" value="transfer_in" />
                <el-option label="账户转出" value="transfer_out" />
                <el-option label="日常其他收入" value="other_income" />
                <el-option label="日常费用支出" value="other_expense" />
              </el-select>
            </el-form-item>
            <el-form-item label="发生时间">
              <el-date-picker
                v-model="journalDateRange"
                value-format="YYYY-MM-DD"
                type="daterange"
                range-separator="-"
                start-placeholder="开始日期"
                end-placeholder="结束日期"
                :shortcuts="ERP_DATE_SHORTCUTS"
                style="width: 235px"
              />
            </el-form-item>
            <el-form-item>
              <el-button type="primary" icon="Search" @click="handleJournalQuery">搜索</el-button>
              <el-button icon="Refresh" @click="resetJournalQuery">重置</el-button>
            </el-form-item>
          </el-form>
        </div>

        <el-table v-loading="journalLoading" :data="journalList">
          <el-table-column label="流水号 / 时间" align="left" prop="journalNo" min-width="168" show-overflow-tooltip>
            <template #default="scope">
              <span class="erp-code-chip">{{ scope.row.journalNo }}</span>
              <div class="erp-cell-sub">{{ scope.row.occurTime || '-' }}</div>
            </template>
          </el-table-column>
          <el-table-column label="资金账户" align="left" prop="accountName" min-width="135" show-overflow-tooltip />
          <el-table-column label="收支 / 类型" align="center" width="128">
            <template #default="scope">
              <el-tag size="small" :type="scope.row.direction === 'in' ? 'success' : 'danger'" effect="light">
                {{ scope.row.direction === 'in' ? '收入' : '支出' }}
              </el-tag>
              <div class="erp-cell-sub" style="margin-top: 3px">
                <el-tag size="small" :type="getJournalBizTypeTag(scope.row.bizType)" effect="plain">
                  {{ formatJournalBizType(scope.row.bizType) }}
                </el-tag>
              </div>
            </template>
          </el-table-column>
          <el-table-column label="关联单 / 往来单位" align="left" min-width="155" show-overflow-tooltip>
            <template #default="scope">
              <div class="erp-cell-title">{{ scope.row.targetName || '-' }}</div>
              <div class="erp-cell-sub">{{ scope.row.bizNo || '-' }}</div>
            </template>
          </el-table-column>
          <el-table-column label="发生金额" align="right" prop="amount" width="114">
            <template #default="scope">
              <span :class="['erp-amount', scope.row.direction === 'in' ? 'is-positive' : 'is-danger']">
                {{ scope.row.direction === 'in' ? '+' : '-' }}¥{{ formatMoney(scope.row.amount) }}
              </span>
            </template>
          </el-table-column>
          <el-table-column label="变动后 / 前余额" align="right" width="132">
            <template #default="scope">
              <div class="erp-amount is-primary">¥{{ formatMoney(scope.row.afterBalance) }}</div>
              <div class="erp-cell-sub">前 ¥{{ formatMoney(scope.row.beforeBalance) }}</div>
            </template>
          </el-table-column>
          <el-table-column label="备注" align="left" prop="remark" min-width="120" show-overflow-tooltip>
            <template #default="scope">
              <span>{{ scope.row.remark || '-' }}</span>
            </template>
          </el-table-column>
        </el-table>

        <pagination
          v-show="journalTotal > 0"
          :total="journalTotal"
          v-model:page="journalQuery.pageNum"
          v-model:limit="journalQuery.pageSize"
          @pagination="loadJournalList"
        />
      </template>

      <template v-else>
        <el-alert
          title="会计期间执行【月结关账】后，该自然月内禁止再审核、冲红或回退销售、采购、出入库、盘点、回付款与发票单据，防止历史财务报表数据漂移；确需调整请先执行【反结账解锁】。"
          type="warning"
          :closable="false"
        />

        <el-table v-loading="periodLoading" :data="periodList">
          <el-table-column label="会计期间" align="center" prop="periodMonth" width="104">
            <template #default="scope">
              <span class="erp-code-chip">{{ scope.row.periodMonth }}</span>
            </template>
          </el-table-column>
          <el-table-column label="月结状态" align="center" prop="closeStatus" width="118">
            <template #default="scope">
              <el-tag :type="scope.row.closeStatus === 'closed' ? 'danger' : 'warning'" effect="light" size="small">
                {{ scope.row.closeStatus === 'closed' ? '已关账锁定' : '已反结账开放' }}
              </el-tag>
            </template>
          </el-table-column>
          <el-table-column label="月销售快照" align="right" prop="saleAmountSnapshot" width="118">
            <template #default="scope">
              <span class="erp-amount is-primary">¥{{ formatMoney(scope.row.saleAmountSnapshot) }}</span>
            </template>
          </el-table-column>
          <el-table-column label="月采购快照" align="right" prop="purchaseAmountSnapshot" width="118">
            <template #default="scope">
              <span class="erp-amount">¥{{ formatMoney(scope.row.purchaseAmountSnapshot) }}</span>
            </template>
          </el-table-column>
          <el-table-column label="关账人 / 时间" align="center" min-width="148" show-overflow-tooltip>
            <template #default="scope">
              <div class="erp-cell-title">{{ scope.row.closedBy || '-' }}</div>
              <div class="erp-cell-sub">{{ scope.row.closedTime || '-' }}</div>
            </template>
          </el-table-column>
          <el-table-column label="备注 / 反结账记录" align="left" min-width="150" show-overflow-tooltip>
            <template #default="scope">
              <span>{{ scope.row.reopenReason ? `[反结账] ${scope.row.reopenReason}` : (scope.row.remark || '-') }}</span>
            </template>
          </el-table-column>
          <el-table-column label="操作" width="124" fixed="right" align="center">
            <template #default="scope">
              <div class="erp-row-actions">
                <el-button v-if="scope.row.closeStatus === 'closed'" link type="danger" icon="Unlock" @click="handleReopenPeriod(scope.row)" v-hasPermi="['business:periodClose:manage']">反结账</el-button>
                <el-button v-else link type="primary" icon="Lock" @click="handleRecloseMonth(scope.row)" v-hasPermi="['business:periodClose:manage']">重新关账</el-button>
              </div>
            </template>
          </el-table-column>
        </el-table>
      </template>
    </section>

    <!-- 结算账户维护弹窗 -->
    <el-dialog v-model="accountDialogOpen" :title="accountDialogTitle" width="560px" append-to-body>
      <el-form ref="accountFormRef" :model="accountForm" :rules="accountRules" label-width="110px">
        <el-form-item label="账户编码" prop="accountCode">
          <el-input v-model="accountForm.accountCode" placeholder="如 ACC-BANK-02" :disabled="!!accountForm.accountId" />
        </el-form-item>
        <el-form-item label="账户名称" prop="accountName">
          <el-input v-model="accountForm.accountName" placeholder="如 工商银行一般结算户" />
        </el-form-item>
        <el-form-item label="账户类型" prop="accountType">
          <el-select v-model="accountForm.accountType" style="width: 100%">
            <el-option label="银行对公/储蓄户" value="bank" />
            <el-option label="微信支付商户" value="wechat" />
            <el-option label="支付宝企业户" value="alipay" />
            <el-option label="库存现金备用金" value="cash" />
          </el-select>
        </el-form-item>
        <el-form-item label="开户行 / 平台" prop="bankName">
          <el-input v-model="accountForm.bankName" placeholder="请输入开户支行或支付平台名称" />
        </el-form-item>
        <el-form-item label="账号 / 商户号" prop="bankNo">
          <el-input v-model="accountForm.bankNo" placeholder="请输入银行卡号或商户号" />
        </el-form-item>
        <el-form-item v-if="!accountForm.accountId" label="期初建账余额" prop="openingBalance">
          <el-input-number v-model="accountForm.openingBalance" :precision="2" controls-position="right" style="width: 100%" />
        </el-form-item>
        <el-form-item label="设为默认账户">
          <el-switch v-model="accountForm.isDefault" active-value="Y" inactive-value="N" />
        </el-form-item>
        <el-form-item label="状态">
          <el-radio-group v-model="accountForm.status">
            <el-radio value="0">正常</el-radio>
            <el-radio value="1">停用</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="备注">
          <el-input v-model="accountForm.remark" type="textarea" placeholder="选填备注" />
        </el-form-item>
      </el-form>
      <template #footer>
        <div class="dialog-footer">
          <el-button type="primary" :loading="savingAccount" @click="submitAccountForm">确 定</el-button>
          <el-button @click="accountDialogOpen = false">取 消</el-button>
        </div>
      </template>
    </el-dialog>

    <!-- 资金账户互转弹窗 -->
    <el-dialog v-model="transferDialogOpen" title="资金账户内部互转" width="500px" append-to-body>
      <el-form ref="transferFormRef" :model="transferForm" :rules="transferRules" label-width="100px">
        <el-form-item label="转出账户" prop="fromAccountId">
          <el-select v-model="transferForm.fromAccountId" placeholder="请选择转出资金账户" style="width: 100%">
            <el-option
              v-for="acc in activeAccountOptions"
              :key="acc.accountId"
              :label="`${acc.accountName}（余额 ¥${formatMoney(acc.currentBalance)}）`"
              :value="acc.accountId"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="转入账户" prop="toAccountId">
          <el-select v-model="transferForm.toAccountId" placeholder="请选择转入资金账户" style="width: 100%">
            <el-option
              v-for="acc in activeAccountOptions"
              :key="acc.accountId"
              :disabled="acc.accountId === transferForm.fromAccountId"
              :label="`${acc.accountName}（余额 ¥${formatMoney(acc.currentBalance)}）`"
              :value="acc.accountId"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="互转金额" prop="amount">
          <el-input-number v-model="transferForm.amount" :min="0.01" :precision="2" controls-position="right" style="width: 100%" />
        </el-form-item>
        <el-form-item label="发生时间" prop="occurTime">
          <el-date-picker
            v-model="transferForm.occurTime"
            type="datetime"
            value-format="YYYY-MM-DD HH:mm:ss"
            placeholder="选择发生时间"
            style="width: 100%"
          />
        </el-form-item>
        <el-form-item label="互转说明" prop="remark">
          <el-input v-model="transferForm.remark" type="textarea" placeholder="如：微信提现转入对公基本户" />
        </el-form-item>
      </el-form>
      <template #footer>
        <div class="dialog-footer">
          <el-button type="primary" :loading="savingTransfer" @click="submitTransferForm">确认互转</el-button>
          <el-button @click="transferDialogOpen = false">取 消</el-button>
        </div>
      </template>
    </el-dialog>

    <!-- 登记日常收支弹窗 -->
    <el-dialog v-model="otherFundDialogOpen" title="登记日常资金收支" width="500px" append-to-body>
      <el-form ref="otherFundFormRef" :model="otherFundForm" :rules="otherFundRules" label-width="100px">
        <el-form-item label="资金账户" prop="accountId">
          <el-select v-model="otherFundForm.accountId" placeholder="请选择资金账户" style="width: 100%">
            <el-option
              v-for="acc in activeAccountOptions"
              :key="acc.accountId"
              :label="`${acc.accountName}（余额 ¥${formatMoney(acc.currentBalance)}）`"
              :value="acc.accountId"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="收支方向" prop="direction">
          <el-radio-group v-model="otherFundForm.direction">
            <el-radio-button value="in">日常其他收入</el-radio-button>
            <el-radio-button value="out">日常费用支出</el-radio-button>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="发生金额" prop="amount">
          <el-input-number v-model="otherFundForm.amount" :min="0.01" :precision="2" controls-position="right" style="width: 100%" />
        </el-form-item>
        <el-form-item label="对手方/科目" prop="targetName">
          <el-input v-model="otherFundForm.targetName" placeholder="如：银行利息收入、房租物业、物流运费等" />
        </el-form-item>
        <el-form-item label="发生时间" prop="occurTime">
          <el-date-picker
            v-model="otherFundForm.occurTime"
            type="datetime"
            value-format="YYYY-MM-DD HH:mm:ss"
            placeholder="选择发生时间"
            style="width: 100%"
          />
        </el-form-item>
        <el-form-item label="备注说明" prop="remark">
          <el-input v-model="otherFundForm.remark" type="textarea" placeholder="选填收支明细说明" />
        </el-form-item>
      </el-form>
      <template #footer>
        <div class="dialog-footer">
          <el-button type="primary" :loading="savingOtherFund" @click="submitOtherFundForm">确认登记</el-button>
          <el-button @click="otherFundDialogOpen = false">取 消</el-button>
        </div>
      </template>
    </el-dialog>

    <!-- 执行月结关账弹窗 -->
    <el-dialog v-model="periodDialogOpen" title="执行财务会计期间月结关账" width="480px" append-to-body>
      <el-form :model="periodForm" label-width="110px">
        <el-form-item label="会计月份" required>
          <el-date-picker
            v-model="periodForm.periodMonth"
            type="month"
            value-format="YYYY-MM"
            placeholder="选择要关账的月份"
            style="width: 100%"
          />
        </el-form-item>
        <el-form-item label="未审单据处理">
          <el-checkbox v-model="periodForm.forceClose">允许带待审单据强制月结（默认会拦截未审单据）</el-checkbox>
        </el-form-item>
        <el-form-item label="月结说明">
          <el-input v-model="periodForm.remark" type="textarea" placeholder="如：本月应收应付核对完毕，正式关账锁定" />
        </el-form-item>
      </el-form>
      <template #footer>
        <div class="dialog-footer">
          <el-button type="primary" :loading="closingPeriod" @click="submitClosePeriod">确认关账锁定</el-button>
          <el-button @click="periodDialogOpen = false">取 消</el-button>
        </div>
      </template>
    </el-dialog>
  </div>
</template>

<script setup name="Account">
import {
  listAccount,
  getAccountSummary,
  addAccount,
  updateAccount,
  listAccountJournal,
  transferAccount,
  recordOtherFund,
  listPeriodClose,
  closePeriod,
  reopenPeriod
} from "@/api/business/account"
import { ERP_DATE_SHORTCUTS } from "@/utils/erpFormat"

const { proxy } = getCurrentInstance()

const activeTab = ref("accounts")
const journalDateRange = ref([])
const accountLoading = ref(true)
const journalLoading = ref(false)
const periodLoading = ref(true)
const accountList = ref([])
const journalList = ref([])
const journalTotal = ref(0)
const periodList = ref([])
const summary = ref({
  totalBalance: 0,
  bankBalance: 0,
  onlineBalance: 0,
  cashBalance: 0,
  activeCount: 0
})

const journalQuery = ref({
  pageNum: 1,
  pageSize: 15,
  accountId: undefined,
  direction: undefined,
  bizType: undefined
})

const activeAccountOptions = computed(() => (accountList.value || []).filter(item => item.status === "0"))

const accountStatItems = computed(() => [
  { label: "可用资金总余额", prefix: "¥", value: formatMoney(summary.value.totalBalance), hint: `在用正常结算户 ${summary.value.activeCount || 0} 个`, icon: "Wallet", tone: "indigo" },
  { label: "银行对公/储蓄", prefix: "¥", value: formatMoney(summary.value.bankBalance), hint: "主力大额收付款资金池", icon: "CreditCard", tone: "blue" },
  { label: "微信/支付宝商户", prefix: "¥", value: formatMoney(summary.value.onlineBalance), hint: "终端扫码与线上收款", icon: "Money", tone: "emerald" },
  { label: "财务库存现金", prefix: "¥", value: formatMoney(summary.value.cashBalance), hint: "零星采购及备用金", icon: "Coin", tone: "amber" }
])

const accountDialogOpen = ref(false)
const accountDialogTitle = ref("新增结算账户")
const savingAccount = ref(false)
const accountForm = ref({})
const accountRules = {
  accountCode: [{ required: true, message: "请输入账户编码", trigger: "blur" }],
  accountName: [{ required: true, message: "请输入账户名称", trigger: "blur" }]
}

const transferDialogOpen = ref(false)
const savingTransfer = ref(false)
const transferForm = ref({
  fromAccountId: undefined,
  toAccountId: undefined,
  amount: undefined,
  occurTime: "",
  remark: ""
})
const transferRules = {
  fromAccountId: [{ required: true, message: "请选择转出账户", trigger: "change" }],
  toAccountId: [{ required: true, message: "请选择转入账户", trigger: "change" }],
  amount: [{ required: true, message: "请输入互转金额", trigger: "blur" }]
}

const otherFundDialogOpen = ref(false)
const savingOtherFund = ref(false)
const otherFundForm = ref({
  accountId: undefined,
  direction: "in",
  amount: undefined,
  targetName: "",
  occurTime: "",
  remark: ""
})
const otherFundRules = {
  accountId: [{ required: true, message: "请选择资金账户", trigger: "change" }],
  direction: [{ required: true, message: "请选择收支方向", trigger: "change" }],
  amount: [{ required: true, message: "请输入发生金额", trigger: "blur" }],
  targetName: [{ required: true, message: "请填写对手方或收支科目", trigger: "blur" }]
}

const periodDialogOpen = ref(false)
const closingPeriod = ref(false)
const periodForm = ref({
  periodMonth: "",
  forceClose: false,
  remark: ""
})

function buildNowDatetime() {
  const d = new Date()
  const pad = n => String(n).padStart(2, "0")
  return `${d.getFullYear()}-${pad(d.getMonth() + 1)}-${pad(d.getDate())} ${pad(d.getHours())}:${pad(d.getMinutes())}:${pad(d.getSeconds())}`
}

function formatMoney(val) {
  const num = Number(val || 0)
  return Number.isFinite(num) ? num.toFixed(2) : "0.00"
}

function formatAccountType(t) {
  if (t === "bank") return "银行账户"
  if (t === "wechat") return "微信商户"
  if (t === "alipay") return "支付宝户"
  if (t === "cash") return "库存现金"
  return t || "-"
}

function getAccountTypeTag(t) {
  if (t === "bank") return "primary"
  if (t === "wechat") return "success"
  if (t === "alipay") return "warning"
  return "info"
}

const journalBizTypeMap = {
  opening: { label: "期初建账", tag: "info" },
  receipt: { label: "销售收款", tag: "success" },
  receipt_void: { label: "收款作废冲回", tag: "warning" },
  payment: { label: "采购付款", tag: "danger" },
  payment_void: { label: "付款作废冲回", tag: "warning" },
  sale_refund: { label: "销售退款", tag: "danger" },
  purchase_refund: { label: "采购退回退款", tag: "success" },
  transfer_in: { label: "账户转入", tag: "primary" },
  transfer_out: { label: "账户转出", tag: "warning" },
  other_income: { label: "日常其他收入", tag: "success" },
  other_expense: { label: "日常费用支出", tag: "danger" }
}

function formatJournalBizType(bizType) {
  return journalBizTypeMap[bizType]?.label || bizType || "-"
}

function getJournalBizTypeTag(bizType) {
  return journalBizTypeMap[bizType]?.tag || "info"
}

function handleTabChange(val) {
  activeTab.value = val
  if (val === "journals") {
    loadJournalList()
  } else if (val === "periods") {
    loadPeriodList()
  } else {
    refreshAccountData()
  }
}

function handleRefreshActiveTab() {
  if (activeTab.value === "journals") {
    loadJournalList()
  } else if (activeTab.value === "periods") {
    loadPeriodList()
  } else {
    refreshAccountData()
  }
}

async function refreshAccountData() {
  accountLoading.value = true
  try {
    const [listRes, sumRes] = await Promise.all([
      listAccount({ pageNum: 1, pageSize: 100 }),
      getAccountSummary()
    ])
    accountList.value = listRes.rows || []
    summary.value = sumRes.data || summary.value
  } finally {
    accountLoading.value = false
  }
}

async function loadJournalList() {
  journalLoading.value = true
  try {
    const res = await listAccountJournal(proxy.addDateRange(journalQuery.value, journalDateRange.value))
    journalList.value = res.rows || []
    journalTotal.value = res.total || 0
  } finally {
    journalLoading.value = false
  }
}

function handleJournalQuery() {
  journalQuery.value.pageNum = 1
  loadJournalList()
}

function resetJournalQuery() {
  journalDateRange.value = []
  journalQuery.value = {
    pageNum: 1,
    pageSize: 15,
    accountId: undefined,
    direction: undefined,
    bizType: undefined
  }
  loadJournalList()
}

function handleExportAccounts() {
  proxy.download("business/account/export", {}, `fin_account_${Date.now()}.xlsx`)
}

function handleExportJournals() {
  proxy.download("business/account/journal/export", {
    ...proxy.addDateRange(journalQuery.value, journalDateRange.value)
  }, `fin_account_journal_${Date.now()}.xlsx`)
}

function handleViewAccountJournal(row) {
  journalQuery.value = {
    pageNum: 1,
    pageSize: 15,
    accountId: row.accountId,
    direction: undefined,
    bizType: undefined
  }
  activeTab.value = "journals"
  loadJournalList()
}

function handleOpenTransfer() {
  const active = activeAccountOptions.value
  transferForm.value = {
    fromAccountId: active[0]?.accountId,
    toAccountId: active[1]?.accountId,
    amount: undefined,
    occurTime: buildNowDatetime(),
    remark: ""
  }
  transferDialogOpen.value = true
}

function submitTransferForm() {
  proxy.$refs["transferFormRef"].validate(async valid => {
    if (!valid) return
    if (transferForm.value.fromAccountId === transferForm.value.toAccountId) {
      proxy.$modal.msgWarning("转出账户和转入账户不能相同")
      return
    }
    savingTransfer.value = true
    try {
      await transferAccount(transferForm.value)
      proxy.$modal.msgSuccess("账户互转登记成功，已生成双边资金流水")
      transferDialogOpen.value = false
      await refreshAccountData()
      if (activeTab.value === "journals") {
        await loadJournalList()
      }
    } finally {
      savingTransfer.value = false
    }
  })
}

function handleOpenOtherFund() {
  const defaultAcc = activeAccountOptions.value.find(a => a.isDefault === "Y") || activeAccountOptions.value[0]
  otherFundForm.value = {
    accountId: defaultAcc?.accountId,
    direction: "in",
    amount: undefined,
    targetName: "",
    occurTime: buildNowDatetime(),
    remark: ""
  }
  otherFundDialogOpen.value = true
}

function submitOtherFundForm() {
  proxy.$refs["otherFundFormRef"].validate(async valid => {
    if (!valid) return
    savingOtherFund.value = true
    try {
      await recordOtherFund(otherFundForm.value)
      proxy.$modal.msgSuccess("日常资金收支已记账并写入流水")
      otherFundDialogOpen.value = false
      await refreshAccountData()
      if (activeTab.value === "journals") {
        await loadJournalList()
      }
    } finally {
      savingOtherFund.value = false
    }
  })
}

async function loadPeriodList() {
  periodLoading.value = true
  try {
    const res = await listPeriodClose({ pageNum: 1, pageSize: 50 })
    periodList.value = res.rows || []
  } finally {
    periodLoading.value = false
  }
}

function handleAddAccount() {
  accountForm.value = {
    accountId: undefined,
    accountCode: "",
    accountName: "",
    accountType: "bank",
    bankName: "",
    bankNo: "",
    openingBalance: 0,
    isDefault: "N",
    status: "0",
    remark: ""
  }
  accountDialogTitle.value = "新增资金结算账户"
  accountDialogOpen.value = true
}

function handleEditAccount(row) {
  accountForm.value = { ...row }
  accountDialogTitle.value = "编辑资金结算账户"
  accountDialogOpen.value = true
}

async function handleSetDefaultAccount(row) {
  await updateAccount({ ...row, isDefault: "Y" })
  proxy.$modal.msgSuccess(`已将【${row.accountName}】设为默认结算户`)
  refreshAccountData()
}

function submitAccountForm() {
  proxy.$refs["accountFormRef"].validate(async valid => {
    if (!valid) return
    savingAccount.value = true
    try {
      if (accountForm.value.accountId) {
        await updateAccount(accountForm.value)
        proxy.$modal.msgSuccess("账户信息已更新")
      } else {
        await addAccount(accountForm.value)
        proxy.$modal.msgSuccess("新增结算账户成功")
      }
      accountDialogOpen.value = false
      refreshAccountData()
    } finally {
      savingAccount.value = false
    }
  })
}

function handleOpenClosePeriod() {
  const d = new Date()
  d.setMonth(d.getMonth() - 1)
  periodForm.value = {
    periodMonth: `${d.getFullYear()}-${String(d.getMonth() + 1).padStart(2, "0")}`,
    forceClose: false,
    remark: "月度账实核对完成，执行月结锁定"
  }
  periodDialogOpen.value = true
}

async function submitClosePeriod() {
  if (!periodForm.value.periodMonth) {
    proxy.$modal.msgWarning("请选择会计月份")
    return
  }
  closingPeriod.value = true
  try {
    await closePeriod(periodForm.value)
    proxy.$modal.msgSuccess(`会计期间 ${periodForm.value.periodMonth} 已完成月结关账`)
    periodDialogOpen.value = false
    loadPeriodList()
  } finally {
    closingPeriod.value = false
  }
}

function handleReopenPeriod(row) {
  proxy.$modal.prompt(`确认对已关账期间【${row.periodMonth}】执行反结账解锁？请输入反结账原因：`).then(({ value }) => {
    if (!value || !value.trim()) {
      proxy.$modal.msgWarning("必须填写反结账原因")
      return Promise.reject(new Error("empty"))
    }
    return reopenPeriod({ periodId: row.periodId, reopenReason: value.trim() })
  }).then(() => {
    proxy.$modal.msgSuccess(`会计期间 ${row.periodMonth} 已解锁开放`)
    loadPeriodList()
  }).catch(() => {})
}

function handleRecloseMonth(row) {
  periodForm.value = {
    periodMonth: row.periodMonth,
    forceClose: false,
    remark: "调整完毕重新关账锁定"
  }
  periodDialogOpen.value = true
}

refreshAccountData()
loadPeriodList()
</script>

<style scoped lang="scss">
.journal-filter-bar {
  padding: 12px 16px 0;
  border-bottom: 1px solid var(--el-border-color-lighter);
}

.mb12 {
  margin-bottom: 12px;
}

.acct-kpi-card {
  padding: 16px 18px;
  border-radius: 12px;
  border: 1px solid var(--el-border-color-lighter);
  background: var(--el-bg-color);

  .kpi-label {
    font-size: 13px;
    color: var(--el-text-color-secondary);
  }

  .kpi-val {
    margin-top: 8px;
    font-size: 22px;
    font-weight: 700;
    color: var(--el-text-color-primary);

    &.text-primary {
      color: var(--el-color-primary);
    }
    &.text-success {
      color: var(--el-color-success);
    }
  }

  .kpi-sub {
    margin-top: 6px;
    font-size: 12px;
    color: var(--el-text-color-secondary);
  }
}
</style>
