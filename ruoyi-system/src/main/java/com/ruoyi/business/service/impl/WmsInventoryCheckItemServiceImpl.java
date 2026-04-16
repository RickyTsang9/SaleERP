package com.ruoyi.business.service.impl;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
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

    /**
     * 新增盘点明细
     *
     * @param wmsInventoryCheckItem 盘点明细
     * @return 新增结果
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public int insertWmsInventoryCheckItem(WmsInventoryCheckItem wmsInventoryCheckItem)
    {
        validateDraftCheck(wmsInventoryCheckItem.getCheckId());
        return wmsInventoryCheckItemMapper.insertWmsInventoryCheckItem(wmsInventoryCheckItem);
    }

    /**
     * 修改盘点明细
     *
     * @param wmsInventoryCheckItem 盘点明细
     * @return 修改结果
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
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

    /**
     * 删除盘点明细
     *
     * @param checkItemId 盘点明细编号
     * @return 删除结果
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
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

    /**
     * 批量删除盘点明细
     *
     * @param checkItemIds 盘点明细编号集合
     * @return 删除结果
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
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

    /**
     * 校验盘点单是否仍允许维护明细
     *
     * @param checkId 盘点单编号
     */
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
