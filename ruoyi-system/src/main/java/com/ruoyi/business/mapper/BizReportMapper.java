package com.ruoyi.business.mapper;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

public interface BizReportMapper
{
    public BigDecimal selectApprovedSaleAmount();

    public BigDecimal selectReceivableAmount();

    public BigDecimal selectReceivedAmount();

    public Long selectOverdueReceivableCount();

    public Long selectStockWarningCount();

    public List<Map<String, Object>> selectSaleTrendList();

    public List<Map<String, Object>> selectTopCustomerList();

    public Long selectPendingSaleOrderCount();

    public Long selectPendingInboundCount();

    public Long selectPendingOutboundCount();

    public List<Map<String, Object>> selectLatestNoticeList();

    public List<Map<String, Object>> selectLatestStockWarningMessageList();

    public List<Map<String, Object>> selectLatestDueReceivableList();

    public List<Map<String, Object>> selectLatestSaleOrderAuditMessageList();
}
