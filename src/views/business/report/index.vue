<template>
  <div class="app-container">
    <el-row :gutter="12">
      <el-col :span="4">
        <el-card shadow="hover">
          <div class="stat-title">销售总额</div>
          <div class="stat-value">{{ dashboardData.totalSaleAmount || 0 }}</div>
        </el-card>
      </el-col>
      <el-col :span="4">
        <el-card shadow="hover">
          <div class="stat-title">应收总额</div>
          <div class="stat-value">{{ dashboardData.totalReceivableAmount || 0 }}</div>
        </el-card>
      </el-col>
      <el-col :span="4">
        <el-card shadow="hover">
          <div class="stat-title">已收总额</div>
          <div class="stat-value">{{ dashboardData.totalReceivedAmount || 0 }}</div>
        </el-card>
      </el-col>
      <el-col :span="4">
        <el-card shadow="hover">
          <div class="stat-title">逾期笔数</div>
          <div class="stat-value">{{ dashboardData.overdueCount || 0 }}</div>
        </el-card>
      </el-col>
      <el-col :span="4">
        <el-card shadow="hover">
          <div class="stat-title">库存预警</div>
          <div class="stat-value">{{ dashboardData.stockWarningCount || 0 }}</div>
        </el-card>
      </el-col>
    </el-row>

    <el-row :gutter="12" style="margin-top: 12px">
      <el-col :span="8">
        <el-card shadow="hover">
          <div class="stat-title">待审核销售单</div>
          <div class="stat-value">{{ dashboardData.pendingSaleOrderCount || 0 }}</div>
        </el-card>
      </el-col>
      <el-col :span="8">
        <el-card shadow="hover">
          <div class="stat-title">待审核入库单</div>
          <div class="stat-value">{{ dashboardData.pendingInboundCount || 0 }}</div>
        </el-card>
      </el-col>
      <el-col :span="8">
        <el-card shadow="hover">
          <div class="stat-title">待审核出库单</div>
          <div class="stat-value">{{ dashboardData.pendingOutboundCount || 0 }}</div>
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
                <el-tooltip :content="getJumpDisabledReason(scope.row)" :disabled="canJumpSaleOrder(scope.row)">
                  <el-button link type="primary" :disabled="!canJumpSaleOrder(scope.row)" @click="handleMessageJump(scope.row)">
                    {{ getJumpActionLabel(scope.row) }}
                  </el-button>
                </el-tooltip>
              </template>
            </el-table-column>
          </el-table>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script setup name="BusinessReport">
import * as echarts from 'echarts'
import { getDashboard } from '@/api/business/report'

const { proxy } = getCurrentInstance()
const dashboardData = ref({})
const saleTrendRef = ref(null)
const topCustomerRef = ref(null)
const saleTrendChartInstance = ref(null)
const topCustomerChartInstance = ref(null)
const selectedMessageType = ref("all")
const router = useRouter()
const filteredMessageCenterList = computed(() => {
  const messageCenterList = dashboardData.value.messageCenter || []
  if (selectedMessageType.value === "all")
  {
    return messageCenterList
  }
  return messageCenterList.filter(messageItem => messageItem.message_type === selectedMessageType.value)
})

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

function canJumpSaleOrder(messageItem) {
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

function getJumpActionLabel(messageItem) {
  if (!canJumpSaleOrder(messageItem))
  {
    return "不可跳转"
  }
  if (messageItem.message_type === "sale_order_audit")
  {
    return "跳转销售单"
  }
  if (messageItem.message_type === "stock_warning")
  {
    return "跳转库存预警"
  }
  if (messageItem.message_type === "receivable")
  {
    return "跳转应收"
  }
  return "快速跳转"
}

function getJumpDisabledReason(messageItem) {
  if (messageItem.message_type === "sale_order_audit" || messageItem.message_type === "receivable")
  {
    return "缺少业务单据编号，无法跳转"
  }
  return "当前消息类型暂不支持跳转"
}

function handleMessageJump(messageItem) {
  if (!canJumpSaleOrder(messageItem))
  {
    proxy.$modal.msgWarning("当前消息暂不支持跳转")
    return
  }
  if (messageItem.message_type === "sale_order_audit")
  {
    router.push({
      path: "/business/saleOrder",
      query: {
        saleOrderId: messageItem.business_id
      }
    })
    return
  }
  if (messageItem.message_type === "stock_warning")
  {
    router.push({
      path: "/business/stock",
      query: {
        warningQuery: "1"
      }
    })
    return
  }
  router.push({
    path: "/business/receivable",
    query: {
      saleOrderId: messageItem.business_id
    }
  })
}

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

function getData() {
  getDashboard().then(response => {
    dashboardData.value = response.data || response
    initCharts(dashboardData.value)
  })
}

onMounted(() => {
  window.addEventListener("resize", handleChartResize)
  getData()
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
</style>
