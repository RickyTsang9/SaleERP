package com.ruoyi.business.mapper;

import java.util.List;
import com.ruoyi.business.domain.WmsPurchaseReturnItem;

public interface WmsPurchaseReturnItemMapper
{
    public WmsPurchaseReturnItem selectWmsPurchaseReturnItemById(Long purchaseReturnItemId);

    public List<WmsPurchaseReturnItem> selectWmsPurchaseReturnItemList(WmsPurchaseReturnItem wmsPurchaseReturnItem);

    public int insertWmsPurchaseReturnItem(WmsPurchaseReturnItem wmsPurchaseReturnItem);

    public int updateWmsPurchaseReturnItem(WmsPurchaseReturnItem wmsPurchaseReturnItem);

    public int deleteWmsPurchaseReturnItemById(Long purchaseReturnItemId);

    public int deleteWmsPurchaseReturnItemByIds(Long[] purchaseReturnItemIds);
}
