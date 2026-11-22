package com.ruoyi.business.service.impl;

import java.math.BigDecimal;
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
import com.ruoyi.common.utils.StringUtils;

/**
 * 盘点明细Service业务层处理
 *
 * @author ruoyi
 * @date 2026-04-17
 */
@Service
public class WmsInventoryCheckItemServiceImpl implements IWmsInventoryCheckItemService
{
    private static final String STATUS_DRAFT = "draft";

    @Autowired
    private WmsInventoryCheckItemMapper wmsInventoryCheckItemMapper;

    @Autowired
    private WmsInventoryCheckMapper wmsInventoryCheckMapper;

    /**
     * 查询盘点明细
     *
     * @param checkItemId 盘点明细主键
     * @return 盘点明细
     */
    @Override
    public WmsInventoryCheckItem selectWmsInventoryCheckItemById(Long checkItemId)
    {
        return wmsInventoryCheckItemMapper.selectWmsInventoryCheckItemById(checkItemId);
    }

    /**
     * 查询盘点明细列表
     *
     * @param wmsInventoryCheckItem 盘点明细
     * @return 盘点明细集合
     */
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
        prepareInventoryCheckItemForSave(wmsInventoryCheckItem);
        int rows = wmsInventoryCheckItemMapper.insertWmsInventoryCheckItem(wmsInventoryCheckItem);
        refreshCheckHeaderTotals(wmsInventoryCheckItem.getCheckId());
        return rows;
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
        prepareInventoryCheckItemForSave(wmsInventoryCheckItem);
        int rows = wmsInventoryCheckItemMapper.updateWmsInventoryCheckItem(wmsInventoryCheckItem);
        refreshCheckHeaderTotals(databaseInventoryCheckItem.getCheckId());
        return rows;
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
        int rows = wmsInventoryCheckItemMapper.deleteWmsInventoryCheckItemById(checkItemId);
        refreshCheckHeaderTotals(databaseInventoryCheckItem.getCheckId());
        return rows;
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
        java.util.Set<Long> touchedCheckIds = new java.util.HashSet<>();
        for (Long checkItemId : checkItemIds)
        {
            WmsInventoryCheckItem databaseInventoryCheckItem = wmsInventoryCheckItemMapper.selectWmsInventoryCheckItemById(checkItemId);
            if (databaseInventoryCheckItem == null)
            {
                continue;
            }
            validateDraftCheck(databaseInventoryCheckItem.getCheckId());
            touchedCheckIds.add(databaseInventoryCheckItem.getCheckId());
        }
        int rows = wmsInventoryCheckItemMapper.deleteWmsInventoryCheckItemByIds(checkItemIds);
        for (Long checkId : touchedCheckIds)
        {
            refreshCheckHeaderTotals(checkId);
        }
        return rows;
    }

    private void refreshCheckHeaderTotals(Long checkId)
    {
        if (checkId == null)
        {
            return;
        }
        List<WmsInventoryCheckItem> items = wmsInventoryCheckItemMapper.selectWmsInventoryCheckItemsByCheckId(checkId);
        BigDecimal totalDiffQty = BigDecimal.ZERO;
        BigDecimal totalDiffAmount = BigDecimal.ZERO;
        if (items != null)
        {
            for (WmsInventoryCheckItem it : items)
            {
                if (it.getDiffQty() != null)
                {
                    totalDiffQty = totalDiffQty.add(it.getDiffQty());
                }
                if (it.getDiffAmount() != null)
                {
                    totalDiffAmount = totalDiffAmount.add(it.getDiffAmount());
                }
            }
        }
        WmsInventoryCheck header = new WmsInventoryCheck();
        header.setCheckId(checkId);
        header.setTotalDiffQty(totalDiffQty);
        header.setTotalDiffAmount(totalDiffAmount);
        wmsInventoryCheckMapper.updateWmsInventoryCheck(header);
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

    /**
     * 保存前校验盘点明细字段并重算差异数据
     *
     * @param wmsInventoryCheckItem 盘点明细
     */
    private void prepareInventoryCheckItemForSave(WmsInventoryCheckItem wmsInventoryCheckItem)
    {
        if (wmsInventoryCheckItem == null)
        {
            throw new ServiceException("盘点明细不能为空");
        }
        if (wmsInventoryCheckItem.getProductId() == null)
        {
            throw new ServiceException("盘点明细商品不能为空");
        }
        if (wmsInventoryCheckItem.getLocationId() == null)
        {
            throw new ServiceException("盘点明细库位不能为空");
        }
        if (StringUtils.isEmpty(wmsInventoryCheckItem.getBatchNo()))
        {
            throw new ServiceException("盘点明细批次号不能为空");
        }
        if (wmsInventoryCheckItem.getActualQty() == null)
        {
            throw new ServiceException("盘点明细实盘数量不能为空");
        }
        BigDecimal stockQuantity = wmsInventoryCheckItem.getStockQty() == null ? BigDecimal.ZERO : wmsInventoryCheckItem.getStockQty();
        BigDecimal actualQuantity = wmsInventoryCheckItem.getActualQty();
        BigDecimal priceValue = wmsInventoryCheckItem.getPrice() == null ? BigDecimal.ZERO : wmsInventoryCheckItem.getPrice();
        if (stockQuantity.compareTo(BigDecimal.ZERO) < 0)
        {
            throw new ServiceException("盘点明细账面数量不能小于0");
        }
        if (actualQuantity.compareTo(BigDecimal.ZERO) < 0)
        {
            throw new ServiceException("盘点明细实盘数量不能小于0");
        }
        if (priceValue.compareTo(BigDecimal.ZERO) < 0)
        {
            throw new ServiceException("盘点明细单价不能小于0");
        }
        BigDecimal diffQuantity = actualQuantity.subtract(stockQuantity);
        wmsInventoryCheckItem.setStockQty(stockQuantity);
        wmsInventoryCheckItem.setDiffQty(diffQuantity);
        wmsInventoryCheckItem.setDiffAmount(diffQuantity.multiply(priceValue));
    }
}
