package com.ruoyi.business.mapper;

import java.math.BigDecimal;
import java.util.List;
import com.ruoyi.business.domain.WmsSaleOrder;

public interface WmsSaleOrderMapper
{
    public WmsSaleOrder selectWmsSaleOrderById(Long saleOrderId);

    public List<WmsSaleOrder> selectWmsSaleOrderList(WmsSaleOrder wmsSaleOrder);

    public int insertWmsSaleOrder(WmsSaleOrder wmsSaleOrder);

    public int updateWmsSaleOrder(WmsSaleOrder wmsSaleOrder);

    public int updateWmsSaleOrderStatus(Long saleOrderId, String currentStatus, String status, String auditBy, String financeAuditComment, String updateBy);

    public int updateWmsSaleOrderManagerAudit(Long saleOrderId, String currentStatus, String status, String managerAuditBy, String managerAuditComment, String updateBy);

    public int rollbackWmsSaleOrderToSubmitted(Long saleOrderId, String currentStatus, String updateBy);

    public int rollbackWmsSaleOrderToManagerApproved(Long saleOrderId, String currentStatus, String updateBy);

    public int updateWmsSaleOrderPaymentStatus(Long saleOrderId, String paymentStatus);

    public int deleteWmsSaleOrderById(Long saleOrderId);

    public int deleteWmsSaleOrderByIds(Long[] saleOrderIds);

    public String selectMaxOrderNoByPrefix(String noPrefix);

    public WmsSaleOrder selectWmsSaleOrderByOrderNo(String orderNo);

    public BigDecimal selectCustomerPendingApprovalAmount(Long customerId);

    public int refreshTotalBySaleOrderId(Long saleOrderId);
}
