package com.ruoyi.business.service.impl;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.ruoyi.business.domain.WmsInboundItem;
import com.ruoyi.business.mapper.WmsInboundItemMapper;
import com.ruoyi.business.service.IWmsInboundItemService;

@Service
public class WmsInboundItemServiceImpl implements IWmsInboundItemService
{
    @Autowired
    private WmsInboundItemMapper wmsInboundItemMapper;

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
        return wmsInboundItemMapper.insertWmsInboundItem(wmsInboundItem);
    }

    @Override
    public int updateWmsInboundItem(WmsInboundItem wmsInboundItem)
    {
        return wmsInboundItemMapper.updateWmsInboundItem(wmsInboundItem);
    }

    @Override
    public int deleteWmsInboundItemById(Long inboundItemId)
    {
        return wmsInboundItemMapper.deleteWmsInboundItemById(inboundItemId);
    }

    @Override
    public int deleteWmsInboundItemByIds(Long[] inboundItemIds)
    {
        return wmsInboundItemMapper.deleteWmsInboundItemByIds(inboundItemIds);
    }
}
