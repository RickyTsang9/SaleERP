package com.ruoyi.business.service;

import java.util.List;
import com.ruoyi.business.domain.WmsSaleReturnItem;

public interface IWmsSaleReturnItemService
{
    public WmsSaleReturnItem selectWmsSaleReturnItemById(Long saleReturnItemId);

    public List<WmsSaleReturnItem> selectWmsSaleReturnItemList(WmsSaleReturnItem wmsSaleReturnItem);

    public int insertWmsSaleReturnItem(WmsSaleReturnItem wmsSaleReturnItem);

    public int updateWmsSaleReturnItem(WmsSaleReturnItem wmsSaleReturnItem);

    public int deleteWmsSaleReturnItemById(Long saleReturnItemId);

    public int deleteWmsSaleReturnItemByIds(Long[] saleReturnItemIds);
}
