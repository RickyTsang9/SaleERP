package com.ruoyi.business.service.impl;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.ruoyi.business.domain.WmsOutboundItem;
import com.ruoyi.business.mapper.WmsOutboundItemMapper;
import com.ruoyi.business.service.IWmsOutboundItemService;

@Service
public class WmsOutboundItemServiceImpl implements IWmsOutboundItemService
{
    @Autowired
    private WmsOutboundItemMapper wmsOutboundItemMapper;

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
        return wmsOutboundItemMapper.insertWmsOutboundItem(wmsOutboundItem);
    }

    @Override
    public int updateWmsOutboundItem(WmsOutboundItem wmsOutboundItem)
    {
        return wmsOutboundItemMapper.updateWmsOutboundItem(wmsOutboundItem);
    }

    @Override
    public int deleteWmsOutboundItemById(Long outboundItemId)
    {
        return wmsOutboundItemMapper.deleteWmsOutboundItemById(outboundItemId);
    }

    @Override
    public int deleteWmsOutboundItemByIds(Long[] outboundItemIds)
    {
        return wmsOutboundItemMapper.deleteWmsOutboundItemByIds(outboundItemIds);
    }
}
