package com.ruoyi.business.service.impl;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.ruoyi.business.domain.WmsInbound;
import com.ruoyi.business.domain.WmsInboundItem;
import com.ruoyi.business.domain.WmsPurchaseOrder;
import com.ruoyi.business.domain.WmsPurchaseOrderItem;
import com.ruoyi.business.mapper.WmsInboundMapper;
import com.ruoyi.business.mapper.WmsInboundItemMapper;
import com.ruoyi.business.service.IWmsInboundItemService;
import com.ruoyi.business.service.IWmsPurchaseOrderService;
import com.ruoyi.common.exception.ServiceException;

@Service
public class WmsInboundItemServiceImpl implements IWmsInboundItemService
{
    private static final String STATUS_DRAFT = "draft";

    @Autowired
    private WmsInboundItemMapper wmsInboundItemMapper;

    @Autowired
    private WmsInboundMapper wmsInboundMapper;

    @Autowired
    private IWmsPurchaseOrderService wmsPurchaseOrderService;

    @Override
    public WmsInboundItem selectWmsInboundItemById(Long inboundItemId)
    {
        return wmsInboundItemMapper.selectWmsInboundItemById(inboundItemId);
    }

    @Override
    public List<WmsInboundItem> selectWmsInboundItemList(WmsInboundItem wmsInboundItem)
    {
        return wmsInboundItemMapper.selectWmsInboundItemList(wmsInboundItem);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int insertWmsInboundItem(WmsInboundItem wmsInboundItem)
    {
        WmsInbound databaseInbound = validateInboundEditable(wmsInboundItem.getInboundId());
        if (databaseInbound.getPurchaseOrderId() != null)
        {
            throw new ServiceException("关联采购单的入库明细由系统自动生成，不支持手工新增");
        }
        prepareInboundItemForSave(databaseInbound, wmsInboundItem);
        int insertRows = wmsInboundItemMapper.insertWmsInboundItem(wmsInboundItem);
        refreshInboundTotal(wmsInboundItem.getInboundId());
        return insertRows;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int updateWmsInboundItem(WmsInboundItem wmsInboundItem)
    {
        WmsInboundItem databaseInboundItem = wmsInboundItemMapper.selectWmsInboundItemById(wmsInboundItem.getInboundItemId());
        if (databaseInboundItem == null)
        {
            throw new ServiceException("入库明细不存在");
        }
        WmsInbound databaseInbound = validateInboundEditable(databaseInboundItem.getInboundId());
        wmsInboundItem.setInboundId(databaseInboundItem.getInboundId());
        if (databaseInbound.getPurchaseOrderId() != null)
        {
            wmsInboundItem.setPurchaseOrderItemId(databaseInboundItem.getPurchaseOrderItemId());
        }
        prepareInboundItemForSave(databaseInbound, wmsInboundItem);
        int updateRows = wmsInboundItemMapper.updateWmsInboundItem(wmsInboundItem);
        refreshInboundTotal(wmsInboundItem.getInboundId());
        return updateRows;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int deleteWmsInboundItemById(Long inboundItemId)
    {
        WmsInboundItem databaseInboundItem = wmsInboundItemMapper.selectWmsInboundItemById(inboundItemId);
        if (databaseInboundItem == null)
        {
            return 0;
        }
        WmsInbound databaseInbound = validateInboundEditable(databaseInboundItem.getInboundId());
        if (databaseInbound.getPurchaseOrderId() != null)
        {
            throw new ServiceException("关联采购单的入库明细不支持删除，请调整数量或作废整张入库单");
        }
        int deleteRows = wmsInboundItemMapper.deleteWmsInboundItemById(inboundItemId);
        refreshInboundTotal(databaseInboundItem.getInboundId());
        return deleteRows;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int deleteWmsInboundItemByIds(Long[] inboundItemIds)
    {
        List<Long> inboundIdList = wmsInboundItemMapper.selectInboundIdsByInboundItemIds(inboundItemIds);
        if (inboundIdList != null && !inboundIdList.isEmpty())
        {
            Set<Long> inboundIdSet = new LinkedHashSet<Long>(inboundIdList);
            for (Long inboundId : inboundIdSet)
            {
                WmsInbound databaseInbound = validateInboundEditable(inboundId);
                if (databaseInbound.getPurchaseOrderId() != null)
                {
                    throw new ServiceException("关联采购单的入库明细不支持删除，请调整数量或作废整张入库单");
                }
            }
        }
        int deleteRows = wmsInboundItemMapper.deleteWmsInboundItemByIds(inboundItemIds);
        if (inboundIdList != null && !inboundIdList.isEmpty())
        {
            Set<Long> inboundIdSet = new LinkedHashSet<Long>(inboundIdList);
            for (Long inboundId : inboundIdSet)
            {
                refreshInboundTotal(inboundId);
            }
        }
        return deleteRows;
    }

    /**
     * 校验入库单是否仍然允许维护明细
     *
     * @param inboundId 入库单编号
     * @return 入库单
     */
    private WmsInbound validateInboundEditable(Long inboundId)
    {
        if (inboundId == null)
        {
            throw new ServiceException("入库单不存在");
        }
        WmsInbound databaseInbound = wmsInboundMapper.selectWmsInboundById(inboundId);
        if (databaseInbound == null)
        {
            throw new ServiceException("入库单不存在");
        }
        if (!STATUS_DRAFT.equals(databaseInbound.getStatus()))
        {
            throw new ServiceException("仅草稿状态入库单允许维护明细");
        }
        return databaseInbound;
    }

    /**
     * 保存前补齐入库明细金额，并校验来源采购单带出的约束
     *
     * @param databaseInbound 数据库中的入库单
     * @param wmsInboundItem 入库明细
     */
    private void prepareInboundItemForSave(WmsInbound databaseInbound, WmsInboundItem wmsInboundItem)
    {
        if (wmsInboundItem.getQuantity() == null || wmsInboundItem.getQuantity().compareTo(BigDecimal.ZERO) <= 0)
        {
            throw new ServiceException("入库数量必须大于0");
        }
        if (wmsInboundItem.getPrice() != null && wmsInboundItem.getPrice().compareTo(BigDecimal.ZERO) < 0)
        {
            throw new ServiceException("入库单价不能小于0");
        }
        if (databaseInbound.getPurchaseOrderId() != null)
        {
            applyPurchaseOrderItemConstraint(databaseInbound, wmsInboundItem);
        }
        wmsInboundItem.setAmount(calculateAmount(wmsInboundItem.getQuantity(), wmsInboundItem.getPrice()));
    }

    /**
     * 根据来源采购明细锁定商品与单价，并校验入库数量范围
     *
     * @param databaseInbound 数据库中的入库单
     * @param wmsInboundItem 入库明细
     */
    private void applyPurchaseOrderItemConstraint(WmsInbound databaseInbound, WmsInboundItem wmsInboundItem)
    {
        if (wmsInboundItem.getPurchaseOrderItemId() == null)
        {
            throw new ServiceException("关联采购单的入库明细必须绑定来源采购明细");
        }
        WmsPurchaseOrder databasePurchaseOrder = wmsPurchaseOrderService.selectWmsPurchaseOrderByPurchaseOrderId(
            databaseInbound.getPurchaseOrderId());
        if (databasePurchaseOrder == null || databasePurchaseOrder.getWmsPurchaseOrderItemList() == null
            || databasePurchaseOrder.getWmsPurchaseOrderItemList().isEmpty())
        {
            throw new ServiceException("来源采购订单不存在可入库明细");
        }
        WmsPurchaseOrderItem purchaseOrderItem = findPurchaseOrderItem(
            databasePurchaseOrder.getWmsPurchaseOrderItemList(), wmsInboundItem.getPurchaseOrderItemId());
        if (purchaseOrderItem == null)
        {
            throw new ServiceException("来源采购明细不存在");
        }
        if (wmsInboundItem.getQuantity() == null || wmsInboundItem.getQuantity().compareTo(BigDecimal.ZERO) <= 0)
        {
            throw new ServiceException("入库数量必须大于0");
        }
        BigDecimal remainQuantity = purchaseOrderItem.getRemainQuantity() == null ? BigDecimal.ZERO : purchaseOrderItem.getRemainQuantity();
        BigDecimal accumulatedInboundQuantity = calculateAccumulatedInboundQuantity(databaseInbound, wmsInboundItem);
        if (accumulatedInboundQuantity.compareTo(remainQuantity) > 0)
        {
            throw new ServiceException("同一来源采购明细的累计入库数量不能超过待入库数量");
        }
        wmsInboundItem.setProductId(purchaseOrderItem.getProductId());
        wmsInboundItem.setPrice(purchaseOrderItem.getPrice());
        wmsInboundItem.setPurchaseQuantity(purchaseOrderItem.getQuantity());
        wmsInboundItem.setReceivedQuantity(purchaseOrderItem.getInboundQuantity());
        wmsInboundItem.setRemainQuantity(purchaseOrderItem.getRemainQuantity());
    }

    /**
     * 计算当前草稿入库单内同一来源采购明细的累计入库数量
     *
     * @param databaseInbound 数据库中的入库单
     * @param wmsInboundItem 当前待保存的入库明细
     * @return 累计入库数量
     */
    private BigDecimal calculateAccumulatedInboundQuantity(WmsInbound databaseInbound, WmsInboundItem wmsInboundItem)
    {
        WmsInboundItem inboundItemQuery = new WmsInboundItem();
        inboundItemQuery.setInboundId(databaseInbound.getInboundId());
        List<WmsInboundItem> databaseInboundItemList = wmsInboundItemMapper.selectWmsInboundItemList(inboundItemQuery);
        Map<Long, BigDecimal> inboundQuantityMap = new HashMap<Long, BigDecimal>();
        if (databaseInboundItemList != null)
        {
            for (WmsInboundItem databaseInboundItem : databaseInboundItemList)
            {
                if (databaseInboundItem.getPurchaseOrderItemId() == null)
                {
                    continue;
                }
                if (wmsInboundItem.getInboundItemId() != null
                    && Objects.equals(databaseInboundItem.getInboundItemId(), wmsInboundItem.getInboundItemId()))
                {
                    continue;
                }
                accumulateInboundQuantity(inboundQuantityMap, databaseInboundItem.getPurchaseOrderItemId(),
                    databaseInboundItem.getQuantity());
            }
        }
        return accumulateInboundQuantity(inboundQuantityMap, wmsInboundItem.getPurchaseOrderItemId(), wmsInboundItem.getQuantity());
    }

    /**
     * 根据采购订单明细编号查找来源采购明细
     *
     * @param purchaseOrderItemList 采购订单明细列表
     * @param purchaseOrderItemId 采购订单明细编号
     * @return 采购订单明细
     */
    private WmsPurchaseOrderItem findPurchaseOrderItem(List<WmsPurchaseOrderItem> purchaseOrderItemList, Long purchaseOrderItemId)
    {
        for (WmsPurchaseOrderItem purchaseOrderItem : purchaseOrderItemList)
        {
            if (Objects.equals(purchaseOrderItem.getPurchaseOrderItemId(), purchaseOrderItemId))
            {
                return purchaseOrderItem;
            }
        }
        return null;
    }

    /**
     * 根据数量和单价重新计算金额
     *
     * @param quantityValue 数量
     * @param priceValue 单价
     * @return 金额
     */
    private BigDecimal calculateAmount(BigDecimal quantityValue, BigDecimal priceValue)
    {
        BigDecimal safeQuantityValue = quantityValue == null ? BigDecimal.ZERO : quantityValue;
        BigDecimal safePriceValue = priceValue == null ? BigDecimal.ZERO : priceValue;
        return safeQuantityValue.multiply(safePriceValue);
    }

    /**
     * 按来源采购明细累计入库数量
     *
     * @param inboundQuantityMap 来源采购明细累计入库数量映射
     * @param purchaseOrderItemId 来源采购明细编号
     * @param currentInboundQuantity 当前入库数量
     * @return 累计入库数量
     */
    private BigDecimal accumulateInboundQuantity(Map<Long, BigDecimal> inboundQuantityMap, Long purchaseOrderItemId,
        BigDecimal currentInboundQuantity)
    {
        BigDecimal historyInboundQuantity = inboundQuantityMap.get(purchaseOrderItemId);
        if (historyInboundQuantity == null)
        {
            historyInboundQuantity = BigDecimal.ZERO;
        }
        BigDecimal validCurrentInboundQuantity =
            currentInboundQuantity == null ? BigDecimal.ZERO : currentInboundQuantity;
        BigDecimal accumulatedInboundQuantity = historyInboundQuantity.add(validCurrentInboundQuantity);
        inboundQuantityMap.put(purchaseOrderItemId, accumulatedInboundQuantity);
        return accumulatedInboundQuantity;
    }

    /**
     * 刷新入库单主表的总数量和总金额
     *
     * @param inboundId 入库单编号
     */
    private void refreshInboundTotal(Long inboundId)
    {
        if (inboundId != null)
        {
            wmsInboundMapper.refreshTotalByInboundId(inboundId);
        }
    }
}
