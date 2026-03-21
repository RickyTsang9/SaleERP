package com.ruoyi.business.service.impl;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.ruoyi.business.domain.WmsOutboundItem;
import com.ruoyi.business.mapper.WmsOutboundMapper;
import com.ruoyi.business.mapper.WmsOutboundItemMapper;
import com.ruoyi.business.service.IWmsOutboundItemService;

@Service
public class WmsOutboundItemServiceImpl implements IWmsOutboundItemService
{
    @Autowired
    private WmsOutboundItemMapper wmsOutboundItemMapper;

    @Autowired
    private WmsOutboundMapper wmsOutboundMapper;

    @Override
    public WmsOutboundItem selectWmsOutboundItemById(Long outboundItemId)
    {
        return wmsOutboundItemMapper.selectWmsOutboundItemById(outboundItemId);
    }

    @Override
    public List<WmsOutboundItem> selectWmsOutboundItemList(WmsOutboundItem wmsOutboundItem)
    {
        return wmsOutboundItemMapper.selectWmsOutboundItemList(wmsOutboundItem);
    }

    @Override
    public int insertWmsOutboundItem(WmsOutboundItem wmsOutboundItem)
    {
        int insertRows = wmsOutboundItemMapper.insertWmsOutboundItem(wmsOutboundItem);
        refreshOutboundTotal(wmsOutboundItem.getOutboundId());
        return insertRows;
    }

    @Override
    public int updateWmsOutboundItem(WmsOutboundItem wmsOutboundItem)
    {
        WmsOutboundItem databaseOutboundItem = wmsOutboundItemMapper.selectWmsOutboundItemById(wmsOutboundItem.getOutboundItemId());
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

    private void refreshOutboundTotal(Long outboundId)
    {
        if (outboundId != null)
        {
            wmsOutboundMapper.refreshTotalByOutboundId(outboundId);
        }
    }
}
