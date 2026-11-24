import { ref, reactive, computed } from "vue"
import { getConfigKey, listConfig, addConfig, updateConfig } from "@/api/system/config"
import {
  normalizeNumberValue,
  formatAmountValue,
  formatRateValue
} from "../utils/reportFormatters"
import {
  getSettledResultValue,
  calculateRateValue
} from "../utils/reportAnalytics"

// 定义经营目标配置项，复用系统参数配置能力承载管理层月度目标。
export const dashboardTargetMetaList = [
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
export const decisionBudgetMetaList = [
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

// 封装经营目标与月度预算配置状态、进度/节奏计算与参数持久化操作。
export function useReportTargetAndBudgetConfig({
  proxy,
  userStore,
  managementOverviewData,
  decisionBudgetOverviewData,
  currentMonthProgressData
}) {
  const dashboardTargetLoading = ref(false)
  const dashboardTargetDialogOpen = ref(false)
  const dashboardTargetFormRef = ref(null)
  const dashboardTargetConfigDetailMap = ref({})

  const decisionBudgetLoading = ref(false)
  const decisionBudgetDialogOpen = ref(false)
  const decisionBudgetFormRef = ref(null)
  const decisionBudgetConfigDetailMap = ref({})

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

  // 判断当前用户是否具备经营目标维护权限，避免无权限用户误触参数配置能力。
  const canManageDashboardTarget = computed(() => {
    const permissionCodeList = userStore.permissions || []
    if (permissionCodeList.includes("*:*:*")) {
      return true
    }
    const hasConfigListPermission = permissionCodeList.includes("system:config:list")
    const hasConfigAddPermission = permissionCodeList.includes("system:config:add")
    const hasConfigEditPermission = permissionCodeList.includes("system:config:edit")
    return hasConfigListPermission && (hasConfigAddPermission || hasConfigEditPermission)
  })

  function getDashboardTargetMeta(targetKey) {
    return dashboardTargetMetaList.find(dashboardTargetMeta => dashboardTargetMeta.key === targetKey)
  }

  function getDashboardTargetConfigDetail(targetKey) {
    return dashboardTargetConfigDetailMap.value[targetKey] || {}
  }

  function getDashboardTargetValue(targetKey) {
    const configDetail = getDashboardTargetConfigDetail(targetKey)
    if (configDetail.configValue === undefined || configDetail.configValue === null || configDetail.configValue === "") {
      return undefined
    }
    return normalizeNumberValue(configDetail.configValue)
  }

  function formatDashboardTargetDisplayValue(targetKey, targetValue) {
    const dashboardTargetMeta = getDashboardTargetMeta(targetKey)
    if (dashboardTargetMeta?.unitType === "rate") {
      return `${normalizeNumberValue(targetValue).toFixed(2)}%`
    }
    return `￥${formatAmountValue(targetValue)}`
  }

  function buildDashboardTargetDifferenceText(targetKey, currentValue, targetValue) {
    if (targetValue === undefined) {
      return "尚未设置本月目标"
    }
    const dashboardTargetMeta = getDashboardTargetMeta(targetKey)
    const normalizedDifferenceValue = normalizeNumberValue(currentValue) - normalizeNumberValue(targetValue)
    if (normalizedDifferenceValue === 0) {
      return "当前已达成本月目标"
    }
    const differencePrefix = normalizedDifferenceValue > 0 ? "已超目标" : "距目标"
    if (dashboardTargetMeta?.unitType === "rate") {
      return `${differencePrefix} ${Math.abs(normalizedDifferenceValue).toFixed(2)} 个点`
    }
    return `${differencePrefix} ￥${formatAmountValue(Math.abs(normalizedDifferenceValue))}`
  }

  function getDashboardTargetProgressStatus(currentValue, targetValue) {
    if (targetValue === undefined || normalizeNumberValue(targetValue) <= 0) {
      return "warning"
    }
    const progressRateValue = normalizeNumberValue(currentValue) / normalizeNumberValue(targetValue)
    if (progressRateValue >= 1) {
      return "success"
    }
    if (progressRateValue >= 0.8) {
      return "warning"
    }
    return "exception"
  }

  function calculateDashboardTargetPaceGapValue(targetKey, currentValue, targetValue, monthProgressRate) {
    if (targetValue === undefined) {
      return 0
    }
    const dashboardTargetMeta = getDashboardTargetMeta(targetKey)
    if (dashboardTargetMeta?.unitType === "rate") {
      return normalizeNumberValue(currentValue) - normalizeNumberValue(targetValue)
    }
    return normalizeNumberValue(currentValue) - normalizeNumberValue(targetValue) * normalizeNumberValue(monthProgressRate)
  }

  function getDashboardTargetPaceTagType(targetKey, paceGapValue) {
    const dashboardTargetMeta = getDashboardTargetMeta(targetKey)
    const normalizedPaceGapValue = normalizeNumberValue(paceGapValue)
    if (dashboardTargetMeta?.unitType === "rate") {
      if (normalizedPaceGapValue >= 0) return "success"
      if (normalizedPaceGapValue >= -3) return "warning"
      return "danger"
    }
    if (normalizedPaceGapValue >= 0) return "success"
    if (Math.abs(normalizedPaceGapValue) <= 10000) return "warning"
    return "danger"
  }

  function buildDashboardTargetPaceTagText(targetKey, paceGapValue) {
    const dashboardTargetMeta = getDashboardTargetMeta(targetKey)
    const normalizedPaceGapValue = normalizeNumberValue(paceGapValue)
    if (dashboardTargetMeta?.unitType === "rate") {
      if (normalizedPaceGapValue >= 0) return "高于目标"
      if (normalizedPaceGapValue >= -3) return "接近目标"
      return "低于目标"
    }
    if (normalizedPaceGapValue >= 0) return "领先节奏"
    if (Math.abs(normalizedPaceGapValue) <= 10000) return "接近节奏"
    return "落后节奏"
  }

  function buildDashboardTargetPaceText(targetKey, currentValue, targetValue, monthProgressRate) {
    if (targetValue === undefined) {
      return "尚未设置目标节奏"
    }
    const dashboardTargetMeta = getDashboardTargetMeta(targetKey)
    const normalizedCurrentValue = normalizeNumberValue(currentValue)
    const normalizedTargetValue = normalizeNumberValue(targetValue)
    if (dashboardTargetMeta?.unitType === "rate") {
      const targetDifferenceValue = normalizedCurrentValue - normalizedTargetValue
      if (targetDifferenceValue >= 0) {
        return `当前高于目标 ${targetDifferenceValue.toFixed(2)} 个点`
      }
      return `当前距目标 ${Math.abs(targetDifferenceValue).toFixed(2)} 个点`
    }
    const expectedCurrentValue = normalizedTargetValue * normalizeNumberValue(monthProgressRate)
    const differenceValue = normalizedCurrentValue - expectedCurrentValue
    if (differenceValue >= 0) {
      return `按当前月份进度应达 ￥${formatAmountValue(expectedCurrentValue)}，目前领先 ￥${formatAmountValue(differenceValue)}`
    }
    return `按当前月份进度应达 ￥${formatAmountValue(expectedCurrentValue)}，目前落后 ￥${formatAmountValue(Math.abs(differenceValue))}`
  }

  function buildDashboardTargetForecastText(targetKey, currentValue, targetValue, monthProgressRate) {
    if (targetValue === undefined) {
      return "配置目标后可显示月末达成预测"
    }
    const dashboardTargetMeta = getDashboardTargetMeta(targetKey)
    const normalizedCurrentValue = normalizeNumberValue(currentValue)
    const normalizedTargetValue = normalizeNumberValue(targetValue)
    if (dashboardTargetMeta?.unitType === "rate") {
      if (normalizedCurrentValue >= normalizedTargetValue) {
        return `按当前回款水平预计月末可维持 ${formatRateValue(normalizedCurrentValue)}`
      }
      return `按当前回款水平预计月末约为 ${formatRateValue(normalizedCurrentValue)}，仍差 ${(normalizedTargetValue - normalizedCurrentValue).toFixed(2)} 个点`
    }
    const normalizedMonthProgressRate = normalizeNumberValue(monthProgressRate)
    if (normalizedMonthProgressRate <= 0) {
      return "当前月份进度不足，暂无法预测月末完成情况"
    }
    const projectedMonthEndValue = normalizedCurrentValue / normalizedMonthProgressRate
    const projectedTargetCompletionRate = calculateRateValue(projectedMonthEndValue, normalizedTargetValue)
    if (projectedMonthEndValue >= normalizedTargetValue) {
      return `按当前节奏预计月末可达 ￥${formatAmountValue(projectedMonthEndValue)}，预计超目标 ${(projectedTargetCompletionRate - 100).toFixed(2)}%`
    }
    return `按当前节奏预计月末可达 ￥${formatAmountValue(projectedMonthEndValue)}，目标完成率约为 ${projectedTargetCompletionRate.toFixed(2)}%`
  }

  function resolveDashboardTargetActionType(targetKey) {
    if (targetKey === "collectionRateTarget") {
      return "receivableSummary"
    }
    return "saleOrderSummary"
  }

  function resolveDashboardTargetActionLabel(targetKey) {
    if (targetKey === "collectionRateTarget") {
      return "查看应收台账"
    }
    return "查看销售单"
  }

  function resetDashboardTargetForm() {
    dashboardTargetForm.saleTargetAmount = undefined
    dashboardTargetForm.grossProfitTargetAmount = undefined
    dashboardTargetForm.collectionRateTarget = undefined
  }

  function syncDashboardTargetFormFromConfigDetailMap() {
    for (const dashboardTargetMeta of dashboardTargetMetaList) {
      const targetValue = getDashboardTargetValue(dashboardTargetMeta.key)
      dashboardTargetForm[dashboardTargetMeta.key] = targetValue === undefined ? undefined : targetValue
    }
  }

  function resolveConfigKeyValue(configResponse) {
    const raw =
      configResponse?.data !== undefined && configResponse?.data !== null && configResponse?.data !== ""
        ? configResponse.data
        : configResponse?.msg
    if (raw === undefined || raw === null) {
      return ""
    }
    const text = String(raw).trim()
    if (!text || text === "操作成功") {
      return ""
    }
    return text
  }

  async function loadDashboardTargetConfig() {
    dashboardTargetLoading.value = true
    try {
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
          configValue: resolveConfigKeyValue(configResponse),
          configType: "N",
          remark: dashboardTargetMeta.remark
        }
      })
      dashboardTargetConfigDetailMap.value = configDetailMap
    } finally {
      dashboardTargetLoading.value = false
    }
  }

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

  async function openDashboardTargetDialog() {
    if (!canManageDashboardTarget.value) {
      return
    }
    dashboardTargetLoading.value = true
    resetDashboardTargetForm()
    try {
      await loadDashboardTargetConfigDetailMap()
      syncDashboardTargetFormFromConfigDetailMap()
      dashboardTargetDialogOpen.value = true
      setTimeout(() => {
        dashboardTargetFormRef.value?.clearValidate()
      }, 0)
    } finally {
      dashboardTargetLoading.value = false
    }
  }

  async function submitDashboardTargetForm() {
    if (!dashboardTargetFormRef.value) {
      return
    }
    const validateResult = await dashboardTargetFormRef.value.validate().catch(() => false)
    if (!validateResult) {
      return
    }
    dashboardTargetLoading.value = true
    try {
      for (const dashboardTargetMeta of dashboardTargetMetaList) {
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
        if (configDetail.configId) {
          await updateConfig(submitData)
          continue
        }
        await addConfig(submitData)
      }
      await loadDashboardTargetConfig()
      dashboardTargetDialogOpen.value = false
      proxy.$modal.msgSuccess("经营目标保存成功")
    } finally {
      dashboardTargetLoading.value = false
    }
  }

  const hasDashboardTargetConfigured = computed(() => {
    return dashboardTargetMetaList.some(dashboardTargetMeta => {
      const configDetail = getDashboardTargetConfigDetail(dashboardTargetMeta.key)
      return configDetail.configValue !== undefined && configDetail.configValue !== null && configDetail.configValue !== ""
    })
  })

  const dashboardTargetCardList = computed(() => {
    return dashboardTargetMetaList.map(dashboardTargetMeta => {
      const rawCurrentValue = managementOverviewData.value[dashboardTargetMeta.currentValueField]
      const isProfitMasked = rawCurrentValue === null
      const currentValue = normalizeNumberValue(rawCurrentValue)
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
      const progressPercentage = isProfitMasked ? 0 : Number(Math.min(100, Math.max(0, actualProgressPercentage)).toFixed(2))
      const progressStatus = getDashboardTargetProgressStatus(currentValue, targetValue)
      const tagType = isProfitMasked ? "info" : (progressStatus === "exception" ? "danger" : progressStatus)
      return {
        key: dashboardTargetMeta.key,
        title: dashboardTargetMeta.title,
        currentValueText: isProfitMasked ? "权限脱敏" : formatDashboardTargetDisplayValue(dashboardTargetMeta.key, currentValue),
        targetValueText: isProfitMasked ? "权限脱敏" : (targetValueConfigured ? formatDashboardTargetDisplayValue(dashboardTargetMeta.key, targetValue) : "未设置"),
        progressText: isProfitMasked ? "权限脱敏" : (targetValueConfigured ? `达成率 ${actualProgressPercentage.toFixed(2)}%` : "未配置"),
        progressPercentage,
        progressStatus,
        tagType,
        paceGapValue,
        paceTagType: isProfitMasked ? "info" : (targetValueConfigured ? getDashboardTargetPaceTagType(dashboardTargetMeta.key, paceGapValue) : "info"),
        paceTagText: isProfitMasked ? "权限脱敏" : (targetValueConfigured ? buildDashboardTargetPaceTagText(dashboardTargetMeta.key, paceGapValue) : "未设目标"),
        differenceText: isProfitMasked ? "需具备成本毛利查看权限" : buildDashboardTargetDifferenceText(dashboardTargetMeta.key, currentValue, targetValue),
        paceText: isProfitMasked ? "权限脱敏" : buildDashboardTargetPaceText(dashboardTargetMeta.key, currentValue, targetValue, currentMonthProgressData.value.monthProgressRate),
        forecastText: isProfitMasked ? "权限脱敏" : buildDashboardTargetForecastText(dashboardTargetMeta.key, currentValue, targetValue, currentMonthProgressData.value.monthProgressRate)
      }
    })
  })

  function getDecisionBudgetMeta(budgetKey) {
    return decisionBudgetMetaList.find(decisionBudgetMeta => decisionBudgetMeta.key === budgetKey)
  }

  function getDecisionBudgetConfigDetail(budgetKey) {
    return decisionBudgetConfigDetailMap.value[budgetKey] || {}
  }

  function getDecisionBudgetValue(budgetKey) {
    const configDetail = getDecisionBudgetConfigDetail(budgetKey)
    if (configDetail.configValue === undefined || configDetail.configValue === null || configDetail.configValue === "") {
      return undefined
    }
    return normalizeNumberValue(configDetail.configValue)
  }

  function formatDecisionBudgetDisplayValue(budgetKey, budgetValue) {
    const decisionBudgetMeta = getDecisionBudgetMeta(budgetKey)
    if (decisionBudgetMeta?.unitType === "rate") {
      return formatRateValue(budgetValue)
    }
    return `￥${formatAmountValue(budgetValue)}`
  }

  function calculateDecisionBudgetForecastValue(actualValue, unitType, monthProgressRate) {
    if (unitType === "rate") {
      return normalizeNumberValue(actualValue)
    }
    const normalizedMonthProgressRate = normalizeNumberValue(monthProgressRate)
    if (normalizedMonthProgressRate <= 0) {
      return normalizeNumberValue(actualValue)
    }
    return normalizeNumberValue(actualValue) / normalizedMonthProgressRate
  }

  function calculateDecisionBudgetPaceGapValue(actualValue, budgetValue, monthProgressRate) {
    if (budgetValue === undefined) {
      return 0
    }
    return normalizeNumberValue(actualValue) - normalizeNumberValue(budgetValue) * normalizeNumberValue(monthProgressRate)
  }

  function getDecisionBudgetPaceTagType(paceGapValue, unitType) {
    if (unitType === "rate") {
      return normalizeNumberValue(paceGapValue) >= 0 ? "success" : "warning"
    }
    if (normalizeNumberValue(paceGapValue) >= 0) return "success"
    if (Math.abs(normalizeNumberValue(paceGapValue)) <= 10000) return "warning"
    return "danger"
  }

  function buildDecisionBudgetPaceTagText(paceGapValue, unitType) {
    if (unitType === "rate") {
      return normalizeNumberValue(paceGapValue) >= 0 ? "达到预算" : "低于预算"
    }
    if (normalizeNumberValue(paceGapValue) >= 0) return "领先预算"
    if (Math.abs(normalizeNumberValue(paceGapValue)) <= 10000) return "接近预算"
    return "低于预算"
  }

  function buildDecisionBudgetDifferenceText(budgetKey, actualValue, budgetValue) {
    if (budgetValue === undefined) {
      return "尚未设置本月预算"
    }
    const differenceValue = normalizeNumberValue(actualValue) - normalizeNumberValue(budgetValue)
    if (differenceValue === 0) {
      return "当前已达到本月预算"
    }
    return differenceValue > 0
      ? `超预算 ￥${formatAmountValue(Math.abs(differenceValue))}`
      : `距预算 ￥${formatAmountValue(Math.abs(differenceValue))}`
  }

  function buildDecisionBudgetForecastText(budgetKey, forecastValue, budgetValue) {
    if (budgetValue === undefined) {
      return "配置预算后可显示月末达成预测"
    }
    const differenceValue = normalizeNumberValue(forecastValue) - normalizeNumberValue(budgetValue)
    if (differenceValue >= 0) {
      return `按当前节奏预计月末可达 ${formatDecisionBudgetDisplayValue(budgetKey, forecastValue)}，有望超过预算。`
    }
    return `按当前节奏预计月末可达 ${formatDecisionBudgetDisplayValue(budgetKey, forecastValue)}，仍差 ￥${formatAmountValue(Math.abs(differenceValue))}。`
  }

  function resetDecisionBudgetForm() {
    decisionBudgetForm.saleBudgetAmount = undefined
    decisionBudgetForm.grossProfitBudgetAmount = undefined
    decisionBudgetForm.collectionBudgetAmount = undefined
    decisionBudgetForm.purchaseBudgetAmount = undefined
    decisionBudgetForm.netCashBudgetAmount = undefined
  }

  function syncDecisionBudgetFormFromConfigDetailMap() {
    for (const decisionBudgetMeta of decisionBudgetMetaList) {
      const budgetValue = getDecisionBudgetValue(decisionBudgetMeta.key)
      decisionBudgetForm[decisionBudgetMeta.key] = budgetValue === undefined ? undefined : budgetValue
    }
  }

  async function loadDecisionBudgetConfig() {
    decisionBudgetLoading.value = true
    try {
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
          configValue: resolveConfigKeyValue(configResponse),
          configType: "N",
          remark: decisionBudgetMeta.remark
        }
      })
      decisionBudgetConfigDetailMap.value = configDetailMap
    } finally {
      decisionBudgetLoading.value = false
    }
  }

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

  async function openDecisionBudgetDialog() {
    if (!canManageDashboardTarget.value) {
      return
    }
    decisionBudgetLoading.value = true
    resetDecisionBudgetForm()
    try {
      await loadDecisionBudgetConfigDetailMap()
      syncDecisionBudgetFormFromConfigDetailMap()
      decisionBudgetDialogOpen.value = true
      setTimeout(() => {
        decisionBudgetFormRef.value?.clearValidate()
      }, 0)
    } finally {
      decisionBudgetLoading.value = false
    }
  }

  async function submitDecisionBudgetForm() {
    if (!decisionBudgetFormRef.value) {
      return
    }
    const validateResult = await decisionBudgetFormRef.value.validate().catch(() => false)
    if (!validateResult) {
      return
    }
    decisionBudgetLoading.value = true
    try {
      for (const decisionBudgetMeta of decisionBudgetMetaList) {
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
        if (configDetail.configId) {
          await updateConfig(submitData)
          continue
        }
        await addConfig(submitData)
      }
      await loadDecisionBudgetConfig()
      decisionBudgetDialogOpen.value = false
      proxy.$modal.msgSuccess("经营预算保存成功")
    } finally {
      decisionBudgetLoading.value = false
    }
  }

  const hasDecisionBudgetConfigured = computed(() => {
    return decisionBudgetMetaList.some(decisionBudgetMeta => {
      const configDetail = getDecisionBudgetConfigDetail(decisionBudgetMeta.key)
      return configDetail.configValue !== undefined && configDetail.configValue !== null && configDetail.configValue !== ""
    })
  })

  const decisionBudgetCardList = computed(() => {
    return decisionBudgetMetaList.map(decisionBudgetMeta => {
      const rawActualValue = decisionBudgetOverviewData.value[decisionBudgetMeta.actualValueField]
      const isProfitMasked = rawActualValue === null
      const actualValue = normalizeNumberValue(rawActualValue)
      const budgetValue = getDecisionBudgetValue(decisionBudgetMeta.key)
      const budgetValueConfigured = budgetValue !== undefined
      const forecastValue = calculateDecisionBudgetForecastValue(actualValue, decisionBudgetMeta.unitType, currentMonthProgressData.value.monthProgressRate)
      const paceGapValue = calculateDecisionBudgetPaceGapValue(actualValue, budgetValue, currentMonthProgressData.value.monthProgressRate)
      return {
        key: decisionBudgetMeta.key,
        title: decisionBudgetMeta.title,
        actualValueText: isProfitMasked ? "权限脱敏" : formatDecisionBudgetDisplayValue(decisionBudgetMeta.key, actualValue),
        budgetValueText: isProfitMasked ? "权限脱敏" : (budgetValueConfigured ? formatDecisionBudgetDisplayValue(decisionBudgetMeta.key, budgetValue) : "未设置"),
        forecastValueText: isProfitMasked ? "权限脱敏" : formatDecisionBudgetDisplayValue(decisionBudgetMeta.key, forecastValue),
        completionRateText: isProfitMasked ? "权限脱敏" : (budgetValueConfigured ? `完成率 ${calculateRateValue(actualValue, budgetValue).toFixed(2)}%` : "未配置"),
        forecastRateText: isProfitMasked ? "权限脱敏" : (budgetValueConfigured ? `预测达成 ${calculateRateValue(forecastValue, budgetValue).toFixed(2)}%` : "待配置"),
        tagType: isProfitMasked ? "info" : (budgetValueConfigured ? getDecisionBudgetPaceTagType(paceGapValue, decisionBudgetMeta.unitType) : "info"),
        paceTagText: isProfitMasked ? "权限脱敏" : (budgetValueConfigured ? buildDecisionBudgetPaceTagText(paceGapValue, decisionBudgetMeta.unitType) : "未设预算"),
        differenceText: isProfitMasked ? "需具备成本毛利查看权限" : buildDecisionBudgetDifferenceText(decisionBudgetMeta.key, actualValue, budgetValue),
        forecastText: isProfitMasked ? "权限脱敏" : buildDecisionBudgetForecastText(decisionBudgetMeta.key, forecastValue, budgetValue),
        progressPercentage: isProfitMasked ? 0 : (budgetValueConfigured ? Number(Math.min(100, Math.max(0, calculateRateValue(actualValue, budgetValue))).toFixed(2)) : 0),
        paceGapValue
      }
    })
  })

  const decisionBudgetInsightList = computed(() => {
    if (!hasDecisionBudgetConfigured.value) {
      return ["暂未配置本月经营预算，建议补齐销售、毛利、回款、采购和净现金流预算。"]
    }
    const laggingDecisionBudgetCard = [...decisionBudgetCardList.value]
      .filter(decisionBudgetCard => decisionBudgetCard.budgetValueText !== "未设置")
      .sort((leftDecisionBudgetCard, rightDecisionBudgetCard) => leftDecisionBudgetCard.paceGapValue - rightDecisionBudgetCard.paceGapValue)[0]
    const negativeNetCashForecastText = normalizeNumberValue(decisionBudgetOverviewData.value.currentMonthNetCashFlowAmount) < 0
      ? `当前本月净现金流为 ￥${formatAmountValue(decisionBudgetOverviewData.value.currentMonthNetCashFlowAmount)}，建议同步关注回款和付款节奏。`
      : `当前本月净现金流为 ￥${formatAmountValue(decisionBudgetOverviewData.value.currentMonthNetCashFlowAmount)}，资金压力仍处于可控范围。`
    if (!laggingDecisionBudgetCard) {
      return [negativeNetCashForecastText]
    }
    return [
      `本月时间进度已达 ${currentMonthProgressData.value.monthProgressPercentage.toFixed(2)}%，${laggingDecisionBudgetCard.title}${laggingDecisionBudgetCard.differenceText}。`,
      laggingDecisionBudgetCard.forecastText,
      negativeNetCashForecastText
    ]
  })

  return {
    dashboardTargetLoading,
    dashboardTargetDialogOpen,
    dashboardTargetFormRef,
    dashboardTargetConfigDetailMap,
    dashboardTargetForm,
    dashboardTargetRules,
    canManageDashboardTarget,
    hasDashboardTargetConfigured,
    dashboardTargetCardList,
    resolveDashboardTargetActionType,
    resolveDashboardTargetActionLabel,
    loadDashboardTargetConfig,
    loadDashboardTargetConfigDetailMap,
    openDashboardTargetDialog,
    submitDashboardTargetForm,
    decisionBudgetLoading,
    decisionBudgetDialogOpen,
    decisionBudgetFormRef,
    decisionBudgetConfigDetailMap,
    decisionBudgetForm,
    decisionBudgetRules,
    hasDecisionBudgetConfigured,
    decisionBudgetCardList,
    decisionBudgetInsightList,
    loadDecisionBudgetConfig,
    loadDecisionBudgetConfigDetailMap,
    openDecisionBudgetDialog,
    submitDecisionBudgetForm
  }
}
