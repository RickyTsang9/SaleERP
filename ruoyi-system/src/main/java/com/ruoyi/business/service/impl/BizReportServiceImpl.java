package com.ruoyi.business.service.impl;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.ruoyi.business.mapper.BizReportMapper;
import com.ruoyi.business.service.IBizReportService;
import com.ruoyi.common.core.domain.AjaxResult;

@Service
public class BizReportServiceImpl implements IBizReportService
{
    /**
     * 日期时间格式化器
     */
    private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    /**
     * 百分比计算基数
     */
    private static final BigDecimal ONE_HUNDRED = new BigDecimal("100");

    @Autowired
    private BizReportMapper bizReportMapper;

    /**
     * 查询经营看板首页数据
     * @return 经营看板首页数据
     */
    @Override
    public AjaxResult getDashboardData()
    {
        AjaxResult ajaxResult = AjaxResult.success();
        Long stockWarningCount = nvlCount(bizReportMapper.selectStockWarningCount());
        ajaxResult.put("totalSaleAmount", nvlAmount(bizReportMapper.selectApprovedSaleAmount()));
        ajaxResult.put("totalReceivableAmount", nvlAmount(bizReportMapper.selectReceivableAmount()));
        ajaxResult.put("totalReceivedAmount", nvlAmount(bizReportMapper.selectReceivedAmount()));
        ajaxResult.put("overdueCount", bizReportMapper.selectOverdueReceivableCount());
        ajaxResult.put("stockWarningCount", stockWarningCount);
        ajaxResult.put("pendingSaleOrderCount", nvlCount(bizReportMapper.selectPendingSaleOrderCount()));
        ajaxResult.put("pendingInboundCount", nvlCount(bizReportMapper.selectPendingInboundCount()));
        ajaxResult.put("pendingOutboundCount", nvlCount(bizReportMapper.selectPendingOutboundCount()));
        ajaxResult.put("saleTrend", getSaleTrendData());
        ajaxResult.put("topCustomer", getTopCustomerData());
        ajaxResult.put("productCategorySales", getProductCategorySalesData());
        ajaxResult.put("messageCenter", getMessageCenterData());
        ajaxResult.put("managementOverview", getManagementOverviewData());
        ajaxResult.put("riskOverview", getRiskOverviewData(stockWarningCount));
        ajaxResult.put("receivableAgingList", getReceivableAgingData());
        ajaxResult.put("payableAgingList", getPayableAgingData());
        ajaxResult.put("topRiskCustomerList", getTopOverdueCustomerData());
        ajaxResult.put("topRiskSupplierList", getTopDueSupplierData());
        return ajaxResult;
    }

    /**
     * 查询销售趋势数据
     * @return 销售趋势数据
     */
    @Override
    public Map<String, Object> getSaleTrendData()
    {
        List<Map<String, Object>> saleTrendList = bizReportMapper.selectSaleTrendList();
        List<String> dateList = new ArrayList<>();
        List<BigDecimal> amountList = new ArrayList<>();
        for (Map<String, Object> rowMap : saleTrendList)
        {
            dateList.add(String.valueOf(rowMap.get("date")));
            amountList.add(convertToAmount(rowMap.get("amount")));
        }
        Map<String, Object> resultMap = new HashMap<>();
        resultMap.put("dateList", dateList);
        resultMap.put("amountList", amountList);
        return resultMap;
    }

    /**
     * 查询大客户排行数据
     * @return 大客户排行数据
     */
    @Override
    public Map<String, Object> getTopCustomerData()
    {
        List<Map<String, Object>> topCustomerList = bizReportMapper.selectTopCustomerList();
        List<String> customerList = new ArrayList<>();
        List<BigDecimal> amountList = new ArrayList<>();
        for (Map<String, Object> rowMap : topCustomerList)
        {
            customerList.add(String.valueOf(rowMap.get("customer")));
            amountList.add(convertToAmount(rowMap.get("amount")));
        }
        Map<String, Object> resultMap = new HashMap<>();
        resultMap.put("customerList", customerList);
        resultMap.put("amountList", amountList);
        return resultMap;
    }

    /**
     * 查询商品销售分类占比数据
     * @return 商品销售分类占比数据
     */
    @Override
    public List<Map<String, Object>> getProductCategorySalesData()
    {
        List<Map<String, Object>> categorySales = bizReportMapper.selectProductCategorySales();
        List<Map<String, Object>> resultList = new ArrayList<>();
        for (Map<String, Object> rowMap : categorySales)
        {
            Map<String, Object> map = new HashMap<>();
            map.put("name", rowMap.get("categoryName"));
            map.put("value", convertToAmount(rowMap.get("amount")));
            resultList.add(map);
        }
        return resultList;
    }

