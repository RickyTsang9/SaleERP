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

    /**
     * 销售单短装强制结案（释放未出库锁库并按实发金额调整应收）
     */
    public int forceCloseWmsSaleOrder(Long saleOrderId, String reason);

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

    /**
     * 推荐客户商品价格（协议价 -> 历史成交价 -> 客户等级折扣 -> 标准售价）并返回成本参考
     */
    public Map<String, Object> suggestCustomerProductPrice(Long customerId, Long productId);

    /**
     * 查询销售订单全链路追踪
     *
     * @param saleOrderId 销售订单主键
     * @return 销售订单链路数据
     */
    public Map<String, Object> selectWmsSaleOrderChain(Long saleOrderId);
}
