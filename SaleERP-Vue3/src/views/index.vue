<template>
  <div class="app-container home erp-page">
    <!-- Hero Command Banner -->
    <section class="home-hero">
      <div class="home-hero__main">
        <div class="home-hero__eyebrow">
          <span class="pulse-dot" />
          <span>SaleERP 智慧经销指挥台</span>
          <span class="home-hero__date">{{ todayText }}</span>
        </div>
        <h1 class="home-hero__title">{{ greetingText }}，欢迎回到经营首页</h1>
        <p class="home-hero__desc">聚合销售营收、应收回款、库存健康度与审批流转状态，一键直达高频核心业务。</p>

        <div class="home-hero__quick">
          <el-button type="primary" size="small" v-hasPermi="['business:saleOrder:add']" @click="navigateTo(businessRoutePathMap.saleOrder, { mode: 'create' })">
            <el-icon><ShoppingCart /></el-icon> 开销售单
          </el-button>
          <el-button plain size="small" v-hasPermi="['business:inbound:add']" @click="navigateTo(businessRoutePathMap.inbound, { mode: 'create' })">
            <el-icon><Goods /></el-icon> 采购入库
          </el-button>
          <el-button plain size="small" v-hasPermi="['business:inventoryCheck:add']" @click="navigateTo(businessRoutePathMap.inventoryCheck, { mode: 'create' })">
            <el-icon><Box /></el-icon> 盘点库存
          </el-button>
          <el-button plain size="small" v-hasPermi="['business:stock:list']" @click="navigateTo('/inventory/mobileWms')">
            <el-icon><Iphone /></el-icon> 移动PDA
          </el-button>
          <el-button plain size="small" v-hasPermi="['business:customer:add']" @click="navigateTo(businessRoutePathMap.customer, { mode: 'create' })">
            <el-icon><User /></el-icon> 新增客户
          </el-button>
        </div>
      </div>

      <div class="home-hero__summary">
        <div class="hero-pill" v-hasPermi="['business:todoCenter:list']" @click="navigateTo('/todoCenter')">
          <span class="hero-pill__label">待处理事项</span>
          <strong class="hero-pill__val erp-num" :class="{ 'is-urgent': pendingTaskCount > 0 }">
            {{ pendingTaskCount }}
          </strong>
          <span class="hero-pill__sub">进入待办中心 →</span>
        </div>
        <div class="hero-pill" v-if="canViewDashboard" v-hasPermi="['business:receivable:list']" @click="navigateTo(businessRoutePathMap.receivable)">
          <span class="hero-pill__label">累计应收回款率</span>
          <strong class="hero-pill__val erp-num is-indigo">{{ receivableRateText }}</strong>
          <span class="hero-pill__sub">查看回款台账 →</span>
        </div>
        <div class="hero-pill" v-if="canViewDashboard" @click="navigateTo('/finance/report')">
          <span class="hero-pill__label">经营驾驶舱</span>
          <strong class="hero-pill__val">实时同步</strong>
          <span class="hero-pill__sub">深度财务分析 →</span>
        </div>
      </div>
    </section>

    <!-- Primary Financial KPIs -->
    <erp-stat-strip v-if="canViewDashboard" :items="financialStatItems" :columns="4" @select="handleStatSelect" />

    <!-- Operational Pulse KPIs -->
    <erp-stat-strip v-if="canViewDashboard" :items="operationalStatItems" :columns="4" @select="handleStatSelect" />

    <!-- Action Workbench -->
    <section v-if="canViewDashboard" class="erp-section-card workbench-section">
      <div class="section-head">
        <div>
          <h2 class="section-title">今日待办优先队列</h2>
          <p class="section-subtitle">点击即刻直达对应业务列表并自动带入筛选条件</p>
        </div>
        <el-button link type="primary" @click="navigateTo('/todoCenter')">
          查看全部待办 →
        </el-button>
      </div>
      <div class="workbench-grid">
        <div
          v-for="item in pendingWorkbenchList"
          :key="item.key"
          class="workbench-tile"
          :class="`tone-${item.tone}`"
          @click="navigateTo(item.path, item.query)"
        >
          <div class="workbench-tile__body">
            <div class="workbench-tile__title">{{ item.title }}</div>
            <div class="workbench-tile__desc">{{ item.description }}</div>
          </div>
          <div class="workbench-tile__aside">
            <div class="workbench-tile__count erp-num">{{ item.count }}</div>
            <el-button size="small" :type="item.buttonType" plain @click.stop="navigateTo(item.path, item.query)">
              {{ item.buttonText }}
            </el-button>
          </div>
        </div>
      </div>
    </section>

    <!-- Charts Analytics Row -->
    <el-row v-if="canViewDashboard" :gutter="16">
      <el-col :xs="24" :lg="8">
        <el-card shadow="never" class="chart-card">
          <template #header>
            <div class="card-header">
              <span>销售趋势洞察</span>
              <el-tag size="small" type="primary" effect="plain">近期走势</el-tag>
            </div>
          </template>
          <div ref="saleTrendChartRef" class="chart-canvas" />
        </el-card>
      </el-col>
      <el-col :xs="24" :lg="8">
        <el-card shadow="never" class="chart-card">
          <template #header>
            <div class="card-header">
              <span>商品分类占比</span>
              <el-tag size="small" type="info" effect="plain">结构分布</el-tag>
            </div>
          </template>
          <div ref="productCategoryChartRef" class="chart-canvas" />
        </el-card>
      </el-col>
      <el-col :xs="24" :lg="8">
        <el-card shadow="never" class="chart-card">
          <template #header>
            <div class="card-header">
              <span>大客户排行 (Top 5)</span>
              <el-tag size="small" type="success" effect="plain">核心贡献</el-tag>
            </div>
          </template>
          <div ref="topCustomerChartRef" class="chart-canvas" />
        </el-card>
      </el-col>
    </el-row>

    <!-- Message Center -->
    <section class="erp-table-card">
      <div class="section-head">
        <div>
          <h2 class="section-title">消息与预警中心</h2>
          <p class="section-subtitle">支持直接跳转业务处理，系统公告可展开查看详情</p>
        </div>
        <el-button size="small" icon="Refresh" plain @click="fetchData">刷新数据</el-button>
      </div>
      <el-table :data="dashboardData.messageCenter" style="width: 100%" max-height="280">
        <el-table-column prop="message_type" label="类型" width="130" align="center">
          <template #default="scope">
            <span class="erp-code-chip">{{ getMessageTypeLabel(scope.row.message_type) }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="messageLevel" label="级别" width="96" align="center">
          <template #default="scope">
            <el-tag :type="scope.row.messageLevel === 'warning' ? 'danger' : 'info'" effect="light">
              {{ scope.row.messageLevel === 'warning' ? '预警' : '通知' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="message_title" label="消息内容" min-width="260" show-overflow-tooltip />
        <el-table-column prop="message_time" label="时间" width="180" align="center">
          <template #default="scope">
            <span class="erp-num">{{ parseTime(scope.row.message_time) }}</span>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="120" align="center" fixed="right">
          <template #default="scope">
            <el-button link type="primary" @click="handleMessageAction(scope.row)">
              {{ getMessageActionLabel(scope.row) }}
            </el-button>
          </template>
        </el-table-column>
      </el-table>
    </section>

    <el-dialog v-model="messagePreviewOpen" title="消息详情" width="680px" append-to-body>
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
import { getDashboard, getTodoCenter } from '@/api/business/report'
import * as echarts from 'echarts'
import { parseTime } from '@/utils/ruoyi'
import { checkPermi } from '@/utils/permission'
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

const greetingText = computed(() => {
  const hour = new Date().getHours()
  if (hour < 12) return '上午好'
  if (hour < 18) return '下午好'
  return '晚上好'
})

const todayText = computed(() => {
  return new Date().toLocaleDateString('zh-CN', {
    year: 'numeric',
    month: 'long',
    day: 'numeric',
    weekday: 'long'
  })
})

const canViewDashboard = computed(() => checkPermi(['business:report:view']))
const canViewTodoCenter = computed(() => checkPermi(['business:todoCenter:list']))
const todoCenterTotalCount = ref(null)

const pendingTaskCount = computed(() => {
  if (todoCenterTotalCount.value !== null) {
    return todoCenterTotalCount.value
  }
  return Number(dashboardData.value.pendingSaleOrderCount || 0)
    + Number(dashboardData.value.pendingInboundCount || 0)
    + Number(dashboardData.value.pendingOutboundCount || 0)
    + Number(dashboardData.value.stockWarningCount || 0)
    + Number(dashboardData.value.overdueCount || 0)
})

const receivableRateText = computed(() => {
  const totalReceivableAmount = Number(dashboardData.value.totalReceivableAmount || 0)
  const totalReceivedAmount = Number(dashboardData.value.totalReceivedAmount || 0)
  if (!totalReceivableAmount) {
    return '0%'
  }
  return `${Math.min(100, Math.round((totalReceivedAmount / totalReceivableAmount) * 100))}%`
})

function formatMoney(val) {
  const num = Number(val || 0)
  return Number.isFinite(num) ? num.toLocaleString('zh-CN', { minimumFractionDigits: 2, maximumFractionDigits: 2 }) : '0.00'
}

const financialStatItems = computed(() => [
  {
    key: 'sale',
    label: '累计净销售额',
    prefix: '¥',
    value: formatMoney(dashboardData.value.totalSaleAmount),
    hint: '已扣除审核退货 · 点击查看销售单',
    icon: 'Money',
    tone: 'indigo',
    clickable: true,
    path: businessRoutePathMap.saleOrder
  },
  {
    key: 'receivable',
    label: '累计应收总额',
    prefix: '¥',
    value: formatMoney(dashboardData.value.totalReceivableAmount),
    hint: '历史已入账累计应收 · 查看应收台账',
    icon: 'Wallet',
    tone: 'blue',
    clickable: true,
    path: businessRoutePathMap.receivable
  },
  {
    key: 'received',
    label: '累计已收总额',
    prefix: '¥',
    value: formatMoney(dashboardData.value.totalReceivedAmount),
    hint: `累计回款率 ${receivableRateText.value}`,
    icon: 'CreditCard',
    tone: 'emerald',
    clickable: true,
    path: businessRoutePathMap.receipt
  },
  {
    key: 'overdue',
    label: '逾期应收数',
    value: dashboardData.value.overdueCount || 0,
    suffix: '笔',
    hint: '优先跟进高风险回款',
    icon: 'Warning',
    tone: 'rose',
    clickable: true,
    path: businessRoutePathMap.receivable,
    query: { overdueQuery: '1' }
  }
])

const operationalStatItems = computed(() => [
  {
    key: 'stockWarning',
    label: '库存预警数',
    value: dashboardData.value.stockWarningCount || 0,
    suffix: '项',
    hint: '低于安全下限或高于上限',
    icon: 'Bell',
    tone: 'amber',
    clickable: true,
    path: businessRoutePathMap.stock,
    query: { warningQuery: '1' }
  },
  {
    key: 'pendingSale',
    label: '待审核销售单',
    value: dashboardData.value.pendingSaleOrderCount || 0,
    suffix: '单',
    hint: '待经理/财务审批流转',
    icon: 'Document',
    tone: 'indigo',
    clickable: true,
    path: businessRoutePathMap.saleOrder,
    query: { status: 'submitted' }
  },
  {
    key: 'pendingInbound',
    label: '待审核入库单',
    value: dashboardData.value.pendingInboundCount || 0,
    suffix: '单',
    hint: '审核后自动入库并生应付',
    icon: 'Download',
    tone: 'emerald',
    clickable: true,
    path: businessRoutePathMap.inbound,
    query: { status: 'submitted' }
  },
  {
    key: 'pendingOutbound',
    label: '待审核出库单',
    value: dashboardData.value.pendingOutboundCount || 0,
    suffix: '单',
    hint: '优先处理减少发货积压',
    icon: 'Upload',
    tone: 'blue',
    clickable: true,
    path: businessRoutePathMap.outbound,
    query: { status: 'submitted' }
  }
])

const pendingWorkbenchList = computed(() => [
  {
    key: 'pendingSaleOrder',
    title: '待审核销售单',
    description: '先完成审批锁库，再进入出库履约与回款结算',
    count: dashboardData.value.pendingSaleOrderCount || 0,
    buttonText: '去审核',
    buttonType: 'primary',
    tone: 'indigo',
    path: businessRoutePathMap.saleOrder,
    query: { status: 'submitted' }
  },
  {
    key: 'pendingInbound',
    title: '待审核采购入库',
    description: '审核后即时增加批次库存并同步生成供应商应付',
    count: dashboardData.value.pendingInboundCount || 0,
    buttonText: '去审核',
    buttonType: 'success',
    tone: 'emerald',
    path: businessRoutePathMap.inbound,
    query: { status: 'submitted' }
  },
  {
    key: 'pendingOutbound',
    title: '待审核销售出库',
    description: '优先处理待发单据，自动释放预占库存',
    count: dashboardData.value.pendingOutboundCount || 0,
    buttonText: '去处理',
    buttonType: 'primary',
    tone: 'blue',
    path: businessRoutePathMap.outbound,
    query: { status: 'submitted' }
  },
  {
    key: 'stockWarning',
    title: '库存预警监控',
    description: '及时发起补货或仓间调拨，保障接单履约',
    count: dashboardData.value.stockWarningCount || 0,
    buttonText: '去查看',
    buttonType: 'warning',
    tone: 'amber',
    path: businessRoutePathMap.stock,
    query: { warningQuery: '1' }
  },
  {
    key: 'overdueReceivable',
    title: '逾期应收催收',
    description: '聚焦超期账款，结合客户跟进降低坏账风险',
    count: dashboardData.value.overdueCount || 0,
    buttonText: '去催收',
    buttonType: 'danger',
    tone: 'rose',
    path: businessRoutePathMap.receivable,
    query: { overdueQuery: '1' }
  }
])

const saleTrendChartRef = ref(null)
const topCustomerChartRef = ref(null)
const productCategoryChartRef = ref(null)

let saleTrendChart = null
let topCustomerChart = null
let productCategoryChart = null

const initCharts = () => {
  if (saleTrendChartRef.value) saleTrendChart = echarts.init(saleTrendChartRef.value)
  if (topCustomerChartRef.value) topCustomerChart = echarts.init(topCustomerChartRef.value)
  if (productCategoryChartRef.value) productCategoryChart = echarts.init(productCategoryChartRef.value)
}

const getCssVar = (name, fallback) => {
  if (typeof window === 'undefined') return fallback
  return getComputedStyle(document.documentElement).getPropertyValue(name).trim() || fallback
}

const updateCharts = (saleTrend, topCustomer, productCategorySales) => {
  const isDark = settingsStore.isDark
  const primary = getCssVar('--erp-primary', '#4F46E5')
  const surface = getCssVar('--erp-surface', '#ffffff')
  const heading = getCssVar('--erp-heading', '#0f172a')
  const muted = getCssVar('--erp-text-muted', '#64748b')
  const border = getCssVar('--erp-border', '#e5eaf2')

  const tooltipStyle = {
    backgroundColor: isDark ? '#1e293b' : '#ffffff',
    borderColor: border,
    borderWidth: 1,
    textStyle: { color: heading, fontSize: 12 }
  }

  if (saleTrendChart && saleTrend) {
    saleTrendChart.setOption({
      tooltip: { trigger: 'axis', ...tooltipStyle },
      grid: { left: 12, right: 28, top: 24, bottom: 18, containLabel: true },
      xAxis: {
        type: 'category',
        boundaryGap: true,
        data: saleTrend.dateList || [],
        axisLabel: { color: muted, fontSize: 11, hideOverlap: true },
        axisLine: { lineStyle: { color: border } },
        axisTick: { show: false }
      },
      yAxis: {
        type: 'value',
        axisLabel: { color: muted, fontSize: 11 },
        splitLine: { lineStyle: { color: border, type: 'dashed' } }
      },
      series: [
        {
          name: '销售额',
          data: saleTrend.amountList || [],
          type: 'line',
          smooth: true,
          symbolSize: 7,
          lineStyle: { width: 2.5, color: primary },
          itemStyle: { color: primary },
          areaStyle: {
            color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
              { offset: 0, color: isDark ? 'rgba(99, 102, 241, 0.34)' : 'rgba(79, 70, 229, 0.22)' },
              { offset: 1, color: 'rgba(79, 70, 229, 0.01)' }
            ])
          }
        }
      ]
    })
  }

  if (topCustomerChart && topCustomer) {
    topCustomerChart.setOption({
      tooltip: { trigger: 'axis', ...tooltipStyle },
      grid: { left: 12, right: 16, top: 24, bottom: 20, containLabel: true },
      xAxis: {
        type: 'category',
        data: topCustomer.customerList || [],
        axisLabel: {
          interval: 0,
          rotate: 20,
          color: muted,
          fontSize: 11,
          formatter: (val) => (val && val.length > 7 ? `${val.slice(0, 7)}…` : val)
        },
        axisLine: { lineStyle: { color: border } },
        axisTick: { show: false }
      },
      yAxis: {
        type: 'value',
        axisLabel: { color: muted, fontSize: 11 },
        splitLine: { lineStyle: { color: border, type: 'dashed' } }
      },
      series: [
        {
          name: '成交额',
          data: topCustomer.amountList || [],
          type: 'bar',
          barWidth: '38%',
          itemStyle: {
            borderRadius: [8, 8, 2, 2],
            color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
              { offset: 0, color: '#6366f1' },
              { offset: 1, color: '#3b82f6' }
            ])
          }
        }
      ]
    })
  }

  if (productCategoryChart && productCategorySales) {
    productCategoryChart.setOption({
      color: ['#4F46E5', '#2563EB', '#0EA5E9', '#10B981', '#F59E0B', '#7C3AED', '#EC4899'],
      tooltip: {
        trigger: 'item',
        formatter: '{a}<br/>{b}: ¥{c} ({d}%)',
        ...tooltipStyle
      },
      legend: {
        orient: 'horizontal',
        bottom: 0,
        icon: 'circle',
        itemWidth: 8,
        itemHeight: 8,
        textStyle: { color: muted, fontSize: 12 }
      },
      series: [
        {
          name: '商品分类占比',
          type: 'pie',
          radius: ['46%', '72%'],
          center: ['50%', '44%'],
          avoidLabelOverlap: true,
          itemStyle: {
            borderRadius: 8,
            borderColor: surface,
            borderWidth: 2
          },
          label: { show: false, position: 'center' },
          emphasis: {
            label: {
              show: true,
              fontSize: 15,
              fontWeight: 'bold',
              color: heading
            }
          },
          labelLine: { show: false },
          data: productCategorySales
        }
      ]
    })
  }
}

