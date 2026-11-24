<template>
  <div class="app-container erp-page">
    <erp-page-header
      eyebrow="财务分析 · 经营看板"
      title="经营分析与财务看板"
      subtitle="汇总展示销售趋势、应收应付账龄、商品毛利排行、预算执行与资金预测。"
      icon="DataAnalysis"
      tone="indigo"
    >
      <template #actions>
        <el-button plain icon="TrendCharts" @click="router.push('/finance/saleReport')">销售明细报表</el-button>
        <el-button type="primary" icon="Refresh" :loading="managementCockpitLoading" @click="getData">刷新看板</el-button>
      </template>
    </erp-page-header>

    <ReportShortcutCardsSection
      :card-list="dashboardOverviewShortcutCardList"
      :column-span="4"
      margin-top="0"
      @shortcut="handleDashboardShortcut"
    />

    <el-row
      ref="executiveActionSectionRef"
      :class="{ 'todo-target-section': currentTodoTargetType === 'executive_action' }"
      :gutter="12"
      style="margin-top: 12px"
      v-if="hasManagementCockpitData"
    >
      <el-col :span="24">
        <el-card>
          <template #header>
            <div class="dashboard-section-header">
              <div class="dashboard-section-title-group">
                <span>经营决议事项</span>
                <el-tag size="small" :type="pendingExecutiveActionItemCount > 0 ? 'warning' : 'success'">
                  {{ pendingExecutiveActionItemCount > 0 ? `待跟进${pendingExecutiveActionItemCount}项` : "事项已清空" }}
                </el-tag>
              </div>
              <el-button v-if="canManageExecutiveActionItem" link type="primary" @click="openExecutiveActionItemDialog()">新增事项</el-button>
            </div>
          </template>
          <div v-loading="executiveActionItemLoading">
            <el-empty v-if="!executiveActionItemList.length" description="当前还没有经营决议事项，归档经营简报后会自动生成待跟进事项。" :image-size="88" />
            <el-table v-else :data="executiveActionItemList" border size="small">
              <el-table-column label="决议事项" align="left" min-width="200" show-overflow-tooltip>
                <template #default="scope">
                  <div class="executive-action-title">{{ scope.row.actionTitle }}</div>
                  <div v-if="scope.row.briefTitleSnapshot" class="executive-action-brief-title">来源：{{ scope.row.briefTitleSnapshot }}</div>
                </template>
              </el-table-column>
              <el-table-column label="负责人" align="center" prop="ownerName" width="84" />
              <el-table-column label="到期日" align="center" width="100">
                <template #default="scope">
                  <span :class="{ 'executive-action-overdue-text': isExecutiveActionItemOverdue(scope.row) }">
                    {{ scope.row.dueDate ? parseTime(scope.row.dueDate, '{y}-{m}-{d}') : '-' }}
                  </span>
                </template>
              </el-table-column>
              <el-table-column label="优先级" align="center" width="76">
                <template #default="scope">
                  <el-tag size="small" :type="getExecutiveActionPriorityTagType(scope.row.priorityLevel)">
                    {{ getExecutiveActionPriorityLabel(scope.row.priorityLevel) }}
                  </el-tag>
                </template>
              </el-table-column>
              <el-table-column label="状态" align="center" width="82">
                <template #default="scope">
                  <el-tag size="small" :type="getExecutiveActionStatusTagType(scope.row.actionStatus)">
                    {{ getExecutiveActionStatusLabel(scope.row.actionStatus) }}
                  </el-tag>
                </template>
              </el-table-column>
              <el-table-column label="进度备注" align="left" min-width="170" show-overflow-tooltip>
                <template #default="scope">
                  <span>{{ scope.row.progressRemark || '-' }}</span>
                </template>
              </el-table-column>
              <el-table-column label="操作" align="center" width="142">
                <template #default="scope">
                  <div class="erp-row-actions">
                    <el-button v-if="canManageExecutiveActionItem" link type="primary" @click="openExecutiveActionItemDialog(scope.row)">编辑</el-button>
                    <el-button
                      v-if="canManageExecutiveActionItem && scope.row.actionStatus !== 'completed'"
                      link
                      type="success"
                      @click="handleExecutiveActionItemComplete(scope.row)"
                    >
                      完成
                    </el-button>
                    <el-button v-if="canManageExecutiveActionItem" link type="danger" @click="handleExecutiveActionItemRemove(scope.row)">删除</el-button>
                  </div>
                </template>
              </el-table-column>
            </el-table>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <ReportShortcutCardsSection
      :card-list="pendingAuditShortcutCardList"
      :column-span="8"
      @shortcut="handleDashboardShortcut"
    />

    <el-row :gutter="12" style="margin-top: 12px">
      <el-col :span="24">
        <el-card>
          <template #header>
            <div class="dashboard-section-header">
              <div class="dashboard-section-title-group">
                <span>核心经营指标</span>
              </div>
              <span class="dashboard-section-subtitle">汇总本月与上月营收、毛利、回款及环比变化</span>
            </div>
          </template>
          <el-alert
            v-if="!hasManagementCockpitData && !managementCockpitLoading"
            title="暂无当前期间经营汇总数据，请刷新页面或检查业务台账记录。"
            type="warning"
            :closable="false"
            show-icon
          />
          <template v-else>
            <el-row :gutter="12">
              <el-col :xs="24" :sm="12" :lg="6" style="margin-bottom: 12px" v-for="managementOverviewCard in managementOverviewCardList" :key="managementOverviewCard.key">
                <div class="management-overview-card">
                  <div class="management-overview-header">
                    <span class="management-overview-title">{{ managementOverviewCard.title }}</span>
                    <el-tag size="small" :type="managementOverviewCard.tagType">{{ managementOverviewCard.tagText }}</el-tag>
                  </div>
                  <div class="management-overview-value">{{ managementOverviewCard.value }}</div>
                  <div class="management-overview-desc">{{ managementOverviewCard.description }}</div>
                </div>
              </el-col>
            </el-row>
            <div class="management-insight-list">
              <div v-for="managementInsight in managementInsightList" :key="managementInsight" class="management-insight-item">
                {{ managementInsight }}
              </div>
            </div>
          </template>
        </el-card>
      </el-col>
    </el-row>

    <el-row :gutter="12" style="margin-top: 12px" v-if="hasManagementCockpitData">
      <el-col :span="24">
        <el-card>
          <template #header>
            <div class="dashboard-section-header">
              <div class="dashboard-section-title-group">
                <span>经营健康度</span>
                <el-tag size="small" :type="managementHealthSummary.tagType">{{ managementHealthSummary.levelText }}</el-tag>
              </div>
              <span class="dashboard-section-subtitle">把增长、回款、资金和流程四个维度收敛成统一健康评分，方便管理层先看整体状态</span>
            </div>
          </template>
          <div class="management-health-layout">
            <div class="management-health-summary">
              <div class="management-health-score-label">综合健康评分</div>
              <div class="management-health-score">{{ managementHealthSummary.score }}</div>
              <div class="management-health-summary-desc">{{ managementHealthSummary.description }}</div>
              <div class="management-health-summary-advice">{{ managementHealthSummary.adviceText }}</div>
              <div v-if="managementHealthSummary.actionLabel" class="management-health-summary-action">
                <span class="management-health-summary-action-label">建议先处理：{{ managementHealthSummary.weakestTitle }}</span>
                <el-button type="primary" @click="handleManagementHealthAction(managementHealthSummary)">{{ managementHealthSummary.actionLabel }}</el-button>
              </div>
            </div>
            <div class="management-health-dimension-list">
              <div v-for="managementHealthDimension in managementHealthDimensionList" :key="managementHealthDimension.key" class="management-health-dimension-item">
                <div class="management-health-dimension-header">
                  <span class="management-health-dimension-title">{{ managementHealthDimension.title }}</span>
                  <el-tag size="small" :type="managementHealthDimension.tagType">{{ managementHealthDimension.levelText }}</el-tag>
                </div>
                <div class="management-health-dimension-score">{{ managementHealthDimension.score }}</div>
                <div class="management-health-dimension-desc">{{ managementHealthDimension.description }}</div>
                <div class="management-health-dimension-footer">
                  <span class="management-health-dimension-advice">{{ managementHealthDimension.adviceText }}</span>
                  <el-button v-if="managementHealthDimension.actionLabel" type="primary" link @click="handleManagementHealthAction(managementHealthDimension)">{{ managementHealthDimension.actionLabel }}</el-button>
                </div>
              </div>
            </div>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <el-row
      ref="annualBudgetSectionRef"
      :class="{ 'todo-target-section': currentTodoTargetType === 'budget_approve' }"
      :gutter="12"
      style="margin-top: 12px"
      v-if="hasManagementCockpitData"
    >
      <el-col :span="8">
        <el-card>
          <template #header>风险提醒</template>
          <div class="risk-overview-list">
            <div v-for="riskOverviewCard in riskOverviewCardList" :key="riskOverviewCard.key" class="risk-overview-item">
              <div class="risk-overview-title">{{ riskOverviewCard.title }}</div>
              <div class="risk-overview-value">{{ riskOverviewCard.value }}</div>
              <div class="risk-overview-desc">{{ riskOverviewCard.description }}</div>
              <div class="risk-overview-action">
                <el-button type="primary" link @click="handleRiskOverviewAction(riskOverviewCard)">{{ riskOverviewCard.actionLabel }}</el-button>
              </div>
            </div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="8">
        <el-card>
          <template #header>
            <div class="dashboard-section-header">
              <span>应收账龄</span>
              <span class="dashboard-section-subtitle">点击区间下钻应收台账</span>
            </div>
          </template>
          <el-table :data="receivableAgingList" border size="small" style="cursor: pointer" @row-click="(row) => handleAgingBucketDrillDown('receivable', row)">
            <el-table-column label="账龄区间" align="center" prop="bucketName" min-width="105" />
            <el-table-column label="金额" align="right" min-width="110">
              <template #default="scope">
                <span>￥{{ formatAmountValue(scope.row.amount) }}</span>
              </template>
            </el-table-column>
            <el-table-column label="笔数" align="center" prop="recordCount" width="60" />
            <el-table-column label="下钻" align="center" width="62">
              <template #default="scope">
                <el-button link type="primary" size="small" @click.stop="handleAgingBucketDrillDown('receivable', scope.row)">明细</el-button>
              </template>
            </el-table-column>
          </el-table>
        </el-card>
      </el-col>
      <el-col :span="8">
        <el-card>
          <template #header>
            <div class="dashboard-section-header">
              <span>应付账龄</span>
              <span class="dashboard-section-subtitle">点击区间下钻应付台账</span>
            </div>
          </template>
          <el-table :data="payableAgingList" border size="small" style="cursor: pointer" @row-click="(row) => handleAgingBucketDrillDown('payable', row)">
            <el-table-column label="账龄区间" align="center" prop="bucketName" min-width="105" />
            <el-table-column label="金额" align="right" min-width="110">
              <template #default="scope">
                <span>￥{{ formatAmountValue(scope.row.amount) }}</span>
              </template>
            </el-table-column>
            <el-table-column label="笔数" align="center" prop="recordCount" width="60" />
            <el-table-column label="下钻" align="center" width="62">
              <template #default="scope">
                <el-button link type="primary" size="small" @click.stop="handleAgingBucketDrillDown('payable', scope.row)">明细</el-button>
              </template>
            </el-table-column>
          </el-table>
        </el-card>
      </el-col>
    </el-row>

    <el-row :gutter="12" style="margin-top: 12px" v-if="hasManagementCockpitData">
      <el-col :xs="24" :lg="12" style="margin-bottom: 12px">
        <el-card>
          <template #header>高风险客户 Top5</template>
          <el-table :data="topRiskCustomerList" border size="small">
            <el-table-column label="客户" align="left" prop="targetName" min-width="180" show-overflow-tooltip />
            <el-table-column label="逾期金额" align="right" min-width="120">
              <template #default="scope">
                <span>￥{{ formatAmountValue(scope.row.remainAmount) }}</span>
              </template>
            </el-table-column>
            <el-table-column label="最长逾期" align="center" min-width="100">
              <template #default="scope">
                <span>{{ scope.row.overdueDays }}天</span>
              </template>
            </el-table-column>
            <el-table-column label="操作" align="center" width="120">
              <template #default="scope">
                <el-button link type="primary" :disabled="!canViewRiskCustomerDetail(scope.row)" @click="handleTopRiskCustomerAction(scope.row)">查看台账</el-button>
              </template>
            </el-table-column>
          </el-table>
        </el-card>
      </el-col>
      <el-col :xs="24" :lg="12" style="margin-bottom: 12px">
        <el-card>
          <template #header>重点付款供应商 Top5</template>
          <el-table :data="topRiskSupplierList" border size="small">
            <el-table-column label="供应商" align="left" prop="targetName" min-width="180" show-overflow-tooltip />
            <el-table-column label="待付款" align="right" min-width="120">
              <template #default="scope">
                <span>￥{{ formatAmountValue(scope.row.remainAmount) }}</span>
              </template>
            </el-table-column>
            <el-table-column label="付款风险" align="center" min-width="120">
              <template #default="scope">
                <span>{{ getDueRiskText(scope.row.dueInDays) }}</span>
              </template>
            </el-table-column>
            <el-table-column label="操作" align="center" width="120">
              <template #default="scope">
                <el-button link type="primary" :disabled="!canViewRiskSupplierDetail(scope.row)" @click="handleTopRiskSupplierAction(scope.row)">查看台账</el-button>
              </template>
            </el-table-column>
          </el-table>
        </el-card>
      </el-col>
    </el-row>

    <el-row :gutter="12" style="margin-top: 12px" v-if="hasManagementCockpitData">
      <el-col :xs="24" :lg="12" style="margin-bottom: 12px">
        <el-card>
          <template #header>
            <div class="dashboard-section-header">
              <div class="dashboard-section-title-group">
                <span>经营目标</span>
                <el-tag size="small" :type="hasDashboardTargetConfigured ? 'success' : 'warning'">
                  {{ hasDashboardTargetConfigured ? "已配置" : "待配置" }}
                </el-tag>
              </div>
              <el-button v-if="canManageDashboardTarget" link type="primary" @click="openDashboardTargetDialog">维护目标</el-button>
            </div>
          </template>
          <div v-loading="dashboardTargetLoading">
            <div class="dashboard-target-month-summary">
              <div class="dashboard-target-month-header">
                <span>本月执行节奏</span>
                <span>已过 {{ currentMonthProgressData.elapsedDayCount }} / {{ currentMonthProgressData.totalDayCount }} 天，剩余 {{ currentMonthProgressData.remainingDayCount }} 天</span>
              </div>
              <el-progress :percentage="currentMonthProgressData.monthProgressPercentage" :stroke-width="10" />
            </div>
            <el-empty v-if="!hasDashboardTargetConfigured" description="暂未配置本月经营目标，建议尽快补齐销售、毛利和回款率目标。" :image-size="88">
              <el-button v-if="canManageDashboardTarget" type="primary" @click="openDashboardTargetDialog">立即配置</el-button>
            </el-empty>
            <div v-else class="dashboard-target-list">
              <div v-for="dashboardTargetCard in dashboardTargetCardList" :key="dashboardTargetCard.key" class="dashboard-target-item">
                <div class="dashboard-target-header">
                  <span class="dashboard-target-title">{{ dashboardTargetCard.title }}</span>
                  <el-tag size="small" :type="dashboardTargetCard.tagType">{{ dashboardTargetCard.progressText }}</el-tag>
                </div>
                <div class="dashboard-target-value-row">
                  <span>当前：{{ dashboardTargetCard.currentValueText }}</span>
                  <span>目标：{{ dashboardTargetCard.targetValueText }}</span>
                </div>
                <el-progress :percentage="dashboardTargetCard.progressPercentage" :status="dashboardTargetCard.progressStatus" :stroke-width="10" />
                <div class="dashboard-target-desc">{{ dashboardTargetCard.differenceText }}</div>
                <div class="dashboard-target-pace-row">
                  <el-tag size="small" :type="dashboardTargetCard.paceTagType">{{ dashboardTargetCard.paceTagText }}</el-tag>
                  <span class="dashboard-target-pace-text">{{ dashboardTargetCard.paceText }}</span>
                </div>
                <div class="dashboard-target-forecast-text">{{ dashboardTargetCard.forecastText }}</div>
              </div>
            </div>
          </div>
        </el-card>
      </el-col>
      <el-col :xs="24" :lg="12" style="margin-bottom: 12px">
        <el-card>
          <template #header>资金压力窗口</template>
          <div v-loading="cashFlowWindowLoading">
            <el-empty v-if="!cashFlowWindowList.length" description="暂未生成近期资金压力窗口数据。" :image-size="88" />
            <div v-else class="cash-flow-window-list">
              <div v-for="cashFlowWindowItem in cashFlowWindowList" :key="cashFlowWindowItem.forecastDays" class="cash-flow-window-item">
                <div class="cash-flow-window-header">
                  <span class="cash-flow-window-title">{{ cashFlowWindowItem.forecastDays }}天窗口</span>
                  <el-tag size="small" :type="cashFlowWindowItem.tagType">{{ cashFlowWindowItem.tagText }}</el-tag>
                </div>
                <div class="cash-flow-window-metrics">
                  <div class="cash-flow-window-metric">
                    <div class="cash-flow-window-label">预计流入</div>
                    <div class="cash-flow-window-value">￥{{ formatAmountValue(cashFlowWindowItem.receivableAmount) }}</div>
                  </div>
                  <div class="cash-flow-window-metric">
                    <div class="cash-flow-window-label">预计流出</div>
                    <div class="cash-flow-window-value">￥{{ formatAmountValue(cashFlowWindowItem.payableAmount) }}</div>
                  </div>
                  <div class="cash-flow-window-metric">
                    <div class="cash-flow-window-label">净现金流</div>
                    <div class="cash-flow-window-value">￥{{ formatAmountValue(cashFlowWindowItem.netCashFlow) }}</div>
                  </div>
                </div>
                <div class="cash-flow-window-desc">{{ cashFlowWindowItem.description }}</div>
              </div>
            </div>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <el-row :gutter="12" style="margin-top: 12px" v-if="hasManagementCockpitData">
      <el-col :span="24">
        <el-card>
          <template #header>
            <div class="dashboard-section-header">
              <div class="dashboard-section-title-group">
                <span>经营预警待办</span>
                <el-tag size="small" :type="managementTodoList.length ? 'danger' : 'success'">
                  {{ managementTodoList.length ? `${managementTodoList.length}项待跟进` : "暂无高优先级风险" }}
                </el-tag>
              </div>
              <span class="dashboard-section-subtitle">把最需要管理层推动的事项集中出来，减少在多个模块间来回查找</span>
            </div>
          </template>
          <el-empty v-if="!managementTodoList.length" description="当前没有需要管理层立即介入的高优先级事项。" :image-size="88" />
          <div v-else class="management-todo-list">
            <div v-for="managementTodoItem in managementTodoList" :key="managementTodoItem.key" class="management-todo-item">
              <div class="management-todo-main">
                <div class="management-todo-header">
                  <div class="management-todo-title-group">
                    <span class="management-todo-title">{{ managementTodoItem.title }}</span>
                    <el-tag size="small" :type="managementTodoItem.levelType">{{ managementTodoItem.levelText }}</el-tag>
                  </div>
                  <span class="management-todo-metric">{{ managementTodoItem.metricText }}</span>
                </div>
                <div class="management-todo-desc">{{ managementTodoItem.description }}</div>
              </div>
              <div class="management-todo-action">
                <el-button type="primary" plain @click="handleManagementTodoAction(managementTodoItem)">{{ managementTodoItem.actionLabel }}</el-button>
              </div>
            </div>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <el-row :gutter="12" style="margin-top: 12px" v-if="hasManagementCockpitData">
      <el-col :xs="24" :lg="12" style="margin-bottom: 12px">
        <el-card>
          <template #header>
            <div class="dashboard-section-header">
              <div class="dashboard-section-title-group">
                <span>预算执行与预测</span>
                <el-tag size="small" :type="hasDecisionBudgetConfigured ? 'success' : 'warning'">
                  {{ hasDecisionBudgetConfigured ? "已配置预算" : "待配置预算" }}
                </el-tag>
              </div>
              <el-button v-if="canManageDashboardTarget" link type="primary" @click="openDecisionBudgetDialog">维护预算</el-button>
            </div>
          </template>
          <div v-loading="decisionSupportLoading || decisionBudgetLoading">
            <el-empty v-if="!hasDecisionSupportData" description="正在生成预算执行与预测数据。" :image-size="88" />
            <template v-else>
              <el-alert
                v-if="!hasDecisionBudgetConfigured"
                title="当前还未配置本月经营预算，系统已先展示实际值和趋势预测，建议尽快补齐预算基线。"
                type="warning"
                :closable="false"
                show-icon
                style="margin-bottom: 12px"
              />
              <div class="decision-budget-list">
                <div v-for="decisionBudgetCard in decisionBudgetCardList" :key="decisionBudgetCard.key" class="decision-budget-item">
                  <div class="decision-budget-header">
                    <span class="decision-budget-title">{{ decisionBudgetCard.title }}</span>
                    <el-tag size="small" :type="decisionBudgetCard.tagType">{{ decisionBudgetCard.paceTagText }}</el-tag>
                  </div>
                  <div class="decision-budget-value-row">
                    <span>实际：{{ decisionBudgetCard.actualValueText }}</span>
                    <span>预算：{{ decisionBudgetCard.budgetValueText }}</span>
                  </div>
                  <el-progress :percentage="decisionBudgetCard.progressPercentage" :stroke-width="10" />
                  <div class="decision-budget-desc">{{ decisionBudgetCard.differenceText }}</div>
                  <div class="decision-budget-forecast-row">
                    <span>{{ decisionBudgetCard.forecastRateText }}</span>
                    <span>预测：{{ decisionBudgetCard.forecastValueText }}</span>
                  </div>
                  <div class="decision-budget-forecast-text">{{ decisionBudgetCard.forecastText }}</div>
                </div>
              </div>
              <div class="decision-budget-insight-list">
                <div v-for="decisionBudgetInsight in decisionBudgetInsightList" :key="decisionBudgetInsight" class="decision-budget-insight-item">
                  {{ decisionBudgetInsight }}
                </div>
              </div>
            </template>
          </div>
        </el-card>
      </el-col>
      <el-col :xs="24" :lg="12" style="margin-bottom: 12px">
        <el-card>
          <template #header>
            <div class="dashboard-section-header">
              <div class="dashboard-section-title-group">
                <span>现金流情景模拟</span>
                <el-tag size="small" :type="simulatedCashScenarioData.riskTagType">{{ simulatedCashScenarioData.riskTagText }}</el-tag>
              </div>
              <el-button link type="primary" @click="resetCashScenarioForm">重置场景</el-button>
            </div>
          </template>
          <div v-loading="decisionSupportLoading">
            <el-empty v-if="!hasDecisionSupportData" description="正在生成现金流模拟基准数据。" :image-size="88" />
            <template v-else>
              <div class="cash-scenario-summary-list">
                <div class="cash-scenario-summary-item">
                  <div class="cash-scenario-summary-title">30天基准流入</div>
                  <div class="cash-scenario-summary-value">￥{{ formatAmountValue(cashScenarioBaseData.baseInflowAmount) }}</div>
                  <div class="cash-scenario-summary-desc">本月已回款 ￥{{ formatAmountValue(cashScenarioBaseData.currentMonthCollectionAmount) }}</div>
                </div>
                <div class="cash-scenario-summary-item">
                  <div class="cash-scenario-summary-title">30天基准流出</div>
                  <div class="cash-scenario-summary-value">￥{{ formatAmountValue(cashScenarioBaseData.baseOutflowAmount) }}</div>
                  <div class="cash-scenario-summary-desc">本月已付款 ￥{{ formatAmountValue(cashScenarioBaseData.currentMonthPaymentAmount) }}</div>
                </div>
                <div class="cash-scenario-summary-item">
                  <div class="cash-scenario-summary-title">30天基准净额</div>
                  <div class="cash-scenario-summary-value">￥{{ formatAmountValue(cashScenarioBaseData.baseNetCashFlowAmount) }}</div>
                  <div class="cash-scenario-summary-desc">用于评估经营动作对近期现金压力的影响</div>
                </div>
              </div>
              <el-form :model="cashScenarioForm" label-position="top" class="cash-scenario-form">
                <el-row :gutter="12">
                  <el-col :xs="24" :sm="12">
                    <el-form-item label="延迟回款金额">
                      <el-input-number v-model="cashScenarioForm.delayedCollectionAmount" :min="0" :precision="2" controls-position="right" style="width: 100%" />
                    </el-form-item>
                  </el-col>
                  <el-col :xs="24" :sm="12">
                    <el-form-item label="提前付款金额">
                      <el-input-number v-model="cashScenarioForm.acceleratedPaymentAmount" :min="0" :precision="2" controls-position="right" style="width: 100%" />
                    </el-form-item>
                  </el-col>
                  <el-col :xs="24" :sm="12">
                    <el-form-item label="新增销售回款">
                      <el-input-number v-model="cashScenarioForm.additionalSaleCollectionAmount" :min="0" :precision="2" controls-position="right" style="width: 100%" />
                    </el-form-item>
                  </el-col>
                  <el-col :xs="24" :sm="12">
                    <el-form-item label="新增采购支出">
                      <el-input-number v-model="cashScenarioForm.additionalProcurementAmount" :min="0" :precision="2" controls-position="right" style="width: 100%" />
                    </el-form-item>
                  </el-col>
                  <el-col :span="24">
                    <el-form-item label="临时融资金额">
                      <el-input-number v-model="cashScenarioForm.temporaryFinancingAmount" :min="0" :precision="2" controls-position="right" style="width: 100%" />
                    </el-form-item>
                  </el-col>
                </el-row>
              </el-form>
              <div class="cash-scenario-result-list">
                <div class="cash-scenario-result-item">
                  <div class="cash-scenario-result-title">模拟流入</div>
                  <div class="cash-scenario-result-value">￥{{ formatAmountValue(simulatedCashScenarioData.simulatedInflowAmount) }}</div>
                </div>
                <div class="cash-scenario-result-item">
                  <div class="cash-scenario-result-title">模拟流出</div>
                  <div class="cash-scenario-result-value">￥{{ formatAmountValue(simulatedCashScenarioData.simulatedOutflowAmount) }}</div>
                </div>
                <div class="cash-scenario-result-item">
                  <div class="cash-scenario-result-title">模拟净现金流</div>
                  <div class="cash-scenario-result-value">￥{{ formatAmountValue(simulatedCashScenarioData.simulatedNetCashFlowAmount) }}</div>
                </div>
              </div>
              <div class="cash-scenario-impact-text">
                相比基准场景，当前经营动作对 30 天净现金流的影响为
                <span :class="simulatedCashScenarioData.impactAmount >= 0 ? 'decision-positive-text' : 'decision-negative-text'">
                  ￥{{ formatAmountValue(Math.abs(simulatedCashScenarioData.impactAmount)) }}
                </span>
                {{ simulatedCashScenarioData.impactAmount >= 0 ? "改善" : "减少" }}。
              </div>
            </template>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <el-row :gutter="12" style="margin-top: 12px" v-if="hasManagementCockpitData">
      <el-col :xs="24" :lg="12" style="margin-bottom: 12px">
        <el-card>
          <template #header>
            <div class="dashboard-section-header">
              <div class="dashboard-section-title-group">
                <span>客户利润与集中度</span>
                <el-tag size="small" :type="customerProfitAnalysisData.concentration?.dependencyRiskText === '高依赖' ? 'danger' : 'info'">
                  {{ customerProfitAnalysisData.concentration?.dependencyRiskText || "待评估" }}
                </el-tag>
              </div>
              <span class="dashboard-section-subtitle">兼顾客户利润、回款和依赖度，帮助管理层识别高价值客户与结构性风险</span>
            </div>
          </template>
          <div v-loading="decisionSupportLoading">
            <el-empty v-if="!hasDecisionSupportData" description="正在生成客户利润分析数据。" :image-size="88" />
            <template v-else>
              <div class="customer-concentration-list">
                <div v-for="customerConcentrationCard in customerConcentrationCardList" :key="customerConcentrationCard.key" class="customer-concentration-item">
                  <div class="customer-concentration-title">{{ customerConcentrationCard.title }}</div>
                  <div class="customer-concentration-value">{{ customerConcentrationCard.value }}</div>
                  <div class="customer-concentration-desc">{{ customerConcentrationCard.description }}</div>
                </div>
              </div>
              <el-table :data="customerProfitTopList" border size="small">
                <el-table-column label="客户" align="left" prop="targetName" min-width="150" show-overflow-tooltip />
                <el-table-column label="等级" align="center" prop="customerLevel" width="96">
                  <template #default="scope">
                    <el-tag size="small" :type="['vip', 'gold', 'a', 'VIP'].includes(String(scope.row.customerLevel || '').toLowerCase()) ? 'warning' : 'primary'" effect="light">
                      {{ formatCustomerLevelLabel(scope.row.customerLevel) }}
                    </el-tag>
                  </template>
                </el-table-column>
                <el-table-column label="销售额" align="right" min-width="110">
                  <template #default="scope">
                    <span>￥{{ formatAmountValue(scope.row.saleAmount) }}</span>
                  </template>
                </el-table-column>
                <el-table-column label="毛利额" align="right" min-width="110">
                  <template #default="scope">
                    <span v-if="scope.row.grossProfitAmount === null || scope.row.grossProfitAmount === undefined" class="erp-cell-sub">权限脱敏</span>
                    <span v-else>￥{{ formatAmountValue(scope.row.grossProfitAmount) }}</span>
                  </template>
                </el-table-column>
                <el-table-column label="毛利率" align="center" min-width="90">
                  <template #default="scope">
                    <span v-if="scope.row.grossMarginRate === null || scope.row.grossMarginRate === undefined" class="erp-cell-sub">权限脱敏</span>
                    <span v-else>{{ formatRateValue(scope.row.grossMarginRate) }}</span>
                  </template>
                </el-table-column>
                <el-table-column label="逾期金额" align="right" min-width="110">
                  <template #default="scope">
                    <span>￥{{ formatAmountValue(scope.row.overdueAmount) }}</span>
                  </template>
                </el-table-column>
                <el-table-column label="回款率" align="center" min-width="90">
                  <template #default="scope">
                    <span>{{ formatRateValue(scope.row.collectionRate) }}</span>
                  </template>
                </el-table-column>
                <el-table-column label="操作" align="center" width="100">
                  <template #default="scope">
                    <el-button link type="primary" @click="handleCustomerProfitAction(scope.row)">查看动作</el-button>
                  </template>
                </el-table-column>
              </el-table>
            </template>
          </div>
        </el-card>
      </el-col>
      <el-col :xs="24" :lg="12" style="margin-bottom: 12px">
        <el-card>
          <template #header>
            <div class="dashboard-section-header">
              <div class="dashboard-section-title-group">
                <span>库存周转与滞销决策</span>
                <el-tag size="small" :type="normalizeNumberValue(inventoryDecisionAnalysisData.slowMovingStockValueAmount) > 0 ? 'warning' : 'success'">
                  {{ normalizeNumberValue(inventoryDecisionAnalysisData.slowMovingStockValueAmount) > 0 ? "存在滞销压力" : "周转平稳" }}
                </el-tag>
              </div>
              <span class="dashboard-section-subtitle">聚焦库存占资、周转天数和滞销商品，帮助管理层决定去库存还是补库存</span>
            </div>
          </template>
          <div v-loading="decisionSupportLoading">
            <el-empty v-if="!hasDecisionSupportData" description="正在生成库存决策分析数据。" :image-size="88" />
            <template v-else>
              <div class="inventory-decision-card-list">
                <div v-for="inventoryDecisionCard in inventoryDecisionCardList" :key="inventoryDecisionCard.key" class="inventory-decision-card">
                  <div class="inventory-decision-title">{{ inventoryDecisionCard.title }}</div>
                  <div class="inventory-decision-value">{{ inventoryDecisionCard.value }}</div>
                  <div class="inventory-decision-desc">{{ inventoryDecisionCard.description }}</div>
                </div>
              </div>
              <el-table :data="inventorySlowMovingProductList" border size="small">
                <el-table-column label="商品" align="left" prop="productName" min-width="150" show-overflow-tooltip />
                <el-table-column label="品类" align="center" prop="categoryName" min-width="100" show-overflow-tooltip />
                <el-table-column label="库存数量" align="right" min-width="100">
                  <template #default="scope">
                    <span>{{ formatAmountValue(scope.row.quantity) }} {{ scope.row.unitName }}</span>
                  </template>
                </el-table-column>
                <el-table-column label="库存货值" align="right" min-width="110">
                  <template #default="scope">
                    <span>￥{{ formatAmountValue(scope.row.stockValueAmount) }}</span>
                  </template>
                </el-table-column>
                <el-table-column label="滞销情况" align="center" min-width="110">
                  <template #default="scope">
                    <span>{{ scope.row.slowMovingText }}</span>
                  </template>
                </el-table-column>
                <el-table-column label="操作" align="center" width="100">
                  <template #default="scope">
                    <el-button link type="primary" @click="handleInventoryProductAction(scope.row)">查看流水</el-button>
                  </template>
                </el-table-column>
              </el-table>
            </template>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <el-row :gutter="12" style="margin-top: 12px" v-if="hasManagementCockpitData">
      <el-col :xs="24" :lg="12" style="margin-bottom: 12px">
        <el-card>
          <template #header>
            <div class="dashboard-section-header">
              <div class="dashboard-section-title-group">
                <span>组织贡献分析</span>
                <el-tag size="small" type="info">{{ activeOrganizationContributionTitle }}</el-tag>
              </div>
              <span class="dashboard-section-subtitle">从品类、区域和销售员三个维度看贡献结构，辅助资源配置和组织激励</span>
            </div>
          </template>
          <div v-loading="decisionSupportLoading">
            <el-empty v-if="!hasDecisionSupportData" description="正在生成组织贡献排行数据。" :image-size="88" />
            <template v-else>
              <el-tabs v-model="organizationContributionTab">
                <el-tab-pane label="品类" name="category" />
                <el-tab-pane label="区域" name="region" />
                <el-tab-pane label="销售员" name="salesperson" />
              </el-tabs>
              <el-table :data="activeOrganizationContributionList" border size="small">
                <el-table-column label="排名" align="center" width="70">
                  <template #default="scope">
                    <span>{{ scope.$index + 1 }}</span>
                  </template>
                </el-table-column>
                <el-table-column label="维度名称" align="left" prop="targetName" min-width="140" show-overflow-tooltip />
                <el-table-column label="销售额" align="right" min-width="110">
                  <template #default="scope">
                    <span>￥{{ formatAmountValue(scope.row.saleAmount) }}</span>
                  </template>
                </el-table-column>
                <el-table-column label="毛利额" align="right" min-width="110">
                  <template #default="scope">
                    <span v-if="scope.row.grossProfitAmount === null || scope.row.grossProfitAmount === undefined" class="erp-cell-sub">权限脱敏</span>
                    <span v-else>￥{{ formatAmountValue(scope.row.grossProfitAmount) }}</span>
                  </template>
                </el-table-column>
                <el-table-column label="毛利率" align="center" min-width="90">
                  <template #default="scope">
                    <span v-if="scope.row.grossMarginRate === null || scope.row.grossMarginRate === undefined" class="erp-cell-sub">权限脱敏</span>
                    <span v-else>{{ formatRateValue(scope.row.grossMarginRate) }}</span>
                  </template>
                </el-table-column>
                <el-table-column label="单据数" align="center" prop="orderCount" width="80" />
              </el-table>
            </template>
          </div>
        </el-card>
      </el-col>
      <el-col :xs="24" :lg="12" style="margin-bottom: 12px">
        <el-card>
          <template #header>
            <div class="dashboard-section-header">
              <div class="dashboard-section-title-group">
                <span>管理层经营简报</span>
                <el-tag size="small" type="success">会议可直接使用</el-tag>
              </div>
              <div>
                <el-button link type="primary" @click="copyExecutiveBriefText">复制简报</el-button>
                <el-button link type="primary" @click="openExecutiveBriefDialog">展开预览</el-button>
              </div>
            </div>
          </template>
          <div v-loading="decisionSupportLoading">
            <el-empty v-if="!hasDecisionSupportData" description="正在生成经营简报。" :image-size="88" />
            <template v-else>
              <div class="executive-brief-meta">生成时间：{{ executiveBriefData.generatedAtText || "-" }}</div>
              <div v-for="executiveBriefSection in executiveBriefSectionList" :key="executiveBriefSection.key" class="executive-brief-section">
                <div class="executive-brief-section-title">{{ executiveBriefSection.title }}</div>
                <div v-if="executiveBriefSection.textList?.length" class="executive-brief-section-body">
                  <div v-for="executiveBriefText in executiveBriefSection.textList" :key="executiveBriefText" class="executive-brief-item">
                    {{ executiveBriefText }}
                  </div>
                </div>
                <div v-else class="decision-support-muted">当前暂无可展示内容。</div>
              </div>
            </template>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <el-row :gutter="12" style="margin-top: 12px" v-if="hasManagementCockpitData">
      <el-col :span="24">
        <el-card>
          <template #header>
            <div class="dashboard-section-header">
              <div class="dashboard-section-title-group">
                <span>年度预算拆解与审批</span>
                <el-tag size="small" :type="annualBudgetPlanStatusTagType">{{ annualBudgetPlanStatusText }}</el-tag>
              </div>
              <div class="annual-budget-header-action">
                <el-input-number v-model="annualBudgetYear" :min="2020" :max="2099" controls-position="right" size="small" class="annual-budget-year-input" />
                <el-button link type="primary" @click="loadAnnualBudgetPlanData">切换年度</el-button>
                <el-button v-if="canManageAnnualBudgetPlan" link type="primary" @click="openAnnualBudgetPlanDialog">编辑预算</el-button>
                <el-button v-if="canCreateAnnualBudgetPlanVersion" link type="primary" @click="openAnnualBudgetPlanVersionDialog">新建版本</el-button>
              </div>
            </div>
          </template>
          <div v-loading="annualBudgetPlanLoading">
            <div class="annual-budget-meta-row">
              <div class="annual-budget-meta-item">
                <span class="annual-budget-meta-label">预算名称</span>
                <span class="annual-budget-meta-value">{{ annualBudgetPlanData.planName || `${annualBudgetYear}年度经营预算` }}</span>
              </div>
              <div class="annual-budget-meta-item">
                <span class="annual-budget-meta-label">版本信息</span>
                <span class="annual-budget-meta-value">
                  {{ annualBudgetPlanData.versionLabel || `v${annualBudgetPlanData.versionNo || 1}` }}
                  <el-tag size="small" :type="annualBudgetPlanEffectiveTagType" style="margin-left: 8px">
                    {{ annualBudgetPlanEffectiveText }}
                  </el-tag>
                </span>
              </div>
              <div class="annual-budget-meta-item">
                <span class="annual-budget-meta-label">提交信息</span>
                <span class="annual-budget-meta-value">{{ annualBudgetPlanData.submitBy ? `${annualBudgetPlanData.submitBy} / ${parseTime(annualBudgetPlanData.submitTime)}` : "暂未提交" }}</span>
              </div>
              <div class="annual-budget-meta-item">
                <span class="annual-budget-meta-label">审批信息</span>
                <span class="annual-budget-meta-value">{{ annualBudgetPlanData.approveBy ? `${annualBudgetPlanData.approveBy} / ${parseTime(annualBudgetPlanData.approveTime)}` : "暂未审批" }}</span>
              </div>
              <div class="annual-budget-meta-item annual-budget-meta-item-wide">
                <span class="annual-budget-meta-label">调整原因</span>
                <span class="annual-budget-meta-value">{{ annualBudgetPlanData.adjustmentReason || "当前版本未填写调整原因" }}</span>
              </div>
            </div>
            <div class="annual-budget-summary-list">
              <div v-for="annualBudgetPlanSummaryCard in annualBudgetPlanSummaryCardList" :key="annualBudgetPlanSummaryCard.key" class="annual-budget-summary-item">
                <div class="annual-budget-summary-title">{{ annualBudgetPlanSummaryCard.title }}</div>
                <div class="annual-budget-summary-value">￥{{ formatAmountValue(annualBudgetPlanSummaryCard.amount) }}</div>
                <div class="annual-budget-summary-desc">当月拆解：￥{{ formatAmountValue(annualBudgetPlanSummaryCard.monthAmount) }}</div>
              </div>
            </div>
            <div class="annual-budget-action-row">
              <el-button v-if="canManageAnnualBudgetPlan" type="primary" plain :disabled="!annualBudgetPlanData.planId || annualBudgetPlanData.planStatus === 'submitted' || annualBudgetPlanData.planStatus === 'approved'" @click="handleAnnualBudgetPlanSubmit">提交审批</el-button>
              <el-button v-if="canApproveAnnualBudgetPlan" type="success" plain :disabled="annualBudgetPlanData.planStatus !== 'submitted'" @click="openAnnualBudgetApprovalDialog('approve')">审批通过</el-button>
              <el-button v-if="canApproveAnnualBudgetPlan" type="danger" plain :disabled="annualBudgetPlanData.planStatus !== 'submitted'" @click="openAnnualBudgetApprovalDialog('reject')">驳回预算</el-button>
              <span v-if="annualBudgetPlanData.approveRemark" class="annual-budget-remark-text">审批意见：{{ annualBudgetPlanData.approveRemark }}</span>
            </div>
            <el-table :data="annualBudgetMonthlyTableList" border size="small">
              <el-table-column label="月份" align="center" prop="monthLabel" width="70" />
              <el-table-column label="销售预算" align="right" min-width="100">
                <template #default="scope">
                  <span>￥{{ formatAmountValue(scope.row.saleBudgetAmount) }}</span>
                </template>
              </el-table-column>
              <el-table-column label="毛利预算" align="right" min-width="100">
                <template #default="scope">
                  <span v-if="scope.row.grossProfitBudgetAmount === null || scope.row.grossProfitBudgetAmount === undefined" class="erp-cell-sub">权限脱敏</span>
                  <span v-else>￥{{ formatAmountValue(scope.row.grossProfitBudgetAmount) }}</span>
                </template>
              </el-table-column>
              <el-table-column label="回款预算" align="right" min-width="100">
                <template #default="scope">
                  <span>￥{{ formatAmountValue(scope.row.collectionBudgetAmount) }}</span>
                </template>
              </el-table-column>
              <el-table-column label="采购预算" align="right" min-width="100">
                <template #default="scope">
                  <span>￥{{ formatAmountValue(scope.row.purchaseBudgetAmount) }}</span>
                </template>
              </el-table-column>
              <el-table-column label="净现金流" align="right" min-width="110">
                <template #default="scope">
                  <span>￥{{ formatAmountValue(scope.row.netCashBudgetAmount) }}</span>
                </template>
              </el-table-column>
            </el-table>
            <div class="annual-budget-history-title">版本历史</div>
            <el-empty v-if="!annualBudgetPlanHistoryList.length" description="当前年度还没有预算版本记录。" :image-size="68" />
            <el-table v-else :data="annualBudgetPlanHistoryList" border size="small">
              <el-table-column label="版本" align="center" min-width="100">
                <template #default="scope">
                  <span>{{ scope.row.versionLabel || `v${scope.row.versionNo || 1}` }}</span>
                </template>
              </el-table-column>
              <el-table-column label="状态" align="center" min-width="100">
                <template #default="scope">
                  <el-tag size="small" :type="scope.row.planStatus === 'approved' ? 'success' : (scope.row.planStatus === 'submitted' ? 'warning' : (scope.row.planStatus === 'rejected' ? 'danger' : 'info'))">
                    {{ scope.row.planStatus === "approved" ? "已通过" : (scope.row.planStatus === "submitted" ? "待审批" : (scope.row.planStatus === "rejected" ? "已驳回" : "草稿")) }}
                  </el-tag>
                </template>
              </el-table-column>
              <el-table-column label="是否生效" align="center" min-width="100">
                <template #default="scope">
                  <el-tag size="small" :type="scope.row.effectiveFlag === 'y' ? 'success' : 'info'">
                    {{ scope.row.effectiveFlag === "y" ? "生效中" : "未生效" }}
                  </el-tag>
                </template>
              </el-table-column>
              <el-table-column label="调整原因" align="left" prop="adjustmentReason" min-width="240" show-overflow-tooltip />
              <el-table-column label="审批信息" align="center" min-width="180">
                <template #default="scope">
                  <span>{{ scope.row.approveBy ? `${scope.row.approveBy} / ${parseTime(scope.row.approveTime, '{y}-{m}-{d}')}` : "-" }}</span>
                </template>
              </el-table-column>
            </el-table>
          </div>
        </el-card>
      </el-col>
      <el-col :span="24" style="margin-top: 12px">
        <el-card>
          <template #header>
            <div class="dashboard-section-header">
              <div class="dashboard-section-title-group">
                <span>经营简报归档</span>
                <el-tag size="small" type="info">{{ executiveBriefRecordList.length }}条归档</el-tag>
              </div>
              <el-button v-if="canArchiveExecutiveBriefRecord" link type="primary" @click="archiveCurrentExecutiveBrief">归档本期简报</el-button>
            </div>
          </template>
          <div v-loading="executiveBriefRecordLoading">
            <el-empty v-if="!executiveBriefRecordList.length" description="当前还没有经营简报归档记录。" :image-size="88" />
            <el-table v-else :data="executiveBriefRecordList" border size="small">
              <el-table-column label="日期" align="center" width="110">
                <template #default="scope">
                  <span>{{ parseTime(scope.row.briefDate, '{y}-{m}-{d}') }}</span>
                </template>
              </el-table-column>
              <el-table-column label="简报标题" align="left" prop="briefTitle" min-width="180" show-overflow-tooltip />
              <el-table-column label="状态" align="center" width="90">
                <template #default="scope">
                  <el-tag size="small" :type="scope.row.briefStatus === 'archived' ? 'success' : 'info'">
                    {{ scope.row.briefStatus === "archived" ? "已归档" : scope.row.briefStatus }}
                  </el-tag>
                </template>
              </el-table-column>
              <el-table-column label="归档人" align="center" prop="createBy" width="100" />
              <el-table-column label="操作" align="center" width="100">
                <template #default="scope">
                  <el-button link type="primary" @click="openExecutiveBriefRecordPreview(scope.row)">查看详情</el-button>
                </template>
              </el-table-column>
            </el-table>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <el-row :gutter="12" style="margin-top: 12px" v-if="hasManagementCockpitData">
      <el-col :span="24">
        <el-card>
          <template #header>
            <div class="dashboard-section-header">
              <div class="dashboard-section-title-group">
                <span>经营快照归档</span>
                <el-tag size="small" type="info">{{ operationSnapshotList.length }}条快照</el-tag>
              </div>
              <div class="annual-budget-header-action">
                <el-button v-if="canArchiveExecutiveBriefRecord" link type="primary" @click="archiveOperationSnapshot('daily')">归档日报</el-button>
                <el-button v-if="canArchiveExecutiveBriefRecord" link type="primary" @click="archiveOperationSnapshot('weekly')">归档周报</el-button>
                <el-button v-if="canArchiveExecutiveBriefRecord" link type="primary" @click="archiveOperationSnapshot('monthly')">归档月报</el-button>
              </div>
            </div>
          </template>
          <div v-loading="operationSnapshotLoading">
            <el-empty v-if="!operationSnapshotList.length" description="当前还没有经营快照归档记录。" :image-size="88" />
            <el-table v-else :data="operationSnapshotList" border size="small">
              <el-table-column label="日期" align="center" width="110">
                <template #default="scope">
                  <span>{{ parseTime(scope.row.snapshotDate, '{y}-{m}-{d}') }}</span>
                </template>
              </el-table-column>
              <el-table-column label="类型" align="center" width="100">
                <template #default="scope">
                  <el-tag size="small" :type="getOperationSnapshotTypeTagType(scope.row.snapshotType)">
                    {{ getOperationSnapshotTypeLabel(scope.row.snapshotType) }}
                  </el-tag>
                </template>
              </el-table-column>
              <el-table-column label="快照标题" align="left" prop="snapshotTitle" min-width="180" show-overflow-tooltip />
              <el-table-column label="销售额" align="right" min-width="120">
                <template #default="scope">
                  <span>￥{{ formatAmountValue(scope.row.saleAmount) }}</span>
                </template>
              </el-table-column>
              <el-table-column label="毛利额" align="right" min-width="120">
                <template #default="scope">
                  <span v-if="scope.row.grossProfitAmount === null || scope.row.grossProfitAmount === undefined" class="erp-cell-sub">权限脱敏</span>
                  <span v-else>￥{{ formatAmountValue(scope.row.grossProfitAmount) }}</span>
                </template>
              </el-table-column>
              <el-table-column label="回款率" align="center" min-width="100">
                <template #default="scope">
                  <span>{{ formatRateValue(scope.row.collectionRate) }}</span>
                </template>
              </el-table-column>
              <el-table-column label="逾期/预警/待办" align="center" min-width="150">
                <template #default="scope">
                  <span>{{ scope.row.overdueCount || 0 }} / {{ scope.row.stockWarningCount || 0 }} / {{ scope.row.pendingActionCount || 0 }}</span>
                </template>
              </el-table-column>
              <el-table-column label="操作" align="center" width="100">
                <template #default="scope">
                  <el-button link type="primary" @click="openOperationSnapshotPreview(scope.row)">查看详情</el-button>
                </template>
              </el-table-column>
            </el-table>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <el-row :gutter="12" style="margin-top: 12px">
      <el-col :xs="24" :lg="12" style="margin-bottom: 12px">
        <el-card>
          <template #header>销售趋势</template>
          <div ref="saleTrendRef" style="height: 360px"></div>
        </el-card>
      </el-col>
      <el-col :xs="24" :lg="12" style="margin-bottom: 12px">
        <el-card>
          <template #header>客户销售Top5</template>
          <div ref="topCustomerRef" style="height: 360px"></div>
        </el-card>
      </el-col>
    </el-row>

    <ReportMessageCenterSection
      v-model:selected-message-type="selectedMessageType"
      :message-list="filteredMessageCenterList"
      :parse-time="parseTime"
      :get-message-type-label="getMessageTypeLabel"
      :get-message-action-label="getMessageActionLabel"
      @message-action="handleMessageAction"
    />

    <ReportProductRankingSection ref="productRankingSectionRef" />

    <ReportFinanceSection
      :reconciliation-query-params="reconciliationQueryParams"
      :reconciliation-list="reconciliationList"
      :reconciliation-loading="reconciliationLoading"
      :reconciliation-total="reconciliationTotal"
      :invoice-tax-query-params="invoiceTaxQueryParams"
      :invoice-tax-list="invoiceTaxList"
      :invoice-tax-loading="invoiceTaxLoading"
      :invoice-tax-total="invoiceTaxTotal"
      :cash-flow-forecast-query-params="cashFlowForecastQueryParams"
      :cash-flow-forecast-data="cashFlowForecastData"
      :parse-time="parseTime"
      :format-amount-value="formatAmountValue"
      :get-reconciliation-target-name="getReconciliationTargetName"
      @reconciliation-query="getReconciliationData"
      @reconciliation-reset="resetReconciliationQuery"
      @reconciliation-action="handleReconciliationAction"
      @invoice-tax-query="getInvoiceTaxData"
      @invoice-tax-reset="resetInvoiceTaxQuery"
      @cash-flow-refresh="getCashFlowForecastData"
    />

    <el-dialog v-model="messagePreviewOpen" title="消息详情" width="720px" append-to-body>
      <el-descriptions :column="1" border>
        <el-descriptions-item label="消息类型">
          {{ getMessageTypeLabel(messagePreviewData.message_type) }}
        </el-descriptions-item>
        <el-descriptions-item label="消息标题">
          {{ messagePreviewData.message_title || "-" }}
        </el-descriptions-item>
        <el-descriptions-item label="消息时间">
          {{ parseTime(messagePreviewData.message_time) || "-" }}
        </el-descriptions-item>
        <el-descriptions-item label="消息内容">
          <div class="message-preview-content">{{ messagePreviewData.previewContent || "-" }}</div>
        </el-descriptions-item>
      </el-descriptions>
      <template #footer>
        <div class="dialog-footer">
          <el-button type="primary" v-if="canDirectJumpMessage(messagePreviewData)" @click="handleMessageJump(messagePreviewData)">去处理</el-button>
          <el-button @click="messagePreviewOpen = false">关 闭</el-button>
        </div>
      </template>
    </el-dialog>

    <el-dialog v-model="dashboardTargetDialogOpen" title="维护经营目标" width="520px" append-to-body>
      <el-alert
        title="经营目标按自然月展示，修改后会同步更新经营报表的达成进度。"
        type="info"
        :closable="false"
        show-icon
      />
      <el-form ref="dashboardTargetFormRef" :model="dashboardTargetForm" :rules="dashboardTargetRules" label-width="120px" style="margin-top: 16px">
        <el-form-item label="销售目标（元）" prop="saleTargetAmount">
          <el-input-number v-model="dashboardTargetForm.saleTargetAmount" :min="0" :precision="2" controls-position="right" style="width: 100%" />
        </el-form-item>
        <el-form-item label="毛利目标（元）" prop="grossProfitTargetAmount">
          <el-input-number v-model="dashboardTargetForm.grossProfitTargetAmount" :min="0" :precision="2" controls-position="right" style="width: 100%" />
        </el-form-item>
        <el-form-item label="回款率目标（%）" prop="collectionRateTarget">
          <el-input-number v-model="dashboardTargetForm.collectionRateTarget" :min="0" :max="100" :precision="2" controls-position="right" style="width: 100%" />
        </el-form-item>
      </el-form>
      <template #footer>
        <div class="dialog-footer">
          <el-button @click="dashboardTargetDialogOpen = false">取 消</el-button>
          <el-button type="primary" :loading="dashboardTargetLoading" @click="submitDashboardTargetForm">保 存</el-button>
        </div>
      </template>
    </el-dialog>

    <el-dialog v-model="decisionBudgetDialogOpen" title="维护经营预算" width="560px" append-to-body>
      <el-alert
        title="经营预算用于预算-实际-预测模块，建议按自然月维护销售、毛利、回款、采购和净现金流预算。"
        type="info"
        :closable="false"
        show-icon
      />
      <el-form ref="decisionBudgetFormRef" :model="decisionBudgetForm" :rules="decisionBudgetRules" label-width="130px" style="margin-top: 16px">
        <el-form-item label="销售预算（元）" prop="saleBudgetAmount">
          <el-input-number v-model="decisionBudgetForm.saleBudgetAmount" :min="0" :precision="2" controls-position="right" style="width: 100%" />
        </el-form-item>
        <el-form-item label="毛利预算（元）" prop="grossProfitBudgetAmount">
          <el-input-number v-model="decisionBudgetForm.grossProfitBudgetAmount" :min="0" :precision="2" controls-position="right" style="width: 100%" />
        </el-form-item>
        <el-form-item label="回款预算（元）" prop="collectionBudgetAmount">
          <el-input-number v-model="decisionBudgetForm.collectionBudgetAmount" :min="0" :precision="2" controls-position="right" style="width: 100%" />
        </el-form-item>
        <el-form-item label="采购预算（元）" prop="purchaseBudgetAmount">
          <el-input-number v-model="decisionBudgetForm.purchaseBudgetAmount" :min="0" :precision="2" controls-position="right" style="width: 100%" />
        </el-form-item>
        <el-form-item label="净现金流预算（元）" prop="netCashBudgetAmount">
          <el-input-number v-model="decisionBudgetForm.netCashBudgetAmount" :precision="2" controls-position="right" style="width: 100%" />
        </el-form-item>
      </el-form>
      <template #footer>
        <div class="dialog-footer">
          <el-button @click="decisionBudgetDialogOpen = false">取 消</el-button>
          <el-button type="primary" :loading="decisionBudgetLoading" @click="submitDecisionBudgetForm">保 存</el-button>
        </div>
      </template>
    </el-dialog>

    <el-dialog v-model="executiveBriefDialogOpen" title="经营简报预览" width="760px" append-to-body>
      <div class="executive-brief-dialog-meta">生成时间：{{ executiveBriefData.generatedAtText || "-" }}</div>
      <div v-for="executiveBriefSection in executiveBriefSectionList" :key="`dialog-${executiveBriefSection.key}`" class="executive-brief-section">
        <div class="executive-brief-section-title">{{ executiveBriefSection.title }}</div>
        <div v-if="executiveBriefSection.textList?.length" class="executive-brief-section-body">
          <div v-for="executiveBriefText in executiveBriefSection.textList" :key="`dialog-${executiveBriefText}`" class="executive-brief-item">
            {{ executiveBriefText }}
          </div>
        </div>
        <div v-else class="decision-support-muted">当前暂无可展示内容。</div>
      </div>
      <el-input type="textarea" :rows="12" :model-value="buildExecutiveBriefPlainText()" readonly class="executive-brief-textarea" />
      <template #footer>
        <div class="dialog-footer">
          <el-button @click="executiveBriefDialogOpen = false">关 闭</el-button>
          <el-button icon="Download" @click="exportExecutiveBriefMarkdown">导出 Markdown</el-button>
          <el-button icon="Document" @click="exportExecutiveBriefWord">导出 Word</el-button>
          <el-button type="primary" @click="copyExecutiveBriefText">复制简报</el-button>
        </div>
      </template>
    </el-dialog>

    <el-dialog v-model="annualBudgetPlanDialogOpen" :title="annualBudgetPlanFormMode === 'createVersion' ? '新建预算版本' : '维护年度预算计划'" width="1160px" append-to-body>
      <el-alert
        :title="annualBudgetPlanFormMode === 'createVersion' ? '系统已带入当前生效版本的预算拆解，请补充版本标签和调整原因后继续微调。' : '建议先录入年度总量，再一键均分到月度，最后按旺淡季和回款节奏微调每个月预算。'"
        type="info"
        :closable="false"
        show-icon
      />
      <el-form ref="annualBudgetPlanFormRef" :model="annualBudgetPlanForm" :rules="annualBudgetPlanRules" label-width="96px" style="margin-top: 14px">
        <el-row :gutter="10">
          <el-col :span="7">
            <el-form-item label="预算年度" prop="budgetYear">
              <el-input-number v-model="annualBudgetPlanForm.budgetYear" :min="2020" :max="2099" controls-position="right" style="width: 100%" :disabled="annualBudgetPlanFormMode === 'createVersion' || !!annualBudgetPlanForm.planId" />
            </el-form-item>
          </el-col>
          <el-col :span="10">
            <el-form-item label="预算名称" prop="planName">
              <el-input v-model="annualBudgetPlanForm.planName" placeholder="请输入预算计划名称" />
            </el-form-item>
          </el-col>
          <el-col :span="7">
            <el-form-item label="版本标签">
              <el-input v-model="annualBudgetPlanForm.versionLabel" placeholder="例如 v2" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-form-item label="调整原因">
          <el-input v-model="annualBudgetPlanForm.adjustmentReason" type="textarea" :rows="2" placeholder="请输入本次预算调整的背景、目标或主要变化" />
        </el-form-item>
        <div class="annual-budget-form-action-row">
          <span class="annual-budget-form-action-text">先填写年度总量，再点击“按年度均分”，最后可逐月微调。</span>
          <el-button type="primary" plain size="small" @click="distributeAllAnnualBudgetPlanMetrics">按年度均分</el-button>
        </div>
        <el-row :gutter="10">
          <el-col :span="8">
            <el-form-item label="年度销售">
              <el-input-number v-model="annualBudgetPlanForm.saleBudgetAmount" :min="0" :precision="2" controls-position="right" style="width: 100%" />
            </el-form-item>
          </el-col>
          <el-col :span="8">
            <el-form-item label="年度毛利">
              <el-input-number v-model="annualBudgetPlanForm.grossProfitBudgetAmount" :min="0" :precision="2" controls-position="right" style="width: 100%" />
            </el-form-item>
          </el-col>
          <el-col :span="8">
            <el-form-item label="年度回款">
              <el-input-number v-model="annualBudgetPlanForm.collectionBudgetAmount" :min="0" :precision="2" controls-position="right" style="width: 100%" />
            </el-form-item>
          </el-col>
          <el-col :span="8">
            <el-form-item label="年度采购">
              <el-input-number v-model="annualBudgetPlanForm.purchaseBudgetAmount" :min="0" :precision="2" controls-position="right" style="width: 100%" />
            </el-form-item>
          </el-col>
          <el-col :span="8">
            <el-form-item label="年度净现金">
              <el-input-number v-model="annualBudgetPlanForm.netCashBudgetAmount" :precision="2" controls-position="right" style="width: 100%" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-table :data="annualBudgetMonthLabelList.map((monthLabel, monthIndexValue) => ({ monthLabel, monthIndexValue }))" border size="small">
          <el-table-column label="月份" align="center" width="60">
            <template #default="scope">
              <span>{{ scope.row.monthLabel }}</span>
            </template>
          </el-table-column>
          <el-table-column label="销售预算" min-width="115">
            <template #default="scope">
              <el-input-number v-model="annualBudgetPlanForm.saleMonthlyPlanList[scope.row.monthIndexValue]" :min="0" :precision="2" :controls="false" size="small" style="width: 100%" @change="syncAnnualBudgetPlanFormTotalAmount('saleBudgetAmount')" />
            </template>
          </el-table-column>
          <el-table-column label="毛利预算" min-width="115">
            <template #default="scope">
              <el-input-number v-model="annualBudgetPlanForm.grossProfitMonthlyPlanList[scope.row.monthIndexValue]" :min="0" :precision="2" :controls="false" size="small" style="width: 100%" @change="syncAnnualBudgetPlanFormTotalAmount('grossProfitBudgetAmount')" />
            </template>
          </el-table-column>
          <el-table-column label="回款预算" min-width="115">
            <template #default="scope">
              <el-input-number v-model="annualBudgetPlanForm.collectionMonthlyPlanList[scope.row.monthIndexValue]" :min="0" :precision="2" :controls="false" size="small" style="width: 100%" @change="syncAnnualBudgetPlanFormTotalAmount('collectionBudgetAmount')" />
            </template>
          </el-table-column>
          <el-table-column label="采购预算" min-width="115">
            <template #default="scope">
              <el-input-number v-model="annualBudgetPlanForm.purchaseMonthlyPlanList[scope.row.monthIndexValue]" :min="0" :precision="2" :controls="false" size="small" style="width: 100%" @change="syncAnnualBudgetPlanFormTotalAmount('purchaseBudgetAmount')" />
            </template>
          </el-table-column>
          <el-table-column label="净现金流" min-width="115">
            <template #default="scope">
              <el-input-number v-model="annualBudgetPlanForm.netCashMonthlyPlanList[scope.row.monthIndexValue]" :precision="2" :controls="false" size="small" style="width: 100%" @change="syncAnnualBudgetPlanFormTotalAmount('netCashBudgetAmount')" />
            </template>
          </el-table-column>
        </el-table>
      </el-form>
      <template #footer>
        <div class="dialog-footer">
          <el-button @click="annualBudgetPlanDialogOpen = false">取 消</el-button>
          <el-button type="primary" :loading="annualBudgetPlanLoading" @click="submitAnnualBudgetPlanForm">{{ annualBudgetPlanFormMode === "createVersion" ? "创建版本" : "保 存" }}</el-button>
        </div>
      </template>
    </el-dialog>

    <el-dialog v-model="annualBudgetApprovalDialogOpen" :title="annualBudgetApprovalActionType === 'approve' ? '审批通过年度预算' : '驳回年度预算'" width="520px" append-to-body>
      <el-form :model="annualBudgetApprovalForm" label-width="90px">
        <el-form-item label="审批意见">
          <el-input v-model="annualBudgetApprovalForm.approveRemark" type="textarea" :rows="5" :placeholder="annualBudgetApprovalActionType === 'approve' ? '请输入审批意见，可为空' : '请输入驳回原因'" />
        </el-form-item>
      </el-form>
      <template #footer>
        <div class="dialog-footer">
          <el-button @click="annualBudgetApprovalDialogOpen = false">取 消</el-button>
          <el-button :type="annualBudgetApprovalActionType === 'approve' ? 'success' : 'danger'" :loading="annualBudgetPlanLoading" @click="submitAnnualBudgetApproval">
            {{ annualBudgetApprovalActionType === "approve" ? "审批通过" : "确认驳回" }}
          </el-button>
        </div>
      </template>
    </el-dialog>

    <el-dialog v-model="executiveBriefRecordDialogOpen" title="归档简报详情" width="760px" append-to-body>
      <div class="executive-brief-dialog-meta">
        归档时间：{{ executiveBriefRecordPreviewData.generatedTime ? parseTime(executiveBriefRecordPreviewData.generatedTime) : "-" }}
      </div>
      <div class="executive-brief-section">
        <div class="executive-brief-section-title">经营结论</div>
        <div class="executive-brief-section-body">
          <div class="executive-brief-item">{{ executiveBriefRecordPreviewData.summaryContent || "暂无经营结论" }}</div>
        </div>
      </div>
      <div class="executive-brief-section">
        <div class="executive-brief-section-title">本期亮点</div>
        <div v-if="executiveBriefRecordPreviewData.highlightTextList?.length" class="executive-brief-section-body">
          <div v-for="executiveBriefText in executiveBriefRecordPreviewData.highlightTextList" :key="`record-highlight-${executiveBriefText}`" class="executive-brief-item">{{ executiveBriefText }}</div>
        </div>
        <div v-else class="decision-support-muted">暂无亮点内容。</div>
      </div>
      <div class="executive-brief-section">
        <div class="executive-brief-section-title">主要风险</div>
        <div v-if="executiveBriefRecordPreviewData.riskTextList?.length" class="executive-brief-section-body">
          <div v-for="executiveBriefText in executiveBriefRecordPreviewData.riskTextList" :key="`record-risk-${executiveBriefText}`" class="executive-brief-item">{{ executiveBriefText }}</div>
        </div>
        <div v-else class="decision-support-muted">暂无风险内容。</div>
      </div>
      <div class="executive-brief-section">
        <div class="executive-brief-section-title">建议决策</div>
        <div v-if="executiveBriefRecordPreviewData.actionTextList?.length" class="executive-brief-section-body">
          <div v-for="executiveBriefText in executiveBriefRecordPreviewData.actionTextList" :key="`record-action-${executiveBriefText}`" class="executive-brief-item">{{ executiveBriefText }}</div>
        </div>
        <div v-else class="decision-support-muted">暂无建议决策。</div>
      </div>
      <el-input type="textarea" :rows="12" :model-value="executiveBriefRecordPreviewData.plainTextContent || ''" readonly class="executive-brief-textarea" />
      <template #footer>
        <div class="dialog-footer">
          <el-button @click="executiveBriefRecordDialogOpen = false">关 闭</el-button>
          <el-button icon="Download" @click="exportArchivedBriefMarkdown">导出 Markdown</el-button>
          <el-button type="primary" icon="Document" @click="exportArchivedBriefWord">导出 Word</el-button>
        </div>
      </template>
    </el-dialog>

    <el-dialog v-model="operationSnapshotDialogOpen" title="经营快照详情" width="760px" append-to-body>
      <div class="executive-brief-dialog-meta">
        快照日期：{{ operationSnapshotPreviewData.snapshotDate ? parseTime(operationSnapshotPreviewData.snapshotDate, '{y}-{m}-{d}') : "-" }}
      </div>
      <div class="annual-budget-summary-list">
        <div class="annual-budget-summary-item">
          <div class="annual-budget-summary-title">销售额</div>
          <div class="annual-budget-summary-value">￥{{ formatAmountValue(operationSnapshotPreviewData.saleAmount) }}</div>
        </div>
        <div class="annual-budget-summary-item">
          <div class="annual-budget-summary-title">毛利额</div>
          <div class="annual-budget-summary-value">￥{{ formatAmountValue(operationSnapshotPreviewData.grossProfitAmount) }}</div>
        </div>
        <div class="annual-budget-summary-item">
          <div class="annual-budget-summary-title">回款率</div>
          <div class="annual-budget-summary-value">{{ formatRateValue(operationSnapshotPreviewData.collectionRate) }}</div>
        </div>
        <div class="annual-budget-summary-item">
          <div class="annual-budget-summary-title">逾期/预警/待办</div>
          <div class="annual-budget-summary-value">{{ operationSnapshotPreviewData.overdueCount || 0 }} / {{ operationSnapshotPreviewData.stockWarningCount || 0 }} / {{ operationSnapshotPreviewData.pendingActionCount || 0 }}</div>
        </div>
      </div>
      <div class="executive-brief-section">
        <div class="executive-brief-section-title">经营结论</div>
        <div class="executive-brief-section-body">
          <div class="executive-brief-item">{{ operationSnapshotPreviewData.summaryContent || "暂无经营结论" }}</div>
        </div>
      </div>
      <el-input type="textarea" :rows="12" :model-value="operationSnapshotPreviewData.plainTextContent || ''" readonly class="executive-brief-textarea" />
      <template #footer>
        <div class="dialog-footer">
          <el-button @click="operationSnapshotDialogOpen = false">关 闭</el-button>
        </div>
      </template>
    </el-dialog>

    <el-dialog v-model="executiveActionItemDialogOpen" :title="executiveActionItemDialogTitle" width="720px" append-to-body>
      <el-form ref="executiveActionItemFormRef" :model="executiveActionItemForm" :rules="executiveActionItemRules" label-width="110px">
        <el-form-item label="来源简报">
          <el-input v-model="executiveActionItemForm.briefTitleSnapshot" placeholder="可选，留空表示手工新增事项" />
        </el-form-item>
        <el-form-item label="决议事项" prop="actionTitle">
          <el-input v-model="executiveActionItemForm.actionTitle" type="textarea" :rows="3" placeholder="请输入需要推进的经营决议事项" />
        </el-form-item>
        <el-row :gutter="12">
          <el-col :span="12">
            <el-form-item label="负责人">
              <el-input v-model="executiveActionItemForm.ownerName" placeholder="请输入负责人" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="到期日期">
              <el-date-picker v-model="executiveActionItemForm.dueDate" type="date" value-format="YYYY-MM-DD" placeholder="请选择到期日期" style="width: 100%" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="12">
          <el-col :span="12">
            <el-form-item label="事项状态">
              <el-select v-model="executiveActionItemForm.actionStatus" placeholder="请选择事项状态" style="width: 100%">
                <el-option label="待跟进" value="todo" />
                <el-option label="执行中" value="in_progress" />
                <el-option label="已完成" value="completed" />
                <el-option label="已取消" value="canceled" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="优先级">
              <el-select v-model="executiveActionItemForm.priorityLevel" placeholder="请选择优先级" style="width: 100%">
                <el-option label="高" value="high" />
                <el-option label="中" value="medium" />
                <el-option label="低" value="low" />
              </el-select>
            </el-form-item>
          </el-col>
        </el-row>
        <el-form-item label="进度备注">
          <el-input v-model="executiveActionItemForm.progressRemark" type="textarea" :rows="3" placeholder="请输入最新进度说明" />
        </el-form-item>
        <el-form-item label="备注">
          <el-input v-model="executiveActionItemForm.remark" type="textarea" :rows="2" placeholder="请输入补充说明" />
        </el-form-item>
      </el-form>
      <template #footer>
        <div class="dialog-footer">
          <el-button @click="executiveActionItemDialogOpen = false">取 消</el-button>
          <el-button type="primary" @click="submitExecutiveActionItemForm">保 存</el-button>
        </div>
      </template>
    </el-dialog>
  </div>
