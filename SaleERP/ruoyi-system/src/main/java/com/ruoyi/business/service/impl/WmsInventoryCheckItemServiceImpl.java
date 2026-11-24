package com.ruoyi.business.service.impl;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.Objects;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.ruoyi.business.domain.MdLocation;
import com.ruoyi.business.domain.MdProduct;
import com.ruoyi.business.domain.WmsInventoryCheck;
import com.ruoyi.business.domain.WmsInventoryCheckItem;
import com.ruoyi.business.mapper.MdLocationMapper;
import com.ruoyi.business.mapper.MdProductMapper;
import com.ruoyi.business.mapper.WmsInventoryCheckItemMapper;
import com.ruoyi.business.mapper.WmsInventoryCheckMapper;
import com.ruoyi.business.service.IWmsInventoryCheckItemService;
import com.ruoyi.business.service.support.BizComplianceHelper;
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

    @Autowired(required = false)
    private MdProductMapper mdProductMapper;

    @Autowired(required = false)
    private MdLocationMapper mdLocationMapper;

    @Autowired(required = false)
    private BizComplianceHelper complianceHelper;

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
        WmsInventoryCheck databaseCheck = validateDraftCheck(wmsInventoryCheckItem.getCheckId());
        prepareInventoryCheckItemForSave(databaseCheck, wmsInventoryCheckItem, null);
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
        WmsInventoryCheck databaseCheck = validateDraftCheck(databaseInventoryCheckItem.getCheckId());
        wmsInventoryCheckItem.setCheckId(databaseInventoryCheckItem.getCheckId());
        prepareInventoryCheckItemForSave(databaseCheck, wmsInventoryCheckItem, databaseInventoryCheckItem);
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
    private WmsInventoryCheck validateDraftCheck(Long checkId)
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
        return databaseInventoryCheck;
    }

    /**
     * 保存前校验盘点明细字段并重算差异数据
     *
     * @param databaseCheck 所属盘点单主表
     * @param wmsInventoryCheckItem 盘点明细
     * @param existingItem 原有盘点明细（新增时为null）
     */
    private void prepareInventoryCheckItemForSave(WmsInventoryCheck databaseCheck,
        WmsInventoryCheckItem wmsInventoryCheckItem, WmsInventoryCheckItem existingItem)
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
        if (complianceHelper != null)
        {
            complianceHelper.assertEntityEnabled("md_product", "product_id", wmsInventoryCheckItem.getProductId(), "商品");
            complianceHelper.assertEntityEnabled("md_location", "location_id", wmsInventoryCheckItem.getLocationId(), "库位");
        }
        if (mdLocationMapper != null && databaseCheck != null && databaseCheck.getWarehouseId() != null)
        {
            MdLocation loc = mdLocationMapper.selectMdLocationById(wmsInventoryCheckItem.getLocationId());
            if (loc != null && loc.getWarehouseId() != null
                && !Objects.equals(loc.getWarehouseId(), databaseCheck.getWarehouseId()))
            {
                throw new ServiceException("盘点明细所选库位不属于当前盘点仓库");
            }
        }
        BigDecimal stockQuantity = wmsInventoryCheckItem.getStockQty() == null ? BigDecimal.ZERO : wmsInventoryCheckItem.getStockQty();
        BigDecimal actualQuantity = wmsInventoryCheckItem.getActualQty();
        BigDecimal priceValue = wmsInventoryCheckItem.getPrice();
        // 当无毛利/成本查看权限的前端用户提交脱敏后的 null 或 0 单价时，服务端自动保留原成本单价或回填商品成本价
        if (priceValue == null
            || (priceValue.compareTo(BigDecimal.ZERO) == 0 && existingItem != null
                && existingItem.getPrice() != null && existingItem.getPrice().compareTo(BigDecimal.ZERO) > 0))
        {
            if (existingItem != null && existingItem.getPrice() != null)
            {
                priceValue = existingItem.getPrice();
            }
        }
        if ((priceValue == null || priceValue.compareTo(BigDecimal.ZERO) == 0) && mdProductMapper != null)
        {
            MdProduct product = mdProductMapper.selectMdProductById(wmsInventoryCheckItem.getProductId());
            if (product != null && product.getCostPrice() != null && product.getCostPrice().compareTo(BigDecimal.ZERO) > 0)
            {
                priceValue = product.getCostPrice();
            }
        }
        if (priceValue == null)
        {
            priceValue = BigDecimal.ZERO;
        }
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
        wmsInventoryCheckItem.setPrice(priceValue);
        wmsInventoryCheckItem.setDiffQty(diffQuantity);
        wmsInventoryCheckItem.setDiffAmount(diffQuantity.multiply(priceValue).setScale(2, RoundingMode.HALF_UP));
    }
}
