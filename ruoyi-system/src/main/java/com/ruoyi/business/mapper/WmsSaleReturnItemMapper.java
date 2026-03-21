package com.ruoyi.business.mapper;

import java.util.List;
import com.ruoyi.business.domain.WmsSaleReturnItem;

public interface WmsSaleReturnItemMapper
{
    public WmsSaleReturnItem selectWmsSaleReturnItemById(Long saleReturnItemId);

    public List<WmsSaleReturnItem> selectWmsSaleReturnItemsByReturnId(Long saleReturnId);

    public List<WmsSaleReturnItem> selectWmsSaleReturnItemList(WmsSaleReturnItem wmsSaleReturnItem);

    public int insertWmsSaleReturnItem(WmsSaleReturnItem wmsSaleReturnItem);

    public int updateWmsSaleReturnItem(WmsSaleReturnItem wmsSaleReturnItem);

    public int deleteWmsSaleReturnItemById(Long saleReturnItemId);

    public int deleteWmsSaleReturnItemByIds(Long[] saleReturnItemIds);

    public int deleteWmsSaleReturnItemBySaleReturnIds(Long[] saleReturnIds);

    public List<Long> selectSaleReturnIdsBySaleReturnItemIds(Long[] saleReturnItemIds);
}
