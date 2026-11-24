package com.ruoyi.business.service.impl;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.ruoyi.business.domain.MdProduct;
import com.ruoyi.business.domain.WmsInventoryCheck;
import com.ruoyi.business.domain.WmsInventoryCheckItem;
import com.ruoyi.business.domain.WmsStock;
import com.ruoyi.business.domain.WmsStockLog;
import com.ruoyi.business.mapper.MdProductMapper;
import com.ruoyi.business.mapper.WmsInventoryCheckItemMapper;
import com.ruoyi.business.mapper.WmsInventoryCheckMapper;
import com.ruoyi.business.mapper.WmsStockLogMapper;
import com.ruoyi.business.mapper.WmsStockMapper;
import com.ruoyi.business.service.IWmsInventoryCheckService;
import com.ruoyi.business.service.IWmsStockService;
import com.ruoyi.business.service.support.BizComplianceHelper;
import com.ruoyi.common.annotation.DataScope;
import com.ruoyi.common.exception.ServiceException;
import com.ruoyi.common.utils.SecurityUtils;
import com.ruoyi.common.utils.StringUtils;

@Service
public class WmsInventoryCheckServiceImpl implements IWmsInventoryCheckService
{
    private static final String STATUS_DRAFT = "draft";

    private static final String STATUS_SUBMITTED = "submitted";

    private static final String STATUS_AUDITED = "audited";

    private static final String STATUS_CANCELLED = "cancelled";

    @Autowired
    private WmsInventoryCheckMapper wmsInventoryCheckMapper;

    @Autowired
    private WmsInventoryCheckItemMapper wmsInventoryCheckItemMapper;

    @Autowired
    private WmsStockMapper wmsStockMapper;

    @Autowired
    private WmsStockLogMapper wmsStockLogMapper;

    @Autowired(required = false)
    private IWmsStockService wmsStockService;

    @Autowired(required = false)
    private MdProductMapper mdProductMapper;

    @Autowired(required = false)
    private BizComplianceHelper complianceHelper;

    @Override
    public WmsInventoryCheck selectWmsInventoryCheckById(Long checkId)
    {
        return wmsInventoryCheckMapper.selectWmsInventoryCheckById(checkId);
    }

    @Override
    @DataScope(deptAlias = "su", userAlias = "su", permission = "business:inventoryCheck:list")
    public List<WmsInventoryCheck> selectWmsInventoryCheckList(WmsInventoryCheck wmsInventoryCheck)
    {
        return wmsInventoryCheckMapper.selectWmsInventoryCheckList(wmsInventoryCheck);
    }

    /**
     * 新增盘点单
     *
     * @param wmsInventoryCheck 盘点单
     * @return 新增结果
     */
    @Override
    public int insertWmsInventoryCheck(WmsInventoryCheck wmsInventoryCheck)
    {
        validateInventoryCheckBeforeSave(wmsInventoryCheck);
        if (StringUtils.isEmpty(wmsInventoryCheck.getCheckNo()))
        {
            wmsInventoryCheck.setCheckNo(generateCheckNo());
        }
        wmsInventoryCheck.setStatus(STATUS_DRAFT);
        if (wmsInventoryCheck.getTotalDiffQty() == null)
        {
            wmsInventoryCheck.setTotalDiffQty(BigDecimal.ZERO);
        }
        if (wmsInventoryCheck.getTotalDiffAmount() == null)
        {
            wmsInventoryCheck.setTotalDiffAmount(BigDecimal.ZERO);
        }
        return wmsInventoryCheckMapper.insertWmsInventoryCheck(wmsInventoryCheck);
    }

