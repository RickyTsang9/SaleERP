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
import java.util.Set;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.ruoyi.business.domain.MdCustomer;
import com.ruoyi.business.domain.MdSupplier;
import com.ruoyi.business.mapper.BizReportMapper;
import com.ruoyi.business.mapper.MdCustomerMapper;
import com.ruoyi.business.mapper.MdSupplierMapper;
import com.ruoyi.business.service.IBizReportService;
import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.common.core.domain.model.LoginUser;
import com.ruoyi.common.exception.ServiceException;
import com.ruoyi.common.utils.SecurityUtils;

@Service
public class BizReportServiceImpl implements IBizReportService
{
    /**
     * 默认待办提醒天数
     */
    private static final int DEFAULT_TODO_DUE_DAYS = 7;

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

    @Autowired
    private MdCustomerMapper mdCustomerMapper;

    @Autowired
    private MdSupplierMapper mdSupplierMapper;

    @Autowired(required = false)
    private com.ruoyi.common.core.redis.RedisCache redisCache;

    /**
     * 查询经营看板首页数据（含 15 秒短 TTL 按用户/权限级缓存，防止首页与看板并发刷新打爆数据库）
     * @return 经营看板首页数据
     */
    @Override
    public AjaxResult getDashboardData()
    {
        String cacheKey = null;
        if (redisCache != null)
        {
            try
            {
                Long uid = SecurityUtils.getUserId();
                boolean profitPerm = SecurityUtils.isAdmin(uid) || SecurityUtils.hasPermi("business:report:profit");
                cacheKey = "biz:dashboard:v3:" + uid + ":" + (profitPerm ? "1" : "0");
                Map<String, Object> cached = redisCache.getCacheObject(cacheKey);
                if (cached != null && !cached.isEmpty())
                {
                    AjaxResult hit = AjaxResult.success();
                    hit.putAll(cached);
                    return hit;
                }
            }
            catch (Exception ignored)
            {
                cacheKey = null;
            }
        }
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
        if (redisCache != null && cacheKey != null)
        {
            try
            {
                redisCache.setCacheObject(cacheKey, new HashMap<>(ajaxResult), 15, java.util.concurrent.TimeUnit.SECONDS);
            }
            catch (Exception ignored) {}
        }
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
        List<Map<String, Object>> rows = bizReportMapper.selectSaleReportList(params);
        if (rows == null || rows.isEmpty())
        {
            return rows;
        }
        if (!canCurrentUserViewProfit())
        {
            for (Map<String, Object> row : rows)
            {
                row.put("costPrice", null);
                row.put("costAmount", null);
                row.put("grossProfit", null);
            }
        }
        return rows;
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
     * 查询统一待办中心数据（按当前登录角色权限过滤）
     * @return 统一待办中心数据
     */
    @Override
    public Map<String, Object> getTodoCenterData()
    {
        Map<String, Object> params = new HashMap<>();
        params.put("dueDays", DEFAULT_TODO_DUE_DAYS);
        try
        {
            LoginUser loginUser = SecurityUtils.getLoginUser();
            if (loginUser != null && !SecurityUtils.isAdmin(loginUser.getUserId()))
            {
                Set<String> perms = loginUser.getPermissions();
                boolean isManager = perms != null && (perms.contains("*:*:*") || perms.contains("business:saleOrder:managerAudit"));
                if (!isManager && com.ruoyi.common.utils.StringUtils.isNotEmpty(loginUser.getUsername()))
                {
                    params.put("followOwnerUsername", loginUser.getUsername());
                }
            }
        }
        catch (Exception ignored) {}
        List<Map<String, Object>> rawList = normalizeTodoCenterList(bizReportMapper.selectTodoCenterList(params));
        List<Map<String, Object>> todoCenterList = filterTodoListByCurrentUserPermissions(rawList);
        Map<String, Object> todoCenterDataMap = new HashMap<>();
        todoCenterDataMap.put("dueDays", DEFAULT_TODO_DUE_DAYS);
        todoCenterDataMap.put("todoList", todoCenterList);
        todoCenterDataMap.put("summary", buildTodoCenterSummary(todoCenterList));
        return todoCenterDataMap;
    }

    private List<Map<String, Object>> filterTodoListByCurrentUserPermissions(List<Map<String, Object>> todoList)
    {
        if (todoList == null || todoList.isEmpty())
        {
            return new ArrayList<>();
        }
        Set<String> perms;
        try
        {
            LoginUser loginUser = SecurityUtils.getLoginUser();
            if (loginUser == null || SecurityUtils.isAdmin(loginUser.getUserId()))
            {
                return todoList;
            }
            perms = loginUser.getPermissions();
            if (perms != null && perms.contains("*:*:*"))
            {
                return todoList;
            }
        }
        catch (Exception ignored)
        {
            return todoList;
        }
        if (perms == null || perms.isEmpty())
        {
            return new ArrayList<>();
        }
        List<Map<String, Object>> filtered = new ArrayList<>();
        for (Map<String, Object> item : todoList)
        {
            String todoType = String.valueOf(item.get("todoType"));
            if (hasPermissionForTodoType(perms, todoType))
            {
                filtered.add(item);
            }
        }
        return filtered;
    }

    private boolean hasPermissionForTodoType(Set<String> perms, String todoType)
    {
        if ("sale_manager_audit".equals(todoType))
        {
            return perms.contains("business:saleOrder:managerAudit") || perms.contains("business:saleOrder:list");
        }
        if ("sale_finance_audit".equals(todoType))
        {
            return perms.contains("business:saleOrder:financeAudit") || perms.contains("business:receivable:list");
        }
        if ("purchase_audit".equals(todoType))
        {
            return perms.contains("business:purchaseOrder:audit") || perms.contains("business:purchaseOrder:list");
        }
        if ("inbound_audit".equals(todoType))
        {
            return perms.contains("business:inbound:audit") || perms.contains("business:inbound:list");
        }
        if ("outbound_audit".equals(todoType))
        {
            return perms.contains("business:outbound:audit") || perms.contains("business:outbound:list");
        }
        if ("purchase_return_audit".equals(todoType))
        {
            return perms.contains("business:purchaseReturn:audit") || perms.contains("business:purchaseReturn:list");
        }
        if ("sale_return_audit".equals(todoType))
        {
            return perms.contains("business:saleReturn:audit") || perms.contains("business:saleReturn:list");
        }
        if ("inventory_check_audit".equals(todoType))
        {
            return perms.contains("business:inventoryCheck:audit") || perms.contains("business:inventoryCheck:list");
        }
        if ("transfer_audit".equals(todoType) || "transfer_receive".equals(todoType))
        {
            return perms.contains("business:transfer:audit") || perms.contains("business:transfer:edit") || perms.contains("business:transfer:list");
        }
        if ("red_refund_pending".equals(todoType))
        {
            return perms.contains("business:receipt:add") || perms.contains("business:payment:add")
                || perms.contains("business:receivable:list") || perms.contains("business:payable:list");
        }
        if ("order_invoice_pending".equals(todoType))
        {
            return perms.contains("business:invoice:add") || perms.contains("business:invoice:list");
        }
        if ("stock_warning".equals(todoType) || "stock_expiring".equals(todoType))
        {
            return perms.contains("business:stock:list");
        }
        if ("receivable_overdue".equals(todoType))
        {
            return perms.contains("business:receivable:list") || perms.contains("business:saleOrder:list");
        }
        if ("payable_due".equals(todoType))
        {
            return perms.contains("business:payable:list");
        }
        if ("customer_follow_due".equals(todoType))
        {
            return perms.contains("business:customerFollow:list");
        }
        if ("budget_approve".equals(todoType) || "executive_action".equals(todoType))
        {
            return perms.contains("business:report:view");
        }
        return true;
    }

    private boolean canCurrentUserViewProfit()
    {
        try
        {
            LoginUser loginUser = SecurityUtils.getLoginUser();
            if (loginUser == null || SecurityUtils.isAdmin(loginUser.getUserId()))
            {
                return true;
            }
            Set<String> perms = loginUser.getPermissions();
            return perms != null && (perms.contains("*:*:*") || perms.contains("business:report:profit"));
        }
        catch (Exception ignored)
        {
            return true;
        }
    }

    /**
     * 查询标准往来对账单明细
     * @param params 查询参数（reconcileType, targetId, startDate, endDate）
     * @return 标准往来对账单数据
     */
    @Override
    public Map<String, Object> getReconciliationStatement(Map<String, Object> params)
    {
        if (params == null)
        {
            throw new ServiceException("对账单查询参数不能为空");
        }
        String reconcileType = params.get("reconcileType") == null ? "" : String.valueOf(params.get("reconcileType")).trim();
        Object targetIdObj = params.get("targetId");
        if (!"receivable".equals(reconcileType) && !"payable".equals(reconcileType))
        {
            throw new ServiceException("对账类型无效，必须为 receivable 或 payable");
        }
        if (targetIdObj == null || String.valueOf(targetIdObj).trim().isEmpty())
        {
            throw new ServiceException("往来单位编号不能为空");
        }
        Long targetId;
        try
        {
            targetId = Long.valueOf(String.valueOf(targetIdObj).trim());
        }
        catch (NumberFormatException ex)
        {
            throw new ServiceException("往来单位编号格式错误");
        }

        Map<String, Object> queryParams = new HashMap<>();
        queryParams.put("targetId", targetId);
        if (params.get("startDate") != null && !String.valueOf(params.get("startDate")).trim().isEmpty())
        {
            queryParams.put("startDate", String.valueOf(params.get("startDate")).trim());
        }
        if (params.get("endDate") != null && !String.valueOf(params.get("endDate")).trim().isEmpty())
        {
            queryParams.put("endDate", String.valueOf(params.get("endDate")).trim());
        }

        String targetName = "";
        String targetCode = "";
        String contactPerson = "";
        String contactPhone = "";
        List<Map<String, Object>> billList;
        List<Map<String, Object>> settlementList;
        if ("receivable".equals(reconcileType))
        {
            MdCustomer customer = mdCustomerMapper.selectMdCustomerById(targetId);
            if (customer != null)
            {
                targetName = customer.getCustomerName();
                targetCode = customer.getCustomerCode();
                contactPerson = customer.getContactPerson();
                contactPhone = customer.getContactPhone();
            }
            else
            {
                targetName = "客户资料缺失";
            }
            billList = bizReportMapper.selectCustomerReconciliationBills(queryParams);
            settlementList = bizReportMapper.selectCustomerReconciliationReceipts(queryParams);
        }
        else
        {
            MdSupplier supplier = mdSupplierMapper.selectMdSupplierBySupplierId(targetId);
            if (supplier != null)
            {
                targetName = supplier.getSupplierName();
                targetCode = supplier.getSupplierCode();
                contactPerson = supplier.getContactPerson();
                contactPhone = supplier.getContactPhone();
            }
            else
            {
                targetName = "供应商资料缺失";
            }
            billList = bizReportMapper.selectSupplierReconciliationBills(queryParams);
            settlementList = bizReportMapper.selectSupplierReconciliationPayments(queryParams);
        }

        if (billList == null)
        {
            billList = new ArrayList<>();
        }
        if (settlementList == null)
        {
            settlementList = new ArrayList<>();
        }

        BigDecimal totalDueAmount = BigDecimal.ZERO;
        BigDecimal totalPaidAmount = BigDecimal.ZERO;
        BigDecimal totalRemainAmount = BigDecimal.ZERO;
        BigDecimal overdueAmount = BigDecimal.ZERO;
        for (Map<String, Object> bill : billList)
        {
            BigDecimal due = convertToAmount(bill.get("amountDue"));
            BigDecimal paid = convertToAmount(bill.get("amountPaid"));
            BigDecimal remain = convertToAmount(bill.get("remainAmount"));
            totalDueAmount = totalDueAmount.add(due);
            totalPaidAmount = totalPaidAmount.add(paid);
            totalRemainAmount = totalRemainAmount.add(remain);
            long overdueDays = bill.get("overdueDays") instanceof Number ? ((Number) bill.get("overdueDays")).longValue() : 0L;
            if (overdueDays > 0 && remain.compareTo(BigDecimal.ZERO) > 0)
            {
                overdueAmount = overdueAmount.add(remain);
            }
            bill.put("statusLabel", formatReconcileBillStatus(reconcileType, bill.get("status")));
        }

        BigDecimal totalSettlementAmount = BigDecimal.ZERO;
        for (Map<String, Object> item : settlementList)
        {
            String itemStatus = item.get("status") == null ? "normal" : String.valueOf(item.get("status"));
            if (!"voided".equalsIgnoreCase(itemStatus))
            {
                totalSettlementAmount = totalSettlementAmount.add(convertToAmount(item.get("amount")));
                item.put("statusLabel", "正常");
            }
            else
            {
                item.put("statusLabel", "已冲红");
            }
            item.put("paymentMethodLabel", formatPaymentMethodLabel(item.get("paymentMethod")));
        }

        Map<String, Object> result = new HashMap<>();
        result.put("reconcileType", reconcileType);
        result.put("reconcileTypeLabel", "receivable".equals(reconcileType) ? "客户应收对账单" : "供应商应付对账单");
        result.put("targetId", targetId);
        result.put("targetName", targetName);
        result.put("targetCode", targetCode);
        result.put("contactPerson", contactPerson);
        result.put("contactPhone", contactPhone);
        result.put("statementDate", LocalDate.now().toString());
        result.put("startDate", queryParams.getOrDefault("startDate", ""));
        result.put("endDate", queryParams.getOrDefault("endDate", ""));
        result.put("totalDueAmount", totalDueAmount);
        result.put("totalPaidAmount", totalPaidAmount);
        result.put("totalRemainAmount", totalRemainAmount);
        result.put("overdueAmount", overdueAmount);
        result.put("totalSettlementAmount", totalSettlementAmount);
        result.put("billList", billList);
        result.put("settlementList", settlementList);
        return result;
    }

    private String formatReconcileBillStatus(String reconcileType, Object statusObj)
    {
        String status = statusObj == null ? "" : String.valueOf(statusObj);
        if ("receivable".equals(reconcileType))
        {
            if ("unpaid".equals(status)) return "未收款";
            if ("partial".equals(status)) return "部分收款";
            if ("paid".equals(status)) return "已结清";
            return status;
        }
        if ("0".equals(status)) return "未付款";
        if ("1".equals(status)) return "部分付款";
        if ("2".equals(status)) return "已结清";
        return status;
    }

    private String formatPaymentMethodLabel(Object methodObj)
    {
        String method = methodObj == null ? "" : String.valueOf(methodObj);
        if ("bank".equals(method) || "bank_transfer".equals(method)) return "银行转账";
        if ("alipay".equals(method)) return "支付宝";
        if ("wechat".equals(method)) return "微信支付";
        if ("cash".equals(method)) return "现金";
        if ("check".equals(method)) return "支票";
        return method.isEmpty() ? "未指定" : method;
    }

    /**
     * 查询商品经营排行分析（销量/销售额/毛利/退货率/库存周转率）
     * @param params 查询参数
     * @return 商品经营排行列表
     */
    @Override
    public List<Map<String, Object>> selectProductRankingList(Map<String, Object> params)
    {
        Map<String, Object> queryParams = params == null ? new HashMap<>() : params;
        List<Map<String, Object>> rawList = bizReportMapper.selectProductRankingList(queryParams);
        List<Map<String, Object>> resultList = new ArrayList<>();
        if (rawList == null)
        {
            return resultList;
        }
        for (Map<String, Object> row : rawList)
        {
            Map<String, Object> item = new HashMap<>(row);
            BigDecimal costPrice = convertToAmount(row.get("costPrice"));
            BigDecimal saleQty = convertToAmount(row.get("saleQty"));
            BigDecimal saleAmount = convertToAmount(row.get("saleAmount"));
            BigDecimal saleCostAmount = convertToAmount(row.get("saleCostAmount"));
            BigDecimal returnQty = convertToAmount(row.get("returnQty"));
            BigDecimal returnAmount = convertToAmount(row.get("returnAmount"));
            BigDecimal returnCostAmount = convertToAmount(row.get("returnCostAmount"));
            BigDecimal stockQty = convertToAmount(row.get("stockQty"));

            BigDecimal netSaleQty = saleQty.subtract(returnQty);
            BigDecimal netSaleAmount = saleAmount.subtract(returnAmount).setScale(2, RoundingMode.HALF_UP);
            BigDecimal estimatedCostAmount = row.get("saleCostAmount") != null
                ? saleCostAmount.subtract(returnCostAmount).setScale(2, RoundingMode.HALF_UP)
                : netSaleQty.max(BigDecimal.ZERO).multiply(costPrice).setScale(2, RoundingMode.HALF_UP);
            BigDecimal grossProfitAmount = netSaleAmount.subtract(estimatedCostAmount).setScale(2, RoundingMode.HALF_UP);
            BigDecimal grossMarginRate = netSaleAmount.compareTo(BigDecimal.ZERO) > 0
                ? grossProfitAmount.multiply(ONE_HUNDRED).divide(netSaleAmount, 2, RoundingMode.HALF_UP)
                : BigDecimal.ZERO;
            BigDecimal returnRate = saleQty.compareTo(BigDecimal.ZERO) > 0
                ? returnQty.multiply(ONE_HUNDRED).divide(saleQty, 2, RoundingMode.HALF_UP)
                : BigDecimal.ZERO;
            BigDecimal turnoverRate = stockQty.compareTo(BigDecimal.ZERO) > 0
                ? netSaleQty.max(BigDecimal.ZERO).divide(stockQty, 2, RoundingMode.HALF_UP)
                : (netSaleQty.compareTo(BigDecimal.ZERO) > 0 ? netSaleQty.setScale(2, RoundingMode.HALF_UP) : BigDecimal.ZERO);

            item.put("saleQty", saleQty);
            item.put("saleAmount", netSaleAmount);
            item.put("grossSaleAmount", saleAmount);
            if (canCurrentUserViewProfit())
            {
                item.put("estimatedCostAmount", estimatedCostAmount);
                item.put("grossProfitAmount", grossProfitAmount);
                item.put("grossMarginRate", grossMarginRate);
            }
            else
            {
                item.put("costPrice", null);
                item.put("estimatedCostAmount", null);
                item.put("grossProfitAmount", null);
                item.put("grossMarginRate", null);
            }
            item.put("returnQty", returnQty);
            item.put("returnAmount", returnAmount);
            item.put("returnRate", returnRate);
            item.put("stockQty", stockQty);
            item.put("turnoverRate", turnoverRate);
            resultList.add(item);
        }

        String sortBy = queryParams.get("sortBy") == null ? "saleAmount" : String.valueOf(queryParams.get("sortBy")).trim();
        resultList.sort((a, b) -> {
            BigDecimal valA = convertToAmount(a.get(sortBy));
            BigDecimal valB = convertToAmount(b.get(sortBy));
            int cmp = valB.compareTo(valA);
            if (cmp != 0)
            {
                return cmp;
            }
            return convertToAmount(b.get("saleAmount")).compareTo(convertToAmount(a.get("saleAmount")));
        });
        return resultList;
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
        if (canCurrentUserViewProfit())
        {
            managementOverviewMap.put("currentMonthGrossProfitAmount", currentMonthGrossProfitAmount);
            managementOverviewMap.put("previousMonthGrossProfitAmount", previousMonthGrossProfitAmount);
            managementOverviewMap.put("grossProfitGrowthRate", calculateGrowthRate(currentMonthGrossProfitAmount, previousMonthGrossProfitAmount));
            managementOverviewMap.put("currentMonthGrossProfitRate", currentMonthGrossProfitRate);
            managementOverviewMap.put("previousMonthGrossProfitRate", previousMonthGrossProfitRate);
        }
        else
        {
            managementOverviewMap.put("currentMonthGrossProfitAmount", null);
            managementOverviewMap.put("previousMonthGrossProfitAmount", null);
            managementOverviewMap.put("grossProfitGrowthRate", null);
            managementOverviewMap.put("currentMonthGrossProfitRate", null);
            managementOverviewMap.put("previousMonthGrossProfitRate", null);
        }
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
     * 标准化统一待办列表
     * @param todoCenterRowList 原始待办列表
     * @return 标准化后的统一待办列表
     */
    private List<Map<String, Object>> normalizeTodoCenterList(List<Map<String, Object>> todoCenterRowList)
    {
        List<Map<String, Object>> normalizedTodoCenterList = new ArrayList<>();
        if (todoCenterRowList == null)
        {
            return normalizedTodoCenterList;
        }
        for (Map<String, Object> todoCenterRowMap : todoCenterRowList)
        {
            Map<String, Object> normalizedTodoCenterMap = new HashMap<>(todoCenterRowMap);
            normalizedTodoCenterMap.put("todoAmount", convertToAmount(todoCenterRowMap.get("todoAmount")));
            normalizedTodoCenterMap.put("remainAmount", convertToAmount(todoCenterRowMap.get("remainAmount")));
            normalizedTodoCenterMap.put("overdueDays", convertToCount(todoCenterRowMap.get("overdueDays")));
            normalizedTodoCenterMap.put("routeQueryKey", todoCenterRowMap.get("routeQueryKey"));
            normalizedTodoCenterMap.put("routeQueryValue", todoCenterRowMap.get("routeQueryValue"));
            normalizedTodoCenterList.add(normalizedTodoCenterMap);
        }
        return normalizedTodoCenterList;
    }

    /**
     * 组装统一待办汇总数据
     * @param todoCenterList 待办列表
     * @return 待办汇总数据
     */
    private Map<String, Object> buildTodoCenterSummary(List<Map<String, Object>> todoCenterList)
    {
        Map<String, Object> summaryMap = new HashMap<>();
        long totalCount = 0L;
        long urgentCount = 0L;
        long highCount = 0L;
        long financeCount = 0L;
        long approvalCount = 0L;
        for (Map<String, Object> todoCenterMap : todoCenterList)
        {
            totalCount++;
            String priorityLevel = String.valueOf(todoCenterMap.get("priorityLevel"));
            String todoCategory = String.valueOf(todoCenterMap.get("todoCategory"));
            if ("urgent".equals(priorityLevel))
            {
                urgentCount++;
            }
            if ("high".equals(priorityLevel))
            {
                highCount++;
            }
            if ("finance".equals(todoCategory))
            {
                financeCount++;
            }
            if ("approval".equals(todoCategory))
            {
                approvalCount++;
            }
        }
        summaryMap.put("totalCount", totalCount);
        summaryMap.put("urgentCount", urgentCount);
        summaryMap.put("highCount", highCount);
        summaryMap.put("financeCount", financeCount);
        summaryMap.put("approvalCount", approvalCount);
        return summaryMap;
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
