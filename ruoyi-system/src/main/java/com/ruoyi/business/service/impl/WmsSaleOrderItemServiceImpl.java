package com.ruoyi.business.service.impl;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.ruoyi.business.domain.WmsSaleOrderItem;
import com.ruoyi.business.mapper.WmsSaleOrderMapper;
import com.ruoyi.business.mapper.WmsSaleOrderItemMapper;
import com.ruoyi.business.service.IWmsSaleOrderItemService;

@Service
public class WmsSaleOrderItemServiceImpl implements IWmsSaleOrderItemService
{
    @Autowired
    private WmsSaleOrderItemMapper wmsSaleOrderItemMapper;

    @Autowired
    private WmsSaleOrderMapper wmsSaleOrderMapper;

    @Override
    public WmsSaleOrderItem selectWmsSaleOrderItemById(Long saleOrderItemId)
    {
        return wmsSaleOrderItemMapper.selectWmsSaleOrderItemById(saleOrderItemId);
    }

    @Override
    public List<WmsSaleOrderItem> selectWmsSaleOrderItemList(WmsSaleOrderItem wmsSaleOrderItem)
    {
        return wmsSaleOrderItemMapper.selectWmsSaleOrderItemList(wmsSaleOrderItem);
    }

    @Override
    public List<WmsSaleOrderItem> selectWmsSaleOrderItemsByOrderId(Long saleOrderId)
    {
        return wmsSaleOrderItemMapper.selectWmsSaleOrderItemsByOrderId(saleOrderId);
    }

    @Override
    public int insertWmsSaleOrderItem(WmsSaleOrderItem wmsSaleOrderItem)
    {
        int insertRows = wmsSaleOrderItemMapper.insertWmsSaleOrderItem(wmsSaleOrderItem);
        refreshSaleOrderTotal(wmsSaleOrderItem.getSaleOrderId());
        return insertRows;
    }

    @Override
    public int updateWmsSaleOrderItem(WmsSaleOrderItem wmsSaleOrderItem)
    {
        WmsSaleOrderItem databaseSaleOrderItem = wmsSaleOrderItemMapper.selectWmsSaleOrderItemById(wmsSaleOrderItem.getSaleOrderItemId());
        int updateRows = wmsSaleOrderItemMapper.updateWmsSaleOrderItem(wmsSaleOrderItem);
        refreshSaleOrderTotal(wmsSaleOrderItem.getSaleOrderId());
        if (databaseSaleOrderItem != null && databaseSaleOrderItem.getSaleOrderId() != null
            && !databaseSaleOrderItem.getSaleOrderId().equals(wmsSaleOrderItem.getSaleOrderId()))
        {
            refreshSaleOrderTotal(databaseSaleOrderItem.getSaleOrderId());
        }
        return updateRows;
    }

    @Override
    public int deleteWmsSaleOrderItemById(Long saleOrderItemId)
    {
        WmsSaleOrderItem databaseSaleOrderItem = wmsSaleOrderItemMapper.selectWmsSaleOrderItemById(saleOrderItemId);
        int deleteRows = wmsSaleOrderItemMapper.deleteWmsSaleOrderItemById(saleOrderItemId);
        if (databaseSaleOrderItem != null)
        {
            refreshSaleOrderTotal(databaseSaleOrderItem.getSaleOrderId());
        }
        return deleteRows;
    }

    @Override
    public int deleteWmsSaleOrderItemByIds(Long[] saleOrderItemIds)
    {
        List<Long> saleOrderIdList = wmsSaleOrderItemMapper.selectSaleOrderIdsBySaleOrderItemIds(saleOrderItemIds);
        int deleteRows = wmsSaleOrderItemMapper.deleteWmsSaleOrderItemByIds(saleOrderItemIds);
        if (saleOrderIdList != null && !saleOrderIdList.isEmpty())
        {
            Set<Long> saleOrderIdSet = new LinkedHashSet<Long>(saleOrderIdList);
            for (Long saleOrderId : saleOrderIdSet)
            {
                refreshSaleOrderTotal(saleOrderId);
            }
        }
        return deleteRows;
    }

    private void refreshSaleOrderTotal(Long saleOrderId)
    {
        if (saleOrderId != null)
        {
            wmsSaleOrderMapper.refreshTotalBySaleOrderId(saleOrderId);
        }
    }
}
