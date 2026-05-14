<template>
  <div class="app-container home">
    <section class="home-overview">
      <div class="home-overview__main">
        <div class="home-overview__eyebrow">销售进销存工作台</div>
        <h2>经营首页</h2>
        <p>聚合销售、库存、应收和审批状态，进入系统后先处理最需要关注的业务。</p>
      </div>
      <div class="home-overview__stats">
        <div class="home-stat">
          <span>当前日期</span>
          <strong>{{ todayText }}</strong>
        </div>
        <div class="home-stat">
          <span>待处理事项</span>
          <strong>{{ pendingTaskCount }}</strong>
        </div>
        <div class="home-stat">
          <span>应收回款率</span>
          <strong>{{ receivableRateText }}</strong>
        </div>
      </div>
    </section>

    <!-- Quick Actions -->
    <el-row :gutter="16" class="panel-group">
      <el-col :span="24">
        <section class="quick-actions-card">
          <div class="quick-actions-main">
            <div class="quick-actions-title"><el-icon><Menu /></el-icon> 快捷操作</div>
            <div class="quick-actions-subtitle">常用业务入口集中处理，减少菜单来回切换。</div>
          </div>
          <div class="quick-actions-container">
            <el-button type="primary" @click="navigateTo(businessRoutePathMap.saleOrder, { mode: 'create' })">
              <el-icon><ShoppingCart /></el-icon> 开销售单
            </el-button>
            <el-button type="success" @click="navigateTo(businessRoutePathMap.inbound, { mode: 'create' })">
              <el-icon><Goods /></el-icon> 采购入库
            </el-button>
            <el-button type="warning" @click="navigateTo(businessRoutePathMap.inventoryCheck, { mode: 'create' })">
              <el-icon><Box /></el-icon> 盘点库存
            </el-button>
            <el-button type="info" @click="navigateTo(businessRoutePathMap.customer, { mode: 'create' })">
              <el-icon><User /></el-icon> 新增客户
            </el-button>
          </div>
        </section>
      </el-col>
    </el-row>

    <!-- Key Metrics -->
    <el-row :gutter="16" class="panel-group">
      <el-col :xs="24" :sm="12" :md="12" :lg="6">
        <el-card shadow="hover" class="metric-card bg-primary" @click="navigateTo(businessRoutePathMap.saleOrder)">
          <div class="metric-icon"><el-icon><Money /></el-icon></div>
          <div class="metric-content">
            <div class="metric-title">总销售额</div>
            <div class="metric-value">￥{{ dashboardData.totalSaleAmount }}</div>
          </div>
        </el-card>
      </el-col>
      <el-col :xs="24" :sm="12" :md="12" :lg="6">
        <el-card shadow="hover" class="metric-card bg-success" @click="navigateTo(businessRoutePathMap.receivable)">
          <div class="metric-icon"><el-icon><Wallet /></el-icon></div>
          <div class="metric-content">
            <div class="metric-title">应收总额</div>
            <div class="metric-value">￥{{ dashboardData.totalReceivableAmount }}</div>
          </div>
        </el-card>
      </el-col>
      <el-col :xs="24" :sm="12" :md="12" :lg="6">
        <el-card shadow="hover" class="metric-card bg-info" @click="navigateTo(businessRoutePathMap.receipt)">
          <div class="metric-icon"><el-icon><CreditCard /></el-icon></div>
          <div class="metric-content">
            <div class="metric-title">已收总额</div>
            <div class="metric-value">￥{{ dashboardData.totalReceivedAmount }}</div>
          </div>
        </el-card>
      </el-col>
      <el-col :xs="24" :sm="12" :md="12" :lg="6">
        <el-card shadow="hover" class="metric-card bg-danger" @click="navigateTo(businessRoutePathMap.receivable, { overdueQuery: '1' })">
          <div class="metric-icon"><el-icon><Warning /></el-icon></div>
          <div class="metric-content">
            <div class="metric-title">逾期应收数</div>
            <div class="metric-value">{{ dashboardData.overdueCount }}</div>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <!-- Operational Metrics -->
    <el-row :gutter="16" class="panel-group">
      <el-col :xs="24" :sm="12" :md="12" :lg="6">
        <el-card shadow="hover" class="metric-card op-warning" @click="navigateTo(businessRoutePathMap.stock, { warningQuery: '1' })">
          <div class="metric-icon"><el-icon><Bell /></el-icon></div>
          <div class="metric-content">
            <div class="metric-title">库存预警数</div>
            <div class="metric-value">{{ dashboardData.stockWarningCount }}</div>
          </div>
        </el-card>
      </el-col>
      <el-col :xs="24" :sm="12" :md="12" :lg="6">
        <el-card shadow="hover" class="metric-card op-primary" @click="navigateTo(businessRoutePathMap.saleOrder, { status: 'submitted' })">
          <div class="metric-icon"><el-icon><Document /></el-icon></div>
          <div class="metric-content">
            <div class="metric-title">待审核销售单</div>
            <div class="metric-value">{{ dashboardData.pendingSaleOrderCount }}</div>
          </div>
        </el-card>
      </el-col>
      <el-col :xs="24" :sm="12" :md="12" :lg="6">
        <el-card shadow="hover" class="metric-card op-success" @click="navigateTo(businessRoutePathMap.inbound, { status: 'submitted' })">
          <div class="metric-icon"><el-icon><Download /></el-icon></div>
          <div class="metric-content">
            <div class="metric-title">待审核入库单</div>
            <div class="metric-value">{{ dashboardData.pendingInboundCount }}</div>
          </div>
        </el-card>
      </el-col>
      <el-col :xs="24" :sm="12" :md="12" :lg="6">
        <el-card shadow="hover" class="metric-card op-info" @click="navigateTo(businessRoutePathMap.outbound, { status: 'submitted' })">
          <div class="metric-icon"><el-icon><Upload /></el-icon></div>
          <div class="metric-content">
            <div class="metric-title">待审核出库单</div>
            <div class="metric-value">{{ dashboardData.pendingOutboundCount }}</div>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <!-- Workbench -->
    <el-row :gutter="16" class="panel-group">
      <el-col :span="24">
        <el-card shadow="hover" class="workbench-card">
          <template #header>
            <div class="card-header">
              <span>今日待办</span>
              <span class="workbench-subtitle">点进对应页面就能直接处理，不需要再二次筛选</span>
            </div>
          </template>
          <el-row :gutter="16">
            <el-col :xs="24" :sm="12" :lg="8" v-for="workbenchItem in pendingWorkbenchList" :key="workbenchItem.key">
              <div class="workbench-item">
                <div class="workbench-item-main">
                  <div class="workbench-item-title">{{ workbenchItem.title }}</div>
                  <div class="workbench-item-desc">{{ workbenchItem.description }}</div>
                </div>
                <div class="workbench-item-side">
                  <div class="workbench-item-count" :class="`is-${workbenchItem.level}`">{{ workbenchItem.count }}</div>
                  <el-button size="small" :type="workbenchItem.buttonType" @click="navigateTo(workbenchItem.path, workbenchItem.query)">
                    {{ workbenchItem.buttonText }}
                  </el-button>
                </div>
              </div>
            </el-col>
          </el-row>
        </el-card>
      </el-col>
    </el-row>

    <!-- Charts -->
    <el-row :gutter="16" class="panel-group">
      <el-col :xs="24" :lg="8">
        <el-card shadow="hover">
          <template #header>
            <div class="card-header">
              <span>销售趋势</span>
            </div>
          </template>
          <div ref="saleTrendChartRef" style="height: 300px"></div>
        </el-card>
      </el-col>
      <el-col :xs="24" :lg="8">
        <el-card shadow="hover">
          <template #header>
            <div class="card-header">
              <span>商品分类占比</span>
            </div>
          </template>
          <div ref="productCategoryChartRef" style="height: 300px"></div>
        </el-card>
      </el-col>
      <el-col :xs="24" :lg="8">
        <el-card shadow="hover">
          <template #header>
            <div class="card-header">
              <span>大客户排行 (Top 5)</span>
            </div>
          </template>
          <div ref="topCustomerChartRef" style="height: 300px"></div>
        </el-card>
      </el-col>
    </el-row>

    <!-- Message Center -->
    <el-row :gutter="16" class="panel-group">
      <el-col :span="24">
        <el-card shadow="hover">
          <template #header>
            <div class="card-header">
              <span>消息中心</span>
              <span class="message-center-subtitle">支持直接跳去处理，系统公告也可以展开查看详情</span>
            </div>
          </template>
          <el-table :data="dashboardData.messageCenter" style="width: 100%" height="250">
            <el-table-column prop="message_type" label="类型" width="120">
              <template #default="scope">
                <span>{{ getMessageTypeLabel(scope.row.message_type) }}</span>
              </template>
            </el-table-column>
            <el-table-column prop="messageLevel" label="级别" width="100">
              <template #default="scope">
                <el-tag :type="scope.row.messageLevel === 'warning' ? 'danger' : 'info'">
                  {{ scope.row.messageLevel === 'warning' ? '警告' : '通知' }}
                </el-tag>
              </template>
            </el-table-column>
            <el-table-column prop="message_title" label="内容" />
            <el-table-column prop="message_time" label="时间" width="180">
              <template #default="scope">
                <span>{{ parseTime(scope.row.message_time) }}</span>
              </template>
            </el-table-column>
            <el-table-column label="操作" width="140" align="center">
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
  </div>