const handleResize = () => {
  saleTrendChart?.resize()
  topCustomerChart?.resize()
  productCategoryChart?.resize()
}

const navigateTo = (path, query = {}) => {
  router.push({ path, query })
}

const handleStatSelect = (item) => {
  if (item?.path) {
    navigateTo(item.path, item.query || {})
  }
}

const fetchData = () => {
  if (canViewTodoCenter.value) {
    getTodoCenter().then(res => {
      const total = res?.data?.summary?.totalCount
      if (total !== undefined && total !== null) {
        todoCenterTotalCount.value = Number(total)
      }
    }).catch(() => {})
  }
  if (!canViewDashboard.value) {
    return
  }
  getDashboard().then(res => {
    dashboardData.value = normalizeDashboardSummaryData(res)
    updateCharts(dashboardData.value.saleTrend, dashboardData.value.topCustomer, dashboardData.value.productCategorySales)
  }).catch(() => {})
}

watch(() => [settingsStore.isDark, settingsStore.theme], () => {
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
  saleTrendChart?.dispose()
  topCustomerChart?.dispose()
  productCategoryChart?.dispose()
  saleTrendChart = null
  topCustomerChart = null
  productCategoryChart = null
})
</script>

<style scoped lang="scss">
.home {
  position: relative;
}

