package com.ruoyi.business.service.impl;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.ruoyi.business.domain.BizMessage;
import com.ruoyi.business.domain.WmsStock;
import com.ruoyi.business.domain.WmsStockLog;
import com.ruoyi.business.mapper.WmsStockLogMapper;
import com.ruoyi.business.mapper.WmsStockMapper;
import com.ruoyi.business.service.IBizMessageService;
import com.ruoyi.business.service.IWmsStockService;
import com.ruoyi.business.service.support.BizComplianceHelper;
import com.ruoyi.common.exception.ServiceException;
import com.ruoyi.common.utils.StringUtils;
import com.ruoyi.system.domain.SysNotice;
import com.ruoyi.system.service.ISysNoticeService;

@Service
public class WmsStockServiceImpl implements IWmsStockService
{
    private static final int MAX_OPTIMISTIC_RETRIES = 3;

    @Autowired
    private WmsStockMapper wmsStockMapper;

    @Autowired(required = false)
    private WmsStockLogMapper wmsStockLogMapper;

    @Autowired
    private ISysNoticeService sysNoticeService;

    @Autowired
    private IBizMessageService bizMessageService;

    @Autowired(required = false)
    private BizComplianceHelper complianceHelper;

    @Autowired(required = false)
    private com.ruoyi.business.mapper.MdProductMapper mdProductMapper;

    @Override
    public WmsStock selectWmsStockById(Long stockId)
    {
        return wmsStockMapper.selectWmsStockById(stockId);
    }

    @Override
    public WmsStock selectWmsStockByKey(WmsStock wmsStock)
    {
        return wmsStockMapper.selectWmsStockByKey(wmsStock);
    }

    @Override
    public List<WmsStock> selectWmsStockWarningList(WmsStock wmsStock)
    {
        return wmsStockMapper.selectWmsStockWarningList(wmsStock);
    }

    @Override
    public int pushWarningReminder(String operatorName)
    {
        Long warningStockCount = wmsStockMapper.selectWmsStockWarningCount(new WmsStock());
        int warningStockCountValue = warningStockCount == null ? 0 : warningStockCount.intValue();
        if (warningStockCountValue <= 0)
        {
            return 0;
        }
        String noticeTitle = "库存预警提醒-" + LocalDate.now();
        String noticeContent = "当前库存预警商品共" + warningStockCountValue + "条，请及时处理。";
        boolean existsBizMessage = bizMessageService.existsBizMessageByTypeAndTitle("stock_warning", noticeTitle);
        if (!existsBizMessage)
        {
            BizMessage bizMessage = new BizMessage();
            bizMessage.setMessageType("stock_warning");
            bizMessage.setMessageTitle(noticeTitle);
            bizMessage.setMessageContent(noticeContent);
            bizMessage.setMessageLevel("warning");
            bizMessage.setBusinessType("stock");
            bizMessage.setStatus("0");
            bizMessage.setCreateBy(operatorName);
            bizMessageService.insertBizMessage(bizMessage);
        }
        boolean existsNotice = sysNoticeService.existsNoticeByTitle(noticeTitle);
        if (existsNotice)
        {
            return warningStockCountValue;
        }
        SysNotice notice = new SysNotice();
        notice.setNoticeTitle(noticeTitle);
        notice.setNoticeType("1");
        notice.setStatus("0");
        notice.setCreateBy(operatorName);
        notice.setNoticeContent(noticeContent);
        sysNoticeService.insertNotice(notice);
        return warningStockCountValue;
    }

    @Override
    public List<WmsStock> selectWmsStockList(WmsStock wmsStock)
    {
        return wmsStockMapper.selectWmsStockList(wmsStock);
    }

