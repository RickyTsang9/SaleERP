package com.ruoyi.business.service.impl;

import java.util.List;
import com.ruoyi.common.utils.DateUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.ArrayList;
import java.math.BigDecimal;
import com.ruoyi.business.domain.FinPayable;
import com.ruoyi.common.utils.StringUtils;
import org.springframework.transaction.annotation.Transactional;
import com.ruoyi.business.domain.WmsPurchaseOrderItem;
import com.ruoyi.business.mapper.FinPayableMapper;
import com.ruoyi.business.mapper.WmsPurchaseOrderMapper;
import com.ruoyi.business.domain.WmsPurchaseOrder;
import com.ruoyi.business.service.IWmsPurchaseOrderService;
import com.ruoyi.common.exception.ServiceException;
import com.ruoyi.common.utils.SecurityUtils;

/**
 * 采购订单Service业务层处理
 * 
 * @author ruoyi
 * @date 2026-03-23
 */
@Service
public class WmsPurchaseOrderServiceImpl implements IWmsPurchaseOrderService 
{
    private static final String STATUS_PENDING_AUDIT = "0";

    private static final String STATUS_AUDITED = "1";

    private static final String STATUS_PARTIALLY_INBOUND = "2";

    private static final String STATUS_COMPLETED = "3";

    private static final String STATUS_CANCELLED = "4";

    @Autowired
    private WmsPurchaseOrderMapper wmsPurchaseOrderMapper;

    @Autowired
    private FinPayableMapper finPayableMapper;

    /**
     * 查询采购订单
     * 
     * @param purchaseOrderId 采购订单主键
     * @return 采购订单
     */
    @Override
    public WmsPurchaseOrder selectWmsPurchaseOrderByPurchaseOrderId(Long purchaseOrderId)
    {
        return wmsPurchaseOrderMapper.selectWmsPurchaseOrderByPurchaseOrderId(purchaseOrderId);
    }

    /**
     * 查询采购订单列表
     * 
     * @param wmsPurchaseOrder 采购订单
     * @return 采购订单
     */
    @Override
    public List<WmsPurchaseOrder> selectWmsPurchaseOrderList(WmsPurchaseOrder wmsPurchaseOrder)
    {
        return wmsPurchaseOrderMapper.selectWmsPurchaseOrderList(wmsPurchaseOrder);
    }

    /**
     * 新增采购订单
     * 
     * @param wmsPurchaseOrder 采购订单
     * @return 结果
     */
    @Transactional
    @Override
    public int insertWmsPurchaseOrder(WmsPurchaseOrder wmsPurchaseOrder)
    {
        validatePurchaseOrderBeforeSave(wmsPurchaseOrder);
        wmsPurchaseOrder.setStatus(STATUS_PENDING_AUDIT);
        wmsPurchaseOrder.setCreateTime(DateUtils.getNowDate());
        try
        {
            wmsPurchaseOrder.setCreateBy(SecurityUtils.getUsername());
        }
        catch (Exception exception)
        {
            // ignore
        }
        int rows = wmsPurchaseOrderMapper.insertWmsPurchaseOrder(wmsPurchaseOrder);
        insertWmsPurchaseOrderItem(wmsPurchaseOrder);
        return rows;
    }

    /**
     * 修改采购订单
     * 
     * @param wmsPurchaseOrder 采购订单
     * @return 结果
     */
    @Transactional
    @Override
    public int updateWmsPurchaseOrder(WmsPurchaseOrder wmsPurchaseOrder)
    {
        WmsPurchaseOrder databasePurchaseOrder = wmsPurchaseOrderMapper.selectWmsPurchaseOrderByPurchaseOrderId(wmsPurchaseOrder.getPurchaseOrderId());
        if (databasePurchaseOrder == null)
        {
            throw new ServiceException("采购订单不存在");
        }
        if (!STATUS_PENDING_AUDIT.equals(databasePurchaseOrder.getStatus()))
        {
            throw new ServiceException("仅待审核状态采购订单允许修改");
        }
        validatePurchaseOrderBeforeSave(wmsPurchaseOrder);
        wmsPurchaseOrder.setStatus(databasePurchaseOrder.getStatus());
        wmsPurchaseOrder.setUpdateTime(DateUtils.getNowDate());
        try
        {
            wmsPurchaseOrder.setUpdateBy(SecurityUtils.getUsername());
        }
        catch (Exception exception)
        {
            // ignore
        }
        wmsPurchaseOrderMapper.deleteWmsPurchaseOrderItemByPurchaseOrderId(wmsPurchaseOrder.getPurchaseOrderId());
        insertWmsPurchaseOrderItem(wmsPurchaseOrder);
        return wmsPurchaseOrderMapper.updateWmsPurchaseOrder(wmsPurchaseOrder);
    }