.home-hero {
  position: relative;
  display: flex;
  align-items: stretch;
  justify-content: space-between;
  gap: 24px;
  padding: 24px 26px;
  border-radius: var(--erp-radius-xl);
  border: 1px solid var(--erp-border);
  background:
    radial-gradient(circle at 92% 12%, rgba(var(--erp-primary-rgb), 0.14), transparent 36%),
    radial-gradient(circle at 70% 90%, rgba(14, 165, 233, 0.08), transparent 38%),
    var(--erp-surface);
  box-shadow: var(--erp-shadow-sm);
  overflow: hidden;
}

.home-hero__main {
  flex: 1;
  min-width: 0;
}

.home-hero__eyebrow {
  display: inline-flex;
  align-items: center;
  gap: 8px;
  font-size: 12px;
  font-weight: 600;
  color: var(--erp-primary);
}

.pulse-dot {
  width: 8px;
  height: 8px;
  border-radius: 50%;
  background: #10b981;
  box-shadow: 0 0 0 4px rgba(16, 185, 129, 0.18);
}

.home-hero__date {
  padding-left: 8px;
  margin-left: 2px;
  border-left: 1px solid var(--erp-border-strong);
  color: var(--erp-text-muted);
  font-weight: 500;
}

.home-hero__title {
  margin: 10px 0 6px;
  font-size: 24px;
  font-weight: 800;
  letter-spacing: -0.02em;
  color: var(--erp-heading);
}

