<template>
  <div class="app-container home">
    <!-- Quick Actions -->
    <el-row :gutter="20" class="panel-group">
      <el-col :span="24">
        <el-card shadow="hover" class="quick-actions-card">
          <div class="quick-actions-container">
            <span class="quick-actions-title"><el-icon><Menu /></el-icon> 快捷操作：</span>
            <el-button type="primary" plain @click="navigateTo(businessRoutePathMap.saleOrder)">
              <el-icon><ShoppingCart /></el-icon> 开销售单
            </el-button>
            <el-button type="success" plain @click="navigateTo(businessRoutePathMap.inbound)">
              <el-icon><Goods /></el-icon> 采购入库
            </el-button>
            <el-button type="warning" plain @click="navigateTo(businessRoutePathMap.inventoryCheck)">
              <el-icon><Box /></el-icon> 盘点库存
            </el-button>
            <el-button type="info" plain @click="navigateTo(businessRoutePathMap.customer)">
              <el-icon><User /></el-icon> 新增客户
            </el-button>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <!-- Key Metrics -->
    <el-row :gutter="20" class="panel-group">
      <el-col :span="6">
        <el-card shadow="hover" class="metric-card bg-primary" @click="navigateTo(businessRoutePathMap.saleOrder)">
          <div class="metric-icon"><el-icon><Money /></el-icon></div>
          <div class="metric-content">
            <div class="metric-title">总销售额</div>
            <div class="metric-value">￥{{ dashboardData.totalSaleAmount }}</div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card shadow="hover" class="metric-card bg-success" @click="navigateTo(businessRoutePathMap.receivable)">
          <div class="metric-icon"><el-icon><Wallet /></el-icon></div>
          <div class="metric-content">
            <div class="metric-title">应收总额</div>
            <div class="metric-value">￥{{ dashboardData.totalReceivableAmount }}</div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card shadow="hover" class="metric-card bg-info" @click="navigateTo(businessRoutePathMap.receipt)">
          <div class="metric-icon"><el-icon><BankCard /></el-icon></div>
          <div class="metric-content">
            <div class="metric-title">已收总额</div>
            <div class="metric-value">￥{{ dashboardData.totalReceivedAmount }}</div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="6">
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
    <el-row :gutter="20" class="panel-group">
      <el-col :span="6">
        <el-card shadow="hover" class="metric-card op-warning" @click="navigateTo(businessRoutePathMap.stock, { warningQuery: '1' })">
          <div class="metric-icon"><el-icon><Bell /></el-icon></div>
          <div class="metric-content">
            <div class="metric-title">库存预警数</div>
            <div class="metric-value">{{ dashboardData.stockWarningCount }}</div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card shadow="hover" class="metric-card op-primary" @click="navigateTo(businessRoutePathMap.saleOrder, { status: 'submitted' })">
          <div class="metric-icon"><el-icon><Document /></el-icon></div>
          <div class="metric-content">
            <div class="metric-title">待审核销售单</div>
            <div class="metric-value">{{ dashboardData.pendingSaleOrderCount }}</div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card shadow="hover" class="metric-card op-success" @click="navigateTo(businessRoutePathMap.inbound, { status: 'submitted' })">
          <div class="metric-icon"><el-icon><Download /></el-icon></div>
          <div class="metric-content">
            <div class="metric-title">待审核入库单</div>
            <div class="metric-value">{{ dashboardData.pendingInboundCount }}</div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="6">
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
    <el-row :gutter="20" class="panel-group">
      <el-col :span="24">
        <el-card shadow="hover" class="workbench-card">
          <template #header>
            <div class="card-header">
              <span>今日待办</span>
              <span class="workbench-subtitle">点进对应页面就能直接处理，不需要再二次筛选</span>
            </div>
          </template>
          <el-row :gutter="16">
            <el-col :span="8" v-for="workbenchItem in pendingWorkbenchList" :key="workbenchItem.key">
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
    <el-row :gutter="20" class="panel-group">
      <el-col :span="8">
        <el-card shadow="hover">
          <template #header>
            <div class="card-header">
              <span>销售趋势</span>
            </div>
          </template>
          <div ref="saleTrendChartRef" style="height: 300px"></div>
        </el-card>
      </el-col>
      <el-col :span="8">
        <el-card shadow="hover">
          <template #header>
            <div class="card-header">
              <span>商品分类占比</span>
            </div>
          </template>
          <div ref="productCategoryChartRef" style="height: 300px"></div>
        </el-card>
      </el-col>
      <el-col :span="8">
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
    <el-row :gutter="20" class="panel-group">
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
import { ref, computed, onMounted, nextTick, onBeforeUnmount } from 'vue'
import { useRouter } from 'vue-router'
import { getDashboard } from '@/api/business/report'
import * as echarts from 'echarts'
import { parseTime } from '@/utils/ruoyi'
import { buildDashboardMessagePreviewContent } from '@/utils/dashboardMessage'