</template>

<script setup name="Report">
import * as echarts from 'echarts'
import { checkPermi } from '@/utils/permission'
import { getDashboard, listSaleReport, listReconciliation, listInvoiceTax, getCashFlowForecast } from '@/api/business/report'
import { listProduct } from '@/api/business/product'
import { listReceivable } from '@/api/business/receivable'
import { listPayable } from '@/api/business/payable'
import { listCustomer } from '@/api/business/customer'
import { listSupplier } from '@/api/business/supplier'
import { listSaleOrder } from '@/api/business/saleOrder'
import { listPurchaseOrder } from '@/api/business/purchaseOrder'
import { listInbound } from '@/api/business/inbound'
import { listOutbound } from '@/api/business/outbound'
import { listStock } from '@/api/business/stock'
import { listStockLog } from '@/api/business/stockLog'
import { listReceipt } from '@/api/business/receipt'
import { listPayment } from '@/api/business/payment'
import useUserStore from '@/store/modules/user'
import useSettingsStore from '@/store/modules/settings'
import { parseTime } from '@/utils/ruoyi'
import { businessDashboardRoutePathMap, normalizeDashboardSummaryData } from '@/utils/businessDashboard'
import { useDashboardMessageCenter } from '@/composables/useDashboardMessageCenter'
import { useReportTargetAndBudgetConfig } from './composables/useReportTargetAndBudgetConfig'
import { useReportGovernance } from './composables/useReportGovernance'
import {
  normalizeNumberValue,
  normalizeCountValue,
  formatAmountValue,
  formatRateValue,
  normalizeScoreValue,
  getManagementHealthLevelText,
  getManagementHealthTagType,
  buildGrowthTagText,
  buildGrowthSummaryText,
  buildSaleAmountSummaryText,
  getGrowthTagType,
  buildRateDifferenceText,
  getDifferenceTagType,
  getCashFlowWindowTagType,
  getCashFlowWindowTagText,
  buildCashFlowWindowDescription,
  getExecutiveActionStatusLabel,
  getExecutiveActionStatusTagType,
  getExecutiveActionPriorityLabel,
  getExecutiveActionPriorityTagType,
  isExecutiveActionItemOverdue,
  getOperationSnapshotTypeLabel,
  getOperationSnapshotTypeTagType,
  getDueRiskText
} from "./utils/reportFormatters"
import {
  hasRemoteManagementCockpitData,
  getFirstDefinedValue,
  parseDateValue,
  formatDateValueForQuery,
  getDateStartValue,
  buildMonthRange,
  isDateInMonthRange,
  fetchPagedRowList,
  getSettledResultValue,
  calculateRateValue,
  calculateGrowthRateValue,
  buildProductCostPriceMap,
  buildCustomerNameMap,
  buildSupplierNameMap,
  getSaleReportBusinessTime,
  getSaleOrderBusinessTime,
  getReceiptBusinessTime,
  getPurchaseOrderBusinessTime,
  getRowCreateTime,
  isOpenReceivableRow,
  isOpenPayableRow,
  getReceivableRemainAmountValue,
  getPayableRemainAmountValue,
  isStatisticalPurchaseOrderStatus,
  isPendingPurchaseOrderStatus,
  isSubmittedDocumentStatus,
  countTimeoutPendingRowList,
  buildAgingBucketList,
  buildReceivableAgingListFromRowList,
  buildPayableAgingListFromRowList,
  getCustomerDisplayNameForCockpit,
  getSupplierDisplayNameForCockpit,
  buildTopRiskCustomerList,
  buildTopRiskSupplierList,
  buildManagementOverviewData,
  buildRiskOverviewData,
  getPaymentBusinessTime,
  getStockLogBusinessTime,
  getStockBusinessTime,
  buildProductInfoMap,
  buildCustomerDetailMap,
  buildSaleOrderLookupMap,
  buildReceivableCustomerSummaryMap,
  extractRegionNameFromAddress,
  getCustomerDisplayNameForDecision,
  getProductDisplayNameForDecision,
  buildCustomerProfitAnalysisData,
  buildInventoryDecisionAnalysisData,
  buildContributionRankingList,
  buildOrganizationContributionAnalysisData,
  buildDecisionBudgetOverviewData,
  buildCashScenarioBaseData,
  buildExecutiveBriefData,
  buildSimulatedCashScenarioData
} from "./utils/reportAnalytics"