.home-hero__desc {
  margin: 0;
  font-size: 13.5px;
  color: var(--erp-text-muted);
  max-width: 560px;
}

.home-hero__quick {
  display: flex;
  flex-wrap: wrap;
  gap: 6px;
  margin-top: 16px;

  :deep(.el-button) {
    margin-left: 0;
    padding: 6px 10px;
    font-size: 12.5px;
  }
}

.home-hero__summary {
  display: grid;
  grid-template-columns: repeat(3, minmax(106px, 1fr));
  gap: 10px;
  min-width: 400px;
  align-self: center;
}

.hero-pill {
  display: flex;
  flex-direction: column;
  justify-content: space-between;
  padding: 14px 16px;
  border-radius: 14px;
  background: var(--erp-surface-soft);
  border: 1px solid var(--erp-border);
  cursor: pointer;
  transition: transform 0.2s ease, border-color 0.2s ease, box-shadow 0.2s ease;

  &:hover {
    transform: translateY(-2px);
    border-color: rgba(var(--erp-primary-rgb), 0.45);
    box-shadow: var(--erp-shadow-sm);
  }
}

.hero-pill__label {
  font-size: 12px;
  font-weight: 500;
  color: var(--erp-text-muted);
}

.hero-pill__val {
  margin-top: 8px;
  font-size: 22px;
  font-weight: 800;
  color: var(--erp-heading);
  line-height: 1.2;

  &.is-urgent {
    color: #e11d48;
  }

  &.is-indigo {
    color: var(--erp-primary);
  }
}

