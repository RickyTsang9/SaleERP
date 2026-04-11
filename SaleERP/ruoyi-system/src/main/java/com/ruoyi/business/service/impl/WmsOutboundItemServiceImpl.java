package com.ruoyi.business.service.impl;

import java.math.BigDecimal;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.ruoyi.business.domain.WmsOutbound;
import com.ruoyi.business.domain.WmsOutboundItem;
import com.ruoyi.business.domain.WmsSaleOrderItem;
import com.ruoyi.business.mapper.WmsOutboundMapper;
import com.ruoyi.business.mapper.WmsOutboundItemMapper;
import com.ruoyi.business.mapper.WmsSaleOrderItemMapper;
import com.ruoyi.business.service.IWmsOutboundItemService;
import com.ruoyi.common.exception.ServiceException;

@Service
public class WmsOutboundItemServiceImpl implements IWmsOutboundItemService
{
    private static final String STATUS_DRAFT = "draft";

    @Autowired
    private WmsOutboundItemMapper wmsOutboundItemMapper;

    @Autowired
    private WmsOutboundMapper wmsOutboundMapper;

    @Autowired
    private WmsSaleOrderItemMapper wmsSaleOrderItemMapper;

    /**
     * 查询出库明细
     *
     * @param outboundItemId 出库明细编号
     * @return 出库明细
     */
    @Override
    public WmsOutboundItem selectWmsOutboundItemById(Long outboundItemId)
    {
        return wmsOutboundItemMapper.selectWmsOutboundItemById(outboundItemId);
    }

    /**
     * 查询出库明细列表
     *
     * @param wmsOutboundItem 出库明细
     * @return 出库明细集合
     */
    @Override
    public List<WmsOutboundItem> selectWmsOutboundItemList(WmsOutboundItem wmsOutboundItem)
    {
        return wmsOutboundItemMapper.selectWmsOutboundItemList(wmsOutboundItem);
    }

    @Override
    public int insertWmsOutboundItem(WmsOutboundItem wmsOutboundItem)
    {
        WmsOutbound databaseOutbound = validateOutboundEditable(wmsOutboundItem.getOutboundId());
        if (databaseOutbound.getSaleOrderId() != null)
        {
            throw new ServiceException("关联销售单的出库明细由系统自动生成，不支持手工新增");
        }
        prepareOutboundItemForSave(databaseOutbound, wmsOutboundItem);
        int insertRows = wmsOutboundItemMapper.insertWmsOutboundItem(wmsOutboundItem);
        refreshOutboundTotal(wmsOutboundItem.getOutboundId());
        return insertRows;
    }

    @Override
    public int updateWmsOutboundItem(WmsOutboundItem wmsOutboundItem)
    {
        WmsOutboundItem databaseOutboundItem = wmsOutboundItemMapper.selectWmsOutboundItemById(wmsOutboundItem.getOutboundItemId());
        if (databaseOutboundItem == null)
        {
            throw new ServiceException("出库明细不存在");
        }
        WmsOutbound databaseOutbound = validateOutboundEditable(databaseOutboundItem.getOutboundId());
        wmsOutboundItem.setOutboundId(databaseOutboundItem.getOutboundId());
        if (databaseOutbound.getSaleOrderId() != null)
        {
            wmsOutboundItem.setSaleOrderItemId(databaseOutboundItem.getSaleOrderItemId());
        }
        prepareOutboundItemForSave(databaseOutbound, wmsOutboundItem);
        int updateRows = wmsOutboundItemMapper.updateWmsOutboundItem(wmsOutboundItem);
        refreshOutboundTotal(wmsOutboundItem.getOutboundId());
        if (databaseOutboundItem != null && databaseOutboundItem.getOutboundId() != null
            && !databaseOutboundItem.getOutboundId().equals(wmsOutboundItem.getOutboundId()))
        {
            refreshOutboundTotal(databaseOutboundItem.getOutboundId());
        }
        return updateRows;
    }

    @Override
    public int deleteWmsOutboundItemById(Long outboundItemId)
    {
        WmsOutboundItem databaseOutboundItem = wmsOutboundItemMapper.selectWmsOutboundItemById(outboundItemId);
        if (databaseOutboundItem == null)
        {
            return 0;
        }
        WmsOutbound databaseOutbound = validateOutboundEditable(databaseOutboundItem.getOutboundId());
        if (databaseOutbound.getSaleOrderId() != null)
        {
            throw new ServiceException("关联销售单的出库明细不支持删除，请调整数量或作废整张出库单");
        }
        int deleteRows = wmsOutboundItemMapper.deleteWmsOutboundItemById(outboundItemId);
        if (databaseOutboundItem != null)
        {
            refreshOutboundTotal(databaseOutboundItem.getOutboundId());
        }
        return deleteRows;
    }