</template>

<script setup name="Index">
import { ref, computed, onMounted, nextTick, onBeforeUnmount, watch } from 'vue'
import { useRouter } from 'vue-router'
import { getDashboard } from '@/api/business/report'
import * as echarts from 'echarts'
import { parseTime } from '@/utils/ruoyi'
import { businessDashboardRoutePathMap, normalizeDashboardSummaryData } from '@/utils/businessDashboard'
import { useDashboardMessageCenter } from '@/composables/useDashboardMessageCenter'
import useSettingsStore from '@/store/modules/settings'

const router = useRouter()
const settingsStore = useSettingsStore()

const businessRoutePathMap = businessDashboardRoutePathMap

const {
  messagePreviewOpen,
  messagePreviewData,
  handleMessageJump,
  handleMessageAction,
  canDirectJumpMessage,
  getMessageActionLabel,
  getMessageTypeLabel
} = useDashboardMessageCenter()

const dashboardData = ref({
  totalSaleAmount: 0,
  totalReceivableAmount: 0,
  totalReceivedAmount: 0,
  overdueCount: 0,
  stockWarningCount: 0,
  pendingSaleOrderCount: 0,
  pendingInboundCount: 0,
  pendingOutboundCount: 0,
  messageCenter: []
})

// 生成首页当前日期文案，保持概览区只展示用户需要的日期信息。
const todayText = computed(() => {
  return new Date().toLocaleDateString('zh-CN', {
    year: 'numeric',
    month: 'long',
    day: 'numeric',
    weekday: 'long'
  })
})

