package com.ruoyi.business.service;

import java.util.List;
import com.ruoyi.business.domain.WmsOutboundItem;

public interface IWmsOutboundItemService
{
    public WmsOutboundItem selectWmsOutboundItemById(Long outboundItemId);

    public List<WmsOutboundItem> selectWmsOutboundItemList(WmsOutboundItem wmsOutboundItem);

    public int insertWmsOutboundItem(WmsOutboundItem wmsOutboundItem);

    public int updateWmsOutboundItem(WmsOutboundItem wmsOutboundItem);

    public int deleteWmsOutboundItemById(Long outboundItemId);

    public int deleteWmsOutboundItemByIds(Long[] outboundItemIds);
}
