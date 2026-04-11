package com.ruoyi.business.service.impl;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.ruoyi.business.domain.WmsSaleReturnItem;
import com.ruoyi.business.mapper.WmsSaleReturnMapper;
import com.ruoyi.business.mapper.WmsSaleReturnItemMapper;
import com.ruoyi.business.service.IWmsSaleReturnItemService;

@Service
public class WmsSaleReturnItemServiceImpl implements IWmsSaleReturnItemService
{
    @Autowired
    private WmsSaleReturnItemMapper wmsSaleReturnItemMapper;

    @Autowired
    private WmsSaleReturnMapper wmsSaleReturnMapper;

    @Override
    public WmsSaleReturnItem selectWmsSaleReturnItemById(Long saleReturnItemId)
    {
        return wmsSaleReturnItemMapper.selectWmsSaleReturnItemById(saleReturnItemId);
    }

    @Override
    public List<WmsSaleReturnItem> selectWmsSaleReturnItemList(WmsSaleReturnItem wmsSaleReturnItem)
    {
        return wmsSaleReturnItemMapper.selectWmsSaleReturnItemList(wmsSaleReturnItem);
    }

    @Override
    public int insertWmsSaleReturnItem(WmsSaleReturnItem wmsSaleReturnItem)
    {
        int insertRows = wmsSaleReturnItemMapper.insertWmsSaleReturnItem(wmsSaleReturnItem);
        refreshSaleReturnTotal(wmsSaleReturnItem.getSaleReturnId());
        return insertRows;
    }

    @Override
    public int updateWmsSaleReturnItem(WmsSaleReturnItem wmsSaleReturnItem)
    {
        WmsSaleReturnItem databaseSaleReturnItem = wmsSaleReturnItemMapper.selectWmsSaleReturnItemById(wmsSaleReturnItem.getSaleReturnItemId());
        int updateRows = wmsSaleReturnItemMapper.updateWmsSaleReturnItem(wmsSaleReturnItem);
        refreshSaleReturnTotal(wmsSaleReturnItem.getSaleReturnId());
        if (databaseSaleReturnItem != null && databaseSaleReturnItem.getSaleReturnId() != null
            && !databaseSaleReturnItem.getSaleReturnId().equals(wmsSaleReturnItem.getSaleReturnId()))
        {
            refreshSaleReturnTotal(databaseSaleReturnItem.getSaleReturnId());
        }
        return updateRows;
    }

    @Override
    public int deleteWmsSaleReturnItemById(Long saleReturnItemId)
    {
        WmsSaleReturnItem databaseSaleReturnItem = wmsSaleReturnItemMapper.selectWmsSaleReturnItemById(saleReturnItemId);
        int deleteRows = wmsSaleReturnItemMapper.deleteWmsSaleReturnItemById(saleReturnItemId);
        if (databaseSaleReturnItem != null)
        {
            refreshSaleReturnTotal(databaseSaleReturnItem.getSaleReturnId());
        }
        return deleteRows;
    }

    @Override
    public int deleteWmsSaleReturnItemByIds(Long[] saleReturnItemIds)
    {
        List<Long> saleReturnIdList = wmsSaleReturnItemMapper.selectSaleReturnIdsBySaleReturnItemIds(saleReturnItemIds);
        int deleteRows = wmsSaleReturnItemMapper.deleteWmsSaleReturnItemByIds(saleReturnItemIds);
        if (saleReturnIdList != null && !saleReturnIdList.isEmpty())
        {
            Set<Long> saleReturnIdSet = new LinkedHashSet<Long>(saleReturnIdList);
            for (Long saleReturnId : saleReturnIdSet)
            {
                refreshSaleReturnTotal(saleReturnId);
            }
        }
        return deleteRows;
    }

    private void refreshSaleReturnTotal(Long saleReturnId)
    {
        if (saleReturnId != null)
        {
            wmsSaleReturnMapper.refreshTotalBySaleReturnId(saleReturnId);
        }
    }
}
