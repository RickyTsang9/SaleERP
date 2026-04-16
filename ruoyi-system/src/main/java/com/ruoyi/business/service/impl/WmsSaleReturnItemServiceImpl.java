package com.ruoyi.business.service.impl;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.ruoyi.business.domain.WmsSaleReturn;
import com.ruoyi.business.domain.WmsSaleReturnItem;
import com.ruoyi.business.mapper.WmsSaleReturnMapper;
import com.ruoyi.business.mapper.WmsSaleReturnItemMapper;
import com.ruoyi.business.service.IWmsSaleReturnItemService;
import com.ruoyi.common.exception.ServiceException;

@Service
public class WmsSaleReturnItemServiceImpl implements IWmsSaleReturnItemService
{
    private static final String STATUS_DRAFT = "draft";

    @Autowired
    private WmsSaleReturnItemMapper wmsSaleReturnItemMapper;

    @Autowired
    private WmsSaleReturnMapper wmsSaleReturnMapper;

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
    @Transactional(rollbackFor = Exception.class)
    public int insertWmsSaleReturnItem(WmsSaleReturnItem wmsSaleReturnItem)
    {
        validateSaleReturnEditable(wmsSaleReturnItem.getSaleReturnId());
        int insertRows = wmsSaleReturnItemMapper.insertWmsSaleReturnItem(wmsSaleReturnItem);
        refreshSaleReturnTotal(wmsSaleReturnItem.getSaleReturnId());
        return insertRows;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int updateWmsSaleReturnItem(WmsSaleReturnItem wmsSaleReturnItem)
    {
        WmsSaleReturnItem databaseSaleReturnItem = wmsSaleReturnItemMapper.selectWmsSaleReturnItemById(wmsSaleReturnItem.getSaleReturnItemId());
        if (databaseSaleReturnItem == null)
        {
            throw new ServiceException("销售退货明细不存在");
        }
        validateSaleReturnEditable(databaseSaleReturnItem.getSaleReturnId());
        wmsSaleReturnItem.setSaleReturnId(databaseSaleReturnItem.getSaleReturnId());
        int updateRows = wmsSaleReturnItemMapper.updateWmsSaleReturnItem(wmsSaleReturnItem);
        refreshSaleReturnTotal(wmsSaleReturnItem.getSaleReturnId());
        if (databaseSaleReturnItem != null && databaseSaleReturnItem.getSaleReturnId() != null
            && !databaseSaleReturnItem.getSaleReturnId().equals(wmsSaleReturnItem.getSaleReturnId()))
        {
            refreshSaleReturnTotal(databaseSaleReturnItem.getSaleReturnId());
        }
        return updateRows;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int deleteWmsSaleReturnItemById(Long saleReturnItemId)
    {
        WmsSaleReturnItem databaseSaleReturnItem = wmsSaleReturnItemMapper.selectWmsSaleReturnItemById(saleReturnItemId);
        if (databaseSaleReturnItem == null)
        {
            return 0;
        }
        validateSaleReturnEditable(databaseSaleReturnItem.getSaleReturnId());
        int deleteRows = wmsSaleReturnItemMapper.deleteWmsSaleReturnItemById(saleReturnItemId);
        if (databaseSaleReturnItem != null)
        {
            refreshSaleReturnTotal(databaseSaleReturnItem.getSaleReturnId());
        }
        return deleteRows;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int deleteWmsSaleReturnItemByIds(Long[] saleReturnItemIds)
    {
        List<Long> saleReturnIdList = wmsSaleReturnItemMapper.selectSaleReturnIdsBySaleReturnItemIds(saleReturnItemIds);
        if (saleReturnIdList != null && !saleReturnIdList.isEmpty())
        {
            Set<Long> saleReturnIdSet = new LinkedHashSet<Long>(saleReturnIdList);
            for (Long saleReturnId : saleReturnIdSet)
            {
                validateSaleReturnEditable(saleReturnId);
            }
        }
        int deleteRows = wmsSaleReturnItemMapper.deleteWmsSaleReturnItemByIds(saleReturnItemIds);
        if (saleReturnIdList != null && !saleReturnIdList.isEmpty())
        {
            Set<Long> saleReturnIdSet = new LinkedHashSet<Long>(saleReturnIdList);
            for (Long saleReturnId : saleReturnIdSet)
            {
                refreshSaleReturnTotal(saleReturnId);
            }
        }
        return deleteRows;
    }

    /**
     * 校验销售退货单是否允许维护明细
     *
     * @param saleReturnId 销售退货单编号
     */
    private void validateSaleReturnEditable(Long saleReturnId)
    {
        if (saleReturnId == null)
        {
            throw new ServiceException("销售退货单不存在");
        }
        WmsSaleReturn databaseSaleReturn = wmsSaleReturnMapper.selectWmsSaleReturnById(saleReturnId);
        if (databaseSaleReturn == null)
        {
            throw new ServiceException("销售退货单不存在");
        }
        if (!STATUS_DRAFT.equals(databaseSaleReturn.getStatus()))
        {
            throw new ServiceException("仅草稿状态销售退货单允许维护明细");
        }
    }

    private void refreshSaleReturnTotal(Long saleReturnId)
    {
        if (saleReturnId != null)
        {
            wmsSaleReturnMapper.refreshTotalBySaleReturnId(saleReturnId);
        }
    }
}
