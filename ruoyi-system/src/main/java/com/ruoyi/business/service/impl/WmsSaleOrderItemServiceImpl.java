package com.ruoyi.business.service.impl;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.ruoyi.business.domain.WmsSaleOrderItem;
import com.ruoyi.business.mapper.WmsSaleOrderItemMapper;
import com.ruoyi.business.service.IWmsSaleOrderItemService;

@Service
public class WmsSaleOrderItemServiceImpl implements IWmsSaleOrderItemService
{
    @Autowired
    private WmsSaleOrderItemMapper wmsSaleOrderItemMapper;

    @Override
    public WmsSaleOrderItem selectWmsSaleOrderItemById(Long saleOrderItemId)
    {
        return wmsSaleOrderItemMapper.selectWmsSaleOrderItemById(saleOrderItemId);
    }

    @Override
    public List<WmsSaleOrderItem> selectWmsSaleOrderItemList(WmsSaleOrderItem wmsSaleOrderItem)
    {
        return wmsSaleOrderItemMapper.selectWmsSaleOrderItemList(wmsSaleOrderItem);
    }

    @Override
    public List<WmsSaleOrderItem> selectWmsSaleOrderItemsByOrderId(Long saleOrderId)
    {
        return wmsSaleOrderItemMapper.selectWmsSaleOrderItemsByOrderId(saleOrderId);
    }

    @Override
    public int insertWmsSaleOrderItem(WmsSaleOrderItem wmsSaleOrderItem)
    {
        return wmsSaleOrderItemMapper.insertWmsSaleOrderItem(wmsSaleOrderItem);
    }

    @Override
    public int updateWmsSaleOrderItem(WmsSaleOrderItem wmsSaleOrderItem)
    {
        return wmsSaleOrderItemMapper.updateWmsSaleOrderItem(wmsSaleOrderItem);
    }

    @Override
    public int deleteWmsSaleOrderItemById(Long saleOrderItemId)
    {
        return wmsSaleOrderItemMapper.deleteWmsSaleOrderItemById(saleOrderItemId);
    }

    @Override
    public int deleteWmsSaleOrderItemByIds(Long[] saleOrderItemIds)
    {
        return wmsSaleOrderItemMapper.deleteWmsSaleOrderItemByIds(saleOrderItemIds);
    }
}
