package com.ruoyi.business.mapper;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;
import org.apache.ibatis.annotations.Param;
import com.ruoyi.business.domain.WmsStock;

public interface WmsStockMapper
{
    public WmsStock selectWmsStockById(Long stockId);

    public WmsStock selectWmsStockByKey(WmsStock wmsStock);

    public List<WmsStock> selectWmsStockWarningList(WmsStock wmsStock);

    public Long selectWmsStockWarningCount(WmsStock wmsStock);

    public List<WmsStock> selectWmsStockList(WmsStock wmsStock);

    public WmsStock selectFirstWmsStockByWarehouseAndProduct(@Param("warehouseId") Long warehouseId, @Param("productId") Long productId);

    public List<WmsStock> selectFefoCandidateBatches(@Param("warehouseId") Long warehouseId, @Param("productId") Long productId);

    public BigDecimal selectTotalProductStockQuantity(@Param("productId") Long productId);

    public int insertWmsStock(WmsStock wmsStock);

    public int updateWmsStock(WmsStock wmsStock);

    public int updateWmsStockWarningThreshold(WmsStock wmsStock);

    public int increaseStockAtomically(@Param("stockId") Long stockId,
                                       @Param("version") Integer version,
                                       @Param("deltaQty") BigDecimal deltaQty,
                                       @Param("expireDate") Date expireDate,
                                       @Param("updateBy") String updateBy);

    public int deductAvailableStockAtomically(@Param("stockId") Long stockId,
                                              @Param("version") Integer version,
                                              @Param("deltaQty") BigDecimal deltaQty,
                                              @Param("updateBy") String updateBy);

    public int lockStockAtomically(@Param("stockId") Long stockId,
                                   @Param("version") Integer version,
                                   @Param("lockQty") BigDecimal lockQty,
                                   @Param("updateBy") String updateBy);

    public int releaseLockAtomically(@Param("stockId") Long stockId,
                                     @Param("version") Integer version,
                                     @Param("releaseQty") BigDecimal releaseQty,
                                     @Param("updateBy") String updateBy);

    public int shipLockedStockAtomically(@Param("stockId") Long stockId,
                                         @Param("version") Integer version,
                                         @Param("shipQty") BigDecimal shipQty,
                                         @Param("updateBy") String updateBy);

    public int freezeStockAtomically(@Param("stockId") Long stockId,
                                     @Param("version") Integer version,
                                     @Param("freezeQty") BigDecimal freezeQty,
                                     @Param("updateBy") String updateBy);

    public int unfreezeStockAtomically(@Param("stockId") Long stockId,
                                       @Param("version") Integer version,
                                       @Param("unfreezeQty") BigDecimal unfreezeQty,
                                       @Param("updateBy") String updateBy);

    public int deleteWmsStockById(Long stockId);

    public int deleteWmsStockByIds(Long[] stockIds);
}