const ReportMessageCenterSection = defineAsyncComponent(() => import("./components/ReportMessageCenterSection.vue"))
const ReportProductRankingSection = defineAsyncComponent(() => import("./components/ReportProductRankingSection.vue"))
const ReportFinanceSection = defineAsyncComponent(() => import("./components/ReportFinanceSection.vue"))
const ReportShortcutCardsSection = defineAsyncComponent(() => import("./components/ReportShortcutCardsSection.vue"))

const { proxy } = getCurrentInstance()
const userStore = useUserStore()
const settingsStore = useSettingsStore()
const dashboardData = ref({})
const saleTrendRef = ref(null)
const topCustomerRef = ref(null)
const saleTrendChartInstance = shallowRef(null)
const topCustomerChartInstance = shallowRef(null)
const selectedMessageType = ref("all")
const router = useRouter()
const route = useRoute()
const currentTodoTargetType = ref("")
const executiveActionSectionRef = ref(null)
const annualBudgetSectionRef = ref(null)
const productRankingSectionRef = ref(null)
const {
  messagePreviewOpen,
  messagePreviewData,
  handleMessageJump,
  handleMessageAction,
  canDirectJumpMessage,
  getMessageActionLabel,
  getMessageTypeLabel
} = useDashboardMessageCenter()
const reconciliationLoading = ref(false)
const reconciliationList = ref([])
const reconciliationTotal = ref(0)
const invoiceTaxLoading = ref(false)
const invoiceTaxList = ref([])
const invoiceTaxTotal = ref(0)
const cashFlowForecastData = ref({})
const cashFlowWindowLoading = ref(false)
const cashFlowWindowList = ref([])
const managementCockpitLoading = ref(false)
const decisionSupportLoading = ref(false)
const organizationContributionTab = ref("category")
const reconciliationQueryParams = ref({
  pageNum: 1,
  pageSize: 10,
  targetName: undefined
})
const invoiceTaxQueryParams = ref({
  pageNum: 1,
  pageSize: 10,
  startDate: undefined,
  endDate: undefined
})
const cashFlowForecastQueryParams = ref({
  forecastDays: 30
})
const cashScenarioForm = reactive({
  delayedCollectionAmount: 0,
  acceleratedPaymentAmount: 0,
  additionalSaleCollectionAmount: 0,
  additionalProcurementAmount: 0,
  temporaryFinancingAmount: 0
})

