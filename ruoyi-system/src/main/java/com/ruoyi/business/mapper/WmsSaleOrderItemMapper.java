package com.ruoyi.business.mapper;

import java.util.List;
import com.ruoyi.business.domain.WmsSaleOrderItem;

public interface WmsSaleOrderItemMapper
{
    public WmsSaleOrderItem selectWmsSaleOrderItemById(Long saleOrderItemId);

    public List<WmsSaleOrderItem> selectWmsSaleOrderItemList(WmsSaleOrderItem wmsSaleOrderItem);

    public List<WmsSaleOrderItem> selectWmsSaleOrderItemsByOrderId(Long saleOrderId);

    public int insertWmsSaleOrderItem(WmsSaleOrderItem wmsSaleOrderItem);

    public int updateWmsSaleOrderItem(WmsSaleOrderItem wmsSaleOrderItem);

    public int deleteWmsSaleOrderItemById(Long saleOrderItemId);

    public int deleteWmsSaleOrderItemByIds(Long[] saleOrderItemIds);
}
