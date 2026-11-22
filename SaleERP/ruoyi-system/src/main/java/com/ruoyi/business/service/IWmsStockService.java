package com.ruoyi.business.service;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;
import com.ruoyi.business.domain.WmsStock;

public interface IWmsStockService
{
    public WmsStock selectWmsStockById(Long stockId);

    public WmsStock selectWmsStockByKey(WmsStock wmsStock);

    public List<WmsStock> selectWmsStockWarningList(WmsStock wmsStock);

    public int pushWarningReminder(String operatorName);

    public List<WmsStock> selectWmsStockList(WmsStock wmsStock);

    public List<WmsStock> recommendFefoBatches(Long warehouseId, Long productId);

    public String importOpeningStock(List<WmsStock> stockList, String operatorName);

    public BigDecimal[] increaseStockByKeyAtomically(Long warehouseId, Long productId, Long locationId,
                                                     String batchNo, Date expireDate, BigDecimal deltaQty, String operatorName);

    public BigDecimal[] deductAvailableStockByKeyAtomically(Long warehouseId, Long productId, Long locationId,
                                                            String batchNo, BigDecimal deltaQty, String operatorName, boolean checkExpiry);

    public void lockStockByKeyAtomically(Long warehouseId, Long productId, Long locationId,
                                         String batchNo, BigDecimal lockQty, String operatorName);

    public void releaseLockByKeyAtomically(Long warehouseId, Long productId, Long locationId,
                                           String batchNo, BigDecimal releaseQty, String operatorName);

    public BigDecimal[] shipLockedStockByKeyAtomically(Long warehouseId, Long productId, Long locationId,
                                                       String batchNo, BigDecimal shipQty, String operatorName);

    public int freezeStock(Long stockId, BigDecimal freezeQty, String reason, String operatorName);

    public int unfreezeStock(Long stockId, BigDecimal unfreezeQty, String reason, String operatorName);

    public int insertWmsStock(WmsStock wmsStock);

    public int updateWmsStock(WmsStock wmsStock);

    public int updateWmsStockWarningThreshold(WmsStock wmsStock);

    public int deleteWmsStockById(Long stockId);

    public int deleteWmsStockByIds(Long[] stockIds);
}