    /**
     * 查询销售明细报表
     * @param params 查询参数
     * @return 销售明细报表
     */
    @Override
    public List<Map<String, Object>> selectSaleReportList(Map<String, Object> params)
    {
        return bizReportMapper.selectSaleReportList(params);
    }

    /**
     * 查询对账中心列表
     * @param params 查询参数
     * @return 对账中心列表
     */
    @Override
    public List<Map<String, Object>> selectReconciliationList(Map<String, Object> params)
    {
        return bizReportMapper.selectReconciliationList(params);
    }

    /**
     * 查询发票税务报表
     * @param params 查询参数
     * @return 发票税务报表
     */
    @Override
    public List<Map<String, Object>> selectInvoiceTaxList(Map<String, Object> params)
    {
        return bizReportMapper.selectInvoiceTaxList(params);
    }

    /**
     * 查询资金预测数据
     * @param forecastDays 预测天数
     * @return 资金预测数据
     */
    @Override
    public Map<String, Object> getCashFlowForecast(Integer forecastDays)
    {
        int validForecastDays = 30;
        if (forecastDays != null && forecastDays > 0)
        {
            validForecastDays = forecastDays;
        }
        Map<String, Object> params = new HashMap<>();
        params.put("forecastDays", validForecastDays);
        Map<String, Object> forecastMap = bizReportMapper.selectCashFlowForecast(params);
        if (forecastMap == null)
        {
            forecastMap = new HashMap<>();
        }
        BigDecimal receivableAmount = convertToAmount(forecastMap.get("receivableAmount"));
        BigDecimal payableAmount = convertToAmount(forecastMap.get("payableAmount"));
        forecastMap.put("forecastDays", validForecastDays);
        forecastMap.put("receivableAmount", receivableAmount);
        forecastMap.put("payableAmount", payableAmount);
        forecastMap.put("netCashFlow", receivableAmount.subtract(payableAmount));
        return forecastMap;
    }

