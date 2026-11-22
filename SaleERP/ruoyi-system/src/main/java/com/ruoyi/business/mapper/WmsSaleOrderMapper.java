package com.ruoyi.business.mapper;

import java.math.BigDecimal;
import java.util.List;
import com.ruoyi.business.domain.WmsSaleOrder;

public interface WmsSaleOrderMapper
{
    /**
     * 查询销售订单
     *
     * @param saleOrderId 销售订单主键
     * @return 销售订单
     */
    public WmsSaleOrder selectWmsSaleOrderById(Long saleOrderId);

    /**
     * 查询销售订单列表
     *
     * @param wmsSaleOrder 销售订单
     * @return 销售订单集合
     */
    public List<WmsSaleOrder> selectWmsSaleOrderList(WmsSaleOrder wmsSaleOrder);

    /**
     * 根据销售单号集合查询导入所需销售订单
     *
     * @param orderNoList 销售单号集合
     * @return 销售订单集合
     */
    public List<WmsSaleOrder> selectWmsSaleOrderImportListByOrderNos(List<String> orderNoList);

    /**
     * 新增销售订单
     *
     * @param wmsSaleOrder 销售订单
     * @return 结果
     */
    public int insertWmsSaleOrder(WmsSaleOrder wmsSaleOrder);

    /**
     * 修改销售订单
     *
     * @param wmsSaleOrder 销售订单
     * @return 结果
     */
    public int updateWmsSaleOrder(WmsSaleOrder wmsSaleOrder);

    /**
     * 更新销售订单状态
     *
     * @param saleOrderId 销售订单主键
     * @param currentStatus 当前状态
     * @param status 目标状态
     * @param auditBy 审核人
     * @param financeAuditComment 财务审核意见
     * @param updateBy 更新人
     * @return 结果
     */
    public int updateWmsSaleOrderStatus(Long saleOrderId, String currentStatus, String status, String auditBy, String financeAuditComment, String updateBy);

    /**
     * 更新销售订单经理审核状态
     *
     * @param saleOrderId 销售订单主键
     * @param currentStatus 当前状态
     * @param status 目标状态
     * @param managerAuditBy 经理审核人
     * @param managerAuditComment 经理审核意见
     * @param updateBy 更新人
     * @return 结果
     */
    public int updateWmsSaleOrderManagerAudit(Long saleOrderId, String currentStatus, String status, String managerAuditBy, String managerAuditComment, String updateBy);

    /**
     * 回退销售订单到已提交状态
     *
     * @param saleOrderId 销售订单主键
     * @param currentStatus 当前状态
     * @param updateBy 更新人
     * @return 结果
     */
    public int rollbackWmsSaleOrderToSubmitted(Long saleOrderId, String currentStatus, String updateBy);

    /**
     * 回退销售订单到经理已审状态
     *
     * @param saleOrderId 销售订单主键
     * @param currentStatus 当前状态
     * @param updateBy 更新人
     * @return 结果
     */
    public int rollbackWmsSaleOrderToManagerApproved(Long saleOrderId, String currentStatus, String updateBy);

    /**
     * 更新销售订单回款状态
     *
     * @param saleOrderId 销售订单主键
     * @param paymentStatus 回款状态
     * @return 结果
     */
    public int updateWmsSaleOrderPaymentStatus(Long saleOrderId, String paymentStatus);

    /**
     * 删除销售订单
     *
     * @param saleOrderId 销售订单主键
     * @return 结果
     */
    public int deleteWmsSaleOrderById(Long saleOrderId);

    /**
     * 批量删除销售订单
     *
     * @param saleOrderIds 销售订单主键集合
     * @return 结果
     */
    public int deleteWmsSaleOrderByIds(Long[] saleOrderIds);

    /**
     * 根据前缀查询最大销售单号
     *
     * @param noPrefix 单号前缀
     * @return 最大销售单号
     */
    public String selectMaxOrderNoByPrefix(String noPrefix);

    /**
     * 根据销售单号查询销售订单
     *
     * @param orderNo 销售单号
     * @return 销售订单
     */
    public WmsSaleOrder selectWmsSaleOrderByOrderNo(String orderNo);

    /**
     * 查询客户待审批金额
     *
     * @param customerId 客户主键
     * @return 待审批金额
     */
    public BigDecimal selectCustomerPendingApprovalAmount(Long customerId);

    /**
     * 刷新销售订单汇总金额
     *
     * @param saleOrderId 销售订单主键
     * @return 结果
     */
    public int refreshTotalBySaleOrderId(Long saleOrderId);
}
