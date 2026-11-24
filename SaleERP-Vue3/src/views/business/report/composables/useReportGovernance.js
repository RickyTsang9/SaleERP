import { ref, reactive, computed } from "vue"
import {
  getDecisionBudgetPlanCurrent,
  listDecisionBudgetPlanHistory,
  addDecisionBudgetPlan,
  updateDecisionBudgetPlan,
  createDecisionBudgetPlanVersion,
  submitDecisionBudgetPlan,
  approveDecisionBudgetPlan,
  rejectDecisionBudgetPlan
} from "@/api/business/decisionBudgetPlan"
import {
  listExecutiveBriefRecord,
  getExecutiveBriefRecord,
  addExecutiveBriefRecord
} from "@/api/business/executiveBriefRecord"
import {
  listOperationSnapshot,
  getOperationSnapshot,
  addOperationSnapshot
} from "@/api/business/operationSnapshot"
import {
  listExecutiveActionItem,
  getExecutiveActionItem,
  addExecutiveActionItem,
  updateExecutiveActionItem,
  delExecutiveActionItem
} from "@/api/business/executiveActionItem"
import { parseTime } from "@/utils/ruoyi"
import {
  normalizeNumberValue,
  formatAmountValue,
  formatRateValue,
  getOperationSnapshotTypeLabel
} from "../utils/reportFormatters"

export const annualBudgetMonthLabelList = ["1月", "2月", "3月", "4月", "5月", "6月", "7月", "8月", "9月", "10月", "11月", "12月"]