// 汇总首页待处理业务数量，作为管理者进入系统后的第一优先级提示。
const pendingTaskCount = computed(() => {
  return Number(dashboardData.value.pendingSaleOrderCount || 0)
    + Number(dashboardData.value.pendingInboundCount || 0)
    + Number(dashboardData.value.pendingOutboundCount || 0)
    + Number(dashboardData.value.stockWarningCount || 0)
    + Number(dashboardData.value.overdueCount || 0)
})

// 计算应收回款率，避免无应收金额时出现异常百分比。
const receivableRateText = computed(() => {
  const totalReceivableAmount = Number(dashboardData.value.totalReceivableAmount || 0)
  const totalReceivedAmount = Number(dashboardData.value.totalReceivedAmount || 0)
  if (!totalReceivableAmount) {
    return '0%'
  }
  return `${Math.min(100, Math.round((totalReceivedAmount / totalReceivableAmount) * 100))}%`
})

const saleTrendChartRef = ref(null)
const topCustomerChartRef = ref(null)
const productCategoryChartRef = ref(null)

let saleTrendChart = null
let topCustomerChart = null
let productCategoryChart = null

const pendingWorkbenchList = computed(() => [
  {
    key: 'pendingSaleOrder',
    title: '待审核销售单',
    description: '先审核，再进入后续出库和收款流程',
    count: dashboardData.value.pendingSaleOrderCount || 0,
    buttonText: '去审核',
    buttonType: 'primary',
    level: 'primary',
    path: businessRoutePathMap.saleOrder,
    query: { status: 'submitted' }
  },
  {
    key: 'pendingInbound',
    title: '待审核采购入库',
    description: '审核后库存和应付才会进入最新状态',
    count: dashboardData.value.pendingInboundCount || 0,
    buttonText: '去审核',
    buttonType: 'success',
    level: 'success',
    path: businessRoutePathMap.inbound,
    query: { status: 'submitted' }
  },
  {
    key: 'pendingOutbound',
    title: '待审核销售出库',
    description: '优先处理待出库单，减少发货积压',
    count: dashboardData.value.pendingOutboundCount || 0,
    buttonText: '去处理',
    buttonType: 'info',
    level: 'info',
    path: businessRoutePathMap.outbound,
    query: { status: 'submitted' }
  },
  {
    key: 'stockWarning',
    title: '库存预警',
    description: '及时补货或调整库存，避免影响接单',
    count: dashboardData.value.stockWarningCount || 0,
    buttonText: '去查看',
    buttonType: 'warning',
    level: 'warning',
    path: businessRoutePathMap.stock,
    query: { warningQuery: '1' }
  },
  {
    key: 'overdueReceivable',
    title: '逾期应收',
    description: '优先跟进逾期单据，降低坏账风险',
    count: dashboardData.value.overdueCount || 0,
    buttonText: '去催收',
    buttonType: 'danger',
    level: 'danger',
    path: businessRoutePathMap.receivable,
    query: { overdueQuery: '1' }
  }
])

