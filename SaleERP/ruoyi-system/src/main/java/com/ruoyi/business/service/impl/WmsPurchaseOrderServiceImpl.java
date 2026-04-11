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
        wmsPurchaseOrder.setCreateTime(DateUtils.getNowDate());
        try {
            wmsPurchaseOrder.setCreateBy(SecurityUtils.getUsername());
        } catch (Exception e) {
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
        wmsPurchaseOrder.setStatus(databasePurchaseOrder.getStatus());
        wmsPurchaseOrder.setUpdateTime(DateUtils.getNowDate());
        try {
            wmsPurchaseOrder.setUpdateBy(SecurityUtils.getUsername());
        } catch (Exception e) {
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
        if (databasePurchaseOrder.getWmsPurchaseOrderItemList() == null || databasePurchaseOrder.getWmsPurchaseOrderItemList().isEmpty())
        {
            throw new ServiceException("采购明细不能为空");
        }
        return 1;
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
        if (STATUS_AUDITED.equals(databasePurchaseOrder.getStatus()))
        {
            throw new ServiceException("已审核采购订单不允许作废");
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

    private void ensurePayableCreated(WmsPurchaseOrder databasePurchaseOrder, String createUsername)
    {
        FinPayable payableQuery = new FinPayable();
        payableQuery.setPurchaseOrderId(databasePurchaseOrder.getPurchaseOrderId());
        List<FinPayable> payableList = finPayableMapper.selectFinPayableList(payableQuery);
        if (payableList != null && !payableList.isEmpty())
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
