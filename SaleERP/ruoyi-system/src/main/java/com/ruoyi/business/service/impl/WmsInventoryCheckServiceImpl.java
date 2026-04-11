package com.ruoyi.business.service.impl;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.ruoyi.business.domain.WmsInventoryCheck;
import com.ruoyi.business.domain.WmsInventoryCheckItem;
import com.ruoyi.business.domain.WmsStock;
import com.ruoyi.business.domain.WmsStockLog;
import com.ruoyi.business.mapper.WmsInventoryCheckItemMapper;
import com.ruoyi.business.mapper.WmsInventoryCheckMapper;
import com.ruoyi.business.mapper.WmsStockLogMapper;
import com.ruoyi.business.mapper.WmsStockMapper;
import com.ruoyi.business.service.IWmsInventoryCheckService;
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

    @Override
    public WmsInventoryCheck selectWmsInventoryCheckById(Long checkId)
    {
        return wmsInventoryCheckMapper.selectWmsInventoryCheckById(checkId);
    }

    @Override
    public List<WmsInventoryCheck> selectWmsInventoryCheckList(WmsInventoryCheck wmsInventoryCheck)
    {
        return wmsInventoryCheckMapper.selectWmsInventoryCheckList(wmsInventoryCheck);
    }

    @Override
    public int insertWmsInventoryCheck(WmsInventoryCheck wmsInventoryCheck)
    {
        if (StringUtils.isEmpty(wmsInventoryCheck.getCheckNo()))
        {
            wmsInventoryCheck.setCheckNo(generateCheckNo());
        }
        if (StringUtils.isEmpty(wmsInventoryCheck.getStatus()))
        {
            wmsInventoryCheck.setStatus(STATUS_DRAFT);
        }
        return wmsInventoryCheckMapper.insertWmsInventoryCheck(wmsInventoryCheck);
    }

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
        wmsInventoryCheck.setCheckNo(databaseInventoryCheck.getCheckNo());
        wmsInventoryCheck.setStatus(databaseInventoryCheck.getStatus());
        wmsInventoryCheck.setTotalDiffQty(databaseInventoryCheck.getTotalDiffQty());
        wmsInventoryCheck.setTotalDiffAmount(databaseInventoryCheck.getTotalDiffAmount());
        wmsInventoryCheck.setAuditBy(databaseInventoryCheck.getAuditBy());
        wmsInventoryCheck.setAuditTime(databaseInventoryCheck.getAuditTime());
        return wmsInventoryCheckMapper.updateWmsInventoryCheck(wmsInventoryCheck);
    }

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
        List<WmsInventoryCheckItem> checkItemList = wmsInventoryCheckItemMapper.selectWmsInventoryCheckItemsByCheckId(checkId);
        if (checkItemList == null || checkItemList.isEmpty())
        {
            throw new ServiceException("盘点明细不能为空");
        }
        return wmsInventoryCheckMapper.updateWmsInventoryCheckStatus(checkId, STATUS_SUBMITTED, null);
    }

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
        List<WmsInventoryCheckItem> checkItemList = wmsInventoryCheckItemMapper.selectWmsInventoryCheckItemsByCheckId(checkId);
        if (checkItemList == null || checkItemList.isEmpty())
        {
            throw new ServiceException("盘点明细不能为空");
        }
        BigDecimal totalDiffQuantity = BigDecimal.ZERO;
        BigDecimal totalDiffAmount = BigDecimal.ZERO;
        for (WmsInventoryCheckItem inventoryCheckItem : checkItemList)
        {
            validateCheckItem(inventoryCheckItem);
            BigDecimal stockQuantity = inventoryCheckItem.getStockQty() == null ? BigDecimal.ZERO : inventoryCheckItem.getStockQty();
            BigDecimal actualQuantity = inventoryCheckItem.getActualQty() == null ? BigDecimal.ZERO : inventoryCheckItem.getActualQty();
            BigDecimal diffQuantity = actualQuantity.subtract(stockQuantity);
            BigDecimal price = inventoryCheckItem.getPrice() == null ? BigDecimal.ZERO : inventoryCheckItem.getPrice();
            BigDecimal diffAmount = diffQuantity.multiply(price);
            inventoryCheckItem.setDiffQty(diffQuantity);
            inventoryCheckItem.setDiffAmount(diffAmount);
            wmsInventoryCheckItemMapper.updateWmsInventoryCheckItem(inventoryCheckItem);
            totalDiffQuantity = totalDiffQuantity.add(diffQuantity);
            totalDiffAmount = totalDiffAmount.add(diffAmount);
            if (diffQuantity.compareTo(BigDecimal.ZERO) == 0)
            {
                continue;
            }
            WmsStock databaseStock = getOrCreateStock(databaseInventoryCheck.getWarehouseId(), inventoryCheckItem, diffQuantity);
            BigDecimal beforeQuantity = databaseStock.getQuantity() == null ? BigDecimal.ZERO : databaseStock.getQuantity();
            BigDecimal afterQuantity;
            String inOutDirection;
            BigDecimal logQuantity;
            if (diffQuantity.compareTo(BigDecimal.ZERO) > 0)
            {
                afterQuantity = beforeQuantity.add(diffQuantity);
                inOutDirection = "in";
                logQuantity = diffQuantity;
            }
            else
            {
                BigDecimal outboundQuantity = diffQuantity.abs();
                if (beforeQuantity.compareTo(outboundQuantity) < 0)
                {
                    throw new ServiceException("库存不足，商品编号：" + inventoryCheckItem.getProductId());
                }
                afterQuantity = beforeQuantity.subtract(outboundQuantity);
                inOutDirection = "out";
                logQuantity = outboundQuantity;
            }
            databaseStock.setQuantity(afterQuantity);
            wmsStockMapper.updateWmsStock(databaseStock);
            WmsStockLog stockLog = new WmsStockLog();
            stockLog.setWarehouseId(databaseInventoryCheck.getWarehouseId());
            stockLog.setProductId(inventoryCheckItem.getProductId());
            stockLog.setLocationId(inventoryCheckItem.getLocationId());
            stockLog.setBatchNo(inventoryCheckItem.getBatchNo());
            stockLog.setBillType("inventory_check");
            stockLog.setBillId(checkId);
            stockLog.setInOut(inOutDirection);
            stockLog.setQuantity(logQuantity);
            stockLog.setPrice(price);
            stockLog.setAmount(logQuantity.multiply(price));
            stockLog.setBeforeQty(beforeQuantity);
            stockLog.setAfterQty(afterQuantity);
            wmsStockLogMapper.insertWmsStockLog(stockLog);
        }
        WmsInventoryCheck updateInventoryCheck = new WmsInventoryCheck();
        updateInventoryCheck.setCheckId(checkId);
        updateInventoryCheck.setTotalDiffQty(totalDiffQuantity);
        updateInventoryCheck.setTotalDiffAmount(totalDiffAmount);
        updateInventoryCheck.setUpdateBy(SecurityUtils.getUsername());
        wmsInventoryCheckMapper.updateWmsInventoryCheck(updateInventoryCheck);
        String auditUsername = SecurityUtils.getUsername();
        return wmsInventoryCheckMapper.updateWmsInventoryCheckStatus(checkId, STATUS_AUDITED, auditUsername);
    }

    @Override
    public int cancelWmsInventoryCheck(Long checkId)
    {
        WmsInventoryCheck databaseInventoryCheck = wmsInventoryCheckMapper.selectWmsInventoryCheckById(checkId);
        if (databaseInventoryCheck == null)
        {
            throw new ServiceException("盘点单不存在");
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
    }

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
