package com.ruoyi.business.service.impl;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.ruoyi.business.domain.WmsInventoryCheck;
import com.ruoyi.business.domain.WmsInventoryCheckItem;
import com.ruoyi.business.mapper.WmsInventoryCheckItemMapper;
import com.ruoyi.business.mapper.WmsInventoryCheckMapper;
import com.ruoyi.business.service.IWmsInventoryCheckItemService;
import com.ruoyi.common.exception.ServiceException;

@Service
public class WmsInventoryCheckItemServiceImpl implements IWmsInventoryCheckItemService
{
    private static final String STATUS_DRAFT = "draft";

    @Autowired
    private WmsInventoryCheckItemMapper wmsInventoryCheckItemMapper;

    @Autowired
    private WmsInventoryCheckMapper wmsInventoryCheckMapper;

    @Override
    public WmsInventoryCheckItem selectWmsInventoryCheckItemById(Long checkItemId)
    {
        return wmsInventoryCheckItemMapper.selectWmsInventoryCheckItemById(checkItemId);
    }

    @Override
    public List<WmsInventoryCheckItem> selectWmsInventoryCheckItemList(WmsInventoryCheckItem wmsInventoryCheckItem)
    {
        return wmsInventoryCheckItemMapper.selectWmsInventoryCheckItemList(wmsInventoryCheckItem);
    }

    @Override
    public int insertWmsInventoryCheckItem(WmsInventoryCheckItem wmsInventoryCheckItem)
    {
        validateDraftCheck(wmsInventoryCheckItem.getCheckId());
        return wmsInventoryCheckItemMapper.insertWmsInventoryCheckItem(wmsInventoryCheckItem);
    }

    @Override
    public int updateWmsInventoryCheckItem(WmsInventoryCheckItem wmsInventoryCheckItem)
    {
        WmsInventoryCheckItem databaseInventoryCheckItem = wmsInventoryCheckItemMapper.selectWmsInventoryCheckItemById(wmsInventoryCheckItem.getCheckItemId());
        if (databaseInventoryCheckItem == null)
        {
            throw new ServiceException("盘点明细不存在");
        }
        validateDraftCheck(databaseInventoryCheckItem.getCheckId());
        wmsInventoryCheckItem.setCheckId(databaseInventoryCheckItem.getCheckId());
        return wmsInventoryCheckItemMapper.updateWmsInventoryCheckItem(wmsInventoryCheckItem);
    }

    @Override
    public int deleteWmsInventoryCheckItemById(Long checkItemId)
    {
        WmsInventoryCheckItem databaseInventoryCheckItem = wmsInventoryCheckItemMapper.selectWmsInventoryCheckItemById(checkItemId);
        if (databaseInventoryCheckItem == null)
        {
            return 0;
        }
        validateDraftCheck(databaseInventoryCheckItem.getCheckId());
        return wmsInventoryCheckItemMapper.deleteWmsInventoryCheckItemById(checkItemId);
    }

    @Override
    public int deleteWmsInventoryCheckItemByIds(Long[] checkItemIds)
    {
        for (Long checkItemId : checkItemIds)
        {
            WmsInventoryCheckItem databaseInventoryCheckItem = wmsInventoryCheckItemMapper.selectWmsInventoryCheckItemById(checkItemId);
            if (databaseInventoryCheckItem == null)
            {
                continue;
            }
            validateDraftCheck(databaseInventoryCheckItem.getCheckId());
        }
        return wmsInventoryCheckItemMapper.deleteWmsInventoryCheckItemByIds(checkItemIds);
    }

    private void validateDraftCheck(Long checkId)
    {
        if (checkId == null)
        {
            throw new ServiceException("盘点单编号不能为空");
        }
        WmsInventoryCheck databaseInventoryCheck = wmsInventoryCheckMapper.selectWmsInventoryCheckById(checkId);
        if (databaseInventoryCheck == null)
        {
            throw new ServiceException("盘点单不存在");
        }
        if (!STATUS_DRAFT.equals(databaseInventoryCheck.getStatus()))
        {
            throw new ServiceException("仅草稿状态盘点单允许维护明细");
        }
    }
}