    /**
     * 组装管理层经营概览数据
     * @return 管理层经营概览数据
     */
    private Map<String, Object> getManagementOverviewData()
    {
        LocalDate currentDate = LocalDate.now();
        LocalDate currentMonthStartDate = currentDate.withDayOfMonth(1);
        LocalDate previousMonthStartDate = currentMonthStartDate.minusMonths(1);
        Map<String, Object> currentMonthSummaryMap = getManagementPeriodSummaryMap(currentMonthStartDate);
        Map<String, Object> previousMonthSummaryMap = getManagementPeriodSummaryMap(previousMonthStartDate);
        BigDecimal currentMonthSaleAmount = convertToAmount(currentMonthSummaryMap.get("saleAmount"));
        BigDecimal previousMonthSaleAmount = convertToAmount(previousMonthSummaryMap.get("saleAmount"));
        BigDecimal currentMonthPurchaseAmount = convertToAmount(currentMonthSummaryMap.get("purchaseAmount"));
        BigDecimal previousMonthPurchaseAmount = convertToAmount(previousMonthSummaryMap.get("purchaseAmount"));
        BigDecimal currentMonthReceivedAmount = convertToAmount(currentMonthSummaryMap.get("receivedAmount"));
        BigDecimal previousMonthReceivedAmount = convertToAmount(previousMonthSummaryMap.get("receivedAmount"));
        BigDecimal currentMonthSaleCostAmount = convertToAmount(currentMonthSummaryMap.get("saleCostAmount"));
        BigDecimal previousMonthSaleCostAmount = convertToAmount(previousMonthSummaryMap.get("saleCostAmount"));
        BigDecimal currentMonthGrossProfitAmount = currentMonthSaleAmount.subtract(currentMonthSaleCostAmount);
        BigDecimal previousMonthGrossProfitAmount = previousMonthSaleAmount.subtract(previousMonthSaleCostAmount);
        BigDecimal currentMonthGrossProfitRate = calculateRate(currentMonthGrossProfitAmount, currentMonthSaleAmount);
        BigDecimal previousMonthGrossProfitRate = calculateRate(previousMonthGrossProfitAmount, previousMonthSaleAmount);
        BigDecimal currentMonthCollectionRate = calculateRate(currentMonthReceivedAmount, currentMonthSaleAmount);
        BigDecimal previousMonthCollectionRate = calculateRate(previousMonthReceivedAmount, previousMonthSaleAmount);
        Map<String, Object> managementOverviewMap = new HashMap<>();
        managementOverviewMap.put("currentMonthSaleAmount", currentMonthSaleAmount);
        managementOverviewMap.put("previousMonthSaleAmount", previousMonthSaleAmount);
        managementOverviewMap.put("saleGrowthRate", calculateGrowthRate(currentMonthSaleAmount, previousMonthSaleAmount));
        managementOverviewMap.put("currentMonthPurchaseAmount", currentMonthPurchaseAmount);
        managementOverviewMap.put("previousMonthPurchaseAmount", previousMonthPurchaseAmount);
        managementOverviewMap.put("purchaseGrowthRate", calculateGrowthRate(currentMonthPurchaseAmount, previousMonthPurchaseAmount));
        managementOverviewMap.put("currentMonthReceivedAmount", currentMonthReceivedAmount);
        managementOverviewMap.put("previousMonthReceivedAmount", previousMonthReceivedAmount);
        managementOverviewMap.put("receivedGrowthRate", calculateGrowthRate(currentMonthReceivedAmount, previousMonthReceivedAmount));
        managementOverviewMap.put("currentMonthGrossProfitAmount", currentMonthGrossProfitAmount);
        managementOverviewMap.put("previousMonthGrossProfitAmount", previousMonthGrossProfitAmount);
        managementOverviewMap.put("grossProfitGrowthRate", calculateGrowthRate(currentMonthGrossProfitAmount, previousMonthGrossProfitAmount));
        managementOverviewMap.put("currentMonthGrossProfitRate", currentMonthGrossProfitRate);
        managementOverviewMap.put("previousMonthGrossProfitRate", previousMonthGrossProfitRate);
        managementOverviewMap.put("currentMonthCollectionRate", currentMonthCollectionRate);
        managementOverviewMap.put("previousMonthCollectionRate", previousMonthCollectionRate);
        managementOverviewMap.put("collectionRateDifference", currentMonthCollectionRate.subtract(previousMonthCollectionRate).setScale(2, RoundingMode.HALF_UP));
        return managementOverviewMap;
    }

    /**
     * 查询指定月份的经营汇总数据
     * @param monthStartDate 月初日期
     * @return 指定月份的经营汇总数据
     */
    private Map<String, Object> getManagementPeriodSummaryMap(LocalDate monthStartDate)
    {
        LocalDateTime startDateTime = monthStartDate.atStartOfDay();
        LocalDateTime endDateTime = monthStartDate.plusMonths(1).atStartOfDay();
        Map<String, Object> params = new HashMap<>();
        params.put("startTime", DATE_TIME_FORMATTER.format(startDateTime));
        params.put("endTime", DATE_TIME_FORMATTER.format(endDateTime));
        Map<String, Object> managementPeriodSummaryMap = bizReportMapper.selectManagementPeriodSummary(params);
        if (managementPeriodSummaryMap == null)
        {
            managementPeriodSummaryMap = new HashMap<>();
        }
        managementPeriodSummaryMap.put("saleAmount", convertToAmount(managementPeriodSummaryMap.get("saleAmount")));
        managementPeriodSummaryMap.put("purchaseAmount", convertToAmount(managementPeriodSummaryMap.get("purchaseAmount")));
        managementPeriodSummaryMap.put("receivedAmount", convertToAmount(managementPeriodSummaryMap.get("receivedAmount")));
        managementPeriodSummaryMap.put("saleCostAmount", convertToAmount(managementPeriodSummaryMap.get("saleCostAmount")));
        return managementPeriodSummaryMap;
    }