function escapeHtmlText(value) {
  return String(value ?? "")
    .replace(/&/g, "&amp;")
    .replace(/</g, "&lt;")
    .replace(/>/g, "&gt;")
    .replace(/"/g, "&quot;")
    .replace(/'/g, "&#39;")
}

function triggerClientFileDownload(filename, content, mimeType) {
  const blob = new Blob(["\uFEFF", content], { type: mimeType })
  const objectUrl = URL.createObjectURL(blob)
  const link = document.createElement("a")
  link.href = objectUrl
  link.download = filename
  document.body.appendChild(link)
  link.click()
  document.body.removeChild(link)
  setTimeout(() => URL.revokeObjectURL(objectUrl), 1000)
}

function buildBriefMarkdownDocument(title, metaLabel, sections = []) {
  const lines = [`# ${title}`, "", `> ${metaLabel}`, ""]
  for (const section of sections) {
    lines.push(`## ${section.title}`)
    if (section.textList?.length) {
      section.textList.forEach(item => lines.push(`- ${item}`))
    } else {
      lines.push("- 暂无内容")
    }
    lines.push("")
  }
  return lines.join("\n")
}

function buildBriefWordHtmlDocument(title, metaLabel, sections = []) {
  const sectionBlocks = sections.map(section => {
    const items = section.textList?.length
      ? `<ul>${section.textList.map(item => `<li style="margin:6px 0;line-height:1.7;">${escapeHtmlText(item)}</li>`).join("")}</ul>`
      : `<p style="color:#666;">暂无内容</p>`
    return `<h2 style="font-size:15pt;color:#1e3a8a;border-bottom:1px solid #cbd5e1;padding-bottom:4px;margin-top:18px;">${escapeHtmlText(section.title)}</h2>${items}`
  }).join("")

  return `<!DOCTYPE html>
<html xmlns:o="urn:schemas-microsoft-com:office:office" xmlns:w="urn:schemas-microsoft-com:office:word" xmlns="http://www.w3.org/TR/REC-html40">
<head>
<meta charset="utf-8" />
<title>${escapeHtmlText(title)}</title>
<style>
  body { font-family: "Microsoft YaHei", "PingFang SC", sans-serif; color: #1f2937; padding: 24px 32px; font-size: 11pt; }
  h1 { font-size: 20pt; color: #0f172a; margin-bottom: 6px; }
  .meta { color: #64748b; font-size: 10pt; margin-bottom: 18px; }
</style>
</head>
<body>
  <h1>${escapeHtmlText(title)}</h1>
  <div class="meta">${escapeHtmlText(metaLabel)}</div>
  ${sectionBlocks}
</body>
</html>`
}

// 封装年度预算拆解与审批、经营决议事项、经营简报（含导出/归档）与经营快照归档治理能力。
export function useReportGovernance({
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
}) {
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
  const executiveBriefRecordDialogOpen = ref(false)
  const executiveBriefRecordPreviewData = ref({})

  const operationSnapshotLoading = ref(false)
  const operationSnapshotList = ref([])
  const operationSnapshotDialogOpen = ref(false)
  const operationSnapshotPreviewData = ref({})

  const executiveActionItemLoading = ref(false)
  const executiveActionItemList = ref([])
  const executiveActionItemDialogOpen = ref(false)
  const executiveActionItemFormRef = ref(null)
  const executiveActionItemDialogTitle = ref("新增经营决议事项")

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

  const annualBudgetPlanRules = {
    budgetYear: [{ required: true, message: "预算年度不能为空", trigger: "blur" }],
    planName: [{ required: true, message: "预算计划名称不能为空", trigger: "blur" }]
  }

  const executiveActionItemRules = {
    actionTitle: [{ required: true, message: "决议事项不能为空", trigger: "blur" }]
  }

  const canManageAnnualBudgetPlan = computed(() => {
    const permissionCodeList = userStore.permissions || []
    if (permissionCodeList.includes("*:*:*")) return true
    return permissionCodeList.includes("business:report:edit")
  })

  const canApproveAnnualBudgetPlan = computed(() => {
    const permissionCodeList = userStore.permissions || []
    if (permissionCodeList.includes("*:*:*")) return true
    return permissionCodeList.includes("business:report:approve")
  })

  const canCreateAnnualBudgetPlanVersion = computed(() => {
    return canManageAnnualBudgetPlan.value
      && !!annualBudgetPlanData.value.planId
      && annualBudgetPlanData.value.planStatus === "approved"
  })

  const canArchiveExecutiveBriefRecord = computed(() => {
    const permissionCodeList = userStore.permissions || []
    if (permissionCodeList.includes("*:*:*")) return true
    return permissionCodeList.includes("business:report:brief")
  })

  const canManageExecutiveActionItem = computed(() => {
    const permissionCodeList = userStore.permissions || []
    if (permissionCodeList.includes("*:*:*")) return true
    return permissionCodeList.includes("business:report:edit")
  })

  const pendingExecutiveActionItemCount = computed(() => {
    return executiveActionItemList.value.filter(executiveActionItem => {
      return executiveActionItem.actionStatus !== "completed" && executiveActionItem.actionStatus !== "canceled"
    }).length
  })

  const annualBudgetPlanEffectiveText = computed(() => {
    return annualBudgetPlanData.value.effectiveFlag === "y" ? "当前生效版本" : "未生效版本"
  })

  const annualBudgetPlanEffectiveTagType = computed(() => {
    return annualBudgetPlanData.value.effectiveFlag === "y" ? "success" : "info"
  })

  const annualBudgetPlanStatusText = computed(() => {
    const status = annualBudgetPlanData.value.planStatus
    if (status === "submitted") return "待审批"
    if (status === "approved") return "已通过"
    if (status === "rejected") return "已驳回"
    return "草稿"
  })

  const annualBudgetPlanStatusTagType = computed(() => {
    const status = annualBudgetPlanData.value.planStatus
    if (status === "submitted") return "warning"
    if (status === "approved") return "success"
    if (status === "rejected") return "danger"
    return "info"
  })

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

  const annualBudgetMonthlyTableList = computed(() => {
    const monthlyTableRowList = []
    for (let monthIndexValue = 0; monthIndexValue < annualBudgetMonthLabelList.length; monthIndexValue++) {
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

  function openExecutiveBriefDialog() {
    executiveBriefDialogOpen.value = true
  }

  function buildExecutiveBriefPlainText() {
    const executiveBriefTextLineList = [`经营简报生成时间：${executiveBriefData.value.generatedAtText || parseTime(new Date(), "{y}-{m}-{d} {h}:{i}:{s}")}`]
    for (const executiveBriefSection of executiveBriefSectionList.value) {
      if (!executiveBriefSection.textList?.length) {
        continue
      }
      executiveBriefTextLineList.push("")
      executiveBriefTextLineList.push(`${executiveBriefSection.title}`)
      executiveBriefSection.textList.forEach(executiveBriefText => {
        executiveBriefTextLineList.push(`- ${executiveBriefText}`)
      })
    }
    return executiveBriefTextLineList.join("\n")
  }

  function copyExecutiveBriefText() {
    const executiveBriefPlainText = buildExecutiveBriefPlainText()
    if (typeof navigator !== "undefined" && navigator.clipboard?.writeText) {
      navigator.clipboard.writeText(executiveBriefPlainText).then(() => {
        proxy.$modal.msgSuccess("经营简报已复制")
      }).catch(() => {
        proxy.$modal.msgWarning("当前浏览器不支持自动复制，请手动复制简报内容")
      })
      return
    }
    proxy.$modal.msgWarning("当前浏览器不支持自动复制，请手动复制简报内容")
  }

  // 导出当前实时经营简报为 Markdown 文件，方便汇报沉淀或导入知识库。
  function exportExecutiveBriefMarkdown() {
    const dateStamp = parseTime(new Date(), "{y}{m}{d}")
    const title = `${parseTime(new Date(), "{y}-{m}-{d}")} SaleERP 经营简报`
    const metaLabel = `生成时间：${executiveBriefData.value.generatedAtText || parseTime(new Date(), "{y}-{m}-{d} {h}:{i}:{s}")}`
    const markdown = buildBriefMarkdownDocument(title, metaLabel, executiveBriefSectionList.value)
    triggerClientFileDownload(`SaleERP经营简报_${dateStamp}.md`, markdown, "text/markdown;charset=utf-8")
    proxy.$modal.msgSuccess("已导出 Markdown 经营简报")
  }

  // 导出当前实时经营简报为 Word 兼容 .doc 文件，便于管理层会议分发。
  function exportExecutiveBriefWord() {
    const dateStamp = parseTime(new Date(), "{y}{m}{d}")
    const title = `${parseTime(new Date(), "{y}-{m}-{d}")} SaleERP 经营简报`
    const metaLabel = `生成时间：${executiveBriefData.value.generatedAtText || parseTime(new Date(), "{y}-{m}-{d} {h}:{i}:{s}")}`
    const html = buildBriefWordHtmlDocument(title, metaLabel, executiveBriefSectionList.value)
    triggerClientFileDownload(`SaleERP经营简报_${dateStamp}.doc`, html, "application/msword;charset=utf-8")
    proxy.$modal.msgSuccess("已导出 Word 经营简报")
  }

  function buildArchivedBriefSections(recordData) {
    return [
      { title: "经营结论", textList: recordData?.summaryContent ? [recordData.summaryContent] : [] },
      { title: "本期亮点", textList: recordData?.highlightTextList || [] },
      { title: "主要风险", textList: recordData?.riskTextList || [] },
      { title: "建议决策", textList: recordData?.actionTextList || [] }
    ]
  }

  function exportArchivedBriefMarkdown() {
    const recordData = executiveBriefRecordPreviewData.value || {}
    const title = recordData.briefTitle || "历史经营简报"
    const metaLabel = `归档时间：${recordData.generatedTime ? parseTime(recordData.generatedTime) : "-"}`
    const markdown = buildBriefMarkdownDocument(title, metaLabel, buildArchivedBriefSections(recordData))
    triggerClientFileDownload(`${title}.md`, markdown, "text/markdown;charset=utf-8")
    proxy.$modal.msgSuccess("已导出归档简报 Markdown")
  }

  function exportArchivedBriefWord() {
    const recordData = executiveBriefRecordPreviewData.value || {}
    const title = recordData.briefTitle || "历史经营简报"
    const metaLabel = `归档时间：${recordData.generatedTime ? parseTime(recordData.generatedTime) : "-"}`
    const html = buildBriefWordHtmlDocument(title, metaLabel, buildArchivedBriefSections(recordData))
    triggerClientFileDownload(`${title}.doc`, html, "application/msword;charset=utf-8")
    proxy.$modal.msgSuccess("已导出归档简报 Word")
  }

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

  function normalizeAnnualBudgetMonthlyList(monthlyBudgetList = []) {
    const normalizedMonthlyBudgetList = []
    for (let monthIndexValue = 0; monthIndexValue < 12; monthIndexValue++) {
      normalizedMonthlyBudgetList.push(normalizeNumberValue(monthlyBudgetList?.[monthIndexValue]))
    }
    return normalizedMonthlyBudgetList
  }

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

  function syncAnnualBudgetPlanFormTotalAmount(fieldKey) {
    const listMap = {
      saleBudgetAmount: "saleMonthlyPlanList",
      grossProfitBudgetAmount: "grossProfitMonthlyPlanList",
      collectionBudgetAmount: "collectionMonthlyPlanList",
      purchaseBudgetAmount: "purchaseMonthlyPlanList",
      netCashBudgetAmount: "netCashMonthlyPlanList"
    }
    const listKey = listMap[fieldKey] || "netCashMonthlyPlanList"
    annualBudgetPlanForm[fieldKey] = annualBudgetPlanForm[listKey].reduce(
      (previousAmountValue, currentAmountValue) => previousAmountValue + normalizeNumberValue(currentAmountValue),
      0
    )
  }

  function distributeAnnualBudgetPlanMetric(totalAmountFieldKey) {
    const totalAmountValue = normalizeNumberValue(annualBudgetPlanForm[totalAmountFieldKey])
    const averageAmountValue = totalAmountValue > 0 ? Number((totalAmountValue / 12).toFixed(2)) : 0
    const monthlyBudgetList = Array.from({ length: 12 }, () => averageAmountValue)
    if (totalAmountValue > 0) {
      const distributedAmountValue = monthlyBudgetList.slice(0, 11).reduce((prev, cur) => prev + cur, 0)
      monthlyBudgetList[11] = Number((totalAmountValue - distributedAmountValue).toFixed(2))
    }
    const listMap = {
      saleBudgetAmount: "saleMonthlyPlanList",
      grossProfitBudgetAmount: "grossProfitMonthlyPlanList",
      collectionBudgetAmount: "collectionMonthlyPlanList",
      purchaseBudgetAmount: "purchaseMonthlyPlanList",
      netCashBudgetAmount: "netCashMonthlyPlanList"
    }
    const listKey = listMap[totalAmountFieldKey] || "netCashMonthlyPlanList"
    annualBudgetPlanForm[listKey] = monthlyBudgetList
  }

  function distributeAllAnnualBudgetPlanMetrics() {
    distributeAnnualBudgetPlanMetric("saleBudgetAmount")
    distributeAnnualBudgetPlanMetric("grossProfitBudgetAmount")
    distributeAnnualBudgetPlanMetric("collectionBudgetAmount")
    distributeAnnualBudgetPlanMetric("purchaseBudgetAmount")
    distributeAnnualBudgetPlanMetric("netCashBudgetAmount")
  }

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

  async function loadAnnualBudgetPlanHistoryList() {
    try {
      const response = await listDecisionBudgetPlanHistory(annualBudgetYear.value)
      annualBudgetPlanHistoryList.value = response.data || response || []
    } catch {
      annualBudgetPlanHistoryList.value = []
    }
  }

  async function loadAnnualBudgetPlanData() {
    annualBudgetPlanLoading.value = true
    try {
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
    } catch {
      annualBudgetPlanData.value = createAnnualBudgetPlanEmptyData(annualBudgetYear.value)
    } finally {
      annualBudgetPlanLoading.value = false
    }
    await loadAnnualBudgetPlanHistoryList()
  }

  function openAnnualBudgetPlanDialog() {
    annualBudgetPlanFormMode.value = "edit"
    annualBudgetPlanSourceId.value = annualBudgetPlanData.value.planId
    syncAnnualBudgetPlanFormFromData(annualBudgetPlanData.value)
    annualBudgetPlanDialogOpen.value = true
    setTimeout(() => {
      annualBudgetPlanFormRef.value?.clearValidate()
    }, 0)
  }

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

  async function submitAnnualBudgetPlanForm() {
    if (!annualBudgetPlanFormRef.value) return
    const validateResult = await annualBudgetPlanFormRef.value.validate().catch(() => false)
    if (!validateResult) return
    annualBudgetPlanLoading.value = true
    try {
      const annualBudgetPlanPayload = buildAnnualBudgetPlanPayload()
      if (annualBudgetPlanFormMode.value === "createVersion" && annualBudgetPlanSourceId.value) {
        await createDecisionBudgetPlanVersion(annualBudgetPlanSourceId.value, annualBudgetPlanPayload)
      } else if (annualBudgetPlanPayload.planId) {
        await updateDecisionBudgetPlan(annualBudgetPlanPayload)
      } else {
        await addDecisionBudgetPlan(annualBudgetPlanPayload)
      }
      annualBudgetYear.value = annualBudgetPlanPayload.budgetYear
      await loadAnnualBudgetPlanData()
      annualBudgetPlanDialogOpen.value = false
      proxy.$modal.msgSuccess(annualBudgetPlanFormMode.value === "createVersion" ? "预算新版本已创建" : "年度预算计划保存成功")
    } finally {
      annualBudgetPlanLoading.value = false
    }
  }

  async function handleAnnualBudgetPlanSubmit() {
    if (!annualBudgetPlanData.value.planId) {
      proxy.$modal.msgWarning("请先保存年度预算计划，再提交审批")
      return
    }
    annualBudgetPlanLoading.value = true
    try {
      await submitDecisionBudgetPlan(annualBudgetPlanData.value.planId)
      await loadAnnualBudgetPlanData()
      proxy.$modal.msgSuccess("年度预算计划已提交审批")
    } finally {
      annualBudgetPlanLoading.value = false
    }
  }

  function openAnnualBudgetApprovalDialog(actionType) {
    annualBudgetApprovalActionType.value = actionType
    annualBudgetApprovalForm.approveRemark = ""
    annualBudgetApprovalDialogOpen.value = true
  }

  async function submitAnnualBudgetApproval() {
    if (!annualBudgetPlanData.value.planId) return
    annualBudgetPlanLoading.value = true
    try {
      if (annualBudgetApprovalActionType.value === "approve") {
        await approveDecisionBudgetPlan(annualBudgetPlanData.value.planId, {
          approveRemark: annualBudgetApprovalForm.approveRemark
        })
      } else {
        await rejectDecisionBudgetPlan(annualBudgetPlanData.value.planId, {
          approveRemark: annualBudgetApprovalForm.approveRemark
        })
      }
      await loadAnnualBudgetPlanData()
      await loadDecisionBudgetConfig()
      annualBudgetApprovalDialogOpen.value = false
      proxy.$modal.msgSuccess(annualBudgetApprovalActionType.value === "approve" ? "预算计划已审批通过" : "预算计划已驳回")
    } finally {
      annualBudgetPlanLoading.value = false
    }
  }

  async function loadExecutiveActionItemList() {
    executiveActionItemLoading.value = true
    try {
      const response = await listExecutiveActionItem({ pageNum: 1, pageSize: 8 })
      executiveActionItemList.value = response.rows || []
    } catch {
      executiveActionItemList.value = []
    } finally {
      executiveActionItemLoading.value = false
    }
  }

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

  async function openExecutiveActionItemDialog(executiveActionItemRow) {
    resetExecutiveActionItemForm()
    if (!executiveActionItemRow?.actionItemId) {
      executiveActionItemDialogTitle.value = "新增经营决议事项"
      executiveActionItemDialogOpen.value = true
      return
    }
    executiveActionItemLoading.value = true
    try {
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
    } finally {
      executiveActionItemLoading.value = false
    }
  }

  async function submitExecutiveActionItemForm() {
    if (!executiveActionItemFormRef.value) return
    const validateResult = await executiveActionItemFormRef.value.validate().catch(() => false)
    if (!validateResult) return
    executiveActionItemLoading.value = true
    try {
      const payload = {
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
      if (payload.actionItemId) {
        await updateExecutiveActionItem(payload)
      } else {
        await addExecutiveActionItem(payload)
      }
      await loadExecutiveActionItemList()
      executiveActionItemDialogOpen.value = false
      proxy.$modal.msgSuccess(payload.actionItemId ? "经营决议事项已更新" : "经营决议事项已新增")
    } finally {
      executiveActionItemLoading.value = false
    }
  }

  async function handleExecutiveActionItemComplete(executiveActionItemRow) {
    executiveActionItemLoading.value = true
    try {
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
    } finally {
      executiveActionItemLoading.value = false
    }
  }

  async function handleExecutiveActionItemRemove(executiveActionItemRow) {
    await proxy.$modal.confirm(`是否确认删除事项“${executiveActionItemRow.actionTitle}”？`)
    executiveActionItemLoading.value = true
    try {
      await delExecutiveActionItem(executiveActionItemRow.actionItemId)
      await loadExecutiveActionItemList()
      proxy.$modal.msgSuccess("经营决议事项已删除")
    } finally {
      executiveActionItemLoading.value = false
    }
  }

  async function loadExecutiveBriefRecordList() {
    executiveBriefRecordLoading.value = true
    try {
      const response = await listExecutiveBriefRecord({ pageNum: 1, pageSize: 5 })
      executiveBriefRecordList.value = response.rows || []
    } catch {
      executiveBriefRecordList.value = []
    } finally {
      executiveBriefRecordLoading.value = false
    }
  }

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

  function buildOperationSnapshotPlainText(snapshotType) {
    const lines = [
      `快照类型：${getOperationSnapshotTypeLabel(snapshotType)}`,
      `快照日期：${parseTime(new Date(), "{y}-{m}-{d}")}`,
      `销售额：￥${formatAmountValue(managementOverviewData.value.currentMonthSaleAmount)}`,
      `毛利额：￥${formatAmountValue(managementOverviewData.value.currentMonthGrossProfitAmount)}`,
      `回款率：${formatRateValue(managementOverviewData.value.currentMonthCollectionRate)}`,
      `逾期笔数：${dashboardData.value.overdueCount || 0}`,
      `库存预警：${dashboardData.value.stockWarningCount || 0}`,
      `经营待办：${pendingExecutiveActionItemCount.value}`
    ]
    if (executiveBriefData.value.summaryText) {
      lines.push("", "经营结论", executiveBriefData.value.summaryText)
    }
    lines.push("", "经营简报摘录", buildExecutiveBriefPlainText())
    return lines.join("\n")
  }

  function buildOperationSnapshotPayload(snapshotType) {
    return {
      snapshotTitle: `${parseTime(new Date(), "{y}-{m}-{d}")}${getOperationSnapshotTypeLabel(snapshotType)}经营快照`,
      snapshotDate: parseTime(new Date(), "{y}-{m}-{d} 00:00:00"),
      snapshotType,
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

  async function archiveCurrentExecutiveBrief() {
    if (!hasDecisionSupportData.value) {
      proxy.$modal.msgWarning("当前还没有可归档的经营简报")
      return
    }
    executiveBriefRecordLoading.value = true
    try {
      await addExecutiveBriefRecord(buildExecutiveBriefRecordPayload())
      await addOperationSnapshot(buildOperationSnapshotPayload("weekly"))
      await loadExecutiveBriefRecordList()
      await loadOperationSnapshotList()
      await loadExecutiveActionItemList()
      proxy.$modal.msgSuccess("经营简报与周经营快照已归档")
    } finally {
      executiveBriefRecordLoading.value = false
    }
  }

  async function openExecutiveBriefRecordPreview(executiveBriefRecordRow) {
    executiveBriefRecordLoading.value = true
    try {
      const response = await getExecutiveBriefRecord(executiveBriefRecordRow.briefId)
      executiveBriefRecordPreviewData.value = response.data || response || {}
      executiveBriefRecordDialogOpen.value = true
    } finally {
      executiveBriefRecordLoading.value = false
    }
  }

  async function loadOperationSnapshotList() {
    operationSnapshotLoading.value = true
    try {
      const response = await listOperationSnapshot({ pageNum: 1, pageSize: 6 })
      operationSnapshotList.value = response.rows || []
    } catch {
      operationSnapshotList.value = []
    } finally {
      operationSnapshotLoading.value = false
    }
  }

  async function archiveOperationSnapshot(snapshotType) {
    if (!hasManagementCockpitData.value) {
      proxy.$modal.msgWarning("当前还没有可归档的经营快照")
      return
    }
    operationSnapshotLoading.value = true
    try {
      await addOperationSnapshot(buildOperationSnapshotPayload(snapshotType))
      await loadOperationSnapshotList()
      proxy.$modal.msgSuccess(`${getOperationSnapshotTypeLabel(snapshotType)}已归档`)
    } finally {
      operationSnapshotLoading.value = false
    }
  }

  async function openOperationSnapshotPreview(operationSnapshotRow) {
    operationSnapshotLoading.value = true
    try {
      const response = await getOperationSnapshot(operationSnapshotRow.snapshotId)
      operationSnapshotPreviewData.value = response.data || response || {}
      operationSnapshotDialogOpen.value = true
    } finally {
      operationSnapshotLoading.value = false
    }
  }

  return {
    annualBudgetMonthLabelList,
    executiveBriefDialogOpen,
    annualBudgetYear,
    annualBudgetPlanLoading,
    annualBudgetPlanDialogOpen,
    annualBudgetPlanFormRef,
    annualBudgetApprovalDialogOpen,
    annualBudgetApprovalActionType,
    annualBudgetPlanFormMode,
    annualBudgetPlanSourceId,
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
    loadAnnualBudgetPlanHistoryList,
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
  }
}