    @Override
    public List<WmsStock> recommendFefoBatches(Long warehouseId, Long productId)
    {
        if (warehouseId == null || productId == null)
        {
            return Collections.emptyList();
        }
        return wmsStockMapper.selectFefoCandidateBatches(warehouseId, productId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public String importOpeningStock(List<WmsStock> stockList, String operatorName)
    {
        if (stockList == null || stockList.isEmpty())
        {
            throw new ServiceException("导入期初库存数据不能为空");
        }
        if (complianceHelper != null)
        {
            complianceHelper.assertPeriodOpen(new Date(), "期初库存导入");
        }
        int successCount = 0;
        StringBuilder errorMsg = new StringBuilder();
        for (int i = 0; i < stockList.size(); i++)
        {
            WmsStock row = stockList.get(i);
            int lineNo = i + 2;
            if (row == null || row.getWarehouseId() == null || row.getProductId() == null)
            {
                errorMsg.append("<br/>第 ").append(lineNo).append(" 行：仓库编号与商品编号不能为空");
                continue;
            }
            if (row.getQuantity() == null || row.getQuantity().compareTo(BigDecimal.ZERO) <= 0)
            {
                errorMsg.append("<br/>第 ").append(lineNo).append(" 行：期初库存数量必须大于 0");
                continue;
            }
            if (complianceHelper != null)
            {
                complianceHelper.assertEntityEnabled("md_warehouse", "warehouse_id", row.getWarehouseId(), "仓库");
                complianceHelper.assertEntityEnabled("md_product", "product_id", row.getProductId(), "商品");
                if (row.getLocationId() != null)
                {
                    complianceHelper.assertEntityEnabled("md_location", "location_id", row.getLocationId(), "库位");
                }
            }
            String batchNo = StringUtils.isEmpty(row.getBatchNo()) ? "INIT-" + LocalDate.now() : row.getBatchNo().trim();
            BigDecimal openingCost = row.getOpeningCostPrice();
            if ((openingCost == null || openingCost.compareTo(BigDecimal.ZERO) <= 0) && mdProductMapper != null)
            {
                com.ruoyi.business.domain.MdProduct product = mdProductMapper.selectMdProductById(row.getProductId());
                if (product != null && product.getCostPrice() != null && product.getCostPrice().compareTo(BigDecimal.ZERO) > 0)
                {
                    openingCost = product.getCostPrice();
                }
            }
            if (openingCost == null)
            {
                openingCost = BigDecimal.ZERO;
            }
            else if (row.getOpeningCostPrice() != null && row.getOpeningCostPrice().compareTo(BigDecimal.ZERO) > 0 && mdProductMapper != null)
            {
                com.ruoyi.business.domain.MdProduct existingProd = mdProductMapper.selectMdProductById(row.getProductId());
                BigDecimal oldTotalStock = wmsStockMapper.selectTotalProductStockQuantity(row.getProductId());
                if (oldTotalStock == null || oldTotalStock.compareTo(BigDecimal.ZERO) < 0)
                {
                    oldTotalStock = BigDecimal.ZERO;
                }
                BigDecimal newCost = row.getOpeningCostPrice();
                if (existingProd != null && existingProd.getCostPrice() != null
                    && existingProd.getCostPrice().compareTo(BigDecimal.ZERO) > 0
                    && oldTotalStock.compareTo(BigDecimal.ZERO) > 0)
                {
                    BigDecimal denom = oldTotalStock.add(row.getQuantity());
                    newCost = oldTotalStock.multiply(existingProd.getCostPrice())
                        .add(row.getQuantity().multiply(row.getOpeningCostPrice()))
                        .divide(denom, 4, java.math.RoundingMode.HALF_UP).stripTrailingZeros();
                    if (newCost.scale() < 2)
                    {
                        newCost = newCost.setScale(2, java.math.RoundingMode.HALF_UP);
                    }
                }
                com.ruoyi.business.domain.MdProduct updateCost = new com.ruoyi.business.domain.MdProduct();
                updateCost.setProductId(row.getProductId());
                updateCost.setCostPrice(newCost);
                updateCost.setUpdateBy(operatorName);
                mdProductMapper.updateMdProduct(updateCost);
            }
            BigDecimal[] qtyPair = increaseStockByKeyAtomically(
                row.getWarehouseId(), row.getProductId(), row.getLocationId(), batchNo,
                row.getExpireDate(), row.getQuantity(), operatorName
            );
            if (row.getWarningMinQty() != null || row.getWarningMaxQty() != null)
            {
                WmsStock keyQuery = buildKeyQuery(row.getWarehouseId(), row.getProductId(), row.getLocationId(), batchNo);
                WmsStock saved = wmsStockMapper.selectWmsStockByKey(keyQuery);
                if (saved != null)
                {
                    saved.setWarningMinQty(row.getWarningMinQty());
                    saved.setWarningMaxQty(row.getWarningMaxQty());
                    saved.setUpdateBy(operatorName);
                    wmsStockMapper.updateWmsStockWarningThreshold(saved);
                }
            }
            if (wmsStockLogMapper != null)
            {
                WmsStockLog stockLog = new WmsStockLog();
                stockLog.setWarehouseId(row.getWarehouseId());
                stockLog.setProductId(row.getProductId());
                stockLog.setLocationId(row.getLocationId());
                stockLog.setBatchNo(batchNo);
                stockLog.setBillType("opening_init");
                stockLog.setBillNo("INIT-" + LocalDate.now().toString().replace("-", ""));
                stockLog.setInOut("in");
                stockLog.setQuantity(row.getQuantity());
                stockLog.setPrice(openingCost);
                stockLog.setAmount(row.getQuantity().multiply(openingCost));
                stockLog.setBeforeQty(qtyPair[0]);
                stockLog.setAfterQty(qtyPair[1]);
                wmsStockLogMapper.insertWmsStockLog(stockLog);
            }
            successCount++;
        }
        if (successCount == 0 && errorMsg.length() > 0)
        {
            throw new ServiceException("期初库存导入失败：" + errorMsg);
        }
        return "成功导入 " + successCount + " 条期初库存批次" + (errorMsg.length() > 0 ? "（部分跳过：" + errorMsg + "）" : "");
    }

    @Override
    public BigDecimal[] increaseStockByKeyAtomically(Long warehouseId, Long productId, Long locationId,
                                                     String batchNo, Date expireDate, BigDecimal deltaQty, String operatorName)
    {
        WmsStock keyQuery = buildKeyQuery(warehouseId, productId, locationId, batchNo);
        for (int attempt = 0; attempt < MAX_OPTIMISTIC_RETRIES; attempt++)
        {
            WmsStock current = wmsStockMapper.selectWmsStockByKey(keyQuery);
            if (current == null)
            {
                WmsStock created = new WmsStock();
                created.setWarehouseId(warehouseId);
                created.setProductId(productId);
                created.setLocationId(locationId);
                created.setBatchNo(batchNo);
                created.setExpireDate(expireDate);
                created.setQuantity(deltaQty);
                created.setLockedQuantity(BigDecimal.ZERO);
                created.setFrozenQuantity(BigDecimal.ZERO);
                created.setVersion(1);
                created.setCreateBy(operatorName);
                created.setUpdateBy(operatorName);
                wmsStockMapper.insertWmsStock(created);
                return new BigDecimal[] { BigDecimal.ZERO, deltaQty };
            }
            BigDecimal beforeQty = nvl(current.getQuantity());
            Integer currentVer = current.getVersion() == null ? 0 : current.getVersion();
            int updated = wmsStockMapper.increaseStockAtomically(current.getStockId(), currentVer, deltaQty, expireDate, operatorName);
            if (updated > 0)
            {
                return new BigDecimal[] { beforeQty, beforeQty.add(deltaQty) };
            }
        }
        throw new ServiceException("库存并发更新繁忙，请刷新后重试（商品编号：" + productId + "）");
    }

    @Override
    public BigDecimal[] deductAvailableStockByKeyAtomically(Long warehouseId, Long productId, Long locationId,
                                                            String batchNo, BigDecimal deltaQty, String operatorName, boolean checkExpiry)
    {
        WmsStock keyQuery = buildKeyQuery(warehouseId, productId, locationId, batchNo);
        for (int attempt = 0; attempt < MAX_OPTIMISTIC_RETRIES; attempt++)
        {
            WmsStock current = wmsStockMapper.selectWmsStockByKey(keyQuery);
            if (current == null)
            {
                throw new ServiceException("库存不存在，商品编号：" + productId + (StringUtils.isNotEmpty(batchNo) ? "，批次：" + batchNo : ""));
            }
            if (checkExpiry && complianceHelper != null)
            {
                complianceHelper.assertBatchNotExpired(current.getExpireDate(), batchNo);
            }
            BigDecimal beforeQty = nvl(current.getQuantity());
            BigDecimal availableQty = beforeQty.subtract(nvl(current.getLockedQuantity())).subtract(nvl(current.getFrozenQuantity()));
            if (availableQty.compareTo(deltaQty) < 0)
            {
                throw new ServiceException("可用库存不足（当前可用：" + availableQty + "，需扣减：" + deltaQty + "，商品编号：" + productId + "）");
            }
            Integer currentVer = current.getVersion() == null ? 0 : current.getVersion();
            int updated = wmsStockMapper.deductAvailableStockAtomically(current.getStockId(), currentVer, deltaQty, operatorName);
            if (updated > 0)
            {
                return new BigDecimal[] { beforeQty, beforeQty.subtract(deltaQty) };
            }
        }
        throw new ServiceException("库存并发扣减冲突，请刷新后重试（商品编号：" + productId + "）");
    }

    @Override
    public void lockStockByKeyAtomically(Long warehouseId, Long productId, Long locationId,
                                         String batchNo, BigDecimal lockQty, String operatorName)
    {
        WmsStock keyQuery = buildKeyQuery(warehouseId, productId, locationId, batchNo);
        for (int attempt = 0; attempt < MAX_OPTIMISTIC_RETRIES; attempt++)
        {
            WmsStock current = wmsStockMapper.selectWmsStockByKey(keyQuery);
            if (current == null)
            {
                throw new ServiceException("库存不存在，商品编号：" + productId + (StringUtils.isNotEmpty(batchNo) ? "，批次：" + batchNo : ""));
            }
            if (complianceHelper != null)
            {
                complianceHelper.assertBatchNotExpired(current.getExpireDate(), batchNo);
            }
            BigDecimal availableQty = nvl(current.getQuantity())
                .subtract(nvl(current.getLockedQuantity()))
                .subtract(nvl(current.getFrozenQuantity()));
            if (availableQty.compareTo(lockQty) < 0)
            {
                throw new ServiceException("可用库存不足以锁定（可用：" + availableQty + "，需锁：" + lockQty + "，商品编号：" + productId + "）");
            }
            Integer currentVer = current.getVersion() == null ? 0 : current.getVersion();
            int updated = wmsStockMapper.lockStockAtomically(current.getStockId(), currentVer, lockQty, operatorName);
            if (updated > 0)
            {
                return;
            }
        }
        throw new ServiceException("库存锁定并发冲突，请刷新后重试（商品编号：" + productId + "）");
    }

    @Override
    public void releaseLockByKeyAtomically(Long warehouseId, Long productId, Long locationId,
                                           String batchNo, BigDecimal releaseQty, String operatorName)
    {
        WmsStock keyQuery = buildKeyQuery(warehouseId, productId, locationId, batchNo);
        for (int attempt = 0; attempt < MAX_OPTIMISTIC_RETRIES; attempt++)
        {
            WmsStock current = wmsStockMapper.selectWmsStockByKey(keyQuery);
            if (current == null)
            {
                return;
            }
            Integer currentVer = current.getVersion() == null ? 0 : current.getVersion();
            int updated = wmsStockMapper.releaseLockAtomically(current.getStockId(), currentVer, releaseQty, operatorName);
            if (updated > 0)
            {
                return;
            }
        }
        throw new ServiceException("库存解锁并发冲突，请刷新后重试");
    }

    @Override
    public BigDecimal[] shipLockedStockByKeyAtomically(Long warehouseId, Long productId, Long locationId,
                                                       String batchNo, BigDecimal shipQty, String operatorName)
    {
        WmsStock keyQuery = buildKeyQuery(warehouseId, productId, locationId, batchNo);
        for (int attempt = 0; attempt < MAX_OPTIMISTIC_RETRIES; attempt++)
        {
            WmsStock current = wmsStockMapper.selectWmsStockByKey(keyQuery);
            if (current == null)
            {
                throw new ServiceException("出库库存批次不存在，商品编号：" + productId + "，批次：" + batchNo);
            }
            if (complianceHelper != null)
            {
                complianceHelper.assertBatchNotExpired(current.getExpireDate(), batchNo);
            }
            BigDecimal beforeQty = nvl(current.getQuantity());
            if (beforeQty.compareTo(shipQty) < 0)
            {
                throw new ServiceException("实物库存不足（当前库存：" + beforeQty + "，本次出库：" + shipQty + "，商品编号：" + productId + "）");
            }
            Integer currentVer = current.getVersion() == null ? 0 : current.getVersion();
            int updated = wmsStockMapper.shipLockedStockAtomically(current.getStockId(), currentVer, shipQty, operatorName);
            if (updated > 0)
            {
                return new BigDecimal[] { beforeQty, beforeQty.subtract(shipQty) };
            }
        }
        throw new ServiceException("出库扣减并发冲突，请刷新后重试（商品编号：" + productId + "）");
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int freezeStock(Long stockId, BigDecimal freezeQty, String reason, String operatorName)
    {
        if (stockId == null || freezeQty == null || freezeQty.compareTo(BigDecimal.ZERO) <= 0)
        {
            throw new ServiceException("请选择库存记录并输入大于 0 的冻结数量");
        }
        if (complianceHelper != null)
        {
            complianceHelper.assertPeriodOpen(new Date(), "冻结库存");
        }
        for (int attempt = 0; attempt < MAX_OPTIMISTIC_RETRIES; attempt++)
        {
            WmsStock current = wmsStockMapper.selectWmsStockById(stockId);
            if (current == null)
            {
                throw new ServiceException("库存记录不存在");
            }
            BigDecimal availableQty = nvl(current.getQuantity())
                .subtract(nvl(current.getLockedQuantity()))
                .subtract(nvl(current.getFrozenQuantity()));
            if (availableQty.compareTo(freezeQty) < 0)
            {
                throw new ServiceException("可用库存不足（当前可用：" + availableQty + "，本次冻结：" + freezeQty + "）");
            }
            Integer currentVer = current.getVersion() == null ? 0 : current.getVersion();
            int updated = wmsStockMapper.freezeStockAtomically(stockId, currentVer, freezeQty, operatorName);
            if (updated > 0)
            {
                if (wmsStockLogMapper != null)
                {
                    WmsStockLog log = new WmsStockLog();
                    log.setWarehouseId(current.getWarehouseId());
                    log.setProductId(current.getProductId());
                    log.setLocationId(current.getLocationId());
                    log.setBatchNo(current.getBatchNo());
                    log.setBillType("stock_freeze");
                    log.setBillId(stockId);
                    log.setBillNo("FRZ-" + stockId + "-" + (System.currentTimeMillis() % 100000));
                    log.setInOut("out");
                    log.setQuantity(freezeQty);
                    log.setPrice(BigDecimal.ZERO);
                    log.setAmount(BigDecimal.ZERO);
                    log.setBeforeQty(nvl(current.getQuantity()));
                    log.setAfterQty(nvl(current.getQuantity()));
                    wmsStockLogMapper.insertWmsStockLog(log);
                }
                return updated;
            }
        }
        throw new ServiceException("库存冻结并发冲突，请刷新后重试");
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int unfreezeStock(Long stockId, BigDecimal unfreezeQty, String reason, String operatorName)
    {
        if (stockId == null || unfreezeQty == null || unfreezeQty.compareTo(BigDecimal.ZERO) <= 0)
        {
            throw new ServiceException("请选择库存记录并输入大于 0 的解冻数量");
        }
        if (complianceHelper != null)
        {
            complianceHelper.assertPeriodOpen(new Date(), "解冻库存");
        }
        for (int attempt = 0; attempt < MAX_OPTIMISTIC_RETRIES; attempt++)
        {
            WmsStock current = wmsStockMapper.selectWmsStockById(stockId);
            if (current == null)
            {
                throw new ServiceException("库存记录不存在");
            }
            BigDecimal frozenQty = nvl(current.getFrozenQuantity());
            if (frozenQty.compareTo(unfreezeQty) < 0)
            {
                throw new ServiceException("冻结数量不足（当前冻结：" + frozenQty + "，本次解冻：" + unfreezeQty + "）");
            }
            Integer currentVer = current.getVersion() == null ? 0 : current.getVersion();
            int updated = wmsStockMapper.unfreezeStockAtomically(stockId, currentVer, unfreezeQty, operatorName);
            if (updated > 0)
            {
                if (wmsStockLogMapper != null)
                {
                    WmsStockLog log = new WmsStockLog();
                    log.setWarehouseId(current.getWarehouseId());
                    log.setProductId(current.getProductId());
                    log.setLocationId(current.getLocationId());
                    log.setBatchNo(current.getBatchNo());
                    log.setBillType("stock_unfreeze");
                    log.setBillId(stockId);
                    log.setBillNo("UFRZ-" + stockId + "-" + (System.currentTimeMillis() % 100000));
                    log.setInOut("in");
                    log.setQuantity(unfreezeQty);
                    log.setPrice(BigDecimal.ZERO);
                    log.setAmount(BigDecimal.ZERO);
                    log.setBeforeQty(nvl(current.getQuantity()));
                    log.setAfterQty(nvl(current.getQuantity()));
                    wmsStockLogMapper.insertWmsStockLog(log);
                }
                return updated;
            }
        }
        throw new ServiceException("库存解冻并发冲突，请刷新后重试");
    }

    @Override
    public int insertWmsStock(WmsStock wmsStock)
    {
        throw new ServiceException("库存台账由入库、出库、盘点、调拨等业务单据自动维护，不支持手工新增");
    }

    @Override
    public int updateWmsStock(WmsStock wmsStock)
    {
        throw new ServiceException("库存台账由入库、出库、盘点、调拨等业务单据自动维护，不支持手工修改");
    }

    @Override
    public int updateWmsStockWarningThreshold(WmsStock wmsStock)
    {
        if (wmsStock == null || wmsStock.getStockId() == null)
        {
            throw new ServiceException("库存记录编号不能为空");
        }
        WmsStock existingStock = wmsStockMapper.selectWmsStockById(wmsStock.getStockId());
        if (existingStock == null)
        {
            throw new ServiceException("库存记录不存在或已被删除");
        }
        BigDecimal minQty = wmsStock.getWarningMinQty();
        BigDecimal maxQty = wmsStock.getWarningMaxQty();
        if (minQty != null && minQty.compareTo(BigDecimal.ZERO) < 0)
        {
            throw new ServiceException("最小预警值不能小于0");
        }
        if (maxQty != null && maxQty.compareTo(BigDecimal.ZERO) < 0)
        {
            throw new ServiceException("最大预警值不能小于0");
        }
        if (minQty != null && maxQty != null && maxQty.compareTo(minQty) < 0)
        {
            throw new ServiceException("最大预警值不能小于最小预警值");
        }
        WmsStock updateParam = new WmsStock();
        updateParam.setStockId(wmsStock.getStockId());
        updateParam.setWarningMinQty(minQty);
        updateParam.setWarningMaxQty(maxQty);
        updateParam.setExpireDate(wmsStock.getExpireDate());
        updateParam.setUpdateBy(wmsStock.getUpdateBy());
        return wmsStockMapper.updateWmsStockWarningThreshold(updateParam);
    }

    @Override
    public int deleteWmsStockById(Long stockId)
    {
        throw new ServiceException("库存台账由业务单据自动维护，不支持手工删除");
    }

    @Override
    public int deleteWmsStockByIds(Long[] stockIds)
    {
        throw new ServiceException("库存台账由业务单据自动维护，不支持手工删除");
    }

    private WmsStock buildKeyQuery(Long warehouseId, Long productId, Long locationId, String batchNo)
    {
        WmsStock q = new WmsStock();
        q.setWarehouseId(warehouseId);
        q.setProductId(productId);
        q.setLocationId(locationId);
        q.setBatchNo(batchNo);
        return q;
    }

    private BigDecimal nvl(BigDecimal val)
    {
        return val == null ? BigDecimal.ZERO : val;
    }
}