    /**
     * 组装管理层风险概览数据
     * @param stockWarningCount 库存预警数量
     * @return 风险概览数据
     */
    private Map<String, Object> getRiskOverviewData(Long stockWarningCount)
    {
        int payableRiskDays = 7;
        Map<String, Object> params = new HashMap<>();
        params.put("payableRiskDays", payableRiskDays);
        Map<String, Object> riskOverviewMap = bizReportMapper.selectManagementRiskOverview(params);
        if (riskOverviewMap == null)
        {
            riskOverviewMap = new HashMap<>();
        }
        riskOverviewMap.put("payableRiskDays", payableRiskDays);
        riskOverviewMap.put("overdueReceivableAmount", convertToAmount(riskOverviewMap.get("overdueReceivableAmount")));
        riskOverviewMap.put("dueSoonPayableAmount", convertToAmount(riskOverviewMap.get("dueSoonPayableAmount")));
        riskOverviewMap.put("longOverdueReceivableCount", convertToCount(riskOverviewMap.get("longOverdueReceivableCount")));
        riskOverviewMap.put("pendingApprovalTimeoutCount", convertToCount(riskOverviewMap.get("pendingApprovalTimeoutCount")));
        riskOverviewMap.put("stockWarningCount", nvlCount(stockWarningCount));
        return riskOverviewMap;
    }

    /**
     * 组装应收账龄分布数据
     * @return 应收账龄分布数据
     */
    private List<Map<String, Object>> getReceivableAgingData()
    {
        return normalizeAgingList(bizReportMapper.selectReceivableAgingList());
    }

    /**
     * 组装应付账龄分布数据
     * @return 应付账龄分布数据
     */
    private List<Map<String, Object>> getPayableAgingData()
    {
        return normalizeAgingList(bizReportMapper.selectPayableAgingList());
    }

    /**
     * 组装高风险客户列表
     * @return 高风险客户列表
     */
    private List<Map<String, Object>> getTopOverdueCustomerData()
    {
        List<Map<String, Object>> topOverdueCustomerList = bizReportMapper.selectTopOverdueCustomerList();
        return normalizeRiskTargetList(topOverdueCustomerList, "overdueDays");
    }

    /**
     * 组装重点付款供应商列表
     * @return 重点付款供应商列表
     */
    private List<Map<String, Object>> getTopDueSupplierData()
    {
        Map<String, Object> params = new HashMap<>();
        params.put("payableRiskDays", 7);
        List<Map<String, Object>> topDueSupplierList = bizReportMapper.selectTopDueSupplierList(params);
        return normalizeRiskTargetList(topDueSupplierList, "dueInDays");
    }

    /**
     * 标准化账龄数据
     * @param agingRowList 原始账龄数据
     * @return 标准化后的账龄数据
     */
    private List<Map<String, Object>> normalizeAgingList(List<Map<String, Object>> agingRowList)
    {
        List<Map<String, Object>> normalizedAgingList = new ArrayList<>();
        if (agingRowList == null)
        {
            return normalizedAgingList;
        }
        for (Map<String, Object> agingRowMap : agingRowList)
        {
            Map<String, Object> normalizedAgingMap = new HashMap<>(agingRowMap);
            normalizedAgingMap.put("amount", convertToAmount(agingRowMap.get("amount")));
            normalizedAgingMap.put("recordCount", convertToCount(agingRowMap.get("recordCount")));
            normalizedAgingList.add(normalizedAgingMap);
        }
        return normalizedAgingList;
    }

    /**
     * 标准化风险对象列表
     * @param riskTargetRowList 风险对象列表
     * @param dayFieldName 天数字段名称
     * @return 标准化后的风险对象列表
     */
    private List<Map<String, Object>> normalizeRiskTargetList(List<Map<String, Object>> riskTargetRowList, String dayFieldName)
    {
        List<Map<String, Object>> normalizedRiskTargetList = new ArrayList<>();
        if (riskTargetRowList == null)
        {
            return normalizedRiskTargetList;
        }
        for (Map<String, Object> riskTargetRowMap : riskTargetRowList)
        {
            Map<String, Object> normalizedRiskTargetMap = new HashMap<>(riskTargetRowMap);
            normalizedRiskTargetMap.put("remainAmount", convertToAmount(riskTargetRowMap.get("remainAmount")));
            normalizedRiskTargetMap.put(dayFieldName, convertToCount(riskTargetRowMap.get(dayFieldName)));
            normalizedRiskTargetMap.put("recordCount", convertToCount(riskTargetRowMap.get("recordCount")));
            normalizedRiskTargetList.add(normalizedRiskTargetMap);
        }
        return normalizedRiskTargetList;
    }

    /**
     * 计算环比增长率
     * @param currentValue 当前值
     * @param previousValue 上期值
     * @return 环比增长率
     */
    private BigDecimal calculateGrowthRate(BigDecimal currentValue, BigDecimal previousValue)
    {
        if (previousValue == null || previousValue.compareTo(BigDecimal.ZERO) == 0)
        {
            return null;
        }
        return currentValue.subtract(previousValue)
            .divide(previousValue, 4, RoundingMode.HALF_UP)
            .multiply(ONE_HUNDRED)
            .setScale(2, RoundingMode.HALF_UP);
    }