// 初始化首页图表实例，后续数据刷新时复用同一批图表对象。
const initCharts = () => {
  if (saleTrendChartRef.value) {
    saleTrendChart = echarts.init(saleTrendChartRef.value)
  }
  if (topCustomerChartRef.value) {
    topCustomerChart = echarts.init(topCustomerChartRef.value)
  }
  if (productCategoryChartRef.value) {
    productCategoryChart = echarts.init(productCategoryChartRef.value)
  }
}

// 读取当前主题变量，确保图表在深色和浅色模式下都保持可读。
const getCssVariableValue = (variableName, fallbackValue) => {
  if (typeof window === 'undefined') {
    return fallbackValue
  }
  const variableValue = getComputedStyle(document.documentElement).getPropertyValue(variableName).trim()
  return variableValue || fallbackValue
}

// 根据最新业务数据和当前主题色刷新首页图表。
const updateCharts = (saleTrend, topCustomer, productCategorySales) => {
  const themePrimaryColor = getCssVariableValue('--menu-active-text', '#1E6F7A')
  const panelBackgroundColor = getCssVariableValue('--app-panel-bg', '#ffffff')
  const textColor = getCssVariableValue('--app-text', '#25313b')
  const mutedTextColor = getCssVariableValue('--app-text-muted', '#667781')
  const borderColor = getCssVariableValue('--app-border', '#dce7ea')

  if (saleTrendChart && saleTrend) {
    saleTrendChart.setOption({
      tooltip: { trigger: 'axis' },
      xAxis: {
        type: 'category',
        data: saleTrend.dateList || [],
        axisLabel: { color: mutedTextColor },
        axisLine: { lineStyle: { color: borderColor } }
      },
      yAxis: {
        type: 'value',
        axisLabel: { color: mutedTextColor },
        splitLine: { lineStyle: { color: borderColor } }
      },
      series: [
        {
          data: saleTrend.amountList || [],
          type: 'line',
          smooth: true,
          areaStyle: {},
          itemStyle: { color: themePrimaryColor }
        }
      ]
    })
  }

  if (topCustomerChart && topCustomer) {
    topCustomerChart.setOption({
      tooltip: { trigger: 'axis' },
      xAxis: {
        type: 'category',
        data: topCustomer.customerList || [],
        axisLabel: { interval: 0, rotate: 30, color: mutedTextColor },
        axisLine: { lineStyle: { color: borderColor } }
      },
      yAxis: {
        type: 'value',
        axisLabel: { color: mutedTextColor },
        splitLine: { lineStyle: { color: borderColor } }
      },
      series: [
        {
          data: topCustomer.amountList || [],
          type: 'bar',
          barWidth: '40%',
          itemStyle: { color: '#67C23A' }
        }
      ]
    })
  }

  if (productCategoryChart && productCategorySales) {
    productCategoryChart.setOption({
      tooltip: {
        trigger: 'item',
        formatter: '{a} <br/>{b}: ￥{c} ({d}%)'
      },
      legend: {
        orient: 'horizontal',
        bottom: 'bottom',
        textStyle: { color: mutedTextColor }
      },
      series: [
        {
          name: '商品分类占比',
          type: 'pie',
          radius: ['40%', '70%'],
          avoidLabelOverlap: false,
          itemStyle: {
            borderRadius: 10,
            borderColor: panelBackgroundColor,
            borderWidth: 2
          },
          label: {
            show: false,
            position: 'center'
          },
          emphasis: {
            label: {
              show: true,
              fontSize: 16,
              fontWeight: 'bold',
              color: textColor
            }
          },
          labelLine: {
            show: false
          },
          data: productCategorySales
        }
      ]
    })
  }
}