const businessRoutePathMap = businessDashboardRoutePathMap

// 组装顶部经营总览快捷卡片，避免模板内重复维护标题、数值和跳转类型。
const dashboardOverviewShortcutCardList = computed(() => [
  {
    shortcutType: "saleOrderSummary",
    title: "累计净销售额",
    value: `￥${formatAmountValue(dashboardData.value.totalSaleAmount)}`,
    actionText: "点击查看销售单"
  },
  {
    shortcutType: "receivableSummary",
    title: "累计应收总额",
    value: `￥${formatAmountValue(dashboardData.value.totalReceivableAmount)}`,
    actionText: "点击查看应收台账"
  },
  {
    shortcutType: "receiptSummary",
    title: "累计已收总额",
    value: `￥${formatAmountValue(dashboardData.value.totalReceivedAmount)}`,
    actionText: "点击查看回款记录"
  },
  {
    shortcutType: "overdueReceivable",
    title: "逾期笔数",
    value: dashboardData.value.overdueCount || 0,
    actionText: "点击查看逾期应收"
  },
  {
    shortcutType: "stockWarning",
    title: "库存预警",
    value: dashboardData.value.stockWarningCount || 0,
    actionText: "点击查看预警库存"
  }
])

// 组装待审核单据快捷卡片，统一保持销售、入库和出库三个入口的展示结构。
const pendingAuditShortcutCardList = computed(() => [
  {
    shortcutType: "pendingSaleOrder",
    title: "待审核销售单",
    value: dashboardData.value.pendingSaleOrderCount || 0,
    actionText: "点击进入待审核列表"
  },
  {
    shortcutType: "pendingInbound",
    title: "待审核入库单",
    value: dashboardData.value.pendingInboundCount || 0,
    actionText: "点击进入待审核列表"
  },
  {
    shortcutType: "pendingOutbound",
    title: "待审核出库单",
    value: dashboardData.value.pendingOutboundCount || 0,
    actionText: "点击进入待审核列表"
  }
])

