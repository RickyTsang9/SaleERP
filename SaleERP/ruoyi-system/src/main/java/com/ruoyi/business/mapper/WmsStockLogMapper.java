package com.ruoyi.business.mapper;

import java.util.List;
import com.ruoyi.business.domain.WmsStockLog;

public interface WmsStockLogMapper
{
    public WmsStockLog selectWmsStockLogById(Long stockLogId);

    public List<WmsStockLog> selectWmsStockLogList(WmsStockLog wmsStockLog);

    public int insertWmsStockLog(WmsStockLog wmsStockLog);

    public int updateWmsStockLog(WmsStockLog wmsStockLog);

    public int deleteWmsStockLogById(Long stockLogId);

    public int deleteWmsStockLogByIds(Long[] stockLogIds);
}