// 响应页面尺寸变化，避免图表容器变化后出现挤压或空白。
const handleResize = () => {
  saleTrendChart?.resize()
  topCustomerChart?.resize()
  productCategoryChart?.resize()
}

// 统一首页入口跳转逻辑，保留查询参数用于默认筛选。
const navigateTo = (path, query = {}) => {
  router.push({ path, query })
}

const fetchData = () => {
  getDashboard().then(res => {
    dashboardData.value = normalizeDashboardSummaryData(res)
    updateCharts(dashboardData.value.saleTrend, dashboardData.value.topCustomer, dashboardData.value.productCategorySales)
  })
}

watch(() => settingsStore.isDark, () => {
  nextTick(() => {
    updateCharts(dashboardData.value.saleTrend, dashboardData.value.topCustomer, dashboardData.value.productCategorySales)
  })
})

onMounted(() => {
  nextTick(() => {
    initCharts()
    fetchData()
    window.addEventListener('resize', handleResize)
  })
})

onBeforeUnmount(() => {
  window.removeEventListener('resize', handleResize)
  if (saleTrendChart) {
    saleTrendChart.dispose()
    saleTrendChart = null
  }
  if (topCustomerChart) {
    topCustomerChart.dispose()
    topCustomerChart = null
  }
  if (productCategoryChart) {
    productCategoryChart.dispose()
    productCategoryChart = null
  }
})
</script>