// 统一读取管理驾驶舱数据来源，方便当前环境标注为实时接口、兼容模式或待发布状态。
const managementCockpitDataSource = computed(() => {
  if (dashboardData.value.managementCockpitDataSource)
  {
    return dashboardData.value.managementCockpitDataSource
  }
  if (managementCockpitLoading.value)
  {
    return "loading"
  }
  if (hasManagementCockpitData.value)
  {
    return "dashboard"
  }
  return "pending"
})

// 返回管理驾驶舱模式文案，帮助管理层理解当前数据来源。
const managementCockpitModeLabel = computed(() => {
  if (managementCockpitDataSource.value === "fallback")
  {
    return "兼容模式"
  }
  if (managementCockpitDataSource.value === "dashboard")
  {
    return "实时接口"
  }
  if (managementCockpitDataSource.value === "loading")
  {
    return "加载中"
  }
  return "待发布"
})

// 返回管理驾驶舱模式标签类型。
const managementCockpitModeTagType = computed(() => {
  if (managementCockpitDataSource.value === "fallback")
  {
    return "warning"
  }
  if (managementCockpitDataSource.value === "dashboard")
  {
    return "success"
  }
  if (managementCockpitDataSource.value === "loading")
  {
    return "info"
  }
  return "danger"
})

// 判断当前经营看板是否已经拿到管理驾驶舱数据，避免旧环境直接展示空白指标。
const hasManagementCockpitData = computed(() => {
  const managementOverviewData = dashboardData.value.managementOverview
  const riskOverviewData = dashboardData.value.riskOverview
  return !!managementOverviewData && Object.keys(managementOverviewData).length > 0
    && !!riskOverviewData && Object.keys(riskOverviewData).length > 0
})

// 统一读取管理驾驶舱经营概览数据。
const managementOverviewData = computed(() => dashboardData.value.managementOverview || {})

// 统一读取风险概览数据。
const riskOverviewData = computed(() => dashboardData.value.riskOverview || {})

// 统一读取应收账龄分布数据。
const receivableAgingList = computed(() => dashboardData.value.receivableAgingList || [])

// 统一读取应付账龄分布数据。
const payableAgingList = computed(() => dashboardData.value.payableAgingList || [])

// 统一读取高风险客户列表。
const topRiskCustomerList = computed(() => dashboardData.value.topRiskCustomerList || [])

// 统一读取重点付款供应商列表。
const topRiskSupplierList = computed(() => dashboardData.value.topRiskSupplierList || [])

// 统一读取管理层决策支持数据，集中承载预算、利润、库存和经营简报等模块。
const decisionSupportData = computed(() => dashboardData.value.decisionSupportData || {})

// 判断当前是否已经生成管理层决策支持数据，避免新模块在旧环境下显示空白结构。
const hasDecisionSupportData = computed(() => {
  return !!decisionSupportData.value && Object.keys(decisionSupportData.value).length > 0
})

// 统一读取预算执行与预测概览数据。
const decisionBudgetOverviewData = computed(() => decisionSupportData.value.decisionBudgetOverview || {})

// 客户等级编码映射中文展示，避免输出原始英文枚举值（如 vip）。
function formatCustomerLevelLabel(levelValue) {
  const key = String(levelValue || "").trim().toLowerCase()
  const map = {
    vip: "VIP客户",
    gold: "金牌客户",
    a: "A级客户",
    b: "B级客户",
    c: "C级客户",
    key: "重点客户",
    important: "重点客户",
    normal: "普通客户",
    ordinary: "普通客户",
    potential: "潜在客户"
  }
  return map[key] || (levelValue ? String(levelValue) : "普通客户")
}

// 统一读取客户利润分析数据。
const customerProfitAnalysisData = computed(() => decisionSupportData.value.customerProfitAnalysis || {})

// 统一读取库存决策分析数据。
const inventoryDecisionAnalysisData = computed(() => decisionSupportData.value.inventoryDecisionAnalysis || {})

// 统一读取组织贡献分析数据。
const organizationContributionAnalysisData = computed(() => decisionSupportData.value.organizationContributionAnalysis || {})

// 统一读取现金流情景模拟基准数据。
const cashScenarioBaseData = computed(() => decisionSupportData.value.cashScenarioBaseData || {})

// 统一读取管理层经营简报数据。
const executiveBriefData = computed(() => decisionSupportData.value.executiveBriefData || {})

// 计算本月时间进度，便于经营目标与预算区判断当前节奏是否领先或落后。
const currentMonthProgressData = computed(() => {
  const currentDateValue = new Date()
  const totalDayCount = new Date(currentDateValue.getFullYear(), currentDateValue.getMonth() + 1, 0).getDate()
  const elapsedDayCount = Math.min(currentDateValue.getDate(), totalDayCount)
  const remainingDayCount = Math.max(totalDayCount - elapsedDayCount, 0)
  const monthProgressPercentage = totalDayCount > 0 ? elapsedDayCount / totalDayCount * 100 : 0
  return {
    totalDayCount: totalDayCount,
    elapsedDayCount: elapsedDayCount,
    remainingDayCount: remainingDayCount,
    monthProgressRate: totalDayCount > 0 ? elapsedDayCount / totalDayCount : 0,
    monthProgressPercentage: Number(monthProgressPercentage.toFixed(2))
  }
})

// 组装管理层经营简报分段，方便页面展示、复制和文件导出。
const executiveBriefSectionList = computed(() => {
  const highlightTextList = executiveBriefData.value.highlightTextList || []
  const riskTextList = executiveBriefData.value.riskTextList || []
  const actionTextList = executiveBriefData.value.actionTextList || []
  return [
    {
      key: "summary",
      title: "经营结论",
      textList: executiveBriefData.value.summaryText ? [executiveBriefData.value.summaryText] : []
    },
    {
      key: "highlight",
      title: "本期亮点",
      textList: highlightTextList
    },
    {
      key: "risk",
      title: "主要风险",
      textList: riskTextList
    },
    {
      key: "action",
      title: "建议决策",
      textList: actionTextList
    }
  ]
})

const {
  dashboardTargetLoading,
  dashboardTargetDialogOpen,
  dashboardTargetFormRef,
  dashboardTargetForm,
  dashboardTargetRules,
  canManageDashboardTarget,
  hasDashboardTargetConfigured,
  dashboardTargetCardList,
  resolveDashboardTargetActionType,
  resolveDashboardTargetActionLabel,
  loadDashboardTargetConfig,
  openDashboardTargetDialog,
  submitDashboardTargetForm,
  decisionBudgetLoading,
  decisionBudgetDialogOpen,
  decisionBudgetFormRef,
  decisionBudgetForm,
  decisionBudgetRules,
  hasDecisionBudgetConfigured,
  decisionBudgetCardList,
  decisionBudgetInsightList,
  loadDecisionBudgetConfig,
  openDecisionBudgetDialog,
  submitDecisionBudgetForm
} = useReportTargetAndBudgetConfig({
  proxy,
  userStore,
  managementOverviewData,
  decisionBudgetOverviewData,
  currentMonthProgressData
})

