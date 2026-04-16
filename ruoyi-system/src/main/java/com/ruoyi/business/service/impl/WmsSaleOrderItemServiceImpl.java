package com.ruoyi.business.service.impl;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.ruoyi.business.domain.WmsSaleOrder;
import com.ruoyi.business.domain.WmsSaleOrderItem;
import com.ruoyi.business.mapper.WmsSaleOrderMapper;
import com.ruoyi.business.mapper.WmsSaleOrderItemMapper;
import com.ruoyi.business.service.IWmsSaleOrderItemService;
import com.ruoyi.common.exception.ServiceException;

@Service
public class WmsSaleOrderItemServiceImpl implements IWmsSaleOrderItemService
{
    private static final String STATUS_DRAFT = "draft";

    @Autowired
    private WmsSaleOrderItemMapper wmsSaleOrderItemMapper;

    @Autowired
    private WmsSaleOrderMapper wmsSaleOrderMapper;

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
    @Transactional(rollbackFor = Exception.class)
    public int insertWmsSaleOrderItem(WmsSaleOrderItem wmsSaleOrderItem)
    {
        validateSaleOrderEditable(wmsSaleOrderItem.getSaleOrderId());
        int insertRows = wmsSaleOrderItemMapper.insertWmsSaleOrderItem(wmsSaleOrderItem);
        refreshSaleOrderTotal(wmsSaleOrderItem.getSaleOrderId());
        return insertRows;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int updateWmsSaleOrderItem(WmsSaleOrderItem wmsSaleOrderItem)
    {
        WmsSaleOrderItem databaseSaleOrderItem = wmsSaleOrderItemMapper.selectWmsSaleOrderItemById(wmsSaleOrderItem.getSaleOrderItemId());
        if (databaseSaleOrderItem == null)
        {
            throw new ServiceException("销售明细不存在");
        }
        validateSaleOrderEditable(databaseSaleOrderItem.getSaleOrderId());
        wmsSaleOrderItem.setSaleOrderId(databaseSaleOrderItem.getSaleOrderId());
        int updateRows = wmsSaleOrderItemMapper.updateWmsSaleOrderItem(wmsSaleOrderItem);
        refreshSaleOrderTotal(wmsSaleOrderItem.getSaleOrderId());
        if (databaseSaleOrderItem != null && databaseSaleOrderItem.getSaleOrderId() != null
            && !databaseSaleOrderItem.getSaleOrderId().equals(wmsSaleOrderItem.getSaleOrderId()))
        {
            refreshSaleOrderTotal(databaseSaleOrderItem.getSaleOrderId());
        }
        return updateRows;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int deleteWmsSaleOrderItemById(Long saleOrderItemId)
    {
        WmsSaleOrderItem databaseSaleOrderItem = wmsSaleOrderItemMapper.selectWmsSaleOrderItemById(saleOrderItemId);
        if (databaseSaleOrderItem == null)
        {
            return 0;
        }
        validateSaleOrderEditable(databaseSaleOrderItem.getSaleOrderId());
        int deleteRows = wmsSaleOrderItemMapper.deleteWmsSaleOrderItemById(saleOrderItemId);
        if (databaseSaleOrderItem != null)
        {
            refreshSaleOrderTotal(databaseSaleOrderItem.getSaleOrderId());
        }
        return deleteRows;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int deleteWmsSaleOrderItemByIds(Long[] saleOrderItemIds)
    {
        List<Long> saleOrderIdList = wmsSaleOrderItemMapper.selectSaleOrderIdsBySaleOrderItemIds(saleOrderItemIds);
        if (saleOrderIdList != null && !saleOrderIdList.isEmpty())
        {
            Set<Long> saleOrderIdSet = new LinkedHashSet<Long>(saleOrderIdList);
            for (Long saleOrderId : saleOrderIdSet)
            {
                validateSaleOrderEditable(saleOrderId);
            }
        }
        int deleteRows = wmsSaleOrderItemMapper.deleteWmsSaleOrderItemByIds(saleOrderItemIds);
        if (saleOrderIdList != null && !saleOrderIdList.isEmpty())
        {
            Set<Long> saleOrderIdSet = new LinkedHashSet<Long>(saleOrderIdList);
            for (Long saleOrderId : saleOrderIdSet)
            {
                refreshSaleOrderTotal(saleOrderId);
            }
        }
        return deleteRows;
    }

    /**
     * 校验销售单是否允许维护明细
     *
     * @param saleOrderId 销售单编号
     */
    private void validateSaleOrderEditable(Long saleOrderId)
    {
        if (saleOrderId == null)
        {
            throw new ServiceException("销售单不存在");
        }
        WmsSaleOrder databaseSaleOrder = wmsSaleOrderMapper.selectWmsSaleOrderById(saleOrderId);
        if (databaseSaleOrder == null)
        {
            throw new ServiceException("销售单不存在");
        }
        if (!STATUS_DRAFT.equals(databaseSaleOrder.getStatus()))
        {
            throw new ServiceException("仅草稿状态销售单允许维护明细");
        }
    }

    private void refreshSaleOrderTotal(Long saleOrderId)
    {
        if (saleOrderId != null)
        {
            wmsSaleOrderMapper.refreshTotalBySaleOrderId(saleOrderId);
        }
    }
}
