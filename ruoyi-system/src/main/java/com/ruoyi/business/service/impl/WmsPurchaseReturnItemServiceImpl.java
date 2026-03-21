package com.ruoyi.business.service.impl;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.ruoyi.business.domain.WmsPurchaseReturnItem;
import com.ruoyi.business.mapper.WmsPurchaseReturnMapper;
import com.ruoyi.business.mapper.WmsPurchaseReturnItemMapper;
import com.ruoyi.business.service.IWmsPurchaseReturnItemService;

@Service
public class WmsPurchaseReturnItemServiceImpl implements IWmsPurchaseReturnItemService
{
    @Autowired
    private WmsPurchaseReturnItemMapper wmsPurchaseReturnItemMapper;

    @Autowired
    private WmsPurchaseReturnMapper wmsPurchaseReturnMapper;

    @Override
    public WmsPurchaseReturnItem selectWmsPurchaseReturnItemById(Long purchaseReturnItemId)
    {
        return wmsPurchaseReturnItemMapper.selectWmsPurchaseReturnItemById(purchaseReturnItemId);
    }

    @Override
    public List<WmsPurchaseReturnItem> selectWmsPurchaseReturnItemList(WmsPurchaseReturnItem wmsPurchaseReturnItem)
    {
        return wmsPurchaseReturnItemMapper.selectWmsPurchaseReturnItemList(wmsPurchaseReturnItem);
    }

    @Override
    public int insertWmsPurchaseReturnItem(WmsPurchaseReturnItem wmsPurchaseReturnItem)
    {
        int insertRows = wmsPurchaseReturnItemMapper.insertWmsPurchaseReturnItem(wmsPurchaseReturnItem);
        refreshPurchaseReturnTotal(wmsPurchaseReturnItem.getPurchaseReturnId());
        return insertRows;
    }

    @Override
    public int updateWmsPurchaseReturnItem(WmsPurchaseReturnItem wmsPurchaseReturnItem)
    {
        WmsPurchaseReturnItem databasePurchaseReturnItem = wmsPurchaseReturnItemMapper.selectWmsPurchaseReturnItemById(wmsPurchaseReturnItem.getPurchaseReturnItemId());
        int updateRows = wmsPurchaseReturnItemMapper.updateWmsPurchaseReturnItem(wmsPurchaseReturnItem);
        refreshPurchaseReturnTotal(wmsPurchaseReturnItem.getPurchaseReturnId());
        if (databasePurchaseReturnItem != null && databasePurchaseReturnItem.getPurchaseReturnId() != null
            && !databasePurchaseReturnItem.getPurchaseReturnId().equals(wmsPurchaseReturnItem.getPurchaseReturnId()))
        {
            refreshPurchaseReturnTotal(databasePurchaseReturnItem.getPurchaseReturnId());
        }
        return updateRows;
    }

    @Override
    public int deleteWmsPurchaseReturnItemById(Long purchaseReturnItemId)
    {
        WmsPurchaseReturnItem databasePurchaseReturnItem = wmsPurchaseReturnItemMapper.selectWmsPurchaseReturnItemById(purchaseReturnItemId);
        int deleteRows = wmsPurchaseReturnItemMapper.deleteWmsPurchaseReturnItemById(purchaseReturnItemId);
        if (databasePurchaseReturnItem != null)
        {
            refreshPurchaseReturnTotal(databasePurchaseReturnItem.getPurchaseReturnId());
        }
        return deleteRows;
    }

    @Override
    public int deleteWmsPurchaseReturnItemByIds(Long[] purchaseReturnItemIds)
    {
        List<Long> purchaseReturnIdList = wmsPurchaseReturnItemMapper.selectPurchaseReturnIdsByPurchaseReturnItemIds(purchaseReturnItemIds);
        int deleteRows = wmsPurchaseReturnItemMapper.deleteWmsPurchaseReturnItemByIds(purchaseReturnItemIds);
        if (purchaseReturnIdList != null && !purchaseReturnIdList.isEmpty())
        {
            Set<Long> purchaseReturnIdSet = new LinkedHashSet<Long>(purchaseReturnIdList);
            for (Long purchaseReturnId : purchaseReturnIdSet)
            {
                refreshPurchaseReturnTotal(purchaseReturnId);
            }
        }
        return deleteRows;
    }

    private void refreshPurchaseReturnTotal(Long purchaseReturnId)
    {
        if (purchaseReturnId != null)
        {
            wmsPurchaseReturnMapper.refreshTotalByPurchaseReturnId(purchaseReturnId);
        }
    }
}