    /**
     * 提交采购订单
     * 
     * @param purchaseOrderId 采购订单主键
     * @return 结果
     */
    @Override
    public int submitWmsPurchaseOrder(Long purchaseOrderId)
    {
        WmsPurchaseOrder databasePurchaseOrder = wmsPurchaseOrderMapper.selectWmsPurchaseOrderByPurchaseOrderId(purchaseOrderId);
        if (databasePurchaseOrder == null)
        {
            throw new ServiceException("采购订单不存在");
        }
        if (!STATUS_PENDING_AUDIT.equals(databasePurchaseOrder.getStatus()))
        {
            throw new ServiceException("仅待审核状态采购订单允许提交");
        }
        validatePurchaseOrderBeforeSave(databasePurchaseOrder);
        throw new ServiceException("采购订单保存后直接进入待审核，不需要重复提交");
    }

    /**
     * 审核采购订单
     * 
     * @param purchaseOrderId 采购订单主键
     * @return 结果
     */
    @Override
    @Transactional
    public int auditWmsPurchaseOrder(Long purchaseOrderId)
    {
        WmsPurchaseOrder databasePurchaseOrder = wmsPurchaseOrderMapper.selectWmsPurchaseOrderByPurchaseOrderId(purchaseOrderId);
        if (databasePurchaseOrder == null)
        {
            throw new ServiceException("采购订单不存在");
        }
        if (!STATUS_PENDING_AUDIT.equals(databasePurchaseOrder.getStatus()))
        {
            throw new ServiceException("仅待审核状态采购订单允许审核");
        }
        if (databasePurchaseOrder.getWmsPurchaseOrderItemList() == null || databasePurchaseOrder.getWmsPurchaseOrderItemList().isEmpty())
        {
            throw new ServiceException("采购明细不能为空");
        }
        String updateUsername = SecurityUtils.getUsername();
        int updateCount = wmsPurchaseOrderMapper.updateWmsPurchaseOrderStatus(
            purchaseOrderId, STATUS_PENDING_AUDIT, STATUS_AUDITED, updateUsername);
        if (updateCount == 0)
        {
            throw new ServiceException("采购订单状态已变更，请刷新后重试");
        }
        ensurePayableCreated(databasePurchaseOrder, updateUsername);
        return updateCount;
    }

    /**
     * 作废采购订单
     * 
     * @param purchaseOrderId 采购订单主键
     * @return 结果
     */
    @Override
    public int cancelWmsPurchaseOrder(Long purchaseOrderId)
    {
        WmsPurchaseOrder databasePurchaseOrder = wmsPurchaseOrderMapper.selectWmsPurchaseOrderByPurchaseOrderId(purchaseOrderId);
        if (databasePurchaseOrder == null)
        {
            throw new ServiceException("采购订单不存在");
        }
        if (STATUS_CANCELLED.equals(databasePurchaseOrder.getStatus()))
        {
            throw new ServiceException("采购订单已作废，无需重复作废");
        }
        if (!STATUS_PENDING_AUDIT.equals(databasePurchaseOrder.getStatus())
            && !STATUS_PARTIALLY_INBOUND.equals(databasePurchaseOrder.getStatus()))
        {
            throw new ServiceException("仅待审核或部分入库状态采购订单允许作废");
        }
        String updateUsername = SecurityUtils.getUsername();
        int updateCount = wmsPurchaseOrderMapper.updateWmsPurchaseOrderStatus(
            purchaseOrderId, databasePurchaseOrder.getStatus(), STATUS_CANCELLED, updateUsername);
        if (updateCount == 0)
        {
            throw new ServiceException("采购订单状态已变更，请刷新后重试");
        }
        return updateCount;
    }