.hero-pill__sub {
  margin-top: 8px;
  font-size: 11.5px;
  color: var(--erp-text-muted);
}

.section-head {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
  flex-wrap: wrap;
  margin-bottom: 14px;
}

.section-title {
  margin: 0;
  font-size: 16px;
  font-weight: 700;
  color: var(--erp-heading);
}

.section-subtitle {
  margin: 4px 0 0;
  font-size: 12.5px;
  color: var(--erp-text-muted);
}

.workbench-grid {
  display: grid;
  grid-template-columns: repeat(5, minmax(0, 1fr));
  gap: 12px;
}

.workbench-tile {
  display: flex;
  flex-direction: column;
  justify-content: space-between;
  gap: 14px;
  padding: 16px;
  border-radius: 14px;
  border: 1px solid var(--erp-border);
  background: var(--erp-surface-soft);
  cursor: pointer;
  position: relative;
  overflow: hidden;
  transition: transform 0.2s ease, border-color 0.2s ease, box-shadow 0.2s ease;

  &::before {
    content: '';
    position: absolute;
    left: 0;
    top: 0;
    right: 0;
    height: 3px;
    background: var(--erp-primary);
  }

  &.tone-emerald::before { background: #10b981; }
  &.tone-blue::before { background: #2563eb; }
  &.tone-amber::before { background: #f59e0b; }
  &.tone-rose::before { background: #e11d48; }

  &:hover {
    transform: translateY(-2px);
    background: var(--erp-surface);
    border-color: var(--erp-border-strong);
    box-shadow: var(--erp-shadow-md);
  }
}

.workbench-tile__title {
  font-size: 14.5px;
  font-weight: 700;
  color: var(--erp-heading);
}

.workbench-tile__desc {
  margin-top: 6px;
  font-size: 12px;
  line-height: 1.55;
  color: var(--erp-text-muted);
}

.workbench-tile__aside {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 8px;
}

.workbench-tile__count {
  font-size: 26px;
  font-weight: 800;
  line-height: 1;
  color: var(--erp-heading);
}

.chart-card {
  margin-bottom: 0;

  .card-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    font-weight: 700;
    font-size: 15px;
  }

  .chart-canvas {
    height: 288px;
    width: 100%;
  }
}

.message-preview-content {
  white-space: pre-wrap;
  line-height: 1.7;
  color: var(--erp-text);
}

@media (max-width: 1360px) {
  .workbench-grid {
    grid-template-columns: repeat(3, minmax(0, 1fr));
  }
}

@media (max-width: 1120px) {
  .home-hero {
    flex-direction: column;
  }
  .home-hero__summary {
    min-width: 0;
    width: 100%;
  }
}

@media (max-width: 768px) {
  .home-hero {
    padding: 18px;
  }
  .home-hero__summary {
    grid-template-columns: 1fr;
  }
  .workbench-grid {
    grid-template-columns: 1fr;
  }
}
</style>
