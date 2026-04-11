package com.ruoyi.business.service;

import java.util.List;
import com.ruoyi.business.domain.WmsPurchaseOrder;

/**
 * 采购订单Service接口
 * 
 * @author ruoyi
 * @date 2026-03-23
 */
public interface IWmsPurchaseOrderService 
{
    /**
     * 查询采购订单
     * 
     * @param purchaseOrderId 采购订单主键
     * @return 采购订单
     */
    public WmsPurchaseOrder selectWmsPurchaseOrderByPurchaseOrderId(Long purchaseOrderId);

    /**
     * 查询采购订单列表
     * 
     * @param wmsPurchaseOrder 采购订单
     * @return 采购订单集合
     */
    public List<WmsPurchaseOrder> selectWmsPurchaseOrderList(WmsPurchaseOrder wmsPurchaseOrder);

    /**
     * 新增采购订单
     * 
     * @param wmsPurchaseOrder 采购订单
     * @return 结果
     */
    public int insertWmsPurchaseOrder(WmsPurchaseOrder wmsPurchaseOrder);

    /**
     * 修改采购订单
     * 
     * @param wmsPurchaseOrder 采购订单
     * @return 结果
     */
    public int updateWmsPurchaseOrder(WmsPurchaseOrder wmsPurchaseOrder);

    /**
     * 提交采购订单
     * 
     * @param purchaseOrderId 采购订单主键
     * @return 结果
     */
    public int submitWmsPurchaseOrder(Long purchaseOrderId);

    /**
     * 审核采购订单
     * 
     * @param purchaseOrderId 采购订单主键
     * @return 结果
     */
    public int auditWmsPurchaseOrder(Long purchaseOrderId);

    /**
     * 作废采购订单
     * 
     * @param purchaseOrderId 采购订单主键
     * @return 结果
     */
    public int cancelWmsPurchaseOrder(Long purchaseOrderId);

    /**
     * 校验采购入库关联的采购订单是否合法
     *
     * @param purchaseOrderId 采购订单主键
     * @param supplierId 供应商主键
     * @return 采购订单
     */
    public WmsPurchaseOrder validatePurchaseOrderForInbound(Long purchaseOrderId, Long supplierId);

    /**
     * 根据采购入库进度刷新采购订单状态
     *
     * @param purchaseOrderId 采购订单主键
     */
    public void refreshPurchaseOrderInboundProgress(Long purchaseOrderId);

    /**
     * 批量删除采购订单
     * 
     * @param purchaseOrderIds 需要删除的采购订单主键集合
     * @return 结果
     */
    public int deleteWmsPurchaseOrderByPurchaseOrderIds(Long[] purchaseOrderIds);

    /**
     * 删除采购订单信息
     * 
     * @param purchaseOrderId 采购订单主键
     * @return 结果
     */
    public int deleteWmsPurchaseOrderByPurchaseOrderId(Long purchaseOrderId);
}