    /**
     * 校验采购入库关联的采购订单是否合法
     *
     * @param purchaseOrderId 采购订单主键
     * @param supplierId 供应商主键
     * @return 采购订单
     */
    @Override
    public WmsPurchaseOrder validatePurchaseOrderForInbound(Long purchaseOrderId, Long supplierId)
    {
        if (purchaseOrderId == null)
        {
            return null;
        }
        WmsPurchaseOrder databasePurchaseOrder = wmsPurchaseOrderMapper.selectWmsPurchaseOrderByPurchaseOrderId(purchaseOrderId);
        if (databasePurchaseOrder == null)
        {
            throw new ServiceException("来源采购订单不存在");
        }
        if (STATUS_PENDING_AUDIT.equals(databasePurchaseOrder.getStatus()))
        {
            throw new ServiceException("待审核采购订单不允许新增采购入库");
        }
        if (STATUS_CANCELLED.equals(databasePurchaseOrder.getStatus()))
        {
            throw new ServiceException("已作废采购订单不允许新增采购入库");
        }
        if (STATUS_COMPLETED.equals(databasePurchaseOrder.getStatus()))
        {
            throw new ServiceException("已完成采购订单不允许继续新增采购入库");
        }
        if (supplierId != null && !supplierId.equals(databasePurchaseOrder.getSupplierId()))
        {
            throw new ServiceException("入库单供应商必须与来源采购订单保持一致");
        }
        return databasePurchaseOrder;
    }

    /**
     * 根据采购入库进度刷新采购订单状态
     *
     * @param purchaseOrderId 采购订单主键
     */
    @Override
    public void refreshPurchaseOrderInboundProgress(Long purchaseOrderId)
    {
        if (purchaseOrderId == null)
        {
            return;
        }
        WmsPurchaseOrder databasePurchaseOrder = wmsPurchaseOrderMapper.selectWmsPurchaseOrderByPurchaseOrderId(purchaseOrderId);
        if (databasePurchaseOrder == null || STATUS_CANCELLED.equals(databasePurchaseOrder.getStatus()))
        {
            return;
        }
        String targetStatus = buildInboundProgressStatus(databasePurchaseOrder);
        if (targetStatus.equals(databasePurchaseOrder.getStatus()))
        {
            return;
        }
        String updateUsername = SecurityUtils.getUsername();
        wmsPurchaseOrderMapper.updateWmsPurchaseOrderInboundStatus(purchaseOrderId, targetStatus, updateUsername);
    }

    /**
     * 批量删除采购订单
     * 
     * @param purchaseOrderIds 需要删除的采购订单主键
     * @return 结果
     */
    @Transactional
    @Override
    public int deleteWmsPurchaseOrderByPurchaseOrderIds(Long[] purchaseOrderIds)
    {
        for (Long purchaseOrderId : purchaseOrderIds)
        {
            WmsPurchaseOrder databasePurchaseOrder = wmsPurchaseOrderMapper.selectWmsPurchaseOrderByPurchaseOrderId(purchaseOrderId);
            if (databasePurchaseOrder == null)
            {
                continue;
            }
            if (!STATUS_PENDING_AUDIT.equals(databasePurchaseOrder.getStatus()))
            {
                throw new ServiceException("仅待审核状态采购订单允许删除，采购单号：" + databasePurchaseOrder.getOrderNo());
            }
        }
        wmsPurchaseOrderMapper.deleteWmsPurchaseOrderItemByPurchaseOrderIds(purchaseOrderIds);
        return wmsPurchaseOrderMapper.deleteWmsPurchaseOrderByPurchaseOrderIds(purchaseOrderIds);
    }

    /**
     * 删除采购订单信息
     * 
     * @param purchaseOrderId 采购订单主键
     * @return 结果
     */
    @Transactional
    @Override
    public int deleteWmsPurchaseOrderByPurchaseOrderId(Long purchaseOrderId)
    {
        WmsPurchaseOrder databasePurchaseOrder = wmsPurchaseOrderMapper.selectWmsPurchaseOrderByPurchaseOrderId(purchaseOrderId);
        if (databasePurchaseOrder == null)
        {
            return 0;
        }
        if (!STATUS_PENDING_AUDIT.equals(databasePurchaseOrder.getStatus()))
        {
            throw new ServiceException("仅待审核状态采购订单允许删除");
        }
        wmsPurchaseOrderMapper.deleteWmsPurchaseOrderItemByPurchaseOrderId(purchaseOrderId);
        return wmsPurchaseOrderMapper.deleteWmsPurchaseOrderByPurchaseOrderId(purchaseOrderId);
    }

