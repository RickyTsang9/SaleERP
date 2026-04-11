package com.ruoyi.business.service;

import java.util.List;
import java.util.Map;
import com.ruoyi.business.domain.WmsSaleOrder;
import com.ruoyi.business.domain.WmsSaleOrderStatusHistory;

public interface IWmsSaleOrderService
{
    public WmsSaleOrder selectWmsSaleOrderById(Long saleOrderId);

    public List<WmsSaleOrder> selectWmsSaleOrderList(WmsSaleOrder wmsSaleOrder);

    public int insertWmsSaleOrder(WmsSaleOrder wmsSaleOrder);

    public int updateWmsSaleOrder(WmsSaleOrder wmsSaleOrder);

    public int submitWmsSaleOrder(Long saleOrderId);

    /**
     * 经理审核销售单
     */
    public int managerAuditWmsSaleOrder(Long saleOrderId, String auditComment);

    /**
     * 经理回退销售单
     */
    public int managerRollbackWmsSaleOrder(Long saleOrderId, String auditComment);

    /**
     * 财务审核销售单
     */
    public int financeAuditWmsSaleOrder(Long saleOrderId, String auditComment);

    /**
     * 财务回退销售单
     */
    public int financeRollbackWmsSaleOrder(Long saleOrderId, String auditComment);

    public int cancelWmsSaleOrder(Long saleOrderId);

    public int deleteWmsSaleOrderById(Long saleOrderId);

    public int deleteWmsSaleOrderByIds(Long[] saleOrderIds);

    /**
     * 导入销售订单数据
     */
    public String importWmsSaleOrder(List<WmsSaleOrder> saleOrderList, boolean updateSupport, String operatorName);

    /**
     * 获取销售订单打印模板数据
     */
    public Map<String, Object> getPrintTemplateData(Long saleOrderId);

    /**
     * 查询销售单状态历史
     */
    public List<WmsSaleOrderStatusHistory> selectWmsSaleOrderStatusHistoryList(Long saleOrderId);
}
