import { buildDashboardMessagePreviewContent } from '@/utils/dashboardMessage'

// 统一首页与经营看板的业务入口路径，避免不同页面继续维护多份路由映射。
export const businessDashboardRoutePathMap = {
  saleOrder: '/sales/saleOrder',
  customer: '/base/customer',
  stock: '/inventory/stock',
  inventoryCheck: '/inventory/inventoryCheck',
  purchaseOrder: '/purchase/purchaseOrder',
  inbound: '/purchase/inbound',
  outbound: '/sales/outbound',
  receivable: '/finance/receivable',
  receipt: '/finance/receipt',
  payable: '/finance/payable',
  transfer: '/inventory/transfer',
  customerFollow: '/base/customerFollow'
}

// 为首页与经营看板补齐常用统计字段默认值，避免页面首次渲染出现 undefined。
const defaultDashboardSummaryData = {
  totalSaleAmount: 0,
  totalReceivableAmount: 0,
  totalReceivedAmount: 0,
  overdueCount: 0,
  stockWarningCount: 0,
  pendingSaleOrderCount: 0,
  pendingInboundCount: 0,
  pendingOutboundCount: 0,
  messageCenter: []
}

// 统一提取仪表盘接口返回主体，兼容 request 包装结构与旧数据结构。
export function resolveDashboardResponseData(dashboardResponse) {
  return dashboardResponse?.data || dashboardResponse || {}
}

// 统一归一化仪表盘数据，保证首页与经营看板拿到一致字段口径。
export function normalizeDashboardSummaryData(dashboardResponse) {
  const dashboardRawData = resolveDashboardResponseData(dashboardResponse)
  return {
    ...defaultDashboardSummaryData,
    ...dashboardRawData,
    messageCenter: Array.isArray(dashboardRawData.messageCenter) ? dashboardRawData.messageCenter : []
  }
}

// 返回消息类型中文名称，统一首页与经营看板中的展示口径。
export function getDashboardMessageTypeLabel(messageType) {
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

// 判断当前消息是否支持直接跳转到业务处理页面。
export function canDirectJumpDashboardMessage(messageItem) {
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

// 返回消息主按钮文案，区分直接处理与仅查看详情两类消息。
export function getDashboardMessageActionLabel(messageItem) {
  return canDirectJumpDashboardMessage(messageItem) ? '去处理' : '查看详情'
}

// 组装消息详情预览数据，保证不同页面打开详情时看到相同正文。
export function buildDashboardMessagePreviewData(messageItem) {
  return {
    ...(messageItem || {}),
    previewContent: buildDashboardMessagePreviewContent(messageItem)
  }
}

// 按消息类型解析业务跳转目标，保证首页与经营看板进入同一处理页面。
export function resolveDashboardMessageRoute(messageItem) {
  if (!canDirectJumpDashboardMessage(messageItem)) {
    return null
  }
  if (messageItem.message_type === 'sale_order_audit') {
    return {
      path: businessDashboardRoutePathMap.saleOrder,
      query: {
        saleOrderId: messageItem.business_id
      }
    }
  }
  if (messageItem.message_type === 'stock_warning') {
    return {
      path: businessDashboardRoutePathMap.stock,
      query: {
        warningQuery: '1'
      }
    }
  }
  return {
    path: businessDashboardRoutePathMap.receivable,
    query: {
      saleOrderId: messageItem.business_id
    }
  }
}