    /**
     * 新增采购订单明细信息
     * 
     * @param wmsPurchaseOrder 采购订单对象
     */
    public void insertWmsPurchaseOrderItem(WmsPurchaseOrder wmsPurchaseOrder)
    {
        List<WmsPurchaseOrderItem> wmsPurchaseOrderItemList = wmsPurchaseOrder.getWmsPurchaseOrderItemList();
        Long purchaseOrderId = wmsPurchaseOrder.getPurchaseOrderId();
        if (StringUtils.isNotNull(wmsPurchaseOrderItemList))
        {
            List<WmsPurchaseOrderItem> list = new ArrayList<WmsPurchaseOrderItem>();
            for (WmsPurchaseOrderItem wmsPurchaseOrderItem : wmsPurchaseOrderItemList)
            {
                wmsPurchaseOrderItem.setPurchaseOrderId(purchaseOrderId);
                list.add(wmsPurchaseOrderItem);
            }
            if (list.size() > 0)
            {
                wmsPurchaseOrderMapper.batchWmsPurchaseOrderItem(list);
            }
        }
    }

    /**
     * 保存采购订单前校验主单和明细数据，并重算总数量与总金额
     * 
     * @param wmsPurchaseOrder 采购订单
     */
    private void validatePurchaseOrderBeforeSave(WmsPurchaseOrder wmsPurchaseOrder)
    {
        if (wmsPurchaseOrder == null)
        {
            throw new ServiceException("采购订单不存在");
        }
        if (wmsPurchaseOrder.getSupplierId() == null)
        {
            throw new ServiceException("供应商不能为空");
        }
        if (wmsPurchaseOrder.getPurchaseDate() == null)
        {
            throw new ServiceException("采购日期不能为空");
        }
        validatePurchaseOrderItemList(wmsPurchaseOrder.getWmsPurchaseOrderItemList());
        wmsPurchaseOrder.setTotalQuantity(calculatePurchaseOrderTotalQuantity(wmsPurchaseOrder.getWmsPurchaseOrderItemList()));
        wmsPurchaseOrder.setTotalAmount(calculatePurchaseOrderTotalAmount(wmsPurchaseOrder.getWmsPurchaseOrderItemList()));
    }

    /**
     * 校验采购订单明细是否完整
     * 
     * @param wmsPurchaseOrderItemList 采购订单明细列表
     */
    private void validatePurchaseOrderItemList(List<WmsPurchaseOrderItem> wmsPurchaseOrderItemList)
    {
        if (wmsPurchaseOrderItemList == null || wmsPurchaseOrderItemList.isEmpty())
        {
            throw new ServiceException("采购明细不能为空");
        }
        for (int purchaseOrderItemIndex = 0; purchaseOrderItemIndex < wmsPurchaseOrderItemList.size(); purchaseOrderItemIndex++)
        {
            WmsPurchaseOrderItem wmsPurchaseOrderItem = wmsPurchaseOrderItemList.get(purchaseOrderItemIndex);
            if (wmsPurchaseOrderItem.getProductId() == null)
            {
                throw new ServiceException("第" + (purchaseOrderItemIndex + 1) + "行采购明细商品不能为空");
            }
            if (wmsPurchaseOrderItem.getQuantity() == null || wmsPurchaseOrderItem.getQuantity().compareTo(BigDecimal.ZERO) <= 0)
            {
                throw new ServiceException("第" + (purchaseOrderItemIndex + 1) + "行采购数量必须大于0");
            }
            if (wmsPurchaseOrderItem.getPrice() == null || wmsPurchaseOrderItem.getPrice().compareTo(BigDecimal.ZERO) < 0)
            {
                throw new ServiceException("第" + (purchaseOrderItemIndex + 1) + "行采购单价不能小于0");
            }
            wmsPurchaseOrderItem.setAmount(calculatePurchaseOrderItemAmount(wmsPurchaseOrderItem));
        }
    }

    /**
     * 根据采购订单明细重新计算总数量
     * 
     * @param wmsPurchaseOrderItemList 采购订单明细列表
     * @return 总数量
     */
    private BigDecimal calculatePurchaseOrderTotalQuantity(List<WmsPurchaseOrderItem> wmsPurchaseOrderItemList)
    {
        BigDecimal totalQuantityValue = BigDecimal.ZERO;
        if (wmsPurchaseOrderItemList == null || wmsPurchaseOrderItemList.isEmpty())
        {
            return totalQuantityValue;
        }
        for (WmsPurchaseOrderItem wmsPurchaseOrderItem : wmsPurchaseOrderItemList)
        {
            if (wmsPurchaseOrderItem.getQuantity() != null)
            {
                totalQuantityValue = totalQuantityValue.add(wmsPurchaseOrderItem.getQuantity());
            }
        }
        return totalQuantityValue;
    }

