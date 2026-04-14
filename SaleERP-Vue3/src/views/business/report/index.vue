<template>
  <div class="app-container">
    <el-row :gutter="12">
      <el-col :span="4">
        <el-card shadow="hover" class="dashboard-action-card" @click="handleDashboardShortcut('saleOrderSummary')">
          <div class="stat-title">销售总额</div>
          <div class="stat-value">￥{{ formatAmountValue(dashboardData.totalSaleAmount) }}</div>
          <div class="stat-action">点击查看销售单</div>
        </el-card>
      </el-col>
      <el-col :span="4">
        <el-card shadow="hover" class="dashboard-action-card" @click="handleDashboardShortcut('receivableSummary')">
          <div class="stat-title">应收总额</div>
          <div class="stat-value">￥{{ formatAmountValue(dashboardData.totalReceivableAmount) }}</div>
          <div class="stat-action">点击查看应收台账</div>
        </el-card>
      </el-col>
      <el-col :span="4">
        <el-card shadow="hover" class="dashboard-action-card" @click="handleDashboardShortcut('receiptSummary')">
          <div class="stat-title">已收总额</div>
          <div class="stat-value">￥{{ formatAmountValue(dashboardData.totalReceivedAmount) }}</div>
          <div class="stat-action">点击查看回款记录</div>
        </el-card>
      </el-col>
      <el-col :span="4">
        <el-card shadow="hover" class="dashboard-action-card" @click="handleDashboardShortcut('overdueReceivable')">
          <div class="stat-title">逾期笔数</div>
          <div class="stat-value">{{ dashboardData.overdueCount || 0 }}</div>
          <div class="stat-action">点击查看逾期应收</div>
        </el-card>
      </el-col>
      <el-col :span="4">
        <el-card shadow="hover" class="dashboard-action-card" @click="handleDashboardShortcut('stockWarning')">
          <div class="stat-title">库存预警</div>
          <div class="stat-value">{{ dashboardData.stockWarningCount || 0 }}</div>
          <div class="stat-action">点击查看预警库存</div>
        </el-card>
      </el-col>
    </el-row>

    <el-row :gutter="12" style="margin-top: 12px" v-if="hasManagementCockpitData">
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
              <el-table-column label="决议事项" align="left" min-width="260" show-overflow-tooltip>
                <template #default="scope">
                  <div class="executive-action-title">{{ scope.row.actionTitle }}</div>
                  <div v-if="scope.row.briefTitleSnapshot" class="executive-action-brief-title">来源：{{ scope.row.briefTitleSnapshot }}</div>
                </template>
              </el-table-column>
              <el-table-column label="负责人" align="center" prop="ownerName" min-width="100" />
              <el-table-column label="到期日" align="center" min-width="120">
                <template #default="scope">
                  <span :class="{ 'executive-action-overdue-text': isExecutiveActionItemOverdue(scope.row) }">
                    {{ scope.row.dueDate ? parseTime(scope.row.dueDate, '{y}-{m}-{d}') : '-' }}
                  </span>
                </template>
              </el-table-column>
              <el-table-column label="优先级" align="center" width="100">
                <template #default="scope">
                  <el-tag size="small" :type="getExecutiveActionPriorityTagType(scope.row.priorityLevel)">
                    {{ getExecutiveActionPriorityLabel(scope.row.priorityLevel) }}
                  </el-tag>
                </template>
              </el-table-column>
              <el-table-column label="状态" align="center" width="110">
                <template #default="scope">
                  <el-tag size="small" :type="getExecutiveActionStatusTagType(scope.row.actionStatus)">
                    {{ getExecutiveActionStatusLabel(scope.row.actionStatus) }}
                  </el-tag>
                </template>
              </el-table-column>
              <el-table-column label="进度备注" align="left" min-width="220" show-overflow-tooltip>
                <template #default="scope">
                  <span>{{ scope.row.progressRemark || '-' }}</span>
                </template>
              </el-table-column>
              <el-table-column label="操作" align="center" width="180">
                <template #default="scope">
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
                </template>
              </el-table-column>
            </el-table>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <el-row :gutter="12" style="margin-top: 12px">
      <el-col :span="8">
        <el-card shadow="hover" class="dashboard-action-card" @click="handleDashboardShortcut('pendingSaleOrder')">
          <div class="stat-title">待审核销售单</div>
          <div class="stat-value">{{ dashboardData.pendingSaleOrderCount || 0 }}</div>
          <div class="stat-action">点击进入待审核列表</div>
        </el-card>
      </el-col>
      <el-col :span="8">
        <el-card shadow="hover" class="dashboard-action-card" @click="handleDashboardShortcut('pendingInbound')">
          <div class="stat-title">待审核入库单</div>
          <div class="stat-value">{{ dashboardData.pendingInboundCount || 0 }}</div>
          <div class="stat-action">点击进入待审核列表</div>
        </el-card>
      </el-col>
      <el-col :span="8">
        <el-card shadow="hover" class="dashboard-action-card" @click="handleDashboardShortcut('pendingOutbound')">
          <div class="stat-title">待审核出库单</div>
          <div class="stat-value">{{ dashboardData.pendingOutboundCount || 0 }}</div>
          <div class="stat-action">点击进入待审核列表</div>
        </el-card>
      </el-col>
    </el-row>

    <el-row :gutter="12" style="margin-top: 12px">
      <el-col :span="24">
        <el-card>
          <template #header>
            <div class="dashboard-section-header">
              <div class="dashboard-section-title-group">
                <span>管理驾驶舱</span>
                <el-tag size="small" :type="managementCockpitModeTagType">{{ managementCockpitModeLabel }}</el-tag>
              </div>
              <span class="dashboard-section-subtitle">聚焦利润、回款和环比变化，帮助管理层快速判断经营状态</span>
            </div>
          </template>
          <el-alert
            v-if="!hasManagementCockpitData && !managementCockpitLoading"
            title="当前环境尚未发布管理驾驶舱数据，部署最新后端后即可显示利润、环比与账龄风险。"
            type="warning"
            :closable="false"
            show-icon
          />
          <template v-else>
            <el-alert
              v-if="managementCockpitDataSource === 'fallback'"
              title="当前看板已切换为兼容模式，利润、回款率和账龄风险由前端按现有业务台账实时汇总。"
              type="info"
              :closable="false"
              show-icon
              style="margin-bottom: 12px"
            />
            <el-row :gutter="12">
              <el-col :span="6" v-for="managementOverviewCard in managementOverviewCardList" :key="managementOverviewCard.key">
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

    <el-row :gutter="12" style="margin-top: 12px" v-if="hasManagementCockpitData">
      <el-col :span="8">
        <el-card>
          <template #header>风险雷达</template>
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
          <template #header>应收账龄</template>
          <el-table :data="receivableAgingList" border size="small">
            <el-table-column label="账龄区间" align="center" prop="bucketName" min-width="110" />
            <el-table-column label="金额" align="right" min-width="120">
              <template #default="scope">
                <span>￥{{ formatAmountValue(scope.row.amount) }}</span>
              </template>
            </el-table-column>
            <el-table-column label="笔数" align="center" prop="recordCount" width="70" />
          </el-table>
        </el-card>
      </el-col>
      <el-col :span="8">
        <el-card>
          <template #header>应付账龄</template>
          <el-table :data="payableAgingList" border size="small">
            <el-table-column label="账龄区间" align="center" prop="bucketName" min-width="110" />
            <el-table-column label="金额" align="right" min-width="120">
              <template #default="scope">
                <span>￥{{ formatAmountValue(scope.row.amount) }}</span>
              </template>
            </el-table-column>
            <el-table-column label="笔数" align="center" prop="recordCount" width="70" />
          </el-table>
        </el-card>
      </el-col>
    </el-row>

    <el-row :gutter="12" style="margin-top: 12px" v-if="hasManagementCockpitData">
      <el-col :span="12">
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
      <el-col :span="12">
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
      <el-col :span="12">
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
      <el-col :span="12">
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
      <el-col :span="12">
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
      <el-col :span="12">
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
              <el-form :model="cashScenarioForm" label-width="120px" class="cash-scenario-form">
                <el-row :gutter="12">
                  <el-col :span="12">
                    <el-form-item label="延迟回款金额">
                      <el-input-number v-model="cashScenarioForm.delayedCollectionAmount" :min="0" :precision="2" controls-position="right" style="width: 100%" />
                    </el-form-item>
                  </el-col>
                  <el-col :span="12">
                    <el-form-item label="提前付款金额">
                      <el-input-number v-model="cashScenarioForm.acceleratedPaymentAmount" :min="0" :precision="2" controls-position="right" style="width: 100%" />
                    </el-form-item>
                  </el-col>
                  <el-col :span="12">
                    <el-form-item label="新增销售回款">
                      <el-input-number v-model="cashScenarioForm.additionalSaleCollectionAmount" :min="0" :precision="2" controls-position="right" style="width: 100%" />
                    </el-form-item>
                  </el-col>
                  <el-col :span="12">
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
      <el-col :span="12">
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
                <el-table-column label="等级" align="center" prop="customerLevel" width="90" />
                <el-table-column label="销售额" align="right" min-width="110">
                  <template #default="scope">
                    <span>￥{{ formatAmountValue(scope.row.saleAmount) }}</span>
                  </template>
                </el-table-column>
                <el-table-column label="毛利额" align="right" min-width="110">
                  <template #default="scope">
                    <span>￥{{ formatAmountValue(scope.row.grossProfitAmount) }}</span>
                  </template>
                </el-table-column>
                <el-table-column label="毛利率" align="center" min-width="90">
                  <template #default="scope">
                    <span>{{ formatRateValue(scope.row.grossMarginRate) }}</span>
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
      <el-col :span="12">
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
      <el-col :span="12">
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
                    <span>￥{{ formatAmountValue(scope.row.grossProfitAmount) }}</span>
                  </template>
                </el-table-column>
                <el-table-column label="毛利率" align="center" min-width="90">
                  <template #default="scope">
                    <span>{{ formatRateValue(scope.row.grossMarginRate) }}</span>
                  </template>
                </el-table-column>
                <el-table-column label="单据数" align="center" prop="orderCount" width="80" />
              </el-table>
            </template>
          </div>
        </el-card>
      </el-col>
      <el-col :span="12">
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
      <el-col :span="12">
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
                  <span>￥{{ formatAmountValue(scope.row.grossProfitBudgetAmount) }}</span>
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
      <el-col :span="12">
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
                  <span>￥{{ formatAmountValue(scope.row.grossProfitAmount) }}</span>
                </template>
              </el-table-column>
              <el-table-column label="回款率" align="center" min-width="100">
                <template #default="scope">
                  <span>{{ formatPercentValue(scope.row.collectionRate) }}</span>
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
      <el-col :span="12">
        <el-card>
          <template #header>销售趋势</template>
          <div ref="saleTrendRef" style="height: 360px"></div>
        </el-card>
      </el-col>
      <el-col :span="12">
        <el-card>
          <template #header>客户销售Top5</template>
          <div ref="topCustomerRef" style="height: 360px"></div>
        </el-card>
      </el-col>
    </el-row>

    <el-row :gutter="12" style="margin-top: 12px">
      <el-col :span="24">
        <el-card>
          <template #header>
            <div style="display: flex; justify-content: space-between; align-items: center">
              <span>消息中心</span>
              <el-select v-model="selectedMessageType" placeholder="请选择消息类型" clearable style="width: 220px">
                <el-option label="全部消息" value="all" />
                <el-option label="销售审核消息" value="sale_order_audit" />
                <el-option label="库存预警消息" value="stock_warning" />
                <el-option label="系统公告消息" value="notice" />
                <el-option label="应收到期消息" value="receivable" />
              </el-select>
            </div>
          </template>
          <el-table :data="filteredMessageCenterList" border>
            <el-table-column label="消息类型" align="center" prop="message_type" width="140">
              <template #default="scope">
                <span>{{ getMessageTypeLabel(scope.row.message_type) }}</span>
              </template>
            </el-table-column>
            <el-table-column label="消息标题" align="left" prop="message_title" />
            <el-table-column label="消息等级" align="center" prop="messageLevel" width="120">
              <template #default="scope">
                <el-tag :type="scope.row.messageLevel === 'warning' ? 'danger' : 'info'">
                  {{ scope.row.messageLevel === "warning" ? "预警" : "通知" }}
                </el-tag>
              </template>
            </el-table-column>
            <el-table-column label="时间" align="center" prop="message_time" width="200">
              <template #default="scope">
                <span>{{ parseTime(scope.row.message_time) }}</span>
              </template>
            </el-table-column>
            <el-table-column label="操作" align="center" width="140">
              <template #default="scope">
                <el-button link type="primary" @click="handleMessageAction(scope.row)">
                  {{ getMessageActionLabel(scope.row) }}
                </el-button>
              </template>
            </el-table-column>
          </el-table>
        </el-card>
      </el-col>
    </el-row>

    <el-row :gutter="12" style="margin-top: 12px">
      <el-col :span="24">
        <el-card>
          <template #header>对账中心</template>
          <el-form :model="reconciliationQueryParams" :inline="true" label-width="90px">
            <el-form-item label="对象名称">
              <el-input v-model="reconciliationQueryParams.targetName" placeholder="请输入客户或供应商" clearable style="width: 240px" @keyup.enter="getReconciliationData" />
            </el-form-item>
            <el-form-item>
              <el-button type="primary" icon="Search" @click="getReconciliationData">查询</el-button>
              <el-button icon="Refresh" @click="resetReconciliationQuery">重置</el-button>
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
            @pagination="getReconciliationData"
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
              <el-button type="primary" icon="Search" @click="getInvoiceTaxData">查询</el-button>
              <el-button icon="Refresh" @click="resetInvoiceTaxQuery">重置</el-button>
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
            @pagination="getInvoiceTaxData"
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
              <el-button type="primary" icon="Search" @click="getCashFlowForecastData">刷新预测</el-button>
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
        title="经营目标按自然月展示，修改后会同步更新管理驾驶舱的达成进度。"
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
          <el-button type="primary" @click="copyExecutiveBriefText">复制简报</el-button>
        </div>
      </template>
    </el-dialog>

    <el-dialog v-model="annualBudgetPlanDialogOpen" :title="annualBudgetPlanFormMode === 'createVersion' ? '新建预算版本' : '维护年度预算计划'" width="1280px" append-to-body>
      <el-alert
        :title="annualBudgetPlanFormMode === 'createVersion' ? '系统已带入当前生效版本的预算拆解，请补充版本标签和调整原因后继续微调。' : '建议先录入年度总量，再一键均分到月度，最后按旺淡季和回款节奏微调每个月预算。'"
        type="info"
        :closable="false"
        show-icon
      />
      <el-form ref="annualBudgetPlanFormRef" :model="annualBudgetPlanForm" :rules="annualBudgetPlanRules" label-width="110px" style="margin-top: 16px">
        <el-row :gutter="12">
          <el-col :span="8">
            <el-form-item label="预算年度" prop="budgetYear">
              <el-input-number v-model="annualBudgetPlanForm.budgetYear" :min="2020" :max="2099" controls-position="right" style="width: 100%" :disabled="annualBudgetPlanFormMode === 'createVersion' || !!annualBudgetPlanForm.planId" />
            </el-form-item>
          </el-col>
          <el-col :span="8">
            <el-form-item label="预算名称" prop="planName">
              <el-input v-model="annualBudgetPlanForm.planName" placeholder="请输入预算计划名称" />
            </el-form-item>
          </el-col>
          <el-col :span="8">
            <el-form-item label="版本标签">
              <el-input v-model="annualBudgetPlanForm.versionLabel" placeholder="请输入版本标签，例如 v2" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-form-item label="调整原因">
          <el-input v-model="annualBudgetPlanForm.adjustmentReason" type="textarea" :rows="2" placeholder="请输入本次预算调整的背景、目标或主要变化" />
        </el-form-item>
        <div class="annual-budget-form-action-row">
          <span class="annual-budget-form-action-text">先填写年度总量，再点击“按年度均分”，最后可逐月微调。</span>
          <el-button type="primary" plain @click="distributeAllAnnualBudgetPlanMetrics">按年度均分</el-button>
        </div>
        <el-row :gutter="12">
          <el-col :span="8">
            <el-form-item label="年度销售预算">
              <el-input-number v-model="annualBudgetPlanForm.saleBudgetAmount" :min="0" :precision="2" controls-position="right" style="width: 100%" />
            </el-form-item>
          </el-col>
          <el-col :span="8">
            <el-form-item label="年度毛利预算">
              <el-input-number v-model="annualBudgetPlanForm.grossProfitBudgetAmount" :min="0" :precision="2" controls-position="right" style="width: 100%" />
            </el-form-item>
          </el-col>
          <el-col :span="8">
            <el-form-item label="年度回款预算">
              <el-input-number v-model="annualBudgetPlanForm.collectionBudgetAmount" :min="0" :precision="2" controls-position="right" style="width: 100%" />
            </el-form-item>
          </el-col>
          <el-col :span="8">
            <el-form-item label="年度采购预算">
              <el-input-number v-model="annualBudgetPlanForm.purchaseBudgetAmount" :min="0" :precision="2" controls-position="right" style="width: 100%" />
            </el-form-item>
          </el-col>
          <el-col :span="8">
            <el-form-item label="年度净现金流">
              <el-input-number v-model="annualBudgetPlanForm.netCashBudgetAmount" :precision="2" controls-position="right" style="width: 100%" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-table :data="annualBudgetMonthLabelList.map((monthLabel, monthIndexValue) => ({ monthLabel, monthIndexValue }))" border size="small">
          <el-table-column label="月份" align="center" width="80">
            <template #default="scope">
              <span>{{ scope.row.monthLabel }}</span>
            </template>
          </el-table-column>
          <el-table-column label="销售预算" min-width="140">
            <template #default="scope">
              <el-input-number v-model="annualBudgetPlanForm.saleMonthlyPlanList[scope.row.monthIndexValue]" :min="0" :precision="2" controls-position="right" style="width: 100%" @change="syncAnnualBudgetPlanFormTotalAmount('saleBudgetAmount')" />
            </template>
          </el-table-column>
          <el-table-column label="毛利预算" min-width="140">
            <template #default="scope">
              <el-input-number v-model="annualBudgetPlanForm.grossProfitMonthlyPlanList[scope.row.monthIndexValue]" :min="0" :precision="2" controls-position="right" style="width: 100%" @change="syncAnnualBudgetPlanFormTotalAmount('grossProfitBudgetAmount')" />
            </template>
          </el-table-column>
          <el-table-column label="回款预算" min-width="140">
            <template #default="scope">
              <el-input-number v-model="annualBudgetPlanForm.collectionMonthlyPlanList[scope.row.monthIndexValue]" :min="0" :precision="2" controls-position="right" style="width: 100%" @change="syncAnnualBudgetPlanFormTotalAmount('collectionBudgetAmount')" />
            </template>
          </el-table-column>
          <el-table-column label="采购预算" min-width="140">
            <template #default="scope">
              <el-input-number v-model="annualBudgetPlanForm.purchaseMonthlyPlanList[scope.row.monthIndexValue]" :min="0" :precision="2" controls-position="right" style="width: 100%" @change="syncAnnualBudgetPlanFormTotalAmount('purchaseBudgetAmount')" />
            </template>
          </el-table-column>
          <el-table-column label="净现金流" min-width="140">
            <template #default="scope">
              <el-input-number v-model="annualBudgetPlanForm.netCashMonthlyPlanList[scope.row.monthIndexValue]" :precision="2" controls-position="right" style="width: 100%" @change="syncAnnualBudgetPlanFormTotalAmount('netCashBudgetAmount')" />
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
          <div class="annual-budget-summary-value">{{ formatPercentValue(operationSnapshotPreviewData.collectionRate) }}</div>
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

<script setup name="BusinessReport">
import * as echarts from 'echarts'
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
import {
  getDecisionBudgetPlanCurrent,
  listDecisionBudgetPlanHistory,
  addDecisionBudgetPlan,
  updateDecisionBudgetPlan,
  createDecisionBudgetPlanVersion,
  submitDecisionBudgetPlan,
  approveDecisionBudgetPlan,
  rejectDecisionBudgetPlan
} from '@/api/business/decisionBudgetPlan'
import {
  listExecutiveBriefRecord,
  getExecutiveBriefRecord,
  addExecutiveBriefRecord
} from '@/api/business/executiveBriefRecord'
import {
  listOperationSnapshot,
  getOperationSnapshot,
  addOperationSnapshot
} from '@/api/business/operationSnapshot'
import {
  listExecutiveActionItem,
  getExecutiveActionItem,
  addExecutiveActionItem,
  updateExecutiveActionItem,
  delExecutiveActionItem
} from '@/api/business/executiveActionItem'
import { getConfigKey, listConfig, addConfig, updateConfig } from '@/api/system/config'
import useUserStore from '@/store/modules/user'
import { parseTime } from '@/utils/ruoyi'
import { buildDashboardMessagePreviewContent } from '@/utils/dashboardMessage'

const { proxy } = getCurrentInstance()
const userStore = useUserStore()
const dashboardData = ref({})
const saleTrendRef = ref(null)
const topCustomerRef = ref(null)
const saleTrendChartInstance = ref(null)
const topCustomerChartInstance = ref(null)
const selectedMessageType = ref("all")
const router = useRouter()
const messagePreviewOpen = ref(false)
const messagePreviewData = ref({})
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
const dashboardTargetLoading = ref(false)
const dashboardTargetDialogOpen = ref(false)
const dashboardTargetFormRef = ref(null)
const dashboardTargetConfigDetailMap = ref({})
const decisionSupportLoading = ref(false)
const decisionBudgetLoading = ref(false)
const decisionBudgetDialogOpen = ref(false)
const decisionBudgetFormRef = ref(null)
const decisionBudgetConfigDetailMap = ref({})
const organizationContributionTab = ref("category")
const executiveBriefDialogOpen = ref(false)
const annualBudgetYear = ref(new Date().getFullYear())
const annualBudgetPlanLoading = ref(false)
const annualBudgetPlanDialogOpen = ref(false)
const annualBudgetPlanFormRef = ref(null)
const annualBudgetApprovalDialogOpen = ref(false)
const annualBudgetApprovalActionType = ref("approve")
const annualBudgetPlanFormMode = ref("edit")
const annualBudgetPlanSourceId = ref(undefined)
const annualBudgetPlanData = ref({})
const annualBudgetPlanHistoryList = ref([])
const executiveBriefRecordLoading = ref(false)
const executiveBriefRecordList = ref([])
const operationSnapshotLoading = ref(false)
const operationSnapshotList = ref([])
const operationSnapshotDialogOpen = ref(false)
const operationSnapshotPreviewData = ref({})
const executiveActionItemLoading = ref(false)
const executiveActionItemList = ref([])
const executiveActionItemDialogOpen = ref(false)
const executiveActionItemFormRef = ref(null)
const executiveActionItemDialogTitle = ref("新增经营决议事项")
const executiveBriefRecordDialogOpen = ref(false)
const executiveBriefRecordPreviewData = ref({})
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
const dashboardTargetForm = reactive({
  saleTargetAmount: undefined,
  grossProfitTargetAmount: undefined,
  collectionRateTarget: undefined
})
const decisionBudgetForm = reactive({
  saleBudgetAmount: undefined,
  grossProfitBudgetAmount: undefined,
  collectionBudgetAmount: undefined,
  purchaseBudgetAmount: undefined,
  netCashBudgetAmount: undefined
})
const cashScenarioForm = reactive({
  delayedCollectionAmount: 0,
  acceleratedPaymentAmount: 0,
  additionalSaleCollectionAmount: 0,
  additionalProcurementAmount: 0,
  temporaryFinancingAmount: 0
})
const annualBudgetPlanForm = reactive({
  planId: undefined,
  budgetYear: new Date().getFullYear(),
  planName: "",
  versionLabel: "v1",
  adjustmentReason: "",
  effectiveFlag: "n",
  saleBudgetAmount: 0,
  grossProfitBudgetAmount: 0,
  collectionBudgetAmount: 0,
  purchaseBudgetAmount: 0,
  netCashBudgetAmount: 0,
  saleMonthlyPlanList: Array.from({ length: 12 }, () => 0),
  grossProfitMonthlyPlanList: Array.from({ length: 12 }, () => 0),
  collectionMonthlyPlanList: Array.from({ length: 12 }, () => 0),
  purchaseMonthlyPlanList: Array.from({ length: 12 }, () => 0),
  netCashMonthlyPlanList: Array.from({ length: 12 }, () => 0)
})
const annualBudgetApprovalForm = reactive({
  approveRemark: ""
})
const executiveActionItemForm = reactive({
  actionItemId: undefined,
  briefId: undefined,
  briefTitleSnapshot: "",
  actionTitle: "",
  ownerName: "",
  dueDate: undefined,
  actionStatus: "todo",
  priorityLevel: "medium",
  progressRemark: "",
  remark: ""
})
const dashboardTargetRules = {
  saleTargetAmount: [{ required: true, message: "销售目标不能为空", trigger: "blur" }],
  grossProfitTargetAmount: [{ required: true, message: "毛利目标不能为空", trigger: "blur" }],
  collectionRateTarget: [{ required: true, message: "回款率目标不能为空", trigger: "blur" }]
}
const decisionBudgetRules = {
  saleBudgetAmount: [{ required: true, message: "销售预算不能为空", trigger: "blur" }],
  grossProfitBudgetAmount: [{ required: true, message: "毛利预算不能为空", trigger: "blur" }],
  collectionBudgetAmount: [{ required: true, message: "回款预算不能为空", trigger: "blur" }],
  purchaseBudgetAmount: [{ required: true, message: "采购预算不能为空", trigger: "blur" }],
  netCashBudgetAmount: [{ required: true, message: "净现金流预算不能为空", trigger: "blur" }]
}
const annualBudgetPlanRules = {
  budgetYear: [{ required: true, message: "预算年度不能为空", trigger: "blur" }],
  planName: [{ required: true, message: "预算计划名称不能为空", trigger: "blur" }]
}
const executiveActionItemRules = {
  actionTitle: [{ required: true, message: "决议事项不能为空", trigger: "blur" }]
}