const {
  annualBudgetMonthLabelList,
  executiveBriefDialogOpen,
  annualBudgetYear,
  annualBudgetPlanLoading,
  annualBudgetPlanDialogOpen,
  annualBudgetPlanFormRef,
  annualBudgetApprovalDialogOpen,
  annualBudgetApprovalActionType,
  annualBudgetPlanFormMode,
  annualBudgetPlanData,
  annualBudgetPlanHistoryList,
  annualBudgetPlanForm,
  annualBudgetApprovalForm,
  annualBudgetPlanRules,
  canManageAnnualBudgetPlan,
  canApproveAnnualBudgetPlan,
  canCreateAnnualBudgetPlanVersion,
  annualBudgetPlanEffectiveText,
  annualBudgetPlanEffectiveTagType,
  annualBudgetPlanStatusText,
  annualBudgetPlanStatusTagType,
  annualBudgetPlanSummaryCardList,
  annualBudgetMonthlyTableList,
  syncAnnualBudgetPlanFormTotalAmount,
  distributeAnnualBudgetPlanMetric,
  distributeAllAnnualBudgetPlanMetrics,
  loadAnnualBudgetPlanData,
  openAnnualBudgetPlanDialog,
  openAnnualBudgetPlanVersionDialog,
  submitAnnualBudgetPlanForm,
  handleAnnualBudgetPlanSubmit,
  openAnnualBudgetApprovalDialog,
  submitAnnualBudgetApproval,
  executiveBriefRecordLoading,
  executiveBriefRecordList,
  executiveBriefRecordDialogOpen,
  executiveBriefRecordPreviewData,
  canArchiveExecutiveBriefRecord,
  openExecutiveBriefDialog,
  buildExecutiveBriefPlainText,
  copyExecutiveBriefText,
  exportExecutiveBriefMarkdown,
  exportExecutiveBriefWord,
  exportArchivedBriefMarkdown,
  exportArchivedBriefWord,
  loadExecutiveBriefRecordList,
  archiveCurrentExecutiveBrief,
  openExecutiveBriefRecordPreview,
  operationSnapshotLoading,
  operationSnapshotList,
  operationSnapshotDialogOpen,
  operationSnapshotPreviewData,
  loadOperationSnapshotList,
  archiveOperationSnapshot,
  openOperationSnapshotPreview,
  executiveActionItemLoading,
  executiveActionItemList,
  executiveActionItemDialogOpen,
  executiveActionItemFormRef,
  executiveActionItemDialogTitle,
  executiveActionItemForm,
  executiveActionItemRules,
  canManageExecutiveActionItem,
  pendingExecutiveActionItemCount,
  loadExecutiveActionItemList,
  openExecutiveActionItemDialog,
  submitExecutiveActionItemForm,
  handleExecutiveActionItemComplete,
  handleExecutiveActionItemRemove
} = useReportGovernance({
  proxy,
  userStore,
  dashboardData,
  managementOverviewData,
  hasManagementCockpitData,
  hasDecisionSupportData,
  executiveBriefData,
  executiveBriefSectionList,
  canManageDashboardTarget,
  loadDecisionBudgetConfig
})

// 返回客户利润 Top 列表，帮助管理层快速锁定高价值客户。
const customerProfitTopList = computed(() => customerProfitAnalysisData.value.topCustomerList || [])

// 返回客户集中度卡片，帮助管理层判断客户结构是否过于集中。
const customerConcentrationCardList = computed(() => {
  const concentrationData = customerProfitAnalysisData.value.concentration || {}
  return [
    {
      key: "top1ShareRate",
      title: "前1大客户占比",
      value: formatRateValue(concentrationData.top1ShareRate),
      description: `销售额 ￥${formatAmountValue(concentrationData.top1SaleAmount)}`
    },
    {
      key: "top3ShareRate",
      title: "前3大客户占比",
      value: formatRateValue(concentrationData.top3ShareRate),
      description: `销售额 ￥${formatAmountValue(concentrationData.top3SaleAmount)}`
    },
    {
      key: "top5ShareRate",
      title: "前5大客户占比",
      value: formatRateValue(concentrationData.top5ShareRate),
      description: `销售额 ￥${formatAmountValue(concentrationData.top5SaleAmount)}`
    },
    {
      key: "dependencyRiskText",
      title: "客户依赖度",
      value: concentrationData.dependencyRiskText || "待评估",
      description: concentrationData.dependencyDescription || "当前尚未生成客户集中度说明。"
    }
  ]
})

// 返回库存决策概览卡片，帮助管理层快速判断库存占资和周转情况。
const inventoryDecisionCardList = computed(() => {
  return [
    {
      key: "totalStockValueAmount",
      title: "当前库存货值",
      value: `￥${formatAmountValue(inventoryDecisionAnalysisData.value.totalStockValueAmount)}`,
      description: `可用库存 ￥${formatAmountValue(inventoryDecisionAnalysisData.value.availableStockValueAmount)}`
    },
    {
      key: "slowMovingStockValueAmount",
      title: "60天以上滞销货值",
      value: `￥${formatAmountValue(inventoryDecisionAnalysisData.value.slowMovingStockValueAmount)}`,
      description: `${normalizeCountValue(inventoryDecisionAnalysisData.value.slowMovingProductCount)} 个商品存在滞销风险`
    },
    {
      key: "recentOutboundCostAmount",
      title: "近30天出库成本",
      value: `￥${formatAmountValue(inventoryDecisionAnalysisData.value.recentOutboundCostAmount)}`,
      description: `库存周转天数 ${normalizeNumberValue(inventoryDecisionAnalysisData.value.inventoryTurnoverDays).toFixed(2)} 天`
    }
  ]
})

// 返回滞销商品列表，帮助管理层快速看到库存积压重点商品。
const inventorySlowMovingProductList = computed(() => inventoryDecisionAnalysisData.value.slowMovingProductList || [])

// 返回组织贡献的品类排行。
const categoryContributionList = computed(() => organizationContributionAnalysisData.value.categoryContributionList || [])

// 返回组织贡献的区域排行。
const regionContributionList = computed(() => organizationContributionAnalysisData.value.regionContributionList || [])

// 返回组织贡献的销售员排行。
const salespersonContributionList = computed(() => organizationContributionAnalysisData.value.salespersonContributionList || [])

// 返回当前选中的组织贡献列表，便于页面切换品类、区域和销售员排行。
const activeOrganizationContributionList = computed(() => {
  if (organizationContributionTab.value === "region")
  {
    return regionContributionList.value
  }
  if (organizationContributionTab.value === "salesperson")
  {
    return salespersonContributionList.value
  }
  return categoryContributionList.value
})

// 返回当前组织贡献排行的标题，帮助管理层理解当前维度。
const activeOrganizationContributionTitle = computed(() => {
  if (organizationContributionTab.value === "region")
  {
    return "区域经营贡献"
  }
  if (organizationContributionTab.value === "salesperson")
  {
    return "销售员经营贡献"
  }
  return "品类经营贡献"
})

// 计算现金流情景模拟结果，帮助管理层快速评估不同决策对 30 天资金窗口的影响。
const simulatedCashScenarioData = computed(() => {
  return buildSimulatedCashScenarioData(cashScenarioBaseData.value, cashScenarioForm)
})

// 判断后端是否按权限对毛利与成本字段进行了脱敏处理。
const isProfitMasked = computed(() => {
  const ov = managementOverviewData.value || {}
  return ov.currentMonthSaleAmount !== undefined
    && ov.currentMonthSaleAmount !== null
    && (ov.currentMonthGrossProfitAmount === null || ov.currentMonthGrossProfitAmount === undefined)
})

// 组装管理驾驶舱概览卡片，方便管理层快速浏览本月关键经营指标。
const managementOverviewCardList = computed(() => {
  return [
    {
      key: "currentMonthSaleAmount",
      title: "本月销售额",
      value: `￥${formatAmountValue(managementOverviewData.value.currentMonthSaleAmount)}`,
      tagText: buildGrowthTagText(managementOverviewData.value.saleGrowthRate),
      tagType: getGrowthTagType(managementOverviewData.value.saleGrowthRate),
      description: `上月销售额：￥${formatAmountValue(managementOverviewData.value.previousMonthSaleAmount)}`
    },
    {
      key: "currentMonthPurchaseAmount",
      title: "本月采购额",
      value: `￥${formatAmountValue(managementOverviewData.value.currentMonthPurchaseAmount)}`,
      tagText: buildGrowthTagText(managementOverviewData.value.purchaseGrowthRate),
      tagType: getGrowthTagType(managementOverviewData.value.purchaseGrowthRate),
      description: `上月采购额：￥${formatAmountValue(managementOverviewData.value.previousMonthPurchaseAmount)}`
    },
    {
      key: "currentMonthGrossProfitAmount",
      title: "本月毛利额",
      value: isProfitMasked.value ? "权限脱敏" : `￥${formatAmountValue(managementOverviewData.value.currentMonthGrossProfitAmount)}`,
      tagText: isProfitMasked.value ? "仅限财务/管理层" : buildGrowthTagText(managementOverviewData.value.grossProfitGrowthRate),
      tagType: isProfitMasked.value ? "info" : getGrowthTagType(managementOverviewData.value.grossProfitGrowthRate),
      description: isProfitMasked.value ? "当前角色未授权查看成本与毛利" : `毛利率：${formatRateValue(managementOverviewData.value.currentMonthGrossProfitRate)}`
    },
    {
      key: "currentMonthCollectionRate",
      title: "本月回款率",
      value: formatRateValue(managementOverviewData.value.currentMonthCollectionRate),
      tagText: buildRateDifferenceText(managementOverviewData.value.collectionRateDifference),
      tagType: getDifferenceTagType(managementOverviewData.value.collectionRateDifference),
      description: managementCockpitDataSource.value === "fallback"
        ? `本月销售已回款：￥${formatAmountValue(managementOverviewData.value.currentMonthReceivedAmount)}`
        : `本月回款额：￥${formatAmountValue(managementOverviewData.value.currentMonthReceivedAmount)}`
    }
  ]
})

// 组装风险雷达卡片，帮助管理层快速定位当下最需要关注的经营风险。
const riskOverviewCardList = computed(() => {
  const primaryPendingShortcutType = getPrimaryPendingShortcutType()
  return [
    {
      key: "overdueReceivableAmount",
      title: "逾期应收金额",
      value: `￥${formatAmountValue(riskOverviewData.value.overdueReceivableAmount)}`,
      description: "优先关注大额逾期客户，降低坏账风险",
      actionType: "overdueReceivable",
      actionLabel: "查看逾期应收"
    },
    {
      key: "dueSoonPayableAmount",
      title: `${riskOverviewData.value.payableRiskDays || 7}天内到期应付`,
      value: `￥${formatAmountValue(riskOverviewData.value.dueSoonPayableAmount)}`,
      description: "提前安排付款计划，避免影响供应协同",
      actionType: "payableSummary",
      actionLabel: "查看应付台账"
    },
    {
      key: "longOverdueReceivableCount",
      title: "超60天逾期应收",
      value: `${normalizeCountValue(riskOverviewData.value.longOverdueReceivableCount)}笔`,
      description: "长期拖欠单据需要升级跟进策略",
      actionType: "overdueReceivable",
      actionLabel: "查看逾期应收"
    },
    {
      key: "pendingApprovalTimeoutCount",
      title: "超3天未审核单据",
      value: `${normalizeCountValue(riskOverviewData.value.pendingApprovalTimeoutCount)}笔`,
      description: `当前库存预警 ${normalizeCountValue(riskOverviewData.value.stockWarningCount)} 项`,
      actionType: normalizeCountValue(riskOverviewData.value.pendingApprovalTimeoutCount) > 0 ? "todoCenter" : (primaryPendingShortcutType || "stockWarning"),
      actionLabel: normalizeCountValue(riskOverviewData.value.pendingApprovalTimeoutCount) > 0 ? "前往待办中心处理" : (primaryPendingShortcutType ? "查看待审核单据" : "查看预警库存")
    }
  ]
})

// 组装管理摘要，帮助管理层快速读懂当前经营状态变化。
const managementInsightList = computed(() => {
  if (!hasManagementCockpitData.value)
  {
    return []
  }
  const saleGrowthText = buildGrowthSummaryText(managementOverviewData.value.saleGrowthRate)
  const grossProfitRateText = isProfitMasked.value ? "权限脱敏" : formatRateValue(managementOverviewData.value.currentMonthGrossProfitRate)
  const overdueReceivableAmountText = formatAmountValue(riskOverviewData.value.overdueReceivableAmount)
  const dueSoonPayableAmountText = formatAmountValue(riskOverviewData.value.dueSoonPayableAmount)
  const pendingApprovalTimeoutCount = normalizeCountValue(riskOverviewData.value.pendingApprovalTimeoutCount)
  const managementInsightTextList = [
    isProfitMasked.value
      ? `本月销售额${saleGrowthText}，毛利指标按角色权限脱敏展示。`
      : `本月销售额${saleGrowthText}，当前毛利率为 ${grossProfitRateText}。`,
    `当前逾期应收金额为 ￥${overdueReceivableAmountText}，建议重点跟进高风险客户 Top5。`,
    `未来 ${riskOverviewData.value.payableRiskDays || 7} 天内到期应付为 ￥${dueSoonPayableAmountText}，另有 ${pendingApprovalTimeoutCount} 笔单据超过 3 天仍未审核。`
  ]
  if (hasDashboardTargetConfigured.value)
  {
    const laggingDashboardTargetCard = [...dashboardTargetCardList.value]
      .filter(dashboardTargetCard => dashboardTargetCard.targetValueText !== "未设置")
      .sort((leftDashboardTargetCard, rightDashboardTargetCard) => leftDashboardTargetCard.paceGapValue - rightDashboardTargetCard.paceGapValue)[0]
    if (laggingDashboardTargetCard)
    {
      managementInsightTextList.push(`本月时间进度已达 ${currentMonthProgressData.value.monthProgressPercentage.toFixed(2)}%，${laggingDashboardTargetCard.title}${laggingDashboardTargetCard.paceText}，${laggingDashboardTargetCard.forecastText}。`)
    }
  }
  return managementInsightTextList
})

// 组装经营健康度维度列表，帮助管理层从增长、回款、资金和流程四个方面快速判断系统状态。
const managementHealthDimensionList = computed(() => {
  if (!hasManagementCockpitData.value)
  {
    return []
  }
  const saleGrowthRateValue = normalizeNumberValue(managementOverviewData.value.saleGrowthRate)
  const grossProfitRateValue = normalizeNumberValue(managementOverviewData.value.currentMonthGrossProfitRate)
  const currentMonthCollectionRateValue = normalizeNumberValue(managementOverviewData.value.currentMonthCollectionRate)
  const overdueReceivableAmountValue = normalizeNumberValue(riskOverviewData.value.overdueReceivableAmount)
  const totalReceivableAmountValue = normalizeNumberValue(dashboardData.value.totalReceivableAmount)
  const overdueReceivableRateValue = totalReceivableAmountValue > 0 ? overdueReceivableAmountValue / totalReceivableAmountValue : 0
  const dueSoonPayableAmountValue = normalizeNumberValue(riskOverviewData.value.dueSoonPayableAmount)
  const pendingApprovalTimeoutCount = normalizeCountValue(riskOverviewData.value.pendingApprovalTimeoutCount)
  const stockWarningCountValue = normalizeCountValue(riskOverviewData.value.stockWarningCount)
  const negativeCashFlowWindowItem = cashFlowWindowList.value.find(cashFlowWindowItem => normalizeNumberValue(cashFlowWindowItem.netCashFlow) < 0)
  const primaryPendingShortcutType = getPrimaryPendingShortcutType()

  let growthQualityScoreValue = 70
  if (saleGrowthRateValue >= 10)
  {
    growthQualityScoreValue += 15
  }
  else if (saleGrowthRateValue >= 0)
  {
    growthQualityScoreValue += 8
  }
  else if (saleGrowthRateValue < -10)
  {
    growthQualityScoreValue -= 12
  }
  if (!isProfitMasked.value)
  {
    if (grossProfitRateValue >= 20)
    {
      growthQualityScoreValue += 15
    }
    else if (grossProfitRateValue >= 10)
    {
      growthQualityScoreValue += 8
    }
    else if (grossProfitRateValue < 5)
    {
      growthQualityScoreValue -= 12
    }
  }

  let collectionSafetyScoreValue = 75
  if (currentMonthCollectionRateValue >= 80)
  {
    collectionSafetyScoreValue += 10
  }
  else if (currentMonthCollectionRateValue >= 60)
  {
    collectionSafetyScoreValue += 5
  }
  else
  {
    collectionSafetyScoreValue -= 10
  }
  if (overdueReceivableRateValue <= 0.1)
  {
    collectionSafetyScoreValue += 10
  }
  else if (overdueReceivableRateValue > 0.4)
  {
    collectionSafetyScoreValue -= 20
  }
  else if (overdueReceivableRateValue > 0.25)
  {
    collectionSafetyScoreValue -= 10
  }

  let cashFlowSafetyScoreValue = 75
  if (negativeCashFlowWindowItem)
  {
    if (normalizeCountValue(negativeCashFlowWindowItem.forecastDays) <= 7)
    {
      cashFlowSafetyScoreValue -= 25
    }
    else if (normalizeCountValue(negativeCashFlowWindowItem.forecastDays) <= 15)
    {
      cashFlowSafetyScoreValue -= 15
    }
    else
    {
      cashFlowSafetyScoreValue -= 10
    }
  }
  if (dueSoonPayableAmountValue <= 0)
  {
    cashFlowSafetyScoreValue += 10
  }
  else if (dueSoonPayableAmountValue > overdueReceivableAmountValue && overdueReceivableAmountValue > 0)
  {
    cashFlowSafetyScoreValue -= 10
  }

  let processEfficiencyScoreValue = 80
  if (pendingApprovalTimeoutCount === 0)
  {
    processEfficiencyScoreValue += 10
  }
  else if (pendingApprovalTimeoutCount > 5)
  {
    processEfficiencyScoreValue -= 25
  }
  else if (pendingApprovalTimeoutCount > 2)
  {
    processEfficiencyScoreValue -= 15
  }
  if (stockWarningCountValue === 0)
  {
    processEfficiencyScoreValue += 5
  }
  else if (stockWarningCountValue > 10)
  {
    processEfficiencyScoreValue -= 20
  }
  else if (stockWarningCountValue > 5)
  {
    processEfficiencyScoreValue -= 10
  }

  const collectionSafetyActionType = overdueReceivableAmountValue > 0 ? "overdueReceivable" : "receivableSummary"
  const cashFlowSafetyActionType = negativeCashFlowWindowItem || dueSoonPayableAmountValue >= overdueReceivableAmountValue
    ? "payableSummary"
    : "overdueReceivable"
  const processEfficiencyActionType = pendingApprovalTimeoutCount > 0
    ? "todoCenter"
    : "stockWarning"

  return [
    {
      key: "growthQuality",
      title: "增长质量",
      score: normalizeScoreValue(growthQualityScoreValue),
      description: isProfitMasked.value
        ? `销售${buildGrowthSummaryText(saleGrowthRateValue)}，毛利指标按权限脱敏。`
        : `销售${buildGrowthSummaryText(saleGrowthRateValue)}，毛利率 ${formatRateValue(grossProfitRateValue)}。`,
      adviceText: saleGrowthRateValue < 0 || (!isProfitMasked.value && grossProfitRateValue < 10)
        ? "建议优先复盘下滑订单和低毛利订单，确认价格策略与客户结构。"
        : "建议继续跟踪高毛利客户和重点订单，保持增长质量。",
      actionType: "saleOrderSummary",
      actionLabel: "查看销售单"
    },
    {
      key: "collectionSafety",
      title: "回款安全",
      score: normalizeScoreValue(collectionSafetyScoreValue),
      description: `本月回款率 ${formatRateValue(currentMonthCollectionRateValue)}，逾期应收占比 ${(overdueReceivableRateValue * 100).toFixed(2)}%。`,
      adviceText: overdueReceivableAmountValue > 0
        ? "建议先处理逾期客户和大额未收款订单，避免坏账继续累积。"
        : "建议继续跟踪新增应收和本月回款节奏，维持健康回款。",
      actionType: collectionSafetyActionType,
      actionLabel: collectionSafetyActionType === "overdueReceivable" ? "查看逾期应收" : "查看应收台账"
    },
    {
      key: "cashFlowSafety",
      title: "资金压力",
      score: normalizeScoreValue(cashFlowSafetyScoreValue),
      description: negativeCashFlowWindowItem
        ? `${negativeCashFlowWindowItem.forecastDays} 天内存在净流出压力，近期应付 ￥${formatAmountValue(dueSoonPayableAmountValue)}。`
        : `近期未出现净流出压力，${riskOverviewData.value.payableRiskDays || 7} 天内应付 ￥${formatAmountValue(dueSoonPayableAmountValue)}。`,
      adviceText: negativeCashFlowWindowItem
        ? `建议优先确认未来 ${negativeCashFlowWindowItem.forecastDays} 天的付款安排和资金覆盖。`
        : "建议持续平衡回款与付款节奏，保持近期现金流稳定。",
      actionType: cashFlowSafetyActionType,
      actionLabel: cashFlowSafetyActionType === "payableSummary" ? "查看应付台账" : "查看逾期应收"
    },
    {
      key: "processEfficiency",
      title: "流程效率",
      score: normalizeScoreValue(processEfficiencyScoreValue),
      description: `超时未审核 ${pendingApprovalTimeoutCount} 笔，库存预警 ${stockWarningCountValue} 项。`,
      adviceText: pendingApprovalTimeoutCount > 0
        ? "建议先清理超时审核单据，避免销售、采购和仓储流程继续积压。"
        : stockWarningCountValue > 0
          ? "建议安排仓库和采购优先处理库存预警，降低断货和积压风险。"
          : "建议继续保持当前审核和库存执行节奏。",
      actionType: processEfficiencyActionType,
      actionLabel: processEfficiencyActionType === "stockWarning" ? "查看预警库存" : "查看待审核单据"
    }
  ].map(managementHealthDimension => ({
    ...managementHealthDimension,
    levelText: getManagementHealthLevelText(managementHealthDimension.score),
    tagType: getManagementHealthTagType(managementHealthDimension.score)
  }))
})

