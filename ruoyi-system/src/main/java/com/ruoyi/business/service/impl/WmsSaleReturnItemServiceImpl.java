package com.ruoyi.business.service.impl;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.ruoyi.business.domain.WmsSaleReturnItem;
import com.ruoyi.business.mapper.WmsSaleReturnItemMapper;
import com.ruoyi.business.service.IWmsSaleReturnItemService;

@Service
public class WmsSaleReturnItemServiceImpl implements IWmsSaleReturnItemService
{
    @Autowired
    private WmsSaleReturnItemMapper wmsSaleReturnItemMapper;

    @Override
    public WmsSaleReturnItem selectWmsSaleReturnItemById(Long saleReturnItemId)
    {
        return wmsSaleReturnItemMapper.selectWmsSaleReturnItemById(saleReturnItemId);
    }

    @Override
    public List<WmsSaleReturnItem> selectWmsSaleReturnItemList(WmsSaleReturnItem wmsSaleReturnItem)
    {
        return wmsSaleReturnItemMapper.selectWmsSaleReturnItemList(wmsSaleReturnItem);
    }

    @Override
    public int insertWmsSaleReturnItem(WmsSaleReturnItem wmsSaleReturnItem)
    {
        return wmsSaleReturnItemMapper.insertWmsSaleReturnItem(wmsSaleReturnItem);
    }

    @Override
    public int updateWmsSaleReturnItem(WmsSaleReturnItem wmsSaleReturnItem)
    {
        return wmsSaleReturnItemMapper.updateWmsSaleReturnItem(wmsSaleReturnItem);
    }

    @Override
    public int deleteWmsSaleReturnItemById(Long saleReturnItemId)
    {
        return wmsSaleReturnItemMapper.deleteWmsSaleReturnItemById(saleReturnItemId);
    }

    @Override
    public int deleteWmsSaleReturnItemByIds(Long[] saleReturnItemIds)
    {
        return wmsSaleReturnItemMapper.deleteWmsSaleReturnItemByIds(saleReturnItemIds);
    }
}
