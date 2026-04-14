package com.ruoyi.business.service.impl;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.ruoyi.business.domain.WmsPurchaseReturn;
import com.ruoyi.business.domain.WmsPurchaseReturnItem;
import com.ruoyi.business.mapper.WmsPurchaseReturnMapper;
import com.ruoyi.business.mapper.WmsPurchaseReturnItemMapper;
import com.ruoyi.business.service.IWmsPurchaseReturnItemService;
import com.ruoyi.common.exception.ServiceException;

@Service
public class WmsPurchaseReturnItemServiceImpl implements IWmsPurchaseReturnItemService
{
    private static final String STATUS_DRAFT = "draft";

    @Autowired
    private WmsPurchaseReturnItemMapper wmsPurchaseReturnItemMapper;

    @Autowired
    private WmsPurchaseReturnMapper wmsPurchaseReturnMapper;

    @Override
    public WmsPurchaseReturnItem selectWmsPurchaseReturnItemById(Long purchaseReturnItemId)
    {
        return wmsPurchaseReturnItemMapper.selectWmsPurchaseReturnItemById(purchaseReturnItemId);
    }

    @Override
    public List<WmsPurchaseReturnItem> selectWmsPurchaseReturnItemList(WmsPurchaseReturnItem wmsPurchaseReturnItem)
    {
        return wmsPurchaseReturnItemMapper.selectWmsPurchaseReturnItemList(wmsPurchaseReturnItem);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int insertWmsPurchaseReturnItem(WmsPurchaseReturnItem wmsPurchaseReturnItem)
    {
        validatePurchaseReturnEditable(wmsPurchaseReturnItem.getPurchaseReturnId());
        int insertRows = wmsPurchaseReturnItemMapper.insertWmsPurchaseReturnItem(wmsPurchaseReturnItem);
        refreshPurchaseReturnTotal(wmsPurchaseReturnItem.getPurchaseReturnId());
        return insertRows;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int updateWmsPurchaseReturnItem(WmsPurchaseReturnItem wmsPurchaseReturnItem)
    {
        WmsPurchaseReturnItem databasePurchaseReturnItem = wmsPurchaseReturnItemMapper.selectWmsPurchaseReturnItemById(wmsPurchaseReturnItem.getPurchaseReturnItemId());
        if (databasePurchaseReturnItem == null)
        {
            throw new ServiceException("采购退货明细不存在");
        }
        validatePurchaseReturnEditable(databasePurchaseReturnItem.getPurchaseReturnId());
        wmsPurchaseReturnItem.setPurchaseReturnId(databasePurchaseReturnItem.getPurchaseReturnId());
        int updateRows = wmsPurchaseReturnItemMapper.updateWmsPurchaseReturnItem(wmsPurchaseReturnItem);
        refreshPurchaseReturnTotal(wmsPurchaseReturnItem.getPurchaseReturnId());
        if (databasePurchaseReturnItem != null && databasePurchaseReturnItem.getPurchaseReturnId() != null
            && !databasePurchaseReturnItem.getPurchaseReturnId().equals(wmsPurchaseReturnItem.getPurchaseReturnId()))
        {
            refreshPurchaseReturnTotal(databasePurchaseReturnItem.getPurchaseReturnId());
        }
        return updateRows;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int deleteWmsPurchaseReturnItemById(Long purchaseReturnItemId)
    {
        WmsPurchaseReturnItem databasePurchaseReturnItem = wmsPurchaseReturnItemMapper.selectWmsPurchaseReturnItemById(purchaseReturnItemId);
        if (databasePurchaseReturnItem == null)
        {
            return 0;
        }
        validatePurchaseReturnEditable(databasePurchaseReturnItem.getPurchaseReturnId());
        int deleteRows = wmsPurchaseReturnItemMapper.deleteWmsPurchaseReturnItemById(purchaseReturnItemId);
        if (databasePurchaseReturnItem != null)
        {
            refreshPurchaseReturnTotal(databasePurchaseReturnItem.getPurchaseReturnId());
        }
        return deleteRows;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int deleteWmsPurchaseReturnItemByIds(Long[] purchaseReturnItemIds)
    {
        List<Long> purchaseReturnIdList = wmsPurchaseReturnItemMapper.selectPurchaseReturnIdsByPurchaseReturnItemIds(purchaseReturnItemIds);
        if (purchaseReturnIdList != null && !purchaseReturnIdList.isEmpty())
        {
            Set<Long> purchaseReturnIdSet = new LinkedHashSet<Long>(purchaseReturnIdList);
            for (Long purchaseReturnId : purchaseReturnIdSet)
            {
                validatePurchaseReturnEditable(purchaseReturnId);
            }
        }
        int deleteRows = wmsPurchaseReturnItemMapper.deleteWmsPurchaseReturnItemByIds(purchaseReturnItemIds);
        if (purchaseReturnIdList != null && !purchaseReturnIdList.isEmpty())
        {
            Set<Long> purchaseReturnIdSet = new LinkedHashSet<Long>(purchaseReturnIdList);
            for (Long purchaseReturnId : purchaseReturnIdSet)
            {
                refreshPurchaseReturnTotal(purchaseReturnId);
            }
        }
        return deleteRows;
    }

    /**
     * 校验采购退货单是否允许维护明细
     *
     * @param purchaseReturnId 采购退货单编号
     */
    private void validatePurchaseReturnEditable(Long purchaseReturnId)
    {
        if (purchaseReturnId == null)
        {
            throw new ServiceException("采购退货单不存在");
        }
        WmsPurchaseReturn databasePurchaseReturn = wmsPurchaseReturnMapper.selectWmsPurchaseReturnById(purchaseReturnId);
        if (databasePurchaseReturn == null)
        {
            throw new ServiceException("采购退货单不存在");
        }
        if (!STATUS_DRAFT.equals(databasePurchaseReturn.getStatus()))
        {
            throw new ServiceException("仅草稿状态采购退货单允许维护明细");
        }
    }

    private void refreshPurchaseReturnTotal(Long purchaseReturnId)
    {
        if (purchaseReturnId != null)
        {
            wmsPurchaseReturnMapper.refreshTotalByPurchaseReturnId(purchaseReturnId);
        }
    }
}