<style scoped lang="scss">
.home {
  padding: 22px;
  min-height: calc(100vh - 92px);
  background:
    radial-gradient(circle at top right, rgba(42, 157, 143, 0.09), transparent 32%),
    var(--app-bg);

  .panel-group {
    margin-bottom: 16px;
  }

  .home-overview {
    display: flex;
    align-items: stretch;
    justify-content: space-between;
    gap: 18px;
    margin-bottom: 18px;
    padding: 24px;
    border: 1px solid var(--app-border);
    border-radius: 8px;
    background: linear-gradient(135deg, var(--app-panel-bg) 0%, var(--app-panel-bg-soft) 100%);
    box-shadow: 0 14px 34px rgba(22, 36, 43, 0.06);
  }

  .home-overview__main {
    min-width: 280px;

    h2 {
      margin: 8px 0 8px;
      color: var(--app-heading);
      font-size: 26px;
      font-weight: 700;
      line-height: 1.2;
    }

    p {
      max-width: 520px;
      margin: 0;
      color: var(--app-text-muted);
      font-size: 14px;
      line-height: 1.7;
    }
  }

  .home-overview__eyebrow {
    color: #1e6f7a;
    font-size: 13px;
    font-weight: 700;
  }

  .home-overview__stats {
    display: grid;
    grid-template-columns: repeat(3, minmax(120px, 1fr));
    gap: 12px;
    min-width: 420px;
  }

  .home-stat {
    display: flex;
    flex-direction: column;
    justify-content: center;
    min-height: 86px;
    padding: 14px 16px;
    border: 1px solid var(--app-accent-soft);
    border-radius: 8px;
    background: var(--app-panel-bg-muted);

    span {
      margin-bottom: 8px;
      color: var(--app-text-muted);
      font-size: 12px;
    }

    strong {
      color: var(--app-heading);
      font-size: 20px;
      line-height: 1.25;
    }
  }

  .quick-actions-card {
    display: flex;
    align-items: center;
    justify-content: space-between;
    gap: 18px;
    padding: 18px 20px;
    border: 1px solid var(--app-border);
    border-radius: 8px;
    background: var(--app-panel-bg);
    box-shadow: 0 8px 24px rgba(18, 35, 52, 0.04);

    .quick-actions-main {
      min-width: 190px;
    }

    .quick-actions-title {
      display: flex;
      align-items: center;
      gap: 6px;
      color: var(--app-heading);
      font-size: 16px;
      font-weight: 700;
    }

    .quick-actions-subtitle {
      margin-top: 6px;
      color: var(--app-text-muted);
      font-size: 13px;
    }

    .quick-actions-container {
      display: flex;
      flex-wrap: wrap;
      justify-content: flex-end;
      gap: 10px;

      :deep(.el-button) {
        margin-left: 0;
        border-radius: 6px;
        min-width: 112px;
        font-weight: 600;
        box-shadow: none;
      }
    }
  }

  .metric-card {
    border-radius: 8px;
    cursor: pointer;
    transition: transform 0.2s ease, box-shadow 0.2s ease;
    display: flex;
    align-items: center;
    min-height: 112px;
    margin-bottom: 16px;
    border: 1px solid var(--app-border);
    box-shadow: 0 8px 24px rgba(18, 35, 52, 0.04);

    &:hover {
      transform: translateY(-2px);
      box-shadow: 0 14px 28px rgba(18, 35, 52, 0.09);
    }

    :deep(.el-card__body) {
      display: flex;
      align-items: center;
      width: 100%;
      padding: 18px;
    }

    .metric-icon {
      flex: 0 0 auto;
      font-size: 34px;
      margin-right: 14px;
      padding: 12px;
      border-radius: 8px;
      color: #fff;
    }

    .metric-content {
      flex: 1;
      .metric-title {
        font-size: 14px;
        color: var(--app-text-muted);
        margin-bottom: 8px;
      }
      .metric-value {
        font-size: 23px;
        font-weight: 700;
        color: var(--app-heading);
        word-break: break-all;
      }
    }

    &.bg-primary .metric-icon { background: linear-gradient(135deg, #1E6F7A, #2a9d8f); }
    &.bg-success .metric-icon { background: linear-gradient(135deg, #67C23A, #85ce61); }
    &.bg-info .metric-icon { background: linear-gradient(135deg, #909399, #a6a9ad); }
    &.bg-danger .metric-icon { background: linear-gradient(135deg, #F56C6C, #f78989); }

    &.op-warning .metric-icon { color: #E6A23C; background: rgba(230, 162, 60, 0.1); }
    &.op-primary .metric-icon { color: #1E6F7A; background: rgba(30, 111, 122, 0.1); }
    &.op-success .metric-icon { color: #67C23A; background: rgba(103, 194, 58, 0.1); }
    &.op-info .metric-icon { color: #909399; background: rgba(144, 147, 153, 0.1); }
  }

  .card-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    font-weight: bold;
  }

  .workbench-card {
    border-radius: 8px;
    border: 1px solid var(--app-border);
    box-shadow: 0 8px 24px rgba(18, 35, 52, 0.04);

    .workbench-subtitle {
      color: var(--app-text-muted);
      font-size: 12px;
      font-weight: normal;
    }

    .workbench-item {
      display: flex;
      justify-content: space-between;
      align-items: center;
      min-height: 108px;
      padding: 18px 16px;
      margin-bottom: 16px;
      background: linear-gradient(135deg, var(--app-panel-bg), var(--app-panel-bg-soft));
      border: 1px solid var(--app-border);
      border-radius: 8px;
    }

    .workbench-item-title {
      color: var(--app-heading);
      font-size: 16px;
      font-weight: 600;
    }

    .workbench-item-desc {
      margin-top: 10px;
      color: var(--app-text-muted);
      font-size: 13px;
      line-height: 1.6;
    }

    .workbench-item-side {
      display: flex;
      flex-direction: column;
      align-items: flex-end;
      gap: 12px;
      min-width: 88px;
    }

    .workbench-item-count {
      font-size: 28px;
      font-weight: 700;
      line-height: 1;
    }

    .workbench-item-count.is-primary {
      color: #1e6f7a;
    }

    .workbench-item-count.is-success {
      color: #67c23a;
    }

    .workbench-item-count.is-info {
      color: var(--app-text-muted);
    }

    .workbench-item-count.is-warning {
      color: #e6a23c;
    }

    .workbench-item-count.is-danger {
      color: #f56c6c;
    }
  }

  .message-center-subtitle {
    color: var(--app-text-muted);
    font-size: 12px;
    font-weight: normal;
  }

  .message-preview-content {
    max-height: 320px;
    overflow-y: auto;
    line-height: 1.8;
    word-break: break-word;
    white-space: pre-wrap;
  }
}

@media (max-width: 768px) {
  .home {
    padding: 12px;

    .home-overview {
      flex-direction: column;
      padding: 18px;
    }

    .home-overview__stats {
      grid-template-columns: 1fr;
      min-width: 0;
    }

    .quick-actions-card {
      align-items: flex-start;
      flex-direction: column;
      padding: 16px;
    }

    .quick-actions-container {
      justify-content: flex-start;
      width: 100%;

      :deep(.el-button) {
        flex: 1 1 132px;
      }
    }

    .metric-card {
      min-height: 96px;

      .metric-icon {
        font-size: 28px;
      }

      .metric-content .metric-value {
        font-size: 20px;
      }
    }

    .card-header {
      align-items: flex-start;
      flex-direction: column;
      gap: 6px;
    }

    .workbench-card {
      .workbench-item {
        align-items: flex-start;
        flex-direction: column;
        min-height: auto;
      }

      .workbench-item-side {
        flex-direction: row;
        align-items: center;
        justify-content: space-between;
        width: 100%;
      }
    }
  }
}
</style>
