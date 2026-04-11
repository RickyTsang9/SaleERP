package com.ruoyi.business.service;

import java.util.List;
import com.ruoyi.business.domain.WmsStock;

public interface IWmsStockService
{
    public WmsStock selectWmsStockById(Long stockId);

    public WmsStock selectWmsStockByKey(WmsStock wmsStock);

    public List<WmsStock> selectWmsStockWarningList(WmsStock wmsStock);

    public int pushWarningReminder(String operatorName);

    public List<WmsStock> selectWmsStockList(WmsStock wmsStock);

    public int insertWmsStock(WmsStock wmsStock);

    public int updateWmsStock(WmsStock wmsStock);

    public int deleteWmsStockById(Long stockId);

    public int deleteWmsStockByIds(Long[] stockIds);
}