    /**
     * 修改盘点单
     *
     * @param wmsInventoryCheck 盘点单
     * @return 修改结果
     */
    @Override
    public int updateWmsInventoryCheck(WmsInventoryCheck wmsInventoryCheck)
    {
        WmsInventoryCheck databaseInventoryCheck = wmsInventoryCheckMapper.selectWmsInventoryCheckById(wmsInventoryCheck.getCheckId());
        if (databaseInventoryCheck == null)
        {
            throw new ServiceException("盘点单不存在");
        }
        if (!STATUS_DRAFT.equals(databaseInventoryCheck.getStatus()))
        {
            throw new ServiceException("仅草稿状态盘点单允许修改");
        }
        validateInventoryCheckBeforeSave(wmsInventoryCheck);
        wmsInventoryCheck.setCheckNo(databaseInventoryCheck.getCheckNo());
        wmsInventoryCheck.setStatus(databaseInventoryCheck.getStatus());
        wmsInventoryCheck.setTotalDiffQty(databaseInventoryCheck.getTotalDiffQty());
        wmsInventoryCheck.setTotalDiffAmount(databaseInventoryCheck.getTotalDiffAmount());
        wmsInventoryCheck.setAuditBy(databaseInventoryCheck.getAuditBy());
        wmsInventoryCheck.setAuditTime(databaseInventoryCheck.getAuditTime());
        return wmsInventoryCheckMapper.updateWmsInventoryCheck(wmsInventoryCheck);
    }

    /**
     * 提交盘点单
     *
     * @param checkId 盘点单编号
     * @return 提交结果
     */
    @Override
    public int submitWmsInventoryCheck(Long checkId)
    {
        WmsInventoryCheck databaseInventoryCheck = wmsInventoryCheckMapper.selectWmsInventoryCheckById(checkId);
        if (databaseInventoryCheck == null)
        {
            throw new ServiceException("盘点单不存在");
        }
        if (!STATUS_DRAFT.equals(databaseInventoryCheck.getStatus()))
        {
            throw new ServiceException("仅草稿状态盘点单允许提交");
        }
        validateInventoryCheckBeforeSave(databaseInventoryCheck);
        List<WmsInventoryCheckItem> checkItemList = wmsInventoryCheckItemMapper.selectWmsInventoryCheckItemsByCheckId(checkId);
        validateCheckItemList(checkItemList);
        return wmsInventoryCheckMapper.updateWmsInventoryCheckStatus(checkId, STATUS_SUBMITTED, null);
    }

