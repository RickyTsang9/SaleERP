package com.ruoyi.business.mapper;

import java.util.List;
import com.ruoyi.business.domain.FinPayable;

/**
 * 应付账款Mapper接口
 * 
 * @author ruoyi
 * @date 2026-03-23
 */
public interface FinPayableMapper 
{
    /**
     * 查询应付账款
     * 
     * @param payableId 应付账款主键
     * @return 应付账款
     */
    public FinPayable selectFinPayableByPayableId(Long payableId);

    /**
     * 查询应付账款列表
     * 
     * @param finPayable 应付账款
     * @return 应付账款集合
     */
    public List<FinPayable> selectFinPayableList(FinPayable finPayable);

    /**
     * 根据采购订单查询应付账款
     * 
     * @param purchaseOrderId 采购订单主键
     * @return 应付账款
     */
    public FinPayable selectFinPayableByPurchaseOrderId(Long purchaseOrderId);

    /**
     * 根据供应商查询待冲减应付账款列表
     * 
     * @param supplierId 供应商主键
     * @return 待冲减应付账款集合
     */
    public List<FinPayable> selectPendingFinPayableListBySupplierId(Long supplierId);

    /**
     * 新增应付账款
     * 
     * @param finPayable 应付账款
     * @return 结果
     */
    public int insertFinPayable(FinPayable finPayable);

    /**
     * 修改应付账款
     * 
     * @param finPayable 应付账款
     * @return 结果
     */
    public int updateFinPayable(FinPayable finPayable);

    /**
     * 调整应付账款的受控字段
     * 
     * @param finPayable 应付账款
     * @return 结果
     */
    public int updateFinPayableAdjustInfo(FinPayable finPayable);

    /**
     * 删除应付账款
     * 
     * @param payableId 应付账款主键
     * @return 结果
     */
    public int deleteFinPayableByPayableId(Long payableId);

    /**
     * 批量删除应付账款
     * 
     * @param payableIds 需要删除的数据主键集合
     * @return 结果
     */
    public int deleteFinPayableByPayableIds(Long[] payableIds);
}