    @Override
    public int deleteWmsOutboundItemByIds(Long[] outboundItemIds)
    {
        List<Long> outboundIdList = wmsOutboundItemMapper.selectOutboundIdsByOutboundItemIds(outboundItemIds);
        if (outboundIdList != null && !outboundIdList.isEmpty())
        {
            Set<Long> outboundIdSet = new LinkedHashSet<Long>(outboundIdList);
            for (Long outboundId : outboundIdSet)
            {
                WmsOutbound databaseOutbound = validateOutboundEditable(outboundId);
                if (databaseOutbound.getSaleOrderId() != null)
                {
                    throw new ServiceException("关联销售单的出库明细不支持删除，请调整数量或作废整张出库单");
                }
            }
        }
        int deleteRows = wmsOutboundItemMapper.deleteWmsOutboundItemByIds(outboundItemIds);
        if (outboundIdList != null && !outboundIdList.isEmpty())
        {
            Set<Long> outboundIdSet = new LinkedHashSet<Long>(outboundIdList);
            for (Long outboundId : outboundIdSet)
            {
                refreshOutboundTotal(outboundId);
            }
        }
        return deleteRows;
    }

    /**
     * 校验出库单是否仍然允许维护明细
     *
     * @param outboundId 出库单编号
     * @return 出库单
     */
    private WmsOutbound validateOutboundEditable(Long outboundId)
    {
        if (outboundId == null)
        {
            throw new ServiceException("出库单不存在");
        }
        WmsOutbound databaseOutbound = wmsOutboundMapper.selectWmsOutboundById(outboundId);
        if (databaseOutbound == null)
        {
            throw new ServiceException("出库单不存在");
        }
        if (!STATUS_DRAFT.equals(databaseOutbound.getStatus()))
        {
            throw new ServiceException("仅草稿状态出库单允许维护明细");
        }
        return databaseOutbound;
    }

    /**
     * 保存前补齐出库明细金额，并校验来源销售单带出的约束
     *
     * @param databaseOutbound 数据库中的出库单
     * @param wmsOutboundItem 出库明细
     */
    private void prepareOutboundItemForSave(WmsOutbound databaseOutbound, WmsOutboundItem wmsOutboundItem)
    {
        if (databaseOutbound.getSaleOrderId() != null)
        {
            applySaleOrderItemConstraint(databaseOutbound, wmsOutboundItem);
        }
        wmsOutboundItem.setAmount(calculateAmount(wmsOutboundItem.getQuantity(), wmsOutboundItem.getPrice()));
    }

    /**
     * 根据来源销售明细锁定商品、库位、批次和单价，并校验出库数量范围
     *
     * @param databaseOutbound 数据库中的出库单
     * @param wmsOutboundItem 出库明细
     */
    private void applySaleOrderItemConstraint(WmsOutbound databaseOutbound, WmsOutboundItem wmsOutboundItem)
    {
        if (wmsOutboundItem.getSaleOrderItemId() == null)
        {
            throw new ServiceException("关联销售单的出库明细必须绑定来源销售明细");
        }
        List<WmsSaleOrderItem> saleOrderItemList = wmsSaleOrderItemMapper.selectWmsSaleOrderItemsByOrderId(databaseOutbound.getSaleOrderId());
        if (saleOrderItemList == null || saleOrderItemList.isEmpty())
        {
            throw new ServiceException("来源销售订单不存在可出库明细");
        }
        WmsSaleOrderItem saleOrderItem = findSaleOrderItem(saleOrderItemList, wmsOutboundItem.getSaleOrderItemId());
        if (saleOrderItem == null)
        {
            throw new ServiceException("来源销售明细不存在");
        }
        if (wmsOutboundItem.getQuantity() == null || wmsOutboundItem.getQuantity().compareTo(BigDecimal.ZERO) <= 0)
        {
            throw new ServiceException("出库数量必须大于0");
        }
        BigDecimal remainQuantity = saleOrderItem.getRemainQuantity() == null ? BigDecimal.ZERO : saleOrderItem.getRemainQuantity();
        if (wmsOutboundItem.getQuantity().compareTo(remainQuantity) > 0)
        {
            throw new ServiceException("出库数量不能超过待出库数量");
        }
        wmsOutboundItem.setProductId(saleOrderItem.getProductId());
        wmsOutboundItem.setLocationId(saleOrderItem.getLocationId());
        wmsOutboundItem.setBatchNo(saleOrderItem.getBatchNo());
        wmsOutboundItem.setPrice(saleOrderItem.getPrice());
        wmsOutboundItem.setSaleQuantity(saleOrderItem.getQuantity());
        wmsOutboundItem.setShippedQuantity(saleOrderItem.getOutboundQuantity());
        wmsOutboundItem.setRemainQuantity(saleOrderItem.getRemainQuantity());
    }

    /**
     * 根据销售订单明细编号查找来源销售明细
     *
     * @param saleOrderItemList 销售订单明细列表
     * @param saleOrderItemId 销售订单明细编号
     * @return 销售订单明细
     */
    private WmsSaleOrderItem findSaleOrderItem(List<WmsSaleOrderItem> saleOrderItemList, Long saleOrderItemId)
    {
        for (WmsSaleOrderItem saleOrderItem : saleOrderItemList)
        {
            if (Objects.equals(saleOrderItem.getSaleOrderItemId(), saleOrderItemId))
            {
                return saleOrderItem;
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
     * 刷新出库单主表的总数量和总金额
     *
     * @param outboundId 出库单编号
     */
    private void refreshOutboundTotal(Long outboundId)
    {
        if (outboundId != null)
        {
            wmsOutboundMapper.refreshTotalByOutboundId(outboundId);
        }
    }
}