    /**
     * 审核盘点单
     *
     * @param checkId 盘点单编号
     * @return 审核结果
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public int auditWmsInventoryCheck(Long checkId)
    {
        WmsInventoryCheck databaseInventoryCheck = wmsInventoryCheckMapper.selectWmsInventoryCheckById(checkId);
        if (databaseInventoryCheck == null)
        {
            throw new ServiceException("盘点单不存在");
        }
        if (!STATUS_SUBMITTED.equals(databaseInventoryCheck.getStatus()))
        {
            throw new ServiceException("仅已提交盘点单允许审核");
        }
        if (databaseInventoryCheck.getWarehouseId() == null)
        {
            throw new ServiceException("盘点单仓库不能为空");
        }
        if (complianceHelper != null)
        {
            complianceHelper.assertPeriodOpen(new Date(), "审核盘点单");
            complianceHelper.assertNotSelfAudit(databaseInventoryCheck.getCreateBy(), "盘点单审核");
            complianceHelper.assertEntityEnabled("md_warehouse", "warehouse_id", databaseInventoryCheck.getWarehouseId(), "仓库");
        }
        List<WmsInventoryCheckItem> checkItemList = wmsInventoryCheckItemMapper.selectWmsInventoryCheckItemsByCheckId(checkId);
        validateCheckItemList(checkItemList);
        String auditUsername = SecurityUtils.getUsername();
        int casUpdated = wmsInventoryCheckMapper.auditWmsInventoryCheckStatusCas(checkId, auditUsername);
        if (casUpdated == 0)
        {
            throw new ServiceException("盘点单状态已变更或正在被并发审核，请刷新后重试");
        }
        BigDecimal totalDiffQuantity = BigDecimal.ZERO;
        BigDecimal totalDiffAmount = BigDecimal.ZERO;
        for (WmsInventoryCheckItem inventoryCheckItem : checkItemList)
        {
            validateCheckItem(inventoryCheckItem);
            if (complianceHelper != null)
            {
                complianceHelper.assertEntityEnabled("md_product", "product_id", inventoryCheckItem.getProductId(), "商品");
                if (inventoryCheckItem.getLocationId() != null)
                {
                    complianceHelper.assertEntityEnabled("md_location", "location_id", inventoryCheckItem.getLocationId(), "库位");
                }
            }
            // 审核瞬时实时重读当前在库账面库存，避免盘点期间正常出入库导致盈亏错算与双倍扣减
            WmsStock liveKey = new WmsStock();
            liveKey.setWarehouseId(databaseInventoryCheck.getWarehouseId());
            liveKey.setProductId(inventoryCheckItem.getProductId());
            liveKey.setLocationId(inventoryCheckItem.getLocationId());
            liveKey.setBatchNo(inventoryCheckItem.getBatchNo());
            WmsStock liveStock = wmsStockMapper.selectWmsStockByKey(liveKey);
            BigDecimal stockQuantity = liveStock != null && liveStock.getQuantity() != null ? liveStock.getQuantity() : BigDecimal.ZERO;
            BigDecimal actualQuantity = inventoryCheckItem.getActualQty() == null ? BigDecimal.ZERO : inventoryCheckItem.getActualQty();
            BigDecimal diffQuantity = actualQuantity.subtract(stockQuantity);
            BigDecimal price = inventoryCheckItem.getPrice() == null ? BigDecimal.ZERO : inventoryCheckItem.getPrice();
            BigDecimal diffAmount = diffQuantity.multiply(price);
            inventoryCheckItem.setStockQty(stockQuantity);
            inventoryCheckItem.setDiffQty(diffQuantity);
            inventoryCheckItem.setDiffAmount(diffAmount);
            wmsInventoryCheckItemMapper.updateWmsInventoryCheckItem(inventoryCheckItem);
            totalDiffQuantity = totalDiffQuantity.add(diffQuantity);
            totalDiffAmount = totalDiffAmount.add(diffAmount);
            if (diffQuantity.compareTo(BigDecimal.ZERO) == 0)
            {
                continue;
            }
            BigDecimal[] qtyPair;
            String inOutDirection;
            BigDecimal logQuantity;
            if (diffQuantity.compareTo(BigDecimal.ZERO) > 0)
            {
                inOutDirection = "in";
                logQuantity = diffQuantity;
                if (wmsStockService != null)
                {
                    qtyPair = wmsStockService.increaseStockByKeyAtomically(
                        databaseInventoryCheck.getWarehouseId(),
                        inventoryCheckItem.getProductId(),
                        inventoryCheckItem.getLocationId(),
                        inventoryCheckItem.getBatchNo(),
                        null,
                        logQuantity,
                        auditUsername
                    );
                }
                else
                {
                    WmsStock databaseStock = getOrCreateStock(databaseInventoryCheck.getWarehouseId(), inventoryCheckItem, diffQuantity);
                    BigDecimal beforeQty = databaseStock.getQuantity() == null ? BigDecimal.ZERO : databaseStock.getQuantity();
                    BigDecimal afterQty = beforeQty.add(logQuantity);
                    databaseStock.setQuantity(afterQty);
                    wmsStockMapper.updateWmsStock(databaseStock);
                    qtyPair = new BigDecimal[] { beforeQty, afterQty };
                }
            }
            else
            {
                inOutDirection = "out";
                logQuantity = diffQuantity.abs();
                if (wmsStockService != null)
                {
                    qtyPair = wmsStockService.deductAvailableStockByKeyAtomically(
                        databaseInventoryCheck.getWarehouseId(),
                        inventoryCheckItem.getProductId(),
                        inventoryCheckItem.getLocationId(),
                        inventoryCheckItem.getBatchNo(),
                        logQuantity,
                        auditUsername,
                        false
                    );
                }
                else
                {
                    WmsStock databaseStock = getOrCreateStock(databaseInventoryCheck.getWarehouseId(), inventoryCheckItem, diffQuantity);
                    BigDecimal beforeQty = databaseStock.getQuantity() == null ? BigDecimal.ZERO : databaseStock.getQuantity();
                    if (beforeQty.compareTo(logQuantity) < 0)
                    {
                        throw new ServiceException("库存不足，商品编号：" + inventoryCheckItem.getProductId());
                    }
                    BigDecimal afterQty = beforeQty.subtract(logQuantity);
                    databaseStock.setQuantity(afterQty);
                    wmsStockMapper.updateWmsStock(databaseStock);
                    qtyPair = new BigDecimal[] { beforeQty, afterQty };
                }
            }
            WmsStockLog stockLog = new WmsStockLog();
            stockLog.setWarehouseId(databaseInventoryCheck.getWarehouseId());
            stockLog.setProductId(inventoryCheckItem.getProductId());
            stockLog.setLocationId(inventoryCheckItem.getLocationId());
            stockLog.setBatchNo(inventoryCheckItem.getBatchNo());
            stockLog.setBillType("inventory_check");
            stockLog.setBillId(checkId);
            stockLog.setBillNo(databaseInventoryCheck.getCheckNo());
            stockLog.setInOut(inOutDirection);
            stockLog.setQuantity(logQuantity);
            stockLog.setPrice(price);
            stockLog.setAmount(logQuantity.multiply(price));
            stockLog.setBeforeQty(qtyPair[0]);
            stockLog.setAfterQty(qtyPair[1]);
            wmsStockLogMapper.insertWmsStockLog(stockLog);
        }
        WmsInventoryCheck updateInventoryCheck = new WmsInventoryCheck();
        updateInventoryCheck.setCheckId(checkId);
        updateInventoryCheck.setTotalDiffQty(totalDiffQuantity);
        updateInventoryCheck.setTotalDiffAmount(totalDiffAmount);
        updateInventoryCheck.setUpdateBy(auditUsername);
        wmsInventoryCheckMapper.updateWmsInventoryCheck(updateInventoryCheck);
        return casUpdated;
    }

    @Override
    public int rejectWmsInventoryCheck(Long checkId, String rejectReason)
    {
        WmsInventoryCheck databaseInventoryCheck = wmsInventoryCheckMapper.selectWmsInventoryCheckById(checkId);
        if (databaseInventoryCheck == null)
        {
            throw new ServiceException("盘点单不存在");
        }
        if (!STATUS_SUBMITTED.equals(databaseInventoryCheck.getStatus()))
        {
            throw new ServiceException("仅已提交待审状态的盘点单允许驳回");
        }
        String reason = StringUtils.isEmpty(rejectReason) ? "审核驳回重盘" : rejectReason.trim();
        int rows = wmsInventoryCheckMapper.rejectWmsInventoryCheckStatus(checkId, reason, SecurityUtils.getUsername());
        if (rows == 0)
        {
            throw new ServiceException("盘点单状态已变更，请刷新后重试");
        }
        return rows;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int loadWarehouseStockSnapshot(Long checkId, Long locationId, Boolean blindMode)
    {
        WmsInventoryCheck databaseCheck = wmsInventoryCheckMapper.selectWmsInventoryCheckById(checkId);
        if (databaseCheck == null)
        {
            throw new ServiceException("盘点单不存在");
        }
        if (!STATUS_DRAFT.equals(databaseCheck.getStatus()))
        {
            throw new ServiceException("仅草稿状态盘点单允许载入库存快照");
        }
        WmsStock stockQuery = new WmsStock();
        stockQuery.setWarehouseId(databaseCheck.getWarehouseId());
        if (locationId != null)
        {
            stockQuery.setLocationId(locationId);
        }
        List<WmsStock> stockList = wmsStockMapper.selectWmsStockList(stockQuery);
        if (stockList == null || stockList.isEmpty())
        {
            return 0;
        }
        List<WmsInventoryCheckItem> existingItems = wmsInventoryCheckItemMapper.selectWmsInventoryCheckItemsByCheckId(checkId);
        Set<String> existingKeys = new HashSet<>();
        if (existingItems != null)
        {
            for (WmsInventoryCheckItem existing : existingItems)
            {
                existingKeys.add(existing.getProductId() + "_" + Objects.toString(existing.getLocationId(), "0") + "_" + Objects.toString(existing.getBatchNo(), ""));
            }
        }
        boolean isBlind = Boolean.TRUE.equals(blindMode);
        List<WmsInventoryCheckItem> batchInsertList = new java.util.ArrayList<>();
        for (WmsStock stock : stockList)
        {
            BigDecimal qty = stock.getQuantity() == null ? BigDecimal.ZERO : stock.getQuantity();
            if (qty.compareTo(BigDecimal.ZERO) <= 0)
            {
                continue;
            }
            String origBatchNo = StringUtils.isEmpty(stock.getBatchNo()) ? null : stock.getBatchNo().trim();
            String key = stock.getProductId() + "_" + Objects.toString(stock.getLocationId(), "0") + "_" + Objects.toString(origBatchNo, "");
            if (existingKeys.contains(key))
            {
                continue;
            }
            BigDecimal unitCost = stock.getOpeningCostPrice();
            if (unitCost == null && mdProductMapper != null && stock.getProductId() != null)
            {
                MdProduct p = mdProductMapper.selectMdProductById(stock.getProductId());
                if (p != null && p.getCostPrice() != null)
                {
                    unitCost = p.getCostPrice();
                }
            }
            if (unitCost == null)
            {
                unitCost = BigDecimal.ZERO;
            }
            WmsInventoryCheckItem item = new WmsInventoryCheckItem();
            item.setCheckId(checkId);
            item.setProductId(stock.getProductId());
            item.setLocationId(stock.getLocationId());
            item.setBatchNo(origBatchNo);
            item.setStockQty(qty);
            BigDecimal initActual = isBlind ? BigDecimal.ZERO : qty;
            BigDecimal initDiff = initActual.subtract(qty);
            item.setActualQty(initActual);
            item.setDiffQty(initDiff);
            item.setPrice(unitCost);
            item.setDiffAmount(initDiff.multiply(unitCost).setScale(2, java.math.RoundingMode.HALF_UP));
            batchInsertList.add(item);
            existingKeys.add(key);
        }
        if (!batchInsertList.isEmpty())
        {
            wmsInventoryCheckItemMapper.insertWmsInventoryCheckItemBatch(batchInsertList);
        }
        return batchInsertList.size();
    }

    /**
     * 作废盘点单
     *
     * @param checkId 盘点单编号
     * @return 作废结果
     */
    @Override
    public int cancelWmsInventoryCheck(Long checkId)
    {
        WmsInventoryCheck databaseInventoryCheck = wmsInventoryCheckMapper.selectWmsInventoryCheckById(checkId);
        if (databaseInventoryCheck == null)
        {
            throw new ServiceException("盘点单不存在");
        }
        if (STATUS_CANCELLED.equals(databaseInventoryCheck.getStatus()))
        {
            throw new ServiceException("盘点单已作废，无需重复作废");
        }
        if (STATUS_AUDITED.equals(databaseInventoryCheck.getStatus()))
        {
            throw new ServiceException("已审核盘点单不允许作废");
        }
        return wmsInventoryCheckMapper.updateWmsInventoryCheckStatus(checkId, STATUS_CANCELLED, null);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int deleteWmsInventoryCheckById(Long checkId)
    {
        WmsInventoryCheck databaseInventoryCheck = wmsInventoryCheckMapper.selectWmsInventoryCheckById(checkId);
        if (databaseInventoryCheck == null)
        {
            return 0;
        }
        if (!STATUS_DRAFT.equals(databaseInventoryCheck.getStatus()))
        {
            throw new ServiceException("仅草稿状态盘点单允许删除");
        }
        wmsInventoryCheckItemMapper.deleteWmsInventoryCheckItemByCheckId(checkId);
        return wmsInventoryCheckMapper.deleteWmsInventoryCheckById(checkId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int deleteWmsInventoryCheckByIds(Long[] checkIds)
    {
        for (Long checkId : checkIds)
        {
            WmsInventoryCheck databaseInventoryCheck = wmsInventoryCheckMapper.selectWmsInventoryCheckById(checkId);
            if (databaseInventoryCheck == null)
            {
                continue;
            }
            if (!STATUS_DRAFT.equals(databaseInventoryCheck.getStatus()))
            {
                throw new ServiceException("仅草稿状态盘点单允许删除，盘点单编号：" + databaseInventoryCheck.getCheckNo());
            }
        }
        wmsInventoryCheckItemMapper.deleteWmsInventoryCheckItemByCheckIds(checkIds);
        return wmsInventoryCheckMapper.deleteWmsInventoryCheckByIds(checkIds);
    }

    /**
     * 保存前校验盘点单主表字段
     *
     * @param wmsInventoryCheck 盘点单
     */
    private void validateInventoryCheckBeforeSave(WmsInventoryCheck wmsInventoryCheck)
    {
        if (wmsInventoryCheck == null)
        {
            throw new ServiceException("盘点单不能为空");
        }
        if (wmsInventoryCheck.getWarehouseId() == null)
        {
            throw new ServiceException("盘点单仓库不能为空");
        }
    }

    /**
     * 校验盘点明细列表是否完整
     *
     * @param checkItemList 盘点明细列表
     */
    private void validateCheckItemList(List<WmsInventoryCheckItem> checkItemList)
    {
        if (checkItemList == null || checkItemList.isEmpty())
        {
            throw new ServiceException("盘点明细不能为空");
        }
        for (WmsInventoryCheckItem inventoryCheckItem : checkItemList)
        {
            validateCheckItem(inventoryCheckItem);
            if (complianceHelper != null)
            {
                complianceHelper.assertEntityEnabled("md_product", "product_id", inventoryCheckItem.getProductId(), "商品");
                if (inventoryCheckItem.getLocationId() != null)
                {
                    complianceHelper.assertEntityEnabled("md_location", "location_id", inventoryCheckItem.getLocationId(), "库位");
                }
            }
        }
    }

    /**
     * 校验盘点明细字段是否完整
     *
     * @param inventoryCheckItem 盘点明细
     */
    private void validateCheckItem(WmsInventoryCheckItem inventoryCheckItem)
    {
        if (inventoryCheckItem.getProductId() == null)
        {
            throw new ServiceException("盘点明细商品不能为空");
        }
        if (inventoryCheckItem.getLocationId() == null)
        {
            throw new ServiceException("盘点明细库位不能为空");
        }
        if (StringUtils.isEmpty(inventoryCheckItem.getBatchNo()))
        {
            throw new ServiceException("盘点明细批次号不能为空");
        }
        if (inventoryCheckItem.getActualQty() == null)
        {
            throw new ServiceException("盘点明细实盘数量不能为空");
        }
        BigDecimal stockQuantity = inventoryCheckItem.getStockQty() == null ? BigDecimal.ZERO : inventoryCheckItem.getStockQty();
        BigDecimal actualQuantity = inventoryCheckItem.getActualQty();
        BigDecimal priceValue = inventoryCheckItem.getPrice() == null ? BigDecimal.ZERO : inventoryCheckItem.getPrice();
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
    }

    /**
     * 根据盘点明细获取或创建库存记录
     *
     * @param warehouseId 仓库编号
     * @param inventoryCheckItem 盘点明细
     * @param diffQuantity 差异数量
     * @return 库存记录
     */
    private WmsStock getOrCreateStock(Long warehouseId, WmsInventoryCheckItem inventoryCheckItem, BigDecimal diffQuantity)
    {
        WmsStock stockQuery = new WmsStock();
        stockQuery.setWarehouseId(warehouseId);
        stockQuery.setProductId(inventoryCheckItem.getProductId());
        stockQuery.setLocationId(inventoryCheckItem.getLocationId());
        stockQuery.setBatchNo(inventoryCheckItem.getBatchNo());
        WmsStock databaseStock = wmsStockMapper.selectWmsStockByKey(stockQuery);
        if (databaseStock != null)
        {
            return databaseStock;
        }
        if (diffQuantity.compareTo(BigDecimal.ZERO) < 0)
        {
            throw new ServiceException("库存不存在，商品编号：" + inventoryCheckItem.getProductId());
        }
        WmsStock newStock = new WmsStock();
        newStock.setWarehouseId(warehouseId);
        newStock.setProductId(inventoryCheckItem.getProductId());
        newStock.setLocationId(inventoryCheckItem.getLocationId());
        newStock.setBatchNo(inventoryCheckItem.getBatchNo());
        newStock.setQuantity(BigDecimal.ZERO);
        newStock.setLockedQuantity(BigDecimal.ZERO);
        newStock.setFrozenQuantity(BigDecimal.ZERO);
        newStock.setVersion(0);
        wmsStockMapper.insertWmsStock(newStock);
        return newStock;
    }

    /**
     * 生成盘点单号
     *
     * @return 盘点单号
     */
    private String generateCheckNo()
    {
        String dateValue = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        String noPrefix = "pd" + dateValue;
        String maxCheckNo = wmsInventoryCheckMapper.selectMaxCheckNoByPrefix(noPrefix);
        if (StringUtils.isEmpty(maxCheckNo))
        {
            return noPrefix + "0001";
        }
        String sequenceValue = maxCheckNo.substring(noPrefix.length());
        int nextSequenceValue = Integer.parseInt(sequenceValue) + 1;
        return noPrefix + String.format("%04d", nextSequenceValue);
    }
}