// 统一经营看板的页面跳转地址，避免继续进入旧的 business 路由。
const businessRoutePathMap = {
  saleOrder: "/sales/saleOrder",
  stock: "/inventory/stock",
  receivable: "/finance/receivable",
  receipt: "/finance/receipt",
  payable: "/finance/payable",
  customerFollow: "/base/customerFollow",
  purchaseOrder: "/purchase/purchaseOrder",
  inbound: "/purchase/inbound",
  outbound: "/sales/outbound"
}

// 定义经营目标配置项，复用系统参数配置能力承载管理层月度目标。
const dashboardTargetMetaList = [
  {
    key: "saleTargetAmount",
    title: "销售目标",
    configKey: "biz.report.dashboard.saleTargetAmount",
    configName: "经营看板销售目标",
    remark: "用于经营看板展示月度销售目标（元）",
    unitType: "amount",
    currentValueField: "currentMonthSaleAmount"
  },
  {
    key: "grossProfitTargetAmount",
    title: "毛利目标",
    configKey: "biz.report.dashboard.grossProfitTargetAmount",
    configName: "经营看板毛利目标",
    remark: "用于经营看板展示月度毛利目标（元）",
    unitType: "amount",
    currentValueField: "currentMonthGrossProfitAmount"
  },
  {
    key: "collectionRateTarget",
    title: "回款率目标",
    configKey: "biz.report.dashboard.collectionRateTarget",
    configName: "经营看板回款率目标",
    remark: "用于经营看板展示月度回款率目标（百分比）",
    unitType: "rate",
    currentValueField: "currentMonthCollectionRate"
  }
]

// 定义管理层预算配置项，复用系统参数能力承载预算-实际-预测模块。
const decisionBudgetMetaList = [
  {
    key: "saleBudgetAmount",
    title: "销售预算",
    configKey: "biz.report.decision.saleBudgetAmount",
    configName: "管理驾驶舱销售预算",
    remark: "用于管理驾驶舱展示本月销售预算（元）",
    unitType: "amount",
    actualValueField: "currentMonthSaleAmount"
  },
  {
    key: "grossProfitBudgetAmount",
    title: "毛利预算",
    configKey: "biz.report.decision.grossProfitBudgetAmount",
    configName: "管理驾驶舱毛利预算",
    remark: "用于管理驾驶舱展示本月毛利预算（元）",
    unitType: "amount",
    actualValueField: "currentMonthGrossProfitAmount"
  },
  {
    key: "collectionBudgetAmount",
    title: "回款预算",
    configKey: "biz.report.decision.collectionBudgetAmount",
    configName: "管理驾驶舱回款预算",
    remark: "用于管理驾驶舱展示本月回款预算（元）",
    unitType: "amount",
    actualValueField: "currentMonthCollectionAmount"
  },
  {
    key: "purchaseBudgetAmount",
    title: "采购预算",
    configKey: "biz.report.decision.purchaseBudgetAmount",
    configName: "管理驾驶舱采购预算",
    remark: "用于管理驾驶舱展示本月采购预算（元）",
    unitType: "amount",
    actualValueField: "currentMonthPurchaseAmount"
  },
  {
    key: "netCashBudgetAmount",
    title: "净现金流预算",
    configKey: "biz.report.decision.netCashBudgetAmount",
    configName: "管理驾驶舱净现金流预算",
    remark: "用于管理驾驶舱展示本月净现金流预算（元）",
    unitType: "amount",
    actualValueField: "currentMonthNetCashFlowAmount"
  }
]
const annualBudgetMonthLabelList = ["1月", "2月", "3月", "4月", "5月", "6月", "7月", "8月", "9月", "10月", "11月", "12月"]

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

// 判断当前用户是否具备经营目标维护权限，避免无权限用户误触参数配置能力。
const canManageDashboardTarget = computed(() => {
  const permissionCodeList = userStore.permissions || []
  if (permissionCodeList.includes("*:*:*"))
  {
    return true
  }
  const hasConfigListPermission = permissionCodeList.includes("system:config:list")
  const hasConfigAddPermission = permissionCodeList.includes("system:config:add")
  const hasConfigEditPermission = permissionCodeList.includes("system:config:edit")
  return hasConfigListPermission && (hasConfigAddPermission || hasConfigEditPermission)
})

// 判断当前用户是否具备二期年度预算维护权限。
const canManageAnnualBudgetPlan = computed(() => {
  const permissionCodeList = userStore.permissions || []
  if (permissionCodeList.includes("*:*:*"))
  {
    return true
  }
  return canManageDashboardTarget.value || permissionCodeList.includes("business:report:edit")
})

// 判断当前用户是否具备二期年度预算审批权限。
const canApproveAnnualBudgetPlan = computed(() => {
  const permissionCodeList = userStore.permissions || []
  if (permissionCodeList.includes("*:*:*"))
  {
    return true
  }
  return permissionCodeList.includes("business:report:approve")
})

// 判断当前预算计划是否允许基于已通过版本继续新建调整版本。
const canCreateAnnualBudgetPlanVersion = computed(() => {
  return canManageAnnualBudgetPlan.value
    && !!annualBudgetPlanData.value.planId
    && annualBudgetPlanData.value.planStatus === "approved"
})

// 判断当前用户是否具备经营简报归档权限。
const canArchiveExecutiveBriefRecord = computed(() => {
  const permissionCodeList = userStore.permissions || []
  if (permissionCodeList.includes("*:*:*"))
  {
    return true
  }
  return permissionCodeList.includes("business:report:brief")
})

// 判断当前用户是否具备经营决议事项维护权限。
const canManageExecutiveActionItem = computed(() => {
  const permissionCodeList = userStore.permissions || []
  if (permissionCodeList.includes("*:*:*"))
  {
    return true
  }
  return permissionCodeList.includes("business:report:edit")
})

// 统计当前仍待推进的经营决议事项数量，帮助管理层快速判断闭环压力。
const pendingExecutiveActionItemCount = computed(() => {
  return executiveActionItemList.value.filter(executiveActionItem => {
    return executiveActionItem.actionStatus !== "completed" && executiveActionItem.actionStatus !== "canceled"
  }).length
})

// 统一读取当前预算版本生效文字，帮助管理层区分“当前在编版本”和“正式生效版本”。
const annualBudgetPlanEffectiveText = computed(() => {
  return annualBudgetPlanData.value.effectiveFlag === "y" ? "当前生效版本" : "未生效版本"
})

// 统一读取当前预算版本生效标签样式。
const annualBudgetPlanEffectiveTagType = computed(() => {
  return annualBudgetPlanData.value.effectiveFlag === "y" ? "success" : "info"
})

// 统一读取年度预算计划状态文字。
const annualBudgetPlanStatusText = computed(() => {
  const annualBudgetPlanStatus = annualBudgetPlanData.value.planStatus
  if (annualBudgetPlanStatus === "submitted")
  {
    return "待审批"
  }
  if (annualBudgetPlanStatus === "approved")
  {
    return "已通过"
  }
  if (annualBudgetPlanStatus === "rejected")
  {
    return "已驳回"
  }
  return "草稿"
})

// 统一读取年度预算计划状态标签类型。
const annualBudgetPlanStatusTagType = computed(() => {
  const annualBudgetPlanStatus = annualBudgetPlanData.value.planStatus
  if (annualBudgetPlanStatus === "submitted")
  {
    return "warning"
  }
  if (annualBudgetPlanStatus === "approved")
  {
    return "success"
  }
  if (annualBudgetPlanStatus === "rejected")
  {
    return "danger"
  }
  return "info"
})

// 组装年度预算概览卡片，帮助管理层快速查看年度总量和当前月拆解。
const annualBudgetPlanSummaryCardList = computed(() => {
  const currentMonthIndexValue = new Date().getMonth()
  return [
    {
      key: "saleBudgetAmount",
      title: "年度销售预算",
      amount: annualBudgetPlanData.value.saleBudgetAmount,
      monthAmount: annualBudgetPlanData.value.saleMonthlyPlanList?.[currentMonthIndexValue]
    },
    {
      key: "grossProfitBudgetAmount",
      title: "年度毛利预算",
      amount: annualBudgetPlanData.value.grossProfitBudgetAmount,
      monthAmount: annualBudgetPlanData.value.grossProfitMonthlyPlanList?.[currentMonthIndexValue]
    },
    {
      key: "collectionBudgetAmount",
      title: "年度回款预算",
      amount: annualBudgetPlanData.value.collectionBudgetAmount,
      monthAmount: annualBudgetPlanData.value.collectionMonthlyPlanList?.[currentMonthIndexValue]
    },
    {
      key: "purchaseBudgetAmount",
      title: "年度采购预算",
      amount: annualBudgetPlanData.value.purchaseBudgetAmount,
      monthAmount: annualBudgetPlanData.value.purchaseMonthlyPlanList?.[currentMonthIndexValue]
    },
    {
      key: "netCashBudgetAmount",
      title: "年度净现金流预算",
      amount: annualBudgetPlanData.value.netCashBudgetAmount,
      monthAmount: annualBudgetPlanData.value.netCashMonthlyPlanList?.[currentMonthIndexValue]
    }
  ]
})

// 组装年度预算月度拆解表格，帮助管理层查看每个月的预算拆分。
const annualBudgetMonthlyTableList = computed(() => {
  const monthlyTableRowList = []
  for (let monthIndexValue = 0; monthIndexValue < annualBudgetMonthLabelList.length; monthIndexValue++)
  {
    monthlyTableRowList.push({
      monthLabel: annualBudgetMonthLabelList[monthIndexValue],
      saleBudgetAmount: annualBudgetPlanData.value.saleMonthlyPlanList?.[monthIndexValue] || 0,
      grossProfitBudgetAmount: annualBudgetPlanData.value.grossProfitMonthlyPlanList?.[monthIndexValue] || 0,
      collectionBudgetAmount: annualBudgetPlanData.value.collectionMonthlyPlanList?.[monthIndexValue] || 0,
      purchaseBudgetAmount: annualBudgetPlanData.value.purchaseMonthlyPlanList?.[monthIndexValue] || 0,
      netCashBudgetAmount: annualBudgetPlanData.value.netCashMonthlyPlanList?.[monthIndexValue] || 0
    })
  }
  return monthlyTableRowList
})

// 判断当前是否已经配置经营目标，便于看板显示空状态或达成进度。
const hasDashboardTargetConfigured = computed(() => {
  return dashboardTargetMetaList.some(dashboardTargetMeta => {
    const configDetail = getDashboardTargetConfigDetail(dashboardTargetMeta.key)
    return configDetail.configValue !== undefined && configDetail.configValue !== null && configDetail.configValue !== ""
  })
})

// 计算本月时间进度，便于经营目标区判断当前节奏是否领先或落后。
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

// 组装经营目标卡片，帮助管理层快速查看当前值、目标值和达成进度。
const dashboardTargetCardList = computed(() => {
  return dashboardTargetMetaList.map(dashboardTargetMeta => {
    const currentValue = normalizeNumberValue(managementOverviewData.value[dashboardTargetMeta.currentValueField])
    const targetValue = getDashboardTargetValue(dashboardTargetMeta.key)
    const targetValueConfigured = targetValue !== undefined
    const paceGapValue = calculateDashboardTargetPaceGapValue(
      dashboardTargetMeta.key,
      currentValue,
      targetValue,
      currentMonthProgressData.value.monthProgressRate
    )
    const actualProgressPercentage = targetValueConfigured && normalizeNumberValue(targetValue) > 0
      ? currentValue / normalizeNumberValue(targetValue) * 100
      : 0
    const progressPercentage = Number(Math.min(100, Math.max(0, actualProgressPercentage)).toFixed(2))
    const progressStatus = getDashboardTargetProgressStatus(currentValue, targetValue)
    const tagType = progressStatus === "exception" ? "danger" : progressStatus
    return {
      key: dashboardTargetMeta.key,
      title: dashboardTargetMeta.title,
      currentValueText: formatDashboardTargetDisplayValue(dashboardTargetMeta.key, currentValue),
      targetValueText: targetValueConfigured ? formatDashboardTargetDisplayValue(dashboardTargetMeta.key, targetValue) : "未设置",
      progressText: targetValueConfigured ? `达成率 ${actualProgressPercentage.toFixed(2)}%` : "未配置",
      progressPercentage: progressPercentage,
      progressStatus: progressStatus,
      tagType: tagType,
      paceGapValue: paceGapValue,
      paceTagType: getDashboardTargetPaceTagType(dashboardTargetMeta.key, paceGapValue),
      paceTagText: buildDashboardTargetPaceTagText(dashboardTargetMeta.key, paceGapValue),
      differenceText: buildDashboardTargetDifferenceText(dashboardTargetMeta.key, currentValue, targetValue),
      paceText: buildDashboardTargetPaceText(dashboardTargetMeta.key, currentValue, targetValue, currentMonthProgressData.value.monthProgressRate),
      forecastText: buildDashboardTargetForecastText(dashboardTargetMeta.key, currentValue, targetValue, currentMonthProgressData.value.monthProgressRate)
    }
  })
})

// 判断当前是否已经配置管理层预算，便于预算模块展示空状态或执行进度。
const hasDecisionBudgetConfigured = computed(() => {
  return decisionBudgetMetaList.some(decisionBudgetMeta => {
    const configDetail = getDecisionBudgetConfigDetail(decisionBudgetMeta.key)
    return configDetail.configValue !== undefined && configDetail.configValue !== null && configDetail.configValue !== ""
  })
})

// 组装预算执行卡片，帮助管理层同时查看预算、实际、预测和偏差。
const decisionBudgetCardList = computed(() => {
  return decisionBudgetMetaList.map(decisionBudgetMeta => {
    const actualValue = normalizeNumberValue(decisionBudgetOverviewData.value[decisionBudgetMeta.actualValueField])
    const budgetValue = getDecisionBudgetValue(decisionBudgetMeta.key)
    const budgetValueConfigured = budgetValue !== undefined
    const forecastValue = calculateDecisionBudgetForecastValue(actualValue, decisionBudgetMeta.unitType, currentMonthProgressData.value.monthProgressRate)
    const paceGapValue = calculateDecisionBudgetPaceGapValue(actualValue, budgetValue, currentMonthProgressData.value.monthProgressRate)
    return {
      key: decisionBudgetMeta.key,
      title: decisionBudgetMeta.title,
      actualValueText: formatDecisionBudgetDisplayValue(decisionBudgetMeta.key, actualValue),
      budgetValueText: budgetValueConfigured ? formatDecisionBudgetDisplayValue(decisionBudgetMeta.key, budgetValue) : "未设置",
      forecastValueText: formatDecisionBudgetDisplayValue(decisionBudgetMeta.key, forecastValue),
      completionRateText: budgetValueConfigured ? `完成率 ${calculateRateValue(actualValue, budgetValue).toFixed(2)}%` : "未配置",
      forecastRateText: budgetValueConfigured ? `预测达成 ${calculateRateValue(forecastValue, budgetValue).toFixed(2)}%` : "待配置",
      tagType: getDecisionBudgetPaceTagType(paceGapValue, decisionBudgetMeta.unitType),
      paceTagText: buildDecisionBudgetPaceTagText(paceGapValue, decisionBudgetMeta.unitType),
      differenceText: buildDecisionBudgetDifferenceText(decisionBudgetMeta.key, actualValue, budgetValue),
      forecastText: buildDecisionBudgetForecastText(decisionBudgetMeta.key, forecastValue, budgetValue),
      progressPercentage: budgetValueConfigured ? Number(Math.min(100, Math.max(0, calculateRateValue(actualValue, budgetValue))).toFixed(2)) : 0,
      paceGapValue: paceGapValue
    }
  })
})