    /**
     * 根据采购订单明细重新计算总金额
     * 
     * @param wmsPurchaseOrderItemList 采购订单明细列表
     * @return 总金额
     */
    private BigDecimal calculatePurchaseOrderTotalAmount(List<WmsPurchaseOrderItem> wmsPurchaseOrderItemList)
    {
        BigDecimal totalAmountValue = BigDecimal.ZERO;
        if (wmsPurchaseOrderItemList == null || wmsPurchaseOrderItemList.isEmpty())
        {
            return totalAmountValue;
        }
        for (WmsPurchaseOrderItem wmsPurchaseOrderItem : wmsPurchaseOrderItemList)
        {
            totalAmountValue = totalAmountValue.add(calculatePurchaseOrderItemAmount(wmsPurchaseOrderItem));
        }
        return totalAmountValue;
    }

    /**
     * 计算采购明细金额
     * 
     * @param wmsPurchaseOrderItem 采购订单明细
     * @return 金额
     */
    private BigDecimal calculatePurchaseOrderItemAmount(WmsPurchaseOrderItem wmsPurchaseOrderItem)
    {
        BigDecimal quantityValue = wmsPurchaseOrderItem.getQuantity() == null ? BigDecimal.ZERO : wmsPurchaseOrderItem.getQuantity();
        BigDecimal priceValue = wmsPurchaseOrderItem.getPrice() == null ? BigDecimal.ZERO : wmsPurchaseOrderItem.getPrice();
        return quantityValue.multiply(priceValue);
    }

    /**
     * 确保采购订单已生成对应应付账款
     * 
     * @param databasePurchaseOrder 数据库中的采购订单
     * @param createUsername 创建人
     */
    private void ensurePayableCreated(WmsPurchaseOrder databasePurchaseOrder, String createUsername)
    {
        FinPayable existingFinPayable =
            finPayableMapper.selectFinPayableByPurchaseOrderId(databasePurchaseOrder.getPurchaseOrderId());
        if (existingFinPayable != null)
        {
            return;
        }
        BigDecimal amountDue = databasePurchaseOrder.getTotalAmount() == null ? BigDecimal.ZERO : databasePurchaseOrder.getTotalAmount();
        FinPayable finPayable = new FinPayable();
        finPayable.setPurchaseOrderId(databasePurchaseOrder.getPurchaseOrderId());
        finPayable.setSupplierId(databasePurchaseOrder.getSupplierId());
        finPayable.setAmountDue(amountDue);
        finPayable.setAmountPaid(BigDecimal.ZERO);
        finPayable.setRemainAmount(amountDue);
        finPayable.setStatus("0");
        finPayable.setDueDate(databasePurchaseOrder.getPurchaseDate());
        finPayable.setCreateBy(createUsername);
        finPayable.setCreateTime(DateUtils.getNowDate());
        finPayableMapper.insertFinPayable(finPayable);
    }

    /**
     * 根据采购订单数量与已入库数量计算目标状态
     *
     * @param databasePurchaseOrder 采购订单
     * @return 目标状态
     */
    private String buildInboundProgressStatus(WmsPurchaseOrder databasePurchaseOrder)
    {
        BigDecimal totalQuantity = databasePurchaseOrder.getTotalQuantity() == null ? BigDecimal.ZERO : databasePurchaseOrder.getTotalQuantity();
        BigDecimal inboundQuantity = databasePurchaseOrder.getInboundQuantity() == null ? BigDecimal.ZERO : databasePurchaseOrder.getInboundQuantity();
        if (totalQuantity.compareTo(BigDecimal.ZERO) <= 0 || inboundQuantity.compareTo(BigDecimal.ZERO) <= 0)
        {
            return STATUS_AUDITED;
        }
        if (inboundQuantity.compareTo(totalQuantity) >= 0)
        {
            return STATUS_COMPLETED;
        }
        return STATUS_PARTIALLY_INBOUND;
    }
}