// 汇总经营健康度，帮助管理层先判断整体状态，再决定看哪一块明细。
const managementHealthSummary = computed(() => {
  if (!managementHealthDimensionList.value.length)
  {
    return {
      score: 0,
      levelText: "待评估",
      tagType: "info",
      weakestTitle: "",
      description: "当前尚未生成经营健康度评估。",
      adviceText: "建议先检查经营报表数据是否已生成，再判断重点处理项。",
      actionType: undefined,
      actionLabel: ""
    }
  }
  const totalHealthScoreValue = managementHealthDimensionList.value.reduce((previousScoreValue, managementHealthDimension) => {
    return previousScoreValue + normalizeNumberValue(managementHealthDimension.score)
  }, 0)
  const averageHealthScoreValue = normalizeScoreValue(totalHealthScoreValue / managementHealthDimensionList.value.length)
  const weakestManagementHealthDimension = [...managementHealthDimensionList.value]
    .sort((leftManagementHealthDimension, rightManagementHealthDimension) => leftManagementHealthDimension.score - rightManagementHealthDimension.score)[0]
  return {
    score: averageHealthScoreValue,
    levelText: getManagementHealthLevelText(averageHealthScoreValue),
    tagType: getManagementHealthTagType(averageHealthScoreValue),
    weakestTitle: weakestManagementHealthDimension?.title || "",
    description: weakestManagementHealthDimension
      ? `当前最弱环节是${weakestManagementHealthDimension.title}，${weakestManagementHealthDimension.description}`
      : "当前经营维度整体平稳。",
    adviceText: weakestManagementHealthDimension
      ? weakestManagementHealthDimension.adviceText
      : "建议继续保持当前经营节奏，并持续关注风险变化。",
    actionType: weakestManagementHealthDimension?.actionType,
    actionLabel: weakestManagementHealthDimension?.actionLabel || ""
  }
})

// 组装经营预警待办列表，帮助管理层快速锁定今天最需要推动的事项和入口。
const managementTodoList = computed(() => {
  if (!hasManagementCockpitData.value)
  {
    return []
  }
  const managementTodoItemList = []
  const overdueReceivableAmountValue = normalizeNumberValue(riskOverviewData.value.overdueReceivableAmount)
  if (overdueReceivableAmountValue > 0)
  {
    managementTodoItemList.push({
      key: "overdueReceivable",
      title: "逾期应收需要加速催收",
      levelType: "danger",
      levelText: "高优先级",
      metricText: `￥${formatAmountValue(overdueReceivableAmountValue)}`,
      description: "当前仍存在逾期应收，建议优先查看逾期台账并督促销售、财务同步跟进。",
      actionType: "overdueReceivable",
      actionLabel: "查看逾期应收"
    })
  }

  const negativeCashFlowWindowItem = cashFlowWindowList.value.find(cashFlowWindowItem => normalizeNumberValue(cashFlowWindowItem.netCashFlow) < 0)
  if (negativeCashFlowWindowItem)
  {
    managementTodoItemList.push({
      key: `cashFlowPressure-${negativeCashFlowWindowItem.forecastDays}`,
      title: `${negativeCashFlowWindowItem.forecastDays}天内存在净流出压力`,
      levelType: "danger",
      levelText: "资金风险",
      metricText: `￥${formatAmountValue(Math.abs(negativeCashFlowWindowItem.netCashFlow))}`,
      description: `${negativeCashFlowWindowItem.description} 建议同步关注近期回款和大额付款安排。`,
      actionType: "receivableSummary",
      actionLabel: "查看应收台账"
    })
  }

  const laggingDashboardTargetCard = [...dashboardTargetCardList.value]
    .filter(dashboardTargetCard => dashboardTargetCard.targetValueText !== "未设置" && dashboardTargetCard.paceGapValue < 0)
    .sort((leftDashboardTargetCard, rightDashboardTargetCard) => leftDashboardTargetCard.paceGapValue - rightDashboardTargetCard.paceGapValue)[0]
  if (laggingDashboardTargetCard)
  {
    managementTodoItemList.push({
      key: `dashboardTarget-${laggingDashboardTargetCard.key}`,
      title: `${laggingDashboardTargetCard.title}目标偏离`,
      levelType: laggingDashboardTargetCard.paceTagType === "danger" ? "danger" : "warning",
      levelText: laggingDashboardTargetCard.paceTagType === "danger" ? "经营偏离" : "重点关注",
      metricText: laggingDashboardTargetCard.progressText,
      description: `${laggingDashboardTargetCard.paceText}，${laggingDashboardTargetCard.forecastText}。`,
      actionType: resolveDashboardTargetActionType(laggingDashboardTargetCard.key),
      actionLabel: resolveDashboardTargetActionLabel(laggingDashboardTargetCard.key)
    })
  }

  const pendingApprovalTimeoutCount = normalizeCountValue(riskOverviewData.value.pendingApprovalTimeoutCount)
  if (pendingApprovalTimeoutCount > 0)
  {
    managementTodoItemList.push({
      key: "pendingApprovalTimeout",
      title: "超3天未审核单据待处理",
      levelType: "warning",
      levelText: "流程堵点",
      metricText: `${pendingApprovalTimeoutCount}笔`,
      description: "流程审批存在积压，建议进入待办中心统一清理超时未审单据，避免影响出入库与回款节奏。",
      actionType: "todoCenter",
      actionLabel: "前往待办中心处理"
    })
  }

  const stockWarningCountValue = normalizeCountValue(riskOverviewData.value.stockWarningCount)
  if (stockWarningCountValue > 0)
  {
    managementTodoItemList.push({
      key: "stockWarning",
      title: "库存预警需要及时处理",
      levelType: "warning",
      levelText: "库存风险",
      metricText: `${stockWarningCountValue}项`,
      description: "库存预警仍未消化，建议安排采购、仓库和销售同步确认补货或清理策略。",
      actionType: "stockWarning",
      actionLabel: "查看预警库存"
    })
  }

  const dueSoonPayableAmountValue = normalizeNumberValue(riskOverviewData.value.dueSoonPayableAmount)
  if (dueSoonPayableAmountValue > 0)
  {
    managementTodoItemList.push({
      key: "dueSoonPayable",
      title: "近期付款计划需要确认",
      levelType: "info",
      levelText: "付款关注",
      metricText: `￥${formatAmountValue(dueSoonPayableAmountValue)}`,
      description: `未来 ${riskOverviewData.value.payableRiskDays || 7} 天内存在集中付款压力，建议结合现金流窗口确认付款顺序。`,
      actionType: "payableSummary",
      actionLabel: "查看应付台账"
    })
  }

  return managementTodoItemList.slice(0, 5)
})

const filteredMessageCenterList = computed(() => {
  const messageCenterList = dashboardData.value.messageCenter || []
  if (selectedMessageType.value === "all")
  {
    return messageCenterList
  }
  return messageCenterList.filter(messageItem => messageItem.message_type === selectedMessageType.value)
})


// 分发管理驾驶舱里的统一动作，保证健康度、待办和目标等区域跳转口径一致。
function handleManagementAction(actionType) {
  if (!actionType)
  {
    return
  }
  if (actionType === "todoCenter")
  {
    router.push({ path: businessRoutePathMap.todoCenter })
    return
  }
  if (actionType === "payableSummary")
  {
    router.push({ path: businessRoutePathMap.payable })
    return
  }
  if (actionType === "configureDashboardTarget")
  {
    openDashboardTargetDialog()
    return
  }
  handleDashboardShortcut(actionType)
}


// 选择当前最值得先处理的待审核入口，优先进入积压最多的单据列表。
function getPrimaryPendingShortcutType() {
  const pendingShortcutItemList = [
    { shortcutType: "pendingSaleOrder", countValue: normalizeCountValue(dashboardData.value.pendingSaleOrderCount) },
    { shortcutType: "pendingInbound", countValue: normalizeCountValue(dashboardData.value.pendingInboundCount) },
    { shortcutType: "pendingOutbound", countValue: normalizeCountValue(dashboardData.value.pendingOutboundCount) }
  ].filter(pendingShortcutItem => pendingShortcutItem.countValue > 0)
  if (!pendingShortcutItemList.length)
  {
    return undefined
  }
  return pendingShortcutItemList.sort((leftPendingShortcutItem, rightPendingShortcutItem) => rightPendingShortcutItem.countValue - leftPendingShortcutItem.countValue)[0].shortcutType
}

// 在旧后端环境中按现有列表接口回补管理驾驶舱数据，确保管理层仍能先看到核心经营指标。
async function hydrateManagementCockpitFallbackData() {
  const currentMonthRange = buildMonthRange(0)
  const previousMonthRange = buildMonthRange(-1)
  const settledResultList = await Promise.allSettled([
    fetchPagedRowList(listSaleReport, {
      startDate: previousMonthRange.startDate,
      endDate: currentMonthRange.endDate
    }),
    fetchPagedRowList(listReceivable),
    fetchPagedRowList(listPurchaseOrder),
    fetchPagedRowList(listProduct),
    fetchPagedRowList(listPayable),
    fetchPagedRowList(listCustomer),
    fetchPagedRowList(listSupplier),
    fetchPagedRowList(listSaleOrder),
    fetchPagedRowList(listInbound, {
      status: "submitted"
    }),
    fetchPagedRowList(listOutbound, {
      status: "submitted"
    })
  ])

  const saleReportRowList = getSettledResultValue(settledResultList[0], [])
  const receivableRowList = getSettledResultValue(settledResultList[1], [])
  const purchaseOrderRowList = getSettledResultValue(settledResultList[2], [])
  const productRowList = getSettledResultValue(settledResultList[3], [])
  const payableRowList = getSettledResultValue(settledResultList[4], [])
  const customerRowList = getSettledResultValue(settledResultList[5], [])
  const supplierRowList = getSettledResultValue(settledResultList[6], [])
  const saleOrderRowList = getSettledResultValue(settledResultList[7], [])
  const inboundRowList = getSettledResultValue(settledResultList[8], [])
  const outboundRowList = getSettledResultValue(settledResultList[9], [])

  const productCostPriceMap = buildProductCostPriceMap(productRowList)
  const customerNameMap = buildCustomerNameMap(customerRowList)
  const supplierNameMap = buildSupplierNameMap(supplierRowList)
  const managementOverview = buildManagementOverviewData(
    saleReportRowList,
    receivableRowList,
    purchaseOrderRowList,
    productCostPriceMap,
    saleOrderRowList,
    currentMonthRange,
    previousMonthRange
  )
  const riskOverview = buildRiskOverviewData(
    receivableRowList,
    payableRowList,
    saleOrderRowList,
    inboundRowList,
    outboundRowList,
    purchaseOrderRowList,
    dashboardData.value.stockWarningCount,
    7
  )

  dashboardData.value = {
    ...dashboardData.value,
    managementCockpitDataSource: "fallback",
    managementOverview: managementOverview,
    riskOverview: riskOverview,
    receivableAgingList: buildReceivableAgingListFromRowList(receivableRowList),
    payableAgingList: buildPayableAgingListFromRowList(payableRowList),
    topRiskCustomerList: buildTopRiskCustomerList(receivableRowList, customerNameMap),
    topRiskSupplierList: buildTopRiskSupplierList(payableRowList, supplierNameMap, 7)
  }
}

// 加载管理层决策支持数据，统一生成预算、利润、库存、组织贡献、情景模拟和经营简报模块。
async function loadDecisionSupportData() {
  decisionSupportLoading.value = true
  try
  {
    const currentMonthRange = buildMonthRange(0)
    const settledResultList = await Promise.allSettled([
      checkPermi(['business:report:sale', 'business:report:view'])
        ? fetchPagedRowList(listSaleReport, { startDate: currentMonthRange.startDate, endDate: currentMonthRange.endDate })
        : Promise.resolve([]),
      checkPermi(['business:saleOrder:list']) ? fetchPagedRowList(listSaleOrder) : Promise.resolve([]),
      checkPermi(['business:customer:list']) ? fetchPagedRowList(listCustomer) : Promise.resolve([]),
      checkPermi(['business:receivable:list']) ? fetchPagedRowList(listReceivable) : Promise.resolve([]),
      checkPermi(['business:product:list']) ? fetchPagedRowList(listProduct) : Promise.resolve([]),
      checkPermi(['business:stock:list']) ? fetchPagedRowList(listStock) : Promise.resolve([]),
      checkPermi(['business:stockLog:list']) ? fetchPagedRowList(listStockLog) : Promise.resolve([]),
      checkPermi(['business:receipt:list']) ? fetchPagedRowList(listReceipt) : Promise.resolve([]),
      checkPermi(['business:payment:list']) ? fetchPagedRowList(listPayment) : Promise.resolve([]),
      checkPermi(['business:payable:list']) ? fetchPagedRowList(listPayable) : Promise.resolve([])
    ])

    const saleReportRowList = getSettledResultValue(settledResultList[0], [])
    const saleOrderRowList = getSettledResultValue(settledResultList[1], [])
    const customerRowList = getSettledResultValue(settledResultList[2], [])
    const receivableRowList = getSettledResultValue(settledResultList[3], [])
    const productRowList = getSettledResultValue(settledResultList[4], [])
    const stockRowList = getSettledResultValue(settledResultList[5], [])
    const stockLogRowList = getSettledResultValue(settledResultList[6], [])
    const receiptRowList = getSettledResultValue(settledResultList[7], [])
    const paymentRowList = getSettledResultValue(settledResultList[8], [])
    const payableRowList = getSettledResultValue(settledResultList[9], [])

    const decisionBudgetOverview = buildDecisionBudgetOverviewData(
      managementOverviewData.value,
      receiptRowList,
      paymentRowList,
      currentMonthRange
    )
    const customerProfitAnalysis = buildCustomerProfitAnalysisData(
      saleReportRowList,
      saleOrderRowList,
      customerRowList,
      receivableRowList,
      productRowList
    )
    const inventoryDecisionAnalysis = buildInventoryDecisionAnalysisData(
      stockRowList,
      stockLogRowList,
      productRowList
    )
    const organizationContributionAnalysis = buildOrganizationContributionAnalysisData(
      saleReportRowList,
      saleOrderRowList,
      customerRowList,
      productRowList
    )
    const cashScenarioBase = buildCashScenarioBaseData(
      receivableRowList,
      payableRowList,
      receiptRowList,
      paymentRowList
    )
    const executiveBrief = buildExecutiveBriefData(
      managementOverviewData.value,
      riskOverviewData.value,
      customerProfitAnalysis,
      inventoryDecisionAnalysis,
      decisionBudgetOverview
    )

    dashboardData.value = {
      ...dashboardData.value,
      decisionSupportData: {
        decisionBudgetOverview: decisionBudgetOverview,
        customerProfitAnalysis: customerProfitAnalysis,
        inventoryDecisionAnalysis: inventoryDecisionAnalysis,
        organizationContributionAnalysis: organizationContributionAnalysis,
        cashScenarioBaseData: cashScenarioBase,
        executiveBriefData: executiveBrief
      }
    }
    resetCashScenarioForm()
  }
  finally
  {
    decisionSupportLoading.value = false
  }
}

