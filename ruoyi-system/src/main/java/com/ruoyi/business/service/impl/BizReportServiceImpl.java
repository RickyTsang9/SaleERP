package com.ruoyi.business.service.impl;

import java.math.BigDecimal;
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
    @Autowired
    private BizReportMapper bizReportMapper;

    @Override
    public AjaxResult getDashboardData()
    {
        AjaxResult ajaxResult = AjaxResult.success();
        ajaxResult.put("totalSaleAmount", nvlAmount(bizReportMapper.selectApprovedSaleAmount()));
        ajaxResult.put("totalReceivableAmount", nvlAmount(bizReportMapper.selectReceivableAmount()));
        ajaxResult.put("totalReceivedAmount", nvlAmount(bizReportMapper.selectReceivedAmount()));
        ajaxResult.put("overdueCount", bizReportMapper.selectOverdueReceivableCount());
        ajaxResult.put("stockWarningCount", bizReportMapper.selectStockWarningCount());
        ajaxResult.put("pendingSaleOrderCount", nvlCount(bizReportMapper.selectPendingSaleOrderCount()));
        ajaxResult.put("pendingInboundCount", nvlCount(bizReportMapper.selectPendingInboundCount()));
        ajaxResult.put("pendingOutboundCount", nvlCount(bizReportMapper.selectPendingOutboundCount()));
        ajaxResult.put("saleTrend", getSaleTrendData());
        ajaxResult.put("topCustomer", getTopCustomerData());
        ajaxResult.put("messageCenter", getMessageCenterData());
        return ajaxResult;
    }

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

    private BigDecimal nvlAmount(BigDecimal valueAmount)
    {
        if (valueAmount == null)
        {
            return BigDecimal.ZERO;
        }
        return valueAmount;
    }

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