    /**
     * 计算百分比指标
     * @param numeratorValue 分子
     * @param denominatorValue 分母
     * @return 百分比指标
     */
    private BigDecimal calculateRate(BigDecimal numeratorValue, BigDecimal denominatorValue)
    {
        if (denominatorValue == null || denominatorValue.compareTo(BigDecimal.ZERO) == 0)
        {
            return BigDecimal.ZERO;
        }
        return numeratorValue.divide(denominatorValue, 4, RoundingMode.HALF_UP)
            .multiply(ONE_HUNDRED)
            .setScale(2, RoundingMode.HALF_UP);
    }

    /**
     * 空值转金额
     * @param valueAmount 原始金额
     * @return 转换后的金额
     */
    private BigDecimal nvlAmount(BigDecimal valueAmount)
    {
        if (valueAmount == null)
        {
            return BigDecimal.ZERO;
        }
        return valueAmount;
    }

    /**
     * 对象转金额
     * @param amountValue 原始金额对象
     * @return 转换后的金额
     */
    private BigDecimal convertToAmount(Object amountValue)
    {
        if (amountValue == null)
        {
            return BigDecimal.ZERO;
        }
        if (amountValue instanceof BigDecimal)
        {
            return (BigDecimal) amountValue;
        }
        return new BigDecimal(String.valueOf(amountValue));
    }

    /**
     * 空值转数量
     * @param countValue 原始数量
     * @return 转换后的数量
     */
    private Long nvlCount(Long countValue)
    {
        if (countValue == null)
        {
            return 0L;
        }
        return countValue;
    }

    /**
     * 对象转数量
     * @param countValue 原始数量对象
     * @return 转换后的数量
     */
    private Long convertToCount(Object countValue)
    {
        if (countValue == null)
        {
            return 0L;
        }
        if (countValue instanceof Number)
        {
            return ((Number) countValue).longValue();
        }
        return Long.valueOf(String.valueOf(countValue));
    }

    /**
     * 组装消息中心数据
     */
    private List<Map<String, Object>> getMessageCenterData()
    {
        int maxMessageCount = 10;
        List<Map<String, Object>> messageCenterList = new ArrayList<>();
        List<Map<String, Object>> saleOrderAuditMessageList = bizReportMapper.selectLatestSaleOrderAuditMessageList();
        for (Map<String, Object> messageMap : saleOrderAuditMessageList)
        {
            String messageLevel = String.valueOf(messageMap.get("message_level"));
            if ("warning".equals(messageLevel))
            {
                messageMap.put("messageLevel", "warning");
            }
            else
            {
                messageMap.put("messageLevel", "normal");
            }
            messageCenterList.add(messageMap);
        }
        List<Map<String, Object>> stockWarningMessageList = bizReportMapper.selectLatestStockWarningMessageList();
        for (Map<String, Object> messageMap : stockWarningMessageList)
        {
            messageMap.put("messageLevel", "warning");
            messageCenterList.add(messageMap);
        }
        List<Map<String, Object>> noticeMessageList = bizReportMapper.selectLatestNoticeList();
        for (Map<String, Object> messageMap : noticeMessageList)
        {
            messageMap.put("messageLevel", "normal");
            messageCenterList.add(messageMap);
        }
        List<Map<String, Object>> dueReceivableMessageList = bizReportMapper.selectLatestDueReceivableList();
        for (Map<String, Object> messageMap : dueReceivableMessageList)
        {
            messageMap.put("messageLevel", "warning");
            messageCenterList.add(messageMap);
        }
        messageCenterList.sort(Comparator.comparingLong(this::getMessageTime).reversed());
        if (messageCenterList.size() > maxMessageCount)
        {
            return new ArrayList<>(messageCenterList.subList(0, maxMessageCount));
        }
        return messageCenterList;
    }

    /**
     * 获取消息时间戳
     * @param messageMap 消息数据
     * @return 消息时间戳
     */
    private long getMessageTime(Map<String, Object> messageMap)
    {
        Object messageTimeObj = messageMap.get("message_time");
        if (messageTimeObj == null)
        {
            return 0L;
        }
        if (messageTimeObj instanceof Date)
        {
            return ((Date) messageTimeObj).getTime();
        }
        return 0L;
    }
}