// 组装预算执行摘要，帮助管理层快速判断预算偏差最大的项目。
const decisionBudgetInsightList = computed(() => {
  if (!hasDecisionBudgetConfigured.value)
  {
    return ["暂未配置本月经营预算，建议补齐销售、毛利、回款、采购和净现金流预算。"]
  }
  const laggingDecisionBudgetCard = [...decisionBudgetCardList.value]
    .filter(decisionBudgetCard => decisionBudgetCard.budgetValueText !== "未设置")
    .sort((leftDecisionBudgetCard, rightDecisionBudgetCard) => leftDecisionBudgetCard.paceGapValue - rightDecisionBudgetCard.paceGapValue)[0]
  const negativeNetCashForecastText = normalizeNumberValue(decisionBudgetOverviewData.value.currentMonthNetCashFlowAmount) < 0
    ? `当前本月净现金流为 ￥${formatAmountValue(decisionBudgetOverviewData.value.currentMonthNetCashFlowAmount)}，建议同步关注回款和付款节奏。`
    : `当前本月净现金流为 ￥${formatAmountValue(decisionBudgetOverviewData.value.currentMonthNetCashFlowAmount)}，资金压力仍处于可控范围。`
  if (!laggingDecisionBudgetCard)
  {
    return [negativeNetCashForecastText]
  }
  return [
    `本月时间进度已达 ${currentMonthProgressData.value.monthProgressPercentage.toFixed(2)}%，${laggingDecisionBudgetCard.title}${laggingDecisionBudgetCard.differenceText}。`,
    laggingDecisionBudgetCard.forecastText,
    negativeNetCashForecastText
  ]
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

// 组装管理层经营简报分段，方便页面展示和复制周报摘要。
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
      value: `￥${formatAmountValue(managementOverviewData.value.currentMonthGrossProfitAmount)}`,
      tagText: buildGrowthTagText(managementOverviewData.value.grossProfitGrowthRate),
      tagType: getGrowthTagType(managementOverviewData.value.grossProfitGrowthRate),
      description: `毛利率：${formatRateValue(managementOverviewData.value.currentMonthGrossProfitRate)}`
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
      actionType: primaryPendingShortcutType || "stockWarning",
      actionLabel: primaryPendingShortcutType ? "查看待审核单据" : "查看预警库存"
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
  const grossProfitRateText = formatRateValue(managementOverviewData.value.currentMonthGrossProfitRate)
  const overdueReceivableAmountText = formatAmountValue(riskOverviewData.value.overdueReceivableAmount)
  const dueSoonPayableAmountText = formatAmountValue(riskOverviewData.value.dueSoonPayableAmount)
  const pendingApprovalTimeoutCount = normalizeCountValue(riskOverviewData.value.pendingApprovalTimeoutCount)
  const managementInsightTextList = [
    `本月销售额${saleGrowthText}，当前毛利率为 ${grossProfitRateText}。`,
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
    ? (primaryPendingShortcutType || "stockWarning")
    : "stockWarning"

  return [
    {
      key: "growthQuality",
      title: "增长质量",
      score: normalizeScoreValue(growthQualityScoreValue),
      description: `销售${buildGrowthSummaryText(saleGrowthRateValue)}，毛利率 ${formatRateValue(grossProfitRateValue)}。`,
      adviceText: saleGrowthRateValue < 0 || grossProfitRateValue < 10
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
      adviceText: "建议先检查经营驾驶舱数据是否已生成，再判断重点动作。",
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
  const primaryPendingShortcutType = getPrimaryPendingShortcutType()
  if (pendingApprovalTimeoutCount > 0 && primaryPendingShortcutType)
  {
    managementTodoItemList.push({
      key: "pendingApprovalTimeout",
      title: "超3天未审核单据待处理",
      levelType: "warning",
      levelText: "流程堵点",
      metricText: `${pendingApprovalTimeoutCount}笔`,
      description: "流程审批存在积压，建议优先清理当前最集中的待审核单据，避免影响出入库与回款节奏。",
      actionType: primaryPendingShortcutType,
      actionLabel: "查看待审核单据"
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

// 统一把金额值转换成前端可展示的数字。
function normalizeNumberValue(numberValue) {
  if (numberValue === undefined || numberValue === null || numberValue === "")
  {
    return 0
  }
  const parsedNumberValue = Number(numberValue)
  if (Number.isNaN(parsedNumberValue))
  {
    return 0
  }
  return parsedNumberValue
}

// 统一把数量值转换成前端可展示的整数。
function normalizeCountValue(countValue) {
  return Math.trunc(normalizeNumberValue(countValue))
}

// 统一格式化金额，保证管理层看到的数据带千分位且保留两位小数。
function formatAmountValue(amountValue) {
  return normalizeNumberValue(amountValue).toLocaleString("zh-CN", {
    minimumFractionDigits: 2,
    maximumFractionDigits: 2
  })
}

// 统一格式化百分比指标。
function formatRateValue(rateValue) {
  return `${normalizeNumberValue(rateValue).toFixed(2)}%`
}

// 统一约束健康评分范围，避免计算过程中出现超过 100 分或低于 0 分的异常值。
function normalizeScoreValue(scoreValue) {
  return Math.max(0, Math.min(100, Math.round(normalizeNumberValue(scoreValue))))
}

// 返回经营健康评分的级别文案，帮助管理层一眼识别当前状态。
function getManagementHealthLevelText(scoreValue) {
  const normalizedScoreValue = normalizeScoreValue(scoreValue)
  if (normalizedScoreValue >= 85)
  {
    return "健康"
  }
  if (normalizedScoreValue >= 70)
  {
    return "可控"
  }
  if (normalizedScoreValue >= 55)
  {
    return "预警"
  }
  return "高风险"
}

// 返回经营健康评分的标签颜色，用于区分健康、可控和风险状态。
function getManagementHealthTagType(scoreValue) {
  const normalizedScoreValue = normalizeScoreValue(scoreValue)
  if (normalizedScoreValue >= 85)
  {
    return "success"
  }
  if (normalizedScoreValue >= 70)
  {
    return "info"
  }
  if (normalizedScoreValue >= 55)
  {
    return "warning"
  }
  return "danger"
}

// 分发管理驾驶舱里的统一动作，保证健康度、待办和目标等区域跳转口径一致。
function handleManagementAction(actionType) {
  if (!actionType)
  {
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

// 统一生成增长率标签文案。
function buildGrowthTagText(growthRateValue) {
  if (growthRateValue === undefined || growthRateValue === null)
  {
    return "上期无数据"
  }
  const normalizedGrowthRateValue = normalizeNumberValue(growthRateValue)
  if (normalizedGrowthRateValue > 0)
  {
    return `环比 +${normalizedGrowthRateValue.toFixed(2)}%`
  }
  if (normalizedGrowthRateValue < 0)
  {
    return `环比 ${normalizedGrowthRateValue.toFixed(2)}%`
  }
  return "环比持平"
}

// 统一生成增长率摘要文案。
function buildGrowthSummaryText(growthRateValue) {
  if (growthRateValue === undefined || growthRateValue === null)
  {
    return "为新增口径"
  }
  const normalizedGrowthRateValue = normalizeNumberValue(growthRateValue)
  if (normalizedGrowthRateValue > 0)
  {
    return `环比增长 ${normalizedGrowthRateValue.toFixed(2)}%`
  }
  if (normalizedGrowthRateValue < 0)
  {
    return `环比下降 ${Math.abs(normalizedGrowthRateValue).toFixed(2)}%`
  }
  return "环比持平"
}

// 统一生成销售额摘要文案，避免当前月无销售时误显示为新增口径。
function buildSaleAmountSummaryText(currentMonthSaleAmountValue, previousMonthSaleAmountValue, growthRateValue) {
  const normalizedCurrentMonthSaleAmountValue = normalizeNumberValue(currentMonthSaleAmountValue)
  const normalizedPreviousMonthSaleAmountValue = normalizeNumberValue(previousMonthSaleAmountValue)
  if (normalizedCurrentMonthSaleAmountValue <= 0 && normalizedPreviousMonthSaleAmountValue <= 0)
  {
    return "本月暂无销售数据"
  }
  return `本月销售额${buildGrowthSummaryText(growthRateValue)}`
}

// 根据增长率返回标签颜色，帮助管理层快速区分增长和下滑。
function getGrowthTagType(growthRateValue) {
  if (growthRateValue === undefined || growthRateValue === null)
  {
    return "info"
  }
  const normalizedGrowthRateValue = normalizeNumberValue(growthRateValue)
  if (normalizedGrowthRateValue > 0)
  {
    return "success"
  }
  if (normalizedGrowthRateValue < 0)
  {
    return "danger"
  }
  return "info"
}

// 统一生成回款率较上月变化的展示文案。
function buildRateDifferenceText(rateDifferenceValue) {
  const normalizedRateDifferenceValue = normalizeNumberValue(rateDifferenceValue)
  if (normalizedRateDifferenceValue > 0)
  {
    return `较上月 +${normalizedRateDifferenceValue.toFixed(2)} 个点`
  }
  if (normalizedRateDifferenceValue < 0)
  {
    return `较上月 ${normalizedRateDifferenceValue.toFixed(2)} 个点`
  }
  return "较上月持平"
}

// 根据回款率变化返回标签颜色。
function getDifferenceTagType(rateDifferenceValue) {
  const normalizedRateDifferenceValue = normalizeNumberValue(rateDifferenceValue)
  if (normalizedRateDifferenceValue > 0)
  {
    return "success"
  }
  if (normalizedRateDifferenceValue < 0)
  {
    return "danger"
  }
  return "info"
}

// 根据经营目标键名返回配置元数据，避免目标维护和展示出现重复分支。
function getDashboardTargetMeta(targetKey) {
  return dashboardTargetMetaList.find(dashboardTargetMeta => dashboardTargetMeta.key === targetKey)
}

// 返回指定经营目标的配置详情，兼容公开读取和带编号的编辑读取两种场景。
function getDashboardTargetConfigDetail(targetKey) {
  return dashboardTargetConfigDetailMap.value[targetKey] || {}
}

// 返回指定经营目标的数值配置，缺失时回退为 undefined 方便区分未配置状态。
function getDashboardTargetValue(targetKey) {
  const configDetail = getDashboardTargetConfigDetail(targetKey)
  if (configDetail.configValue === undefined || configDetail.configValue === null || configDetail.configValue === "")
  {
    return undefined
  }
  return normalizeNumberValue(configDetail.configValue)
}

// 按目标类型格式化经营目标展示值，保证金额和回款率口径统一。
function formatDashboardTargetDisplayValue(targetKey, targetValue) {
  const dashboardTargetMeta = getDashboardTargetMeta(targetKey)
  if (dashboardTargetMeta?.unitType === "rate")
  {
    return `${normalizeNumberValue(targetValue).toFixed(2)}%`
  }
  return `￥${formatAmountValue(targetValue)}`
}

// 返回经营目标差距文案，帮助管理层快速判断当前距离目标还有多远。
function buildDashboardTargetDifferenceText(targetKey, currentValue, targetValue) {
  if (targetValue === undefined)
  {
    return "尚未设置本月目标"
  }
  const dashboardTargetMeta = getDashboardTargetMeta(targetKey)
  const normalizedDifferenceValue = normalizeNumberValue(currentValue) - normalizeNumberValue(targetValue)
  if (normalizedDifferenceValue === 0)
  {
    return "当前已达成本月目标"
  }
  const differencePrefix = normalizedDifferenceValue > 0 ? "已超目标" : "距目标"
  if (dashboardTargetMeta?.unitType === "rate")
  {
    return `${differencePrefix} ${Math.abs(normalizedDifferenceValue).toFixed(2)} 个点`
  }
  return `${differencePrefix} ￥${formatAmountValue(Math.abs(normalizedDifferenceValue))}`
}

// 返回经营目标进度状态，便于进度条区分达成、预警和明显落后。
function getDashboardTargetProgressStatus(currentValue, targetValue) {
  if (targetValue === undefined || normalizeNumberValue(targetValue) <= 0)
  {
    return "warning"
  }
  const progressRateValue = normalizeNumberValue(currentValue) / normalizeNumberValue(targetValue)
  if (progressRateValue >= 1)
  {
    return "success"
  }
  if (progressRateValue >= 0.8)
  {
    return "warning"
  }
  return "exception"
}

// 计算经营目标相对本月节奏的差值，帮助管理层识别领先或落后程度。
function calculateDashboardTargetPaceGapValue(targetKey, currentValue, targetValue, monthProgressRate) {
  if (targetValue === undefined)
  {
    return 0
  }
  const dashboardTargetMeta = getDashboardTargetMeta(targetKey)
  if (dashboardTargetMeta?.unitType === "rate")
  {
    return normalizeNumberValue(currentValue) - normalizeNumberValue(targetValue)
  }
  return normalizeNumberValue(currentValue) - normalizeNumberValue(targetValue) * normalizeNumberValue(monthProgressRate)
}

// 返回经营目标节奏标签类型，帮助管理层快速看到领先、持平或落后状态。
function getDashboardTargetPaceTagType(targetKey, paceGapValue) {
  const dashboardTargetMeta = getDashboardTargetMeta(targetKey)
  const normalizedPaceGapValue = normalizeNumberValue(paceGapValue)
  if (dashboardTargetMeta?.unitType === "rate")
  {
    if (normalizedPaceGapValue >= 0)
    {
      return "success"
    }
    if (normalizedPaceGapValue >= -3)
    {
      return "warning"
    }
    return "danger"
  }
  if (normalizedPaceGapValue >= 0)
  {
    return "success"
  }
  if (Math.abs(normalizedPaceGapValue) <= 10000)
  {
    return "warning"
  }
  return "danger"
}

// 返回经营目标节奏标签文案，统一卡片上的节奏状态提示。
function buildDashboardTargetPaceTagText(targetKey, paceGapValue) {
  const dashboardTargetMeta = getDashboardTargetMeta(targetKey)
  const normalizedPaceGapValue = normalizeNumberValue(paceGapValue)
  if (dashboardTargetMeta?.unitType === "rate")
  {
    if (normalizedPaceGapValue >= 0)
    {
      return "高于目标"
    }
    if (normalizedPaceGapValue >= -3)
    {
      return "接近目标"
    }
    return "低于目标"
  }
  if (normalizedPaceGapValue >= 0)
  {
    return "领先节奏"
  }
  if (Math.abs(normalizedPaceGapValue) <= 10000)
  {
    return "接近节奏"
  }
  return "落后节奏"
}

// 返回经营目标节奏说明文案，帮助管理层快速理解当前值与月度应达值的差距。
function buildDashboardTargetPaceText(targetKey, currentValue, targetValue, monthProgressRate) {
  if (targetValue === undefined)
  {
    return "尚未设置目标节奏"
  }
  const dashboardTargetMeta = getDashboardTargetMeta(targetKey)
  const normalizedCurrentValue = normalizeNumberValue(currentValue)
  const normalizedTargetValue = normalizeNumberValue(targetValue)
  if (dashboardTargetMeta?.unitType === "rate")
  {
    const targetDifferenceValue = normalizedCurrentValue - normalizedTargetValue
    if (targetDifferenceValue >= 0)
    {
      return `当前高于目标 ${targetDifferenceValue.toFixed(2)} 个点`
    }
    return `当前距目标 ${Math.abs(targetDifferenceValue).toFixed(2)} 个点`
  }
  const expectedCurrentValue = normalizedTargetValue * normalizeNumberValue(monthProgressRate)
  const differenceValue = normalizedCurrentValue - expectedCurrentValue
  if (differenceValue >= 0)
  {
    return `按当前月份进度应达 ￥${formatAmountValue(expectedCurrentValue)}，目前领先 ￥${formatAmountValue(differenceValue)}`
  }
  return `按当前月份进度应达 ￥${formatAmountValue(expectedCurrentValue)}，目前落后 ￥${formatAmountValue(Math.abs(differenceValue))}`
}

// 返回经营目标月末预测文案，帮助管理层提前判断本月是否能够完成目标。
function buildDashboardTargetForecastText(targetKey, currentValue, targetValue, monthProgressRate) {
  if (targetValue === undefined)
  {
    return "配置目标后可显示月末达成预测"
  }
  const dashboardTargetMeta = getDashboardTargetMeta(targetKey)
  const normalizedCurrentValue = normalizeNumberValue(currentValue)
  const normalizedTargetValue = normalizeNumberValue(targetValue)
  if (dashboardTargetMeta?.unitType === "rate")
  {
    if (normalizedCurrentValue >= normalizedTargetValue)
    {
      return `按当前回款水平预计月末可维持 ${formatRateValue(normalizedCurrentValue)}`
    }
    return `按当前回款水平预计月末约为 ${formatRateValue(normalizedCurrentValue)}，仍差 ${(normalizedTargetValue - normalizedCurrentValue).toFixed(2)} 个点`
  }
  const normalizedMonthProgressRate = normalizeNumberValue(monthProgressRate)
  if (normalizedMonthProgressRate <= 0)
  {
    return "当前月份进度不足，暂无法预测月末完成情况"
  }
  const projectedMonthEndValue = normalizedCurrentValue / normalizedMonthProgressRate
  const projectedTargetCompletionRate = calculateRateValue(projectedMonthEndValue, normalizedTargetValue)
  if (projectedMonthEndValue >= normalizedTargetValue)
  {
    return `按当前节奏预计月末可达 ￥${formatAmountValue(projectedMonthEndValue)}，预计超目标 ${(projectedTargetCompletionRate - 100).toFixed(2)}%`
  }
  return `按当前节奏预计月末可达 ￥${formatAmountValue(projectedMonthEndValue)}，目标完成率约为 ${projectedTargetCompletionRate.toFixed(2)}%`
}

// 返回经营目标偏离项的推荐处理入口，帮助管理层从待办直接进入对应模块。
function resolveDashboardTargetActionType(targetKey) {
  if (targetKey === "collectionRateTarget")
  {
    return "receivableSummary"
  }
  return "saleOrderSummary"
}

// 返回经营目标偏离项的推荐处理按钮文案，避免管理层进入错误模块。
function resolveDashboardTargetActionLabel(targetKey) {
  if (targetKey === "collectionRateTarget")
  {
    return "查看应收台账"
  }
  return "查看销售单"
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

// 重置经营目标表单，保证每次打开弹窗都基于最新配置而不是上次输入值。
function resetDashboardTargetForm() {
  dashboardTargetForm.saleTargetAmount = undefined
  dashboardTargetForm.grossProfitTargetAmount = undefined
  dashboardTargetForm.collectionRateTarget = undefined
}

// 根据最新配置详情回填经营目标表单，避免编辑弹窗出现旧值残留。
function syncDashboardTargetFormFromConfigDetailMap() {
  for (const dashboardTargetMeta of dashboardTargetMetaList)
  {
    const targetValue = getDashboardTargetValue(dashboardTargetMeta.key)
    dashboardTargetForm[dashboardTargetMeta.key] = targetValue === undefined ? undefined : targetValue
  }
}

// 生成资金压力窗口状态标签类型，帮助管理层快速区分净流入与净流出压力。
function getCashFlowWindowTagType(netCashFlowValue) {
  const normalizedNetCashFlowValue = normalizeNumberValue(netCashFlowValue)
  if (normalizedNetCashFlowValue < 0)
  {
    return "danger"
  }
  if (normalizedNetCashFlowValue > 0)
  {
    return "success"
  }
  return "warning"
}

// 生成资金压力窗口状态文案，便于管理层快速判断未来一段时间的收支态势。
function getCashFlowWindowTagText(netCashFlowValue) {
  const normalizedNetCashFlowValue = normalizeNumberValue(netCashFlowValue)
  if (normalizedNetCashFlowValue < 0)
  {
    return "净流出压力"
  }
  if (normalizedNetCashFlowValue > 0)
  {
    return "预计净流入"
  }
  return "基本持平"
}

// 生成资金压力窗口摘要文案，提示管理层近期应重点关注流出压力还是回款安排。
function buildCashFlowWindowDescription(netCashFlowValue, forecastDays) {
  const normalizedNetCashFlowValue = normalizeNumberValue(netCashFlowValue)
  if (normalizedNetCashFlowValue < 0)
  {
    return `未来 ${forecastDays} 天预计净流出，建议提前安排付款节奏和资金准备。`
  }
  if (normalizedNetCashFlowValue > 0)
  {
    return `未来 ${forecastDays} 天预计净流入，可结合订单节奏统筹资金使用。`
  }
  return `未来 ${forecastDays} 天预计收支基本持平，建议持续关注大额回款和付款节点。`
}

// 判断当前后端是否已经直接返回管理驾驶舱数据，避免兼容模式重复执行。
function hasRemoteManagementCockpitData(dashboardResponseData) {
  const managementOverviewValue = dashboardResponseData?.managementOverview
  const riskOverviewValue = dashboardResponseData?.riskOverview
  return !!managementOverviewValue && Object.keys(managementOverviewValue).length > 0
    && !!riskOverviewValue && Object.keys(riskOverviewValue).length > 0
}

// 统一提取对象中第一个存在的字段值，兼容历史接口字段差异。
function getFirstDefinedValue(targetRow, fieldNameList = []) {
  for (const fieldName of fieldNameList)
  {
    const fieldValue = targetRow?.[fieldName]
    if (fieldValue !== undefined && fieldValue !== null && fieldValue !== "")
    {
      return fieldValue
    }
  }
  return undefined
}

// 统一解析日期字段，兼容日期字符串、时间字符串和 Date 对象。
function parseDateValue(dateValue) {
  if (!dateValue)
  {
    return undefined
  }
  if (dateValue instanceof Date)
  {
    return Number.isNaN(dateValue.getTime()) ? undefined : dateValue
  }
  const normalizedDateText = String(dateValue).trim().replace(" ", "T")
  const parsedDateValue = new Date(normalizedDateText)
  return Number.isNaN(parsedDateValue.getTime()) ? undefined : parsedDateValue
}

// 把日期对象格式化成查询接口使用的 yyyy-mm-dd 文本。
function formatDateValueForQuery(dateValue) {
  const yearValue = dateValue.getFullYear()
  const monthValue = `${dateValue.getMonth() + 1}`.padStart(2, "0")
  const dayValue = `${dateValue.getDate()}`.padStart(2, "0")
  return `${yearValue}-${monthValue}-${dayValue}`
}

// 统一返回日期的零点时间，便于账龄和逾期计算保持按天对齐。
function getDateStartValue(dateValue) {
  return new Date(dateValue.getFullYear(), dateValue.getMonth(), dateValue.getDate())
}

// 构造指定月份的起止范围，供经营指标兼容模式按月汇总使用。
function buildMonthRange(monthOffset) {
  const currentDateValue = new Date()
  const targetMonthFirstDateValue = new Date(currentDateValue.getFullYear(), currentDateValue.getMonth() + monthOffset, 1)
  const targetMonthLastDateValue = new Date(targetMonthFirstDateValue.getFullYear(), targetMonthFirstDateValue.getMonth() + 1, 0)
  return {
    startDateObject: targetMonthFirstDateValue,
    endDateObject: targetMonthLastDateValue,
    startDate: formatDateValueForQuery(targetMonthFirstDateValue),
    endDate: formatDateValueForQuery(targetMonthLastDateValue),
    startDateTime: `${formatDateValueForQuery(targetMonthFirstDateValue)} 00:00:00`,
    endDateTime: `${formatDateValueForQuery(targetMonthLastDateValue)} 23:59:59`
  }
}

// 判断业务时间是否落在指定月份范围内，避免兼容模式统计跨月串数。
function isDateInMonthRange(dateValue, monthRange) {
  const parsedDateValue = parseDateValue(dateValue)
  if (!parsedDateValue)
  {
    return false
  }
  return parsedDateValue >= monthRange.startDateObject
    && parsedDateValue < new Date(monthRange.endDateObject.getFullYear(), monthRange.endDateObject.getMonth(), monthRange.endDateObject.getDate() + 1)
}

// 统一按分页拉取完整列表，保证兼容模式可以在现有列表接口上完成管理驾驶舱汇总。
async function fetchPagedRowList(requestMethod, baseQueryParams = {}) {
  const resultRowList = []
  const pageSize = normalizeCountValue(baseQueryParams.pageSize) || 200
  const maxPageCount = 20
  for (let currentPageNumber = 1; currentPageNumber <= maxPageCount; currentPageNumber += 1)
  {
    const response = await requestMethod({
      ...baseQueryParams,
      pageNum: currentPageNumber,
      pageSize: pageSize
    })
    const currentRowList = response?.rows || []
    const currentTotal = normalizeCountValue(response?.total)
    resultRowList.push(...currentRowList)
    if (currentRowList.length < pageSize || (currentTotal > 0 && resultRowList.length >= currentTotal))
    {
      break
    }
  }
  return resultRowList
}

// 读取 Promise.allSettled 的成功结果，失败时回落到默认值，避免兼容模式整体中断。
function getSettledResultValue(settledResult, defaultValue) {
  if (settledResult.status === "fulfilled")
  {
    return settledResult.value
  }
  return defaultValue
}

// 统一计算比率，分母为空时回落到 0，保证管理驾驶舱不会出现 NaN。
function calculateRateValue(numeratorValue, denominatorValue) {
  const normalizedDenominatorValue = normalizeNumberValue(denominatorValue)
  if (normalizedDenominatorValue <= 0)
  {
    return 0
  }
  return normalizeNumberValue(numeratorValue) / normalizedDenominatorValue * 100
}

// 统一计算环比增长率，上一期为 0 时返回 null 以提示新增口径。
function calculateGrowthRateValue(currentValue, previousValue) {
  const normalizedCurrentValue = normalizeNumberValue(currentValue)
  const normalizedPreviousValue = normalizeNumberValue(previousValue)
  if (normalizedPreviousValue <= 0)
  {
    return normalizedCurrentValue === 0 ? 0 : null
  }
  return (normalizedCurrentValue - normalizedPreviousValue) / normalizedPreviousValue * 100
}

// 把商品主数据转换为成本价映射，便于兼容模式快速计算销售毛利。
function buildProductCostPriceMap(productRowList = []) {
  const productCostPriceMap = {}
  for (const productRow of productRowList)
  {
    const productCode = getFirstDefinedValue(productRow, ["productCode", "product_code"])
    if (!productCode)
    {
      continue
    }
    productCostPriceMap[productCode] = normalizeNumberValue(getFirstDefinedValue(productRow, ["costPrice", "cost_price"]))
  }
  return productCostPriceMap
}

// 把客户主数据转换为名称映射，避免兼容模式展示内部编号。
function buildCustomerNameMap(customerRowList = []) {
  const customerNameMap = {}
  for (const customerRow of customerRowList)
  {
    const customerId = getFirstDefinedValue(customerRow, ["customerId", "customer_id"])
    if (customerId === undefined || customerId === null || customerId === "")
    {
      continue
    }
    customerNameMap[customerId] = getFirstDefinedValue(customerRow, ["customerName", "customer_name"])
  }
  return customerNameMap
}

// 把供应商主数据转换为名称映射，避免兼容模式展示内部编号。
function buildSupplierNameMap(supplierRowList = []) {
  const supplierNameMap = {}
  for (const supplierRow of supplierRowList)
  {
    const supplierId = getFirstDefinedValue(supplierRow, ["supplierId", "supplier_id"])
    if (supplierId === undefined || supplierId === null || supplierId === "")
    {
      continue
    }
    supplierNameMap[supplierId] = getFirstDefinedValue(supplierRow, ["supplierName", "supplier_name"])
  }
  return supplierNameMap
}

// 返回销售报表行的业务时间，优先使用销售日期字段。
function getSaleReportBusinessTime(saleReportRow) {
  return parseDateValue(getFirstDefinedValue(saleReportRow, ["saleDate", "auditTime", "createTime"]))
}

// 返回销售单的业务时间，优先使用审核时间字段。
function getSaleOrderBusinessTime(saleOrderRow) {
  return parseDateValue(getFirstDefinedValue(saleOrderRow, ["auditTime", "createTime"]))
}

// 返回回款流水的业务时间，优先使用回款时间字段。
function getReceiptBusinessTime(receiptRow) {
  return parseDateValue(getFirstDefinedValue(receiptRow, ["paymentTime", "receiptDate", "createTime"]))
}

// 返回采购订单的业务时间，优先使用采购日期字段。
function getPurchaseOrderBusinessTime(purchaseOrderRow) {
  return parseDateValue(getFirstDefinedValue(purchaseOrderRow, ["purchaseDate", "auditTime", "createTime"]))
}

// 返回待审核单据的创建时间，供超时未审核计算使用。
function getRowCreateTime(rowData) {
  return parseDateValue(getFirstDefinedValue(rowData, ["createTime", "create_time"]))
}

// 判断应收记录是否仍然未结清，便于兼容模式计算账龄与风险。
function isOpenReceivableRow(receivableRow) {
  const receivableStatus = String(getFirstDefinedValue(receivableRow, ["status"]) || "")
  const remainAmountValue = getReceivableRemainAmountValue(receivableRow)
  return remainAmountValue > 0 && receivableStatus !== "paid" && receivableStatus !== "2"
}

// 判断应付记录是否仍然未结清，便于兼容模式计算账龄与风险。
function isOpenPayableRow(payableRow) {
  const payableStatus = String(getFirstDefinedValue(payableRow, ["status"]) || "")
  const remainAmountValue = getPayableRemainAmountValue(payableRow)
  return remainAmountValue > 0 && payableStatus !== "2" && payableStatus !== "paid"
}

// 返回应收记录的未收金额，兼容旧字段和前端已计算字段。
function getReceivableRemainAmountValue(receivableRow) {
  const remainAmountValue = getFirstDefinedValue(receivableRow, ["remainAmount", "remain_amount"])
  if (remainAmountValue !== undefined)
  {
    return normalizeNumberValue(remainAmountValue)
  }
  return normalizeNumberValue(getFirstDefinedValue(receivableRow, ["amountDue", "amount_due"]))
    - normalizeNumberValue(getFirstDefinedValue(receivableRow, ["amountPaid", "amount_paid"]))
}

// 返回应付记录的未付金额，兼容旧字段和前端已计算字段。
function getPayableRemainAmountValue(payableRow) {
  const remainAmountValue = getFirstDefinedValue(payableRow, ["remainAmount", "remain_amount"])
  if (remainAmountValue !== undefined)
  {
    return normalizeNumberValue(remainAmountValue)
  }
  return normalizeNumberValue(getFirstDefinedValue(payableRow, ["amountDue", "amount_due"]))
    - normalizeNumberValue(getFirstDefinedValue(payableRow, ["amountPaid", "amount_paid"]))
}

// 判断采购订单是否应计入经营统计，避免草稿和作废单据干扰管理口径。
function isStatisticalPurchaseOrderStatus(statusValue) {
  const normalizedStatusValue = String(statusValue || "")
  return ["1", "2", "3", "audited", "partially_inbound", "completed"].includes(normalizedStatusValue)
}

// 判断采购订单是否仍处于待审核状态，供超时未审核统计使用。
function isPendingPurchaseOrderStatus(statusValue) {
  const normalizedStatusValue = String(statusValue || "")
  return normalizedStatusValue === "0" || normalizedStatusValue === "submitted"
}

// 判断销售、入库、出库单是否仍处于已提交待审核状态。
function isSubmittedDocumentStatus(statusValue) {
  return String(statusValue || "") === "submitted"
}

// 统计超过 3 天仍未审核的单据数量，帮助管理层识别流程堵点。
function countTimeoutPendingRowList(rowList = [], statusMatcher) {
  const timeoutThresholdValue = new Date(Date.now() - 3 * 24 * 60 * 60 * 1000)
  let pendingTimeoutCount = 0
  for (const rowData of rowList)
  {
    if (!statusMatcher(getFirstDefinedValue(rowData, ["status"])))
    {
      continue
    }
    const rowCreateTime = getRowCreateTime(rowData)
    if (rowCreateTime && rowCreateTime < timeoutThresholdValue)
    {
      pendingTimeoutCount += 1
    }
  }
  return pendingTimeoutCount
}

// 构造账龄分布基础桶，保证兼容模式的表格顺序与后端正式口径一致。
function buildAgingBucketList() {
  return [
    { bucketName: "未设置到期日", sortOrder: 0, amount: 0, recordCount: 0 },
    { bucketName: "未到期", sortOrder: 1, amount: 0, recordCount: 0 },
    { bucketName: "逾期1-30天", sortOrder: 2, amount: 0, recordCount: 0 },
    { bucketName: "逾期31-60天", sortOrder: 3, amount: 0, recordCount: 0 },
    { bucketName: "逾期61天以上", sortOrder: 4, amount: 0, recordCount: 0 }
  ]
}

// 根据应收台账构造账龄分布，帮助管理层查看回款风险集中区间。
function buildReceivableAgingListFromRowList(receivableRowList = []) {
  const agingBucketList = buildAgingBucketList()
  const currentDateStartValue = getDateStartValue(new Date())
  for (const receivableRow of receivableRowList)
  {
    if (!isOpenReceivableRow(receivableRow))
    {
      continue
    }
    const remainAmountValue = getReceivableRemainAmountValue(receivableRow)
    const dueDateValue = parseDateValue(getFirstDefinedValue(receivableRow, ["dueDate", "due_date"]))
    let bucketIndex = 0
    if (dueDateValue)
    {
      const overdueDayCount = Math.floor((currentDateStartValue.getTime() - getDateStartValue(dueDateValue).getTime()) / (24 * 60 * 60 * 1000))
      if (overdueDayCount <= 0)
      {
        bucketIndex = 1
      }
      else if (overdueDayCount <= 30)
      {
        bucketIndex = 2
      }
      else if (overdueDayCount <= 60)
      {
        bucketIndex = 3
      }
      else
      {
        bucketIndex = 4
      }
    }
    agingBucketList[bucketIndex].amount += remainAmountValue
    agingBucketList[bucketIndex].recordCount += 1
  }
  return agingBucketList
}

// 根据应付台账构造账龄分布，帮助管理层查看未来付款压力区间。
function buildPayableAgingListFromRowList(payableRowList = []) {
  const agingBucketList = buildAgingBucketList()
  const currentDateStartValue = getDateStartValue(new Date())
  for (const payableRow of payableRowList)
  {
    if (!isOpenPayableRow(payableRow))
    {
      continue
    }
    const remainAmountValue = getPayableRemainAmountValue(payableRow)
    const dueDateValue = parseDateValue(getFirstDefinedValue(payableRow, ["dueDate", "due_date"]))
    let bucketIndex = 0
    if (dueDateValue)
    {
      const overdueDayCount = Math.floor((currentDateStartValue.getTime() - getDateStartValue(dueDateValue).getTime()) / (24 * 60 * 60 * 1000))
      if (overdueDayCount <= 0)
      {
        bucketIndex = 1
      }
      else if (overdueDayCount <= 30)
      {
        bucketIndex = 2
      }
      else if (overdueDayCount <= 60)
      {
        bucketIndex = 3
      }
      else
      {
        bucketIndex = 4
      }
    }
    agingBucketList[bucketIndex].amount += remainAmountValue
    agingBucketList[bucketIndex].recordCount += 1
  }
  return agingBucketList
}

// 返回兼容模式下客户展示名称，优先使用主数据名称，缺失时回退到更友好的资料缺失提示。
function getCustomerDisplayNameForCockpit(receivableRow, customerNameMap) {
  const customerId = getFirstDefinedValue(receivableRow, ["customerId", "customer_id"])
  const customerName = getFirstDefinedValue(receivableRow, ["customerName", "customer_name"]) || customerNameMap[customerId]
  if (customerName)
  {
    return customerName
  }
  return "客户资料缺失"
}

// 返回兼容模式下供应商展示名称，优先使用主数据名称，缺失时回退到更友好的资料缺失提示。
function getSupplierDisplayNameForCockpit(payableRow, supplierNameMap) {
  const supplierId = getFirstDefinedValue(payableRow, ["supplierId", "supplier_id"])
  const supplierName = getFirstDefinedValue(payableRow, ["supplierName", "supplier_name"]) || supplierNameMap[supplierId]
  if (supplierName)
  {
    return supplierName
  }
  return "供应商资料缺失"
}

// 根据应收台账汇总高风险客户，帮助管理层快速锁定逾期最严重的客户。
function buildTopRiskCustomerList(receivableRowList = [], customerNameMap = {}) {
  const groupedCustomerMap = {}
  const currentDateStartValue = getDateStartValue(new Date())
  for (const receivableRow of receivableRowList)
  {
    if (!isOpenReceivableRow(receivableRow))
    {
      continue
    }
    const dueDateValue = parseDateValue(getFirstDefinedValue(receivableRow, ["dueDate", "due_date"]))
    if (!dueDateValue || getDateStartValue(dueDateValue) >= currentDateStartValue)
    {
      continue
    }
    const customerId = getFirstDefinedValue(receivableRow, ["customerId", "customer_id"]) || `history-${getFirstDefinedValue(receivableRow, ["saleOrderId", "sale_order_id", "receivableId", "receivable_id"])}`
    const remainAmountValue = getReceivableRemainAmountValue(receivableRow)
    const overdueDayCount = Math.floor((currentDateStartValue.getTime() - getDateStartValue(dueDateValue).getTime()) / (24 * 60 * 60 * 1000))
    if (!groupedCustomerMap[customerId])
    {
      groupedCustomerMap[customerId] = {
        targetId: getFirstDefinedValue(receivableRow, ["customerId", "customer_id"]),
        targetName: getCustomerDisplayNameForCockpit(receivableRow, customerNameMap),
        remainAmount: 0,
        overdueDays: 0,
        recordCount: 0
      }
    }
    groupedCustomerMap[customerId].remainAmount += remainAmountValue
    groupedCustomerMap[customerId].overdueDays = Math.max(groupedCustomerMap[customerId].overdueDays, overdueDayCount)
    groupedCustomerMap[customerId].recordCount += 1
  }
  return Object.values(groupedCustomerMap)
    .sort((leftCustomer, rightCustomer) => {
      if (normalizeNumberValue(rightCustomer.remainAmount) !== normalizeNumberValue(leftCustomer.remainAmount))
      {
        return normalizeNumberValue(rightCustomer.remainAmount) - normalizeNumberValue(leftCustomer.remainAmount)
      }
      return normalizeNumberValue(rightCustomer.overdueDays) - normalizeNumberValue(leftCustomer.overdueDays)
    })
    .slice(0, 5)
}

// 根据应付台账汇总重点付款供应商，帮助管理层提前安排近期付款。
function buildTopRiskSupplierList(payableRowList = [], supplierNameMap = {}, payableRiskDays = 7) {
  const groupedSupplierMap = {}
  const currentDateStartValue = getDateStartValue(new Date())
  const dueRiskBoundaryValue = new Date(currentDateStartValue.getTime() + payableRiskDays * 24 * 60 * 60 * 1000)
  for (const payableRow of payableRowList)
  {
    if (!isOpenPayableRow(payableRow))
    {
      continue
    }
    const dueDateValue = parseDateValue(getFirstDefinedValue(payableRow, ["dueDate", "due_date"]))
    if (!dueDateValue || getDateStartValue(dueDateValue) > dueRiskBoundaryValue)
    {
      continue
    }
    const supplierId = getFirstDefinedValue(payableRow, ["supplierId", "supplier_id"]) || `history-${getFirstDefinedValue(payableRow, ["purchaseOrderId", "purchase_order_id", "payableId", "payable_id"])}`
    const remainAmountValue = getPayableRemainAmountValue(payableRow)
    const dueInDayCount = Math.floor((getDateStartValue(dueDateValue).getTime() - currentDateStartValue.getTime()) / (24 * 60 * 60 * 1000))
    if (!groupedSupplierMap[supplierId])
    {
      groupedSupplierMap[supplierId] = {
        targetId: getFirstDefinedValue(payableRow, ["supplierId", "supplier_id"]),
        targetName: getSupplierDisplayNameForCockpit(payableRow, supplierNameMap),
        remainAmount: 0,
        dueInDays: dueInDayCount,
        recordCount: 0
      }
    }
    groupedSupplierMap[supplierId].remainAmount += remainAmountValue
    groupedSupplierMap[supplierId].dueInDays = Math.min(groupedSupplierMap[supplierId].dueInDays, dueInDayCount)
    groupedSupplierMap[supplierId].recordCount += 1
  }
  return Object.values(groupedSupplierMap)
    .sort((leftSupplier, rightSupplier) => {
      if (normalizeCountValue(leftSupplier.dueInDays) !== normalizeCountValue(rightSupplier.dueInDays))
      {
        return normalizeCountValue(leftSupplier.dueInDays) - normalizeCountValue(rightSupplier.dueInDays)
      }
      return normalizeNumberValue(rightSupplier.remainAmount) - normalizeNumberValue(leftSupplier.remainAmount)
    })
    .slice(0, 5)
}

// 根据现有业务列表计算经营概览，保证管理层在旧后端环境下也能先看趋势与利润。
function buildManagementOverviewData(saleReportRowList = [], receivableRowList = [], purchaseOrderRowList = [], productCostPriceMap = {}, saleOrderRowList = [], currentMonthRange, previousMonthRange) {
  let currentMonthSaleAmount = 0
  let previousMonthSaleAmount = 0
  let currentMonthSaleCostAmount = 0
  let previousMonthSaleCostAmount = 0
  for (const saleReportRow of saleReportRowList)
  {
    const saleBusinessTime = getSaleReportBusinessTime(saleReportRow)
    if (!saleBusinessTime)
    {
      continue
    }
    const saleAmountValue = normalizeNumberValue(getFirstDefinedValue(saleReportRow, ["amount"]))
    const productCode = getFirstDefinedValue(saleReportRow, ["productCode", "product_code"])
    const productCostPriceValue = normalizeNumberValue(productCostPriceMap[productCode])
    const quantityValue = normalizeNumberValue(getFirstDefinedValue(saleReportRow, ["quantity"]))
    const saleCostAmountValue = quantityValue * productCostPriceValue
    if (isDateInMonthRange(saleBusinessTime, currentMonthRange))
    {
      currentMonthSaleAmount += saleAmountValue
      currentMonthSaleCostAmount += saleCostAmountValue
      continue
    }
    if (isDateInMonthRange(saleBusinessTime, previousMonthRange))
    {
      previousMonthSaleAmount += saleAmountValue
      previousMonthSaleCostAmount += saleCostAmountValue
    }
  }

  const saleOrderPeriodMap = {}
  for (const saleOrderRow of saleOrderRowList)
  {
    const saleOrderId = getFirstDefinedValue(saleOrderRow, ["saleOrderId", "sale_order_id"])
    if (saleOrderId === undefined || saleOrderId === null || saleOrderId === "")
    {
      continue
    }
    const saleOrderBusinessTime = getSaleOrderBusinessTime(saleOrderRow)
    if (!saleOrderBusinessTime)
    {
      continue
    }
    if (isDateInMonthRange(saleOrderBusinessTime, currentMonthRange))
    {
      saleOrderPeriodMap[saleOrderId] = "current"
      continue
    }
    if (isDateInMonthRange(saleOrderBusinessTime, previousMonthRange))
    {
      saleOrderPeriodMap[saleOrderId] = "previous"
    }
  }

  let currentMonthReceivedAmount = 0
  let previousMonthReceivedAmount = 0
  for (const receivableRow of receivableRowList)
  {
    const saleOrderId = getFirstDefinedValue(receivableRow, ["saleOrderId", "sale_order_id"])
    const saleOrderPeriod = saleOrderPeriodMap[saleOrderId]
    if (!saleOrderPeriod)
    {
      continue
    }
    const paidAmountValue = normalizeNumberValue(getFirstDefinedValue(receivableRow, ["amountPaid", "amount_paid"]))
    if (saleOrderPeriod === "current")
    {
      currentMonthReceivedAmount += paidAmountValue
      continue
    }
    if (saleOrderPeriod === "previous")
    {
      previousMonthReceivedAmount += paidAmountValue
    }
  }

  let currentMonthPurchaseAmount = 0
  let previousMonthPurchaseAmount = 0
  for (const purchaseOrderRow of purchaseOrderRowList)
  {
    if (!isStatisticalPurchaseOrderStatus(getFirstDefinedValue(purchaseOrderRow, ["status"])))
    {
      continue
    }
    const purchaseBusinessTime = getPurchaseOrderBusinessTime(purchaseOrderRow)
    if (!purchaseBusinessTime)
    {
      continue
    }
    const purchaseAmountValue = normalizeNumberValue(getFirstDefinedValue(purchaseOrderRow, ["totalAmount", "total_amount"]))
    if (isDateInMonthRange(purchaseBusinessTime, currentMonthRange))
    {
      currentMonthPurchaseAmount += purchaseAmountValue
      continue
    }
    if (isDateInMonthRange(purchaseBusinessTime, previousMonthRange))
    {
      previousMonthPurchaseAmount += purchaseAmountValue
    }
  }

  const currentMonthGrossProfitAmount = currentMonthSaleAmount - currentMonthSaleCostAmount
  const previousMonthGrossProfitAmount = previousMonthSaleAmount - previousMonthSaleCostAmount
  const currentMonthCollectionRate = calculateRateValue(currentMonthReceivedAmount, currentMonthSaleAmount)
  const previousMonthCollectionRate = calculateRateValue(previousMonthReceivedAmount, previousMonthSaleAmount)

  return {
    currentMonthSaleAmount: currentMonthSaleAmount,
    previousMonthSaleAmount: previousMonthSaleAmount,
    saleGrowthRate: calculateGrowthRateValue(currentMonthSaleAmount, previousMonthSaleAmount),
    currentMonthPurchaseAmount: currentMonthPurchaseAmount,
    previousMonthPurchaseAmount: previousMonthPurchaseAmount,
    purchaseGrowthRate: calculateGrowthRateValue(currentMonthPurchaseAmount, previousMonthPurchaseAmount),
    currentMonthGrossProfitAmount: currentMonthGrossProfitAmount,
    previousMonthGrossProfitAmount: previousMonthGrossProfitAmount,
    grossProfitGrowthRate: calculateGrowthRateValue(currentMonthGrossProfitAmount, previousMonthGrossProfitAmount),
    currentMonthGrossProfitRate: calculateRateValue(currentMonthGrossProfitAmount, currentMonthSaleAmount),
    currentMonthReceivedAmount: currentMonthReceivedAmount,
    previousMonthReceivedAmount: previousMonthReceivedAmount,
    currentMonthCollectionRate: currentMonthCollectionRate,
    previousMonthCollectionRate: previousMonthCollectionRate,
    collectionRateDifference: currentMonthCollectionRate - previousMonthCollectionRate
  }
}

// 根据现有应收应付和待审核单据构造风险概览，保证兼容模式下也能快速识别经营风险。
function buildRiskOverviewData(receivableRowList = [], payableRowList = [], saleOrderRowList = [], inboundRowList = [], outboundRowList = [], purchaseOrderRowList = [], stockWarningCountValue = 0, payableRiskDays = 7) {
  const currentDateStartValue = getDateStartValue(new Date())
  const dueRiskBoundaryValue = new Date(currentDateStartValue.getTime() + payableRiskDays * 24 * 60 * 60 * 1000)
  let overdueReceivableAmount = 0
  let dueSoonPayableAmount = 0
  let longOverdueReceivableCount = 0

  for (const receivableRow of receivableRowList)
  {
    if (!isOpenReceivableRow(receivableRow))
    {
      continue
    }
    const dueDateValue = parseDateValue(getFirstDefinedValue(receivableRow, ["dueDate", "due_date"]))
    if (!dueDateValue)
    {
      continue
    }
    const dueDateStartValue = getDateStartValue(dueDateValue)
    if (dueDateStartValue < currentDateStartValue)
    {
      overdueReceivableAmount += getReceivableRemainAmountValue(receivableRow)
      const overdueDayCount = Math.floor((currentDateStartValue.getTime() - dueDateStartValue.getTime()) / (24 * 60 * 60 * 1000))
      if (overdueDayCount > 60)
      {
        longOverdueReceivableCount += 1
      }
    }
  }

  for (const payableRow of payableRowList)
  {
    if (!isOpenPayableRow(payableRow))
    {
      continue
    }
    const dueDateValue = parseDateValue(getFirstDefinedValue(payableRow, ["dueDate", "due_date"]))
    if (!dueDateValue)
    {
      continue
    }
    const dueDateStartValue = getDateStartValue(dueDateValue)
    if (dueDateStartValue <= dueRiskBoundaryValue)
    {
      dueSoonPayableAmount += getPayableRemainAmountValue(payableRow)
    }
  }

  const pendingApprovalTimeoutCount = countTimeoutPendingRowList(saleOrderRowList, isSubmittedDocumentStatus)
    + countTimeoutPendingRowList(inboundRowList, isSubmittedDocumentStatus)
    + countTimeoutPendingRowList(outboundRowList, isSubmittedDocumentStatus)
    + countTimeoutPendingRowList(purchaseOrderRowList, isPendingPurchaseOrderStatus)

  return {
    overdueReceivableAmount: overdueReceivableAmount,
    dueSoonPayableAmount: dueSoonPayableAmount,
    longOverdueReceivableCount: longOverdueReceivableCount,
    pendingApprovalTimeoutCount: pendingApprovalTimeoutCount,
    stockWarningCount: normalizeCountValue(stockWarningCountValue),
    payableRiskDays: payableRiskDays
  }
}

// 返回付款流水的业务时间，优先使用付款时间字段。
function getPaymentBusinessTime(paymentRow) {
  return parseDateValue(getFirstDefinedValue(paymentRow, ["paymentTime", "createTime"]))
}

// 返回库存流水的业务时间，统一库存周转和滞销计算口径。
function getStockLogBusinessTime(stockLogRow) {
  return parseDateValue(getFirstDefinedValue(stockLogRow, ["createTime", "create_time"]))
}

// 返回库存台账的参考时间，便于在无出库记录时仍能计算积压天数。
function getStockBusinessTime(stockRow) {
  return parseDateValue(getFirstDefinedValue(stockRow, ["updateTime", "createTime", "create_time"]))
}

// 把商品主数据转换为详情映射，便于利润、库存和组织贡献模块统一取商品信息。
function buildProductInfoMap(productRowList = []) {
  const productInfoMap = {}
  for (const productRow of productRowList)
  {
    const productId = getFirstDefinedValue(productRow, ["productId", "product_id"])
    if (productId === undefined || productId === null || productId === "")
    {
      continue
    }
    productInfoMap[productId] = {
      productId: productId,
      productCode: getFirstDefinedValue(productRow, ["productCode", "product_code"]),
      productName: getFirstDefinedValue(productRow, ["productName", "product_name"]) || "商品资料缺失",
      categoryName: getFirstDefinedValue(productRow, ["categoryName", "category_name"]) || "未分类",
      costPrice: normalizeNumberValue(getFirstDefinedValue(productRow, ["costPrice", "cost_price"])),
      unitName: getFirstDefinedValue(productRow, ["unitName", "unit_name"]) || "件"
    }
  }
  return productInfoMap
}

// 把客户主数据转换为详情映射，便于客户利润、区域分析和经营简报统一取值。
function buildCustomerDetailMap(customerRowList = []) {
  const customerDetailMap = {}
  for (const customerRow of customerRowList)
  {
    const customerId = getFirstDefinedValue(customerRow, ["customerId", "customer_id"])
    if (customerId === undefined || customerId === null || customerId === "")
    {
      continue
    }
    customerDetailMap[customerId] = {
      customerId: customerId,
      customerName: getFirstDefinedValue(customerRow, ["customerName", "customer_name"]) || "客户资料缺失",
      customerLevel: getFirstDefinedValue(customerRow, ["customerLevel", "customer_level"]) || "未分级",
      address: getFirstDefinedValue(customerRow, ["address"]) || ""
    }
  }
  return customerDetailMap
}

// 构造销售单查找映射，便于从销售报表反查客户、销售员和来源单据。
function buildSaleOrderLookupMap(saleOrderRowList = []) {
  const orderNoMap = {}
  const saleOrderIdMap = {}
  for (const saleOrderRow of saleOrderRowList)
  {
    const orderNo = getFirstDefinedValue(saleOrderRow, ["orderNo", "order_no"])
    const saleOrderId = getFirstDefinedValue(saleOrderRow, ["saleOrderId", "sale_order_id"])
    if (orderNo)
    {
      orderNoMap[orderNo] = saleOrderRow
    }
    if (saleOrderId !== undefined && saleOrderId !== null && saleOrderId !== "")
    {
      saleOrderIdMap[saleOrderId] = saleOrderRow
    }
  }
  return {
    orderNoMap: orderNoMap,
    saleOrderIdMap: saleOrderIdMap
  }
}

// 把应收台账按客户聚合，便于客户利润和客户结构模块直接读取回款情况。
function buildReceivableCustomerSummaryMap(receivableRowList = [], customerDetailMap = {}) {
  const currentDateStartValue = getDateStartValue(new Date())
  const groupedCustomerMap = {}
  const customerNameMap = buildCustomerNameMap(Object.values(customerDetailMap))
  for (const receivableRow of receivableRowList)
  {
    const customerId = getFirstDefinedValue(receivableRow, ["customerId", "customer_id"]) || `history-${getFirstDefinedValue(receivableRow, ["saleOrderId", "sale_order_id", "receivableId", "receivable_id"]) || "unknown"}`
    if (!groupedCustomerMap[customerId])
    {
      groupedCustomerMap[customerId] = {
        customerId: getFirstDefinedValue(receivableRow, ["customerId", "customer_id"]),
        customerName: getCustomerDisplayNameForCockpit(receivableRow, customerNameMap),
        dueAmount: 0,
        paidAmount: 0,
        remainAmount: 0,
        overdueAmount: 0
      }
    }
    const dueAmountValue = normalizeNumberValue(getFirstDefinedValue(receivableRow, ["amountDue", "amount_due", "receivableAmount", "receivable_amount"]))
    const paidAmountValue = normalizeNumberValue(getFirstDefinedValue(receivableRow, ["amountPaid", "amount_paid"]))
    const remainAmountValue = getReceivableRemainAmountValue(receivableRow)
    groupedCustomerMap[customerId].dueAmount += dueAmountValue
    groupedCustomerMap[customerId].paidAmount += paidAmountValue
    groupedCustomerMap[customerId].remainAmount += remainAmountValue

    const dueDateValue = parseDateValue(getFirstDefinedValue(receivableRow, ["dueDate", "due_date"]))
    if (dueDateValue && getDateStartValue(dueDateValue) < currentDateStartValue && remainAmountValue > 0)
    {
      groupedCustomerMap[customerId].overdueAmount += remainAmountValue
    }

    if (!groupedCustomerMap[customerId].customerName && groupedCustomerMap[customerId].customerId !== undefined)
    {
      groupedCustomerMap[customerId].customerName = customerDetailMap[groupedCustomerMap[customerId].customerId]?.customerName || "客户资料缺失"
    }
  }
  return groupedCustomerMap
}

// 从地址里提取区域名称，帮助管理层在没有独立区域字段时也能快速查看区域贡献。
function extractRegionNameFromAddress(addressValue) {
  if (!addressValue)
  {
    return "未设置区域"
  }
  const normalizedAddressText = String(addressValue).trim()
  if (!normalizedAddressText)
  {
    return "未设置区域"
  }
  const regionMatchResult = normalizedAddressText.match(/^(.*?(省|自治区|特别行政区|市))/)
  if (regionMatchResult && regionMatchResult[1])
  {
    return regionMatchResult[1]
  }
  const separatedRegionText = normalizedAddressText.split(/[,\s/-]/).find(addressPart => addressPart)
  return separatedRegionText || "未设置区域"
}

// 返回决策分析场景下的客户展示名称，优先使用主数据名称，缺失时保留明确的资料缺失提示。
function getCustomerDisplayNameForDecision(customerId, fallbackCustomerName, customerDetailMap = {}) {
  if (customerId !== undefined && customerId !== null && customerId !== "")
  {
    return customerDetailMap[customerId]?.customerName || fallbackCustomerName || "客户资料缺失"
  }
  return fallbackCustomerName || "客户资料缺失"
}

// 返回决策分析场景下的商品展示名称，缺失主数据时仍保留明确提示。
function getProductDisplayNameForDecision(productId, productInfoMap = {}) {
  if (productId !== undefined && productId !== null && productId !== "")
  {
    return productInfoMap[productId]?.productName || "商品资料缺失"
  }
  return "商品资料缺失"
}

// 根据销售报表、销售单和应收台账生成客户利润分析，帮助管理层识别高价值客户和集中度风险。
function buildCustomerProfitAnalysisData(saleReportRowList = [], saleOrderRowList = [], customerRowList = [], receivableRowList = [], productRowList = []) {
  const customerDetailMap = buildCustomerDetailMap(customerRowList)
  const productCostPriceMap = buildProductCostPriceMap(productRowList)
  const saleOrderLookupMap = buildSaleOrderLookupMap(saleOrderRowList)
  const receivableCustomerSummaryMap = buildReceivableCustomerSummaryMap(receivableRowList, customerDetailMap)
  const groupedCustomerMap = {}
  let totalSaleAmount = 0

  for (const saleReportRow of saleReportRowList)
  {
    const orderNo = getFirstDefinedValue(saleReportRow, ["orderNo", "order_no"])
    const saleOrderRow = saleOrderLookupMap.orderNoMap[orderNo]
    const customerId = saleOrderRow?.customerId
    const customerName = getCustomerDisplayNameForDecision(
      customerId,
      getFirstDefinedValue(saleReportRow, ["customerName", "customer_name"]),
      customerDetailMap
    )
    const customerKey = customerId !== undefined && customerId !== null && customerId !== ""
      ? `customer-${customerId}`
      : `history-${customerName}-${orderNo || "unknown"}`
    const amountValue = normalizeNumberValue(getFirstDefinedValue(saleReportRow, ["amount"]))
    const quantityValue = normalizeNumberValue(getFirstDefinedValue(saleReportRow, ["quantity"]))
    const productCode = getFirstDefinedValue(saleReportRow, ["productCode", "product_code"])
    const costPriceValue = normalizeNumberValue(productCostPriceMap[productCode])
    const grossProfitAmountValue = amountValue - quantityValue * costPriceValue

    if (!groupedCustomerMap[customerKey])
    {
      groupedCustomerMap[customerKey] = {
        customerId: customerId,
        targetName: customerName,
        customerLevel: customerDetailMap[customerId]?.customerLevel || "未分级",
        saleAmount: 0,
        grossProfitAmount: 0,
        dueAmount: 0,
        paidAmount: 0,
        remainAmount: 0,
        overdueAmount: 0,
        orderNoSet: new Set()
      }
    }

    groupedCustomerMap[customerKey].saleAmount += amountValue
    groupedCustomerMap[customerKey].grossProfitAmount += grossProfitAmountValue
    if (orderNo)
    {
      groupedCustomerMap[customerKey].orderNoSet.add(orderNo)
    }
    totalSaleAmount += amountValue
  }

  const customerSummaryList = Object.values(groupedCustomerMap).map(groupedCustomer => {
    const receivableSummary = groupedCustomer.customerId !== undefined && groupedCustomer.customerId !== null && groupedCustomer.customerId !== ""
      ? receivableCustomerSummaryMap[groupedCustomer.customerId]
      : undefined
    const dueAmountValue = normalizeNumberValue(receivableSummary?.dueAmount)
    const paidAmountValue = normalizeNumberValue(receivableSummary?.paidAmount)
    const remainAmountValue = normalizeNumberValue(receivableSummary?.remainAmount)
    const overdueAmountValue = normalizeNumberValue(receivableSummary?.overdueAmount)
    return {
      customerId: groupedCustomer.customerId,
      targetName: groupedCustomer.targetName,
      customerLevel: groupedCustomer.customerLevel,
      saleAmount: groupedCustomer.saleAmount,
      grossProfitAmount: groupedCustomer.grossProfitAmount,
      grossMarginRate: calculateRateValue(groupedCustomer.grossProfitAmount, groupedCustomer.saleAmount),
      saleShareRate: calculateRateValue(groupedCustomer.saleAmount, totalSaleAmount),
      dueAmount: dueAmountValue,
      paidAmount: paidAmountValue,
      remainAmount: remainAmountValue,
      overdueAmount: overdueAmountValue,
      collectionRate: calculateRateValue(paidAmountValue, dueAmountValue),
      orderCount: groupedCustomer.orderNoSet.size
    }
  }).sort((leftCustomerSummary, rightCustomerSummary) => {
    if (normalizeNumberValue(rightCustomerSummary.grossProfitAmount) !== normalizeNumberValue(leftCustomerSummary.grossProfitAmount))
    {
      return normalizeNumberValue(rightCustomerSummary.grossProfitAmount) - normalizeNumberValue(leftCustomerSummary.grossProfitAmount)
    }
    return normalizeNumberValue(rightCustomerSummary.saleAmount) - normalizeNumberValue(leftCustomerSummary.saleAmount)
  })

  const topCustomerBySaleAmountList = [...customerSummaryList].sort((leftCustomerSummary, rightCustomerSummary) => {
    return normalizeNumberValue(rightCustomerSummary.saleAmount) - normalizeNumberValue(leftCustomerSummary.saleAmount)
  })
  const top1SaleAmount = normalizeNumberValue(topCustomerBySaleAmountList[0]?.saleAmount)
  const top3SaleAmount = topCustomerBySaleAmountList.slice(0, 3).reduce((previousAmountValue, currentCustomerSummary) => {
    return previousAmountValue + normalizeNumberValue(currentCustomerSummary.saleAmount)
  }, 0)
  const top5SaleAmount = topCustomerBySaleAmountList.slice(0, 5).reduce((previousAmountValue, currentCustomerSummary) => {
    return previousAmountValue + normalizeNumberValue(currentCustomerSummary.saleAmount)
  }, 0)
  const top1ShareRate = calculateRateValue(top1SaleAmount, totalSaleAmount)
  const top5ShareRate = calculateRateValue(top5SaleAmount, totalSaleAmount)
  let dependencyRiskText = "结构分散"
  let dependencyDescription = "客户结构相对均衡，单一客户波动对整体经营影响较小。"
  if (top1ShareRate >= 35 || top5ShareRate >= 80)
  {
    dependencyRiskText = "高依赖"
    dependencyDescription = "客户集中度偏高，建议尽快评估核心客户订单波动对收入和回款的影响。"
  }
  else if (top1ShareRate >= 25 || top5ShareRate >= 65)
  {
    dependencyRiskText = "重点关注"
    dependencyDescription = "前几大客户占比较高，建议同步跟踪客户稳定性与续单情况。"
  }

  return {
    topCustomerList: customerSummaryList.slice(0, 8),
    concentration: {
      totalSaleAmount: totalSaleAmount,
      top1SaleAmount: top1SaleAmount,
      top3SaleAmount: top3SaleAmount,
      top5SaleAmount: top5SaleAmount,
      top1ShareRate: top1ShareRate,
      top3ShareRate: calculateRateValue(top3SaleAmount, totalSaleAmount),
      top5ShareRate: top5ShareRate,
      dependencyRiskText: dependencyRiskText,
      dependencyDescription: dependencyDescription
    }
  }
}

// 根据当前库存和库存流水生成库存决策分析，帮助管理层识别周转效率和滞销压力。
function buildInventoryDecisionAnalysisData(stockRowList = [], stockLogRowList = [], productRowList = []) {
  const productInfoMap = buildProductInfoMap(productRowList)
  const groupedProductMap = {}
  const currentDateStartValue = getDateStartValue(new Date())
  const recentThirtyDayBoundaryValue = new Date(currentDateStartValue.getTime() - 30 * 24 * 60 * 60 * 1000)

  for (const stockRow of stockRowList)
  {
    const productId = getFirstDefinedValue(stockRow, ["productId", "product_id"])
    if (productId === undefined || productId === null || productId === "")
    {
      continue
    }
    if (!groupedProductMap[productId])
    {
      groupedProductMap[productId] = {
        productId: productId,
        productName: getProductDisplayNameForDecision(productId, productInfoMap),
        categoryName: productInfoMap[productId]?.categoryName || "未分类",
        unitName: productInfoMap[productId]?.unitName || "件",
        costPrice: normalizeNumberValue(productInfoMap[productId]?.costPrice),
        quantity: 0,
        availableQuantity: 0,
        stockValueAmount: 0,
        availableStockValueAmount: 0,
        recentOutboundCostAmount: 0,
        latestOutboundTime: undefined,
        latestStockTime: undefined
      }
    }
    const quantityValue = normalizeNumberValue(getFirstDefinedValue(stockRow, ["quantity"]))
    const lockedQuantityValue = normalizeNumberValue(getFirstDefinedValue(stockRow, ["lockedQuantity", "locked_quantity"]))
    const frozenQuantityValue = normalizeNumberValue(getFirstDefinedValue(stockRow, ["frozenQuantity", "frozen_quantity"]))
    const availableQuantityValue = Math.max(quantityValue - lockedQuantityValue - frozenQuantityValue, 0)
    const costPriceValue = normalizeNumberValue(groupedProductMap[productId].costPrice)
    groupedProductMap[productId].quantity += quantityValue
    groupedProductMap[productId].availableQuantity += availableQuantityValue
    groupedProductMap[productId].stockValueAmount += quantityValue * costPriceValue
    groupedProductMap[productId].availableStockValueAmount += availableQuantityValue * costPriceValue
    const stockBusinessTime = getStockBusinessTime(stockRow)
    if (stockBusinessTime && (!groupedProductMap[productId].latestStockTime || stockBusinessTime > groupedProductMap[productId].latestStockTime))
    {
      groupedProductMap[productId].latestStockTime = stockBusinessTime
    }
  }

  for (const stockLogRow of stockLogRowList)
  {
    const productId = getFirstDefinedValue(stockLogRow, ["productId", "product_id"])
    if (productId === undefined || productId === null || productId === "")
    {
      continue
    }
    if (!groupedProductMap[productId])
    {
      groupedProductMap[productId] = {
        productId: productId,
        productName: getProductDisplayNameForDecision(productId, productInfoMap),
        categoryName: productInfoMap[productId]?.categoryName || "未分类",
        unitName: productInfoMap[productId]?.unitName || "件",
        costPrice: normalizeNumberValue(productInfoMap[productId]?.costPrice),
        quantity: 0,
        availableQuantity: 0,
        stockValueAmount: 0,
        availableStockValueAmount: 0,
        recentOutboundCostAmount: 0,
        latestOutboundTime: undefined,
        latestStockTime: undefined
      }
    }
    const stockLogBusinessTime = getStockLogBusinessTime(stockLogRow)
    if (String(getFirstDefinedValue(stockLogRow, ["inOut", "in_out"])) !== "out" || !stockLogBusinessTime)
    {
      continue
    }
    const stockLogAmountValue = normalizeNumberValue(getFirstDefinedValue(stockLogRow, ["amount"]))
    const stockLogQuantityValue = normalizeNumberValue(getFirstDefinedValue(stockLogRow, ["quantity"]))
    const fallbackOutboundCostAmount = stockLogQuantityValue * normalizeNumberValue(groupedProductMap[productId].costPrice)
    if (!groupedProductMap[productId].latestOutboundTime || stockLogBusinessTime > groupedProductMap[productId].latestOutboundTime)
    {
      groupedProductMap[productId].latestOutboundTime = stockLogBusinessTime
    }
    if (stockLogBusinessTime >= recentThirtyDayBoundaryValue)
    {
      groupedProductMap[productId].recentOutboundCostAmount += stockLogAmountValue > 0 ? stockLogAmountValue : fallbackOutboundCostAmount
    }
  }

  const inventoryProductSummaryList = Object.values(groupedProductMap).map(groupedProduct => {
    const referenceDateValue = groupedProduct.latestOutboundTime || groupedProduct.latestStockTime
    const slowMovingDayCount = referenceDateValue
      ? Math.max(0, Math.floor((currentDateStartValue.getTime() - getDateStartValue(referenceDateValue).getTime()) / (24 * 60 * 60 * 1000)))
      : 999
    return {
      ...groupedProduct,
      slowMovingDayCount: slowMovingDayCount,
      slowMovingText: groupedProduct.latestOutboundTime
        ? `${slowMovingDayCount}天未出库`
        : "暂无出库记录"
    }
  })

  const totalStockValueAmount = inventoryProductSummaryList.reduce((previousAmountValue, inventoryProductSummary) => {
    return previousAmountValue + normalizeNumberValue(inventoryProductSummary.stockValueAmount)
  }, 0)
  const availableStockValueAmount = inventoryProductSummaryList.reduce((previousAmountValue, inventoryProductSummary) => {
    return previousAmountValue + normalizeNumberValue(inventoryProductSummary.availableStockValueAmount)
  }, 0)
  const recentOutboundCostAmount = inventoryProductSummaryList.reduce((previousAmountValue, inventoryProductSummary) => {
    return previousAmountValue + normalizeNumberValue(inventoryProductSummary.recentOutboundCostAmount)
  }, 0)
  const slowMovingProductList = inventoryProductSummaryList
    .filter(inventoryProductSummary => inventoryProductSummary.slowMovingDayCount >= 60 && normalizeNumberValue(inventoryProductSummary.stockValueAmount) > 0)
    .sort((leftInventoryProductSummary, rightInventoryProductSummary) => {
      if (normalizeCountValue(rightInventoryProductSummary.slowMovingDayCount) !== normalizeCountValue(leftInventoryProductSummary.slowMovingDayCount))
      {
        return normalizeCountValue(rightInventoryProductSummary.slowMovingDayCount) - normalizeCountValue(leftInventoryProductSummary.slowMovingDayCount)
      }
      return normalizeNumberValue(rightInventoryProductSummary.stockValueAmount) - normalizeNumberValue(leftInventoryProductSummary.stockValueAmount)
    })

  return {
    totalStockValueAmount: totalStockValueAmount,
    availableStockValueAmount: availableStockValueAmount,
    slowMovingStockValueAmount: slowMovingProductList.reduce((previousAmountValue, inventoryProductSummary) => {
      return previousAmountValue + normalizeNumberValue(inventoryProductSummary.stockValueAmount)
    }, 0),
    slowMovingProductCount: slowMovingProductList.length,
    recentOutboundCostAmount: recentOutboundCostAmount,
    inventoryTurnoverDays: recentOutboundCostAmount > 0 ? totalStockValueAmount / (recentOutboundCostAmount / 30) : 0,
    slowMovingProductList: slowMovingProductList.slice(0, 8)
  }
}

// 生成统一的经营贡献排行列表，便于品类、区域和销售员三个维度复用同一套表格展示。
function buildContributionRankingList(groupedContributionMap = {}) {
  return Object.values(groupedContributionMap).map(groupedContribution => {
    return {
      targetName: groupedContribution.targetName,
      saleAmount: groupedContribution.saleAmount,
      grossProfitAmount: groupedContribution.grossProfitAmount,
      grossMarginRate: calculateRateValue(groupedContribution.grossProfitAmount, groupedContribution.saleAmount),
      orderCount: groupedContribution.orderNoSet.size
    }
  }).sort((leftContribution, rightContribution) => {
    if (normalizeNumberValue(rightContribution.saleAmount) !== normalizeNumberValue(leftContribution.saleAmount))
    {
      return normalizeNumberValue(rightContribution.saleAmount) - normalizeNumberValue(leftContribution.saleAmount)
    }
    return normalizeNumberValue(rightContribution.grossProfitAmount) - normalizeNumberValue(leftContribution.grossProfitAmount)
  }).slice(0, 10)
}

// 根据销售报表构造品类、区域和销售员经营贡献排行，帮助管理层做资源配置决策。
function buildOrganizationContributionAnalysisData(saleReportRowList = [], saleOrderRowList = [], customerRowList = [], productRowList = []) {
  const saleOrderLookupMap = buildSaleOrderLookupMap(saleOrderRowList)
  const customerDetailMap = buildCustomerDetailMap(customerRowList)
  const productCostPriceMap = buildProductCostPriceMap(productRowList)
  const categoryContributionMap = {}
  const regionContributionMap = {}
  const salespersonContributionMap = {}

  for (const saleReportRow of saleReportRowList)
  {
    const orderNo = getFirstDefinedValue(saleReportRow, ["orderNo", "order_no"])
    const saleOrderRow = saleOrderLookupMap.orderNoMap[orderNo]
    const customerId = saleOrderRow?.customerId
    const categoryName = getFirstDefinedValue(saleReportRow, ["categoryName", "category_name"]) || "未分类"
    const regionName = extractRegionNameFromAddress(customerDetailMap[customerId]?.address)
    const salespersonName = getFirstDefinedValue(saleOrderRow, ["createBy", "create_by"]) || "未分配业务员"
    const saleAmountValue = normalizeNumberValue(getFirstDefinedValue(saleReportRow, ["amount"]))
    const quantityValue = normalizeNumberValue(getFirstDefinedValue(saleReportRow, ["quantity"]))
    const productCode = getFirstDefinedValue(saleReportRow, ["productCode", "product_code"])
    const grossProfitAmountValue = saleAmountValue - quantityValue * normalizeNumberValue(productCostPriceMap[productCode])

    if (!categoryContributionMap[categoryName])
    {
      categoryContributionMap[categoryName] = {
        targetName: categoryName,
        saleAmount: 0,
        grossProfitAmount: 0,
        orderNoSet: new Set()
      }
    }
    if (!regionContributionMap[regionName])
    {
      regionContributionMap[regionName] = {
        targetName: regionName,
        saleAmount: 0,
        grossProfitAmount: 0,
        orderNoSet: new Set()
      }
    }
    if (!salespersonContributionMap[salespersonName])
    {
      salespersonContributionMap[salespersonName] = {
        targetName: salespersonName,
        saleAmount: 0,
        grossProfitAmount: 0,
        orderNoSet: new Set()
      }
    }

    categoryContributionMap[categoryName].saleAmount += saleAmountValue
    categoryContributionMap[categoryName].grossProfitAmount += grossProfitAmountValue
    regionContributionMap[regionName].saleAmount += saleAmountValue
    regionContributionMap[regionName].grossProfitAmount += grossProfitAmountValue
    salespersonContributionMap[salespersonName].saleAmount += saleAmountValue
    salespersonContributionMap[salespersonName].grossProfitAmount += grossProfitAmountValue

    if (orderNo)
    {
      categoryContributionMap[categoryName].orderNoSet.add(orderNo)
      regionContributionMap[regionName].orderNoSet.add(orderNo)
      salespersonContributionMap[salespersonName].orderNoSet.add(orderNo)
    }
  }

  return {
    categoryContributionList: buildContributionRankingList(categoryContributionMap),
    regionContributionList: buildContributionRankingList(regionContributionMap),
    salespersonContributionList: buildContributionRankingList(salespersonContributionMap)
  }
}

// 根据本月销售、回款和付款流水生成预算执行概览，帮助预算模块同时展示实际值和净现金流。
function buildDecisionBudgetOverviewData(managementOverview, receiptRowList = [], paymentRowList = [], currentMonthRange) {
  let currentMonthCollectionAmount = 0
  let currentMonthPaymentAmount = 0

  for (const receiptRow of receiptRowList)
  {
    const receiptBusinessTime = getReceiptBusinessTime(receiptRow)
    if (!receiptBusinessTime || !isDateInMonthRange(receiptBusinessTime, currentMonthRange))
    {
      continue
    }
    currentMonthCollectionAmount += normalizeNumberValue(getFirstDefinedValue(receiptRow, ["amount"]))
  }

  for (const paymentRow of paymentRowList)
  {
    const paymentBusinessTime = getPaymentBusinessTime(paymentRow)
    if (!paymentBusinessTime || !isDateInMonthRange(paymentBusinessTime, currentMonthRange))
    {
      continue
    }
    currentMonthPaymentAmount += normalizeNumberValue(getFirstDefinedValue(paymentRow, ["amount"]))
  }

  return {
    currentMonthSaleAmount: normalizeNumberValue(managementOverview.currentMonthSaleAmount),
    currentMonthGrossProfitAmount: normalizeNumberValue(managementOverview.currentMonthGrossProfitAmount),
    currentMonthCollectionAmount: currentMonthCollectionAmount,
    currentMonthPurchaseAmount: normalizeNumberValue(managementOverview.currentMonthPurchaseAmount),
    currentMonthPaymentAmount: currentMonthPaymentAmount,
    currentMonthNetCashFlowAmount: currentMonthCollectionAmount - currentMonthPaymentAmount
  }
}

// 生成现金流情景模拟的 30 天基准数据，帮助管理层在模拟前先看当前窗口压力。
function buildCashScenarioBaseData(receivableRowList = [], payableRowList = [], receiptRowList = [], paymentRowList = []) {
  const currentDateStartValue = getDateStartValue(new Date())
  const futureThirtyDayBoundaryValue = new Date(currentDateStartValue.getTime() + 30 * 24 * 60 * 60 * 1000)
  const currentMonthRange = buildMonthRange(0)
  let baseInflowAmount = 0
  let baseOutflowAmount = 0
  let currentMonthCollectionAmount = 0
  let currentMonthPaymentAmount = 0

  for (const receivableRow of receivableRowList)
  {
    if (!isOpenReceivableRow(receivableRow))
    {
      continue
    }
    const dueDateValue = parseDateValue(getFirstDefinedValue(receivableRow, ["dueDate", "due_date"]))
    if (!dueDateValue || getDateStartValue(dueDateValue) <= futureThirtyDayBoundaryValue)
    {
      baseInflowAmount += getReceivableRemainAmountValue(receivableRow)
    }
  }

  for (const payableRow of payableRowList)
  {
    if (!isOpenPayableRow(payableRow))
    {
      continue
    }
    const dueDateValue = parseDateValue(getFirstDefinedValue(payableRow, ["dueDate", "due_date"]))
    if (!dueDateValue || getDateStartValue(dueDateValue) <= futureThirtyDayBoundaryValue)
    {
      baseOutflowAmount += getPayableRemainAmountValue(payableRow)
    }
  }

  for (const receiptRow of receiptRowList)
  {
    const receiptBusinessTime = getReceiptBusinessTime(receiptRow)
    if (receiptBusinessTime && isDateInMonthRange(receiptBusinessTime, currentMonthRange))
    {
      currentMonthCollectionAmount += normalizeNumberValue(getFirstDefinedValue(receiptRow, ["amount"]))
    }
  }

  for (const paymentRow of paymentRowList)
  {
    const paymentBusinessTime = getPaymentBusinessTime(paymentRow)
    if (paymentBusinessTime && isDateInMonthRange(paymentBusinessTime, currentMonthRange))
    {
      currentMonthPaymentAmount += normalizeNumberValue(getFirstDefinedValue(paymentRow, ["amount"]))
    }
  }

  return {
    forecastDays: 30,
    baseInflowAmount: baseInflowAmount,
    baseOutflowAmount: baseOutflowAmount,
    baseNetCashFlowAmount: baseInflowAmount - baseOutflowAmount,
    currentMonthCollectionAmount: currentMonthCollectionAmount,
    currentMonthPaymentAmount: currentMonthPaymentAmount
  }
}

// 根据当前经营指标生成管理层经营简报，帮助周会和月会快速形成结论。
function buildExecutiveBriefData(managementOverview, riskOverview, customerProfitAnalysis, inventoryDecisionAnalysis, decisionBudgetOverview) {
  const concentrationData = customerProfitAnalysis.concentration || {}
  const summaryText = `${buildSaleAmountSummaryText(managementOverview.currentMonthSaleAmount, managementOverview.previousMonthSaleAmount, managementOverview.saleGrowthRate)}，毛利率 ${formatRateValue(managementOverview.currentMonthGrossProfitRate)}，回款率 ${formatRateValue(managementOverview.currentMonthCollectionRate)}。`
  const highlightTextList = [
    `本月毛利额为 ￥${formatAmountValue(managementOverview.currentMonthGrossProfitAmount)}，采购额为 ￥${formatAmountValue(managementOverview.currentMonthPurchaseAmount)}。`,
    `前 3 大客户销售占比为 ${formatRateValue(concentrationData.top3ShareRate)}，高价值客户集中在 ${customerProfitAnalysis.topCustomerList?.[0]?.targetName || "暂无"}。`,
    `当前库存货值为 ￥${formatAmountValue(inventoryDecisionAnalysis.totalStockValueAmount)}，近 30 天出库成本为 ￥${formatAmountValue(inventoryDecisionAnalysis.recentOutboundCostAmount)}。`
  ]
  const riskTextList = [
    `逾期应收金额 ￥${formatAmountValue(riskOverview.overdueReceivableAmount)}，其中超 60 天逾期 ${normalizeCountValue(riskOverview.longOverdueReceivableCount)} 笔。`,
    `未来 ${riskOverview.payableRiskDays || 7} 天内到期应付 ￥${formatAmountValue(riskOverview.dueSoonPayableAmount)}，超时未审核单据 ${normalizeCountValue(riskOverview.pendingApprovalTimeoutCount)} 笔。`,
    `60 天以上滞销货值 ￥${formatAmountValue(inventoryDecisionAnalysis.slowMovingStockValueAmount)}，净现金流实际为 ￥${formatAmountValue(decisionBudgetOverview.currentMonthNetCashFlowAmount)}。`
  ]
  const actionTextList = [
    riskOverview.overdueReceivableAmount > 0 ? "优先清理逾期应收和高风险客户，必要时安排管理层直接跟进大额客户。" : "继续保持回款节奏，重点盯住新增应收变化。",
    concentrationData.dependencyRiskText === "高依赖" ? "客户集中度偏高，建议尽快推进第二梯队客户拓展，降低核心客户波动影响。" : "客户结构仍处于可控范围，可继续巩固高毛利客户关系。",
    inventoryDecisionAnalysis.slowMovingStockValueAmount > 0 ? "对滞销商品制定促销、调拨或采购收缩方案，减少库存占资。" : "库存周转暂时平稳，建议继续按动销节奏安排补货。"
  ]
  return {
    generatedAtText: parseTime(new Date(), "{y}-{m}-{d} {h}:{i}:{s}"),
    summaryText: summaryText,
    highlightTextList: highlightTextList,
    riskTextList: riskTextList,
    actionTextList: actionTextList
  }
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
      fetchPagedRowList(listSaleReport, {
        startDate: currentMonthRange.startDate,
        endDate: currentMonthRange.endDate
      }),
      fetchPagedRowList(listSaleOrder),
      fetchPagedRowList(listCustomer),
      fetchPagedRowList(listReceivable),
      fetchPagedRowList(listProduct),
      fetchPagedRowList(listStock),
      fetchPagedRowList(listStockLog),
      fetchPagedRowList(listReceipt),
      fetchPagedRowList(listPayment),
      fetchPagedRowList(listPayable)
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

// 返回预算配置元数据，统一预算读取和维护逻辑。
function getDecisionBudgetMeta(budgetKey) {
  return decisionBudgetMetaList.find(decisionBudgetMeta => decisionBudgetMeta.key === budgetKey)
}

// 返回指定预算项的完整配置详情，便于后续按配置编号执行更新。
function getDecisionBudgetConfigDetail(budgetKey) {
  return decisionBudgetConfigDetailMap.value[budgetKey] || {}
}

// 返回指定预算项的数值配置，缺失时回退为 undefined 方便区分未配置状态。
function getDecisionBudgetValue(budgetKey) {
  const configDetail = getDecisionBudgetConfigDetail(budgetKey)
  if (configDetail.configValue === undefined || configDetail.configValue === null || configDetail.configValue === "")
  {
    return undefined
  }
  return normalizeNumberValue(configDetail.configValue)
}

// 按预算类型格式化展示值，统一金额和百分比口径。
function formatDecisionBudgetDisplayValue(budgetKey, budgetValue) {
  const decisionBudgetMeta = getDecisionBudgetMeta(budgetKey)
  if (decisionBudgetMeta?.unitType === "rate")
  {
    return formatRateValue(budgetValue)
  }
  return `￥${formatAmountValue(budgetValue)}`
}

// 按当前月份进度预测月末完成值，帮助管理层提前判断预算达成情况。
function calculateDecisionBudgetForecastValue(actualValue, unitType, monthProgressRate) {
  if (unitType === "rate")
  {
    return normalizeNumberValue(actualValue)
  }
  const normalizedMonthProgressRate = normalizeNumberValue(monthProgressRate)
  if (normalizedMonthProgressRate <= 0)
  {
    return normalizeNumberValue(actualValue)
  }
  return normalizeNumberValue(actualValue) / normalizedMonthProgressRate
}

// 计算预算相对当前月份节奏的差距，帮助管理层识别预算偏差程度。
function calculateDecisionBudgetPaceGapValue(actualValue, budgetValue, monthProgressRate) {
  if (budgetValue === undefined)
  {
    return 0
  }
  return normalizeNumberValue(actualValue) - normalizeNumberValue(budgetValue) * normalizeNumberValue(monthProgressRate)
}

// 返回预算节奏标签类型，便于区分领先、接近和落后状态。
function getDecisionBudgetPaceTagType(paceGapValue, unitType) {
  if (unitType === "rate")
  {
    return normalizeNumberValue(paceGapValue) >= 0 ? "success" : "warning"
  }
  if (normalizeNumberValue(paceGapValue) >= 0)
  {
    return "success"
  }
  if (Math.abs(normalizeNumberValue(paceGapValue)) <= 10000)
  {
    return "warning"
  }
  return "danger"
}

// 返回预算节奏标签文案，帮助管理层快速读懂预算执行状态。
function buildDecisionBudgetPaceTagText(paceGapValue, unitType) {
  if (unitType === "rate")
  {
    return normalizeNumberValue(paceGapValue) >= 0 ? "达到预算" : "低于预算"
  }
  if (normalizeNumberValue(paceGapValue) >= 0)
  {
    return "领先预算"
  }
  if (Math.abs(normalizeNumberValue(paceGapValue)) <= 10000)
  {
    return "接近预算"
  }
  return "低于预算"
}

// 返回预算差异文案，帮助管理层快速看懂实际和预算之间的金额差距。
function buildDecisionBudgetDifferenceText(budgetKey, actualValue, budgetValue) {
  if (budgetValue === undefined)
  {
    return "尚未设置本月预算"
  }
  const differenceValue = normalizeNumberValue(actualValue) - normalizeNumberValue(budgetValue)
  if (differenceValue === 0)
  {
    return "当前已达到本月预算"
  }
  return differenceValue > 0
    ? `超预算 ￥${formatAmountValue(Math.abs(differenceValue))}`
    : `距预算 ￥${formatAmountValue(Math.abs(differenceValue))}`
}

// 返回预算预测文案，帮助管理层提前判断月末是否能够完成预算。
function buildDecisionBudgetForecastText(budgetKey, forecastValue, budgetValue) {
  if (budgetValue === undefined)
  {
    return "配置预算后可显示月末达成预测"
  }
  const differenceValue = normalizeNumberValue(forecastValue) - normalizeNumberValue(budgetValue)
  if (differenceValue >= 0)
  {
    return `按当前节奏预计月末可达 ${formatDecisionBudgetDisplayValue(budgetKey, forecastValue)}，有望超过预算。`
  }
  return `按当前节奏预计月末可达 ${formatDecisionBudgetDisplayValue(budgetKey, forecastValue)}，仍差 ￥${formatAmountValue(Math.abs(differenceValue))}。`
}

// 重置预算维护表单，避免多次打开弹窗残留旧输入值。
function resetDecisionBudgetForm() {
  decisionBudgetForm.saleBudgetAmount = undefined
  decisionBudgetForm.grossProfitBudgetAmount = undefined
  decisionBudgetForm.collectionBudgetAmount = undefined
  decisionBudgetForm.purchaseBudgetAmount = undefined
  decisionBudgetForm.netCashBudgetAmount = undefined
}

// 根据当前配置详情回填预算表单，确保维护弹窗展示最新预算值。
function syncDecisionBudgetFormFromConfigDetailMap() {
  for (const decisionBudgetMeta of decisionBudgetMetaList)
  {
    const budgetValue = getDecisionBudgetValue(decisionBudgetMeta.key)
    decisionBudgetForm[decisionBudgetMeta.key] = budgetValue === undefined ? undefined : budgetValue
  }
}

// 读取预算配置值，保证管理层能直接查看预算执行与预测。
async function loadDecisionBudgetConfig() {
  decisionBudgetLoading.value = true
  try
  {
    const settledResultList = await Promise.allSettled(
      decisionBudgetMetaList.map(decisionBudgetMeta => getConfigKey(decisionBudgetMeta.configKey))
    )
    const configDetailMap = {}
    decisionBudgetMetaList.forEach((decisionBudgetMeta, indexValue) => {
      const configResponse = getSettledResultValue(settledResultList[indexValue], {})
      const currentConfigDetail = getDecisionBudgetConfigDetail(decisionBudgetMeta.key)
      configDetailMap[decisionBudgetMeta.key] = {
        ...currentConfigDetail,
        configKey: decisionBudgetMeta.configKey,
        configName: decisionBudgetMeta.configName,
        configValue: configResponse?.data,
        configType: "N",
        remark: decisionBudgetMeta.remark
      }
    })
    decisionBudgetConfigDetailMap.value = configDetailMap
  }
  finally
  {
    decisionBudgetLoading.value = false
  }
}

// 读取预算配置完整详情，便于后续更新时复用已有配置编号。
async function loadDecisionBudgetConfigDetailMap() {
  const settledResultList = await Promise.allSettled(
    decisionBudgetMetaList.map(decisionBudgetMeta => listConfig({
      pageNum: 1,
      pageSize: 10,
      configKey: decisionBudgetMeta.configKey
    }))
  )
  const configDetailMap = {}
  decisionBudgetMetaList.forEach((decisionBudgetMeta, indexValue) => {
    const configResponse = getSettledResultValue(settledResultList[indexValue], {})
    const configRowList = configResponse?.rows || []
    const matchedConfig = configRowList.find(configRow => configRow.configKey === decisionBudgetMeta.configKey)
    configDetailMap[decisionBudgetMeta.key] = matchedConfig || {
      configKey: decisionBudgetMeta.configKey,
      configName: decisionBudgetMeta.configName,
      configValue: getDecisionBudgetConfigDetail(decisionBudgetMeta.key).configValue,
      configType: "N",
      remark: decisionBudgetMeta.remark
    }
  })
  decisionBudgetConfigDetailMap.value = configDetailMap
}

// 打开预算维护弹窗，并同步当前配置值，避免覆盖线上已有预算。
async function openDecisionBudgetDialog() {
  if (!canManageDashboardTarget.value)
  {
    return
  }
  decisionBudgetLoading.value = true
  resetDecisionBudgetForm()
  try
  {
    await loadDecisionBudgetConfigDetailMap()
    syncDecisionBudgetFormFromConfigDetailMap()
    decisionBudgetDialogOpen.value = true
    setTimeout(() => {
      decisionBudgetFormRef.value?.clearValidate()
    }, 0)
  }
  finally
  {
    decisionBudgetLoading.value = false
  }
}

// 提交预算配置，复用系统参数配置能力保存各项经营预算。
async function submitDecisionBudgetForm() {
  if (!decisionBudgetFormRef.value)
  {
    return
  }
  const validateResult = await decisionBudgetFormRef.value.validate().catch(() => false)
  if (!validateResult)
  {
    return
  }
  decisionBudgetLoading.value = true
  try
  {
    for (const decisionBudgetMeta of decisionBudgetMetaList)
    {
      const configValue = normalizeNumberValue(decisionBudgetForm[decisionBudgetMeta.key]).toFixed(2)
      const configDetail = getDecisionBudgetConfigDetail(decisionBudgetMeta.key)
      const submitData = {
        ...configDetail,
        configName: decisionBudgetMeta.configName,
        configKey: decisionBudgetMeta.configKey,
        configValue: configValue,
        configType: "N",
        remark: decisionBudgetMeta.remark
      }
      if (configDetail.configId)
      {
        await updateConfig(submitData)
        continue
      }
      await addConfig(submitData)
    }
    await loadDecisionBudgetConfig()
    decisionBudgetDialogOpen.value = false
    proxy.$modal.msgSuccess("经营预算保存成功")
  }
  finally
  {
    decisionBudgetLoading.value = false
  }
}

// 生成现金流情景模拟结果，帮助管理层评估不同决策对未来 30 天净现金流的影响。
function buildSimulatedCashScenarioData(baseScenarioData = {}, currentCashScenarioForm = {}) {
  const baseInflowAmount = normalizeNumberValue(baseScenarioData.baseInflowAmount)
  const baseOutflowAmount = normalizeNumberValue(baseScenarioData.baseOutflowAmount)
  const baseNetCashFlowAmount = normalizeNumberValue(baseScenarioData.baseNetCashFlowAmount)
  const delayedCollectionAmount = normalizeNumberValue(currentCashScenarioForm.delayedCollectionAmount)
  const acceleratedPaymentAmount = normalizeNumberValue(currentCashScenarioForm.acceleratedPaymentAmount)
  const additionalSaleCollectionAmount = normalizeNumberValue(currentCashScenarioForm.additionalSaleCollectionAmount)
  const additionalProcurementAmount = normalizeNumberValue(currentCashScenarioForm.additionalProcurementAmount)
  const temporaryFinancingAmount = normalizeNumberValue(currentCashScenarioForm.temporaryFinancingAmount)
  const simulatedInflowAmount = Math.max(baseInflowAmount - delayedCollectionAmount + additionalSaleCollectionAmount + temporaryFinancingAmount, 0)
  const simulatedOutflowAmount = Math.max(baseOutflowAmount + acceleratedPaymentAmount + additionalProcurementAmount, 0)
  const simulatedNetCashFlowAmount = simulatedInflowAmount - simulatedOutflowAmount
  const impactAmount = simulatedNetCashFlowAmount - baseNetCashFlowAmount
  let riskTagType = "warning"
  let riskTagText = "基本持平"
  if (simulatedNetCashFlowAmount > 0)
  {
    riskTagType = "success"
    riskTagText = "预计净流入"
  }
  else if (simulatedNetCashFlowAmount < 0)
  {
    riskTagType = "danger"
    riskTagText = "净流出压力"
  }
  return {
    baseInflowAmount: baseInflowAmount,
    baseOutflowAmount: baseOutflowAmount,
    baseNetCashFlowAmount: baseNetCashFlowAmount,
    simulatedInflowAmount: simulatedInflowAmount,
    simulatedOutflowAmount: simulatedOutflowAmount,
    simulatedNetCashFlowAmount: simulatedNetCashFlowAmount,
    impactAmount: impactAmount,
    riskTagType: riskTagType,
    riskTagText: riskTagText
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

// 打开经营简报弹窗，方便管理层在会议前集中阅读总结。
function openExecutiveBriefDialog() {
  executiveBriefDialogOpen.value = true
}

// 组装经营简报纯文本，便于管理层直接复制到周报或汇报材料。
function buildExecutiveBriefPlainText() {
  const executiveBriefTextLineList = [`经营简报生成时间：${executiveBriefData.value.generatedAtText || parseTime(new Date(), "{y}-{m}-{d} {h}:{i}:{s}")}`]
  for (const executiveBriefSection of executiveBriefSectionList.value)
  {
    if (!executiveBriefSection.textList?.length)
    {
      continue
    }
    executiveBriefTextLineList.push(``)
    executiveBriefTextLineList.push(`${executiveBriefSection.title}`)
    executiveBriefSection.textList.forEach(executiveBriefText => {
      executiveBriefTextLineList.push(`- ${executiveBriefText}`)
    })
  }
  return executiveBriefTextLineList.join("\n")
}

// 复制经营简报文本，方便管理层快速发送到周报、群消息或汇报材料。
function copyExecutiveBriefText() {
  const executiveBriefPlainText = buildExecutiveBriefPlainText()
  if (typeof navigator !== "undefined" && navigator.clipboard?.writeText)
  {
    navigator.clipboard.writeText(executiveBriefPlainText).then(() => {
      proxy.$modal.msgSuccess("经营简报已复制")
    }).catch(() => {
      proxy.$modal.msgWarning("当前浏览器不支持自动复制，请手动复制简报内容")
    })
    return
  }
  proxy.$modal.msgWarning("当前浏览器不支持自动复制，请手动复制简报内容")
}

// 生成默认年度预算空数据，便于页面首次加载和接口失败时仍能保持稳定结构。
function createAnnualBudgetPlanEmptyData(budgetYearValue = annualBudgetYear.value) {
  return {
    planId: undefined,
    budgetYear: budgetYearValue,
    planName: `${budgetYearValue}年度经营预算`,
    versionNo: 1,
    versionLabel: "v1",
    basePlanId: undefined,
    effectiveFlag: "n",
    adjustmentReason: "",
    planStatus: "draft",
    saleBudgetAmount: 0,
    grossProfitBudgetAmount: 0,
    collectionBudgetAmount: 0,
    purchaseBudgetAmount: 0,
    netCashBudgetAmount: 0,
    saleMonthlyPlanList: Array.from({ length: 12 }, () => 0),
    grossProfitMonthlyPlanList: Array.from({ length: 12 }, () => 0),
    collectionMonthlyPlanList: Array.from({ length: 12 }, () => 0),
    purchaseMonthlyPlanList: Array.from({ length: 12 }, () => 0),
    netCashMonthlyPlanList: Array.from({ length: 12 }, () => 0),
    submitBy: undefined,
    submitTime: undefined,
    approveBy: undefined,
    approveTime: undefined,
    approveRemark: undefined
  }
}

// 规范月度预算数组，保证前端表格始终固定展示 12 个月数据。
function normalizeAnnualBudgetMonthlyList(monthlyBudgetList = []) {
  const normalizedMonthlyBudgetList = []
  for (let monthIndexValue = 0; monthIndexValue < 12; monthIndexValue++)
  {
    normalizedMonthlyBudgetList.push(normalizeNumberValue(monthlyBudgetList?.[monthIndexValue]))
  }
  return normalizedMonthlyBudgetList
}

// 把年度预算数据同步到表单，便于管理层在弹窗里继续编辑当前方案。
function syncAnnualBudgetPlanFormFromData(annualBudgetPlanValue = {}) {
  const normalizedAnnualBudgetPlanValue = {
    ...createAnnualBudgetPlanEmptyData(annualBudgetPlanValue.budgetYear || annualBudgetYear.value),
    ...annualBudgetPlanValue
  }
  annualBudgetPlanForm.planId = normalizedAnnualBudgetPlanValue.planId
  annualBudgetPlanForm.budgetYear = normalizedAnnualBudgetPlanValue.budgetYear
  annualBudgetPlanForm.planName = normalizedAnnualBudgetPlanValue.planName
  annualBudgetPlanForm.versionLabel = normalizedAnnualBudgetPlanValue.versionLabel || `v${normalizedAnnualBudgetPlanValue.versionNo || 1}`
  annualBudgetPlanForm.adjustmentReason = normalizedAnnualBudgetPlanValue.adjustmentReason || ""
  annualBudgetPlanForm.effectiveFlag = normalizedAnnualBudgetPlanValue.effectiveFlag || "n"
  annualBudgetPlanForm.saleBudgetAmount = normalizeNumberValue(normalizedAnnualBudgetPlanValue.saleBudgetAmount)
  annualBudgetPlanForm.grossProfitBudgetAmount = normalizeNumberValue(normalizedAnnualBudgetPlanValue.grossProfitBudgetAmount)
  annualBudgetPlanForm.collectionBudgetAmount = normalizeNumberValue(normalizedAnnualBudgetPlanValue.collectionBudgetAmount)
  annualBudgetPlanForm.purchaseBudgetAmount = normalizeNumberValue(normalizedAnnualBudgetPlanValue.purchaseBudgetAmount)
  annualBudgetPlanForm.netCashBudgetAmount = normalizeNumberValue(normalizedAnnualBudgetPlanValue.netCashBudgetAmount)
  annualBudgetPlanForm.saleMonthlyPlanList = normalizeAnnualBudgetMonthlyList(normalizedAnnualBudgetPlanValue.saleMonthlyPlanList)
  annualBudgetPlanForm.grossProfitMonthlyPlanList = normalizeAnnualBudgetMonthlyList(normalizedAnnualBudgetPlanValue.grossProfitMonthlyPlanList)
  annualBudgetPlanForm.collectionMonthlyPlanList = normalizeAnnualBudgetMonthlyList(normalizedAnnualBudgetPlanValue.collectionMonthlyPlanList)
  annualBudgetPlanForm.purchaseMonthlyPlanList = normalizeAnnualBudgetMonthlyList(normalizedAnnualBudgetPlanValue.purchaseMonthlyPlanList)
  annualBudgetPlanForm.netCashMonthlyPlanList = normalizeAnnualBudgetMonthlyList(normalizedAnnualBudgetPlanValue.netCashMonthlyPlanList)
}

// 汇总年度预算表单的指定年度总量，避免月度微调后顶部总量不同步。
function syncAnnualBudgetPlanFormTotalAmount(fieldKey) {
  if (fieldKey === "saleBudgetAmount")
  {
    annualBudgetPlanForm.saleBudgetAmount = annualBudgetPlanForm.saleMonthlyPlanList.reduce((previousAmountValue, currentAmountValue) => previousAmountValue + normalizeNumberValue(currentAmountValue), 0)
    return
  }
  if (fieldKey === "grossProfitBudgetAmount")
  {
    annualBudgetPlanForm.grossProfitBudgetAmount = annualBudgetPlanForm.grossProfitMonthlyPlanList.reduce((previousAmountValue, currentAmountValue) => previousAmountValue + normalizeNumberValue(currentAmountValue), 0)
    return
  }
  if (fieldKey === "collectionBudgetAmount")
  {
    annualBudgetPlanForm.collectionBudgetAmount = annualBudgetPlanForm.collectionMonthlyPlanList.reduce((previousAmountValue, currentAmountValue) => previousAmountValue + normalizeNumberValue(currentAmountValue), 0)
    return
  }
  if (fieldKey === "purchaseBudgetAmount")
  {
    annualBudgetPlanForm.purchaseBudgetAmount = annualBudgetPlanForm.purchaseMonthlyPlanList.reduce((previousAmountValue, currentAmountValue) => previousAmountValue + normalizeNumberValue(currentAmountValue), 0)
    return
  }
  annualBudgetPlanForm.netCashBudgetAmount = annualBudgetPlanForm.netCashMonthlyPlanList.reduce((previousAmountValue, currentAmountValue) => previousAmountValue + normalizeNumberValue(currentAmountValue), 0)
}

// 按年度总量均分到每个月，方便管理层先快速铺底预算再做旺淡季调整。
function distributeAnnualBudgetPlanMetric(totalAmountFieldKey) {
  const totalAmountValue = normalizeNumberValue(annualBudgetPlanForm[totalAmountFieldKey])
  const averageAmountValue = totalAmountValue > 0 ? Number((totalAmountValue / 12).toFixed(2)) : 0
  const monthlyBudgetList = Array.from({ length: 12 }, () => averageAmountValue)
  if (totalAmountValue > 0)
  {
    const distributedAmountValue = monthlyBudgetList.slice(0, 11).reduce((previousAmountValue, currentAmountValue) => previousAmountValue + currentAmountValue, 0)
    monthlyBudgetList[11] = Number((totalAmountValue - distributedAmountValue).toFixed(2))
  }
  if (totalAmountFieldKey === "saleBudgetAmount")
  {
    annualBudgetPlanForm.saleMonthlyPlanList = monthlyBudgetList
    return
  }
  if (totalAmountFieldKey === "grossProfitBudgetAmount")
  {
    annualBudgetPlanForm.grossProfitMonthlyPlanList = monthlyBudgetList
    return
  }
  if (totalAmountFieldKey === "collectionBudgetAmount")
  {
    annualBudgetPlanForm.collectionMonthlyPlanList = monthlyBudgetList
    return
  }
  if (totalAmountFieldKey === "purchaseBudgetAmount")
  {
    annualBudgetPlanForm.purchaseMonthlyPlanList = monthlyBudgetList
    return
  }
  annualBudgetPlanForm.netCashMonthlyPlanList = monthlyBudgetList
}

// 按五个年度总量统一均分到月度，减少预算拆解的初始录入成本。
function distributeAllAnnualBudgetPlanMetrics() {
  distributeAnnualBudgetPlanMetric("saleBudgetAmount")
  distributeAnnualBudgetPlanMetric("grossProfitBudgetAmount")
  distributeAnnualBudgetPlanMetric("collectionBudgetAmount")
  distributeAnnualBudgetPlanMetric("purchaseBudgetAmount")
  distributeAnnualBudgetPlanMetric("netCashBudgetAmount")
}

// 组装年度预算计划提交参数，统一控制字段结构。
function buildAnnualBudgetPlanPayload() {
  return {
    planId: annualBudgetPlanForm.planId,
    budgetYear: annualBudgetPlanForm.budgetYear,
    planName: annualBudgetPlanForm.planName,
    versionLabel: annualBudgetPlanForm.versionLabel,
    adjustmentReason: annualBudgetPlanForm.adjustmentReason,
    effectiveFlag: annualBudgetPlanForm.effectiveFlag,
    saleBudgetAmount: annualBudgetPlanForm.saleBudgetAmount,
    grossProfitBudgetAmount: annualBudgetPlanForm.grossProfitBudgetAmount,
    collectionBudgetAmount: annualBudgetPlanForm.collectionBudgetAmount,
    purchaseBudgetAmount: annualBudgetPlanForm.purchaseBudgetAmount,
    netCashBudgetAmount: annualBudgetPlanForm.netCashBudgetAmount,
    saleMonthlyPlanList: normalizeAnnualBudgetMonthlyList(annualBudgetPlanForm.saleMonthlyPlanList),
    grossProfitMonthlyPlanList: normalizeAnnualBudgetMonthlyList(annualBudgetPlanForm.grossProfitMonthlyPlanList),
    collectionMonthlyPlanList: normalizeAnnualBudgetMonthlyList(annualBudgetPlanForm.collectionMonthlyPlanList),
    purchaseMonthlyPlanList: normalizeAnnualBudgetMonthlyList(annualBudgetPlanForm.purchaseMonthlyPlanList),
    netCashMonthlyPlanList: normalizeAnnualBudgetMonthlyList(annualBudgetPlanForm.netCashMonthlyPlanList)
  }
}

// 查询当前年度预算版本历史，帮助管理层比较不同调整版的审批与生效情况。
async function loadAnnualBudgetPlanHistoryList() {
  try
  {
    const response = await listDecisionBudgetPlanHistory(annualBudgetYear.value)
    annualBudgetPlanHistoryList.value = response.data || response || []
  }
  catch (error)
  {
    annualBudgetPlanHistoryList.value = []
  }
}

// 查询当前选中年度的预算计划，支撑二期年度预算拆解和审批区块。
async function loadAnnualBudgetPlanData() {
  annualBudgetPlanLoading.value = true
  try
  {
    const response = await getDecisionBudgetPlanCurrent(annualBudgetYear.value)
    const annualBudgetPlanValue = response.data || response || createAnnualBudgetPlanEmptyData(annualBudgetYear.value)
    annualBudgetPlanData.value = {
      ...createAnnualBudgetPlanEmptyData(annualBudgetYear.value),
      ...annualBudgetPlanValue,
      saleMonthlyPlanList: normalizeAnnualBudgetMonthlyList(annualBudgetPlanValue.saleMonthlyPlanList),
      grossProfitMonthlyPlanList: normalizeAnnualBudgetMonthlyList(annualBudgetPlanValue.grossProfitMonthlyPlanList),
      collectionMonthlyPlanList: normalizeAnnualBudgetMonthlyList(annualBudgetPlanValue.collectionMonthlyPlanList),
      purchaseMonthlyPlanList: normalizeAnnualBudgetMonthlyList(annualBudgetPlanValue.purchaseMonthlyPlanList),
      netCashMonthlyPlanList: normalizeAnnualBudgetMonthlyList(annualBudgetPlanValue.netCashMonthlyPlanList)
    }
  }
  catch (error)
  {
    annualBudgetPlanData.value = createAnnualBudgetPlanEmptyData(annualBudgetYear.value)
  }
  finally
  {
    annualBudgetPlanLoading.value = false
  }
  await loadAnnualBudgetPlanHistoryList()
}

// 打开年度预算维护弹窗，并回显当前年度预算计划。
function openAnnualBudgetPlanDialog() {
  annualBudgetPlanFormMode.value = "edit"
  annualBudgetPlanSourceId.value = annualBudgetPlanData.value.planId
  syncAnnualBudgetPlanFormFromData(annualBudgetPlanData.value)
  annualBudgetPlanDialogOpen.value = true
  setTimeout(() => {
    annualBudgetPlanFormRef.value?.clearValidate()
  }, 0)
}

// 基于当前已通过预算快速创建新版本，避免反复手工录入全年拆解数据。
function openAnnualBudgetPlanVersionDialog() {
  annualBudgetPlanFormMode.value = "createVersion"
  annualBudgetPlanSourceId.value = annualBudgetPlanData.value.planId
  syncAnnualBudgetPlanFormFromData({
    ...annualBudgetPlanData.value,
    planId: undefined,
    effectiveFlag: "n",
    adjustmentReason: "",
    versionLabel: `v${Number(annualBudgetPlanData.value.versionNo || 1) + 1}`,
    planStatus: "draft",
    submitBy: undefined,
    submitTime: undefined,
    approveBy: undefined,
    approveTime: undefined,
    approveRemark: undefined
  })
  annualBudgetPlanDialogOpen.value = true
  setTimeout(() => {
    annualBudgetPlanFormRef.value?.clearValidate()
  }, 0)
}

// 保存年度预算计划，支撑二期年度预算拆解和审批流程。
async function submitAnnualBudgetPlanForm() {
  if (!annualBudgetPlanFormRef.value)
  {
    return
  }
  const validateResult = await annualBudgetPlanFormRef.value.validate().catch(() => false)
  if (!validateResult)
  {
    return
  }
  annualBudgetPlanLoading.value = true
  try
  {
    const annualBudgetPlanPayload = buildAnnualBudgetPlanPayload()
    if (annualBudgetPlanFormMode.value === "createVersion" && annualBudgetPlanSourceId.value)
    {
      await createDecisionBudgetPlanVersion(annualBudgetPlanSourceId.value, annualBudgetPlanPayload)
    }
    else if (annualBudgetPlanPayload.planId)
    {
      await updateDecisionBudgetPlan(annualBudgetPlanPayload)
    }
    else
    {
      await addDecisionBudgetPlan(annualBudgetPlanPayload)
    }
    annualBudgetYear.value = annualBudgetPlanPayload.budgetYear
    await loadAnnualBudgetPlanData()
    annualBudgetPlanDialogOpen.value = false
    proxy.$modal.msgSuccess(annualBudgetPlanFormMode.value === "createVersion" ? "预算新版本已创建" : "年度预算计划保存成功")
  }
  finally
  {
    annualBudgetPlanLoading.value = false
  }
}

// 提交当前年度预算计划审批，推动预算从草稿进入正式审批流程。
async function handleAnnualBudgetPlanSubmit() {
  if (!annualBudgetPlanData.value.planId)
  {
    proxy.$modal.msgWarning("请先保存年度预算计划，再提交审批")
    return
  }
  annualBudgetPlanLoading.value = true
  try
  {
    await submitDecisionBudgetPlan(annualBudgetPlanData.value.planId)
    await loadAnnualBudgetPlanData()
    proxy.$modal.msgSuccess("年度预算计划已提交审批")
  }
  finally
  {
    annualBudgetPlanLoading.value = false
  }
}

// 打开年度预算审批弹窗，统一承接通过和驳回动作。
function openAnnualBudgetApprovalDialog(actionType) {
  annualBudgetApprovalActionType.value = actionType
  annualBudgetApprovalForm.approveRemark = ""
  annualBudgetApprovalDialogOpen.value = true
}

// 提交年度预算审批结果，并在通过后同步刷新一期月度预算显示。
async function submitAnnualBudgetApproval() {
  if (!annualBudgetPlanData.value.planId)
  {
    return
  }
  annualBudgetPlanLoading.value = true
  try
  {
    if (annualBudgetApprovalActionType.value === "approve")
    {
      await approveDecisionBudgetPlan(annualBudgetPlanData.value.planId, {
        approveRemark: annualBudgetApprovalForm.approveRemark
      })
    }
    else
    {
      await rejectDecisionBudgetPlan(annualBudgetPlanData.value.planId, {
        approveRemark: annualBudgetApprovalForm.approveRemark
      })
    }
    await loadAnnualBudgetPlanData()
    await loadDecisionBudgetConfig()
    annualBudgetApprovalDialogOpen.value = false
    proxy.$modal.msgSuccess(annualBudgetApprovalActionType.value === "approve" ? "预算计划已审批通过" : "预算计划已驳回")
  }
  finally
  {
    annualBudgetPlanLoading.value = false
  }
}

// 查询经营决议事项列表，方便管理层直接在看板查看闭环执行情况。
async function loadExecutiveActionItemList() {
  executiveActionItemLoading.value = true
  try
  {
    const response = await listExecutiveActionItem({
      pageNum: 1,
      pageSize: 8
    })
    executiveActionItemList.value = response.rows || []
  }
  catch (error)
  {
    executiveActionItemList.value = []
  }
  finally
  {
    executiveActionItemLoading.value = false
  }
}

// 重置经营决议事项表单，避免新增和编辑之间互相污染。
function resetExecutiveActionItemForm() {
  executiveActionItemForm.actionItemId = undefined
  executiveActionItemForm.briefId = undefined
  executiveActionItemForm.briefTitleSnapshot = ""
  executiveActionItemForm.actionTitle = ""
  executiveActionItemForm.ownerName = ""
  executiveActionItemForm.dueDate = undefined
  executiveActionItemForm.actionStatus = "todo"
  executiveActionItemForm.priorityLevel = "medium"
  executiveActionItemForm.progressRemark = ""
  executiveActionItemForm.remark = ""
  executiveActionItemFormRef.value?.clearValidate()
}

// 打开经营决议事项弹窗，支持新增和编辑两种场景。
async function openExecutiveActionItemDialog(executiveActionItemRow) {
  resetExecutiveActionItemForm()
  if (!executiveActionItemRow?.actionItemId)
  {
    executiveActionItemDialogTitle.value = "新增经营决议事项"
    executiveActionItemDialogOpen.value = true
    return
  }
  executiveActionItemLoading.value = true
  try
  {
    const response = await getExecutiveActionItem(executiveActionItemRow.actionItemId)
    const executiveActionItemData = response.data || response || {}
    executiveActionItemForm.actionItemId = executiveActionItemData.actionItemId
    executiveActionItemForm.briefId = executiveActionItemData.briefId
    executiveActionItemForm.briefTitleSnapshot = executiveActionItemData.briefTitleSnapshot || ""
    executiveActionItemForm.actionTitle = executiveActionItemData.actionTitle || ""
    executiveActionItemForm.ownerName = executiveActionItemData.ownerName || ""
    executiveActionItemForm.dueDate = executiveActionItemData.dueDate ? parseTime(executiveActionItemData.dueDate, "{y}-{m}-{d}") : undefined
    executiveActionItemForm.actionStatus = executiveActionItemData.actionStatus || "todo"
    executiveActionItemForm.priorityLevel = executiveActionItemData.priorityLevel || "medium"
    executiveActionItemForm.progressRemark = executiveActionItemData.progressRemark || ""
    executiveActionItemForm.remark = executiveActionItemData.remark || ""
    executiveActionItemDialogTitle.value = "编辑经营决议事项"
    executiveActionItemDialogOpen.value = true
  }
  finally
  {
    executiveActionItemLoading.value = false
  }
}

// 提交经营决议事项表单，把管理建议真正沉淀为可跟踪的执行事项。
async function submitExecutiveActionItemForm() {
  if (!executiveActionItemFormRef.value)
  {
    return
  }
  const validateResult = await executiveActionItemFormRef.value.validate().catch(() => false)
  if (!validateResult)
  {
    return
  }
  executiveActionItemLoading.value = true
  try
  {
    const executiveActionItemPayload = {
      actionItemId: executiveActionItemForm.actionItemId,
      briefId: executiveActionItemForm.briefId,
      briefTitleSnapshot: executiveActionItemForm.briefTitleSnapshot,
      actionTitle: executiveActionItemForm.actionTitle,
      ownerName: executiveActionItemForm.ownerName,
      dueDate: executiveActionItemForm.dueDate,
      actionStatus: executiveActionItemForm.actionStatus,
      priorityLevel: executiveActionItemForm.priorityLevel,
      progressRemark: executiveActionItemForm.progressRemark,
      remark: executiveActionItemForm.remark
    }
    if (executiveActionItemPayload.actionItemId)
    {
      await updateExecutiveActionItem(executiveActionItemPayload)
    }
    else
    {
      await addExecutiveActionItem(executiveActionItemPayload)
    }
    await loadExecutiveActionItemList()
    executiveActionItemDialogOpen.value = false
    proxy.$modal.msgSuccess(executiveActionItemPayload.actionItemId ? "经营决议事项已更新" : "经营决议事项已新增")
  }
  finally
  {
    executiveActionItemLoading.value = false
  }
}

// 快速将事项标记为已完成，帮助管理层在看板里直接关闭已落地动作。
async function handleExecutiveActionItemComplete(executiveActionItemRow) {
  executiveActionItemLoading.value = true
  try
  {
    await updateExecutiveActionItem({
      actionItemId: executiveActionItemRow.actionItemId,
      briefId: executiveActionItemRow.briefId,
      briefTitleSnapshot: executiveActionItemRow.briefTitleSnapshot,
      actionTitle: executiveActionItemRow.actionTitle,
      ownerName: executiveActionItemRow.ownerName,
      dueDate: executiveActionItemRow.dueDate ? parseTime(executiveActionItemRow.dueDate, "{y}-{m}-{d}") : undefined,
      actionStatus: "completed",
      priorityLevel: executiveActionItemRow.priorityLevel,
      progressRemark: executiveActionItemRow.progressRemark || "已按计划完成。",
      remark: executiveActionItemRow.remark
    })
    await loadExecutiveActionItemList()
    proxy.$modal.msgSuccess("经营决议事项已标记完成")
  }
  finally
  {
    executiveActionItemLoading.value = false
  }
}

// 删除经营决议事项，避免历史误录事项继续干扰管理层判断。
async function handleExecutiveActionItemRemove(executiveActionItemRow) {
  await proxy.$modal.confirm(`是否确认删除事项“${executiveActionItemRow.actionTitle}”？`)
  executiveActionItemLoading.value = true
  try
  {
    await delExecutiveActionItem(executiveActionItemRow.actionItemId)
    await loadExecutiveActionItemList()
    proxy.$modal.msgSuccess("经营决议事项已删除")
  }
  finally
  {
    executiveActionItemLoading.value = false
  }
}

// 返回经营决议事项状态文案。
function getExecutiveActionStatusLabel(actionStatus) {
  if (actionStatus === "in_progress")
  {
    return "执行中"
  }
  if (actionStatus === "completed")
  {
    return "已完成"
  }
  if (actionStatus === "canceled")
  {
    return "已取消"
  }
  return "待跟进"
}

// 返回经营决议事项状态标签类型。
function getExecutiveActionStatusTagType(actionStatus) {
  if (actionStatus === "in_progress")
  {
    return "warning"
  }
  if (actionStatus === "completed")
  {
    return "success"
  }
  if (actionStatus === "canceled")
  {
    return "info"
  }
  return "danger"
}

// 返回经营决议事项优先级文案。
function getExecutiveActionPriorityLabel(priorityLevel) {
  if (priorityLevel === "high")
  {
    return "高"
  }
  if (priorityLevel === "low")
  {
    return "低"
  }
  return "中"
}

// 返回经营决议事项优先级标签类型。
function getExecutiveActionPriorityTagType(priorityLevel) {
  if (priorityLevel === "high")
  {
    return "danger"
  }
  if (priorityLevel === "low")
  {
    return "info"
  }
  return "warning"
}

// 判断经营决议事项是否已经逾期，方便管理层快速识别需要优先催办的动作。
function isExecutiveActionItemOverdue(executiveActionItemRow) {
  if (!executiveActionItemRow?.dueDate)
  {
    return false
  }
  if (executiveActionItemRow.actionStatus === "completed" || executiveActionItemRow.actionStatus === "canceled")
  {
    return false
  }
  const dueDateTime = new Date(executiveActionItemRow.dueDate).getTime()
  const todayDateTime = new Date(parseTime(new Date(), "{y}-{m}-{d}")).getTime()
  return !Number.isNaN(dueDateTime) && dueDateTime < todayDateTime
}

// 查询经营简报归档列表，帮助管理层追踪历次经营结论沉淀。
async function loadExecutiveBriefRecordList() {
  executiveBriefRecordLoading.value = true
  try
  {
    const response = await listExecutiveBriefRecord({
      pageNum: 1,
      pageSize: 5
    })
    executiveBriefRecordList.value = response.rows || []
  }
  catch (error)
  {
    executiveBriefRecordList.value = []
  }
  finally
  {
    executiveBriefRecordLoading.value = false
  }
}

// 组装经营简报归档参数，便于把当前经营看板生成的简报沉淀为历史记录。
function buildExecutiveBriefRecordPayload() {
  return {
    briefTitle: `${parseTime(new Date(), "{y}-{m}-{d}")}经营简报`,
    briefDate: parseTime(new Date(), "{y}-{m}-{d} 00:00:00"),
    briefStatus: "archived",
    generatedTime: parseTime(new Date(), "{y}-{m}-{d} {h}:{i}:{s}"),
    summaryContent: executiveBriefData.value.summaryText,
    highlightTextList: executiveBriefData.value.highlightTextList || [],
    riskTextList: executiveBriefData.value.riskTextList || [],
    actionTextList: executiveBriefData.value.actionTextList || [],
    plainTextContent: buildExecutiveBriefPlainText(),
    sourceMode: "dashboard"
  }
}

// 把当前经营简报归档到后台，方便周会和月会后续追溯。
async function archiveCurrentExecutiveBrief() {
  if (!hasDecisionSupportData.value)
  {
    proxy.$modal.msgWarning("当前还没有可归档的经营简报")
    return
  }
  executiveBriefRecordLoading.value = true
  try
  {
    await addExecutiveBriefRecord(buildExecutiveBriefRecordPayload())
    await addOperationSnapshot(buildOperationSnapshotPayload("weekly"))
    await loadExecutiveBriefRecordList()
    await loadOperationSnapshotList()
    await loadExecutiveActionItemList()
    proxy.$modal.msgSuccess("经营简报与周经营快照已归档")
  }
  finally
  {
    executiveBriefRecordLoading.value = false
  }
}

// 打开经营简报归档详情，便于管理层回看历史会议结论。
async function openExecutiveBriefRecordPreview(executiveBriefRecordRow) {
  executiveBriefRecordLoading.value = true
  try
  {
    const response = await getExecutiveBriefRecord(executiveBriefRecordRow.briefId)
    executiveBriefRecordPreviewData.value = response.data || response || {}
    executiveBriefRecordDialogOpen.value = true
  }
  finally
  {
    executiveBriefRecordLoading.value = false
  }
}

// 统一读取经营快照类型文字，帮助管理层区分日报、周报和月报沉淀。
function getOperationSnapshotTypeLabel(snapshotType) {
  if (snapshotType === "daily")
  {
    return "日报"
  }
  if (snapshotType === "monthly")
  {
    return "月报"
  }
  return "周报"
}

// 统一读取经营快照类型标签样式。
function getOperationSnapshotTypeTagType(snapshotType) {
  if (snapshotType === "daily")
  {
    return "success"
  }
  if (snapshotType === "monthly")
  {
    return "warning"
  }
  return "primary"
}

// 查询经营快照归档列表，帮助管理层查看日报、周报和月报的沉淀轨迹。
async function loadOperationSnapshotList() {
  operationSnapshotLoading.value = true
  try
  {
    const response = await listOperationSnapshot({
      pageNum: 1,
      pageSize: 6
    })
    operationSnapshotList.value = response.rows || []
  }
  catch (error)
  {
    operationSnapshotList.value = []
  }
  finally
  {
    operationSnapshotLoading.value = false
  }
}

// 组装经营快照纯文本，方便管理层直接把阶段性经营结果同步到周会材料。
function buildOperationSnapshotPlainText(snapshotType) {
  const operationSnapshotTextLineList = [
    `快照类型：${getOperationSnapshotTypeLabel(snapshotType)}`,
    `快照日期：${parseTime(new Date(), "{y}-{m}-{d}")}`,
    `销售额：￥${formatAmountValue(managementOverviewData.value.currentMonthSaleAmount)}`,
    `毛利额：￥${formatAmountValue(managementOverviewData.value.currentMonthGrossProfitAmount)}`,
    `回款率：${formatPercentValue(managementOverviewData.value.currentMonthCollectionRate)}`,
    `逾期笔数：${dashboardData.value.overdueCount || 0}`,
    `库存预警：${dashboardData.value.stockWarningCount || 0}`,
    `经营待办：${pendingExecutiveActionItemCount.value}`
  ]
  if (executiveBriefData.value.summaryText)
  {
    operationSnapshotTextLineList.push("")
    operationSnapshotTextLineList.push("经营结论")
    operationSnapshotTextLineList.push(executiveBriefData.value.summaryText)
  }
  operationSnapshotTextLineList.push("")
  operationSnapshotTextLineList.push("经营简报摘录")
  operationSnapshotTextLineList.push(buildExecutiveBriefPlainText())
  return operationSnapshotTextLineList.join("\n")
}

// 组装经营快照参数，把当前驾驶舱关键指标沉淀为日报、周报或月报快照。
function buildOperationSnapshotPayload(snapshotType) {
  return {
    snapshotTitle: `${parseTime(new Date(), "{y}-{m}-{d}")}${getOperationSnapshotTypeLabel(snapshotType)}经营快照`,
    snapshotDate: parseTime(new Date(), "{y}-{m}-{d} 00:00:00"),
    snapshotType: snapshotType,
    saleAmount: managementOverviewData.value.currentMonthSaleAmount || 0,
    grossProfitAmount: managementOverviewData.value.currentMonthGrossProfitAmount || 0,
    collectionRate: managementOverviewData.value.currentMonthCollectionRate || 0,
    overdueCount: dashboardData.value.overdueCount || 0,
    stockWarningCount: dashboardData.value.stockWarningCount || 0,
    pendingActionCount: pendingExecutiveActionItemCount.value || 0,
    summaryContent: executiveBriefData.value.summaryText || "暂无经营结论",
    plainTextContent: buildOperationSnapshotPlainText(snapshotType),
    sourceMode: "dashboard"
  }
}

// 把当前经营结果归档为快照，支持管理层按日报、周报、月报节奏沉淀经营过程。
async function archiveOperationSnapshot(snapshotType) {
  if (!hasManagementCockpitData.value)
  {
    proxy.$modal.msgWarning("当前还没有可归档的经营快照")
    return
  }
  operationSnapshotLoading.value = true
  try
  {
    await addOperationSnapshot(buildOperationSnapshotPayload(snapshotType))
    await loadOperationSnapshotList()
    proxy.$modal.msgSuccess(`${getOperationSnapshotTypeLabel(snapshotType)}已归档`)
  }
  finally
  {
    operationSnapshotLoading.value = false
  }
}

// 打开经营快照详情，便于管理层回看当期核心经营指标和摘要。
async function openOperationSnapshotPreview(operationSnapshotRow) {
  operationSnapshotLoading.value = true
  try
  {
    const response = await getOperationSnapshot(operationSnapshotRow.snapshotId)
    operationSnapshotPreviewData.value = response.data || response || {}
    operationSnapshotDialogOpen.value = true
  }
  finally
  {
    operationSnapshotLoading.value = false
  }
}

// 读取经营目标配置值，确保管理层即使没有参数维护权限也能看到目标完成进度。
async function loadDashboardTargetConfig() {
  dashboardTargetLoading.value = true
  try
  {
    const settledResultList = await Promise.allSettled(
      dashboardTargetMetaList.map(dashboardTargetMeta => getConfigKey(dashboardTargetMeta.configKey))
    )
    const configDetailMap = {}
    dashboardTargetMetaList.forEach((dashboardTargetMeta, indexValue) => {
      const configResponse = getSettledResultValue(settledResultList[indexValue], {})
      const currentConfigDetail = getDashboardTargetConfigDetail(dashboardTargetMeta.key)
      configDetailMap[dashboardTargetMeta.key] = {
        ...currentConfigDetail,
        configKey: dashboardTargetMeta.configKey,
        configName: dashboardTargetMeta.configName,
        configValue: configResponse?.data,
        configType: "N",
        remark: dashboardTargetMeta.remark
      }
    })
    dashboardTargetConfigDetailMap.value = configDetailMap
  }
  finally
  {
    dashboardTargetLoading.value = false
  }
}

// 读取经营目标完整配置详情，便于后续按配置编号执行修改而不是重复新增。
async function loadDashboardTargetConfigDetailMap() {
  const settledResultList = await Promise.allSettled(
    dashboardTargetMetaList.map(dashboardTargetMeta => listConfig({
      pageNum: 1,
      pageSize: 10,
      configKey: dashboardTargetMeta.configKey
    }))
  )
  const configDetailMap = {}
  dashboardTargetMetaList.forEach((dashboardTargetMeta, indexValue) => {
    const configResponse = getSettledResultValue(settledResultList[indexValue], {})
    const configRowList = configResponse?.rows || []
    const matchedConfig = configRowList.find(configRow => configRow.configKey === dashboardTargetMeta.configKey)
    configDetailMap[dashboardTargetMeta.key] = matchedConfig || {
      configKey: dashboardTargetMeta.configKey,
      configName: dashboardTargetMeta.configName,
      configValue: getDashboardTargetConfigDetail(dashboardTargetMeta.key).configValue,
      configType: "N",
      remark: dashboardTargetMeta.remark
    }
  })
  dashboardTargetConfigDetailMap.value = configDetailMap
}

// 打开经营目标维护弹窗，并先同步当前系统参数配置，避免修改时覆盖旧值。
async function openDashboardTargetDialog() {
  if (!canManageDashboardTarget.value)
  {
    return
  }
  dashboardTargetLoading.value = true
  resetDashboardTargetForm()
  try
  {
    await loadDashboardTargetConfigDetailMap()
    syncDashboardTargetFormFromConfigDetailMap()
    dashboardTargetDialogOpen.value = true
    setTimeout(() => {
      dashboardTargetFormRef.value?.clearValidate()
    }, 0)
  }
  finally
  {
    dashboardTargetLoading.value = false
  }
}

// 提交经营目标配置，复用系统参数配置能力保存销售、毛利和回款率月度目标。
async function submitDashboardTargetForm() {
  if (!dashboardTargetFormRef.value)
  {
    return
  }
  const validateResult = await dashboardTargetFormRef.value.validate().catch(() => false)
  if (!validateResult)
  {
    return
  }
  dashboardTargetLoading.value = true
  try
  {
    for (const dashboardTargetMeta of dashboardTargetMetaList)
    {
      const configValue = normalizeNumberValue(dashboardTargetForm[dashboardTargetMeta.key]).toFixed(2)
      const configDetail = getDashboardTargetConfigDetail(dashboardTargetMeta.key)
      const submitData = {
        ...configDetail,
        configName: dashboardTargetMeta.configName,
        configKey: dashboardTargetMeta.configKey,
        configValue: configValue,
        configType: "N",
        remark: dashboardTargetMeta.remark
      }
      if (configDetail.configId)
      {
        await updateConfig(submitData)
        continue
      }
      await addConfig(submitData)
    }
    await loadDashboardTargetConfig()
    dashboardTargetDialogOpen.value = false
    proxy.$modal.msgSuccess("经营目标保存成功")
  }
  finally
  {
    dashboardTargetLoading.value = false
  }
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

// 返回消息类型中文标签，统一经营看板中的展示口径。
function getMessageTypeLabel(messageType) {
  if (messageType === "sale_order_audit")
  {
    return "销售审核"
  }
  if (messageType === "stock_warning")
  {
    return "库存预警"
  }
  if (messageType === "receivable")
  {
    return "应收到期"
  }
  if (messageType === "notice")
  {
    return "系统公告"
  }
  return messageType
}

// 判断当前消息是否支持直接跳转到业务处理页面。
function canDirectJumpMessage(messageItem) {
  if (messageItem.message_type === "sale_order_audit")
  {
    return messageItem.business_type === "sale_order" && !!messageItem.business_id
  }
  if (messageItem.message_type === "stock_warning")
  {
    return true
  }
  if (messageItem.message_type === "receivable")
  {
    return messageItem.business_type === "sale_order" && !!messageItem.business_id
  }
  return false
}

// 返回消息操作按钮文案，区分可处理消息和仅可查看的消息。
function getMessageActionLabel(messageItem) {
  if (!canDirectJumpMessage(messageItem))
  {
    return "查看详情"
  }
  return "去处理"
}

// 打开消息详情弹窗，保证系统公告和不可直跳的消息也能被查看。
function openMessagePreview(messageItem) {
  messagePreviewData.value = {
    ...(messageItem || {}),
    previewContent: buildDashboardMessagePreviewContent(messageItem)
  }
  messagePreviewOpen.value = true
}

// 处理消息中心的主操作，优先跳转可处理消息，其他消息进入详情查看。
function handleMessageAction(messageItem) {
  if (canDirectJumpMessage(messageItem))
  {
    handleMessageJump(messageItem)
    return
  }
  openMessagePreview(messageItem)
}

// 按消息类型跳转到对应业务页面，帮助用户直接进入待处理页面。
function handleMessageJump(messageItem) {
  if (!canDirectJumpMessage(messageItem))
  {
    openMessagePreview(messageItem)
    return
  }
  if (messageItem.message_type === "sale_order_audit")
  {
    router.push({
      path: businessRoutePathMap.saleOrder,
      query: {
        saleOrderId: messageItem.business_id
      }
    })
    return
  }
  if (messageItem.message_type === "stock_warning")
  {
    router.push({
      path: businessRoutePathMap.stock,
      query: {
        warningQuery: "1"
      }
    })
    return
  }
  router.push({
    path: businessRoutePathMap.receivable,
    query: {
      saleOrderId: messageItem.business_id
    }
  })
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

// 返回重点付款供应商的风险提示文案，帮助管理层区分已逾期和即将到期。
function getDueRiskText(dueInDays) {
  const normalizedDueInDays = normalizeCountValue(dueInDays)
  if (normalizedDueInDays < 0)
  {
    return `已逾期 ${Math.abs(normalizedDueInDays)} 天`
  }
  if (normalizedDueInDays === 0)
  {
    return "今日到期"
  }
  return `${normalizedDueInDays} 天后到期`
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

// 浏览器尺寸变化时同步调整图表大小，避免经营看板在缩放后布局错位。
function handleChartResize() {
  if (saleTrendChartInstance.value)
  {
    saleTrendChartInstance.value.resize()
  }
  if (topCustomerChartInstance.value)
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
  if (!saleTrendChartInstance.value)
  {
    saleTrendChartInstance.value = echarts.init(saleTrendRef.value, "macarons")
  }
  saleTrendChartInstance.value.setOption({
    tooltip: { trigger: "axis" },
    xAxis: {
      type: "category",
      data: data.saleTrend?.dateList || []
    },
    yAxis: {
      type: "value"
    },
    series: [
      {
        type: "line",
        smooth: true,
        data: data.saleTrend?.amountList || []
      }
    ]
  })

  if (!topCustomerChartInstance.value)
  {
    topCustomerChartInstance.value = echarts.init(topCustomerRef.value, "macarons")
  }
  topCustomerChartInstance.value.setOption({
    tooltip: { trigger: "axis" },
    xAxis: {
      type: "category",
      data: data.topCustomer?.customerList || []
    },
    yAxis: {
      type: "value"
    },
    series: [
      {
        type: "bar",
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
    dashboardData.value = response.data || response || {}
  }
  catch (error)
  {
    dashboardData.value = {}
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
    if (hasManagementCockpitData.value)
    {
      await loadDecisionSupportData()
    }
  }
  catch (error)
  {
    // 管理驾驶舱扩展数据异常时保留首页基础数据，避免顶部指标和消息中心被整体清空。
    console.error("经营看板扩展数据加载失败：", error)
  }
  finally
  {
    await nextTick()
    initCharts(dashboardData.value)
    managementCockpitLoading.value = false
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
})

onBeforeUnmount(() => {
  disposeCharts()
})
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

.stat-action {
  margin-top: 10px;
  color: #909399;
  font-size: 12px;
}

.dashboard-action-card {
  cursor: pointer;
}

.dashboard-action-card:hover .stat-action {
  color: #409eff;
}

.dashboard-section-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.dashboard-section-title-group {
  display: flex;
  align-items: center;
  gap: 8px;
}

.dashboard-section-subtitle {
  color: #909399;
  font-size: 12px;
}

.management-overview-card {
  min-height: 132px;
  padding: 16px;
  border: 1px solid #ebeef5;
  border-radius: 10px;
  background: linear-gradient(180deg, #ffffff 0%, #f8fbff 100%);
}

.management-overview-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  gap: 8px;
}

.management-overview-title {
  color: #606266;
  font-size: 14px;
}

.management-overview-value {
  margin-top: 14px;
  color: #303133;
  font-size: 28px;
  font-weight: 600;
}

.management-overview-desc {
  margin-top: 12px;
  color: #909399;
  font-size: 12px;
  line-height: 1.6;
}

.management-health-layout {
  display: grid;
  grid-template-columns: 260px minmax(0, 1fr);
  gap: 14px;
}

.management-health-summary {
  display: flex;
  flex-direction: column;
  justify-content: center;
  padding: 20px;
  border-radius: 12px;
  background: linear-gradient(180deg, #f8fbff 0%, #eef5ff 100%);
  border: 1px solid #d9ecff;
}

.management-health-score-label {
  color: #606266;
  font-size: 13px;
}

.management-health-score {
  margin-top: 10px;
  color: #303133;
  font-size: 48px;
  font-weight: 700;
  line-height: 1;
}

.management-health-summary-desc {
  margin-top: 14px;
  color: #606266;
  font-size: 13px;
  line-height: 1.8;
}

.management-health-summary-advice {
  margin-top: 12px;
  color: #303133;
  font-size: 13px;
  line-height: 1.8;
}

.management-health-summary-action {
  display: flex;
  flex-direction: column;
  align-items: flex-start;
  gap: 10px;
  margin-top: 18px;
}

.management-health-summary-action-label {
  color: #909399;
  font-size: 12px;
}

.management-health-dimension-list {
  display: grid;
  grid-template-columns: repeat(4, minmax(0, 1fr));
  gap: 12px;
}

.management-health-dimension-item {
  padding: 16px;
  border: 1px solid #ebeef5;
  border-radius: 10px;
  background: #fcfdff;
}

.management-health-dimension-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  gap: 8px;
}

.management-health-dimension-title {
  color: #606266;
  font-size: 13px;
}

.management-health-dimension-score {
  margin-top: 12px;
  color: #303133;
  font-size: 30px;
  font-weight: 700;
}

.management-health-dimension-desc {
  margin-top: 10px;
  color: #909399;
  font-size: 12px;
  line-height: 1.7;
}

.management-health-dimension-footer {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  gap: 12px;
  margin-top: 14px;
}

.management-health-dimension-advice {
  flex: 1;
  color: #909399;
  font-size: 12px;
  line-height: 1.7;
}

.management-insight-list {
  margin-top: 16px;
  padding: 14px 16px;
  border-radius: 10px;
  background: #f5f7fa;
}

.management-insight-item {
  color: #606266;
  line-height: 1.8;
}

.management-insight-item + .management-insight-item {
  margin-top: 6px;
}

.risk-overview-list {
  display: grid;
  grid-template-columns: repeat(1, minmax(0, 1fr));
  gap: 12px;
}

.risk-overview-item {
  padding: 14px 16px;
  border-left: 4px solid #409eff;
  border-radius: 8px;
  background: #f8fbff;
}

.risk-overview-title {
  color: #606266;
  font-size: 13px;
}

.risk-overview-value {
  margin-top: 8px;
  color: #303133;
  font-size: 24px;
  font-weight: 600;
}

.risk-overview-desc {
  margin-top: 8px;
  color: #909399;
  font-size: 12px;
  line-height: 1.6;
}

.risk-overview-action {
  margin-top: 8px;
}

.dashboard-target-list {
  display: grid;
  gap: 12px;
}

.dashboard-target-month-summary {
  margin-bottom: 14px;
  padding: 14px 16px;
  border-radius: 10px;
  background: #f5f7fa;
}

.dashboard-target-month-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  gap: 12px;
  margin-bottom: 10px;
  color: #606266;
  font-size: 13px;
}

.dashboard-target-item {
  padding: 16px;
  border: 1px solid #ebeef5;
  border-radius: 10px;
  background: #fcfdff;
}

.dashboard-target-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  gap: 10px;
}

.dashboard-target-title {
  color: #606266;
  font-size: 14px;
}

.dashboard-target-value-row {
  display: flex;
  justify-content: space-between;
  gap: 12px;
  margin-top: 12px;
  color: #303133;
  font-size: 13px;
}

.dashboard-target-desc {
  margin-top: 10px;
  color: #909399;
  font-size: 12px;
}

.dashboard-target-pace-row {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-top: 10px;
}

.dashboard-target-pace-text {
  color: #606266;
  font-size: 12px;
  line-height: 1.6;
}

.dashboard-target-forecast-text {
  margin-top: 8px;
  color: #909399;
  font-size: 12px;
  line-height: 1.7;
}

.cash-flow-window-list {
  display: grid;
  gap: 12px;
}

.cash-flow-window-item {
  padding: 16px;
  border: 1px solid #ebeef5;
  border-radius: 10px;
  background: linear-gradient(180deg, #ffffff 0%, #f7fbff 100%);
}

.cash-flow-window-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  gap: 10px;
}

.cash-flow-window-title {
  color: #606266;
  font-size: 14px;
}

.cash-flow-window-metrics {
  display: grid;
  grid-template-columns: repeat(3, minmax(0, 1fr));
  gap: 12px;
  margin-top: 14px;
}

.cash-flow-window-metric {
  padding: 12px;
  border-radius: 8px;
  background: #f5f7fa;
}

.cash-flow-window-label {
  color: #909399;
  font-size: 12px;
}

.cash-flow-window-value {
  margin-top: 6px;
  color: #303133;
  font-size: 18px;
  font-weight: 600;
}

.cash-flow-window-desc {
  margin-top: 12px;
  color: #909399;
  font-size: 12px;
  line-height: 1.6;
}

.management-todo-list {
  display: grid;
  gap: 12px;
}

.management-todo-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  gap: 16px;
  padding: 16px;
  border: 1px solid #ebeef5;
  border-radius: 10px;
  background: #fcfdff;
}

.management-todo-main {
  flex: 1;
  min-width: 0;
}

.management-todo-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  gap: 12px;
}

.management-todo-title-group {
  display: flex;
  align-items: center;
  gap: 8px;
  min-width: 0;
}

.management-todo-title {
  color: #303133;
  font-size: 14px;
  font-weight: 600;
}

.management-todo-metric {
  color: #303133;
  font-size: 18px;
  font-weight: 600;
  white-space: nowrap;
}

.management-todo-desc {
  margin-top: 10px;
  color: #606266;
  font-size: 13px;
  line-height: 1.7;
}

.management-todo-action {
  flex-shrink: 0;
}

.annual-budget-header-action {
  display: flex;
  align-items: center;
  gap: 8px;
}

.annual-budget-year-input {
  width: 120px;
}

.annual-budget-meta-row {
  display: grid;
  grid-template-columns: repeat(3, minmax(0, 1fr));
  gap: 12px;
  margin-bottom: 14px;
}

.annual-budget-meta-item {
  padding: 14px;
  border-radius: 10px;
  background: #f5f7fa;
}

.annual-budget-meta-item-wide {
  grid-column: span 3;
}

.annual-budget-meta-label {
  display: block;
  color: #909399;
  font-size: 12px;
}

.annual-budget-meta-value {
  display: block;
  margin-top: 8px;
  color: #303133;
  font-size: 13px;
  line-height: 1.7;
}

.annual-budget-summary-list {
  display: grid;
  grid-template-columns: repeat(5, minmax(0, 1fr));
  gap: 12px;
  margin-bottom: 14px;
}

.annual-budget-summary-item {
  padding: 14px;
  border-radius: 10px;
  background: linear-gradient(180deg, #ffffff 0%, #f8fbff 100%);
  border: 1px solid #ebeef5;
}

.annual-budget-summary-title {
  color: #606266;
  font-size: 13px;
}

.annual-budget-summary-value {
  margin-top: 10px;
  color: #303133;
  font-size: 22px;
  font-weight: 600;
}

.annual-budget-summary-desc {
  margin-top: 8px;
  color: #909399;
  font-size: 12px;
}

.annual-budget-history-title {
  margin: 16px 0 12px;
  color: #303133;
  font-size: 14px;
  font-weight: 600;
}

.annual-budget-action-row {
  display: flex;
  align-items: center;
  gap: 10px;
  flex-wrap: wrap;
  margin-bottom: 14px;
}

.annual-budget-remark-text {
  color: #909399;
  font-size: 12px;
  line-height: 1.7;
}

.annual-budget-form-action-row {
  display: flex;
  justify-content: space-between;
  align-items: center;
  gap: 12px;
  margin-bottom: 14px;
  padding: 12px 14px;
  border-radius: 10px;
  background: #f5f7fa;
}

.annual-budget-form-action-text {
  color: #606266;
  font-size: 12px;
}

.decision-budget-list {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 12px;
}

.decision-budget-item {
  padding: 16px;
  border: 1px solid #ebeef5;
  border-radius: 10px;
  background: linear-gradient(180deg, #ffffff 0%, #f8fbff 100%);
}

.decision-budget-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  gap: 10px;
}

.decision-budget-title {
  color: #606266;
  font-size: 14px;
}

.decision-budget-value-row {
  display: flex;
  justify-content: space-between;
  gap: 12px;
  margin-top: 12px;
  color: #303133;
  font-size: 13px;
}

.decision-budget-desc {
  margin-top: 10px;
  color: #909399;
  font-size: 12px;
  line-height: 1.7;
}

.decision-budget-forecast-row {
  display: flex;
  justify-content: space-between;
  gap: 12px;
  margin-top: 10px;
  color: #606266;
  font-size: 12px;
}

.decision-budget-forecast-text {
  margin-top: 8px;
  color: #909399;
  font-size: 12px;
  line-height: 1.7;
}

.decision-budget-insight-list {
  margin-top: 14px;
  padding: 14px 16px;
  border-radius: 10px;
  background: #f5f7fa;
}

.decision-budget-insight-item {
  color: #606266;
  font-size: 13px;
  line-height: 1.8;
}

.decision-budget-insight-item + .decision-budget-insight-item {
  margin-top: 6px;
}

.cash-scenario-summary-list {
  display: grid;
  grid-template-columns: repeat(3, minmax(0, 1fr));
  gap: 12px;
}

.cash-scenario-summary-item {
  padding: 14px;
  border-radius: 10px;
  background: linear-gradient(180deg, #ffffff 0%, #f7fbff 100%);
  border: 1px solid #ebeef5;
}

.cash-scenario-summary-title {
  color: #606266;
  font-size: 13px;
}

.cash-scenario-summary-value {
  margin-top: 10px;
  color: #303133;
  font-size: 22px;
  font-weight: 600;
}

.cash-scenario-summary-desc {
  margin-top: 10px;
  color: #909399;
  font-size: 12px;
  line-height: 1.6;
}

.cash-scenario-form {
  margin-top: 16px;
  padding: 16px 16px 4px;
  border-radius: 10px;
  background: #fcfdff;
  border: 1px solid #ebeef5;
}

.cash-scenario-result-list {
  display: grid;
  grid-template-columns: repeat(3, minmax(0, 1fr));
  gap: 12px;
  margin-top: 16px;
}

.cash-scenario-result-item {
  padding: 14px;
  border-radius: 10px;
  background: #f5f7fa;
}

.cash-scenario-result-title {
  color: #606266;
  font-size: 13px;
}

.cash-scenario-result-value {
  margin-top: 10px;
  color: #303133;
  font-size: 20px;
  font-weight: 600;
}

.cash-scenario-impact-text {
  margin-top: 14px;
  color: #606266;
  font-size: 13px;
  line-height: 1.8;
}

.decision-positive-text {
  color: #67c23a;
  font-weight: 600;
}

.decision-negative-text {
  color: #f56c6c;
  font-weight: 600;
}

.customer-concentration-list {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 12px;
  margin-bottom: 14px;
}

.customer-concentration-item {
  padding: 14px;
  border-radius: 10px;
  background: #fcfdff;
  border: 1px solid #ebeef5;
}

.customer-concentration-title {
  color: #606266;
  font-size: 13px;
}

.customer-concentration-value {
  margin-top: 10px;
  color: #303133;
  font-size: 24px;
  font-weight: 600;
}

.customer-concentration-desc {
  margin-top: 10px;
  color: #909399;
  font-size: 12px;
  line-height: 1.6;
}

.inventory-decision-card-list {
  display: grid;
  grid-template-columns: repeat(3, minmax(0, 1fr));
  gap: 12px;
  margin-bottom: 14px;
}

.inventory-decision-card {
  padding: 14px;
  border-radius: 10px;
  background: #fcfdff;
  border: 1px solid #ebeef5;
}

.inventory-decision-title {
  color: #606266;
  font-size: 13px;
}

.inventory-decision-value {
  margin-top: 10px;
  color: #303133;
  font-size: 24px;
  font-weight: 600;
}

.inventory-decision-desc {
  margin-top: 10px;
  color: #909399;
  font-size: 12px;
  line-height: 1.6;
}

.executive-brief-meta,
.executive-brief-dialog-meta {
  margin-bottom: 12px;
  color: #909399;
  font-size: 12px;
}

.executive-brief-section {
  padding: 14px 16px;
  border-radius: 10px;
  background: #fcfdff;
  border: 1px solid #ebeef5;
}

.executive-brief-section + .executive-brief-section {
  margin-top: 12px;
}

.executive-brief-section-title {
  color: #303133;
  font-size: 14px;
  font-weight: 600;
}

.executive-brief-section-body {
  margin-top: 10px;
}

.executive-brief-item {
  color: #606266;
  font-size: 13px;
  line-height: 1.8;
}

.executive-brief-item + .executive-brief-item {
  margin-top: 6px;
}

.decision-support-muted {
  margin-top: 10px;
  color: #909399;
  font-size: 12px;
  line-height: 1.7;
}

.executive-brief-textarea {
  margin-top: 16px;
}

.executive-action-title {
  color: #303133;
  font-size: 13px;
  line-height: 1.7;
}

.executive-action-brief-title {
  margin-top: 4px;
  color: #909399;
  font-size: 12px;
}

.executive-action-overdue-text {
  color: #f56c6c;
  font-weight: 600;
}

.message-preview-content {
  max-height: 320px;
  overflow-y: auto;
  line-height: 1.8;
  word-break: break-word;
  white-space: pre-wrap;
}

@media (max-width: 1400px) {
  .management-health-layout {
    grid-template-columns: 1fr;
  }

  .management-health-dimension-list {
    grid-template-columns: repeat(2, minmax(0, 1fr));
  }

  .decision-budget-list,
  .customer-concentration-list,
  .annual-budget-summary-list,
  .annual-budget-meta-row {
    grid-template-columns: repeat(1, minmax(0, 1fr));
  }

  .annual-budget-meta-item-wide {
    grid-column: span 1;
  }

  .inventory-decision-card-list,
  .cash-scenario-summary-list,
  .cash-scenario-result-list {
    grid-template-columns: repeat(1, minmax(0, 1fr));
  }
}

@media (max-width: 992px) {
  .management-health-dimension-list {
    grid-template-columns: repeat(1, minmax(0, 1fr));
  }

  .management-health-dimension-footer {
    flex-direction: column;
  }

  .cash-flow-window-metrics {
    grid-template-columns: repeat(1, minmax(0, 1fr));
  }

  .management-todo-item {
    flex-direction: column;
    align-items: flex-start;
  }

  .management-todo-header {
    flex-direction: column;
    align-items: flex-start;
  }

  .dashboard-target-value-row {
    flex-direction: column;
  }

  .dashboard-target-month-header {
    flex-direction: column;
    align-items: flex-start;
  }

  .annual-budget-form-action-row,
  .annual-budget-header-action {
    flex-direction: column;
    align-items: flex-start;
  }

  .decision-budget-value-row,
  .decision-budget-forecast-row {
    flex-direction: column;
  }
}
</style>
