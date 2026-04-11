package com.ruoyi.business.service;

import java.util.List;
import com.ruoyi.business.domain.WmsInboundItem;

public interface IWmsInboundItemService
{
    public WmsInboundItem selectWmsInboundItemById(Long inboundItemId);

    public List<WmsInboundItem> selectWmsInboundItemList(WmsInboundItem wmsInboundItem);

    public int insertWmsInboundItem(WmsInboundItem wmsInboundItem);

    public int updateWmsInboundItem(WmsInboundItem wmsInboundItem);

    public int deleteWmsInboundItemById(Long inboundItemId);

    public int deleteWmsInboundItemByIds(Long[] inboundItemIds);
}