const router = useRouter()

// 统一首页业务入口路径，避免继续跳转到已经失效的旧路由。
const businessRoutePathMap = {
  saleOrder: '/sales/saleOrder',
  customer: '/base/customer',
  stock: '/inventory/stock',
  inventoryCheck: '/inventory/inventoryCheck',
  inbound: '/purchase/inbound',
  outbound: '/sales/outbound',
  receivable: '/finance/receivable',
  receipt: '/finance/receipt',
  transfer: '/inventory/transfer'
}

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

const saleTrendChartRef = ref(null)
const topCustomerChartRef = ref(null)
const productCategoryChartRef = ref(null)
const messagePreviewOpen = ref(false)
const messagePreviewData = ref({})

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

const updateCharts = (saleTrend, topCustomer, productCategorySales) => {
  if (saleTrendChart && saleTrend) {
    saleTrendChart.setOption({
      tooltip: { trigger: 'axis' },
      xAxis: {
        type: 'category',
        data: saleTrend.dateList || []
      },
      yAxis: {
        type: 'value'
      },
      series: [
        {
          data: saleTrend.amountList || [],
          type: 'line',
          smooth: true,
          areaStyle: {},
          itemStyle: { color: '#409EFF' }
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
        axisLabel: { interval: 0, rotate: 30 }
      },
      yAxis: {
        type: 'value'
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
        bottom: 'bottom'
      },
      series: [
        {
          name: '商品分类占比',
          type: 'pie',
          radius: ['40%', '70%'],
          avoidLabelOverlap: false,
          itemStyle: {
            borderRadius: 10,
            borderColor: '#fff',
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
              fontWeight: 'bold'
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

const handleResize = () => {
  saleTrendChart?.resize()
  topCustomerChart?.resize()
  productCategoryChart?.resize()
}

// 统一首页入口跳转逻辑，保留查询参数用于默认筛选。
const navigateTo = (path, query = {}) => {
  router.push({ path, query })
}

// 返回消息类型中文标签，让首页消息中心更容易理解。
const getMessageTypeLabel = (messageType) => {
  if (messageType === 'sale_order_audit') {
    return '销售审核'
  }
  if (messageType === 'stock_warning') {
    return '库存预警'
  }
  if (messageType === 'receivable') {
    return '应收到期'
  }
  if (messageType === 'notice') {
    return '系统公告'
  }
  return messageType || '系统消息'
}

// 判断消息是否支持直接跳到业务处理页面。
const canDirectJumpMessage = (messageItem) => {
  if (!messageItem) {
    return false
  }
  if (messageItem.message_type === 'sale_order_audit') {
    return messageItem.business_type === 'sale_order' && !!messageItem.business_id
  }
  if (messageItem.message_type === 'stock_warning') {
    return true
  }
  if (messageItem.message_type === 'receivable') {
    return messageItem.business_type === 'sale_order' && !!messageItem.business_id
  }
  return false
}

// 返回消息按钮文案，区分可处理和仅可查看的消息。
const getMessageActionLabel = (messageItem) => {
  return canDirectJumpMessage(messageItem) ? '去处理' : '查看详情'
}

// 打开消息详情弹窗，保证公告和通知都能被完整查看。
const openMessagePreview = (messageItem) => {
  messagePreviewData.value = {
    ...(messageItem || {}),
    previewContent: buildDashboardMessagePreviewContent(messageItem)
  }
  messagePreviewOpen.value = true
}

// 点击消息时优先跳转可处理项，其余消息展示详情。
const handleMessageAction = (messageItem) => {
  if (canDirectJumpMessage(messageItem)) {
    handleMessageJump(messageItem)
    return
  }
  openMessagePreview(messageItem)
}

// 根据消息类型跳到对应业务页面，减少用户寻找入口的步骤。
const handleMessageJump = (messageItem) => {
  if (!canDirectJumpMessage(messageItem)) {
    openMessagePreview(messageItem)
    return
  }
  if (messageItem.message_type === 'sale_order_audit') {
    navigateTo(businessRoutePathMap.saleOrder, { saleOrderId: messageItem.business_id })
    return
  }
  if (messageItem.message_type === 'stock_warning') {
    navigateTo(businessRoutePathMap.stock, { warningQuery: '1' })
    return
  }
  navigateTo(businessRoutePathMap.receivable, { saleOrderId: messageItem.business_id })
}

const fetchData = () => {
  getDashboard().then(res => {
    dashboardData.value = {
      totalSaleAmount: res.totalSaleAmount || 0,
      totalReceivableAmount: res.totalReceivableAmount || 0,
      totalReceivedAmount: res.totalReceivedAmount || 0,
      overdueCount: res.overdueCount || 0,
      stockWarningCount: res.stockWarningCount || 0,
      pendingSaleOrderCount: res.pendingSaleOrderCount || 0,
      pendingInboundCount: res.pendingInboundCount || 0,
      pendingOutboundCount: res.pendingOutboundCount || 0,
      messageCenter: res.messageCenter || []
    }
    
    updateCharts(res.saleTrend, res.topCustomer, res.productCategorySales)
  })
}

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
  padding: 20px;
  background-color: #f5f7f8;

  .panel-group {
    margin-bottom: 20px;
  }

  .quick-actions-card {
    border-radius: 8px;
    .quick-actions-container {
      display: flex;
      align-items: center;
      gap: 15px;
      .quick-actions-title {
        font-weight: bold;
        font-size: 16px;
        color: #303133;
        display: flex;
        align-items: center;
        gap: 5px;
      }
    }
  }

  .metric-card {
    border-radius: 8px;
    cursor: pointer;
    transition: all 0.3s;
    display: flex;
    align-items: center;
    padding: 10px;

    &:hover {
      transform: translateY(-3px);
      box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
    }

    ::v-deep .el-card__body {
      display: flex;
      align-items: center;
      width: 100%;
      padding: 15px;
    }

    .metric-icon {
      font-size: 48px;
      margin-right: 15px;
      padding: 10px;
      border-radius: 8px;
      color: #fff;
    }

    .metric-content {
      flex: 1;
      .metric-title {
        font-size: 14px;
        color: #909399;
        margin-bottom: 8px;
      }
      .metric-value {
        font-size: 24px;
        font-weight: bold;
        color: #303133;
      }
    }

    &.bg-primary .metric-icon { background: linear-gradient(135deg, #409EFF, #66b1ff); }
    &.bg-success .metric-icon { background: linear-gradient(135deg, #67C23A, #85ce61); }
    &.bg-info .metric-icon { background: linear-gradient(135deg, #909399, #a6a9ad); }
    &.bg-danger .metric-icon { background: linear-gradient(135deg, #F56C6C, #f78989); }

    &.op-warning .metric-icon { color: #E6A23C; background: rgba(230, 162, 60, 0.1); }
    &.op-primary .metric-icon { color: #409EFF; background: rgba(64, 158, 255, 0.1); }
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

    .workbench-subtitle {
      color: #909399;
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
      background: linear-gradient(135deg, #ffffff, #f6f8fb);
      border: 1px solid #ebeef5;
      border-radius: 12px;
    }

    .workbench-item-title {
      color: #303133;
      font-size: 16px;
      font-weight: 600;
    }

    .workbench-item-desc {
      margin-top: 10px;
      color: #909399;
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
      color: #409eff;
    }

    .workbench-item-count.is-success {
      color: #67c23a;
    }

    .workbench-item-count.is-info {
      color: #909399;
    }

    .workbench-item-count.is-warning {
      color: #e6a23c;
    }

    .workbench-item-count.is-danger {
      color: #f56c6c;
    }
  }

  .message-center-subtitle {
    color: #909399;
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
</style>
