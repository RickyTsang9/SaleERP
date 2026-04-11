package com.ruoyi.business.service;

import java.util.List;
import com.ruoyi.business.domain.WmsPurchaseReturnItem;

public interface IWmsPurchaseReturnItemService
{
    public WmsPurchaseReturnItem selectWmsPurchaseReturnItemById(Long purchaseReturnItemId);

    public List<WmsPurchaseReturnItem> selectWmsPurchaseReturnItemList(WmsPurchaseReturnItem wmsPurchaseReturnItem);

    public int insertWmsPurchaseReturnItem(WmsPurchaseReturnItem wmsPurchaseReturnItem);

    public int updateWmsPurchaseReturnItem(WmsPurchaseReturnItem wmsPurchaseReturnItem);

    public int deleteWmsPurchaseReturnItemById(Long purchaseReturnItemId);

    public int deleteWmsPurchaseReturnItemByIds(Long[] purchaseReturnItemIds);
}
