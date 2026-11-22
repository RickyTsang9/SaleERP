import { parseTime } from "@/utils/ruoyi"
import {
  normalizeNumberValue,
  normalizeCountValue,
  formatAmountValue,
  formatRateValue,
  buildSaleAmountSummaryText
} from "./reportFormatters"

// 判断当前后端是否已经直接返回管理驾驶舱数据，避免兼容模式重复执行。
export function hasRemoteManagementCockpitData(dashboardResponseData) {
  const managementOverviewValue = dashboardResponseData?.managementOverview
  const riskOverviewValue = dashboardResponseData?.riskOverview
  return !!managementOverviewValue && Object.keys(managementOverviewValue).length > 0
    && !!riskOverviewValue && Object.keys(riskOverviewValue).length > 0
}

// 统一提取对象中第一个存在的字段值，兼容历史接口字段差异。
export function getFirstDefinedValue(targetRow, fieldNameList = []) {
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
export function parseDateValue(dateValue) {
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
export function formatDateValueForQuery(dateValue) {
  const yearValue = dateValue.getFullYear()
  const monthValue = `${dateValue.getMonth() + 1}`.padStart(2, "0")
  const dayValue = `${dateValue.getDate()}`.padStart(2, "0")
  return `${yearValue}-${monthValue}-${dayValue}`
}

// 统一返回日期的零点时间，便于账龄和逾期计算保持按天对齐。
export function getDateStartValue(dateValue) {
  return new Date(dateValue.getFullYear(), dateValue.getMonth(), dateValue.getDate())
}

// 构造指定月份的起止范围，供经营指标兼容模式按月汇总使用。
export function buildMonthRange(monthOffset) {
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
export function isDateInMonthRange(dateValue, monthRange) {
  const parsedDateValue = parseDateValue(dateValue)
  if (!parsedDateValue)
  {
    return false
  }
  return parsedDateValue >= monthRange.startDateObject
    && parsedDateValue < new Date(monthRange.endDateObject.getFullYear(), monthRange.endDateObject.getMonth(), monthRange.endDateObject.getDate() + 1)
}

// 统一按分页拉取完整列表，保证兼容模式可以在现有列表接口上完成管理驾驶舱汇总。
export async function fetchPagedRowList(requestMethod, baseQueryParams = {}) {
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
export function getSettledResultValue(settledResult, defaultValue) {
  if (settledResult.status === "fulfilled")
  {
    return settledResult.value
  }
  return defaultValue
}

// 统一计算比率，分母为空时回落到 0，保证管理驾驶舱不会出现 NaN。
export function calculateRateValue(numeratorValue, denominatorValue) {
  const normalizedDenominatorValue = normalizeNumberValue(denominatorValue)
  if (normalizedDenominatorValue <= 0)
  {
    return 0
  }
  return normalizeNumberValue(numeratorValue) / normalizedDenominatorValue * 100
}

// 统一计算环比增长率，上一期为 0 时返回 null 以提示新增口径。
export function calculateGrowthRateValue(currentValue, previousValue) {
  const normalizedCurrentValue = normalizeNumberValue(currentValue)
  const normalizedPreviousValue = normalizeNumberValue(previousValue)
  if (normalizedPreviousValue <= 0)
  {
    return normalizedCurrentValue === 0 ? 0 : null
  }
  return (normalizedCurrentValue - normalizedPreviousValue) / normalizedPreviousValue * 100
}

// 把商品主数据转换为成本价映射，便于兼容模式快速计算销售毛利。
export function buildProductCostPriceMap(productRowList = []) {
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
export function buildCustomerNameMap(customerRowList = []) {
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
export function buildSupplierNameMap(supplierRowList = []) {
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
export function getSaleReportBusinessTime(saleReportRow) {
  return parseDateValue(getFirstDefinedValue(saleReportRow, ["saleDate", "auditTime", "createTime"]))
}

// 返回销售单的业务时间，优先使用审核时间字段。
export function getSaleOrderBusinessTime(saleOrderRow) {
  return parseDateValue(getFirstDefinedValue(saleOrderRow, ["auditTime", "createTime"]))
}

// 返回回款流水的业务时间，优先使用回款时间字段。
export function getReceiptBusinessTime(receiptRow) {
  return parseDateValue(getFirstDefinedValue(receiptRow, ["paymentTime", "receiptDate", "createTime"]))
}

// 返回采购订单的业务时间，优先使用采购日期字段。
export function getPurchaseOrderBusinessTime(purchaseOrderRow) {
  return parseDateValue(getFirstDefinedValue(purchaseOrderRow, ["purchaseDate", "auditTime", "createTime"]))
}

// 返回待审核单据的创建时间，供超时未审核计算使用。
export function getRowCreateTime(rowData) {
  return parseDateValue(getFirstDefinedValue(rowData, ["createTime", "create_time"]))
}

// 判断应收记录是否仍然未结清，便于兼容模式计算账龄与风险。
export function isOpenReceivableRow(receivableRow) {
  const receivableStatus = String(getFirstDefinedValue(receivableRow, ["status"]) || "")
  const remainAmountValue = getReceivableRemainAmountValue(receivableRow)
  return remainAmountValue > 0 && receivableStatus !== "paid" && receivableStatus !== "2"
}

// 判断应付记录是否仍然未结清，便于兼容模式计算账龄与风险。
export function isOpenPayableRow(payableRow) {
  const payableStatus = String(getFirstDefinedValue(payableRow, ["status"]) || "")
  const remainAmountValue = getPayableRemainAmountValue(payableRow)
  return remainAmountValue > 0 && payableStatus !== "2" && payableStatus !== "paid"
}

// 返回应收记录的未收金额，兼容旧字段和前端已计算字段。
export function getReceivableRemainAmountValue(receivableRow) {
  const remainAmountValue = getFirstDefinedValue(receivableRow, ["remainAmount", "remain_amount"])
  if (remainAmountValue !== undefined)
  {
    return normalizeNumberValue(remainAmountValue)
  }
  return normalizeNumberValue(getFirstDefinedValue(receivableRow, ["amountDue", "amount_due"]))
    - normalizeNumberValue(getFirstDefinedValue(receivableRow, ["amountPaid", "amount_paid"]))
}

// 返回应付记录的未付金额，兼容旧字段和前端已计算字段。
export function getPayableRemainAmountValue(payableRow) {
  const remainAmountValue = getFirstDefinedValue(payableRow, ["remainAmount", "remain_amount"])
  if (remainAmountValue !== undefined)
  {
    return normalizeNumberValue(remainAmountValue)
  }
  return normalizeNumberValue(getFirstDefinedValue(payableRow, ["amountDue", "amount_due"]))
    - normalizeNumberValue(getFirstDefinedValue(payableRow, ["amountPaid", "amount_paid"]))
}

// 判断采购订单是否应计入经营统计，避免草稿和作废单据干扰管理口径。
export function isStatisticalPurchaseOrderStatus(statusValue) {
  const normalizedStatusValue = String(statusValue || "")
  return ["1", "2", "3", "audited", "partially_inbound", "completed"].includes(normalizedStatusValue)
}

// 判断采购订单是否仍处于待审核状态，供超时未审核统计使用。
export function isPendingPurchaseOrderStatus(statusValue) {
  const normalizedStatusValue = String(statusValue || "")
  return normalizedStatusValue === "0" || normalizedStatusValue === "submitted"
}

// 判断销售、入库、出库单是否仍处于已提交待审核状态。
export function isSubmittedDocumentStatus(statusValue) {
  return String(statusValue || "") === "submitted"
}

// 统计超过 3 天仍未审核的单据数量，帮助管理层识别流程堵点。
export function countTimeoutPendingRowList(rowList = [], statusMatcher) {
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
export function buildAgingBucketList() {
  return [
    { bucketName: "未设置到期日", sortOrder: 0, amount: 0, recordCount: 0 },
    { bucketName: "未到期", sortOrder: 1, amount: 0, recordCount: 0 },
    { bucketName: "逾期1-30天", sortOrder: 2, amount: 0, recordCount: 0 },
    { bucketName: "逾期31-60天", sortOrder: 3, amount: 0, recordCount: 0 },
    { bucketName: "逾期61天以上", sortOrder: 4, amount: 0, recordCount: 0 }
  ]
}

// 根据应收台账构造账龄分布，帮助管理层查看回款风险集中区间。
export function buildReceivableAgingListFromRowList(receivableRowList = []) {
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
export function buildPayableAgingListFromRowList(payableRowList = []) {
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
export function getCustomerDisplayNameForCockpit(receivableRow, customerNameMap) {
  const customerId = getFirstDefinedValue(receivableRow, ["customerId", "customer_id"])
  const customerName = getFirstDefinedValue(receivableRow, ["customerName", "customer_name"]) || customerNameMap[customerId]
  if (customerName)
  {
    return customerName
  }
  return "客户资料缺失"
}

// 返回兼容模式下供应商展示名称，优先使用主数据名称，缺失时回退到更友好的资料缺失提示。
export function getSupplierDisplayNameForCockpit(payableRow, supplierNameMap) {
  const supplierId = getFirstDefinedValue(payableRow, ["supplierId", "supplier_id"])
  const supplierName = getFirstDefinedValue(payableRow, ["supplierName", "supplier_name"]) || supplierNameMap[supplierId]
  if (supplierName)
  {
    return supplierName
  }
  return "供应商资料缺失"
}

// 根据应收台账汇总高风险客户，帮助管理层快速锁定逾期最严重的客户。
export function buildTopRiskCustomerList(receivableRowList = [], customerNameMap = {}) {
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
export function buildTopRiskSupplierList(payableRowList = [], supplierNameMap = {}, payableRiskDays = 7) {
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
export function buildManagementOverviewData(saleReportRowList = [], receivableRowList = [], purchaseOrderRowList = [], productCostPriceMap = {}, saleOrderRowList = [], currentMonthRange, previousMonthRange) {
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
export function buildRiskOverviewData(receivableRowList = [], payableRowList = [], saleOrderRowList = [], inboundRowList = [], outboundRowList = [], purchaseOrderRowList = [], stockWarningCountValue = 0, payableRiskDays = 7) {
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
export function getPaymentBusinessTime(paymentRow) {
  return parseDateValue(getFirstDefinedValue(paymentRow, ["paymentTime", "createTime"]))
}

// 返回库存流水的业务时间，统一库存周转和滞销计算口径。
export function getStockLogBusinessTime(stockLogRow) {
  return parseDateValue(getFirstDefinedValue(stockLogRow, ["createTime", "create_time"]))
}

// 返回库存台账的参考时间，便于在无出库记录时仍能计算积压天数。
export function getStockBusinessTime(stockRow) {
  return parseDateValue(getFirstDefinedValue(stockRow, ["updateTime", "createTime", "create_time"]))
}

// 把商品主数据转换为详情映射，便于利润、库存和组织贡献模块统一取商品信息。
export function buildProductInfoMap(productRowList = []) {
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
export function buildCustomerDetailMap(customerRowList = []) {
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
export function buildSaleOrderLookupMap(saleOrderRowList = []) {
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
export function buildReceivableCustomerSummaryMap(receivableRowList = [], customerDetailMap = {}) {
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
export function extractRegionNameFromAddress(addressValue) {
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
export function getCustomerDisplayNameForDecision(customerId, fallbackCustomerName, customerDetailMap = {}) {
  if (customerId !== undefined && customerId !== null && customerId !== "")
  {
    return customerDetailMap[customerId]?.customerName || fallbackCustomerName || "客户资料缺失"
  }
  return fallbackCustomerName || "客户资料缺失"
}

// 返回决策分析场景下的商品展示名称，缺失主数据时仍保留明确提示。
export function getProductDisplayNameForDecision(productId, productInfoMap = {}) {
  if (productId !== undefined && productId !== null && productId !== "")
  {
    return productInfoMap[productId]?.productName || "商品资料缺失"
  }
  return "商品资料缺失"
}

// 根据销售报表、销售单和应收台账生成客户利润分析，帮助管理层识别高价值客户和集中度风险。
export function buildCustomerProfitAnalysisData(saleReportRowList = [], saleOrderRowList = [], customerRowList = [], receivableRowList = [], productRowList = []) {
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
export function buildInventoryDecisionAnalysisData(stockRowList = [], stockLogRowList = [], productRowList = []) {
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
export function buildContributionRankingList(groupedContributionMap = {}) {
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
export function buildOrganizationContributionAnalysisData(saleReportRowList = [], saleOrderRowList = [], customerRowList = [], productRowList = []) {
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
export function buildDecisionBudgetOverviewData(managementOverview, receiptRowList = [], paymentRowList = [], currentMonthRange) {
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
export function buildCashScenarioBaseData(receivableRowList = [], payableRowList = [], receiptRowList = [], paymentRowList = []) {
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
export function buildExecutiveBriefData(managementOverview, riskOverview, customerProfitAnalysis, inventoryDecisionAnalysis, decisionBudgetOverview) {
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

// 生成现金流情景模拟结果，帮助管理层评估不同决策对未来 30 天净现金流的影响。
export function buildSimulatedCashScenarioData(baseScenarioData = {}, currentCashScenarioForm = {}) {
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
