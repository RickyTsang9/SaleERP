package com.ruoyi.business.service.impl;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.ruoyi.business.domain.WmsInboundItem;
import com.ruoyi.business.mapper.WmsInboundMapper;
import com.ruoyi.business.mapper.WmsInboundItemMapper;
import com.ruoyi.business.service.IWmsInboundItemService;

@Service
public class WmsInboundItemServiceImpl implements IWmsInboundItemService
{
    @Autowired
    private WmsInboundItemMapper wmsInboundItemMapper;

    @Autowired
    private WmsInboundMapper wmsInboundMapper;

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
    public int insertWmsInboundItem(WmsInboundItem wmsInboundItem)
    {
        int insertRows = wmsInboundItemMapper.insertWmsInboundItem(wmsInboundItem);
        refreshInboundTotal(wmsInboundItem.getInboundId());
        return insertRows;
    }

    @Override
    public int updateWmsInboundItem(WmsInboundItem wmsInboundItem)
    {
        WmsInboundItem databaseInboundItem = wmsInboundItemMapper.selectWmsInboundItemById(wmsInboundItem.getInboundItemId());
        int updateRows = wmsInboundItemMapper.updateWmsInboundItem(wmsInboundItem);
        refreshInboundTotal(wmsInboundItem.getInboundId());
        if (databaseInboundItem != null && databaseInboundItem.getInboundId() != null
            && !databaseInboundItem.getInboundId().equals(wmsInboundItem.getInboundId()))
        {
            refreshInboundTotal(databaseInboundItem.getInboundId());
        }
        return updateRows;
    }

    @Override
    public int deleteWmsInboundItemById(Long inboundItemId)
    {
        WmsInboundItem databaseInboundItem = wmsInboundItemMapper.selectWmsInboundItemById(inboundItemId);
        int deleteRows = wmsInboundItemMapper.deleteWmsInboundItemById(inboundItemId);
        if (databaseInboundItem != null)
        {
            refreshInboundTotal(databaseInboundItem.getInboundId());
        }
        return deleteRows;
    }

    @Override
    public int deleteWmsInboundItemByIds(Long[] inboundItemIds)
    {
        List<Long> inboundIdList = wmsInboundItemMapper.selectInboundIdsByInboundItemIds(inboundItemIds);
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

    private void refreshInboundTotal(Long inboundId)
    {
        if (inboundId != null)
        {
            wmsInboundMapper.refreshTotalByInboundId(inboundId);
        }
    }
}
