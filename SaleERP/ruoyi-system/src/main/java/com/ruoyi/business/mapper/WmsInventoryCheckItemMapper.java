package com.ruoyi.business.mapper;

import java.util.List;
import com.ruoyi.business.domain.WmsInventoryCheckItem;

public interface WmsInventoryCheckItemMapper
{
    public WmsInventoryCheckItem selectWmsInventoryCheckItemById(Long checkItemId);

    public List<WmsInventoryCheckItem> selectWmsInventoryCheckItemsByCheckId(Long checkId);

    public List<WmsInventoryCheckItem> selectWmsInventoryCheckItemList(WmsInventoryCheckItem wmsInventoryCheckItem);

    public int insertWmsInventoryCheckItem(WmsInventoryCheckItem wmsInventoryCheckItem);

    public int updateWmsInventoryCheckItem(WmsInventoryCheckItem wmsInventoryCheckItem);

    public int deleteWmsInventoryCheckItemById(Long checkItemId);

    public int deleteWmsInventoryCheckItemByIds(Long[] checkItemIds);

    public int deleteWmsInventoryCheckItemByCheckId(Long checkId);

    public int deleteWmsInventoryCheckItemByCheckIds(Long[] checkIds);
}