// 重置现金流情景模拟输入，方便管理层快速回到基准场景。
function resetCashScenarioForm() {
  cashScenarioForm.delayedCollectionAmount = 0
  cashScenarioForm.acceleratedPaymentAmount = 0
  cashScenarioForm.additionalSaleCollectionAmount = 0
  cashScenarioForm.additionalProcurementAmount = 0
  cashScenarioForm.temporaryFinancingAmount = 0
}

// 处理客户利润模块的查看动作，优先跳到客户应收或客户跟进页面。
function handleCustomerProfitAction(customerProfitRow) {
  if (!customerProfitRow?.customerId)
  {
    router.push({ path: businessRoutePathMap.saleOrder })
    return
  }
  if (normalizeNumberValue(customerProfitRow.overdueAmount) > 0 || normalizeNumberValue(customerProfitRow.remainAmount) > 0)
  {
    router.push({
      path: businessRoutePathMap.receivable,
      query: {
        customerId: customerProfitRow.customerId,
        overdueQuery: normalizeNumberValue(customerProfitRow.overdueAmount) > 0 ? "1" : undefined
      }
    })
    return
  }
  router.push({
    path: businessRoutePathMap.customerFollow,
    query: {
      customerId: customerProfitRow.customerId
    }
  })
}

// 处理滞销库存查看动作，优先带着商品筛选打开库存流水页面。
function handleInventoryProductAction(inventoryProductRow) {
  if (!inventoryProductRow?.productId)
  {
    router.push({ path: businessRoutePathMap.stock })
    return
  }
  router.push({
    path: "/inventory/stockLog",
    query: {
      productId: inventoryProductRow.productId
    }
  })
}

// 查询 7/15/30 天资金压力窗口，帮助管理层快速判断近期现金流入流出结构。
async function loadCashFlowWindowData() {
  cashFlowWindowLoading.value = true
  try
  {
    const forecastDayList = [7, 15, 30]
    const settledResultList = await Promise.allSettled(
      forecastDayList.map(forecastDays => getCashFlowForecast({ forecastDays: forecastDays }))
    )
    cashFlowWindowList.value = forecastDayList.map((forecastDays, indexValue) => {
      const cashFlowResponse = getSettledResultValue(settledResultList[indexValue], {})
      const cashFlowData = cashFlowResponse?.data || {}
      const receivableAmount = normalizeNumberValue(getFirstDefinedValue(cashFlowData, ["receivableAmount", "receivable_amount"]))
      const payableAmount = normalizeNumberValue(getFirstDefinedValue(cashFlowData, ["payableAmount", "payable_amount"]))
      const netCashFlow = normalizeNumberValue(getFirstDefinedValue(cashFlowData, ["netCashFlow", "net_cash_flow"]))
      return {
        forecastDays: forecastDays,
        receivableAmount: receivableAmount,
        payableAmount: payableAmount,
        netCashFlow: netCashFlow,
        tagType: getCashFlowWindowTagType(netCashFlow),
        tagText: getCashFlowWindowTagText(netCashFlow),
        description: buildCashFlowWindowDescription(netCashFlow, forecastDays)
      }
    })
  }
  finally
  {
    cashFlowWindowLoading.value = false
  }
}

// 点击经营看板卡片时跳转到对应业务页面，帮助用户直接进入待处理列表。
function handleDashboardShortcut(shortcutType) {
  if (shortcutType === "saleOrderSummary")
  {
    router.push({ path: businessRoutePathMap.saleOrder })
    return
  }
  if (shortcutType === "receivableSummary")
  {
    router.push({ path: businessRoutePathMap.receivable })
    return
  }
  if (shortcutType === "receiptSummary")
  {
    router.push({ path: businessRoutePathMap.receipt })
    return
  }
  if (shortcutType === "stockWarning")
  {
    router.push({
      path: businessRoutePathMap.stock,
      query: { warningQuery: "1" }
    })
    return
  }
  if (shortcutType === "pendingSaleOrder")
  {
    router.push({
      path: businessRoutePathMap.saleOrder,
      query: { status: "submitted" }
    })
    return
  }
  if (shortcutType === "pendingInbound")
  {
    router.push({
      path: businessRoutePathMap.inbound,
      query: { status: "submitted" }
    })
    return
  }
  if (shortcutType === "pendingOutbound")
  {
    router.push({
      path: businessRoutePathMap.outbound,
      query: { status: "submitted" }
    })
    return
  }
  if (shortcutType === "overdueReceivable")
  {
    router.push({
      path: businessRoutePathMap.receivable,
      query: { overdueQuery: "1" }
    })
  }
}

// 从对账中心直接跳到对应台账，减少财务人工二次查询。
function handleReconciliationAction(reconciliationRow) {
  if (reconciliationRow.reconcileType === "receivable")
  {
    router.push({
      path: businessRoutePathMap.receivable,
      query: {
        customerId: reconciliationRow.targetId
      }
    })
    return
  }
  router.push({
    path: businessRoutePathMap.payable,
    query: {
      supplierId: reconciliationRow.targetId
    }
  })
}

// 返回对账中心对象名称，旧环境只返回编号时也给出明确但不暴露内部编号的提示。
function getReconciliationTargetName(reconciliationRow) {
  const targetName = reconciliationRow?.targetName
  if (!targetName)
  {
    return reconciliationRow?.reconcileType === "receivable" ? "客户资料缺失" : "供应商资料缺失"
  }
  if (/^\d+$/.test(String(targetName)))
  {
    return reconciliationRow?.reconcileType === "receivable" ? "客户资料缺失" : "供应商资料缺失"
  }
  return targetName
}


// 判断高风险客户行是否具备可跳转的客户编号，避免历史脏数据进入空白页面。
function canViewRiskCustomerDetail(riskCustomerRow) {
  return !!riskCustomerRow?.targetId
}

// 从高风险客户榜单直接跳到应收台账，方便管理层查看逾期明细和跟进进度。
function handleTopRiskCustomerAction(riskCustomerRow) {
  if (!canViewRiskCustomerDetail(riskCustomerRow))
  {
    return
  }
  router.push({
    path: businessRoutePathMap.receivable,
    query: {
      customerId: riskCustomerRow.targetId,
      overdueQuery: "1"
    }
  })
}

// 判断重点付款供应商行是否具备可跳转的供应商编号，避免历史脏数据进入空白页面。
function canViewRiskSupplierDetail(riskSupplierRow) {
  return !!riskSupplierRow?.targetId
}

// 从重点付款供应商榜单直接跳到应付台账，方便管理层查看待付款明细。
function handleTopRiskSupplierAction(riskSupplierRow) {
  if (!canViewRiskSupplierDetail(riskSupplierRow))
  {
    return
  }
  router.push({
    path: businessRoutePathMap.payable,
    query: {
      supplierId: riskSupplierRow.targetId
    }
  })
}

// 处理经营预警待办的跳转，确保管理层可以从待办直接进入对应处理页面。
function handleManagementTodoAction(managementTodoItem) {
  handleManagementAction(managementTodoItem?.actionType)
}

// 处理经营健康度卡片的动作入口，让管理层能从最弱维度直接进入对应页面。
function handleManagementHealthAction(managementHealthItem) {
  handleManagementAction(managementHealthItem?.actionType)
}

// 处理风险雷达卡片的动作入口，确保管理层在风险区也能直接下钻处理。
function handleRiskOverviewAction(riskOverviewCard) {
  handleManagementAction(riskOverviewCard?.actionType)
}

// 账龄区间点击下钻至对应应收/应付台账页面并自动筛选该账龄区间。
function handleAgingBucketDrillDown(side, agingRow) {
  if (!agingRow) return
  const sortOrderBucketMap = {
    0: "no_due",
    1: "not_due",
    2: "1_30",
    3: "31_60",
    4: "61_plus"
  }
  const nameBucketMap = {
    "未设置到期日": "no_due",
    "未到期": "not_due",
    "逾期1-30天": "1_30",
    "逾期31-60天": "31_60",
    "逾期61天以上": "61_plus"
  }
  const bucketCode = sortOrderBucketMap[agingRow.sortOrder] || nameBucketMap[agingRow.bucketName]
  const targetPath = side === "payable" ? businessRoutePathMap.payable : businessRoutePathMap.receivable
  router.push({
    path: targetPath,
    query: bucketCode ? { agingBucket: bucketCode } : {}
  })
}

// 浏览器尺寸变化时同步调整图表大小，避免经营看板在缩放后布局错位。
function handleChartResize() {
  if (saleTrendChartInstance.value && !saleTrendChartInstance.value.isDisposed?.())
  {
    saleTrendChartInstance.value.resize()
  }
  if (topCustomerChartInstance.value && !topCustomerChartInstance.value.isDisposed?.())
  {
    topCustomerChartInstance.value.resize()
  }
}

// 初始化经营看板图表，只在容器存在时创建实例并刷新最新数据。
function initCharts(data) {
  if (!saleTrendRef.value || !topCustomerRef.value)
  {
    return
  }
  const rootStyle = getComputedStyle(document.documentElement)
  const mutedTextColor = rootStyle.getPropertyValue("--erp-text-muted").trim() || "#64748b"
  const borderColor = rootStyle.getPropertyValue("--erp-border").trim() || "#e5eaf2"
  const primaryColor = rootStyle.getPropertyValue("--erp-primary").trim() || "#4f46e5"

  if (!saleTrendChartInstance.value || saleTrendChartInstance.value.isDisposed?.())
  {
    saleTrendChartInstance.value = markRaw(echarts.init(saleTrendRef.value))
  }
  saleTrendChartInstance.value.setOption({
    tooltip: { trigger: "axis" },
    grid: { left: 16, right: 28, top: 28, bottom: 24, containLabel: true },
    xAxis: {
      type: "category",
      boundaryGap: true,
      data: data.saleTrend?.dateList || [],
      axisLabel: { color: mutedTextColor, hideOverlap: true },
      axisLine: { lineStyle: { color: borderColor } },
      axisTick: { show: false }
    },
    yAxis: {
      type: "value",
      axisLabel: { color: mutedTextColor },
      splitLine: { lineStyle: { color: borderColor, type: "dashed" } }
    },
    series: [
      {
        type: "line",
        smooth: true,
        symbolSize: 7,
        lineStyle: { width: 2.5, color: primaryColor },
        areaStyle: {
          color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
            { offset: 0, color: "rgba(79, 70, 229, 0.24)" },
            { offset: 1, color: "rgba(79, 70, 229, 0.01)" }
          ])
        },
        itemStyle: { color: primaryColor },
        data: data.saleTrend?.amountList || []
      }
    ]
  })

  if (!topCustomerChartInstance.value || topCustomerChartInstance.value.isDisposed?.())
  {
    topCustomerChartInstance.value = markRaw(echarts.init(topCustomerRef.value))
  }
  topCustomerChartInstance.value.setOption({
    tooltip: { trigger: "axis" },
    grid: { left: 16, right: 24, top: 28, bottom: 32, containLabel: true },
    xAxis: {
      type: "category",
      data: data.topCustomer?.customerList || [],
      axisLabel: {
        interval: 0,
        rotate: 22,
        color: mutedTextColor,
        formatter: (value) => (value && value.length > 7 ? `${value.slice(0, 7)}…` : value)
      },
      axisLine: { lineStyle: { color: borderColor } },
      axisTick: { show: false }
    },
    yAxis: {
      type: "value",
      axisLabel: { color: mutedTextColor },
      splitLine: { lineStyle: { color: borderColor, type: "dashed" } }
    },
    series: [
      {
        type: "bar",
        barWidth: "38%",
        itemStyle: {
          borderRadius: [8, 8, 2, 2],
          color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
            { offset: 0, color: "#6366f1" },
            { offset: 1, color: "#3b82f6" }
          ])
        },
        data: data.topCustomer?.amountList || []
      }
    ]
  })

}

// 销毁经营看板图表实例，避免页面离开后残留无效监听。
function disposeCharts() {
  if (saleTrendChartInstance.value)
  {
    saleTrendChartInstance.value.dispose()
    saleTrendChartInstance.value = null
  }
  if (topCustomerChartInstance.value)
  {
    topCustomerChartInstance.value.dispose()
    topCustomerChartInstance.value = null
  }
  window.removeEventListener("resize", handleChartResize)
}

// 查询经营看板首页数据，并在旧后端环境下自动回补管理驾驶舱兼容数据。
async function getData() {
  managementCockpitLoading.value = true
  try
  {
    const response = await getDashboard()
    dashboardData.value = normalizeDashboardSummaryData(response)
  }
  catch (error)
  {
    dashboardData.value = normalizeDashboardSummaryData({})
    await nextTick()
    initCharts({})
    managementCockpitLoading.value = false
    return
  }

  try
  {
    if (!hasRemoteManagementCockpitData(dashboardData.value))
    {
      await hydrateManagementCockpitFallbackData()
    }
  }
  catch (error)
  {
    console.error("经营报表基础数据回补失败：", error)
  }
  finally
  {
    await nextTick()
    initCharts(dashboardData.value)
    managementCockpitLoading.value = false
  }

  if (hasManagementCockpitData.value)
  {
    setTimeout(() => {
      loadDecisionSupportData().catch(err => {
        console.error("经营分析扩展数据加载失败：", err)
      })
    }, 80)
  }
}

// 查询对账中心数据，便于管理层快速查看客户和供应商往来余额。
function getReconciliationData() {
  reconciliationLoading.value = true
  listReconciliation(reconciliationQueryParams.value).then(response => {
    reconciliationList.value = response.rows
    reconciliationTotal.value = response.total
    reconciliationLoading.value = false
  }).catch(() => {
    reconciliationLoading.value = false
  })
}

// 重置对账中心查询条件。
function resetReconciliationQuery() {
  reconciliationQueryParams.value.targetName = undefined
  reconciliationQueryParams.value.pageNum = 1
  getReconciliationData()
}

// 查询发票税务数据，帮助管理层核对业务发生与税额情况。
function getInvoiceTaxData() {
  invoiceTaxLoading.value = true
  listInvoiceTax(invoiceTaxQueryParams.value).then(response => {
    invoiceTaxList.value = response.rows
    invoiceTaxTotal.value = response.total
    invoiceTaxLoading.value = false
  }).catch(() => {
    invoiceTaxLoading.value = false
  })
}

// 重置发票税务查询条件。
function resetInvoiceTaxQuery() {
  invoiceTaxQueryParams.value.startDate = undefined
  invoiceTaxQueryParams.value.endDate = undefined
  invoiceTaxQueryParams.value.pageNum = 1
  getInvoiceTaxData()
}

// 查询资金预测数据，帮助管理层评估近期资金流入流出压力。
function getCashFlowForecastData() {
  getCashFlowForecast({ forecastDays: cashFlowForecastQueryParams.value.forecastDays }).then(response => {
    cashFlowForecastData.value = response.data || {}
    loadCashFlowWindowData()
  })
}

// 根据统一待办中心传入的类型定位经营看板里的具体处理区块，减少管理者进入后再次查找的步骤。
async function scrollToTodoTargetSection(todoTargetType) {
  currentTodoTargetType.value = todoTargetType || ""
  if (!currentTodoTargetType.value)
  {
    return
  }
  await nextTick()
  const targetSectionRefMap = {
    executive_action: executiveActionSectionRef,
    budget_approve: annualBudgetSectionRef
  }
  const targetSectionRef = targetSectionRefMap[currentTodoTargetType.value]
  targetSectionRef?.value?.$el?.scrollIntoView({
    behavior: "smooth",
    block: "start"
  })
}

// 初始化待办来源定位，兼容同一看板页面内重复从待办中心跳转的情况。
function initializeTodoTargetFromRoute() {
  scrollToTodoTargetSection(route.query.todoType)
}

onMounted(() => {
  window.addEventListener("resize", handleChartResize)
  getData()
  loadDashboardTargetConfig()
  loadDecisionBudgetConfig()
  loadAnnualBudgetPlanData()
  loadExecutiveBriefRecordList()
  loadOperationSnapshotList()
  loadExecutiveActionItemList()
  getReconciliationData()
  getInvoiceTaxData()
  getCashFlowForecastData()
  initializeTodoTargetFromRoute()
})

watch(() => route.query.todoType, () => {
  initializeTodoTargetFromRoute()
})

watch(() => settingsStore.isDark, () => {
  nextTick(() => {
    initCharts(dashboardData.value || {})
  })
})

watch(hasManagementCockpitData, (hasData) => {
  if (hasData && route.query.todoType)
  {
    initializeTodoTargetFromRoute()
  }
})

onBeforeUnmount(() => {
  disposeCharts()
})
</script>

<style scoped src="./styles/report.scss"></style>
