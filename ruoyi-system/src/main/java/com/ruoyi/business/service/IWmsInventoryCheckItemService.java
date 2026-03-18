package com.ruoyi.business.service;

import java.util.List;
import com.ruoyi.business.domain.WmsInventoryCheckItem;

public interface IWmsInventoryCheckItemService
{
    public WmsInventoryCheckItem selectWmsInventoryCheckItemById(Long checkItemId);

    public List<WmsInventoryCheckItem> selectWmsInventoryCheckItemList(WmsInventoryCheckItem wmsInventoryCheckItem);

    public int insertWmsInventoryCheckItem(WmsInventoryCheckItem wmsInventoryCheckItem);

    public int updateWmsInventoryCheckItem(WmsInventoryCheckItem wmsInventoryCheckItem);

    public int deleteWmsInventoryCheckItemById(Long checkItemId);

    public int deleteWmsInventoryCheckItemByIds(Long[] checkItemIds);
}
