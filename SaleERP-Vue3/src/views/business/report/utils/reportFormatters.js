import { parseTime } from "@/utils/ruoyi"

// 统一把金额值转换成前端可展示的数字。
export function normalizeNumberValue(numberValue) {
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
export function normalizeCountValue(countValue) {
  return Math.trunc(normalizeNumberValue(countValue))
}

// 统一格式化金额，保证管理层看到的数据带千分位且保留两位小数。
export function formatAmountValue(amountValue) {
  return normalizeNumberValue(amountValue).toLocaleString("zh-CN", {
    minimumFractionDigits: 2,
    maximumFractionDigits: 2
  })
}

// 统一格式化百分比指标。
export function formatRateValue(rateValue) {
  return `${normalizeNumberValue(rateValue).toFixed(2)}%`
}

// 统一约束健康评分范围，避免计算过程中出现超过 100 分或低于 0 分的异常值。
export function normalizeScoreValue(scoreValue) {
  return Math.max(0, Math.min(100, Math.round(normalizeNumberValue(scoreValue))))
}

// 返回经营健康评分的级别文案，帮助管理层一眼识别当前状态。
export function getManagementHealthLevelText(scoreValue) {
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
export function getManagementHealthTagType(scoreValue) {
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

// 统一生成增长率标签文案。
export function buildGrowthTagText(growthRateValue) {
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
export function buildGrowthSummaryText(growthRateValue) {
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
export function buildSaleAmountSummaryText(currentMonthSaleAmountValue, previousMonthSaleAmountValue, growthRateValue) {
  const normalizedCurrentMonthSaleAmountValue = normalizeNumberValue(currentMonthSaleAmountValue)
  const normalizedPreviousMonthSaleAmountValue = normalizeNumberValue(previousMonthSaleAmountValue)
  if (normalizedCurrentMonthSaleAmountValue <= 0 && normalizedPreviousMonthSaleAmountValue <= 0)
  {
    return "本月暂无销售数据"
  }
  return `本月销售额${buildGrowthSummaryText(growthRateValue)}`
}

// 根据增长率返回标签颜色，帮助管理层快速区分增长和下滑。
export function getGrowthTagType(growthRateValue) {
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
export function buildRateDifferenceText(rateDifferenceValue) {
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
export function getDifferenceTagType(rateDifferenceValue) {
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

// 生成资金压力窗口状态标签类型，帮助管理层快速区分净流入与净流出压力。
export function getCashFlowWindowTagType(netCashFlowValue) {
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
export function getCashFlowWindowTagText(netCashFlowValue) {
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
export function buildCashFlowWindowDescription(netCashFlowValue, forecastDays) {
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

// 返回经营决议事项状态文案。
export function getExecutiveActionStatusLabel(actionStatus) {
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
export function getExecutiveActionStatusTagType(actionStatus) {
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
export function getExecutiveActionPriorityLabel(priorityLevel) {
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
export function getExecutiveActionPriorityTagType(priorityLevel) {
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
export function isExecutiveActionItemOverdue(executiveActionItemRow) {
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

// 统一读取经营快照类型文字，帮助管理层区分日报、周报和月报沉淀。
export function getOperationSnapshotTypeLabel(snapshotType) {
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
export function getOperationSnapshotTypeTagType(snapshotType) {
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

// 返回重点付款供应商的风险提示文案，帮助管理层区分已逾期和即将到期。
export function getDueRiskText(dueInDays) {
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
