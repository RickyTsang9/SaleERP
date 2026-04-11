package com.ruoyi.business.mapper;

import java.util.List;
import com.ruoyi.business.domain.WmsPurchaseOrder;
import com.ruoyi.business.domain.WmsPurchaseOrderItem;

/**
 * 采购订单Mapper接口
 * 
 * @author ruoyi
 * @date 2026-03-23
 */
public interface WmsPurchaseOrderMapper 
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
     * 更新采购订单状态
     * 
     * @param purchaseOrderId 采购订单主键
     * @param oldStatus 旧状态
     * @param newStatus 新状态
     * @param updateBy 更新人
     * @return 结果
     */
    public int updateWmsPurchaseOrderStatus(Long purchaseOrderId, String oldStatus, String newStatus, String updateBy);

    /**
     * 根据采购入库进度更新采购订单状态
     *
     * @param purchaseOrderId 采购订单主键
     * @param newStatus 新状态
     * @param updateBy 更新人
     * @return 结果
     */
    public int updateWmsPurchaseOrderInboundStatus(Long purchaseOrderId, String newStatus, String updateBy);

    /**
     * 删除采购订单
     * 
     * @param purchaseOrderId 采购订单主键
     * @return 结果
     */
    public int deleteWmsPurchaseOrderByPurchaseOrderId(Long purchaseOrderId);

    /**
     * 批量删除采购订单
     * 
     * @param purchaseOrderIds 需要删除的数据主键集合
     * @return 结果
     */
    public int deleteWmsPurchaseOrderByPurchaseOrderIds(Long[] purchaseOrderIds);

    /**
     * 批量删除采购订单明细
     * 
     * @param purchaseOrderIds 需要删除的数据主键集合
     * @return 结果
     */
    public int deleteWmsPurchaseOrderItemByPurchaseOrderIds(Long[] purchaseOrderIds);
    
    /**
     * 批量新增采购订单明细
     * 
     * @param wmsPurchaseOrderItemList 采购订单明细列表
     * @return 结果
     */
    public int batchWmsPurchaseOrderItem(List<WmsPurchaseOrderItem> wmsPurchaseOrderItemList);
    

    /**
     * 通过采购订单主键删除采购订单明细信息
     * 
     * @param purchaseOrderId 采购订单ID
     * @return 结果
     */
    public int deleteWmsPurchaseOrderItemByPurchaseOrderId(Long purchaseOrderId);
}
